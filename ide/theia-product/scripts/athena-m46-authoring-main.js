const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { createHash } = require('node:crypto');
const { app } = require('electron');
const { AthenaJvmRuntimeResolver } = require('@engineeringood/athena-theia-backend/lib/node/athena-jvm-runtime-resolver.js');

const repositoryRoot = path.resolve(process.argv[2]);
const sourcePath = path.resolve(process.env.ATHENA_M46_SOURCE_PATH || '');
const screenshotRoot = path.resolve(process.env.ATHENA_M46_SCREENSHOT_ROOT || '.');
const mode = process.env.ATHENA_M46_AUTHORING_MODE || 'author';
const timeoutMs = Number(process.env.ATHENA_M46_TIMEOUT_MS || 180000);

new AthenaJvmRuntimeResolver().configureProcessEnvironment(process.env, process.platform);
app.setPath('userData', path.join(os.tmpdir(), `athena-m46-authoring-${process.pid}`));

app.on('browser-window-created', (_event, window) => {
    window.setSkipTaskbar(true);
    window.show();
    window.webContents.setBackgroundThrottling(false);
    window.webContents.once('did-finish-load', async () => {
        try {
            const result = mode === 'author'
                ? await runAuthoring(window)
                : await runReopen(window);
            console.log(`ATHENA_M46_AUTHORING_PROOF=${JSON.stringify({ ...result, launcherPid: process.pid })}`);
            app.exit(0);
        } catch (error) {
            console.error(`Athena M46 authoring proof failed: ${error.stack || String(error)}`);
            app.exit(1);
        }
    });
});

require('../lib/backend/electron-main.js');

