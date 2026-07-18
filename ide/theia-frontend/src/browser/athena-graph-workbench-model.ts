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
    AthenaGraphResolvedPresentationOccurrence,
    AthenaGraphResolvedPresentationPart,
    resolvePresentationConnectors,
    resolvePresentationOccurrences
} from './athena-graph-presentation-model';

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
    persisted: false;
};

export type AthenaGraphLayoutSourceEdit = {
    uri: string;
    range: {
        start: { line: number; character: number };
        end: { line: number; character: number };
    };
    newText: string;
    selectionRange?: {
        start: { line: number; character: number };
        end: { line: number; character: number };
    };
    suggestedSemanticId?: string;
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
    sheetChrome: AthenaGraphWorkbenchSheetChrome;
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
    crossReferenceMarkers: AthenaGraphWorkbenchCrossReferenceMarker[];
};

export type AthenaGraphWorkbenchSheetFrame = {
    width: number;
    height: number;
};

export type AthenaGraphWorkbenchSheetGrid = {
    majorStep: number;
    minorStep: number;
};

export type AthenaGraphWorkbenchSheetSummary = {
    sheetId: string;
    displayName: string;
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
};

export type AthenaGraphWorkbenchCrossReferenceMarker = {
    semanticId: string;
    kind: string;
    markerLabel: string;
    sheetIds: string[];
    occurrenceIds: string[];
    isActiveSheetLinked: boolean;
};

