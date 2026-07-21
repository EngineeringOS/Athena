import assert from 'node:assert/strict';
import test from 'node:test';

const graphWorkbenchModel = await import('../lib/browser/athena-graph-workbench-model.js');

const readyDiagram = {
    kind: 'athena-glsp-diagram',
    projectName: 'FactoryLine',
    semanticPath: 'frontend -> LSP -> runtime/compiler',
    activeViewId: 'cabinet',
    status: 'ready',
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
                        canvasTint: 'var(--athena-graph-cabinet-canvas-tint)'
                    }
                },
                {
                    surface: 'node',
                    tokens: {
                        fill: 'var(--athena-graph-cabinet-node-fill)'
                    }
                }
            ]
        }
    ],
    supportedViews: [
        {
            viewId: 'cabinet',
            displayName: 'Cabinet',
            description: 'Cabinet projection',
            familyId: 'electrical/cabinet',
            ownershipContract: {
                interactivity: 'interactive',
                displayScopes: ['devices', 'ports'],
                semanticCommandIds: [],
                projectionCommandIds: ['adjust-layout-placement'],
                transientInteractionKinds: ['navigate-view', 'inspect-selection'],
                persistedProjectionMetadataKeys: ['layout-placement']
            }
        }
    ],
    governedCommands: [],
    activeSheetId: 'cabinet/sheet/01-main',
    sheets: [
        {
            sheetId: 'cabinet/sheet/01-main',
            displayName: 'Cabinet Main',
            order: 0,
            subjectSemanticIds: ['component:PLC1', 'connection:PLC1.out->M1.in']
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
            },
            {
                semanticId: 'port:PLC1.out',
                symbolKey: 'port.cabinet.default',
                labelPolicy: 'terminal_label',
                markerKeys: []
            },
            {
                semanticId: 'port:M1.in',
                symbolKey: 'port.cabinet.default',
                labelPolicy: 'terminal_label',
                markerKeys: []
            }
        ]
    },
    crossReferences: [
        {
            semanticId: 'component:PLC1',
            kind: 'repeated_reference',
            sheetIds: ['cabinet/sheet/01-main', 'cabinet/sheet/02-reference'],
            occurrenceIds: [
                'cabinet/projection/node/component_PLC1',
                'cabinet/projection/node/component_PLC1_reference'
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
            y: 260,
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
                    y: 260
                }
            ]
        }
    ],
    diagnostics: [],
    graph: {
        id: 'FactoryLine:cabinet',
        type: 'graph',
        canvas: {
            width: 1440,
            height: 900
        },
        nodes: [
            {
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
            },
            {
                id: 'cabinet/projection/label/port_PLC1_out',
                semanticId: 'port:PLC1.out',
                type: 'node',
                kind: 'label',
                label: 'OUT',
                position: {
                    x: 402,
                    y: 148
                },
                size: {
                    width: 42,
                    height: 20
                }
            },
            {
                id: 'cabinet/projection/label/port_M1_in',
                semanticId: 'port:M1.in',
                type: 'node',
                kind: 'label',
                label: 'IN',
                position: {
                    x: 628,
                    y: 248
                },
                size: {
                    width: 32,
                    height: 20
                }
            }
        ],
        edges: [
            {
                id: 'cabinet/projection/connection/connection_PLC1_out_M1_in',
                semanticId: 'connection:PLC1.out->M1.in',
                type: 'edge',
                sourcePoint: {
                    x: 380,
                    y: 160
                },
                targetPoint: {
                    x: 720,
                    y: 260
                },
                routingStyle: 'orthogonal',
                bendPoints: [
                    {
                        x: 540,
                        y: 160
                    },
                    {
                        x: 540,
                        y: 260
                    }
                ],
                sourceAnchorId: 'cabinet/projection/label/port_PLC1_out/anchor',
                targetAnchorId: 'cabinet/projection/label/port_M1_in/anchor',
                sourcePortSemanticId: 'port:PLC1.out',
                targetPortSemanticId: 'port:M1.in'
            }
        ]
    }
};

const unavailableDiagram = {
    kind: 'athena-glsp-diagram',
    projectName: 'FactoryLine',
    semanticPath: 'frontend -> LSP -> runtime/compiler',
    activeViewId: 'cabinet',
    status: 'unavailable',
    activeRenderContributions: [],
    supportedViews: [],
    governedCommands: [],
    sheets: [],
    unavailableReason: 'No supported projection views are available.',
    diagnostics: [
        {
            severity: 'error',
            code: 'projection.unavailable',
            message: 'Projection materialization failed.',
            provenance: 'runtime'
        }
    ],
    electricalAnchors: [],
    electricalConnectionEndpoints: [],
    electricalRoutingCorridors: [],
    graph: {
        id: 'FactoryLine:cabinet',
        type: 'graph',
        canvas: {
            width: 0,
            height: 0
        },
        nodes: [],
        edges: []
    }
};

test('builds a ready graphical workbench model from the adapter diagram', () => {
    assert.equal(typeof graphWorkbenchModel.buildAthenaGraphWorkbenchModel, 'function');

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(readyDiagram);

    assert.equal(model.statusTone, 'ready');
    assert.equal(model.headerTitle, 'FactoryLine');
    assert.equal(model.viewLabel, 'Cabinet');
    assert.equal(model.viewFamilyId, 'electrical/cabinet');
    assert.equal(model.isElectricalFamily, true);
    assert.equal(model.activeSheetId, 'cabinet/sheet/01-main');
    assert.equal(model.sheetCount, 1);
    assert.equal(model.notationPackId, 'electrical-notation/cabinet/default-v1');
    assert.equal(model.crossReferenceCount, 1);
    assert.deepEqual(model.sheetChrome.frame, {
        width: 1440,
        height: 900
    });
    assert.deepEqual(model.sheetChrome.grid, {
        majorStep: 120,
        minorStep: 24
    });
    assert.deepEqual(model.sheetChrome.activeSheet, {
        sheetId: 'cabinet/sheet/01-main',
        displayName: 'Cabinet Main',
        order: 0,
        subjectSemanticIds: ['component:PLC1', 'connection:PLC1.out->M1.in'],
        subjectCount: 2,
        isActive: true
    });
    assert.deepEqual(model.sheetChrome.titleBlock, {
        sheetId: 'cabinet/sheet/01-main',
        displayName: 'Cabinet Main',
        order: 0,
        subjectCount: 2,
        crossReferenceCount: 1
    });
    assert.deepEqual(model.sheetChrome.crossReferenceMarkers, [
        {
            semanticId: 'component:PLC1',
            kind: 'repeated_reference',
            markerLabel: 'repeated reference',
            sheetIds: ['cabinet/sheet/01-main', 'cabinet/sheet/02-reference'],
            occurrenceIds: [
                'cabinet/projection/node/component_PLC1',
                'cabinet/projection/node/component_PLC1_reference'
            ],
            isActiveSheetLinked: true
        }
    ]);
    assert.equal(model.svgViewBox, '120 80 600 188');
    assert.equal(model.canvas.width, 600);
    assert.equal(model.canvas.height, 188);
    assert.equal(model.metrics.nodeCount, 3);
    assert.equal(model.metrics.edgeCount, 1);
    assert.equal(model.sceneBounds.minX, 120);
    assert.equal(model.sceneBounds.minY, 80);
    assert.equal(model.sceneBounds.maxX, 720);
    assert.equal(model.sceneBounds.maxY, 268);
    assert.equal(model.surfaceTokens.canvas.canvasTint, 'var(--athena-graph-cabinet-canvas-tint)');
    assert.equal(model.surfaceTokens.node.fill, 'var(--athena-graph-cabinet-node-fill)');
    assert.equal(model.edges[0].conductorStyle, 'electrical');
    assert.equal(model.edges[0].path, 'M 380 160 L 540 160 L 540 260 L 720 260');
    assert.equal(
        model.nodes.find(node => node.semanticId === 'component:PLC1')?.renderVariant,
        'electrical-device'
    );
    assert.equal(
        model.nodes.find(node => node.semanticId === 'port:PLC1.out')?.renderVariant,
        'electrical-terminal-label'
    );
    assert.deepEqual(
        model.nodes.find(node => node.semanticId === 'port:PLC1.out')?.labelLeader,
        {
            start: {
                x: 380,
                y: 160
            },
            end: {
                x: 402,
                y: 160
            }
        }
    );
    assert.deepEqual(model.edges[0].bendMarkerPoints, [
        {
            x: 540,
            y: 160
        },
        {
            x: 540,
            y: 260
        }
    ]);
    assert.deepEqual(model.edges[0].terminals, [
        {
            role: 'source',
            point: {
                x: 380,
                y: 160
            },
            endpointId: 'cabinet/projection/connection/connection_PLC1_out_M1_in/endpoint/source',
            anchorId: 'cabinet/projection/label/port_PLC1_out/anchor',
            portSemanticId: 'port:PLC1.out',
            ownerSemanticId: 'component:PLC1',
            nodeId: 'cabinet/projection/node/component_PLC1',
            labelId: 'cabinet/projection/label/port_PLC1_out'
        },
        {
            role: 'target',
            point: {
                x: 720,
                y: 260
            },
            endpointId: 'cabinet/projection/connection/connection_PLC1_out_M1_in/endpoint/target',
            anchorId: 'cabinet/projection/label/port_M1_in/anchor',
            portSemanticId: 'port:M1.in',
            ownerSemanticId: 'component:M1',
            nodeId: 'cabinet/projection/node/component_M1',
            labelId: 'cabinet/projection/label/port_M1_in'
        }
    ]);
    assert.equal(model.emptyState, undefined);
});

