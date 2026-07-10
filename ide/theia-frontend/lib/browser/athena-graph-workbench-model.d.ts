import { AthenaGLSPDiagram, AthenaGLSPEdge, AthenaGLSPNode, AthenaGLSPRenderContributionSource } from '@engineeringood/athena-graph-glsp';
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
    statusLabel: string;
    statusTone: 'ready' | 'warning' | 'idle';
    semanticPath: string;
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
        isActive: boolean;
    }>;
    diagnostics: AthenaGLSPDiagram['diagnostics'];
    activeRenderContributions: AthenaGLSPRenderContributionSource[];
    nodes: AthenaGLSPNode[];
    edges: AthenaGLSPEdge[];
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
/** Builds one deterministic workbench-facing view model from the adapter-owned graph diagram. */
export declare function buildAthenaGraphWorkbenchModel(diagram: AthenaGLSPDiagram): AthenaGraphWorkbenchModel;
export declare function clampAthenaGraphZoom(zoom: number): number;
export declare function fitAthenaGraphViewport(bounds: AthenaGraphSceneBounds, viewport: AthenaGraphViewportSize): AthenaGraphViewportTransform;
export declare function panAthenaGraphViewport(transform: AthenaGraphViewportTransform, deltaX: number, deltaY: number): AthenaGraphViewportTransform;
export declare function zoomAthenaGraphViewportAtPoint(transform: AthenaGraphViewportTransform, screenPoint: {
    x: number;
    y: number;
}, nextZoom: number): AthenaGraphViewportTransform;
//# sourceMappingURL=athena-graph-workbench-model.d.ts.map