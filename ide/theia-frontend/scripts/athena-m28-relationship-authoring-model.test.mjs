import assert from 'node:assert/strict';
import test from 'node:test';

const {
    activateRelationshipMode,
    buildRelationshipAuthoringPreview,
    buildRelationshipInteractionCommand,
    clearRelationshipAuthoringPreview,
    relationshipModeSubjectClassName,
    selectRelationshipModeSubject,
} = await import('../lib/browser/athena-relationship-authoring-model.js');

test('relationship mode activates without inventing a selected subject', () => {
    assert.deepEqual(
        activateRelationshipMode(),
        {
            active: true,
            sourceSubject: undefined,
            targetSubject: undefined,
            diagnostics: []
        }
    );
});

test('relationship subject selection resolves canonical port identity from projection facts only', () => {
    const afterSource = selectRelationshipModeSubject(
        activateRelationshipMode(),
        {
            endpointId: 'documentation/projection/connection/c1/endpoint/source',
            anchorId: 'documentation/projection/label/port_PLC1_out/anchor',
            portSemanticId: 'port:PLC1.out',
            role: 'source'
        }
    );
    const afterTarget = selectRelationshipModeSubject(
        afterSource,
        {
            id: 'dom-node-with-text-PLC1-out',
            endpointId: 'documentation/projection/connection/c1/endpoint/target',
            anchorId: 'documentation/projection/label/port_M1_in/anchor',
            portSemanticId: 'port:M1.in',
            role: 'target'
        }
    );

    assert.equal(afterSource.sourceSubject?.semanticId, 'port:PLC1.out');
    assert.equal(afterSource.sourceSubject?.source, 'terminal');
    assert.equal(afterSource.targetSubject, undefined);
    assert.equal(afterTarget.sourceSubject?.semanticId, 'port:PLC1.out');
    assert.equal(afterTarget.targetSubject?.semanticId, 'port:M1.in');
    assert.equal(afterTarget.diagnostics.length, 0);
});

test('relationship subject selection rejects DOM-only and non-port subjects', () => {
    const domOnly = selectRelationshipModeSubject(
        activateRelationshipMode(),
        {
            id: 'dom-node-with-visible-text-PLC1-out'
        }
    );
    const component = selectRelationshipModeSubject(
        activateRelationshipMode(),
        {
            id: 'documentation/projection/node/component_PLC1',
            type: 'node',
            semanticId: 'component:PLC1'
        }
    );

    assert.equal(domOnly.sourceSubject, undefined);
    assert.equal(domOnly.diagnostics[0].code, 'relationship.subject.unresolved');
    assert.equal(component.sourceSubject, undefined);
    assert.equal(component.diagnostics[0].code, 'relationship.subject.not-port');
});

test('relationship mode affordance class keeps normal chrome transparent and only marks transient states', () => {
    assert.equal(
        relationshipModeSubjectClassName({ active: false, selected: false, hovered: false }),
        'athena-relationship-subject'
    );
    assert.equal(
        relationshipModeSubjectClassName({ active: true, selected: false, hovered: false }),
        'athena-relationship-subject'
    );
    assert.equal(
        relationshipModeSubjectClassName({ active: true, selected: true, hovered: false }),
        'athena-relationship-subject athena-relationship-subject--selected'
    );
    assert.equal(
        relationshipModeSubjectClassName({ active: true, selected: false, hovered: true }),
        'athena-relationship-subject athena-relationship-subject--hovered'
    );
});

test('relationship authoring preview shows transient route quality and backend-owned source impact', () => {
    const selected = selectRelationshipModeSubject(
        selectRelationshipModeSubject(
            activateRelationshipMode(),
            { portSemanticId: 'port:PLC1.out', anchorId: 'anchor:PLC1.out' }
        ),
        { portSemanticId: 'port:M1.in', anchorId: 'anchor:M1.in' }
    );

    const preview = buildRelationshipAuthoringPreview({
        state: selected,
        routeQuality: 'SATISFIED',
        serializationTargetUri: 'file:///workspace/main.athena'
    });

    assert.deepEqual(preview, {
        status: 'ready',
        relationshipType: 'ElectricalConnectionRelationship',
        sourceSemanticId: 'port:PLC1.out',
        targetSemanticId: 'port:M1.in',
        routeQuality: 'SATISFIED',
        sourceImpact: {
            authority: 'backend-runtime-source-edit',
            serializationTargetUri: 'file:///workspace/main.athena',
            status: 'pending-runtime-source-edit'
        },
        transient: true,
        persisted: false,
        diagnostics: []
    });
});

test('relationship authoring discovers an Interaction command for electrical semantic relationship mutation', () => {
    const selected = selectRelationshipModeSubject(
        selectRelationshipModeSubject(
            activateRelationshipMode(),
            { portSemanticId: 'port:PLC1.out', anchorId: 'anchor:PLC1.out' }
        ),
        { portSemanticId: 'port:M1.in', anchorId: 'anchor:M1.in' }
    );

    assert.deepEqual(buildRelationshipInteractionCommand(selected), {
        status: 'ready',
        commandId: 'command:relationship:port:PLC1.out->port:M1.in',
        actionIntent: {
            actionIntentId: 'action:relationship:port:PLC1.out->port:M1.in',
            actionFamily: 'mutate',
            subject: {
                canonicalSubjectId: 'port:PLC1.out',
                subjectKind: 'port'
            },
            targetSubjects: [
                {
                    canonicalSubjectId: 'port:M1.in',
                    subjectKind: 'port'
                }
            ],
            requestedBy: {
                originSurface: 'graph',
                reason: 'relationship authoring'
            },
            parameters: {
                relationshipType: 'ElectricalConnectionRelationship'
            }
        },
        diagnostics: []
    });
});

test('relationship authoring preview clears or becomes stale on lifecycle invalidation', () => {
    const selected = selectRelationshipModeSubject(
        selectRelationshipModeSubject(
            activateRelationshipMode(),
            { portSemanticId: 'port:PLC1.out' }
        ),
        { portSemanticId: 'port:M1.in' }
    );
    const preview = buildRelationshipAuthoringPreview({
        state: selected,
        routeQuality: 'SATISFIED',
        serializationTargetUri: 'file:///workspace/main.athena'
    });

    assert.equal(clearRelationshipAuthoringPreview(preview, 'cancel'), undefined);
    assert.deepEqual(clearRelationshipAuthoringPreview(preview, 'source-reload'), {
        status: 'stale',
        staleReason: 'source-reload',
        transient: true,
        persisted: false,
        diagnostics: [
            {
                code: 'relationship.preview.stale',
                message: 'Relationship preview invalidated by source-reload.'
            }
        ]
    });
    assert.equal(clearRelationshipAuthoringPreview(preview, 'projection-refresh')?.status, 'stale');
    assert.equal(clearRelationshipAuthoringPreview(preview, 'active-source-change')?.status, 'stale');
    assert.equal(clearRelationshipAuthoringPreview(preview, 'accepted-mutation')?.status, 'stale');
});
