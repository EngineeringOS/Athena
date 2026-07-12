import {
    AthenaGLSPDiagram,
    AthenaGLSPGraph,
    AthenaGLSPProjectionSource,
} from './athena-glsp-diagram-model';

/** Converts Athena-owned projection payloads into a disposable GLSP-shaped graph snapshot. */
export function translateProjectionSessionToGLSPDiagram(
    projection: AthenaGLSPProjectionSource,
): AthenaGLSPDiagram {
    const readyProjection = projection.readyProjection;
    const electricalAnchors = normalizeArray(readyProjection?.electricalAnchors).map(anchor => ({ ...anchor }));
    const electricalConnectionEndpoints = normalizeArray(readyProjection?.electricalConnectionEndpoints).map(endpoint => ({ ...endpoint }));
    const electricalRoutingCorridors = normalizeArray(readyProjection?.electricalRoutingCorridors).map(corridor => ({
        ...corridor,
        preferredBendPoints: normalizeArray(corridor.preferredBendPoints).map(point => ({ ...point })),
    }));
    return {
        kind: 'athena-glsp-diagram',
        projectName: projection.projectName,
        semanticPath: projection.semanticPath,
        activeViewId: projection.activeViewId,
        status: projection.status,
        activeRenderContributions: normalizeArray(readyProjection?.activeRenderContributions).map(contribution => ({
            ...contribution,
            surfaceMappings: normalizeArray(contribution.surfaceMappings).map(mapping => ({
                surface: mapping.surface,
                tokens: { ...mapping.tokens },
            })),
        })) ?? [],
        supportedViews: normalizeArray(projection.supportedViews).map(view => ({
            ...view,
            ownershipContract: normalizeOwnershipContract(view.ownershipContract),
        })),
        governedCommands: normalizeArray(projection.governedCommands).map(command => ({
            ...command,
            requiredArguments: [...normalizeArray(command.requiredArguments)],
        })),
        activeSheetId: readyProjection?.activeSheetId,
        sheets: normalizeArray(readyProjection?.sheets).map(sheet => ({
            ...sheet,
            subjectSemanticIds: [...normalizeArray(sheet.subjectSemanticIds)],
        })),
        notationPack: readyProjection?.notationPack
            ? {
                ...readyProjection.notationPack,
                subjects: normalizeArray(readyProjection.notationPack.subjects).map(subject => ({
                    ...subject,
                    markerKeys: [...normalizeArray(subject.markerKeys)],
                })),
            }
            : undefined,
        crossReferences: normalizeArray(readyProjection?.crossReferences).map(crossReference => ({
            ...crossReference,
            sheetIds: [...normalizeArray(crossReference.sheetIds)],
            occurrenceIds: [...normalizeArray(crossReference.occurrenceIds)],
        })),
        electricalAnchors,
        electricalConnectionEndpoints,
        electricalRoutingCorridors,
        unavailableReason: projection.unavailableReason,
        diagnostics: normalizeArray(projection.diagnostics).map(diagnostic => ({ ...diagnostic })),
        graph: toGraph({
            projection,
            electricalConnectionEndpoints,
            electricalRoutingCorridors,
        }),
    };
}

function toGraph(args: {
    projection: AthenaGLSPProjectionSource;
    electricalConnectionEndpoints: AthenaGLSPDiagram['electricalConnectionEndpoints'];
    electricalRoutingCorridors: AthenaGLSPDiagram['electricalRoutingCorridors'];
}): AthenaGLSPGraph {
    const { projection, electricalConnectionEndpoints, electricalRoutingCorridors } = args;
    const readyProjection = projection.readyProjection;
    if (projection.status !== 'ready' || !readyProjection) {
        return {
            id: `${projection.projectName}:${projection.activeViewId}`,
            type: 'graph',
            canvas: {
                width: 0,
                height: 0,
            },
            nodes: [],
            edges: [],
        };
    }

    const endpointsByConnectionId = new Map<string, {
        sourcePortSemanticId?: string;
        targetPortSemanticId?: string;
    }>();
    for (const endpoint of electricalConnectionEndpoints) {
        const current = endpointsByConnectionId.get(endpoint.projectionConnectionId) ?? {};
        if (endpoint.endpointRole === 'source') {
            current.sourcePortSemanticId = endpoint.portSemanticId;
        } else if (endpoint.endpointRole === 'target') {
            current.targetPortSemanticId = endpoint.portSemanticId;
        }
        endpointsByConnectionId.set(endpoint.projectionConnectionId, current);
    }
    const corridorByConnectionId = new Map(
        electricalRoutingCorridors.map(corridor => [corridor.projectionConnectionId, corridor] as const),
    );

    return {
        id: `${projection.projectName}:${readyProjection.viewId}`,
        type: 'graph',
        canvas: {
            width: readyProjection.canvasWidth,
            height: readyProjection.canvasHeight,
        },
        nodes: [
            ...normalizeArray(readyProjection.components).map(component => ({
                id: component.projectionId,
                semanticId: component.semanticId,
                type: 'node' as const,
                kind: 'component' as const,
                label: component.label,
                position: {
                    x: component.x,
                    y: component.y,
                },
                size: {
                    width: component.width,
                    height: component.height,
                },
            })),
            ...normalizeArray(readyProjection.labels).map(label => ({
                id: label.projectionId,
                semanticId: label.semanticId,
                type: 'node' as const,
                kind: 'label' as const,
                label: label.label,
                position: {
                    x: label.x,
                    y: label.y,
                },
                size: {
                    width: label.width,
                    height: label.height,
                },
            })),
        ],
        edges: normalizeArray(readyProjection.connections).map(connection => {
            const corridor = corridorByConnectionId.get(connection.projectionId);
            const endpoints = endpointsByConnectionId.get(connection.projectionId);
            return {
                id: connection.projectionId,
                semanticId: connection.semanticId,
                type: 'edge' as const,
                sourcePoint: {
                    x: connection.x1,
                    y: connection.y1,
                },
                targetPoint: {
                    x: connection.x2,
                    y: connection.y2,
                },
                routingStyle: corridor?.routingStyle,
                bendPoints: normalizeArray(corridor?.preferredBendPoints).map(point => ({
                    x: point.x,
                    y: point.y,
                })),
                sourceAnchorId: corridor?.sourceAnchorId,
                targetAnchorId: corridor?.targetAnchorId,
                sourcePortSemanticId: endpoints?.sourcePortSemanticId,
                targetPortSemanticId: endpoints?.targetPortSemanticId,
            };
        }),
    };
}

function normalizeArray<T>(value: readonly T[] | T[] | undefined): T[] {
    return Array.isArray(value) ? [...value] : [];
}

function normalizeOwnershipContract(
    ownershipContract: AthenaGLSPProjectionSource['supportedViews'][number]['ownershipContract'] | undefined,
): AthenaGLSPProjectionSource['supportedViews'][number]['ownershipContract'] {
    return {
        interactivity: ownershipContract?.interactivity ?? 'inspect_only',
        displayScopes: [...normalizeArray(ownershipContract?.displayScopes)],
        semanticCommandIds: [...normalizeArray(ownershipContract?.semanticCommandIds)],
        projectionCommandIds: [...normalizeArray(ownershipContract?.projectionCommandIds)],
        transientInteractionKinds: [...normalizeArray(ownershipContract?.transientInteractionKinds)],
        persistedProjectionMetadataKeys: [...normalizeArray(ownershipContract?.persistedProjectionMetadataKeys)],
    };
}
