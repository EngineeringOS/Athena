import {
    AthenaGLSPDiagram,
    AthenaGLSPPoint,
    AthenaGLSPPresentationAnchorBindingSource,
    AthenaGLSPPresentationBoundsSource,
    AthenaGLSPPresentationCompositeDefinitionSource,
    AthenaGLSPPresentationGraphicOccurrenceSource,
    AthenaGLSPPresentationOccurrenceSource,
    AthenaGLSPPresentationPrimitiveDefinitionSource,
    AthenaGLSPPresentationReferenceMarkerSource,
    AthenaGLSPPresentationRepresentationFactSource,
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

export type AthenaGraphResolvedPresentationGraphicOccurrence = AthenaGLSPPresentationGraphicOccurrenceSource & {
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

export type AthenaGraphResolvedPresentationRepresentation = {
    subjectId: string;
    occurrenceId: string;
    representationId: string;
    context: string;
    symbolFamilyId: string;
    fallback: false;
    sourceProjectionIds: string[];
    packageEvidence?: AthenaGLSPPresentationRepresentationFactSource['packageEvidence'];
    parts: AthenaGraphResolvedPresentationPart[];
    terminals: AthenaGLSPPresentationRepresentationFactSource['terminals'];
    labels: AthenaGLSPPresentationRepresentationFactSource['labels'];
};

export type AthenaGraphResolvedPresentationReferenceMarker = AthenaGLSPPresentationReferenceMarkerSource;

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

export function resolvePresentationGraphicOccurrences(
    diagram: AthenaGLSPDiagram,
): AthenaGraphResolvedPresentationGraphicOccurrence[] {
    return (diagram.presentation?.graphicOccurrences ?? []).map(occurrence => ({
        ...occurrence,
        bounds: { ...occurrence.bounds },
        graphic: {
            ...occurrence.graphic,
            ...(occurrence.graphic.bounds ? { bounds: { ...occurrence.graphic.bounds } } : {}),
            primitives: (occurrence.graphic.primitives ?? []).map(primitive => ({
                ...primitive,
                bounds: { ...primitive.bounds },
                ...(primitive.start ? { start: { ...primitive.start } } : {}),
                ...(primitive.end ? { end: { ...primitive.end } } : {}),
                points: (primitive.points ?? []).map(point => ({ ...point })),
                ...(primitive.center ? { center: { ...primitive.center } } : {}),
                ...(primitive.origin ? { origin: { ...primitive.origin } } : {}),
            })),
            provenanceSources: [...(occurrence.graphic.provenanceSources ?? [])],
            forbiddenAuthorityClaims: [...(occurrence.graphic.forbiddenAuthorityClaims ?? [])],
        },
        terminalBindings: (occurrence.terminalBindings ?? []).map(binding => ({
            ...binding,
            point: { ...binding.point },
        })),
        labels: (occurrence.labels ?? []).map(label => ({
            ...label,
            bounds: { ...label.bounds },
        })),
        sourceProvenance: [...(occurrence.sourceProvenance ?? [])],
        authorities: { ...occurrence.authorities },
        parts: [resolveGraphicOccurrencePart(occurrence)],
    }));
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

function resolveGraphicOccurrencePart(
    occurrence: AthenaGLSPPresentationGraphicOccurrenceSource,
): AthenaGraphResolvedPresentationPart {
    return {
        partId: occurrence.graphic.documentId ?? occurrence.definitionId,
        primitiveId: occurrence.definitionId,
        bounds: { ...(occurrence.graphic.bounds ?? occurrence.bounds) },
        commands: (occurrence.graphic.primitives ?? [])
            .map(graphicPrimitiveToCommand)
            .filter(isPresentationShapeCommand),
        textSlots: [],
        tokenDefaults: {
            stroke: '#202020',
            strokeWidth: '1.4',
            label: '#202020',
        },
        tokenOverrides: {},
    };
}

function graphicPrimitiveToCommand(
    primitive: AthenaGLSPPresentationGraphicOccurrenceSource['graphic']['primitives'][number],
): AthenaGLSPPresentationShapeCommandSource | undefined {
    switch (primitive.kind) {
        case 'line':
            return primitive.start && primitive.end
                ? {
                    kind: 'stroke_line',
                    start: { ...primitive.start },
                    end: { ...primitive.end },
                    strokeTokenKey: 'stroke',
                    strokeWidthTokenKey: 'strokeWidth',
                }
                : undefined;
        case 'rectangle':
            return {
                kind: 'stroke_rectangle',
                bounds: { ...primitive.bounds },
                radius: primitive.cornerRadius,
                strokeTokenKey: 'stroke',
                strokeWidthTokenKey: 'strokeWidth',
            };
        case 'circle':
        case 'connection-dot':
            return primitive.center && primitive.radius
                ? {
                    kind: 'circle',
                    center: { ...primitive.center },
                    radius: primitive.radius,
                    strokeTokenKey: 'stroke',
                    strokeWidthTokenKey: 'strokeWidth',
                }
                : undefined;
        case 'polyline':
            return {
                kind: 'svg_path',
                pathData: (primitive.points ?? [])
                    .map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`)
                    .join(' '),
                strokeTokenKey: 'stroke',
                strokeWidthTokenKey: 'strokeWidth',
            };
        case 'arc':
            return primitive.center && primitive.radius !== undefined &&
                primitive.startAngleDegrees !== undefined && primitive.sweepAngleDegrees !== undefined
                ? {
                    kind: 'svg_path',
                    pathData: arcPrimitivePathData(
                        primitive.center,
                        primitive.radius,
                        primitive.startAngleDegrees,
                        primitive.sweepAngleDegrees,
                    ),
                    strokeTokenKey: 'stroke',
                    strokeWidthTokenKey: 'strokeWidth',
                }
                : undefined;
        case 'text':
            return primitive.origin && primitive.text
                ? {
                    kind: 'text',
                    origin: { ...primitive.origin },
                    text: primitive.text,
                    fillTokenKey: 'label',
                }
                : undefined;
        case 'marker':
            return primitive.origin
                ? {
                    kind: 'circle',
                    center: { ...primitive.origin },
                    radius: Math.max(2, primitive.bounds.width / 2, primitive.bounds.height / 2),
                    strokeTokenKey: 'stroke',
                    strokeWidthTokenKey: 'strokeWidth',
                }
                : undefined;
        case 'reference-arrow':
            return primitive.start && primitive.end
                ? {
                    kind: 'stroke_line',
                    start: { ...primitive.start },
                    end: { ...primitive.end },
                    strokeTokenKey: 'stroke',
                    strokeWidthTokenKey: 'strokeWidth',
                }
                : undefined;
        default:
            return undefined;
    }
}

function arcPrimitivePathData(
    center: AthenaGLSPPoint,
    radius: number,
    startAngleDegrees: number,
    sweepAngleDegrees: number,
): string {
    const startAngle = (startAngleDegrees * Math.PI) / 180;
    const endAngle = ((startAngleDegrees + sweepAngleDegrees) * Math.PI) / 180;
    const start = {
        x: center.x + radius * Math.cos(startAngle),
        y: center.y + radius * Math.sin(startAngle),
    };
    const end = {
        x: center.x + radius * Math.cos(endAngle),
        y: center.y + radius * Math.sin(endAngle),
    };
    const largeArc = Math.abs(sweepAngleDegrees) > 180 ? 1 : 0;
    const sweep = sweepAngleDegrees >= 0 ? 1 : 0;
    return `M ${roundPathNumber(start.x)} ${roundPathNumber(start.y)} A ${radius} ${radius} 0 ${largeArc} ${sweep} ${roundPathNumber(end.x)} ${roundPathNumber(end.y)}`;
}

function roundPathNumber(value: number): number {
    return Math.round(value * 1000) / 1000;
}

export function resolvePresentationRepresentations(
    diagram: AthenaGLSPDiagram,
): AthenaGraphResolvedPresentationRepresentation[] {
    return (diagram.presentation?.representationFacts ?? []).map(fact => ({
        subjectId: fact.subjectId,
        occurrenceId: fact.occurrenceId,
        representationId: fact.anatomy.representationId,
        context: fact.anatomy.context,
        symbolFamilyId: fact.symbol.familyId,
        fallback: false,
        sourceProjectionIds: [...(fact.sourceProjectionIds ?? [])],
        packageEvidence: fact.packageEvidence ? {
            ...fact.packageEvidence,
            anchorMapSummary: [...(fact.packageEvidence.anchorMapSummary ?? [])],
            labelBindingSummary: [...(fact.packageEvidence.labelBindingSummary ?? [])],
        } : undefined,
        parts: [
            resolveRepresentationPart(fact),
        ],
        terminals: [...(fact.terminals ?? [])],
        labels: [...(fact.labels ?? [])],
    }));
}

export function resolvePresentationReferenceMarkers(
    diagram: AthenaGLSPDiagram,
): AthenaGraphResolvedPresentationReferenceMarker[] {
    return (diagram.presentation?.referenceMarkers ?? []).map(marker => ({
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
        sourceProjectionIds: [...(marker.sourceProjectionIds ?? [])],
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

function resolveRepresentationPart(
    fact: AthenaGLSPPresentationRepresentationFactSource,
): AthenaGraphResolvedPresentationPart {
    const bounds = {
        x: 0,
        y: 0,
        width: fact.anatomy.bounds.width,
        height: fact.anatomy.bounds.height,
    };
    return {
        partId: fact.anatomy.representationId,
        primitiveId: fact.symbol.familyId,
        bounds,
        commands: (fact.anatomy.primitives ?? [])
            .map(primitive => representationPrimitiveToCommand(primitive, fact))
            .filter(isPresentationShapeCommand),
        textSlots: [],
        tokenDefaults: {
            stroke: '#202020',
            strokeWidth: '1.6',
            label: '#202020',
        },
        tokenOverrides: {},
    };
}

function representationPrimitiveToCommand(
    primitive: AthenaGLSPPresentationRepresentationFactSource['anatomy']['primitives'][number],
    fact: AthenaGLSPPresentationRepresentationFactSource,
): AthenaGLSPPresentationShapeCommandSource | undefined {
    switch (primitive.kind) {
        case 'rectangle':
            return {
                kind: 'stroke_rectangle',
                bounds: {
                    x: primitive.origin.x,
                    y: primitive.origin.y,
                    width: primitive.size.width,
                    height: primitive.size.height,
                },
                strokeTokenKey: 'stroke',
                strokeWidthTokenKey: 'strokeWidth',
            };
        case 'line':
            return {
                kind: 'stroke_line',
                start: { ...primitive.start },
                end: { ...primitive.end },
                strokeTokenKey: 'stroke',
                strokeWidthTokenKey: 'strokeWidth',
            };
        case 'circle':
            return {
                kind: 'circle',
                center: { ...primitive.center },
                radius: primitive.radius,
                strokeTokenKey: 'stroke',
                strokeWidthTokenKey: 'strokeWidth',
            };
        case 'polyline':
            return {
                kind: 'svg_path',
                pathData: primitive.points
                    .map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`)
                    .join(' '),
                strokeTokenKey: 'stroke',
                strokeWidthTokenKey: 'strokeWidth',
            };
        case 'text':
            return {
                kind: 'text',
                origin: { ...primitive.origin },
                text: primitive.text,
                fillTokenKey: 'label',
            };
        default:
            return undefined;
    }
}

function isPresentationShapeCommand(
    command: AthenaGLSPPresentationShapeCommandSource | undefined,
): command is AthenaGLSPPresentationShapeCommandSource {
    return !!command;
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
        origin: command.origin
            ? {
                x: scaleX(command.origin.x, primitive.viewBoxWidth, targetBounds),
                y: scaleY(command.origin.y, primitive.viewBoxHeight, targetBounds),
            }
            : undefined,
        radius: command.radius
            ? Math.max(1, Math.round(command.radius * Math.min(
                targetBounds.width / Math.max(primitive.viewBoxWidth, 1),
                targetBounds.height / Math.max(primitive.viewBoxHeight, 1),
            )))
            : undefined,
        text: command.text,
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
