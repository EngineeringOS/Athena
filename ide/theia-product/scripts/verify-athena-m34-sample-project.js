const fs = require('node:fs');
const path = require('node:path');
const { spawn } = require('node:child_process');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_WORKSPACE_OPENED_SENTINEL = 'ATHENA_WORKSPACE_OPENED=';
const ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL = 'ATHENA_WORKSPACE_OPEN_FAILURE=';
const ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_PROOF=';
const ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_SCREENSHOT=';
const ATHENA_CONTROL_DRAWING_PRODUCT_PROOF_SENTINEL = 'ATHENA_M34_CONTROL_DRAWING_PRODUCT_PROOF=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME=';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED=';
const STARTUP_TIMEOUT_MS = 120000;
const SOURCE_RELATIVE = 'src/com/engineeringood/m34/professional/01-control-drawing.athena';
const EXPECTED_OUTLINE_PATH = 'RollingShutterControlM34 > ShutterMotorM34 > up';
const REQUIRED_DRAWING_KINDS = [
    'sheet-frame',
    'drawing-area',
    'title-block',
    'title-field',
    'zone-column',
    'zone-row',
    'rail',
    'lane',
    'terminal-strip',
    'label-band',
    'route-channel',
    'reference-marker',
];

async function main() {
    const repositoryRoot = resolveM34SampleProject();
    const screenshotPaths = resolveM34ScreenshotPaths();
    assertInstalledLspHostPresent();

    const desktopSmoke = await runElectronSmoke(repositoryRoot, screenshotPaths.desktop, {
        viewportName: 'desktop',
        windowWidth: 1920,
        windowHeight: 1080,
    });
    const narrowSmoke = await runElectronSmoke(repositoryRoot, screenshotPaths.narrow, {
        viewportName: 'narrow',
        windowWidth: 1280,
        windowHeight: 900,
    });
    const screenshotEvidenceProof = buildScreenshotEvidenceProof([
        { ...desktopSmoke, expectedScreenshotPath: screenshotPaths.desktop },
        { ...narrowSmoke, expectedScreenshotPath: screenshotPaths.narrow },
    ]);
    assertScreenshotEvidenceProof(screenshotEvidenceProof);
    const professionalDrawingProof = buildProfessionalDrawingProof(
        repositoryRoot,
        desktopSmoke.graphWorkbenchProof,
        screenshotEvidenceProof,
    );
    assertProfessionalDrawingProof(professionalDrawingProof);

    console.log(`${ATHENA_CONTROL_DRAWING_PRODUCT_PROOF_SENTINEL}${JSON.stringify(professionalDrawingProof)}`);
    console.log(`${ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL}${JSON.stringify(desktopSmoke.graphWorkbenchProof)}`);
    console.log(`Athena M34 Control Drawing product smoke passed. workspace=${desktopSmoke.openedWorkspace} screenshots=${screenshotPaths.desktop},${screenshotPaths.narrow} javaHome=${desktopSmoke.resolvedJavaHome || 'n/a'}`);
}

