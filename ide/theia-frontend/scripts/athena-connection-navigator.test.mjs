import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';

const root = path.resolve(import.meta.dirname, '..');
const read = relativePath => fs.readFileSync(path.join(root, relativePath), 'utf8');
const bridge = read('src/browser/athena-lsp-editor-bridge-service.ts');
const widget = read('src/browser/athena-connection-navigator-widget.tsx');
const moduleSource = read('src/browser/athena-frontend-module.ts');
const extensions = read('src/browser/athena-workbench-extensions.ts');
const selectionService = read('src/browser/athena-semantic-selection-service.ts');
const presentation = read('src/browser/athena-presentation-widget.tsx');
const adapter = read('src/browser/diagram/konva-diagram-adapter.ts');

test('Connection Navigator uses one typed LSP read model boundary', () => {
    assert.match(bridge, /AthenaConnectionReadModelPublication/);
    assert.match(bridge, /requestConnectionReadModel\(widget: EditorWidget \| undefined/);
    assert.match(bridge, /textDocument: \{ uri: sourceEditor\.editor\.uri\.toString\(\) \}/);
    assert.match(bridge, /'athena\/connectionReadModel'/);
    assert.doesNotMatch(widget, /fetch\(|ConnectionDocument|SpatialSheet|SceneConnection|readFile|writeFile/);
});

test('Connection Navigator is registered as compact left workbench view', () => {
    assert.match(moduleSource, /bind\(AthenaConnectionNavigatorWidget\)\.toSelf\(\)/);
    assert.match(moduleSource, /id: AthenaConnectionNavigatorWidget\.ID/);
    const extension = extensions.match(/\{\s*command:\s*AthenaCommands\.REVEAL_CONNECTION_NAVIGATOR,[\s\S]*?\n\s*\},/)?.[0];
    assert.ok(extension, 'Connection Navigator extension missing');
    assert.match(extension, /area:\s*'left'/);
});

test('frontend cache accepts only current READY accepted revision', () => {
    assert.match(widget, /readyByAcceptedInputRevision/);
    assert.match(widget, /response\.state !== 'READY'/);
    assert.match(widget, /response\.attemptedInputRevision !== acceptedRevision/);
    assert.match(widget, /requestSequence !== this\.refreshSequence/);
    assert.match(widget, /requestedRepositoryRoot !== this\.repositorySessionService\.state\.repositoryRoot/);
    assert.match(widget, /this\.clearReadModel\(\)/);
});

test('unplaced connectivity stays inspectable without placeholder graphics', () => {
    assert.match(widget, /item\.placed \? `\$\{item\.projectionCount\} placed` : 'unplaced'/);
    assert.match(widget, /item\.endpoints/);
    assert.match(widget, /item\.resolvedSpecifications/);
    assert.doesNotMatch(widget, /placeholder|Konva|Scene/);
});

test('Navigator source selection and canvas connection selection share semantic service', () => {
    assert.match(widget, /selectPublishedSourceTrace\(item\.sourceTrace, item\.semanticId\)/);
    assert.match(selectionService, /resolveSemanticSelectionFromPublishedSourceTrace/);
    assert.match(presentation, /onDidChangeSelection\(selection =>/);
    assert.match(presentation, /selectSemanticConnection\(selection\?\.semanticId\)/);
});

test('external connection selection paints every matching projection only', () => {
    assert.match(adapter, /selectSemanticConnection\(connectionId: string \| undefined\)/);
    assert.match(adapter, /scene\.connections\.filter\(connection => connection\.connectionId === this\.selectedConnectionId\)/);
    assert.match(adapter, /for \(const selectedConnection of selectedConnections\)/);
    const method = adapter.match(/selectSemanticConnection\(connectionId:[\s\S]*?\n    \}/)?.[0] ?? '';
    assert.doesNotMatch(method, /stylePreview|publication\s*=|scene\s*=|onSelection/);
});
