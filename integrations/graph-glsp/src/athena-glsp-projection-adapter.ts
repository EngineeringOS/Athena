import {
    AthenaGLSPDiagram,
    AthenaGLSPGraph,
    AthenaGLSPProjectionSource,
    AthenaGLSPReadyProjectionSource,
    AthenaGLSPSheetPolicyEvidenceSource,
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
        presentation: readyProjection?.presentation
            ? normalizePresentationDocument(readyProjection.presentation)
            : undefined,
        sheets: normalizeArray(readyProjection?.sheets).map(sheet => {
            const { policyEvidence: rawPolicyEvidence, ...sheetWithoutPolicyEvidence } = sheet;
            const policyEvidence = normalizeSheetPolicyEvidence(rawPolicyEvidence);
            return {
                ...sheetWithoutPolicyEvidence,
                role: sheet.role ?? policyEvidence?.sheetViewRole,
                subjectSemanticIds: [...normalizeArray(sheet.subjectSemanticIds)],
                ...(policyEvidence ? { policyEvidence } : {}),
                ...(sheet.publication ? { publication: normalizeSheetPublication(sheet.publication) } : {}),
            };
        }),
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
            links: normalizeArray(crossReference.links).map(link => ({ ...link })),
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

function normalizeSheetPolicyEvidence(
    policyEvidence: AthenaGLSPSheetPolicyEvidenceSource | undefined,
): AthenaGLSPSheetPolicyEvidenceSource | undefined {
    if (!policyEvidence) {
        return undefined;
    }
    if (
        typeof policyEvidence.policyId !== 'string' ||
        typeof policyEvidence.policyVersion !== 'string' ||
        typeof policyEvidence.policyDeterministicIdentity !== 'string' ||
        typeof policyEvidence.sheetViewRole !== 'string' ||
        typeof policyEvidence.sheetViewRoleOrder !== 'number'
    ) {
        return undefined;
    }
    return { ...policyEvidence };
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

function normalizePresentationDocument(
    document: AthenaGLSPReadyProjectionSource['presentation'],
): AthenaGLSPDiagram['presentation'] {
    if (!document) {
        return undefined;
    }
    return {
        canvasWidth: document.canvasWidth,
        canvasHeight: document.canvasHeight,
        ...(document.sheetSurface ? { sheetSurface: normalizeSheetSurface(document.sheetSurface) } : {}),
        primitivePacks: normalizeArray(document.primitivePacks).map(pack => ({
            packId: pack.packId,
            displayName: pack.displayName,
            familyIds: [...normalizeArray(pack.familyIds)],
            primitives: normalizeArray(pack.primitives).map(primitive => ({
                primitiveId: primitive.primitiveId,
                displayName: primitive.displayName,
                viewBoxWidth: primitive.viewBoxWidth,
                viewBoxHeight: primitive.viewBoxHeight,
                commands: normalizeArray(primitive.commands).map(command => ({
                    kind: command.kind,
                    bounds: command.bounds ? { ...command.bounds } : undefined,
                    start: command.start ? { ...command.start } : undefined,
                    end: command.end ? { ...command.end } : undefined,
                    center: command.center ? { ...command.center } : undefined,
                    radius: command.radius,
                    pathData: command.pathData,
                    strokeTokenKey: command.strokeTokenKey,
                    strokeWidthTokenKey: command.strokeWidthTokenKey,
                    fillTokenKey: command.fillTokenKey,
                })),
                textSlots: normalizeArray(primitive.textSlots).map(slot => ({
                    slotId: slot.slotId,
                    origin: { ...slot.origin },
                    tokenKey: slot.tokenKey,
                })),
                anchors: normalizeArray(primitive.anchors).map(anchor => ({
                    alias: anchor.alias,
                    point: { ...anchor.point },
                })),
                tokenDefaults: { ...primitive.tokenDefaults },
                supportedOrientations: [...normalizeArray(primitive.supportedOrientations)],
            })),
        })),
        compositePacks: normalizeArray(document.compositePacks).map(pack => ({
            packId: pack.packId,
            displayName: pack.displayName,
            familyIds: [...normalizeArray(pack.familyIds)],
            composites: normalizeArray(pack.composites).map(composite => ({
                compositeId: composite.compositeId,
                displayName: composite.displayName,
                viewBoxWidth: composite.viewBoxWidth,
                viewBoxHeight: composite.viewBoxHeight,
                parts: normalizeArray(composite.parts).map(part => ({
                    partId: part.partId,
                    primitiveId: part.primitiveId,
                    bounds: { ...part.bounds },
                    tokenOverrides: { ...part.tokenOverrides },
                    orientation: part.orientation,
                })),
                textSlots: normalizeArray(composite.textSlots).map(slot => ({
                    slotId: slot.slotId,
                    origin: { ...slot.origin },
                    tokenKey: slot.tokenKey,
                })),
                tokenDefaults: { ...composite.tokenDefaults },
                supportedOrientations: [...normalizeArray(composite.supportedOrientations)],
            })),
        })),
        occurrences: normalizeArray(document.occurrences).map(occurrence => ({
            occurrenceId: occurrence.occurrenceId,
            semanticId: occurrence.semanticId,
            referenceKind: occurrence.referenceKind,
            primitiveId: occurrence.primitiveId,
            compositeId: occurrence.compositeId,
            bounds: { ...occurrence.bounds },
            layer: occurrence.layer,
            displayLabel: occurrence.displayLabel,
            orientation: occurrence.orientation,
            markerKeys: [...normalizeArray(occurrence.markerKeys)],
            textValues: { ...occurrence.textValues },
            anchorBindings: normalizeArray(occurrence.anchorBindings).map(binding => ({
                alias: binding.alias,
                anchorId: binding.anchorId,
                portSemanticId: binding.portSemanticId,
                ownerSemanticId: binding.ownerSemanticId,
                sourceLabelId: binding.sourceLabelId,
            })),
            tokenOverrides: { ...occurrence.tokenOverrides },
            sourceProjectionIds: [...normalizeArray(occurrence.sourceProjectionIds)],
        })),
        connectors: normalizeArray(document.connectors).map(connector => ({
            occurrenceId: connector.occurrenceId,
            semanticId: connector.semanticId,
            primitiveId: connector.primitiveId,
            routePoints: normalizeArray(connector.routePoints).map(point => ({ ...point })),
            layer: connector.layer,
            sourceAnchorId: connector.sourceAnchorId,
            targetAnchorId: connector.targetAnchorId,
            sourcePortSemanticId: connector.sourcePortSemanticId,
            targetPortSemanticId: connector.targetPortSemanticId,
            markerKeys: [...normalizeArray(connector.markerKeys)],
            tokenOverrides: { ...connector.tokenOverrides },
            sourceProjectionIds: [...normalizeArray(connector.sourceProjectionIds)],
        })),
        representationFacts: normalizeArray(document.representationFacts).map(fact => ({
            subjectId: fact.subjectId,
            occurrenceId: fact.occurrenceId,
            sourceProjectionIds: [...normalizeArray(fact.sourceProjectionIds)],
            symbol: {
                familyId: fact.symbol.familyId,
            },
            anatomy: {
                representationId: fact.anatomy.representationId,
                context: fact.anatomy.context,
                bounds: { ...fact.anatomy.bounds },
                hotspot: { ...fact.anatomy.hotspot },
                primitives: normalizeArray(fact.anatomy.primitives).map(primitive => normalizeRepresentationPrimitive(primitive)),
                terminals: normalizeArray(fact.anatomy.terminals).map(terminal => ({
                    terminalId: terminal.terminalId,
                    role: terminal.role,
                    localPoint: { ...terminal.localPoint },
                    side: terminal.side,
                    notation: { ...terminal.notation },
                })),
                labelAnchors: normalizeArray(fact.anatomy.labelAnchors).map(anchor => ({
                    anchorId: anchor.anchorId,
                    role: anchor.role,
                    point: { ...anchor.point },
                })),
            },
            terminals: normalizeArray(fact.terminals).map(terminal => ({
                presentationTerminalId: terminal.presentationTerminalId,
                subjectId: terminal.subjectId,
                occurrenceId: terminal.occurrenceId,
                portId: terminal.portId,
                physicalTerminalId: terminal.physicalTerminalId,
                side: terminal.side,
                routeAnchor: {
                    anchorId: terminal.routeAnchor.anchorId,
                    point: { ...terminal.routeAnchor.point },
                },
                notation: { ...terminal.notation },
            })),
            labels: normalizeArray(fact.labels).map(label => ({
                labelId: label.labelId,
                subjectId: label.subjectId,
                occurrenceId: label.occurrenceId,
                role: label.role,
                value: label.value,
                anchor: {
                    anchorId: label.anchor.anchorId,
                    role: label.anchor.role,
                    point: { ...label.anchor.point },
                },
            })),
            ...(fact.packageEvidence ? {
                packageEvidence: {
                    engineeringPackageId: fact.packageEvidence.engineeringPackageId,
                    engineeringPackageVersion: fact.packageEvidence.engineeringPackageVersion,
                    presentationProfileId: fact.packageEvidence.presentationProfileId,
                    bindingManifestId: fact.packageEvidence.bindingManifestId,
                    representationPackageId: fact.packageEvidence.representationPackageId,
                    representationPackageVersion: fact.packageEvidence.representationPackageVersion,
                    descriptorId: fact.packageEvidence.descriptorId,
                    graphicResourceId: fact.packageEvidence.graphicResourceId,
                    variant: fact.packageEvidence.variant,
                    anchorMapSummary: [...normalizeArray(fact.packageEvidence.anchorMapSummary)],
                    labelBindingSummary: [...normalizeArray(fact.packageEvidence.labelBindingSummary)],
                    resolverStage: fact.packageEvidence.resolverStage,
                    rendererFallbackAccepted: fact.packageEvidence.rendererFallbackAccepted === true,
                },
            } : {}),
        })),
    };
}

function normalizeSheetPublication(
    publication: NonNullable<NonNullable<AthenaGLSPReadyProjectionSource['sheets']>[number]['publication']>,
): NonNullable<AthenaGLSPDiagram['sheets'][number]['publication']> {
    return {
        pageSize: { ...publication.pageSize },
        frame: { ...publication.frame },
        coordinateZones: normalizeArray(publication.coordinateZones).map(zone => ({ ...zone })),
        titleBlock: { ...publication.titleBlock },
        revisionMetadata: { ...publication.revisionMetadata },
        viewComposition: {
            ...publication.viewComposition,
            subjectSemanticIds: [...normalizeArray(publication.viewComposition.subjectSemanticIds)],
        },
    };
}

function normalizeSheetSurface(
    sheetSurface: NonNullable<NonNullable<AthenaGLSPReadyProjectionSource['presentation']>['sheetSurface']>,
): NonNullable<NonNullable<AthenaGLSPDiagram['presentation']>['sheetSurface']> {
    return {
        surfaceId: sheetSurface.surfaceId,
        source: sheetSurface.source,
        frame: {
            width: sheetSurface.frame.width,
            height: sheetSurface.frame.height,
            ...(sheetSurface.frame.margins ? { margins: { ...sheetSurface.frame.margins } } : {}),
            ...(sheetSurface.frame.zoneColumns ? { zoneColumns: [...normalizeArray(sheetSurface.frame.zoneColumns)] } : {}),
            ...(sheetSurface.frame.zoneRows ? { zoneRows: [...normalizeArray(sheetSurface.frame.zoneRows)] } : {}),
        },
        grid: { ...sheetSurface.grid },
        titleBlock: {
            fields: normalizeArray(sheetSurface.titleBlock.fields).map(field => ({ ...field })),
        },
        metadata: { ...sheetSurface.metadata },
    };
}

function normalizeRepresentationPrimitive(
    primitive: NonNullable<NonNullable<AthenaGLSPReadyProjectionSource['presentation']>['representationFacts']>[number]['anatomy']['primitives'][number],
): NonNullable<NonNullable<AthenaGLSPDiagram['presentation']>['representationFacts']>[number]['anatomy']['primitives'][number] {
    switch (primitive.kind) {
        case 'line':
            return {
                kind: primitive.kind,
                primitiveId: primitive.primitiveId,
                start: { ...primitive.start },
                end: { ...primitive.end },
            };
        case 'rectangle':
            return {
                kind: primitive.kind,
                primitiveId: primitive.primitiveId,
                origin: { ...primitive.origin },
                size: { ...primitive.size },
            };
        case 'polyline':
            return {
                kind: primitive.kind,
                primitiveId: primitive.primitiveId,
                points: normalizeArray(primitive.points).map(point => ({ ...point })),
            };
        case 'circle':
            return {
                kind: primitive.kind,
                primitiveId: primitive.primitiveId,
                center: { ...primitive.center },
                radius: primitive.radius,
            };
    }
}