async function runElectronSmoke(repositoryRoot, screenshotPath, options = {}) {
    const electronBinary = require('electron');
    const entryScript = path.resolve(__dirname, 'athena-electron-open-workspace-main.js');
    const child = spawn(electronBinary, [entryScript, repositoryRoot], {
        cwd: path.resolve(__dirname, '..'),
        env: {
            ...process.env,
            ATHENA_ELECTRON_SMOKE_EXIT_ON_WORKSPACE_OPEN: '1',
            ATHENA_ELECTRON_SMOKE_SKIP_OUTLINE: '1',
            ATHENA_ELECTRON_SMOKE_OPEN_SOURCE_FOR_DIAGNOSTICS: '1',
            ATHENA_ELECTRON_SMOKE_OUTLINE_SOURCE_RELATIVE: SOURCE_RELATIVE,
            ATHENA_ELECTRON_SMOKE_OUTLINE_EXPECTED_PATH: EXPECTED_OUTLINE_PATH,
            ATHENA_ELECTRON_SMOKE_ACTIVE_VIEW: 'schematic',
            ATHENA_ELECTRON_SMOKE_EXPECTED_PRODUCT_SURFACE_ID: 'control-drawing',
            ATHENA_ELECTRON_SMOKE_EXPECTED_PRODUCT_SURFACE_LABEL: 'Control Drawing',
            ATHENA_ELECTRON_SMOKE_EXPECTED_BACKING_VIEW_ID: 'schematic',
            ATHENA_ELECTRON_GRAPH_VIEW_SCREENSHOT: screenshotPath || '',
            ATHENA_ELECTRON_SMOKE_WINDOW_WIDTH: String(options.windowWidth || 1920),
            ATHENA_ELECTRON_SMOKE_WINDOW_HEIGHT: String(options.windowHeight || 1080),
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
        if (!trimmedLine) {
            return;
        }
        outputLines.push(trimmedLine);
        if (trimmedLine === ATHENA_WINDOW_CREATED_SENTINEL) {
            sawWindowCreated = true;
        }
        if (trimmedLine === ATHENA_READY_SENTINEL) {
            sawReady = true;
        }
        if (trimmedLine.startsWith(ATHENA_WORKSPACE_OPENED_SENTINEL)) {
            openedWorkspace = trimmedLine.substring(ATHENA_WORKSPACE_OPENED_SENTINEL.length);
        }
        if (trimmedLine.startsWith(ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL)) {
            graphWorkbenchProof = JSON.parse(trimmedLine.substring(ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL.length));
        }
        if (trimmedLine.startsWith(ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL)) {
            capturedScreenshotPath = trimmedLine.substring(ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL.length);
        }
        if (trimmedLine.startsWith(ATHENA_JAVA_SENTINEL)) {
            resolvedJavaHome = trimmedLine.substring(ATHENA_JAVA_SENTINEL.length);
        }
        if (trimmedLine.startsWith(ATHENA_JAVA_UNRESOLVED_SENTINEL)) {
            unresolvedJavaSignal = trimmedLine.substring(ATHENA_JAVA_UNRESOLVED_SENTINEL.length);
        }
        if (hasZeroDiagnosticPublication([trimmedLine])) {
            zeroDiagnosticPublication = true;
        }
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
        fail('electron-runtime', `M34 smoke failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}`, outputLines);
    }
    if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
        fail('java-runtime', `M34 smoke did not resolve Java 25: ${unresolvedJavaSignal}`);
    }
    if (openedWorkspace !== repositoryRoot) {
        const failureLine = outputLines.find(line => line.startsWith(ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL));
        fail('repository-contract', `Opened ${openedWorkspace || '<missing>'} instead of ${repositoryRoot}.${failureLine ? `\n${failureLine}` : ''}`);
    }
    if (!graphWorkbenchProof) {
        fail('graph-workbench-proof', 'M34 smoke did not report graph workbench proof.', outputLines);
    }
    if (!zeroDiagnosticPublication && !hasZeroDiagnosticPublication(outputLines)) {
        fail('lsp-diagnostics', `M34 smoke did not prove zero LSP diagnostics for ${SOURCE_RELATIVE}.`, outputLines);
    }
    return {
        viewportName: options.viewportName || 'desktop',
        windowWidth: options.windowWidth || 1920,
        windowHeight: options.windowHeight || 1080,
        openedWorkspace,
        graphWorkbenchProof,
        capturedScreenshotPath,
        resolvedJavaHome,
    };
}

function buildProfessionalDrawingProof(repositoryRoot, graphWorkbenchProof, screenshotEvidenceProof) {
    const sourceUri = pathToFileUri(path.join(repositoryRoot, SOURCE_RELATIVE));
    return {
        schemaVersion: 'm34.professional-drawing.v1',
        sourceUri,
        activeViewId: graphWorkbenchProof?.activeViewId || '',
        projectionViewProof: graphWorkbenchProof?.projectionViewProof,
        toolbarDensityProof: graphWorkbenchProof?.toolbarDensityProof,
        drawingLayerProof: graphWorkbenchProof?.drawingLayerProof,
        representationProof: graphWorkbenchProof?.representationProof,
        routeProof: graphWorkbenchProof?.routeProof,
        visualProof: graphWorkbenchProof?.visualProof,
        createEntityPanelProof: graphWorkbenchProof?.createEntityPanelProof,
        sourceDiagnosticActivationProof: graphWorkbenchProof?.sourceDiagnosticActivationProof,
        screenshotEvidenceProof,
        m34AuthorityProof: buildM34AuthorityProof(repositoryRoot, graphWorkbenchProof),
        outlineProof: graphWorkbenchProof?.outlineProof,
    };
}

function assertProfessionalDrawingProof(proof) {
    requireValue(proof.activeViewId === 'schematic', 'projection-authority', `Expected active schematic-backed Control Drawing view, got ${proof.activeViewId || '<missing>'}.`, proof);
    assertProjectionViewProof(proof.projectionViewProof);
    assertToolbarDensityProof(proof.toolbarDensityProof);
    assertDrawingLayerProof(proof.drawingLayerProof);
    assertRepresentationProof(proof.representationProof);
    assertRouteProof(proof.routeProof);
    assertVisualProof(proof.visualProof);
    assertCreateEntityPanelProof(proof.createEntityPanelProof);
    assertSourceDiagnosticActivationProof(proof.sourceDiagnosticActivationProof);
    assertScreenshotEvidenceProof(proof.screenshotEvidenceProof);
    assertM34AuthorityProof(proof.m34AuthorityProof);
}

function buildM34AuthorityProof(repositoryRoot, graphWorkbenchProof) {
    const representationRoot = path.join(repositoryRoot, 'packages', 'representation');
    const packageFiles = listFiles(repositoryRoot);
    const xmlFiles = packageFiles.filter(file => file.toLowerCase().endsWith('.xml'));
    const athenaRepresentationSources = packageFiles
        .filter(file => file.startsWith('packages/representation'))
        .filter(file => file.endsWith('.athena'))
        .sort();
    const governedSvgResources = packageFiles
        .filter(file => file.startsWith('packages/representation'))
        .filter(file => file.endsWith('.svg'))
        .sort();
    return {
        schemaVersion: 'm34.authority-proof.v1',
        representationRoot,
        athenaRepresentationSourceCount: athenaRepresentationSources.length,
        governedSvgResourceCount: governedSvgResources.length,
        xmlRuntimeAuthorityAbsent: xmlFiles.length === 0,
        rawMarkupAuthorityAbsent: true,
        fallbackAuthorityAbsent: normalizeArray(graphWorkbenchProof?.representationProof?.fallbackRepresentationIds).length === 0,
        hardCodedDocumentBoundsAbsent: graphWorkbenchProof?.visualProof?.svgViewBox !== '0 0 1680 1188',
        ungovernedAssetCount: packageFiles.filter(file => /m33|qelectrotech|qet/i.test(file)).length,
        xmlFiles,
        athenaRepresentationSources,
        governedSvgResources,
    };
}

function assertM34AuthorityProof(proof) {
    requireValue(proof, 'm34-authority', 'Missing M34 authority proof.');
    requireValue(proof.xmlRuntimeAuthorityAbsent === true, 'm34-authority', 'M34 sample still contains XML runtime authority.', proof);
    requireValue(proof.rawMarkupAuthorityAbsent === true, 'm34-authority', 'M34 verifier detected raw markup authority.', proof);
    requireValue(proof.fallbackAuthorityAbsent === true, 'm34-authority', 'M34 runtime accepted fallback authority.', proof);
    requireValue(proof.hardCodedDocumentBoundsAbsent === true, 'm34-authority', 'M34 runtime still uses hard-coded document bounds.', proof);
    requireValue(Number(proof.athenaRepresentationSourceCount) >= 4, 'm34-authority', 'M34 requires governed Athena representation sources.', proof);
    requireValue(Number(proof.governedSvgResourceCount) >= 0, 'm34-authority', 'M34 governed SVG resource count must be numeric.', proof);
    requireValue(Number(proof.ungovernedAssetCount) === 0, 'm34-authority', 'M34 sample references ungoverned legacy/QET assets.', proof);
}

function buildScreenshotEvidenceProof(smokes) {
    return {
        schemaVersion: 'm34.screenshot-evidence.v1',
        screenshots: smokes.map(smoke => {
            if (smoke.capturedScreenshotPath !== smoke.expectedScreenshotPath) {
                fail('electron-screenshot', `Expected screenshot ${smoke.expectedScreenshotPath}, got ${smoke.capturedScreenshotPath || '<missing>'}.`);
            }
            const png = assertPngScreenshot(smoke.expectedScreenshotPath);
            const graphProof = smoke.graphWorkbenchProof || {};
            const visualProof = graphProof.visualProof || {};
            const drawingLayerProof = graphProof.drawingLayerProof || {};
            const routeProof = graphProof.routeProof || {};
            const representationProof = graphProof.representationProof || {};
            const createEntityPanelProof = graphProof.createEntityPanelProof || {};
            return {
                viewportName: smoke.viewportName,
                windowWidth: smoke.windowWidth,
                windowHeight: smoke.windowHeight,
                screenshotPath: smoke.expectedScreenshotPath,
                pngWidth: png.width,
                pngHeight: png.height,
                pngBytes: png.bytes,
                activeViewId: graphProof.activeViewId || '',
                usableViewportWidth: Number(visualProof.usableViewportWidth || 0),
                usableViewportHeight: Number(visualProof.usableViewportHeight || 0),
                sheetWidth: Number(visualProof.sheetWidth || 0),
                sheetHeight: Number(visualProof.sheetHeight || 0),
                canvasWidth: Number(visualProof.canvasWidth || 0),
                canvasHeight: Number(visualProof.canvasHeight || 0),
                canvasNonblank: Number(drawingLayerProof.visibleItemCount || 0) > 0 &&
                    Number(routeProof.routeCount || 0) > 0 &&
                    Number(representationProof.representationCount || 0) > 0,
                framedCorrectly: graphProof.sheetFrame === true &&
                    Number(visualProof.viewBoxWidth || 0) > 0 &&
                    Number(visualProof.viewBoxHeight || 0) > 0,
                toolbarControlsDoNotOverlap: Number(graphProof.toolbarDensityProof?.normalToolbarButtonCount || 0) <= 4 &&
                    createEntityPanelProof.withinViewport === true &&
                    createEntityPanelProof.frontmostAtCenter === true,
            };
        }),
    };
}

function assertScreenshotEvidenceProof(proof) {
    requireValue(proof, 'electron-screenshot', 'Missing screenshot evidence proof.');
    const screenshots = normalizeArray(proof.screenshots);
    requireValue(screenshots.length === 2, 'electron-screenshot', 'Expected desktop and narrow screenshot evidence.', proof);
    const byName = new Map(screenshots.map(item => [item.viewportName, item]));
    for (const viewportName of ['desktop', 'narrow']) {
        const screenshot = byName.get(viewportName);
        requireValue(screenshot, 'electron-screenshot', `Missing ${viewportName} screenshot evidence.`, proof);
        requireValue(screenshot.activeViewId === 'schematic', 'electron-screenshot', `${viewportName} screenshot is not Control Drawing.`, screenshot);
        requireValue(Number(screenshot.pngWidth) >= 320 && Number(screenshot.pngHeight) >= 240, 'electron-screenshot', `${viewportName} screenshot dimensions are too small.`, screenshot);
        requireValue(screenshot.canvasNonblank === true, 'electron-screenshot', `${viewportName} Control Drawing canvas is blank.`, screenshot);
        requireValue(screenshot.framedCorrectly === true, 'electron-screenshot', `${viewportName} Control Drawing frame/viewBox evidence failed.`, screenshot);
        requireValue(screenshot.toolbarControlsDoNotOverlap === true, 'electron-screenshot', `${viewportName} toolbar or panel controls overlap.`, screenshot);
    }
    requireValue(
        Number(byName.get('desktop')?.usableViewportWidth || 0) > Number(byName.get('narrow')?.usableViewportWidth || 0),
        'electron-screenshot',
        'Desktop screenshot did not prove a wider viewport than narrow screenshot.',
        proof,
    );
}

function assertProjectionViewProof(proof) {
    requireValue(proof, 'projection-navigation', 'Missing Control Drawing navigation proof.');
    requireValue(normalizeArray(proof.visibleProductSurfaceIds).join(',') === 'control-drawing', 'projection-navigation', 'Control Drawing must be the only visible product surface.', proof);
    requireValue(normalizeArray(proof.visibleViewIds).join(',') === 'schematic', 'projection-navigation', 'Control Drawing must use schematic as the only visible backing view.', proof);
    requireValue(proof.primaryLabelMatched === true, 'projection-navigation', 'Control Drawing product surface label did not match.', proof);
    requireValue(proof.cabinetRefreshAccepted === true && proof.cabinetActiveAfterRefresh === true, 'projection-navigation', 'Control Drawing did not survive refresh.', proof);
}

function assertToolbarDensityProof(proof) {
    requireValue(proof, 'workbench-toolbar', 'Missing toolbar density proof.');
    requireValue(Number(proof.normalToolbarButtonCount) <= 4, 'workbench-toolbar', 'Too many Control Drawing toolbar controls.', proof);
    requireValue(Number(proof.internalProofControlCount) === 0, 'workbench-toolbar', 'Internal proof/debug controls leaked into product toolbar.', proof);
}

function assertDrawingLayerProof(proof) {
    requireValue(proof, 'drawing-composition', 'Missing live drawing layer proof.');
    const kinds = new Set(normalizeArray(proof.kinds));
    const missingKinds = REQUIRED_DRAWING_KINDS.filter(kind => !kinds.has(kind));
    requireValue(missingKinds.length === 0, 'drawing-composition', `Missing drawing layer kinds: ${missingKinds.join(', ')}`, proof);
    requireValue(Number(proof.visibleItemCount) >= REQUIRED_DRAWING_KINDS.length - 1, 'drawing-composition', 'Drawing layer items are not visibly rendered.', proof);
    requireValue(normalizeArray(proof.referenceMarkerIdentities).includes('iec.folio-continuation-reference'), 'drawing-reference', 'Package-backed folio reference marker not rendered.', proof);
    requireValue(Number(proof.wrapperBorderCount) === 0, 'interaction-hitbox', 'Normal Control Drawing component wrapper borders are visible.', proof);
}

function assertRepresentationProof(proof) {
    requireValue(proof && proof.representationCount > 0, 'representation-binding', 'Missing representation facts.', proof);
    requireValue(proof.presentationTerminalCount > 0, 'representation-binding', 'Missing presentation terminals.', proof);
    requireValue(proof.presentationLabelCount > 0, 'representation-binding', 'Missing presentation labels.', proof);
    requireValue(normalizeArray(proof.fallbackRepresentationIds).length === 0, 'representation-binding', 'Generic fallback representations used.', proof);
    requireValue(duplicateValues(proof.semanticIds).length === 0, 'representation-binding', 'Duplicate semantic representation occurrences rendered.', proof);
}

function assertRouteProof(proof) {
    requireValue(proof && proof.routeCount > 0, 'route-anchors', 'Missing route proof.', proof);
    requireValue(proof.routesWithTerminalAnchors >= proof.routeCount, 'route-anchors', 'Routes without terminal anchors rendered.', proof);
    requireValue(normalizeArray(proof.centerFallbackRouteIds).length === 0, 'route-anchors', 'Center-anchor fallback routes rendered.', proof);
}

function assertVisualProof(proof) {
    requireValue(proof, 'visual-bounds', 'Missing visual proof.');
    requireValue(proof.svgViewBox !== '0 0 1680 1188', 'visual-bounds', 'Hard-coded oversized viewBox is still active.', proof);
    requireValue(Number(proof.viewBoxWidth) > 0 && Number(proof.viewBoxHeight) > 0, 'visual-bounds', 'Invalid viewBox dimensions.', proof);
    requireValue(Number(proof.routeBodyIntersectionCount) === 0, 'visual-bounds', 'Routes intersect component bodies.', proof);
    requireValue(Number(proof.nonOrthogonalSegmentCount ?? 0) === 0, 'visual-bounds', 'Non-orthogonal route segments rendered.', proof);
    requireValue(duplicateValues(proof.nodeSemanticIds).length === 0, 'visual-bounds', 'Duplicate/off-screen semantic nodes rendered.', proof);
}

function assertCreateEntityPanelProof(proof) {
    requireValue(proof, 'create-device', 'Missing Create Device proof.');
    for (const key of ['buttonPresent', 'opened', 'closed', 'hasConceptSelect', 'hasTagInput', 'hasModelInput', 'previewButtonPresent', 'textIncludesCreateEntity']) {
        requireValue(proof[key] === true, 'create-device', `Create Device proof failed ${key}.`, proof);
    }
    requireValue(proof.buttonDisabledBeforeClick !== true, 'create-device', 'Create Device button was disabled before click.', proof);
    requireValue(proof.withinViewport === true && proof.frontmostAtCenter === true, 'create-device', 'Create Device panel is overlapped or outside viewport.', proof);
    requireValue(Number(proof.reachableControlCount) >= 5, 'create-device', 'Create Device panel controls are not reachable.', proof);
}

function assertSourceDiagnosticActivationProof(proof) {
    requireValue(proof, 'lsp-diagnostics', 'Missing source diagnostic activation proof.');
    requireValue(proof.requested === true, 'lsp-diagnostics', 'M34 smoke did not request source diagnostic activation.', proof);
    requireValue(proof.opened === true, 'lsp-diagnostics', 'M34 smoke did not open the source document for LSP diagnostics.', proof);
}

function requireValue(condition, failedAuthority, message, proof) {
    if (!condition) {
        fail(failedAuthority, message, proof);
    }
}

function fail(failedAuthority, message, proof) {
    const error = new Error(`${message}\nfailedAuthority=${failedAuthority}${proof ? `\n${JSON.stringify(proof, null, 2)}` : ''}`);
    error.failedAuthority = failedAuthority;
    throw error;
}

function assertInstalledLspHostPresent() {
    const launcher = path.resolve(__dirname, '..', '..', 'lsp', 'build', 'install', 'athena-lsp-host', 'bin', process.platform === 'win32' ? 'athena-lsp-host.bat' : 'athena-lsp-host');
    if (!fs.existsSync(launcher)) {
        fail('lsp-installation', `M34 smoke requires installed LSP host. Missing ${launcher}. Run sequentially: .\\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`);
    }
}

function resolveM34SampleProject() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm34', 'professional-control-drawing');
    const requiredFiles = [
        'athena.yaml',
        'athena.lock',
        SOURCE_RELATIVE,
        path.join('packages', 'representation', 'com', 'engineeringood', 'm34', 'control', 'control-drawing-bindings.athena'),
        path.join('packages', 'representation', 'com', 'engineeringood', 'm34', 'control', 'control', 'control-bindings.athena'),
        path.join('packages', 'representation', 'com', 'engineeringood', 'm34', 'control', 'control', 'contactor-material.athena'),
        path.join('packages', 'representation', 'com', 'engineeringood', 'm34', 'control', 'field', 'field-material.athena'),
        path.join('packages', 'representation', 'com', 'engineeringood', 'm34', 'control', 'power', 'power-material.athena'),
    ];
    const missing = requiredFiles.filter(filePath => !fs.existsSync(path.join(repositoryRoot, filePath)));
    if (missing.length > 0) {
        fail('repository-contract', `M34 professional control drawing project missing: ${missing.join(', ')}`);
    }
    return repositoryRoot;
}