test('uses governed presentation sheet surface facts before canvas-derived sheet chrome', () => {
    const diagram = {
        ...readyDiagram,
        presentation: {
            canvasWidth: 1700,
            canvasHeight: 1100,
            primitivePacks: [],
            compositePacks: [],
            occurrences: [],
            connectors: [],
            sheetSurface: {
                surfaceId: 'presentation/sheet-surface/a3-landscape',
                source: 'presentation-ir',
                frame: {
                    width: 1680,
                    height: 1080,
                    margins: {
                        top: 40,
                        right: 48,
                        bottom: 72,
                        left: 48
                    },
                    zoneColumns: ['1', '2', '3', '4', '5', '6'],
                    zoneRows: ['A', 'B', 'C', 'D']
                },
                grid: {
                    majorStep: 96,
                    minorStep: 24
                },
                titleBlock: {
                    fields: [
                        { role: 'project', label: 'Project', value: 'FactoryLine' },
                        { role: 'sheet', label: 'Sheet', value: 'Cabinet Main' },
                        { role: 'policy', label: 'Policy', value: 'athena-m27-sheet-surface-v0' }
                    ]
                },
                metadata: {
                    sheetSize: 'A3',
                    orientation: 'landscape',
                    projectionPolicyId: 'athena-m27-sheet-surface-v0'
                }
            }
        }
    };

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);
    const second = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);

    assert.equal(model.sheetChrome.frame.width, 1680);
    assert.equal(model.sheetChrome.frame.height, 1080);
    assert.deepEqual(model.sheetChrome.frame.margins, {
        top: 40,
        right: 48,
        bottom: 72,
        left: 48
    });
    assert.deepEqual(model.sheetChrome.frame.zoneColumns, ['1', '2', '3', '4', '5', '6']);
    assert.deepEqual(model.sheetChrome.frame.zoneRows, ['A', 'B', 'C', 'D']);
    assert.deepEqual(model.sheetChrome.grid, {
        majorStep: 96,
        minorStep: 24
    });
    assert.deepEqual(model.sheetChrome.titleBlock?.fields, [
        { role: 'project', label: 'Project', value: 'FactoryLine' },
        { role: 'sheet', label: 'Sheet', value: 'Cabinet Main' },
        { role: 'policy', label: 'Policy', value: 'athena-m27-sheet-surface-v0' }
    ]);
    assert.equal(model.sheetChrome.metadata?.sheetSize, 'A3');
    assert.equal(model.sheetChrome.metadata?.orientation, 'landscape');
    assert.equal(model.svgViewBox, '120 80 600 188');
    assert.equal(model.canvas.width, 600);
    assert.equal(model.canvas.height, 188);
    assert.deepEqual(second.sheetChrome, model.sheetChrome);
});

test('keeps projection publication sheet chrome separate from active content bounds when no presentation surface exists', () => {
    const diagram = JSON.parse(JSON.stringify(readyDiagram));
    diagram.graph.canvas = {
        width: 2120,
        height: 172
    };
    diagram.presentation = undefined;
    diagram.sheets = [
        {
            ...diagram.sheets[0],
            publication: {
                pageSize: {
                    format: 'A3',
                    orientation: 'landscape'
                },
                frame: {
                    frameId: 'engineering-sheet-frame',
                    style: 'schematic'
                },
                coordinateZones: [],
                titleBlock: {
                    sheetTitle: 'Power Distribution',
                    sheetFamily: 'documentation',
                    sheetNumber: '01'
                },
                revisionMetadata: {
                    revisionCode: 'A',
                    revisionNote: 'Initial governed sheet publication'
                },
                viewComposition: {
                    primaryViewId: 'documentation',
                    primarySheetOrder: 0,
                    subjectSemanticIds: ['component:PLC1']
                }
            }
        }
    ];

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);

    assert.equal(model.sheetChrome.frame.width, 1680);
    assert.equal(model.sheetChrome.frame.height, 1188);
    assert.equal(model.sheetChrome.frame.source, 'projection-sheet-publication');
    assert.equal(model.sheetChrome.metadata?.sheetSize, 'A3');
    assert.equal(model.sheetChrome.metadata?.orientation, 'landscape');
    assert.notEqual(model.svgViewBox, '0 0 1680 1188');
    assert.equal(model.svgViewBox, '120 80 600 188');
    assert.equal(model.canvas.width, 600);
    assert.equal(model.canvas.height, 188);
    assert.deepEqual(model.sceneBounds, {
        minX: 120,
        minY: 80,
        maxX: 720,
        maxY: 268,
        width: 600,
        height: 188,
        centerX: 420,
        centerY: 174
    });
});

test('keeps governed sheet fit bounds stable when reference content is outside the sheet frame', () => {
    const diagram = JSON.parse(JSON.stringify(readyDiagram));
    diagram.presentation = undefined;
    diagram.graph.canvas = {
        width: 2120,
        height: 172
    };
    diagram.graph.nodes.push({
        id: 'documentation/projection/node/component_PLC1_reference',
        semanticId: 'component:PLC1',
        type: 'node',
        kind: 'component',
        label: 'PLC1 reference',
        position: {
            x: 2016,
            y: 120
        },
        size: {
            width: 260,
            height: 160
        }
    });
    diagram.graph.edges.push({
        id: 'documentation/projection/connection/reference_overflow',
        semanticId: 'connection:PLC1.out->Reference.in',
        type: 'edge',
        sourcePoint: {
            x: 1800,
            y: 180
        },
        targetPoint: {
            x: 2140,
            y: 180
        }
    });
    diagram.sheets = [
        {
            ...diagram.sheets[0],
            publication: {
                pageSize: {
                    format: 'A3',
                    orientation: 'landscape'
                },
                frame: {
                    frameId: 'engineering-sheet-frame',
                    style: 'schematic'
                },
                coordinateZones: [],
                titleBlock: {
                    sheetTitle: 'Control And PLC Logic',
                    sheetFamily: 'documentation',
                    sheetNumber: '02'
                },
                revisionMetadata: {
                    revisionCode: 'A',
                    revisionNote: 'Initial governed sheet publication'
                },
                viewComposition: {
                    primaryViewId: 'documentation',
                    primarySheetOrder: 1,
                    subjectSemanticIds: ['component:PLC1']
                }
            }
        }
    ];

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);

    assert.equal(model.sheetChrome.frame.width, 1680);
    assert.equal(model.sheetChrome.frame.height, 1188);
    assert.notEqual(model.svgViewBox, '0 0 1680 1188');
    assert.equal(model.svgViewBox, '120 80 600 188');
    assert.equal(model.sceneBounds.maxX, 720);
    assert.equal(model.sceneBounds.maxY, 268);
});

