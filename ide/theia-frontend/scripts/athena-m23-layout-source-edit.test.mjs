import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import test from 'node:test';

const graphWorkbenchModel = await import('../lib/browser/athena-graph-workbench-model.js');

test('M23 graph workbench transports structured authored layout intent without serializing source', () => {
    const intent = graphWorkbenchModel.buildAthenaGraphAuthoredLayoutIntent({
        intentId: 'layout-adjustment:place:component:HMI1:occ:HMI1:snapshot',
        kind: 'place',
        subjectSemanticId: 'component:HMI1',
        occurrenceId: 'occ:HMI1',
        viewId: 'schematic-sheet',
        sheetId: 'sheet:main',
        snapshotId: 'snapshot',
        sourceUri: 'file:///workspace/src/01-layout-hints.athena',
        targetSemanticId: 'component:PLC1',
        relation: 'near',
        transientOnly: true,
        persisted: false,
    });

    assert.deepEqual(intent, {
        viewFamily: 'schematic-sheet',
        statements: [{
            subject: 'HMI1',
            relation: 'near',
            target: 'PLC1',
            priority: 'preference',
        }],
    });
    assert.equal(typeof graphWorkbenchModel.serializeAthenaGraphAuthoredLayoutIntent, 'undefined');
    assert.equal(typeof graphWorkbenchModel.buildAthenaGraphLayoutSourceEdit, 'undefined');
});

test('M23 layout preview displays and applies the backend-owned source edit', () => {
    const sourceEdit = {
        uri: 'file:///workspace/src/01-layout-hints.athena',
        range: {
            start: { line: 10, character: 0 },
            end: { line: 10, character: 0 },
        },
        newText: '\n\n  layout schematic-sheet {\n    place HMI1 near PLC1\n  }\n',
        suggestedSemanticId: 'component:HMI1',
    };
    const preview = graphWorkbenchModel.buildAthenaGraphLayoutMutationPreview({
        intentId: 'layout-adjustment:place:component:HMI1:occ:HMI1:snapshot',
        kind: 'place',
        subjectSemanticId: 'component:HMI1',
        occurrenceId: 'occ:HMI1',
        viewId: 'schematic-sheet',
        sheetId: 'sheet:main',
        snapshotId: 'snapshot',
        sourceUri: sourceEdit.uri,
        targetSemanticId: 'component:PLC1',
        relation: 'near',
        transientOnly: true,
        persisted: false,
    }, sourceEdit);

    assert.equal(preview.sourceEdit, sourceEdit);
    assert.match(preview.layoutBlockSnippet, /place HMI1 near PLC1/);
});

test('M23 system-scope insertion moved to the AST-aware backend planner', () => {
    const planner = readFileSync(
        resolve('..', '..', 'kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/BackendAuthoringSourceEditPlanner.kt'),
        'utf8',
    );
    assert.match(planner, /ast\.system\.span\.end\.offset - 1/);
    assert.doesNotMatch(planner, /lastIndexOf\(['"]}['"]\)/);
});
