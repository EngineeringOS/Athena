import {
    AthenaGLSPDiagram,
    AthenaGLSPElectricalAnchorSource,
    AthenaGLSPElectricalConnectionEndpointSource,
    AthenaGLSPEdge,
    AthenaGLSPNode,
    AthenaGLSPPoint,
    AthenaGLSPRenderContributionSource
} from '@engineeringood/athena-graph-glsp';

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

/** Pure presentation model used by the first Athena graphical workbench panel. */
export type AthenaGraphWorkbenchModel = {
    headerTitle: string;
    viewLabel: string;
    viewFamilyId?: string;
    statusLabel: string;
    statusTone: 'ready' | 'warning' | 'idle';
    semanticPath: string;
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
        ownershipContract: AthenaGLSPDiagram['supportedViews'][number]['ownershipContract'];
        isActive: boolean;
    }>;
    diagnostics: AthenaGLSPDiagram['diagnostics'];
    activeRenderContributions: AthenaGLSPRenderContributionSource[];
    nodes: AthenaGLSPNode[];
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

export type AthenaGraphWorkbenchEdge = AthenaGLSPEdge & {
    routePoints: AthenaGLSPPoint[];
    bendMarkerPoints: AthenaGLSPPoint[];
    path: string;
    conductorStyle: 'electrical' | 'generic';
    terminals: AthenaGraphWorkbenchEdgeTerminal[];
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
    const supportedViews = normalizeArray(diagram.supportedViews);
    const diagnostics = normalizeArray(diagram.diagnostics);
    const sheets = normalizeArray(diagram.sheets);
    const crossReferences = normalizeArray(diagram.crossReferences);
    const electricalAnchors = normalizeArray(diagram.electricalAnchors);
    const electricalConnectionEndpoints = normalizeArray(diagram.electricalConnectionEndpoints);
    const renderContributions = normalizeArray(diagram.activeRenderContributions);
    const canvasWidth = graph.canvas.width > 0 ? graph.canvas.width : fallbackCanvasWidth;
    const canvasHeight = graph.canvas.height > 0 ? graph.canvas.height : fallbackCanvasHeight;
    const activeView = supportedViews.find(view => view.viewId === diagram.activeViewId);
    const viewLabel = activeView?.displayName ?? diagram.activeViewId ?? 'graph';
    const anchorById = new Map(
        electricalAnchors.map(anchor => [anchor.anchorId, anchor] as const)
    );
    const endpointsByConnectionId = groupEndpointsByConnectionId(electricalConnectionEndpoints);
    const edges = graphEdges.map(edge => buildWorkbenchEdge(
        edge,
        endpointsByConnectionId.get(edge.id) ?? [],
        anchorById,
    ));

    return {
        headerTitle: diagram.projectName,
        viewLabel,
        viewFamilyId: activeView?.familyId,
        statusLabel: diagram.status,
        statusTone: diagram.status === 'ready' ? 'ready' : 'warning',
        semanticPath: diagram.semanticPath,
        activeSheetId: diagram.activeSheetId,
        sheetCount: sheets.length,
        notationPackId: diagram.notationPack?.packId,
        crossReferenceCount: crossReferences.length,
        svgViewBox: `0 0 ${canvasWidth} ${canvasHeight}`,
        metrics: {
            nodeCount: graphNodes.length,
            edgeCount: graphEdges.length,
            supportedViewCount: supportedViews.length,
            diagnosticCount: diagnostics.length,
        },
        supportedViews: supportedViews.map(view => ({
            ...view,
            isActive: view.viewId === diagram.activeViewId,
        })),
        diagnostics,
        activeRenderContributions: renderContributions,
        nodes: graphNodes,
        edges,
        canvas: {
            width: canvasWidth,
            height: canvasHeight,
        },
        sceneBounds: resolveSceneBounds(graphNodes, edges, canvasWidth, canvasHeight),
        surfaceTokens: resolveSurfaceTokens(renderContributions),
        emptyState: resolveEmptyState(diagram, graphNodes, graphEdges),
    };
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

function resolveEmptyState(
    diagram: AthenaGLSPDiagram,
    nodes: AthenaGLSPNode[],
    edges: AthenaGLSPEdge[],
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

function resolveSceneBounds(
    nodes: AthenaGLSPNode[],
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

function buildEdgePath(points: AthenaGLSPPoint[]): string {
    if (points.length === 0) {
        return '';
    }
    return points.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`).join(' ');
}

function normalizeArray<T>(value: readonly T[] | T[] | undefined): T[] {
    return Array.isArray(value) ? [...value] : [];
}