test('builds compact sheet-view selector entries from governed sheet metadata', () => {
    const diagram = JSON.parse(JSON.stringify(readyDiagram));
    diagram.activeSheetId = 'document-projection/sheet/control-plc';
    diagram.sheets = [
        {
            sheetId: 'document-projection/sheet/power-distribution',
            displayName: 'Power Distribution',
            role: 'power_distribution',
            order: 0,
            subjectSemanticIds: ['component:PSU1', 'component:QF1', 'connection:PSU1.L+->QF1.L+']
        },
        {
            sheetId: 'document-projection/sheet/control-plc',
            displayName: 'Control And PLC Logic',
            role: 'control_logic',
            order: 1,
            subjectSemanticIds: ['component:PLC1', 'component:HMI1']
        },
        {
            sheetId: 'document-projection/sheet/field-wiring-terminal-transition',
            displayName: 'Field Wiring And Terminal Transition',
            role: 'field_wiring',
            order: 2,
            subjectSemanticIds: ['component:XT1']
        }
    ];

    const originalSheets = JSON.parse(JSON.stringify(diagram.sheets));
    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);

    assert.deepEqual(model.sheetViewSelector, {
        activeSheetViewId: 'document-projection/sheet/control-plc',
        hasMultipleSheetViews: true,
        entries: [
            {
                sheetViewId: 'document-projection/sheet/power-distribution',
                displayOrder: 1,
                title: 'Power Distribution',
                role: 'power_distribution',
                subjectCount: 3,
                isActive: false,
                label: '1 - Power Distribution'
            },
            {
                sheetViewId: 'document-projection/sheet/control-plc',
                displayOrder: 2,
                title: 'Control And PLC Logic',
                role: 'control_logic',
                subjectCount: 2,
                isActive: true,
                label: '2 - Control And PLC Logic'
            },
            {
                sheetViewId: 'document-projection/sheet/field-wiring-terminal-transition',
                displayOrder: 3,
                title: 'Field Wiring And Terminal Transition',
                role: 'field_wiring',
                subjectCount: 1,
                isActive: false,
                label: '3 - Field Wiring And Terminal Transition'
            }
        ]
    });
    assert.deepEqual(diagram.sheets, originalSheets);
});

test('omits sheet-view selector when only one sheet view is available', () => {
    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(readyDiagram);

    assert.equal(model.sheetViewSelector, undefined);
});

test('preserves document sheet selector across projection view changes from sheet facts', () => {
    assert.equal(typeof graphWorkbenchModel.resolveVisibleAthenaGraphSheetViewSelector, 'function');

    const documentationDiagram = JSON.parse(JSON.stringify(readyDiagram));
    documentationDiagram.activeViewId = 'documentation';
    documentationDiagram.activeSheetId = 'documentation/sheet/02-control';
    documentationDiagram.sourceFiles = [
        'src/01-main.athena',
        'src/02-library.athena',
    ];
    documentationDiagram.supportedViews = [
        ...readyDiagram.supportedViews,
        {
            viewId: 'documentation',
            displayName: 'Documentation',
            description: 'Governed schematic sheets',
            familyId: 'electrical/documentation',
            ownershipContract: readyDiagram.supportedViews[0].ownershipContract,
        },
    ];
    documentationDiagram.sheets = [
        {
            sheetId: 'documentation/sheet/01-power',
            displayName: 'Power Distribution',
            role: 'power_distribution',
            order: 0,
            subjectSemanticIds: ['component:PSU1'],
        },
        {
            sheetId: 'documentation/sheet/02-control',
            displayName: 'Control Logic',
            role: 'control_logic',
            order: 1,
            subjectSemanticIds: ['component:PLC1', 'component:HMI1'],
        },
        {
            sheetId: 'documentation/sheet/03-field',
            displayName: 'Field Wiring',
            role: 'field_wiring',
            order: 2,
            subjectSemanticIds: ['component:XT1'],
        },
    ];
    const documentationModel = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(documentationDiagram);
    const cabinetModel = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(readyDiagram);

    assert.equal(documentationModel.sheetViewSelector.entries.length, 3);
    assert.equal(documentationModel.sheetViewSelector.entries.length, documentationDiagram.sheets.length);
    assert.notEqual(documentationModel.sheetViewSelector.entries.length, documentationDiagram.sourceFiles.length);
    assert.deepEqual(
        graphWorkbenchModel.resolveVisibleAthenaGraphSheetViewSelector(cabinetModel, documentationModel.sheetViewSelector),
        documentationModel.sheetViewSelector,
    );
});

test('resolves reference marker navigation through target occurrence identity', () => {
    assert.equal(typeof graphWorkbenchModel.resolveAthenaGraphReferenceMarkerNavigation, 'function');

    const diagram = JSON.parse(JSON.stringify(readyDiagram));
    diagram.activeSheetId = 'document-projection/sheet/power-distribution';
    diagram.sheets = [
        {
            sheetId: 'document-projection/sheet/power-distribution',
            displayName: 'Power Distribution',
            role: 'power_distribution',
            order: 0,
            subjectSemanticIds: ['component:PSU1', 'connection:PSU1.L+->PLC1.L+']
        },
        {
            sheetId: 'document-projection/sheet/control-plc',
            displayName: 'Control And PLC Logic',
            role: 'control_logic',
            order: 1,
            subjectSemanticIds: ['component:PLC1', 'connection:PSU1.L+->PLC1.L+']
        }
    ];
    diagram.presentation = {
        canvasWidth: 960,
        canvasHeight: 540,
        primitivePacks: [],
        compositePacks: [],
        occurrences: [],
        connectors: [],
        referenceMarkers: [
            {
                markerId: 'marker:route:power-to-control',
                markerKind: 'continuation',
                relationType: 'route_continuation',
                selectedSheetViewId: 'document-projection/sheet/power-distribution',
                sourceOccurrenceId: 'occurrence:power-route',
                targetOccurrenceId: 'occurrence:control-route',
                sourceIdentity: 'connection:PSU1.L+->PLC1.L+',
                targetIdentity: 'connection:PSU1.L+->PLC1.L+',
                sourceDocumentLocation: {
                    sheetViewId: 'document-projection/sheet/power-distribution',
                    zoneId: 'A1',
                    displayNotation: '1-A1'
                },
                targetDocumentLocation: {
                    sheetViewId: 'document-projection/sheet/control-plc',
                    zoneId: 'B2',
                    displayNotation: '2-B2'
                },
                compactNotation: '2-B2',
                sourceProjectionIds: ['cross-reference:power-to-control']
            }
        ]
    };

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);

    assert.deepEqual(
        graphWorkbenchModel.resolveAthenaGraphReferenceMarkerNavigation(model, 'marker:route:power-to-control'),
        {
            status: 'ready',
            markerId: 'marker:route:power-to-control',
            relationType: 'route_continuation',
            targetSheetViewId: 'document-projection/sheet/control-plc',
            targetOccurrenceId: 'occurrence:control-route',
            targetCanonicalId: 'connection:PSU1.L+->PLC1.L+',
            requiresSheetSwitch: true,
            displayNotation: '2-B2'
        }
    );
});

