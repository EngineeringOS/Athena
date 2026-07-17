import assert from 'node:assert/strict';
import test from 'node:test';

const semanticSelectionModel = await import('../lib/browser/athena-semantic-selection-model.js');
const { default: m20ReadyDiagram } = await import('../../../examples/m20/schematic-sheet-proof/ready-sheet.diagram.mjs');

const inspection = {
    uri: 'file:///workspace/demo.athena',
    version: 2,
    status: 'ready',
    systemName: 'DemoCabinet',
    diagnosticsCount: 0,
    diagnosticSummaries: [],
    componentCount: 1,
    portCount: 1,
    connectionCount: 1,
    components: [
        {
            semanticId: 'component:PLC1',
            name: 'PLC1',
            kind: 'Switch',
            properties: 'type=Switch',
            sourceRange: {
                start: {
                    line: 1,
                    character: 2
                },
                end: {
                    line: 4,
                    character: 3
                }
            }
        }
    ],
    ports: [
        {
            semanticId: 'port:PLC1.out',
            path: 'PLC1.out',
            properties: 'direction=out',
            sourceRange: {
                start: {
                    line: 6,
                    character: 2
                },
                end: {
                    line: 9,
                    character: 3
                }
            }
        }
    ],
    connections: [
        {
            semanticId: 'connection:PLC1.out->M1.in',
            fromPath: 'PLC1.out',
            toPath: 'M1.in',
            sourceRange: {
                start: {
                    line: 11,
                    character: 2
                },
                end: {
                    line: 11,
                    character: 28
                }
            }
        }
    ]
};

test('resolves canonical semantic selection from typed inspection payload', () => {
    assert.equal(typeof semanticSelectionModel.resolveSemanticSelectionFromInspection, 'function');

    const selection = semanticSelectionModel.resolveSemanticSelectionFromInspection(
        inspection,
        'port:PLC1.out'
    );

    assert.deepEqual(selection, {
        semanticId: 'port:PLC1.out',
        label: 'PLC1.out',
        kind: 'port',
        sourceUri: 'file:///workspace/demo.athena',
        sourceRange: inspection.ports[0].sourceRange
    });
});

test('resolves rendered sheet subjects to canonical selection targets without DOM inference', () => {
    assert.equal(typeof semanticSelectionModel.resolveRenderedSelectionTarget, 'function');

    assert.deepEqual(
        semanticSelectionModel.resolveRenderedSelectionTarget({
            id: 'schematic/projection/node/component_PSU1',
            type: 'node',
            kind: 'component',
            semanticId: 'component:PSU1'
        }),
        {
            semanticId: 'component:PSU1',
            occurrenceId: 'schematic/projection/node/component_PSU1',
            subjectKind: 'component',
            source: 'node'
        }
    );
    assert.deepEqual(
        semanticSelectionModel.resolveRenderedSelectionTarget({
            id: 'schematic/projection/connection/connection_PSU1_plus_PLC1_power',
            type: 'edge',
            semanticId: 'connection:PSU1.plus->PLC1.power'
        }),
        {
            semanticId: 'connection:PSU1.plus->PLC1.power',
            occurrenceId: 'schematic/projection/connection/connection_PSU1_plus_PLC1_power',
            subjectKind: 'connection',
            source: 'edge'
        }
    );
    assert.deepEqual(
        semanticSelectionModel.resolveRenderedSelectionTarget({
            endpointId: 'schematic/projection/connection/connection_PSU1_plus_PLC1_power/endpoint/source',
            anchorId: 'schematic/projection/label/port_PSU1_plus/anchor',
            portSemanticId: 'port:PSU1.plus',
            role: 'source'
        }),
        {
            semanticId: 'port:PSU1.plus',
            endpointId: 'schematic/projection/connection/connection_PSU1_plus_PLC1_power/endpoint/source',
            anchorId: 'schematic/projection/label/port_PSU1_plus/anchor',
            subjectKind: 'port',
            source: 'terminal'
        }
    );
    assert.equal(
        semanticSelectionModel.resolveRenderedSelectionTarget({
            id: 'dom-node-without-governed-semantic'
        }),
        undefined
    );
});

