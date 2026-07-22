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

test('graph workbench applies approved layout preview as reviewable source edit', () => {
    const modelSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-model.ts');
    const widgetSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx');
    const usage = readRepoFile('docs/usages/m22-proof-usage.md');

    assert.match(modelSource, /sourceEdit:\s*AthenaAuthoringSourceEditPayload/);
    assert.match(modelSource, /sourceEdit\.newText\.trim\(\)/);
    assert.match(widgetSource, /acceptLayoutMutationPreview/);
    assert.match(widgetSource, /applyAuthoringSourceEdit\(preview\.sourceEdit\)/);
    assert.match(widgetSource, /this\.layoutMutationPreview = undefined/);
    assert.match(widgetSource, /this\.scheduleRefresh\(\)/);
    assert.doesNotMatch(widgetSource, /localStorage|sessionStorage|indexedDB/);
    assert.match(usage, /approved adjustments/i);
    assert.match(usage, /reviewable `\.athena`\s+layout intent/i);
});
