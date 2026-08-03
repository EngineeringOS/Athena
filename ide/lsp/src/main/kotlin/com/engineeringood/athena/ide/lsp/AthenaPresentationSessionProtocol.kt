package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.presentation.PresentationAnchorBinding
import com.engineeringood.athena.presentation.PresentationAnchorDefinition
import com.engineeringood.athena.presentation.PresentationCircle
import com.engineeringood.athena.presentation.PresentationCompositeDefinition
import com.engineeringood.athena.presentation.PresentationCompositeOccurrenceReference
import com.engineeringood.athena.presentation.PresentationCompositePack
import com.engineeringood.athena.presentation.PresentationCompositePart
import com.engineeringood.athena.presentation.PresentationConnector
import com.engineeringood.athena.presentation.PresentationConnectorEndpoint
import com.engineeringood.athena.presentation.PresentationConnectorLabel
import com.engineeringood.athena.presentation.PresentationConnectorLine
import com.engineeringood.athena.presentation.PresentationConnectionMarker
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationDrawingBounds
import com.engineeringood.athena.presentation.PresentationGraphicLabel
import com.engineeringood.athena.presentation.PresentationGraphicOccurrence
import com.engineeringood.athena.presentation.PresentationGraphicOccurrenceAuthorities
import com.engineeringood.athena.presentation.PresentationGraphicTerminalBinding
import com.engineeringood.athena.presentation.PresentationPlacedAnchor
import com.engineeringood.athena.presentation.PresentationOccurrence
import com.engineeringood.athena.presentation.PresentationPackageTrace
import com.engineeringood.athena.presentation.PresentationPaintItem
import com.engineeringood.athena.presentation.PresentationPaintPlan
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
import com.engineeringood.athena.presentation.representationFactsForRendering
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.representation.LabelFact
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.PresentationLabelAnchor
import com.engineeringood.athena.representation.PresentationPoint
import com.engineeringood.athena.representation.PresentationRouteAnchor
import com.engineeringood.athena.representation.PresentationTerminalFact
import com.engineeringood.athena.representation.RepresentationAnchorContract
import com.engineeringood.athena.representation.RepresentationDefinition
import com.engineeringood.athena.representation.RepresentationLabelSlot
import com.engineeringood.athena.representation.TerminalNotation
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
        connectors = connectors.map(PresentationConnector::toPayload),
        connectionMarkers = connectionMarkers.map(PresentationConnectionMarker::toPayload),
        paintPlan = requireNotNull(paintPlan) {
            "Presentation Document requires paint plan before LSP publication."
        }.toPayload(),
        representationFacts = representationFactsForRendering().map(PresentationRepresentationFact::toPayload),
        referenceMarkers = referenceMarkers.map(PresentationReferenceMarkerFact::toPayload),
        drawingComposition = drawingComposition?.toPayload(),
    )
}

private fun PresentationPaintPlan.toPayload(): AthenaPresentationPaintPlanPayload =
    AthenaPresentationPaintPlanPayload(
        items = items.map(PresentationPaintItem::toPayload),
    )

private fun PresentationPaintItem.toPayload(): AthenaPresentationPaintItemPayload =
    AthenaPresentationPaintItemPayload(
        itemId = itemId,
        targetId = targetId,
        kind = kind,
        visible = visible,
        order = order,
    )

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
        placedAnchors = placedAnchors.map(PresentationPlacedAnchor::toPayload),
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

