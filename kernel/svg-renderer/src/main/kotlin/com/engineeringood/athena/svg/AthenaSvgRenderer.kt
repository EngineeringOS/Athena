package com.engineeringood.athena.svg

import com.engineeringood.athena.presentation.AssetBundle
import com.engineeringood.athena.presentation.AssetMediaKind
import com.engineeringood.athena.presentation.AthenaDiagramScene
import com.engineeringood.athena.presentation.AthenaDiagramSceneContract
import com.engineeringood.athena.presentation.DecorationKind
import com.engineeringood.athena.presentation.ResolvedStyle
import com.engineeringood.athena.presentation.SceneAsset
import com.engineeringood.athena.presentation.SceneBounds
import com.engineeringood.athena.presentation.SceneDecoration
import com.engineeringood.athena.presentation.SceneLabel
import com.engineeringood.athena.presentation.SceneOccurrence
import com.engineeringood.athena.presentation.ScenePoint
import com.engineeringood.athena.presentation.SceneConnection
import com.engineeringood.athena.presentation.SceneConnectionMarkerKind
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Base64

/** Deterministic renderer-neutral SVG oracle. It consumes only a complete Presentation publication. */
object AthenaSvgRenderer {
    fun render(scene: AthenaDiagramScene, assetBundle: AssetBundle): String {
        AthenaDiagramSceneContract.validate(scene, assetBundle)
        val canonicalScene = AthenaDiagramSceneContract.canonicalize(scene)
        AthenaDiagramSceneContract.validate(canonicalScene, assetBundle)
        return SvgSceneSerializer(canonicalScene, assetBundle).serialize()
    }
}

