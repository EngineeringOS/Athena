import assert from 'node:assert/strict';
import test from 'node:test';

const { buildAthenaGraphWorkbenchModel } = await import('../lib/browser/athena-graph-workbench-model.js');
const { default: denseDiagram } = await import('../../../examples/m20/dense-sheet-proof/ready-sheet.diagram.mjs');

function overlaps(left, right) {
    return left.x < right.x + right.width
        && left.x + left.width > right.x
        && left.y < right.y + right.height
        && left.y + left.height > right.y;
}

function boundsOf(node) {
    return {
        x: node.position.x,
        y: node.position.y,
        width: node.size.width,
        height: node.size.height,
    };
}

test('M20 dense sheet proof keeps drawing rules governed and readable', () => {
    assert.equal(denseDiagram.projectName, 'M20DenseSheetProof');
    assert.deepEqual(denseDiagram.drawingRules, {
        minimumSpacingPx: 32,
        labelOffsetPx: 18,
        routingLaneStyle: 'orthogonal',
        titleBlockSafeMarginPx: 48,
    });
    assert.equal(denseDiagram.sheets.length, 1);
    assert.deepEqual(
        denseDiagram.sheets[0].composition.publication.viewComposition.subjectSemanticIds,
        denseDiagram.sheets[0].subjectSemanticIds,
    );

    const firstModel = buildAthenaGraphWorkbenchModel(denseDiagram);
    const secondModel = buildAthenaGraphWorkbenchModel(denseDiagram);

    assert.equal(firstModel.statusTone, 'ready');
    assert.equal(firstModel.nodes.length, 7);
    assert.equal(firstModel.edges.length, 3);
    assert.deepEqual(firstModel.sheetChrome.titleBlock, {
        sheetId: 'schematic/sheet/01-main',
        displayName: '24V Control Power',
        order: 0,
        subjectCount: 10,
        crossReferenceCount: 1,
    });
    assert.equal(firstModel.edges.every(edge => edge.routePoints.length >= 3), true);
    assert.equal(firstModel.edges.every(edge => edge.conductorStyle === 'electrical'), true);

    const nodeBounds = firstModel.nodes.map(boundsOf);
    for (let leftIndex = 0; leftIndex < nodeBounds.length; leftIndex += 1) {
        for (let rightIndex = leftIndex + 1; rightIndex < nodeBounds.length; rightIndex += 1) {
            assert.equal(
                overlaps(nodeBounds[leftIndex], nodeBounds[rightIndex]),
                false,
                `node bounds ${leftIndex} and ${rightIndex} should not overlap`,
            );
        }
    }

    assert.deepEqual(firstModel, secondModel);
});
