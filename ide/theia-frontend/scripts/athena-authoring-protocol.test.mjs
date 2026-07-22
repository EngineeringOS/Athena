import assert from 'node:assert/strict';
import test from 'node:test';

const {
    buildAuthoringDecisionRequest,
    buildCreateEntityPreviewRequest,
    buildSemanticRelationshipPreviewRequest,
    buildUpdateEntityPropertiesPreviewRequest,
    collectAuthoringDecisionDiagnostics,
    isAuthoringDecisionCommitted,
    sourceEditMatchesPreviewEvidence,
} = await import('../lib/browser/athena-authoring-protocol.js');

test('buildCreateEntityPreviewRequest keeps the governed create-entity request shape stable', async () => {
    const request = buildCreateEntityPreviewRequest({
        systemSemanticId: 'system:FactoryLine',
        conceptTemplateId: 'electrical.plc.cpu.default',
        conceptId: 'electrical.plc.cpu',
        actor: 'user:test',
        preferredImplementationId: 'impl/electrical/plc-cpu/siemens-proof-cpu313c',
        suggestedName: 'PlcCpu2',
        originDetail: 'component-panel:electrical.plc.cpu',
        intentId: 'intent-1234',
    });

    assert.deepEqual(request, {
        intentId: 'intent-1234',
        intentKind: 'create-entity',
        originSurface: 'palette',
        originDetail: 'component-panel:electrical.plc.cpu',
        parentSubjectId: 'system:FactoryLine',
        conceptTemplateId: 'electrical.plc.cpu.default',
        conceptId: 'electrical.plc.cpu',
        actor: 'user:test',
        preferredImplementationId: 'impl/electrical/plc-cpu/siemens-proof-cpu313c',
        suggestedName: 'PlcCpu2',
    });
});

