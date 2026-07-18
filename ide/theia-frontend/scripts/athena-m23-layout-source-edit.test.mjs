import assert from 'node:assert/strict';
import test from 'node:test';

const graphWorkbenchModel = await import('../lib/browser/athena-graph-workbench-model.js');

test('M23 graph workbench serializes authored layout intent into accepted source syntax', () => {
    assert.equal(typeof graphWorkbenchModel.serializeAthenaGraphAuthoredLayoutIntent, 'function');

    const source = graphWorkbenchModel.serializeAthenaGraphAuthoredLayoutIntent({
        viewFamily: 'schematic-sheet',
        statements: [
            {
                subject: 'component:HMI1',
                relation: 'near',
                target: 'component:PLC1',
                priority: 'preference',
            },
            {
                subject: 'component:XT1',
                relation: 'below',
                target: 'component:PLC1',
                priority: 'preference',
            },
            {
                subject: 'component:HMI1',
                relation: 'aligned-with',
                target: 'component:PLC1',
                axis: 'vertical',
                priority: 'preference',
            },
            {
                subject: 'component:HMI1',
                relation: 'grouped-with',
                target: 'component:PLC1',
                priority: 'preference',
            },
        ],
    });

    assert.equal(source, [
        'layout schematic-sheet {',
        '  place HMI1 near PLC1',
        '  place XT1 below PLC1',
        '  align HMI1 aligned-with PLC1 axis vertical',
        '  group HMI1 grouped-with PLC1',
        '}',
    ].join('\n'));
    assert.doesNotMatch(source, /component:/);
});

test('M23 graph workbench source edit uses serialized intent rather than preview text', () => {
    const preview = graphWorkbenchModel.buildAthenaGraphLayoutMutationPreview({
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

    const sourceEdit = graphWorkbenchModel.buildAthenaGraphLayoutSourceEdit({
        preview: {
            ...preview,
            layoutBlockSnippet: 'layout schematic-sheet {\n  place component:HMI1 near component:PLC1\n}',
        },
        insertionLine: 10,
        insertionCharacter: 1,
    });

    assert.match(sourceEdit.newText, /place HMI1 near PLC1/);
    assert.doesNotMatch(sourceEdit.newText, /component:HMI1|component:PLC1/);
    assert.equal(sourceEdit.suggestedSemanticId, 'component:HMI1');
});

test('M23 graph workbench source edit inserts layout block inside the active system scope', () => {
    const source = [
        'package com.engineeringood.factoryline',
        '',
        'system FactoryLine {',
        '  device PLC1 {',
        '    type Switch',
        '  }',
        '',
        '  device HMI1 {',
        '    type Switch',
        '  }',
        '}',
    ].join('\n');
    const preview = graphWorkbenchModel.buildAthenaGraphLayoutMutationPreview({
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

    const sourceEdit = graphWorkbenchModel.buildAthenaGraphLayoutSourceEdit({
        preview,
        documentText: source,
        insertionLine: source.split('\n').length - 1,
        insertionCharacter: 1,
    });
    const updated = applySourceEdit(source, sourceEdit);

    assert.match(updated, /system FactoryLine \{[\s\S]*layout schematic-sheet \{[\s\S]*place HMI1 near PLC1[\s\S]*\}\s*\}/);
    assert.ok(updated.indexOf('layout schematic-sheet {') < updated.lastIndexOf('}'));
});

function applySourceEdit(source, edit) {
    const lines = source.split('\n');
    const startOffset = offsetAt(lines, edit.range.start.line, edit.range.start.character);
    const endOffset = offsetAt(lines, edit.range.end.line, edit.range.end.character);
    return `${source.slice(0, startOffset)}${edit.newText}${source.slice(endOffset)}`;
}

function offsetAt(lines, line, character) {
    const normalizedLine = Math.max(0, Math.min(line, lines.length - 1));
    const prefix = lines.slice(0, normalizedLine).join('\n');
    return (prefix.length === 0 ? 0 : prefix.length + 1) + Math.max(0, character);
}
