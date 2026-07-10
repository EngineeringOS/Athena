"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.buildAthenaGraphWorkbenchModel = buildAthenaGraphWorkbenchModel;
exports.clampAthenaGraphZoom = clampAthenaGraphZoom;
exports.fitAthenaGraphViewport = fitAthenaGraphViewport;
exports.panAthenaGraphViewport = panAthenaGraphViewport;
exports.zoomAthenaGraphViewportAtPoint = zoomAthenaGraphViewportAtPoint;
/** Builds one deterministic workbench-facing view model from the adapter-owned graph diagram. */
function buildAthenaGraphWorkbenchModel(diagram) {
    const fallbackCanvasWidth = 960;
    const fallbackCanvasHeight = 540;
    const canvasWidth = diagram.graph.canvas.width > 0 ? diagram.graph.canvas.width : fallbackCanvasWidth;
    const canvasHeight = diagram.graph.canvas.height > 0 ? diagram.graph.canvas.height : fallbackCanvasHeight;
    const activeView = diagram.supportedViews.find(view => view.viewId === diagram.activeViewId);
    const viewLabel = activeView?.displayName ?? diagram.activeViewId;
    return {
        headerTitle: diagram.projectName,
        viewLabel,
        statusLabel: diagram.status,
        statusTone: diagram.status === 'ready' ? 'ready' : 'warning',
        semanticPath: diagram.semanticPath,
        svgViewBox: `0 0 ${canvasWidth} ${canvasHeight}`,
        metrics: {
            nodeCount: diagram.graph.nodes.length,
            edgeCount: diagram.graph.edges.length,
            supportedViewCount: diagram.supportedViews.length,
            diagnosticCount: diagram.diagnostics.length,
        },
        supportedViews: diagram.supportedViews.map(view => ({
            ...view,
            isActive: view.viewId === diagram.activeViewId,
        })),
        diagnostics: diagram.diagnostics,
        activeRenderContributions: diagram.activeRenderContributions,
        nodes: diagram.graph.nodes,
        edges: diagram.graph.edges,
        canvas: {
            width: canvasWidth,
            height: canvasHeight,
        },
        sceneBounds: resolveSceneBounds(diagram.graph.nodes, diagram.graph.edges, canvasWidth, canvasHeight),
        surfaceTokens: resolveSurfaceTokens(diagram.activeRenderContributions),
        emptyState: resolveEmptyState(diagram),
    };
}
function clampAthenaGraphZoom(zoom) {
    if (!Number.isFinite(zoom)) {
        return 1;
    }
    return Math.min(2.5, Math.max(0.2, zoom));
}
function fitAthenaGraphViewport(bounds, viewport) {
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
function panAthenaGraphViewport(transform, deltaX, deltaY) {
    return {
        ...transform,
        offsetX: transform.offsetX + deltaX,
        offsetY: transform.offsetY + deltaY,
    };
}
function zoomAthenaGraphViewportAtPoint(transform, screenPoint, nextZoom) {
    const zoom = clampAthenaGraphZoom(nextZoom);
    const worldX = (screenPoint.x - transform.offsetX) / transform.zoom;
    const worldY = (screenPoint.y - transform.offsetY) / transform.zoom;
    return {
        zoom,
        offsetX: screenPoint.x - (worldX * zoom),
        offsetY: screenPoint.y - (worldY * zoom),
    };
}
function resolveEmptyState(diagram) {
    if (diagram.status !== 'ready') {
        return {
            title: 'Projection unavailable',
            message: diagram.unavailableReason ?? 'Athena did not publish a usable graphical projection for the active workbench session.',
        };
    }
    if (diagram.graph.nodes.length === 0 && diagram.graph.edges.length === 0) {
        return {
            title: 'Projection is empty',
            message: 'Athena published an active graphical view, but no nodes or relationships are currently visible in that projection.',
        };
    }
    return undefined;
}
function resolveSceneBounds(nodes, edges, canvasWidth, canvasHeight) {
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
        minX = Math.min(minX, edge.sourcePoint.x, edge.targetPoint.x);
        minY = Math.min(minY, edge.sourcePoint.y, edge.targetPoint.y);
        maxX = Math.max(maxX, edge.sourcePoint.x, edge.targetPoint.x);
        maxY = Math.max(maxY, edge.sourcePoint.y, edge.targetPoint.y);
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
function resolveSurfaceTokens(contributions) {
    const tokens = {
        canvas: {},
        node: {},
        edge: {},
    };
    for (const contribution of contributions) {
        for (const mapping of contribution.surfaceMappings) {
            if (mapping.surface === 'canvas') {
                Object.assign(tokens.canvas, mapping.tokens);
            }
            else if (mapping.surface === 'node') {
                Object.assign(tokens.node, mapping.tokens);
            }
            else if (mapping.surface === 'edge') {
                Object.assign(tokens.edge, mapping.tokens);
            }
        }
    }
    return tokens;
}
//# sourceMappingURL=athena-graph-workbench-model.js.map