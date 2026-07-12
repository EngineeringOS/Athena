/**
 * Translation-only input snapshot derived from the Athena-owned projection-session transport.
 *
 * This type exists only so the adapter can transform upstream payloads into GLSP-shaped data.
 * It is not a semantic authority and must stay rebuildable from Athena-owned state.
 */
export type AthenaGLSPProjectionSource = {
    projectName: string;
    semanticPath: string;
    activeViewId: string;
    supportedViews: AthenaGLSPProjectionViewSource[];
    governedCommands: AthenaGLSPGovernedCommandSource[];
    status: string;
    readyProjection?: AthenaGLSPReadyProjectionSource;
    unavailableReason?: string;
    diagnostics: AthenaGLSPDiagnosticSource[];
};

export type AthenaGLSPProjectionViewSource = {
    viewId: string;
    displayName: string;
    description: string;
    familyId?: string;
    ownershipContract: AthenaGLSPProjectionOwnershipContractSource;
};

export type AthenaGLSPProjectionOwnershipContractSource = {
    interactivity: string;
    displayScopes: string[];
    semanticCommandIds: string[];
    projectionCommandIds: string[];
    transientInteractionKinds: string[];
    persistedProjectionMetadataKeys: string[];
};

export type AthenaGLSPGovernedCommandSource = {
    commandId: string;
    displayName: string;
    description: string;
    requiredArguments: string[];
};

export type AthenaGLSPReadyProjectionSource = {
    viewId: string;
    familyId?: string;
    systemName: string;
    canvasWidth: number;
    canvasHeight: number;
    activeSheetId?: string;
    sheets?: AthenaGLSPSheetSource[];
    notationPack?: AthenaGLSPNotationPackSource;
    crossReferences?: AthenaGLSPCrossReferenceSource[];
    electricalAnchors?: AthenaGLSPElectricalAnchorSource[];
    electricalConnectionEndpoints?: AthenaGLSPElectricalConnectionEndpointSource[];
    electricalRoutingCorridors?: AthenaGLSPElectricalRoutingCorridorSource[];
    activeRenderContributions: AthenaGLSPRenderContributionSource[];
    components: AthenaGLSPComponentSource[];
    connections: AthenaGLSPConnectionSource[];
    labels: AthenaGLSPLabelSource[];
};

export type AthenaGLSPSheetSource = {
    sheetId: string;
    displayName: string;
    order: number;
    previousSheetId?: string;
    nextSheetId?: string;
    subjectSemanticIds: string[];
};

export type AthenaGLSPNotationSubjectSource = {
    semanticId: string;
    symbolKey: string;
    labelPolicy: string;
    markerKeys: string[];
};

export type AthenaGLSPNotationPackSource = {
    packId: string;
    displayName: string;
    subjects: AthenaGLSPNotationSubjectSource[];
};

export type AthenaGLSPCrossReferenceSource = {
    semanticId: string;
    kind: string;
    sheetIds: string[];
    occurrenceIds: string[];
};

export type AthenaGLSPElectricalAnchorSource = {
    anchorId: string;
    portSemanticId: string;
    ownerSemanticId: string;
    nodeId: string;
    labelId?: string;
    x: number;
    y: number;
    side: string;
};

export type AthenaGLSPElectricalConnectionEndpointSource = {
    endpointId: string;
    projectionConnectionId: string;
    connectionSemanticId: string;
    endpointRole: string;
    portSemanticId: string;
    anchorId: string;
};

export type AthenaGLSPPoint = {
    x: number;
    y: number;
};

export type AthenaGLSPElectricalRoutingCorridorSource = {
    corridorId: string;
    projectionConnectionId: string;
    connectionSemanticId: string;
    sourceAnchorId: string;
    targetAnchorId: string;
    routingStyle: string;
    preferredBendPoints: AthenaGLSPPoint[];
};

export type AthenaGLSPRenderContributionSource = {
    pluginId: string;
    contributionId: string;
    displayName: string;
    description: string;
    rendererTarget: string;
    surfaceMappings: AthenaGLSPRenderSurfaceMappingSource[];
};

export type AthenaGLSPRenderSurfaceMappingSource = {
    surface: string;
    tokens: Record<string, string>;
};

export type AthenaGLSPComponentSource = {
    projectionId: string;
    semanticId: string;
    label: string;
    x: number;
    y: number;
    width: number;
    height: number;
};

export type AthenaGLSPConnectionSource = {
    projectionId: string;
    semanticId: string;
    x1: number;
    y1: number;
    x2: number;
    y2: number;
};

export type AthenaGLSPLabelSource = {
    projectionId: string;
    semanticId: string;
    label: string;
    x: number;
    y: number;
    width: number;
    height: number;
};

export type AthenaGLSPDiagnosticSource = {
    severity: string;
    code: string;
    message: string;
    provenance?: string;
};
