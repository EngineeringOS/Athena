import { AthenaGLSPDiagram, AthenaGLSPPoint, AthenaGLSPPresentationAnchorBindingSource, AthenaGLSPPresentationBoundsSource, AthenaGLSPPresentationShapeCommandSource } from '@engineeringood/athena-graph-glsp';
export type AthenaGraphResolvedPresentationPart = {
    partId: string;
    primitiveId: string;
    bounds: AthenaGLSPPresentationBoundsSource;
    commands: AthenaGLSPPresentationShapeCommandSource[];
    textSlots: Array<{
        slotId: string;
        text?: string;
        x: number;
        y: number;
        tokenKey: string;
    }>;
    tokenDefaults: Record<string, string>;
    tokenOverrides: Record<string, string>;
};
export type AthenaGraphResolvedPresentationOccurrence = {
    occurrenceId: string;
    semanticId: string;
    layer: string;
    bounds: AthenaGLSPPresentationBoundsSource;
    displayLabel?: string;
    orientation: string;
    markerKeys: string[];
    sourceProjectionIds: string[];
    anchorBindings: AthenaGLSPPresentationAnchorBindingSource[];
    textSlots: Array<{
        slotId: string;
        text?: string;
        x: number;
        y: number;
        tokenKey: string;
    }>;
    parts: AthenaGraphResolvedPresentationPart[];
};
export type AthenaGraphResolvedPresentationConnector = {
    occurrenceId: string;
    semanticId: string;
    primitiveId: string;
    layer: string;
    routePoints: AthenaGLSPPoint[];
    sourceAnchorId?: string;
    targetAnchorId?: string;
    sourcePortSemanticId?: string;
    targetPortSemanticId?: string;
    markerKeys: string[];
    tokenOverrides: Record<string, string>;
    sourceProjectionIds: string[];
};
export declare function resolvePresentationOccurrences(diagram: AthenaGLSPDiagram): AthenaGraphResolvedPresentationOccurrence[];
export declare function resolvePresentationConnectors(diagram: AthenaGLSPDiagram): AthenaGraphResolvedPresentationConnector[];
//# sourceMappingURL=athena-graph-presentation-model.d.ts.map