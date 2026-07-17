import assert from 'node:assert/strict';
import test from 'node:test';

const {
    buildAthenaGraphWorkbenchModel,
    fitAthenaGraphViewport
} = await import('../lib/browser/athena-graph-workbench-model.js');
const { resolveRenderedSelectionTarget } = await import('../lib/browser/athena-semantic-selection-model.js');
const { default: acceptanceDiagram } = await import('../../../examples/m20/acceptance-sheet-proof/ready-sheet.diagram.mjs');
const { default: denseDiagram } = await import('../../../examples/m20/dense-sheet-proof/ready-sheet.diagram.mjs');

test('M20 regression suite covers acceptance and dense fixtures with local executable checks', () => {
    const acceptanceModel = buildAthenaGraphWorkbenchModel(acceptanceDiagram);
    const repeatedAcceptanceModel = buildAthenaGraphWorkbenchModel(acceptanceDiagram);
    const denseModel = buildAthenaGraphWorkbenchModel(denseDiagram);
    const repeatedDenseModel = buildAthenaGraphWorkbenchModel(denseDiagram);

    assert.deepEqual(acceptanceModel, repeatedAcceptanceModel);
    assert.deepEqual(denseModel, repeatedDenseModel);
    assert.equal(acceptanceModel.sheetChrome.titleBlock.displayName, '24V Control Power');
    assert.deepEqual(
        resolveRenderedSelectionTarget(acceptanceModel.nodes.find(node => node.semanticId === 'component:PSU1')),
        {
            semanticId: 'component:PSU1',
            occurrenceId: 'schematic/projection/node/component_PSU1',
            subjectKind: 'component',
            source: 'node',
        },
    );

    const denseTransform = fitAthenaGraphViewport(denseModel.sceneBounds, {
        width: 1440,
        height: 900,
    });

    assert.ok(denseTransform.zoom > 1);
    assert.ok(Number.isFinite(denseTransform.offsetX));
    assert.ok(Number.isFinite(denseTransform.offsetY));
    assert.equal(denseModel.sheetChrome.titleBlock.subjectCount, 10);
});
