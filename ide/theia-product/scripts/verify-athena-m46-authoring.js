const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { createHash } = require('node:crypto');
const { spawn } = require('node:child_process');

const repoRoot = path.resolve(__dirname, '..', '..', '..');
const activeRepository = path.join(repoRoot, 'examples', 'm46', 'rolling-shutter');
const artifactsRoot = path.join(repoRoot, '_bmad-output', 'implementation-artifacts', 'm46');
const screenshotsRoot = path.join(artifactsRoot, 'screenshots');
const transcriptRoot = path.join(artifactsRoot, 'operation-transcripts');
const proofPath = path.join(transcriptRoot, '4-3-author-reopen-product-proof.json');
const relativeSource = path.join('src', 'com', 'engineeringood', 'm46', 'rollingshutter', 'rolling-shutter.athena');
const relativeSheet = path.join('src', 'com', 'engineeringood', 'm46', 'rollingshutter', 'rolling-shutter.sheet.athena');

main().catch(error => {
    console.error(error.stack || String(error));
    process.exit(1);
});

async function main() {
    const activeBefore = snapshot(activeRepository);
    const tempRoot = fs.mkdtempSync(path.join(os.tmpdir(), 'athena-m46-authoring-'));
    const repositoryRoot = path.join(tempRoot, 'rolling-shutter');
    fs.cpSync(activeRepository, repositoryRoot, { recursive: true });
    const sourcePath = path.join(repositoryRoot, relativeSource);
    const before = snapshot(repositoryRoot);
    try {
        const author = await runProduct(repositoryRoot, sourcePath, 'author');
        const after = snapshot(repositoryRoot);
        verifyAuthoring(author);

        const writableFiles = changedPaths(before, after);
        const expectedWritableFiles = ['athena.lock', slash(relativeSheet), slash(relativeSource)].sort();
        if (JSON.stringify(writableFiles) !== JSON.stringify(expectedWritableFiles)) {
            throw new Error(`M46 authoring changed files outside Engineering/Sheet authority: ${writableFiles.join(', ')}`);
        }

        const reopen = await runProduct(repositoryRoot, sourcePath, 'reopen');
        const reopenedSnapshot = snapshot(repositoryRoot);
        if (JSON.stringify(after) !== JSON.stringify(reopenedSnapshot)) {
            throw new Error('Fresh-process reopen changed accepted source bytes.');
        }
        verifyReopen(author, reopen);

        const activeExampleUnchanged = JSON.stringify(activeBefore) === JSON.stringify(snapshot(activeRepository));
        if (!activeExampleUnchanged) throw new Error('M46 product proof mutated active example.');

        const proof = {
            schemaVersion: 'M46.author-reopen-proof',
            status: 'passed',
            activeExampleUnchanged,
            activeRepository,
            temporaryRepository: repositoryRoot,
            writableFiles,
            before,
            after,
            reopenedSnapshot,
            author,
            reopen,
            screenshots: [...author.screenshots, ...reopen.screenshots],
        };
        fs.mkdirSync(transcriptRoot, { recursive: true });
        fs.writeFileSync(proofPath, `${JSON.stringify(proof, null, 2)}\n`, 'utf8');
        console.log(`Athena M46 author/reopen proof passed: ${proofPath}`);
    } finally {
        fs.rmSync(tempRoot, { recursive: true, force: true });
    }
}

