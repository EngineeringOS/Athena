import assert from 'node:assert/strict';
import test from 'node:test';

const selectionModel = await import('../lib/browser/athena-semantic-selection-model.js');

const entityRange = { start: { line: 1, character: 2 }, end: { line: 8, character: 3 } };
const portRange = { start: { line: 4, character: 4 }, end: { line: 7, character: 5 } };
const inspection = {
    uri: 'file:///workspace/anatomy.athena',
    version: 2,
    status: 'ready',
    systemName: 'Anatomy',
    diagnosticsCount: 0,
    diagnosticSummaries: [],
    entityCount: 1,
    portCount: 1,
    relationshipCount: 0,
    entities: [{
        semanticId: 'entity:Drive',
        name: 'Drive',
        concept: 'core.Drive',
        properties: '',
        authoredProperties: [],
        sourceRange: entityRange
    }],
    ports: [{
        semanticId: 'port:Drive.powerIn',
        path: 'Drive.powerIn',
        properties: '',
        authoredProperties: [],
        sourceRange: portRange
    }],
    relationships: []
};

test('resolves Entity and Port selections from compiled inspection', () => {
    assert.deepEqual(
        selectionModel.resolveSemanticSelectionFromInspection(inspection, 'entity:Drive'),
        {
            semanticId: 'entity:Drive',
            label: 'Drive',
            kind: 'entity',
            sourceUri: inspection.uri,
            sourceRange: entityRange
        }
    );
    assert.equal(
        selectionModel.resolveSemanticSelectionFromSourceRange(
            inspection,
            inspection.uri,
            { start: { line: 5, character: 6 }, end: { line: 5, character: 6 } }
        ).semanticId,
        'port:Drive.powerIn'
    );
});
