const fs = require('node:fs');
const path = require('node:path');
const { spawn } = require('node:child_process');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_WORKSPACE_OPENED_SENTINEL = 'ATHENA_WORKSPACE_OPENED=';
const ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL = 'ATHENA_WORKSPACE_OPEN_FAILURE=';
const ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_PROOF=';
const ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_SCREENSHOT=';
const ATHENA_CABINET_PRODUCT_PROOF_SENTINEL = 'ATHENA_M36_CONNECTIVITY_CABINET_PRODUCT_PROOF=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME=';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED=';
const STARTUP_TIMEOUT_MS = 120000;
const SOURCE_RELATIVE = 'src/com/engineeringood/m36/connectivitycabinet/01-connectivity-cabinet.athena';
const REQUIRED_SAMPLE_IDENTITIES = [
    'component:Supply35',
    'component:BackupSupply35',
    'component:BreakerQF35',
    'component:BreakerQF36',
    'component:BreakerQF37',
    'component:BreakerQF38',
    'component:RelayK35',
    'component:RelayK36',
    'component:RelayK37',
    'component:RelayK38',
    'component:PFEA112',
    'component:TerminalXT35',
    'component:TerminalXT36',
    'component:TerminalXT37',
    'component:TerminalXT38',
    'component:TerminalXT39',
    'component:TerminalXT40',
    'component:ProtectiveEarthPE35',
    'component:PowerJunction35',
    'component:ControlJunction35',
    'component:FeedbackJunction35',
];
const EXPECTED_ROUTE_COUNT = 31;

async function main() {
    const repositoryRoot = resolveM36SampleProject();
    const screenshotPaths = resolveM36ScreenshotPaths();
    assertInstalledLspHostPresent();

    const smokes = [
        await runElectronSmoke(repositoryRoot, screenshotPaths.desktop1920, 'desktop-1920x1080', 1920, 1080, false),
        await runElectronSmoke(repositoryRoot, screenshotPaths.desktop1280, 'desktop-1280x900', 1280, 900, false),
        await runElectronSmoke(repositoryRoot, screenshotPaths.narrow, 'narrow', 720, 900, false),
    ];
    const screenshotEvidenceProof = buildScreenshotEvidenceProof(smokes);
    assertScreenshotEvidenceProof(screenshotEvidenceProof);
    const cabinetProductProof = buildCabinetProductProof(repositoryRoot, smokes[0].graphWorkbenchProof, screenshotEvidenceProof);
    assertCabinetProductProof(cabinetProductProof);

    console.log(`${ATHENA_CABINET_PRODUCT_PROOF_SENTINEL}${JSON.stringify(cabinetProductProof)}`);
    console.log(`${ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL}${JSON.stringify(smokes[0].graphWorkbenchProof)}`);
    console.log(`Athena M36 connectivity Cabinet product smoke passed. workspace=${smokes[0].openedWorkspace} screenshots=${Object.values(screenshotPaths).join(',')}`);
}