function verifyAuthoring(author) {
    if (author.mode !== 'author') throw new Error('M46 author process returned wrong mode.');
    const byName = name => author.operations.find(operation => operation.name === name);
    const valid = byName('validReconnect');
    const invalid = byName('invalidReconnect');
    const route = byName('routeAdjust');
    const undo = byName('undo');
    const redo = byName('redo');
    const stale = byName('staleRedo');
    for (const [name, operation] of Object.entries({ valid, invalid, route, undo, redo, stale })) {
        if (!operation) throw new Error(`Missing M46 author operation: ${name}`);
    }
    requireStatus(valid, 'ACCEPTED');
    requireStatus(invalid, 'REJECTED');
    requireStatus(route, 'ACCEPTED');
    requireStatus(undo, 'ACCEPTED');
    requireStatus(redo, 'ACCEPTED');
    requireStatus(stale, 'REJECTED');
    if (invalid.result.result.rejection?.reason !== 'INVALID') throw new Error('Direction-invalid reconnect must reject as INVALID.');
    if (stale.result.result.rejection?.reason !== 'STALE') throw new Error('Repeated Redo must reject as STALE.');
    if (author.pointerOnlyEvidenceCountBefore !== author.pointerOnlyEvidenceCountAfter) {
        throw new Error('Pointer-only interaction created an edit operation.');
    }
    if (invalid.state.presentation.acceptedInputRevision !== valid.state.presentation.acceptedInputRevision
        || invalid.state.presentation.sceneDigest !== valid.state.presentation.sceneDigest) {
        throw new Error('Rejected reconnect changed accepted revision or Scene.');
    }
    if (route.state.connectionReadModel.connectionIrDigest !== invalid.state.connectionReadModel.connectionIrDigest) {
        throw new Error('Presentation route adjustment changed compiler-owned Connection IR.');
    }
    if (stale.state.presentation.acceptedInputRevision !== redo.state.presentation.acceptedInputRevision
        || stale.state.presentation.sceneDigest !== redo.state.presentation.sceneDigest) {
        throw new Error('Stale Redo changed final accepted state.');
    }
    const beforeConnections = connectionIdentity(author.before.presentation);
    const finalConnections = connectionIdentity(author.finalState.presentation);
    const replacedConnectionId = valid.result.operation.body.connectionId;
    const resultingConnectionId = route.result.operation.body.connectionId;
    const affectedBefore = new Set([replacedConnectionId, ...connectionsTouchingPort(author.before.presentation, valid.result.operation.body.replacementPortId)]);
    const affectedFinal = new Set([resultingConnectionId, ...connectionsTouchingPort(author.finalState.presentation, valid.result.operation.body.replacementPortId)]);
    const unaffectedBefore = beforeConnections.filter(connection => !affectedBefore.has(connection.connectionId));
    const unaffectedFinal = finalConnections.filter(connection => !affectedFinal.has(connection.connectionId));
    if (JSON.stringify(unaffectedBefore) !== JSON.stringify(unaffectedFinal)) {
        throw new Error(`Unaffected Connection identity or trace changed across accepted authoring loop: ${JSON.stringify({ unaffectedBefore, unaffectedFinal })}`);
    }
    if (replacedConnectionId === resultingConnectionId) {
        throw new Error('Accepted reconnect did not publish a deterministic replacement Connection identity.');
    }
    verifyVisualState(author.finalState, 'desktop');
    verifyVisualState(author.narrowState, 'narrow');
    verifyScreenshots(author.screenshots);
}

function verifyReopen(author, reopen) {
    if (reopen.mode !== 'reopen') throw new Error('M46 reopen process returned wrong mode.');
    const finalState = author.finalState;
    const reopened = reopen.reopened;
    const comparableFinal = acceptedFacts(finalState);
    const comparableReopened = acceptedFacts(reopened);
    if (JSON.stringify(comparableFinal) !== JSON.stringify(comparableReopened)) {
        throw new Error(`Fresh-process reopened facts differ from accepted author state: ${JSON.stringify({ comparableFinal, comparableReopened })}`);
    }
    verifyVisualState(reopened, 'reopened desktop');
    verifyVisualState(reopen.narrowState, 'reopened narrow');
    verifyScreenshots(reopen.screenshots);
}

function acceptedFacts(state) {
    return {
        acceptedInputRevision: state.presentation.acceptedInputRevision,
        sceneId: state.presentation.sceneId,
        sceneDigest: state.presentation.sceneDigest,
        connections: connectionIdentity(state.presentation),
        readModelRevision: state.connectionReadModel.acceptedInputRevision,
        connectionIrDigest: state.connectionReadModel.connectionIrDigest,
        readModelItems: state.connectionReadModel.items,
        canvasDigest: state.canvas.canvasDigest,
    };
}

function connectionIdentity(presentation) {
    return presentation.connections.map(connection => ({
        connectionId: connection.connectionId,
        projectionId: connection.projectionId,
        traceId: connection.traceId,
    })).sort((left, right) => left.connectionId.localeCompare(right.connectionId));
}

function connectionsTouchingPort(presentation, semanticPortId) {
    const anchorIds = new Set(presentation.ports
        .filter(port => port.semanticPortId === semanticPortId)
        .map(port => port.anchorId));
    return presentation.connections
        .filter(connection => anchorIds.has(connection.sourceAnchorId) || anchorIds.has(connection.targetAnchorId))
        .map(connection => connection.connectionId);
}