private class SvgSceneSerializer(
    private val scene: AthenaDiagramScene,
    assetBundle: AssetBundle,
) {
    private val styles = scene.styles.associateBy { it.styleId }
    private val assets = scene.assets.associateBy { it.assetId }
    private val bundleEntries = assetBundle.entries.associateBy { it.entryId }
    private val paintedSharedSegments = mutableSetOf<String>()

    fun serialize(): String = buildString {
        append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"")
        append(boundsViewBox(scene.page.pageBounds))
        append("\">")
        appendEmbeddedFonts()
        paintElements().forEach { element -> element.write(this) }
        append("</svg>")
    }

    private fun StringBuilder.appendEmbeddedFonts() {
        val fontAssets = scene.styles.mapNotNull { style -> style.fontAssetId?.let { assets.getValue(it) } }
            .distinctBy { it.assetId }
            .sortedBy { it.assetId.value }
        if (fontAssets.isEmpty()) return
        append("<style>")
        fontAssets.forEach { asset ->
            require(asset.mediaKind == AssetMediaKind.WOFF2)
            val bytes = bundleEntries.getValue(asset.bundleEntryId).bytes
            append("@font-face{font-family:")
            append(fontFamily(asset))
            append(";src:url(data:font/woff2;base64,")
            append(Base64.getEncoder().encodeToString(bytes))
            append(") format('woff2');}")
        }
        append("</style>")
    }

    private fun paintElements(): List<SvgPaintElement> = buildList {
        scene.decorations.forEach { decoration ->
            add(SvgPaintElement(decoration.zIndex, decorationRank(decoration.kind), decoration.elementId.value) { target -> target.appendDecoration(decoration) })
        }
        scene.connections.forEach { connection ->
            add(SvgPaintElement(connection.zIndex, ROUTE_RANK, connection.elementId.value) { target -> target.appendConnection(connection) })
            connection.annotations.forEach { annotation ->
                add(SvgPaintElement(connection.zIndex, CONNECTION_ANNOTATION_RANK, annotation.elementId.value) { target -> target.appendText(annotation.value, annotation.bounds, 0, styles.getValue(connection.styleId), annotation.anchor) })
            }
        }
        scene.occurrences.forEach { occurrence ->
            add(SvgPaintElement(occurrence.zIndex, OCCURRENCE_RANK, occurrence.elementId.value) { target -> target.appendOccurrence(occurrence) })
            occurrence.labels.forEach { label ->
                add(SvgPaintElement(occurrence.zIndex, LABEL_RANK, label.elementId.value) { target -> target.appendLabel(label) })
            }
        }
    }.sortedWith(compareBy<SvgPaintElement>({ it.zIndex }, { it.kindRank }, { it.elementId }))

    private fun StringBuilder.appendDecoration(decoration: SceneDecoration) {
        val style = styles.getValue(decoration.styleId)
        when (decoration.kind) {
            DecorationKind.PAGE_BACKGROUND -> element("rect", fillAttributes(style) + boundsAttributes(decoration.bounds))
            DecorationKind.FRAME_SEGMENT -> element(
                "line",
                strokeAttributes(style) + listOf("shape-rendering" to "crispEdges") + frameSegmentAttributes(decoration.bounds),
            )
            DecorationKind.COORDINATE_LABEL -> decoration.text?.let { text -> appendText(text, decoration.bounds, 0, style) }
        }
    }

    private fun StringBuilder.appendConnection(connection: SceneConnection) {
        val style = styles.getValue(connection.styleId)
        connection.segments.forEach { segment ->
            if (segment.kind.name == "SHARED" && !paintedSharedSegments.add(segment.segmentId)) return@forEach
            val points = "${segment.start.x},${segment.start.y} ${segment.end.x},${segment.end.y}"
            element("polyline", listOf("fill" to "none", "points" to points) + strokeAttributes(style))
        }
        connection.markers.sortedWith(compareBy({ it.kind.ordinal }, { it.point.x }, { it.point.y }, { it.elementId.value })).forEach { marker ->
            when (marker.kind) {
                SceneConnectionMarkerKind.JUNCTION -> element("circle", listOf("cx" to marker.point.x.toString(), "cy" to marker.point.y.toString(), "r" to "1", "fill" to strokeColor(style)))
                SceneConnectionMarkerKind.CROSSING -> if (marker.bridgeOwner) {
                    element("path", listOf("d" to "M${marker.point.x - 2},${marker.point.y} Q${marker.point.x},${marker.point.y - 2} ${marker.point.x + 2},${marker.point.y}", "fill" to "none") + strokeAttributes(style))
                }
                SceneConnectionMarkerKind.INTERRUPTION_START, SceneConnectionMarkerKind.INTERRUPTION_END -> element("circle", listOf("cx" to marker.point.x.toString(), "cy" to marker.point.y.toString(), "r" to "0.5", "fill" to strokeColor(style)))
            }
        }
    }

    private fun StringBuilder.appendOccurrence(occurrence: SceneOccurrence) {
        val assetId = requireNotNull(occurrence.assetId) {
            "Occurrence `${occurrence.occurrenceId}` must reference an admitted asset."
        }
        val asset = requireNotNull(assets[assetId]) {
            "Occurrence `${occurrence.occurrenceId}` references unavailable admitted asset `${assetId.value}`."
        }
        require(asset.mediaKind == AssetMediaKind.SVG || asset.mediaKind == AssetMediaKind.PNG) {
            "Only visual SVG or PNG assets may render an occurrence."
        }
        val mimeType = when (asset.mediaKind) {
            AssetMediaKind.SVG -> "image/svg+xml"
            AssetMediaKind.PNG -> "image/png"
            AssetMediaKind.WOFF2 -> error("WOFF2 cannot render an occurrence.")
        }
        val encoded = Base64.getEncoder().encodeToString(bundleEntries.getValue(asset.bundleEntryId).bytes)
        element(
            "image",
            listOf(
                "height" to occurrence.bounds.height.toString(),
                "href" to "data:$mimeType;base64,$encoded",
                "preserveAspectRatio" to "xMidYMid meet",
                "width" to occurrence.bounds.width.toString(),
                "x" to occurrence.bounds.x.toString(),
                "y" to occurrence.bounds.y.toString(),
            ),
        )
    }

    private fun StringBuilder.appendLabel(label: SceneLabel) {
        appendText(label.text, label.bounds, label.rotationDegrees, styles.getValue(label.styleId), label.anchor)
    }

    private fun StringBuilder.appendText(
        text: String,
        bounds: SceneBounds,
        rotationDegrees: Int,
        style: ResolvedStyle,
        anchor: ScenePoint = ScenePoint(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2),
    ) {
        val attributes = buildList<Pair<String, String>> {
            add("fill" to strokeColor(style))
            opacity(style)?.let { add("opacity" to it) }
            style.fontAssetId?.let { add("font-family" to fontFamily(assets.getValue(it))) }
            add("font-size" to style.fontSize.toString())
            add("font-weight" to style.fontWeight.toString())
            add("text-anchor" to textAnchor(style))
            add("dominant-baseline" to textBaseline(style))
            if (rotationDegrees != 0) add("transform" to "rotate($rotationDegrees ${anchor.x} ${anchor.y})")
            add("x" to anchor.x.toString())
            add("y" to anchor.y.toString())
        }
        element("text", attributes, text)
    }

    private fun StringBuilder.element(name: String, attributes: List<Pair<String, String>>, text: String? = null) {
        append('<').append(name)
        attributes.forEach { (key, value) ->
            append(' ').append(key).append("=\"").append(escape(value)).append('\"')
        }
        if (text == null) {
            append("/>")
        } else {
            append('>').append(escape(text)).append("</").append(name).append('>')
        }
    }

    private fun fillAttributes(style: ResolvedStyle): List<Pair<String, String>> = buildList {
        add("fill" to fillColor(style))
        opacity(style)?.let { add("opacity" to it) }
    }

    private fun fillAndStrokeAttributes(style: ResolvedStyle): List<Pair<String, String>> = fillAttributes(style) + strokeAttributes(style)

    private fun strokeAttributes(style: ResolvedStyle): List<Pair<String, String>> = buildList {
        add("stroke" to strokeColor(style))
        add("stroke-linecap" to style.lineCap.name.lowercase())
        add("stroke-linejoin" to style.lineJoin.name.lowercase())
        add("stroke-width" to style.strokeWidth.toString())
        add("vector-effect" to "non-scaling-stroke")
        if (style.dash.isNotEmpty()) add("stroke-dasharray" to style.dash.joinToString(" "))
    }

    private fun fillColor(style: ResolvedStyle): String = style.fillRgba.take(7)

    private fun strokeColor(style: ResolvedStyle): String = style.strokeRgba.take(7)

    private fun opacity(style: ResolvedStyle): String? {
        val fillAlpha = style.fillRgba.drop(7).toInt(16)
        val numerator = fillAlpha * style.opacity
        if (numerator == 255 * 255) return null
        return BigDecimal(numerator).divide(BigDecimal(255 * 255), 6, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    }

    private fun fontFamily(asset: SceneAsset): String = "athena-font-${asset.assetId.value.removePrefix("asset:sha256:").take(16)}"

    private fun textAnchor(style: ResolvedStyle): String = when (style.textAlign.name) {
        "START" -> "start"
        "MIDDLE" -> "middle"
        "END" -> "end"
        else -> error("Unknown text alignment.")
    }

    private fun textBaseline(style: ResolvedStyle): String = when (style.textBaseline.name) {
        "ALPHABETIC" -> "alphabetic"
        "MIDDLE" -> "middle"
        "HANGING" -> "hanging"
        else -> error("Unknown text baseline.")
    }

    private fun boundsViewBox(bounds: SceneBounds): String = "${bounds.x} ${bounds.y} ${bounds.width} ${bounds.height}"

    private fun boundsAttributes(bounds: SceneBounds): List<Pair<String, String>> = listOf(
        "height" to bounds.height.toString(),
        "width" to bounds.width.toString(),
        "x" to bounds.x.toString(),
        "y" to bounds.y.toString(),
    )

    private fun frameSegmentAttributes(bounds: SceneBounds): List<Pair<String, String>> {
        require(bounds.width == 1 || bounds.height == 1) { "Frame segment must be one scene unit thick." }
        val horizontal = bounds.height == 1
        val x1 = bounds.x + 0.5
        val x2 = if (horizontal) bounds.x + bounds.width - 0.5 else x1
        val y1 = bounds.y + 0.5
        val y2 = if (horizontal) y1 else bounds.y + bounds.height - 0.5
        return listOf(
            "x1" to x1.toString(),
            "x2" to x2.toString(),
            "y1" to y1.toString(),
            "y2" to y2.toString(),
        )
    }

    private fun decorationRank(kind: DecorationKind): Int = when (kind) {
        DecorationKind.PAGE_BACKGROUND -> PAGE_BACKGROUND_RANK
        DecorationKind.FRAME_SEGMENT -> FRAME_RANK
        DecorationKind.COORDINATE_LABEL -> COORDINATE_LABEL_RANK
    }

    private fun escape(value: String): String = value
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

    private data class SvgPaintElement(
        val zIndex: Int,
        val kindRank: Int,
        val elementId: String,
        val write: (StringBuilder) -> Unit,
    )

    private companion object {
        const val PAGE_BACKGROUND_RANK = 0
        const val FRAME_RANK = 1
        const val ROUTE_RANK = 2
        const val OCCURRENCE_RANK = 3
        const val LABEL_RANK = 5
        const val CONNECTION_ANNOTATION_RANK = 5
        const val COORDINATE_LABEL_RANK = 6
    }
}