private fun PresentationPlacedAnchor.toPayload() =
    AthenaPresentationPlacedAnchorPayload(
        anchorId = anchorId.value,
        geometryRef = geometryRef,
        primitiveId = primitiveId.value,
        point = AthenaPointPayload(point.x, point.y),
        role = role.name.lowercase(),
        required = required,
        sourceProvenance = sourceProvenance.sorted(),
        trace = AthenaPresentationTracePayload(
            sourceProvenance = sourceProvenance.sorted(),
            sourceProjectionIds = listOf(anchorId.value, primitiveId.value),
            compilerStage = "graphic-occurrence-anchor",
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
	            bindingId = bindingId.value,
	            anchorId = anchorId,
	            terminalIdentity = terminalIdentity,
        point = AthenaPointPayload(point.x, point.y),
        labelPoint = AthenaPointPayload(labelPoint.x, labelPoint.y),
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

internal fun GraphicPrimitiveDocument.toPayload() =
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

private fun GraphicPoint.toPayload() = AthenaPointPayload(
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
            start = AthenaPointPayload(x = start.x, y = start.y),
            end = AthenaPointPayload(x = end.x, y = end.y),
            strokeTokenKey = strokeTokenKey,
            strokeWidthTokenKey = strokeWidthTokenKey,
        )

        is PresentationCircle -> AthenaPresentationShapeCommandPayload(
            kind = "circle",
            center = AthenaPointPayload(x = center.x, y = center.y),
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
        origin = AthenaPointPayload(x = origin.x, y = origin.y),
        tokenKey = tokenKey,
    )
}

private fun PresentationAnchorDefinition.toPayload(): AthenaPresentationAnchorDefinitionPayload {
    return AthenaPresentationAnchorDefinitionPayload(
        alias = alias.value,
        point = AthenaPointPayload(x = point.x, y = point.y),
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
	        routePoints = routePoints.map { point -> AthenaPointPayload(x = point.x, y = point.y) },
	        lineClassId = line.classId,
	        line = line.toPayload(),
		        routeId = routeId,
	        bundleId = bundleId,
	        laneId = laneId,
	        laneRouteIds = laneRouteIds.sorted(),
	        selectedChannelIds = selectedChannelIds.sorted(),
	        labels = labels.map(PresentationConnectorLabel::toPayload),
	        quality = quality,
	        sourceEndpoint = sourceEndpoint.toPayload(),
	        targetEndpoint = targetEndpoint.toPayload(),
	        layer = layer.name.lowercase(),
	        markerIds = markerIds.map { markerId -> markerId.value }.sorted(),
	        tokenOverrides = tokenOverrides.toSortedMap(),
	        sourceProjectionIds = sourceProjectionIds.sorted(),
	        trace = AthenaPresentationTracePayload(
	            sourceProvenance = (sourceEndpoint.sourceProvenance + targetEndpoint.sourceProvenance).distinct().sorted(),
	            sourceProjectionIds = sourceProjectionIds.sorted(),
	            compilerStage = primitiveId.value,
	            sourceSpan = sourceSpan.toPayload(),
	        ),
	        sourceSpan = sourceSpan.toPayload(),
	    )
	}

private fun PresentationConnectorLine.toPayload(): AthenaPresentationConnectorLinePayload =
    AthenaPresentationConnectorLinePayload(
        classId = classId,
        lineKind = lineKind,
        lineStyleId = lineStyleId,
        weight = weight,
        style = style,
        colorKey = colorKey,
        endpointBehavior = endpointBehavior,
        labelPolicy = labelPolicy,
        crossingBehavior = crossingBehavior,
        policyId = policyId,
        compilerSnapshotId = compilerSnapshotId,
    )

private fun PresentationConnectorLabel.toPayload(): AthenaPresentationConnectorLabelPayload =
    AthenaPresentationConnectorLabelPayload(
        labelId = labelId,
        text = text,
        point = AthenaPointPayload(point.x, point.y),
        bounds = bounds.toPayload(),
        labelClassId = labelClassId,
        display = display.name.lowercase(),
        sourceProvenance = sourceProvenance.sorted(),
        compilerSnapshotId = compilerSnapshotId,
        trace = AthenaPresentationTracePayload(
            sourceProvenance = sourceProvenance.sorted(),
            sourceProjectionIds = listOf(labelId),
            compilerStage = labelClassId,
            compilerSnapshotId = compilerSnapshotId,
        ),
    )

private fun PresentationConnectorEndpoint.toPayload(): AthenaPresentationConnectorEndpointPayload =
    AthenaPresentationConnectorEndpointPayload(
        portSemanticId = portSemanticId.value,
        bindingId = bindingId.value,
        occurrenceId = occurrenceId.value,
        anchorId = anchorId.value,
        point = AthenaPointPayload(point.x, point.y),
        sourceProvenance = sourceProvenance.sorted(),
	        trace = AthenaPresentationTracePayload(
	            sourceProvenance = sourceProvenance.sorted(),
	            sourceProjectionIds = listOf(
	                portSemanticId.value,
	                bindingId.value,
	                occurrenceId.value,
	                anchorId.value,
	            ).sorted(),
	            compilerStage = "presentation-connector-endpoint",
	        ),
	    )

private fun PresentationConnectionMarker.toPayload(): AthenaPresentationConnectionMarkerPayload =
    AthenaPresentationConnectionMarkerPayload(
        markerId = markerId.value,
        kind = kind.name.lowercase(),
        point = AthenaPointPayload(point.x, point.y),
        routeIds = routeIds.sorted(),
        connectorIds = connectorIds.map { connectorId -> connectorId.value }.sorted(),
        semanticId = semanticId?.value,
        joined = joined,
        appearanceClassId = appearanceClassId,
        sourceProjectionIds = sourceProjectionIds.sorted(),
        sourceProvenance = sourceProvenance.sorted(),
        compilerSnapshotId = compilerSnapshotId,
        trace = AthenaPresentationTracePayload(
            sourceProvenance = sourceProvenance.sorted(),
            sourceProjectionIds = sourceProjectionIds.sorted(),
            compilerStage = appearanceClassId,
            compilerSnapshotId = compilerSnapshotId,
        ),
    )

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
        definition = definition.toPayload(),
        terminals = terminals.map(PresentationTerminalFact::toPayload),
        labels = labels.map(LabelFact::toPayload),
        packageTrace = packageTrace?.toPayload(),
        trace = AthenaPresentationTracePayload(
            sourceProvenance = buildList {
                packageTrace?.let { trace ->
                    add(trace.engineeringPackageId)
                    add(trace.presentationProfileId)
                    add(trace.representationPackageId)
                    add(trace.descriptorId)
                    add(trace.graphicResourceId)
                    add(trace.resolverStage)
                }
            }.sorted(),
            sourceProjectionIds = sourceProjectionIds.sorted(),
            compilerStage = packageTrace?.resolverStage ?: "representation-fact-deriver",
            packageTrace = packageTrace?.toPayload(),
        ),
    )
}