async function runElectronSmoke(repositoryRoot, screenshotPath, viewportName, windowWidth, windowHeight, openSourceForDiagnostics) {
    const electronBinary = require('electron');
    const entryScript = path.resolve(__dirname, 'athena-electron-open-workspace-main.js');
    const child = spawn(electronBinary, [entryScript, repositoryRoot, '--active-view', 'cabinet'], {
        cwd: path.resolve(__dirname, '..'),
            env: {
                ...process.env,
            ATHENA_ELECTRON_SMOKE_EXIT_ON_WORKSPACE_OPEN: '1',
            ATHENA_ELECTRON_SMOKE_SKIP_OUTLINE: '1',
            ATHENA_ELECTRON_SMOKE_OPEN_SOURCE_FOR_DIAGNOSTICS: openSourceForDiagnostics ? '1' : '0',
                ATHENA_ELECTRON_SMOKE_OUTLINE_SOURCE_RELATIVE: SOURCE_RELATIVE,
            ATHENA_ELECTRON_SMOKE_ACTIVE_VIEW: 'cabinet',
            ATHENA_ELECTRON_SMOKE_EXPECTED_PRODUCT_SURFACE_ID: 'cabinet',
            ATHENA_ELECTRON_SMOKE_EXPECTED_PRODUCT_SURFACE_LABEL: 'Cabinet',
            ATHENA_ELECTRON_SMOKE_EXPECTED_BACKING_VIEW_ID: 'cabinet',
            ATHENA_ELECTRON_GRAPH_VIEW_SCREENSHOT: screenshotPath,
            ATHENA_ELECTRON_SMOKE_WINDOW_WIDTH: String(windowWidth),
            ATHENA_ELECTRON_SMOKE_WINDOW_HEIGHT: String(windowHeight),
            ATHENA_ELECTRON_TEMP_USER_DATA: '1',
            ELECTRON_ENABLE_LOGGING: '1',
        },
        stdio: ['ignore', 'pipe', 'pipe'],
        windowsHide: true,
    });

    let sawWindowCreated = false;
    let sawReady = false;
    let openedWorkspace;
    let graphWorkbenchProof;
    let capturedScreenshotPath;
    let resolvedJavaHome;
    let unresolvedJavaSignal;
    let zeroDiagnosticPublication = false;
    const outputLines = [];

    const recordLine = line => {
        const trimmedLine = line.trim();
        if (!trimmedLine) return;
        outputLines.push(trimmedLine);
        if (trimmedLine === ATHENA_WINDOW_CREATED_SENTINEL) sawWindowCreated = true;
        if (trimmedLine === ATHENA_READY_SENTINEL) sawReady = true;
        if (trimmedLine.startsWith(ATHENA_WORKSPACE_OPENED_SENTINEL)) openedWorkspace = trimmedLine.substring(ATHENA_WORKSPACE_OPENED_SENTINEL.length);
        if (trimmedLine.startsWith(ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL)) graphWorkbenchProof = JSON.parse(trimmedLine.substring(ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL.length));
        if (trimmedLine.startsWith(ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL)) capturedScreenshotPath = trimmedLine.substring(ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL.length);
        if (trimmedLine.startsWith(ATHENA_JAVA_SENTINEL)) resolvedJavaHome = trimmedLine.substring(ATHENA_JAVA_SENTINEL.length);
        if (trimmedLine.startsWith(ATHENA_JAVA_UNRESOLVED_SENTINEL)) unresolvedJavaSignal = trimmedLine.substring(ATHENA_JAVA_UNRESOLVED_SENTINEL.length);
        if (hasZeroDiagnosticPublication([trimmedLine])) zeroDiagnosticPublication = true;
    };

    child.stdout.setEncoding('utf8');
    child.stderr.setEncoding('utf8');
    child.stdout.on('data', chunk => chunk.split(/\r?\n/).forEach(recordLine));
    child.stderr.on('data', chunk => chunk.split(/\r?\n/).forEach(recordLine));

    const timeoutHandle = setTimeout(() => child.kill(), STARTUP_TIMEOUT_MS);
    const exitCode = await new Promise(resolveExit => {
        child.on('exit', code => resolveExit(code ?? -1));
        child.on('error', () => resolveExit(-1));
    });
    clearTimeout(timeoutHandle);

    if (!sawWindowCreated || !sawReady || exitCode !== 0) {
        fail('electron-runtime', `M36 smoke failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}`, outputLines);
    }
    if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
        fail('java-runtime', `M36 smoke did not resolve Java 25: ${unresolvedJavaSignal}`);
    }
    if (openedWorkspace !== repositoryRoot) {
        const failureLine = outputLines.find(line => line.startsWith(ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL));
        fail('repository-contract', `Opened ${openedWorkspace || '<missing>'} instead of ${repositoryRoot}.${failureLine ? `\n${failureLine}` : ''}`, outputLines);
    }
    if (!graphWorkbenchProof) {
        fail('graph-workbench-proof', 'M36 smoke did not report graph workbench proof.', outputLines);
    }
    if (
        openSourceForDiagnostics &&
        !hasZeroProblemMarkerProof(graphWorkbenchProof?.sourceDiagnosticActivationProof) &&
        !zeroDiagnosticPublication &&
        !hasZeroDiagnosticPublication(outputLines)
    ) {
        fail('lsp-diagnostics', `M36 smoke did not prove zero LSP diagnostics for ${SOURCE_RELATIVE}.`, outputLines);
    }
    return { viewportName, windowWidth, windowHeight, screenshotPath, openedWorkspace, graphWorkbenchProof, capturedScreenshotPath };
}

