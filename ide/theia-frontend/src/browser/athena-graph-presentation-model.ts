import {
    AthenaGLSPDiagram,
    AthenaGLSPPoint,
    AthenaGLSPPresentationAnchorBindingSource,
    AthenaGLSPPresentationBoundsSource,
    AthenaGLSPPresentationCompositeDefinitionSource,
    AthenaGLSPPresentationOccurrenceSource,
    AthenaGLSPPresentationPrimitiveDefinitionSource,
    AthenaGLSPPresentationShapeCommandSource
} from '@engineeringood/athena-graph-glsp';

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

export function resolvePresentationOccurrences(
    diagram: AthenaGLSPDiagram,
): AthenaGraphResolvedPresentationOccurrence[] {
    const presentation = diagram.presentation;
    if (!presentation) {
        return [];
    }

    const primitiveById = new Map<string, AthenaGLSPPresentationPrimitiveDefinitionSource>();
    for (const pack of presentation.primitivePacks ?? []) {
        for (const primitive of pack.primitives ?? []) {
            primitiveById.set(primitive.primitiveId, primitive);
        }
    }
    const compositeById = new Map<string, AthenaGLSPPresentationCompositeDefinitionSource>();
    for (const pack of presentation.compositePacks ?? []) {
        for (const composite of pack.composites ?? []) {
            compositeById.set(composite.compositeId, composite);
        }
    }

    return (presentation.occurrences ?? []).map(occurrence => {
        const parts = resolveOccurrenceParts(occurrence, primitiveById, compositeById);
        return {
            occurrenceId: occurrence.occurrenceId,
            semanticId: occurrence.semanticId,
            layer: occurrence.layer,
            bounds: { ...occurrence.bounds },
            displayLabel: occurrence.displayLabel,
            orientation: occurrence.orientation,
            markerKeys: [...(occurrence.markerKeys ?? [])],
            sourceProjectionIds: [...(occurrence.sourceProjectionIds ?? [])],
            anchorBindings: [...(occurrence.anchorBindings ?? [])],
            textSlots: resolveOccurrenceTextSlots(occurrence, primitiveById, compositeById),
            parts,
        };
    });
}

export function resolvePresentationConnectors(
    diagram: AthenaGLSPDiagram,
): AthenaGraphResolvedPresentationConnector[] {
    return (diagram.presentation?.connectors ?? []).map(connector => ({
        occurrenceId: connector.occurrenceId,
        semanticId: connector.semanticId,
        primitiveId: connector.primitiveId,
        layer: connector.layer,
        routePoints: (connector.routePoints ?? []).map(point => ({ ...point })),
        sourceAnchorId: connector.sourceAnchorId,
        targetAnchorId: connector.targetAnchorId,
        sourcePortSemanticId: connector.sourcePortSemanticId,
        targetPortSemanticId: connector.targetPortSemanticId,
        markerKeys: [...(connector.markerKeys ?? [])],
        tokenOverrides: { ...(connector.tokenOverrides ?? {}) },
        sourceProjectionIds: [...(connector.sourceProjectionIds ?? [])],
    }));
}

function resolveOccurrenceParts(
    occurrence: AthenaGLSPPresentationOccurrenceSource,
    primitiveById: Map<string, AthenaGLSPPresentationPrimitiveDefinitionSource>,
    compositeById: Map<string, AthenaGLSPPresentationCompositeDefinitionSource>,
): AthenaGraphResolvedPresentationPart[] {
    if (occurrence.referenceKind === 'primitive' && occurrence.primitiveId) {
        const primitive = primitiveById.get(occurrence.primitiveId);
        return primitive
            ? [
                resolvePrimitivePart({
                    partId: occurrence.primitiveId,
                    primitive,
                    targetBounds: occurrence.bounds,
                    tokenOverrides: occurrence.tokenOverrides ?? {},
                    textValues: occurrence.textValues ?? {},
                }),
            ]
            : [];
    }

    if (occurrence.referenceKind === 'composite' && occurrence.compositeId) {
        const composite = compositeById.get(occurrence.compositeId);
        if (!composite) {
            return [];
        }
        return (composite.parts ?? []).flatMap(part => {
            const primitive = primitiveById.get(part.primitiveId);
            if (!primitive) {
                return [];
            }
            return [
                resolvePrimitivePart({
                    partId: part.partId,
                    primitive,
                    targetBounds: transformCompositeBounds(occurrence.bounds, composite, part.bounds),
                    tokenOverrides: {
                        ...(composite.tokenDefaults ?? {}),
                        ...(part.tokenOverrides ?? {}),
                        ...(occurrence.tokenOverrides ?? {}),
                    },
                    textValues: occurrence.textValues ?? {},
                }),
            ];
        });
    }

    return [];
}

function resolveOccurrenceTextSlots(
    occurrence: AthenaGLSPPresentationOccurrenceSource,
    primitiveById: Map<string, AthenaGLSPPresentationPrimitiveDefinitionSource>,
    compositeById: Map<string, AthenaGLSPPresentationCompositeDefinitionSource>,
): AthenaGraphResolvedPresentationOccurrence['textSlots'] {
    if (occurrence.referenceKind === 'primitive' && occurrence.primitiveId) {
        const primitive = primitiveById.get(occurrence.primitiveId);
        return primitive
            ? (primitive.textSlots ?? []).map(slot => ({
                slotId: slot.slotId,
                text: occurrence.textValues?.[slot.slotId],
                x: scaleX(slot.origin.x, primitive.viewBoxWidth, occurrence.bounds),
                y: scaleY(slot.origin.y, primitive.viewBoxHeight, occurrence.bounds),
                tokenKey: slot.tokenKey,
            }))
            : [];
    }
    if (occurrence.referenceKind === 'composite' && occurrence.compositeId) {
        const composite = compositeById.get(occurrence.compositeId);
        return composite
            ? (composite.textSlots ?? []).map(slot => ({
                slotId: slot.slotId,
                text: occurrence.textValues?.[slot.slotId],
                x: scaleX(slot.origin.x, composite.viewBoxWidth, occurrence.bounds),
                y: scaleY(slot.origin.y, composite.viewBoxHeight, occurrence.bounds),
                tokenKey: slot.tokenKey,
            }))
            : [];
    }
    return [];
}

