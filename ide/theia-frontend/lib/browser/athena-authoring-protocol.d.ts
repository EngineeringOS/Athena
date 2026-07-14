export type AthenaAuthoringValuePayload = {
    kind: 'text' | 'symbol' | 'boolean' | 'integer';
    text?: string;
    booleanValue?: boolean;
    integerValue?: number;
};
export type AthenaAuthoringPreviewParams = {
    intentId: string;
    intentKind: 'create-component' | 'update-component-properties' | 'connect-ports' | 'reveal-subject';
    originSurface: 'palette' | 'inspector' | 'graph' | 'form' | 'template' | 'ai' | 'api' | 'dsl';
    originDetail?: string;
    parentIdentity?: string;
    conceptId?: string;
    preferredImplementationId?: string;
    suggestedName?: string;
    componentId?: string;
    properties?: Record<string, AthenaAuthoringValuePayload>;
    sourcePortId?: string;
    targetPortId?: string;
    subjectId?: string;
    revealTargets?: Array<'source' | 'graph' | 'inspector' | 'semantic-scm'>;
};
export type AthenaAuthoringPreviewChangePayload = {
    kind: string;
    title: string;
    summary?: string;
    affectedSubjectIdentities: string[];
};
export type AthenaAuthoringPreviewPayload = {
    previewId: string;
    intentId: string;
    intentKind: string;
    originSurface: string;
    originDetail?: string;
    status: string;
    title: string;
    changes: AthenaAuthoringPreviewChangePayload[];
    warnings: string[];
};
export type AthenaAuthoringPreviewSubmissionPayload = {
    projectName: string;
    semanticPath: string;
    status: string;
    preview: AthenaAuthoringPreviewPayload;
};
export type AthenaAuthoringSourcePositionPayload = {
    line: number;
    character: number;
};
export type AthenaAuthoringSourceRangePayload = {
    start: AthenaAuthoringSourcePositionPayload;
    end: AthenaAuthoringSourcePositionPayload;
};
export type AthenaAuthoringSourceEditPayload = {
    uri: string;
    range: AthenaAuthoringSourceRangePayload;
    newText: string;
    selectionRange?: AthenaAuthoringSourceRangePayload;
    suggestedSemanticId?: string;
};
export type AthenaAuthoringDecisionParams = {
    previewId: string;
    intentId: string;
    decision: 'accept' | 'accepted' | 'reject' | 'rejected';
    note?: string;
};
export type AthenaAuthoringPreviewDecisionPayload = {
    projectName: string;
    semanticPath: string;
    status: string;
    preview?: AthenaAuthoringPreviewPayload;
    sourceEdit?: AthenaAuthoringSourceEditPayload;
    reason?: string;
};
export declare function buildCreateComponentPreviewRequest(input: {
    systemSemanticId: string;
    conceptId: string;
    preferredImplementationId?: string;
    suggestedName?: string;
    originDetail?: string;
    intentId?: string;
}): AthenaAuthoringPreviewParams;
export declare function buildUpdateComponentPropertiesPreviewRequest(input: {
    componentId: string;
    name?: string;
    label?: string;
    description?: string;
    preferredImplementationId?: string;
    originDetail?: string;
    intentId?: string;
}): AthenaAuthoringPreviewParams;
export declare function buildConnectPortsPreviewRequest(input: {
    sourcePortId: string;
    targetPortId: string;
    originDetail?: string;
    intentId?: string;
}): AthenaAuthoringPreviewParams;
export declare function buildAuthoringDecisionRequest(input: {
    previewId: string;
    intentId: string;
    decision: 'accept' | 'accepted' | 'reject' | 'rejected';
    note?: string;
}): AthenaAuthoringDecisionParams;
//# sourceMappingURL=athena-authoring-protocol.d.ts.map