private fun PresentationPackageTrace.toPayload(): AthenaPresentationPackageTracePayload =
    AthenaPresentationPackageTracePayload(
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
    )

private fun RepresentationDefinition.toPayload(): AthenaRepresentationDefinitionPayload =
    AthenaRepresentationDefinitionPayload(
        symbolId = symbolId.value,
        libraryId = libraryId.value,
        version = version.value,
        kind = kind.name.lowercase(),
        definitionKind = definitionKind.name.lowercase(),
        graphicBody = graphicBody.toPayload(),
        anchors = anchors.map(RepresentationAnchorContract::toPayload),
        labelSlots = labelSlots.map(RepresentationLabelSlot::toPayload),
        provenance = lifecycle.provenance.source,
    )

private fun RepresentationAnchorContract.toPayload(): AthenaRepresentationAnchorPayload =
    AthenaRepresentationAnchorPayload(
        anchorId = anchorId.value,
        geometryRef = geometryRef,
        primitiveId = primitiveId.value,
        point = point.toPayload(),
        role = role.name.lowercase(),
        required = required,
    )

private fun RepresentationLabelSlot.toPayload(): AthenaRepresentationLabelSlotPayload =
    AthenaRepresentationLabelSlotPayload(
        slotId = slotId.value,
        role = role.name.lowercase(),
        origin = origin?.toPayload(),
        bounds = bounds?.toPayload(),
        styleTokenId = styleTokenId?.value,
    )

private fun SourceProvenance.toPayload(): AthenaPresentationSourceSpanPayload =
    AthenaPresentationSourceSpanPayload(
        file = file,
        startLine = startLine,
        startColumn = startColumn,
        endLine = endLine,
        endColumn = endColumn,
    )

private fun LayoutSourceSpan.toPayload(): AthenaPresentationSourceSpanPayload =
    AthenaPresentationSourceSpanPayload(
        file = sourceUnitId,
        startLine = startLine,
        startColumn = startColumn,
        endLine = endLine,
        endColumn = endColumn,
    )

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
            point = AthenaPointPayload(x = point.x, y = point.y),
        ),
        notation = AthenaPresentationTerminalNotationPayload(
            marker = "circle",
            number = portId.value,
        ),
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

private fun PresentationPoint.toPayload(): AthenaPointPayload {
    return AthenaPointPayload(
        x = x.value,
        y = y.value,
    )
}
