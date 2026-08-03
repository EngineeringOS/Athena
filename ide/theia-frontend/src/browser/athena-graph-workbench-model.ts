import {
    AthenaGLSPDiagram,
    AthenaGLSPElectricalAnchorSource,
    AthenaGLSPElectricalConnectionEndpointSource,
    AthenaGLSPEdge,
    AthenaGLSPNode,
    AthenaGLSPPoint,
    AthenaGLSPRenderContributionSource
} from '@engineeringood/athena-graph-glsp';
import {
    AthenaGraphResolvedPresentationConnector,
    AthenaGraphResolvedPresentationConnectorEndpoint,
    AthenaGraphResolvedPresentationGraphicOccurrence,
    AthenaGraphResolvedPresentationOccurrence,
    AthenaGraphResolvedPresentationPart,
    resolvePresentationConnectors,
    resolvePresentationGraphicOccurrences,
    resolvePresentationOccurrences,
    resolvePresentationRepresentations,
    resolvePresentationReferenceMarkers,
    AthenaGraphResolvedPresentationRepresentation
} from './athena-graph-presentation-model';
import type { AthenaAuthoringSourceEditPayload } from './athena-authoring-protocol';

export type AthenaGraphSceneBounds = {
    minX: number;
    minY: number;
    maxX: number;
    maxY: number;
    width: number;
    height: number;
    centerX: number;
    centerY: number;
};

export type AthenaGraphViewportSize = {
    width: number;
    height: number;
};

export type AthenaGraphViewportTransform = {
    zoom: number;
    offsetX: number;
    offsetY: number;
};

export type AthenaGraphLayoutAdjustmentKind = 'place' | 'align' | 'group' | 'route' | 'label';

export type AthenaGraphLayoutAdjustmentIntent = {
    intentId: string;
    kind: 'place' | 'align' | 'group';
    subjectSemanticId: string;
    occurrenceId: string;
    viewId: string;
    sheetId: string;
    snapshotId: string;
    sourceUri: string;
    targetSemanticId?: string;
    relation?: 'near' | 'below' | 'aligned-with' | 'grouped-with';
    transientOnly: true;
    persisted: false;
};

export type AthenaGraphAuthoredLayoutIntentRelation = 'near' | 'below' | 'aligned-with' | 'grouped-with';

export type AthenaGraphAuthoredLayoutAxis = 'horizontal' | 'vertical';

export type AthenaGraphAuthoredLayoutIntentStatement = {
    subject: string;
    relation: AthenaGraphAuthoredLayoutIntentRelation;
    target: string;
    axis?: AthenaGraphAuthoredLayoutAxis;
    priority: 'preference';
};

export type AthenaGraphAuthoredLayoutIntent = {
    viewFamily: string;
    statements: AthenaGraphAuthoredLayoutIntentStatement[];
};

export type AthenaGraphLayoutAdjustmentCaptureResult =
    | { accepted: true; intent: AthenaGraphLayoutAdjustmentIntent }
    | { accepted: false; reason: string };

export type AthenaGraphLayoutMutationPreview = {
    previewId: string;
    intentId: string;
    subjectSemanticId: string;
    sourceUri: string;
    title: string;
    authoredIntent: AthenaGraphAuthoredLayoutIntent;
    layoutBlockSnippet: string;
    sourceEdit: AthenaAuthoringSourceEditPayload;
    persisted: false;
};

/** Pure presentation model used by the first Athena graphical workbench panel. */
export type AthenaGraphWorkbenchModel = {
    headerTitle: string;
    viewLabel: string;
    viewFamilyId?: string;
    isElectricalFamily: boolean;
    statusLabel: string;
    statusTone: 'ready' | 'warning' | 'idle';
    semanticPath: string;
    snapshotId: string;
    activeSheetId?: string;
    sheetCount: number;
    sheetViewSelector?: AthenaGraphWorkbenchSheetViewSelector;
    notationPackId?: string;
    crossReferenceCount: number;
    svgViewBox: string;
    metrics: {
        nodeCount: number;
        edgeCount: number;
        supportedViewCount: number;
        diagnosticCount: number;
    };
    supportedViews: Array<{
        viewId: string;
        displayName: string;
        description: string;
        familyId?: string;
        ownershipContract: AthenaGLSPDiagram['supportedViews'][number]['ownershipContract'];
        isActive: boolean;
    }>;
    diagnostics: AthenaGLSPDiagram['diagnostics'];
    activeRenderContributions: AthenaGLSPRenderContributionSource[];
    drawingComposition?: NonNullable<AthenaGLSPDiagram['presentation']>['drawingComposition'];
    sheetChrome: AthenaGraphWorkbenchSheetChrome;
    referenceMarkers: AthenaGraphWorkbenchReferenceMarker[];
    nodes: AthenaGraphWorkbenchNode[];
    edges: AthenaGraphWorkbenchEdge[];
    canvas: {
        width: number;
        height: number;
    };
    sceneBounds: AthenaGraphSceneBounds;
    surfaceTokens: AthenaGraphSurfaceTokens;
    emptyState?: {
        title: string;
        message: string;
    };
};

export type AthenaGraphWorkbenchProductSurface = {
    surfaceId: 'control-drawing' | 'cabinet';
    displayName: 'Control Drawing' | 'Cabinet';
    description: string;
    backingViewId: string;
    backingFamilyId?: string;
    isActive: boolean;
};

export function resolveAthenaGraphPrimaryProductSurface(
    supportedViews: AthenaGraphWorkbenchModel['supportedViews'],
): AthenaGraphWorkbenchProductSurface | undefined {
    const controlDrawingView = supportedViews.find(view =>
        view.viewId === 'schematic' && view.displayName === 'Control Drawing'
    );
    if (controlDrawingView) {
        return {
            surfaceId: 'control-drawing',
            displayName: 'Control Drawing',
            description: 'Professional engineering control drawing',
            backingViewId: controlDrawingView.viewId,
            backingFamilyId: controlDrawingView.familyId,
            isActive: controlDrawingView.isActive,
        };
    }
    const cabinetView = supportedViews.find(view => view.viewId === 'cabinet');
    if (cabinetView) {
        return {
            surfaceId: 'cabinet',
            displayName: 'Cabinet',
            description: 'Physical installation Cabinet projection',
            backingViewId: cabinetView.viewId,
            backingFamilyId: cabinetView.familyId,
            isActive: cabinetView.isActive,
        };
    }
    return undefined;
}

export function resolveAthenaGraphPrimaryProductActivationViewId(
    supportedViews: ReadonlyArray<Pick<AthenaGraphWorkbenchModel['supportedViews'][number], 'viewId' | 'displayName'>>,
    activeViewId: string | undefined,
): string | undefined {
    if (supportedViews.some(view => view.viewId === 'schematic' && view.displayName === 'Control Drawing')) {
        return activeViewId === 'schematic' ? undefined : 'schematic';
    }
    if (supportedViews.some(view => view.viewId === 'cabinet')) {
        return activeViewId === 'cabinet' ? undefined : 'cabinet';
    }
    return undefined;
}

export function requiresAthenaCabinetProductActivation(
    supportedViews: ReadonlyArray<Pick<AthenaGraphWorkbenchModel['supportedViews'][number], 'viewId' | 'displayName'>>,
    activeViewId: string | undefined,
): boolean {
    return resolveAthenaGraphPrimaryProductActivationViewId(supportedViews, activeViewId) === 'cabinet';
}

export type AthenaGraphSurfaceTokens = {
    canvas: Record<string, string>;
    node: Record<string, string>;
    edge: Record<string, string>;
};

export type AthenaGraphWorkbenchSheetChrome = {
    frame: AthenaGraphWorkbenchSheetFrame;
    grid: AthenaGraphWorkbenchSheetGrid;
    activeSheet?: AthenaGraphWorkbenchSheetSummary;
    titleBlock?: AthenaGraphWorkbenchSheetTitleBlock;
    metadata?: AthenaGraphWorkbenchSheetMetadata;
    crossReferenceMarkers: AthenaGraphWorkbenchCrossReferenceMarker[];
};

export type AthenaGraphWorkbenchSheetViewSelector = {
    activeSheetViewId?: string;
    hasMultipleSheetViews: boolean;
    entries: AthenaGraphWorkbenchSheetViewSelectorEntry[];
};

export type AthenaGraphWorkbenchSheetViewSelectorEntry = {
    sheetViewId: string;
    displayOrder: number;
    title: string;
    role?: string;
    subjectCount: number;
    isActive: boolean;
    label: string;
};

export type AthenaGraphWorkbenchSheetPolicyEvidence = {
    policyId: string;
    policyVersion: string;
    policyDeterministicIdentity: string;
    sheetViewRole: string;
    sheetViewRoleOrder: number;
};

export type AthenaGraphWorkbenchSheetFrame = {
    width: number;
    height: number;
    surfaceId?: string;
    source?: string;
    margins?: AthenaGraphWorkbenchSheetMargins;
    zoneColumns?: string[];
    zoneRows?: string[];
};

export type AthenaGraphWorkbenchSheetMargins = {
    top: number;
    right: number;
    bottom: number;
    left: number;
};

export type AthenaGraphWorkbenchSheetGrid = {
    majorStep: number;
    minorStep: number;
};

export type AthenaGraphWorkbenchSheetSummary = {
    sheetId: string;
    displayName: string;
    role?: string;
    order: number;
    previousSheetId?: string;
    nextSheetId?: string;
    subjectSemanticIds: string[];
    subjectCount: number;
    isActive: boolean;
};

export type AthenaGraphWorkbenchSheetTitleBlock = {
    sheetId: string;
    displayName: string;
    order: number;
    previousSheetId?: string;
    nextSheetId?: string;
    subjectCount: number;
    crossReferenceCount: number;
    fields?: AthenaGraphWorkbenchSheetTitleBlockField[];
};

export type AthenaGraphWorkbenchSheetTitleBlockField = {
    role: string;
    label: string;
    value: string;
};

export type AthenaGraphWorkbenchSheetMetadata = {
    sheetSize: string;
    orientation: string;
    projectionPolicyId: string;
};

export type AthenaGraphWorkbenchCrossReferenceMarker = {
    semanticId: string;
    kind: string;
    markerLabel: string;
    sheetIds: string[];
    occurrenceIds: string[];
    isActiveSheetLinked: boolean;
};

export type AthenaGraphWorkbenchReferenceMarker = {
    markerId: string;
    markerKind: string;
    relationType: string;
    selectedSheetViewId: string;
    sourceOccurrenceId: string;
    targetOccurrenceId: string;
    sourceIdentity: string;
    targetIdentity: string;
    sourceDocumentLocation: AthenaGraphWorkbenchDocumentLocation;
    targetDocumentLocation: AthenaGraphWorkbenchDocumentLocation;
    compactNotation: string;
    sourceProjectionIds: string[];
};

export type AthenaGraphWorkbenchConnectionMarker = {
    markerId: string;
    kind: string;
    point: AthenaGLSPPoint;
    routeIds: string[];
    connectorIds: string[];
    joined: boolean;
    appearanceClassId: string;
};

export type AthenaGraphDrawingComposition = NonNullable<NonNullable<AthenaGLSPDiagram['presentation']>['drawingComposition']>;
type AthenaGraphPresentationPaintItem = NonNullable<NonNullable<AthenaGLSPDiagram['presentation']>['paintPlan']>['items'][number];

export type AthenaGraphDrawingLayerModel = {
    items: AthenaGraphDrawingLayerItem[];
};

export type AthenaGraphDrawingLayerItem = {
    id: string;
    kind: 'sheet-frame'
        | 'drawing-area'
        | 'title-block'
        | 'title-field'
        | 'zone-column'
        | 'zone-row'
        | 'rail'
        | 'lane'
        | 'terminal-strip'
        | 'label-band'
        | 'route-channel'
        | 'reference-marker';
    authority: string;
    bounds?: AthenaGraphRect;
    start?: AthenaGLSPPoint;
    end?: AthenaGLSPPoint;
    label?: string;
    identity?: string;
};

export type AthenaGraphRect = {
    x: number;
    y: number;
    width: number;
    height: number;
};

export type AthenaGraphWorkbenchDocumentLocation = {
    sheetViewId: string;
    zoneId?: string;
    displayNotation: string;
};

export type AthenaGraphReferenceMarkerNavigation =
    | {
        status: 'ready';
        markerId: string;
        relationType: string;
        targetSheetViewId: string;
        targetOccurrenceId: string;
        targetCanonicalId: string;
        requiresSheetSwitch: boolean;
        displayNotation: string;
    }
    | {
        status: 'missing-marker';
        markerId: string;
        reason: string;
    };

