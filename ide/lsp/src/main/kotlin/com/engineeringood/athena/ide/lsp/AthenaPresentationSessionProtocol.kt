package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.presentation.PresentationAnchorBinding
import com.engineeringood.athena.presentation.PresentationAnchorDefinition
import com.engineeringood.athena.presentation.PresentationCircle
import com.engineeringood.athena.presentation.PresentationCompositeDefinition
import com.engineeringood.athena.presentation.PresentationCompositeOccurrenceReference
import com.engineeringood.athena.presentation.PresentationCompositePack
import com.engineeringood.athena.presentation.PresentationCompositePart
import com.engineeringood.athena.presentation.PresentationConnector
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationDrawingBounds
import com.engineeringood.athena.presentation.PresentationGraphicLabel
import com.engineeringood.athena.presentation.PresentationGraphicOccurrence
import com.engineeringood.athena.presentation.PresentationGraphicOccurrenceAuthorities
import com.engineeringood.athena.presentation.PresentationGraphicTerminalBinding
import com.engineeringood.athena.presentation.PresentationOccurrence
import com.engineeringood.athena.presentation.PresentationPackageEvidence
import com.engineeringood.athena.presentation.PresentationPrimitiveDefinition
import com.engineeringood.athena.presentation.PresentationPrimitiveOccurrenceReference
import com.engineeringood.athena.presentation.PresentationPrimitivePack
import com.engineeringood.athena.presentation.PresentationReferenceMarkerFact
import com.engineeringood.athena.presentation.PresentationRepresentationFact
import com.engineeringood.athena.presentation.PresentationShapeCommand
import com.engineeringood.athena.presentation.PresentationStrokeLine
import com.engineeringood.athena.presentation.PresentationStrokeRectangle
import com.engineeringood.athena.presentation.PresentationSvgPath
import com.engineeringood.athena.presentation.PresentationTextSlot
import com.engineeringood.athena.presentation.connectorsForRendering
import com.engineeringood.athena.presentation.representationFactsForRendering
import com.engineeringood.athena.representation.LabelFact
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.PresentationAnatomy
import com.engineeringood.athena.representation.PresentationLabelAnchor
import com.engineeringood.athena.representation.PresentationPoint
import com.engineeringood.athena.representation.PresentationPrimitive
import com.engineeringood.athena.representation.PresentationRouteAnchor
import com.engineeringood.athena.representation.PresentationSize
import com.engineeringood.athena.representation.PresentationTerminalFact
import com.engineeringood.athena.representation.PresentationTerminalPoint
import com.engineeringood.athena.representation.SymbolAnatomy
import com.engineeringood.athena.representation.TerminalNotation
import com.engineeringood.athena.routing.RouteCrossingFact
import com.engineeringood.athena.routing.RouteFact
import com.engineeringood.athena.routing.RouteFactSnapshot
import com.engineeringood.athena.routing.RouteJunctionFact
import com.engineeringood.athena.routing.RouteQuality
import com.engineeringood.athena.routing.RouteQualityState
import com.engineeringood.athena.routing.RouteLabelFact
import com.engineeringood.athena.routing.TerminalAnchorFact
import kotlin.math.roundToInt

internal fun PresentationDocument.toPayload(): AthenaPresentationDocumentPayload {
    return AthenaPresentationDocumentPayload(
        canvasWidth = canvasWidth,
        canvasHeight = canvasHeight,
        primitivePacks = primitivePacks.map(PresentationPrimitivePack::toPayload),
        compositePacks = compositePacks.map(PresentationCompositePack::toPayload),
        occurrences = occurrences.map(PresentationOccurrence::toPayload),
        graphicOccurrences = graphicOccurrences.map(PresentationGraphicOccurrence::toPayload),
        connectors = connectorsForRendering().map(PresentationConnector::toPayload),
        representationFacts = representationFactsForRendering().map(PresentationRepresentationFact::toPayload),
        referenceMarkers = referenceMarkers.map(PresentationReferenceMarkerFact::toPayload),
        routeFactSnapshot = routeFactSnapshot?.toPayload(),
        drawingComposition = drawingComposition?.toPayload(),
    )
}

