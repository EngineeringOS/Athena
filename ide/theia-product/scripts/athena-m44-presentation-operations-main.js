const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { createHash } = require('node:crypto');
const { app } = require('electron');
const { AthenaJvmRuntimeResolver } = require('@engineeringood/athena-theia-backend/lib/node/athena-jvm-runtime-resolver.js');

const repositoryRoot = path.resolve(process.argv[2]);
const sourcePath = path.resolve(process.env.ATHENA_M44_SOURCE_PATH || '');
const screenshotRoot = path.resolve(process.env.ATHENA_M44_SCREENSHOT_ROOT || '.');
const mode = process.env.ATHENA_M44_PROOF_MODE || 'author';
const timeoutMs = Number(process.env.ATHENA_M44_TIMEOUT_MS || 120000);

new AthenaJvmRuntimeResolver().configureProcessEnvironment(process.env, process.platform);
app.setPath('userData', path.join(os.tmpdir(), `athena-m44-presentation-proof-${process.pid}`));

app.on('browser-window-created', (_event, window) => {
    window.webContents.once('did-finish-load', async () => {
        try {
            window.unmaximize();
            window.setBounds({ width: 1440, height: 960 });
            await waitForWindowBounds(window, 1440, 960);
            const ready = await waitForReady(window);
            await capture(window, mode === 'reopen' ? 'reopened' : 'accepted');
            if (mode === 'author') {
                const operations = await runAuthoringFlow(window, ready);
                console.log(`ATHENA_M44_PRESENTATION_PROOF=${JSON.stringify({ mode, ...operations })}`);
            } else {
                const state = await canvasState(window);
                console.log(`ATHENA_M44_PRESENTATION_PROOF=${JSON.stringify({ mode, ready, reopened: state })}`);
            }
            app.exit(0);
        } catch (error) {
            console.error(`Athena M44 presentation proof failed: ${error.stack || String(error)}`);
            app.exit(1);
        }
    });
});

require('../lib/backend/electron-main.js');

async function runAuthoringFlow(window, ready) {
    const state = await presentationState(window);
    const byLabel = label => state.occurrences.find(occurrence => occurrence.labels.includes(label) || occurrence.subjectId === label || occurrence.occurrenceId === label);
    const supply = byLabel('Supply');
    const q1 = byLabel('Q1');
    const km1 = byLabel('KM1');
    const m1 = byLabel('M1');
    const plc1 = byLabel('PLC1');
    const s2 = byLabel('S2');
    if (!supply || !q1 || !km1 || !m1 || !plc1 || !s2) {
        throw new Error(`Required occurrence labels unavailable: ${JSON.stringify({ state })}`);
    }
    await execute(window, 'clearEvidence');
    const operations = [];
    const before = await canvasState(window);
    operations.push({ name: 'before', before });

    const move = await execute(window, 'executeMove', supply.occurrenceId, { x: 12, y: 12 }, 'PRESERVE');
    await waitForSceneDigest(window, before.sceneDigest);
    await capture(window, 'desktop-after-move');
    operations.push({ name: 'move', result: move, after: await canvasState(window) });

    const snap = await execute(window, 'executeSnap', s2.occurrenceId);
    await waitForSceneDigest(window, operations[operations.length - 1].after.sceneDigest);
    await capture(window, 'desktop-after-snap');
    operations.push({ name: 'snap', result: snap, after: await canvasState(window) });

    const align = await execute(window, 'executeAlign', [supply.occurrenceId, plc1.occurrenceId], 'LEFT');
    await waitForSceneDigest(window, operations[operations.length - 1].after.sceneDigest);
    await capture(window, 'desktop-after-align');
    operations.push({ name: 'align', result: align, after: await canvasState(window) });

    const distribute = await execute(window, 'executeDistribute', [q1.occurrenceId, km1.occurrenceId, m1.occurrenceId], 'HORIZONTAL');
    await waitForSceneDigest(window, operations[operations.length - 1].after.sceneDigest);
    await capture(window, 'desktop-after-distribute');
    operations.push({ name: 'distribute', result: distribute, after: await canvasState(window) });

    const journalEntryId = distribute?.result?.acceptance?.journalEntryId;
    if (!journalEntryId) throw new Error('Accepted distribute operation did not publish a journal entry.');
    const undo = await execute(window, 'executeUndo', journalEntryId);
    await waitForSceneDigest(window, operations[operations.length - 1].after.sceneDigest);
    operations.push({ name: 'undo', result: undo, after: await canvasState(window) });

    const redo = await execute(window, 'executeRedo', journalEntryId);
    await waitForSceneDigest(window, operations[operations.length - 1].after.sceneDigest);
    operations.push({ name: 'redo', result: redo, after: await canvasState(window) });

    const staleRedo = await execute(window, 'executeRedo', journalEntryId);
    await delay(150);
    operations.push({ name: 'staleRedo', result: staleRedo, after: await canvasState(window) });

    const rejected = await execute(window, 'executeMove', m1.occurrenceId, { x: 9999, y: 9999 }, 'PRESERVE');
    await delay(150);
    await capture(window, 'rejected');
    operations.push({ name: 'rejectedMove', result: rejected, after: await canvasState(window) });

    window.unmaximize();
    window.setBounds({ width: 760, height: 720 });
    await waitForWindowBounds(window, 760, 720);
    await capture(window, 'narrow-after');

    return { ready, operations, rejection: rejected, finalState: await canvasState(window) };
}

