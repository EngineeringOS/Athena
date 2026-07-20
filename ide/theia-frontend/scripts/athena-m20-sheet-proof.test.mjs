import assert from 'node:assert/strict';
import test from 'node:test';

const { buildAthenaGraphWorkbenchModel } = await import('../lib/browser/athena-graph-workbench-model.js');
const { resolveRenderedSelectionTarget } = await import('../lib/browser/athena-semantic-selection-model.js');
const { default: readyDiagram } = await import('../../../examples/m20/schematic-sheet-proof/ready-sheet.diagram.mjs');

test('M20 proof corpus binds sheet composition facts before render checks', () => {
    assert.equal(readyDiagram.activeViewId, 'schematic-sheet');
    assert.equal(readyDiagram.supportedViews[0].familyId, 'electrical/schematic');
    assert.equal(readyDiagram.sheets.length, 1);
    assert.equal(readyDiagram.sheets[0].composition.representationFamilyId, 'schematic-sheet');
    assert.deepEqual(
        readyDiagram.sheets[0].composition.publication.coordinateZones.map(zone => zone.zoneId),
        ['header', 'body', 'title-block'],
    );
    assert.deepEqual(
        readyDiagram.sheets[0].composition.subjectSemanticIds,
        readyDiagram.sheets[0].composition.publication.viewComposition.subjectSemanticIds,
    );
    assert.deepEqual(
        readyDiagram.sheets[0].composition.publication.titleBlock,
        {
            sheetTitle: '24V Control Power',
            sheetFamily: 'schematic-sheet',
            sheetNumber: '01-main',
        },
    );

    const model = buildAthenaGraphWorkbenchModel(readyDiagram);

    assert.equal(model.statusTone, 'ready');
    assert.equal(model.activeSheetId, 'schematic/sheet/01-main');
    assert.deepEqual(model.sheetChrome.titleBlock, {
        sheetId: 'schematic/sheet/01-main',
        displayName: '24V Control Power',
        order: 0,
        subjectCount: 4,
        crossReferenceCount: 1,
    });
    assert.deepEqual(
        resolveRenderedSelectionTarget(model.nodes.find(node => node.semanticId === 'component:PSU1')),
        {
            semanticId: 'component:PSU1',
            occurrenceId: 'schematic/projection/node/component_PSU1',
            subjectKind: 'component',
            source: 'node',
        },
    );
    assert.deepEqual(
        resolveRenderedSelectionTarget(model.edges[0]),
        {
            semanticId: 'connection:PSU1.plus->PLC1.power',
            occurrenceId: 'schematic/projection/connection/connection_PSU1_plus_PLC1_power',
            subjectKind: 'connection',
            source: 'edge',
        },
    );
});