function buildCabinetProductProof(repositoryRoot, graphWorkbenchProof, screenshotEvidenceProof) {
    return {
        schemaVersion: 'm36.connectivity-cabinet-product.v1',
        sourceUri: pathToFileUri(path.join(repositoryRoot, SOURCE_RELATIVE)),
        activeViewId: graphWorkbenchProof?.activeViewId || '',
        projectionViewProof: graphWorkbenchProof?.projectionViewProof,
        drawingLayerProof: graphWorkbenchProof?.drawingLayerProof,
        representationProof: graphWorkbenchProof?.representationProof,
        routeProof: graphWorkbenchProof?.routeProof,
        visualProof: graphWorkbenchProof?.visualProof,
        traceProof: buildTraceProof(graphWorkbenchProof),
        cabinetAuthorityProof: buildCabinetAuthorityProof(repositoryRoot, graphWorkbenchProof),
        sourceDiagnosticActivationProof: graphWorkbenchProof?.sourceDiagnosticActivationProof,
        screenshotEvidenceProof,
    };
}

function assertCabinetProductProof(proof) {
    requireValue(proof.activeViewId === 'cabinet', 'projection-authority', `Expected Cabinet view, got ${proof.activeViewId || '<missing>'}.`, proof);
    assertProjectionViewProof(proof.projectionViewProof);
    assertDrawingLayerProof(proof.drawingLayerProof);
    assertRepresentationProof(proof.representationProof);
    assertRouteProof(proof.routeProof);
    assertVisualProof(proof.visualProof);
    assertTraceProof(proof.traceProof);
    assertCabinetAuthorityProof(proof.cabinetAuthorityProof);
    assertSourceDiagnosticActivationProof(proof.sourceDiagnosticActivationProof);
    assertScreenshotEvidenceProof(proof.screenshotEvidenceProof);
}

function buildTraceProof(graphWorkbenchProof) {
    const representationProof = graphWorkbenchProof?.representationProof || {};
    const semanticIds = new Set(normalizeArray(representationProof.semanticIds));
    const graphicOccurrenceStates = normalizeArray(representationProof.graphicOccurrenceStates);
    const missingRequiredIdentities = REQUIRED_SAMPLE_IDENTITIES.filter(identity => !semanticIds.has(identity));
    return {
        schemaVersion: 'm36.trace-proof.v1',
        requiredIdentitiesPresent: missingRequiredIdentities.length === 0,
        missingRequiredIdentities,
        graphicOccurrenceCount: graphicOccurrenceStates.length,
        tracedGraphicOccurrenceCount: graphicOccurrenceStates.filter(occurrence =>
            occurrence.occurrenceId &&
            occurrence.definitionId &&
            occurrence.packageId &&
            occurrence.bindingRuleId &&
            occurrence.physicalComponentId &&
            occurrence.semanticId &&
            occurrence.graphicAuthority &&
            occurrence.placementAuthority &&
            occurrence.materialAuthority
        ).length,
    };
}

function assertTraceProof(proof) {
    requireValue(proof, 'trace-authority', 'Missing M36 trace proof.');
    requireValue(proof.requiredIdentitiesPresent === true, 'trace-authority', 'Rendered sample identities do not match M36 sample.', proof);
    requireValue(Number(proof.graphicOccurrenceCount) >= REQUIRED_SAMPLE_IDENTITIES.length, 'trace-authority', 'Missing graphic occurrence traces.', proof);
    requireValue(Number(proof.tracedGraphicOccurrenceCount) === Number(proof.graphicOccurrenceCount), 'trace-authority', 'Some graphic occurrences lack governed trace.', proof);
}

