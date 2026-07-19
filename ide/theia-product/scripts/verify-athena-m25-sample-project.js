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
    const repositoryRoot = resolveM25SampleProject();
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
            `Athena M25 sample project smoke failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}\n${outputLines.join('\n')}`
        );
    }
    if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
        throw new Error(`Athena M25 sample project smoke did not resolve Java 25: ${unresolvedJavaSignal}`);
    }
    if (openedWorkspace !== repositoryRoot) {
        const failureLine = outputLines.find(line => line.startsWith(ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL));
        throw new Error(
            `Athena M25 sample project smoke opened '${openedWorkspace || 'n/a'}' instead of '${repositoryRoot}'.${failureLine ? `\n${failureLine}` : ''}`
        );
    }
    if (!graphWorkbenchProof) {
        throw new Error(`Athena M25 sample project smoke did not report graph workbench DOM proof.\n${outputLines.join('\n')}`);
    }
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
            `Athena M25 graph-workbench DOM proof failed: ${missingGraphProof.join(', ')}\n${JSON.stringify(graphWorkbenchProof)}`
        );
    }

    assertRouteProof(graphWorkbenchProof.routeProof);
    assertRepresentationProof(graphWorkbenchProof.representationProof);

    console.log(`${ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL}${JSON.stringify(graphWorkbenchProof)}`);
    console.log('Athena M25 representation DOM proof passed.');
    console.log(`Athena M25 sample project smoke passed. workspace=${openedWorkspace} javaHome=${resolvedJavaHome || 'n/a'}`);
}

function assertRouteProof(routeProof) {
    if (!routeProof) {
        throw new Error('Athena M25 smoke did not receive routeProof in graph-workbench proof payload.');
    }
    const failures = [];
    if (routeProof.routeCount < 1) {
        failures.push('routeCount < 1');
    }
    if (routeProof.terminalCount < 2) {
        failures.push('route terminalCount < 2');
    }
    if (routeProof.routesWithTerminalAnchors < routeProof.routeCount) {
        failures.push('not every route reports terminal anchors');
    }
    if (routeProof.routesWithOrthogonalBends < 1) {
        failures.push('no route reports orthogonal bend points');
    }
    if (routeProof.centerFallbackRouteIds.length > 0) {
        failures.push(`center fallback route ids: ${routeProof.centerFallbackRouteIds.join(', ')}`);
    }
    if (failures.length > 0) {
        throw new Error(`Athena M25 route proof failed: ${failures.join('; ')}\n${JSON.stringify(routeProof, null, 2)}`);
    }
}

function assertRepresentationProof(representationProof) {
    if (!representationProof) {
        throw new Error('Athena M25 smoke did not receive representationProof in graph-workbench proof payload.');
    }
    const failures = [];
    if (representationProof.representationCount < 4) {
        failures.push('representationCount < 4');
    }
    if (representationProof.presentationTerminalCount < 4) {
        failures.push('presentationTerminalCount < 4');
    }
    if (representationProof.presentationLabelCount < 4) {
        failures.push('presentationLabelCount < 4');
    }
    if (representationProof.terminalNumbers.length < 4) {
        failures.push('terminal numbers missing');
    }
    if (!representationProof.labelRoles.includes('device_tag')) {
        failures.push('device_tag label role missing');
    }
    if (representationProof.fallbackRepresentationIds.length > 0) {
        failures.push(`fallback representation ids: ${representationProof.fallbackRepresentationIds.join(', ')}`);
    }
    if (!representationProof.semanticIds.some(id => id.includes('PowerSupplyPS1') || id.includes('ControllerPLC1'))) {
        failures.push('active source proof did not include expected M25 sample subjects');
    }
    if (failures.length > 0) {
        throw new Error(
            `Athena M25 representation proof failed: ${failures.join('; ')}\n${JSON.stringify(representationProof, null, 2)}`
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
            `Athena M25 smoke requires the installed LSP host before product proof. Missing ${launcher}. Run: .\\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`
        );
    }
}

function resolveM25SampleProject() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm25', 'sample-project');
    const requiredFiles = [
        'athena.yaml',
        'athena.lock',
        'README.md',
        path.join('src', '01-professional-symbol-sheet.athena'),
        path.join('src', '02-terminal-labels-and-routes.athena'),
        path.join('src', '03-six-family-acceptance.athena')
    ];
    const missing = requiredFiles.filter(filePath => !fs.existsSync(path.join(repositoryRoot, filePath)));
    if (missing.length > 0) {
        throw new Error(
            `Athena M25 sample project could not resolve the checked-in proof repository at ${repositoryRoot}. Missing: ${missing.join(', ')}`
        );
    }
    return repositoryRoot;
}

main().catch(error => {
    console.error(error.stack || String(error));
    process.exit(1);
});
