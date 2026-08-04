const fs = require('node:fs');
const crypto = require('node:crypto');
const path = require('node:path');
const { fileURLToPath } = require('node:url');
const { parse: parseYaml } = require('yaml');
const { assertM41ProductProof } = require('./verify-athena-m41-product-proof.js');

const M41_RELATIVE = path.join('_bmad-output', 'implementation-artifacts', 'm41');
const REQUIRED_EPICS = ['m41-e1', 'm41-e2', 'm41-e3', 'm41-e4'];
const REQUIRED_STORIES = [
    '1-1-derive-deterministic-explainable-placement',
    '1-2-derive-bounds-and-alignment',
    '1-3-derive-grid-reference-facts',
    '2-1-build-anchor-accurate-routes',
    '2-2-build-orthogonal-route-facts-and-lanes',
    '2-3-trace-routes-and-hold-the-no-optimization-boundary',
    '3-1-publish-explicit-geometry-facts',
    '3-2-enforce-geometry-validation',
    '4-1-measure-quality-milestone-facts',
    '4-2-measure-density-and-occupancy-against-the-baseline',
    '5-1-build-the-dedicated-m41-example',
    '5-2-build-the-m41-e2e-evidence',
];
const CLOSURE_STORY = '5-3-close-m41-with-an-honest-retrospective';
const EXPECTED_VIEW = 'schematic';
const EXPECTED_SHEET = 'schematic/sheet/S1';
const EXPECTED_SOURCE_FRAGMENT = 'examples/m41/rolling-shutter/';
const REQUIRED_STATUS_KEYS = [...REQUIRED_EPICS, ...REQUIRED_STORIES, 'm41-e5', 'm41-e5-retrospective', CLOSURE_STORY];
const REQUIRED_VERIFICATION_COMMANDS = [
    ['product-proof-contract', 'node --test scripts/athena-m41-product-proof-contract.test.mjs'],
    ['closure-contract', 'node --test scripts/athena-m41-closure-contract.test.mjs'],
    ['glsp-tests', 'yarn test (integrations/graph-glsp)'],
    ['frontend-tests', 'yarn test (ide/theia-frontend)'],
    ['repository-test', '.\\gradlew.bat --no-daemon --console=plain test'],
    ['lsp-install', '.\\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist'],
    ['ide-build', 'yarn --cwd ide build'],
    ['electron-smoke', 'yarn --cwd ide start:smoke:m41'],
    ['source-set-hygiene', 'powershell -ExecutionPolicy Bypass -File .\\tools\\source-set-hygiene-audit.ps1'],
    ['encoding-audit', 'powershell -ExecutionPolicy Bypass -File .\\tools\\encoding-audit.ps1'],
    ['diff-check', 'git diff --check'],
];
const REQUIRED_METRIC_KEYS = [
    'construct-containment-failure-count',
    'occurrence-overlap-count',
    'peak-routes-per-lane',
    'route-body-intersection-count',
    'route-crossing-count',
    'twist-count',
    'used-lane-count',
    'density',
    'occupancy',
];

function resolveRepoRoot(start = __dirname) {
    let current = path.resolve(start);
    while (path.dirname(current) !== current && !fs.existsSync(path.join(current, 'settings.gradle.kts'))) {
        current = path.dirname(current);
    }
    if (!fs.existsSync(path.join(current, 'settings.gradle.kts'))) {
        throw closureFailure('repository', 'Could not locate Athena repository root.');
    }
    return current;
}

function defaultPaths(repoRoot) {
    const root = path.resolve(repoRoot || resolveRepoRoot());
    const artifactRoot = path.join(root, M41_RELATIVE);
    return {
        repoRoot: root,
        artifactRoot,
        statusPath: path.join(artifactRoot, 'sprint-status.yaml'),
        proofPath: path.join(artifactRoot, 'm41-product-proof.json'),
        baselinePath: path.join(artifactRoot, 'm41-spatial-quality-baseline.properties'),
        verificationPath: path.join(artifactRoot, 'm41-verification-results.json'),
        screenshotRoot: path.join(artifactRoot, 'screenshots'),
        retrospectivePath: path.join(artifactRoot, 'm41-retrospective-2026-08-04.md'),
        storyPath: path.join(artifactRoot, `${CLOSURE_STORY}.md`),
    };
}

