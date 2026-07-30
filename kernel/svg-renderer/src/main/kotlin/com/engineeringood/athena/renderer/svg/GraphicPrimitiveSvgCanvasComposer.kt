package com.engineeringood.athena.renderer.svg

import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveIrValidator
import java.math.BigDecimal

data class GraphicPrimitiveSvgSafetyFacts(
    val duplicateLabelOccurrenceIds: List<String> = emptyList(),
    val offscreenOccurrenceIds: List<String> = emptyList(),
    val centerFallbackRouteIds: List<String> = emptyList(),
    val genericFallbackOccurrenceIds: List<String> = emptyList(),
)

data class GraphicPrimitiveSvgCanvasRequest(
    val document: GraphicPrimitiveDocument,
    val fragment: GraphicPrimitiveSvgRenderResult,
    val margin: Double,
    val safetyFacts: GraphicPrimitiveSvgSafetyFacts = GraphicPrimitiveSvgSafetyFacts(),
)

data class GraphicPrimitiveSvgCanvasDiagnostic(
    val code: String,
    val authority: String,
    val subject: String,
    val message: String,
)

data class GraphicPrimitiveSvgCanvasEvidence(
    val contentBounds: GraphicBounds,
    val boundsAuthority: String,
    val margin: Double,
    val marginPolicy: String,
    val viewBox: GraphicBounds,
)

data class GraphicPrimitiveSvgCanvasResult(
    val svg: String?,
    val evidence: GraphicPrimitiveSvgCanvasEvidence?,
    val diagnostics: List<GraphicPrimitiveSvgCanvasDiagnostic>,
) {
    val isValid: Boolean
        get() = svg != null && evidence != null && diagnostics.isEmpty()
}

class GraphicPrimitiveSvgCanvasComposer {
    fun compose(request: GraphicPrimitiveSvgCanvasRequest): GraphicPrimitiveSvgCanvasResult {
        val diagnostics = mutableListOf<GraphicPrimitiveSvgCanvasDiagnostic>()
        diagnostics += GraphicPrimitiveIrValidator.validate(request.document).diagnostics.map {
            diagnostic(it.code.wireValue, "graphic-primitive-ir", it.subject, it.message)
        }
        if (!request.margin.isFinite() || request.margin < 0.0) {
            diagnostics += diagnostic(
                "drawing.svg.margin.invalid",
                "presentation-policy",
                "margin",
                "SVG canvas margin must be finite and non-negative.",
            )
        }
        val documentId = request.document.documentId?.value
        if (!request.fragment.isValid || request.fragment.evidence?.documentId != documentId) {
            diagnostics += diagnostic(
                "drawing.svg.fragment.invalid",
                "svg-adapter",
                documentId.orEmpty(),
                "Root SVG composition requires one valid fragment for the same Graphic Primitive document.",
            )
        }
        diagnostics += safetyDiagnostics(request.safetyFacts)
        if (diagnostics.isNotEmpty()) {
            return failed(diagnostics)
        }

        val bounds = requireNotNull(request.document.bounds)
        val viewBox = GraphicBounds(
            x = bounds.x - request.margin,
            y = bounds.y - request.margin,
            width = bounds.width + request.margin * 2.0,
            height = bounds.height + request.margin * 2.0,
        )
        if (!viewBox.isValidViewBox()) {
            return failed(
                listOf(
                    diagnostic(
                        "drawing.svg.viewbox.invalid",
                        "presentation-policy",
                        documentId.orEmpty(),
                        "Derived SVG viewBox must remain finite with positive width and height.",
                    ),
                ),
            )
        }
        val svg = buildString {
            append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"")
            append(listOf(viewBox.x, viewBox.y, viewBox.width, viewBox.height).joinToString(" ", transform = ::number))
            append("\">")
            append(requireNotNull(request.fragment.fragment))
            append("</svg>")
        }
        return GraphicPrimitiveSvgCanvasResult(
            svg = svg,
            evidence = GraphicPrimitiveSvgCanvasEvidence(
                contentBounds = bounds,
                boundsAuthority = "graphic-primitive-ir",
                margin = request.margin,
                marginPolicy = "governed-explicit-margin",
                viewBox = viewBox,
            ),
            diagnostics = emptyList(),
        )
    }

    private fun safetyDiagnostics(facts: GraphicPrimitiveSvgSafetyFacts): List<GraphicPrimitiveSvgCanvasDiagnostic> =
        buildList {
            facts.duplicateLabelOccurrenceIds.forEach { subject ->
                add(diagnostic("drawing.svg.safety.duplicate-label", "presentation", subject, "Duplicate label occurrence blocks SVG output."))
            }
            facts.offscreenOccurrenceIds.forEach { subject ->
                add(diagnostic("drawing.svg.safety.offscreen-occurrence", "presentation", subject, "Off-screen occurrence blocks SVG output."))
            }
            facts.centerFallbackRouteIds.forEach { subject ->
                add(diagnostic("drawing.svg.safety.center-fallback-route", "spatial-routing", subject, "Center-fallback route blocks SVG output."))
            }
            facts.genericFallbackOccurrenceIds.forEach { subject ->
                add(diagnostic("drawing.svg.safety.generic-fallback", "representation-binding", subject, "Generic fallback occurrence blocks SVG output."))
            }
        }

    private fun failed(diagnostics: List<GraphicPrimitiveSvgCanvasDiagnostic>) = GraphicPrimitiveSvgCanvasResult(
        svg = null,
        evidence = null,
        diagnostics = diagnostics.distinct().sortedWith(compareBy({ it.code }, { it.authority }, { it.subject }, { it.message })),
    )

    private fun diagnostic(code: String, authority: String, subject: String, message: String) =
        GraphicPrimitiveSvgCanvasDiagnostic(code, authority, subject, message)

    private fun number(value: Double): String = when {
        value == 0.0 -> "0"
        else -> BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
    }
}

private fun GraphicBounds.isValidViewBox(): Boolean =
    x.isFinite() && y.isFinite() && width.isFinite() && width > 0.0 && height.isFinite() && height > 0.0