function buildCabinetAuthorityProof(repositoryRoot, graphWorkbenchProof) {
    const packageFiles = listFiles(repositoryRoot);
    const xmlFiles = packageFiles.filter(file => file.toLowerCase().endsWith('.xml'));
    const htmlFiles = packageFiles.filter(file => file.toLowerCase().endsWith('.html'));
    const athenaRepresentationSources = packageFiles.filter(file => file.startsWith('packages/representation') && file.endsWith('.athena')).sort();
    const governedSvgResources = packageFiles.filter(file => file.startsWith('packages/representation') && file.endsWith('.svg')).sort();
    return {
        schemaVersion: 'm36.cabinet-authority-proof.v1',
        athenaRepresentationSourceCount: athenaRepresentationSources.length,
        governedSvgResourceCount: governedSvgResources.length,
        xmlRuntimeAuthorityAbsent: xmlFiles.length === 0,
        rawMarkupAuthorityAbsent: htmlFiles.length === 0,
        fallbackAuthorityAbsent: normalizeArray(graphWorkbenchProof?.representationProof?.fallbackRepresentationIds).length === 0,
        physicalSourcePresent: packageFiles.includes(SOURCE_RELATIVE),
        packageDescriptorPresent: packageFiles.includes('athena.yaml'),
        packageLockPresent: packageFiles.includes('athena.lock'),
        xmlFiles,
        htmlFiles,
        athenaRepresentationSources,
        governedSvgResources,
    };
}

function assertCabinetAuthorityProof(proof) {
    requireValue(proof, 'cabinet-authority', 'Missing M36 Cabinet authority proof.');
    requireValue(proof.xmlRuntimeAuthorityAbsent === true, 'cabinet-authority', 'M36 sample still contains XML authority.', proof);
    requireValue(proof.rawMarkupAuthorityAbsent === true, 'cabinet-authority', 'M36 sample still contains raw HTML/QET authority.', proof);
    requireValue(proof.fallbackAuthorityAbsent === true, 'cabinet-authority', 'M36 runtime accepted fallback authority.', proof);
    requireValue(proof.physicalSourcePresent === true, 'cabinet-authority', 'M36 connectivity source missing.', proof);
    requireValue(proof.packageDescriptorPresent === true, 'cabinet-authority', 'M36 package descriptor missing.', proof);
    requireValue(proof.packageLockPresent === true, 'cabinet-authority', 'M36 package lock missing.', proof);
    requireValue(Number(proof.athenaRepresentationSourceCount) >= 3, 'cabinet-authority', 'M36 requires governed Athena representation sources.', proof);
    requireValue(Number(proof.governedSvgResourceCount) >= 1, 'cabinet-authority', 'M36 vendor SVG resource is missing.', proof);
}

function buildScreenshotEvidenceProof(smokes) {
    return {
        schemaVersion: 'm36.screenshot-evidence.v1',
        screenshots: smokes.map(smoke => {
            if (smoke.capturedScreenshotPath !== smoke.screenshotPath) {
                fail('electron-screenshot', `Expected screenshot ${smoke.screenshotPath}, got ${smoke.capturedScreenshotPath || '<missing>'}.`);
            }
            const png = assertPngScreenshot(smoke.screenshotPath);
            const graphProof = smoke.graphWorkbenchProof || {};
            const visualProof = graphProof.visualProof || {};
            const drawingLayerProof = graphProof.drawingLayerProof || {};
            const routeProof = graphProof.routeProof || {};
            const representationProof = graphProof.representationProof || {};
            return {
                viewportName: smoke.viewportName,
                windowWidth: smoke.windowWidth,
                windowHeight: smoke.windowHeight,
                screenshotPath: smoke.screenshotPath,
                pngWidth: png.width,
                pngHeight: png.height,
                pngBytes: png.bytes,
                activeViewId: graphProof.activeViewId || '',
                usableViewportWidth: Number(visualProof.usableViewportWidth || 0),
                viewBoxWidth: Number(visualProof.viewBoxWidth || 0),
                viewBoxHeight: Number(visualProof.viewBoxHeight || 0),
                canvasNonblank: Number(drawingLayerProof.visibleItemCount || 0) > 0 &&
                    Number(routeProof.routeCount || 0) > 0 &&
                    Number(representationProof.representationCount || 0) > 0,
                framedCorrectly: graphProof.sheetFrame === true &&
                    Number(visualProof.viewBoxWidth || 0) > 0 &&
                    Number(visualProof.viewBoxHeight || 0) > 0,
                requiredBodyIntersectionsAbsent: Number(visualProof.routeBodyIntersectionCount || 0) === 0,
            };
        }),
    };
}

