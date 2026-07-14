import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdir, mkdtemp, writeFile, rm } from 'node:fs/promises';
import { join } from 'node:path';
import { tmpdir } from 'node:os';
import { pathToFileURL } from 'node:url';

const { AthenaRepositorySessionManager } = await import('../lib/node/athena-repository-session-manager.js');
const { AthenaJvmRuntimeResolver } = await import('../lib/node/athena-jvm-runtime-resolver.js');

class TestableAthenaRepositorySessionManager extends AthenaRepositorySessionManager {
    constructor() {
        super();
        this.jvmRuntimeResolver = new AthenaJvmRuntimeResolver();
        this.activations = [];
    }

    exposeBuildLaunchSpec(launcherPath, environment, platform) {
        return this.buildSessionHostLaunchSpec(launcherPath, environment, platform);
    }

    exposeResolveRepositoryRootForDocumentUri(documentUri) {
        return this.resolveRepositoryRootForDocumentUri(documentUri);
    }

    async activateRepositorySession(repositoryRootPath) {
        this.activations.push(repositoryRootPath);
        this.state = {
            lifecycle: 'ready',
            lspLifecycle: 'ready',
            repositoryRoot: repositoryRootPath,
            semanticPath: 'frontend -> LSP -> runtime/compiler',
            message: `Athena LSP ready for ${repositoryRootPath}`
        };
        return this.state;
    }
}

async function withTempDirectory(prefix, callback) {
    const directoryPath = await mkdtemp(join(tmpdir(), prefix));
    try {
        await callback(directoryPath);
    } finally {
        await rm(directoryPath, { recursive: true, force: true });
    }
}

test('builds a direct Java launch spec for the Athena LSP host on Windows', async () => {
    await withTempDirectory('athena-session-host-', async rootDirectoryPath => {
        const launcherDirectoryPath = join(rootDirectoryPath, 'bin');
        const libraryDirectoryPath = join(rootDirectoryPath, 'lib');
        const javaHomePath = join(rootDirectoryPath, 'jdk-25');
        await mkdir(launcherDirectoryPath, { recursive: true });
        await mkdir(libraryDirectoryPath, { recursive: true });
        await mkdir(join(javaHomePath, 'bin'), { recursive: true });
        const launcherPath = join(launcherDirectoryPath, 'athena-lsp-host.bat');
        await writeFile(launcherPath, '@echo off\n', 'utf8');
        await writeFile(join(libraryDirectoryPath, 'b.jar'), '', 'utf8');
        await writeFile(join(libraryDirectoryPath, 'a.jar'), '', 'utf8');
        await writeFile(join(javaHomePath, 'bin', 'java.exe'), '', 'utf8');
        await writeFile(join(javaHomePath, 'release'), 'JAVA_VERSION="25.0.1"\n', 'utf8');

        const manager = new TestableAthenaRepositorySessionManager();
        const launchSpec = manager.exposeBuildLaunchSpec(
            launcherPath,
            {
                ATHENA_JAVA_HOME: javaHomePath
            },
            'win32'
        );

        assert.equal(launchSpec.command, join(javaHomePath, 'bin', 'java.exe'));
        assert.deepEqual(
            launchSpec.args,
            [
                '-classpath',
                `${join(libraryDirectoryPath, 'a.jar')};${join(libraryDirectoryPath, 'b.jar')}`,
                'com.engineeringood.athena.ide.lsp.MainKt',
                '--stdio'
            ]
        );
        assert.equal(launchSpec.cwd, join(rootDirectoryPath, '..', '..'));
        assert.equal(launchSpec.runtime.source, 'athena-java-home');
        assert.equal(launchSpec.runtime.version, '25.0.1');
    });
});

test('falls back from JAVA_HOME 19 to a valid Athena-managed Java 25 runtime', async () => {
    await withTempDirectory('athena-session-host-', async rootDirectoryPath => {
        const launcherDirectoryPath = join(rootDirectoryPath, 'bin');
        const libraryDirectoryPath = join(rootDirectoryPath, 'lib');
        const localAppDataPath = join(rootDirectoryPath, 'local-app-data');
        const managedRuntimePath = join(localAppDataPath, 'EngineeringOOD', 'Athena', 'runtimes', 'jdk-25.0.1');
        await mkdir(launcherDirectoryPath, { recursive: true });
        await mkdir(libraryDirectoryPath, { recursive: true });
        await mkdir(join(managedRuntimePath, 'bin'), { recursive: true });
        const launcherPath = join(launcherDirectoryPath, 'athena-lsp-host.bat');
        await writeFile(launcherPath, '@echo off\n', 'utf8');
        await writeFile(join(libraryDirectoryPath, 'runtime.jar'), '', 'utf8');
        await writeFile(join(managedRuntimePath, 'bin', 'java.exe'), '', 'utf8');
        await writeFile(join(managedRuntimePath, 'release'), 'JAVA_VERSION="25.0.1"\n', 'utf8');

        const manager = new TestableAthenaRepositorySessionManager();
        const launchSpec = manager.exposeBuildLaunchSpec(
            launcherPath,
            {
                JAVA_HOME: 'C:\\Java\\jdk-19',
                LOCALAPPDATA: localAppDataPath,
            },
            'win32'
        );

        assert.equal(launchSpec.command, join(managedRuntimePath, 'bin', 'java.exe'));
        assert.deepEqual(
            launchSpec.args,
            [
                '-classpath',
                join(libraryDirectoryPath, 'runtime.jar'),
                'com.engineeringood.athena.ide.lsp.MainKt',
                '--stdio'
            ]
        );
        assert.equal(launchSpec.runtime.source, 'managed-runtime');
        assert.equal(launchSpec.runtime.version, '25.0.1');
    });
});

