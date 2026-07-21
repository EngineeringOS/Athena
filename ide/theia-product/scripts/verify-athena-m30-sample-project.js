const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');
const { spawn } = require('node:child_process');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_WORKSPACE_OPENED_SENTINEL = 'ATHENA_WORKSPACE_OPENED=';
const ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL = 'ATHENA_WORKSPACE_OPEN_FAILURE=';
const ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_PROOF=';
const ATHENA_GRAPH_WORKBENCH_SCREENSHOT_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_SCREENSHOT=';
const ATHENA_REPRESENTATION_PROOF_SENTINEL = 'ATHENA_M30_REPRESENTATION_PROOF=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME=';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED=';
const STARTUP_TIMEOUT_MS = 90000;
const REPRESENTATION_SCHEMA_VERSION = 'm30.representation.v1';

// No Gradle verification runs in parallel. Windows Gradle verification must stay sequential.
const REQUIRED_REPRESENTATION_PROOF_KINDS = [
    'representation-library',
    'binding-counts',
    'anchor-usage',
    'composition-bounds',
    'route-anchors',
    'transparent-chrome',
];

async function main() {
    const repositoryRoot = resolveM30SampleProject();
    const screenshotPath = resolveM30ScreenshotPath();
    assertInstalledLspHostPresent();
    const electronBinary = require('electron');
    const entryScript = path.resolve(__dirname, 'athena-electron-open-workspace-main.js');
    const child = spawn(
        electronBinary,
        [entryScript, repositoryRoot],
        {
            cwd: path.resolve(__dirname, '..'),
            env: {
                ...process.env,
                ATHENA_ELECTRON_SMOKE_EXIT_ON_WORKSPACE_OPEN: '1',
                ATHENA_ELECTRON_SMOKE_OUTLINE_SOURCE_RELATIVE: 'src/01-rolling-shutter-control-source.athena',
                ATHENA_ELECTRON_SMOKE_OUTLINE_EXPECTED_PATH: 'RollingShutterControlProof > ShutterMotorM30 > up',
                ATHENA_ELECTRON_GRAPH_VIEW_SCREENSHOT: screenshotPath,
                ATHENA_ELECTRON_TEMP_USER_DATA: '1',
                ELECTRON_ENABLE_LOGGING: '1'
            },
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true
        }
    );

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
        throw new Error(
            `Athena M30 sample project smoke failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}\n${outputLines.join('\n')}`
        );
    }
    if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
        throw new Error(`Athena M30 sample project smoke did not resolve Java 25: ${unresolvedJavaSignal}`);
    }
    if (openedWorkspace !== repositoryRoot) {
        const failureLine = outputLines.find(line => line.startsWith(ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL));
        throw new Error(
            `Athena M30 sample project smoke opened '${openedWorkspace || 'n/a'}' instead of '${repositoryRoot}'.${failureLine ? `\n${failureLine}` : ''}`
        );
    }
    if (!graphWorkbenchProof) {
        throw new Error(`Athena M30 sample project smoke did not report graph workbench proof.\n${outputLines.join('\n')}`);
    }
    if (capturedScreenshotPath !== screenshotPath) {
        throw new Error(`Athena M30 sample project smoke did not capture expected screenshot '${screenshotPath}'. Captured '${capturedScreenshotPath || 'n/a'}'.`);
    }
    assertPngScreenshot(screenshotPath);

    const representationProofPayloads = buildStructuredRepresentationProofPayloads(repositoryRoot, graphWorkbenchProof);
    assertStructuredRepresentationProofPayloads(representationProofPayloads);
    assertGraphWorkbenchProof(graphWorkbenchProof);

    console.log(`${ATHENA_REPRESENTATION_PROOF_SENTINEL}${JSON.stringify(representationProofPayloads)}`);
    console.log(`${ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL}${JSON.stringify(graphWorkbenchProof)}`);
    console.log(`Athena M30 sample project smoke passed. workspace=${openedWorkspace} screenshot=${screenshotPath} javaHome=${resolvedJavaHome || 'n/a'}`);
}

