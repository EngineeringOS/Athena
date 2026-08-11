const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { spawn } = require('node:child_process');

const repoRoot = path.resolve(__dirname, '..', '..', '..');
const workspaceRoot = path.join(repoRoot, 'examples', 'm46', 'rolling-shutter');
const sourcePath = path.join(workspaceRoot, 'src', 'com', 'engineeringood', 'm46', 'rollingshutter', 'rolling-shutter.athena');
const evidencePath = path.join(repoRoot, '_bmad-output', 'implementation-artifacts', 'm46', 'performance', 'm46-connection-performance.json');
const thresholds = { panZoomP95Ms: 22, selectionP95Ms: 100, localReplanP95Ms: 250 };

run().catch(error => { console.error(error.stack || String(error)); process.exit(1); });

async function run() {
    if (!fs.existsSync(sourcePath)) throw new Error(`M46 source missing: ${sourcePath}`);
    const result = await launch();
    await terminateProcessTree(result.launcherPid);
    const raw = result.result;
    if (!raw || !Array.isArray(raw.samples)) throw new Error('M46 benchmark returned no raw samples.');
    const metrics = Object.fromEntries([
        ['panZoomP95Ms', p95(raw.samples.filter(sample => sample.kind === 'PAN_ZOOM'))],
        ['selectionP95Ms', p95(raw.samples.filter(sample => sample.kind === 'SELECTION'))],
        ['localReplanP95Ms', p95(raw.samples.filter(sample => sample.kind === 'LOCAL_REPLAN'))],
    ]);
    const samplesFinite = raw.samples.length > 0 && raw.samples.every(sample => Number.isFinite(sample.durationMs));
    const environment = {
        os: `${os.platform()} ${os.release()}`,
        cpu: os.cpus()[0]?.model || 'unknown',
        nodeVersion: process.versions.node,
        electronVersion: require('electron/package.json').version,
        konvaVersion: raw.environment?.konvaVersion,
        viewport: raw.environment?.viewport,
        dpr: raw.environment?.dpr,
    };
    const bounded = raw.samples.filter(sample => sample.kind === 'LOCAL_REPLAN').every(sample =>
        sample.changedConnectionIds.length > 0 && sample.changedConnectionIds.length < raw.connectionCount
        && sample.repaintedConnectionIds.every(id => sample.changedConnectionIds.includes(id))
        && sample.paintCount < raw.fullScenePaintCount);
    const gates = {
        workspace: fs.existsSync(workspaceRoot),
        environment: [environment.os, environment.cpu, environment.nodeVersion, environment.electronVersion, environment.konvaVersion].every(value => typeof value === 'string' && value.length > 0 && value !== 'unknown')
            && Number.isFinite(environment.viewport?.width) && Number.isFinite(environment.viewport?.height) && Number.isFinite(environment.dpr),
        profile: raw.profileRevision === 'athena.connection-performance.v1' && raw.connectionCount === 1000 && /^sha256:[0-9a-f]{64}$/.test(raw.fixtureDigest),
        samples: samplesFinite && ['PAN_ZOOM', 'SELECTION', 'LOCAL_REPLAN'].every(kind => raw.samples.filter(sample => sample.kind === kind).length >= 20),
        panZoom: metrics.panZoomP95Ms <= thresholds.panZoomP95Ms,
        selection: metrics.selectionP95Ms <= thresholds.selectionP95Ms,
        localReplan: metrics.localReplanP95Ms <= thresholds.localReplanP95Ms,
        identity: raw.identityErrors === 0 && raw.samples.every(sample => sample.identityErrors === 0),
        trace: raw.traceErrors === 0 && raw.samples.every(sample => sample.traceErrors === 0),
        unhandled: raw.unhandledErrors === 0 && raw.samples.every(sample => sample.unhandledErrors === 0),
        boundedInvalidation: bounded,
        revision: raw.acceptedInputRevision && raw.samples.every(sample => sample.acceptedInputRevision === raw.acceptedInputRevision),
    };
    const evidence = {
        schemaVersion: 'M46.connection-performance-proof',
        status: Object.values(gates).every(Boolean) ? 'passed' : 'failed',
        profile: { revision: raw.profileRevision, connectionCount: raw.connectionCount, fixtureDigest: raw.fixtureDigest },
        environment,
        adapterEnvironment: raw.environment,
        workspaceRoot,
        sourcePath,
        samples: raw.samples,
        metrics,
        paint: { changedConnectionIds: raw.changedConnectionIds, repaintedConnectionIds: raw.repaintedConnectionIds, fullScenePaintCount: raw.fullScenePaintCount, incrementalPaintCount: raw.incrementalPaintCount },
        thresholds,
        gates,
    };
    fs.mkdirSync(path.dirname(evidencePath), { recursive: true });
    fs.writeFileSync(evidencePath, `${JSON.stringify(evidence, null, 2)}\n`, 'utf8');
    if (!Object.values(gates).every(Boolean)) throw new Error(`M46 connection performance gates failed: ${JSON.stringify(gates)}; metrics: ${JSON.stringify(metrics)}`);
    console.log(`Athena M46 connection performance proof passed: ${evidencePath}`);
}

function launch() {
    return new Promise((resolve, reject) => {
        const child = spawn(require('electron'), [path.join(__dirname, 'athena-m46-performance-main.js'), workspaceRoot], {
            cwd: path.resolve(__dirname, '..'),
            env: { ...process.env, ATHENA_M46_SOURCE_PATH: sourcePath, ATHENA_M46_TIMEOUT_MS: '300000', ELECTRON_ENABLE_LOGGING: '1' },
            stdio: ['ignore', 'pipe', 'pipe'], windowsHide: true,
        });
        const lines = []; let pending = ''; let result;
        const consume = chunk => { pending += String(chunk); const parts = pending.split(/\r?\n/); pending = parts.pop() || ''; for (const line of parts) { const value = line.trim(); if (!value) continue; lines.push(value); if (value.startsWith('ATHENA_M46_CONNECTION_PERFORMANCE=')) result = JSON.parse(value.slice('ATHENA_M46_CONNECTION_PERFORMANCE='.length)); } };
        child.stdout.on('data', consume); child.stderr.on('data', consume);
        const timer = setTimeout(() => terminateProcessTree(child.pid), 310000);
        child.once('error', reject);
        child.once('close', code => { clearTimeout(timer); consume('\n'); if (code !== 0 || !result) reject(new Error(`M46 performance process failed (${code})\n${lines.join('\n')}`)); else resolve(result); });
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
    process.kill(pid, 'SIGKILL');
    return Promise.resolve();
}

function p95(samples) {
    if (samples.length === 0 || samples.some(sample => !Number.isFinite(sample.durationMs))) return Number.POSITIVE_INFINITY;
    const sorted = samples.map(sample => sample.durationMs).sort((a, b) => a - b);
    return sorted[Math.ceil(sorted.length * 0.95) - 1];
}
