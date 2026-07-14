const path = require('node:path');
const { spawn } = require('node:child_process');
const { pathToFileURL } = require('node:url');
const { AthenaRepositorySessionManager } = require('@engineeringood/athena-theia-backend/lib/node/athena-repository-session-manager.js');
const { AthenaJvmRuntimeResolver } = require('@engineeringood/athena-theia-backend/lib/node/athena-jvm-runtime-resolver.js');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME=';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED=';
const STARTUP_TIMEOUT_MS = 30000;

async function main() {
    const electronBinary = require('electron');
    const entryScript = path.resolve(__dirname, 'athena-electron-main.js');
    const workingDirectory = path.resolve(__dirname, '..');

    const child = spawn(
        electronBinary,
        [entryScript],
        {
            cwd: workingDirectory,
            env: {
                ...process.env,
                ATHENA_ELECTRON_SMOKE_EXIT_ON_READY: '1',
                ELECTRON_ENABLE_LOGGING: '1'
            },
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true
        }
    );

    let sawWindowCreated = false;
    let sawReady = false;
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
        const details = outputLines.join('\n');
        throw new Error(
            `Athena desktop smoke start failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}\n${details}`
        );
    }

    if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
        throw new Error(`Athena desktop smoke start did not resolve Java 25: ${unresolvedJavaSignal}`);
    }

    await verifyRepositorySession();
    console.log(`Athena desktop smoke start passed. ready=${sawReady} javaHome=${resolvedJavaHome || 'n/a'}`);
}

async function verifyRepositorySession() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm4', 'open-repository-proof');
    const documentPath = path.join(repositoryRoot, 'src', 'factory-line.athena');
    const documentUri = pathToFileURL(documentPath).toString();
    const manager = new AthenaRepositorySessionManager();

    manager.logger = {
        info: () => undefined,
        warn: () => undefined,
        error: () => undefined,
        debug: () => undefined,
        trace: () => undefined,
        log: () => undefined
    };
    manager.jvmRuntimeResolver = new AthenaJvmRuntimeResolver();

    const activation = await manager.ensureRepositorySessionForDocument(documentUri);
    if (activation.lifecycle !== 'ready') {
        await manager.dispose();
        throw new Error(`Athena repository session smoke check failed: ${activation.message}`);
    }

    await manager.dispose();
}

main().catch(error => {
    console.error(error.stack || String(error));
    process.exit(1);
});
