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
import com.engineeringood.athena.presentation.PresentationOccurrence
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

internal fun PresentationDocument.toPayload(): AthenaPresentationDocumentPayload {
    return AthenaPresentationDocumentPayload(
        canvasWidth = canvasWidth,
        canvasHeight = canvasHeight,
        primitivePacks = primitivePacks.map(PresentationPrimitivePack::toPayload),
        compositePacks = compositePacks.map(PresentationCompositePack::toPayload),
        occurrences = occurrences.map(PresentationOccurrence::toPayload),
        connectors = connectorsForRendering().map(PresentationConnector::toPayload),
        representationFacts = representationFactsForRendering().map(PresentationRepresentationFact::toPayload),
        referenceMarkers = referenceMarkers.map(PresentationReferenceMarkerFact::toPayload),
    )
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
    val primitiveReference = reference as? PresentationPrimitiveOccurrenceReference
    val compositeReference = reference as? PresentationCompositeOccurrenceReference
    return AthenaPresentationOccurrencePayload(
        occurrenceId = occurrenceId.value,
        semanticId = semanticId.value,
        referenceKind = when (reference) {
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
    )
}

private fun PresentationAnchorBinding.toPayload(): AthenaPresentationAnchorBindingPayload {
    return AthenaPresentationAnchorBindingPayload(
        alias = alias.value,
        anchorId = anchorId,
        portSemanticId = portSemanticId?.value,
        ownerSemanticId = ownerSemanticId?.value,
        sourceLabelId = sourceLabelId,
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
    )
}

private fun SymbolAnatomy.toPayload(): AthenaPresentationSymbolAnatomyPayload {
    return AthenaPresentationSymbolAnatomyPayload(
        familyId = familyId.value,
    )
}

private fun PresentationAnatomy.toPayload(): AthenaPresentationAnatomyPayload {
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