test('buildCreateEntityPreviewRequest carries graph-origin tag and model values without source planning', async () => {
    const request = buildCreateEntityPreviewRequest({
        systemSemanticId: 'system:FactoryLine',
        conceptTemplateId: 'electrical.motor.shutter.default',
        conceptId: 'electrical.motor.shutter',
        actor: 'user:test',
        suggestedName: 'ShutterMotorM31',
        model: 'SHUTTER-230V',
        originSurface: 'graph',
        originDetail: 'graph:create-entity',
        intentId: 'intent-graph-create-1',
    });

    assert.deepEqual(request, {
        intentId: 'intent-graph-create-1',
        intentKind: 'create-entity',
        originSurface: 'graph',
        originDetail: 'graph:create-entity',
        parentSubjectId: 'system:FactoryLine',
        conceptTemplateId: 'electrical.motor.shutter.default',
        conceptId: 'electrical.motor.shutter',
        actor: 'user:test',
        preferredImplementationId: undefined,
        suggestedName: 'ShutterMotorM31',
        properties: {
            model: {
                kind: 'text',
                text: 'SHUTTER-230V',
            },
        },
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

test('buildUpdateEntityPropertiesPreviewRequest keeps the governed inspector update request shape stable', async () => {
    const request = buildUpdateEntityPropertiesPreviewRequest({
        entitySubjectId: 'component:PLC1',
        actor: 'user:test',
        name: 'PLC2',
        label: 'Main PLC',
        description: 'Line controller',
        preferredImplementationId: 'impl/electrical/plc-cpu/siemens-proof-cpu314c',
        originDetail: 'semantic-inspection:component:PLC1',
        intentId: 'intent-5678',
    });

    assert.deepEqual(request, {
        intentId: 'intent-5678',
        intentKind: 'update-entity-properties',
        originSurface: 'inspector',
        originDetail: 'semantic-inspection:component:PLC1',
        entitySubjectId: 'component:PLC1',
        actor: 'user:test',
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

test('authoring decision helpers accept reprojected commits and block unsafe accept responses', async () => {
    assert.equal(isAuthoringDecisionCommitted({
        status: 'updated',
        transactionResult: {
            lifecycleState: 'reprojected',
            affectedSemanticIds: [],
            projectionOccurrenceIds: [],
            diagnostics: [],
        },
    }), true);

    assert.equal(isAuthoringDecisionCommitted({
        status: 'updated',
        transactionResult: {
            lifecycleState: 'committed',
            affectedSemanticIds: [],
            projectionOccurrenceIds: [],
            diagnostics: [],
        },
    }), true);

    assert.equal(isAuthoringDecisionCommitted({
        status: 'updated',
        transactionResult: {
            lifecycleState: 'blocked',
            affectedSemanticIds: [],
            projectionOccurrenceIds: [],
            diagnostics: [],
        },
    }), false);

    assert.equal(isAuthoringDecisionCommitted({
        status: 'updated',
        transactionResult: {
            lifecycleState: 'reprojected',
            affectedSemanticIds: [],
            projectionOccurrenceIds: [],
            diagnostics: [{ code: 'STALE', message: 'stale source', authority: 'transaction-runtime', lifecycleStage: 'blocked' }],
        },
    }), false);

    assert.equal(isAuthoringDecisionCommitted({
        status: 'updated',
        transactionResult: {
            lifecycleState: 'projection-failed',
            affectedSemanticIds: ['component:OperatorHMI1'],
            projectionOccurrenceIds: [],
            diagnostics: [{
                code: 'authoring.projection.failed-after-commit',
                message: 'Mutation committed, but projection refresh failed.',
                authority: 'projection-runtime',
                lifecycleStage: 'projection-failed',
                recoveryAction: 'refresh-projection',
            }],
        },
    }), true);

    assert.equal(collectAuthoringDecisionDiagnostics({
        reason: 'stale',
        transactionResult: {
            lifecycleState: 'stale',
            affectedSemanticIds: [],
            projectionOccurrenceIds: [],
            diagnostics: [{ code: 'STALE', message: 'stale source', authority: 'transaction-runtime', lifecycleStage: 'blocked' }],
        },
    }), 'STALE [transaction-runtime/blocked]: stale source; stale');
});

test('sourceEditMatchesPreviewEvidence requires uri, affected ids, and revision guard equality', async () => {
    const previewEvidence = {
        uri: 'file:///workspace/main.athena',
        startOffset: 10,
        endOffset: 10,
        admittedText: 'device MotorM31 {}',
        selectionStartOffset: null,
        selectionEndOffset: null,
        affectedSemanticIds: ['entity:MotorM31', 'port:MotorM31.in'],
        revisionGuard: {
            semanticSnapshotId: 'snapshot-1',
            sourceUri: 'file:///workspace/main.athena',
            documentVersion: 7,
            contentSha256: 'abc123',
        },
    };

    assert.equal(sourceEditMatchesPreviewEvidence({
        uri: 'file:///workspace/main.athena',
        startOffset: 10,
        endOffset: 10,
        range: {
            start: { line: 1, character: 1 },
            end: { line: 1, character: 1 },
        },
        newText: 'device MotorM31 {}',
        suggestedSemanticId: 'entity:MotorM31',
        revisionGuard: {
            semanticSnapshotId: 'snapshot-1',
            sourceUri: 'file:///workspace/main.athena',
            documentVersion: 7,
            contentSha256: 'abc123',
        },
    }, previewEvidence), true);

    assert.equal(sourceEditMatchesPreviewEvidence({
        uri: 'file:///workspace/other.athena',
        startOffset: 10,
        endOffset: 10,
        range: {
            start: { line: 1, character: 1 },
            end: { line: 1, character: 1 },
        },
        newText: 'device MotorM31 {}',
        suggestedSemanticId: 'entity:MotorM31',
        revisionGuard: {
            semanticSnapshotId: 'snapshot-1',
            sourceUri: 'file:///workspace/main.athena',
            documentVersion: 7,
            contentSha256: 'abc123',
        },
    }, previewEvidence), false);

    assert.equal(sourceEditMatchesPreviewEvidence({
        uri: 'file:///workspace/main.athena',
        startOffset: 99,
        endOffset: 99,
        range: {
            start: { line: 9, character: 9 },
            end: { line: 9, character: 9 },
        },
        newText: 'device MotorM31 {}',
        suggestedSemanticId: 'entity:MotorM31',
        revisionGuard: {
            semanticSnapshotId: 'snapshot-1',
            sourceUri: 'file:///workspace/main.athena',
            documentVersion: 7,
            contentSha256: 'abc123',
        },
    }, previewEvidence), false);
});
