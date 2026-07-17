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

test('M22 documents the IDE-visible baseline proof path', () => {
    const proofPath = 'examples/m22/sample-project/M22-BASELINE-PROOF.md';
    assertFile(proofPath);

    const proof = readRepoFile(proofPath);
    const sampleReadme = readRepoFile('examples/m22/sample-project/README.md');
    const usage = readRepoFile('docs/usages/m22-proof-usage.md');
    const smoke = readRepoFile('ide/theia-product/scripts/verify-athena-m22-sample-project.js');

    assert.match(proof, /^# M22 IDE-Visible Baseline Proof/m);
    assert.match(proof, /src\/01-baseline-sheet\.athena/);
    assert.match(proof, /Graphical View/);
    assert.match(proof, /stage grid/i);
    assert.match(proof, /sheet and component bodies/i);
    assert.match(proof, /Cabinet Main/i);
    assert.match(proof, /transparent canvas overlays/i);
    assert.match(proof, /active source file/i);
    assert.match(sampleReadme, /M22-BASELINE-PROOF\.md/);
    assert.match(usage, /M22-BASELINE-PROOF\.md/);
    [
        'stageHasGrid',
        'sheetTransparent',
        'floatingBarTransparent',
        'bottomDockTransparent',
        'zoomDockTransparent',
        'infoPopoverOpened',
        'infoPopoverClosedOnWhitespace'
    ].forEach(marker => {
        assert.match(smoke, new RegExp(marker));
    });
});