test('resolves the most specific semantic selection from a source-editor range', () => {
    assert.equal(typeof semanticSelectionModel.resolveSemanticSelectionFromSourceRange, 'function');

    const selection = semanticSelectionModel.resolveSemanticSelectionFromSourceRange(
        inspection,
        inspection.uri,
        {
            start: { line: 11, character: 8 },
            end: { line: 11, character: 18 }
        }
    );

    assert.deepEqual(selection, {
        semanticId: 'connection:PLC1.out->M1.in',
        label: 'PLC1.out -> M1.in',
        kind: 'connection',
        sourceUri: inspection.uri,
        sourceRange: inspection.connections[0].sourceRange
    });
});

test('resolves source range reveal targets through governed sheet projection facts', () => {
    assert.equal(typeof semanticSelectionModel.resolveSemanticRevealTargetFromSourceRange, 'function');

    const diagram = {
        activeSheetId: 'schematic/sheet/01-main',
        sheets: [
            {
                sheetId: 'schematic/sheet/01-main',
                subjectSemanticIds: ['connection:PLC1.out->M1.in']
            }
        ],
        graph: {
            nodes: [],
            edges: [
                {
                    id: 'schematic/projection/connection/connection_PLC1_out_M1_in',
                    semanticId: 'connection:PLC1.out->M1.in'
                }
            ]
        }
    };

    assert.deepEqual(
        semanticSelectionModel.resolveSemanticRevealTargetFromSourceRange(
            inspection,
            diagram,
            inspection.uri,
            {
                start: { line: 11, character: 8 },
                end: { line: 11, character: 18 }
            }
        ),
        {
            semanticId: 'connection:PLC1.out->M1.in',
            label: 'PLC1.out -> M1.in',
            kind: 'connection',
            sourceUri: inspection.uri,
            sourceRange: inspection.connections[0].sourceRange,
            revealSource: 'source',
            occurrenceIds: ['schematic/projection/connection/connection_PLC1_out_M1_in'],
            endpointIds: [],
            anchorIds: [],
            connectionIds: []
        }
    );
});

test('resolves Problem diagnostics through canonical ids or governed source ranges without parsing message text', () => {
    assert.equal(typeof semanticSelectionModel.resolveSemanticRevealTargetFromDiagnostic, 'function');

    const diagram = {
        electricalAnchors: [
            {
                anchorId: 'schematic/projection/label/port_PLC1_out/anchor',
                portSemanticId: 'port:PLC1.out',
                ownerSemanticId: 'component:PLC1',
                nodeId: 'schematic/projection/node/component_PLC1'
            }
        ],
        electricalConnectionEndpoints: [
            {
                endpointId: 'schematic/projection/connection/connection_PLC1_out_M1_in/endpoint/source',
                projectionConnectionId: 'schematic/projection/connection/connection_PLC1_out_M1_in',
                connectionSemanticId: 'connection:PLC1.out->M1.in',
                endpointRole: 'source',
                portSemanticId: 'port:PLC1.out',
                anchorId: 'schematic/projection/label/port_PLC1_out/anchor'
            }
        ],
        graph: {
            nodes: [
                {
                    id: 'schematic/projection/node/component_PLC1',
                    semanticId: 'component:PLC1'
                }
            ],
            edges: []
        }
    };

    assert.deepEqual(
        semanticSelectionModel.resolveSemanticRevealTargetFromDiagnostic(
            inspection,
            diagram,
            inspection.uri,
            {
                message: 'Breaker current is below required threshold',
                range: {
                    start: { line: 0, character: 0 },
                    end: { line: 0, character: 1 }
                },
                data: {
                    semanticId: 'port:PLC1.out'
                }
            }
        ),
        {
            semanticId: 'port:PLC1.out',
            label: 'PLC1.out',
            kind: 'port',
            sourceUri: inspection.uri,
            sourceRange: inspection.ports[0].sourceRange,
            revealSource: 'diagnostic',
            occurrenceIds: [],
            endpointIds: ['schematic/projection/connection/connection_PLC1_out_M1_in/endpoint/source'],
            anchorIds: ['schematic/projection/label/port_PLC1_out/anchor'],
            connectionIds: ['connection:PLC1.out->M1.in']
        }
    );

    assert.deepEqual(
        semanticSelectionModel.resolveSemanticRevealTargetFromDiagnostic(
            inspection,
            diagram,
            inspection.uri,
            {
                message: 'Range-owned problem',
                range: {
                    start: { line: 6, character: 4 },
                    end: { line: 6, character: 8 }
                }
            }
        )?.semanticId,
        'port:PLC1.out'
    );

    assert.equal(
        semanticSelectionModel.resolveSemanticRevealTargetFromDiagnostic(
            inspection,
            diagram,
            inspection.uri,
            {
                message: 'component:PLC1 appears only in diagnostic text',
                range: {
                    start: { line: 20, character: 0 },
                    end: { line: 20, character: 1 }
                }
            }
        ),
        undefined
    );
});