test('resolves same-view reference marker navigation without switching views', () => {
    const diagram = JSON.parse(JSON.stringify(readyDiagram));
    diagram.activeSheetId = 'document-projection/sheet/control-plc';
    diagram.sheets = [
        {
            sheetId: 'document-projection/sheet/control-plc',
            displayName: 'Control And PLC Logic',
            role: 'control_logic',
            order: 1,
            subjectSemanticIds: ['component:PLC1', 'component:HMI1']
        }
    ];
    diagram.presentation = {
        canvasWidth: 960,
        canvasHeight: 540,
        primitivePacks: [],
        compositePacks: [],
        occurrences: [],
        connectors: [],
        referenceMarkers: [
            {
                markerId: 'marker:repeated:plc',
                markerKind: 'cross_reference',
                relationType: 'repeated_subject',
                selectedSheetViewId: 'document-projection/sheet/control-plc',
                sourceOccurrenceId: 'occurrence:plc-a',
                targetOccurrenceId: 'occurrence:plc-b',
                sourceIdentity: 'component:PLC1',
                targetIdentity: 'component:PLC1',
                sourceDocumentLocation: {
                    sheetViewId: 'document-projection/sheet/control-plc',
                    zoneId: 'A2',
                    displayNotation: '2-A2'
                },
                targetDocumentLocation: {
                    sheetViewId: 'document-projection/sheet/control-plc',
                    zoneId: 'C2',
                    displayNotation: '2-C2'
                },
                compactNotation: '2-C2',
                sourceProjectionIds: ['cross-reference:plc-repeat']
            }
        ]
    };

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);
    const result = graphWorkbenchModel.resolveAthenaGraphReferenceMarkerNavigation(model, 'marker:repeated:plc');

    assert.equal(result.status, 'ready');
    assert.equal(result.requiresSheetSwitch, false);
    assert.equal(result.targetCanonicalId, 'component:PLC1');
});

test('reference marker navigation fails closed when the target marker is missing', () => {
    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(readyDiagram);

    assert.deepEqual(
        graphWorkbenchModel.resolveAthenaGraphReferenceMarkerNavigation(model, 'marker:missing'),
        {
            status: 'missing-marker',
            markerId: 'marker:missing',
            reason: 'No governed reference marker is available for marker:missing.'
        }
    );
});

test('builds document reference inspection from selected canonical identity', () => {
    assert.equal(typeof graphWorkbenchModel.buildAthenaGraphDocumentReferenceInspection, 'function');

    const diagram = JSON.parse(JSON.stringify(readyDiagram));
    diagram.activeSheetId = 'document-projection/sheet/control-plc';
    diagram.presentation = {
        canvasWidth: 960,
        canvasHeight: 540,
        primitivePacks: [],
        compositePacks: [],
        occurrences: [],
        connectors: [],
        referenceMarkers: [
            {
                markerId: 'marker:repeated:plc',
                markerKind: 'cross_reference',
                relationType: 'repeated_subject',
                selectedSheetViewId: 'document-projection/sheet/control-plc',
                sourceOccurrenceId: 'occurrence:plc-a',
                targetOccurrenceId: 'occurrence:plc-b',
                sourceIdentity: 'component:PLC1',
                targetIdentity: 'component:PLC1',
                sourceDocumentLocation: {
                    sheetViewId: 'document-projection/sheet/control-plc',
                    zoneId: 'A2',
                    displayNotation: '2-A2'
                },
                targetDocumentLocation: {
                    sheetViewId: 'document-projection/sheet/control-plc',
                    zoneId: 'C2',
                    displayNotation: '2-C2'
                },
                compactNotation: '2-C2',
                sourceProjectionIds: ['cross-reference:plc-repeat']
            }
        ]
    };

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);

    assert.deepEqual(
        graphWorkbenchModel.buildAthenaGraphDocumentReferenceInspection(model, 'component:PLC1'),
        {
            status: 'ready',
            canonicalIdentity: 'component:PLC1',
            references: [
                {
                    markerId: 'marker:repeated:plc',
                    markerKind: 'cross_reference',
                    relationType: 'repeated_subject',
                    compactNotation: '2-C2',
                    sourceOccurrenceId: 'occurrence:plc-a',
                    targetOccurrenceId: 'occurrence:plc-b',
                    sourceLocation: '2-A2',
                    targetLocation: '2-C2',
                    targetSheetViewId: 'document-projection/sheet/control-plc',
                    sourceProjectionIds: ['cross-reference:plc-repeat']
                }
            ],
            persisted: false
        }
    );
    assert.deepEqual(
        graphWorkbenchModel.buildAthenaGraphDocumentReferenceInspection(model, 'component:missing'),
        {
            status: 'unavailable',
            canonicalIdentity: 'component:missing',
            references: [],
            persisted: false
        }
    );
});

