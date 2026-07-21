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

test('M30 normal graph chrome is transparent and state chrome is conditional', () => {
    const css = readRepoFile('ide/theia-frontend/src/browser/style/index.css');
    const widgetSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx');

    assert.match(css, /\.athena-graph-workbench__node-hitbox\s*\{[^}]*fill:\s*transparent;[^}]*stroke:\s*transparent;/s);
    assert.match(css, /\.athena-graph-workbench__node--electrical-device\s*\{[^}]*fill:\s*transparent;/s);
    assert.match(css, /\.athena-graph-workbench__element:is\(:hover, :focus, :focus-visible\) > \.athena-graph-workbench__node-hitbox\s*\{[^}]*stroke:/s);
    assert.match(css, /\.athena-graph-workbench__node--selected\s*\{/);
    assert.doesNotMatch(css, /\.athena-graph-workbench__element\s*\{[^}]*border:/s);

    assert.match(widgetSource, /className='athena-graph-workbench__node-hitbox'/);
    assert.doesNotMatch(widgetSource, /className='athena-graph-workbench__.*wrapper.*border/i);
});