test('resolves source and Problem reveal through governed M20 presentation occurrences', () => {
    const m20Inspection = {
        uri: 'file:///workspace/m20-ready.athena',
        version: 1,
        status: 'ready',
        systemName: 'M20ReadySheet',
        diagnosticsCount: 1,
        diagnosticSummaries: [],
        componentCount: 1,
        portCount: 0,
        connectionCount: 1,
        components: [
            {
                semanticId: 'component:PSU1',
                name: 'PSU1',
                kind: 'PowerSupply',
                properties: '',
                sourceRange: {
                    start: { line: 2, character: 0 },
                    end: { line: 5, character: 1 }
                }
            }
        ],
        ports: [],
        connections: [
            {
                semanticId: 'connection:PSU1.plus->PLC1.power',
                fromPath: 'PSU1.plus',
                toPath: 'PLC1.power',
                sourceRange: {
                    start: { line: 7, character: 0 },
                    end: { line: 7, character: 31 }
                }
            }
        ]
    };
    const presentationOnlyDiagram = {
        ...m20ReadyDiagram,
        graph: {
            ...m20ReadyDiagram.graph,
            nodes: [],
            edges: []
        },
        presentation: {
            occurrences: [
                {
                    occurrenceId: 'schematic/presentation/occurrence/component_PSU1',
                    semanticId: 'component:PSU1'
                }
            ],
            connectors: [
                {
                    occurrenceId: 'schematic/presentation/connector/connection_PSU1_plus_PLC1_power',
                    semanticId: 'connection:PSU1.plus->PLC1.power'
                }
            ]
        }
    };

    const sourceReveal = semanticSelectionModel.resolveSemanticRevealTargetFromSourceRange(
        m20Inspection,
        presentationOnlyDiagram,
        m20Inspection.uri,
        {
            start: { line: 3, character: 1 },
            end: { line: 3, character: 4 }
        }
    );
    const diagnosticReveal = semanticSelectionModel.resolveSemanticRevealTargetFromDiagnostic(
        m20Inspection,
        presentationOnlyDiagram,
        m20Inspection.uri,
        {
            range: m20Inspection.connections[0].sourceRange,
            data: {
                semanticId: 'connection:PSU1.plus->PLC1.power'
            }
        }
    );

    assert.deepEqual(sourceReveal?.occurrenceIds, [
        'schematic/presentation/occurrence/component_PSU1'
    ]);
    assert.deepEqual(diagnosticReveal?.occurrenceIds, [
        'schematic/presentation/connector/connection_PSU1_plus_PLC1_power'
    ]);
    assert.equal(sourceReveal?.semanticId, 'component:PSU1');
    assert.equal(diagnosticReveal?.semanticId, 'connection:PSU1.plus->PLC1.power');
});