async function runAuthoring(window) {
    await setWindowSize(window, 1440, 960);
    const ready = await waitForReady(window);
    await execute(window, 'clearEvidence');
    await execute(window, 'switchFolioPage', 'power');
    await waitForPage(window, 'power');
    await execute(window, 'refresh');
    const before = await waitForStableScene(window);
    const sourcePortForAnchor = new Map(before.presentation.ports.map(port => [port.anchorId, port]));
    // Route topology may order visual endpoints differently from semantic SOURCE/SINK.
    // Select a canonical Connection; edit validation resolves endpoint roles from IR.
    let connection = before.presentation.connections.find(candidate => candidate.connectionId.includes(':signal:S1.limitOpen.output->PLC1.control.input'))
        ?? before.presentation.connections.find(candidate => candidate.connectionId.startsWith('connection:'));
    if (!connection) {
        throw new Error(`Placed Scene has no Connection with an OUT source Port: ${JSON.stringify(before.presentation.connections.map(candidate => ({
            id: candidate.connectionId,
            source: candidate.sourceAnchorId,
            sourceDirection: sourcePortForAnchor.get(candidate.sourceAnchorId)?.direction,
        })))}`);
    }

    const operations = [];
    let validReplacement;
    let validReconnect;
    let validEndpointRole;
    const reconnectConnection = before.presentation.connections.find(candidate => candidate.connectionId.includes(':wire:Q1.protection.line->KM1.mainContact.power'))
        ?? connection;
    const replacementPort = before.presentation.ports.find(port => port.semanticPortId === 'port:X2.powerTerminal.spare');
    if (!reconnectConnection || !replacementPort) {
        throw new Error(`Deterministic M46 reconnect fixture is unavailable on the active Power page: ${JSON.stringify({
            sheetId: before.presentation.sheetId,
            connectionIds: before.presentation.connections.map(candidate => candidate.connectionId),
            portIds: before.presentation.ports.map(candidate => candidate.semanticPortId),
        })}`);
    }
    connection = reconnectConnection;
    validEndpointRole = 'SINK';
    validReplacement = replacementPort;
    validReconnect = await execute(window, 'executeReconnect', connection.connectionId, validEndpointRole, validReplacement.semanticPortId);
    if (!validReconnect || !validReplacement) {
        throw new Error('No deterministic package-backed endpoint produced an accepted reconnect.');
    }
    requireStatus(validReconnect, 'ACCEPTED', 'valid reconnect');
    const invalidReplacement = before.presentation.ports.find(port => port.direction === (validEndpointRole === 'SOURCE' ? 'IN' : 'OUT'));
    if (!invalidReplacement) throw new Error('No package-backed IN Port available for invalid reconnect proof.');
    const afterReconnect = await waitForRevision(window, before.presentation.acceptedInputRevision);
    const reconnectedConnection = connectionForPort(afterReconnect.presentation, validReplacement.semanticPortId);
    if (!reconnectedConnection) throw new Error(`Accepted reconnect did not publish a placed Connection for ${validReplacement.semanticPortId}.`);
    const activeConnectionId = reconnectedConnection.connectionId;
    operations.push({ name: 'validReconnect', result: validReconnect, state: afterReconnect });

    const invalidReconnect = await execute(window, 'executeReconnect', activeConnectionId, validEndpointRole, invalidReplacement.semanticPortId);
    requireStatus(invalidReconnect, 'REJECTED', 'direction-invalid reconnect');
    const afterInvalid = await acceptedState(window);
    operations.push({ name: 'invalidReconnect', result: invalidReconnect, state: afterInvalid });

    const routedConnection = afterInvalid.presentation.connections.find(candidate => candidate.connectionId === activeConnectionId);
    if (!routedConnection || routedConnection.segments.length === 0) throw new Error('Accepted Connection has no route segment to adjust.');
    const routeTarget = { kind: 'SEGMENT', ordinal: Math.min(1, routedConnection.segments.length - 1) };
    const routePoint = { column: 15, row: 14 };
    const routeAdjust = await execute(window, 'executeRouteAdjust', activeConnectionId, routeTarget, routePoint);
    requireStatus(routeAdjust, 'ACCEPTED', 'route adjustment');
    const afterRoute = await waitForRevision(window, afterInvalid.presentation.acceptedInputRevision);
    operations.push({ name: 'routeAdjust', result: routeAdjust, state: afterRoute, routeTarget, routePoint });

    const routeJournalEntryId = routeAdjust.result.acceptance.journalEntryId;
    const evidenceBeforePointer = await execute(window, 'evidence');
    await exercisePointerOnly(window);
    const evidenceAfterPointer = await execute(window, 'evidence');

    const undo = await execute(window, 'executeUndo', routeJournalEntryId);
    requireStatus(undo, 'ACCEPTED', 'route Undo');
    const afterUndo = await waitForRevision(window, afterRoute.presentation.acceptedInputRevision);
    operations.push({ name: 'undo', result: undo, state: afterUndo });

    const redo = await execute(window, 'executeRedo', routeJournalEntryId);
    requireStatus(redo, 'ACCEPTED', 'route Redo');
    const afterRedo = await waitForRevision(window, afterUndo.presentation.acceptedInputRevision);
    operations.push({ name: 'redo', result: redo, state: afterRedo });

    const staleRedo = await execute(window, 'executeRedo', routeJournalEntryId);
    requireStatus(staleRedo, 'REJECTED', 'stale Redo');
    const finalState = await acceptedState(window);
    operations.push({ name: 'staleRedo', result: staleRedo, state: finalState });

    await execute(window, 'switchFolioPage', 'control_cpu');
    await waitForStableScene(window);
    await execute(window, 'clearEvidence');
    await dismissTransientNotifications(window);
    const desktop = await capture(window, 'desktop');
    await setWindowSize(window, 760, 720);
    const narrowState = await acceptedState(window);
    const narrow = await capture(window, 'narrow');

    return {
        schemaVersion: 'M46.authoring-product-proof',
        mode,
        ready,
        before,
        operations,
        pointerOnlyEvidenceCountBefore: evidenceBeforePointer.length,
        pointerOnlyEvidenceCountAfter: evidenceAfterPointer.length,
        finalState,
        narrowState,
        screenshots: [desktop, narrow],
    };
}

async function runReopen(window) {
    await setWindowSize(window, 1440, 960);
    const ready = await waitForReady(window);
    const reopened = await acceptedState(window);
    const desktop = await capture(window, 'reopened-desktop');
    await setWindowSize(window, 760, 720);
    const narrowState = await acceptedState(window);
    const narrow = await capture(window, 'reopened-narrow');
    return {
        schemaVersion: 'M46.authoring-product-proof',
        mode,
        ready,
        reopened,
        narrowState,
        screenshots: [desktop, narrow],
    };
}