private fun PresentationGraphicOccurrence.toPayload(): AthenaPresentationGraphicOccurrencePayload =
    AthenaPresentationGraphicOccurrencePayload(
        occurrenceId = occurrenceId.value,
        semanticSubjectId = semanticSubjectId,
        physicalComponentId = physicalComponentId,
        functionId = functionId,
        bounds = bounds.toPayload(),
        orientation = orientation.name.lowercase(),
        deviceLabel = deviceLabel,
        modelLabel = modelLabel,
        packageId = packageId,
        definitionId = definitionId,
        bindingRuleId = bindingRuleId,
        graphic = graphic.toPayload(),
        terminalBindings = terminalBindings.map(PresentationGraphicTerminalBinding::toPayload),
        labels = labels.map(PresentationGraphicLabel::toPayload),
        sourceProvenance = sourceProvenance.sorted(),
        authorities = authorities.toPayload(),
        trace = AthenaPresentationTracePayload(
            sourceProvenance = sourceProvenance.sorted(),
            sourceProjectionIds = listOf(occurrenceId.value),
            compilerStage = authorities.material,
        ),
    )

private fun PresentationGraphicOccurrenceAuthorities.toPayload() =
    AthenaPresentationGraphicOccurrenceAuthoritiesPayload(
        graphic = graphic,
        placement = placement,
        material = material,
    )

private fun PresentationGraphicTerminalBinding.toPayload() =
    AthenaPresentationGraphicTerminalBindingPayload(
        portSemanticId = portSemanticId,
        anchorId = anchorId,
        terminalIdentity = terminalIdentity,
        point = AthenaProjectionPointPayload(point.x, point.y),
        side = side.name.lowercase(),
        trace = AthenaPresentationTracePayload(
            sourceProvenance = listOf(anchorId, terminalIdentity, side.name.lowercase()),
            sourceProjectionIds = listOf(portSemanticId, anchorId),
            compilerStage = "graphic-terminal-binding",
        ),
    )

private fun PresentationGraphicLabel.toPayload() =
    AthenaPresentationGraphicLabelPayload(
        labelId = labelId,
        role = role,
        value = value,
        bounds = bounds.toPayload(),
    )

private fun GraphicPrimitiveDocument.toPayload() =
    AthenaGraphicPrimitiveDocumentPayload(
        documentId = documentId?.value,
        bounds = bounds?.toPayload(),
        primitives = primitives.flatMap(GraphicPrimitive::flattenForTransport).map(GraphicPrimitive::toPayload),
        provenanceSources = provenanceSources.sorted(),
        forbiddenAuthorityClaims = forbiddenAuthorityClaims.map { claim -> claim.name }.sorted(),
    )

private fun GraphicPrimitive.flattenForTransport(): List<GraphicPrimitive> = when (this) {
    is GraphicPrimitive.Group -> children.flatMap(GraphicPrimitive::flattenForTransport)
    is GraphicPrimitive.Transformed -> child.flattenForTransport()
    else -> listOf(this)
}

