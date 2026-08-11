const path = require('node:path');
const os = require('node:os');
const { app } = require('electron');
const { AthenaJvmRuntimeResolver } = require('@engineeringood/athena-theia-backend/lib/node/athena-jvm-runtime-resolver.js');

const workspaceRoot = path.resolve(process.argv[2]);
const sourcePath = path.resolve(process.env.ATHENA_M46_SOURCE_PATH || '');
const timeoutMs = Number(process.env.ATHENA_M46_TIMEOUT_MS || 180000);
const benchmarkHook = '__athenaRunM46ConnectionPerformanceBenchmark';

new AthenaJvmRuntimeResolver().configureProcessEnvironment(process.env, process.platform);
app.commandLine.appendSwitch('enable-precise-memory-info');
app.setPath('userData', path.join(os.tmpdir(), `athena-m46-performance-${process.pid}`));

app.on('browser-window-created', (_event, window) => {
    console.error(`ATHENA_M46_PROGRESS window-created id=${window.id}`);
    window.setContentSize(1600, 1000);
    window.webContents.setBackgroundThrottling(false);
    window.setSkipTaskbar(true);
    window.show();
    window.focus();
    window.webContents.once('did-finish-load', async () => {
        console.error(`ATHENA_M46_PROGRESS did-finish-load id=${window.id}`);
        try {
            const result = await waitForBenchmark(window);
            console.log(`ATHENA_M46_CONNECTION_PERFORMANCE=${JSON.stringify({ ...result, launcherPid: process.pid })}`);
            app.exit(0);
        } catch (error) {
            console.error(`Athena M46 connection performance failed: ${error.stack || String(error)}`);
            app.exit(1);
        }
    });
});

require('../lib/backend/electron-main.js');

async function waitForBenchmark(window) {
    const started = Date.now();
    let lastPhase = '';
    while (Date.now() - started < timeoutMs) {
        await window.webContents.executeJavaScript(`(${prepareWorkbench.toString()})(${JSON.stringify(workspaceRoot)}, ${JSON.stringify(sourcePath)})`, true);
        const state = await acceptedState(window);
        const phase = JSON.stringify(readinessPhase(state));
        if (phase !== lastPhase) {
            console.error(`ATHENA_M46_PROGRESS waiting ${phase}`);
            lastPhase = phase;
        }
        if (isReady(state)) {
            console.error('ATHENA_M46_PROGRESS benchmark-start');
            await window.webContents.executeJavaScript(`void window[${JSON.stringify(benchmarkHook)}]()`, true);
            while (Date.now() - started < timeoutMs) {
                const state = await window.webContents.executeJavaScript(`(() => ({
                    result: window.__athenaM46ConnectionPerformanceResult || null,
                    error: window.__athenaM46ConnectionPerformanceError || null,
                    viewport: { width: window.innerWidth, height: window.innerHeight, dpr: window.devicePixelRatio || 1 },
                    repositoryState: window.__athenaWorkbenchAutomation?.repositoryState?.() || null,
                }))()`, true);
                if (state.error) throw new Error(state.error);
                if (state.result) {
                    console.error('ATHENA_M46_PROGRESS benchmark-result');
                    return { ...state, status: 'measured' };
                }
                await delay(250);
            }
        }
        await delay(500);
    }
    throw new Error('Timed out waiting for READY M46 connection performance benchmark.');
}

async function acceptedState(window) {
    const rootName = path.basename(workspaceRoot);
    const workbench = await window.webContents.executeJavaScript(`(${workbenchState.toString()})(${JSON.stringify(rootName)})`, true);
    const presentation = await window.webContents.executeJavaScript('window.__athenaPresentationAutomation?.getState?.()', true);
    const connectionReadModel = await window.webContents.executeJavaScript('window.__athenaPresentationAutomation?.getConnectionReadModel?.()', true);
    const shell = await window.webContents.executeJavaScript(`(() => {
        const element = document.querySelector('.athena-presentation__canvas-shell[data-publication-state="READY"]');
        return { publicationState: element?.dataset.publicationState || null, hook: typeof window[${JSON.stringify(benchmarkHook)}] };
    })()`, true);
    return { workbench, presentation, connectionReadModel, shell };
}

function isReady(state) {
    if (!state?.workbench || !state.presentation || !state.connectionReadModel || !state.shell) return false;
    const expected = normalize(workspaceRoot);
    return state.workbench.workspaceOpened
        && state.workbench.workspaceRoots.length === 1
        && normalize(state.workbench.workspaceRoots[0]) === expected
        && state.workbench.repositoryLifecycle?.toUpperCase() === 'READY'
        && normalize(state.workbench.repositoryRoot) === expected
        && normalize(state.workbench.lspRepositoryRoot) === expected
        && state.workbench.explorerContainsRoot
        && state.presentation.publicationState === 'READY'
        && state.connectionReadModel.state === 'READY'
        && state.shell.publicationState === 'READY'
        && state.shell.hook === 'function';
}

function readinessPhase(state) {
    return {
        workspaceOpened: state?.workbench?.workspaceOpened,
        workspaceRoots: state?.workbench?.workspaceRoots?.map(normalize),
        repositoryLifecycle: state?.workbench?.repositoryLifecycle,
        repositoryRoot: normalize(state?.workbench?.repositoryRoot),
        lspRepositoryRoot: normalize(state?.workbench?.lspRepositoryRoot),
        explorerContainsRoot: state?.workbench?.explorerContainsRoot,
        presentationState: state?.presentation?.publicationState,
        connectionState: state?.connectionReadModel?.state,
        canvasState: state?.shell?.publicationState,
        hook: state?.shell?.hook,
    };
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
    const explorerContainsRoot = explorerNodes.some(node => `${node.textContent || ''} ${node.getAttribute('aria-label') || ''} ${node.getAttribute('title') || ''}`.includes(rootName));
    return state ? { ...state, explorerContainsRoot: explorerContainsRoot || visibleRootCandidates.length > 0 } : undefined;
}

function normalize(value) {
    return typeof value === 'string' ? path.resolve(value).replace(/\\\\/g, '/').toLowerCase() : '';
}

async function prepareWorkbench(root, source) {
    if (!window.theia?.container || !window.__athenaWorkbenchAutomation) return false;
    await window.__athenaWorkbenchAutomation.openWorkspaceAndSource(root, source);
    return true;
}

function delay(milliseconds) { return new Promise(resolve => setTimeout(resolve, milliseconds)); }
