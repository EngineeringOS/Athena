export type AthenaSourceMutationTextDocument = {
    uri: string;
};
export type AthenaSourceMutationParams = {
    textDocument: AthenaSourceMutationTextDocument;
};
export type AthenaMutationValidationFeedbackPayload = {
    code: string;
    message: string;
    severity: string;
    relatedSemanticIds: string[];
};
export type AthenaSemanticDiffEntryPayload = {
    semanticId: string;
    semanticKind: string;
    changeKind: string;
    beforeSummary?: string;
    afterSummary?: string;
};
export type AthenaSemanticHistoryConsequencePayload = {
    commandId: string;
    commandKind: string;
    status: string;
    changedSemanticIds: string[];
};
export type AthenaProjectionRefreshConsequencePayload = {
    layer: string;
    mode?: string;
    affectedViewIds: string[];
    affectedSemanticIds: string[];
};
export type AthenaSemanticDiffInspectionPayload = {
    projectName: string;
    source: string;
    affectedCommandIds: string[];
    affectedSemanticIds: string[];
    entries: AthenaSemanticDiffEntryPayload[];
    historyConsequences: AthenaSemanticHistoryConsequencePayload[];
    projectionConsequences: AthenaProjectionRefreshConsequencePayload[];
};
export type AthenaSourceMutationPayload = {
    uri: string;
    version: number;
    projectName: string;
    semanticPath: string;
    mutationCategory: string;
    outcome: string;
    changedSemanticIds: string[];
    validationFeedback: AthenaMutationValidationFeedbackPayload[];
    reason?: string;
    inspection?: AthenaSemanticDiffInspectionPayload;
};
export type AthenaSourceMutationRequestEnvelope<TModel = unknown> = {
    method: 'athena/sourceMutationEvaluation';
    params: AthenaSourceMutationParams;
    model?: TModel;
};
export declare function buildAthenaSourceMutationRequest<TModel = unknown>(documentUri: string, model?: TModel): AthenaSourceMutationRequestEnvelope<TModel>;
//# sourceMappingURL=athena-source-mutation-protocol.d.ts.map