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

test('M30 renderer paints resolved presentation primitives without raw asset authority', () => {
    const presentationModelSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-presentation-model.ts');
    const presentationNodeSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-presentation-node.tsx');

    assert.match(presentationModelSource, /function representationPrimitiveToCommand/);
    assert.match(presentationModelSource, /case 'rectangle':/);
    assert.match(presentationModelSource, /case 'line':/);
    assert.match(presentationModelSource, /case 'circle':/);
    assert.match(presentationModelSource, /case 'polyline':/);
    assert.doesNotMatch(presentationModelSource, /default:\s*return\s*\{\s*kind:\s*'stroke_rectangle'/s);
    assert.doesNotMatch(presentationModelSource, /qet|\.elmt|readFileSync|fetch\(/i);

    assert.match(presentationNodeSource, /renderPresentationCommand/);
    assert.match(presentationNodeSource, /data-athena-render-authority='presentation-ir'/);
    assert.doesNotMatch(presentationNodeSource, /qet|\.elmt|fetch\(/i);
});