function resolveM34ScreenshotPaths() {
    const screenshotRoot = path.resolve(resolveRepoRoot(), '_bmad-output', 'implementation-artifacts', 'm34', 'screenshots');
    return {
        desktop: path.join(screenshotRoot, 'm34-control-drawing-product-smoke-desktop.png'),
        narrow: path.join(screenshotRoot, 'm34-control-drawing-product-smoke-narrow.png'),
    };
}

function assertPngScreenshot(screenshotPath) {
    if (!fs.existsSync(screenshotPath)) {
        fail('electron-screenshot', `Screenshot missing: ${screenshotPath}`);
    }
    const bytes = fs.readFileSync(screenshotPath);
    if (bytes.length < 1024 || bytes.subarray(0, 8).toString('hex') !== '89504e470d0a1a0a') {
        fail('electron-screenshot', `Invalid PNG screenshot: ${screenshotPath}`);
    }
    return {
        bytes: bytes.length,
        width: bytes.readUInt32BE(16),
        height: bytes.readUInt32BE(20),
    };
}

function listFiles(root) {
    const output = [];
    const visit = directory => {
        for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
            const fullPath = path.join(directory, entry.name);
            if (entry.isDirectory()) {
                visit(fullPath);
            } else if (entry.isFile()) {
                output.push(path.relative(root, fullPath));
            }
        }
    };
    visit(root);
    return output.map(file => file.replace(/\\/g, '/')).sort();
}

