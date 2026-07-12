import assert from 'node:assert/strict';
import test from 'node:test';

const semanticSelectionModel = await import('../lib/browser/athena-semantic-selection-model.js');

const inspection = {
    uri: 'file:///workspace/demo.athena',
    version: 2,
    status: 'ready',
    systemName: 'DemoCabinet',
    diagnosticsCount: 0,
    diagnosticSummaries: [],
    componentCount: 1,
    portCount: 1,
    connectionCount: 1,
    components: [
        {
            semanticId: 'component:PLC1',
            name: 'PLC1',
            kind: 'Switch',
            properties: 'type=Switch',
            sourceRange: {
                start: {
                    line: 1,
                    character: 2
                },
                end: {
                    line: 4,
                    character: 3
                }
            }
        }
    ],
    ports: [
        {
            semanticId: 'port:PLC1.out',
            path: 'PLC1.out',
            properties: 'direction=out',
            sourceRange: {
                start: {
                    line: 6,
                    character: 2
                },
                end: {
                    line: 9,
                    character: 3
                }
            }
        }
    ],
    connections: [
        {
            semanticId: 'connection:PLC1.out->M1.in',
            fromPath: 'PLC1.out',
            toPath: 'M1.in',
            sourceRange: {
                start: {
                    line: 11,
                    character: 2
                },
                end: {
                    line: 11,
                    character: 28
                }
            }
        }
    ]
};

test('resolves canonical semantic selection from typed inspection payload', () => {
    assert.equal(typeof semanticSelectionModel.resolveSemanticSelectionFromInspection, 'function');

    const selection = semanticSelectionModel.resolveSemanticSelectionFromInspection(
        inspection,
        'port:PLC1.out'
    );

    assert.deepEqual(selection, {
        semanticId: 'port:PLC1.out',
        label: 'PLC1.out',
        kind: 'port',
        sourceUri: 'file:///workspace/demo.athena',
        sourceRange: inspection.ports[0].sourceRange
    });
});

test('resolves the most specific semantic selection from a source-editor range', () => {
    assert.equal(typeof semanticSelectionModel.resolveSemanticSelectionFromSourceRange, 'function');

    const selection = semanticSelectionModel.resolveSemanticSelectionFromSourceRange(
        inspection,
        inspection.uri,
        {
            start: { line: 11, character: 8 },
            end: { line: 11, character: 18 }
        }
    );

    assert.deepEqual(selection, {
        semanticId: 'connection:PLC1.out->M1.in',
        label: 'PLC1.out -> M1.in',
        kind: 'connection',
        sourceUri: inspection.uri,
        sourceRange: inspection.connections[0].sourceRange
    });
});

test('matches semantic scm context through subject identity and fact-reference vocabulary', () => {
    assert.equal(typeof semanticSelectionModel.matchesSemanticScmContext, 'function');

    const selectedSemanticId = 'connection:PLC1.out->M1.in';
    const reviewEntry = {
        subjectIdentity: 'component:PLC1',
        factReferences: [
            {
                kind: 'semantic-change',
                identifier: 'review-entry',
                subjectIdentity: selectedSemanticId
            }
        ]
    };

    const commitEntry = {
        subjectIdentity: selectedSemanticId,
        factReferences: []
    };

    assert.equal(
        semanticSelectionModel.matchesSemanticScmContext(reviewEntry, selectedSemanticId),
        true
    );
    assert.equal(
        semanticSelectionModel.matchesSemanticScmContext(commitEntry, selectedSemanticId),
        true
    );
    assert.equal(
        semanticSelectionModel.matchesSemanticScmContext(reviewEntry, 'component:M1'),
        false
    );
});

test('selectable semantic scm context prefers subject identity and falls back to fact references', () => {
    assert.equal(typeof semanticSelectionModel.selectableSemanticIdFromScmContext, 'function');

    const fromSubjectIdentity = semanticSelectionModel.selectableSemanticIdFromScmContext({
        subjectIdentity: 'component:PLC1',
        factReferences: []
    });
    const fromFactReference = semanticSelectionModel.selectableSemanticIdFromScmContext({
        factReferences: [
            {
                kind: 'authored-change',
                identifier: 'change-1',
                subjectIdentity: 'port:PLC1.out'
            }
        ]
    });

    assert.equal(fromSubjectIdentity, 'component:PLC1');
    assert.equal(fromFactReference, 'port:PLC1.out');
});

test('drops transient selection when refreshed projection no longer contains the semantic id', () => {
    assert.equal(typeof semanticSelectionModel.retainSelectionIfPresent, 'function');

    const selected = {
        semanticId: 'port:PLC1.out',
        label: 'PLC1.out',
        kind: 'port'
    };
    const visibleDiagram = {
        graph: {
            nodes: [{ id: 'port:PLC1.out' }],
            edges: []
        }
    };
    const refreshedDiagram = {
        graph: {
            nodes: [{ id: 'component:M1' }],
            edges: [{ id: 'connection:M1.out->Lamp.in' }]
        }
    };

    assert.deepEqual(
        semanticSelectionModel.retainSelectionIfPresent(visibleDiagram, selected),
        selected
    );
    assert.equal(
        semanticSelectionModel.retainSelectionIfPresent(refreshedDiagram, selected),
        undefined
    );
    assert.equal(
        semanticSelectionModel.graphContainsSemanticId(visibleDiagram, 'port:PLC1.out'),
        true
    );
    assert.equal(
        semanticSelectionModel.graphContainsSemanticId(refreshedDiagram, 'port:PLC1.out'),
        false
    );
});

test('resolves repeated projection occurrences without inventing alias identities', () => {
    assert.equal(typeof semanticSelectionModel.resolveProjectionOccurrence, 'function');

    const repeatedDiagram = {
        crossReferences: [
            {
                semanticId: 'component:PLC1',
                kind: 'repeated_reference',
                sheetIds: ['documentation/sheet/01-overview', 'documentation/sheet/02-reference'],
                occurrenceIds: [
                    'documentation/projection/node/component_PLC1',
                    'documentation/projection/node/component_PLC1_reference'
                ]
            }
        ],
        graph: {
            nodes: [
                { id: 'documentation/projection/node/component_PLC1', semanticId: 'component:PLC1' },
                { id: 'documentation/projection/node/component_PLC1_reference', semanticId: 'component:PLC1' }
            ],
            edges: []
        }
    };

    assert.deepEqual(
        semanticSelectionModel.resolveProjectionCrossReference(repeatedDiagram, 'component:PLC1'),
        {
            semanticId: 'component:PLC1',
            kind: 'repeated_reference',
            sheetIds: ['documentation/sheet/01-overview', 'documentation/sheet/02-reference'],
            occurrenceIds: [
                'documentation/projection/node/component_PLC1',
                'documentation/projection/node/component_PLC1_reference'
            ]
        }
    );
    assert.deepEqual(
        semanticSelectionModel.resolveProjectionOccurrence(repeatedDiagram, 'component:PLC1'),
        {
            semanticId: 'component:PLC1',
            status: 'ambiguous',
            occurrenceIds: [
                'documentation/projection/node/component_PLC1',
                'documentation/projection/node/component_PLC1_reference'
            ]
        }
    );
    assert.deepEqual(
        semanticSelectionModel.resolveProjectionOccurrence(repeatedDiagram, 'component:M1'),
        {
            semanticId: 'component:M1',
            status: 'unresolved',
            occurrenceIds: []
        }
    );
});