function samePath(left, right) {
    const resolvedLeft = path.resolve(left);
    const resolvedRight = path.resolve(right);
    return process.platform === 'win32'
        ? resolvedLeft.toLowerCase() === resolvedRight.toLowerCase()
        : resolvedLeft === resolvedRight;
}

function readDevelopmentStatus(statusPath) {
    if (!fs.existsSync(statusPath)) closureFailure('status', `M41 sprint status missing: ${statusPath}`);
    let document;
    try {
        document = parseYaml(fs.readFileSync(statusPath, 'utf8'));
    } catch (error) {
        closureFailure('status', `M41 sprint status YAML invalid: ${error.message}`);
    }
    const statuses = document?.development_status;
    if (!statuses || typeof statuses !== 'object' || Array.isArray(statuses)) closureFailure('status', `M41 development_status missing: ${statusPath}`);
    for (const [key, value] of Object.entries(statuses)) {
        if (typeof value !== 'string') closureFailure('status', `M41 status ${key} is not a scalar string.`);
    }
    return statuses;
}

function assertPriorStatusesDone(statuses) {
    const pending = [];
    const unexpectedStories = Object.keys(statuses).filter(key => /^\d+-\d+-/.test(key) && !REQUIRED_STATUS_KEYS.includes(key));
    if (unexpectedStories.length > 0) closureFailure('status', `M41 closure found unexpected M41 story keys: ${unexpectedStories.join(', ')}`);
    for (const key of REQUIRED_STATUS_KEYS) {
        if (!Object.prototype.hasOwnProperty.call(statuses, key)) pending.push(`${key}=missing`);
    }
    for (const epic of REQUIRED_EPICS) {
        if (statuses[epic] !== 'done') pending.push(`${epic}=${statuses[epic] || 'missing'}`);
    }
    for (const story of REQUIRED_STORIES) {
        if (statuses[story] !== 'done') pending.push(`${story}=${statuses[story] || 'missing'}`);
    }
    if (!['in-progress', 'review', 'done'].includes(statuses['m41-e5'])) pending.push(`m41-e5=${statuses['m41-e5'] || 'missing'}`);
    if (!['optional', 'done'].includes(statuses['m41-e5-retrospective'])) pending.push(`m41-e5-retrospective=${statuses['m41-e5-retrospective'] || 'missing'}`);
    if (!['ready-for-dev', 'in-progress', 'review', 'done'].includes(statuses[CLOSURE_STORY])) pending.push(`${CLOSURE_STORY}=${statuses[CLOSURE_STORY] || 'missing'}`);
    if (pending.length > 0) {
        closureFailure('status', `M41 closure blocked by unfinished work: ${pending.join(', ')}`);
    }
}

function parseProperties(filePath) {
    if (!fs.existsSync(filePath)) closureFailure('evidence', `M41 baseline missing: ${filePath}`);
    const properties = {};
    for (const line of fs.readFileSync(filePath, 'utf8').split(/\r?\n/)) {
        const match = line.match(/^([^=]+)=(.*)$/);
        if (!match) continue;
        const value = match[2].replace(/\\([\\:=])/g, '$1');
        properties[match[1]] = value !== '' && Number.isFinite(Number(value)) ? Number(value) : value;
    }
    return properties;
}

function camelCase(value) {
    return value.replace(/-([a-z])/g, (_match, letter) => letter.toUpperCase());
}

