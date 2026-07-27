package com.engineeringood.athena.renderer.svg

import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicFill
import com.engineeringood.athena.representation.GraphicLineCap
import com.engineeringood.athena.representation.GraphicLineJoin
import com.engineeringood.athena.representation.GraphicPaintToken
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicStyleToken
import com.engineeringood.athena.representation.GraphicStyleTokenId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GraphicPrimitiveSvgCanvasComposerTest {
    @Test
    fun `composer derives root viewBox exactly from IR bounds and governed margin`() {
        val document = document(GraphicBounds(10.0, -5.0, 80.0, 40.0))
        val fragment = GraphicPrimitiveSvgAdapter().render(fragmentRequest(document))

        val result = GraphicPrimitiveSvgCanvasComposer().compose(
            GraphicPrimitiveSvgCanvasRequest(document, fragment, margin = 8.0),
        )

        assertTrue(result.isValid, result.diagnostics.toString())
        assertTrue(requireNotNull(result.svg).startsWith("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"2 -13 96 56\">"))
        assertEquals(GraphicBounds(2.0, -13.0, 96.0, 56.0), result.proof?.viewBox)
        assertEquals(document.bounds, result.proof?.contentBounds)
        assertEquals(8.0, result.proof?.margin)
        assertEquals("graphic-primitive-ir", result.proof?.boundsAuthority)
        assertEquals(result, GraphicPrimitiveSvgCanvasComposer().compose(GraphicPrimitiveSvgCanvasRequest(document, fragment, 8.0)))

        val fractionalDocument = document(GraphicBounds(-2.5, 4.25, 12.5, 7.75))
        val fractionalFragment = GraphicPrimitiveSvgAdapter().render(fragmentRequest(fractionalDocument))
        val fractional = GraphicPrimitiveSvgCanvasComposer().compose(
            GraphicPrimitiveSvgCanvasRequest(fractionalDocument, fractionalFragment, margin = 0.25),
        )
        assertEquals(GraphicBounds(-2.75, 4.0, 13.0, 8.25), fractional.proof?.viewBox)
        assertTrue(requireNotNull(fractional.svg).contains("viewBox=\"-2.75 4 13 8.25\""))
    }

    @Test
    fun `composer rejects invalid margin and all explicit toy fallback facts without partial SVG`() {
        val document = document(GraphicBounds(0.0, 0.0, 80.0, 40.0))
        val fragment = GraphicPrimitiveSvgAdapter().render(fragmentRequest(document))
        val negativeMargin = GraphicPrimitiveSvgCanvasComposer().compose(
            GraphicPrimitiveSvgCanvasRequest(document, fragment, -1.0),
        )
        val nonFiniteMargin = GraphicPrimitiveSvgCanvasComposer().compose(
            GraphicPrimitiveSvgCanvasRequest(document, fragment, Double.NaN),
        )
        val unsafe = GraphicPrimitiveSvgCanvasComposer().compose(
            GraphicPrimitiveSvgCanvasRequest(
                document = document,
                fragment = fragment,
                margin = 4.0,
                safetyFacts = GraphicPrimitiveSvgSafetyFacts(
                    duplicateLabelOccurrenceIds = listOf("label.M1"),
                    offscreenOccurrenceIds = listOf("occurrence.hidden"),
                    centerFallbackRouteIds = listOf("route.power"),
                    genericFallbackOccurrenceIds = listOf("device.unknown"),
                ),
            ),
        )

        assertFalse(negativeMargin.isValid)
        assertNull(negativeMargin.svg)
        assertEquals(listOf("drawing.svg.margin.invalid"), negativeMargin.diagnostics.map { it.code })
        assertFalse(nonFiniteMargin.isValid)
        assertNull(nonFiniteMargin.svg)
        assertEquals(listOf("drawing.svg.margin.invalid"), nonFiniteMargin.diagnostics.map { it.code })
        assertFalse(unsafe.isValid)
        assertNull(unsafe.svg)
        assertNull(unsafe.proof)
        assertEquals(
            listOf(
                Triple("drawing.svg.safety.center-fallback-route", "spatial-routing", "route.power"),
                Triple("drawing.svg.safety.duplicate-label", "presentation", "label.M1"),
                Triple("drawing.svg.safety.generic-fallback", "representation-binding", "device.unknown"),
                Triple("drawing.svg.safety.offscreen-occurrence", "presentation", "occurrence.hidden"),
            ),
            unsafe.diagnostics.map { Triple(it.code, it.authority, it.subject) },
        )

        val overflowingDocument = document(GraphicBounds(0.0, 0.0, Double.MAX_VALUE, 40.0))
        val overflowingFragment = GraphicPrimitiveSvgAdapter().render(fragmentRequest(overflowingDocument))
        val overflowingViewBox = GraphicPrimitiveSvgCanvasComposer().compose(
            GraphicPrimitiveSvgCanvasRequest(overflowingDocument, overflowingFragment, Double.MAX_VALUE),
        )
        assertFalse(overflowingViewBox.isValid)
        assertNull(overflowingViewBox.svg)
        assertEquals(listOf("drawing.svg.viewbox.invalid"), overflowingViewBox.diagnostics.map { it.code })
    }

    private fun fragmentRequest(document: GraphicPrimitiveDocument) = GraphicPrimitiveSvgRenderRequest(
        document,
        GraphicPrimitiveSvgPalette(mapOf("drawing.foreground" to "#202020"), "#ffffff"),
    )

    private fun document(bounds: GraphicBounds): GraphicPrimitiveDocument {
        val styleId = GraphicStyleTokenId("line")
        return GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId("descriptor.canvas-test"),
            bounds = bounds,
            primitives = listOf(
                GraphicPrimitive.Line(
                    GraphicPrimitiveId("line"),
                    GraphicBounds(bounds.x, bounds.y, bounds.width, 1.0),
                    GraphicPoint(bounds.x, bounds.y),
                    GraphicPoint(bounds.x + bounds.width, bounds.y),
                    styleId,
                ),
            ),
            styleTokens = listOf(
                GraphicStyleToken(
                    styleId,
                    GraphicPaintToken("drawing.foreground"),
                    1.0,
                    GraphicFill.TRANSPARENT,
                    GraphicLineCap.ROUND,
                    GraphicLineJoin.ROUND,
                ),
            ),
        )
    }
}