private fun GraphicPrimitive.toPayload(): AthenaGraphicPrimitivePayload = when (this) {
    is GraphicPrimitive.Line -> AthenaGraphicPrimitivePayload(
        primitiveId = primitiveId.value,
        kind = kind.wireValue,
        bounds = bounds.toPayload(),
        styleTokenId = styleTokenId.value,
        start = start.toPayload(),
        end = end.toPayload(),
    )
    is GraphicPrimitive.Polyline -> AthenaGraphicPrimitivePayload(
        primitiveId = primitiveId.value,
        kind = kind.wireValue,
        bounds = bounds.toPayload(),
        styleTokenId = styleTokenId.value,
        points = points.map(GraphicPoint::toPayload),
    )
    is GraphicPrimitive.Arc -> AthenaGraphicPrimitivePayload(
        primitiveId = primitiveId.value,
        kind = kind.wireValue,
        bounds = bounds.toPayload(),
        styleTokenId = styleTokenId.value,
        center = center.toPayload(),
        radius = radius.roundToInt(),
        startAngleDegrees = startAngleDegrees,
        sweepAngleDegrees = sweepAngleDegrees,
    )
    is GraphicPrimitive.Circle -> AthenaGraphicPrimitivePayload(
        primitiveId = primitiveId.value,
        kind = kind.wireValue,
        bounds = bounds.toPayload(),
        styleTokenId = styleTokenId.value,
        center = center.toPayload(),
        radius = radius.roundToInt(),
    )
    is GraphicPrimitive.Rectangle -> AthenaGraphicPrimitivePayload(
        primitiveId = primitiveId.value,
        kind = kind.wireValue,
        bounds = bounds.toPayload(),
        styleTokenId = styleTokenId.value,
        cornerRadius = cornerRadius.roundToInt(),
    )
    is GraphicPrimitive.Text -> AthenaGraphicPrimitivePayload(
        primitiveId = primitiveId.value,
        kind = kind.wireValue,
        bounds = bounds.toPayload(),
        styleTokenId = styleTokenId.value,
        origin = origin.toPayload(),
        text = text,
    )
    is GraphicPrimitive.Marker -> AthenaGraphicPrimitivePayload(
        primitiveId = primitiveId.value,
        kind = kind.wireValue,
        bounds = bounds.toPayload(),
        styleTokenId = styleTokenId.value,
        origin = origin.toPayload(),
        markerKind = markerKind.name.lowercase(),
    )
    is GraphicPrimitive.ConnectionDot -> AthenaGraphicPrimitivePayload(
        primitiveId = primitiveId.value,
        kind = kind.wireValue,
        bounds = bounds.toPayload(),
        styleTokenId = styleTokenId.value,
        center = center.toPayload(),
        radius = radius.roundToInt(),
    )
    is GraphicPrimitive.ReferenceArrow -> AthenaGraphicPrimitivePayload(
        primitiveId = primitiveId.value,
        kind = kind.wireValue,
        bounds = bounds.toPayload(),
        styleTokenId = styleTokenId.value,
        start = start.toPayload(),
        end = end.toPayload(),
        headSize = headSize.roundToInt(),
    )
    is GraphicPrimitive.Group,
    is GraphicPrimitive.Transformed,
    -> error("Graphic group and transform primitives must be flattened before LSP transport.")
}

private fun PresentationPrimitivePack.toPayload(): AthenaPresentationPrimitivePackPayload {
    return AthenaPresentationPrimitivePackPayload(
        packId = packId.value,
        displayName = displayName,
        familyIds = familyIds.sorted(),
        primitives = primitives.map(PresentationPrimitiveDefinition::toPayload),
    )
}

private fun PresentationCompositePack.toPayload(): AthenaPresentationCompositePackPayload {
    return AthenaPresentationCompositePackPayload(
        packId = packId.value,
        displayName = displayName,
        familyIds = familyIds.sorted(),
        composites = composites.map(PresentationCompositeDefinition::toPayload),
    )
}

private fun PresentationPrimitiveDefinition.toPayload(): AthenaPresentationPrimitiveDefinitionPayload {
    return AthenaPresentationPrimitiveDefinitionPayload(
        primitiveId = primitiveId.value,
        displayName = displayName,
        viewBoxWidth = viewBoxWidth,
        viewBoxHeight = viewBoxHeight,
        commands = commands.map(PresentationShapeCommand::toPayload),
        textSlots = textSlots.map(PresentationTextSlot::toPayload),
        anchors = anchors.map(PresentationAnchorDefinition::toPayload),
        tokenDefaults = tokenDefaults.toSortedMap(),
        supportedOrientations = supportedOrientations.map { orientation -> orientation.name.lowercase() }.sorted(),
    )
}

private fun PresentationCompositeDefinition.toPayload(): AthenaPresentationCompositeDefinitionPayload {
    return AthenaPresentationCompositeDefinitionPayload(
        compositeId = compositeId.value,
        displayName = displayName,
        viewBoxWidth = viewBoxWidth,
        viewBoxHeight = viewBoxHeight,
        parts = parts.map(PresentationCompositePart::toPayload),
        textSlots = textSlots.map(PresentationTextSlot::toPayload),
        tokenDefaults = tokenDefaults.toSortedMap(),
        supportedOrientations = supportedOrientations.map { orientation -> orientation.name.lowercase() }.sorted(),
    )
}

private fun PresentationCompositePart.toPayload(): AthenaPresentationCompositePartPayload {
    return AthenaPresentationCompositePartPayload(
        partId = partId,
        primitiveId = primitiveId.value,
        bounds = AthenaPresentationBoundsPayload(
            x = bounds.x,
            y = bounds.y,
            width = bounds.width,
            height = bounds.height,
        ),
        tokenOverrides = tokenOverrides.toSortedMap(),
        orientation = orientation.name.lowercase(),
    )
}