function buildStructuredRepresentationProofPayloads(repositoryRoot, graphWorkbenchProof) {
    const sourceUri = pathToFileURL(
        path.join(repositoryRoot, 'src', '01-rolling-shutter-control-source.athena')
    ).toString();
    const representationProof = graphWorkbenchProof?.representationProof ?? {};
    const routeProof = graphWorkbenchProof?.routeProof ?? {};
    const sheetSurfaceProof = graphWorkbenchProof?.sheetSurfaceProof ?? {};
    const visualProof = graphWorkbenchProof?.visualProof ?? {};

    return [
        proofEnvelope('representation-library', sourceUri, {
            activeSourceContext: 'M30 native representation library',
            loadedRepresentationLibraryIds: deriveRepresentationLibraryIds(representationProof.representationIds).join(','),
            loadedRepresentationIds: normalizeArray(representationProof.representationIds).join(','),
        }),
        proofEnvelope('binding-counts', sourceUri, {
            activeSourceContext: 'M30 semantic representation binding',
            representationCount: String(representationProof.representationCount ?? 0),
            terminalCount: String(representationProof.presentationTerminalCount ?? 0),
            labelCount: String(representationProof.presentationLabelCount ?? 0),
            missingBindingDiagnostics: '0',
            fallbackRepresentationIds: normalizeArray(representationProof.fallbackRepresentationIds).join(','),
        }),
        proofEnvelope('anchor-usage', sourceUri, {
            activeSourceContext: 'M30 terminal anchor binding',
            presentationTerminalCount: String(representationProof.presentationTerminalCount ?? 0),
            routeTerminalCount: String(routeProof.terminalCount ?? 0),
        }),
        proofEnvelope('composition-bounds', sourceUri, {
            activeSourceContext: 'M30 schematic composition bounds',
            sheetSize: sheetSurfaceProof.sheetSize || '',
            orientation: sheetSurfaceProof.orientation || '',
            viewBoxWidth: String(visualProof.viewBoxWidth ?? 0),
            viewBoxHeight: String(visualProof.viewBoxHeight ?? 0),
            routeBodyIntersectionCount: String(visualProof.routeBodyIntersectionCount ?? 0),
        }),
        proofEnvelope('route-anchors', sourceUri, {
            activeSourceContext: 'M30 route anchor proof',
            routeCount: String(routeProof.routeCount ?? 0),
            routesWithTerminalAnchors: String(routeProof.routesWithTerminalAnchors ?? 0),
            centerFallbackRouteIds: normalizeArray(routeProof.centerFallbackRouteIds).join(','),
        }),
        proofEnvelope('transparent-chrome', sourceUri, {
            activeSourceContext: 'M30 transparent normal chrome',
            floatingBarTransparent: String(graphWorkbenchProof?.floatingBarTransparent === true),
            bottomDockTransparent: String(graphWorkbenchProof?.bottomDockTransparent === true),
            zoomDockTransparent: String(graphWorkbenchProof?.zoomDockTransparent === true),
            sheetTransparent: String(graphWorkbenchProof?.sheetTransparent === true),
        }),
    ];
}

function proofEnvelope(proofKind, activeSourceUri, payload) {
    return {
        schemaVersion: REPRESENTATION_SCHEMA_VERSION,
        requestId: `proof:${proofKind}`,
        activeSourceUri,
        activeSourceRevision: 'm30-sample-project',
        payloadKind: 'proof',
        payload: {
            proofKind,
            ...payload,
        },
    };
}

function assertStructuredRepresentationProofPayloads(proofPayloads) {
    if (!Array.isArray(proofPayloads)) {
        throw new Error('Athena M30 representation proof payloads must be an array.');
    }
    const byKind = new Map(proofPayloads.map(payload => [payload?.payload?.proofKind, payload]));
    const missing = REQUIRED_REPRESENTATION_PROOF_KINDS.filter(proofKind => !byKind.has(proofKind));
    if (missing.length > 0) {
        throw new Error(`Athena M30 representation proof payloads missing: ${missing.join(', ')}`);
    }
    for (const proofKind of REQUIRED_REPRESENTATION_PROOF_KINDS) {
        const envelope = byKind.get(proofKind);
        if (envelope.schemaVersion !== REPRESENTATION_SCHEMA_VERSION) {
            throw new Error(`${proofKind} proof schema mismatch: ${envelope.schemaVersion || '<missing>'}`);
        }
        if (envelope.payloadKind !== 'proof') {
            throw new Error(`${proofKind} proof payloadKind expected proof but received ${envelope.payloadKind || '<missing>'}`);
        }
        if (!envelope.activeSourceUri) {
            throw new Error(`${proofKind} proof missing activeSourceUri.`);
        }
        if (!envelope.payload.activeSourceContext) {
            throw new Error(`${proofKind} proof missing activeSourceContext.`);
        }
    }
    assertPositiveNumber(byKind.get('representation-library').payload.loadedRepresentationIds.split(',').filter(Boolean).length, 'loaded representation library ids');
    assertPositiveNumber(byKind.get('binding-counts').payload.representationCount, 'representation binding count');
    assertPositiveNumber(byKind.get('binding-counts').payload.terminalCount, 'presentation terminal count');
    if (byKind.get('binding-counts').payload.missingBindingDiagnostics !== '0') {
        throw new Error('M30 binding proof must have zero missing-binding diagnostics.');
    }
    if (byKind.get('binding-counts').payload.fallbackRepresentationIds) {
        throw new Error(`M30 binding proof used fallback representations: ${byKind.get('binding-counts').payload.fallbackRepresentationIds}`);
    }
    assertPositiveNumber(byKind.get('anchor-usage').payload.presentationTerminalCount, 'presentation anchor count');
    assertPositiveNumber(byKind.get('route-anchors').payload.routeCount, 'route count');
    assertPositiveNumber(byKind.get('route-anchors').payload.routesWithTerminalAnchors, 'anchored route count');
    if (byKind.get('route-anchors').payload.centerFallbackRouteIds) {
        throw new Error(`M30 route proof used center fallback routes: ${byKind.get('route-anchors').payload.centerFallbackRouteIds}`);
    }
    if (byKind.get('composition-bounds').payload.routeBodyIntersectionCount !== '0') {
        throw new Error('M30 composition proof must have zero route/body intersections.');
    }
    ['floatingBarTransparent', 'bottomDockTransparent', 'zoomDockTransparent', 'sheetTransparent'].forEach(key => {
        if (byKind.get('transparent-chrome').payload[key] !== 'true') {
            throw new Error(`M30 transparent chrome proof failed: ${key}`);
        }
    });
}

