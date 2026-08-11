import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';

const root = path.resolve(import.meta.dirname, '..');
const bridge = fs.readFileSync(path.join(root, 'src/browser/athena-lsp-editor-bridge-service.ts'), 'utf8');
const widget = fs.readFileSync(path.join(root, 'src/browser/athena-presentation-widget.tsx'), 'utf8');
const adapter = fs.readFileSync(path.join(root, 'src/browser/diagram/konva-diagram-adapter.ts'), 'utf8');

test('presentation edit context exposes engineering and route source authority', () => {
    assert.match(bridge, /engineeringWritableFiles:\s*string\[\]/);
    assert.match(bridge, /routeWritableFiles:\s*string\[\]/);
});

test('Port gesture intent uses semantic Port identity while anchor identity remains paint-only', () => {
    assert.match(adapter, /semanticPortId/);
    assert.match(adapter, /anchorId/);
    assert.match(adapter, /onConnectionIntent/);
    assert.match(adapter, /pendingPortCandidate/);
    assert.match(adapter, /commitPortIntent/);
    assert.doesNotMatch(adapter, /portId:\s*port\.anchorId/);
});

test('first Port click previews and second Port click emits one Connect intent', () => {
    assert.match(adapter, /if \(!this\.pendingPortCandidate\)/);
    assert.match(adapter, /this\.pendingPortCandidate\s*=/);
    assert.match(adapter, /kind:\s*'CONNECT_PORTS'/);
    assert.match(adapter, /await this\.onConnectionIntent\?\./);
    assert.doesNotMatch(adapter, /dragmove[^}]*onConnectionIntent/);
});

test('selected Connection then Port click emits one Reconnect intent', () => {
    assert.match(adapter, /kind:\s*'RECONNECT_CONNECTION_ENDPOINT'/);
    assert.match(adapter, /connectionId:\s*connection\.connectionId/);
    assert.match(adapter, /replacementPortId:\s*port\.semanticPortId/);
});

test('route drag paints transiently and emits exactly one logical operation on dragend', () => {
    assert.match(adapter, /onRouteIntent/);
    assert.match(adapter, /dragmove/);
    assert.match(adapter, /dragend/);
    assert.match(adapter, /pointFromRouteDrag/);
    assert.match(adapter, /kind:\s*'ADJUST_CONNECTION_ROUTE'/);
    assert.match(adapter, /column:/);
    assert.match(adapter, /row:/);
    assert.doesNotMatch(adapter, /dragmove[^}]*onRouteIntent/);
});

test('Escape and rejected commits clear transient connection and route previews', () => {
    assert.match(adapter, /event\.key === 'Escape'/);
    assert.match(adapter, /clearConnectionPreview/);
    assert.match(adapter, /clearRoutePreview/);
    assert.match(adapter, /if \(!accepted\)/);
});

test('widget builds generated Connect Reconnect and route envelopes with correct authority', () => {
    assert.match(widget, /onConnectionIntent:/);
    assert.match(widget, /onRouteIntent:/);
    assert.match(widget, /intent\.kind/);
    assert.match(widget, /CONNECT_PORTS/);
    assert.match(widget, /RECONNECT_CONNECTION_ENDPOINT/);
    assert.match(widget, /authorityClass:\s*'ENGINEERING'/);
    assert.match(widget, /requestedWritableFiles:\s*context\.engineeringWritableFiles/);
    assert.match(widget, /authorityClass:\s*'PRESENTATION'/);
    assert.match(widget, /requestedWritableFiles:\s*context\.routeWritableFiles/);
    assert.match(widget, /sourceRevision:\s*context\.sourceRevision/);
    assert.match(widget, /operationId:\s*crypto\.randomUUID\(\)/);
});

test('connection authoring refreshes only accepted server state and adds no toolbar control', () => {
    assert.match(widget, /result\.status !== 'ACCEPTED'/);
    assert.match(widget, /result\?\.status === 'ACCEPTED'/);
    assert.doesNotMatch(widget, /title='Connect|title='Reconnect|title='Adjust route/);
});

test('product automation reuses typed connection and route transaction paths', () => {
    assert.match(widget, /executeReconnect\(/);
    assert.match(widget, /executeRouteAdjust\(/);
    assert.match(widget, /this\.commitConnectionIntent\(/);
    assert.match(widget, /this\.commitRouteIntent\(/);
    assert.match(widget, /kind:\s*'RECONNECT_CONNECTION_ENDPOINT'/);
    assert.match(widget, /kind:\s*'ADJUST_CONNECTION_ROUTE'/);
    assert.doesNotMatch(widget, /writeFile|writeText|fs\./);
});

test('automation state exposes current connection and semantic port facts', () => {
    assert.match(widget, /connections:\s*scene\.connections\.map/);
    assert.match(widget, /semanticPortId:\s*port\.semanticPortId/);
    assert.match(widget, /direction:\s*port\.direction/);
    assert.match(widget, /segments:\s*connection\.segments/);
    assert.match(widget, /markers:\s*connection\.markers/);
    assert.match(widget, /annotations:\s*connection\.annotations/);
    assert.match(widget, /acceptedInputRevision:\s*this\.publication\.acceptedInputRevision/);
});

test('product automation exposes compiler-owned Connection IR through the existing LSP read model', () => {
    assert.match(widget, /getConnectionReadModel\(\)/);
    assert.match(widget, /getConnectionReadModel:\s*\(\)\s*=>\s*this\.bridge\.requestConnectionReadModel\(\)/);
});
