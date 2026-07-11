export declare const GRAPH_COMMAND_INTENT_METHOD = "athena/graphCommandIntent";
export declare const ADJUST_LAYOUT_PLACEMENT_INTENT_ID = "adjust-layout-placement";
export declare const CONNECT_PORTS_INTENT_ID = "connect-ports";
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
export declare function buildAdjustLayoutPlacementIntentRequest<TModel = unknown>(args: {
    viewId: string;
    semanticId: string;
    subjectKind: AthenaGraphCommandSubjectKind;
    x: number;
    y: number;
    model?: TModel;
}): AthenaGraphCommandIntentRequestEnvelope<TModel>;
export declare function buildConnectPortsIntentRequest<TModel = unknown>(args: {
    viewId: string;
    sourceSemanticId: string;
    targetSemanticId: string;
    model?: TModel;
}): AthenaGraphCommandIntentRequestEnvelope<TModel>;
export declare function supportsAdjustLayoutPlacementIntent(view: AthenaGraphCommandIntentViewLike | undefined): boolean;
export declare function supportsConnectPortsIntent(view: AthenaGraphCommandIntentViewLike | undefined): boolean;
//# sourceMappingURL=athena-graph-command-intent-protocol.d.ts.map