test('prefers Presentation IR occurrences and symbol commands when the diagram includes a governed presentation document', () => {
    const diagram = JSON.parse(JSON.stringify(readyDiagram));
    diagram.presentation = {
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
                    },
                    {
                        primitiveId: 'electrical.label.terminal',
                        displayName: 'Terminal label',
                        viewBoxWidth: 72,
                        viewBoxHeight: 24,
                        commands: [
                            {
                                kind: 'stroke_line',
                                start: { x: 0, y: 12 },
                                end: { x: 14, y: 12 },
                                strokeTokenKey: 'stroke',
                                strokeWidthTokenKey: 'strokeWidth'
                            }
                        ],
                        textSlots: [
                            {
                                slotId: 'terminal-label',
                                origin: { x: 20, y: 16 },
                                tokenKey: 'label'
                            }
                        ],
                        anchors: [
                            {
                                alias: 'terminal',
                                point: { x: 0, y: 12 }
                            }
                        ],
                        tokenDefaults: { stroke: '#202020', strokeWidth: '1.6', label: '#202020' },
                        supportedOrientations: ['horizontal', 'vertical']
                    },
                    {
                        primitiveId: 'electrical.conductor.orthogonal',
                        displayName: 'Orthogonal conductor',
                        viewBoxWidth: 1,
                        viewBoxHeight: 1,
                        commands: [],
                        textSlots: [],
                        anchors: [],
                        tokenDefaults: { stroke: '#202020', strokeWidth: '1.6', label: '#202020' },
                        supportedOrientations: ['horizontal', 'vertical']
                    }
                ]
            }
        ],
        compositePacks: [
            {
                packId: 'electrical-composites/panel-v1',
                displayName: 'Electrical panel composites',
                familyIds: ['electrical/cabinet'],
                composites: [
                    {
                        compositeId: 'electrical.device.switch-panel',
                        displayName: 'Switch device panel',
                        viewBoxWidth: 140,
                        viewBoxHeight: 72,
                        parts: [
                            {
                                partId: 'frame',
                                primitiveId: 'electrical.frame.device-box',
                                bounds: { x: 0, y: 0, width: 140, height: 72 },
                                tokenOverrides: {},
                                orientation: 'horizontal'
                            },
                            {
                                partId: 'contact-mark',
                                primitiveId: 'electrical.mark.contact-open',
                                bounds: { x: 94, y: 22, width: 24, height: 24 },
                                tokenOverrides: {},
                                orientation: 'horizontal'
                            }
                        ],
                        textSlots: [
                            {
                                slotId: 'subject-label',
                                origin: { x: 12, y: 10 },
                                tokenKey: 'label'
                            }
                        ],
                        tokenDefaults: { stroke: '#202020', strokeWidth: '1.6', label: '#202020' },
                        supportedOrientations: ['horizontal', 'vertical']
                    }
                ]
            }
        ],
        occurrences: [
            {
                occurrenceId: 'cabinet/presentation/occurrence/component_PLC1',
                semanticId: 'component:PLC1',
                referenceKind: 'composite',
                compositeId: 'electrical.device.switch-panel',
                bounds: { x: 120, y: 80, width: 260, height: 160 },
                layer: 'device',
                displayLabel: 'PLC1',
                orientation: 'horizontal',
                markerKeys: ['owned-device'],
                textValues: { 'subject-label': 'PLC1' },
                anchorBindings: [
                    {
                        alias: 'right-terminal',
                        anchorId: 'cabinet/projection/label/port_PLC1_out/anchor',
                        portSemanticId: 'port:PLC1.out',
                        ownerSemanticId: 'component:PLC1',
                        sourceLabelId: 'cabinet/projection/label/port_PLC1_out'
                    }
                ],
                tokenOverrides: {},
                sourceProjectionIds: ['cabinet/projection/node/component_PLC1']
            },
            {
                occurrenceId: 'cabinet/presentation/occurrence/port_PLC1_out',
                semanticId: 'port:PLC1.out',
                referenceKind: 'primitive',
                primitiveId: 'electrical.label.terminal',
                bounds: { x: 390, y: 142, width: 60, height: 20 },
                layer: 'label',
                displayLabel: 'OUT',
                orientation: 'horizontal',
                markerKeys: [],
                textValues: { 'terminal-label': 'OUT' },
                anchorBindings: [
                    {
                        alias: 'terminal',
                        anchorId: 'cabinet/projection/label/port_PLC1_out/anchor',
                        portSemanticId: 'port:PLC1.out',
                        ownerSemanticId: 'component:PLC1',
                        sourceLabelId: 'cabinet/projection/label/port_PLC1_out'
                    }
                ],
                tokenOverrides: {},
                sourceProjectionIds: ['cabinet/projection/label/port_PLC1_out']
            },
            {
                occurrenceId: 'cabinet/presentation/occurrence/port_M1_in',
                semanticId: 'port:M1.in',
                referenceKind: 'primitive',
                primitiveId: 'electrical.label.terminal',
                bounds: { x: 628, y: 248, width: 32, height: 20 },
                layer: 'label',
                displayLabel: 'IN',
                orientation: 'horizontal',
                markerKeys: [],
                textValues: { 'terminal-label': 'IN' },
                anchorBindings: [
                    {
                        alias: 'terminal',
                        anchorId: 'cabinet/projection/label/port_M1_in/anchor',
                        portSemanticId: 'port:M1.in',
                        ownerSemanticId: 'component:M1',
                        sourceLabelId: 'cabinet/projection/label/port_M1_in'
                    }
                ],
                tokenOverrides: {},
                sourceProjectionIds: ['cabinet/projection/label/port_M1_in']
            }
        ],
        connectors: [
            {
                occurrenceId: 'cabinet/presentation/connector/connection_PLC1_out_M1_in',
                semanticId: 'connection:PLC1.out->M1.in',
                primitiveId: 'electrical.conductor.orthogonal',
                routePoints: [
                    { x: 380, y: 160 },
                    { x: 540, y: 160 },
                    { x: 540, y: 260 },
                    { x: 720, y: 260 }
                ],
                layer: 'connection',
                sourceAnchorId: 'cabinet/projection/label/port_PLC1_out/anchor',
                targetAnchorId: 'cabinet/projection/label/port_M1_in/anchor',
                sourcePortSemanticId: 'port:PLC1.out',
                targetPortSemanticId: 'port:M1.in',
                markerKeys: [],
                tokenOverrides: {},
                sourceProjectionIds: ['cabinet/projection/connection/connection_PLC1_out_M1_in']
            }
        ]
    };
    diagram.graph.nodes = [];
    diagram.graph.edges = [];

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);

    assert.equal(model.nodes.length, 3);
    assert.equal(model.edges.length, 1);
    assert.equal(
        model.nodes.find(node => node.semanticId === 'component:PLC1')?.presentationParts[1].commands[0].kind,
        'svg_path'
    );
    assert.equal(
        model.nodes.find(node => node.semanticId === 'component:PLC1')?.presentationOccurrence?.sourceProjectionIds[0],
        'cabinet/projection/node/component_PLC1'
    );
    assert.equal(model.edges[0].presentationConnector?.routePoints.length, 4);
    assert.equal(model.edges[0].path, 'M 380 160 L 540 160 L 540 260 L 720 260');
    assert.equal(model.edges[0].sourcePoint.x, 380);
    assert.equal(model.edges[0].sourcePoint.y, 160);
    assert.equal(model.edges[0].targetPoint.x, 720);
    assert.equal(model.edges[0].targetPoint.y, 260);
    assert.notDeepEqual(
        model.edges[0].routePoints,
        [
            { x: 180, y: 90 },
            { x: 180, y: 260 },
            { x: 628, y: 260 }
        ]
    );
    assert.equal(model.edges[0].terminals[0].anchorId, 'cabinet/projection/label/port_PLC1_out/anchor');
    assert.equal(model.edges[0].terminals[0].portSemanticId, 'port:PLC1.out');
    assert.equal(model.edges[0].terminals[1].anchorId, 'cabinet/projection/label/port_M1_in/anchor');
    assert.equal(model.edges[0].terminals[1].portSemanticId, 'port:M1.in');
});