export type AthenaGraphDocumentReferenceInspection =
    | {
        status: 'ready';
        canonicalIdentity: string;
        references: AthenaGraphDocumentReferenceInspectionEntry[];
        persisted: false;
    }
    | {
        status: 'unavailable';
        canonicalIdentity: string;
        references: [];
        persisted: false;
    };

export type AthenaGraphDocumentReferenceInspectionEntry = {
    markerId: string;
    markerKind: string;
    relationType: string;
    compactNotation: string;
    sourceOccurrenceId: string;
    targetOccurrenceId: string;
    sourceLocation: string;
    targetLocation: string;
    targetSheetViewId: string;
    sourceProjectionIds: string[];
};

export type AthenaGraphWorkbenchEdge = AthenaGLSPEdge & {
    routePoints: AthenaGLSPPoint[];
    bendMarkerPoints: AthenaGLSPPoint[];
    crossingMarkerPoints: AthenaGraphWorkbenchConnectionMarker[];
    connectionLabels: AthenaGraphWorkbenchConnectionLabel[];
    path: string;
    conductorStyle: 'electrical' | 'generic';
    line: AthenaGraphResolvedPresentationConnector['line'] | undefined;
    terminals: AthenaGraphWorkbenchEdgeTerminal[];
    presentationConnector?: AthenaGraphResolvedPresentationConnector;
};

export type AthenaGraphWorkbenchConnectionLabel = {
    text: string;
    point: AthenaGLSPPoint;
    canvasDisplay: 'always' | 'selection';
    labelId?: string;
};

export type AthenaGraphRouteInspection =
    | {
        status: 'ready';
        connectionId: string;
        sourcePortSemanticId?: string;
        targetPortSemanticId?: string;
        routeQuality: string;
        policySummary: string;
        labels: string[];
        persisted: false;
    }
    | {
        status: 'unavailable';
        reason: string;
        persisted: false;
    };

export type AthenaGraphEndpointInspection =
    | {
        status: 'ready';
        connectionId: string;
        endpointRole: 'source' | 'target';
        portSemanticId: string;
        bindingId: string;
        occurrenceId: string;
        anchorId: string;
        placedPoint: AthenaGLSPPoint;
        routeEndpointPoint: AthenaGLSPPoint;
        sourceProvenance: string[];
        sourceProjectionIds: string[];
        packageResource?: string;
        packageId?: string;
        elementId?: string;
        persisted: false;
    }
    | {
        status: 'unavailable';
        reason: string;
        persisted: false;
    };

export type AthenaGraphRepresentationInspection =
    | {
        status: 'ready';
        subjectId: string;
        occurrenceId: string;
        representationId: string;
        symbolFamilyId: string;
        fallback: false;
        terminals: Array<{
            terminalId: string;
            portId: string;
            physicalTerminalId: string;
            anchorId: string;
            side: string;
            number: string;
            marker: string;
        }>;
        labels: Array<{
            labelId: string;
            role: string;
            value: string;
            anchorId: string;
        }>;
        selectedTerminal?: {
            terminalId: string;
            portId: string;
            physicalTerminalId: string;
            anchorId: string;
            side: string;
            number: string;
            marker: string;
        };
        selectedLabel?: {
            labelId: string;
            role: string;
            value: string;
            anchorId: string;
        };
        persisted: false;
    }
    | {
        status: 'unavailable';
        reason: string;
        persisted: false;
    };

export type AthenaGraphWorkbenchEdgeTerminal = {
    role: 'source' | 'target';
    point: AthenaGLSPPoint;
    endpointId?: string;
    anchorId?: string;
    portSemanticId?: string;
    ownerSemanticId?: string;
    nodeId?: string;
    labelId?: string;
};

export type AthenaGraphWorkbenchLeaderSegment = {
    start: AthenaGLSPPoint;
    end: AthenaGLSPPoint;
};

export type AthenaGraphWorkbenchNodeAnchor = {
    anchorId: string;
    point: AthenaGLSPPoint;
    side: string;
    portSemanticId: string;
    labelId?: string;
};

export type AthenaGraphWorkbenchPresentationTerminal = {
    terminalId: string;
    subjectId: string;
    occurrenceId: string;
    portId: string;
    physicalTerminalId: string;
    side: string;
    marker: string;
    number: string;
    point: AthenaGLSPPoint;
    labelPoint: AthenaGLSPPoint;
    anchorId: string;
};

export type AthenaGraphWorkbenchPresentationLabel = {
    labelId: string;
    subjectId: string;
    occurrenceId: string;
    role: string;
    value: string;
    point: AthenaGLSPPoint;
    anchorId: string;
};

export type AthenaGraphWorkbenchNode = AthenaGLSPNode & {
    renderVariant: 'generic-component' | 'electrical-device' | 'generic-label' | 'electrical-terminal-label';
    notationSymbolKey?: string;
    labelPolicy?: string;
    markerKeys: string[];
    labelLeader?: AthenaGraphWorkbenchLeaderSegment;
    electricalAnchors: AthenaGraphWorkbenchNodeAnchor[];
    presentationOccurrence?: AthenaGraphResolvedPresentationOccurrence;
    presentationGraphicOccurrence?: AthenaGraphResolvedPresentationGraphicOccurrence;
    presentationRepresentation?: AthenaGraphResolvedPresentationRepresentation;
    presentationParts: AthenaGraphResolvedPresentationPart[];
    presentationTerminals: AthenaGraphWorkbenchPresentationTerminal[];
    presentationLabels: AthenaGraphWorkbenchPresentationLabel[];
};

/** Builds one deterministic workbench-facing view model from the adapter-owned graph diagram. */
export function buildAthenaGraphWorkbenchModel(diagram: AthenaGLSPDiagram): AthenaGraphWorkbenchModel {
    const fallbackCanvasWidth = 960;
    const fallbackCanvasHeight = 540;
    const graph = diagram.graph ?? {
        id: `${diagram.projectName}:${diagram.activeViewId}`,
        type: 'graph' as const,
        canvas: {
            width: 0,
            height: 0,
        },
        nodes: [],
        edges: [],
    };
    const rawGraphNodes = normalizeArray(graph.nodes);
    const rawGraphEdges = normalizeArray(graph.edges);
    const governedSheetSurfaceSize = resolveGovernedSheetSurfaceSize(diagram);
    const drawingComposition = diagram.presentation?.drawingComposition;
    const governedDrawingBounds = drawingComposition?.sheetBounds;
    const canvasWidth = governedSheetSurfaceSize?.width ?? (graph.canvas.width > 0 ? graph.canvas.width : fallbackCanvasWidth);
    const canvasHeight = governedSheetSurfaceSize?.height ?? (graph.canvas.height > 0 ? graph.canvas.height : fallbackCanvasHeight);
    const graphNodes = rawGraphNodes
        .filter(node => presentationBoundsIntersectsSurface({
            x: node.position.x,
            y: node.position.y,
            width: node.size.width,
            height: node.size.height,
        }, governedDrawingBounds, canvasWidth, canvasHeight));
    const graphEdges = rawGraphEdges
        .filter(edge => [edge.sourcePoint, ...normalizeArray(edge.bendPoints), edge.targetPoint]
            .every(point => pointIntersectsSurface(point, governedDrawingBounds, canvasWidth, canvasHeight)));
    const paintItemsByTarget = resolvePaintItemsByTarget(diagram);
    const presentationOccurrences = orderByPaintPlan(
        resolvePresentationOccurrences(diagram)
            .filter(occurrence => isPaintTargetVisible(occurrence.occurrenceId, paintItemsByTarget)),
        occurrence => occurrence.occurrenceId,
        paintItemsByTarget,
    );
    const presentationGraphicOccurrences = orderByPaintPlan(
        resolvePresentationGraphicOccurrences(diagram)
            .filter(occurrence => isPaintTargetVisible(occurrence.occurrenceId, paintItemsByTarget)),
        occurrence => occurrence.occurrenceId,
        paintItemsByTarget,
    );
    const presentationFailure = resolvePresentationFailure(diagram);
    const presentationConnectors = presentationFailure
        ? []
        : orderByPaintPlan(
            resolvePresentationConnectors(diagram)
                .filter(connector => isPaintTargetVisible(connector.occurrenceId, paintItemsByTarget)),
            connector => connector.occurrenceId,
            paintItemsByTarget,
        );
    const presentationConnectionMarkers = orderByPaintPlan(
        resolvePresentationConnectionMarkers(diagram)
            .filter(marker => isPaintTargetVisible(marker.markerId, paintItemsByTarget)),
        marker => marker.markerId,
        paintItemsByTarget,
    );
    const presentationRepresentations = resolvePresentationRepresentations(diagram);
    const supportedViews = normalizeArray(diagram.supportedViews);
    const diagnostics = normalizeArray(diagram.diagnostics);
    const sheets = normalizeArray(diagram.sheets);
    const crossReferences = normalizeArray(diagram.crossReferences);
    const notationSubjects = normalizeArray(diagram.notationPack?.subjects);
    const electricalAnchors = normalizeArray(diagram.electricalAnchors);
    const electricalConnectionEndpoints = normalizeArray(diagram.electricalConnectionEndpoints);
    const renderContributions = normalizeArray(diagram.activeRenderContributions);
    const activeView = supportedViews.find(view => view.viewId === diagram.activeViewId);
    const viewLabel = activeView?.displayName ?? diagram.activeViewId ?? 'graph';
    const isElectricalFamily = !!activeView?.familyId?.startsWith('electrical/')
        || !!diagram.notationPack?.packId?.startsWith('electrical-notation/');
    const notationBySemanticId = new Map(
        notationSubjects.map(subject => [subject.semanticId, subject] as const)
    );
    const anchorById = new Map(
        electricalAnchors.map(anchor => [anchor.anchorId, anchor] as const)
    );
    const anchorsByNodeId = groupAnchorsByNodeId(electricalAnchors);
    const anchorByLabelId = new Map(
        electricalAnchors.flatMap(anchor => anchor.labelId ? [[anchor.labelId, anchor] as const] : [])
    );
    const endpointsByConnectionId = groupEndpointsByConnectionId(electricalConnectionEndpoints);
    const representationByProjectionId = new Map(
        presentationRepresentations.flatMap(representation =>
            representation.sourceProjectionIds.map(projectionId => [projectionId, representation] as const)
        ),
    );
    const representationBySubjectId = new Map(
        presentationRepresentations.map(representation => [representation.subjectId, representation] as const),
    );
    const nodes = presentationGraphicOccurrences.length > 0
        ? presentationGraphicOccurrences.map(occurrence => buildWorkbenchNodeFromGraphicOccurrence(occurrence))
        : presentationOccurrences.length > 0
            ? presentationOccurrences.map(occurrence => buildWorkbenchNodeFromPresentation(
            occurrence,
            notationBySemanticId,
            anchorsByNodeId,
            anchorByLabelId,
            isElectricalFamily,
            resolveRepresentationForOccurrence(occurrence, representationByProjectionId, representationBySubjectId),
        ))
            : graphNodes.map(node => buildWorkbenchNode(
            node,
            notationBySemanticId,
            anchorsByNodeId,
            anchorByLabelId,
            isElectricalFamily,
            representationByProjectionId.get(node.id) ?? representationBySubjectId.get(node.semanticId),
        ));
    const rawEdges = diagram.presentation
        ? presentationConnectors.map(connector => buildWorkbenchEdgeFromPresentation(
            connector,
            presentationConnectionMarkers,
            endpointsByConnectionId,
            anchorById,
            paintItemsByTarget,
        ))
        : graphEdges.map(edge => buildWorkbenchEdge(
            edge,
            endpointsByConnectionId.get(edge.id) ?? [],
            anchorById,
        ));
    const edges = rawEdges;
    const sheetChrome = resolveSheetChrome(diagram, canvasWidth, canvasHeight);
    const sheetViewSelector = resolveSheetViewSelector(diagram);
    const referenceMarkers = resolveWorkbenchReferenceMarkers(diagram);
    const sceneBounds = resolveSceneBounds(nodes, edges, canvasWidth, canvasHeight, governedDrawingBounds);

    return {
        headerTitle: diagram.projectName,
        viewLabel,
        viewFamilyId: activeView?.familyId,
        isElectricalFamily,
        statusLabel: diagram.status,
        statusTone: diagram.status === 'ready' && !presentationFailure ? 'ready' : 'warning',
        semanticPath: diagram.semanticPath,
        snapshotId: resolveWorkbenchSnapshotId(diagram),
        activeSheetId: diagram.activeSheetId,
        sheetCount: sheets.length,
        ...(sheetViewSelector ? { sheetViewSelector } : {}),
        notationPackId: diagram.notationPack?.packId,
        crossReferenceCount: crossReferences.length,
        svgViewBox: formatSvgViewBox(sceneBounds),
        metrics: {
            nodeCount: nodes.length,
            edgeCount: edges.length,
            supportedViewCount: supportedViews.length,
            diagnosticCount: diagnostics.length,
        },
        supportedViews: supportedViews.map(view => ({
            ...view,
            isActive: view.viewId === diagram.activeViewId,
        })),
        diagnostics: presentationFailure ? [
            ...diagnostics,
            {
                severity: 'error',
                code: 'presentation.connector.invalid',
                message: presentationFailure,
            },
        ] : diagnostics,
        activeRenderContributions: renderContributions,
        ...(drawingComposition ? { drawingComposition } : {}),
        sheetChrome,
        referenceMarkers,
        nodes,
        edges,
        canvas: {
            width: sceneBounds.width,
            height: sceneBounds.height,
        },
        sceneBounds,
        surfaceTokens: resolveSurfaceTokens(renderContributions),
        emptyState: presentationFailure
            ? {
                title: 'Presentation rejected',
                message: presentationFailure,
            }
            : resolveEmptyState(diagram, nodes, edges),
    };
}

