package com.engineeringood.athena.representation

object NativeRepresentationPrimitiveEmitter {
    fun emit(library: NativeRepresentationLibrary): List<String> {
        return library.definitions
            .sortedBy { definition -> definition.symbolId.value }
            .flatMap { definition ->
                definition.graphicBody.primitives
                    .sortedBy { primitive -> primitive.primitiveId.value }
                    .map { primitive -> "${definition.symbolId.value}:${primitive.toStablePayload()}" }
            }
            .sorted()
    }

    private fun GraphicPrimitive.toStablePayload(): String {
        return when (this) {
            is GraphicPrimitive.Circle -> listOf(
                "circle",
                primitiveId.value,
                center.x,
                center.y,
                radius,
            ).joinToString(":")
            is GraphicPrimitive.Line -> listOf(
                "line",
                primitiveId.value,
                start.x,
                start.y,
                end.x,
                end.y,
            ).joinToString(":")
            is GraphicPrimitive.Polyline -> listOf(
                "polyline",
                primitiveId.value,
                points.joinToString(separator = ";") { point -> "${point.x},${point.y}" },
            ).joinToString(":")
            is GraphicPrimitive.Rectangle -> listOf(
                "rectangle",
                primitiveId.value,
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height,
            ).joinToString(":")
            is GraphicPrimitive.Text -> listOf(
                "text",
                primitiveId.value,
                origin.x,
                origin.y,
                text,
            ).joinToString(":")
            is GraphicPrimitive.Arc -> listOf(
                "arc",
                primitiveId.value,
                center.x,
                center.y,
                radius,
                startAngleDegrees,
                sweepAngleDegrees,
            ).joinToString(":")
            is GraphicPrimitive.Marker -> listOf(
                "marker",
                primitiveId.value,
                origin.x,
                origin.y,
                markerKind.name,
            ).joinToString(":")
            is GraphicPrimitive.ConnectionDot -> listOf(
                "connection-dot",
                primitiveId.value,
                center.x,
                center.y,
                radius,
            ).joinToString(":")
            is GraphicPrimitive.ReferenceArrow -> listOf(
                "reference-arrow",
                primitiveId.value,
                start.x,
                start.y,
                end.x,
                end.y,
                headSize,
            ).joinToString(":")
            is GraphicPrimitive.Group -> listOf(
                "group",
                primitiveId.value,
                children.joinToString(separator = "|") { child -> child.toStablePayload() },
            ).joinToString(":")
            is GraphicPrimitive.Transformed -> listOf(
                "transform",
                primitiveId.value,
                child.toStablePayload(),
            ).joinToString(":")
        }
    }
}
