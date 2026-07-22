export type AthenaAuthoringValuePayload = {
    kind: 'text' | 'symbol' | 'boolean' | 'integer';
    text?: string;
    booleanValue?: boolean;
    integerValue?: number;
};

export type AthenaAuthoringPreviewParams = {
    intentId: string;
    intentKind: 'create-entity' | 'update-entity-properties' | 'remove-entity' | 'semantic-relationship' | 'remove-semantic-relationship' | 'reveal-subject';
    originSurface: 'palette' | 'inspector' | 'graph' | 'form' | 'template' | 'ai' | 'api' | 'dsl';
    originDetail?: string;
    parentSubjectId?: string;
    conceptTemplateId?: string;
    conceptId?: string;
    preferredImplementationId?: string;
    suggestedName?: string;
    entitySubjectId?: string;
    properties?: Record<string, AthenaAuthoringValuePayload>;
    relationshipType?: string;
    sourceSubjectId?: string;
    targetSubjectId?: string;
    projectionViewId?: string;
    projectionOccurrenceId?: string;
    persistenceSourceUri?: string;
    provenance?: string;
    actor?: string;
    subjectId?: string;
    revealTargets?: Array<'source' | 'graph' | 'inspector' | 'semantic-scm'>;
};

export type AthenaAuthoringPreviewChangePayload = {
    kind: string;
    title: string;
    summary?: string;
    affectedSubjectIdentities: string[];
};

export type AthenaAuthoringRevisionGuardPayload = {
    semanticSnapshotId: string;
    sourceUri: string;
    documentVersion: number;
    contentSha256: string;
};

export type AthenaAuthoringNestedPortEvidencePayload = {
    name: string;
    direction: string;
    signalOrMedium: string;
    semanticId: string;
};

export type AthenaAuthoringSourceEditEvidencePayload = {
    uri: string;
    startOffset: number;
    endOffset: number;
    admittedText: string;
    selectionStartOffset: number | null;
    selectionEndOffset: number | null;
    affectedSemanticIds: string[];
    revisionGuard: AthenaAuthoringRevisionGuardPayload;
};

export type AthenaAuthoringEntityCreationEvidencePayload = {
    canonicalTag: string;
    semanticType: string;
    model: string | null;
    nestedPorts: AthenaAuthoringNestedPortEvidencePayload[];
    affectedSemanticIds: string[];
    sourceEdit: AthenaAuthoringSourceEditEvidencePayload;
    representationId: string;
    compositionTargetId: string;
    projectionOccurrenceIds: string[];
};

export type AthenaAuthoringRelationshipRoutePreviewPayload = {
    routeId: string;
    quality: string;
    sourceAnchorId: string | null;
    targetAnchorId: string | null;
    pointCount: number;
};

export type AthenaAuthoringRelationshipEvidencePayload = {
    sourceSubjectId: string;
    targetSubjectId: string;
    relationshipType: string;
    compatibility: 'compatible' | 'incompatible' | 'not-evaluated';
    affectedSemanticIds: string[];
    sourceEdit?: AthenaAuthoringSourceEditEvidencePayload;
    routePreview?: AthenaAuthoringRelationshipRoutePreviewPayload;
};

export type AthenaAuthoringDiagnosticPayload = {
    code: string;
    message: string;
    authority: string;
    lifecycleStage: string;
    recoveryAction?: string;
};

export const AthenaAuthoringLifecycleDiagnosticCodes = {
    stopDownstream: 'authoring.validation.stop-downstream',
    projectionFailedAfterCommit: 'authoring.projection.failed-after-commit',
} as const;

export type AthenaAuthoringPreviewPayload = {
    previewId: string;
    intentId: string;
    intentKind: string;
    originSurface: string;
    originDetail?: string;
    status: string;
    title: string;
    changes: AthenaAuthoringPreviewChangePayload[];
    revisionGuard?: AthenaAuthoringRevisionGuardPayload;
    warnings: string[];
    sourceImpact?: AthenaAuthoringSourceEditPayload;
    acceptanceEligible: boolean;
    diagnostics: AthenaAuthoringDiagnosticPayload[];
    entityCreationEvidence?: AthenaAuthoringEntityCreationEvidencePayload;
    relationshipEvidence?: AthenaAuthoringRelationshipEvidencePayload;
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
    startOffset?: number;
    endOffset?: number;
    range: AthenaAuthoringSourceRangePayload;
    newText: string;
    selectionStartOffset?: number | null;
    selectionEndOffset?: number | null;
    selectionRange?: AthenaAuthoringSourceRangePayload;
    suggestedSemanticId?: string;
    revisionGuard?: AthenaAuthoringRevisionGuardPayload;
    appliedByAuthority?: boolean;
};

export type AthenaAuthoringDecisionParams = {
    previewId: string;
    intentId: string;
    decision: 'accept' | 'accepted' | 'reject' | 'rejected' | 'cancel' | 'cancelled';
    note?: string;
};

export type AthenaAuthoringPreviewDecisionPayload = {
    projectName: string;
    semanticPath: string;
    status: string;
    preview?: AthenaAuthoringPreviewPayload;
    sourceEdit?: AthenaAuthoringSourceEditPayload;
    transactionResult?: AthenaAuthoringTransactionResultPayload;
    reason?: string;
};