function assertScreenshotEvidenceProof(proof) {
    requireValue(proof, 'electron-screenshot', 'Missing screenshot evidence proof.');
    const screenshots = normalizeArray(proof.screenshots);
    requireValue(screenshots.length === 3, 'electron-screenshot', 'Expected three M36 screenshot evidence records.', proof);
    const byName = new Map(screenshots.map(item => [item.viewportName, item]));
    for (const viewportName of ['desktop-1920x1080', 'desktop-1280x900', 'narrow']) {
        const screenshot = byName.get(viewportName);
        requireValue(screenshot, 'electron-screenshot', `Missing ${viewportName} screenshot evidence.`, proof);
        requireValue(screenshot.activeViewId === 'cabinet', 'electron-screenshot', `${viewportName} screenshot is not Cabinet.`, screenshot);
        requireValue(Number(screenshot.pngWidth) >= 320 && Number(screenshot.pngHeight) >= 240, 'electron-screenshot', `${viewportName} screenshot dimensions are too small.`, screenshot);
        requireValue(screenshot.canvasNonblank === true, 'electron-screenshot', `${viewportName} Cabinet canvas is blank.`, screenshot);
        requireValue(screenshot.framedCorrectly === true, 'electron-screenshot', `${viewportName} Cabinet frame/viewBox evidence failed.`, screenshot);
        requireValue(screenshot.requiredBodyIntersectionsAbsent === true, 'electron-screenshot', `${viewportName} Cabinet routes cross required component bodies.`, screenshot);
    }
    requireValue(
        Number(byName.get('desktop-1920x1080')?.usableViewportWidth || 0) > Number(byName.get('desktop-1280x900')?.usableViewportWidth || 0) &&
            Number(byName.get('desktop-1280x900')?.usableViewportWidth || 0) > Number(byName.get('narrow')?.usableViewportWidth || 0),
        'electron-screenshot',
        'Viewport evidence did not prove three distinct widths.',
        proof,
    );
}

function assertProjectionViewProof(proof) {
    requireValue(proof, 'projection-navigation', 'Missing Cabinet navigation proof.');
    requireValue(normalizeArray(proof.visibleProductSurfaceIds).join(',') === 'cabinet', 'projection-navigation', 'Cabinet must be the only visible product surface.', proof);
    requireValue(normalizeArray(proof.visibleViewIds).join(',') === 'cabinet', 'projection-navigation', 'Cabinet must be the only visible backing view.', proof);
    requireValue(proof.primaryLabelMatched === true, 'projection-navigation', 'Cabinet product surface label did not match.', proof);
    requireValue(proof.cabinetRefreshAccepted === true && proof.cabinetActiveAfterRefresh === true, 'projection-navigation', 'Cabinet did not survive refresh.', proof);
}

function assertDrawingLayerProof(proof) {
    requireValue(proof, 'drawing-composition', 'Missing live drawing layer proof.');
    requireValue(Number(proof.visibleItemCount) > 0, 'drawing-composition', 'Drawing layer items are not visibly rendered.', proof);
    requireValue(Number(proof.wrapperBorderCount) === 0, 'interaction-hitbox', 'Normal Cabinet component wrapper borders are visible.', proof);
}

function assertRepresentationProof(proof) {
    requireValue(proof && proof.representationCount > 0, 'representation-binding', 'Missing representation facts.', proof);
    requireValue(proof.presentationLabelCount > 0, 'representation-binding', 'Missing presentation labels.', proof);
    requireValue(normalizeArray(proof.fallbackRepresentationIds).length === 0, 'representation-binding', 'Generic fallback representations used.', proof);
    requireValue(duplicateValues(proof.semanticIds).length === 0, 'representation-binding', 'Duplicate semantic representation occurrences rendered.', proof);
    requireValue(normalizeArray(proof.graphicResourceIds).some(id => id.includes('PFEA112')), 'representation-binding', 'Vendor SVG-backed element was not rendered.', proof);
}

