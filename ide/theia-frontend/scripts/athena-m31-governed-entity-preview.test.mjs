import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const protocolSource = await readFile(
    new URL('../src/browser/athena-authoring-protocol.ts', import.meta.url),
    'utf8',
);
const graphWorkbenchSource = await readFile(
    new URL('../src/browser/athena-graph-workbench-widget.tsx', import.meta.url),
    'utf8',
);

test('M31 governed entity preview remains a typed transport contract', () => {
    for (const requiredContract of [
        'AthenaAuthoringEntityCreationEvidencePayload',
        'AthenaAuthoringNestedPortEvidencePayload',
        'AthenaAuthoringSourceEditEvidencePayload',
        'entityCreationEvidence?: AthenaAuthoringEntityCreationEvidencePayload',
        'acceptanceEligible: boolean',
        'diagnostics: AthenaAuthoringDiagnosticPayload[]',
        'sourceImpact?: AthenaAuthoringSourceEditPayload',
        'AthenaAuthoringTransactionResultPayload',
        'transactionResult?: AthenaAuthoringTransactionResultPayload',
        "decision: 'accept' | 'accepted' | 'reject' | 'rejected' | 'cancel' | 'cancelled'",
    ]) {
        assert.match(protocolSource, new RegExp(requiredContract.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')));
    }
});

test('M31 frontend protocol contains no source planner or representation inference', () => {
    assert.doesNotMatch(protocolSource, /BackendAuthoringSourceEditPlanner|RepresentationBindingCompiler|SchematicCompositionIntentCompiler/);
});

test('Graphical View hosts create-entity transaction UX as an adapter only', () => {
    for (const requiredSource of [
        'buildCreateEntityPreviewRequest',
        'collectAuthoringDecisionDiagnostics',
        'createEntityPreview',
        'createEntityDraft',
        'previewCreateEntityTransaction',
        'acceptCreateEntityPreview',
        'rejectCreateEntityPreview',
        'cancelCreateEntityPreview',
        'clearCreateEntityPreview',
        'currentEditorMatchesCreateEntityPreview',
        'isAuthoringDecisionCommitted',
        'sourceEditMatchesPreviewEvidence',
        'renderCreateEntityControls',
        'renderCreateEntityPreview',
        'entityCreationEvidence',
        'nestedPorts',
        'sourceEdit',
        'representationId',
        'compositionTargetId',
        'projectionOccurrenceIds',
    ]) {
        assert.match(graphWorkbenchSource, new RegExp(requiredSource.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')));
    }

    assert.match(graphWorkbenchSource, /originSurface:\s*'graph'/);
    assert.match(graphWorkbenchSource, /applyAuthoringSourceEdit\(decision\.sourceEdit\)/);
    assert.match(graphWorkbenchSource, /decision:\s*'cancelled'/);
    assert.match(graphWorkbenchSource, /currentEditorMatchesCreateEntityPreview\(preview\)/);
    assert.match(graphWorkbenchSource, /isAuthoringDecisionCommitted\(decision\)/);
    assert.match(graphWorkbenchSource, /sourceEditMatchesPreviewEvidence\(decision\.sourceEdit,\s*preview\.entityCreationEvidence\.sourceEdit\)/);
    assert.match(graphWorkbenchSource, /collectAuthoringDecisionDiagnostics\(decision\)/);
    assert.doesNotMatch(
        graphWorkbenchSource,
        /BackendAuthoringSourceEditPlanner|serializeAthena|lastIndexOf\('}'\)|document\.querySelector|querySelector|canvasCoordinates|textContent|innerText|visibleLabel|svgId/i,
    );
});

test('Graphical View clears stale create previews on editor and diagram refresh boundaries', () => {
    assert.match(graphWorkbenchSource, /this\.editorManager\.onCurrentEditorChanged\(widget => \{\s*this\.clearCreateEntityPreview\(/s);
    assert.match(graphWorkbenchSource, /onDocumentContentChanged\(\(\) => \{\s*this\.clearCreateEntityPreview\(/s);
    assert.match(graphWorkbenchSource, /this\.clearCreateEntityPreview\('Diagram refreshed after create-entity preview/s);
});

test('Graphical View renders structured diagnostics and backend source impact before admitted text', () => {
    assert.match(graphWorkbenchSource, /diagnostic\.authority/);
    assert.match(graphWorkbenchSource, /diagnostic\.lifecycleStage/);
    assert.match(graphWorkbenchSource, /preview\.sourceImpact\?\.newText/);
    assert.match(graphWorkbenchSource, /sourceEditPreviewText/);
});

test('Graphical View reveal path does not assume component namespace for created entities', () => {
    assert.doesNotMatch(graphWorkbenchSource, /startsWith\('component:'\)/);
    assert.match(graphWorkbenchSource, /resolveCreatedEntitySemanticId/);
    assert.match(graphWorkbenchSource, /entityCreationEvidence\?\.affectedSemanticIds\[0\]/);
});