function resolvePrimitivePart(args: {
    partId: string;
    primitive: AthenaGLSPPresentationPrimitiveDefinitionSource;
    targetBounds: AthenaGLSPPresentationBoundsSource;
    tokenOverrides: Record<string, string>;
    textValues: Record<string, string>;
}): AthenaGraphResolvedPresentationPart {
    const { partId, primitive, targetBounds, tokenOverrides, textValues } = args;
    return {
        partId,
        primitiveId: primitive.primitiveId,
        bounds: { ...targetBounds },
        commands: (primitive.commands ?? []).map(command => scaleShapeCommand(command, primitive, targetBounds)),
        textSlots: (primitive.textSlots ?? []).map(slot => ({
            slotId: slot.slotId,
            text: textValues[slot.slotId],
            x: scaleX(slot.origin.x, primitive.viewBoxWidth, targetBounds),
            y: scaleY(slot.origin.y, primitive.viewBoxHeight, targetBounds),
            tokenKey: slot.tokenKey,
        })),
        tokenDefaults: { ...(primitive.tokenDefaults ?? {}) },
        tokenOverrides: { ...tokenOverrides },
    };
}

function scaleShapeCommand(
    command: AthenaGLSPPresentationShapeCommandSource,
    primitive: AthenaGLSPPresentationPrimitiveDefinitionSource,
    targetBounds: AthenaGLSPPresentationBoundsSource,
): AthenaGLSPPresentationShapeCommandSource {
    return {
        kind: command.kind,
        bounds: command.bounds
            ? {
                x: scaleX(command.bounds.x, primitive.viewBoxWidth, targetBounds),
                y: scaleY(command.bounds.y, primitive.viewBoxHeight, targetBounds),
                width: scaleWidth(command.bounds.width, primitive.viewBoxWidth, targetBounds),
                height: scaleHeight(command.bounds.height, primitive.viewBoxHeight, targetBounds),
            }
            : undefined,
        start: command.start
            ? {
                x: scaleX(command.start.x, primitive.viewBoxWidth, targetBounds),
                y: scaleY(command.start.y, primitive.viewBoxHeight, targetBounds),
            }
            : undefined,
        end: command.end
            ? {
                x: scaleX(command.end.x, primitive.viewBoxWidth, targetBounds),
                y: scaleY(command.end.y, primitive.viewBoxHeight, targetBounds),
            }
            : undefined,
        center: command.center
            ? {
                x: scaleX(command.center.x, primitive.viewBoxWidth, targetBounds),
                y: scaleY(command.center.y, primitive.viewBoxHeight, targetBounds),
            }
            : undefined,
        radius: command.radius
            ? Math.max(1, Math.round(command.radius * Math.min(
                targetBounds.width / Math.max(primitive.viewBoxWidth, 1),
                targetBounds.height / Math.max(primitive.viewBoxHeight, 1),
            )))
            : undefined,
        strokeTokenKey: command.strokeTokenKey,
        strokeWidthTokenKey: command.strokeWidthTokenKey,
        fillTokenKey: command.fillTokenKey,
    };
}

function transformCompositeBounds(
    occurrenceBounds: AthenaGLSPPresentationBoundsSource,
    composite: AthenaGLSPPresentationCompositeDefinitionSource,
    partBounds: AthenaGLSPPresentationBoundsSource,
): AthenaGLSPPresentationBoundsSource {
    return {
        x: scaleX(partBounds.x, composite.viewBoxWidth, occurrenceBounds),
        y: scaleY(partBounds.y, composite.viewBoxHeight, occurrenceBounds),
        width: scaleWidth(partBounds.width, composite.viewBoxWidth, occurrenceBounds),
        height: scaleHeight(partBounds.height, composite.viewBoxHeight, occurrenceBounds),
    };
}

function scaleX(
    value: number,
    sourceWidth: number,
    targetBounds: AthenaGLSPPresentationBoundsSource,
): number {
    return targetBounds.x + Math.round((value / Math.max(sourceWidth, 1)) * targetBounds.width);
}

function scaleY(
    value: number,
    sourceHeight: number,
    targetBounds: AthenaGLSPPresentationBoundsSource,
): number {
    return targetBounds.y + Math.round((value / Math.max(sourceHeight, 1)) * targetBounds.height);
}

function scaleWidth(
    value: number,
    sourceWidth: number,
    targetBounds: AthenaGLSPPresentationBoundsSource,
): number {
    return Math.max(1, Math.round((value / Math.max(sourceWidth, 1)) * targetBounds.width));
}

function scaleHeight(
    value: number,
    sourceHeight: number,
    targetBounds: AthenaGLSPPresentationBoundsSource,
): number {
    return Math.max(1, Math.round((value / Math.max(sourceHeight, 1)) * targetBounds.height));
}
