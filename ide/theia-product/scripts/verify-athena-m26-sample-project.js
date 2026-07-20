const fs = require('node:fs');
const path = require('node:path');
const { spawn } = require('node:child_process');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_WORKSPACE_OPENED_SENTINEL = 'ATHENA_WORKSPACE_OPENED=';
const ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL = 'ATHENA_WORKSPACE_OPEN_FAILURE=';
const ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_PROOF=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME=';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED=';
const STARTUP_TIMEOUT_MS = 90000;

async function main() {
    const repositoryRoot = resolveM26SampleProject();
    assertInstalledLspHostPresent();
    const electronBinary = require('electron');
    const entryScript = path.resolve(__dirname, 'athena-electron-open-workspace-main.js');
    const workingDirectory = path.resolve(__dirname, '..');
    const child = spawn(
        electronBinary,
        [entryScript, repositoryRoot],
        {
            cwd: workingDirectory,
            env: {
                ...process.env,
                ATHENA_ELECTRON_SMOKE_EXIT_ON_WORKSPACE_OPEN: '1',
                ATHENA_ELECTRON_TEMP_USER_DATA: '1',
                ATHENA_ELECTRON_SMOKE_ACTIVE_VIEW: 'documentation',
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

    const timeoutHandle = setTimeout(() => {
        child.kill();
    }, STARTUP_TIMEOUT_MS);

    const exitCode = await new Promise(resolveExit => {
        child.on('exit', code => resolveExit(code ?? -1));
        child.on('error', () => resolveExit(-1));
    });
    clearTimeout(timeoutHandle);

    if (!sawWindowCreated || !sawReady || exitCode !== 0) {
        throw new Error(
            `Athena M26 sample project smoke failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}\n${outputLines.join('\n')}`
        );
    }
    if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
        throw new Error(`Athena M26 sample project smoke did not resolve Java 25: ${unresolvedJavaSignal}`);
    }
    if (openedWorkspace !== repositoryRoot) {
        const failureLine = outputLines.find(line => line.startsWith(ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL));
        throw new Error(
            `Athena M26 sample project smoke opened '${openedWorkspace || 'n/a'}' instead of '${repositoryRoot}'.${failureLine ? `\n${failureLine}` : ''}`
        );
    }
    if (!graphWorkbenchProof) {
        throw new Error(`Athena M26 sample project smoke did not report graph workbench DOM proof.\n${outputLines.join('\n')}`);
    }

    assertGraphWorkbenchProof(graphWorkbenchProof);
    assertRouteProof(graphWorkbenchProof.routeProof);
    assertRepresentationProof(graphWorkbenchProof.representationProof);
    assertDocumentProjectionProof(graphWorkbenchProof.documentProjectionProof);

    console.log(`${ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL}${JSON.stringify(graphWorkbenchProof)}`);
    console.log('Athena M26 semantic document projection proof passed.');
    console.log(`Athena M26 sample project smoke passed. workspace=${openedWorkspace} javaHome=${resolvedJavaHome || 'n/a'}`);
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
        'sheetFrame',
        'stageHasGrid',
        'infoPopoverOpened',
        'infoPopoverClosedOnWhitespace'
    ].filter(key => graphWorkbenchProof[key] !== true);
    if (missingGraphProof.length > 0) {
        throw new Error(
            `Athena M26 graph-workbench DOM proof failed: ${missingGraphProof.join(', ')}\n${JSON.stringify(graphWorkbenchProof)}`
        );
    }
}

function assertRouteProof(routeProof) {
    if (!routeProof) {
        throw new Error('Athena M26 smoke did not receive routeProof in graph-workbench proof payload.');
    }
    const failures = [];
    if (routeProof.routeCount < 1) {
        failures.push('routeCount < 1');
    }
    if (routeProof.terminalCount < 2) {
        failures.push('terminalCount < 2');
    }
    if (routeProof.routesWithOrthogonalBends < 1) {
        failures.push('no route reports orthogonal bend points');
    }
    const verboseVisibleLabels = (routeProof.visibleLabelTexts ?? [])
        .filter(label => label.includes(' -> ') && label.includes('.'));
    if (verboseVisibleLabels.length > 0) {
        failures.push(`verbose semantic route labels visible: ${verboseVisibleLabels.join(', ')}`);
    }
    if (failures.length > 0) {
        throw new Error(`Athena M26 route proof failed: ${failures.join('; ')}\n${JSON.stringify(routeProof, null, 2)}`);
    }
}

function assertRepresentationProof(representationProof) {
    if (!representationProof) {
        throw new Error('Athena M26 smoke did not receive representationProof in graph-workbench proof payload.');
    }
    const failures = [];
    if (representationProof.representationCount < 4) {
        failures.push('representationCount < 4');
    }
    if (representationProof.presentationTerminalCount < 4) {
        failures.push('presentationTerminalCount < 4');
    }
    if (representationProof.fallbackRepresentationIds.length > 0) {
        failures.push(`fallback representation ids: ${representationProof.fallbackRepresentationIds.join(', ')}`);
    }
    if (failures.length > 0) {
        throw new Error(
            `Athena M26 representation proof failed: ${failures.join('; ')}\n${JSON.stringify(representationProof, null, 2)}`
        );
    }
}

function assertDocumentProjectionProof(documentProjectionProof) {
    if (!documentProjectionProof) {
        throw new Error('Athena M26 smoke did not receive documentProjectionProof in graph-workbench proof payload.');
    }
    const failures = [];
    if (!documentProjectionProof.hasSheetViewSelector) {
        failures.push('sheet-view selector missing');
    }
    if (documentProjectionProof.sheetViewOptionCount < 3) {
        failures.push('sheetViewOptionCount < 3');
    }
    for (const title of ['Power Distribution', 'Control And PLC Logic', 'Field Wiring And Terminal Transition']) {
        if (!documentProjectionProof.sheetViewOptionTexts.some(text => text.includes(title))) {
            failures.push(`missing sheet-view option ${title}`);
        }
    }
    if (documentProjectionProof.compactReferenceMarkerNotations.some(text => text.includes(' -> '))) {
        failures.push(`verbose reference marker notation: ${documentProjectionProof.compactReferenceMarkerNotations.join(', ')}`);
    }
    if (failures.length > 0) {
        throw new Error(
            `Athena M26 document projection proof failed: ${failures.join('; ')}\n${JSON.stringify(documentProjectionProof, null, 2)}`
        );
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
            `Athena M26 smoke requires the installed LSP host before product proof. Missing ${launcher}. Run: .\\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`
        );
    }
}

function resolveM26SampleProject() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm26', 'sample-project');
    const requiredFiles = [
        'athena.yaml',
        'athena.lock',
        'README.md',
        path.join('src', '01-workspace-semantic-source.athena'),
        path.join('src', '02-field-assets-not-a-sheet.athena')
    ];
    const missing = requiredFiles.filter(filePath => !fs.existsSync(path.join(repositoryRoot, filePath)));
    if (missing.length > 0) {
        throw new Error(
            `Athena M26 sample project could not resolve the checked-in proof repository at ${repositoryRoot}. Missing: ${missing.join(', ')}`
        );
    }
    return repositoryRoot;
}

main().catch(error => {
    console.error(error.stack || String(error));
    process.exit(1);
});
