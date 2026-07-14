import assert from 'node:assert/strict';
import test from 'node:test';

const {
    buildAthenaInspectorComponentSnapshot,
    buildAthenaInspectorDraftChanges,
    createAthenaInspectorEditDraft,
} = await import('../lib/browser/athena-inspector-model.js');

test('buildAthenaInspectorComponentSnapshot resolves the selected component through canonical semantic identity', async () => {
    const snapshot = buildAthenaInspectorComponentSnapshot({
        inspection: {
            uri: 'file:///workspace/factory-line.athena',
            version: 3,
            status: 'ready',
            systemName: 'FactoryLine',
            diagnosticsCount: 0,
            diagnosticSummaries: [],
            componentCount: 1,
            portCount: 1,
            connectionCount: 0,
            components: [{
                semanticId: 'component:PLC1',
                name: 'PLC1',
                kind: 'PlcCpu',
                properties: 'componentRef=electrical.plc.cpu',
                authoredProperties: [
                    { name: 'label', valueKind: 'string', valueText: 'Main PLC' },
                    { name: 'description', valueKind: 'string', valueText: 'Line controller' },
                    { name: 'vendorPartNumber', valueKind: 'string', valueText: 'proof.cpu.313c' },
                ],
                sourceRange: {
                    start: { line: 1, character: 2 },
                    end: { line: 6, character: 3 },
                },
            }],
            ports: [{
                semanticId: 'port:PLC1.Lplus',
                path: 'PLC1.Lplus',
                properties: 'direction=in',
                sourceRange: {
                    start: { line: 7, character: 2 },
                    end: { line: 10, character: 3 },
                },
            }],
            connections: [],
        },
        knowledge: {
            projectName: 'factory-line',
            systemSemanticId: 'system:FactoryLine',
            semanticPath: 'frontend -> LSP -> runtime/compiler',
            status: 'ready',
            contributingPluginIds: ['com.engineeringood.athena.domain.electrical'],
            activeConceptCount: 1,
            activeImplementationCount: 1,
            availableComponents: [],
            components: [{
                semanticSubjectId: 'component:PLC1',
                authoredComponentReference: 'electrical.plc.cpu',
                conceptId: 'electrical.plc.cpu',
                conceptDisplayName: 'PLC CPU',
                implementationId: 'impl/electrical/plc-cpu/siemens-proof-cpu313c',
                vendorId: 'siemens',
                vendorPartNumber: 'proof.cpu.313c',
            }],
            semanticPorts: [{
                portSemanticId: 'port:PLC1.Lplus',
                ownerSemanticId: 'component:PLC1',
                portTypeId: 'electrical.power.24v',
                roleId: 'Lplus',
                direction: 'in',
                signalFamilyId: 'electrical.power',
                protocolIds: [],
            }],
            physicalTraits: [{
                semanticSubjectId: 'component:PLC1',
                displayName: 'S7-300 rail footprint',
                widthMillimeters: 40,
                heightMillimeters: 125,
                depthMillimeters: 120,
                mountingTypeId: 'din-rail',
                installationMarkerIds: ['cabinet'],
            }],
            diagnostics: [],
        },
        selection: {
            semanticId: 'component:PLC1',
            label: 'PLC1',
            kind: 'component',
        },
    });

    assert.ok(snapshot);
    assert.equal(snapshot.semanticId, 'component:PLC1');
    assert.equal(snapshot.conceptDisplayName, 'PLC CPU');
    assert.equal(snapshot.vendorPartNumber, 'proof.cpu.313c');
    assert.equal(snapshot.label, 'Main PLC');
    assert.equal(snapshot.description, 'Line controller');
    assert.equal(snapshot.implementationOptions.length, 1);
    assert.equal(snapshot.implementationOptions[0].selected, true);
    assert.equal(snapshot.ports.length, 1);
    assert.equal(snapshot.ports[0].label, 'PLC1.Lplus');
    assert.equal(snapshot.physicalTraits[0].mountingTypeId, 'din-rail');
});