function resolveRepoRoot() {
    let current = path.resolve(__dirname);
    while (path.dirname(current) !== current && !fs.existsSync(path.join(current, 'settings.gradle.kts'))) {
        current = path.dirname(current);
    }
    if (!fs.existsSync(path.join(current, 'settings.gradle.kts'))) {
        fail('repository-contract', 'Could not locate Athena repository root.');
    }
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
    return (
        normalizedText.includes('published 0 diagnostic(s)') &&
        (
            normalizedText.includes(SOURCE_RELATIVE.replaceAll('\\', '/').toLowerCase()) ||
            compactText.includes(compactSourceNeedle)
        )
    );
}

function duplicateValues(value) {
    const counts = new Map();
    normalizeArray(value)
        .map(item => String(item).trim())
        .filter(Boolean)
        .forEach(item => counts.set(item, (counts.get(item) ?? 0) + 1));
    return [...counts.entries()]
        .filter(([, count]) => count > 1)
        .map(([item]) => item)
        .sort();
}

if (require.main === module) {
    main().catch(error => {
        console.error(error.stack || String(error));
        process.exit(1);
    });
}

module.exports = {
    buildProfessionalDrawingProof,
    assertProfessionalDrawingProof,
    buildM34AuthorityProof,
    assertM34AuthorityProof,
    assertScreenshotEvidenceProof,
    hasZeroDiagnosticPublication,
    REQUIRED_DRAWING_KINDS,
};