private fun PresentationDrawingBounds.toPayload() = AthenaPresentationBoundsPayload(
    x = x,
    y = y,
    width = width,
    height = height,
)

private fun GraphicBounds.toPayload() = AthenaPresentationBoundsPayload(
    x = x.roundToInt(),
    y = y.roundToInt(),
    width = width.roundToInt().coerceAtLeast(1),
    height = height.roundToInt().coerceAtLeast(1),
)

private fun GraphicPoint.toPayload() = AthenaProjectionPointPayload(
    x = x.roundToInt(),
    y = y.roundToInt(),
)

private fun PresentationShapeCommand.toPayload(): AthenaPresentationShapeCommandPayload {
    return when (this) {
        is PresentationStrokeRectangle -> AthenaPresentationShapeCommandPayload(
            kind = "stroke_rectangle",
            bounds = AthenaPresentationBoundsPayload(
                x = bounds.x,
                y = bounds.y,
                width = bounds.width,
                height = bounds.height,
            ),
            strokeTokenKey = strokeTokenKey,
            strokeWidthTokenKey = strokeWidthTokenKey,
            radius = radius,
        )

        is PresentationStrokeLine -> AthenaPresentationShapeCommandPayload(
            kind = "stroke_line",
            start = AthenaProjectionPointPayload(x = start.x, y = start.y),
            end = AthenaProjectionPointPayload(x = end.x, y = end.y),
            strokeTokenKey = strokeTokenKey,
            strokeWidthTokenKey = strokeWidthTokenKey,
        )

        is PresentationCircle -> AthenaPresentationShapeCommandPayload(
            kind = "circle",
            center = AthenaProjectionPointPayload(x = center.x, y = center.y),
            radius = radius,
            strokeTokenKey = strokeTokenKey,
            strokeWidthTokenKey = strokeWidthTokenKey,
            fillTokenKey = fillTokenKey,
        )

        is PresentationSvgPath -> AthenaPresentationShapeCommandPayload(
            kind = "svg_path",
            pathData = pathData,
            strokeTokenKey = strokeTokenKey,
            strokeWidthTokenKey = strokeWidthTokenKey,
            fillTokenKey = fillTokenKey,
        )
    }
}

private fun PresentationTextSlot.toPayload(): AthenaPresentationTextSlotPayload {
    return AthenaPresentationTextSlotPayload(
        slotId = slotId.value,
        origin = AthenaProjectionPointPayload(x = origin.x, y = origin.y),
        tokenKey = tokenKey,
    )
}

private fun PresentationAnchorDefinition.toPayload(): AthenaPresentationAnchorDefinitionPayload {
    return AthenaPresentationAnchorDefinitionPayload(
        alias = alias.value,
        point = AthenaProjectionPointPayload(x = point.x, y = point.y),
    )
}

private fun PresentationOccurrence.toPayload(): AthenaPresentationOccurrencePayload {
    val occurrenceReference = reference
    val primitiveReference = occurrenceReference as? PresentationPrimitiveOccurrenceReference
    val compositeReference = occurrenceReference as? PresentationCompositeOccurrenceReference
    return AthenaPresentationOccurrencePayload(
        occurrenceId = occurrenceId.value,
        semanticId = semanticId.value,
        referenceKind = when (occurrenceReference) {
            is PresentationPrimitiveOccurrenceReference -> "primitive"
            is PresentationCompositeOccurrenceReference -> "composite"
        },
        primitiveId = primitiveReference?.primitiveId?.value,
        compositeId = compositeReference?.compositeId?.value,
        bounds = AthenaPresentationBoundsPayload(
            x = bounds.x,
            y = bounds.y,
            width = bounds.width,
            height = bounds.height,
        ),
        layer = layer.name.lowercase(),
        displayLabel = displayLabel,
        orientation = orientation.name.lowercase(),
        markerKeys = markerKeys,
        textValues = textValues.mapKeys { (slotId, _) -> slotId.value }.toSortedMap(),
        anchorBindings = anchorBindings.map(PresentationAnchorBinding::toPayload),
        tokenOverrides = tokenOverrides.toSortedMap(),
        sourceProjectionIds = sourceProjectionIds.sorted(),
        trace = AthenaPresentationTracePayload(
            sourceProjectionIds = sourceProjectionIds.sorted(),
            compilerStage = when (occurrenceReference) {
                is PresentationPrimitiveOccurrenceReference -> occurrenceReference.primitiveId.value
                is PresentationCompositeOccurrenceReference -> occurrenceReference.compositeId.value
            },
        ),
    )
}

