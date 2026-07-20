import assert from 'node:assert/strict';
import test from 'node:test';

const presentationModel = await import('../lib/browser/athena-graph-presentation-model.js');

test('M26 presentation reference markers are transported as compact typed payloads', () => {
    const diagram = {
        presentation: {
            canvasWidth: 640,
            canvasHeight: 360,
            primitivePacks: [],
            compositePacks: [],
            occurrences: [],
            connectors: [],
            referenceMarkers: [
                {
                    markerId: 'reference-marker:route-1',
                    markerKind: 'continuation',
                    relationType: 'route_continuation',
                    selectedSheetViewId: 'sheet-view:control-and-plc-logic',
                    sourceOccurrenceId: 'occurrence:route:control',
                    targetOccurrenceId: 'occurrence:route:field',
                    sourceIdentity: 'connection:PLC1.Q0.0->XT1.1',
                    targetIdentity: 'connection:PLC1.Q0.0->XT1.1',
                    sourceDocumentLocation: {
                        sheetViewId: 'sheet-view:control-and-plc-logic',
                        zoneId: 'A1',
                        displayNotation: 'A1'
                    },
                    targetDocumentLocation: {
                        sheetViewId: 'sheet-view:field-wiring-and-terminal-transition',
                        zoneId: 'B2',
                        displayNotation: 'B2'
                    },
                    compactNotation: 'sheet-view:field-wiring-and-terminal-transition B2',
                    sourceProjectionIds: ['cross-reference:route-1']
                }
            ]
        }
    };

    const markers = presentationModel.resolvePresentationReferenceMarkers(diagram);

    assert.equal(markers.length, 1);
    assert.equal(markers[0].markerKind, 'continuation');
    assert.equal(markers[0].relationType, 'route_continuation');
    assert.equal(markers[0].compactNotation, 'sheet-view:field-wiring-and-terminal-transition B2');
    assert.equal(markers[0].sourceIdentity, 'connection:PLC1.Q0.0->XT1.1');
    assert.equal(markers[0].targetOccurrenceId, 'occurrence:route:field');
    assert.ok(!markers[0].compactNotation.includes('connection:'));
});

test('M26 marker resolver is backward compatible with presentations without markers', () => {
    assert.deepEqual(
        presentationModel.resolvePresentationReferenceMarkers({
            presentation: {
                canvasWidth: 640,
                canvasHeight: 360,
                primitivePacks: [],
                compositePacks: [],
                occurrences: [],
                connectors: []
            }
        }),
        []
    );
});
