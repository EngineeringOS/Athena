import {
    AthenaGLSPDiagram,
    AthenaGLSPGraph,
    AthenaGLSPPresentationBoundsSource,
    AthenaGLSPPresentationGraphicOccurrenceSource,
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

function requiredGraphicOccurrenceBounds(
    occurrence: AthenaGLSPPresentationGraphicOccurrenceSource,
): AthenaGLSPPresentationBoundsSource {
    const bounds = occurrence.bounds ?? occurrence.graphic.bounds;
    if (!bounds) {
        throw new Error(`Graphic occurrence ${occurrence.occurrenceId} requires compiler-owned placement bounds.`);
    }
    return bounds;
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
        ...(document.drawingComposition ? { drawingComposition: normalizeDrawingComposition(document.drawingComposition) } : {}),
        ...(document.paintPlan ? {
            paintPlan: {
                items: normalizeArray(document.paintPlan.items).map(item => ({ ...item })),
            },
        } : {}),
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
        graphicOccurrences: normalizeArray(document.graphicOccurrences).map(occurrence => ({
            occurrenceId: occurrence.occurrenceId,
            semanticSubjectId: occurrence.semanticSubjectId,
            physicalComponentId: occurrence.physicalComponentId,
            functionId: occurrence.functionId,
            bounds: { ...requiredGraphicOccurrenceBounds(occurrence) },
            orientation: occurrence.orientation,
            deviceLabel: occurrence.deviceLabel,
            modelLabel: occurrence.modelLabel,
            packageId: occurrence.packageId,
            definitionId: occurrence.definitionId,
            bindingRuleId: occurrence.bindingRuleId,
            graphic: {
                documentId: occurrence.graphic.documentId,
                ...(occurrence.graphic.bounds ? { bounds: { ...occurrence.graphic.bounds } } : {}),
                primitives: normalizeArray(occurrence.graphic.primitives).map(primitive => ({
                    primitiveId: primitive.primitiveId,
                    kind: primitive.kind,
                    ...(primitive.bounds ? { bounds: { ...primitive.bounds } } : {}),
                    styleTokenId: primitive.styleTokenId,
                    ...(primitive.start ? { start: { ...primitive.start } } : {}),
                    ...(primitive.end ? { end: { ...primitive.end } } : {}),
                    points: normalizeArray(primitive.points).map(point => ({ ...point })),
                    ...(primitive.center ? { center: { ...primitive.center } } : {}),
                    ...(primitive.origin ? { origin: { ...primitive.origin } } : {}),
                    radius: primitive.radius,
                    startAngleDegrees: primitive.startAngleDegrees,
                    sweepAngleDegrees: primitive.sweepAngleDegrees,
                    text: primitive.text,
                    cornerRadius: primitive.cornerRadius,
                    markerKind: primitive.markerKind,
                    headSize: primitive.headSize,
                })),
                provenanceSources: [...normalizeArray(occurrence.graphic.provenanceSources)],
                forbiddenAuthorityClaims: [...normalizeArray(occurrence.graphic.forbiddenAuthorityClaims)],
            },
            terminalBindings: normalizeArray(occurrence.terminalBindings).map(binding => ({
                portSemanticId: binding.portSemanticId,
                anchorId: binding.anchorId,
                terminalIdentity: binding.terminalIdentity,
                point: { ...binding.point },
                labelPoint: { ...binding.labelPoint },
                side: binding.side,
            })),
            labels: normalizeArray(occurrence.labels).map(label => ({
                labelId: label.labelId,
                role: label.role,
                value: label.value,
                bounds: { ...label.bounds },
            })),
            sourceProvenance: [...normalizeArray(occurrence.sourceProvenance)],
            authorities: { ...occurrence.authorities },
        })),
        connectors: normalizeArray(document.connectors).map(connector => ({
            occurrenceId: connector.occurrenceId,
            semanticId: connector.semanticId,
            primitiveId: connector.primitiveId,
            routePoints: normalizeArray(connector.routePoints).map(point => ({ ...point })),
            lineClassId: connector.lineClassId,
            line: { ...connector.line },
            routeId: connector.routeId,
            bundleId: connector.bundleId,
            laneId: connector.laneId,
            laneRouteIds: [...normalizeArray(connector.laneRouteIds)],
            selectedChannelIds: [...normalizeArray(connector.selectedChannelIds)],
            labels: normalizeArray(connector.labels).map(label => ({
                ...label,
                point: { ...label.point },
                bounds: { ...label.bounds },
                sourceProvenance: [...normalizeArray(label.sourceProvenance)],
            })),
            quality: connector.quality,
            sourceEndpoint: {
                ...connector.sourceEndpoint,
                point: { ...connector.sourceEndpoint.point },
                sourceProvenance: [...normalizeArray(connector.sourceEndpoint.sourceProvenance)],
            },
            targetEndpoint: {
                ...connector.targetEndpoint,
                point: { ...connector.targetEndpoint.point },
                sourceProvenance: [...normalizeArray(connector.targetEndpoint.sourceProvenance)],
            },
            layer: connector.layer,
            markerIds: [...normalizeArray(connector.markerIds)],
            tokenOverrides: { ...connector.tokenOverrides },
            sourceProjectionIds: [...normalizeArray(connector.sourceProjectionIds)],
            trace: connector.trace ? { ...connector.trace } : undefined,
            sourceSpan: connector.sourceSpan ? { ...connector.sourceSpan } : undefined,
        })),
        connectionMarkers: normalizeArray(document.connectionMarkers).map(marker => ({
            ...marker,
            point: { ...marker.point },
            routeIds: [...normalizeArray(marker.routeIds)],
            connectorIds: [...normalizeArray(marker.connectorIds)],
            sourceProjectionIds: [...normalizeArray(marker.sourceProjectionIds)],
            sourceProvenance: [...normalizeArray(marker.sourceProvenance)],
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
            ...(fact.packageTrace ? {
                packageTrace: {
                    engineeringPackageId: fact.packageTrace.engineeringPackageId,
                    engineeringPackageVersion: fact.packageTrace.engineeringPackageVersion,
                    presentationProfileId: fact.packageTrace.presentationProfileId,
                    bindingManifestId: fact.packageTrace.bindingManifestId,
                    representationPackageId: fact.packageTrace.representationPackageId,
                    representationPackageVersion: fact.packageTrace.representationPackageVersion,
                    descriptorId: fact.packageTrace.descriptorId,
                    graphicResourceId: fact.packageTrace.graphicResourceId,
                    variant: fact.packageTrace.variant,
                    anchorMapSummary: [...normalizeArray(fact.packageTrace.anchorMapSummary)],
                    labelBindingSummary: [...normalizeArray(fact.packageTrace.labelBindingSummary)],
                    resolverStage: fact.packageTrace.resolverStage,
                    rendererFallbackAccepted: fact.packageTrace.rendererFallbackAccepted === true,
                },
            } : {}),
        })),
    };
}