function assertGraphWorkbenchProof(graphWorkbenchProof) {
    const missingGraphProof = [
        'root',
        'stage',
        'viewport',
        'sheet',
        'canvas',
        'floatingBarTransparent',
        'bottomDockTransparent',
        'zoomDockTransparent',
        'sheetTransparent',
        'stageHasGrid'
    ].filter(key => graphWorkbenchProof[key] !== true);
    if (missingGraphProof.length > 0) {
        throw new Error(
            `Athena M30 graph-workbench proof failed: ${missingGraphProof.join(', ')}\n${JSON.stringify(graphWorkbenchProof)}`
        );
    }
    if (graphWorkbenchProof.activeViewId !== 'cabinet') {
        throw new Error(
            `Athena M30 graph-workbench must default to Cabinet view, received ${graphWorkbenchProof.activeViewId || '<missing>'}.`
        );
    }
    assertStructuredRepresentationProofPayloads(buildStructuredRepresentationProofPayloads(resolveM30SampleProject(), graphWorkbenchProof));
}

function assertPositiveNumber(value, label) {
    const number = Number(value);
    if (!Number.isFinite(number) || number <= 0) {
        throw new Error(`M30 proof expected positive ${label}, received ${value}.`);
    }
}

function assertInstalledLspHostPresent() {
    const launcher = path.resolve(
        __dirname,
        '..',
        '..',
        'lsp',
        'build',
        'install',
        'athena-lsp-host',
        'bin',
        process.platform === 'win32' ? 'athena-lsp-host.bat' : 'athena-lsp-host'
    );
    if (!fs.existsSync(launcher)) {
        throw new Error(
            `Athena M30 smoke requires the installed LSP host before product proof. Missing ${launcher}. Run sequentially: .\\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`
        );
    }
}

function resolveM30ScreenshotPath() {
    const repositoryRoot = resolveRepoRoot();
    return path.resolve(
        repositoryRoot,
        '_bmad-output',
        'implementation-artifacts',
        'm30',
        'screenshots',
        'm30-graph-workbench-smoke.png'
    );
}

function assertPngScreenshot(screenshotPath) {
    if (!fs.existsSync(screenshotPath)) {
        throw new Error(`Athena M30 screenshot guard did not create ${screenshotPath}.`);
    }
    const bytes = fs.readFileSync(screenshotPath);
    const pngSignature = '89504e470d0a1a0a';
    if (bytes.length < 1024 || bytes.subarray(0, 8).toString('hex') !== pngSignature) {
        throw new Error(`Athena M30 screenshot guard produced an invalid PNG at ${screenshotPath}.`);
    }
}

function resolveM30SampleProject() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm30', 'sample-project');
    const requiredFiles = [
        'athena.yaml',
        'athena.lock',
        'README.md',
        path.join('src', '01-rolling-shutter-control-source.athena')
    ];
    const missing = requiredFiles.filter(filePath => !fs.existsSync(path.join(repositoryRoot, filePath)));
    if (missing.length > 0) {
        throw new Error(
            `Athena M30 sample project could not resolve the checked-in proof repository at ${repositoryRoot}. Missing: ${missing.join(', ')}`
        );
    }
    return repositoryRoot;
}

function resolveRepoRoot() {
    let current = path.resolve(__dirname);
    while (path.dirname(current) !== current && !fs.existsSync(path.join(current, 'settings.gradle.kts'))) {
        current = path.dirname(current);
    }
    if (!fs.existsSync(path.join(current, 'settings.gradle.kts'))) {
        throw new Error('Could not locate Athena repository root.');
    }
    return current;
}

function normalizeArray(value) {
    return Array.isArray(value) ? value : [];
}

function deriveRepresentationLibraryIds(representationIds) {
    return [...new Set(
        normalizeArray(representationIds)
            .map(representationId => String(representationId).split(':')[0])
            .filter(Boolean)
    )].sort();
}

if (require.main === module) {
    main().catch(error => {
        console.error(error.stack || String(error));
        process.exit(1);
    });
}

module.exports = {
    buildStructuredRepresentationProofPayloads,
    assertStructuredRepresentationProofPayloads,
    deriveRepresentationLibraryIds,
    assertPngScreenshot,
    assertGraphWorkbenchProof,
    REQUIRED_REPRESENTATION_PROOF_KINDS,
};
