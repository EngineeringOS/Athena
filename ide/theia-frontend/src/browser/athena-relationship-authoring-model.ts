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
            authority: 'backend-runtime-source-edit';
            serializationTargetUri: string;
            status: 'pending-runtime-source-edit';
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
    }
    | {
        status: 'stale';
        staleReason: AthenaRelationshipAuthoringPreviewClearReason;
        transient: true;
        persisted: false;
        diagnostics: AthenaRelationshipAuthoringDiagnostic[];
    };

export type AthenaRelationshipAuthoringPreviewClearReason =
    | 'cancel'
    | 'source-reload'
    | 'projection-refresh'
    | 'active-source-change'
    | 'accepted-mutation';

export type AthenaRelationshipInteractionCommand =
    | {
        status: 'ready';
        commandId: string;
        actionIntent: {
            actionIntentId: string;
            actionFamily: 'mutate';
            subject: {
                canonicalSubjectId: string;
                subjectKind: 'port';
            };
            targetSubjects: Array<{
                canonicalSubjectId: string;
                subjectKind: 'port';
            }>;
            requestedBy: {
                originSurface: 'graph';
                reason: 'relationship authoring';
            };
            parameters: {
                relationshipType: 'ElectricalConnectionRelationship';
            };
        };
        diagnostics: AthenaRelationshipAuthoringDiagnostic[];
    }
    | {
        status: 'blocked';
        diagnostics: AthenaRelationshipAuthoringDiagnostic[];
    };

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
            authority: 'backend-runtime-source-edit',
            serializationTargetUri: input.serializationTargetUri,
            status: 'pending-runtime-source-edit',
        },
        transient: true,
        persisted: false,
        diagnostics: [],
    };
}

export function buildRelationshipInteractionCommand(
    state: AthenaRelationshipAuthoringModeState,
): AthenaRelationshipInteractionCommand {
    const source = state.sourceSubject;
    const target = state.targetSubject;
    if (!source || !target) {
        return {
            status: 'blocked',
            diagnostics: [
                {
                    code: 'relationship.command.incomplete',
                    message: 'Relationship command requires source and target semantic port subjects.',
                },
            ],
        };
    }

    const relationshipKey = `${source.semanticId}->${target.semanticId}`;
    return {
        status: 'ready',
        commandId: `command:relationship:${relationshipKey}`,
        actionIntent: {
            actionIntentId: `action:relationship:${relationshipKey}`,
            actionFamily: 'mutate',
            subject: {
                canonicalSubjectId: source.semanticId,
                subjectKind: 'port',
            },
            targetSubjects: [
                {
                    canonicalSubjectId: target.semanticId,
                    subjectKind: 'port',
                },
            ],
            requestedBy: {
                originSurface: 'graph',
                reason: 'relationship authoring',
            },
            parameters: {
                relationshipType: 'ElectricalConnectionRelationship',
            },
        },
        diagnostics: [],
    };
}

export function clearRelationshipAuthoringPreview(
    preview: AthenaRelationshipAuthoringPreview | undefined,
    reason: AthenaRelationshipAuthoringPreviewClearReason,
): AthenaRelationshipAuthoringPreview | undefined {
    if (!preview) {
        return undefined;
    }
    if (reason === 'cancel') {
        return undefined;
    }
    return {
        status: 'stale',
        staleReason: reason,
        transient: true,
        persisted: false,
        diagnostics: [
            {
                code: 'relationship.preview.stale',
                message: `Relationship preview invalidated by ${reason}.`,
            },
        ],
    };
}