test('buildAthenaInspectorComponentSnapshot resolves a selected port back to its owning component', async () => {
    const snapshot = buildAthenaInspectorComponentSnapshot({
        inspection: {
            uri: 'file:///workspace/factory-line.athena',
            version: 4,
            status: 'ready',
            systemName: 'FactoryLine',
            diagnosticsCount: 0,
            diagnosticSummaries: [],
            componentCount: 1,
            portCount: 1,
            connectionCount: 0,
            components: [{
                semanticId: 'component:PLC1',
                name: 'PLC1',
                kind: 'PlcCpu',
                properties: '',
                authoredProperties: [],
                sourceRange: {
                    start: { line: 1, character: 2 },
                    end: { line: 6, character: 3 },
                },
            }],
            ports: [{
                semanticId: 'port:PLC1.MPI',
                path: 'PLC1.MPI',
                properties: '',
                sourceRange: {
                    start: { line: 7, character: 2 },
                    end: { line: 10, character: 3 },
                },
            }],
            connections: [],
        },
        knowledge: {
            projectName: 'factory-line',
            systemSemanticId: 'system:FactoryLine',
            semanticPath: 'frontend -> LSP -> runtime/compiler',
            status: 'ready',
            contributingPluginIds: [],
            activeConceptCount: 1,
            activeImplementationCount: 0,
            availableComponents: [],
            components: [{
                semanticSubjectId: 'component:PLC1',
                authoredComponentReference: 'electrical.plc.cpu',
                conceptId: 'electrical.plc.cpu',
                conceptDisplayName: 'PLC CPU',
            }],
            semanticPorts: [{
                portSemanticId: 'port:PLC1.MPI',
                ownerSemanticId: 'component:PLC1',
                portTypeId: 'electrical.comm.mpi',
                roleId: 'MPI',
                direction: 'in',
                signalFamilyId: 'electrical.comm',
                protocolIds: ['mpi'],
            }],
            physicalTraits: [],
            diagnostics: [],
        },
        selection: {
            semanticId: 'port:PLC1.MPI',
            label: 'PLC1.MPI',
            kind: 'port',
        },
    });

    assert.ok(snapshot);
    assert.equal(snapshot.semanticId, 'component:PLC1');
    assert.equal(snapshot.ports[0].selected, true);
});

test('buildAthenaInspectorDraftChanges keeps inspector edits focused on changed governed properties', async () => {
    const snapshot = buildAthenaInspectorComponentSnapshot({
        inspection: {
            uri: 'file:///workspace/factory-line.athena',
            version: 5,
            status: 'ready',
            systemName: 'FactoryLine',
            diagnosticsCount: 0,
            diagnosticSummaries: [],
            componentCount: 1,
            portCount: 0,
            connectionCount: 0,
            components: [{
                semanticId: 'component:PLC1',
                name: 'PLC1',
                kind: 'PlcCpu',
                properties: '',
                authoredProperties: [
                    { name: 'label', valueKind: 'string', valueText: 'Main PLC' },
                    { name: 'note', valueKind: 'string', valueText: 'Original note' },
                ],
                sourceRange: {
                    start: { line: 1, character: 2 },
                    end: { line: 6, character: 3 },
                },
            }],
            ports: [],
            connections: [],
        },
        knowledge: {
            projectName: 'factory-line',
            systemSemanticId: 'system:FactoryLine',
            semanticPath: 'frontend -> LSP -> runtime/compiler',
            status: 'ready',
            contributingPluginIds: [],
            activeConceptCount: 1,
            activeImplementationCount: 2,
            availableComponents: [{
                conceptId: 'electrical.plc.cpu',
                displayName: 'PLC CPU',
                classificationKeys: ['plc'],
                implementations: [
                    {
                        implementationId: 'impl/electrical/plc-cpu/siemens-proof-cpu313c',
                        vendorId: 'siemens',
                        vendorPartNumber: 'proof.cpu.313c',
                        displayName: 'CPU 313C',
                    },
                    {
                        implementationId: 'impl/electrical/plc-cpu/siemens-proof-cpu314c',
                        vendorId: 'siemens',
                        vendorPartNumber: 'proof.cpu.314c',
                        displayName: 'CPU 314C',
                    },
                ],
            }],
            components: [{
                semanticSubjectId: 'component:PLC1',
                authoredComponentReference: 'electrical.plc.cpu',
                conceptId: 'electrical.plc.cpu',
                conceptDisplayName: 'PLC CPU',
                implementationId: 'impl/electrical/plc-cpu/siemens-proof-cpu313c',
                vendorId: 'siemens',
                vendorPartNumber: 'proof.cpu.313c',
            }],
            semanticPorts: [],
            physicalTraits: [],
            diagnostics: [],
        },
        selection: {
            semanticId: 'component:PLC1',
            label: 'PLC1',
            kind: 'component',
        },
    });

    assert.ok(snapshot);
    const draft = createAthenaInspectorEditDraft(snapshot);
    draft.name = 'PLC2';
    draft.description = 'Updated line controller';
    draft.preferredImplementationId = 'impl/electrical/plc-cpu/siemens-proof-cpu314c';

    assert.deepEqual(
        buildAthenaInspectorDraftChanges({
            snapshot,
            draft,
        }),
        {
            name: 'PLC2',
            description: 'Updated line controller',
            preferredImplementationId: 'impl/electrical/plc-cpu/siemens-proof-cpu314c',
        },
    );
});
