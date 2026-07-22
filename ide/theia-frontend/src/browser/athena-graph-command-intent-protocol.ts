import type { AthenaAuthoringSourceEditPayload } from './athena-authoring-protocol';

export const GRAPH_COMMAND_INTENT_METHOD = 'athena/graphCommandIntent';
export const ADJUST_LAYOUT_PLACEMENT_INTENT_ID = 'adjust-layout-placement';
export const CREATE_SEMANTIC_RELATIONSHIP_INTENT_ID = 'create-semantic-relationship';

export type AthenaGraphCommandSubjectKind = 'component' | 'label' | 'connection' | 'port';

export type AthenaGraphCommandTargetPayload = {
    semanticId: string;
    subjectKind: AthenaGraphCommandSubjectKind;
};

export type AthenaGraphPlacementPayload = {
    x: number;
    y: number;
};

export type AthenaGraphAuthoredLayoutIntentPayload = {
    viewFamily: string;
    statements: Array<{
        subject: string;
        relation: 'near' | 'below' | 'aligned-with' | 'grouped-with';
        target: string;
        axis?: 'horizontal' | 'vertical';
        priority: 'preference';
    }>;
};

export type AthenaGraphCommandIntentParams = {
    intentId: string;
    viewId: string;
    source?: AthenaGraphCommandTargetPayload;
    target: AthenaGraphCommandTargetPayload;
    requestedPlacement?: AthenaGraphPlacementPayload;
    authoredLayoutIntent?: AthenaGraphAuthoredLayoutIntentPayload;
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
    sourceEdit?: AthenaAuthoringSourceEditPayload;
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
    authoredLayoutIntent?: AthenaGraphAuthoredLayoutIntentPayload;
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
            },
            ...(args.authoredLayoutIntent ? { authoredLayoutIntent: args.authoredLayoutIntent } : {})
        },
        model: args.model
    };
}

export function supportsAdjustLayoutPlacementIntent(view: AthenaGraphCommandIntentViewLike | undefined): boolean {
    return view?.ownershipContract?.interactivity === 'interactive'
        && view.ownershipContract.projectionCommandIds?.includes(ADJUST_LAYOUT_PLACEMENT_INTENT_ID) === true;
}

export function supportsCreateSemanticRelationshipIntent(view: AthenaGraphCommandIntentViewLike | undefined): boolean {
    return view?.ownershipContract?.interactivity === 'interactive'
        && view.ownershipContract.semanticCommandIds?.includes(CREATE_SEMANTIC_RELATIONSHIP_INTENT_ID) === true;
}
