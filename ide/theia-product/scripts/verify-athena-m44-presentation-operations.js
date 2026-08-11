const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { createHash } = require('node:crypto');
const { spawn } = require('node:child_process');

const repoRoot = path.resolve(__dirname, '..', '..', '..');
const sourceRepository = path.join(repoRoot, 'examples', 'm44', 'rolling-shutter');
const artifactsRoot = path.join(repoRoot, '_bmad-output', 'implementation-artifacts', 'm44');
const screenshotsRoot = path.join(artifactsRoot, 'screenshots');
const transcriptsRoot = path.join(artifactsRoot, 'operation-transcripts');
const proofPath = path.join(transcriptsRoot, '3-2-presentation-operations-product-proof.json');
const styleProofPath = path.join(transcriptsRoot, '2-2-style-authoring-product-proof.json');

async function main() {
    const sourceBefore = snapshot(sourceRepository);
    const tempRoot = fs.mkdtempSync(path.join(os.tmpdir(), 'athena-m44-presentation-proof-'));
    const repositoryRoot = path.join(tempRoot, 'rolling-shutter');
    fs.cpSync(sourceRepository, repositoryRoot, { recursive: true });
    const sourceRoot = path.join(repositoryRoot, 'src', 'com', 'engineeringood', 'm44', 'rollingshutter');
    const sourcePath = path.join(sourceRoot, 'rolling-shutter.athena');
    const sheetPath = path.join(sourceRoot, 'rolling-shutter.sheet.athena');
    const before = snapshot(repositoryRoot);

    fs.writeFileSync(
        sheetPath,
        fs.readFileSync(sheetPath, 'utf8')
            .replace('snap: 1', 'snap: 4')
            .replace('"S2" at (56, 36)', '"S2" at (55, 35)'),
        'utf8',
    );

    try {
        const author = await runProduct(repositoryRoot, sourcePath, 'author');
        const after = snapshot(repositoryRoot);
        const undo = author.operations.find(operation => operation.name === 'undo');
        const redo = author.operations.find(operation => operation.name === 'redo');
        const staleRedo = author.operations.find(operation => operation.name === 'staleRedo');
        if (undo?.result?.result?.status !== 'ACCEPTED' || redo?.result?.result?.status !== 'ACCEPTED') {
            throw new Error('Undo and Redo must publish accepted journal transactions.');
        }
        if (staleRedo?.result?.result?.status !== 'REJECTED' || staleRedo.result.result.rejection?.reason !== 'STALE') {
            throw new Error('Repeated Redo must reject as stale without mutation.');
        }
        if (staleRedo.after.sceneDigest !== redo.after.sceneDigest) {
            throw new Error('Failed stale Redo changed accepted scene.');
        }
        const changed = changedPaths(before, after);
        const expectedSheetPath = 'src/com/engineeringood/m44/rollingshutter/rolling-shutter.sheet.athena';
        if (changed.length !== 1 || changed[0] !== expectedSheetPath) {
            throw new Error(`Only Sheet Companion may change during Presentation operations; changed: ${changed.join(', ')}`);
        }

        const reopened = await runProduct(repositoryRoot, sourcePath, 'reopen');
        const reopenedSnapshot = snapshot(repositoryRoot);
        if (JSON.stringify(after) !== JSON.stringify(reopenedSnapshot)) {
            throw new Error('Reopened Presentation proof did not preserve authored Sheet state.');
        }
        if (!fs.existsSync(styleProofPath)) {
            throw new Error('Style proof evidence missing; run style proof before presentation proof.');
        }

        const proof = {
            schemaVersion: 'M44.presentation-operations-proof',
            activeExampleUnchanged: JSON.stringify(sourceBefore) === JSON.stringify(snapshot(sourceRepository)),
            writableFiles: changed,
            before,
            after,
            reopened,
            reopenedSnapshot,
            operations: author.operations,
            rejection: author.rejection,
            styleProofPath: slash(styleProofPath),
            screenshots: ['accepted', 'desktop-after-move', 'desktop-after-snap', 'desktop-after-align', 'desktop-after-distribute', 'rejected', 'narrow-after'].map(name =>
                slash(path.join(screenshotsRoot, `m44-presentation-${name}.png`))),
        };
        if (!proof.activeExampleUnchanged) throw new Error('Product proof mutated active M44 example.');
        fs.mkdirSync(transcriptsRoot, { recursive: true });
        fs.writeFileSync(proofPath, `${JSON.stringify(proof, null, 2)}\n`, 'utf8');
        console.log(`Athena M44 presentation proof passed: ${proofPath}`);
    } finally {
        fs.rmSync(tempRoot, { recursive: true, force: true });
    }
}

function runProduct(repositoryRoot, sourcePath, mode) {
    return new Promise((resolve, reject) => {
        const electron = require('electron');
        const entry = path.join(__dirname, 'athena-m44-presentation-operations-main.js');
        const child = spawn(electron, [entry, repositoryRoot], {
            cwd: path.resolve(__dirname, '..'),
            env: {
                ...process.env,
                ATHENA_M44_SOURCE_PATH: sourcePath,
                ATHENA_M44_SCREENSHOT_ROOT: screenshotsRoot,
                ATHENA_M44_PROOF_MODE: mode,
                ELECTRON_ENABLE_LOGGING: '1',
            },
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true,
        });
        const lines = [];
        let proof;
        const consume = chunk => String(chunk).split(/\r?\n/).forEach(line => {
            const trimmed = line.trim();
            if (!trimmed) return;
            lines.push(trimmed);
            if (trimmed.startsWith('ATHENA_M44_PRESENTATION_PROOF=')) proof = JSON.parse(trimmed.slice('ATHENA_M44_PRESENTATION_PROOF='.length));
        });
        child.stdout.on('data', consume);
        child.stderr.on('data', consume);
        const timeout = setTimeout(() => child.kill(), 180000);
        child.once('error', error => {
            clearTimeout(timeout);
            reject(error);
        });
        child.once('close', code => {
            clearTimeout(timeout);
            if (code !== 0 || !proof) {
                reject(new Error(`M44 presentation proof failed with exit ${code}.\n${lines.join('\n')}`));
                return;
            }
            resolve(proof);
        });
    });
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

main().catch(error => {
    console.error(error.stack || String(error));
    process.exit(1);
});
