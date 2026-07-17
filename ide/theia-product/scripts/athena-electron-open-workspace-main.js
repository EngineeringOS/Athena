const path = require('node:path');
const { app } = require('electron');
const { AthenaJvmRuntimeResolver } = require('@engineeringood/athena-theia-backend/lib/node/athena-jvm-runtime-resolver.js');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_WORKSPACE_OPENED_SENTINEL = 'ATHENA_WORKSPACE_OPENED=';
const ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL = 'ATHENA_WORKSPACE_OPEN_FAILURE=';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED';
const SHOULD_EXIT_ON_WORKSPACE_OPEN = process.env.ATHENA_ELECTRON_SMOKE_EXIT_ON_WORKSPACE_OPEN === '1';

const targetWorkspace = process.argv[2] ? path.resolve(process.cwd(), process.argv[2]) : undefined;

function main() {
    const runtimeResolution = configureJvmRuntime();
    if (runtimeResolution.status === 'ready') {
        console.log(`${ATHENA_JAVA_SENTINEL}=${runtimeResolution.javaHome}`);
    } else if (process.platform === 'win32') {
        console.warn(`${ATHENA_JAVA_UNRESOLVED_SENTINEL}=${runtimeResolution.message}`);
    }

    app.on('browser-window-created', (_event, window) => {
        console.log(ATHENA_WINDOW_CREATED_SENTINEL);
        window.webContents.once('did-finish-load', () => {
            console.log(ATHENA_READY_SENTINEL);
            void openWorkspace(window).catch(error => {
                console.error(`${ATHENA_WORKSPACE_OPEN_FAILURE_SENTINEL}${error.stack || String(error)}`);
                if (SHOULD_EXIT_ON_WORKSPACE_OPEN) {
                    setTimeout(() => app.exit(1), 50);
                }
            });
        });
    });

    require('../lib/backend/electron-main.js');
}

async function openWorkspace(window) {
    if (!targetWorkspace) {
        throw new Error('No target workspace path was provided.');
    }
    const openedPath = await window.webContents.executeJavaScript(`
        (async () => {
            const waitFor = async (predicate, description, timeoutMs = 60000, intervalMs = 100) => {
                const startedAt = Date.now();
                while (Date.now() - startedAt < timeoutMs) {
                    const result = await predicate();
                    if (result) {
                        return result;
                    }
                    await new Promise(resolve => setTimeout(resolve, intervalMs));
                }
                throw new Error('Timed out waiting for ' + description);
            };
            const normalizePath = value => {
                const normalized = decodeURI(String(value || ''))
                    .replace(/^#/, '')
                    .replace(/\\\\/g, '/')
                    .replace(/^\\/([A-Za-z]:\\/)/, '$1')
                    .replace(/\\/+$/, '')
                    .toLowerCase();
                return normalized;
            };

            await waitFor(() => window.theia?.container, 'theia container');
            const target = ${JSON.stringify(targetWorkspace)};
            const normalizedTarget = normalizePath(target);
            await waitFor(() => {
                const normalizedHash = normalizePath(window.location.hash);
                return normalizedHash === normalizedTarget ? target : undefined;
            }, 'target workspace URL fragment');
            return target;
        })();
    `, true);

    console.log(`${ATHENA_WORKSPACE_OPENED_SENTINEL}${openedPath}`);
    if (SHOULD_EXIT_ON_WORKSPACE_OPEN) {
        setTimeout(() => app.exit(0), 250);
    }
}

function configureJvmRuntime() {
    const resolver = new AthenaJvmRuntimeResolver();
    return resolver.configureProcessEnvironment(process.env, process.platform);
}

main();
