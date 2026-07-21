import assert from 'node:assert/strict';
import test from 'node:test';

const interactionAdapter = await import('../lib/browser/athena-interaction-adapter-model.js');

test('builds an Interaction IR selection payload only from governed semantic selection', () => {
    const payload = interactionAdapter.toInteractionSelectionPayload({
        semanticId: 'component:PLC1',
        kind: 'component',
        sourceUri: 'file:///workspace/main.athena'
    }, {
        occurrenceId: 'documentation/projection/node/component_PLC1',
        projectionViewId: 'schematic',
        sheetId: 'documentation/sheet/01-main',
        adapterMetadata: {
            svgNodeId: 'node-PLC1'
        }
    });

    assert.deepEqual(payload, {
        subjectKey: {
            canonicalSubjectId: 'component:PLC1',
            subjectKind: 'component',
            sourceContextId: 'file:///workspace/main.athena'
        },
        occurrence: {
            canonicalSubjectId: 'component:PLC1',
            subjectKind: 'component',
            sourceContextId: 'file:///workspace/main.athena',
            occurrenceId: 'documentation/projection/node/component_PLC1',
            projectionViewId: 'schematic',
            sheetId: 'documentation/sheet/01-main'
        },
        adapterMetadata: {
            svgNodeId: 'node-PLC1'
        }
    });
});

test('does not infer Interaction IR selection from DOM text or SVG geometry', () => {
    assert.deepEqual(
        interactionAdapter.toInteractionSelectionPayload(undefined, {
            adapterMetadata: {
                text: 'component:PLC1',
                svgX: '100'
            }
        }),
        {
            subjectKey: undefined,
            occurrence: undefined,
            adapterMetadata: {
                text: 'component:PLC1',
                svgX: '100'
            }
        }
    );
});
