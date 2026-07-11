import assert from 'node:assert/strict';
import test from 'node:test';

const {
    ADJUST_LAYOUT_PLACEMENT_INTENT_ID,
    CONNECT_PORTS_INTENT_ID,
    buildAdjustLayoutPlacementIntentRequest,
    buildConnectPortsIntentRequest,
    supportsConnectPortsIntent,
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

test('buildConnectPortsIntentRequest keeps the Athena semantic graph intent contract and current editor model', async () => {
    const model = {
        id: 'current-athena-model'
    };
    const request = buildConnectPortsIntentRequest({
        viewId: 'cabinet',
        sourceSemanticId: 'port:PLC1.out',
        targetSemanticId: 'port:M1.in',
        model
    });

    assert.equal(request.method, 'athena/graphCommandIntent');
    assert.deepEqual(request.params, {
        intentId: CONNECT_PORTS_INTENT_ID,
        viewId: 'cabinet',
        source: {
            semanticId: 'port:PLC1.out',
            subjectKind: 'port'
        },
        target: {
            semanticId: 'port:M1.in',
            subjectKind: 'port'
        }
    });
    assert.equal(request.model, model);
});

test('supportsConnectPortsIntent follows semantic command ownership instead of graph-local capability', async () => {
    assert.equal(supportsConnectPortsIntent({
        viewId: 'cabinet',
        ownershipContract: {
            interactivity: 'interactive',
            semanticCommandIds: ['connect-ports']
        }
    }), true);

    assert.equal(supportsConnectPortsIntent({
        viewId: 'wiring',
        ownershipContract: {
            interactivity: 'inspect_only',
            semanticCommandIds: ['connect-ports']
        }
    }), false);
});