function baselineFacts(properties) {
    const rect = prefix => ({
        x: properties[`sheet.0.${prefix}.x`],
        y: properties[`sheet.0.${prefix}.y`],
        width: properties[`sheet.0.${prefix}.width`],
        height: properties[`sheet.0.${prefix}.height`],
    });
    const quality = {};
    for (const [key, value] of Object.entries(properties)) {
        const match = key.match(/^sheet\.0\.metric\.([^.]+)\.value$/);
        if (match) quality[camelCase(match[1])] = Number(value);
    }
    for (const kind of ['density', 'occupancy']) {
        const numerator = Number(properties[`sheet.0.metric.${kind}.numerator`]);
        const denominator = Number(properties[`sheet.0.metric.${kind}.denominator`]);
        if (Number.isFinite(numerator) && Number.isFinite(denominator) && denominator !== 0) quality[kind] = numerator / denominator;
    }
    return {
        sheetId: properties['sheet.0.id'],
        extent: rect('extent'),
        drawingArea: rect('drawing-area'),
        counts: {
            occurrences: properties['sheet.0.count.occurrences'],
            regions: properties['sheet.0.count.regions'],
            constructs: properties['sheet.0.count.constructs'],
            anchors: properties['sheet.0.count.anchors'],
            routes: properties['sheet.0.count.routes'],
            lanes: properties['sheet.0.count.used-lanes'],
            gridReferences: properties['sheet.0.count.grid-references'],
        },
        quality,
        ratios: {
            density: `${properties['sheet.0.metric.density.numerator']}/${properties['sheet.0.metric.density.denominator']}`,
            occupancy: `${properties['sheet.0.metric.occupancy.numerator']}/${properties['sheet.0.metric.occupancy.denominator']}`,
        },
        sourceDigest: properties['fixture.source.sha256'],
        generationCommand: properties['generation.command'],
    };
}

function assertBaselineProperties(properties) {
    const required = [
        'fixture.source.path', 'fixture.source.sha256', 'generation.command', 'comparison.m40.comparable',
        'sheet.0.id',
        ...['x', 'y', 'width', 'height'].flatMap(axis => [`sheet.0.extent.${axis}`, `sheet.0.drawing-area.${axis}`]),
        ...['occurrences', 'regions', 'constructs', 'anchors', 'routes', 'used-lanes', 'grid-references'].map(key => `sheet.0.count.${key}`),
    ];
    for (const key of required) {
        if (!Object.prototype.hasOwnProperty.call(properties, key) || String(properties[key]).trim() === '') closureFailure('evidence', `M41 baseline properties missing ${key}.`);
    }
    for (const metric of REQUIRED_METRIC_KEYS) {
        const valueKey = `sheet.0.metric.${metric}.value`;
        const ratioKeys = [`sheet.0.metric.${metric}.numerator`, `sheet.0.metric.${metric}.denominator`];
        const hasValue = Object.prototype.hasOwnProperty.call(properties, valueKey);
        const hasRatio = ratioKeys.every(key => Object.prototype.hasOwnProperty.call(properties, key));
        if (!hasValue && !hasRatio) closureFailure('evidence', `M41 baseline properties missing metric ${metric}.`);
    }
}

function readPngDimensions(filePath) {
    if (!fs.existsSync(filePath)) closureFailure('evidence', `M41 screenshot missing: ${filePath}`);
    const bytes = fs.readFileSync(filePath);
    if (bytes.length < 1024 || bytes.subarray(0, 8).toString('hex') !== '89504e470d0a1a0a'
        || bytes.readUInt32BE(8) !== 13 || bytes.subarray(12, 16).toString('ascii') !== 'IHDR'
        || bytes.subarray(-8, -4).toString('ascii') !== 'IEND') {
        closureFailure('evidence', `M41 screenshot is not a PNG: ${filePath}`);
    }
    return {
        width: bytes.readUInt32BE(16),
        height: bytes.readUInt32BE(20),
        bytes: bytes.length,
        sha256: `sha256:${crypto.createHash('sha256').update(bytes).digest('hex')}`,
    };
}

function readVerificationEvidence(filePath, sourceDigest, screenshots, expectedScreenshotNames, screenshotRoot) {
    if (!fs.existsSync(filePath)) closureFailure('evidence', `M41 verification evidence missing: ${filePath}`);
    let verification;
    try {
        verification = JSON.parse(fs.readFileSync(filePath, 'utf8'));
    } catch (error) {
        closureFailure('evidence', `M41 verification evidence invalid: ${error.message}`);
    }
    if (verification.schemaVersion !== 'M41.verification-results' || verification.sourceSha256 !== sourceDigest) {
        closureFailure('evidence', 'M41 verification evidence schema or source digest is invalid.');
    }
    if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:Z|[+-]\d{2}:\d{2})$/.test(String(verification.generatedAt || ''))) {
        closureFailure('evidence', 'M41 verification evidence timestamp is invalid.');
    }
    for (const [id, command] of REQUIRED_VERIFICATION_COMMANDS) {
        const record = verification.commands?.find(candidate => candidate.id === id);
        if (!record || record.command !== command || record.status !== 'PASS' || !String(record.summary || '').trim()) {
            closureFailure('evidence', `M41 verification command evidence missing or invalid: ${id}.`);
        }
    }
    for (const [viewportName, expected] of Object.entries(expectedScreenshotNames)) {
        const record = verification.screenshots?.find(candidate => candidate.viewportName === viewportName);
        const screenshot = screenshots[viewportName === 'desktop-1920x1080' ? 'desktop' : 'narrow'];
        const expectedPath = path.join(screenshotRoot, expected);
        if (!record || !samePath(record.path, expectedPath) || record.sha256 !== screenshot.dimensions.sha256
            || record.width !== screenshot.dimensions.width || record.height !== screenshot.dimensions.height) {
            closureFailure('evidence', `M41 verification screenshot evidence invalid: ${viewportName}.`);
        }
    }
    return verification;
}

