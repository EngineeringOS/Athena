const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');

const { AthenaJvmRuntimeResolver } = require('../theia-backend/lib/node/athena-jvm-runtime-resolver.js');

function main() {
    const repositoryRoot = path.resolve(__dirname, '../..');
    const launcherPath = resolveSourceTreeLauncherPath(repositoryRoot);
    const runtimeResolution = new AthenaJvmRuntimeResolver().configureProcessEnvironment(process.env, process.platform);

    const env = runtimeResolution.status === 'ready'
        ? {
            ...process.env,
            ATHENA_JAVA_HOME: runtimeResolution.javaHome,
            JAVA_HOME: runtimeResolution.javaHome,
        }
        : process.env;

    const result = runGradleInstallDist(repositoryRoot, env);
    if (result.status !== 0 || !fs.existsSync(launcherPath)) {
        throw new Error(
            `Failed to prepare Athena JVM LSP host. exitCode=${result.status ?? 'null'} ` +
            `error=${result.error ? result.error.message : 'none'} launcher=${launcherPath}`
        );
    }

    console.log(`Athena dev runtime ready: ${launcherPath}`);
}

function runGradleInstallDist(repositoryRoot, env) {
    const gradleWrapperPath = path.join(repositoryRoot, process.platform === 'win32' ? 'gradlew.bat' : 'gradlew');
    if (!fs.existsSync(gradleWrapperPath)) {
        throw new Error(`Gradle wrapper not found: ${gradleWrapperPath}`);
    }

    if (process.platform === 'win32') {
        const escapedGradleWrapperPath = gradleWrapperPath.replace(/'/g, "''");
        return spawnSync(
            'powershell.exe',
            [
                '-NoProfile',
                '-ExecutionPolicy', 'Bypass',
                '-Command',
                `& '${escapedGradleWrapperPath}' --no-daemon --console=plain :ide:lsp:installDist`
            ],
            {
                cwd: repositoryRoot,
                env,
                stdio: 'inherit',
                windowsHide: true
            }
        );
    }

    return spawnSync(
        gradleWrapperPath,
        ['--no-daemon', '--console=plain', ':ide:lsp:installDist'],
        {
            cwd: repositoryRoot,
            env,
            stdio: 'inherit',
            windowsHide: true
        }
    );
}

function resolveSourceTreeLauncherPath(repositoryRoot) {
    const launcherFileName = process.platform === 'win32' ? 'athena-lsp-host.bat' : 'athena-lsp-host';
    return path.join(repositoryRoot, 'ide', 'lsp', 'build', 'install', 'athena-lsp-host', 'bin', launcherFileName);
}

main();