function resolvePresentationFailure(diagram: AthenaGLSPDiagram): string | undefined {
    if (!diagram.presentation) {
        return undefined;
    }
    try {
        resolvePresentationConnectors(diagram);
        return undefined;
    } catch (error) {
        return error instanceof Error ? error.message : 'Athena rejected malformed presentation connector facts.';
    }
}

function resolvePaintItemsByTarget(diagram: AthenaGLSPDiagram): Map<string, AthenaGraphPresentationPaintItem> {
    return new Map((diagram.presentation?.paintPlan?.items ?? []).map(item => [item.targetId, item]));
}

function isPaintTargetVisible(targetId: string, paintItemsByTarget: Map<string, AthenaGraphPresentationPaintItem>): boolean {
    const item = paintItemsByTarget.get(targetId);
    return item ? item.visible : true;
}

function orderByPaintPlan<T>(
    values: T[],
    targetId: (value: T) => string,
    paintItemsByTarget: Map<string, AthenaGraphPresentationPaintItem>,
): T[] {
    return values.map((value, index) => ({ value, index })).sort((left, right) => {
        const leftItem = paintItemsByTarget.get(targetId(left.value));
        const rightItem = paintItemsByTarget.get(targetId(right.value));
        if (!leftItem && !rightItem) {
            return left.index - right.index;
        }
        const leftOrder = leftItem?.order ?? Number.MAX_SAFE_INTEGER;
        const rightOrder = rightItem?.order ?? Number.MAX_SAFE_INTEGER;
        return leftOrder - rightOrder || left.index - right.index;
    }).map(entry => entry.value);
}

export function buildAthenaGraphDrawingLayerModel(
    drawingComposition: AthenaGraphDrawingComposition | undefined,
): AthenaGraphDrawingLayerModel {
    if (!drawingComposition) {
        return { items: [] };
    }

    const items: AthenaGraphDrawingLayerItem[] = [
        {
            id: drawingComposition.frameId || `${drawingComposition.sheetId}:frame`,
            kind: 'sheet-frame',
            authority: drawingComposition.authorities?.bounds ?? 'drawing-composition',
            bounds: normalizeRect(drawingComposition.frameBounds),
        },
        {
            id: `${drawingComposition.sheetId}:drawing-area`,
            kind: 'drawing-area',
            authority: drawingComposition.authorities?.bounds ?? 'drawing-composition',
            bounds: normalizeRect(drawingComposition.drawingAreaBounds),
        },
        {
            id: `${drawingComposition.sheetId}:title-block`,
            kind: 'title-block',
            authority: drawingComposition.authorities?.policy ?? 'presentation-profile-policy',
            bounds: normalizeRect(drawingComposition.titleBlockBounds),
        },
        {
            id: `${drawingComposition.sheetId}:title-field`,
            kind: 'title-field',
            authority: drawingComposition.authorities?.policy ?? 'presentation-profile-policy',
            bounds: normalizeRect(drawingComposition.titleBlockBounds),
            label: [
                drawingComposition.title?.sheetTitle,
                drawingComposition.title?.sheetNumber,
                drawingComposition.title?.revisionCode ? `REV ${drawingComposition.title.revisionCode}` : undefined,
            ].filter(Boolean).join(' / '),
        },
    ];

    normalizeArray(drawingComposition.coordinateZones).forEach(zone => {
        const axis = String(zone.axis ?? '').toUpperCase();
        if (axis !== 'COLUMN' && axis !== 'ROW') {
            return;
        }
        items.push({
            id: zone.zoneId,
            kind: axis === 'COLUMN' ? 'zone-column' : 'zone-row',
            authority: drawingComposition.authorities?.policy ?? 'presentation-profile-policy',
            bounds: normalizeRect(zone.bounds),
            label: zone.label,
        });
    });

    normalizeArray(drawingComposition.structureFacts).forEach(fact => {
        const kind = normalizeDrawingStructureKind(fact.kind);
        if (!kind) {
            return;
        }
        items.push({
            id: fact.factId,
            kind,
            authority: fact.authority ?? drawingComposition.authorities?.structure ?? 'drawing-structure',
            bounds: normalizeRect(fact.bounds),
            start: normalizePoint(fact.start),
            end: normalizePoint(fact.end),
        });
    });

    normalizeArray(drawingComposition.referencePlacements).forEach(reference => {
        items.push({
            id: reference.placementId,
            kind: 'reference-marker',
            authority: drawingComposition.authorities?.representation ?? 'drawing-symbol-anatomy',
            bounds: normalizeRect(reference.bounds),
            identity: reference.representationIdentity,
            label: reference.compactNotation,
        });
    });

    return { items };
}

export function buildAthenaGraphRouteInspection(
    model: AthenaGraphWorkbenchModel,
    semanticId: string,
): AthenaGraphRouteInspection {
    const edge = model.edges.find(candidate => candidate.semanticId === semanticId || candidate.id === semanticId);
    if (!edge?.presentationConnector) {
        return {
            status: 'unavailable',
            reason: 'No governed route fact is available for the selected rendered route.',
            persisted: false,
        };
    }
    const routeQuality = edge.presentationConnector.quality;
    const routeSegmentCount = `${Math.max(0, edge.routePoints.length - 1)}`;
    return {
        status: 'ready',
        connectionId: edge.semanticId,
        sourcePortSemanticId: edge.presentationConnector.sourceEndpoint.portSemanticId,
        targetPortSemanticId: edge.presentationConnector.targetEndpoint.portSemanticId,
        routeQuality,
        policySummary: `route-fact:${routeQuality}:${routeSegmentCount}-segment`,
        labels: edge.connectionLabels.map(label => label.text),
        persisted: false,
    };
}

export function buildAthenaGraphEndpointInspection(
    model: AthenaGraphWorkbenchModel,
    semanticId: string,
): AthenaGraphEndpointInspection {
    const match = findPresentationEndpoint(model, semanticId);
    if (!match) {
        return {
            status: 'unavailable',
            reason: 'No governed endpoint trace is available for the selected rendered endpoint.',
            persisted: false,
        };
    }
    const endpoint = match.role === 'source'
        ? match.edge.presentationConnector.sourceEndpoint
        : match.edge.presentationConnector.targetEndpoint;
    const routeEndpointPoint = match.role === 'source'
        ? match.edge.routePoints[0]
        : match.edge.routePoints[match.edge.routePoints.length - 1];
    if (!routeEndpointPoint || !samePoint(routeEndpointPoint, endpoint.point)) {
        return {
            status: 'unavailable',
            reason: 'Endpoint trace does not match the current rendered route snapshot.',
            persisted: false,
        };
    }
    const packageTrace = resolveEndpointPackageTrace(model, endpoint.occurrenceId);
    return {
        status: 'ready',
        connectionId: match.edge.semanticId,
        endpointRole: match.role,
        portSemanticId: endpoint.portSemanticId,
        bindingId: endpoint.bindingId,
        occurrenceId: endpoint.occurrenceId,
        anchorId: endpoint.anchorId,
        placedPoint: { ...endpoint.point },
        routeEndpointPoint: { ...routeEndpointPoint },
        sourceProvenance: [...endpoint.sourceProvenance],
        sourceProjectionIds: [...new Set([
            ...match.edge.presentationConnector.sourceProjectionIds,
            endpoint.portSemanticId,
            endpoint.bindingId,
            endpoint.occurrenceId,
            endpoint.anchorId,
        ])],
        ...(packageTrace.packageResource ? { packageResource: packageTrace.packageResource } : {}),
        ...(packageTrace.packageId ? { packageId: packageTrace.packageId } : {}),
        ...(packageTrace.elementId ? { elementId: packageTrace.elementId } : {}),
        persisted: false,
    };
}

export function buildAthenaGraphRepresentationInspection(
    model: AthenaGraphWorkbenchModel,
    semanticId: string,
): AthenaGraphRepresentationInspection {
    const node = model.nodes.find(candidate =>
        candidate.semanticId === semanticId
        || candidate.id === semanticId
        || candidate.presentationRepresentation?.occurrenceId === semanticId
        || candidate.presentationTerminals.some(terminal =>
            terminal.terminalId === semanticId
            || terminal.anchorId === semanticId
            || terminal.portId === semanticId
        )
        || candidate.presentationLabels.some(label =>
            label.labelId === semanticId
            || label.anchorId === semanticId
        )
    );
    if (!node?.presentationRepresentation) {
        return {
            status: 'unavailable',
            reason: 'No governed representation fact is available for the selected rendered subject.',
            persisted: false,
        };
    }
    const terminalSummaries = node.presentationTerminals.map(terminal => ({
        terminalId: terminal.terminalId,
        portId: terminal.portId,
        physicalTerminalId: terminal.physicalTerminalId,
        anchorId: terminal.anchorId,
        side: terminal.side,
        number: terminal.number,
        marker: terminal.marker,
    }));
    const labelSummaries = node.presentationLabels.map(label => ({
        labelId: label.labelId,
        role: label.role,
        value: label.value,
        anchorId: label.anchorId,
    }));
    return {
        status: 'ready',
        subjectId: node.presentationRepresentation.subjectId,
        occurrenceId: node.presentationRepresentation.occurrenceId,
        representationId: node.presentationRepresentation.representationId,
        symbolFamilyId: node.presentationRepresentation.symbolFamilyId,
        fallback: false,
        terminals: terminalSummaries,
        labels: labelSummaries,
        selectedTerminal: terminalSummaries.find(terminal =>
            terminal.terminalId === semanticId
            || terminal.anchorId === semanticId
            || terminal.portId === semanticId
        ),
        selectedLabel: labelSummaries.find(label =>
            label.labelId === semanticId
            || label.anchorId === semanticId
        ),
        persisted: false,
    };
}

function findPresentationEndpoint(
    model: AthenaGraphWorkbenchModel,
    semanticId: string,
): { edge: AthenaGraphWorkbenchEdge & { presentationConnector: AthenaGraphResolvedPresentationConnector }; role: 'source' | 'target' } | undefined {
    for (const edge of model.edges) {
        if (!edge.presentationConnector) {
            continue;
        }
        const connector = edge.presentationConnector;
        const sourceTokens = endpointSelectionTokens(edge, 'source', connector.sourceEndpoint);
        const targetTokens = endpointSelectionTokens(edge, 'target', connector.targetEndpoint);
        if (sourceTokens.has(semanticId)) {
            return { edge: edge as AthenaGraphWorkbenchEdge & { presentationConnector: AthenaGraphResolvedPresentationConnector }, role: 'source' };
        }
        if (targetTokens.has(semanticId)) {
            return { edge: edge as AthenaGraphWorkbenchEdge & { presentationConnector: AthenaGraphResolvedPresentationConnector }, role: 'target' };
        }
        if (edge.semanticId === semanticId || edge.id === semanticId) {
            return { edge: edge as AthenaGraphWorkbenchEdge & { presentationConnector: AthenaGraphResolvedPresentationConnector }, role: 'source' };
        }
    }
    return undefined;
}

function endpointSelectionTokens(
    edge: AthenaGraphWorkbenchEdge,
    role: 'source' | 'target',
    endpoint: AthenaGraphResolvedPresentationConnectorEndpoint,
): Set<string> {
    const terminal = edge.terminals.find(candidate => candidate.role === role);
    return new Set([
        endpoint.portSemanticId,
        endpoint.bindingId,
        endpoint.occurrenceId,
        endpoint.anchorId,
        terminal?.endpointId,
        terminal?.anchorId,
        terminal?.portSemanticId,
        terminal?.nodeId,
        terminal?.labelId,
    ].filter((value): value is string => !!value));
}

function samePoint(left: AthenaGLSPPoint, right: AthenaGLSPPoint): boolean {
    return left.x === right.x && left.y === right.y;
}

