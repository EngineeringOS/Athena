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

test('M22 publishes a named professional layout acceptance checklist', () => {
    const checklistPath = 'examples/m22/sample-project/M22-LAYOUT-ACCEPTANCE.md';
    assertFile(checklistPath);

    const checklist = readRepoFile(checklistPath);
    const sampleReadme = readRepoFile('examples/m22/sample-project/README.md');
    const usage = readRepoFile('docs/usages/m22-proof-usage.md');

    assert.match(checklist, /^# M22 Professional Layout Acceptance Checklist/m);
    assert.match(checklist, /M21 baseline/i);
    assert.match(checklist, /draft\/screenshort/i);
    assert.match(checklist, /zones/i);
    assert.match(checklist, /spacing/i);
    assert.match(checklist, /grouping/i);
    assert.match(checklist, /basic orthogonal edge routing/i);
    assert.match(checklist, /label overlap avoidance/i);
    assert.match(checklist, /Route-lane preference/i);
    assert.match(checklist, /horizontal-first or vertical-first schematic segments/i);
    assert.match(checklist, /declared subject bounds/i);
    assert.match(checklist, /outside the component body/i);
    assert.match(checklist, /Governed Placement And Grouping Evidence/i);
    assert.match(checklist, /LayoutConstraintSnapshot/);
    assert.match(checklist, /RuleBasedSchematicLayoutOptimizer/);
    assert.match(checklist, /SchematicLayoutGroupFact/);
    assert.match(checklist, /renderer must not infer/i);
    assert.match(checklist, /not full EPLAN parity/i);
    assert.match(checklist, /not pixel-perfect/i);
    assert.match(sampleReadme, /M22-LAYOUT-ACCEPTANCE\.md/);
    assert.match(usage, /M22-LAYOUT-ACCEPTANCE\.md/);
    assert.doesNotMatch(checklist, /must match EPLAN/i);
});