async function waitForReady(window) {
    const started = Date.now();
    let last;
    let lastPhase = '';
    while (Date.now() - started < timeoutMs) {
        await window.webContents.executeJavaScript(`(${prepareWorkbench.toString()})(${JSON.stringify(repositoryRoot)}, ${JSON.stringify(sourcePath)})`, true);
        last = await acceptedState(window);
        const phase = JSON.stringify(readinessPhase(last));
        if (phase !== lastPhase) {
            console.error(`ATHENA_M46_AUTHORING_PROGRESS ${phase}`);
            lastPhase = phase;
        }
        if (isReady(last)) return last;
        await delay(250);
    }
    throw new Error(`Timed out waiting for exact M46 workspace and accepted Scene: ${JSON.stringify(last)}`);
}

function readinessPhase(state) {
    const expected = normalize(repositoryRoot);
    return {
        workspaceOpened: state?.workbench?.workspaceOpened,
        workspaceRoots: state?.workbench?.workspaceRoots?.map(normalize),
        repositoryLifecycle: state?.workbench?.repositoryLifecycle,
        repositoryRoot: normalize(state?.workbench?.repositoryRoot),
        lspRepositoryRoot: normalize(state?.workbench?.lspRepositoryRoot),
        expected,
        explorerContainsRoot: state?.workbench?.explorerContainsRoot,
        presentationState: state?.presentation?.publicationState,
        connectionState: state?.connectionReadModel?.state,
        canvasState: state?.canvas?.publicationState,
        nonWhiteSamples: state?.canvas?.nonWhiteSamples,
        canvasWidth: state?.canvas?.width,
        canvasHeight: state?.canvas?.height,
        canvasHostWidth: state?.canvas?.hostWidth,
        canvasHostHeight: state?.canvas?.hostHeight,
        loadedAssetCount: state?.canvas?.loadedAssetCount,
        expectedAssetCount: state?.canvas?.expectedAssetCount,
        dpr: state?.canvas?.dpr,
    };
}

function isReady(state) {
    if (!state?.workbench || !state.presentation || !state.connectionReadModel || !state.canvas) return false;
    const expected = normalize(repositoryRoot);
    return state.workbench.workspaceOpened
        && state.workbench.workspaceRoots.length === 1
        && normalize(state.workbench.workspaceRoots[0]) === expected
        && state.workbench.repositoryLifecycle?.toUpperCase() === 'READY'
        && normalize(state.workbench.repositoryRoot) === expected
        && normalize(state.workbench.lspRepositoryRoot) === expected
        && state.workbench.explorerContainsRoot
        && state.presentation.publicationState === 'READY'
        && state.connectionReadModel.state === 'READY'
        && state.canvas.publicationState === 'READY'
        && state.canvas.nonWhiteSamples > 0;
}

async function waitForRevision(window, previousRevision) {
    const started = Date.now();
    let state;
    while (Date.now() - started < 12000) {
        state = await acceptedState(window);
        if (state.presentation?.acceptedInputRevision && state.presentation.acceptedInputRevision !== previousRevision
            && state.connectionReadModel?.acceptedInputRevision === state.presentation.acceptedInputRevision) return state;
        await delay(100);
    }
    throw new Error(`Accepted revision did not advance from ${previousRevision}: ${JSON.stringify(state)}`);
}

async function waitForStableScene(window) {
    let previous;
    let stableSamples = 0;
    let signature = '';
    const started = Date.now();
    while (Date.now() - started < 10000) {
        const current = await acceptedState(window);
        const currentSignature = current?.presentation?.publicationState === 'READY'
            ? `${current.presentation.sceneDigest}|${current.presentation.connections.map(connection => connection.connectionId).join('|')}`
            : '';
        stableSamples = currentSignature && currentSignature === signature ? stableSamples + 1 : 0;
        signature = currentSignature;
        if (currentSignature && current.presentation.connections.length > 0 && stableSamples >= 3) {
            await delay(1000);
            return await acceptedState(window);
        }
        previous = current;
        await delay(100);
    }
    throw new Error(`Accepted Scene did not stabilize: ${JSON.stringify(previous?.presentation)}`);
}

async function waitForPage(window, pageId) {
    const started = Date.now();
    let state;
    while (Date.now() - started < 12000) {
        state = await acceptedState(window);
        if (state.presentation?.sheetId === pageId) return state;
        await delay(100);
    }
    throw new Error(`Folio page did not switch to ${pageId}: ${JSON.stringify(state?.presentation?.sheetId)}`);
}

