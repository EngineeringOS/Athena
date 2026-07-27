package com.engineeringood.athena.representation

import java.util.IdentityHashMap

data class GraphicPrimitiveDocumentTransportPayload(
    val documentId: String,
    val bounds: GraphicBoundsTransportPayload?,
    val primitives: List<GraphicPrimitiveTransportPayload>,
    val styleTokens: List<GraphicStyleTokenTransportPayload>,
    val forbiddenAuthorityClaims: List<String>,
)

data class GraphicPrimitiveTransportPayload(
    val primitiveId: String,
    val kind: String,
    val bounds: GraphicBoundsTransportPayload,
    val styleTokenId: String?,
    val start: GraphicPointTransportPayload? = null,
    val end: GraphicPointTransportPayload? = null,
    val center: GraphicPointTransportPayload? = null,
    val origin: GraphicPointTransportPayload? = null,
    val points: List<GraphicPointTransportPayload> = emptyList(),
    val radius: Double? = null,
    val cornerRadius: Double? = null,
    val startAngleDegrees: Double? = null,
    val sweepAngleDegrees: Double? = null,
    val text: String? = null,
    val markerKind: String? = null,
    val headSize: Double? = null,
    val transform: GraphicTransformTransportPayload? = null,
    val children: List<GraphicPrimitiveTransportPayload> = emptyList(),
)

data class GraphicTransformTransportPayload(
    val kind: String,
    val x: Double? = null,
    val y: Double? = null,
    val angleDegrees: Double? = null,
    val pivot: GraphicPointTransportPayload? = null,
)

data class GraphicStyleTokenTransportPayload(
    val styleTokenId: String,
    val stroke: String,
    val strokeWidth: Double,
    val fill: String,
    val lineCap: String,
    val lineJoin: String,
    val dashPattern: List<Double>,
    val textAnchor: String,
    val textBaseline: String,
)

data class GraphicBoundsTransportPayload(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
)

data class GraphicPointTransportPayload(
    val x: Double,
    val y: Double,
)

fun GraphicPrimitiveDocument.toTransportPayload(): GraphicPrimitiveDocumentTransportPayload =
    GraphicPrimitiveDocumentTransportPayload(
        documentId = documentId?.value.orEmpty(),
        bounds = bounds?.toTransportPayload(),
        primitives = primitives.map { it.toTransportPayload() },
        styleTokens = styleTokens.sortedBy { it.styleTokenId.value }.map { it.toTransportPayload() },
        forbiddenAuthorityClaims = forbiddenAuthorityClaims.map { it.name }.sorted(),
    )

fun GraphicPrimitive.toTransportPayload(): GraphicPrimitiveTransportPayload =
    toTransportPayload(IdentityHashMap(), 0)

private fun GraphicPrimitive.toTransportPayload(
    ancestors: IdentityHashMap<GraphicPrimitive, Boolean>,
    depth: Int,
): GraphicPrimitiveTransportPayload {
    require(depth <= 64) { "Graphic primitive nesting exceeds transport depth limit." }
    require(ancestors.put(this, true) == null) { "Graphic primitive transport contains a cycle." }
    val payload = when (this) {
        is GraphicPrimitive.Line -> base(start = start.toTransportPayload(), end = end.toTransportPayload())
        is GraphicPrimitive.Polyline -> base(points = points.map { it.toTransportPayload() })
        is GraphicPrimitive.Arc -> base(
            center = center.toTransportPayload(),
            radius = radius,
            startAngleDegrees = startAngleDegrees,
            sweepAngleDegrees = sweepAngleDegrees,
        )
        is GraphicPrimitive.Circle -> base(center = center.toTransportPayload(), radius = radius)
        is GraphicPrimitive.Rectangle -> base(cornerRadius = cornerRadius)
        is GraphicPrimitive.Text -> base(origin = origin.toTransportPayload(), text = text)
        is GraphicPrimitive.Marker -> base(origin = origin.toTransportPayload(), markerKind = markerKind.name.lowercase())
        is GraphicPrimitive.ConnectionDot -> base(center = center.toTransportPayload(), radius = radius)
        is GraphicPrimitive.ReferenceArrow -> base(
            start = start.toTransportPayload(),
            end = end.toTransportPayload(),
            headSize = headSize,
        )
        is GraphicPrimitive.Group -> base(children = children.map { it.toTransportPayload(ancestors, depth + 1) })
        is GraphicPrimitive.Transformed -> base(
            transform = transform.toTransportPayload(),
            children = listOf(child.toTransportPayload(ancestors, depth + 1)),
        )
    }
    ancestors.remove(this)
    return payload
}

private fun GraphicPrimitive.base(
    start: GraphicPointTransportPayload? = null,
    end: GraphicPointTransportPayload? = null,
    center: GraphicPointTransportPayload? = null,
    origin: GraphicPointTransportPayload? = null,
    points: List<GraphicPointTransportPayload> = emptyList(),
    radius: Double? = null,
    cornerRadius: Double? = null,
    startAngleDegrees: Double? = null,
    sweepAngleDegrees: Double? = null,
    text: String? = null,
    markerKind: String? = null,
    headSize: Double? = null,
    transform: GraphicTransformTransportPayload? = null,
    children: List<GraphicPrimitiveTransportPayload> = emptyList(),
): GraphicPrimitiveTransportPayload = GraphicPrimitiveTransportPayload(
    primitiveId = primitiveId.value,
    kind = kind.wireValue,
    bounds = bounds.toTransportPayload(),
    styleTokenId = styleTokenId?.value,
    start = start,
    end = end,
    center = center,
    origin = origin,
    points = points,
    radius = radius,
    cornerRadius = cornerRadius,
    startAngleDegrees = startAngleDegrees,
    sweepAngleDegrees = sweepAngleDegrees,
    text = text,
    markerKind = markerKind,
    headSize = headSize,
    transform = transform,
    children = children,
)

private fun GraphicTransform.toTransportPayload(): GraphicTransformTransportPayload = when (this) {
    is GraphicTransform.Translation -> GraphicTransformTransportPayload("translation", x = dx, y = dy)
    is GraphicTransform.Rotation -> GraphicTransformTransportPayload(
        "rotation",
        angleDegrees = angleDegrees,
        pivot = pivot.toTransportPayload(),
    )
    is GraphicTransform.Scale -> GraphicTransformTransportPayload(
        "scale",
        x = x,
        y = y,
        pivot = pivot.toTransportPayload(),
    )
}

private fun GraphicStyleToken.toTransportPayload(): GraphicStyleTokenTransportPayload =
    GraphicStyleTokenTransportPayload(
        styleTokenId = styleTokenId.value,
        stroke = stroke.value,
        strokeWidth = strokeWidth,
        fill = fill.name.lowercase(),
        lineCap = lineCap.name.lowercase(),
        lineJoin = lineJoin.name.lowercase(),
        dashPattern = dashPattern.toList(),
        textAnchor = textAnchor.name.lowercase(),
        textBaseline = textBaseline.name.lowercase(),
    )

private fun GraphicBounds.toTransportPayload(): GraphicBoundsTransportPayload =
    GraphicBoundsTransportPayload(x, y, width, height)

private fun GraphicPoint.toTransportPayload(): GraphicPointTransportPayload = GraphicPointTransportPayload(x, y)