test('renders M25 representation facts as governed electrical symbols without generic fallback', () => {
    const diagram = JSON.parse(JSON.stringify(readyDiagram));
    diagram.presentation = {
        canvasWidth: 1440,
        canvasHeight: 900,
        primitivePacks: [],
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
                        },
                        {
                            kind: 'circle',
                            primitiveId: 'plc-controller:status',
                            center: { x: 20, y: 24 },
                            radius: 6
                        }
                    ],
                    terminals: [
                        {
                            terminalId: 'terminal:PLC1:Q1.0',
                            role: 'digital_output',
                            localPoint: { x: 80, y: 24 },
                            side: 'right',
                            notation: {
                                marker: 'circle',
                                number: 'Q1.0'
                            }
                        }
                    ],
                    labelAnchors: [
                        {
                            anchorId: 'representation:PLC1@schematic-sheet:component:PLC1:device_tag',
                            role: 'device_tag',
                            point: { x: 0, y: -12 }
                        }
                    ]
                },
                terminals: [
                    {
                        presentationTerminalId: 'terminal:PLC1:Q1.0',
                        subjectId: 'component:PLC1',
                        occurrenceId: 'representation:PLC1@schematic-sheet',
                        portId: 'Q1.0',
                        physicalTerminalId: 'PLC1:Q1.0',
                        side: 'right',
                        routeAnchor: {
                            anchorId: 'anchor:PLC1:Q1.0',
                            point: { x: 80, y: 24 }
                        },
                        notation: {
                            marker: 'circle',
                            number: 'Q1.0'
                        }
                    }
                ],
                labels: [
                    {
                        labelId: 'label:PLC1:device-tag',
                        subjectId: 'component:PLC1',
                        occurrenceId: 'representation:PLC1@schematic-sheet',
                        role: 'device_tag',
                        value: 'PLC1',
                        anchor: {
                            anchorId: 'representation:PLC1@schematic-sheet:component:PLC1:device_tag',
                            role: 'device_tag',
                            point: { x: 0, y: -12 }
                        }
                    }
                ]
            }
        ]
    };
    diagram.presentation.occurrences = [];

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);
    const node = model.nodes.find(candidate => candidate.semanticId === 'component:PLC1');

    assert.equal(node?.renderVariant, 'electrical-device');
    assert.equal(node?.presentationRepresentation?.representationId, 'athena-industrial-control-v0:plc-controller');
    assert.equal(node?.presentationRepresentation?.fallback, false);
    assert.equal(node?.presentationParts.length, 1);
    assert.deepEqual(
        node?.presentationParts[0].commands.map(command => command.kind),
        ['stroke_rectangle', 'stroke_line', 'circle']
    );
    assert.equal(node?.position.x, 300);
    assert.equal(node?.position.y, 136);
    assert.equal(node?.size.width, 80);
    assert.equal(node?.size.height, 48);
    assert.equal(node?.presentationParts[0].commands[0].bounds.width, 80);
    assert.equal(node?.presentationParts[0].commands[1].start.x, 360);
    assert.equal(node?.presentationParts[0].commands[1].end.x, 380);
    assert.equal(node?.presentationParts[0].commands[2].center.x, 320);
    assert.deepEqual(node?.presentationTerminals, [
        {
            terminalId: 'terminal:PLC1:Q1.0',
            subjectId: 'component:PLC1',
            occurrenceId: 'representation:PLC1@schematic-sheet',
            portId: 'Q1.0',
            physicalTerminalId: 'PLC1:Q1.0',
            side: 'right',
            marker: 'circle',
            number: 'Q1.0',
            point: { x: 380, y: 160 },
            anchorId: 'anchor:PLC1:Q1.0'
        }
    ]);
    assert.ok(model.sceneBounds.maxX >= 418);
    assert.deepEqual(node?.presentationLabels, [
        {
            labelId: 'label:PLC1:device-tag',
            subjectId: 'component:PLC1',
            occurrenceId: 'representation:PLC1@schematic-sheet',
            role: 'device_tag',
            value: 'PLC1',
            point: { x: 300, y: 124 },
            anchorId: 'representation:PLC1@schematic-sheet:component:PLC1:device_tag'
        }
    ]);
    assert.ok(node?.presentationLabels[0].point.y < model.edges[0].routePoints[0].y);

    assert.deepEqual(
        graphWorkbenchModel.buildAthenaGraphRepresentationInspection(model, 'component:PLC1'),
        {
            status: 'ready',
            subjectId: 'component:PLC1',
            occurrenceId: 'representation:PLC1@schematic-sheet',
            representationId: 'athena-industrial-control-v0:plc-controller',
            symbolFamilyId: 'plc-controller',
            fallback: false,
            terminals: [
                {
                    terminalId: 'terminal:PLC1:Q1.0',
                    portId: 'Q1.0',
                    physicalTerminalId: 'PLC1:Q1.0',
                    anchorId: 'anchor:PLC1:Q1.0',
                    side: 'right',
                    number: 'Q1.0',
                    marker: 'circle'
                }
            ],
            labels: [
                {
                    labelId: 'label:PLC1:device-tag',
                    role: 'device_tag',
                    value: 'PLC1',
                    anchorId: 'representation:PLC1@schematic-sheet:component:PLC1:device_tag'
                }
            ],
            selectedTerminal: undefined,
            selectedLabel: undefined,
            persisted: false
        }
    );
    assert.equal(
        graphWorkbenchModel.buildAthenaGraphRepresentationInspection(model, 'terminal:PLC1:Q1.0').selectedTerminal?.portId,
        'Q1.0'
    );
    assert.equal(
        graphWorkbenchModel.buildAthenaGraphRepresentationInspection(model, 'label:PLC1:device-tag').selectedLabel?.role,
        'device_tag'
    );
});

test('keeps M25 representation facts when Presentation IR occurrences drive sheet nodes', () => {
    const diagram = JSON.parse(JSON.stringify(readyDiagram));
    diagram.presentation = {
        canvasWidth: 1440,
        canvasHeight: 900,
        primitivePacks: [],
        compositePacks: [],
        occurrences: [
            {
                occurrenceId: 'presentation:component:PLC1',
                semanticId: 'component:PLC1',
                primitiveId: 'electrical.device.plc',
                bounds: { x: 120, y: 80, width: 260, height: 160 },
                displayLabel: 'PLC1',
                layer: 'component',
                markerKeys: [],
                sourceProjectionIds: ['cabinet/projection/node/component_PLC1'],
                parts: [
                    {
                        partId: 'presentation:component:PLC1:body',
                        kind: 'component',
                        bounds: { x: 120, y: 80, width: 260, height: 160 },
                        commands: [],
                        textSlots: []
                    }
                ]
            }
        ],
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
                        }
                    ],
                    terminals: [
                        {
                            terminalId: 'terminal:PLC1:Q1.0',
                            role: 'digital_output',
                            localPoint: { x: 80, y: 24 },
                            side: 'right',
                            notation: {
                                marker: 'circle',
                                number: 'Q1.0'
                            }
                        }
                    ],
                    labelAnchors: [
                        {
                            anchorId: 'representation:PLC1@schematic-sheet:component:PLC1:device_tag',
                            role: 'device_tag',
                            point: { x: 0, y: -12 }
                        }
                    ]
                },
                terminals: [
                    {
                        presentationTerminalId: 'terminal:PLC1:Q1.0',
                        subjectId: 'component:PLC1',
                        occurrenceId: 'representation:PLC1@schematic-sheet',
                        portId: 'Q1.0',
                        physicalTerminalId: 'PLC1:Q1.0',
                        side: 'right',
                        routeAnchor: {
                            anchorId: 'anchor:PLC1:Q1.0',
                            point: { x: 80, y: 24 }
                        },
                        notation: {
                            marker: 'circle',
                            number: 'Q1.0'
                        }
                    }
                ],
                labels: [
                    {
                        labelId: 'label:PLC1:device-tag',
                        subjectId: 'component:PLC1',
                        occurrenceId: 'representation:PLC1@schematic-sheet',
                        role: 'device_tag',
                        value: 'PLC1',
                        anchor: {
                            anchorId: 'representation:PLC1@schematic-sheet:component:PLC1:device_tag',
                            role: 'device_tag',
                            point: { x: 0, y: -12 }
                        }
                    }
                ]
            }
        ]
    };

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);
    const node = model.nodes.find(candidate => candidate.semanticId === 'component:PLC1');

    assert.equal(node?.id, 'presentation:component:PLC1');
    assert.equal(node?.renderVariant, 'electrical-device');
    assert.equal(node?.presentationRepresentation?.representationId, 'athena-industrial-control-v0:plc-controller');
    assert.equal(node?.position.x, 300);
    assert.equal(node?.position.y, 136);
    assert.equal(node?.size.width, 80);
    assert.equal(node?.size.height, 48);
    assert.equal(node?.presentationTerminals[0]?.number, 'Q1.0');
    assert.equal(node?.presentationTerminals[0]?.point.x, 380);
    assert.equal(node?.presentationLabels[0]?.role, 'device_tag');
    assert.equal(node?.presentationLabels[0]?.point.y, 124);
    assert.equal(
        graphWorkbenchModel.buildAthenaGraphRepresentationInspection(model, 'component:PLC1').status,
        'ready'
    );
});

