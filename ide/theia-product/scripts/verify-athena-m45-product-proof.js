const fs = require('node:fs');
const path = require('node:path');
const { spawn } = require('node:child_process');

const repoRoot = path.resolve(__dirname, '..', '..', '..');
const workspaceRoot = path.join(repoRoot, 'examples', 'm45', 'rolling-shutter');
const sourcePath = path.join(workspaceRoot, 'src', 'com', 'engineeringood', 'm45', 'rollingshutter', 'rolling-shutter.athena');
const artifactsRoot = path.join(repoRoot, '_bmad-output', 'implementation-artifacts', 'm45', 'screenshots');

async function main() {
    if (!fs.existsSync(sourcePath)) throw new Error(`M45 source missing: ${sourcePath}`);
    fs.mkdirSync(artifactsRoot, { recursive: true });
    const viewports = [
        ['desktop-1920x1080', 1920, 1080],
        ['narrow-720x900', 720, 900],
    ];
    const proofs = [];
    for (const [name, width, height] of viewports) {
        proofs.push(await runViewport(name, width, height));
    }
    const output = path.join(repoRoot, '_bmad-output', 'implementation-artifacts', 'm45', 'm45-product-proof.json');
    fs.writeFileSync(output, `${JSON.stringify({ schemaVersion: 'M45.product-proof', workspaceRoot, sourcePath, viewports: proofs }, null, 2)}\n`, 'utf8');
    console.log(`Athena M45 product proof passed: ${output}`);
}

function runViewport(name, width, height) {
    return new Promise((resolve, reject) => {
        const electron = require('electron');
        const screenshotPath = path.join(artifactsRoot, `m45-rolling-shutter-${name}.png`);
        const child = spawn(electron, [path.join(__dirname, 'athena-m45-proof-main.js'), workspaceRoot, sourcePath], {
            cwd: path.resolve(__dirname, '..'),
            env: { ...process.env, ATHENA_M45_WIDTH: String(width), ATHENA_M45_HEIGHT: String(height), ATHENA_M45_SCREENSHOT: screenshotPath, ATHENA_M45_TEMP_USER_DATA: '1' },
            stdio: ['ignore', 'pipe', 'pipe'],
        });
        let proof;
        const lines = [];
        const consumeLine = line => {
            const value = line.trim();
            if (!value) return;
            lines.push(value);
            if (value.startsWith('ATHENA_M45_PROOF=')) proof = JSON.parse(value.slice(17));
        };
        const createLineConsumer = () => {
            let lineBuffer = '';
            return {
                consume(chunk) {
                    lineBuffer += String(chunk);
                    let newlineIndex = lineBuffer.indexOf('\n');
                    while (newlineIndex >= 0) {
                        consumeLine(lineBuffer.slice(0, newlineIndex));
                        lineBuffer = lineBuffer.slice(newlineIndex + 1);
                        newlineIndex = lineBuffer.indexOf('\n');
                    }
                },
                flush() {
                    consumeLine(lineBuffer);
                    lineBuffer = '';
                },
            };
        };
        const stdout = createLineConsumer();
        const stderr = createLineConsumer();
        child.stdout.on('data', chunk => stdout.consume(chunk));
        child.stderr.on('data', chunk => stderr.consume(chunk));
        const timer = setTimeout(() => child.kill(), 210000);
        child.once('error', reject);
        child.once('close', code => {
            clearTimeout(timer);
            stdout.flush();
            stderr.flush();
            if (code !== 0 || !proof) return reject(new Error(`M45 ${name} proof failed (${code})\n${lines.join('\n')}`));
            resolve({ name, width, height, ...proof });
        });
    });
}

main().catch(error => { console.error(error.stack || String(error)); process.exit(1); });
