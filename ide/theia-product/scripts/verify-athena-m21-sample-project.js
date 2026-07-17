const fs = require('node:fs');
const path = require('node:path');
const { spawn } = require('node:child_process');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_WORKSPACE_OPENED_SENTINEL = 'ATHENA_WORKSPACE_OPENED=';
const ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL = 'ATHENA_WORKSPACE_OPEN_FAILURE=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME=';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED=';
const STARTUP_TIMEOUT_MS = 90000;

async function main() {
    const repositoryRoot = resolveM21SampleProject();
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
                ELECTRON_ENABLE_LOGGING: '1'
            },
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true
        }
    );

    let sawWindowCreated = false;
    let sawReady = false;
    let openedWorkspace;
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
        if (trimmedLine.startsWith(ATHENA_JAVA_SENTINEL)) {
            resolvedJavaHome = trimmedLine.substring(ATHENA_JAVA_SENTINEL.length);
        }
        if (trimmedLine.startsWith(ATHENA_JAVA_UNRESOLVED_SENTINEL)) {
            unresolvedJavaSignal = trimmedLine.substring(ATHENA_JAVA_UNRESOLVED_SENTINEL.length);
        }
    };

    child.stdout.setEncoding('utf8');
    child.stderr.setEncoding('utf8');
    child.stdout.on('data', chunk => {
        chunk.split(/\r?\n/).forEach(recordLine);
    });
    child.stderr.on('data', chunk => {
        chunk.split(/\r?\n/).forEach(recordLine);
    });

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
            `Athena M21 sample project smoke failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}\n${outputLines.join('\n')}`
        );
    }
    if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
        throw new Error(`Athena M21 sample project smoke did not resolve Java 25: ${unresolvedJavaSignal}`);
    }
    if (openedWorkspace !== repositoryRoot) {
        const failureLine = outputLines.find(line => line.startsWith(ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL));
        throw new Error(
            `Athena M21 sample project smoke opened '${openedWorkspace || 'n/a'}' instead of '${repositoryRoot}'.${failureLine ? `\n${failureLine}` : ''}`
        );
    }

    console.log(`Athena M21 sample project smoke passed. workspace=${openedWorkspace} javaHome=${resolvedJavaHome || 'n/a'}`);
}

function resolveM21SampleProject() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm21', 'sample-project');
    const requiredFiles = [
        'athena.yaml',
        'athena.lock',
        'README.md',
        path.join('src', '01-baseline-sheet.athena'),
        path.join('src', '02-layout-intelligence-acceptance.athena'),
        path.join('src', '03-routing-and-label-readability.athena'),
        path.join('src', '04-boundary-scope.athena')
    ];
    const missing = requiredFiles.filter(filePath => !fs.existsSync(path.join(repositoryRoot, filePath)));
    if (missing.length > 0) {
        throw new Error(
            `Athena M21 sample project could not resolve the checked-in proof repository at ${repositoryRoot}. Missing: ${missing.join(', ')}`
        );
    }
    return repositoryRoot;
}

main().catch(error => {
    console.error(error.stack || String(error));
    process.exit(1);
});