async function acceptedState(window) {
    const workbench = await window.webContents.executeJavaScript(`(${workbenchState.toString()})(${JSON.stringify(path.basename(repositoryRoot))})`, true);
    const presentation = await window.webContents.executeJavaScript(`window.__athenaPresentationAutomation?.getState?.()`, true);
    const connectionReadModel = await window.webContents.executeJavaScript(`window.__athenaPresentationAutomation?.getConnectionReadModel?.()`, true);
    const canvas = await canvasState(window);
    return { workbench, presentation, connectionReadModel, canvas };
}

async function workbenchState(rootName) {
    const state = await window.__athenaWorkbenchAutomation?.getState?.();
    const explorerNodes = Array.from(document.querySelectorAll('.theia-FileTree, .theia-navigator-container, .navigator-container, [id="files"] .theia-TreeContainer, [id="files"], #shell-tab-files'));
    const visibleRootCandidates = Array.from(document.querySelectorAll('*')).filter(node => {
        if (!node.offsetParent || !node.closest('#files, #shell-tab-files, .lm-TabBar')) return false;
        const text = (node.textContent || '').trim();
        const label = node.getAttribute('aria-label') || '';
        const title = node.getAttribute('title') || '';
        return text === rootName || label.includes(rootName) || title.includes(rootName);
    });
    const explorerContainsRoot = explorerNodes.some(node => `${node.textContent || ''} ${node.getAttribute('aria-label') || ''} ${node.getAttribute('title') || ''}`.includes(rootName))
        || visibleRootCandidates.length > 0;
    return state ? { ...state, explorerContainsRoot } : undefined;
}

async function prepareWorkbench(workspaceRoot, sourceFile) {
    if (!window.theia?.container || !window.__athenaWorkbenchAutomation) return false;
    await window.__athenaWorkbenchAutomation.openWorkspaceAndSource(workspaceRoot, sourceFile);
    return true;
}

async function execute(window, method, ...args) {
    return window.webContents.executeJavaScript(`window.__athenaPresentationAutomation.${method}(...${JSON.stringify(args)})`, true);
}

function requiredPort(presentation, suffix) {
    const port = presentation.ports.find(candidate => candidate.semanticPortId === `port:${suffix}` || candidate.semanticPortId === suffix);
    if (!port) throw new Error(`Required semantic Port unavailable: ${suffix}`);
    return port;
}

function requiredPortAny(presentation, suffixes) {
    for (const suffix of suffixes) {
        const port = presentation.ports.find(candidate => candidate.semanticPortId === `port:${suffix}` || candidate.semanticPortId === suffix);
        if (port) return port;
    }
    throw new Error(`Required semantic Port unavailable: ${suffixes.join(', ')}`);
}

function connectionForPort(presentation, semanticPortId) {
    const anchorIds = new Set(presentation.ports
        .filter(port => port.semanticPortId === semanticPortId)
        .map(port => port.anchorId));
    return presentation.connections.find(connection =>
        anchorIds.has(connection.sourceAnchorId) || anchorIds.has(connection.targetAnchorId));
}

function requireStatus(evidence, expected, name) {
    if (evidence?.result?.status !== expected) {
        throw new Error(`${name} expected ${expected}: ${JSON.stringify(evidence)}`);
    }
}

async function exercisePointerOnly(window) {
    await window.webContents.executeJavaScript(`(() => {
        const host = document.querySelector('.athena-presentation__canvas-host');
        if (!host) throw new Error('Canvas host unavailable for pointer-only proof.');
        host.dispatchEvent(new PointerEvent('pointermove', { bubbles: true, clientX: 32, clientY: 32 }));
    })()`, true);
    await delay(100);
}

async function dismissTransientNotifications(window) {
    await window.webContents.executeJavaScript(`(() => {
        for (const button of document.querySelectorAll('.theia-notification-item button[title*="Close"], .theia-notification-item .codicon-close')) {
            button.dispatchEvent(new MouseEvent('click', { bubbles: true }));
        }
    })()`, true);
    await delay(100);
}

async function setWindowSize(window, width, height) {
    window.unmaximize();
    window.setBounds({ x: 80, y: 80, width, height });
    const started = Date.now();
    while (Date.now() - started < 5000) {
        const bounds = window.getBounds();
        if (Math.abs(bounds.width - width) <= 2 && Math.abs(bounds.height - height) <= 2) {
            await delay(250);
            return;
        }
        await delay(100);
    }
    throw new Error(`Window did not reach ${width}x${height}.`);
}

