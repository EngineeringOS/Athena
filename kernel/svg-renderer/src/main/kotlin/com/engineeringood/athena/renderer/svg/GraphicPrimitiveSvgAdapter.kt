package com.engineeringood.athena.renderer.svg

import com.engineeringood.athena.representation.GraphicFill
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveIrValidator
import com.engineeringood.athena.representation.GraphicStyleToken
import com.engineeringood.athena.representation.GraphicTransform
import java.math.BigDecimal
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

data class GraphicPrimitiveSvgPalette(
    val paintTokens: Map<String, String>,
    val backgroundPaint: String,
)

data class GraphicPrimitiveSvgRenderRequest(
    val document: GraphicPrimitiveDocument,
    val palette: GraphicPrimitiveSvgPalette,
)

data class GraphicPrimitiveSvgDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
)

data class GraphicPrimitiveSvgEvidence(
    val documentId: String,
    val primitiveIds: List<String>,
    val elementCount: Int,
    val normalChromeVisible: Boolean,
)

data class GraphicPrimitiveSvgRenderResult(
    val fragment: String?,
    val evidence: GraphicPrimitiveSvgEvidence?,
    val diagnostics: List<GraphicPrimitiveSvgDiagnostic>,
) {
    val isValid: Boolean
        get() = fragment != null && evidence != null && diagnostics.isEmpty()
}

class GraphicPrimitiveSvgAdapter {
    fun render(request: GraphicPrimitiveSvgRenderRequest): GraphicPrimitiveSvgRenderResult {
        val validationDiagnostics = GraphicPrimitiveIrValidator.validate(request.document).diagnostics.map {
            GraphicPrimitiveSvgDiagnostic(it.code.wireValue, it.subject, it.message)
        }
        if (validationDiagnostics.isNotEmpty()) {
            return failed(validationDiagnostics)
        }

        val primitives = request.document.primitives.flatMap(::flatten)
        val adapterDiagnostics = buildList {
            primitives.filterIsInstance<GraphicPrimitive.Arc>()
                .filter { abs(it.sweepAngleDegrees) > 360.0 }
                .forEach { arc ->
                    add(
                        diagnostic(
                            "drawing.svg.arc.sweep.unsupported",
                            arc.primitiveId.value,
                            "SVG adapter supports a maximum absolute arc sweep of 360 degrees per primitive.",
                        ),
                    )
                }
            val xmlValues = buildList {
                add("documentId" to requireNotNull(request.document.documentId).value)
                primitives.forEach { primitive ->
                    add("primitives.${primitive.primitiveId.value}.primitiveId" to primitive.primitiveId.value)
                    if (primitive is GraphicPrimitive.Text) {
                        add("primitives.${primitive.primitiveId.value}.text" to primitive.text)
                    }
                }
                request.palette.paintTokens.forEach { (token, value) -> add("paintTokens.$token" to value) }
                add("backgroundPaint" to request.palette.backgroundPaint)
            }
            xmlValues.filterNot { (_, value) -> value.isXml10() }.forEach { (subject, _) ->
                add(
                    diagnostic(
                        "drawing.svg.xml.invalid",
                        subject,
                        "SVG text and attribute values must contain only valid XML 1.0 characters.",
                    ),
                )
            }
        }
        if (adapterDiagnostics.isNotEmpty()) {
            return failed(adapterDiagnostics)
        }

        val paletteDiagnostics = buildList {
            request.document.styleTokens.forEach { style ->
                if (request.palette.paintTokens[style.stroke.value].isNullOrBlank()) {
                    add(
                        diagnostic(
                            "drawing.svg.paint.unresolved",
                            style.stroke.value,
                            "SVG adapter requires an explicit non-blank paint value for every Graphic Paint Token.",
                        ),
                    )
                }
                if (style.fill == GraphicFill.BACKGROUND && request.palette.backgroundPaint.isBlank()) {
                    add(
                        diagnostic(
                            "drawing.svg.paint.unresolved",
                            "backgroundPaint",
                            "SVG adapter requires an explicit non-blank background paint for background fill.",
                        ),
                    )
                }
            }
        }
        if (paletteDiagnostics.isNotEmpty()) {
            return failed(paletteDiagnostics)
        }

        val styles = request.document.styleTokens.associateBy { it.styleTokenId }
        val rendered = request.document.primitives.map { primitive -> renderPrimitive(primitive, styles, request.palette) }
        val documentId = requireNotNull(request.document.documentId).value
        val fragment = buildString {
            append("<g data-athena-document-id=\"")
            append(documentId.escapeXml())
            append("\" data-athena-render-authority=\"graphic-primitive-ir\">")
            rendered.forEach { append(it.fragment) }
            append("</g>")
        }
        return GraphicPrimitiveSvgRenderResult(
            fragment = fragment,
            evidence = GraphicPrimitiveSvgEvidence(
                documentId = documentId,
                primitiveIds = rendered.flatMap { it.primitiveIds },
                elementCount = 1 + rendered.sumOf { it.elementCount },
                normalChromeVisible = false,
            ),
            diagnostics = emptyList(),
        )
    }