test('fails with a clear error when no Java 25 runtime is available', async () => {
    await withTempDirectory('athena-session-host-', async rootDirectoryPath => {
        const launcherDirectoryPath = join(rootDirectoryPath, 'bin');
        const libraryDirectoryPath = join(rootDirectoryPath, 'lib');
        await mkdir(launcherDirectoryPath, { recursive: true });
        await mkdir(libraryDirectoryPath, { recursive: true });
        const launcherPath = join(launcherDirectoryPath, 'athena-lsp-host.bat');
        await writeFile(launcherPath, '@echo off\n', 'utf8');
        await writeFile(join(libraryDirectoryPath, 'runtime.jar'), '', 'utf8');

        const manager = new TestableAthenaRepositorySessionManager();

        assert.throws(
            () => manager.exposeBuildLaunchSpec(
                launcherPath,
                { ATHENA_DISABLE_WINDOWS_INSTALL_SCAN: '1' },
                'win32'
            ),
            /Athena requires Java 25\+/
        );
    });
});

test('fails with a clear error when the Athena LSP host install has no runtime jars', async () => {
    await withTempDirectory('athena-session-host-', async rootDirectoryPath => {
        const launcherDirectoryPath = join(rootDirectoryPath, 'bin');
        const libraryDirectoryPath = join(rootDirectoryPath, 'lib');
        const javaHomePath = join(rootDirectoryPath, 'jdk-25');
        await mkdir(launcherDirectoryPath, { recursive: true });
        await mkdir(libraryDirectoryPath, { recursive: true });
        await mkdir(join(javaHomePath, 'bin'), { recursive: true });
        const launcherPath = join(launcherDirectoryPath, 'athena-lsp-host.bat');
        await writeFile(launcherPath, '@echo off\n', 'utf8');
        await writeFile(join(javaHomePath, 'bin', 'java.exe'), '', 'utf8');
        await writeFile(join(javaHomePath, 'release'), 'JAVA_VERSION="25.0.1"\n', 'utf8');

        const manager = new TestableAthenaRepositorySessionManager();

        assert.throws(
            () => manager.exposeBuildLaunchSpec(launcherPath, { ATHENA_JAVA_HOME: javaHomePath }, 'win32'),
            /Athena LSP host install is missing runtime jars/
        );
    });
});

test('resolves the nearest governed repository root for a nested Athena document and activates that session', async () => {
    await withTempDirectory('athena-session-root-', async rootDirectoryPath => {
        const monorepoPath = join(rootDirectoryPath, 'Athena');
        const nestedRepositoryPath = join(monorepoPath, 'examples', 'm4', 'open-repository-proof');
        const sourceRootPath = join(nestedRepositoryPath, 'src');
        const sourcePath = join(sourceRootPath, 'factory-line.athena');
        await mkdir(sourceRootPath, { recursive: true });
        await writeFile(join(nestedRepositoryPath, 'athena.yaml'), 'primaryPackage:\n  name: com.engineeringood.factory-line\n  version: 1.0.0\n  sourceRoot: src\n', 'utf8');
        await writeFile(sourcePath, 'system FactoryLine {\n}\n', 'utf8');

        const manager = new TestableAthenaRepositorySessionManager();
        const documentUri = pathToFileURL(sourcePath).toString();

        assert.equal(
            manager.exposeResolveRepositoryRootForDocumentUri(documentUri),
            nestedRepositoryPath
        );

        const state = await manager.ensureRepositorySessionForDocument(documentUri);

        assert.equal(manager.activations.length, 1);
        assert.equal(manager.activations[0], nestedRepositoryPath);
        assert.equal(state.lifecycle, 'ready');
        assert.equal(state.repositoryRoot, nestedRepositoryPath);
    });
});
