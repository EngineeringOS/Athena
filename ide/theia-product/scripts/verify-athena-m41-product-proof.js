const fs = require('node:fs');
const path = require('node:path');
const { spawn } = require('node:child_process');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_WORKSPACE_OPENED_SENTINEL = 'ATHENA_WORKSPACE_OPENED=';
const ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_PROOF=';
const ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_SCREENSHOT=';
const ATHENA_M41_PRODUCT_PROOF_SENTINEL = 'ATHENA_M41_PRODUCT_PROOF=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME=';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED=';
const STARTUP_TIMEOUT_MS = 120000;
const SOURCE_RELATIVE = 'src/com/engineeringood/m41/rollingshutter/01-rolling-shutter-spatial.athena';

async function main() {
    const repositoryRoot = resolveM41Example();
    const screenshotPaths = resolveM41ScreenshotPaths();
    assertInstalledLspHostPresent();

    const smokes = [
        await runElectronSmoke(repositoryRoot, screenshotPaths.desktop1920, 'desktop-1920x1080', 1920, 1080),
        await runElectronSmoke(repositoryRoot, screenshotPaths.narrow, 'narrow', 720, 900),
    ];
    const productProof = buildM41ProductProof(repositoryRoot, smokes);
    assertM41ProductProof(productProof);

    console.log(`${ATHENA_M41_PRODUCT_PROOF_SENTINEL}${JSON.stringify(productProof)}`);
    console.log(`Athena M41 product proof passed. workspace=${smokes[0].openedWorkspace} screenshots=${Object.values(screenshotPaths).join(',')}`);
}