function assertEvidence(paths) {
    if (!fs.existsSync(paths.proofPath)) closureFailure('evidence', `M41 product proof missing: ${paths.proofPath}`);
    let proof;
    try {
        proof = JSON.parse(fs.readFileSync(paths.proofPath, 'utf8'));
    } catch (error) {
        closureFailure('evidence', `M41 product proof invalid JSON: ${error.message}`);
    }
    try {
        assertM41ProductProof(proof);
    } catch (error) {
        if (error && error.failedAuthority) throw error;
        closureFailure('evidence', `M41 product proof invalid: ${error.message}`);
    }
    if (proof.schemaVersion !== 'M41.product-proof') closureFailure('evidence', 'M41 product proof schema is not M41.product-proof.');
    if (!String(proof.sourceUri || '').replace(/\\/g, '/').includes(EXPECTED_SOURCE_FRAGMENT)) {
        closureFailure('evidence', `M41 source is not Golden rolling-shutter fixture: ${proof.sourceUri || '<missing>'}`);
    }
    let sourcePath;
    try {
        sourcePath = fileURLToPath(proof.sourceUri);
    } catch (error) {
        closureFailure('evidence', `M41 source URI invalid: ${error.message}`);
    }
    const expectedSourcePath = path.join(paths.repoRoot, 'examples', 'm41', 'rolling-shutter', 'src', 'com', 'engineeringood', 'm41', 'rollingshutter', '01-rolling-shutter-spatial.athena');
    if (!samePath(sourcePath, expectedSourcePath)) closureFailure('evidence', `M41 source path differs from Golden fixture: ${sourcePath}`);
    const properties = parseProperties(paths.baselinePath);
    assertBaselineProperties(properties);
    const baseline = baselineFacts(properties);
    const sourceDigest = `sha256:${crypto.createHash('sha256').update(fs.readFileSync(sourcePath)).digest('hex')}`;
    if (baseline.sourceDigest !== sourceDigest) closureFailure('evidence', `M41 baseline source digest ${baseline.sourceDigest} differs from ${sourceDigest}.`);
    if (proof.sourceSha256 !== sourceDigest) closureFailure('evidence', `M41 product proof source digest ${proof.sourceSha256 || '<missing>'} differs from ${sourceDigest}.`);
    if (String(properties['comparison.m40.comparable']) !== 'false') closureFailure('evidence', 'M41 baseline incorrectly claims an M40 comparison is comparable.');

    const viewportEvidence = proof.viewports.map(viewport => {
        const sheetId = viewport.projectionReality?.activeSheetId;
        const sheets = viewport.spatialReality?.proof?.sheets || [];
        const sheet = sheets.find(candidate => candidate.sheetId === sheetId);
        if (viewport.activeViewId !== EXPECTED_VIEW || sheetId !== EXPECTED_SHEET || !sheet) {
            closureFailure('evidence', `M41 ${viewport.viewportName} active view/Sheet mismatch: ${viewport.activeViewId || '<missing>'}/${sheetId || '<missing>'}`);
        }
        const activeSheet = viewport.spatialReality?.activeSheet;
        if (JSON.stringify(activeSheet) !== JSON.stringify(sheet)) {
            closureFailure('evidence', `M41 ${viewport.viewportName} active Sheet differs from compiler-backed Spatial proof.`);
        }
        const actualCounts = {
            occurrences: sheet.occurrences?.length,
            regions: sheet.regions?.length,
            constructs: sheet.constructs?.length,
            anchors: sheet.anchors?.length,
            routes: sheet.routes?.length,
            lanes: sheet.lanes?.filter(lane => Array.isArray(lane.routeIds) && lane.routeIds.length > 0).length,
            gridReferences: sheet.gridReferences?.length,
        };
        for (const [key, expected] of Object.entries(baseline.counts)) {
            if (Number(actualCounts[key]) !== Number(expected)) closureFailure('evidence', `M41 ${viewport.viewportName} ${key} count ${actualCounts[key]} differs from baseline ${expected}.`);
        }
        for (const [key, expected] of Object.entries(baseline.quality)) {
            if (sheet.quality?.[key] !== expected) closureFailure('evidence', `M41 ${viewport.viewportName} quality ${key} differs from baseline ${expected}.`);
        }
        if (baseline.sheetId !== sheet.sheetId || JSON.stringify(baseline.extent) !== JSON.stringify(sheet.extent) || JSON.stringify(baseline.drawingArea) !== JSON.stringify(sheet.drawingArea)) {
            closureFailure('evidence', `M41 ${viewport.viewportName} Sheet extent or Drawing Area differs from baseline.`);
        }
        return { viewport, sheet };
    });
    const sheet = viewportEvidence[0].sheet;
    const screenshots = {};
    const expectedScreenshotNames = {
        'desktop-1920x1080': 'm41-rolling-shutter-desktop-1920x1080.png',
        narrow: 'm41-rolling-shutter-narrow.png',
    };
    for (const { viewport } of viewportEvidence) {
        const screenshot = viewport.screenshot;
        const expectedPath = path.join(paths.screenshotRoot, expectedScreenshotNames[viewport.viewportName] || 'invalid');
        if (!samePath(screenshot?.screenshotPath || '', expectedPath)) closureFailure('evidence', `M41 screenshot path is not milestone-local: ${screenshot?.screenshotPath || '<missing>'}`);
        const key = viewport.viewportName === 'desktop-1920x1080' ? 'desktop' : viewport.viewportName;
        screenshots[key] = { screenshot, dimensions: readPngDimensions(screenshot.screenshotPath) };
    }
    const expectedDimensions = { desktop: [1920, 1080], narrow: [720, 900] };
    for (const [viewport, [width, height]] of Object.entries(expectedDimensions)) {
        const screenshot = screenshots[viewport];
        if (!screenshot || screenshot.dimensions.width !== width || screenshot.dimensions.height !== height
            || screenshot.screenshot.pngWidth !== width || screenshot.screenshot.pngHeight !== height
            || screenshot.screenshot.capturedViewportWidth !== width || screenshot.screenshot.capturedViewportHeight !== height) {
            closureFailure('evidence', `M41 ${viewport} screenshot dimensions invalid.`);
        }
    }
    const verification = readVerificationEvidence(paths.verificationPath, sourceDigest, screenshots, expectedScreenshotNames, paths.screenshotRoot);
    return { proof, baseline, sheet, screenshots, verification };
}

