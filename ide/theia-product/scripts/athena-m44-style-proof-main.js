const fs = require('node:fs');
const path = require('node:path');
const { createHash } = require('node:crypto');
const { app } = require('electron');
const { AthenaJvmRuntimeResolver } = require('@engineeringood/athena-theia-backend/lib/node/athena-jvm-runtime-resolver.js');

const repositoryRoot = path.resolve(process.argv[2]);
const sourcePath = path.resolve(process.env.ATHENA_M44_SOURCE_PATH || '');
const stylePath = path.resolve(process.env.ATHENA_M44_STYLE_PATH || '');
const screenshotRoot = path.resolve(process.env.ATHENA_M44_SCREENSHOT_ROOT || '.');
const timeoutMs = Number(process.env.ATHENA_M44_TIMEOUT_MS || 120000);

new AthenaJvmRuntimeResolver().configureProcessEnvironment(process.env, process.platform);
app.setPath('userData', path.join(require('node:os').tmpdir(), `athena-m44-style-proof-${process.pid}`));

app.on('browser-window-created', (_event, window) => {
    window.webContents.once('did-finish-load', async () => {
        try {
            window.unmaximize();
            window.setBounds({ width: 1440, height: 960 });
            const desktopWindow = await waitForWindowBounds(window, 1440, 960);
            const accepted = await waitForReady(window);
            await capture(window, 'accepted');
            const desktopRulers = await rulerGeometry(window);
            await capture(window, 'rulers-desktop');

            window.unmaximize();
            window.setBounds({ width: 760, height: 720 });
            const narrowWindow = await waitForWindowBounds(window, 760, 720);
            const narrowRulers = await rulerGeometry(window);
            await capture(window, 'rulers-narrow');
            const acceptedForEdit = await waitForStableCanvas(window, accepted.sceneDigest);

            await setControl(window, 'Width', '3');
            await setControl(window, 'Route marker', 'END_ARROW');
            await clickCommand(window, "title='Preview style'");
            await delay(250);
            const preview = await canvasState(window);
            if (preview.sceneDigest !== acceptedForEdit.sceneDigest) throw new Error('Preview changed accepted scene digest.');
            if (preview.canvasDigest === acceptedForEdit.canvasDigest) throw new Error('Preview did not change paint output.');
            if (fs.existsSync(stylePath)) throw new Error('Preview wrote Style Companion.');
            await capture(window, 'preview');

            await clickCommand(window, "title='Discard style preview'");
            const discarded = await waitForCanvasDigest(window, acceptedForEdit);
            const acceptedSceneDigest = acceptedForEdit.sceneDigest;
            const discardedSceneDigest = discarded.sceneDigest;
            if (!(acceptedSceneDigest === discardedSceneDigest)) throw new Error('Discard changed accepted scene digest.');
            if (fs.existsSync(stylePath)) throw new Error('Discard wrote Style Companion.');
            await capture(window, 'discarded');

            await clickCommand(window, "title='Preview style'");
            await clickCommand(window, "title='Solidify style'");
            const solidified = await waitForSolidify(window, acceptedForEdit.sceneDigest);
            await capture(window, 'solidified');

            console.log(`ATHENA_M44_STYLE_PROOF=${JSON.stringify({ accepted, acceptedForEdit, desktopWindow, desktopRulers, narrowWindow, narrowRulers, preview, discarded, solidified })}`);
            app.exit(0);
        } catch (error) {
            console.error(`Athena M44 style proof failed: ${error.stack || String(error)}`);
            app.exit(1);
        }
    });
});

require('../lib/backend/electron-main.js');

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

async function waitForReady(window) {
    const started = Date.now();
    let lastCanvasDigest;
    let lastState;
    let workbenchPrepared = false;
    let stableSamples = 0;
    while (Date.now() - started < timeoutMs) {
        if (!workbenchPrepared) {
            workbenchPrepared = await window.webContents.executeJavaScript(
                `(${prepareWorkbench.toString()})(${JSON.stringify(repositoryRoot)}, ${JSON.stringify(sourcePath)})`,
                true,
            );
        }
        const state = await canvasState(window);
        lastState = state;
        if (state?.publicationState === 'READY' && state.canvasDigest && state.nonWhiteSamples > 0) {
            stableSamples = state.canvasDigest === lastCanvasDigest ? stableSamples + 1 : 0;
            lastCanvasDigest = state.canvasDigest;
            if (stableSamples >= 2) return state;
        }
        await delay(250);
    }
    throw new Error(`Timed out waiting for accepted M44 Engineering Document: ${JSON.stringify({ workbenchPrepared, lastState })}`);
}

async function setControl(window, ariaLabel, value) {
    const changed = await window.webContents.executeJavaScript(`(() => {
        const control = document.querySelector('[aria-label=${JSON.stringify(ariaLabel)}]');
        if (!control) return false;
        const prototype = control instanceof HTMLInputElement ? HTMLInputElement.prototype : HTMLSelectElement.prototype;
        Object.getOwnPropertyDescriptor(prototype, 'value').set.call(control, ${JSON.stringify(value)});
        control.dispatchEvent(new Event('change', { bubbles: true }));
        return true;
    })()`, true);
    if (!changed) throw new Error(`Style control ${ariaLabel} is unavailable.`);
    await delay(100);
}