private fun PresentationAnchorBinding.toPayload(): AthenaPresentationAnchorBindingPayload {
    return AthenaPresentationAnchorBindingPayload(
        alias = alias.value,
        anchorId = anchorId,
        portSemanticId = portSemanticId?.value,
        ownerSemanticId = ownerSemanticId?.value,
        sourceLabelId = sourceLabelId,
        trace = AthenaPresentationTracePayload(
            sourceProvenance = listOfNotNull(
                alias.value.takeIf(String::isNotBlank),
                anchorId.takeIf(String::isNotBlank),
                portSemanticId?.value?.takeIf(String::isNotBlank),
                ownerSemanticId?.value?.takeIf(String::isNotBlank),
                sourceLabelId?.takeIf(String::isNotBlank),
            ),
            sourceProjectionIds = listOfNotNull(anchorId, portSemanticId?.value, ownerSemanticId?.value),
            compilerStage = "anchor-binding",
        ),
    )
}

private fun PresentationConnector.toPayload(): AthenaPresentationConnectorPayload {
    return AthenaPresentationConnectorPayload(
        occurrenceId = occurrenceId.value,
        semanticId = semanticId.value,
        primitiveId = primitiveId.value,
        routePoints = routePoints.map { point -> AthenaProjectionPointPayload(x = point.x, y = point.y) },
        layer = layer.name.lowercase(),
        sourceAnchorId = sourceAnchorId,
        targetAnchorId = targetAnchorId,
        sourcePortSemanticId = sourcePortSemanticId?.value,
        targetPortSemanticId = targetPortSemanticId?.value,
        markerKeys = markerKeys,
        tokenOverrides = tokenOverrides.toSortedMap(),
        sourceProjectionIds = sourceProjectionIds.sorted(),
        trace = AthenaPresentationTracePayload(
            sourceProvenance = listOfNotNull(sourceAnchorId, targetAnchorId).sorted(),
            sourceProjectionIds = sourceProjectionIds.sorted(),
            compilerStage = primitiveId.value,
        ),
    )
}

private fun PresentationReferenceMarkerFact.toPayload(): AthenaPresentationReferenceMarkerPayload {
    return AthenaPresentationReferenceMarkerPayload(
        markerId = markerId.value,
        markerKind = markerKind.name.lowercase(),
        relationType = relationType.name.lowercase(),
        selectedSheetViewId = selectedSheetViewId.value,
        sourceOccurrenceId = sourceOccurrenceId.value,
        targetOccurrenceId = targetOccurrenceId.value,
        sourceIdentity = sourceIdentity.value,
        targetIdentity = targetIdentity.value,
        sourceDocumentLocation = AthenaDocumentLocationPayload(
            sheetViewId = sourceDocumentLocation.sheetViewId.value,
            zoneId = sourceDocumentLocation.zoneId.value,
            displayNotation = sourceDocumentLocation.displayNotation,
        ),
        targetDocumentLocation = AthenaDocumentLocationPayload(
            sheetViewId = targetDocumentLocation.sheetViewId.value,
            zoneId = targetDocumentLocation.zoneId.value,
            displayNotation = targetDocumentLocation.displayNotation,
        ),
        compactNotation = compactNotation,
        sourceProjectionIds = sourceProjectionIds.sorted(),
    )
}

private fun PresentationRepresentationFact.toPayload(): AthenaPresentationRepresentationFactPayload {
    return AthenaPresentationRepresentationFactPayload(
        subjectId = subjectId.value,
        occurrenceId = occurrenceId.value,
        sourceProjectionIds = sourceProjectionIds.sorted(),
        symbol = symbol.toPayload(),
        anatomy = anatomy.toPayload(),
        terminals = terminals.map(PresentationTerminalFact::toPayload),
        labels = labels.map(LabelFact::toPayload),
        packageEvidence = packageEvidence?.toPayload(),
        trace = AthenaPresentationTracePayload(
            sourceProvenance = buildList {
                packageEvidence?.let { evidence ->
                    add(evidence.engineeringPackageId)
                    add(evidence.presentationProfileId)
                    add(evidence.representationPackageId)
                    add(evidence.descriptorId)
                    add(evidence.graphicResourceId)
                    add(evidence.resolverStage)
                }
            }.sorted(),
            sourceProjectionIds = sourceProjectionIds.sorted(),
            compilerStage = packageEvidence?.resolverStage ?: "representation-fact-deriver",
            packageEvidence = packageEvidence?.toPayload(),
        ),
    )
}

