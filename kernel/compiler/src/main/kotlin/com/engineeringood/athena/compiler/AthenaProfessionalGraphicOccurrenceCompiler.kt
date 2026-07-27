package com.engineeringood.athena.compiler

import com.engineeringood.athena.layout.DrawingGridPosition
import com.engineeringood.athena.layout.LayoutOrientation
import com.engineeringood.athena.presentation.PresentationDrawingBounds
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicTransform
import com.engineeringood.athena.representation.RepresentationLabelSlot
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

internal data class AthenaCompiledGraphicOccurrenceMaterial(
    val bounds: PresentationDrawingBounds,
    val graphic: GraphicPrimitiveDocument,
    val anchors: Map<String, GraphicPoint>,
    val labelSlots: Map<String, GraphicBounds>,
)

/** Applies one compiler-owned sheet transform to graphic commands, anchors, and label slots. */
internal object AthenaProfessionalGraphicOccurrenceCompiler {
    fun compile(
        occurrenceId: String,
        material: AthenaResolvedRepresentationMaterial,
        gridPosition: DrawingGridPosition,
        orientation: LayoutOrientation,
        drawingArea: PresentationDrawingBounds,
        policy: AthenaProfessionalDrawingPolicy,
    ): AthenaCompiledGraphicOccurrenceMaterial {
        val sourceBounds = requireNotNull(material.definition.graphicBody.bounds) {
            "Resolved Graphic Primitive material requires intrinsic bounds."
        }
        val cellWidth = drawingArea.width.toDouble() / policy.columnLabels.size
        val cellHeight = drawingArea.height.toDouble() / policy.rowLabels.size
        val orientedWidth = if (orientation == LayoutOrientation.HORIZONTAL) sourceBounds.height else sourceBounds.width
        val orientedHeight = if (orientation == LayoutOrientation.HORIZONTAL) sourceBounds.width else sourceBounds.height
        val availableWidth = cellWidth - policy.occurrenceHorizontalPadding * 2.0
        val availableHeight = cellHeight - policy.occurrenceVerticalPadding * 2.0
        val scale = min(availableWidth / orientedWidth, availableHeight / orientedHeight)
        require(scale.isFinite() && scale > 0.0) { "Professional drawing occurrence scale must be finite and positive." }

        val targetWidth = orientedWidth * scale
        val targetHeight = orientedHeight * scale
        val cellX = drawingArea.x + (gridPosition.column - 1) * cellWidth
        val cellY = drawingArea.y + (gridPosition.row - 1) * cellHeight
        val targetX = cellX + (cellWidth - targetWidth) / 2.0
        val targetY = cellY + (cellHeight - targetHeight) / 2.0
        val targetBounds = GraphicBounds(targetX, targetY, targetWidth, targetHeight)
        val sheetTransform = sheetTransform(sourceBounds, targetBounds, orientation, scale)
        val prefix = "$occurrenceId:"
        val primitives = material.definition.graphicBody.primitives.flatMap { primitive ->
            primitive.flatten(sheetTransform, prefix)
        }
        val transformedGraphic = GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId(occurrenceId),
            bounds = targetBounds,
            primitives = primitives,
            styleTokens = material.definition.graphicBody.styleTokens,
            provenanceSources = material.definition.graphicBody.provenanceSources,
            forbiddenAuthorityClaims = material.definition.graphicBody.forbiddenAuthorityClaims,
        )
        return AthenaCompiledGraphicOccurrenceMaterial(
            bounds = targetBounds.toPresentationBounds(),
            graphic = transformedGraphic,
            anchors = material.definition.anchors.associate { anchor ->
                anchor.anchorId.value to sheetTransform.point(anchor.point)
            }.toSortedMap(),
            labelSlots = material.definition.labelSlots.mapNotNull { slot ->
                slot.bounds?.let { bounds -> slot.slotId.value to sheetTransform.bounds(bounds) }
            }.toMap().toSortedMap(),
        )
    }

    private fun sheetTransform(
        source: GraphicBounds,
        target: GraphicBounds,
        orientation: LayoutOrientation,
        scale: Double,
    ): AffineTransform = when (orientation) {
        LayoutOrientation.VERTICAL -> AffineTransform.translation(target.x, target.y)
            .compose(AffineTransform.scale(scale, scale))
            .compose(AffineTransform.translation(-source.x, -source.y))
        LayoutOrientation.HORIZONTAL -> AffineTransform.translation(target.x, target.y)
            .compose(AffineTransform.scale(scale, scale))
            .compose(AffineTransform.translation(source.height, 0.0))
            .compose(AffineTransform.rotation(90.0, GraphicPoint(0.0, 0.0)))
            .compose(AffineTransform.translation(-source.x, -source.y))
    }
}

