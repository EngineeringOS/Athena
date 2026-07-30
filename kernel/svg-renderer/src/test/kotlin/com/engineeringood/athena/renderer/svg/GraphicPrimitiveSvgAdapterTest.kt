package com.engineeringood.athena.renderer.svg

import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicFill
import com.engineeringood.athena.representation.GraphicLineCap
import com.engineeringood.athena.representation.GraphicLineJoin
import com.engineeringood.athena.representation.GraphicMarkerKind
import com.engineeringood.athena.representation.GraphicPaintToken
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicStyleToken
import com.engineeringood.athena.representation.GraphicStyleTokenId
import com.engineeringood.athena.representation.GraphicTextAnchor
import com.engineeringood.athena.representation.GraphicTextBaseline
import com.engineeringood.athena.representation.GraphicTransform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GraphicPrimitiveSvgAdapterTest {
    @Test
    fun `adapter renders every primitive with deterministic style transform and traceability`() {
        val result = GraphicPrimitiveSvgAdapter().render(request(document()))

        assertTrue(result.isValid, result.diagnostics.toString())
        val fragment = requireNotNull(result.fragment)
        assertTrue(fragment.startsWith("<g data-athena-document-id=\"descriptor.test\""))
        assertTrue(fragment.contains("<line"))
        assertTrue(fragment.contains("<polyline"))
        assertTrue(fragment.contains("<path data-athena-primitive-id=\"arc\""))
        assertTrue(fragment.contains("<circle"))
        assertTrue(fragment.contains("<rect"))
        assertTrue(fragment.contains("&lt;M1 &amp; M2&gt;"))
        assertTrue(fragment.contains("data-athena-marker-kind=\"terminal\""))
        assertTrue(fragment.contains("data-athena-primitive-kind=\"connection-dot\""))
        assertTrue(fragment.contains("data-athena-primitive-kind=\"reference-arrow\""))
        assertTrue(fragment.contains("transform=\"translate(5 6)\""))
        assertTrue(fragment.contains("transform=\"rotate(90 110 110)\""))
        assertTrue(fragment.contains("transform=\"translate(150 110) scale(2 0.5) translate(-150 -110)\""))
        assertTrue(fragment.contains("stroke=\"#202020\""))
        assertTrue(fragment.contains("stroke-width=\"1.5\""))
        assertTrue(fragment.contains("stroke-linecap=\"round\""))
        assertTrue(fragment.contains("stroke-linejoin=\"bevel\""))
        assertTrue(fragment.contains("stroke-dasharray=\"4 2\""))
        assertTrue(fragment.contains("text-anchor=\"middle\""))
        assertTrue(fragment.contains("dominant-baseline=\"central\""))
        assertFalse(fragment.contains("<svg"))
        assertFalse(fragment.contains("viewBox"))
        assertFalse(fragment.contains(" class="))
        assertFalse(Regex("\\sid=").containsMatchIn(fragment))
        assertFalse(fragment.contains("hitbox", ignoreCase = true))
        assertEquals(
            listOf(
                "line", "polyline", "arc", "circle", "rect", "text", "marker", "dot", "arrow",
                "group", "group-line", "moved", "moved-circle", "rotated", "rotated-line", "scaled", "scaled-circle",
            ),
            result.evidence?.primitiveIds,
        )
        assertEquals(result, GraphicPrimitiveSvgAdapter().render(request(document())))
    }

    @Test
    fun `adapter fails closed for invalid IR and unresolved paint without partial SVG`() {
        val invalid = GraphicPrimitiveSvgAdapter().render(request(document().copy(styleTokens = emptyList())))
        val unresolved = GraphicPrimitiveSvgAdapter().render(
            request(document()).copy(palette = GraphicPrimitiveSvgPalette(emptyMap(), "#ffffff")),
        )
        val nonFinite = GraphicPrimitiveSvgAdapter().render(
            request(document().copy(bounds = GraphicBounds(0.0, 0.0, Double.NaN, 200.0))),
        )

        assertFalse(invalid.isValid)
        assertNull(invalid.fragment)
        assertNull(invalid.evidence)
        assertTrue(invalid.diagnostics.any { it.code == "graphic.ir.style-token.missing" })
        assertFalse(unresolved.isValid)
        assertNull(unresolved.fragment)
        assertNull(unresolved.evidence)
        assertEquals(listOf("drawing.svg.paint.unresolved"), unresolved.diagnostics.map { it.code })
        assertFalse(nonFinite.isValid)
        assertNull(nonFinite.fragment)
        assertTrue(nonFinite.diagnostics.any { it.code == "graphic.ir.bounds.invalid" })
    }

    @Test
    fun `adapter splits a full circle arc so SVG does not collapse equal endpoints`() {
        val base = document()
        val fullCircle = base.copy(
            primitives = listOf(
                GraphicPrimitive.Arc(
                    id("full-circle"),
                    GraphicBounds(30.0, 30.0, 20.0, 20.0),
                    point(40, 40),
                    10.0,
                    0.0,
                    360.0,
                    lineStyleId,
                ),
            ),
        )

        val result = GraphicPrimitiveSvgAdapter().render(request(fullCircle))

        assertTrue(result.isValid, result.diagnostics.toString())
        assertEquals(2, Regex(" A ").findAll(requireNotNull(result.fragment)).count())
    }

    @Test
    fun `adapter rejects unbounded arc work and invalid XML characters`() {
        val base = document()
        val unboundedArc = base.copy(
            primitives = listOf(
                GraphicPrimitive.Arc(
                    id("unbounded"),
                    GraphicBounds(30.0, 30.0, 20.0, 20.0),
                    point(40, 40),
                    10.0,
                    0.0,
                    720.0,
                    lineStyleId,
                ),
            ),
        )
        val invalidXml = base.copy(
            primitives = listOf(
                GraphicPrimitive.Text(
                    id("invalid-xml"),
                    GraphicBounds(10.0, 60.0, 80.0, 20.0),
                    point(15, 75),
                    "bad\u0001text",
                    textStyleId,
                ),
            ),
        )

        val arcResult = GraphicPrimitiveSvgAdapter().render(request(unboundedArc))
        val xmlResult = GraphicPrimitiveSvgAdapter().render(request(invalidXml))

        assertFalse(arcResult.isValid)
        assertNull(arcResult.fragment)
        assertEquals(listOf("drawing.svg.arc.sweep.unsupported"), arcResult.diagnostics.map { it.code })
        assertFalse(xmlResult.isValid)
        assertNull(xmlResult.fragment)
        assertEquals(listOf("drawing.svg.xml.invalid"), xmlResult.diagnostics.map { it.code })
    }

    private fun request(document: GraphicPrimitiveDocument) = GraphicPrimitiveSvgRenderRequest(
        document = document,
        palette = GraphicPrimitiveSvgPalette(
            paintTokens = mapOf("drawing.foreground" to "#202020"),
            backgroundPaint = "#ffffff",
        ),
    )

    private fun document(): GraphicPrimitiveDocument = GraphicPrimitiveDocument(
        documentId = GraphicPrimitiveDocumentId("descriptor.test"),
        bounds = GraphicBounds(0.0, 0.0, 200.0, 200.0),
        primitives = listOf(
            GraphicPrimitive.Line(id("line"), GraphicBounds(10.0, 10.0, 20.0, 1.0), point(10, 10), point(30, 10), lineStyleId),
            GraphicPrimitive.Polyline(id("polyline"), GraphicBounds(10.0, 20.0, 20.0, 10.0), listOf(point(10, 20), point(20, 30), point(30, 20)), lineStyleId),
            GraphicPrimitive.Arc(id("arc"), GraphicBounds(30.0, 30.0, 20.0, 20.0), point(40, 40), 10.0, 0.0, 90.0, lineStyleId),
            GraphicPrimitive.Circle(id("circle"), GraphicBounds(60.0, 30.0, 20.0, 20.0), point(70, 40), 10.0, lineStyleId),
            GraphicPrimitive.Rectangle(id("rect"), GraphicBounds(90.0, 30.0, 30.0, 20.0), 2.0, lineStyleId),
            GraphicPrimitive.Text(id("text"), GraphicBounds(10.0, 60.0, 80.0, 20.0), point(15, 75), "<M1 & M2>", textStyleId),
            GraphicPrimitive.Marker(id("marker"), GraphicBounds(100.0, 60.0, 10.0, 10.0), point(105, 65), GraphicMarkerKind.TERMINAL, lineStyleId),
            GraphicPrimitive.ConnectionDot(id("dot"), GraphicBounds(120.0, 60.0, 8.0, 8.0), point(124, 64), 4.0, solidStyleId),
            GraphicPrimitive.ReferenceArrow(id("arrow"), GraphicBounds(140.0, 60.0, 30.0, 20.0), point(140, 70), point(170, 70), 6.0, lineStyleId),
            GraphicPrimitive.Group(
                id("group"),
                GraphicBounds(10.0, 100.0, 30.0, 20.0),
                listOf(GraphicPrimitive.Line(id("group-line"), GraphicBounds(10.0, 110.0, 30.0, 1.0), point(10, 110), point(40, 110), lineStyleId)),
            ),
            GraphicPrimitive.Transformed(
                id("moved"),
                GraphicBounds(60.0, 100.0, 20.0, 20.0),
                GraphicTransform.Translation(5.0, 6.0),
                GraphicPrimitive.Circle(id("moved-circle"), GraphicBounds(60.0, 100.0, 20.0, 20.0), point(70, 110), 10.0, lineStyleId),
            ),
            GraphicPrimitive.Transformed(
                id("rotated"),
                GraphicBounds(100.0, 100.0, 20.0, 20.0),
                GraphicTransform.Rotation(90.0, point(110, 110)),
                GraphicPrimitive.Line(id("rotated-line"), GraphicBounds(100.0, 110.0, 20.0, 1.0), point(100, 110), point(120, 110), lineStyleId),
            ),
            GraphicPrimitive.Transformed(
                id("scaled"),
                GraphicBounds(140.0, 100.0, 20.0, 20.0),
                GraphicTransform.Scale(2.0, 0.5, point(150, 110)),
                GraphicPrimitive.Circle(id("scaled-circle"), GraphicBounds(140.0, 100.0, 20.0, 20.0), point(150, 110), 10.0, lineStyleId),
            ),
        ),
        styleTokens = listOf(
            GraphicStyleToken(
                lineStyleId,
                GraphicPaintToken("drawing.foreground"),
                1.5,
                GraphicFill.TRANSPARENT,
                GraphicLineCap.ROUND,
                GraphicLineJoin.BEVEL,
                dashPattern = listOf(4.0, 2.0),
            ),
            GraphicStyleToken(
                solidStyleId,
                GraphicPaintToken("drawing.foreground"),
                1.0,
                GraphicFill.FOREGROUND,
                GraphicLineCap.BUTT,
                GraphicLineJoin.MITER,
            ),
            GraphicStyleToken(
                textStyleId,
                GraphicPaintToken("drawing.foreground"),
                1.0,
                GraphicFill.FOREGROUND,
                GraphicLineCap.BUTT,
                GraphicLineJoin.MITER,
                textAnchor = GraphicTextAnchor.MIDDLE,
                textBaseline = GraphicTextBaseline.CENTRAL,
            ),
        ),
    )

    private fun id(value: String) = GraphicPrimitiveId(value)
    private fun point(x: Int, y: Int) = GraphicPoint(x.toDouble(), y.toDouble())

    private companion object {
        val lineStyleId = GraphicStyleTokenId("line")
        val solidStyleId = GraphicStyleTokenId("solid")
        val textStyleId = GraphicStyleTokenId("text")
    }
}
