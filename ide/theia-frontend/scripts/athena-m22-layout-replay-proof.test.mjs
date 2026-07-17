import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import test from 'node:test';

const repoRoot = [
    process.cwd(),
    resolve(process.cwd(), '..'),
    resolve(process.cwd(), '..', '..'),
].find(candidate => existsSync(resolve(candidate, '_bmad-output'))) ?? process.cwd();

function readRepoFile(path) {
    return readFileSync(resolve(repoRoot, path), 'utf8');
}

function assertFile(path) {
    assert.equal(existsSync(resolve(repoRoot, path)), true, `${path} should exist`);
}

test('M22 publishes deterministic replay proof before visual acceptance', () => {
    const proofPath = 'examples/m22/sample-project/M22-LAYOUT-REPLAY-PROOF.md';
    assertFile(proofPath);

    const proof = readRepoFile(proofPath);
    const checklist = readRepoFile('examples/m22/sample-project/M22-LAYOUT-ACCEPTANCE.md');
    const sampleReadme = readRepoFile('examples/m22/sample-project/README.md');
    const usage = readRepoFile('docs/usages/m22-proof-usage.md');
    const engineTest = readRepoFile(
        'kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt',
    );

    assert.match(proof, /^# M22 Deterministic Layout Replay Proof/m);
    assert.match(proof, /layout facts are compared across repeated runs before screenshot/i);
    assert.match(proof, /adapter-normalized facts/i);
    assert.match(proof, /Athena normalization/i);
    assert.match(proof, /zones/i);
    assert.match(proof, /spacing/i);
    assert.match(proof, /grouping/i);
    assert.match(proof, /basic orthogonal edge routing/i);
    assert.match(proof, /label overlap avoidance/i);
    assert.match(checklist, /M21 Baseline Comparison/i);
    assert.match(sampleReadme, /M22-LAYOUT-REPLAY-PROOF\.md/);
    assert.match(usage, /M22-LAYOUT-REPLAY-PROOF\.md/);
    assert.match(engineTest, /optimization boundary canonicalizes inputs and emits stable Athena layout facts/);
    assert.match(engineTest, /assertEquals\(first, second\)/);
});
