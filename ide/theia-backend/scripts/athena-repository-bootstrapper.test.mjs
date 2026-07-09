import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, readFile, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

const { AthenaRepositoryBootstrapper } = await import('../lib/node/athena-repository-bootstrapper.js');

async function withTempDirectory(prefix, callback) {
    const directoryPath = await mkdtemp(join(tmpdir(), prefix));
    try {
        await callback(directoryPath);
    } finally {
        await rm(directoryPath, { recursive: true, force: true });
    }
}

test('creates the governed M5 repository bootstrap shape', async () => {
    await withTempDirectory('athena-backend-', async parentDirectoryPath => {
        const bootstrapper = new AthenaRepositoryBootstrapper();

        const result = await bootstrapper.createRepository(parentDirectoryPath, 'Factory Line');

        assert.equal(result.repositoryName, 'Factory Line');
        assert.equal(result.projectName, 'factory-line');
        assert.equal(result.repositoryRootPath, join(parentDirectoryPath, 'Factory Line'));
        assert.equal(result.sourcePath, join(parentDirectoryPath, 'Factory Line', 'src', 'factory-line.athena'));

        const manifest = await readFile(join(result.repositoryRootPath, 'athena.yaml'), 'utf8');
        const lock = await readFile(join(result.repositoryRootPath, 'athena.lock'), 'utf8');
        const source = await readFile(result.sourcePath, 'utf8');

        assert.match(manifest, /^primaryPackage:\n/m);
        assert.match(manifest, /name: com\.engineeringood\.factory-line/);
        assert.match(manifest, /version: 0\.1\.0/);
        assert.match(manifest, /sourceRoot: src/);
        assert.match(lock, /Generated from compiler-owned repository resolution/);
        assert.match(lock, /primaryPackage:/);
        assert.match(lock, /name: com\.engineeringood\.factory-line/);
        assert.match(lock, /sourceRoot: src/);
        assert.match(lock, /dependencies: \[\]/);
        assert.equal(source, 'system FactoryLine {\n}\n');
    });
});

test('rejects invalid repository names before creating a governed bootstrap', async () => {
    await withTempDirectory('athena-backend-', async parentDirectoryPath => {
        const bootstrapper = new AthenaRepositoryBootstrapper();

        await assert.rejects(
            () => bootstrapper.createRepository(parentDirectoryPath, '***'),
            /Repository name must use letters, numbers, spaces, hyphens, or underscores/,
        );
    });
});

test('rejects duplicate repository targets', async () => {
    await withTempDirectory('athena-backend-', async parentDirectoryPath => {
        const bootstrapper = new AthenaRepositoryBootstrapper();

        await bootstrapper.createRepository(parentDirectoryPath, 'Factory Line');

        await assert.rejects(
            () => bootstrapper.createRepository(parentDirectoryPath, 'Factory Line'),
            /Target repository already exists/,
        );
    });
});
