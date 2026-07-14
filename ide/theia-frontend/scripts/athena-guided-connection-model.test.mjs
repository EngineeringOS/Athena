import assert from 'node:assert/strict';
import test from 'node:test';

const {
    buildConnectPortsPreviewRequest,
} = await import('../lib/browser/athena-authoring-protocol.js');
const {
    buildAthenaCompatibleConnectionTargets,
    buildAthenaPortConnectionStateMap,
} = await import('../lib/browser/athena-guided-connection-model.js');

test('buildConnectPortsPreviewRequest emits a graph-owned governed connect preview request', async () => {
    const request = buildConnectPortsPreviewRequest({
        sourcePortId: 'port:PLC1.out',
        targetPortId: 'port:M1.in',
        originDetail: 'graph:wiring',
        intentId: 'intent-connect-1',
    });

    assert.equal(request.intentId, 'intent-connect-1');
    assert.equal(request.intentKind, 'connect-ports');
    assert.equal(request.originSurface, 'graph');
    assert.equal(request.originDetail, 'graph:wiring');
    assert.equal(request.sourcePortId, 'port:PLC1.out');
    assert.equal(request.targetPortId, 'port:M1.in');
});

test('buildAthenaCompatibleConnectionTargets filters targets by signal family, direction, and protocol metadata', async () => {
    const targets = buildAthenaCompatibleConnectionTargets({
        sourcePortSemanticId: 'port:PLC1.mpi',
        inspection: {
            uri: 'file:///workspace/factory-line.athena',
            version: 1,
            status: 'ready',
            systemName: 'FactoryLine',
            diagnosticsCount: 0,
            diagnosticSummaries: [],
            componentCount: 2,
            portCount: 4,
            connectionCount: 0,
            components: [],
            ports: [
                { semanticId: 'port:PLC1.mpi', path: 'PLC1.mpi', properties: '', sourceRange: { start: { line: 1, character: 0 }, end: { line: 1, character: 1 } } },
                { semanticId: 'port:HMI1.mpi', path: 'HMI1.mpi', properties: '', sourceRange: { start: { line: 2, character: 0 }, end: { line: 2, character: 1 } } },
                { semanticId: 'port:M1.power', path: 'M1.power', properties: '', sourceRange: { start: { line: 3, character: 0 }, end: { line: 3, character: 1 } } },
                { semanticId: 'port:PLC1.out', path: 'PLC1.out', properties: '', sourceRange: { start: { line: 4, character: 0 }, end: { line: 4, character: 1 } } },
            ],
            connections: [],
        },
        knowledge: {
            projectName: 'factory-line',
            systemSemanticId: 'system:FactoryLine',
            semanticPath: 'frontend -> LSP -> runtime/compiler',
            status: 'ready',
            contributingPluginIds: [],
            activeConceptCount: 2,
            activeImplementationCount: 1,
            availableComponents: [],
            components: [],
            semanticPorts: [
                {
                    portSemanticId: 'port:PLC1.mpi',
                    ownerSemanticId: 'component:PLC1',
                    portTypeId: 'electrical.communication.mpi',
                    roleId: 'mpi',
                    direction: 'bidirectional',
                    signalFamilyId: 'electrical.communication',
                    protocolIds: ['mpi'],
                },
                {
                    portSemanticId: 'port:HMI1.mpi',
                    ownerSemanticId: 'component:HMI1',
                    portTypeId: 'electrical.communication.mpi',
                    roleId: 'mpi',
                    direction: 'bidirectional',
                    signalFamilyId: 'electrical.communication',
                    protocolIds: ['mpi'],
                },
                {
                    portSemanticId: 'port:M1.power',
                    ownerSemanticId: 'component:M1',
                    portTypeId: 'electrical.power.dc24-output',
                    roleId: 'power',
                    direction: 'output',
                    signalFamilyId: 'electrical.power',
                    protocolIds: [],
                },
                {
                    portSemanticId: 'port:PLC1.out',
                    ownerSemanticId: 'component:PLC1',
                    portTypeId: 'electrical.digital.output',
                    roleId: 'out',
                    direction: 'output',
                    signalFamilyId: 'electrical.digital',
                    protocolIds: [],
                },
            ],
            physicalTraits: [],
            diagnostics: [],
        },
    });

    assert.equal(targets.length, 1);
    assert.equal(targets[0].semanticId, 'port:HMI1.mpi');
    assert.equal(targets[0].label, 'HMI1.mpi');
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
