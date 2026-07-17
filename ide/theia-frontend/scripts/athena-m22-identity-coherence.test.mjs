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

test('M22 keeps source, outline, Problems, and sheet selection on canonical identities', () => {
    const selectionModel = readRepoFile('ide/theia-frontend/src/browser/athena-semantic-selection-model.ts');
    const widgetSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx');
    const bridgeSource = readRepoFile('ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts');
    const productPackage = JSON.parse(readRepoFile('ide/theia-product/package.json'));
    const usage = readRepoFile('docs/usages/m22-proof-usage.md');

    assert.match(selectionModel, /resolveRenderedSelectionTarget/);
    assert.match(selectionModel, /const semanticId = subject\.portSemanticId \?\? subject\.semanticId/);
    assert.match(selectionModel, /resolveSemanticRevealTargetFromDiagnostic/);
    assert.match(selectionModel, /semanticIdFromDiagnosticData\(diagnostic\.data\)/);
    assert.match(selectionModel, /resolveProjectionOccurrence\(diagram, selection\.semanticId\)/);
    assert.match(selectionModel, /resolveProjectionEndpointAlias\(diagram, selection\.semanticId\)/);
    assert.doesNotMatch(selectionModel, /message\.match|message\.includes|innerText|textContent|querySelector/);

    assert.match(widgetSource, /onSelectSemanticId=\{semanticId => this\.semanticSelectionService\.selectSemanticId\(semanticId\)\}/);
    assert.match(widgetSource, /resolveProjectionOccurrence\(this\.diagram, selectedSemanticId\)/);
    assert.match(widgetSource, /resolveProjectionEndpointAlias\(this\.diagram, selectedSemanticId\)/);
    assert.match(widgetSource, /resolveProjectionRelatedSubjects\(this\.diagram, selectedSemanticId\)/);

    assert.match(bridgeSource, /provideDocumentSymbols: async model =>/);
    assert.match(bridgeSource, /'textDocument\/documentSymbol'/);
    assert.match(bridgeSource, /uri: model\.uri\.toString\(\)/);
    assert.match(bridgeSource, /this\.sendLanguageRequest<DocumentSymbol\[]>\(/);

    const preferences = productPackage?.theia?.frontend?.config?.preferences ?? {};
    assert.equal(preferences['workbench.editor.revealIfOpen'], true);
    assert.equal(preferences['editor.enablePreview'], false);

    assert.match(usage, /source, outline, Problems, and sheet identity remain coherent/i);
    assert.match(usage, /same canonical subject and occurrence identity/i);
    assert.match(usage, /outline navigation in the same editor tab/i);
});