function resolveEndpointPackageTrace(
    model: AthenaGraphWorkbenchModel,
    occurrenceId: string,
): { packageResource?: string; packageId?: string; elementId?: string } {
    const node = model.nodes.find(candidate =>
        candidate.presentationGraphicOccurrence?.occurrenceId === occurrenceId ||
        candidate.presentationRepresentation?.occurrenceId === occurrenceId ||
        candidate.id === occurrenceId,
    );
    const graphicOccurrence = node?.presentationGraphicOccurrence;
    const representation = node?.presentationRepresentation;
    return {
        packageResource: representation?.packageTrace?.graphicResourceId,
        packageId: graphicOccurrence?.packageId ?? representation?.packageTrace?.representationPackageId,
        elementId: graphicOccurrence?.definitionId ?? representation?.representationId,
    };
}

export function captureAthenaGraphLayoutAdjustmentIntent(args: {
    model: AthenaGraphWorkbenchModel;
    node: AthenaGraphWorkbenchNode;
    kind: AthenaGraphLayoutAdjustmentKind;
    targetSemanticId?: string;
    relation?: AthenaGraphLayoutAdjustmentIntent['relation'];
}): AthenaGraphLayoutAdjustmentCaptureResult {
    if (args.kind === 'route' || args.kind === 'label') {
        return {
            accepted: false,
            reason: 'Route and label adjustment persistence is not supported.',
        };
    }
    const activeView = args.model.supportedViews.find(view => view.isActive);
    const viewId = activeView?.viewId ?? args.model.viewFamilyId ?? args.model.viewLabel;
    const sheetId = args.model.activeSheetId ?? args.model.sheetChrome.activeSheet?.sheetId ?? 'sheet:unknown';
    const occurrenceId = args.node.presentationOccurrence?.occurrenceId ?? args.node.id;
    return {
        accepted: true,
        intent: {
            intentId: [
                'layout-adjustment',
                args.kind,
                args.node.semanticId,
                occurrenceId,
                args.model.snapshotId,
            ].join(':'),
            kind: args.kind,
            subjectSemanticId: args.node.semanticId,
            occurrenceId,
            viewId,
            sheetId,
            snapshotId: args.model.snapshotId,
            sourceUri: args.model.semanticPath,
            ...(args.targetSemanticId ? { targetSemanticId: args.targetSemanticId } : {}),
            ...(args.relation ? { relation: args.relation } : {}),
            transientOnly: true,
            persisted: false,
        },
    };
}

export function buildAthenaGraphLayoutMutationPreview(
    intent: AthenaGraphLayoutAdjustmentIntent,
    sourceEdit: AthenaAuthoringSourceEditPayload,
): AthenaGraphLayoutMutationPreview {
    const authoredIntent = buildAthenaGraphAuthoredLayoutIntent(intent);
    const statement = authoredIntent.statements[0];
    const subject = statement?.subject ?? semanticIdToAuthoredName(intent.subjectSemanticId);
    const relation = intent.relation ?? (
        intent.kind === 'align' ? 'aligned-with' : intent.kind === 'group' ? 'grouped-with' : 'near'
    );
    return {
        previewId: `layout-preview:${intent.intentId}`,
        intentId: intent.intentId,
        subjectSemanticId: intent.subjectSemanticId,
        sourceUri: intent.sourceUri,
        title: `Layout ${intent.kind} preview for ${subject}`,
        authoredIntent,
        layoutBlockSnippet: sourceEdit.newText.trim(),
        sourceEdit,
        persisted: false,
    };
}

export function buildAthenaGraphAuthoredLayoutIntent(
    intent: AthenaGraphLayoutAdjustmentIntent,
): AthenaGraphAuthoredLayoutIntent {
    const relation = intent.relation ?? (
        intent.kind === 'align' ? 'aligned-with' : intent.kind === 'group' ? 'grouped-with' : 'near'
    );
    return {
        viewFamily: resolveLayoutViewFamily(intent.viewId),
        statements: [
            {
                subject: semanticIdToAuthoredName(intent.subjectSemanticId),
                relation,
                target: semanticIdToAuthoredName(intent.targetSemanticId ?? intent.subjectSemanticId),
                ...(relation === 'aligned-with' ? { axis: 'vertical' as const } : {}),
                priority: 'preference' as const,
            },
        ],
    };
}

function resolveLayoutViewFamily(viewId: string): string {
    if (viewId.includes('schematic')) {
        return 'schematic-sheet';
    }
    return 'schematic-sheet';
}

function semanticIdToAuthoredName(semanticId: string): string {
    const lastSegment = semanticId.split(':').filter(Boolean).at(-1) ?? semanticId;
    return lastSegment.split('.').filter(Boolean).at(-1) ?? lastSegment;
}

function normalizeDrawingStructureKind(kind: string | undefined): AthenaGraphDrawingLayerItem['kind'] | undefined {
    switch (kind) {
        case 'rail':
            return 'rail';
        case 'lane':
            return 'lane';
        case 'terminal-strip':
            return 'terminal-strip';
        case 'label-band':
            return 'label-band';
        case 'route-channel':
            return 'route-channel';
        default:
            return undefined;
    }
}

function normalizeRect(rect: Partial<AthenaGraphRect> | undefined): AthenaGraphRect | undefined {
    if (!rect) {
        return undefined;
    }
    const x = toFiniteNumber(rect.x);
    const y = toFiniteNumber(rect.y);
    const width = toFiniteNumber(rect.width);
    const height = toFiniteNumber(rect.height);
    if (width <= 0 || height <= 0) {
        return undefined;
    }
    return { x, y, width, height };
}

function normalizePoint(point: Partial<AthenaGLSPPoint> | undefined): AthenaGLSPPoint | undefined {
    if (!point) {
        return undefined;
    }
    return {
        x: toFiniteNumber(point.x),
        y: toFiniteNumber(point.y),
    };
}

function toFiniteNumber(value: unknown): number {
    return typeof value === 'number' && Number.isFinite(value) ? value : 0;
}

function resolveWorkbenchSnapshotId(diagram: AthenaGLSPDiagram): string {
    return [
        'workbench-snapshot',
        diagram.semanticPath || 'unknown-source',
        diagram.activeViewId || 'unknown-view',
        diagram.activeSheetId || 'unknown-sheet',
    ].join(':');
}

export function resolveSheetViewSelector(
    diagram: AthenaGLSPDiagram,
): AthenaGraphWorkbenchSheetViewSelector | undefined {
    const sheetSummaries = normalizeArray(diagram.sheets)
        .map(sheet => buildSheetSummary(sheet, diagram.activeSheetId))
        .sort(compareSheetSummaries);
    if (sheetSummaries.length <= 1) {
        return undefined;
    }

    const activeSheetViewId = sheetSummaries.find(sheet => sheet.isActive)?.sheetId
        ?? diagram.activeSheetId
        ?? sheetSummaries[0]?.sheetId;
    return {
        activeSheetViewId,
        hasMultipleSheetViews: true,
        entries: sheetSummaries.map(sheet => {
            const displayOrder = sheet.order + 1;
            return {
                sheetViewId: sheet.sheetId,
                displayOrder,
                title: sheet.displayName,
                ...(sheet.role ? { role: sheet.role } : {}),
                subjectCount: sheet.subjectCount,
                isActive: sheet.sheetId === activeSheetViewId,
                label: `${displayOrder} - ${sheet.displayName}`,
            };
        }),
    };
}

export function resolveVisibleAthenaGraphSheetViewSelector(
    model: Pick<AthenaGraphWorkbenchModel, 'sheetViewSelector'>,
): AthenaGraphWorkbenchSheetViewSelector | undefined {
    return isGovernedMultiSheetSelector(model.sheetViewSelector) ? model.sheetViewSelector : undefined;
}

function isGovernedMultiSheetSelector(
    selector: AthenaGraphWorkbenchSheetViewSelector | undefined,
): selector is AthenaGraphWorkbenchSheetViewSelector {
    if (!selector || !selector.hasMultipleSheetViews || selector.entries.length <= 1) {
        return false;
    }
    const sheetViewIds = new Set(selector.entries.map(entry => entry.sheetViewId).filter(Boolean));
    return sheetViewIds.size === selector.entries.length;
}

export function resolveAthenaGraphReferenceMarkerNavigation(
    model: AthenaGraphWorkbenchModel,
    markerId: string,
): AthenaGraphReferenceMarkerNavigation {
    const marker = model.referenceMarkers.find(candidate => candidate.markerId === markerId);
    if (!marker) {
        return {
            status: 'missing-marker',
            markerId,
            reason: `No governed reference marker is available for ${markerId}.`,
        };
    }

    return {
        status: 'ready',
        markerId: marker.markerId,
        relationType: marker.relationType,
        targetSheetViewId: marker.targetDocumentLocation.sheetViewId,
        targetOccurrenceId: marker.targetOccurrenceId,
        targetCanonicalId: marker.targetIdentity,
        requiresSheetSwitch: marker.targetDocumentLocation.sheetViewId !== model.activeSheetId,
        displayNotation: marker.compactNotation,
    };
}

export function buildAthenaGraphDocumentReferenceInspection(
    model: AthenaGraphWorkbenchModel,
    canonicalIdentity: string,
): AthenaGraphDocumentReferenceInspection {
    const references = model.referenceMarkers
        .filter(marker => marker.sourceIdentity === canonicalIdentity || marker.targetIdentity === canonicalIdentity)
        .map(marker => ({
            markerId: marker.markerId,
            markerKind: marker.markerKind,
            relationType: marker.relationType,
            compactNotation: marker.compactNotation,
            sourceOccurrenceId: marker.sourceOccurrenceId,
            targetOccurrenceId: marker.targetOccurrenceId,
            sourceLocation: marker.sourceDocumentLocation.displayNotation,
            targetLocation: marker.targetDocumentLocation.displayNotation,
            targetSheetViewId: marker.targetDocumentLocation.sheetViewId,
            sourceProjectionIds: [...marker.sourceProjectionIds],
        }))
        .sort(compareDocumentReferenceInspectionEntries);

    if (references.length === 0) {
        return {
            status: 'unavailable',
            canonicalIdentity,
            references: [],
            persisted: false,
        };
    }

    return {
        status: 'ready',
        canonicalIdentity,
        references,
        persisted: false,
    };
}

function resolveWorkbenchReferenceMarkers(
    diagram: AthenaGLSPDiagram,
): AthenaGraphWorkbenchReferenceMarker[] {
    return [
        ...resolvePresentationReferenceMarkers(diagram)
        .map(marker => ({
            markerId: marker.markerId,
            markerKind: marker.markerKind,
            relationType: marker.relationType,
            selectedSheetViewId: marker.selectedSheetViewId,
            sourceOccurrenceId: marker.sourceOccurrenceId,
            targetOccurrenceId: marker.targetOccurrenceId,
            sourceIdentity: marker.sourceIdentity,
            targetIdentity: marker.targetIdentity,
            sourceDocumentLocation: { ...marker.sourceDocumentLocation },
            targetDocumentLocation: { ...marker.targetDocumentLocation },
            compactNotation: marker.compactNotation,
            sourceProjectionIds: [...marker.sourceProjectionIds],
        })),
        ...resolveCrossReferenceLinkMarkers(diagram),
    ]
        .sort(compareReferenceMarkers);
}

function resolvePresentationConnectionMarkers(diagram: AthenaGLSPDiagram): AthenaGraphWorkbenchConnectionMarker[] {
    return (diagram.presentation?.connectionMarkers ?? []).map(marker => ({
        markerId: marker.markerId,
        kind: marker.kind,
        point: { ...marker.point },
        routeIds: [...(marker.routeIds ?? [])],
        connectorIds: [...(marker.connectorIds ?? [])],
        joined: marker.joined,
        appearanceClassId: marker.appearanceClassId,
    }));
}

