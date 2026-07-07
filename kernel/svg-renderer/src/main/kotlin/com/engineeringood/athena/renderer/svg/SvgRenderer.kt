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
            connections = geometry.elements
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
                    )
                },
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
                    """  <line x1="${connection.x1}" y1="${connection.y1}" x2="${connection.x2}" y2="${connection.y2}" class="connection" />""",
                )
            }
            model.boxes.forEach { box ->
                appendLine(
                    """  <rect x="${box.x}" y="${box.y}" width="${box.width}" height="${box.height}" rx="8" ry="8" class="component" />""",
                )
                appendLine("""  <text x="${box.x + 12}" y="${box.y + 28}" class="label">${box.label.escapeXml()}</text>""")
            }
            append("""</svg>""")
        }
    }
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
