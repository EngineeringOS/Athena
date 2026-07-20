import assert from 'node:assert/strict';
import test from 'node:test';

const {
    buildAuthoringDecisionRequest,
    buildCreateComponentPreviewRequest,
    buildSemanticRelationshipPreviewRequest,
    buildUpdateComponentPropertiesPreviewRequest,
} = await import('../lib/browser/athena-authoring-protocol.js');

test('buildCreateComponentPreviewRequest keeps the governed create-component request shape stable', async () => {
    const request = buildCreateComponentPreviewRequest({
        systemSemanticId: 'system:FactoryLine',
        conceptId: 'electrical.plc.cpu',
        preferredImplementationId: 'impl/electrical/plc-cpu/siemens-proof-cpu313c',
        suggestedName: 'PlcCpu2',
        originDetail: 'component-panel:electrical.plc.cpu',
        intentId: 'intent-1234',
    });

    assert.deepEqual(request, {
        intentId: 'intent-1234',
        intentKind: 'create-component',
        originSurface: 'palette',
        originDetail: 'component-panel:electrical.plc.cpu',
        parentIdentity: 'system:FactoryLine',
        conceptId: 'electrical.plc.cpu',
        preferredImplementationId: 'impl/electrical/plc-cpu/siemens-proof-cpu313c',
        suggestedName: 'PlcCpu2',
    });
});

test('buildAuthoringDecisionRequest keeps accept and reject decision payloads transport-safe', async () => {
    assert.deepEqual(
        buildAuthoringDecisionRequest({
            previewId: 'authoring-preview-0001',
            intentId: 'intent-0001',
            decision: 'accepted',
            note: 'Apply guided insertion.',
        }),
        {
            previewId: 'authoring-preview-0001',
            intentId: 'intent-0001',
            decision: 'accepted',
            note: 'Apply guided insertion.',
        },
    );
});

test('buildSemanticRelationshipPreviewRequest emits generic relationship authoring payload', async () => {
    assert.deepEqual(
        buildSemanticRelationshipPreviewRequest({
            sourceSubjectId: 'port:PLC1.out',
            targetSubjectId: 'port:M1.in',
            projectionViewId: 'schematic',
            persistenceSourceUri: 'file:///workspace/main.athena',
            originDetail: 'graph:schematic',
            intentId: 'intent-relationship-1',
        }),
        {
            intentId: 'intent-relationship-1',
            intentKind: 'semantic-relationship',
            originSurface: 'graph',
            originDetail: 'graph:schematic',
            relationshipType: 'ElectricalConnectionRelationship',
            sourceSubjectId: 'port:PLC1.out',
            targetSubjectId: 'port:M1.in',
            projectionViewId: 'schematic',
            projectionOccurrenceId: undefined,
            persistenceSourceUri: 'file:///workspace/main.athena',
            provenance: undefined,
        },
    );
});

test('buildUpdateComponentPropertiesPreviewRequest keeps the governed inspector update request shape stable', async () => {
    const request = buildUpdateComponentPropertiesPreviewRequest({
        componentId: 'component:PLC1',
        name: 'PLC2',
        label: 'Main PLC',
        description: 'Line controller',
        preferredImplementationId: 'impl/electrical/plc-cpu/siemens-proof-cpu314c',
        originDetail: 'semantic-inspection:component:PLC1',
        intentId: 'intent-5678',
    });

    assert.deepEqual(request, {
        intentId: 'intent-5678',
        intentKind: 'update-component-properties',
        originSurface: 'inspector',
        originDetail: 'semantic-inspection:component:PLC1',
        componentId: 'component:PLC1',
        properties: {
            name: {
                kind: 'symbol',
                text: 'PLC2',
            },
            label: {
                kind: 'text',
                text: 'Main PLC',
            },
            description: {
                kind: 'text',
                text: 'Line controller',
            },
            preferredImplementationId: {
                kind: 'symbol',
                text: 'impl/electrical/plc-cpu/siemens-proof-cpu314c',
            },
        },
    });
});