function resolveCrossReferenceLinkMarkers(
    diagram: AthenaGLSPDiagram,
): AthenaGraphWorkbenchReferenceMarker[] {
    const sheetIds = new Set(normalizeArray(diagram.sheets).map(sheet => sheet.sheetId).filter(isNonBlankString));
    return normalizeArray(diagram.crossReferences)
        .flatMap(reference => {
            const referenceId = reference.crossReferenceId;
            if (!isNonBlankString(referenceId)) {
                return [];
            }
            const occurrenceIds = new Set(normalizeArray(reference.occurrenceIds).filter(isNonBlankString));
            return normalizeArray(reference.links)
                .map(link => {
                    if (!isValidCrossReferenceLink(link, sheetIds, occurrenceIds)) {
                        return undefined;
                    }
                    const [sourceNotation, targetNotation] = link.compactNotation
                        .split('->')
                        .map(part => part.trim());
                    return {
                        markerId: `${referenceId}:${link.sourceSheetId}->${link.targetSheetId}`,
                        markerKind: 'cross_reference',
                        relationType: reference.kind,
                        selectedSheetViewId: link.sourceSheetId,
                        sourceOccurrenceId: link.sourceOccurrenceId,
                        targetOccurrenceId: link.targetOccurrenceId,
                        sourceIdentity: link.semanticId,
                        targetIdentity: link.semanticId,
                        sourceDocumentLocation: {
                            sheetViewId: link.sourceSheetId,
                            displayNotation: sourceNotation,
                        },
                        targetDocumentLocation: {
                            sheetViewId: link.targetSheetId,
                            displayNotation: targetNotation,
                        },
                        compactNotation: link.compactNotation,
                        sourceProjectionIds: [referenceId],
                    } satisfies AthenaGraphWorkbenchReferenceMarker;
                })
                .filter((marker): marker is AthenaGraphWorkbenchReferenceMarker => marker !== undefined);
        });
}

function isValidCrossReferenceLink(
    link: NonNullable<AthenaGLSPDiagram['crossReferences'][number]['links']>[number],
    sheetIds: ReadonlySet<string>,
    occurrenceIds: ReadonlySet<string>,
): boolean {
    return isNonBlankString(link.semanticId) &&
        isNonBlankString(link.sourceSheetId) &&
        isNonBlankString(link.targetSheetId) &&
        isNonBlankString(link.sourceOccurrenceId) &&
        isNonBlankString(link.targetOccurrenceId) &&
        isNonBlankString(link.compactNotation) &&
        link.compactNotation.split('->').length === 2 &&
        sheetIds.has(link.sourceSheetId) &&
        sheetIds.has(link.targetSheetId) &&
        occurrenceIds.has(link.sourceOccurrenceId) &&
        occurrenceIds.has(link.targetOccurrenceId);
}

function isNonBlankString(value: unknown): value is string {
    return typeof value === 'string' && value.trim().length > 0;
}

export function clampAthenaGraphZoom(zoom: number): number {
    if (!Number.isFinite(zoom)) {
        return 1;
    }
    return Math.min(2.5, Math.max(0.2, zoom));
}

export function fitAthenaGraphViewport(
    bounds: AthenaGraphSceneBounds,
    viewport: AthenaGraphViewportSize
): AthenaGraphViewportTransform {
    const width = Math.max(viewport.width, 1);
    const height = Math.max(viewport.height, 1);
    const padding = Math.max(36, Math.min(width, height) * 0.08);
    const availableWidth = Math.max(1, width - padding * 2);
    const availableHeight = Math.max(1, height - padding * 2);
    const scaleX = availableWidth / Math.max(bounds.width, 1);
    const scaleY = availableHeight / Math.max(bounds.height, 1);
    const zoom = clampAthenaGraphZoom(Math.min(scaleX, scaleY));

    return {
        zoom,
        offsetX: (width / 2) - (bounds.centerX * zoom),
        offsetY: (height / 2) - (bounds.centerY * zoom),
    };
}

export function panAthenaGraphViewport(
    transform: AthenaGraphViewportTransform,
    deltaX: number,
    deltaY: number
): AthenaGraphViewportTransform {
    return {
        ...transform,
        offsetX: transform.offsetX + deltaX,
        offsetY: transform.offsetY + deltaY,
    };
}

export function zoomAthenaGraphViewportAtPoint(
    transform: AthenaGraphViewportTransform,
    screenPoint: { x: number; y: number },
    nextZoom: number
): AthenaGraphViewportTransform {
    const zoom = clampAthenaGraphZoom(nextZoom);
    const worldX = (screenPoint.x - transform.offsetX) / transform.zoom;
    const worldY = (screenPoint.y - transform.offsetY) / transform.zoom;

    return {
        zoom,
        offsetX: screenPoint.x - (worldX * zoom),
        offsetY: screenPoint.y - (worldY * zoom),
    };
}

export function resizeAthenaGraphViewport(
    transform: AthenaGraphViewportTransform,
    previousViewport: AthenaGraphViewportSize,
    nextViewport: AthenaGraphViewportSize,
): AthenaGraphViewportTransform {
    const zoom = transform.zoom <= 0 ? 1 : transform.zoom;
    const previousCenterX = previousViewport.width > 0 ? previousViewport.width / 2 : 0;
    const previousCenterY = previousViewport.height > 0 ? previousViewport.height / 2 : 0;
    const worldCenterX = (previousCenterX - transform.offsetX) / zoom;
    const worldCenterY = (previousCenterY - transform.offsetY) / zoom;

    return {
        zoom: transform.zoom,
        offsetX: (nextViewport.width / 2) - (worldCenterX * zoom),
        offsetY: (nextViewport.height / 2) - (worldCenterY * zoom),
    };
}

export function keepAthenaGraphViewportFocusedOnSelection(
    transform: AthenaGraphViewportTransform,
    viewport: AthenaGraphViewportSize,
    nodes: AthenaGraphWorkbenchNode[],
    edges: AthenaGraphWorkbenchEdge[],
    semanticId: string,
    padding: number = 48,
): AthenaGraphViewportTransform {
    const selectedNode = nodes.find(node => node.semanticId === semanticId);
    if (selectedNode) {
        return keepAthenaGraphViewportFocusedOnBounds(
            transform,
            viewport,
            resolveAthenaGraphNodeBounds(selectedNode),
            padding,
        );
    }

    const selectedEdge = edges.find(edge => edge.semanticId === semanticId);
    if (selectedEdge) {
        const routeBounds = resolveAthenaGraphRouteBounds(selectedEdge.routePoints);
        return routeBounds
            ? keepAthenaGraphViewportFocusedOnBounds(transform, viewport, routeBounds, padding)
            : transform;
    }

    return transform;
}

export function keepAthenaGraphViewportFocusedOnBounds(
    transform: AthenaGraphViewportTransform,
    viewport: AthenaGraphViewportSize,
    bounds: AthenaGraphSceneBounds,
    padding: number = 48,
): AthenaGraphViewportTransform {
    if (viewport.width <= 0 || viewport.height <= 0) {
        return transform;
    }
    if (isAthenaGraphBoundsVisible(transform, viewport, bounds, padding)) {
        return transform;
    }
    return centerAthenaGraphViewportOnBounds(transform, viewport, bounds);
}

function resolveAthenaGraphNodeBounds(
    node: Pick<AthenaGraphWorkbenchNode, 'position' | 'size'>,
): AthenaGraphSceneBounds {
    const minX = node.position.x;
    const minY = node.position.y;
    const maxX = node.position.x + node.size.width;
    const maxY = node.position.y + node.size.height;
    return {
        minX,
        minY,
        maxX,
        maxY,
        width: Math.max(maxX - minX, 1),
        height: Math.max(maxY - minY, 1),
        centerX: minX + (Math.max(maxX - minX, 1) / 2),
        centerY: minY + (Math.max(maxY - minY, 1) / 2),
    };
}

function resolveAthenaGraphRouteBounds(routePoints: AthenaGLSPPoint[]): AthenaGraphSceneBounds | undefined {
    if (routePoints.length === 0) {
        return undefined;
    }

    let minX = Number.POSITIVE_INFINITY;
    let minY = Number.POSITIVE_INFINITY;
    let maxX = Number.NEGATIVE_INFINITY;
    let maxY = Number.NEGATIVE_INFINITY;

    for (const point of routePoints) {
        minX = Math.min(minX, point.x);
        minY = Math.min(minY, point.y);
        maxX = Math.max(maxX, point.x);
        maxY = Math.max(maxY, point.y);
    }

    if (!Number.isFinite(minX) || !Number.isFinite(minY) || !Number.isFinite(maxX) || !Number.isFinite(maxY)) {
        return undefined;
    }

    const width = Math.max(maxX - minX, 1);
    const height = Math.max(maxY - minY, 1);
    return {
        minX,
        minY,
        maxX,
        maxY,
        width,
        height,
        centerX: minX + (width / 2),
        centerY: minY + (height / 2),
    };
}

function centerAthenaGraphViewportOnBounds(
    transform: AthenaGraphViewportTransform,
    viewport: AthenaGraphViewportSize,
    bounds: AthenaGraphSceneBounds,
): AthenaGraphViewportTransform {
    const zoom = transform.zoom <= 0 ? 1 : transform.zoom;
    return {
        zoom,
        offsetX: (viewport.width / 2) - (bounds.centerX * zoom),
        offsetY: (viewport.height / 2) - (bounds.centerY * zoom),
    };
}

function isAthenaGraphBoundsVisible(
    transform: AthenaGraphViewportTransform,
    viewport: AthenaGraphViewportSize,
    bounds: AthenaGraphSceneBounds,
    padding: number,
): boolean {
    const zoom = transform.zoom <= 0 ? 1 : transform.zoom;
    const left = (bounds.minX * zoom) + transform.offsetX;
    const top = (bounds.minY * zoom) + transform.offsetY;
    const right = (bounds.maxX * zoom) + transform.offsetX;
    const bottom = (bounds.maxY * zoom) + transform.offsetY;
    const inset = Math.max(0, padding);
    return left >= inset &&
        top >= inset &&
        right <= Math.max(viewport.width - inset, inset) &&
        bottom <= Math.max(viewport.height - inset, inset);
}

function resolveEmptyState(
    diagram: AthenaGLSPDiagram,
    nodes: Array<{ id: string }>,
    edges: Array<{ id: string }>,
): AthenaGraphWorkbenchModel['emptyState'] {
    if (diagram.status !== 'ready') {
        return {
            title: 'Projection unavailable',
            message: diagram.unavailableReason ?? 'Athena did not publish a usable graphical projection for the active workbench session.',
        };
    }

    if (nodes.length === 0 && edges.length === 0) {
        return {
            title: 'Projection is empty',
            message: 'Athena published an active graphical view, but no nodes or relationships are currently visible in that projection.',
        };
    }

    return undefined;
}

function presentationBoundsIntersectsSurface(
    bounds: { x: number; y: number; width: number; height: number },
    governedBounds: { x: number; y: number; width: number; height: number } | undefined,
    canvasWidth: number,
    canvasHeight: number,
): boolean {
    const surface = governedBounds ?? { x: 0, y: 0, width: canvasWidth, height: canvasHeight };
    const minX = bounds.x;
    const minY = bounds.y;
    const maxX = bounds.x + bounds.width;
    const maxY = bounds.y + bounds.height;
    return maxX >= surface.x && maxY >= surface.y && minX <= surface.x + surface.width && minY <= surface.y + surface.height;
}

function pointIntersectsSurface(
    point: AthenaGLSPPoint,
    governedBounds: { x: number; y: number; width: number; height: number } | undefined,
    canvasWidth: number,
    canvasHeight: number,
): boolean {
    const surface = governedBounds ?? { x: 0, y: 0, width: canvasWidth, height: canvasHeight };
    return point.x >= surface.x && point.x <= surface.x + surface.width
        && point.y >= surface.y && point.y <= surface.y + surface.height;
}

function resolveSheetChrome(
    diagram: AthenaGLSPDiagram,
    canvasWidth: number,
    canvasHeight: number,
): AthenaGraphWorkbenchSheetChrome {
    const sheetSummaries = normalizeArray(diagram.sheets)
        .map(sheet => buildSheetSummary(sheet, diagram.activeSheetId))
        .sort(compareSheetSummaries);
    const activeSheet = sheetSummaries.find(sheet => sheet.isActive) ?? sheetSummaries[0];
    const crossReferenceMarkers = normalizeArray(diagram.crossReferences)
        .map(reference => ({
            semanticId: reference.semanticId,
            kind: reference.kind,
            markerLabel: humanizeMarkerKind(reference.kind),
            sheetIds: [...reference.sheetIds],
            occurrenceIds: [...reference.occurrenceIds],
            isActiveSheetLinked: activeSheet ? reference.sheetIds.includes(activeSheet.sheetId) : false,
        }))
        .sort(compareCrossReferenceMarkers);
    const governedSheetSurface = resolveGovernedSheetSurface(diagram, activeSheet, canvasWidth, canvasHeight);
    const titleBlockFields = normalizeArray(governedSheetSurface?.titleBlock.fields);

    return {
        frame: {
            width: governedSheetSurface?.frame.width ?? canvasWidth,
            height: governedSheetSurface?.frame.height ?? canvasHeight,
            ...(governedSheetSurface?.surfaceId ? { surfaceId: governedSheetSurface.surfaceId } : {}),
            ...(governedSheetSurface?.source ? { source: governedSheetSurface.source } : {}),
            ...(governedSheetSurface?.frame.margins ? { margins: { ...governedSheetSurface.frame.margins } } : {}),
            ...(governedSheetSurface?.frame.zoneColumns ? { zoneColumns: [...governedSheetSurface.frame.zoneColumns] } : {}),
            ...(governedSheetSurface?.frame.zoneRows ? { zoneRows: [...governedSheetSurface.frame.zoneRows] } : {}),
        },
        grid: governedSheetSurface?.grid
            ? { ...governedSheetSurface.grid }
            : {
                majorStep: 120,
                minorStep: 24,
            },
        activeSheet,
        titleBlock: activeSheet ? {
            sheetId: activeSheet.sheetId,
            displayName: activeSheet.displayName,
            order: activeSheet.order,
            subjectCount: activeSheet.subjectCount,
            crossReferenceCount: crossReferenceMarkers.filter(marker => marker.isActiveSheetLinked).length,
            ...(titleBlockFields.length > 0 ? { fields: titleBlockFields.map(field => ({ ...field })) } : {}),
            ...(activeSheet.previousSheetId ? { previousSheetId: activeSheet.previousSheetId } : {}),
            ...(activeSheet.nextSheetId ? { nextSheetId: activeSheet.nextSheetId } : {}),
        } : undefined,
        ...(governedSheetSurface?.metadata ? { metadata: { ...governedSheetSurface.metadata } } : {}),
        crossReferenceMarkers,
    };
}