function assertM41ClosureReady(options = {}) {
    const defaults = defaultPaths(options.repoRoot);
    const paths = { ...defaults, ...options };
    const storyStatus = readDevelopmentStatus(paths.statusPath);
    assertPriorStatusesDone(storyStatus);
    const evidence = assertEvidence(paths);
    return { storyStatus, ...evidence, paths };
}

function retrospectiveText(report, date = '2026-08-04') {
    const { proof, baseline, sheet, screenshots, verification } = report;
    const desktop = screenshots.desktop.dimensions;
    const narrow = screenshots.narrow.dimensions;
    const desktopScreenshot = screenshots.desktop.screenshot;
    const narrowScreenshot = screenshots.narrow.screenshot;
    const desktopSpan = proof.validationSummary.spatialSpan.find(item => item.viewportName === 'desktop-1920x1080');
    const narrowSpan = proof.validationSummary.spatialSpan.find(item => item.viewportName === 'narrow');
    const desktopPixels = proof.validationSummary.pixelBuckets.find(item => item.viewportName === 'desktop-1920x1080');
    const narrowPixels = proof.validationSummary.pixelBuckets.find(item => item.viewportName === 'narrow');
    const usedLaneCount = sheet.lanes.filter(lane => Array.isArray(lane.routeIds) && lane.routeIds.length > 0).length;
    const verificationLines = verification.commands.map(command => `- ${command.command}: ${command.status} (${command.summary})`);
    return [
        '# M41 Retrospective',
        '',
        `Date: ${date}`,
        'Status: complete',
        '',
        '## Outcome',
        '',
        'M41 establishes compiler-owned Spatial Reality for the Golden rolling-shutter fixture. One',
        'Projection session retains typed Sheet, Occurrence, Region, Construct, Anchor, Route, Lane, Grid',
        'Reference, and quality facts through runtime, LSP, GLSP, and Theia product proof. Product proof',
        'validates exact identity and geometry; it does not claim professional drafting parity.',
        '',
        '## Evidence Ledger',
        '',
        `- Source: ${proof.sourceUri}`,
        `- Source SHA-256: ${proof.sourceSha256}`,
        `- Active view/Sheet: ${proof.viewports[0].activeViewId} / ${sheet.sheetId}`,
        `- Counts: ${sheet.occurrences.length} Occurrences, ${sheet.regions.length} Regions, ${sheet.constructs.length} Constructs, ${sheet.anchors.length} Anchors, ${sheet.routes.length} Routes, ${usedLaneCount} used Lanes, ${sheet.gridReferences.length} Grid References`,
        `- Blocking metrics: overlap ${sheet.quality.occurrenceOverlapCount}, containment failure ${sheet.quality.constructContainmentFailureCount}, Route/body intersection ${sheet.quality.routeBodyIntersectionCount}, twist ${sheet.quality.twistCount}`,
        `- Quality: crossings ${sheet.quality.routeCrossingCount}, peak Routes/Lane ${sheet.quality.peakRoutesPerLane}, Density ${baseline.ratios.density}, Occupancy ${baseline.ratios.occupancy}`,
        `- Spatial occupied span: desktop ${desktopSpan.width}/${sheet.drawingArea.width} = ${desktopSpan.widthRatio} width and ${desktopSpan.height}/${sheet.drawingArea.height} = ${desktopSpan.heightRatio} height; narrow ${narrowSpan.width}/${sheet.drawingArea.width} = ${narrowSpan.widthRatio} width and ${narrowSpan.height}/${sheet.drawingArea.height} = ${narrowSpan.heightRatio} height`,
        `- Pixel buckets: desktop horizontal [${desktopPixels.horizontal.join(', ')}], vertical [${desktopPixels.vertical.join(', ')}]; narrow horizontal [${narrowPixels.horizontal.join(', ')}], vertical [${narrowPixels.vertical.join(', ')}]`,
        `- Rendered DOM geometry: both viewports preserve ${sheet.occurrences.length} exact Component bounds and ${sheet.routes.length} exact Route point lists`,
        `- Baseline source digest: ${baseline.sourceDigest}`,
        `- Baseline generation command: ${baseline.generationCommand}`,
        `- Desktop screenshot: ${desktopScreenshot.screenshotPath} (${desktop.width}x${desktop.height})`,
        `- Narrow screenshot: ${narrowScreenshot.screenshotPath} (${narrow.width}x${narrow.height})`,
        '',
        '## Sequential Verification',
        '',
        ...verificationLines,
        '',
        '## What Improved',
        '',
        '- Placement is distributed across the governed Drawing Area and remains compiler-owned.',
        '- Routes retain exact endpoint Anchors, ordered points, Lane ownership, and Projection identity.',
        '- Presentation coordinates are compared against Spatial coordinates without renderer repair.',
        '- Native pixel buckets reject blank or top-strip-only screenshots.',
        '- Production source-set hygiene removes milestone-named proof authority and stale alternatives.',
        '',
        '## Human Screenshot Review',
        '',
        '- Desktop and narrow views show full two-dimensional Sheet content across all three horizontal and vertical Drawing Area buckets.',
        '- Occurrence bodies and Routes remain visible at both viewport sizes without renderer coordinate repair.',
        '- Labels remain small and overlap at fit-to-sheet zoom; M42 owns labels/styling and M44 owns readability optimization.',
        '- Grid facts use vertical letters and horizontal numbers such as A1 and B3; painted grid chrome remains M42 scope.',
        '',
        '## Honest Boundaries',
        '',
        'M41 does not provide label layout, styling/readability optimization, rendering/export evolution, or',
        'professional routing parity. No QElectroTech or EPLAN parity claim is made.',
        '',
        '- M42: labels, styling, visibility, terminal labels, grid chrome.',
        '- M43: SVG/Theia/PDF/Canvas rendering and Excel export.',
        '- M44: readability optimization and quality target tuning.',
        '- M45: professional routing, bundles/trunks, and multi-Sheet continuation.',
        '',
        '## Process Lessons',
        '',
        '- Runtime evidence must be checked before closure prose is written.',
        '- Counts alone are insufficient; stable identities, ordered coordinates, pixel distribution, and',
        '  exact metrics must be checked together.',
        '- Milestone artifacts stay under the active milestone directory; deferred work remains explicit.',
        '',
        '## Closure Decision',
        '',
        'All M41 stories and Epics are marked done only after this evidence ledger, closure tests, BMad review,',
        'and final audits pass.',
        '',
    ].join('\n');
}

