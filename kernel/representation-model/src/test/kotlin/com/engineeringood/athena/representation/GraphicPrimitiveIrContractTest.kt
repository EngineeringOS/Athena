package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GraphicPrimitiveIrContractTest {
    @Test
    fun `graphic primitive document supports the complete renderer neutral v0 vocabulary`() {
        val document = validDocument()

        val result = GraphicPrimitiveIrValidator.validate(document)

        assertTrue(result.isValid, result.diagnostics.toString())
        assertEquals(
            listOf(
                "arc",
                "circle",
                "connection-dot",
                "group",
                "line",
                "marker",
                "polyline",
                "rectangle",
                "reference-arrow",
                "text",
                "transform",
            ),
            document.primitives.map { it.kind.wireValue }.sorted(),
        )
        val first = document.toTransportPayload()
        val second = document.toTransportPayload()
        assertEquals(first, second)
        assertEquals("graphic.demo.v0", first.documentId)
        assertEquals(11, first.primitives.size)
        assertEquals("drawing.default", first.styleTokens.single().styleTokenId)
        assertEquals(20.0, first.primitives.first { it.kind == "line" }.end?.x)
        assertEquals("K1", first.primitives.first { it.kind == "text" }.text)
        assertEquals("rotation", first.primitives.first { it.kind == "transform" }.transform?.kind)
        assertEquals("line", first.primitives.first { it.kind == "group" }.children.single().kind)
    }

    @Test
    fun `validator reports missing document bounds primitives and style tokens deterministically`() {
        val result = GraphicPrimitiveIrValidator.validate(
            GraphicPrimitiveDocument(
                documentId = null,
                bounds = null,
                primitives = emptyList(),
                styleTokens = emptyList(),
            ),
        )

        assertFalse(result.isValid)
        assertEquals(
            listOf(
                "graphic.ir.bounds.missing",
                "graphic.ir.document-id.missing",
                "graphic.ir.primitive.missing",
                "graphic.ir.style-token.missing",
            ),
            result.toTransportPayload().map { it.getValue("code") },
        )
    }

    @Test
    fun `validator rejects invalid primitive geometry and unresolved style references`() {
        val badLine = GraphicPrimitive.Line(
            primitiveId = GraphicPrimitiveId("bad-line"),
            bounds = GraphicBounds(0.0, 0.0, 0.0, -1.0),
            start = GraphicPoint(0.0, 0.0),
            end = GraphicPoint(0.0, 0.0),
            styleTokenId = GraphicStyleTokenId("missing-style"),
        )

        val result = GraphicPrimitiveIrValidator.validate(
            validDocument().copy(primitives = listOf(badLine)),
        )

        assertFalse(result.isValid)
        assertEquals(
            listOf(
                "graphic.ir.bounds.invalid",
                "graphic.ir.bounds.out-of-document",
                "graphic.ir.geometry.invalid",
                "graphic.ir.style-token.unresolved",
            ),
            result.toTransportPayload().map { it.getValue("code") },
        )
    }

    @Test
    fun `validator rejects semantic package source DOM CSS and SVG authority`() {
        val result = GraphicPrimitiveIrValidator.validate(
            validDocument().copy(forbiddenAuthorityClaims = GraphicPrimitiveForbiddenAuthority.entries.toSet()),
        )

        assertFalse(result.isValid)
        assertEquals(
            listOf("graphic.ir.authority-forbidden"),
            result.diagnostics.map { it.code.wireValue }.distinct(),
        )
        val message = result.diagnostics.single().message
        GraphicPrimitiveForbiddenAuthority.entries.forEach { authority ->
            assertTrue(message.contains(authority.name), "Missing forbidden authority ${authority.name}")
        }
    }

    @Test
    fun `validator rejects duplicate ids non finite values and geometry outside declared bounds`() {
        val document = validDocument()
        val duplicate = document.primitives.first() as GraphicPrimitive.Line
        val invalid = duplicate.copy(
            primitiveId = GraphicPrimitiveId("invalid"),
            bounds = GraphicBounds(0.0, 0.0, Double.POSITIVE_INFINITY, 1.0),
            end = GraphicPoint(200.0, Double.NaN),
        )
        val result = GraphicPrimitiveIrValidator.validate(
            document.copy(
                primitives = listOf(duplicate, duplicate, invalid),
                styleTokens = listOf(document.styleTokens.single(), document.styleTokens.single()),
            ),
        )

        assertFalse(result.isValid)
        assertEquals(
            setOf(
                "graphic.ir.bounds.invalid",
                "graphic.ir.bounds.out-of-document",
                "graphic.ir.geometry.invalid",
                "graphic.ir.primitive-id.duplicate",
                "graphic.ir.style-token.duplicate",
            ),
            result.diagnostics.map { it.code.wireValue }.toSet(),
        )
    }

    @Test
    fun `validator reports cyclic primitive groups instead of overflowing`() {
        val children = mutableListOf<GraphicPrimitive>()
        val group = GraphicPrimitive.Group(
            GraphicPrimitiveId("cycle"),
            GraphicBounds(0.0, 0.0, 20.0, 20.0),
            children,
        )
        children += group

        val result = GraphicPrimitiveIrValidator.validate(validDocument().copy(primitives = listOf(group)))

        assertFalse(result.isValid)
        assertTrue(result.diagnostics.any { it.code.wireValue == "graphic.ir.nesting.cycle" })
    }

    private fun validDocument(): GraphicPrimitiveDocument {
        val style = GraphicStyleToken(
            styleTokenId = GraphicStyleTokenId("drawing.default"),
            stroke = GraphicPaintToken("drawing.foreground"),
            strokeWidth = 1.6,
            fill = GraphicFill.TRANSPARENT,
            lineCap = GraphicLineCap.ROUND,
            lineJoin = GraphicLineJoin.MITER,
            dashPattern = listOf(4.0, 2.0),
            textAnchor = GraphicTextAnchor.MIDDLE,
            textBaseline = GraphicTextBaseline.CENTRAL,
        )
        val line = GraphicPrimitive.Line(
            primitiveId = GraphicPrimitiveId("line"),
            bounds = GraphicBounds(0.0, 0.0, 20.0, 1.0),
            start = GraphicPoint(0.0, 0.0),
            end = GraphicPoint(20.0, 0.0),
            styleTokenId = style.styleTokenId,
        )
        return GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId("graphic.demo.v0"),
            bounds = GraphicBounds(0.0, 0.0, 120.0, 80.0),
            styleTokens = listOf(style),
            primitives = listOf(
                line,
                GraphicPrimitive.Polyline(
                    GraphicPrimitiveId("polyline"),
                    GraphicBounds(0.0, 0.0, 20.0, 10.0),
                    listOf(GraphicPoint(0.0, 0.0), GraphicPoint(10.0, 10.0), GraphicPoint(20.0, 0.0)),
                    style.styleTokenId,
                ),
                GraphicPrimitive.Arc(
                    GraphicPrimitiveId("arc"),
                    GraphicBounds(0.0, 0.0, 20.0, 20.0),
                    GraphicPoint(10.0, 10.0),
                    10.0,
                    0.0,
                    180.0,
                    style.styleTokenId,
                ),
                GraphicPrimitive.Circle(
                    GraphicPrimitiveId("circle"),
                    GraphicBounds(0.0, 0.0, 20.0, 20.0),
                    GraphicPoint(10.0, 10.0),
                    10.0,
                    style.styleTokenId,
                ),
                GraphicPrimitive.Rectangle(
                    GraphicPrimitiveId("rectangle"),
                    GraphicBounds(0.0, 0.0, 20.0, 20.0),
                    0.0,
                    style.styleTokenId,
                ),
                GraphicPrimitive.Text(
                    GraphicPrimitiveId("text"),
                    GraphicBounds(0.0, 0.0, 40.0, 12.0),
                    GraphicPoint(20.0, 6.0),
                    "K1",
                    style.styleTokenId,
                ),
                GraphicPrimitive.Marker(
                    GraphicPrimitiveId("marker"),
                    GraphicBounds(0.0, 0.0, 8.0, 8.0),
                    GraphicPoint(4.0, 4.0),
                    GraphicMarkerKind.TERMINAL,
                    style.styleTokenId,
                ),
                GraphicPrimitive.ConnectionDot(
                    GraphicPrimitiveId("connection-dot"),
                    GraphicBounds(0.0, 0.0, 4.0, 4.0),
                    GraphicPoint(2.0, 2.0),
                    2.0,
                    style.styleTokenId,
                ),
                GraphicPrimitive.ReferenceArrow(
                    GraphicPrimitiveId("reference-arrow"),
                    GraphicBounds(0.0, 0.0, 20.0, 8.0),
                    GraphicPoint(0.0, 4.0),
                    GraphicPoint(20.0, 4.0),
                    6.0,
                    style.styleTokenId,
                ),
                GraphicPrimitive.Group(
                    GraphicPrimitiveId("group"),
                    GraphicBounds(0.0, 0.0, 20.0, 1.0),
                    listOf(line.copy(primitiveId = GraphicPrimitiveId("group-line"))),
                ),
                GraphicPrimitive.Transformed(
                    GraphicPrimitiveId("transform"),
                    GraphicBounds(0.0, 0.0, 20.0, 1.0),
                    GraphicTransform.Rotation(90.0, GraphicPoint(10.0, 0.0)),
                    line.copy(primitiveId = GraphicPrimitiveId("transform-line")),
                ),
            ),
        )
    }
}
