import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';

const root = path.resolve(import.meta.dirname, '..');
const css = fs.readFileSync(path.join(root, 'src/browser/style/index.css'), 'utf8');
const adapter = fs.readFileSync(path.join(root, 'src/browser/diagram/konva-diagram-adapter.ts'), 'utf8');
const widget = fs.readFileSync(path.join(root, 'src/browser/athena-presentation-widget.tsx'), 'utf8');
const opener = fs.readFileSync(path.join(root, 'src/browser/athena-sheet-companion-opener.ts'), 'utf8');

test('engineering document keeps compact style controls beside the canvas', () => {
    assert.match(widget, /athena-presentation__style-bar/);
    assert.match(widget, /<button type='button'/);
    assert.match(css, /\.athena-presentation__style-bar\s*\{/);
    assert.doesNotMatch(widget, /MARKER_AND_LABEL/);
});

test('Folio sheet companions open as independent editor widgets', () => {
    assert.match(widget, /configureSheet/);
    assert.match(widget, /onActivateRequest/);
    assert.match(widget, /this\.bridge\.requestDiagramScene\(sheetId\)/);
    assert.match(opener, /getOrCreateWidget/);
    assert.match(opener, /sheetId/);
    assert.match(widget, /showFolioBar/);
    assert.match(widget, /showFolioBar \? <nav className='athena-presentation__folio-bar'/);
    assert.match(widget, /new URI\(this\.sourceUri\)\.path\.toString\(\)\.toLowerCase\(\)\.endsWith\('\.folio\.athena'\)/);
    assert.match(widget, /requestFolioPages/);
    assert.match(widget, /athena-presentation__folio-bar/);
});

test('canvas selection does not switch style authority or recolor the document', () => {
    assert.doesNotMatch(widget, /this\.styleTarget = 'occurrence'/);
});

test('canvas click selects semantic subject without opening source editor', () => {
    assert.match(widget, /this\.selectionService\.selectSemanticId\(selection\.semanticId\)/);
    assert.doesNotMatch(widget, /this\.selectionService\.selectSceneTrace\(trace, selection\.semanticId\)/);
});

test('connection selection preserves style target and annotation visibility', () => {
    assert.match(widget, /selection\?\.kind === 'connection'/);
    assert.doesNotMatch(widget, /selection\?\.kind === 'connection'[^}]*styleTarget\s*=/s);
    assert.doesNotMatch(widget, /selection\?\.kind === 'connection'[^}]*annotations/s);
});

test('presentation widget gives canvas the complete center editor panel', () => {
    assert.match(css, /\.athena-presentation-widget\s*\{[^}]*display:\s*flex;[^}]*height:\s*100%;/s);
    assert.match(css, /\.athena-presentation__canvas-shell\s*\{[^}]*display:\s*flex;[^}]*flex:\s*1 1 auto;[^}]*background:\s*#fff;/s);
    assert.match(css, /\.athena-presentation__canvas-host\s*\{[^}]*display:\s*block;[^}]*flex:\s*1 1 auto;/s);
});

test('default page fit uses all available host space without artificial breathing margin', () => {
    assert.match(adapter, /this\.fitScale\s*=\s*Math\.min\(width \/ bounds\.width, height \/ bounds\.height\);/);
    assert.doesNotMatch(adapter, /Math\.min\(width \/ bounds\.width, height \/ bounds\.height\)\s*\*\s*0\.94/);
});

test('page frame anchors to top of full-height canvas', () => {
    assert.match(adapter, /y:\s*-bounds\.y\s*\*\s*this\.fitScale/);
    assert.doesNotMatch(adapter, /y:\s*\(height\s*-\s*bounds\.height\s*\*\s*this\.fitScale\)\s*\/\s*2/);
});
