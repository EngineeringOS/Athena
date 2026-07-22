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

function methodBody(source, methodName) {
    const methodPattern = new RegExp(`protected\\s+(?:async\\s+)?${methodName}\\b`);
    const match = source.match(methodPattern);
    const start = match?.index ?? -1;
    assert.notEqual(start, -1, `${methodName} should exist`);
    const nextMethod = source.indexOf('\n    protected ', start + methodName.length);
    return source.slice(start, nextMethod === -1 ? source.length : nextMethod);
}

const graphWorkbenchModel = await import('../lib/browser/athena-graph-workbench-model.js');
const protocolSource = readRepoFile('ide/theia-frontend/src/browser/athena-authoring-protocol.ts');
const graphWorkbenchSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx');
const bridgeSource = readRepoFile('ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts');

function selector(entries, activeSheetViewId = entries[0]?.sheetViewId) {
    return {
        activeSheetViewId,
        hasMultipleSheetViews: entries.length > 1,
        entries: entries.map((entry, index) => ({
            sheetViewId: entry.sheetViewId,
            displayOrder: index + 1,
            title: entry.title,
            role: entry.role,
            subjectCount: entry.subjectCount ?? 1,
            isActive: entry.sheetViewId === activeSheetViewId,
            label: `${index + 1} - ${entry.title}`,
        })),
    };
}

test('M31 visible sheet selector preserves only the governed two-sheet policy through mode switches', () => {
    const emptyModeModel = { sheetViewSelector: undefined };
    const m31Selector = selector([
        { sheetViewId: 'm31/sheet/control', title: 'Control', role: 'control-and-plc-logic' },
        { sheetViewId: 'm31/sheet/field-device', title: 'Field Device', role: 'field-wiring-and-terminal-transition' },
    ]);
    const staleThreeSheetSelector = selector([
        { sheetViewId: 'legacy/sheet/source-a', title: 'Source A', role: 'source_file' },
        { sheetViewId: 'legacy/sheet/source-b', title: 'Source B', role: 'source_file' },
        { sheetViewId: 'legacy/sheet/source-c', title: 'Source C', role: 'source_file' },
    ]);

    assert.equal(
        graphWorkbenchModel.resolveVisibleAthenaGraphSheetViewSelector(emptyModeModel, m31Selector).entries.length,
        2,
    );
    assert.deepEqual(
        graphWorkbenchModel.resolveVisibleAthenaGraphSheetViewSelector(emptyModeModel, m31Selector).entries.map(entry => entry.role),
        ['control-and-plc-logic', 'field-wiring-and-terminal-transition'],
    );
    assert.equal(
        graphWorkbenchModel.resolveVisibleAthenaGraphSheetViewSelector(emptyModeModel, staleThreeSheetSelector),
        undefined,
    );
    assert.equal(
        graphWorkbenchModel.resolveVisibleAthenaGraphSheetViewSelector({ sheetViewSelector: staleThreeSheetSelector }, m31Selector),
        undefined,
    );
});

test('M31 authoring diagnostics expose recovery actions as transport data', () => {
    assert.match(protocolSource, /recoveryAction\?: string/);
    assert.match(protocolSource, /authoring\.validation\.stop-downstream/);
    assert.match(protocolSource, /authoring\.projection\.failed-after-commit/);
    assert.doesNotMatch(protocolSource, /Projection unavailable/);
});

test('Graphical View renders lifecycle diagnostics with recovery actions and keeps controls separate', () => {
    const createPreviewBody = methodBody(graphWorkbenchSource, 'renderCreateEntityPreview');
    const relationshipPreviewBody = methodBody(graphWorkbenchSource, 'renderSemanticRelationshipPreview');
    const sheetSelectorBody = methodBody(graphWorkbenchSource, 'renderSheetViewSelector');

    for (const body of [createPreviewBody, relationshipPreviewBody]) {
        assert.match(body, /diagnostic\.authority/);
        assert.match(body, /diagnostic\.lifecycleStage/);
        assert.match(body, /diagnostic\.recoveryAction/);
        assert.doesNotMatch(body, /Projection unavailable/);
    }

    assert.match(sheetSelectorBody, /resolveVisibleSheetViewSelector\(model\)/);
    assert.doesNotMatch(sheetSelectorBody, /createEntityPreview|connectPreview|Projection unavailable/);
});

test('Graphical View lifecycle guards keep projection-failed commits and invalid previews recoverable', () => {
    const acceptCreateBody = methodBody(graphWorkbenchSource, 'acceptCreateEntityPreview');
    const previewCreateBody = methodBody(graphWorkbenchSource, 'previewCreateEntityTransaction');
    const acceptRelationshipBody = methodBody(graphWorkbenchSource, 'acceptConnectPreview');
    const rejectRelationshipBody = methodBody(graphWorkbenchSource, 'rejectConnectPreview');
    const cancelRelationshipBody = methodBody(graphWorkbenchSource, 'cancelConnectPreview');
    const refreshBody = methodBody(graphWorkbenchSource, 'refreshDiagram');

    assert.match(protocolSource, /lifecycleState === 'reprojected'/);
    assert.match(protocolSource, /lifecycleState === 'projection-failed'/);
    assert.match(protocolSource, /projectionFailedAfterCommit/);
    assert.match(acceptCreateBody, /committedDiagnostics/);
    assert.match(acceptCreateBody, /sourceEditMatchesActiveDocument\(preview\.entityCreationEvidence\.sourceEdit\.revisionGuard\)/);
    assert.match(acceptCreateBody, /createEntityPreviewMessage = committedDiagnostics \|\| undefined/);
    assert.match(previewCreateBody, /createEntityPreviewRequestToken/);
    assert.match(previewCreateBody, /requestToken !== this\.createEntityPreviewRequestToken/);
    assert.match(acceptRelationshipBody, /committedDiagnostics/);
    assert.match(acceptRelationshipBody, /connectPreviewMessage = committedDiagnostics \|\| undefined/);
    assert.match(rejectRelationshipBody, /isSameConnectPreviewSession\(preview\)/);
    assert.doesNotMatch(rejectRelationshipBody, /isCurrentConnectPreview\(preview\)/);
    assert.match(cancelRelationshipBody, /isSameConnectPreviewSession\(preview\)/);
    assert.doesNotMatch(cancelRelationshipBody, /isCurrentConnectPreview\(preview\)/);
    assert.match(refreshBody, /!this\.connectApplyingDecision/);
    assert.match(refreshBody, /!this\.createEntityApplyingDecision/);
});

test('Theia document symbol bridge preserves nested port children for Outline', () => {
    const symbolBody = methodBody(bridgeSource, 'toMonacoDocumentSymbol');
    assert.match(symbolBody, /children:\s*\(symbol\.children \?\? \[\]\)\.map\(child => this\.toMonacoDocumentSymbol\(child\)\)/);
    assert.match(bridgeSource, /registerDocumentSymbolProvider\(ATHENA_LANGUAGE_ID/);
    assert.match(bridgeSource, /'textDocument\/documentSymbol'/);
});
