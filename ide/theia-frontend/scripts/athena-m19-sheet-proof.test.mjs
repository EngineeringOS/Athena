import assert from 'node:assert/strict';
import test from 'node:test';

const { buildAthenaGraphWorkbenchModel } = await import('../lib/browser/athena-graph-workbench-model.js');
const { default: readyDiagram } = await import('../../../examples/m19/schematic-sheet-proof/ready-sheet.diagram.mjs');

test('M19 schematic proof corpus stays deterministic from local fixtures', () => {
    const first = buildAthenaGraphWorkbenchModel(readyDiagram);
    const second = buildAthenaGraphWorkbenchModel(readyDiagram);

    assert.equal(readyDiagram.activeViewId, 'schematic-sheet');
    assert.equal(readyDiagram.supportedViews.length, 1);
    assert.equal(readyDiagram.supportedViews[0].familyId, 'electrical/schematic');
    assert.equal(readyDiagram.supportedViews.some(view => view.viewId.includes('cabinet')), false);
    assert.equal(JSON.stringify(readyDiagram).includes('registry'), false);
    assert.equal(JSON.stringify(readyDiagram).includes('sprotty'), false);
    assert.equal(JSON.stringify(readyDiagram).includes('elk'), false);
    assert.deepEqual(second.sheetChrome, first.sheetChrome);
    assert.deepEqual(
        second.nodes.map(node => ({
            id: node.id,
            semanticId: node.semanticId,
            renderVariant: node.renderVariant,
            anchorIds: node.electricalAnchors.map(anchor => anchor.anchorId),
        })),
        first.nodes.map(node => ({
            id: node.id,
            semanticId: node.semanticId,
            renderVariant: node.renderVariant,
            anchorIds: node.electricalAnchors.map(anchor => anchor.anchorId),
        })),
    );
    assert.deepEqual(
        second.edges.map(edge => ({
            id: edge.id,
            semanticId: edge.semanticId,
            path: edge.path,
            terminalAnchorIds: edge.terminals.map(terminal => terminal.anchorId),
        })),
        first.edges.map(edge => ({
            id: edge.id,
            semanticId: edge.semanticId,
            path: edge.path,
            terminalAnchorIds: edge.terminals.map(terminal => terminal.anchorId),
        })),
    );
    assert.equal(first.sheetChrome.activeSheet?.sheetId, 'schematic/sheet/01-main');
    assert.equal(first.sheetChrome.crossReferenceMarkers[0]?.isActiveSheetLinked, true);
});
