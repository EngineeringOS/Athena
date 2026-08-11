const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { spawn } = require('node:child_process');

const repoRoot = path.resolve(__dirname, '..', '..', '..');
const repositoryRoot = path.join(repoRoot, 'examples', 'm44', 'rolling-shutter');
const sourcePath = path.join(repositoryRoot, 'src', 'com', 'engineeringood', 'm44', 'rollingshutter', 'rolling-shutter.athena');
const evidencePath = path.join(repoRoot, '_bmad-output', 'implementation-artifacts', 'm44', 'performance', 'm44-konva-benchmark.json');

function run() {
    return new Promise((resolve, reject) => {
        const electron = require('electron');
        const entry = path.join(__dirname, 'athena-m43-scale-main.js');
        const child = spawn(electron, [entry, repositoryRoot], {
            cwd: path.resolve(__dirname, '..'),
            env: { ...process.env, ATHENA_M43_SOURCE_PATH: sourcePath, ATHENA_M43_TEMP_USER_DATA: '1', ATHENA_M43_TIMEOUT_MS: '180000', ATHENA_SCALE_BENCHMARK_HOOK: '__athenaRunM44PerformanceBenchmark', ELECTRON_ENABLE_LOGGING: '1' },
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true,
        });
        const lines = [];
        let pending = '';
        let result;
        const consume = chunk => {
            pending += String(chunk);
            const parts = pending.split(/\r?\n/);
            pending = parts.pop() || '';
            parts.forEach(line => {
                const value = line.trim();
                if (!value) return;
                lines.push(value);
                if (value.startsWith('ATHENA_M43_SCALE=')) result = JSON.parse(value.slice('ATHENA_M43_SCALE='.length));
            });
        };
        child.stdout.on('data', consume); child.stderr.on('data', consume);
        const timeout = setTimeout(() => child.kill(), 180000);
        child.once('error', error => { clearTimeout(timeout); reject(error); });
        child.once('close', code => {
            clearTimeout(timeout);
            consume('\n');
            if (code !== 0 || !result) reject(new Error(`M44 performance process failed with exit ${code}.\n${lines.join('\n')}`));
            else resolve(result);
        });
    });
}

run().then(result => {
    const measurements = result.result;
    const evidence = {
        schemaVersion: 'M44.performance-proof',
        status: 'passed',
        profile: {
            os: `${os.platform()} ${os.release()}`,
            viewport: result.viewport,
            dpr: result.viewport?.dpr ?? 1,
            adapter: 'KonvaDiagramAdapter@10.3.0',
            fixture: 'M44 rolling-shutter 300-real-occurrence authoring profile',
            note: 'Profile clones admitted active-scene occurrences and assets for measurement only; source authority remains M44 Canonical Scene.',
        },
        measurements,
        gates: {
            panZoomP95Ms: measurements.panZoomP95Ms <= 50,
            dragPreviewP95Ms: measurements.dragPreviewP95Ms <= 100,
            incrementalHeapMiB: measurements.incrementalHeapMiB !== null && measurements.incrementalHeapMiB <= 20,
            selectionP95Ms: measurements.selectionP95Ms <= 100,
            stableSceneRevision: measurements.stableSceneRevision === true,
            identityErrors: measurements.identityErrors === 0,
            traceErrors: measurements.traceErrors === 0,
            unhandledErrors: measurements.unhandledErrors === 0,
        },
    };
    fs.mkdirSync(path.dirname(evidencePath), { recursive: true });
    fs.writeFileSync(evidencePath, `${JSON.stringify(evidence, null, 2)}\n`, 'utf8');
    if (!Object.values(evidence.gates).every(Boolean)) throw new Error(`M44 performance gates failed: ${JSON.stringify(evidence.gates)}; measurements: ${JSON.stringify(measurements)}`);
    console.log(`Athena M44 performance proof passed: ${evidencePath}`);
}).catch(error => { console.error(error.stack || String(error)); process.exit(1); });
