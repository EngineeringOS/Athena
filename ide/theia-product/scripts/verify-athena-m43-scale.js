const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { spawn } = require('node:child_process');

const repoRoot = path.resolve(__dirname, '..', '..', '..');
const repositoryRoot = path.join(repoRoot, 'examples', 'm43', 'rolling-shutter');
const sourcePath = path.join(repositoryRoot, 'src', 'com', 'engineeringood', 'm43', 'rollingshutter', 'rolling-shutter.athena');
const artifactsRoot = path.join(repoRoot, '_bmad-output', 'implementation-artifacts', 'm43');
const evidencePath = path.join(artifactsRoot, 'm43-scale-benchmark.json');
const manifestPath = path.join(repoRoot, 'contracts', 'presentation', 'v1', 'benchmark', 'scene-100k-manifest.json');
const transcriptPath = path.join(repoRoot, 'contracts', 'presentation', 'v1', 'benchmark', 'interaction-transcript.json');

async function main() {
    const manifest = readJson(manifestPath);
    const transcript = readJson(transcriptPath);
    validateFixture(manifest, transcript);
    const run = await runElectron();
    const evidence = buildEvidence(manifest, transcript, run);
    fs.mkdirSync(artifactsRoot, { recursive: true });
    fs.writeFileSync(evidencePath, `${JSON.stringify(evidence, null, 2)}\n`, 'utf8');
    if (evidence.status !== 'passed') throw new Error(`M43 scale gates failed: ${JSON.stringify(evidence.gates)}`);
    console.log(`Athena M43 scale benchmark passed: ${evidencePath}`);
}

function validateFixture(manifest, transcript) {
    if (manifest.paintElementCount !== 100000 || manifest.visiblePercent !== 5) throw new Error('Scale manifest count/visibility drift.');
    if (manifest.viewportCss?.width !== 1600 || manifest.viewportCss?.height !== 1000 || manifest.cssDpr !== 1) throw new Error('Scale manifest viewport drift.');
    if (manifest.warmupFrames !== 60 || manifest.measuredFrames !== 300) throw new Error('Scale manifest frame drift.');
    if (JSON.stringify(transcript.operations) !== JSON.stringify(['pan', 'zoom', 'select', 'drag'])) throw new Error('Scale interaction transcript drift.');
    if (transcript.warmupFrames !== 60 || transcript.measuredFrames !== 300 || transcript.requiresStableSceneRevision !== true) throw new Error('Scale transcript gate drift.');
}

function runElectron() {
    return new Promise((resolve, reject) => {
        const electron = require('electron');
        const entry = path.join(__dirname, 'athena-m43-scale-main.js');
        const child = spawn(electron, [entry, repositoryRoot], {
            cwd: path.resolve(__dirname, '..'),
            env: { ...process.env, ATHENA_M43_SOURCE_PATH: sourcePath, ATHENA_M43_TEMP_USER_DATA: '1', ELECTRON_ENABLE_LOGGING: '1' },
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true,
        });
        const lines = [];
        let result;
        let pending = '';
        const consume = chunk => {
            pending += String(chunk);
            const parts = pending.split(/\r?\n/);
            pending = parts.pop() || '';
            parts.forEach(line => {
            const trimmed = line.trim();
            if (!trimmed) return;
            lines.push(trimmed);
            if (trimmed.startsWith('ATHENA_M43_SCALE=')) {
                const payload = trimmed.slice('ATHENA_M43_SCALE='.length);
                try { result = JSON.parse(payload); } catch { pending = `${trimmed}\n${pending}`; }
            }
            });
        };
        child.stdout.on('data', consume);
        child.stderr.on('data', consume);
        const timeout = setTimeout(() => child.kill(), 180000);
        child.once('error', error => { clearTimeout(timeout); reject(error); });
        child.once('close', code => {
            clearTimeout(timeout);
            if (code !== 0 || !result) {
                reject(new Error(`M43 scale process failed with exit ${code}.\n${lines.join('\n')}`));
                return;
            }
            resolve(result);
        });
    });
}

function buildEvidence(manifest, transcript, run) {
    const measurements = run.result;
    const gates = {
        firstStablePaint: measurements.firstStablePaintMs <= manifest.gates.firstStablePaintMs,
        incrementalHeap: measurements.incrementalHeapMiB !== null && measurements.incrementalHeapMiB <= manifest.gates.incrementalHeapMiB,
        interactionP95: measurements.interactionP95Ms <= manifest.gates.interactionP95Ms,
        selectionP95: measurements.selectionP95Ms <= manifest.gates.selectionP95Ms,
        exactSceneCount: measurements.scenePaintElementCount === manifest.paintElementCount,
        exactVisibleCount: measurements.visiblePaintElements === manifest.paintElementCount * manifest.visiblePercent / 100,
        stableSceneRevision: measurements.stableSceneRevision === true,
        identityErrors: measurements.identityErrors === manifest.gates.identityErrors,
        traceErrors: measurements.traceErrors === 0,
        unhandledErrors: measurements.unhandledErrors === 0,
        sampleCount: measurements.samples.length === manifest.measuredFrames,
    };
    return {
        schemaVersion: 'M43.scale-benchmark',
        status: Object.values(gates).every(Boolean) ? 'passed' : 'failed',
        fixture: { manifestPath: slash(path.relative(repoRoot, manifestPath)), transcriptPath: slash(path.relative(repoRoot, transcriptPath)), manifest, transcript },
        environment: {
            os: { platform: process.platform, release: os.release(), arch: process.arch },
            cpu: { model: os.cpus()[0]?.model || 'unknown', logicalCores: os.cpus().length },
            memoryMiB: Math.round(os.totalmem() / (1024 * 1024)),
            node: process.version,
            electronUserAgent: run.userAgent,
            viewport: run.viewport,
            adapter: 'KonvaDiagramAdapter@10.3.0',
        },
        measurements,
        gates,
    };
}

function readJson(file) { return JSON.parse(fs.readFileSync(file, 'utf8')); }
function slash(value) { return value.replaceAll('\\', '/'); }

main().catch(error => {
    console.error(error.stack || String(error));
    process.exit(1);
});