export type AthenaGraphWorkbenchEdge = AthenaGLSPEdge & {
    routePoints: AthenaGLSPPoint[];
    bendMarkerPoints: AthenaGLSPPoint[];
    path: string;
    conductorStyle: 'electrical' | 'generic';
    terminals: AthenaGraphWorkbenchEdgeTerminal[];
    presentationConnector?: AthenaGraphResolvedPresentationConnector;
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

export type AthenaGraphWorkbenchNode = AthenaGLSPNode & {
    renderVariant: 'generic-component' | 'electrical-device' | 'generic-label' | 'electrical-terminal-label';
    notationSymbolKey?: string;
    labelPolicy?: string;
    markerKeys: string[];
    labelLeader?: AthenaGraphWorkbenchLeaderSegment;
    electricalAnchors: AthenaGraphWorkbenchNodeAnchor[];
    presentationOccurrence?: AthenaGraphResolvedPresentationOccurrence;
    presentationParts: AthenaGraphResolvedPresentationPart[];
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
    const graphNodes = normalizeArray(graph.nodes);
    const graphEdges = normalizeArray(graph.edges);
    const presentationOccurrences = resolvePresentationOccurrences(diagram);
    const presentationConnectors = resolvePresentationConnectors(diagram);
    const supportedViews = normalizeArray(diagram.supportedViews);
    const diagnostics = normalizeArray(diagram.diagnostics);
    const sheets = normalizeArray(diagram.sheets);
    const crossReferences = normalizeArray(diagram.crossReferences);
    const notationSubjects = normalizeArray(diagram.notationPack?.subjects);
    const electricalAnchors = normalizeArray(diagram.electricalAnchors);
    const electricalConnectionEndpoints = normalizeArray(diagram.electricalConnectionEndpoints);
    const renderContributions = normalizeArray(diagram.activeRenderContributions);
    const canvasWidth = graph.canvas.width > 0 ? graph.canvas.width : fallbackCanvasWidth;
    const canvasHeight = graph.canvas.height > 0 ? graph.canvas.height : fallbackCanvasHeight;
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
    const nodes = presentationOccurrences.length > 0
        ? presentationOccurrences.map(occurrence => buildWorkbenchNodeFromPresentation(
            occurrence,
            notationBySemanticId,
            anchorsByNodeId,
            anchorByLabelId,
            isElectricalFamily,
        ))
        : graphNodes.map(node => buildWorkbenchNode(
            node,
            notationBySemanticId,
            anchorsByNodeId,
            anchorByLabelId,
            isElectricalFamily,
        ));
    const edges = presentationConnectors.length > 0
        ? presentationConnectors.map(connector => buildWorkbenchEdgeFromPresentation(
            connector,
            endpointsByConnectionId,
            anchorById,
        ))
        : graphEdges.map(edge => buildWorkbenchEdge(
            edge,
            endpointsByConnectionId.get(edge.id) ?? [],
            anchorById,
        ));
    const sheetChrome = resolveSheetChrome(diagram, canvasWidth, canvasHeight);

    return {
        headerTitle: diagram.projectName,
        viewLabel,
        viewFamilyId: activeView?.familyId,
        isElectricalFamily,
        statusLabel: diagram.status,
        statusTone: diagram.status === 'ready' ? 'ready' : 'warning',
        semanticPath: diagram.semanticPath,
        snapshotId: resolveWorkbenchSnapshotId(diagram),
        activeSheetId: diagram.activeSheetId,
        sheetCount: sheets.length,
        notationPackId: diagram.notationPack?.packId,
        crossReferenceCount: crossReferences.length,
        svgViewBox: `0 0 ${canvasWidth} ${canvasHeight}`,
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
        diagnostics,
        activeRenderContributions: renderContributions,
        sheetChrome,
        nodes,
        edges,
        canvas: {
            width: canvasWidth,
            height: canvasHeight,
        },
        sceneBounds: resolveSceneBounds(nodes, edges, canvasWidth, canvasHeight),
        surfaceTokens: resolveSurfaceTokens(renderContributions),
        emptyState: resolveEmptyState(diagram, nodes, edges),
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
            reason: 'Route and label adjustment persistence is outside M22 scope.',
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
): AthenaGraphLayoutMutationPreview {
    const authoredIntent = buildAthenaGraphAuthoredLayoutIntent(intent);
    const layoutBlockSnippet = serializeAthenaGraphAuthoredLayoutIntent(authoredIntent);
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
        layoutBlockSnippet,
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

export function serializeAthenaGraphAuthoredLayoutIntent(intent: AthenaGraphAuthoredLayoutIntent): string {
    return [
        `layout ${intent.viewFamily} {`,
        ...intent.statements.map(statement => `  ${serializeAthenaGraphAuthoredLayoutIntentStatement(statement)}`),
        `}`,
    ].join('\n');
}

function serializeAthenaGraphAuthoredLayoutIntentStatement(statement: AthenaGraphAuthoredLayoutIntentStatement): string {
    if (statement.priority !== 'preference') {
        throw new Error('M23 source syntax admits only default preference layout hints.');
    }
    const subject = semanticIdToAuthoredName(statement.subject);
    const target = semanticIdToAuthoredName(statement.target);
    switch (statement.relation) {
        case 'near':
            return `place ${subject} near ${target}`;
        case 'below':
            return `place ${subject} below ${target}`;
        case 'aligned-with':
            return `align ${subject} aligned-with ${target} axis ${statement.axis ?? 'vertical'}`;
        case 'grouped-with':
            return `group ${subject} grouped-with ${target}`;
    }
}

export function buildAthenaGraphLayoutSourceEdit(args: {
    preview: AthenaGraphLayoutMutationPreview;
    documentText?: string;
    insertionLine: number;
    insertionCharacter: number;
}): AthenaGraphLayoutSourceEdit {
    const preview = args.preview;
    const position = args.documentText
        ? resolveSystemScopedLayoutInsertionPosition(args.documentText, args.insertionLine, args.insertionCharacter)
        : {
            line: Math.max(0, args.insertionLine),
            character: Math.max(0, args.insertionCharacter),
        };
    const layoutSource = indentLayoutBlock(
        serializeAthenaGraphAuthoredLayoutIntent(preview.authoredIntent),
        '  ',
    );
    return {
        uri: preview.sourceUri,
        range: {
            start: position,
            end: position,
        },
        newText: `

${layoutSource}
`,
        selectionRange: {
            start: position,
            end: position,
        },
        suggestedSemanticId: preview.subjectSemanticId,
    };
}

function resolveSystemScopedLayoutInsertionPosition(
    documentText: string,
    fallbackLine: number,
    fallbackCharacter: number,
): { line: number; character: number } {
    const systemCloseOffset = documentText.lastIndexOf('}');
    if (systemCloseOffset < 0) {
        return {
            line: Math.max(0, fallbackLine),
            character: Math.max(0, fallbackCharacter),
        };
    }
    return offsetToPosition(documentText, systemCloseOffset);
}

function offsetToPosition(text: string, offset: number): { line: number; character: number } {
    const beforeOffset = text.slice(0, Math.max(0, offset));
    const lines = beforeOffset.split(/\r\n|\r|\n/);
    return {
        line: Math.max(0, lines.length - 1),
        character: lines.at(-1)?.length ?? 0,
    };
}

function indentLayoutBlock(source: string, indent: string): string {
    return source
        .split('\n')
        .map(line => `${indent}${line}`)
        .join('\n');
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

function resolveWorkbenchSnapshotId(diagram: AthenaGLSPDiagram): string {
    return [
        'workbench-snapshot',
        diagram.semanticPath || 'unknown-source',
        diagram.activeViewId || 'unknown-view',
        diagram.activeSheetId || 'unknown-sheet',
    ].join(':');
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

    return {
        frame: {
            width: canvasWidth,
            height: canvasHeight,
        },
        grid: {
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
            ...(activeSheet.previousSheetId ? { previousSheetId: activeSheet.previousSheetId } : {}),
            ...(activeSheet.nextSheetId ? { nextSheetId: activeSheet.nextSheetId } : {}),
        } : undefined,
        crossReferenceMarkers,
    };
}

function buildSheetSummary(
    sheet: AthenaGLSPDiagram['sheets'][number],
    activeSheetId: string | undefined,
): AthenaGraphWorkbenchSheetSummary {
    const subjectSemanticIds = normalizeArray(sheet.subjectSemanticIds);
    return {
        sheetId: sheet.sheetId,
        displayName: sheet.displayName,
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
    canvasHeight: number
): AthenaGraphSceneBounds {
    let minX = Number.POSITIVE_INFINITY;
    let minY = Number.POSITIVE_INFINITY;
    let maxX = Number.NEGATIVE_INFINITY;
    let maxY = Number.NEGATIVE_INFINITY;

    for (const node of nodes) {
        minX = Math.min(minX, node.position.x);
        minY = Math.min(minY, node.position.y);
        maxX = Math.max(maxX, node.position.x + node.size.width);
        maxY = Math.max(maxY, node.position.y + node.size.height);
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

function buildWorkbenchNode(
    node: AthenaGLSPNode,
    notationBySemanticId: Map<string, NonNullable<AthenaGLSPDiagram['notationPack']>['subjects'][number]>,
    anchorsByNodeId: Map<string, AthenaGLSPElectricalAnchorSource[]>,
    anchorByLabelId: Map<string, AthenaGLSPElectricalAnchorSource>,
    isElectricalFamily: boolean,
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
    const renderVariant = resolveNodeRenderVariant(node, notation?.symbolKey, labelAnchor, isElectricalFamily);

    return {
        ...node,
        renderVariant,
        notationSymbolKey: notation?.symbolKey,
        labelPolicy: notation?.labelPolicy,
        markerKeys: normalizeArray(notation?.markerKeys),
        labelLeader: renderVariant === 'electrical-terminal-label' && labelAnchor
            ? buildLabelLeader(node, labelAnchor)
            : undefined,
        electricalAnchors: nodeAnchors,
        presentationOccurrence: undefined,
        presentationParts: [],
    };
}

function buildWorkbenchNodeFromPresentation(
    occurrence: AthenaGraphResolvedPresentationOccurrence,
    notationBySemanticId: Map<string, NonNullable<AthenaGLSPDiagram['notationPack']>['subjects'][number]>,
    anchorsByNodeId: Map<string, AthenaGLSPElectricalAnchorSource[]>,
    anchorByLabelId: Map<string, AthenaGLSPElectricalAnchorSource>,
    isElectricalFamily: boolean,
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
    const renderVariant = resolveNodeRenderVariant(baseNode, notation?.symbolKey, labelAnchor, isElectricalFamily);
    return {
        ...baseNode,
        renderVariant,
        notationSymbolKey: notation?.symbolKey,
        labelPolicy: notation?.labelPolicy,
        markerKeys: normalizeArray(notation?.markerKeys.length ? notation.markerKeys : occurrence.markerKeys),
        labelLeader: renderVariant === 'electrical-terminal-label' && labelAnchor
            ? buildLabelLeader(baseNode, labelAnchor)
            : undefined,
        electricalAnchors: nodeAnchors,
        presentationOccurrence: occurrence,
        presentationParts: normalizeArray(occurrence.parts),
    };
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
        path: buildEdgePath(routePoints),
        conductorStyle: edge.routingStyle === 'orthogonal' || bendPoints.length > 0 ? 'electrical' : 'generic',
        terminals: [
            buildWorkbenchTerminal('source', edge.sourcePoint, edge.sourceAnchorId, edge.sourcePortSemanticId, endpoints, anchorById),
            buildWorkbenchTerminal('target', edge.targetPoint, edge.targetAnchorId, edge.targetPortSemanticId, endpoints, anchorById),
        ],
        presentationConnector: undefined,
    };
}

function buildWorkbenchEdgeFromPresentation(
    connector: AthenaGraphResolvedPresentationConnector,
    endpointsByConnectionId: Map<string, AthenaGLSPElectricalConnectionEndpointSource[]>,
    anchorById: Map<string, AthenaGLSPElectricalAnchorSource>,
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
        sourceAnchorId: connector.sourceAnchorId,
        targetAnchorId: connector.targetAnchorId,
        sourcePortSemanticId: connector.sourcePortSemanticId,
        targetPortSemanticId: connector.targetPortSemanticId,
    };
    const built = buildWorkbenchEdge(edge, anchorScopedEndpoints, anchorById);
    return {
        ...built,
        routePoints,
        bendMarkerPoints: routePoints.slice(1, Math.max(routePoints.length - 1, 1)),
        path: buildEdgePath(routePoints),
        presentationConnector: connector,
    };
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