test('chooses governed reveal views deterministically and stops after supported views are exhausted', () => {
    assert.equal(typeof semanticSelectionModel.nextRevealViewId, 'function');

    const diagram = {
        activeViewId: 'overview',
        supportedViews: [
            { viewId: 'overview' },
            { viewId: 'schematic-sheet' },
            { viewId: 'diagnostics-sheet' }
        ]
    };

    assert.equal(
        semanticSelectionModel.nextRevealViewId(diagram),
        'schematic-sheet'
    );
    assert.equal(
        semanticSelectionModel.nextRevealViewId(diagram, ['overview', 'schematic-sheet']),
        'diagnostics-sheet'
    );
    assert.equal(
        semanticSelectionModel.nextRevealViewId(diagram, ['overview', 'schematic-sheet', 'diagnostics-sheet']),
        undefined
    );
});

test('matches semantic scm context through subject identity and fact-reference vocabulary', () => {
    assert.equal(typeof semanticSelectionModel.matchesSemanticScmContext, 'function');

    const selectedSemanticId = 'connection:PLC1.out->M1.in';
    const reviewEntry = {
        subjectIdentity: 'component:PLC1',
        factReferences: [
            {
                kind: 'semantic-change',
                identifier: 'review-entry',
                subjectIdentity: selectedSemanticId
            }
        ]
    };

    const commitEntry = {
        subjectIdentity: selectedSemanticId,
        factReferences: []
    };

    assert.equal(
        semanticSelectionModel.matchesSemanticScmContext(reviewEntry, selectedSemanticId),
        true
    );
    assert.equal(
        semanticSelectionModel.matchesSemanticScmContext(commitEntry, selectedSemanticId),
        true
    );
    assert.equal(
        semanticSelectionModel.matchesSemanticScmContext(reviewEntry, 'component:M1'),
        false
    );
});

test('selectable semantic scm context prefers subject identity and falls back to fact references', () => {
    assert.equal(typeof semanticSelectionModel.selectableSemanticIdFromScmContext, 'function');

    const fromSubjectIdentity = semanticSelectionModel.selectableSemanticIdFromScmContext({
        subjectIdentity: 'component:PLC1',
        factReferences: []
    });
    const fromFactReference = semanticSelectionModel.selectableSemanticIdFromScmContext({
        factReferences: [
            {
                kind: 'authored-change',
                identifier: 'change-1',
                subjectIdentity: 'port:PLC1.out'
            }
        ]
    });

    assert.equal(fromSubjectIdentity, 'component:PLC1');
    assert.equal(fromFactReference, 'port:PLC1.out');
});

test('drops transient selection when refreshed projection no longer contains the semantic id', () => {
    assert.equal(typeof semanticSelectionModel.retainSelectionIfPresent, 'function');

    const selected = {
        semanticId: 'port:PLC1.out',
        label: 'PLC1.out',
        kind: 'port'
    };
    const visibleDiagram = {
        graph: {
            nodes: [{ id: 'port:PLC1.out' }],
            edges: []
        }
    };
    const refreshedDiagram = {
        graph: {
            nodes: [{ id: 'component:M1' }],
            edges: [{ id: 'connection:M1.out->Lamp.in' }]
        }
    };

    assert.deepEqual(
        semanticSelectionModel.retainSelectionIfPresent(visibleDiagram, selected),
        selected
    );
    assert.equal(
        semanticSelectionModel.retainSelectionIfPresent(refreshedDiagram, selected),
        undefined
    );
    assert.equal(
        semanticSelectionModel.graphContainsSemanticId(visibleDiagram, 'port:PLC1.out'),
        true
    );
    assert.equal(
        semanticSelectionModel.graphContainsSemanticId(refreshedDiagram, 'port:PLC1.out'),
        false
    );
});

test('keeps package-aware navigation selection when an existing workbench sheet publishes the canonical subject id', () => {
    const selected = {
        semanticId: 'component:VendorPump',
        label: 'VendorPump',
        kind: 'component',
        sourceUri: 'file:///workspace/vendor/pump.athena',
        sourceRange: {
            start: { line: 4, character: 0 },
            end: { line: 8, character: 1 }
        }
    };
    const sheetPublishedDiagram = {
        activeSheetId: 'vendor/sheet/01-pump',
        sheets: [
            {
                sheetId: 'vendor/sheet/01-pump',
                displayName: 'Vendor Pump',
                order: 0,
                subjectSemanticIds: ['component:VendorPump']
            }
        ],
        graph: {
            nodes: [],
            edges: []
        }
    };

    assert.equal(
        semanticSelectionModel.graphContainsSemanticId(sheetPublishedDiagram, 'component:VendorPump'),
        true
    );
    assert.deepEqual(
        semanticSelectionModel.retainSelectionIfPresent(sheetPublishedDiagram, selected),
        selected
    );
});

