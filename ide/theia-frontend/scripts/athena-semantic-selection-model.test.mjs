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

test('resolves Scene trace origin to a nonempty source decoration range', () => {
    const selection = selectionModel.resolveSemanticSelectionFromSceneTrace(
        'D:\\workspace\\project',
        {
            traceId: `trace:sha256:${'1'.repeat(64)}`,
            origins: [{
                relativePath: 'src/com/example/project.athena',
                sourceDigest: `sha256:${'2'.repeat(64)}`,
                role: 'SEMANTIC_DECLARATION',
                startLine: 0,
                startCharacter: 0,
                endLine: 0,
                endCharacter: 0,
                subjectId: 'function:KM1.main',
                primary: true
            }]
        },
        'function:KM1.main'
    );

    assert.equal(selection.kind, 'trace');
    assert.match(selection.sourceUri, /project\.athena$/);
    assert.deepEqual(selection.sourceRange, {
        start: { line: 0, character: 0 },
        end: { line: 0, character: 1 }
    });
});

test('resolves published Connection source trace to zero-based editor range', () => {
    const selection = selectionModel.resolveSemanticSelectionFromPublishedSourceTrace(
        'D:\\workspace\\project',
        {
            relativePath: 'src/com/example/project.athena',
            startLine: 7,
            startCharacter: 3,
            endLine: 7,
            endCharacter: 18,
            subjectId: 'connection:PLC1.out:KM1.coil'
        },
        'connection:PLC1.out:KM1.coil'
    );

    assert.equal(selection.kind, 'trace');
    assert.match(selection.sourceUri, /project\.athena$/);
    assert.deepEqual(selection.sourceRange, {
        start: { line: 6, character: 2 },
        end: { line: 6, character: 17 }
    });
});
