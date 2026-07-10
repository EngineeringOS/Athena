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
};

export type AthenaGLSPGovernedCommandSource = {
    commandId: string;
    displayName: string;
    description: string;
    requiredArguments: string[];
};

export type AthenaGLSPReadyProjectionSource = {
    viewId: string;
    systemName: string;
    canvasWidth: number;
    canvasHeight: number;
    activeRenderContributions: AthenaGLSPRenderContributionSource[];
    components: AthenaGLSPComponentSource[];
    connections: AthenaGLSPConnectionSource[];
    labels: AthenaGLSPLabelSource[];
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
    semanticId: string;
    label: string;
    x: number;
    y: number;
    width: number;
    height: number;
};

export type AthenaGLSPConnectionSource = {
    semanticId: string;
    x1: number;
    y1: number;
    x2: number;
    y2: number;
};

export type AthenaGLSPLabelSource = {
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

/** Disposable GLSP-shaped output rebuilt from Athena-owned projection state whenever needed. */
export type AthenaGLSPDiagram = {
    kind: 'athena-glsp-diagram';
    projectName: string;
    semanticPath: string;
    activeViewId: string;
    status: string;
    activeRenderContributions: AthenaGLSPRenderContributionSource[];
    supportedViews: AthenaGLSPProjectionViewSource[];
    governedCommands: AthenaGLSPGovernedCommandSource[];
    unavailableReason?: string;
    diagnostics: AthenaGLSPDiagnosticSource[];
    graph: AthenaGLSPGraph;
};

export type AthenaGLSPGraph = {
    id: string;
    type: 'graph';
    canvas: AthenaGLSPCanvasBounds;
    nodes: AthenaGLSPNode[];
    edges: AthenaGLSPEdge[];
};

export type AthenaGLSPCanvasBounds = {
    width: number;
    height: number;
};

export type AthenaGLSPNode = {
    id: string;
    type: 'node';
    kind: 'component' | 'label';
    label: string;
    position: AthenaGLSPPoint;
    size: AthenaGLSPSize;
};

export type AthenaGLSPEdge = {
    id: string;
    type: 'edge';
    sourcePoint: AthenaGLSPPoint;
    targetPoint: AthenaGLSPPoint;
};

export type AthenaGLSPPoint = {
    x: number;
    y: number;
};

export type AthenaGLSPSize = {
    width: number;
    height: number;
};