async function capture(window, name) {
    const image = await window.webContents.capturePage();
    if (image.isEmpty()) throw new Error(`${name} screenshot is empty.`);
    fs.mkdirSync(screenshotRoot, { recursive: true });
    const file = path.join(screenshotRoot, `m46-authoring-${name}.png`);
    fs.writeFileSync(file, image.toPNG());
    return { name, path: file, width: image.getSize().width, height: image.getSize().height, digest: sha256(image.toPNG()) };
}

async function canvasState(window) {
    const raw = await window.webContents.executeJavaScript(`(() => {
        const shell = document.querySelector('.athena-presentation__canvas-shell');
        const frame = document.querySelector('.athena-presentation__document-frame');
        const host = document.querySelector('.athena-presentation__canvas-host');
        const corner = document.querySelector('.athena-presentation__ruler-corner');
        const columns = document.querySelector('.athena-presentation__ruler--columns');
        const rows = document.querySelector('.athena-presentation__ruler--rows');
        const canvases = Array.from(document.querySelectorAll('.athena-presentation__canvas-host canvas'));
        if (!shell || !frame || !host || !corner || !columns || !rows || canvases.length === 0) return null;
        const sample = canvases.reduce((total, canvas) => {
            const context = canvas.getContext('2d', { willReadFrequently: true });
            if (!context) return total;
            const pixels = context.getImageData(0, 0, canvas.width, canvas.height).data;
            let count = 0;
            for (let index = 0; index < pixels.length; index += 64) {
                if (pixels[index + 3] > 0 && (pixels[index] < 245 || pixels[index + 1] < 245 || pixels[index + 2] < 245)) count += 1;
            }
            return total + count;
        }, 0);
        const frameRect = frame.getBoundingClientRect();
        const hostRect = host.getBoundingClientRect();
        const cornerRect = corner.getBoundingClientRect();
        const columnRect = columns.getBoundingClientRect();
        const rowRect = rows.getBoundingClientRect();
        const shellText = shell.textContent || '';
        return {
            publicationState: shell.dataset.publicationState || '',
            sceneId: shell.dataset.sceneId || '',
            sceneDigest: shell.dataset.sceneDigest || '',
            nonWhiteSamples: sample,
            hostWidth: hostRect.width,
            hostHeight: hostRect.height,
            loadedAssetCount: host.dataset.loadedAssetCount || '0',
            expectedAssetCount: host.dataset.expectedAssetCount || '0',
            width: canvases[0].width,
            height: canvases[0].height,
            dpr: window.devicePixelRatio || 1,
            layers: canvases.map(canvas => canvas.toDataURL('image/png')),
            visualChecks: {
                cornerAligned: Math.abs(cornerRect.top - frameRect.top) <= 1 && Math.abs(cornerRect.left - frameRect.left) <= 1,
                columnRulerFlush: Math.abs(columnRect.top - frameRect.top) <= 1 && Math.abs(columnRect.left - cornerRect.right) <= 1,
                rowRulerFlush: Math.abs(rowRect.left - frameRect.left) <= 1 && Math.abs(rowRect.top - cornerRect.bottom) <= 1,
                rulersNarrow: cornerRect.width <= 24 && cornerRect.height <= 24 && rowRect.width <= 24 && columnRect.height <= 24,
                canvasFillsBody: hostRect.width > frameRect.width * 0.75 && hostRect.height > frameRect.height * 0.75,
                noConstructionGrid: getComputedStyle(host).backgroundImage === 'none',
                noDebugSourceText: !/\b(package|system|entity|connect|sceneDigest|traceId)\b/.test(shellText),
                noBottomTable: !shell.querySelector('table'),
                noPersistentPortRings: !shell.querySelector('[data-port-ring], .port-ring'),
            },
        };
    })()`, true);
    if (!raw) return null;
    return {
        ...raw,
        canvasDigest: sha256(raw.layers.join('\n')),
        layerDigests: raw.layers.map(sha256),
        layers: undefined,
    };
}

function normalize(value) {
    return typeof value === 'string' ? path.resolve(value).replaceAll('\\', '/').toLowerCase() : '';
}

function sha256(value) {
    return createHash('sha256').update(value).digest('hex');
}

function delay(milliseconds) {
    return new Promise(resolve => setTimeout(resolve, milliseconds));
}