function resolveGovernedSheetSurfaceSize(
    diagram: AthenaGLSPDiagram,
): Pick<AthenaGraphWorkbenchSheetFrame, 'width' | 'height'> | undefined {
    const drawingBounds = diagram.presentation?.drawingComposition?.sheetBounds;
    if (drawingBounds && drawingBounds.width > 0 && drawingBounds.height > 0) {
        return { width: drawingBounds.width, height: drawingBounds.height };
    }
    const frame = diagram.presentation?.sheetSurface?.frame;
    if (frame && frame.width > 0 && frame.height > 0) {
        return {
            width: frame.width,
            height: frame.height,
        };
    }

    const activeSheet = normalizeArray(diagram.sheets)
        .find(sheet => sheet.sheetId === diagram.activeSheetId)
        ?? normalizeArray(diagram.sheets)[0];
    return resolvePublicationSheetFrameSize(activeSheet?.publication?.pageSize);
}

function resolveGovernedSheetSurface(
    diagram: AthenaGLSPDiagram,
    activeSheet: AthenaGraphWorkbenchSheetSummary | undefined,
    canvasWidth: number,
    canvasHeight: number,
): AthenaGraphPresentationSheetSurface | undefined {
    const drawingComposition = diagram.presentation?.drawingComposition;
    if (drawingComposition) {
        const frame = drawingComposition.frameBounds;
        const sheet = drawingComposition.sheetBounds;
        const columns = drawingComposition.coordinateZones
            .filter(zone => zone.axis === 'COLUMN')
            .sort((left, right) => left.order - right.order)
            .map(zone => zone.label);
        const rows = drawingComposition.coordinateZones
            .filter(zone => zone.axis === 'ROW')
            .sort((left, right) => left.order - right.order)
            .map(zone => zone.label);
        return {
            surfaceId: `presentation/drawing-composition/${drawingComposition.sheetId}`,
            source: drawingComposition.authorities.bounds,
            frame: {
                width: sheet.width,
                height: sheet.height,
                margins: {
                    top: frame.y - sheet.y,
                    right: (sheet.x + sheet.width) - (frame.x + frame.width),
                    bottom: (sheet.y + sheet.height) - (frame.y + frame.height),
                    left: frame.x - sheet.x,
                },
                zoneColumns: columns,
                zoneRows: rows,
            },
            grid: { majorStep: 24, minorStep: 6 },
            titleBlock: {
                fields: [
                    { role: 'sheet', label: 'Sheet', value: drawingComposition.title.sheetTitle },
                    { role: 'sheet-number', label: 'Sheet No.', value: drawingComposition.title.sheetNumber },
                    { role: 'revision', label: 'Revision', value: drawingComposition.title.revisionCode },
                    { role: 'policy', label: 'Policy', value: drawingComposition.policyId },
                ],
            },
            metadata: {
                sheetSize: drawingComposition.title.pageFormat,
                orientation: drawingComposition.title.orientation,
                projectionPolicyId: drawingComposition.policyId,
            },
        };
    }
    const presentationSurface = diagram.presentation?.sheetSurface;
    if (presentationSurface) {
        return {
            surfaceId: presentationSurface.surfaceId,
            source: presentationSurface.source,
            frame: {
                width: presentationSurface.frame.width,
                height: presentationSurface.frame.height,
                ...(presentationSurface.frame.margins ? { margins: { ...presentationSurface.frame.margins } } : {}),
                ...(presentationSurface.frame.zoneColumns ? { zoneColumns: [...presentationSurface.frame.zoneColumns] } : {}),
                ...(presentationSurface.frame.zoneRows ? { zoneRows: [...presentationSurface.frame.zoneRows] } : {}),
            },
            grid: { ...presentationSurface.grid },
            titleBlock: {
                fields: normalizeArray(presentationSurface.titleBlock.fields).map(field => ({ ...field })),
            },
            metadata: { ...presentationSurface.metadata },
        };
    }

    const activeProjectionSheet = activeSheet
        ? normalizeArray(diagram.sheets).find(sheet => sheet.sheetId === activeSheet.sheetId)
        : undefined;
    const publication = activeProjectionSheet?.publication;
    if (!publication) {
        return undefined;
    }

    const frameSize = resolvePublicationSheetFrameSize(publication.pageSize) ?? {
        width: canvasWidth,
        height: canvasHeight,
    };
    return {
        surfaceId: `presentation/sheet-surface/${publication.frame.frameId}/${publication.titleBlock.sheetNumber}`,
        source: 'projection-sheet-publication',
        frame: {
            width: frameSize.width,
            height: frameSize.height,
            margins: { top: 40, right: 48, bottom: 72, left: 48 },
            zoneColumns: ['1', '2', '3', '4', '5', '6'],
            zoneRows: ['A', 'B', 'C', 'D'],
        },
        grid: {
            majorStep: 96,
            minorStep: 24,
        },
        titleBlock: {
            fields: [
                { role: 'project', label: 'Project', value: diagram.projectName },
                { role: 'sheet', label: 'Sheet', value: publication.titleBlock.sheetTitle },
                { role: 'sheet-family', label: 'Family', value: publication.titleBlock.sheetFamily },
                { role: 'sheet-number', label: 'Sheet No.', value: publication.titleBlock.sheetNumber },
                { role: 'revision', label: 'Revision', value: publication.revisionMetadata.revisionCode },
                { role: 'policy', label: 'Policy', value: `${publication.frame.frameId}:${publication.frame.style}` },
            ],
        },
        metadata: {
            sheetSize: publication.pageSize.format,
            orientation: publication.pageSize.orientation,
            projectionPolicyId: `${publication.frame.frameId}:${publication.frame.style}`,
        },
    };
}

function resolvePublicationSheetFrameSize(
    pageSize: AthenaGLSPDiagram['sheets'][number]['publication']['pageSize'] | undefined,
): Pick<AthenaGraphWorkbenchSheetFrame, 'width' | 'height'> | undefined {
    if (!pageSize) {
        return undefined;
    }
    const format = pageSize.format.toLowerCase();
    const orientation = pageSize.orientation.toLowerCase();
    const knownSizes: Record<string, { width: number; height: number }> = {
        a3: { width: 1680, height: 1188 },
        a4: { width: 1188, height: 840 },
    };
    const size = knownSizes[format];
    if (!size) {
        return undefined;
    }
    return orientation === 'portrait'
        ? { width: size.height, height: size.width }
        : { width: size.width, height: size.height };
}

type AthenaGraphPresentationSheetSurface = {
    surfaceId: string;
    source: string;
    frame: {
        width: number;
        height: number;
        margins?: AthenaGraphWorkbenchSheetMargins;
        zoneColumns?: string[];
        zoneRows?: string[];
    };
    grid: AthenaGraphWorkbenchSheetGrid;
    titleBlock: {
        fields: AthenaGraphWorkbenchSheetTitleBlockField[];
    };
    metadata: AthenaGraphWorkbenchSheetMetadata;
};

function buildSheetSummary(
    sheet: AthenaGLSPDiagram['sheets'][number],
    activeSheetId: string | undefined,
): AthenaGraphWorkbenchSheetSummary {
    const subjectSemanticIds = normalizeArray(sheet.subjectSemanticIds);
    const role = sheet.role ?? sheet.policyEvidence?.sheetViewRole;
    return {
        sheetId: sheet.sheetId,
        displayName: sheet.displayName,
        ...(role ? { role } : {}),
        order: sheet.order,
        subjectSemanticIds,
        subjectCount: subjectSemanticIds.length,
        isActive: sheet.sheetId === activeSheetId,
        ...(sheet.previousSheetId ? { previousSheetId: sheet.previousSheetId } : {}),
        ...(sheet.nextSheetId ? { nextSheetId: sheet.nextSheetId } : {}),
    };
}

function compareSheetSummaries(
    left: AthenaGraphWorkbenchSheetSummary,
    right: AthenaGraphWorkbenchSheetSummary,
): number {
    return (left.order - right.order) || compareStrings(left.sheetId, right.sheetId);
}

function compareCrossReferenceMarkers(
    left: AthenaGraphWorkbenchCrossReferenceMarker,
    right: AthenaGraphWorkbenchCrossReferenceMarker,
): number {
    return compareStrings(left.kind, right.kind) || compareStrings(left.semanticId, right.semanticId);
}

function compareReferenceMarkers(
    left: AthenaGraphWorkbenchReferenceMarker,
    right: AthenaGraphWorkbenchReferenceMarker,
): number {
    return compareStrings(left.markerId, right.markerId);
}

function compareDocumentReferenceInspectionEntries(
    left: AthenaGraphDocumentReferenceInspectionEntry,
    right: AthenaGraphDocumentReferenceInspectionEntry,
): number {
    return compareStrings(left.relationType, right.relationType)
        || compareStrings(left.markerId, right.markerId);
}

function compareStrings(left: string, right: string): number {
    return left < right ? -1 : left > right ? 1 : 0;
}

function humanizeMarkerKind(kind: string): string {
    return kind.replace(/[_-]+/g, ' ');
}

function resolveSceneBounds(
    nodes: AthenaGraphWorkbenchNode[],
    edges: AthenaGraphWorkbenchEdge[],
    canvasWidth: number,
    canvasHeight: number,
    governedBounds?: { x: number; y: number; width: number; height: number },
): AthenaGraphSceneBounds {
    if (governedBounds && governedBounds.width > 0 && governedBounds.height > 0) {
        return {
            minX: governedBounds.x,
            minY: governedBounds.y,
            maxX: governedBounds.x + governedBounds.width,
            maxY: governedBounds.y + governedBounds.height,
            width: governedBounds.width,
            height: governedBounds.height,
            centerX: governedBounds.x + governedBounds.width / 2,
            centerY: governedBounds.y + governedBounds.height / 2,
        };
    }
    let minX = Number.POSITIVE_INFINITY;
    let minY = Number.POSITIVE_INFINITY;
    let maxX = Number.NEGATIVE_INFINITY;
    let maxY = Number.NEGATIVE_INFINITY;

    for (const node of nodes) {
        minX = Math.min(minX, node.position.x);
        minY = Math.min(minY, node.position.y);
        maxX = Math.max(maxX, node.position.x + node.size.width);
        maxY = Math.max(maxY, node.position.y + node.size.height);
        for (const terminal of node.presentationTerminals) {
            const markerPadding = 5;
            const numberOffset = terminal.side.toLowerCase() === 'left' ? -34 : 10;
            const numberX = terminal.point.x + numberOffset;
            const numberY = terminal.point.y - 8;
            const numberWidth = estimateGraphTextWidth(terminal.number);
            minX = Math.min(minX, terminal.point.x - markerPadding, numberX);
            minY = Math.min(minY, terminal.point.y - markerPadding, numberY - 10);
            maxX = Math.max(maxX, terminal.point.x + markerPadding, numberX + numberWidth);
            maxY = Math.max(maxY, terminal.point.y + markerPadding, numberY + 4);
        }
        for (const label of node.presentationLabels) {
            minX = Math.min(minX, label.point.x);
            minY = Math.min(minY, label.point.y - 12);
            maxX = Math.max(maxX, label.point.x + estimateGraphTextWidth(label.value));
            maxY = Math.max(maxY, label.point.y + 4);
        }
    }

    for (const edge of edges) {
        for (const point of edge.routePoints) {
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
        }
    }

    if (!Number.isFinite(minX) || !Number.isFinite(minY) || !Number.isFinite(maxX) || !Number.isFinite(maxY)) {
        minX = 0;
        minY = 0;
        maxX = canvasWidth;
        maxY = canvasHeight;
    }

    const width = Math.max(maxX - minX, 1);
    const height = Math.max(maxY - minY, 1);

    return {
        minX,
        minY,
        maxX,
        maxY,
        width,
        height,
        centerX: minX + (width / 2),
        centerY: minY + (height / 2),
    };
}