function verifyVisualState(state, label) {
    if (state.presentation?.publicationState !== 'READY' || state.connectionReadModel?.state !== 'READY' || state.canvas?.publicationState !== 'READY') {
        throw new Error(`${label} product state is not READY.`);
    }
    const failed = Object.entries(state.canvas.visualChecks || {}).filter(([, value]) => !value).map(([name]) => name);
    if (failed.length > 0) throw new Error(`${label} visual checks failed: ${failed.join(', ')}`);
    if (!(state.canvas.nonWhiteSamples > 0)) throw new Error(`${label} canvas is blank.`);
}

function verifyScreenshots(screenshots) {
    if (!Array.isArray(screenshots) || screenshots.length !== 2) throw new Error('Desktop and narrow screenshots are required.');
    for (const screenshot of screenshots) {
        if (!fs.existsSync(screenshot.path) || fs.statSync(screenshot.path).size === 0) throw new Error(`Screenshot unavailable: ${screenshot.path}`);
        if (!(screenshot.width > 0 && screenshot.height > 0 && /^[0-9a-f]{64}$/.test(screenshot.digest))) {
            throw new Error(`Screenshot evidence invalid: ${JSON.stringify(screenshot)}`);
        }
    }
}

function requireStatus(operation, expected) {
    if (operation.result?.result?.status !== expected) {
        throw new Error(`${operation.name} expected ${expected}: ${JSON.stringify(operation.result)}`);
    }
}

function runProduct(repositoryRoot, sourcePath, mode) {
    return new Promise((resolve, reject) => {
        const child = spawn(require('electron'), [path.join(__dirname, 'athena-m46-authoring-main.js'), repositoryRoot], {
            cwd: path.resolve(__dirname, '..'),
            env: {
                ...process.env,
                ATHENA_M46_SOURCE_PATH: sourcePath,
                ATHENA_M46_SCREENSHOT_ROOT: screenshotsRoot,
                ATHENA_M46_AUTHORING_MODE: mode,
                ATHENA_M46_TIMEOUT_MS: '180000',
                ELECTRON_ENABLE_LOGGING: '1',
            },
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true,
        });
        const lines = [];
        let pending = '';
        let result;
        const consume = chunk => {
            pending += String(chunk);
            const chunks = pending.split(/\r?\n/);
            pending = chunks.pop() || '';
            for (const line of chunks) {
                const value = line.trim();
                if (!value) continue;
                lines.push(value);
                if (value.startsWith('ATHENA_M46_AUTHORING_PROGRESS')) process.stderr.write(`${value}\n`);
                if (value.startsWith('ATHENA_M46_AUTHORING_PROOF=')) {
                    result = JSON.parse(value.slice('ATHENA_M46_AUTHORING_PROOF='.length));
                }
            }
        };
        child.stdout.on('data', consume);
        child.stderr.on('data', consume);
        const timeout = setTimeout(() => terminateProcessTree(child.pid), 200000);
        child.once('error', error => {
            clearTimeout(timeout);
            reject(error);
        });
        child.once('close', async code => {
            clearTimeout(timeout);
            consume('\n');
            await terminateProcessTree(result?.launcherPid || child.pid);
            if (code !== 0 || !result) {
                reject(new Error(`M46 ${mode} process failed (${code}).\n${lines.join('\n')}`));
                return;
            }
            resolve(result);
        });
    });
}

function terminateProcessTree(pid) {
    if (!pid) return Promise.resolve();
    if (process.platform === 'win32') {
        return new Promise(resolve => {
            const terminator = spawn('taskkill', ['/PID', String(pid), '/T', '/F'], { stdio: 'ignore', windowsHide: true });
            terminator.once('close', resolve);
            terminator.once('error', resolve);
        });
    }
    try { process.kill(pid, 'SIGKILL'); } catch { /* Process already exited. */ }
    return Promise.resolve();
}

function snapshot(root) {
    const result = {};
    const visit = directory => fs.readdirSync(directory, { withFileTypes: true })
        .sort((left, right) => left.name.localeCompare(right.name))
        .forEach(entry => {
            const absolute = path.join(directory, entry.name);
            if (entry.isDirectory()) visit(absolute);
            else result[slash(path.relative(root, absolute))] = sha256(fs.readFileSync(absolute));
        });
    visit(root);
    return result;
}

function changedPaths(before, after) {
    return [...new Set([...Object.keys(before), ...Object.keys(after)])]
        .filter(key => before[key] !== after[key])
        .sort();
}

function sha256(value) {
    return createHash('sha256').update(value).digest('hex');
}

function slash(value) {
    return value.replaceAll('\\', '/');
}
