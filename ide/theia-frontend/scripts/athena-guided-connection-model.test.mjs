import assert from 'node:assert/strict';
import test from 'node:test';

const {
    buildSemanticRelationshipPreviewRequest,
} = await import('../lib/browser/athena-authoring-protocol.js');
const {
    buildAthenaPortConnectionStateMap,
} = await import('../lib/browser/athena-guided-connection-model.js');

test('buildSemanticRelationshipPreviewRequest emits a graph-owned governed relationship preview request', async () => {
    const request = buildSemanticRelationshipPreviewRequest({
        sourceSubjectId: 'port:PLC1.out',
        targetSubjectId: 'port:M1.in',
        originDetail: 'graph:wiring',
        intentId: 'intent-connect-1',
    });

    assert.equal(request.intentId, 'intent-connect-1');
    assert.equal(request.intentKind, 'semantic-relationship');
    assert.equal(request.originSurface, 'graph');
    assert.equal(request.originDetail, 'graph:wiring');
    assert.equal(request.sourceSubjectId, 'port:PLC1.out');
    assert.equal(request.targetSubjectId, 'port:M1.in');
});

test('buildAthenaPortConnectionStateMap projects canonical connection state onto both ports', async () => {
    const states = buildAthenaPortConnectionStateMap({
        uri: 'file:///workspace/factory-line.athena',
        version: 2,
        status: 'ready',
        systemName: 'FactoryLine',
        diagnosticsCount: 0,
        diagnosticSummaries: [],
        componentCount: 2,
        portCount: 2,
        connectionCount: 1,
        components: [],
        ports: [
            { semanticId: 'port:PLC1.out', path: 'PLC1.out', properties: '', sourceRange: { start: { line: 1, character: 0 }, end: { line: 1, character: 1 } } },
            { semanticId: 'port:M1.in', path: 'M1.in', properties: '', sourceRange: { start: { line: 2, character: 0 }, end: { line: 2, character: 1 } } },
        ],
        connections: [
            {
                semanticId: 'connection:PLC1.out->M1.in',
                fromPath: 'PLC1.out',
                toPath: 'M1.in',
                sourceRange: { start: { line: 3, character: 0 }, end: { line: 3, character: 1 } },
            },
        ],
    });

    assert.deepEqual(states.get('port:PLC1.out'), {
        connectionIds: ['connection:PLC1.out->M1.in'],
        connectedPaths: ['M1.in'],
        connectedPeerSemanticIds: ['port:M1.in'],
    });
    assert.deepEqual(states.get('port:M1.in'), {
        connectionIds: ['connection:PLC1.out->M1.in'],
        connectedPaths: ['PLC1.out'],
        connectedPeerSemanticIds: ['port:PLC1.out'],
    });
});