function assertRouteProof(proof) {
    requireValue(proof && proof.routeCount > 0, 'route-anchors', 'Missing route proof.', proof);
    requireValue(Number(proof.routeCount) === EXPECTED_ROUTE_COUNT, 'route-connectivity', `Expected ${EXPECTED_ROUTE_COUNT} compiled M36 routes.`, proof);
    requireValue(proof.routesWithTerminalAnchors >= proof.routeCount, 'route-anchors', 'Routes without terminal anchors rendered.', proof);
    requireValue(proof.routesWithOrthogonalBends >= proof.routeCount, 'route-anchors', 'Routes without governed orthogonal channel bends rendered.', proof);
    requireValue(normalizeArray(proof.centerFallbackRouteIds).length === 0, 'route-anchors', 'Center-anchor fallback routes rendered.', proof);
}

function assertVisualProof(proof) {
    requireValue(proof, 'visual-bounds', 'Missing visual proof.');
    requireValue(Number(proof.viewBoxWidth) > 0 && Number(proof.viewBoxHeight) > 0, 'visual-bounds', 'Invalid viewBox dimensions.', proof);
    requireValue(Number(proof.routeBodyIntersectionCount) === 0, 'visual-bounds', 'Routes intersect component bodies.', proof);
    requireValue(Number(proof.nonOrthogonalSegmentCount ?? 0) === 0, 'visual-bounds', 'Non-orthogonal route segments rendered.', proof);
    requireValue(duplicateValues(proof.nodeSemanticIds).length === 0, 'visual-bounds', 'Duplicate/off-screen semantic nodes rendered.', proof);
}

function assertSourceDiagnosticActivationProof(proof) {
    requireValue(proof, 'lsp-diagnostics', 'Missing source diagnostic activation proof.');
    requireValue(
        proof.requested === true || proof.skipped === true,
        'lsp-diagnostics',
        'M36 smoke did not request or skip source diagnostic activation deterministically.',
        proof,
    );
    if (proof.requested === true) {
        requireValue(proof.opened === true, 'lsp-diagnostics', 'M36 smoke did not open the governed Athena source editor.', proof);
        requireValue(proof.problemMarkerCount === 0, 'lsp-diagnostics', 'M36 source has LSP problem markers.', proof);
        requireValue(proof.zeroProblemMarkerCount === true, 'lsp-diagnostics', 'M36 source did not prove zero problem markers.', proof);
    }
}

function resolveM36SampleProject() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm36', 'connectivity-cabinet');
    const requiredFiles = [
        'athena.yaml',
        'athena.lock',
        SOURCE_RELATIVE,
        path.join('packages', 'representation', 'com', 'engineeringood', 'm36', 'control', 'cabinet-bindings.athena'),
        path.join('packages', 'representation', 'com', 'engineeringood', 'm36', 'standard', 'iec', 'standard-elements.athena'),
        path.join('packages', 'representation', 'com', 'engineeringood', 'm36', 'vendor', 'abb', 'pfea112', 'vendor-elements.athena'),
        path.join('packages', 'representation', 'com', 'engineeringood', 'm36', 'vendor', 'abb', 'pfea112', 'pfea112.svg'),
    ];
    const missing = requiredFiles.filter(filePath => !fs.existsSync(path.join(repositoryRoot, filePath)));
    if (missing.length > 0) {
        fail('repository-contract', `M36 connectivity Cabinet project missing: ${missing.join(', ')}`);
    }
    return repositoryRoot;
}

function resolveM36ScreenshotPaths() {
    const screenshotRoot = path.resolve(resolveRepoRoot(), '_bmad-output', 'implementation-artifacts', 'm36', 'screenshots');
    fs.mkdirSync(screenshotRoot, { recursive: true });
    return {
        desktop1920: path.join(screenshotRoot, 'm36-connectivity-cabinet-desktop-1920x1080.png'),
        desktop1280: path.join(screenshotRoot, 'm36-connectivity-cabinet-desktop-1280x900.png'),
        narrow: path.join(screenshotRoot, 'm36-connectivity-cabinet-narrow.png'),
    };
}