export type AthenaAuthoringTransactionResultPayload = {
    lifecycleState: string;
    committedRevision?: AthenaAuthoringRevisionGuardPayload;
    mutationId?: string;
    affectedSemanticIds: string[];
    projectionOccurrenceIds: string[];
    diagnostics: AthenaAuthoringDiagnosticPayload[];
};

export type AthenaAuthoringDecisionGuardPayload = {
    status?: string;
    transactionResult?: {
        lifecycleState?: string;
        diagnostics?: AthenaAuthoringDiagnosticPayload[];
    };
    reason?: string;
};

export function buildCreateEntityPreviewRequest(input: {
    systemSemanticId: string;
    conceptTemplateId: string;
    conceptId: string;
    actor: string;
    preferredImplementationId?: string;
    suggestedName?: string;
    model?: string;
    originSurface?: AthenaAuthoringPreviewParams['originSurface'];
    originDetail?: string;
    intentId?: string;
}): AthenaAuthoringPreviewParams {
    const properties = Object.fromEntries([
        toOptionalPreviewProperty('model', input.model, 'text'),
    ].filter((entry): entry is [string, AthenaAuthoringValuePayload] => !!entry));
    return {
        intentId: input.intentId ?? `intent-${Date.now()}`,
        intentKind: 'create-entity',
        originSurface: input.originSurface ?? 'palette',
        originDetail: input.originDetail,
        parentSubjectId: input.systemSemanticId,
        conceptTemplateId: input.conceptTemplateId,
        conceptId: input.conceptId,
        actor: input.actor,
        preferredImplementationId: input.preferredImplementationId,
        suggestedName: input.suggestedName,
        ...(Object.keys(properties).length > 0 ? { properties } : {}),
    };
}

export function buildUpdateEntityPropertiesPreviewRequest(input: {
    entitySubjectId: string;
    actor: string;
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
        intentKind: 'update-entity-properties',
        originSurface: 'inspector',
        originDetail: input.originDetail,
        entitySubjectId: input.entitySubjectId,
        actor: input.actor,
        properties,
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
    decision: 'accept' | 'accepted' | 'reject' | 'rejected' | 'cancel' | 'cancelled';
    note?: string;
}): AthenaAuthoringDecisionParams {
    return {
        previewId: input.previewId,
        intentId: input.intentId,
        decision: input.decision,
        note: input.note,
    };
}

export function isAuthoringDecisionCommitted(decision: AthenaAuthoringDecisionGuardPayload | undefined): boolean {
    const lifecycleState = decision?.transactionResult?.lifecycleState?.trim().toLowerCase();
    const diagnostics = decision?.transactionResult?.diagnostics ?? [];
    const committedLifecycle = lifecycleState === 'committed' ||
        lifecycleState === 'reprojected' ||
        lifecycleState === 'projection-failed';
    return decision?.status === 'updated' && committedLifecycle &&
        diagnostics.every(diagnostic => diagnostic.code === AthenaAuthoringLifecycleDiagnosticCodes.projectionFailedAfterCommit);
}

export function collectAuthoringDecisionDiagnostics(
    decision: AthenaAuthoringDecisionGuardPayload | undefined,
): string {
    const diagnostics = decision?.transactionResult?.diagnostics ?? [];
    const diagnosticMessage = diagnostics
        .map(diagnostic => {
            const recovery = diagnostic.recoveryAction ? ` Recovery: ${diagnostic.recoveryAction}` : '';
            return `${diagnostic.code} [${diagnostic.authority}/${diagnostic.lifecycleStage}]: ${diagnostic.message}${recovery}`;
        })
        .join('; ');
    return [diagnosticMessage, decision?.reason]
        .filter((message): message is string => !!message?.trim())
        .join('; ');
}

export function sourceEditMatchesPreviewEvidence(
    sourceEdit: AthenaAuthoringSourceEditPayload | undefined,
    previewEvidence: AthenaAuthoringSourceEditEvidencePayload | undefined,
): boolean {
    if (!sourceEdit || !previewEvidence) {
        return false;
    }
    return sourceEdit.uri === previewEvidence.uri &&
        sourceEdit.newText === previewEvidence.admittedText &&
        sourceEdit.startOffset === previewEvidence.startOffset &&
        sourceEdit.endOffset === previewEvidence.endOffset &&
        (sourceEdit.selectionStartOffset ?? null) === previewEvidence.selectionStartOffset &&
        (sourceEdit.selectionEndOffset ?? null) === previewEvidence.selectionEndOffset &&
        revisionGuardsMatch(sourceEdit.revisionGuard, previewEvidence.revisionGuard) &&
        (!sourceEdit.suggestedSemanticId || previewEvidence.affectedSemanticIds.includes(sourceEdit.suggestedSemanticId));
}

function revisionGuardsMatch(
    left: AthenaAuthoringRevisionGuardPayload | undefined,
    right: AthenaAuthoringRevisionGuardPayload | undefined,
): boolean {
    return !!left && !!right &&
        left.semanticSnapshotId === right.semanticSnapshotId &&
        left.sourceUri === right.sourceUri &&
        left.documentVersion === right.documentVersion &&
        left.contentSha256 === right.contentSha256;
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
