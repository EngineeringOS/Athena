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

test('M22 compares ELK-assisted output using normalized Athena facts', () => {
    const comparisonPath = '_bmad-output/implementation-artifacts/m22/M22-ELK-COMPARISON.md';
    assertFile(comparisonPath);

    const comparison = readRepoFile(comparisonPath);
    const usage = readRepoFile('docs/usages/m22-proof-usage.md');
    const engineTest = readRepoFile(
        'kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt',
    );

    assert.match(comparison, /^# M22 ELK Comparison/m);
    assert.match(comparison, /normalized Athena layout facts/i);
    assert.match(comparison, /not raw adapter output/i);
    assert.match(comparison, /spacing/i);
    assert.match(comparison, /grouping/i);
    assert.match(comparison, /basic routing/i);
    assert.match(comparison, /does not select ELK as final architecture/i);
    assert.match(comparison, /not the sole layout engine/i);
    assert.match(usage, /M22-ELK-COMPARISON\.md/);
    assert.match(engineTest, /experimental ELK adapter normalizes output into Athena facts/);
});