test('filters off-sheet duplicate presentation occurrences from the active sheet model', () => {
    const diagram = JSON.parse(JSON.stringify(readyDiagram));
    diagram.graph.canvas = { width: 1680, height: 1188 };
    diagram.presentation = {
        canvasWidth: 1680,
        canvasHeight: 1188,
        primitivePacks: [],
        compositePacks: [],
        occurrences: [
            {
                occurrenceId: 'documentation/presentation/occurrence/MainBreakerQF1/active',
                semanticId: 'component:MainBreakerQF1',
                referenceKind: 'composite',
                compositeId: 'electrical.device.protection',
                bounds: { x: 300, y: 60, width: 80, height: 48 },
                displayLabel: 'MainBreakerQF1',
                layer: 'device',
                markerKeys: [],
                sourceProjectionIds: ['documentation/projection/node/component_MainBreakerQF1']
            },
            {
                occurrenceId: 'documentation/presentation/occurrence/MainBreakerQF1/off-sheet',
                semanticId: 'component:MainBreakerQF1',
                referenceKind: 'composite',
                compositeId: 'electrical.device.protection',
                bounds: { x: 2200, y: 60, width: 80, height: 48 },
                displayLabel: 'MainBreakerQF1',
                layer: 'device',
                markerKeys: [],
                sourceProjectionIds: ['documentation/projection/node/component_MainBreakerQF1/off-sheet']
            },
            {
                occurrenceId: 'documentation/presentation/occurrence/MainBreakerQF1/partial-overflow',
                semanticId: 'component:MainBreakerQF1',
                referenceKind: 'composite',
                compositeId: 'electrical.device.protection',
                bounds: { x: 1640, y: 60, width: 80, height: 48 },
                displayLabel: 'MainBreakerQF1',
                layer: 'device',
                markerKeys: [],
                sourceProjectionIds: ['documentation/projection/node/component_MainBreakerQF1/partial-overflow']
            }
        ],
        connectors: [],
        representationFacts: []
    };

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);
    const breakerNodes = model.nodes.filter(node => node.semanticId === 'component:MainBreakerQF1');

    assert.deepEqual(
        breakerNodes.map(node => node.id),
        [
            'documentation/presentation/occurrence/MainBreakerQF1/active',
            'documentation/presentation/occurrence/MainBreakerQF1/partial-overflow'
        ]
    );
    assert.equal(breakerNodes[0].position.x, 300);
    assert.equal(breakerNodes[1].position.x, 1640);
    assert.ok(!model.nodes.some(node => node.id === 'documentation/presentation/occurrence/MainBreakerQF1/off-sheet'));
    assert.equal(model.sceneBounds.maxX, 1720);
    assert.ok(model.sceneBounds.maxX < 2200);
});

test('builds route inspection from governed connector facts without canvas persistence', () => {
    const diagram = JSON.parse(JSON.stringify(readyDiagram));
    diagram.presentation = {
        canvasWidth: 960,
        canvasHeight: 540,
        primitivePacks: [],
        compositePacks: [],
        occurrences: [],
        connectors: [
            {
                occurrenceId: 'route:PLC1.out:M1.in',
                semanticId: 'connection:PLC1.out->M1.in',
                primitiveId: 'electrical.conductor.orthogonal',
                routePoints: [
                    { x: 380, y: 160 },
                    { x: 540, y: 160 },
                    { x: 540, y: 260 },
                    { x: 720, y: 260 }
                ],
                layer: 'connection',
                sourceAnchorId: 'cabinet/projection/label/port_PLC1_out/anchor',
                targetAnchorId: 'cabinet/projection/label/port_M1_in/anchor',
                sourcePortSemanticId: 'port:PLC1.out',
                targetPortSemanticId: 'port:M1.in',
                markerKeys: [],
                tokenOverrides: {
                    routeQuality: 'SATISFIED',
                    routeLane: '0',
                    routeSegmentCount: '3',
                    routeLabels: 'M1.IN'
                },
                sourceProjectionIds: []
            }
        ]
    };
    diagram.graph.edges = [];

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);
    const inspection = graphWorkbenchModel.buildAthenaGraphRouteInspection(model, 'connection:PLC1.out->M1.in');

    assert.deepEqual(inspection, {
        status: 'ready',
        connectionId: 'connection:PLC1.out->M1.in',
        sourcePortSemanticId: 'port:PLC1.out',
        targetPortSemanticId: 'port:M1.in',
        routeQuality: 'SATISFIED',
        policySummary: 'm24:route-fact:SATISFIED:3-segment',
        labels: ['M1.IN'],
        persisted: false
    });
    assert.equal(Object.hasOwn(inspection, 'routePoints'), false);
    assert.equal(Object.hasOwn(inspection, 'canvasCoordinates'), false);
    assert.equal(graphWorkbenchModel.buildAthenaGraphRouteInspection(model, 'connection:missing').status, 'unavailable');
});

test('keeps verbose semantic route labels selection-only to avoid crowding the sheet canvas', () => {
    const diagram = JSON.parse(JSON.stringify(readyDiagram));
    diagram.presentation = {
        canvasWidth: 960,
        canvasHeight: 540,
        primitivePacks: [],
        compositePacks: [],
        occurrences: [],
        connectors: [
            {
                occurrenceId: 'route:ControllerPLC3.hmi:OperatorHMI3.status',
                semanticId: 'connection:ControllerPLC3.hmi->OperatorHMI3.status',
                primitiveId: 'electrical.conductor.orthogonal',
                routePoints: [
                    { x: 160, y: 200 },
                    { x: 320, y: 200 },
                    { x: 320, y: 280 },
                    { x: 480, y: 280 }
                ],
                layer: 'connection',
                markerKeys: [],
                tokenOverrides: {
                    routeQuality: 'SATISFIED',
                    routeLabels: 'ControllerPLC3.hmi -> OperatorHMI3.status|HMI.OK'
                },
                sourceProjectionIds: []
            }
        ]
    };
    diagram.graph.edges = [];

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);

    assert.equal(model.edges[0].routeLabels[0].text, 'ControllerPLC3.hmi -> OperatorHMI3.status');
    assert.equal(model.edges[0].routeLabels[0].canvasDisplay, 'selection');
    assert.equal(model.edges[0].routeLabels[1].text, 'HMI.OK');
    assert.equal(model.edges[0].routeLabels[1].canvasDisplay, 'always');
    assert.deepEqual(
        graphWorkbenchModel.buildAthenaGraphRouteInspection(model, 'connection:ControllerPLC3.hmi->OperatorHMI3.status').labels,
        ['ControllerPLC3.hmi -> OperatorHMI3.status', 'HMI.OK']
    );
});

test('marks deliberate crossings between governed presentation route facts', () => {
    const diagram = JSON.parse(JSON.stringify(readyDiagram));
    diagram.presentation = {
        canvasWidth: 960,
        canvasHeight: 540,
        primitivePacks: [],
        compositePacks: [],
        occurrences: [],
        connectors: [
            {
                occurrenceId: 'route:crossing:a',
                semanticId: 'connection:A.out->B.in',
                primitiveId: 'electrical.conductor.orthogonal',
                routePoints: [
                    { x: 100, y: 200 },
                    { x: 300, y: 200 }
                ],
                layer: 'connection',
                markerKeys: [],
                tokenOverrides: { routeLabels: 'A1' },
                sourceProjectionIds: []
            },
            {
                occurrenceId: 'route:crossing:b',
                semanticId: 'connection:C.out->D.in',
                primitiveId: 'electrical.conductor.orthogonal',
                routePoints: [
                    { x: 200, y: 100 },
                    { x: 200, y: 300 }
                ],
                layer: 'connection',
                markerKeys: [],
                tokenOverrides: { routeLabels: 'B1' },
                sourceProjectionIds: []
            }
        ]
    };
    diagram.graph.edges = [];

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(diagram);

    assert.deepEqual(model.edges[0].crossingMarkerPoints, [{ x: 200, y: 200 }]);
    assert.deepEqual(model.edges[1].crossingMarkerPoints, [{ x: 200, y: 200 }]);
    assert.equal(model.edges[0].routeLabels[0].text, 'A1');
    assert.deepEqual(model.edges[0].routeLabels[0].point, { x: 200, y: 184 });
    assert.equal(model.edges[1].routeLabels[0].text, 'B1');
    assert.deepEqual(model.edges[1].routeLabels[0].point, { x: 216, y: 200 });
});

