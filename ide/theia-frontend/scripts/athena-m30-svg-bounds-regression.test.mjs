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

test('M30 SVG viewBox is content-derived and duplicate-safe', () => {
    const modelSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-model.ts');
    const widgetSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx');
    const presentationNodeSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-presentation-node.tsx');

    assert.match(modelSource, /const sceneBounds = resolveSceneBounds\(nodes, edges, canvasWidth, canvasHeight\);/);
    assert.match(modelSource, /svgViewBox: formatSvgViewBox\(sceneBounds\)/);
    assert.match(modelSource, /presentationBoundsIntersectsCanvas\(occurrence\.bounds, canvasWidth, canvasHeight\)/);
    assert.doesNotMatch(modelSource, /svgViewBox:\s*['"`]0 0 (1680|960)/);
    assert.doesNotMatch(widgetSource, /viewBox=['"`]0 0 (1680|960)/);

    assert.match(presentationNodeSource, /const hasPartTextSlots = node\.presentationParts\.some/);
    assert.match(presentationNodeSource, /const renderOccurrenceTextSlots = !node\.presentationRepresentation && !hasPartTextSlots/);
    assert.doesNotMatch(presentationNodeSource, /<text[\s\S]*<text[\s\S]*\{slot\.text\}[\s\S]*\{slot\.text\}/);
});
