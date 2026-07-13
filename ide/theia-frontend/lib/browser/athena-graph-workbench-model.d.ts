import { AthenaGLSPDiagram, AthenaGLSPEdge, AthenaGLSPNode, AthenaGLSPPoint, AthenaGLSPRenderContributionSource } from '@engineeringood/athena-graph-glsp';
import { AthenaGraphResolvedPresentationConnector, AthenaGraphResolvedPresentationOccurrence, AthenaGraphResolvedPresentationPart } from './athena-graph-presentation-model';
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
    isElectricalFamily: boolean;
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
export declare function buildAthenaGraphWorkbenchModel(diagram: AthenaGLSPDiagram): AthenaGraphWorkbenchModel;
export declare function clampAthenaGraphZoom(zoom: number): number;
export declare function fitAthenaGraphViewport(bounds: AthenaGraphSceneBounds, viewport: AthenaGraphViewportSize): AthenaGraphViewportTransform;
export declare function panAthenaGraphViewport(transform: AthenaGraphViewportTransform, deltaX: number, deltaY: number): AthenaGraphViewportTransform;
export declare function zoomAthenaGraphViewportAtPoint(transform: AthenaGraphViewportTransform, screenPoint: {
    x: number;
    y: number;
}, nextZoom: number): AthenaGraphViewportTransform;
export declare function resizeAthenaGraphViewport(transform: AthenaGraphViewportTransform, previousViewport: AthenaGraphViewportSize, nextViewport: AthenaGraphViewportSize): AthenaGraphViewportTransform;
//# sourceMappingURL=athena-graph-workbench-model.d.ts.map