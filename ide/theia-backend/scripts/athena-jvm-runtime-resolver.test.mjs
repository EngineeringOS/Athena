import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdir, mkdtemp, writeFile, rm } from 'node:fs/promises';
import { join } from 'node:path';
import { tmpdir } from 'node:os';

const { AthenaJvmRuntimeResolver } = await import('../lib/node/athena-jvm-runtime-resolver.js');

async function withTempDirectory(prefix, callback) {
    const directoryPath = await mkdtemp(join(tmpdir(), prefix));
    try {
        await callback(directoryPath);
    } finally {
        await rm(directoryPath, { recursive: true, force: true });
    }
}

async function writeFakeJavaHome(javaHomePath, version, platform = 'win32') {
    const executableName = platform === 'win32' ? 'java.exe' : 'java';
    await mkdir(join(javaHomePath, 'bin'), { recursive: true });
    await writeFile(join(javaHomePath, 'bin', executableName), '', 'utf8');
    await writeFile(join(javaHomePath, 'release'), `JAVA_VERSION="${version}"\n`, 'utf8');
}

test('prefers ATHENA_JAVA_HOME when it already points to Java 25', async () => {
    await withTempDirectory('athena-jvm-resolver-', async rootDirectoryPath => {
        const javaHomePath = join(rootDirectoryPath, 'jdk-25');
        await writeFakeJavaHome(javaHomePath, '25.0.1');

        const resolver = new AthenaJvmRuntimeResolver();
        const resolution = resolver.resolve(
            {
                ATHENA_JAVA_HOME: javaHomePath
            },
            'win32'
        );

        assert.equal(resolution.status, 'ready');
        assert.equal(resolution.source, 'athena-java-home');
        assert.equal(resolution.javaHome, javaHomePath);
        assert.equal(resolution.majorVersion, 25);
    });
});

test('falls back from JAVA_HOME 19 to an Athena-managed Java 25 runtime', async () => {
    await withTempDirectory('athena-jvm-resolver-', async rootDirectoryPath => {
        const java19HomePath = join(rootDirectoryPath, 'jdk-19');
        const localAppDataPath = join(rootDirectoryPath, 'local-app-data');
        const managedRuntimePath = join(localAppDataPath, 'EngineeringOOD', 'Athena', 'runtimes', 'jdk-25.0.1');
        await writeFakeJavaHome(java19HomePath, '19.0.2');
        await writeFakeJavaHome(managedRuntimePath, '25.0.1');

        const resolver = new AthenaJvmRuntimeResolver();
        const resolution = resolver.resolve(
            {
                JAVA_HOME: java19HomePath,
                LOCALAPPDATA: localAppDataPath,
            },
            'win32'
        );

        assert.equal(resolution.status, 'ready');
        assert.equal(resolution.source, 'managed-runtime');
        assert.equal(resolution.javaHome, managedRuntimePath);
        assert.equal(resolution.version, '25.0.1');
    });
});

test('uses PATH java when environment variables are unset', async () => {
    await withTempDirectory('athena-jvm-resolver-', async rootDirectoryPath => {
        const javaDirectoryPath = join(rootDirectoryPath, 'bin');
        const javaHomePath = rootDirectoryPath;
        await mkdir(javaDirectoryPath, { recursive: true });
        await writeFakeJavaHome(javaHomePath, '25.0.1');

        const resolver = new AthenaJvmRuntimeResolver();
        const resolution = resolver.resolve(
            {
                PATH: javaDirectoryPath,
                ATHENA_DISABLE_WINDOWS_INSTALL_SCAN: '1',
            },
            'win32'
        );

        assert.equal(resolution.status, 'ready');
        assert.equal(resolution.source, 'path-java');
        assert.equal(resolution.javaHome, javaHomePath);
    });
});

test('reports a clear missing-runtime error when no Java 25 candidate exists', () => {
    const resolver = new AthenaJvmRuntimeResolver();
    const resolution = resolver.resolve(
        {
            PATH: '',
            ATHENA_DISABLE_WINDOWS_INSTALL_SCAN: '1',
        },
        'win32'
    );

    assert.equal(resolution.status, 'missing');
    assert.match(resolution.message, /Athena requires Java 25\+/);
});