async function presentationState(window) {
    const started = Date.now();
    while (Date.now() - started < timeoutMs) {
        const state = await window.webContents.executeJavaScript(`window.__athenaPresentationAutomation?.getState?.()`, true);
        if (state?.publicationState === 'READY') return state;
        await delay(250);
    }
    throw new Error('Presentation automation seam unavailable.');
}

async function execute(window, method, ...args) {
    return window.webContents.executeJavaScript(
        `window.__athenaPresentationAutomation.${method}(...${JSON.stringify(args)})`,
        true,
    );
}

async function waitForReady(window) {
    const started = Date.now();
    let lastCanvasDigest;
    let stableSamples = 0;
    let lastState;
    while (Date.now() - started < timeoutMs) {
        await window.webContents.executeJavaScript(`(${prepareWorkbench.toString()})(${JSON.stringify(repositoryRoot)}, ${JSON.stringify(sourcePath)})`, true);
        const state = await canvasState(window);
        lastState = state;
        if (state?.publicationState === 'READY' && state.canvasDigest && state.nonWhiteSamples > 0) {
            stableSamples = state.canvasDigest === lastCanvasDigest ? stableSamples + 1 : 0;
            lastCanvasDigest = state.canvasDigest;
            if (stableSamples >= 2) return state;
        }
        await delay(250);
    }
    throw new Error(`Timed out waiting for READY Presentation surface: ${JSON.stringify({ lastState })}`);
}

async function waitForSceneDigest(window, previousDigest) {
    const started = Date.now();
    while (Date.now() - started < 8000) {
        const state = await canvasState(window);
        if (state?.sceneDigest && state.sceneDigest !== previousDigest) return state;
        await delay(100);
    }
    throw new Error(`Scene digest did not change from ${previousDigest}.`);
}

async function waitForWindowBounds(window, expectedWidth, expectedHeight) {
    const started = Date.now();
    let actual = window.getBounds();
    while (Date.now() - started < 5000) {
        actual = window.getBounds();
        if (Math.abs(actual.width - expectedWidth) <= 2 && Math.abs(actual.height - expectedHeight) <= 2) {
            await delay(250);
            return actual;
        }
        await delay(100);
    }
    throw new Error(`Window bounds did not reach requested size: ${JSON.stringify({ expectedWidth, expectedHeight, actual })}`);
}

async function capture(window, name) {
    const started = Date.now();
    let lastError;
    while (Date.now() - started < 5000) {
        if (!window.isVisible()) window.show();
        if (window.isMinimized()) window.restore();
        try {
            const image = await window.webContents.capturePage();
            if (!image.isEmpty()) {
                fs.mkdirSync(screenshotRoot, { recursive: true });
                fs.writeFileSync(path.join(screenshotRoot, `m44-presentation-${name}.png`), image.toPNG());
                return;
            }
        } catch (error) {
            lastError = error;
        }
        await delay(100);
    }
    throw new Error(`${name} screenshot unavailable: ${lastError?.message || 'empty display surface'}`);
}

async function canvasState(window) {
    const raw = await window.webContents.executeJavaScript(`(() => {
        const shell = document.querySelector('.athena-presentation__canvas-shell');
        const canvases = Array.from(document.querySelectorAll('.athena-presentation__canvas-host canvas'));
        if (!shell || canvases.length === 0) return null;
        const nonWhiteSamples = canvases.reduce((total, canvas) => {
            const context = canvas.getContext('2d', { willReadFrequently: true });
            if (!context) return total;
            const pixels = context.getImageData(0, 0, canvas.width, canvas.height).data;
            let count = 0;
            for (let index = 0; index < pixels.length; index += 64) {
                if (pixels[index + 3] > 0 && (pixels[index] < 245 || pixels[index + 1] < 245 || pixels[index + 2] < 245)) count += 1;
            }
            return total + count;
        }, 0);
        return {
            publicationState: shell.dataset.publicationState || '',
            sceneId: shell.dataset.sceneId || '',
            sceneDigest: shell.dataset.sceneDigest || '',
            layers: canvases.map(canvas => canvas.toDataURL('image/png')),
            nonWhiteSamples,
            width: canvases[0].width,
            height: canvases[0].height,
        };
    })()`, true);
    if (!raw) return null;
    return {
        publicationState: raw.publicationState,
        sceneId: raw.sceneId,
        sceneDigest: raw.sceneDigest,
        canvasDigest: sha256(raw.layers.join('\n')),
        layerDigests: raw.layers.map(sha256),
        nonWhiteSamples: raw.nonWhiteSamples,
        width: raw.width,
        height: raw.height,
    };
}

async function prepareWorkbench(workspaceRoot, sourceFile) {
    const automation = window.__athenaWorkbenchAutomation;
    if (!window.theia?.container || !automation) return false;
    await automation.openWorkspaceAndSource(workspaceRoot, sourceFile);
    return true;
}

function sha256(value) {
    return createHash('sha256').update(value).digest('hex');
}

function delay(milliseconds) {
    return new Promise(resolve => setTimeout(resolve, milliseconds));
}
