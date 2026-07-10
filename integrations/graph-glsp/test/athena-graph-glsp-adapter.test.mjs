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
        systemName: 'FactoryLine',
        canvasWidth: 1440,
        canvasHeight: 900,
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
                            canvasTint: 'rgba(22, 18, 12, 0.92)'
                        }
                    }
                ]
            }
        ],
        components: [
            {
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
                semanticId: 'connection:PLC1.out->M1.in',
                x1: 380,
                y1: 160,
                x2: 720,
                y2: 160
            }
        ],
        labels: [
            {
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
    assert.deepEqual(diagram.supportedViews[0].ownershipContract, readyProjectionSession.supportedViews[0].ownershipContract);
    assert.equal(diagram.graph.type, 'graph');
    assert.equal(diagram.graph.nodes.length, 2);
    assert.equal(diagram.graph.edges.length, 1);
    assert.deepEqual(diagram.graph.nodes[0], {
        id: 'component:PLC1',
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
        id: 'port:PLC1.out',
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
        id: 'connection:PLC1.out->M1.in',
        type: 'edge',
        sourcePoint: {
            x: 380,
            y: 160
        },
        targetPoint: {
            x: 720,
            y: 160
        }
    });
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
    assert.deepEqual(diagram.supportedViews[1].ownershipContract, {
        interactivity: 'inspect_only',
        displayScopes: [],
        semanticCommandIds: [],
        projectionCommandIds: [],
        transientInteractionKinds: [],
        persistedProjectionMetadataKeys: []
    });
});