private data class AffineTransform(
    val a: Double,
    val b: Double,
    val c: Double,
    val d: Double,
    val e: Double,
    val f: Double,
) {
    fun compose(inner: AffineTransform): AffineTransform = AffineTransform(
        a = a * inner.a + c * inner.b,
        b = b * inner.a + d * inner.b,
        c = a * inner.c + c * inner.d,
        d = b * inner.c + d * inner.d,
        e = a * inner.e + c * inner.f + e,
        f = b * inner.e + d * inner.f + f,
    )

    fun point(point: GraphicPoint): GraphicPoint = GraphicPoint(
        x = a * point.x + c * point.y + e,
        y = b * point.x + d * point.y + f,
    )

    fun vector(point: GraphicPoint): GraphicPoint = GraphicPoint(
        x = a * point.x + c * point.y,
        y = b * point.x + d * point.y,
    )

    fun bounds(bounds: GraphicBounds): GraphicBounds {
        val points = listOf(
            point(GraphicPoint(bounds.x, bounds.y)),
            point(GraphicPoint(bounds.x + bounds.width, bounds.y)),
            point(GraphicPoint(bounds.x + bounds.width, bounds.y + bounds.height)),
            point(GraphicPoint(bounds.x, bounds.y + bounds.height)),
        )
        val left = points.minOf(GraphicPoint::x)
        val top = points.minOf(GraphicPoint::y)
        val right = points.maxOf(GraphicPoint::x)
        val bottom = points.maxOf(GraphicPoint::y)
        return GraphicBounds(left, top, right - left, bottom - top)
    }

    fun uniformScale(): Double {
        val xScale = hypot(a, b)
        val yScale = hypot(c, d)
        require(abs(xScale - yScale) <= 0.000_001) {
            "Circle and arc material requires a uniform occurrence transform."
        }
        return xScale
    }

    fun transformedAngle(angleDegrees: Double): Double {
        val radians = angleDegrees * PI / 180.0
        val transformed = vector(GraphicPoint(cos(radians), sin(radians)))
        return atan2(transformed.y, transformed.x) * 180.0 / PI
    }

    val determinant: Double
        get() = a * d - b * c

    companion object {
        fun translation(dx: Double, dy: Double) = AffineTransform(1.0, 0.0, 0.0, 1.0, dx, dy)

        fun scale(x: Double, y: Double, pivot: GraphicPoint = GraphicPoint(0.0, 0.0)): AffineTransform =
            translation(pivot.x, pivot.y)
                .compose(AffineTransform(x, 0.0, 0.0, y, 0.0, 0.0))
                .compose(translation(-pivot.x, -pivot.y))

        fun rotation(angleDegrees: Double, pivot: GraphicPoint): AffineTransform {
            val radians = angleDegrees * PI / 180.0
            val rotation = AffineTransform(cos(radians), sin(radians), -sin(radians), cos(radians), 0.0, 0.0)
            return translation(pivot.x, pivot.y).compose(rotation).compose(translation(-pivot.x, -pivot.y))
        }
    }
}

private fun GraphicTransform.toAffine(): AffineTransform = when (this) {
    is GraphicTransform.Translation -> AffineTransform.translation(dx, dy)
    is GraphicTransform.Rotation -> AffineTransform.rotation(angleDegrees, pivot)
    is GraphicTransform.Scale -> AffineTransform.scale(x, y, pivot)
}

private fun GraphicPrimitive.flatten(
    transform: AffineTransform,
    idPrefix: String,
): List<GraphicPrimitive> = when (this) {
    is GraphicPrimitive.Group -> children.flatMap { child -> child.flatten(transform, idPrefix) }
    is GraphicPrimitive.Transformed -> child.flatten(transform.compose(this.transform.toAffine()), idPrefix)
    is GraphicPrimitive.Line -> listOf(
        copy(
            primitiveId = primitiveId.namespaced(idPrefix),
            bounds = transform.bounds(bounds),
            start = transform.point(start),
            end = transform.point(end),
        ),
    )
    is GraphicPrimitive.Polyline -> listOf(
        copy(
            primitiveId = primitiveId.namespaced(idPrefix),
            bounds = transform.bounds(bounds),
            points = points.map(transform::point),
        ),
    )
    is GraphicPrimitive.Arc -> listOf(
        copy(
            primitiveId = primitiveId.namespaced(idPrefix),
            bounds = transform.bounds(bounds),
            center = transform.point(center),
            radius = radius * transform.uniformScale(),
            startAngleDegrees = transform.transformedAngle(startAngleDegrees),
            sweepAngleDegrees = if (transform.determinant < 0.0) -sweepAngleDegrees else sweepAngleDegrees,
        ),
    )
    is GraphicPrimitive.Circle -> listOf(
        copy(
            primitiveId = primitiveId.namespaced(idPrefix),
            bounds = transform.bounds(bounds),
            center = transform.point(center),
            radius = radius * transform.uniformScale(),
        ),
    )
    is GraphicPrimitive.Rectangle -> listOf(
        copy(
            primitiveId = primitiveId.namespaced(idPrefix),
            bounds = transform.bounds(bounds),
            cornerRadius = cornerRadius * transform.uniformScale(),
        ),
    )
    is GraphicPrimitive.Text -> listOf(
        copy(
            primitiveId = primitiveId.namespaced(idPrefix),
            bounds = transform.bounds(bounds),
            origin = transform.point(origin),
        ),
    )
    is GraphicPrimitive.Marker -> listOf(
        copy(
            primitiveId = primitiveId.namespaced(idPrefix),
            bounds = transform.bounds(bounds),
            origin = transform.point(origin),
        ),
    )
    is GraphicPrimitive.ConnectionDot -> listOf(
        copy(
            primitiveId = primitiveId.namespaced(idPrefix),
            bounds = transform.bounds(bounds),
            center = transform.point(center),
            radius = radius * transform.uniformScale(),
        ),
    )
    is GraphicPrimitive.ReferenceArrow -> listOf(
        copy(
            primitiveId = primitiveId.namespaced(idPrefix),
            bounds = transform.bounds(bounds),
            start = transform.point(start),
            end = transform.point(end),
            headSize = headSize * transform.uniformScale(),
        ),
    )
}

private fun GraphicPrimitiveId.namespaced(prefix: String) = GraphicPrimitiveId(prefix + value)

private fun GraphicBounds.toPresentationBounds() = PresentationDrawingBounds(
    x = x.roundToInt(),
    y = y.roundToInt(),
    width = width.roundToInt().coerceAtLeast(1),
    height = height.roundToInt().coerceAtLeast(1),
)