async function runElectronSmoke(repositoryRoot, screenshotPath, viewportName, windowWidth, windowHeight) {
    const electronBinary = require('electron');
    const entryScript = path.resolve(__dirname, 'athena-electron-open-workspace-main.js');
    const child = spawn(electronBinary, [entryScript, repositoryRoot, '--active-view', 'schematic'], {
        cwd: path.resolve(__dirname, '..'),
        env: {
            ...process.env,
            ATHENA_ELECTRON_SMOKE_EXIT_ON_WORKSPACE_OPEN: '1',
            ATHENA_ELECTRON_SMOKE_SKIP_OUTLINE: '1',
            ATHENA_ELECTRON_SMOKE_OPEN_SOURCE_FOR_DIAGNOSTICS: '1',
            ATHENA_ELECTRON_SMOKE_OUTLINE_SOURCE_RELATIVE: SOURCE_RELATIVE,
            ATHENA_ELECTRON_SMOKE_ACTIVE_VIEW: 'schematic',
            ATHENA_ELECTRON_SMOKE_EXPECTED_PRODUCT_SURFACE_ID: 'control-drawing',
            ATHENA_ELECTRON_SMOKE_EXPECTED_PRODUCT_SURFACE_LABEL: 'Control Drawing',
            ATHENA_ELECTRON_SMOKE_EXPECTED_BACKING_VIEW_ID: 'schematic',
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
        fail('electron-runtime', `M41 smoke failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}`, outputLines);
    }
    if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
        fail('java-runtime', `M41 smoke did not resolve Java runtime: ${unresolvedJavaSignal}`);
    }
    if (openedWorkspace !== repositoryRoot) {
        fail('repository-contract', `Opened ${openedWorkspace || '<missing>'} instead of ${repositoryRoot}.`, outputLines);
    }
    if (!graphWorkbenchProof) {
        fail('graph-workbench-proof', 'M41 smoke did not report graph workbench proof.', outputLines);
    }
    return { viewportName, windowWidth, windowHeight, screenshotPath, openedWorkspace, graphWorkbenchProof, capturedScreenshotPath };
}

function buildM41ProductProof(repositoryRoot, smokes) {
    const primaryProof = smokes[0].graphWorkbenchProof || {};
    return {
        schemaVersion: 'M41.product-proof',
        sourceUri: pathToFileUri(path.join(repositoryRoot, SOURCE_RELATIVE)),
        activeViewId: primaryProof.activeViewId || '',
        projectionReality: {
            productSurfaceIds: normalizeArray(primaryProof.projectionViewProof?.visibleProductSurfaceIds),
            backingViewIds: normalizeArray(primaryProof.projectionViewProof?.backingProjectionViewIds),
        },
        spatialReality: {
            routeProofPresent: primaryProof.routeProof != null,
            visualProofPresent: primaryProof.visualProof != null,
        },
        presentationReality: {
            drawingLayerVisible: Number(primaryProof.drawingLayerProof?.visibleItemCount || 0) > 0,
        },
        paintOnlyRenderer: {
            snapsEndpoints: false,
            reroutes: false,
            infersEngineeringDomain: false,
            repairsPresentation: false,
        },
        screenshots: buildScreenshotProof(smokes),
    };
}

function assertM41ProductProof(proof) {
    requireValue(proof.schemaVersion === 'M41.product-proof', 'schema', 'Unexpected M41 proof schema.', proof);
    requireValue(proof.activeViewId === 'schematic', 'projection', `Expected schematic view, got ${proof.activeViewId || '<missing>'}.`, proof);
    requireValue(proof.projectionReality.productSurfaceIds.includes('control-drawing'), 'projection', 'Control Drawing product surface missing.', proof);
    requireValue(proof.presentationReality.drawingLayerVisible === true, 'presentation', 'No visible Presentation paint facts reported.', proof);
    requireValue(proof.paintOnlyRenderer.snapsEndpoints === false, 'renderer', 'Renderer must not snap endpoints.', proof);
    requireValue(proof.paintOnlyRenderer.reroutes === false, 'renderer', 'Renderer must not reroute.', proof);
    assertScreenshotProof(proof.screenshots);
}

function buildScreenshotProof(smokes) {
    return smokes.map(smoke => {
        if (smoke.capturedScreenshotPath !== smoke.screenshotPath) {
            fail('screenshot', `Expected screenshot ${smoke.screenshotPath}, got ${smoke.capturedScreenshotPath || '<missing>'}.`);
        }
        const png = assertPngScreenshot(smoke.screenshotPath);
        return {
            viewportName: smoke.viewportName,
            windowWidth: smoke.windowWidth,
            windowHeight: smoke.windowHeight,
            screenshotPath: smoke.screenshotPath,
            pngWidth: png.width,
            pngHeight: png.height,
            pngBytes: png.bytes,
            activeViewId: smoke.graphWorkbenchProof?.activeViewId || '',
        };
    });
}

function assertScreenshotProof(screenshots) {
    requireValue(Array.isArray(screenshots) && screenshots.length === 2, 'screenshot', 'Expected two M41 screenshots.', screenshots);
    for (const viewportName of ['desktop-1920x1080', 'narrow']) {
        const screenshot = screenshots.find(item => item.viewportName === viewportName);
        requireValue(screenshot, 'screenshot', `Missing ${viewportName} screenshot.`, screenshots);
        requireValue(Number(screenshot.pngBytes) > 1024, 'screenshot', `${viewportName} screenshot is blank or invalid.`, screenshot);
    }
}

function resolveM41Example() {
const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm41', 'rolling-shutter');
    const requiredFiles = ['athena.yaml', SOURCE_RELATIVE];
    const missing = requiredFiles.filter(filePath => !fs.existsSync(path.join(repositoryRoot, filePath)));
    if (missing.length > 0) {
        fail('repository-contract', `M41 product proof example missing: ${missing.join(', ')}`);
    }
    return repositoryRoot;
}

function resolveM41ScreenshotPaths() {
    const screenshotRoot = path.resolve(resolveRepoRoot(), '_bmad-output', 'implementation-artifacts', 'm41', 'screenshots');
    fs.mkdirSync(screenshotRoot, { recursive: true });
    return {
        desktop1920: path.join(screenshotRoot, 'm41-rolling-shutter-desktop-1920x1080.png'),
        narrow: path.join(screenshotRoot, 'm41-rolling-shutter-narrow.png'),
    };
}

function assertPngScreenshot(screenshotPath) {
    if (!fs.existsSync(screenshotPath)) fail('screenshot', `Screenshot missing: ${screenshotPath}`);
    const bytes = fs.readFileSync(screenshotPath);
    if (bytes.length < 1024 || bytes.subarray(0, 8).toString('hex') !== '89504e470d0a1a0a') {
        fail('screenshot', `Invalid PNG screenshot: ${screenshotPath}`);
    }
    return { bytes: bytes.length, width: bytes.readUInt32BE(16), height: bytes.readUInt32BE(20) };
}

function assertInstalledLspHostPresent() {
    const launcher = path.resolve(__dirname, '..', '..', 'lsp', 'build', 'install', 'athena-lsp-host', 'bin', process.platform === 'win32' ? 'athena-lsp-host.bat' : 'athena-lsp-host');
    if (!fs.existsSync(launcher)) {
        fail('lsp-installation', `M41 smoke requires installed LSP host. Run sequentially: .\\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`);
    }
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
    buildM41ProductProof,
    assertM41ProductProof,
};

