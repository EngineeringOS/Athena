package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementKind
import com.engineeringood.athena.geometry.GeometryDocument
import com.engineeringood.athena.geometry.GeometryElement
import com.engineeringood.athena.geometry.GeometryPoint
import com.engineeringood.athena.renderer.svg.SvgRenderBox
import com.engineeringood.athena.renderer.svg.SvgRenderConnection
import com.engineeringood.athena.renderer.svg.SvgRenderCrossing
import com.engineeringood.athena.renderer.svg.SvgRenderModel
import com.engineeringood.athena.renderer.svg.SvgRenderPoint

/** Derives the thin runtime viewer model from explicit `Geometry IR`. */
class SvgRenderModelDeriver {
    /** Derives one runtime-facing render model from [geometry]. */
    fun derive(
        systemName: String,
        geometry: GeometryDocument,
    ): SvgRenderModel {
        val connections = geometry.elements
            .filter { element -> element.kind == GeometryElementKind.PATH }
            .map { element ->
                val start = element.connectionStart()
                val end = element.connectionEnd()
                SvgRenderConnection(
                    semanticId = element.semanticId,
                    x1 = start.x,
                    y1 = start.y,
                    x2 = end.x,
                    y2 = end.y,
                    points = element.points
                        .ifEmpty { listOf(start, end) }
                        .map { point -> SvgRenderPoint(point.x, point.y) }
                        .orthogonalized(),
                )
            }
        return SvgRenderModel(
            systemName = systemName,
            canvasWidth = geometry.canvasWidth,
            canvasHeight = geometry.canvasHeight,
            boxes = geometry.elements
                .filter { element -> element.kind == GeometryElementKind.BOX }
                .map { element ->
                    SvgRenderBox(
                        semanticId = element.semanticId,
                        label = element.label.orEmpty(),
                        x = element.bounds.x,
                        y = element.bounds.y,
                        width = element.bounds.width,
                        height = element.bounds.height,
                    )
                },
            connections = connections,
            crossings = connections.crossings(),
        )
    }

    /**
     * Applies a scoped render refresh over [previousModel] using the already-derived [geometry] document.
     */
    fun deriveIncremental(
        systemName: String,
        geometry: GeometryDocument,
        previousModel: SvgRenderModel,
        affectedScope: CompilerAffectedScope,
    ): SvgRenderModel? {
        val nextModel = derive(
            systemName = systemName,
            geometry = geometry,
        )
        val previousBoxesById = previousModel.boxes.associateBy { box -> box.semanticId }
        if (nextModel.boxes.map { box -> box.semanticId } != previousModel.boxes.map { box -> box.semanticId }) {
            return null
        }
        val previousConnectionsById = previousModel.connections.associateBy { connection -> connection.semanticId }

        return nextModel.copy(
            boxes = nextModel.boxes.map { box ->
                if (box.semanticId.value in affectedScope.renderComponentSemanticIds) {
                    box
                } else {
                    previousBoxesById[box.semanticId] ?: box
                }
            },
            connections = nextModel.connections.map { connection ->
                if (connection.semanticId.value in affectedScope.renderConnectionSemanticIds) {
                    connection
                } else {
                    previousConnectionsById[connection.semanticId] ?: connection
                }
            },
        )
    }
}

private fun List<SvgRenderConnection>.crossings(): List<SvgRenderCrossing> {
    val crossings = mutableListOf<SvgRenderCrossing>()
    forEachIndexed { index, connection ->
        drop(index + 1).forEach { other ->
            val point = connection.crossingWith(other)
            if (point != null) {
                val (x, y) = point
                crossings += SvgRenderCrossing(
                    crossingId = "crossing:${connection.semanticId.value}:${other.semanticId.value}:$x:$y",
                    x = x,
                    y = y,
                )
            }
        }
    }
    return crossings.distinctBy { crossing -> crossing.crossingId }
        .sortedWith(compareBy<SvgRenderCrossing>({ it.y }, { it.x }, { it.crossingId }))
}

private fun SvgRenderConnection.crossingWith(other: SvgRenderConnection): Pair<Int, Int>? {
    segments().forEach { segment ->
        other.segments().forEach { otherSegment ->
            val point = segment.crossingWith(otherSegment)
            if (point != null) return point
        }
    }
    return null
}

private data class RenderSegment(val x1: Int, val y1: Int, val x2: Int, val y2: Int)

private fun SvgRenderConnection.segments(): List<RenderSegment> =
    points.zipWithNext()
        .filterNot { (from, to) -> from == to }
        .map { (from, to) -> RenderSegment(from.x, from.y, to.x, to.y) }

private fun RenderSegment.crossingWith(other: RenderSegment): Pair<Int, Int>? {
    val denominator = ((x1 - x2) * (other.y1 - other.y2)) - ((y1 - y2) * (other.x1 - other.x2))
    if (denominator == 0) return null
    val determinant = ((x1 - other.x1) * (other.y1 - other.y2)) -
        ((y1 - other.y1) * (other.x1 - other.x2))
    val otherDeterminant = ((x1 - other.x1) * (y1 - y2)) -
        ((y1 - other.y1) * (x1 - x2))
    val t = determinant.toDouble() / denominator.toDouble()
    val u = otherDeterminant.toDouble() / denominator.toDouble()
    if (t <= 0.0 || t >= 1.0 || u <= 0.0 || u >= 1.0) return null
    val x = x1 + ((x2 - x1) * t)
    val y = y1 + ((y2 - y1) * t)
    return x.toInt() to y.toInt()
}

private fun List<SvgRenderPoint>.orthogonalized(): List<SvgRenderPoint> {
    if (size < 2) return this
    return zipWithNext()
        .fold(listOf(first())) { acc, (from, to) ->
            if (from.x == to.x || from.y == to.y) {
                acc + to
            } else {
                acc + SvgRenderPoint(from.x, to.y) + to
            }
        }
}

/** Resolves the first path point used as the runtime line start. */
private fun GeometryElement.connectionStart(): GeometryPoint {
    return points.firstOrNull()
        ?: GeometryPoint(
            x = bounds.x,
            y = bounds.y + bounds.height / 2,
        )
}

/** Resolves the last path point used as the runtime line end. */
private fun GeometryElement.connectionEnd(): GeometryPoint {
    return points.lastOrNull()
        ?: GeometryPoint(
            x = bounds.x + bounds.width,
            y = bounds.y + bounds.height / 2,
        )
}