    private fun renderPrimitive(
        primitive: GraphicPrimitive,
        styles: Map<com.engineeringood.athena.representation.GraphicStyleTokenId, GraphicStyleToken>,
        palette: GraphicPrimitiveSvgPalette,
    ): RenderedPrimitive {
        val trace = trace(primitive)
        val style = primitive.styleTokenId?.let { styleId -> styleAttributes(styles.getValue(styleId), palette) }.orEmpty()
        return when (primitive) {
            is GraphicPrimitive.Line -> rendered(
                primitive,
                "<line$trace x1=\"${number(primitive.start.x)}\" y1=\"${number(primitive.start.y)}\" " +
                    "x2=\"${number(primitive.end.x)}\" y2=\"${number(primitive.end.y)}\"$style />",
            )
            is GraphicPrimitive.Polyline -> rendered(
                primitive,
                "<polyline$trace points=\"${primitive.points.joinToString(" ") { "${number(it.x)},${number(it.y)}" }}\"$style />",
            )
            is GraphicPrimitive.Arc -> {
                val start = polar(primitive.center.x, primitive.center.y, primitive.radius, primitive.startAngleDegrees)
                val segmentCount = ceil(abs(primitive.sweepAngleDegrees) / 180.0).toInt().coerceAtLeast(1)
                val segmentSweep = primitive.sweepAngleDegrees / segmentCount
                val sweep = if (primitive.sweepAngleDegrees >= 0.0) 1 else 0
                val pathData = buildString {
                    append("M ${number(start.first)} ${number(start.second)}")
                    repeat(segmentCount) { index ->
                        val end = polar(
                            primitive.center.x,
                            primitive.center.y,
                            primitive.radius,
                            primitive.startAngleDegrees + segmentSweep * (index + 1),
                        )
                        append(
                            " A ${number(primitive.radius)} ${number(primitive.radius)} 0 0 $sweep " +
                                "${number(end.first)} ${number(end.second)}",
                        )
                    }
                }
                rendered(
                    primitive,
                    "<path$trace d=\"$pathData\"$style />",
                )
            }
            is GraphicPrimitive.Circle -> rendered(
                primitive,
                "<circle$trace cx=\"${number(primitive.center.x)}\" cy=\"${number(primitive.center.y)}\" " +
                    "r=\"${number(primitive.radius)}\"$style />",
            )
            is GraphicPrimitive.Rectangle -> rendered(
                primitive,
                "<rect$trace x=\"${number(primitive.bounds.x)}\" y=\"${number(primitive.bounds.y)}\" " +
                    "width=\"${number(primitive.bounds.width)}\" height=\"${number(primitive.bounds.height)}\" " +
                    "rx=\"${number(primitive.cornerRadius)}\" ry=\"${number(primitive.cornerRadius)}\"$style />",
            )
            is GraphicPrimitive.Text -> rendered(
                primitive,
                "<text$trace x=\"${number(primitive.origin.x)}\" y=\"${number(primitive.origin.y)}\"$style>" +
                    primitive.text.escapeXml() + "</text>",
            )
            is GraphicPrimitive.Marker -> rendered(
                primitive,
                markerFragment(primitive, trace, style),
            )
            is GraphicPrimitive.ConnectionDot -> rendered(
                primitive,
                "<circle$trace cx=\"${number(primitive.center.x)}\" cy=\"${number(primitive.center.y)}\" " +
                    "r=\"${number(primitive.radius)}\"$style />",
            )
            is GraphicPrimitive.ReferenceArrow -> referenceArrow(primitive, trace, style)
            is GraphicPrimitive.Group -> {
                val children = primitive.children.map { renderPrimitive(it, styles, palette) }
                RenderedPrimitive(
                    fragment = "<g$trace>${children.joinToString("") { it.fragment }}</g>",
                    primitiveIds = listOf(primitive.primitiveId.value) + children.flatMap { it.primitiveIds },
                    elementCount = 1 + children.sumOf { it.elementCount },
                )
            }
            is GraphicPrimitive.Transformed -> {
                val child = renderPrimitive(primitive.child, styles, palette)
                RenderedPrimitive(
                    fragment = "<g$trace transform=\"${transform(primitive.transform)}\">${child.fragment}</g>",
                    primitiveIds = listOf(primitive.primitiveId.value) + child.primitiveIds,
                    elementCount = 1 + child.elementCount,
                )
            }
        }
    }

