const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');
const { app } = require('electron');

const ATHENA_READY_SENTINEL = 'ATHENA_DESKTOP_READY';
const ATHENA_WINDOW_CREATED_SENTINEL = 'ATHENA_DESKTOP_WINDOW_CREATED';
const ATHENA_JAVA_SENTINEL = 'ATHENA_JAVA_HOME';
const ATHENA_JAVA_UNRESOLVED_SENTINEL = 'ATHENA_JAVA_HOME_UNRESOLVED';
const SHOULD_EXIT_ON_READY = process.env.ATHENA_ELECTRON_SMOKE_EXIT_ON_READY === '1';

function main() {
    const resolvedJavaHome = configureWindowsJava25();
    if (resolvedJavaHome) {
        console.log(`${ATHENA_JAVA_SENTINEL}=${resolvedJavaHome}`);
    } else if (process.platform === 'win32') {
        console.warn(`${ATHENA_JAVA_UNRESOLVED_SENTINEL}=java25-helper-not-found`);
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

function configureWindowsJava25() {
    if (process.platform !== 'win32') {
        return process.env.ATHENA_JAVA_HOME || process.env.JAVA_HOME;
    }

    const preferredJavaHome = resolvePreferredJavaHome();
    if (!preferredJavaHome) {
        return undefined;
    }

    process.env.ATHENA_JAVA_HOME = preferredJavaHome;
    process.env.JAVA_HOME = preferredJavaHome;

    const javaBinPath = path.join(preferredJavaHome, 'bin');
    const currentPath = process.env.PATH || process.env.Path || '';
    const pathSegments = currentPath.split(path.delimiter).filter(Boolean);
    if (!pathSegments.some(segment => path.resolve(segment) === path.resolve(javaBinPath))) {
        process.env.PATH = `${javaBinPath}${path.delimiter}${currentPath}`;
    }

    return preferredJavaHome;
}

function resolvePreferredJavaHome() {
    const configuredJavaHome = process.env.ATHENA_JAVA_HOME || process.env.JAVA_HOME;
    if (configuredJavaHome && isJava25Home(configuredJavaHome)) {
        return configuredJavaHome;
    }

    const java25Command = resolveJava25Command();
    if (!java25Command) {
        return undefined;
    }

    return parseJavaHomeFromBatchScript(java25Command);
}

function isJava25Home(javaHome) {
    const javaExecutable = path.join(javaHome, 'bin', 'java.exe');
    if (!fs.existsSync(javaExecutable)) {
        return false;
    }

    const versionProbe = spawnSync(javaExecutable, ['-version'], {
        encoding: 'utf8',
        windowsHide: true
    });
    const combinedOutput = `${versionProbe.stdout || ''}\n${versionProbe.stderr || ''}`;
    return versionProbe.status === 0 && combinedOutput.includes('"25');
}

function resolveJava25Command() {
    const lookup = spawnSync('where.exe', ['java25'], {
        encoding: 'utf8',
        windowsHide: true
    });
    if (lookup.status !== 0) {
        return undefined;
    }

    const commandPath = lookup.stdout
        .split(/\r?\n/)
        .map(line => line.trim())
        .find(Boolean);
    return commandPath || undefined;
}

function parseJavaHomeFromBatchScript(commandPath) {
    if (!fs.existsSync(commandPath)) {
        return undefined;
    }

    const scriptText = fs.readFileSync(commandPath, 'utf8');
    const match = scriptText.match(/set\s+JAVA_HOME=(.+)/i);
    if (!match) {
        return undefined;
    }

    const javaHome = match[1].trim().replace(/^"+|"+$/g, '');
    return javaHome || undefined;
}

main();
