package com.engineeringood.athena.renderer.svg

import com.engineeringood.athena.geometry.GeometryElementKind
import com.engineeringood.athena.geometry.GeometryDocument
import com.engineeringood.athena.geometry.GeometryElement
import com.engineeringood.athena.geometry.GeometryPoint

/** Emits deterministic simple SVG from either a thin scene model or explicit `Geometry IR`. */
class SvgRenderer {
    /**
     * Renders [geometry] into a stable SVG string from the explicit geometry stage without recovering semantics.
     */
    fun render(
        systemName: String,
        geometry: GeometryDocument,
    ): String {
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
                        .map { point -> SvgRenderPoint(point.x, point.y) },
                )
            }
        val model = SvgRenderModel(
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
        return render(model)
    }

    /** Renders [model] into a stable SVG string without recovering or inventing semantic meaning. */
    fun render(model: SvgRenderModel): String {
        return buildString {
            appendLine(
                """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${model.canvasWidth} ${model.canvasHeight}" width="${model.canvasWidth}" height="${model.canvasHeight}">""",
            )
            appendLine("""  <title>${model.systemName.escapeXml()}</title>""")
            appendLine("""  <text x="40" y="28" class="system-label">${model.systemName.escapeXml()}</text>""")
            model.connections.forEach { connection ->
                appendLine(
                    """  <polyline points="${connection.points.svgPoints()}" class="connection" data-connection-id="${connection.semanticId.value.escapeXml()}" />""",
                )
            }
            model.crossings.forEach { crossing ->
                appendLine(
                    """  <path data-athena-marker-kind="crossing" data-crossing-id="${crossing.crossingId.escapeXml()}" d="M ${crossing.x - 4} ${crossing.y - 4} L ${crossing.x + 4} ${crossing.y + 4} M ${crossing.x + 4} ${crossing.y - 4} L ${crossing.x - 4} ${crossing.y + 4}" class="route-crossing" />""",
                )
            }
            model.boxes.forEach { box ->
                appendLine(
                    """  <rect x="${box.x}" y="${box.y}" width="${box.width}" height="${box.height}" rx="8" ry="8" class="component" data-subject="${box.semanticId.value.escapeXml()}" />""",
                )
                appendLine("""  <text x="${box.x + 12}" y="${box.y + 28}" class="label">${box.label.escapeXml()}</text>""")
            }
            append("""</svg>""")
        }
    }
}

private fun List<SvgRenderPoint>.svgPoints(): String =
    joinToString(separator = " ") { point -> "${point.x},${point.y}" }

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

/** Resolves the first path point used as the SVG line start. */
private fun GeometryElement.connectionStart(): GeometryPoint {
    return points.firstOrNull()
        ?: GeometryPoint(
            x = bounds.x,
            y = bounds.y + bounds.height / 2,
        )
}

/** Resolves the last path point used as the SVG line end. */
private fun GeometryElement.connectionEnd(): GeometryPoint {
    return points.lastOrNull()
        ?: GeometryPoint(
            x = bounds.x + bounds.width,
            y = bounds.y + bounds.height / 2,
        )
}

/** Escapes plain text for simple XML element content. */
private fun String.escapeXml(): String {
    return buildString(length) {
        this@escapeXml.forEach { character ->
            append(
                when (character) {
                    '&' -> "&amp;"
                    '<' -> "&lt;"
                    '>' -> "&gt;"
                    '"' -> "&quot;"
                    '\'' -> "&apos;"
                    else -> character
                },
            )
        }
    }
}
