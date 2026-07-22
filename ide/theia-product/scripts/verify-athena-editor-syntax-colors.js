const path = require('node:path');
const { spawn } = require('node:child_process');

const ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL = 'ATHENA_GRAPH_WORKBENCH_PROOF=';
const STARTUP_TIMEOUT_MS = 120000;
const SOURCE_RELATIVE = 'src/01-governed-authoring-customer-source.athena';

async function main() {
    const repositoryRoot = path.resolve(__dirname, '..', '..', '..', 'examples', 'm31', 'sample-project');
    const proof = await runElectronSyntaxColorSmoke(repositoryRoot);
    const colorProof = proof.editorSyntaxColorProof;

    if (!colorProof || colorProof.skipped) {
        throw new Error(`Athena editor syntax color proof was skipped: ${JSON.stringify(colorProof)}`);
    }
    const requiredTokens = [
        'system',
        'direction',
        'out',
        'connect',
        '->',
        'layout',
        'place',
        'near',
        'align',
        'aligned-with',
        'axis',
        'vertical',
        'group',
        'grouped-with',
    ];
    const missing = requiredTokens.filter(token => !colorProof.selected?.[token]);
    if (missing.length > 0) {
        throw new Error(`Athena editor syntax color proof missing rendered tokens: ${missing.join(', ')}\n${JSON.stringify(colorProof, null, 2)}`);
    }
    if (colorProof.distinctCategoryColorCount < 4) {
        throw new Error(`Athena editor syntax color proof did not distinguish keyword families.\n${JSON.stringify(colorProof, null, 2)}`);
    }
    console.log(`Athena editor syntax color smoke passed. colors=${JSON.stringify(colorProof.selected)}`);
}

async function runElectronSyntaxColorSmoke(repositoryRoot) {
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
                ATHENA_ELECTRON_SMOKE_EDITOR_SYNTAX_COLORS: '1',
                ATHENA_ELECTRON_SMOKE_OUTLINE_SOURCE_RELATIVE: SOURCE_RELATIVE,
                ATHENA_ELECTRON_SMOKE_OUTLINE_EXPECTED_PATH: 'RollingShutterGovernedAuthoringProof > SpareTerminalXT31 > in1',
                ATHENA_ELECTRON_TEMP_USER_DATA: '1',
                ELECTRON_ENABLE_LOGGING: '1',
            },
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true,
        }
    );

    let graphWorkbenchProof;
    const outputLines = [];
    const recordLine = line => {
        const trimmedLine = line.trim();
        if (!trimmedLine) {
            return;
        }
        outputLines.push(trimmedLine);
        if (trimmedLine.startsWith(ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL)) {
            graphWorkbenchProof = JSON.parse(trimmedLine.substring(ATHENA_GRAPH_WORKBENCH_PROOF_SENTINEL.length));
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

    if (exitCode !== 0 || !graphWorkbenchProof) {
        throw new Error(`Athena editor syntax color smoke failed. exitCode=${exitCode}\n${outputLines.join('\n')}`);
    }
    return graphWorkbenchProof;
}

main().catch(error => {
    console.error(error.stack || String(error));
    process.exit(1);
});
