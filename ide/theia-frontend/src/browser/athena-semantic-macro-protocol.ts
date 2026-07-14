export type AthenaSemanticMacroCatalogRequestParams = {
    marker?: string;
};

export type AthenaSemanticMacroParameterValuePayload = {
    kind: 'text' | 'symbol' | 'boolean' | 'integer';
    text?: string;
    booleanValue?: boolean;
    integerValue?: number;
};

export type AthenaSemanticMacroValidationParams = {
    macroId: string;
    instantiationId: string;
    parameterValues?: Record<string, AthenaSemanticMacroParameterValuePayload>;
};

export type AthenaSemanticMacroPreviewParams = {
    macroId: string;
    instantiationId: string;
    parameterValues?: Record<string, AthenaSemanticMacroParameterValuePayload>;
};

export type AthenaSemanticMacroAcceptanceParams = {
    previewId: string;
    macroId: string;
    instantiationId: string;
};

export type AthenaSemanticMacroOriginInspectionParams = {
    subjectId?: string;
    instantiationId?: string;
};

export type AthenaSemanticMacroCatalogDiagnosticPayload = {
    code: string;
    subject: string;
    message: string;
};

export type AthenaSemanticMacroCatalogEntryPayload = {
    macroId: string;
    displayName: string;
    summary: string;
    packageName: string;
    packageVersion?: string;
    definitionPath: string;
    classificationKeys: string[];
};

export type AthenaSemanticMacroCatalogPayload = {
    projectName: string;
    semanticPath: string;
    status: string;
    entries: AthenaSemanticMacroCatalogEntryPayload[];
    diagnostics: AthenaSemanticMacroCatalogDiagnosticPayload[];
    reason?: string;
};

export type AthenaSemanticMacroParameterValidationRulesPayload = {
    allowedValues: string[];
    pattern?: string;
    minLength?: number;
    maxLength?: number;
    minInteger?: number;
    maxInteger?: number;
};

export type AthenaSemanticMacroParameterDefinitionPayload = {
    name: string;
    valueKind: AthenaSemanticMacroParameterValuePayload['kind'];
    label: string;
    description?: string;
    required: boolean;
    defaultValue?: AthenaSemanticMacroParameterValuePayload;
    validationRules: AthenaSemanticMacroParameterValidationRulesPayload;
};

export type AthenaSemanticMacroValidationDiagnosticPayload = {
    code: string;
    parameterName?: string;
    message: string;
};

export type AthenaSemanticMacroValidationPayload = {
    projectName: string;
    semanticPath: string;
    status: string;
    macroId: string;
    instantiationId: string;
    parameters: AthenaSemanticMacroParameterDefinitionPayload[];
    normalizedValues: Record<string, AthenaSemanticMacroParameterValuePayload>;
    diagnostics: AthenaSemanticMacroValidationDiagnosticPayload[];
    reason?: string;
};

export type AthenaSemanticMacroPreviewChangePayload = {
    kind: string;
    title: string;
    summary?: string;
    affectedSubjectIds: string[];
};

export type AthenaSemanticMacroPreviewComponentPayload = {
    templateId: string;
    conceptId: string;
    implementationId?: string;
    title: string;
    summary?: string;
    originAnchorId: string;
    properties: Record<string, AthenaSemanticMacroParameterValuePayload>;
    tags: string[];
};

export type AthenaSemanticMacroPreviewPortPayload = {
    componentTemplateId: string;
    portRoleId: string;
    title: string;
    originAnchorId: string;
};

export type AthenaSemanticMacroPreviewConnectionPayload = {
    templateId: string;
    fromComponentTemplateId: string;
    fromPortRoleId: string;
    toComponentTemplateId: string;
    toPortRoleId: string;
    title: string;
    summary?: string;
    originAnchorId: string;
};

export type AthenaSemanticMacroPreviewOriginAnchorPayload = {
    anchorId: string;
    subjectKind: string;
    templateId: string;
    derivedSubjectId?: string;
};

export type AthenaSemanticMacroPreviewPresentationConsequencePayload = {
    scope: string;
    templateId?: string;
    hintType: string;
    attributes: Record<string, string>;
    originAnchorId: string;
};

export type AthenaSemanticMacroPreviewPayload = {
    projectName: string;
    semanticPath: string;
    status: string;
    previewId?: string;
    title?: string;
    macroId: string;
    instantiationId: string;
    changes: AthenaSemanticMacroPreviewChangePayload[];
    components: AthenaSemanticMacroPreviewComponentPayload[];
    ports: AthenaSemanticMacroPreviewPortPayload[];
    connections: AthenaSemanticMacroPreviewConnectionPayload[];
    originAnchors: AthenaSemanticMacroPreviewOriginAnchorPayload[];
    presentationConsequences: AthenaSemanticMacroPreviewPresentationConsequencePayload[];
    warnings: string[];
    reason?: string;
};

