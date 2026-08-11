package com.engineeringood.athena.language

/** Presentation-only fields accepted by a Style Companion edit transaction. */
data class SheetStyleFields(
    val strokeRgba: String? = null,
    val fillRgba: String? = null,
    val strokeWidth: Int? = null,
    val dash: List<Int>? = null,
    val lineCap: String? = null,
    val lineJoin: String? = null,
    val opacity: Int? = null,
    val fontSize: Int? = null,
    val fontWeight: Int? = null,
    val routeMarker: String? = null,
    val portDisplay: String? = null,
) {
    init {
        require(listOf(strokeRgba, fillRgba, strokeWidth, dash, lineCap, lineJoin, opacity, fontSize, fontWeight, routeMarker, portDisplay).any { it != null }) {
            "Style edit must declare at least one presentation field."
        }
    }
}

/** Deterministically creates or patches one named style block while preserving unrelated blocks. */
class SheetStyleCompanionEditor(
    private val parser: AthenaSheetStyleCompanionParser = AthenaSheetStyleCompanionParser(),
) {
    fun setStyle(existingSource: String?, target: String, fields: SheetStyleFields): String {
        require(target.isNotBlank() && '"' !in target && '\n' !in target && '\r' !in target) {
            "Style target must be a non-blank quoted-name-safe identity."
        }
        val source = existingSource?.takeIf { it.isNotBlank() }
        val existing = source?.let { text ->
            when (val parsed = parser.parse("style-companion", text)) {
                is SheetStyleCompanionParseSuccess -> parsed.source
                is SheetStyleCompanionParseFailure -> throw IllegalArgumentException(parsed.diagnostics.first().message)
            }
        }
        val current = existing?.styles?.firstOrNull { it.name == target }
        val block = render(target, current, fields)
        val result = if (source == null) {
            "$block\n"
        } else {
            val match = STYLE_BLOCK.findAll(source).firstOrNull { candidate ->
                candidate.groupValues[1].ifBlank { candidate.groupValues[2] } == target
            }
            if (match == null) source.trimEnd() + "\n\n$block\n"
            else source.replaceRange(match.range, block).let { it.trimEnd() + "\n" }
        }
        check(parser.parse("style-companion", result) is SheetStyleCompanionParseSuccess) {
            "Generated Style Companion must parse successfully."
        }
        return result
    }

    private fun render(target: String, current: SheetStyleIntent?, update: SheetStyleFields): String = buildString {
        append("style \"").append(target).append("\" {\n")
        field("stroke", update.strokeRgba ?: current?.strokeRgba)
        field("fill", update.fillRgba ?: current?.fillRgba)
        field("width", update.strokeWidth ?: current?.strokeWidth)
        field("dash", (update.dash ?: current?.dash)?.joinToString(prefix = "[", postfix = "]"))
        field("cap", update.lineCap ?: current?.lineCap)
        field("join", update.lineJoin ?: current?.lineJoin)
        field("opacity", update.opacity ?: current?.opacity)
        field("font-size", update.fontSize ?: current?.fontSize)
        field("font-weight", update.fontWeight ?: current?.fontWeight)
        field("route-marker", update.routeMarker ?: current?.routeMarker)
        field("port-display", update.portDisplay ?: current?.portDisplay)
        current?.annotations.orEmpty().forEach { annotation ->
            append("  annotation: ").append(annotation.subjectName).append(' ').append(annotation.displayRole).append('\n')
        }
        append('}')
    }

    private fun StringBuilder.field(name: String, value: Any?) {
        if (value != null) append("  ").append(name).append(": ").append(value).append('\n')
    }

    private companion object {
        val STYLE_BLOCK = Regex("(?ms)^\\s*style\\s+(?:\\\"([^\\\"]+)\\\"|([A-Za-z][A-Za-z0-9_-]*))\\s*\\{.*?^\\s*}")
    }
}
