import {
    AthenaGLSPCrossReferenceSource,
    AthenaGLSPDiagnosticSource,
    AthenaGLSPElectricalAnchorSource,
    AthenaGLSPElectricalConnectionEndpointSource,
    AthenaGLSPElectricalRoutingCorridorSource,
    AthenaGLSPGovernedCommandSource,
    AthenaGLSPNotationPackSource,
    AthenaGLSPPoint,
    AthenaGLSPProjectionViewSource,
    AthenaGLSPRenderContributionSource,
    AthenaGLSPSheetSource,
} from './athena-glsp-projection-source';

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
    activeSheetId?: string;
    sheets: AthenaGLSPSheetSource[];
    notationPack?: AthenaGLSPNotationPackSource;
    crossReferences: AthenaGLSPCrossReferenceSource[];
    electricalAnchors: AthenaGLSPElectricalAnchorSource[];
    electricalConnectionEndpoints: AthenaGLSPElectricalConnectionEndpointSource[];
    electricalRoutingCorridors: AthenaGLSPElectricalRoutingCorridorSource[];
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
    semanticId: string;
    type: 'node';
    kind: 'component' | 'label';
    label: string;
    position: AthenaGLSPPoint;
    size: AthenaGLSPSize;
};

export type AthenaGLSPEdge = {
    id: string;
    semanticId: string;
    type: 'edge';
    sourcePoint: AthenaGLSPPoint;
    targetPoint: AthenaGLSPPoint;
    routingStyle?: string;
    bendPoints: AthenaGLSPPoint[];
    sourceAnchorId?: string;
    targetAnchorId?: string;
    sourcePortSemanticId?: string;
    targetPortSemanticId?: string;
};

export type AthenaGLSPSize = {
    width: number;
    height: number;
};