export type AthenaSemanticMacroAcceptancePayload = {
    projectName: string;
    semanticPath: string;
    status: string;
    previewId: string;
    macroId: string;
    instantiationId: string;
    bundleId?: string;
    acceptedExpansion?: AthenaSemanticMacroAcceptedExpansionPayload;
    operations: AthenaSemanticMacroMutationOperationPayload[];
    affectedSemanticIds: string[];
    presentationConsequences: AthenaSemanticMacroPreviewPresentationConsequencePayload[];
    execution?: AthenaSemanticMacroAcceptanceExecutionPayload;
    inspection?: AthenaSemanticMacroAcceptanceInspectionPayload;
    semanticReview?: AthenaSemanticMacroAcceptanceReviewPayload;
    reason?: string;
};

export type AthenaSemanticMacroOriginInspectionPayload = {
    projectName: string;
    semanticPath: string;
    status: string;
    subjectId?: string;
    instantiationId?: string;
    commandId?: string;
    bundleId?: string;
    acceptedExpansion?: AthenaSemanticMacroAcceptedExpansionPayload;
    matchedMembership?: AthenaSemanticMacroExpansionMembershipPayload;
    reason?: string;
};

export type AthenaSemanticMacroAcceptedExpansionPayload = {
    expansionId: string;
    previewId: string;
    macroId: string;
    instantiationId: string;
    packageName: string;
    packageVersion?: string;
    parameterValues: Record<string, AthenaSemanticMacroParameterValuePayload>;
    memberships: AthenaSemanticMacroExpansionMembershipPayload[];
};

export type AthenaSemanticMacroExpansionMembershipPayload = {
    subjectId: string;
    role?: string;
};

export type AthenaSemanticMacroMutationOperationPayload = {
    operationId: string;
    kind: string;
    subjectId?: string;
    relatedSubjectIds: string[];
    templateId?: string;
    conceptId?: string;
    implementationId?: string;
    componentTemplateId?: string;
    portRoleId?: string;
    membershipCount?: number;
    summary?: string;
};

export type AthenaSemanticMacroAcceptanceExecutionPayload = {
    commandKind: string;
    outcome: string;
    commandId: string;
    changedSemanticIds: string[];
};

export type AthenaSemanticMacroAcceptanceInspectionPayload = {
    projectName: string;
    source: string;
    affectedCommandIds: string[];
    affectedSemanticIds: string[];
};

export type AthenaSemanticMacroAcceptanceReviewPayload = {
    authoredChangeCount: number;
    derivedConsequenceCount: number;
    engineeringImpactCount: number;
};

export type AthenaSemanticMacroCatalogRequestEnvelope<TModel = unknown> = {
    method: 'athena/semanticMacroCatalog';
    params: AthenaSemanticMacroCatalogRequestParams;
    model?: TModel;
};

export type AthenaSemanticMacroValidationRequestEnvelope<TModel = unknown> = {
    method: 'athena/semanticMacroValidation';
    params: AthenaSemanticMacroValidationParams;
    model?: TModel;
};

export type AthenaSemanticMacroPreviewRequestEnvelope<TModel = unknown> = {
    method: 'athena/semanticMacroPreview';
    params: AthenaSemanticMacroPreviewParams;
    model?: TModel;
};

export type AthenaSemanticMacroAcceptanceRequestEnvelope<TModel = unknown> = {
    method: 'athena/semanticMacroAccept';
    params: AthenaSemanticMacroAcceptanceParams;
    model?: TModel;
};

export type AthenaSemanticMacroOriginInspectionRequestEnvelope<TModel = unknown> = {
    method: 'athena/semanticMacroOriginInspection';
    params: AthenaSemanticMacroOriginInspectionParams;
    model?: TModel;
};

export function buildAthenaSemanticMacroCatalogRequest<TModel = unknown>(
    params: AthenaSemanticMacroCatalogRequestParams = {},
    model?: TModel
): AthenaSemanticMacroCatalogRequestEnvelope<TModel> {
    return {
        method: 'athena/semanticMacroCatalog',
        params,
        model
    };
}

export function buildAthenaSemanticMacroValidationRequest<TModel = unknown>(
    params: AthenaSemanticMacroValidationParams,
    model?: TModel
): AthenaSemanticMacroValidationRequestEnvelope<TModel> {
    return {
        method: 'athena/semanticMacroValidation',
        params,
        model
    };
}

export function buildAthenaSemanticMacroPreviewRequest<TModel = unknown>(
    params: AthenaSemanticMacroPreviewParams,
    model?: TModel
): AthenaSemanticMacroPreviewRequestEnvelope<TModel> {
    return {
        method: 'athena/semanticMacroPreview',
        params,
        model
    };
}

export function buildAthenaSemanticMacroAcceptanceRequest<TModel = unknown>(
    params: AthenaSemanticMacroAcceptanceParams,
    model?: TModel
): AthenaSemanticMacroAcceptanceRequestEnvelope<TModel> {
    return {
        method: 'athena/semanticMacroAccept',
        params,
        model
    };
}

export function buildAthenaSemanticMacroOriginInspectionRequest<TModel = unknown>(
    params: AthenaSemanticMacroOriginInspectionParams,
    model?: TModel
): AthenaSemanticMacroOriginInspectionRequestEnvelope<TModel> {
    return {
        method: 'athena/semanticMacroOriginInspection',
        params,
        model
    };
}
