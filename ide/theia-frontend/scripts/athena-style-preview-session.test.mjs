import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';

const root = path.resolve(import.meta.dirname, '..');
const source = fs.readFileSync(path.join(root, 'src/browser/diagram/style-preview-session.ts'), 'utf8');
const widget = fs.readFileSync(path.join(root, 'src/browser/athena-presentation-widget.tsx'), 'utf8');

test('style preview stays revision-bound, immutable, and disposable', () => {
    assert.match(source, /class StylePreviewSession/);
    assert.match(source, /acceptedInputRevision/);
    assert.match(source, /preview\(/);
    assert.match(source, /discard\(/);
    assert.match(source, /clearForPublication\(/);
    assert.doesNotMatch(source, /\.athena|fetch\(|writeFile|FileSystem/);
});

test('solidify submits typed intent and never assembles Athena source', () => {
    assert.match(widget, /requestPresentationEditContext/);
    assert.match(widget, /requestEditOperation/);
    assert.match(widget, /sourceRevision: context\.sourceRevision/);
    assert.match(widget, /requestedWritableFiles: context\.styleWritableFiles/);
    assert.match(widget, /kind: 'SET_STYLE'/);
    assert.match(widget, /authorityClass: 'PRESENTATION'/);
    assert.doesNotMatch(widget, /\.sheet\.style\.athena|style \"|writeFile|FileSystem/);
});

test('preview supports required presentation fields without engineering fields', () => {
    assert.match(source, /strokeWidth/);
    assert.match(source, /dash/);
    assert.match(source, /strokeRgba/);
    assert.match(source, /fontSize/);
    assert.match(source, /routeMarker/);
    assert.match(source, /portDisplay/);
    assert.doesNotMatch(source, /relationship|capability|partRef|symbolRef|direction|flowKind/);
});