private fun PresentationPackageEvidence.toPayload(): AthenaPresentationPackageEvidencePayload =
    AthenaPresentationPackageEvidencePayload(
        engineeringPackageId = engineeringPackageId,
        engineeringPackageVersion = engineeringPackageVersion,
        presentationProfileId = presentationProfileId,
        bindingManifestId = bindingManifestId,
        representationPackageId = representationPackageId,
        representationPackageVersion = representationPackageVersion,
        descriptorId = descriptorId,
        graphicResourceId = graphicResourceId,
        variant = variant,
        anchorMapSummary = anchorMapSummary,
        labelBindingSummary = labelBindingSummary,
        resolverStage = resolverStage,
        rendererFallbackAccepted = rendererFallbackAccepted,
    )

private fun SymbolAnatomy.toPayload(): AthenaPresentationSymbolAnatomyPayload {
    return AthenaPresentationSymbolAnatomyPayload(
        familyId = familyId.value,
    )
}

internal fun PresentationAnatomy.toPayload(): AthenaPresentationAnatomyPayload {
    return AthenaPresentationAnatomyPayload(
        representationId = representationId.value,
        context = context.name.lowercase(),
        bounds = AthenaPresentationSizePayload(
            width = bounds.width.value,
            height = bounds.height.value,
        ),
        hotspot = hotspot.point.toPayload(),
        primitives = primitives.map(PresentationPrimitive::toPayload),
        terminals = terminals.map(PresentationTerminalPoint::toPayload),
        labelAnchors = labelAnchors.map(PresentationLabelAnchor::toPayload),
    )
}

private fun RouteFactSnapshot.toPayload(): AthenaPresentationRouteFactSnapshotPayload {
    return AthenaPresentationRouteFactSnapshotPayload(
        snapshotId = snapshotId.value,
        family = family,
        routeFacts = routeFacts.map(RouteFact::toPayload),
        junctionFacts = junctionFacts.map(RouteJunctionFact::toPayload),
        crossingFacts = crossingFacts.map(RouteCrossingFact::toPayload),
    )
}

private fun RouteFact.toPayload(): AthenaPresentationRouteFactPayload {
    return AthenaPresentationRouteFactPayload(
        routeId = routeId.value,
        snapshotId = snapshotId.value,
        connectionId = connectionId.value,
        source = source.toPayload(),
        target = target.toPayload(),
        segments = segments.map { segment ->
            AthenaPresentationRouteSegmentPayload(
                start = AthenaProjectionPointPayload(x = segment.start.x, y = segment.start.y),
                end = AthenaProjectionPointPayload(x = segment.end.x, y = segment.end.y),
            )
        },
        lane = lane.value,
        quality = quality.toPayload(),
        trace = AthenaPresentationTracePayload(
            sourceProvenance = listOf(source.policySource, target.policySource).distinct().sorted(),
            sourceProjectionIds = listOf(snapshotId.value, connectionId.value),
            compilerStage = "route-fact-deriver",
        ),
    )
}

private fun TerminalAnchorFact.toPayload(): AthenaPresentationTerminalFactPayload {
    return AthenaPresentationTerminalFactPayload(
        presentationTerminalId = "terminal:${subjectId.value}:${anchorId.value}",
        subjectId = subjectId.value,
        occurrenceId = occurrenceId.value,
        portId = portId.value,
        physicalTerminalId = "${subjectId.value}:${anchorId.value}",
        side = side.name.lowercase(),
        routeAnchor = AthenaPresentationRouteAnchorPayload(
            anchorId = anchorId.value,
            point = AthenaProjectionPointPayload(x = point.x, y = point.y),
        ),
        notation = AthenaPresentationTerminalNotationPayload(
            marker = "circle",
            number = portId.value,
        ),
    )
}

private fun RouteJunctionFact.toPayload(): AthenaPresentationRouteJunctionFactPayload {
    return AthenaPresentationRouteJunctionFactPayload(
        junctionId = junctionId,
        point = AthenaProjectionPointPayload(x = point.x, y = point.y),
        routeIds = routeIds.map { routeId -> routeId.value },
        semanticPortId = semanticPortId,
    )
}