function estimateGraphTextWidth(value: string): number {
    return Math.max(8, value.length * 7);
}

function formatSvgViewBox(bounds: AthenaGraphSceneBounds): string {
    return `${bounds.minX} ${bounds.minY} ${bounds.width} ${bounds.height}`;
}

function buildWorkbenchNode(
    node: AthenaGLSPNode,
    notationBySemanticId: Map<string, NonNullable<AthenaGLSPDiagram['notationPack']>['subjects'][number]>,
    anchorsByNodeId: Map<string, AthenaGLSPElectricalAnchorSource[]>,
    anchorByLabelId: Map<string, AthenaGLSPElectricalAnchorSource>,
    isElectricalFamily: boolean,
    representation: AthenaGraphResolvedPresentationRepresentation | undefined,
): AthenaGraphWorkbenchNode {
    const notation = notationBySemanticId.get(node.semanticId);
    const nodeAnchors = normalizeArray(anchorsByNodeId.get(node.id)).map(anchor => ({
        anchorId: anchor.anchorId,
        point: { x: anchor.x, y: anchor.y },
        side: anchor.side,
        portSemanticId: anchor.portSemanticId,
        labelId: anchor.labelId,
    }));
    const labelAnchor = node.kind === 'label' ? anchorByLabelId.get(node.id) : undefined;
    const renderVariant = representation
        ? 'electrical-device'
        : resolveNodeRenderVariant(node, notation?.symbolKey, labelAnchor, isElectricalFamily);
    const visualNode = representation
        ? resolveRepresentationVisualNode(node, representation, nodeAnchors)
        : node;

    return {
        ...visualNode,
        renderVariant,
        notationSymbolKey: notation?.symbolKey,
        labelPolicy: notation?.labelPolicy,
        markerKeys: normalizeArray(notation?.markerKeys),
        labelLeader: renderVariant === 'electrical-terminal-label' && labelAnchor
            ? buildLabelLeader(node, labelAnchor)
            : undefined,
        electricalAnchors: nodeAnchors,
        presentationOccurrence: undefined,
        presentationGraphicOccurrence: undefined,
        presentationRepresentation: representation,
        presentationParts: representation
            ? scaleRepresentationPartsToNode(representation.parts, visualNode)
            : [],
        presentationTerminals: representation
            ? scaleRepresentationTerminalsToNode(representation, visualNode)
            : [],
        presentationLabels: representation
            ? scaleRepresentationLabelsToNode(representation, visualNode)
            : [],
    };
}

function buildWorkbenchNodeFromPresentation(
    occurrence: AthenaGraphResolvedPresentationOccurrence,
    notationBySemanticId: Map<string, NonNullable<AthenaGLSPDiagram['notationPack']>['subjects'][number]>,
    anchorsByNodeId: Map<string, AthenaGLSPElectricalAnchorSource[]>,
    anchorByLabelId: Map<string, AthenaGLSPElectricalAnchorSource>,
    isElectricalFamily: boolean,
    representation: AthenaGraphResolvedPresentationRepresentation | undefined,
): AthenaGraphWorkbenchNode {
    const notation = notationBySemanticId.get(occurrence.semanticId);
    const sourceProjectionIds = normalizeArray(occurrence.sourceProjectionIds);
    const nodeAnchors = sourceProjectionIds
        .flatMap(sourceProjectionId => normalizeArray(anchorsByNodeId.get(sourceProjectionId)))
        .map(anchor => ({
            anchorId: anchor.anchorId,
            point: { x: anchor.x, y: anchor.y },
            side: anchor.side,
            portSemanticId: anchor.portSemanticId,
            labelId: anchor.labelId,
        }));
    const labelAnchor = sourceProjectionIds
        .map(sourceProjectionId => anchorByLabelId.get(sourceProjectionId))
        .find(Boolean);
    const kind = occurrence.layer === 'label' ? 'label' as const : 'component' as const;
    const baseNode: AthenaGLSPNode = {
        id: occurrence.occurrenceId,
        semanticId: occurrence.semanticId,
        type: 'node',
        kind,
        label: occurrence.displayLabel ?? occurrence.semanticId,
        position: {
            x: occurrence.bounds.x,
            y: occurrence.bounds.y,
        },
        size: {
            width: occurrence.bounds.width,
            height: occurrence.bounds.height,
        },
    };
    const renderVariant = representation
        ? 'electrical-device'
        : resolveNodeRenderVariant(baseNode, notation?.symbolKey, labelAnchor, isElectricalFamily);
    const visualNode = representation
        ? resolveRepresentationVisualNode(baseNode, representation, nodeAnchors)
        : baseNode;
    return {
        ...visualNode,
        renderVariant,
        notationSymbolKey: notation?.symbolKey,
        labelPolicy: notation?.labelPolicy,
        markerKeys: normalizeArray(notation?.markerKeys.length ? notation.markerKeys : occurrence.markerKeys),
        labelLeader: renderVariant === 'electrical-terminal-label' && labelAnchor
            ? buildLabelLeader(baseNode, labelAnchor)
            : undefined,
        electricalAnchors: nodeAnchors,
        presentationOccurrence: occurrence,
        presentationGraphicOccurrence: undefined,
        presentationRepresentation: representation,
        presentationParts: representation
            ? scaleRepresentationPartsToNode(representation.parts, visualNode)
            : normalizeArray(occurrence.parts),
        presentationTerminals: representation
            ? scaleRepresentationTerminalsToNode(representation, visualNode)
            : [],
        presentationLabels: representation
            ? scaleRepresentationLabelsToNode(representation, visualNode)
            : [],
    };
}

function buildWorkbenchNodeFromGraphicOccurrence(
    occurrence: AthenaGraphResolvedPresentationGraphicOccurrence,
): AthenaGraphWorkbenchNode {
    const node: AthenaGLSPNode = {
        id: occurrence.occurrenceId,
        semanticId: occurrence.semanticSubjectId,
        type: 'node',
        kind: 'component',
        label: occurrence.deviceLabel,
        position: {
            x: occurrence.bounds.x,
            y: occurrence.bounds.y,
        },
        size: {
            width: occurrence.bounds.width,
            height: occurrence.bounds.height,
        },
    };
    const terminalBindings = normalizeArray(occurrence.terminalBindings);
    const placedAnchors = normalizeArray(occurrence.placedAnchors);
    const terminalByAnchorId = new Map(terminalBindings.map(binding => [binding.anchorId, binding]));
    return {
        ...node,
        renderVariant: 'electrical-device',
        markerKeys: [],
        electricalAnchors: placedAnchors.map(anchor => ({
            anchorId: anchor.anchorId,
            point: { ...anchor.point },
            side: terminalByAnchorId.get(anchor.anchorId)?.side ?? 'right',
            portSemanticId: terminalByAnchorId.get(anchor.anchorId)?.portSemanticId,
        })),
        presentationOccurrence: undefined,
        presentationGraphicOccurrence: occurrence,
        presentationRepresentation: undefined,
        presentationParts: normalizeArray(occurrence.parts),
        presentationTerminals: terminalBindings.map(binding => ({
            terminalId: `${occurrence.occurrenceId}:${binding.anchorId}`,
            subjectId: occurrence.semanticSubjectId,
            occurrenceId: occurrence.occurrenceId,
            portId: binding.portSemanticId,
            physicalTerminalId: binding.terminalIdentity,
            side: binding.side,
            marker: binding.terminalIdentity,
            number: binding.terminalIdentity,
            point: { ...binding.point },
            labelPoint: { ...binding.labelPoint },
            anchorId: binding.anchorId,
        })),
        presentationLabels: normalizeArray(occurrence.labels).map(label => ({
            labelId: label.labelId,
            subjectId: occurrence.semanticSubjectId,
            occurrenceId: occurrence.occurrenceId,
            role: label.role,
            value: label.value,
            point: {
                x: label.bounds.x,
                y: label.bounds.y,
            },
            anchorId: label.labelId,
        })),
    };
}

function resolveRepresentationForOccurrence(
    occurrence: AthenaGraphResolvedPresentationOccurrence,
    representationByProjectionId: Map<string, AthenaGraphResolvedPresentationRepresentation>,
    representationBySubjectId: Map<string, AthenaGraphResolvedPresentationRepresentation>,
): AthenaGraphResolvedPresentationRepresentation | undefined {
    for (const sourceProjectionId of normalizeArray(occurrence.sourceProjectionIds)) {
        const representation = representationByProjectionId.get(sourceProjectionId);
        if (representation) {
            return representation;
        }
    }
    return representationBySubjectId.get(occurrence.semanticId);
}

function scaleRepresentationPartsToNode(
    parts: AthenaGraphResolvedPresentationPart[],
    node: AthenaGLSPNode,
): AthenaGraphResolvedPresentationPart[] {
    return parts.map(part => ({
        ...part,
        bounds: {
            x: node.position.x,
            y: node.position.y,
            width: node.size.width,
            height: node.size.height,
        },
        commands: part.commands.map(command => scaleRepresentationCommand(command, part.bounds, node)),
        textSlots: part.textSlots.map(slot => ({
            ...slot,
            x: scaleWithinNode(slot.x, part.bounds.width, node.position.x, node.size.width),
            y: scaleWithinNode(slot.y, part.bounds.height, node.position.y, node.size.height),
        })),
    }));
}

function resolveRepresentationVisualNode(
    node: AthenaGLSPNode,
    representation: AthenaGraphResolvedPresentationRepresentation,
    anchors: AthenaGraphWorkbenchNodeAnchor[],
): AthenaGLSPNode {
    const anatomyBounds = representation.parts[0]?.bounds;
    const width = Math.max(1, anatomyBounds?.width ?? node.size.width);
    const height = Math.max(1, anatomyBounds?.height ?? node.size.height);
    const placement = resolveRepresentationAnchorPlacement(representation, anchors);

    return {
        ...node,
        position: {
            x: placement?.x ?? node.position.x,
            y: placement?.y ?? node.position.y,
        },
        size: {
            width,
            height,
        },
    };
}

function resolveRepresentationAnchorPlacement(
    representation: AthenaGraphResolvedPresentationRepresentation,
    anchors: AthenaGraphWorkbenchNodeAnchor[],
): AthenaGLSPPoint | undefined {
    const anchorById = new Map(anchors.map(anchor => [anchor.anchorId, anchor] as const));
    const terminals = normalizeArray(representation.terminals);
    const translations = terminals
        .flatMap(terminal => {
            const anchor = anchorById.get(terminal.routeAnchor.anchorId)
                ?? resolveCompatibleRepresentationAnchor(terminal.side, terminals.length, anchors);
            return anchor
                ? [{
                    x: anchor.point.x - terminal.routeAnchor.point.x,
                    y: anchor.point.y - terminal.routeAnchor.point.y,
                }]
                : [];
        });

    if (translations.length === 0) {
        return undefined;
    }

    return {
        x: Math.round(translations.reduce((sum, translation) => sum + translation.x, 0) / translations.length),
        y: Math.round(translations.reduce((sum, translation) => sum + translation.y, 0) / translations.length),
    };
}

function resolveCompatibleRepresentationAnchor(
    terminalSide: string,
    terminalCount: number,
    anchors: AthenaGraphWorkbenchNodeAnchor[],
): AthenaGraphWorkbenchNodeAnchor | undefined {
    if (anchors.length === 1 && terminalCount === 1) {
        return anchors[0];
    }
    const normalizedSide = terminalSide.toLowerCase();
    const sideMatches = anchors.filter(anchor => anchor.side.toLowerCase() === normalizedSide);
    return sideMatches.length === 1 ? sideMatches[0] : undefined;
}