function closureTimestamp(now) {
    if (typeof now === 'string' && now.trim()) {
        if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:Z|[+-]\d{2}:\d{2})$/.test(now.trim()) || Number.isNaN(Date.parse(now.trim()))) {
            closureFailure('status', `Invalid closure timestamp: ${now}`);
        }
        return now.trim();
    }
    if (now instanceof Date) {
        if (Number.isNaN(now.getTime())) closureFailure('status', 'Invalid closure timestamp Date.');
        return now.toISOString();
    }
    const parts = new Intl.DateTimeFormat('en-CA', {
        timeZone: 'Asia/Shanghai',
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', second: '2-digit',
        hourCycle: 'h23',
    }).formatToParts(new Date()).reduce((result, part) => ({ ...result, [part.type]: part.value }), {});
    return `${parts.year}-${parts.month}-${parts.day}T${parts.hour}:${parts.minute}:${parts.second}+08:00`;
}

function readClosureTimestamp(statusText) {
    const comments = [...statusText.matchAll(/^# last_updated:\s*(\S+)\s*$/gm)];
    const fields = [...statusText.matchAll(/^last_updated:\s*(\S+)\s*$/gm)];
    if (comments.length !== 1 || fields.length !== 1 || comments[0][1] !== fields[0][1]) {
        closureFailure('status', 'M41 sprint status requires exactly matching last_updated comment and field.');
    }
    return closureTimestamp(fields[0][1]);
}

function storyStatus(text) {
    const matches = [...text.matchAll(/^Status:\s*([^\s#]+)\s*$/gm)];
    if (matches.length !== 1) closureFailure('status', `M41 Story 5.3 requires exactly one Status line, found ${matches.length}.`);
    return matches[0][1];
}

function assertStoryRecords(text) {
    if (/- \[ \]/.test(text)) closureFailure('review', 'M41 Story 5.3 has an unchecked task or subtask.');
    for (const heading of ['### Debug Log References', '### Completion Notes List', '### File List', '### Change Log']) {
        const match = text.match(new RegExp(`^${heading.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}\\s*$([\\s\\S]*?)(?=^### |^## |(?![\\s\\S]))`, 'm'));
        if (!match || !match[1].trim()) closureFailure('review', `M41 Story 5.3 record section is empty: ${heading}.`);
    }
}

function assertRetrospectivePath(paths, retrospectivePath, storyPath) {
    if (!samePath(path.dirname(retrospectivePath), path.dirname(paths.statusPath))
        || !samePath(path.dirname(retrospectivePath), path.dirname(storyPath))) {
        closureFailure('path', 'M41 retrospective must remain in the same M41 artifact directory as status and story.');
    }
}

function replaceRequiredStatus(statusText, key) {
    const pattern = new RegExp(`^(\\s{2}${key}:\\s*)[^\\s#]+(\\s*)$`, 'm');
    if (!pattern.test(statusText)) closureFailure('status', `M41 required status key missing: ${key}.`);
    return statusText.replace(pattern, '$1done$2');
}

function atomicWriteSet(entries) {
    const token = `${process.pid}-${Date.now()}`;
    const staged = entries.map(entry => ({
        ...entry,
        temp: `${entry.target}.${token}.tmp`,
        backup: `${entry.target}.${token}.bak`,
        existed: fs.existsSync(entry.target),
    }));
    try {
        for (const entry of staged) fs.writeFileSync(entry.temp, entry.content, 'utf8');
        for (const entry of staged) if (entry.existed) fs.copyFileSync(entry.target, entry.backup);
        for (const entry of staged) fs.renameSync(entry.temp, entry.target);
    } catch (error) {
        for (const entry of staged) {
            try {
                if (entry.existed && fs.existsSync(entry.backup)) fs.copyFileSync(entry.backup, entry.target);
                else if (!entry.existed && fs.existsSync(entry.target)) fs.rmSync(entry.target, { force: true });
            } catch (_rollbackError) { /* preserve original failure */ }
        }
        closureFailure('write', `M41 closure write rolled back: ${error.message}`);
    } finally {
        for (const entry of staged) {
            fs.rmSync(entry.temp, { force: true });
            fs.rmSync(entry.backup, { force: true });
        }
    }
}

function closeM41(options = {}) {
    const report = assertM41ClosureReady(options);
    const paths = report.paths;
    const retrospectivePath = options.retrospectivePath || paths.retrospectivePath;
    if (options.write !== true) return report;
    assertRetrospectivePath(paths, retrospectivePath, paths.storyPath);
    const closureStatus = report.storyStatus[CLOSURE_STORY];
    if (!['review', 'done'].includes(closureStatus)) closureFailure('review', `M41 final write requires Story 5.3 review status, got ${closureStatus}.`);
    if (!fs.existsSync(paths.storyPath)) closureFailure('status', `M41 Story 5.3 file missing: ${paths.storyPath}`);
    const currentStoryText = fs.readFileSync(paths.storyPath, 'utf8');
    const storyFileStatus = storyStatus(currentStoryText);
    if (!['review', 'done'].includes(storyFileStatus)) closureFailure('review', `M41 Story 5.3 file requires review status, got ${storyFileStatus || 'missing'}.`);
    assertStoryRecords(currentStoryText);
    if (closureStatus === 'done' && storyFileStatus === 'done') {
        if (!fs.existsSync(retrospectivePath)) closureFailure('evidence', `M41 retrospective missing after closure: ${retrospectivePath}`);
        const existingTimestamp = readClosureTimestamp(fs.readFileSync(paths.statusPath, 'utf8'));
        const expectedRetrospective = retrospectiveText(report, existingTimestamp.slice(0, 10));
        if (fs.readFileSync(retrospectivePath, 'utf8') !== expectedRetrospective) closureFailure('evidence', 'M41 completed retrospective differs from validated evidence.');
        return { ...report, retrospectivePath };
    }
    const timestamp = closureTimestamp(options.now);
    const completedRetrospective = retrospectiveText(report, timestamp.slice(0, 10));
    const completedStoryText = currentStoryText.replace(/^Status:\s*[^\s#]+\s*$/m, 'Status: done');
    let statusText = fs.readFileSync(paths.statusPath, 'utf8');
    for (const key of [...REQUIRED_EPICS, 'm41-e5', 'm41-e5-retrospective', ...REQUIRED_STORIES, CLOSURE_STORY]) statusText = replaceRequiredStatus(statusText, key);
    if (!/^# last_updated:/m.test(statusText) || !/^last_updated:/m.test(statusText)) closureFailure('status', 'M41 sprint status last_updated fields missing.');
    statusText = statusText.replace(/^# last_updated:.*$/m, `# last_updated: ${timestamp}`);
    statusText = statusText.replace(/^last_updated:.*$/m, `last_updated: ${timestamp}`);
    atomicWriteSet([
        { target: retrospectivePath, content: completedRetrospective },
        { target: paths.statusPath, content: statusText },
        { target: paths.storyPath, content: completedStoryText },
    ]);
    return { ...report, retrospectivePath };
}

function closureFailure(failedAuthority, message) {
    const error = new Error(`${message}\nfailedAuthority=${failedAuthority}`);
    error.failedAuthority = failedAuthority;
    throw error;
}

if (require.main === module) {
    try {
        const report = closeM41({ write: process.argv.includes('--write') });
        console.log(JSON.stringify({ status: process.argv.includes('--write') ? 'complete' : 'ready', retrospectivePath: report.retrospectivePath || null }, null, 2));
    } catch (error) {
        console.error(error.stack || String(error));
        process.exitCode = 1;
    }
}

module.exports = { assertM41ClosureReady, closeM41, readDevelopmentStatus, retrospectiveText };