test('does not treat inactive sheet subject metadata as already revealed', () => {
    const inactiveSheetDiagram = {
        activeSheetId: 'vendor/sheet/01-overview',
        sheets: [
            {
                sheetId: 'vendor/sheet/01-overview',
                subjectSemanticIds: ['component:OverviewOnly']
            },
            {
                sheetId: 'vendor/sheet/02-pump',
                subjectSemanticIds: ['component:VendorPump']
            }
        ],
        graph: {
            nodes: [],
            edges: []
        }
    };

    assert.equal(
        semanticSelectionModel.graphContainsSemanticId(inactiveSheetDiagram, 'component:VendorPump'),
        false
    );
});

test('resolves repeated projection occurrences without inventing alias identities', () => {
    assert.equal(typeof semanticSelectionModel.resolveProjectionOccurrence, 'function');

    const repeatedDiagram = {
        crossReferences: [
            {
                semanticId: 'component:PLC1',
                kind: 'repeated_reference',
                sheetIds: ['documentation/sheet/01-overview', 'documentation/sheet/02-reference'],
                occurrenceIds: [
                    'documentation/projection/node/component_PLC1',
                    'documentation/projection/node/component_PLC1_reference'
                ]
            }
        ],
        graph: {
            nodes: [
                { id: 'documentation/projection/node/component_PLC1', semanticId: 'component:PLC1' },
                { id: 'documentation/projection/node/component_PLC1_reference', semanticId: 'component:PLC1' }
            ],
            edges: []
        }
    };

    assert.deepEqual(
        semanticSelectionModel.resolveProjectionCrossReference(repeatedDiagram, 'component:PLC1'),
        {
            semanticId: 'component:PLC1',
            kind: 'repeated_reference',
            sheetIds: ['documentation/sheet/01-overview', 'documentation/sheet/02-reference'],
            occurrenceIds: [
                'documentation/projection/node/component_PLC1',
                'documentation/projection/node/component_PLC1_reference'
            ]
        }
    );
    assert.deepEqual(
        semanticSelectionModel.resolveProjectionOccurrence(repeatedDiagram, 'component:PLC1'),
        {
            semanticId: 'component:PLC1',
            status: 'ambiguous',
            occurrenceIds: [
                'documentation/projection/node/component_PLC1',
                'documentation/projection/node/component_PLC1_reference'
            ]
        }
    );
    assert.deepEqual(
        semanticSelectionModel.resolveProjectionOccurrence(repeatedDiagram, 'component:M1'),
        {
            semanticId: 'component:M1',
            status: 'unresolved',
            occurrenceIds: []
        }
    );
});

