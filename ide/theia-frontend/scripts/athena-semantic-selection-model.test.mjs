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
});
