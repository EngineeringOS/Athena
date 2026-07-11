export const GRAPH_COMMAND_INTENT_METHOD = 'athena/graphCommandIntent';
export const ADJUST_LAYOUT_PLACEMENT_INTENT_ID = 'adjust-layout-placement';
export const CONNECT_PORTS_INTENT_ID = 'connect-ports';

export type AthenaGraphCommandSubjectKind = 'component' | 'label' | 'connection' | 'port';

export type AthenaGraphCommandTargetPayload = {
    semanticId: string;
    subjectKind: AthenaGraphCommandSubjectKind;
};

export type AthenaGraphPlacementPayload = {
    x: number;
    y: number;
};

export type AthenaGraphCommandIntentParams = {
    intentId: string;
    viewId: string;
    source?: AthenaGraphCommandTargetPayload;
    target: AthenaGraphCommandTargetPayload;
    requestedPlacement?: AthenaGraphPlacementPayload;
};

export type AthenaGraphCommandExecutionPayload = {
    commandKind: string;
    outcome: string;
    commandId?: string;
    changedSemanticIds: string[];
    validationFeedback: AthenaMutationValidationFeedbackPayload[];
};

export type AthenaMutationValidationFeedbackPayload = {
    code: string;
    message: string;
    severity: string;
    relatedSemanticIds: string[];
};

export type AthenaGraphCommandIntentPayload = {
    projectName: string;
    semanticPath: string;
    status: string;
    intentId: string;
    mutationCategory: string;
    viewId: string;
    source?: AthenaGraphCommandTargetPayload;
    target: AthenaGraphCommandTargetPayload;
    requestedPlacement?: AthenaGraphPlacementPayload;
    execution?: AthenaGraphCommandExecutionPayload;
    validationFeedback: AthenaMutationValidationFeedbackPayload[];
    reason?: string;
};

export type AthenaGraphCommandIntentRequestEnvelope<TModel = unknown> = {
    method: typeof GRAPH_COMMAND_INTENT_METHOD;
    params: AthenaGraphCommandIntentParams;
    model?: TModel;
};

export type AthenaGraphCommandIntentViewLike = {
    viewId: string;
    ownershipContract?: {
        interactivity?: string;
        semanticCommandIds?: string[];
        projectionCommandIds?: string[];
    };
};

export function buildAdjustLayoutPlacementIntentRequest<TModel = unknown>(args: {
    viewId: string;
    semanticId: string;
    subjectKind: AthenaGraphCommandSubjectKind;
    x: number;
    y: number;
    model?: TModel;
}): AthenaGraphCommandIntentRequestEnvelope<TModel> {
    return {
        method: GRAPH_COMMAND_INTENT_METHOD,
        params: {
            intentId: ADJUST_LAYOUT_PLACEMENT_INTENT_ID,
            viewId: args.viewId,
            target: {
                semanticId: args.semanticId,
                subjectKind: args.subjectKind
            },
            requestedPlacement: {
                x: args.x,
                y: args.y
            }
        },
        model: args.model
    };
}

export function buildConnectPortsIntentRequest<TModel = unknown>(args: {
    viewId: string;
    sourceSemanticId: string;
    targetSemanticId: string;
    model?: TModel;
}): AthenaGraphCommandIntentRequestEnvelope<TModel> {
    return {
        method: GRAPH_COMMAND_INTENT_METHOD,
        params: {
            intentId: CONNECT_PORTS_INTENT_ID,
            viewId: args.viewId,
            source: {
                semanticId: args.sourceSemanticId,
                subjectKind: 'port'
            },
            target: {
                semanticId: args.targetSemanticId,
                subjectKind: 'port'
            }
        },
        model: args.model
    };
}

export function supportsAdjustLayoutPlacementIntent(view: AthenaGraphCommandIntentViewLike | undefined): boolean {
    return view?.ownershipContract?.interactivity === 'interactive'
        && view.ownershipContract.projectionCommandIds?.includes(ADJUST_LAYOUT_PLACEMENT_INTENT_ID) === true;
}

export function supportsConnectPortsIntent(view: AthenaGraphCommandIntentViewLike | undefined): boolean {
    return view?.ownershipContract?.interactivity === 'interactive'
        && view.ownershipContract.semanticCommandIds?.includes(CONNECT_PORTS_INTENT_ID) === true;
}