function normalizeDrawingComposition(
    composition: NonNullable<NonNullable<AthenaGLSPReadyProjectionSource['presentation']>['drawingComposition']>,
): NonNullable<NonNullable<AthenaGLSPDiagram['presentation']>['drawingComposition']> {
    return {
        sheetId: composition.sheetId,
        policyId: composition.policyId,
        contentBounds: { ...composition.contentBounds },
        frameBounds: { ...composition.frameBounds },
        drawingAreaBounds: { ...composition.drawingAreaBounds },
        titleBlockBounds: { ...composition.titleBlockBounds },
        sheetBounds: { ...composition.sheetBounds },
        frameId: composition.frameId,
        frameStyle: composition.frameStyle,
        title: { ...composition.title },
        coordinateZones: normalizeArray(composition.coordinateZones).map(zone => ({
            ...zone,
            bounds: { ...zone.bounds },
        })),
        structureSubjects: normalizeArray(composition.structureSubjects).map(subject => ({
            ...subject,
            bounds: { ...subject.bounds },
        })),
        structureFacts: normalizeArray(composition.structureFacts).map(fact => ({
            ...fact,
            ...(fact.bounds ? { bounds: { ...fact.bounds } } : {}),
            ...(fact.start ? { start: { ...fact.start } } : {}),
            ...(fact.end ? { end: { ...fact.end } } : {}),
            memberIds: [...normalizeArray(fact.memberIds)],
        })),
        referencePlacements: normalizeArray(composition.referencePlacements).map(placement => ({
            ...placement,
            ...(placement.bounds ? { bounds: { ...placement.bounds } } : {}),
            ...(placement.anchor ? { anchor: { ...placement.anchor } } : {}),
            ...(placement.anatomy ? { anatomy: normalizePresentationAnatomy(placement.anatomy) } : {}),
        })),
        authorities: { ...composition.authorities },
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
        case 'text':
            return {
                kind: primitive.kind,
                primitiveId: primitive.primitiveId,
                origin: { ...primitive.origin },
                text: primitive.text,
            };
    }
}

function normalizePresentationAnatomy(
    anatomy: NonNullable<NonNullable<AthenaGLSPReadyProjectionSource['presentation']>['representationFacts']>[number]['anatomy'],
): NonNullable<NonNullable<AthenaGLSPDiagram['presentation']>['representationFacts']>[number]['anatomy'] {
    return {
        ...anatomy,
        bounds: { ...anatomy.bounds },
        hotspot: { ...anatomy.hotspot },
        primitives: normalizeArray(anatomy.primitives).map(normalizeRepresentationPrimitive),
        terminals: normalizeArray(anatomy.terminals).map(terminal => ({
            ...terminal,
            localPoint: { ...terminal.localPoint },
            notation: { ...terminal.notation },
        })),
        labelAnchors: normalizeArray(anatomy.labelAnchors).map(anchor => ({
            ...anchor,
            point: { ...anchor.point },
        })),
    };
}
