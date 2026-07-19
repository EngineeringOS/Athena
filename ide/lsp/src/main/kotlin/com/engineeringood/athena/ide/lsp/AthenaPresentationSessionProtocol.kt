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
import com.engineeringood.athena.presentation.PresentationShapeCommand
import com.engineeringood.athena.presentation.PresentationStrokeLine
import com.engineeringood.athena.presentation.PresentationStrokeRectangle
import com.engineeringood.athena.presentation.PresentationSvgPath
import com.engineeringood.athena.presentation.PresentationTextSlot
import com.engineeringood.athena.presentation.connectorsForRendering

internal fun PresentationDocument.toPayload(): AthenaPresentationDocumentPayload {
    return AthenaPresentationDocumentPayload(
        canvasWidth = canvasWidth,
        canvasHeight = canvasHeight,
        primitivePacks = primitivePacks.map(PresentationPrimitivePack::toPayload),
        compositePacks = compositePacks.map(PresentationCompositePack::toPayload),
        occurrences = occurrences.map(PresentationOccurrence::toPayload),
        connectors = connectorsForRendering().map(PresentationConnector::toPayload),
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
