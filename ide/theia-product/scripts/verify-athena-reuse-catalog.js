const fs = require('node:fs');
const path = require('node:path');
const { spawn } = require('node:child_process');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME=';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED=';
const ATHENA_REUSE_E2E_RESULT_SENTINEL = 'ATHENA_REUSE_CATALOG_E2E_RESULT=';
const ATHENA_REUSE_E2E_FAILURE_SENTINEL = 'ATHENA_REUSE_CATALOG_E2E_FAILURE=';
const STARTUP_TIMEOUT_MS = 90000;

async function main() {
    const repositoryRoot = resolveReuseProofRepository();
    const electronBinary = require('electron');
    const entryScript = path.resolve(__dirname, 'athena-electron-reuse-e2e-main.js');
    const workingDirectory = path.resolve(__dirname, '..');
    const child = spawn(
        electronBinary,
        [entryScript, repositoryRoot],
        {
            cwd: workingDirectory,
            env: {
                ...process.env,
                ATHENA_ELECTRON_SMOKE_EXIT_ON_REUSE_E2E: '1',
                ATHENA_E2E_REUSE_MACRO_ID: 'macro:dol-starter',
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
    let result;
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
        if (trimmedLine.startsWith(ATHENA_REUSE_E2E_RESULT_SENTINEL)) {
            result = JSON.parse(trimmedLine.substring(ATHENA_REUSE_E2E_RESULT_SENTINEL.length));
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
                `Athena reuse catalog E2E failed. windowCreated=${sawWindowCreated} ready=${sawReady} exitCode=${exitCode}\n${outputLines.join('\n')}`
            );
        }
        if (process.platform === 'win32' && !resolvedJavaHome && unresolvedJavaSignal) {
            throw new Error(`Athena reuse catalog E2E did not resolve Java 25: ${unresolvedJavaSignal}`);
        }
        if (!result) {
            const failureLine = outputLines.find(line => line.startsWith(ATHENA_REUSE_E2E_FAILURE_SENTINEL));
            throw new Error(
                `Athena reuse catalog E2E finished without a result payload.${failureLine ? `\n${failureLine}` : ''}`
            );
        }
        if (!String(result.catalogStatus || '').length) {
            throw new Error('Athena reuse catalog E2E did not observe a catalog status badge.');
        }
        if (result.previewStatus !== 'ready') {
            throw new Error(`Athena reuse catalog E2E expected ready preview status but saw ${result.previewStatus}.`);
        }
        if (!Array.isArray(result.componentTexts) || !result.componentTexts.some(text => text.includes('Main contactor'))) {
            throw new Error('Athena reuse catalog E2E did not observe the expected previewed Main contactor component.');
        }
        if (!String(result.approvalMessage || '').length) {
            throw new Error('Athena reuse catalog E2E did not observe an approval response message.');
        }
        if (!String(result.approvalMessage || '').includes('sole M8 mutation authority')) {
            throw new Error('Athena reuse catalog E2E did not observe the committed-through-M8 approval message.');
        }
        if (!String(result.approvalBundleId || '').length) {
            throw new Error('Athena reuse catalog E2E did not observe an acceptance bundle id.');
        }
        if (!(Number(result.approvalOperationCount) > 0)) {
            throw new Error('Athena reuse catalog E2E did not observe prepared mutation operations.');
        }
        if (!String(result.approvalCommandId || '').length) {
            throw new Error('Athena reuse catalog E2E did not observe the committed M8 command id.');
        }
        if (!(Number(result.approvalChangedCount) > 0)) {
            throw new Error('Athena reuse catalog E2E did not observe changed semantic ids after acceptance.');
        }
        if (result.originStatus !== 'ready') {
            throw new Error(`Athena reuse catalog E2E expected ready origin status but saw ${result.originStatus}.`);
        }
        if (!String(result.originExpansionId || '').length) {
            throw new Error('Athena reuse catalog E2E did not observe an origin expansion id.');
        }
        if (!String(result.originMatchedRole || '').includes('component:template:starter.contactor')) {
            throw new Error('Athena reuse catalog E2E did not observe the expected matched origin membership role.');
        }
        if (!(Number(result.originMembershipCount) > 0)) {
            throw new Error('Athena reuse catalog E2E did not observe accepted expansion memberships.');
        }
        if (!String(result.cancellationMessage || '').includes('Canonical state remains unchanged')) {
            throw new Error('Athena reuse catalog E2E did not observe the review-first cancellation message.');
        }
        if (result.persistentOriginStatus !== 'ready') {
            throw new Error(`Athena reuse catalog E2E expected origin traceability to persist after cancel but saw ${result.persistentOriginStatus}.`);
        }
        if (result.persistentOriginExpansionId !== result.originExpansionId) {
            throw new Error('Athena reuse catalog E2E observed origin traceability drift after cancel.');
        }

    console.log(
        `Athena reuse catalog E2E passed. previewStatus=${result.previewStatus} components=${result.componentCount} javaHome=${resolvedJavaHome || 'n/a'}`
    );
}

function resolveReuseProofRepository() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm16', 'semantic-reuse-proof');
    const requiredFiles = [
        'athena.yaml',
        'athena.lock',
        'athena-semantic-macros.properties',
        path.join('src', 'semantic-reuse-proof.athena')
    ];
    const missing = requiredFiles.filter(filePath => !fs.existsSync(path.join(repositoryRoot, filePath)));
    if (missing.length > 0) {
        throw new Error(
            `Athena reuse catalog E2E could not resolve the checked-in M16 proof repository at ${repositoryRoot}. Missing: ${missing.join(', ')}`
        );
    }
    return repositoryRoot;
}

main().catch(error => {
    console.error(error.stack || String(error));
    process.exit(1);
});
