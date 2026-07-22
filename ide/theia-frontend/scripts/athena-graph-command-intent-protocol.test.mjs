import assert from 'node:assert/strict';
import test from 'node:test';

const {
    ADJUST_LAYOUT_PLACEMENT_INTENT_ID,
    CREATE_SEMANTIC_RELATIONSHIP_INTENT_ID,
    buildAdjustLayoutPlacementIntentRequest,
    supportsCreateSemanticRelationshipIntent,
    supportsAdjustLayoutPlacementIntent
} = await import('../lib/browser/athena-graph-command-intent-protocol.js');

test('buildAdjustLayoutPlacementIntentRequest keeps the Athena graph intent contract and current editor model', async () => {
    const model = {
        id: 'current-athena-model'
    };
    const request = buildAdjustLayoutPlacementIntentRequest({
        viewId: 'cabinet',
        semanticId: 'component:PLC1',
        subjectKind: 'component',
        x: 180,
        y: 120,
        model
    });

    assert.equal(request.method, 'athena/graphCommandIntent');
    assert.deepEqual(request.params, {
        intentId: ADJUST_LAYOUT_PLACEMENT_INTENT_ID,
        viewId: 'cabinet',
        target: {
            semanticId: 'component:PLC1',
            subjectKind: 'component'
        },
        requestedPlacement: {
            x: 180,
            y: 120
        }
    });
    assert.equal(request.model, model);
});

test('supportsAdjustLayoutPlacementIntent follows the projection ownership contract instead of graph-local capability', async () => {
    assert.equal(supportsAdjustLayoutPlacementIntent({
        viewId: 'cabinet',
        ownershipContract: {
            interactivity: 'interactive',
            projectionCommandIds: ['adjust-layout-placement']
        }
    }), true);

    assert.equal(supportsAdjustLayoutPlacementIntent({
        viewId: 'wiring',
        ownershipContract: {
            interactivity: 'inspect_only',
            projectionCommandIds: ['adjust-layout-placement']
        }
    }), false);
});

test('supportsCreateSemanticRelationshipIntent follows semantic command ownership instead of graph-local capability', async () => {
    assert.equal(supportsCreateSemanticRelationshipIntent({
        viewId: 'cabinet',
        ownershipContract: {
            interactivity: 'interactive',
            semanticCommandIds: ['create-semantic-relationship']
        }
    }), true);

    assert.equal(supportsCreateSemanticRelationshipIntent({
        viewId: 'wiring',
        ownershipContract: {
            interactivity: 'inspect_only',
            semanticCommandIds: ['create-semantic-relationship']
        }
    }), false);
});
