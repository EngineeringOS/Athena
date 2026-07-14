import assert from 'node:assert/strict';
import test from 'node:test';

const sessionModelModule = await import('../lib/browser/athena-repository-session-model.js');

test('isAthenaDocumentCoveredBySession accepts Athena files inside the active governed source root', () => {
    const covered = sessionModelModule.isAthenaDocumentCoveredBySession(
        {
            lifecycle: 'ready',
            repositoryRoot: 'D:/Aaron/workspace/projects/2026/eos/Athena/examples/m4/open-repository-proof',
            sourceRootPath: 'D:/Aaron/workspace/projects/2026/eos/Athena/examples/m4/open-repository-proof/src'
        },
        'file:///d%3A/Aaron/workspace/projects/2026/eos/Athena/examples/m4/open-repository-proof/src/factory-line.athena'
    );

    assert.equal(covered, true);
});

test('isAthenaDocumentCoveredBySession rejects Athena files outside the active repository session', () => {
    const covered = sessionModelModule.isAthenaDocumentCoveredBySession(
        {
            lifecycle: 'ready',
            repositoryRoot: 'D:/Aaron/workspace/projects/2026/eos/Athena/examples/m5/repository-a',
            sourceRootPath: 'D:/Aaron/workspace/projects/2026/eos/Athena/examples/m5/repository-a/src'
        },
        'file:///d%3A/Aaron/workspace/projects/2026/eos/Athena/examples/m4/open-repository-proof/src/factory-line.athena'
    );

    assert.equal(covered, false);
});
