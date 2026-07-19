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

test('M25 graph workbench renders governed representation terminal and label DOM markers', () => {
    const widgetSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx');
    const modelSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-model.ts');

    assert.match(widgetSource, /data-athena-representation-fact=\{node\.presentationRepresentation \? 'true' : undefined\}/);
    assert.match(widgetSource, /data-athena-render-fallback=\{node\.presentationRepresentation \? 'false' : undefined\}/);
    assert.match(widgetSource, /data-athena-presentation-terminal='true'/);
    assert.match(widgetSource, /data-athena-presentation-terminal-number=\{terminal\.number\}/);
    assert.match(widgetSource, /data-athena-presentation-label='true'/);
    assert.match(widgetSource, /data-athena-presentation-label-role=\{label\.role\}/);
    assert.match(modelSource, /presentationTerminals: representation/);
    assert.match(modelSource, /presentationLabels: representation/);
});
