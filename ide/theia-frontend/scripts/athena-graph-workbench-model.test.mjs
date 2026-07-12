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
                        canvasTint: 'rgba(22, 18, 12, 0.92)'
                    }
                },
                {
                    surface: 'node',
                    tokens: {
                        fill: 'rgba(52, 38, 21, 0.88)'
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
                label: 'PLC1',
                position: {
                    x: 120,
                    y: 80
                },
                size: {
                    width: 260,
                    height: 160
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
                    y: 160
                }
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
    assert.equal(model.activeSheetId, 'cabinet/sheet/01-main');
    assert.equal(model.sheetCount, 1);
    assert.equal(model.notationPackId, 'electrical-notation/cabinet/default-v1');
    assert.equal(model.crossReferenceCount, 1);
    assert.equal(model.svgViewBox, '0 0 1440 900');
    assert.equal(model.metrics.nodeCount, 1);
    assert.equal(model.metrics.edgeCount, 1);
    assert.equal(model.sceneBounds.minX, 120);
    assert.equal(model.sceneBounds.minY, 80);
    assert.equal(model.sceneBounds.maxX, 720);
    assert.equal(model.sceneBounds.maxY, 240);
    assert.equal(model.surfaceTokens.canvas.canvasTint, 'rgba(22, 18, 12, 0.92)');
    assert.equal(model.surfaceTokens.node.fill, 'rgba(52, 38, 21, 0.88)');
    assert.equal(model.emptyState, undefined);
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
