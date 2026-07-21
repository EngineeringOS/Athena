package com.engineeringood.athena.representation

object NativeRepresentationPrimitiveEmitter {
    fun emit(library: NativeRepresentationLibrary): List<String> {
        return library.definitions
            .sortedBy { definition -> definition.symbolId.value }
            .flatMap { definition ->
                definition.anatomy.primitives
                    .sortedBy { primitive -> primitive.primitiveId.value }
                    .map { primitive -> "${definition.symbolId.value}:${primitive.toStablePayload()}" }
            }
            .sorted()
    }

    private fun PresentationPrimitive.toStablePayload(): String {
        return when (this) {
            is PresentationPrimitive.Circle -> listOf(
                "circle",
                primitiveId.value,
                center.x.value,
                center.y.value,
                radius.value,
            ).joinToString(":")
            is PresentationPrimitive.Line -> listOf(
                "line",
                primitiveId.value,
                start.x.value,
                start.y.value,
                end.x.value,
                end.y.value,
            ).joinToString(":")
            is PresentationPrimitive.Polyline -> listOf(
                "polyline",
                primitiveId.value,
                points.joinToString(separator = ";") { point -> "${point.x.value},${point.y.value}" },
            ).joinToString(":")
            is PresentationPrimitive.Rectangle -> listOf(
                "rectangle",
                primitiveId.value,
                origin.x.value,
                origin.y.value,
                size.width.value,
                size.height.value,
            ).joinToString(":")
        }
    }
}
