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
    presentation?: AthenaGLSPPresentationDocumentSource;
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
export type AthenaGLSPPresentationDocumentSource = {
    canvasWidth: number;
    canvasHeight: number;
    primitivePacks: AthenaGLSPPresentationPrimitivePackSource[];
    compositePacks: AthenaGLSPPresentationCompositePackSource[];
    occurrences: AthenaGLSPPresentationOccurrenceSource[];
    connectors: AthenaGLSPPresentationConnectorSource[];
    representationFacts?: AthenaGLSPPresentationRepresentationFactSource[];
};
export type AthenaGLSPPresentationPrimitivePackSource = {
    packId: string;
    displayName: string;
    familyIds: string[];
    primitives: AthenaGLSPPresentationPrimitiveDefinitionSource[];
};
export type AthenaGLSPPresentationCompositePackSource = {
    packId: string;
    displayName: string;
    familyIds: string[];
    composites: AthenaGLSPPresentationCompositeDefinitionSource[];
};
export type AthenaGLSPPresentationPrimitiveDefinitionSource = {
    primitiveId: string;
    displayName: string;
    viewBoxWidth: number;
    viewBoxHeight: number;
    commands: AthenaGLSPPresentationShapeCommandSource[];
    textSlots: AthenaGLSPPresentationTextSlotSource[];
    anchors: AthenaGLSPPresentationAnchorDefinitionSource[];
    tokenDefaults: Record<string, string>;
    supportedOrientations: string[];
};
export type AthenaGLSPPresentationCompositeDefinitionSource = {
    compositeId: string;
    displayName: string;
    viewBoxWidth: number;
    viewBoxHeight: number;
    parts: AthenaGLSPPresentationCompositePartSource[];
    textSlots: AthenaGLSPPresentationTextSlotSource[];
    tokenDefaults: Record<string, string>;
    supportedOrientations: string[];
};
export type AthenaGLSPPresentationCompositePartSource = {
    partId: string;
    primitiveId: string;
    bounds: AthenaGLSPPresentationBoundsSource;
    tokenOverrides: Record<string, string>;
    orientation: string;
};
export type AthenaGLSPPresentationShapeCommandSource = {
    kind: string;
    bounds?: AthenaGLSPPresentationBoundsSource;
    start?: AthenaGLSPPoint;
    end?: AthenaGLSPPoint;
    center?: AthenaGLSPPoint;
    radius?: number;
    pathData?: string;
    strokeTokenKey?: string;
    strokeWidthTokenKey?: string;
    fillTokenKey?: string;
};
export type AthenaGLSPPresentationOccurrenceSource = {
    occurrenceId: string;
    semanticId: string;
    referenceKind: string;
    primitiveId?: string;
    compositeId?: string;
    bounds: AthenaGLSPPresentationBoundsSource;
    layer: string;
    displayLabel?: string;
    orientation: string;
    markerKeys: string[];
    textValues: Record<string, string>;
    anchorBindings: AthenaGLSPPresentationAnchorBindingSource[];
    tokenOverrides: Record<string, string>;
    sourceProjectionIds: string[];
};
export type AthenaGLSPPresentationConnectorSource = {
    occurrenceId: string;
    semanticId: string;
    primitiveId: string;
    routePoints: AthenaGLSPPoint[];
    layer: string;
    sourceAnchorId?: string;
    targetAnchorId?: string;
    sourcePortSemanticId?: string;
    targetPortSemanticId?: string;
    markerKeys: string[];
    tokenOverrides: Record<string, string>;
    sourceProjectionIds: string[];
};
export type AthenaGLSPPresentationBoundsSource = {
    x: number;
    y: number;
    width: number;
    height: number;
};
export type AthenaGLSPPresentationTextSlotSource = {
    slotId: string;
    origin: AthenaGLSPPoint;
    tokenKey: string;
};
export type AthenaGLSPPresentationAnchorDefinitionSource = {
    alias: string;
    point: AthenaGLSPPoint;
};
export type AthenaGLSPPresentationAnchorBindingSource = {
    alias: string;
    anchorId: string;
    portSemanticId?: string;
    ownerSemanticId?: string;
    sourceLabelId?: string;
};
export type AthenaGLSPPresentationRepresentationFactSource = {
    subjectId: string;
    occurrenceId: string;
    sourceProjectionIds: string[];
    symbol: AthenaGLSPSymbolAnatomySource;
    anatomy: AthenaGLSPPresentationAnatomySource;
    terminals: AthenaGLSPPresentationTerminalFactSource[];
    labels: AthenaGLSPLabelFactSource[];
};
export type AthenaGLSPSymbolAnatomySource = {
    familyId: string;
};
export type AthenaGLSPPresentationAnatomySource = {
    representationId: string;
    context: string;
    bounds: AthenaGLSPPresentationSizeSource;
    hotspot: AthenaGLSPPoint;
    primitives: AthenaGLSPPresentationPrimitiveSource[];
    terminals: AthenaGLSPPresentationTerminalPointSource[];
    labelAnchors: AthenaGLSPPresentationLabelAnchorSource[];
};
export type AthenaGLSPPresentationPrimitiveSource = {
    kind: 'line';
    primitiveId: string;
    start: AthenaGLSPPoint;
    end: AthenaGLSPPoint;
} | {
    kind: 'rectangle';
    primitiveId: string;
    origin: AthenaGLSPPoint;
    size: AthenaGLSPPresentationSizeSource;
} | {
    kind: 'polyline';
    primitiveId: string;
    points: AthenaGLSPPoint[];
} | {
    kind: 'circle';
    primitiveId: string;
    center: AthenaGLSPPoint;
    radius: number;
};
export type AthenaGLSPPresentationSizeSource = {
    width: number;
    height: number;
};
export type AthenaGLSPPresentationTerminalPointSource = {
    terminalId: string;
    role: string;
    localPoint: AthenaGLSPPoint;
    side: string;
    notation: AthenaGLSPTerminalNotationSource;
};
export type AthenaGLSPPresentationTerminalFactSource = {
    presentationTerminalId: string;
    subjectId: string;
    occurrenceId: string;
    portId: string;
    physicalTerminalId: string;
    side: string;
    routeAnchor: {
        anchorId: string;
        point: AthenaGLSPPoint;
    };
    notation: AthenaGLSPTerminalNotationSource;
};
export type AthenaGLSPTerminalNotationSource = {
    marker: string;
    number: string;
};
export type AthenaGLSPPresentationLabelAnchorSource = {
    anchorId: string;
    role: string;
    point: AthenaGLSPPoint;
};
export type AthenaGLSPLabelFactSource = {
    labelId: string;
    subjectId: string;
    occurrenceId: string;
    role: string;
    value: string;
    anchor: AthenaGLSPPresentationLabelAnchorSource;
};
//# sourceMappingURL=athena-glsp-projection-source.d.ts.map