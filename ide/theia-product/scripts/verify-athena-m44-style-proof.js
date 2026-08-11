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
const proofPath = path.join(transcriptsRoot, '2-2-style-authoring-product-proof.json');

async function main() {
    const sourceBefore = snapshot(sourceRepository);
    const tempRoot = fs.mkdtempSync(path.join(os.tmpdir(), 'athena-m44-style-proof-'));
    const repositoryRoot = path.join(tempRoot, 'rolling-shutter');
    fs.cpSync(sourceRepository, repositoryRoot, { recursive: true });
    const sourceRoot = path.join(repositoryRoot, 'src', 'com', 'engineeringood', 'm44', 'rollingshutter');
    const sourcePath = path.join(sourceRoot, 'rolling-shutter.athena');
    const stylePath = path.join(sourceRoot, 'rolling-shutter.sheet.style.athena');
    const before = snapshot(repositoryRoot);
    if (fs.existsSync(stylePath)) throw new Error('M44 source example must begin without a solidified Style Companion.');

    try {
        const product = await runProduct(repositoryRoot, sourcePath, stylePath);
        const after = snapshot(repositoryRoot);
        const changed = changedPaths(before, after);
        const expectedStylePath = 'src/com/engineeringood/m44/rollingshutter/rolling-shutter.sheet.style.athena';
        if (changed.length !== 1 || changed[0] !== expectedStylePath) {
            throw new Error(`Only Style Companion may change during solidify; changed: ${changed.join(', ')}`);
        }
        const style = fs.readFileSync(stylePath, 'utf8');
        if (!style.includes('width: 3') || !style.includes('route-marker: end-arrow')) {
            throw new Error('Solidified Style Companion does not contain previewed route style.');
        }
        const sourceAfter = snapshot(sourceRepository);
        if (JSON.stringify(sourceBefore) !== JSON.stringify(sourceAfter)) throw new Error('Product proof mutated active M44 example.');
        for (const name of ['accepted', 'rulers-desktop', 'rulers-narrow', 'preview', 'discarded', 'solidified']) {
            const screenshot = path.join(screenshotsRoot, `m44-style-${name}.png`);
            if (!fs.existsSync(screenshot) || fs.statSync(screenshot).size < 10000) throw new Error(`${name} screenshot missing or empty.`);
        }
        const proof = {
            schemaVersion: 'M44.style-authoring-proof',
            activeExampleUnchanged: true,
            writableFiles: changed,
            before,
            after,
            styleCompanion: { relativePath: expectedStylePath, sha256: after[expectedStylePath], source: style },
            product,
            screenshots: ['accepted', 'rulers-desktop', 'rulers-narrow', 'preview', 'discarded', 'solidified'].map(name =>
                slash(path.join(screenshotsRoot, `m44-style-${name}.png`))),
        };
        fs.mkdirSync(transcriptsRoot, { recursive: true });
        fs.writeFileSync(proofPath, `${JSON.stringify(proof, null, 2)}\n`, 'utf8');
        console.log(`Athena M44 style proof passed: ${proofPath}`);
    } finally {
        fs.rmSync(tempRoot, { recursive: true, force: true });
    }
}

function runProduct(repositoryRoot, sourcePath, stylePath) {
    return new Promise((resolve, reject) => {
        const electron = require('electron');
        const entry = path.join(__dirname, 'athena-m44-style-proof-main.js');
        const child = spawn(electron, [entry, repositoryRoot], {
            cwd: path.resolve(__dirname, '..'),
            env: {
                ...process.env,
                ATHENA_M44_SOURCE_PATH: sourcePath,
                ATHENA_M44_STYLE_PATH: stylePath,
                ATHENA_M44_SCREENSHOT_ROOT: screenshotsRoot,
                ELECTRON_ENABLE_LOGGING: '1',
            },
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true,
        });
        const lines = [];
        let proof;
        const consume = chunk => String(chunk).split(/\r?\n/).forEach(line => {
            const value = line.trim();
            if (!value) return;
            lines.push(value);
            if (value.startsWith('ATHENA_M44_STYLE_PROOF=')) proof = JSON.parse(value.slice('ATHENA_M44_STYLE_PROOF='.length));
        });
        child.stdout.on('data', consume);
        child.stderr.on('data', consume);
        const timeout = setTimeout(() => child.kill(), 150000);
        child.once('error', error => { clearTimeout(timeout); reject(error); });
        child.once('close', code => {
            clearTimeout(timeout);
            if (code !== 0 || !proof) reject(new Error(`M44 style proof failed with exit ${code}.\n${lines.join('\n')}`));
            else resolve(proof);
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
