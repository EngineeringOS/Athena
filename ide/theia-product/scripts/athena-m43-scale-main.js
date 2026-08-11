const path = require('node:path');
const os = require('node:os');
const { app } = require('electron');
const { AthenaJvmRuntimeResolver } = require('@engineeringood/athena-theia-backend/lib/node/athena-jvm-runtime-resolver.js');

const repositoryRoot = path.resolve(process.argv[2]);
const sourcePath = path.resolve(process.env.ATHENA_M43_SOURCE_PATH || '');
const timeoutMs = Number(process.env.ATHENA_M43_TIMEOUT_MS || 120000);
const benchmarkHook = process.env.ATHENA_SCALE_BENCHMARK_HOOK || '__athenaRunScaleBenchmark';

new AthenaJvmRuntimeResolver().configureProcessEnvironment(process.env, process.platform);
app.commandLine.appendSwitch('enable-precise-memory-info');
if (process.env.ATHENA_M43_TEMP_USER_DATA === '1') {
    app.setPath('userData', path.join(os.tmpdir(), `athena-m43-scale-${process.pid}`));
}

app.on('browser-window-created', (_event, window) => {
    window.setContentSize(1600, 1000);
    window.webContents.once('did-finish-load', async () => {
        try {
            const result = await waitForScale(window);
            console.log(`ATHENA_M43_SCALE=${JSON.stringify(result)}`);
            app.exit(0);
        } catch (error) {
            console.error(`Athena M43 scale benchmark failed: ${error.stack || String(error)}`);
            app.exit(1);
        }
    });
});

require('../lib/backend/electron-main.js');

async function waitForScale(window) {
    const started = Date.now();
    while (Date.now() - started < timeoutMs) {
        await window.webContents.executeJavaScript(`(${prepareWorkbench.toString()})(${JSON.stringify(repositoryRoot)}, ${JSON.stringify(sourcePath)})`, true);
        const ready = await window.webContents.executeJavaScript(`(() => {
            const shell = document.querySelector('.athena-presentation__canvas-shell[data-publication-state="READY"]');
            const host = shell?.querySelector('.athena-presentation__canvas-host');
            return Boolean(shell && host && typeof window[${JSON.stringify(benchmarkHook)}] === 'function');
        })()`, true);
        if (ready) {
            await window.webContents.executeJavaScript(`window[${JSON.stringify(benchmarkHook)}]()`, true);
            while (Date.now() - started < timeoutMs) {
                const state = await window.webContents.executeJavaScript(`(() => ({
                    result: window.__athenaScaleBenchmarkResult || null,
                    error: window.__athenaScaleBenchmarkError || null,
                    viewport: { width: window.innerWidth, height: window.innerHeight, dpr: window.devicePixelRatio || 1 },
                    userAgent: navigator.userAgent,
                }))()`, true);
                if (state.error) throw new Error(state.error);
                if (state.result) return { ...state, status: 'ready' };
                await delay(250);
            }
        }
        await delay(500);
    }
    const diagnostics = await window.webContents.executeJavaScript(`(${browserScaleDiagnostics.toString()})()`, true);
    throw new Error(`Timed out waiting for READY scene and scale benchmark hook: ${JSON.stringify(diagnostics)}`);
}

async function prepareWorkbench(workspaceRoot, sourceFile) {
    function resolveTheiaService() {
        return window.__athenaWorkbenchAutomation;
    }

    window.__athenaM43ScalePrepareState = { theia: Boolean(window.theia?.container), require: typeof require };
    if (!window.theia?.container) return false;
    try {
        const automation = resolveTheiaService();
        if (!automation) throw new Error('Athena workbench automation unavailable.');
        await automation.openWorkspaceAndSource(workspaceRoot, sourceFile);
        window.__athenaM43ScalePrepareState.commandExecuted = true;
        return true;
    } catch (error) {
        window.__athenaM43ScalePrepareState.error = String(error?.stack || error);
        return false;
    }
}

function browserScaleDiagnostics() {
    const shell = document.querySelector('.athena-presentation__canvas-shell');
    const host = document.querySelector('.athena-presentation__canvas-host');
    return {
        bodyText: (document.body.innerText || '').slice(0, 1600),
        shell: shell ? {
            className: shell.className,
            state: shell.dataset.publicationState || '',
            attributes: Array.from(shell.attributes).map(attribute => [attribute.name, attribute.value]),
        } : null,
        host: host ? {
            className: host.className,
            canvas: Boolean(host.querySelector('canvas')),
        } : null,
        runScaleType: typeof window.__athenaRunScaleBenchmark,
        prepareState: window.__athenaM43ScalePrepareState || null,
        automation: Boolean(window.__athenaWorkbenchAutomation),
        workspaceRoots: Array.from(document.querySelectorAll('[data-uri], .theia-navigator-tree, .theia-navigator-container')).slice(0, 5).map(node => node.textContent?.trim()).filter(Boolean),
    };
}

function delay(milliseconds) {
    return new Promise(resolve => setTimeout(resolve, milliseconds));
}