test('keeps port selection alive through governed endpoint and anchor aliases', () => {
    assert.equal(typeof semanticSelectionModel.resolveProjectionEndpointAlias, 'function');

    const endpointOnlyDiagram = {
        electricalAnchors: [
            {
                anchorId: 'cabinet/projection/label/port_PLC1_out/anchor',
                portSemanticId: 'port:PLC1.out',
                ownerSemanticId: 'component:PLC1',
                nodeId: 'cabinet/projection/node/component_PLC1',
                labelId: 'cabinet/projection/label/port_PLC1_out'
            }
        ],
        electricalConnectionEndpoints: [
            {
                endpointId: 'cabinet/projection/connection/connection_PLC1_out_M1_in/endpoint/source',
                projectionConnectionId: 'cabinet/projection/connection/connection_PLC1_out_M1_in',
                connectionSemanticId: 'connection:PLC1.out->M1.in',
                endpointRole: 'source',
                portSemanticId: 'port:PLC1.out',
                anchorId: 'cabinet/projection/label/port_PLC1_out/anchor'
            }
        ],
        graph: {
            nodes: [{ id: 'component:PLC1' }],
            edges: [{ id: 'connection:PLC1.out->M1.in' }]
        }
    };

    assert.deepEqual(
        semanticSelectionModel.resolveProjectionEndpointAlias(endpointOnlyDiagram, 'port:PLC1.out'),
        {
            semanticId: 'port:PLC1.out',
            status: 'ambiguous',
            endpointIds: ['cabinet/projection/connection/connection_PLC1_out_M1_in/endpoint/source'],
            anchorIds: ['cabinet/projection/label/port_PLC1_out/anchor'],
            connectionIds: ['connection:PLC1.out->M1.in']
        }
    );
    assert.equal(
        semanticSelectionModel.graphContainsSemanticId(endpointOnlyDiagram, 'port:PLC1.out'),
        true
    );
    assert.deepEqual(
        semanticSelectionModel.retainSelectionIfPresent(endpointOnlyDiagram, {
            semanticId: 'port:PLC1.out',
            label: 'PLC1.out',
            kind: 'port'
        }),
        {
            semanticId: 'port:PLC1.out',
            label: 'PLC1.out',
            kind: 'port'
        }
    );
});

test('resolves related subjects through governed anchors and connection endpoints', () => {
    const relatedDiagram = {
        electricalAnchors: [
            {
                anchorId: 'cabinet/projection/label/port_PLC1_out/anchor',
                portSemanticId: 'port:PLC1.out',
                ownerSemanticId: 'component:PLC1',
                nodeId: 'cabinet/projection/node/component_PLC1',
                labelId: 'cabinet/projection/label/port_PLC1_out'
            },
            {
                anchorId: 'cabinet/projection/label/port_M1_in/anchor',
                portSemanticId: 'port:M1.in',
                ownerSemanticId: 'component:M1',
                nodeId: 'cabinet/projection/node/component_M1',
                labelId: 'cabinet/projection/label/port_M1_in'
            }
        ],
        electricalConnectionEndpoints: [
            {
                endpointId: 'cabinet/projection/connection/connection_PLC1_out_M1_in/endpoint/source',
                projectionConnectionId: 'cabinet/projection/connection/connection_PLC1_out_M1_in',
                connectionSemanticId: 'connection:PLC1.out->M1.in',
                endpointRole: 'source',
                portSemanticId: 'port:PLC1.out',
                anchorId: 'cabinet/projection/label/port_PLC1_out/anchor'
            },
            {
                endpointId: 'cabinet/projection/connection/connection_PLC1_out_M1_in/endpoint/target',
                projectionConnectionId: 'cabinet/projection/connection/connection_PLC1_out_M1_in',
                connectionSemanticId: 'connection:PLC1.out->M1.in',
                endpointRole: 'target',
                portSemanticId: 'port:M1.in',
                anchorId: 'cabinet/projection/label/port_M1_in/anchor'
            }
        ],
        graph: {
            nodes: [],
            edges: []
        }
    };

    assert.deepEqual(
        semanticSelectionModel.resolveProjectionRelatedSubjects(relatedDiagram, 'port:PLC1.out'),
        [
            {
                semanticId: 'port:PLC1.out',
                relatedSemanticId: 'component:PLC1',
                relation: 'owner'
            },
            {
                semanticId: 'port:PLC1.out',
                relatedSemanticId: 'connection:PLC1.out->M1.in',
                relation: 'connection'
            }
        ]
    );
    assert.deepEqual(
        semanticSelectionModel.resolveProjectionRelatedSubjects(relatedDiagram, 'connection:PLC1.out->M1.in'),
        [
            {
                semanticId: 'connection:PLC1.out->M1.in',
                relatedSemanticId: 'port:PLC1.out',
                relation: 'source-port'
            },
            {
                semanticId: 'connection:PLC1.out->M1.in',
                relatedSemanticId: 'port:M1.in',
                relation: 'target-port'
            }
        ]
    );
});
