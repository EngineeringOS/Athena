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

test('graph workbench source captures M22 layout adjustment intent without hidden canvas truth', () => {
    const modelSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-model.ts');
    const widgetSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx');

    assert.match(modelSource, /AthenaGraphLayoutAdjustmentIntent/);
    assert.match(modelSource, /captureAthenaGraphLayoutAdjustmentIntent/);
    assert.match(modelSource, /subjectSemanticId/);
    assert.match(modelSource, /occurrenceId/);
    assert.match(modelSource, /viewId/);
    assert.match(modelSource, /sheetId/);
    assert.match(modelSource, /snapshotId/);
    assert.match(modelSource, /sourceUri/);
    assert.match(modelSource, /transientOnly:\s*true/);
    assert.match(modelSource, /persisted:\s*false/);
    assert.match(modelSource, /route and label adjustment persistence is outside M22 scope/i);
    assert.doesNotMatch(modelSource, /localStorage|sessionStorage|indexedDB/);
    assert.match(widgetSource, /captureAthenaGraphLayoutAdjustmentIntent/);
    assert.match(widgetSource, /lastLayoutAdjustmentIntent/);
});