    private fun markerFragment(primitive: GraphicPrimitive.Marker, trace: String, style: String): String =
        when (primitive.markerKind) {
            com.engineeringood.athena.representation.GraphicMarkerKind.TERMINAL ->
                "<circle$trace data-athena-marker-kind=\"terminal\" cx=\"${number(primitive.origin.x)}\" " +
                    "cy=\"${number(primitive.origin.y)}\" r=\"3\"$style />"
            com.engineeringood.athena.representation.GraphicMarkerKind.REFERENCE ->
                "<path$trace data-athena-marker-kind=\"reference\" d=\"M ${number(primitive.origin.x - 4)} " +
                    "${number(primitive.origin.y + 4)} L ${number(primitive.origin.x)} ${number(primitive.origin.y - 4)} " +
                    "L ${number(primitive.origin.x + 4)} ${number(primitive.origin.y + 4)}\"$style />"
            com.engineeringood.athena.representation.GraphicMarkerKind.CROSSING ->
                "<path$trace data-athena-marker-kind=\"crossing\" d=\"M ${number(primitive.origin.x - 4)} " +
                    "${number(primitive.origin.y)} A 4 4 0 0 1 ${number(primitive.origin.x + 4)} ${number(primitive.origin.y)}\"$style />"
        }

    private fun referenceArrow(primitive: GraphicPrimitive.ReferenceArrow, trace: String, style: String): RenderedPrimitive {
        val angle = atan2(primitive.end.y - primitive.start.y, primitive.end.x - primitive.start.x)
        val left = pointBehind(primitive.end.x, primitive.end.y, angle - PI / 6.0, primitive.headSize)
        val right = pointBehind(primitive.end.x, primitive.end.y, angle + PI / 6.0, primitive.headSize)
        val fragment = "<g$trace><line x1=\"${number(primitive.start.x)}\" y1=\"${number(primitive.start.y)}\" " +
            "x2=\"${number(primitive.end.x)}\" y2=\"${number(primitive.end.y)}\"$style />" +
            "<polyline points=\"${number(left.first)},${number(left.second)} ${number(primitive.end.x)},${number(primitive.end.y)} " +
            "${number(right.first)},${number(right.second)}\"$style /></g>"
        return RenderedPrimitive(fragment, listOf(primitive.primitiveId.value), 3)
    }