private fun RouteCrossingFact.toPayload(): AthenaPresentationRouteCrossingFactPayload {
    return AthenaPresentationRouteCrossingFactPayload(
        crossingId = crossingId,
        point = AthenaProjectionPointPayload(x = point.x, y = point.y),
        routeIds = routeIds.map { routeId -> routeId.value },
        joined = joined,
    )
}

private fun RouteQuality.toPayload(): AthenaPresentationRouteQualityPayload {
    return AthenaPresentationRouteQualityPayload(
        state = state.name.lowercase(),
        failedConstraintIds = failedConstraintIds.map { constraintId -> constraintId.value }.sorted(),
        message = message,
    )
}

private fun PresentationPrimitive.toPayload(): AthenaPresentationAnatomyPrimitivePayload {
    return when (this) {
        is PresentationPrimitive.Rectangle -> AthenaPresentationAnatomyPrimitivePayload(
            kind = "rectangle",
            primitiveId = primitiveId.value,
            origin = origin.toPayload(),
            size = AthenaPresentationSizePayload(
                width = size.width.value,
                height = size.height.value,
            ),
        )

        is PresentationPrimitive.Line -> AthenaPresentationAnatomyPrimitivePayload(
            kind = "line",
            primitiveId = primitiveId.value,
            start = start.toPayload(),
            end = end.toPayload(),
        )

        is PresentationPrimitive.Polyline -> AthenaPresentationAnatomyPrimitivePayload(
            kind = "polyline",
            primitiveId = primitiveId.value,
            points = points.map(PresentationPoint::toPayload),
        )

        is PresentationPrimitive.Circle -> AthenaPresentationAnatomyPrimitivePayload(
            kind = "circle",
            primitiveId = primitiveId.value,
            center = center.toPayload(),
            radius = radius.value,
        )

        is PresentationPrimitive.Text -> AthenaPresentationAnatomyPrimitivePayload(
            kind = "text",
            primitiveId = primitiveId.value,
            origin = origin.toPayload(),
            text = text,
        )
    }
}

private fun PresentationTerminalPoint.toPayload(): AthenaPresentationTerminalPointPayload {
    return AthenaPresentationTerminalPointPayload(
        terminalId = terminalId.value,
        role = role.name.lowercase(),
        localPoint = localPoint.toPayload(),
        side = side.name.lowercase(),
        notation = notation.toPayload(),
    )
}

private fun PresentationTerminalFact.toPayload(): AthenaPresentationTerminalFactPayload {
    return AthenaPresentationTerminalFactPayload(
        presentationTerminalId = presentationTerminalId.value,
        subjectId = subjectId.value,
        occurrenceId = occurrenceId.value,
        portId = portId.value,
        physicalTerminalId = physicalTerminalId.value,
        side = side.name.lowercase(),
        routeAnchor = routeAnchor.toPayload(),
        notation = notation.toPayload(),
    )
}

private fun PresentationRouteAnchor.toPayload(): AthenaPresentationRouteAnchorPayload {
    return AthenaPresentationRouteAnchorPayload(
        anchorId = anchorId.value,
        point = point.toPayload(),
    )
}

private fun TerminalNotation.toPayload(): AthenaPresentationTerminalNotationPayload {
    return AthenaPresentationTerminalNotationPayload(
        marker = marker.name.lowercase(),
        number = number.value,
    )
}

private fun LabelFact.toPayload(): AthenaPresentationLabelFactPayload {
    return AthenaPresentationLabelFactPayload(
        labelId = labelId.value,
        subjectId = subjectId.value,
        occurrenceId = occurrenceId.value,
        role = role.name.lowercase(),
        value = value.value,
        anchor = anchor.toPayload(),
    )
}

private fun PresentationLabelAnchor.toPayload(): AthenaPresentationLabelAnchorPayload {
    return AthenaPresentationLabelAnchorPayload(
        anchorId = anchorId.value,
        role = role.name.lowercase(),
        point = point.toPayload(),
    )
}

private fun PresentationPoint.toPayload(): AthenaProjectionPointPayload {
    return AthenaProjectionPointPayload(
        x = x.value,
        y = y.value,
    )
}
