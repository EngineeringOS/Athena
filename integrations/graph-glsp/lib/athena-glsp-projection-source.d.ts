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
    projectionRegionIds?: string[];
    projectionConstructIds?: string[];
    spatialFacts?: AthenaGLSPSpatialFactsSource;
};
export type AthenaGLSPSpatialFactsSource = {
    viewId: string;
    activeSheetId?: string;
    sheets: AthenaGLSPSpatialSheetFactsSource[];
};
export type AthenaGLSPSpatialSheetFactsSource = {
    sheetId: string;
    extent: AthenaGLSPRectSource;
    drawingArea: AthenaGLSPRectSource;
    occurrences: AthenaGLSPSpatialOccurrenceFactsSource[];
    regions: AthenaGLSPSpatialRegionFactsSource[];
    constructs: AthenaGLSPSpatialConstructFactsSource[];
    anchors: AthenaGLSPSpatialAnchorFactsSource[];
    routes: AthenaGLSPSpatialRouteFactsSource[];
    lanes: AthenaGLSPSpatialLaneFactsSource[];
    gridReferences: AthenaGLSPSpatialGridReferenceFactsSource[];
    quality: AthenaGLSPSpatialQualityFactsSource;
};
export type AthenaGLSPRectSource = {
    x: number;
    y: number;
    width: number;
    height: number;
};
export type AthenaGLSPSpatialPointSource = {
    x: number;
    y: number;
};
export type AthenaGLSPSpatialOccurrenceFactsSource = {
    occurrenceId: string;
    semanticId: string;
    regionId: string;
    bounds: AthenaGLSPRectSource;
};
export type AthenaGLSPSpatialRegionFactsSource = {
    regionId: string;
    bounds: AthenaGLSPRectSource;
    memberOccurrenceIds: string[];
};
export type AthenaGLSPSpatialConstructFactsSource = {
    constructId: string;
    kind: string;
    name?: string;
    bounds: AthenaGLSPRectSource;
    memberOccurrenceIds: string[];
};
export type AthenaGLSPSpatialAnchorFactsSource = {
    anchorId: string;
    occurrenceId: string;
    portSemanticId: string;
    side: string;
    point: AthenaGLSPSpatialPointSource;
};
export type AthenaGLSPSpatialRouteFactsSource = {
    routeId: string;
    projectionConnectionId: string;
    connectionId: string;
    sourceAnchorId: string;
    targetAnchorId: string;
    laneId: string;
    points: AthenaGLSPSpatialPointSource[];
};
export type AthenaGLSPSpatialLaneFactsSource = {
    laneId: string;
    orientation: string;
    coordinate: number;
    routeIds: string[];
};
export type AthenaGLSPSpatialGridReferenceFactsSource = {
    gridReferenceId: string;
    subjectId: string;
    cellReference: string;
    rowLabel: string;
    columnNumber: number;
};
export type AthenaGLSPSpatialQualityFactsSource = {
    occurrenceOverlapCount: number;
    constructContainmentFailureCount: number;
    routeBodyIntersectionCount: number;
    routeCrossingCount: number;
    twistCount: number;
    usedLaneCount: number;
    peakRoutesPerLane: number;
    density: number;
    occupancy: number;
};
export type AthenaGLSPSheetSource = {
    sheetId: string;
    displayName: string;
    role?: string;
    order: number;
    previousSheetId?: string;
    nextSheetId?: string;
    subjectSemanticIds: string[];
    policyEvidence?: AthenaGLSPSheetPolicyEvidenceSource;
    publication?: AthenaGLSPSheetPublicationSource;
};
export type AthenaGLSPSheetPolicyEvidenceSource = {
    policyId: string;
    policyVersion: string;
    policyDeterministicIdentity: string;
    sheetViewRole: string;
    sheetViewRoleOrder: number;
};
export type AthenaGLSPSheetPublicationSource = {
    pageSize: AthenaGLSPSheetPageSizeSource;
    frame: AthenaGLSPSheetFrameSource;
    coordinateZones: AthenaGLSPSheetCoordinateZoneSource[];
    titleBlock: AthenaGLSPSheetTitleBlockSource;
    revisionMetadata: AthenaGLSPSheetRevisionMetadataSource;
    viewComposition: AthenaGLSPSheetViewCompositionSource;
};
export type AthenaGLSPSheetPageSizeSource = {
    format: string;
    orientation: string;
};
export type AthenaGLSPSheetFrameSource = {
    frameId: string;
    style: string;
};
export type AthenaGLSPSheetCoordinateZoneSource = {
    zoneId: string;
    label: string;
    order: number;
};
export type AthenaGLSPSheetTitleBlockSource = {
    sheetTitle: string;
    sheetFamily: string;
    sheetNumber: string;
};
export type AthenaGLSPSheetRevisionMetadataSource = {
    revisionCode: string;
    revisionNote: string;
};
export type AthenaGLSPSheetViewCompositionSource = {
    primaryViewId: string;
    primarySheetOrder: number;
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
    crossReferenceId?: string;
    sheetIds: string[];
    occurrenceIds: string[];
    links?: AthenaGLSPCrossReferenceLinkSource[];
};
export type AthenaGLSPCrossReferenceLinkSource = {
    semanticId: string;
    sourceSheetId: string;
    targetSheetId: string;
    sourceOccurrenceId: string;
    targetOccurrenceId: string;
    compactNotation: string;
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
    sheetSurface?: AthenaGLSPPresentationSheetSurfaceSource;
    primitivePacks: AthenaGLSPPresentationPrimitivePackSource[];
    compositePacks: AthenaGLSPPresentationCompositePackSource[];
    occurrences: AthenaGLSPPresentationOccurrenceSource[];
    graphicOccurrences?: AthenaGLSPPresentationGraphicOccurrenceSource[];
    connectors: AthenaGLSPPresentationConnectorSource[];
    connectionMarkers?: AthenaGLSPPresentationConnectionMarkerSource[];
    paintPlan?: AthenaGLSPPresentationPaintPlanSource;
    representationFacts?: AthenaGLSPPresentationRepresentationFactSource[];
    referenceMarkers?: AthenaGLSPPresentationReferenceMarkerSource[];
    drawingComposition?: AthenaGLSPDrawingCompositionSource;
};
export type AthenaGLSPPresentationPaintPlanSource = {
    items: AthenaGLSPPresentationPaintItemSource[];
};
export type AthenaGLSPPresentationPaintItemSource = {
    itemId: string;
    targetId: string;
    kind: string;
    visible: boolean;
    order: number;
};
export type AthenaGLSPPresentationGraphicOccurrenceSource = {
    occurrenceId: string;
    semanticSubjectId: string;
    physicalComponentId: string;
    functionId?: string;
    bounds?: AthenaGLSPPresentationBoundsSource;
    orientation: string;
    deviceLabel: string;
    modelLabel?: string;
    packageId: string;
    definitionId: string;
    bindingRuleId: string;
    graphic: AthenaGLSPGraphicPrimitiveDocumentSource;
    terminalBindings: AthenaGLSPPresentationGraphicTerminalBindingSource[];
    labels: AthenaGLSPPresentationGraphicLabelSource[];
    sourceProvenance: string[];
    authorities: AthenaGLSPPresentationGraphicOccurrenceAuthoritiesSource;
};
export type AthenaGLSPPresentationGraphicOccurrenceAuthoritiesSource = {
    graphic: string;
    placement: string;
    material: string;
};
export type AthenaGLSPPresentationGraphicTerminalBindingSource = {
    portSemanticId: string;
    anchorId: string;
    terminalIdentity: string;
    point: AthenaGLSPPoint;
    labelPoint: AthenaGLSPPoint;
    side: string;
};
export type AthenaGLSPPresentationGraphicLabelSource = {
    labelId: string;
    role: string;
    value: string;
    bounds: AthenaGLSPPresentationBoundsSource;
};
export type AthenaGLSPGraphicPrimitiveDocumentSource = {
    documentId?: string;
    bounds?: AthenaGLSPPresentationBoundsSource;
    primitives: AthenaGLSPGraphicPrimitiveSource[];
    provenanceSources: string[];
    forbiddenAuthorityClaims: string[];
};
export type AthenaGLSPGraphicPrimitiveSource = {
    primitiveId: string;
    kind: string;
    bounds?: AthenaGLSPPresentationBoundsSource;
    styleTokenId?: string;
    start?: AthenaGLSPPoint;
    end?: AthenaGLSPPoint;
    points?: AthenaGLSPPoint[];
    center?: AthenaGLSPPoint;
    origin?: AthenaGLSPPoint;
    radius?: number;
    startAngleDegrees?: number;
    sweepAngleDegrees?: number;
    text?: string;
    cornerRadius?: number;
    markerKind?: string;
    headSize?: number;
};
export type AthenaGLSPDrawingCompositionSource = {
    sheetId: string;
    policyId: string;
    contentBounds: AthenaGLSPPresentationBoundsSource;
    frameBounds: AthenaGLSPPresentationBoundsSource;
    drawingAreaBounds: AthenaGLSPPresentationBoundsSource;
    titleBlockBounds: AthenaGLSPPresentationBoundsSource;
    sheetBounds: AthenaGLSPPresentationBoundsSource;
    frameId: string;
    frameStyle: string;
    title: AthenaGLSPDrawingTitleSource;
    coordinateZones: AthenaGLSPDrawingCoordinateZoneSource[];
    structureSubjects: AthenaGLSPDrawingStructureSubjectSource[];
    structureFacts: AthenaGLSPDrawingStructureFactSource[];
    referencePlacements: AthenaGLSPDrawingReferencePlacementSource[];
    authorities: AthenaGLSPDrawingAuthoritiesSource;
};
export type AthenaGLSPDrawingTitleSource = {
    sheetTitle: string;
    sheetFamily: string;
    sheetNumber: string;
    revisionCode: string;
    revisionNote: string;
    pageFormat: string;
    orientation: string;
};
export type AthenaGLSPDrawingCoordinateZoneSource = {
    zoneId: string;
    axis: string;
    label: string;
    order: number;
    bounds: AthenaGLSPPresentationBoundsSource;
};
export type AthenaGLSPDrawingStructureSubjectSource = {
    subjectId: string;
    representationIdentity: string;
    bounds: AthenaGLSPPresentationBoundsSource;
    representationAuthority: string;
    boundsAuthority: string;
};
export type AthenaGLSPDrawingStructureFactSource = {
    factId: string;
    kind: string;
    axis?: string;
    bounds?: AthenaGLSPPresentationBoundsSource;
    start?: AthenaGLSPPoint;
    end?: AthenaGLSPPoint;
    memberIds: string[];
    authority: string;
    boundsAuthority?: string;
};
export type AthenaGLSPDrawingReferencePlacementSource = {
    placementId: string;
    referenceId: string;
    subjectId: string;
    role: string;
    representationIdentity: string;
    bounds?: AthenaGLSPPresentationBoundsSource;
    anchor?: AthenaGLSPPoint;
    compactNotation: string;
    anatomy?: AthenaGLSPPresentationAnatomySource;
};
export type AthenaGLSPDrawingAuthoritiesSource = {
    contentBounds: string;
    bounds: string;
    projection: string;
    representation: string;
    structure: string;
    policy: string;
};
export type AthenaGLSPPresentationSheetSurfaceSource = {
    surfaceId: string;
    source: string;
    frame: AthenaGLSPPresentationSheetFrameSource;
    grid: AthenaGLSPPresentationSheetGridSource;
    titleBlock: AthenaGLSPPresentationSheetTitleBlockSource;
    metadata: AthenaGLSPPresentationSheetMetadataSource;
};
export type AthenaGLSPPresentationSheetFrameSource = {
    width: number;
    height: number;
    margins?: AthenaGLSPPresentationSheetMarginsSource;
    zoneColumns?: string[];
    zoneRows?: string[];
};
export type AthenaGLSPPresentationSheetMarginsSource = {
    top: number;
    right: number;
    bottom: number;
    left: number;
};
export type AthenaGLSPPresentationSheetGridSource = {
    majorStep: number;
    minorStep: number;
};
export type AthenaGLSPPresentationSheetTitleBlockSource = {
    fields: AthenaGLSPPresentationSheetTitleBlockFieldSource[];
};
export type AthenaGLSPPresentationSheetTitleBlockFieldSource = {
    role: string;
    label: string;
    value: string;
};
export type AthenaGLSPPresentationSheetMetadataSource = {
    sheetSize: string;
    orientation: string;
    projectionPolicyId: string;
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
    origin?: AthenaGLSPPoint;
    radius?: number;
    text?: string;
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
    lineClassId: string;
    line: AthenaGLSPPresentationConnectorLineSource;
    routeId: string;
    bundleId: string;
    laneId: string;
    laneRouteIds: string[];
    selectedChannelIds: string[];
    labels: AthenaGLSPPresentationConnectorLabelSource[];
    quality: string;
    sourceEndpoint: AthenaGLSPPresentationConnectorEndpointSource;
    targetEndpoint: AthenaGLSPPresentationConnectorEndpointSource;
    layer: string;
    markerIds: string[];
    tokenOverrides: Record<string, string>;
    sourceProjectionIds: string[];
    trace?: AthenaGLSPPresentationTraceSource;
    sourceSpan?: AthenaGLSPPresentationSourceSpanSource;
};
export type AthenaGLSPPresentationConnectorLineSource = {
    classId: string;
    lineKind: string;
    lineStyleId: string;
    weight: number;
    style: string;
    colorKey: string;
    endpointBehavior: string;
    labelPolicy: string;
    crossingBehavior: string;
    policyId: string;
    compilerSnapshotId: string;
};
export type AthenaGLSPPresentationConnectorLabelSource = {
    labelId: string;
    text: string;
    point: AthenaGLSPPoint;
    bounds: AthenaGLSPPresentationBoundsSource;
    labelClassId: string;
    display: string;
    sourceProvenance: string[];
    compilerSnapshotId: string;
};
export type AthenaGLSPPresentationConnectorEndpointSource = {
    portSemanticId: string;
    bindingId: string;
    occurrenceId: string;
    anchorId: string;
    point: AthenaGLSPPoint;
    sourceProvenance: string[];
    trace?: AthenaGLSPPresentationTraceSource;
};
export type AthenaGLSPPresentationTraceSource = {
    sourceProvenance?: string[];
    sourceProjectionIds?: string[];
    compilerStage?: string;
    compilerSnapshotId?: string;
    sourceSpan?: AthenaGLSPPresentationSourceSpanSource;
    packageTrace?: unknown;
};
export type AthenaGLSPPresentationSourceSpanSource = {
    file: string;
    startLine: number;
    startColumn: number;
    endLine: number;
    endColumn: number;
};
export type AthenaGLSPPresentationConnectionMarkerSource = {
    markerId: string;
    kind: string;
    point: AthenaGLSPPoint;
    routeIds: string[];
    connectorIds: string[];
    semanticId?: string;
    joined: boolean;
    appearanceClassId: string;
    sourceProjectionIds: string[];
    sourceProvenance: string[];
    compilerSnapshotId: string;
};
export type AthenaGLSPPresentationReferenceMarkerSource = {
    markerId: string;
    markerKind: string;
    relationType: string;
    selectedSheetViewId: string;
    sourceOccurrenceId: string;
    targetOccurrenceId: string;
    sourceIdentity: string;
    targetIdentity: string;
    sourceDocumentLocation: AthenaGLSPDocumentLocationSource;
    targetDocumentLocation: AthenaGLSPDocumentLocationSource;
    compactNotation: string;
    sourceProjectionIds: string[];
};
export type AthenaGLSPDocumentLocationSource = {
    sheetViewId: string;
    zoneId: string;
    displayNotation: string;
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
    packageTrace?: AthenaGLSPPresentationPackageTraceSource;
};
export type AthenaGLSPPresentationPackageTraceSource = {
    engineeringPackageId: string;
    engineeringPackageVersion: string;
    presentationProfileId: string;
    bindingManifestId: string;
    representationPackageId: string;
    representationPackageVersion: string;
    descriptorId: string;
    graphicResourceId: string;
    variant: string;
    anchorMapSummary: string[];
    labelBindingSummary: string[];
    resolverStage: string;
    rendererFallbackAccepted: boolean;
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
} | {
    kind: 'text';
    primitiveId: string;
    origin: AthenaGLSPPoint;
    text: string;
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