test('keeps schematic sheet chrome and identity mapping deterministic across repeated builds', () => {
    const first = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(readyDiagram);
    const second = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(readyDiagram);

    assert.deepEqual(second.sheetChrome, first.sheetChrome);
    assert.deepEqual(
        second.nodes.map(node => ({
            id: node.id,
            semanticId: node.semanticId,
            renderVariant: node.renderVariant,
            anchorIds: node.electricalAnchors.map(anchor => anchor.anchorId)
        })),
        first.nodes.map(node => ({
            id: node.id,
            semanticId: node.semanticId,
            renderVariant: node.renderVariant,
            anchorIds: node.electricalAnchors.map(anchor => anchor.anchorId)
        }))
    );
    assert.deepEqual(
        second.edges.map(edge => ({
            id: edge.id,
            semanticId: edge.semanticId,
            path: edge.path,
            terminalAnchorIds: edge.terminals.map(terminal => terminal.anchorId)
        })),
        first.edges.map(edge => ({
            id: edge.id,
            semanticId: edge.semanticId,
            path: edge.path,
            terminalAnchorIds: edge.terminals.map(terminal => terminal.anchorId)
        }))
    );
});

test('builds an inspectable unavailable state without inventing fallback graph truth', () => {
    assert.equal(typeof graphWorkbenchModel.buildAthenaGraphWorkbenchModel, 'function');

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(unavailableDiagram);

    assert.equal(model.statusTone, 'warning');
    assert.equal(model.headerTitle, 'FactoryLine');
    assert.equal(model.viewLabel, 'cabinet');
    assert.equal(model.sheetCount, 0);
    assert.equal(model.crossReferenceCount, 0);
    assert.equal(model.svgViewBox, '0 0 960 540');
    assert.equal(model.metrics.nodeCount, 0);
    assert.equal(model.metrics.edgeCount, 0);
    assert.equal(model.emptyState?.title, 'Projection unavailable');
    assert.match(model.emptyState?.message ?? '', /No supported projection views are available/);
});

test('fits the graph viewport around scene bounds with padding', () => {
    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(readyDiagram);
    const transform = graphWorkbenchModel.fitAthenaGraphViewport(model.sceneBounds, {
        width: 1200,
        height: 700
    });

    assert.equal(typeof transform.zoom, 'number');
    assert.ok(transform.zoom > 0);
    assert.ok(Number.isFinite(transform.offsetX));
    assert.ok(Number.isFinite(transform.offsetY));
    assert.ok(transform.zoom > 1);
    assert.ok(transform.offsetX < 200);
});

test('fits a wide dense documentation graph around the scene center', () => {
    const transform = graphWorkbenchModel.fitAthenaGraphViewport({
        minX: 120,
        minY: 80,
        maxX: 2840,
        maxY: 1240,
        width: 2720,
        height: 1160,
        centerX: 1480,
        centerY: 660
    }, {
        width: 1600,
        height: 900
    });

    assert.ok(transform.zoom > 0.2);
    assert.ok(transform.zoom < 1);
    assert.equal(Math.round((1480 * transform.zoom) + transform.offsetX), 800);
    assert.equal(Math.round((660 * transform.zoom) + transform.offsetY), 450);
});

test('zooms around a screen point without losing focus anchor', () => {
    const initial = {
        zoom: 1,
        offsetX: 120,
        offsetY: 80
    };

    const transform = graphWorkbenchModel.zoomAthenaGraphViewportAtPoint(initial, {
        x: 400,
        y: 240
    }, 2);

    assert.equal(transform.zoom, 2);
    assert.equal(transform.offsetX, -160);
    assert.equal(transform.offsetY, -80);
});

test('preserves the same world center when the viewport resizes in manual mode', () => {
    const resized = graphWorkbenchModel.resizeAthenaGraphViewport({
        zoom: 1.5,
        offsetX: -180,
        offsetY: -120
    }, {
        width: 1000,
        height: 600
    }, {
        width: 1400,
        height: 900
    });

    assert.equal(Math.round(((1000 / 2) - (-180)) / 1.5), Math.round(((1400 / 2) - resized.offsetX) / 1.5));
    assert.equal(Math.round(((600 / 2) - (-120)) / 1.5), Math.round(((900 / 2) - resized.offsetY) / 1.5));
});

test('keeps an offscreen node selection centered without drifting on repeated focus checks', () => {
    const nodes = [
        {
            semanticId: 'component:PLC1',
            position: { x: 1320, y: 760 },
            size: { width: 140, height: 90 }
        }
    ];
    const edges = [];
    const viewport = {
        width: 960,
        height: 540
    };
    const initial = {
        zoom: 1,
        offsetX: 0,
        offsetY: 0
    };

    const once = graphWorkbenchModel.keepAthenaGraphViewportFocusedOnSelection(
        initial,
        viewport,
        nodes,
        edges,
        'component:PLC1'
    );
    const twice = graphWorkbenchModel.keepAthenaGraphViewportFocusedOnSelection(
        once,
        viewport,
        nodes,
        edges,
        'component:PLC1'
    );

    assert.notDeepEqual(once, initial);
    assert.deepEqual(twice, once);
    assert.equal(once.zoom, 1);
    assert.equal(Math.round((1320 + 70) + once.offsetX), Math.round(viewport.width / 2));
    assert.equal(Math.round((760 + 45) + once.offsetY), Math.round(viewport.height / 2));
});

test('keeps a connection selection centered from its route points', () => {
    const transform = {
        zoom: 1.25,
        offsetX: -60,
        offsetY: -40
    };
    const viewport = {
        width: 1200,
        height: 800
    };
    const nodes = [];
    const edges = [
        {
            semanticId: 'connection:PLC1.out->M1.in',
            routePoints: [
                { x: 1640, y: 420 },
                { x: 1760, y: 480 },
                { x: 1880, y: 540 }
            ]
        }
    ];

    const focused = graphWorkbenchModel.keepAthenaGraphViewportFocusedOnSelection(
        transform,
        viewport,
        nodes,
        edges,
        'connection:PLC1.out->M1.in'
    );

    assert.equal(focused.zoom, transform.zoom);
    assert.notDeepEqual(focused, transform);
    assert.equal(Math.round(((1640 + 1880) / 2) * focused.zoom + focused.offsetX), Math.round(viewport.width / 2));
    assert.equal(Math.round(((420 + 540) / 2) * focused.zoom + focused.offsetY), Math.round(viewport.height / 2));
});

test('builds a safe model even when optional diagram arrays are missing at runtime', () => {
    const sparseDiagram = {
        kind: 'athena-glsp-diagram',
        projectName: 'SparseFactory',
        semanticPath: 'frontend -> LSP -> runtime/compiler',
        activeViewId: 'cabinet',
        status: 'ready',
        graph: {
            id: 'SparseFactory:cabinet',
            type: 'graph',
            canvas: {
                width: 0,
                height: 0
            }
        }
    };

    const model = graphWorkbenchModel.buildAthenaGraphWorkbenchModel(sparseDiagram);

    assert.equal(model.canvas.width, 960);
    assert.equal(model.canvas.height, 540);
    assert.deepEqual(model.activeRenderContributions, []);
    assert.deepEqual(model.supportedViews, []);
    assert.deepEqual(model.nodes, []);
    assert.deepEqual(model.edges, []);
    assert.equal(model.emptyState?.title, 'Projection is empty');
});
