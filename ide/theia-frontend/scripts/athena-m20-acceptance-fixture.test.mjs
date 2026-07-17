import assert from 'node:assert/strict';
import test from 'node:test';

const { buildAthenaGraphWorkbenchModel } = await import('../lib/browser/athena-graph-workbench-model.js');
const { default: acceptanceDiagram } = await import('../../../examples/m20/acceptance-sheet-proof/ready-sheet.diagram.mjs');
const { default: schematicDiagram } = await import('../../../examples/m20/schematic-sheet-proof/ready-sheet.diagram.mjs');

test('M20 acceptance fixture stays grounded in the governed schematic proof', () => {
    assert.deepEqual(acceptanceDiagram, schematicDiagram);
    assert.equal(acceptanceDiagram.sheets[0].composition.representationFamilyId, 'schematic-sheet');
    assert.deepEqual(
        acceptanceDiagram.sheets[0].composition.publication.titleBlock,
        {
            sheetTitle: '24V Control Power',
            sheetFamily: 'schematic-sheet',
            sheetNumber: '01-main',
        },
    );

    const model = buildAthenaGraphWorkbenchModel(acceptanceDiagram);

    assert.equal(model.statusTone, 'ready');
    assert.equal(model.sheetChrome.titleBlock.displayName, '24V Control Power');
    assert.equal(model.sheetChrome.titleBlock.subjectCount, 4);
    assert.equal(model.nodes.some(node => node.semanticId === 'component:PSU1'), true);
});
