export type AthenaAuthoringValuePayload = {
    kind: 'text' | 'symbol' | 'boolean' | 'integer';
    text?: string;
    booleanValue?: boolean;
    integerValue?: number;
};

export type AthenaAuthoringPreviewParams = {
    intentId: string;
    intentKind: 'create-component' | 'update-component-properties' | 'connect-ports' | 'semantic-relationship' | 'reveal-subject';
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
    relationshipType?: string;
    sourceSubjectId?: string;
    targetSubjectId?: string;
    projectionViewId?: string;
    projectionOccurrenceId?: string;
    persistenceSourceUri?: string;
    provenance?: string;
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

export function buildCreateComponentPreviewRequest(input: {
    systemSemanticId: string;
    conceptId: string;
    preferredImplementationId?: string;
    suggestedName?: string;
    originDetail?: string;
    intentId?: string;
}): AthenaAuthoringPreviewParams {
    return {
        intentId: input.intentId ?? `intent-${Date.now()}`,
        intentKind: 'create-component',
        originSurface: 'palette',
        originDetail: input.originDetail,
        parentIdentity: input.systemSemanticId,
        conceptId: input.conceptId,
        preferredImplementationId: input.preferredImplementationId,
        suggestedName: input.suggestedName,
    };
}

export function buildUpdateComponentPropertiesPreviewRequest(input: {
    componentId: string;
    name?: string;
    label?: string;
    description?: string;
    preferredImplementationId?: string;
    originDetail?: string;
    intentId?: string;
}): AthenaAuthoringPreviewParams {
    const properties = Object.fromEntries([
        toOptionalPreviewProperty('name', input.name, 'symbol'),
        toOptionalPreviewProperty('label', input.label, 'text'),
        toOptionalPreviewProperty('description', input.description, 'text'),
        toOptionalPreviewProperty('preferredImplementationId', input.preferredImplementationId, 'symbol'),
    ].filter((entry): entry is [string, AthenaAuthoringValuePayload] => !!entry));

    return {
        intentId: input.intentId ?? `intent-${Date.now()}`,
        intentKind: 'update-component-properties',
        originSurface: 'inspector',
        originDetail: input.originDetail,
        componentId: input.componentId,
        properties,
    };
}

export function buildConnectPortsPreviewRequest(input: {
    sourcePortId: string;
    targetPortId: string;
    originDetail?: string;
    intentId?: string;
}): AthenaAuthoringPreviewParams {
    return {
        intentId: input.intentId ?? `intent-${Date.now()}`,
        intentKind: 'connect-ports',
        originSurface: 'graph',
        originDetail: input.originDetail,
        sourcePortId: input.sourcePortId,
        targetPortId: input.targetPortId,
    };
}

export function buildSemanticRelationshipPreviewRequest(input: {
    relationshipType?: string;
    sourceSubjectId: string;
    targetSubjectId: string;
    projectionViewId?: string;
    projectionOccurrenceId?: string;
    persistenceSourceUri?: string;
    provenance?: string;
    originDetail?: string;
    intentId?: string;
}): AthenaAuthoringPreviewParams {
    return {
        intentId: input.intentId ?? `intent-${Date.now()}`,
        intentKind: 'semantic-relationship',
        originSurface: 'graph',
        originDetail: input.originDetail,
        relationshipType: input.relationshipType ?? 'ElectricalConnectionRelationship',
        sourceSubjectId: input.sourceSubjectId,
        targetSubjectId: input.targetSubjectId,
        projectionViewId: input.projectionViewId,
        projectionOccurrenceId: input.projectionOccurrenceId,
        persistenceSourceUri: input.persistenceSourceUri,
        provenance: input.provenance,
    };
}

export function buildAuthoringDecisionRequest(input: {
    previewId: string;
    intentId: string;
    decision: 'accept' | 'accepted' | 'reject' | 'rejected';
    note?: string;
}): AthenaAuthoringDecisionParams {
    return {
        previewId: input.previewId,
        intentId: input.intentId,
        decision: input.decision,
        note: input.note,
    };
}

function toOptionalPreviewProperty(
    name: string,
    value: string | undefined,
    kind: AthenaAuthoringValuePayload['kind'],
): [string, AthenaAuthoringValuePayload] | undefined {
    const normalizedValue = value?.trim();
    if (!normalizedValue) {
        return undefined;
    }
    return [
        name,
        {
            kind,
            text: normalizedValue,
        },
    ];
}
