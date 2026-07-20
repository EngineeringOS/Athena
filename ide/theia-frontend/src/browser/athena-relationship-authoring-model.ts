import {
    resolveRenderedSelectionTarget,
    type AthenaRenderedSelectionSubject,
    type AthenaRenderedSelectionTarget,
} from './athena-semantic-selection-model';

export type AthenaRelationshipAuthoringDiagnostic = {
    code: string;
    message: string;
};

export type AthenaRelationshipAuthoringModeState = {
    active: boolean;
    sourceSubject?: AthenaRenderedSelectionTarget;
    targetSubject?: AthenaRenderedSelectionTarget;
    diagnostics: AthenaRelationshipAuthoringDiagnostic[];
};

export type AthenaRelationshipSubjectAffordance = {
    active: boolean;
    selected: boolean;
    hovered: boolean;
};

export type AthenaRelationshipAuthoringPreview =
    | {
        status: 'ready';
        relationshipType: 'ElectricalConnectionRelationship';
        sourceSemanticId: string;
        targetSemanticId: string;
        routeQuality: string;
        sourceImpact: {
            serializationTargetUri: string;
            statement: string;
        };
        transient: true;
        persisted: false;
        diagnostics: AthenaRelationshipAuthoringDiagnostic[];
    }
    | {
        status: 'blocked';
        transient: true;
        persisted: false;
        diagnostics: AthenaRelationshipAuthoringDiagnostic[];
    };

export type AthenaRelationshipAuthoringPreviewClearReason =
    | 'cancel'
    | 'source-reload'
    | 'projection-refresh'
    | 'accepted-mutation';

export function activateRelationshipMode(): AthenaRelationshipAuthoringModeState {
    return {
        active: true,
        sourceSubject: undefined,
        targetSubject: undefined,
        diagnostics: [],
    };
}

export function deactivateRelationshipMode(): AthenaRelationshipAuthoringModeState {
    return {
        active: false,
        sourceSubject: undefined,
        targetSubject: undefined,
        diagnostics: [],
    };
}

export function selectRelationshipModeSubject(
    state: AthenaRelationshipAuthoringModeState,
    subject: AthenaRenderedSelectionSubject,
): AthenaRelationshipAuthoringModeState {
    if (!state.active) {
        return state;
    }

    const target = resolveRenderedSelectionTarget(subject);
    if (!target) {
        return {
            ...state,
            diagnostics: [
                {
                    code: 'relationship.subject.unresolved',
                    message: 'Relationship authoring requires a governed semantic subject from projection facts.',
                },
            ],
        };
    }

    if (target.subjectKind !== 'port') {
        return {
            ...state,
            diagnostics: [
                {
                    code: 'relationship.subject.not-port',
                    message: 'M28 electrical relationship authoring starts from semantic port subjects.',
                },
            ],
        };
    }

    if (!state.sourceSubject) {
        return {
            ...state,
            sourceSubject: target,
            targetSubject: undefined,
            diagnostics: [],
        };
    }

    return {
        ...state,
        targetSubject: target,
        diagnostics: [],
    };
}

export function relationshipModeSubjectClassName(
    affordance: AthenaRelationshipSubjectAffordance,
): string {
    return [
        'athena-relationship-subject',
        affordance.active && affordance.selected ? 'athena-relationship-subject--selected' : '',
        affordance.active && affordance.hovered ? 'athena-relationship-subject--hovered' : '',
    ].filter(Boolean).join(' ');
}

export function buildRelationshipAuthoringPreview(input: {
    state: AthenaRelationshipAuthoringModeState;
    routeQuality: string;
    serializationTargetUri: string;
}): AthenaRelationshipAuthoringPreview {
    const source = input.state.sourceSubject;
    const target = input.state.targetSubject;
    if (!source || !target) {
        return {
            status: 'blocked',
            transient: true,
            persisted: false,
            diagnostics: [
                {
                    code: 'relationship.preview.incomplete',
                    message: 'Relationship preview requires source and target semantic subjects.',
                },
            ],
        };
    }

    return {
        status: 'ready',
        relationshipType: 'ElectricalConnectionRelationship',
        sourceSemanticId: source.semanticId,
        targetSemanticId: target.semanticId,
        routeQuality: input.routeQuality,
        sourceImpact: {
            serializationTargetUri: input.serializationTargetUri,
            statement: `connect ${authoredPortPath(source.semanticId)} -> ${authoredPortPath(target.semanticId)}`,
        },
        transient: true,
        persisted: false,
        diagnostics: [],
    };
}

export function clearRelationshipAuthoringPreview(
    _preview: AthenaRelationshipAuthoringPreview | undefined,
    _reason: AthenaRelationshipAuthoringPreviewClearReason,
): undefined {
    return undefined;
}

function authoredPortPath(semanticId: string): string {
    return semanticId.startsWith('port:') ? semanticId.slice('port:'.length) : semanticId;
}
