import assert from 'node:assert/strict';
import test from 'node:test';

const { buildAthenaGraphWorkbenchModel } = await import('../lib/browser/athena-graph-workbench-model.js');

test('Control Drawing consumes compiler-owned Graphic Primitive occurrences without graph fallback', () => {
    const diagram = {
        kind: 'athena-glsp-diagram',
        projectName: 'M34ProfessionalControlDrawing',
        semanticPath: 'examples/m34/professional-control-drawing/src/com/engineeringood/m34/professional/01-control-drawing.athena',
        activeViewId: 'schematic',
        status: 'ready',
        activeRenderContributions: [],
        supportedViews: [{
            viewId: 'schematic',
            displayName: 'Schematic',
            description: 'Semantic schematic projection',
            familyId: 'electrical/schematic',
            ownershipContract: {},
        }],
        governedCommands: [],
        sheets: [],
        crossReferences: [],
        electricalAnchors: [],
        electricalConnectionEndpoints: [],
        electricalRoutingCorridors: [],
        diagnostics: [],
        presentation: {
            canvasWidth: 1050,
            canvasHeight: 720,
            primitivePacks: [],
            compositePacks: [],
            occurrences: [],
            connectors: [],
            representationFacts: [],
            graphicOccurrences: [{
                occurrenceId: 'drawing:PowerSourceG34',
                semanticSubjectId: 'component:PowerSourceG34',
                physicalComponentId: 'component:PowerSourceG34',
                bounds: { x: 92, y: 74, width: 48, height: 44 },
                orientation: 'horizontal',
                deviceLabel: 'PowerSourceG34',
                modelLabel: '400V-3PH-N-PE',
                packageId: 'com.engineeringood.m34.control.power',
                definitionId: 'iec.source.element',
                bindingRuleId: 'IecPowerSource',
                graphic: {
                    bounds: { x: 92, y: 74, width: 48, height: 44 },
                    primitives: [{
                        primitiveId: 'drawing:PowerSourceG34:sourceBody',
                        kind: 'circle',
                        bounds: { x: 103, y: 82, width: 26, height: 26 },
                        center: { x: 116, y: 95 },
                        radius: 13,
                        styleTokenId: 'symbol',
                    }],
                    styleTokens: [{
                        styleTokenId: 'symbol',
                        stroke: 'foreground',
                        strokeWidth: 1.4,
                        fill: 'transparent',
                    }],
                },
                terminalBindings: [],
                labels: [],
                authorities: {
                    graphic: 'graphic-primitive-ir',
                    placement: 'semantic-layout-facts',
                    material: 'representation-material-resolver',
                },
            }],
            drawingComposition: {
                sheetId: 'control-drawing/01',
                policyId: 'rolling-shutter-control-drawing',
                contentBounds: { x: 48, y: 42, width: 954, height: 614 },
                frameBounds: { x: 30, y: 24, width: 990, height: 672 },
                drawingAreaBounds: { x: 48, y: 42, width: 954, height: 564 },
                titleBlockBounds: { x: 48, y: 606, width: 954, height: 72 },
                sheetBounds: { x: 0, y: 0, width: 1050, height: 720 },
                frameId: 'professional-control-drawing-frame',
                frameStyle: 'professional-control-drawing',
                title: {
                    sheetTitle: 'Rolling Shutter Control',
                    sheetFamily: 'schematic',
                    sheetNumber: '1',
                    revisionCode: 'A',
                    revisionNote: 'M34',
                    pageFormat: '1050x720',
                    orientation: 'landscape',
                    fields: [],
                },
                coordinateZones: [],
                structureSubjects: [],
                structureFacts: [],
                referencePlacements: [],
                authorities: {
                    contentBounds: 'graphic-primitive-ir',
                    bounds: 'drawing-composition',
                    projection: 'projection-sheet-publication',
                    representation: 'graphic-primitive-ir',
                    structureIntent: 'drawing-composition',
                    policy: 'presentation-profile-policy',
                },
            },
        },
        graph: {
            id: 'legacy-fallback-must-not-render',
            type: 'graph',
            canvas: { width: 1050, height: 720 },
            nodes: [{
                id: 'legacy-box',
                semanticId: 'component:Legacy',
                type: 'node',
                kind: 'component',
                label: 'Legacy',
                position: { x: 10, y: 10 },
                size: { width: 100, height: 50 },
            }],
            edges: [],
        },
    };

    const model = buildAthenaGraphWorkbenchModel(diagram);

    assert.equal(model.nodes.length, 1);
    assert.equal(model.nodes[0].id, 'drawing:PowerSourceG34');
    assert.equal(model.nodes[0].presentationGraphicOccurrence.authorities.graphic, 'graphic-primitive-ir');
    assert.equal(model.nodes[0].presentationParts[0].commands[0].kind, 'circle');
    assert.equal(model.nodes.some(node => node.id === 'legacy-box'), false);
    assert.equal(model.svgViewBox, '0 0 1050 720');
});
