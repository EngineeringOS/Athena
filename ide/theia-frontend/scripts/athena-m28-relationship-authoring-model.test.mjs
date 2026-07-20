import assert from 'node:assert/strict';
import test from 'node:test';

const {
    activateRelationshipMode,
    buildRelationshipAuthoringPreview,
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

test('relationship authoring preview shows transient route quality and source impact', () => {
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
            serializationTargetUri: 'file:///workspace/main.athena',
            statement: 'connect PLC1.out -> M1.in'
        },
        transient: true,
        persisted: false,
        diagnostics: []
    });
});

test('relationship authoring preview clears on cancel reload refresh or accepted mutation', () => {
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
    assert.equal(clearRelationshipAuthoringPreview(preview, 'source-reload'), undefined);
    assert.equal(clearRelationshipAuthoringPreview(preview, 'projection-refresh'), undefined);
    assert.equal(clearRelationshipAuthoringPreview(preview, 'accepted-mutation'), undefined);
});
