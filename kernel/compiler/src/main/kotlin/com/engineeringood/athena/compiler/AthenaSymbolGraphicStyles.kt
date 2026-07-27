package com.engineeringood.athena.compiler

import com.engineeringood.athena.representation.GraphicFill
import com.engineeringood.athena.representation.GraphicLineCap
import com.engineeringood.athena.representation.GraphicLineJoin
import com.engineeringood.athena.representation.GraphicPaintToken
import com.engineeringood.athena.representation.GraphicStyleToken
import com.engineeringood.athena.representation.GraphicStyleTokenId
import com.engineeringood.athena.representation.GraphicTextAnchor
import com.engineeringood.athena.representation.GraphicTextBaseline

internal object AthenaSymbolGraphicStyleRegistryV0 {
    private val styles = listOf(
        GraphicStyleToken(
            styleTokenId = GraphicStyleTokenId("conductor"),
            stroke = GraphicPaintToken("drawing.foreground"),
            strokeWidth = 1.5,
            fill = GraphicFill.TRANSPARENT,
            lineCap = GraphicLineCap.ROUND,
            lineJoin = GraphicLineJoin.ROUND,
        ),
        lineStyle("symbol", 1.5),
        lineStyle("terminal", 1.2),
        lineStyle("reference", 1.0, dashPattern = listOf(4.0, 2.0)),
        textStyle("device-label", GraphicTextAnchor.MIDDLE),
        textStyle("terminal-label", GraphicTextAnchor.MIDDLE),
        textStyle("reference-label", GraphicTextAnchor.START),
        textStyle("model-label", GraphicTextAnchor.MIDDLE),
    ).associateBy { style -> style.styleTokenId.value }

    fun resolve(id: String): GraphicStyleToken? = styles[id]

    private fun lineStyle(
        id: String,
        strokeWidth: Double,
        dashPattern: List<Double> = emptyList(),
    ) = GraphicStyleToken(
        styleTokenId = GraphicStyleTokenId(id),
        stroke = GraphicPaintToken("drawing.foreground"),
        strokeWidth = strokeWidth,
        fill = GraphicFill.TRANSPARENT,
        lineCap = GraphicLineCap.ROUND,
        lineJoin = GraphicLineJoin.ROUND,
        dashPattern = dashPattern,
    )

    private fun textStyle(id: String, anchor: GraphicTextAnchor) = GraphicStyleToken(
        styleTokenId = GraphicStyleTokenId(id),
        stroke = GraphicPaintToken("drawing.foreground"),
        strokeWidth = 1.0,
        fill = GraphicFill.FOREGROUND,
        lineCap = GraphicLineCap.BUTT,
        lineJoin = GraphicLineJoin.MITER,
        textAnchor = anchor,
        textBaseline = GraphicTextBaseline.CENTRAL,
    )
}
