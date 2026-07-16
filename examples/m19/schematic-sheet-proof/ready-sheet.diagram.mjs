export default {
    kind: 'athena-glsp-diagram',
    projectName: 'M19SchematicProof',
    semanticPath: 'frontend -> LSP -> runtime/compiler',
    activeViewId: 'schematic-sheet',
    status: 'ready',
    activeRenderContributions: [
        {
            pluginId: 'com.engineeringood.athena.domain.electrical-runtime',
            contributionId: 'electrical-runtime.render.schematic-sheet',
            displayName: 'Electrical schematic sheet rendering intent',
            description: 'Publishes schematic-sheet visual intent without taking renderer ownership.',
            rendererTarget: 'graph-workbench',
            surfaceMappings: [
                {
                    surface: 'canvas',
                    tokens: {
                        canvasTint: 'var(--athena-graph-wiring-canvas-tint)',
                        gridMajor: 'var(--athena-graph-wiring-grid-major)',
                        gridMinor: 'var(--athena-graph-wiring-grid-minor)'
                    }
                },
                {
                    surface: 'node',
                    tokens: {
                        fill: 'var(--athena-graph-wiring-node-fill)',
                        stroke: 'var(--athena-graph-wiring-node-stroke)',
                        label: 'var(--athena-graph-wiring-node-label)',
                        meta: 'var(--athena-graph-wiring-node-meta)'
                    }
                },
                {
                    surface: 'edge',
                    tokens: {
                        stroke: 'var(--athena-graph-wiring-edge-stroke)'
                    }
                }
            ]
        }
    ],
    supportedViews: [
        {
            viewId: 'schematic-sheet',
            displayName: 'Schematic Sheet',
            description: 'M19 schematic sheet projection',
            familyId: 'electrical/schematic',
            ownershipContract: {
                interactivity: 'read-only',
                displayScopes: ['devices', 'ports', 'connections', 'sheet-publication'],
                semanticCommandIds: [],
                projectionCommandIds: [],
                transientInteractionKinds: ['navigate-view', 'inspect-selection'],
                persistedProjectionMetadataKeys: []
            }
        }
    ],
    governedCommands: [],
    activeSheetId: 'schematic/sheet/01-main',
    sheets: [
        {
            sheetId: 'schematic/sheet/01-main',
            displayName: '24V Control Power',
            order: 0,
            subjectSemanticIds: [
                'component:PSU1',
                'port:PSU1.plus',
                'port:PLC1.power',
                'connection:PSU1.plus->PLC1.power'
            ]
        }
    ],
    notationPack: {
        packId: 'electrical-notation/schematic/default-v1',
        displayName: 'Electrical Schematic Default',
        subjects: [
            {
                semanticId: 'component:PSU1',
                symbolKey: 'device.schematic.power-supply',
                labelPolicy: 'subject_label',
                markerKeys: ['source-device']
            },
            {
                semanticId: 'port:PSU1.plus',
                symbolKey: 'port.schematic.terminal',
                labelPolicy: 'terminal_label',
                markerKeys: []
            },
            {
                semanticId: 'port:PLC1.power',
                symbolKey: 'port.schematic.terminal',
                labelPolicy: 'terminal_label',
                markerKeys: []
            }
        ]
    },
    crossReferences: [
        {
            semanticId: 'component:PSU1',
            kind: 'repeated_reference',
            sheetIds: ['schematic/sheet/01-main', 'schematic/sheet/02-io'],
            occurrenceIds: [
                'schematic/projection/node/component_PSU1',
                'schematic/projection/node/component_PSU1_reference'
            ]
        }
    ],
    electricalAnchors: [
        {
            anchorId: 'schematic/projection/label/port_PSU1_plus/anchor',
            portSemanticId: 'port:PSU1.plus',
            ownerSemanticId: 'component:PSU1',
            nodeId: 'schematic/projection/node/component_PSU1',
            labelId: 'schematic/projection/label/port_PSU1_plus',
            x: 360,
            y: 180,
            side: 'right'
        },
        {
            anchorId: 'schematic/projection/label/port_PLC1_power/anchor',
            portSemanticId: 'port:PLC1.power',
            ownerSemanticId: 'component:PLC1',
            nodeId: 'schematic/projection/node/component_PLC1',
            labelId: 'schematic/projection/label/port_PLC1_power',
            x: 720,
            y: 300,
            side: 'left'
        }
    ],
    electricalConnectionEndpoints: [
        {
            endpointId: 'schematic/projection/connection/connection_PSU1_plus_PLC1_power/endpoint/source',
            projectionConnectionId: 'schematic/projection/connection/connection_PSU1_plus_PLC1_power',
            connectionSemanticId: 'connection:PSU1.plus->PLC1.power',
            endpointRole: 'source',
            portSemanticId: 'port:PSU1.plus',
            anchorId: 'schematic/projection/label/port_PSU1_plus/anchor'
        },
        {
            endpointId: 'schematic/projection/connection/connection_PSU1_plus_PLC1_power/endpoint/target',
            projectionConnectionId: 'schematic/projection/connection/connection_PSU1_plus_PLC1_power',
            connectionSemanticId: 'connection:PSU1.plus->PLC1.power',
            endpointRole: 'target',
            portSemanticId: 'port:PLC1.power',
            anchorId: 'schematic/projection/label/port_PLC1_power/anchor'
        }
    ],
    electricalRoutingCorridors: [
        {
            corridorId: 'schematic/projection/connection/connection_PSU1_plus_PLC1_power/corridor',
            projectionConnectionId: 'schematic/projection/connection/connection_PSU1_plus_PLC1_power',
            connectionSemanticId: 'connection:PSU1.plus->PLC1.power',
            sourceAnchorId: 'schematic/projection/label/port_PSU1_plus/anchor',
            targetAnchorId: 'schematic/projection/label/port_PLC1_power/anchor',
            routingStyle: 'orthogonal',
            preferredBendPoints: [
                { x: 540, y: 180 },
                { x: 540, y: 300 }
            ]
        }
    ],
    diagnostics: [],
    graph: {
        id: 'M19SchematicProof:schematic-sheet',
        type: 'graph',
        canvas: {
            width: 1440,
            height: 900
        },
        nodes: [
            {
                id: 'schematic/projection/node/component_PSU1',
                semanticId: 'component:PSU1',
                type: 'node',
                kind: 'component',
                label: 'PSU1',
                position: { x: 120, y: 100 },
                size: { width: 240, height: 160 }
            },
            {
                id: 'schematic/projection/label/port_PSU1_plus',
                semanticId: 'port:PSU1.plus',
                type: 'node',
                kind: 'label',
                label: '+24V',
                position: { x: 384, y: 168 },
                size: { width: 56, height: 22 }
            },
            {
                id: 'schematic/projection/label/port_PLC1_power',
                semanticId: 'port:PLC1.power',
                type: 'node',
                kind: 'label',
                label: 'L+',
                position: { x: 640, y: 288 },
                size: { width: 42, height: 22 }
            }
        ],
        edges: [
            {
                id: 'schematic/projection/connection/connection_PSU1_plus_PLC1_power',
                semanticId: 'connection:PSU1.plus->PLC1.power',
                type: 'edge',
                sourcePoint: { x: 360, y: 180 },
                targetPoint: { x: 720, y: 300 },
                routingStyle: 'orthogonal',
                bendPoints: [
                    { x: 540, y: 180 },
                    { x: 540, y: 300 }
                ],
                sourceAnchorId: 'schematic/projection/label/port_PSU1_plus/anchor',
                targetAnchorId: 'schematic/projection/label/port_PLC1_power/anchor',
                sourcePortSemanticId: 'port:PSU1.plus',
                targetPortSemanticId: 'port:PLC1.power'
            }
        ]
    }
};