function assertPngScreenshot(screenshotPath) {
    if (!fs.existsSync(screenshotPath)) fail('electron-screenshot', `Screenshot missing: ${screenshotPath}`);
    const bytes = fs.readFileSync(screenshotPath);
    if (bytes.length < 1024 || bytes.subarray(0, 8).toString('hex') !== '89504e470d0a1a0a') {
        fail('electron-screenshot', `Invalid PNG screenshot: ${screenshotPath}`);
    }
    return { bytes: bytes.length, width: bytes.readUInt32BE(16), height: bytes.readUInt32BE(20) };
}

function assertInstalledLspHostPresent() {
    const launcher = path.resolve(__dirname, '..', '..', 'lsp', 'build', 'install', 'athena-lsp-host', 'bin', process.platform === 'win32' ? 'athena-lsp-host.bat' : 'athena-lsp-host');
    if (!fs.existsSync(launcher)) {
        fail('lsp-installation', `M36 smoke requires installed LSP host. Missing ${launcher}. Run sequentially: .\\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`);
    }
}

function listFiles(root) {
    const output = [];
    const visit = directory => {
        for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
            const fullPath = path.join(directory, entry.name);
            if (entry.isDirectory()) visit(fullPath);
            if (entry.isFile()) output.push(path.relative(root, fullPath));
        }
    };
    visit(root);
    return output.map(file => file.replace(/\\/g, '/')).sort();
}

function resolveRepoRoot() {
    let current = path.resolve(__dirname);
    while (path.dirname(current) !== current && !fs.existsSync(path.join(current, 'settings.gradle.kts'))) current = path.dirname(current);
    if (!fs.existsSync(path.join(current, 'settings.gradle.kts'))) fail('repository-contract', 'Could not locate Athena repository root.');
    return current;
}

function pathToFileUri(filePath) {
    return `file:///${path.resolve(filePath).replace(/\\/g, '/').replace(/^\/?([A-Za-z]:)/, '$1')}`;
}

function normalizeArray(value) {
    return Array.isArray(value) ? value : [];
}

function hasZeroDiagnosticPublication(lines) {
    const normalizedText = normalizeArray(lines)
        .map(line => String(line))
        .join('\n')
        .replace(/%3A/ig, ':')
        .replace(/\\/g, '/')
        .toLowerCase();
    const compactText = normalizedText.replace(/\s+/g, '');
    const compactSourceNeedle = SOURCE_RELATIVE.replaceAll('\\', '/').toLowerCase().replace(/\s+/g, '');
    return normalizedText.includes('published 0 diagnostic(s)') &&
        (normalizedText.includes(SOURCE_RELATIVE.replaceAll('\\', '/').toLowerCase()) || compactText.includes(compactSourceNeedle));
}

function hasZeroProblemMarkerProof(proof) {
    return proof?.requested === true &&
        proof.opened === true &&
        proof.problemMarkerCount === 0 &&
        proof.zeroProblemMarkerCount === true;
}

function duplicateValues(value) {
    const counts = new Map();
    normalizeArray(value)
        .map(item => String(item).trim())
        .filter(Boolean)
        .forEach(item => counts.set(item, (counts.get(item) ?? 0) + 1));
    return [...counts.entries()].filter(([, count]) => count > 1).map(([item]) => item).sort();
}

function requireValue(condition, failedAuthority, message, proof) {
    if (!condition) fail(failedAuthority, message, proof);
}

function fail(failedAuthority, message, proof) {
    const error = new Error(`${message}\nfailedAuthority=${failedAuthority}${proof ? `\n${JSON.stringify(proof, null, 2)}` : ''}`);
    error.failedAuthority = failedAuthority;
    throw error;
}

if (require.main === module) {
    main().catch(error => {
        console.error(error.stack || String(error));
        process.exit(1);
    });
}

module.exports = {
    buildCabinetProductProof,
    assertCabinetProductProof,
    buildCabinetAuthorityProof,
    assertCabinetAuthorityProof,
    buildTraceProof,
    assertTraceProof,
    assertScreenshotEvidenceProof,
    assertRouteProof,
    hasZeroDiagnosticPublication,
};