    private fun styleAttributes(style: GraphicStyleToken, palette: GraphicPrimitiveSvgPalette): String {
        val paint = requireNotNull(palette.paintTokens[style.stroke.value])
        val fill = when (style.fill) {
            GraphicFill.TRANSPARENT -> "none"
            GraphicFill.BACKGROUND -> palette.backgroundPaint
            GraphicFill.FOREGROUND -> paint
        }
        return buildString {
            append(" stroke=\"").append(paint.escapeXml()).append('"')
            append(" stroke-width=\"").append(number(style.strokeWidth)).append('"')
            append(" stroke-linecap=\"").append(style.lineCap.name.lowercase()).append('"')
            append(" stroke-linejoin=\"").append(style.lineJoin.name.lowercase()).append('"')
            if (style.dashPattern.isNotEmpty()) {
                append(" stroke-dasharray=\"").append(style.dashPattern.joinToString(" ", transform = ::number)).append('"')
            }
            append(" fill=\"").append(fill.escapeXml()).append('"')
            append(" text-anchor=\"").append(style.textAnchor.name.lowercase()).append('"')
            append(" dominant-baseline=\"").append(style.textBaseline.name.lowercase()).append('"')
            append(" vector-effect=\"non-scaling-stroke\"")
        }
    }

    private fun trace(primitive: GraphicPrimitive): String =
        " data-athena-primitive-id=\"${primitive.primitiveId.value.escapeXml()}\" " +
            "data-athena-primitive-kind=\"${primitive.kind.wireValue}\""

    private fun transform(transform: GraphicTransform): String = when (transform) {
        is GraphicTransform.Translation -> "translate(${number(transform.dx)} ${number(transform.dy)})"
        is GraphicTransform.Rotation ->
            "rotate(${number(transform.angleDegrees)} ${number(transform.pivot.x)} ${number(transform.pivot.y)})"
        is GraphicTransform.Scale ->
            "translate(${number(transform.pivot.x)} ${number(transform.pivot.y)}) scale(${number(transform.x)} ${number(transform.y)}) " +
                "translate(${number(-transform.pivot.x)} ${number(-transform.pivot.y)})"
    }

    private fun rendered(primitive: GraphicPrimitive, fragment: String) =
        RenderedPrimitive(fragment, listOf(primitive.primitiveId.value), 1)

    private fun flatten(primitive: GraphicPrimitive): List<GraphicPrimitive> = when (primitive) {
        is GraphicPrimitive.Group -> listOf(primitive) + primitive.children.flatMap(::flatten)
        is GraphicPrimitive.Transformed -> listOf(primitive) + flatten(primitive.child)
        else -> listOf(primitive)
    }

    private fun failed(diagnostics: List<GraphicPrimitiveSvgDiagnostic>) = GraphicPrimitiveSvgRenderResult(
        fragment = null,
        evidence = null,
        diagnostics = diagnostics.distinct().sortedWith(compareBy({ it.code }, { it.subject }, { it.message })),
    )

    private fun diagnostic(code: String, subject: String, message: String) =
        GraphicPrimitiveSvgDiagnostic(code, subject, message)

    private fun polar(cx: Double, cy: Double, radius: Double, degrees: Double): Pair<Double, Double> {
        val radians = degrees * PI / 180.0
        return (cx + radius * cos(radians)) to (cy + radius * sin(radians))
    }

    private fun pointBehind(x: Double, y: Double, angle: Double, distance: Double): Pair<Double, Double> =
        (x - distance * cos(angle)) to (y - distance * sin(angle))

    private fun number(value: Double): String = when {
        value == 0.0 -> "0"
        else -> BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
    }

    private fun String.escapeXml(): String = buildString(length) {
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

    private fun String.isXml10(): Boolean = codePoints().allMatch { codePoint ->
        codePoint == 0x9 || codePoint == 0xA || codePoint == 0xD ||
            codePoint in 0x20..0xD7FF || codePoint in 0xE000..0xFFFD || codePoint in 0x10000..0x10FFFF
    }

    private data class RenderedPrimitive(
        val fragment: String,
        val primitiveIds: List<String>,
        val elementCount: Int,
    )
}
