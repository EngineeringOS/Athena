package com.engineeringood.athena.renderer.svg

/** Emits a deterministic simple SVG from the thin renderer-facing model derived by the compiler. */
class SvgRenderer {
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
