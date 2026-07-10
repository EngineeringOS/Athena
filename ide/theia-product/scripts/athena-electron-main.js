const { app } = require('electron');
const { AthenaJvmRuntimeResolver } = require('@engineeringood/athena-theia-backend/lib/node/athena-jvm-runtime-resolver.js');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED';
const SHOULD_EXIT_ON_READY = process.env.ATHENA_ELECTRON_SMOKE_EXIT_ON_READY === '1';

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
            if (SHOULD_EXIT_ON_READY) {
                setTimeout(() => app.exit(0), 750);
            }
        });
    });

    require('../lib/backend/electron-main.js');
}

function configureJvmRuntime() {
    const resolver = new AthenaJvmRuntimeResolver();
    return resolver.configureProcessEnvironment(process.env, process.platform);
}

main();
