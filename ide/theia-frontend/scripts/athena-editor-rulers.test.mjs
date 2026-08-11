import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';

const { editorRulerLabels } = await import('../lib/browser/diagram/editor-rulers.js');
const root = path.resolve(import.meta.dirname, '..');
const widget = fs.readFileSync(path.join(root, 'src/browser/athena-presentation-widget.tsx'), 'utf8');
const css = fs.readFileSync(path.join(root, 'src/browser/style/index.css'), 'utf8');

test('editor ruler labels come only from published Plot Frame counts', () => {
    assert.deepEqual(editorRulerLabels(3, 4), {
        rows: ['A', 'B', 'C'],
        columns: ['1', '2', '3', '4'],
    });
    assert.equal(editorRulerLabels(27, 1).rows.at(-1), 'AA');
});

test('widget renders one corner and two plain flex rulers around the canvas host', () => {
    assert.match(widget, /editorRulerLabels\(scene\.plotFrame\.rows, scene\.plotFrame\.columns\)/);
    assert.match(widget, /athena-presentation__ruler-corner/);
    assert.match(widget, /athena-presentation__ruler--columns/);
    assert.match(widget, /athena-presentation__ruler--rows/);
    assert.match(widget, /athena-presentation__document-frame/);
});

test('ruler CSS is small edge-pinned flex chrome around an uncluttered canvas', () => {
    assert.match(css, /\.athena-presentation__canvas-shell\s*\{[^}]*flex-direction:\s*column/s);
    assert.match(css, /\.athena-presentation__ruler\s*\{[^}]*display:\s*flex/s);
    assert.match(css, /\.athena-presentation__ruler--rows\s*\{[^}]*flex-direction:\s*column/s);
    assert.match(css, /\.athena-presentation__ruler-cell[^}]*border/s);
    assert.match(css, /\.athena-presentation__canvas-viewport\s*\{[^}]*padding:\s*0;/s);
    assert.match(css, /\.athena-presentation__document-frame\s*\{[^}]*grid-template-columns:\s*20px\s+minmax\(0, 1fr\);/s);
    assert.match(css, /\.athena-presentation__document-frame\s*\{[^}]*grid-template-rows:\s*20px\s+minmax\(0, 1fr\);/s);
    assert.match(css, /\.athena-presentation__document-frame\s*\{[^}]*width:\s*100%;[^}]*height:\s*100%;/s);
    assert.doesNotMatch(css, /\.athena-presentation__document-frame\s*\{[^}]*aspect-ratio:/s);
    assert.match(css, /\.athena-presentation__style-bar\s*\{/);
});