async function clickCommand(window, selectorBody) {
    const clicked = await window.webContents.executeJavaScript(`(() => {
        const button = document.querySelector(\`button[${selectorBody}]\`);
        if (!button) return false;
        button.click();
        return true;
    })()`, true);
    if (!clicked) throw new Error(`Style command ${selectorBody} is unavailable.`);
    await delay(100);
}

async function waitForCanvasDigest(window, expected) {
    const started = Date.now();
    let current;
    while (Date.now() - started < 5000) {
        current = await canvasState(window);
        if (current.canvasDigest === expected.canvasDigest) return current;
        await delay(100);
    }
    throw new Error(`Discard did not restore exact accepted paint: ${JSON.stringify({ expected, current })}`);
}

async function waitForStableCanvas(window, expectedSceneDigest) {
    const started = Date.now();
    let lastCanvasDigest;
    let stableSamples = 0;
    let current;
    while (Date.now() - started < 5000) {
        current = await canvasState(window);
        if (current?.publicationState === 'READY' && current.sceneDigest === expectedSceneDigest) {
            stableSamples = current.canvasDigest === lastCanvasDigest ? stableSamples + 1 : 0;
            lastCanvasDigest = current.canvasDigest;
            if (stableSamples >= 2) return current;
        }
        await delay(100);
    }
    throw new Error(`Canvas did not settle after resize: ${JSON.stringify({ expectedSceneDigest, current })}`);
}

async function waitForSolidify(window, previousSceneDigest) {
    const started = Date.now();
    while (Date.now() - started < timeoutMs) {
        const state = await canvasState(window);
        if (fs.existsSync(stylePath) && state.publicationState === 'READY' && state.sceneDigest !== previousSceneDigest) return state;
        const operationError = await window.webContents.executeJavaScript(
            `document.querySelector('.athena-presentation__style-error')?.textContent || ''`,
            true,
        );
        if (operationError) throw new Error(`Solidify rejected: ${operationError}`);
        await delay(200);
    }
    throw new Error('Timed out waiting for server-owned Style Companion solidification.');
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

async function rulerGeometry(window) {
    const result = await window.webContents.executeJavaScript(`(() => {
        const frameElement = document.querySelector('.athena-presentation__document-frame');
        const columnsElement = document.querySelector('.athena-presentation__ruler--columns');
        const rowsElement = document.querySelector('.athena-presentation__ruler--rows');
        const canvasElement = document.querySelector('.athena-presentation__canvas-host');
        const viewportElement = document.querySelector('.athena-presentation__canvas-viewport');
        const toolbarElement = document.querySelector('.athena-presentation__style-bar');
        const shell = document.querySelector('.athena-presentation__canvas-shell');
        if (!frameElement || !columnsElement || !rowsElement || !canvasElement || !viewportElement || !toolbarElement || !shell) return null;
        const rect = element => {
            const value = element.getBoundingClientRect();
            return { left: value.left, top: value.top, right: value.right, bottom: value.bottom, width: value.width, height: value.height };
        };
        const frame = rect(frameElement);
        const columns = rect(columnsElement);
        const rows = rect(rowsElement);
        const canvas = rect(canvasElement);
        const viewport = rect(viewportElement);
        const toolbar = rect(toolbarElement);
        const columnCells = Array.from(columnsElement.children).map(element => ({ ...rect(element), text: element.textContent }));
        const rowCells = Array.from(rowsElement.children).map(element => ({ ...rect(element), text: element.textContent }));
        const contiguous = (cells, axis) => cells.slice(1).every((cell, index) => Math.abs(cell[axis === 'x' ? 'left' : 'top'] - cells[index][axis === 'x' ? 'right' : 'bottom']) <= 1);
        return {
            frame, columns, rows, canvas, viewport, toolbar, columnCells, rowCells,
            expectedColumns: Number(shell.dataset.frameColumns),
            expectedRows: Number(shell.dataset.frameRows),
            aligned:
                toolbar.bottom <= frame.top &&
                Math.abs(frame.left - viewport.left) <= 1 &&
                Math.abs(frame.top - viewport.top) <= 1 &&
                Math.abs(frame.right - viewport.right) <= 1 &&
                Math.abs(frame.bottom - viewport.bottom) <= 1 &&
                columns.height <= 20 &&
                rows.width <= 20 &&
                Math.abs(columns.left - canvas.left) <= 1 &&
                Math.abs(columns.right - canvas.right) <= 1 &&
                Math.abs(columns.bottom - canvas.top) <= 1 &&
                Math.abs(rows.right - canvas.left) <= 1 &&
                Math.abs(rows.top - canvas.top) <= 1 &&
                Math.abs(rows.bottom - canvas.bottom) <= 1 &&
                contiguous(columnCells, 'x') && contiguous(rowCells, 'y'),
        };
    })()`, true);
    if (!result) throw new Error('Editor ruler DOM is unavailable.');
    if (!result.aligned) throw new Error(`Editor rulers are not aligned: ${JSON.stringify(result)}`);
    if (result.columnCells.length !== result.expectedColumns || result.rowCells.length !== result.expectedRows) {
        throw new Error(`Editor ruler counts do not match scene: ${JSON.stringify(result)}`);
    }
    return result;
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
                fs.writeFileSync(path.join(screenshotRoot, `m44-style-${name}.png`), image.toPNG());
                return;
            }
        } catch (error) {
            lastError = error;
        }
        await delay(100);
    }
    throw new Error(`${name} screenshot unavailable: ${lastError?.message || 'empty display surface'}`);
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
