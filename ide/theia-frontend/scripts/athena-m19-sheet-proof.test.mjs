import assert from 'node:assert/strict';
import test from 'node:test';

const { buildAthenaGraphWorkbenchModel } = await import('../lib/browser/athena-graph-workbench-model.js');
const { resolveRenderedSelectionTarget } = await import('../lib/browser/athena-semantic-selection-model.js');
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
    assert.deepEqual(
        resolveRenderedSelectionTarget(first.nodes.find(node => node.semanticId === 'component:PSU1')),
        {
            semanticId: 'component:PSU1',
            occurrenceId: 'schematic/projection/node/component_PSU1',
            subjectKind: 'component',
            source: 'node'
        }
    );
    assert.deepEqual(
        resolveRenderedSelectionTarget(first.edges[0]),
        {
            semanticId: 'connection:PSU1.plus->PLC1.power',
            occurrenceId: 'schematic/projection/connection/connection_PSU1_plus_PLC1_power',
            subjectKind: 'connection',
            source: 'edge'
        }
    );
    assert.deepEqual(
        resolveRenderedSelectionTarget(first.edges[0].terminals[0]),
        {
            semanticId: 'port:PSU1.plus',
            endpointId: 'schematic/projection/connection/connection_PSU1_plus_PLC1_power/endpoint/source',
            anchorId: 'schematic/projection/label/port_PSU1_plus/anchor',
            subjectKind: 'port',
            source: 'terminal'
        }
    );
    assert.equal(first.sheetChrome.activeSheet?.sheetId, 'schematic/sheet/01-main');
    assert.equal(first.sheetChrome.crossReferenceMarkers[0]?.isActiveSheetLinked, true);
});