function scaleRepresentationCommand(
    command: AthenaGraphResolvedPresentationPart['commands'][number],
    sourceBounds: AthenaGraphResolvedPresentationPart['bounds'],
    node: AthenaGLSPNode,
): AthenaGraphResolvedPresentationPart['commands'][number] {
    return {
        ...command,
        bounds: command.bounds
            ? {
                x: scaleWithinNode(command.bounds.x, sourceBounds.width, node.position.x, node.size.width),
                y: scaleWithinNode(command.bounds.y, sourceBounds.height, node.position.y, node.size.height),
                width: scaleLengthWithinNode(command.bounds.width, sourceBounds.width, node.size.width),
                height: scaleLengthWithinNode(command.bounds.height, sourceBounds.height, node.size.height),
            }
            : undefined,
        start: command.start
            ? {
                x: scaleWithinNode(command.start.x, sourceBounds.width, node.position.x, node.size.width),
                y: scaleWithinNode(command.start.y, sourceBounds.height, node.position.y, node.size.height),
            }
            : undefined,
        end: command.end
            ? {
                x: scaleWithinNode(command.end.x, sourceBounds.width, node.position.x, node.size.width),
                y: scaleWithinNode(command.end.y, sourceBounds.height, node.position.y, node.size.height),
            }
            : undefined,
        center: command.center
            ? {
                x: scaleWithinNode(command.center.x, sourceBounds.width, node.position.x, node.size.width),
                y: scaleWithinNode(command.center.y, sourceBounds.height, node.position.y, node.size.height),
            }
            : undefined,
        origin: command.origin
            ? {
                x: scaleWithinNode(command.origin.x, sourceBounds.width, node.position.x, node.size.width),
                y: scaleWithinNode(command.origin.y, sourceBounds.height, node.position.y, node.size.height),
            }
            : undefined,
        radius: command.radius === undefined
            ? undefined
            : Math.max(1, Math.round(command.radius * Math.min(node.size.width / sourceBounds.width, node.size.height / sourceBounds.height))),
        text: command.text,
    };
}

function scaleWithinNode(value: number, sourceLength: number, targetOrigin: number, targetLength: number): number {
    return targetOrigin + Math.round((value / sourceLength) * targetLength);
}

function scaleLengthWithinNode(value: number, sourceLength: number, targetLength: number): number {
    return Math.round((value / sourceLength) * targetLength);
}

function scaleRepresentationTerminalsToNode(
    representation: AthenaGraphResolvedPresentationRepresentation,
    node: AthenaGLSPNode,
): AthenaGraphWorkbenchPresentationTerminal[] {
    const bounds = representation.parts[0]?.bounds ?? { x: 0, y: 0, width: node.size.width, height: node.size.height };
    return normalizeArray(representation.terminals).map(terminal => {
        const point = {
            x: scaleWithinNode(terminal.routeAnchor.point.x, bounds.width, node.position.x, node.size.width),
            y: scaleWithinNode(terminal.routeAnchor.point.y, bounds.height, node.position.y, node.size.height),
        };
        return {
            terminalId: terminal.presentationTerminalId,
            subjectId: terminal.subjectId,
            occurrenceId: terminal.occurrenceId,
            portId: terminal.portId,
            physicalTerminalId: terminal.physicalTerminalId,
            side: terminal.side,
            marker: terminal.notation.marker,
            number: terminal.notation.number,
            point,
            labelPoint: defaultTerminalLabelPoint(point, terminal.side),
            anchorId: terminal.routeAnchor.anchorId,
        };
    });
}

function defaultTerminalLabelPoint(point: AthenaGLSPPoint, side: string): AthenaGLSPPoint {
    switch (side.toLowerCase()) {
        case 'left':
            return { x: point.x - 34, y: point.y - 8 };
        case 'top':
            return { x: point.x + 10, y: point.y - 14 };
        case 'bottom':
            return { x: point.x + 10, y: point.y + 18 };
        default:
            return { x: point.x + 10, y: point.y - 8 };
    }
}

function scaleRepresentationLabelsToNode(
    representation: AthenaGraphResolvedPresentationRepresentation,
    node: AthenaGLSPNode,
): AthenaGraphWorkbenchPresentationLabel[] {
    const bounds = representation.parts[0]?.bounds ?? { x: 0, y: 0, width: node.size.width, height: node.size.height };
    return normalizeArray(representation.labels).map(label => ({
        labelId: label.labelId,
        subjectId: label.subjectId,
        occurrenceId: label.occurrenceId,
        role: label.role,
        value: label.value,
        point: {
            x: scaleWithinNode(label.anchor.point.x, bounds.width, node.position.x, node.size.width),
            y: scaleWithinNode(label.anchor.point.y, bounds.height, node.position.y, node.size.height),
        },
        anchorId: label.anchor.anchorId,
    }));
}

function resolveNodeRenderVariant(
    node: AthenaGLSPNode,
    symbolKey: string | undefined,
    labelAnchor: AthenaGLSPElectricalAnchorSource | undefined,
    isElectricalFamily: boolean,
): AthenaGraphWorkbenchNode['renderVariant'] {
    if (!isElectricalFamily) {
        return node.kind === 'component' ? 'generic-component' : 'generic-label';
    }
    if (node.kind === 'component' && symbolKey?.startsWith('device.')) {
        return 'electrical-device';
    }
    if (node.kind === 'label' && labelAnchor && symbolKey?.startsWith('port.')) {
        return 'electrical-terminal-label';
    }
    return node.kind === 'component' ? 'generic-component' : 'generic-label';
}

function buildLabelLeader(
    node: AthenaGLSPNode,
    anchor: AthenaGLSPElectricalAnchorSource,
): AthenaGraphWorkbenchLeaderSegment {
    return {
        start: {
            x: anchor.x,
            y: anchor.y,
        },
        end: clampPointToNodeBounds(
            {
                x: anchor.x,
                y: anchor.y,
            },
            node,
        ),
    };
}

function clampPointToNodeBounds(
    point: AthenaGLSPPoint,
    node: AthenaGLSPNode,
): AthenaGLSPPoint {
    return {
        x: point.x < node.position.x
            ? node.position.x
            : point.x > node.position.x + node.size.width
                ? node.position.x + node.size.width
                : point.x,
        y: point.y < node.position.y
            ? node.position.y
            : point.y > node.position.y + node.size.height
                ? node.position.y + node.size.height
                : point.y,
    };
}

function resolveSurfaceTokens(
    contributions: AthenaGLSPRenderContributionSource[]
): AthenaGraphSurfaceTokens {
    const tokens: AthenaGraphSurfaceTokens = {
        canvas: {},
        node: {},
        edge: {},
    };

    for (const contribution of contributions) {
        for (const mapping of contribution.surfaceMappings) {
            if (mapping.surface === 'canvas') {
                Object.assign(tokens.canvas, mapping.tokens);
            } else if (mapping.surface === 'node') {
                Object.assign(tokens.node, mapping.tokens);
            } else if (mapping.surface === 'edge') {
                Object.assign(tokens.edge, mapping.tokens);
            }
        }
    }

    return tokens;
}

function buildWorkbenchEdge(
    edge: AthenaGLSPEdge,
    endpoints: AthenaGLSPElectricalConnectionEndpointSource[],
    anchorById: Map<string, AthenaGLSPElectricalAnchorSource>,
): AthenaGraphWorkbenchEdge {
    const bendPoints = edge.bendPoints ?? [];
    const routePoints = [edge.sourcePoint, ...bendPoints, edge.targetPoint].map(point => ({
        x: point.x,
        y: point.y,
    }));
    return {
        ...edge,
        bendPoints,
        routePoints,
        bendMarkerPoints: bendPoints.map(point => ({ x: point.x, y: point.y })),
        crossingMarkerPoints: [],
        connectionLabels: [],
        path: buildEdgePath(routePoints),
        conductorStyle: edge.routingStyle === 'orthogonal' || bendPoints.length > 0 ? 'electrical' : 'generic',
        line: undefined,
        terminals: [
            buildWorkbenchTerminal('source', edge.sourcePoint, edge.sourceAnchorId, edge.sourcePortSemanticId, endpoints, anchorById),
            buildWorkbenchTerminal('target', edge.targetPoint, edge.targetAnchorId, edge.targetPortSemanticId, endpoints, anchorById),
        ],
        presentationConnector: undefined,
    };
}

function buildWorkbenchEdgeFromPresentation(
    connector: AthenaGraphResolvedPresentationConnector,
    connectionMarkers: AthenaGraphWorkbenchConnectionMarker[],
    endpointsByConnectionId: Map<string, AthenaGLSPElectricalConnectionEndpointSource[]>,
    anchorById: Map<string, AthenaGLSPElectricalAnchorSource>,
    paintItemsByTarget: Map<string, AthenaGraphPresentationPaintItem>,
): AthenaGraphWorkbenchEdge {
    const routePoints = normalizeArray(connector.routePoints).map(point => ({ x: point.x, y: point.y }));
    const sourcePoint = routePoints[0] ?? { x: 0, y: 0 };
    const targetPoint = routePoints[routePoints.length - 1] ?? sourcePoint;
    const anchorScopedEndpoints = [...endpointsByConnectionId.values()]
        .flat()
        .filter(endpoint => endpoint.connectionSemanticId === connector.semanticId);
    const edge: AthenaGLSPEdge = {
        id: connector.occurrenceId,
        semanticId: connector.semanticId,
        type: 'edge',
        sourcePoint,
        targetPoint,
        routingStyle: 'orthogonal',
        bendPoints: routePoints.slice(1, Math.max(routePoints.length - 1, 1)),
        sourceAnchorId: connector.sourceEndpoint.anchorId,
        targetAnchorId: connector.targetEndpoint.anchorId,
        sourcePortSemanticId: connector.sourceEndpoint.portSemanticId,
        targetPortSemanticId: connector.targetEndpoint.portSemanticId,
    };
    const built = buildWorkbenchEdge(edge, anchorScopedEndpoints, anchorById);
    return {
        ...built,
        routePoints,
        bendMarkerPoints: routePoints.slice(1, Math.max(routePoints.length - 1, 1)),
        crossingMarkerPoints: connectionMarkers.filter(marker => marker.connectorIds.includes(connector.occurrenceId)),
        connectionLabels: buildConnectionLabels(connector, paintItemsByTarget),
        path: buildEdgePath(routePoints),
        line: connector.line,
        presentationConnector: connector,
    };
}

function buildConnectionLabels(
    connector: AthenaGraphResolvedPresentationConnector,
    paintItemsByTarget: Map<string, AthenaGraphPresentationPaintItem>,
): AthenaGraphWorkbenchConnectionLabel[] {
    return orderByPaintPlan(
        (connector.labels ?? []).filter(label => isPaintTargetVisible(label.labelId, paintItemsByTarget)),
        label => label.labelId,
        paintItemsByTarget,
    )
        .map(label => ({
            labelId: label.labelId,
            text: label.text,
            point: { ...label.point },
            canvasDisplay: label.display === 'always' ? 'always' : 'selection',
        }));
}

function buildWorkbenchTerminal(
    role: 'source' | 'target',
    point: AthenaGLSPPoint,
    fallbackAnchorId: string | undefined,
    fallbackPortSemanticId: string | undefined,
    endpoints: AthenaGLSPElectricalConnectionEndpointSource[],
    anchorById: Map<string, AthenaGLSPElectricalAnchorSource>,
): AthenaGraphWorkbenchEdgeTerminal {
    const endpoint = endpoints.find(candidate => candidate.endpointRole === role);
    const anchor = endpoint?.anchorId
        ? anchorById.get(endpoint.anchorId)
        : fallbackAnchorId
            ? anchorById.get(fallbackAnchorId)
            : undefined;
    return {
        role,
        point: {
            x: point.x,
            y: point.y,
        },
        endpointId: endpoint?.endpointId,
        anchorId: endpoint?.anchorId ?? fallbackAnchorId,
        portSemanticId: endpoint?.portSemanticId ?? fallbackPortSemanticId ?? anchor?.portSemanticId,
        ownerSemanticId: anchor?.ownerSemanticId,
        nodeId: anchor?.nodeId,
        labelId: anchor?.labelId,
    };
}

function groupEndpointsByConnectionId(
    endpoints: AthenaGLSPElectricalConnectionEndpointSource[],
): Map<string, AthenaGLSPElectricalConnectionEndpointSource[]> {
    const grouped = new Map<string, AthenaGLSPElectricalConnectionEndpointSource[]>();
    for (const endpoint of endpoints) {
        const current = grouped.get(endpoint.projectionConnectionId);
        if (current) {
            current.push(endpoint);
        } else {
            grouped.set(endpoint.projectionConnectionId, [endpoint]);
        }
    }
    return grouped;
}

function groupAnchorsByNodeId(
    anchors: AthenaGLSPElectricalAnchorSource[],
): Map<string, AthenaGLSPElectricalAnchorSource[]> {
    const grouped = new Map<string, AthenaGLSPElectricalAnchorSource[]>();
    for (const anchor of anchors) {
        const current = grouped.get(anchor.nodeId);
        if (current) {
            current.push(anchor);
        } else {
            grouped.set(anchor.nodeId, [anchor]);
        }
    }
    return grouped;
}

function buildEdgePath(points: AthenaGLSPPoint[]): string {
    if (points.length === 0) {
        return '';
    }
    return points.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`).join(' ');
}

function normalizeArray<T>(value: readonly T[] | T[] | undefined): T[] {
    return Array.isArray(value) ? [...value] : [];
}
