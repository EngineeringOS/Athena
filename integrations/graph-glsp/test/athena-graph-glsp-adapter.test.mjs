import assert from 'node:assert/strict';
import test from 'node:test';

const adapter = await import('../lib/index.js');

const readyProjectionSession = {
    projectName: 'FactoryLine',
    semanticPath: 'frontend -> LSP -> runtime/compiler',
    activeViewId: 'cabinet',
    supportedViews: [
        {
            viewId: 'cabinet',
            displayName: 'Cabinet',
            description: 'Cabinet projection',
            familyId: 'electrical/cabinet',
            ownershipContract: {
                interactivity: 'interactive',
                displayScopes: ['devices', 'ports', 'ownership-relationships'],
                semanticCommandIds: [],
                projectionCommandIds: ['adjust-layout-placement', 'adjust-layout-grouping'],
                transientInteractionKinds: ['navigate-view', 'inspect-selection', 'preview-related-elements'],
                persistedProjectionMetadataKeys: ['layout-placement', 'layout-group-membership']
            }
        }
    ],
    governedCommands: [
        {
            commandId: 'switch-active-view',
            displayName: 'Switch active view',
            description: 'Switch the runtime-owned active projection view.',
            requiredArguments: ['viewId']
        }
    ],
    status: 'ready',
    readyProjection: {
        viewId: 'cabinet',
        familyId: 'electrical/cabinet',
        systemName: 'FactoryLine',
        canvasWidth: 1440,
        canvasHeight: 900,
        presentation: {
            canvasWidth: 1440,
            canvasHeight: 900,
            primitivePacks: [
                {
                    packId: 'electrical-primitives/default-v1',
                    displayName: 'Electrical primitives',
                    familyIds: ['electrical/cabinet'],
                    primitives: [
                        {
                            primitiveId: 'electrical.frame.device-box',
                            displayName: 'Device box',
                            viewBoxWidth: 140,
                            viewBoxHeight: 72,
                            commands: [
                                {
                                    kind: 'stroke_rectangle',
                                    bounds: { x: 8, y: 12, width: 124, height: 48 },
                                    strokeTokenKey: 'stroke',
                                    strokeWidthTokenKey: 'strokeWidth'
                                }
                            ],
                            textSlots: [],
                            anchors: [],
                            tokenDefaults: { stroke: '#202020', strokeWidth: '1.6', label: '#202020' },
                            supportedOrientations: ['horizontal', 'vertical']
                        },
                        {
                            primitiveId: 'electrical.mark.contact-open',
                            displayName: 'Open contact mark',
                            viewBoxWidth: 32,
                            viewBoxHeight: 32,
                            commands: [
                                {
                                    kind: 'svg_path',
                                    pathData: 'M 8 8 L 8 24 M 24 8 L 24 24 M 8 20 L 24 12',
                                    strokeTokenKey: 'stroke',
                                    strokeWidthTokenKey: 'strokeWidth'
                                }
                            ],
                            textSlots: [],
                            anchors: [],
                            tokenDefaults: { stroke: '#202020', strokeWidth: '1.6', label: '#202020' },
                            supportedOrientations: ['horizontal', 'vertical']
                        }
                    ]
                }
            ],
            compositePacks: [],
            occurrences: [],
            connectors: [],
            representationFacts: [
                {
                    subjectId: 'component:PLC1',
                    occurrenceId: 'representation:PLC1@schematic-sheet',
                    sourceProjectionIds: ['cabinet/projection/node/component_PLC1'],
                    symbol: {
                        familyId: 'plc-controller'
                    },
                    anatomy: {
                        representationId: 'athena-industrial-control-v0:plc-controller',
                        context: 'electrical_schematic',
                        bounds: { width: 80, height: 48 },
                        hotspot: { x: 0, y: 0 },
                        primitives: [
                            {
                                kind: 'rectangle',
                                primitiveId: 'plc-controller:body',
                                origin: { x: 0, y: 0 },
                                size: { width: 80, height: 48 }
                            },
                            {
                                kind: 'line',
                                primitiveId: 'plc-controller:terminal-line',
                                start: { x: 60, y: 24 },
                                end: { x: 80, y: 24 }
                            }
                        ],
                        terminals: [],
                        labelAnchors: []
                    },
                    terminals: [],
                    labels: []
                }
            ]
        },
        activeSheetId: 'cabinet/sheet/01-main',
        sheets: [
            {
                sheetId: 'cabinet/sheet/01-main',
                displayName: 'Cabinet Main',
                order: 0,
                subjectSemanticIds: ['component:PLC1', 'connection:PLC1.out->M1.in', 'port:PLC1.out']
            }
        ],
        notationPack: {
            packId: 'electrical-notation/cabinet/default-v1',
            displayName: 'Electrical Cabinet Default',
            subjects: [
                {
                    semanticId: 'component:PLC1',
                    symbolKey: 'device.cabinet.default',
                    labelPolicy: 'subject_label',
                    markerKeys: ['owned-device']
                }
            ]
        },
        crossReferences: [
            {
                semanticId: 'component:PLC1',
                kind: 'repeated_reference',
                crossReferenceId: 'cross-reference:component:PLC1',
                sheetIds: ['cabinet/sheet/01-main', 'cabinet/sheet/02-reference'],
                occurrenceIds: [
                    'cabinet/projection/node/component_PLC1',
                    'cabinet/projection/node/component_PLC1_reference'
                ],
                links: [
                    {
                        semanticId: 'component:PLC1',
                        sourceSheetId: 'cabinet/sheet/01-main',
                        targetSheetId: 'cabinet/sheet/02-reference',
                        sourceOccurrenceId: 'cabinet/projection/node/component_PLC1',
                        targetOccurrenceId: 'cabinet/projection/node/component_PLC1_reference',
                        compactNotation: '01-main -> 02-reference'
                    }
                ]
            }
        ],
        electricalAnchors: [
            {
                anchorId: 'cabinet/projection/label/port_PLC1_out/anchor',
                portSemanticId: 'port:PLC1.out',
                ownerSemanticId: 'component:PLC1',
                nodeId: 'cabinet/projection/node/component_PLC1',
                labelId: 'cabinet/projection/label/port_PLC1_out',
                x: 380,
                y: 160,
                side: 'right'
            },
            {
                anchorId: 'cabinet/projection/label/port_M1_in/anchor',
                portSemanticId: 'port:M1.in',
                ownerSemanticId: 'component:M1',
                nodeId: 'cabinet/projection/node/component_M1',
                labelId: 'cabinet/projection/label/port_M1_in',
                x: 720,
                y: 160,
                side: 'left'
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
        electricalRoutingCorridors: [
            {
                corridorId: 'cabinet/projection/connection/connection_PLC1_out_M1_in/corridor',
                projectionConnectionId: 'cabinet/projection/connection/connection_PLC1_out_M1_in',
                connectionSemanticId: 'connection:PLC1.out->M1.in',
                sourceAnchorId: 'cabinet/projection/label/port_PLC1_out/anchor',
                targetAnchorId: 'cabinet/projection/label/port_M1_in/anchor',
                routingStyle: 'orthogonal',
                preferredBendPoints: [
                    {
                        x: 540,
                        y: 160
                    },
                    {
                        x: 540,
                        y: 220
                    }
                ]
            }
        ],
        activeRenderContributions: [
            {
                pluginId: 'com.engineeringood.athena.domain.electrical-runtime',
                contributionId: 'electrical-runtime.render.cabinet',
                displayName: 'Electrical cabinet rendering intent',
                description: 'Publishes cabinet-view visual intent for hosted electrical structure without taking renderer ownership.',
                rendererTarget: 'graph-workbench',
                surfaceMappings: [
                    {
                        surface: 'canvas',
                        tokens: {
                            canvasTint: 'rgba(208, 208, 204, 0.98)'
                        }
                    }
                ]
            }
        ],
        components: [
            {
                projectionId: 'cabinet/projection/node/component_PLC1',
                semanticId: 'component:PLC1',
                label: 'PLC1',
                x: 120,
                y: 80,
                width: 260,
                height: 160
            }
        ],
        connections: [
            {
                projectionId: 'cabinet/projection/connection/connection_PLC1_out_M1_in',
                semanticId: 'connection:PLC1.out->M1.in',
                x1: 380,
                y1: 160,
                x2: 720,
                y2: 160
            }
        ],
        labels: [
            {
                projectionId: 'cabinet/projection/label/port_PLC1_out',
                semanticId: 'port:PLC1.out',
                label: 'out',
                x: 390,
                y: 142,
                width: 60,
                height: 20
            }
        ]
    },
    diagnostics: []
};

const unavailableProjectionSession = {
    projectName: 'FactoryLine',
    semanticPath: 'frontend -> LSP -> runtime/compiler',
    activeViewId: 'cabinet',
    supportedViews: [],
    governedCommands: [],
    status: 'unavailable',
    unavailableReason: 'No supported projection views are available.',
    diagnostics: [
        {
            severity: 'error',
            code: 'projection.unavailable',
            message: 'Projection materialization failed.',
            provenance: 'runtime'
        }
    ]
};

test('translates a ready Athena projection session into a GLSP-shaped diagram model', () => {
    assert.equal(typeof adapter.translateProjectionSessionToGLSPDiagram, 'function');

    const diagram = adapter.translateProjectionSessionToGLSPDiagram(readyProjectionSession);

    assert.equal(diagram.kind, 'athena-glsp-diagram');
    assert.equal(diagram.status, 'ready');
    assert.equal(diagram.activeRenderContributions.length, 1);
    assert.equal(diagram.activeRenderContributions[0].contributionId, 'electrical-runtime.render.cabinet');
    assert.equal(diagram.supportedViews[0].familyId, 'electrical/cabinet');
    assert.equal(diagram.activeSheetId, 'cabinet/sheet/01-main');
    assert.equal(diagram.sheets.length, 1);
    assert.equal(diagram.sheets[0].role, undefined);
    assert.equal(diagram.notationPack?.packId, 'electrical-notation/cabinet/default-v1');
    assert.equal(diagram.crossReferences.length, 1);
    assert.equal(diagram.electricalAnchors.length, 2);
    assert.equal(diagram.electricalConnectionEndpoints.length, 2);
    assert.equal(diagram.electricalRoutingCorridors.length, 1);
    assert.equal(diagram.presentation?.primitivePacks[0].primitives[1].commands[0].kind, 'svg_path');
    assert.equal(diagram.presentation?.representationFacts[0].subjectId, 'component:PLC1');
    assert.equal(diagram.presentation?.representationFacts[0].anatomy.primitives[1].kind, 'line');
    assert.deepEqual(diagram.presentation?.representationFacts[0].sourceProjectionIds, ['cabinet/projection/node/component_PLC1']);
    assert.equal(
        diagram.presentation?.primitivePacks[0].primitives[1].commands[0].pathData,
        'M 8 8 L 8 24 M 24 8 L 24 24 M 8 20 L 24 12'
    );
    assert.deepEqual(diagram.crossReferences[0], readyProjectionSession.readyProjection.crossReferences[0]);
    assert.deepEqual(diagram.supportedViews[0].ownershipContract, readyProjectionSession.supportedViews[0].ownershipContract);
    assert.equal(diagram.graph.type, 'graph');
    assert.equal(diagram.graph.nodes.length, 2);
    assert.equal(diagram.graph.edges.length, 1);
    assert.deepEqual(diagram.graph.nodes[0], {
        id: 'cabinet/projection/node/component_PLC1',
        semanticId: 'component:PLC1',
        type: 'node',
        kind: 'component',
        label: 'PLC1',
        position: {
            x: 120,
            y: 80
        },
        size: {
            width: 260,
            height: 160
        }
    });
    assert.deepEqual(diagram.graph.nodes[1], {
        id: 'cabinet/projection/label/port_PLC1_out',
        semanticId: 'port:PLC1.out',
        type: 'node',
        kind: 'label',
        label: 'out',
        position: {
            x: 390,
            y: 142
        },
        size: {
            width: 60,
            height: 20
        }
    });
    assert.deepEqual(diagram.graph.edges[0], {
        id: 'cabinet/projection/connection/connection_PLC1_out_M1_in',
        semanticId: 'connection:PLC1.out->M1.in',
        type: 'edge',
        sourcePoint: {
            x: 380,
            y: 160
        },
        targetPoint: {
            x: 720,
            y: 160
        },
        routingStyle: 'orthogonal',
        bendPoints: [
            {
                x: 540,
                y: 160
            },
            {
                x: 540,
                y: 220
            }
        ],
        sourceAnchorId: 'cabinet/projection/label/port_PLC1_out/anchor',
        targetAnchorId: 'cabinet/projection/label/port_M1_in/anchor',
        sourcePortSemanticId: 'port:PLC1.out',
        targetPortSemanticId: 'port:M1.in'
    });
});

test('assigns stable M31 document sheet roles from typed policy evidence', () => {
    assert.equal(typeof adapter.translateProjectionSessionToGLSPDiagram, 'function');

    const diagram = adapter.translateProjectionSessionToGLSPDiagram({
        ...readyProjectionSession,
        activeViewId: 'documentation',
        readyProjection: {
            ...readyProjectionSession.readyProjection,
            viewId: 'documentation',
            activeSheetId: 'documentation/sheet/01-control',
            sheets: [
                {
                    sheetId: 'documentation/sheet/01-control',
                    displayName: 'Control',
                    order: 0,
                    subjectSemanticIds: ['component:ControllerPLC1'],
                    policyEvidence: {
                        policyId: 'athena-m31-customer-projection-v0',
                        policyVersion: '0',
                        policyDeterministicIdentity: 'policy:m31:test',
                        sheetViewRole: 'control-and-plc-logic',
                        sheetViewRoleOrder: 0,
                    },
                },
                {
                    sheetId: 'documentation/sheet/02-field-device',
                    displayName: 'Field Device',
                    order: 1,
                    subjectSemanticIds: ['component:TerminalBlock1'],
                    policyEvidence: {
                        policyId: 'athena-m31-customer-projection-v0',
                        policyVersion: '0',
                        policyDeterministicIdentity: 'policy:m31:test',
                        sheetViewRole: 'field-wiring-and-terminal-transition',
                        sheetViewRoleOrder: 1,
                    },
                },
            ],
        },
    });

    assert.deepEqual(
        diagram.sheets.map(sheet => ({
            sheetId: sheet.sheetId,
            role: sheet.role,
            policyEvidence: sheet.policyEvidence,
        })),
        [
            {
                sheetId: 'documentation/sheet/01-control',
                role: 'control-and-plc-logic',
                policyEvidence: {
                    policyId: 'athena-m31-customer-projection-v0',
                    policyVersion: '0',
                    policyDeterministicIdentity: 'policy:m31:test',
                    sheetViewRole: 'control-and-plc-logic',
                    sheetViewRoleOrder: 0,
                },
            },
            {
                sheetId: 'documentation/sheet/02-field-device',
                role: 'field-wiring-and-terminal-transition',
                policyEvidence: {
                    policyId: 'athena-m31-customer-projection-v0',
                    policyVersion: '0',
                    policyDeterministicIdentity: 'policy:m31:test',
                    sheetViewRole: 'field-wiring-and-terminal-transition',
                    sheetViewRoleOrder: 1,
                },
            },
        ]
    );
});

test('drops malformed sheet policy evidence instead of treating it as governed authority', () => {
    assert.equal(typeof adapter.translateProjectionSessionToGLSPDiagram, 'function');

    const diagram = adapter.translateProjectionSessionToGLSPDiagram({
        ...readyProjectionSession,
        activeViewId: 'documentation',
        readyProjection: {
            ...readyProjectionSession.readyProjection,
            viewId: 'documentation',
            activeSheetId: 'documentation/sheet/02-field-device',
            sheets: [
                {
                    sheetId: 'documentation/sheet/02-field-device',
                    displayName: 'Field Device',
                    order: 1,
                    subjectSemanticIds: ['component:TerminalBlock1'],
                    policyEvidence: {
                        sheetViewRole: 'field-wiring-and-terminal-transition',
                    },
                },
            ],
        },
    });

    assert.equal(diagram.sheets[0].policyEvidence, undefined);
    assert.equal(diagram.sheets[0].role, undefined);
});

test('keeps unavailable projection state disposable instead of inventing fallback authority', () => {
    assert.equal(typeof adapter.translateProjectionSessionToGLSPDiagram, 'function');

    const diagram = adapter.translateProjectionSessionToGLSPDiagram(unavailableProjectionSession);

    assert.equal(diagram.kind, 'athena-glsp-diagram');
    assert.equal(diagram.status, 'unavailable');
    assert.equal(diagram.activeRenderContributions.length, 0);
    assert.equal(diagram.graph.nodes.length, 0);
    assert.equal(diagram.graph.edges.length, 0);
    assert.equal(diagram.unavailableReason, 'No supported projection views are available.');
    assert.deepEqual(diagram.diagnostics, unavailableProjectionSession.diagnostics);
});

test('normalizes omitted projection arrays from transport payloads before building a diagram', () => {
    assert.equal(typeof adapter.translateProjectionSessionToGLSPDiagram, 'function');

    const diagram = adapter.translateProjectionSessionToGLSPDiagram({
        projectName: 'FactoryLine',
        semanticPath: 'frontend -> LSP -> runtime/compiler',
        activeViewId: 'cabinet',
        status: 'ready',
        readyProjection: {
            viewId: 'cabinet',
            systemName: 'FactoryLine',
            canvasWidth: 1440,
            canvasHeight: 900,
        },
    });

    assert.equal(diagram.kind, 'athena-glsp-diagram');
    assert.equal(diagram.status, 'ready');
    assert.deepEqual(diagram.supportedViews, []);
    assert.deepEqual(diagram.governedCommands, []);
    assert.equal(diagram.activeSheetId, undefined);
    assert.deepEqual(diagram.sheets, []);
    assert.equal(diagram.notationPack, undefined);
    assert.deepEqual(diagram.crossReferences, []);
    assert.deepEqual(diagram.electricalAnchors, []);
    assert.deepEqual(diagram.electricalConnectionEndpoints, []);
    assert.deepEqual(diagram.electricalRoutingCorridors, []);
    assert.deepEqual(diagram.diagnostics, []);
    assert.deepEqual(diagram.activeRenderContributions, []);
    assert.deepEqual(diagram.graph.nodes, []);
    assert.deepEqual(diagram.graph.edges, []);
});

test('normalizes partial or missing ownership contracts inside supported views', () => {
    assert.equal(typeof adapter.translateProjectionSessionToGLSPDiagram, 'function');

    const diagram = adapter.translateProjectionSessionToGLSPDiagram({
        projectName: 'FactoryLine',
        semanticPath: 'frontend -> LSP -> runtime/compiler',
        activeViewId: 'cabinet',
        status: 'ready',
        supportedViews: [
            {
                viewId: 'cabinet',
                displayName: 'Cabinet',
                description: 'Cabinet projection',
                familyId: 'electrical/cabinet',
                ownershipContract: {
                    interactivity: 'interactive',
                    displayScopes: ['devices'],
                    projectionCommandIds: ['adjust-layout-placement']
                }
            },
            {
                viewId: 'wiring',
                displayName: 'Wiring',
                description: 'Wiring projection'
            }
        ],
        readyProjection: {
            viewId: 'cabinet',
            systemName: 'FactoryLine',
            canvasWidth: 1440,
            canvasHeight: 900,
        },
    });

    assert.deepEqual(diagram.supportedViews[0].ownershipContract, {
        interactivity: 'interactive',
        displayScopes: ['devices'],
        semanticCommandIds: [],
        projectionCommandIds: ['adjust-layout-placement'],
        transientInteractionKinds: [],
        persistedProjectionMetadataKeys: []
    });
    assert.equal(diagram.supportedViews[0].familyId, 'electrical/cabinet');
    assert.deepEqual(diagram.supportedViews[1].ownershipContract, {
        interactivity: 'inspect_only',
        displayScopes: [],
        semanticCommandIds: [],
        projectionCommandIds: [],
        transientInteractionKinds: [],
        persistedProjectionMetadataKeys: []
    });
});
