package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class AthenaM34DrawingPrimitiveSyntaxTest {
    @Test
    fun `parses the complete native control drawing vocabulary with exact values`() {
        val result = AthenaLanguageParser().parse("drawing.athena", SOURCE)
        val parsed = assertIs<ParseSuccess>(result, result.toString())
        val symbol = assertIs<SymbolDeclaration>(parsed.ast.representationDeclarations.single())
        val graphic = assertNotNull(symbol.graphic)

        assertEquals(
            listOf("lead", "contact", "coil", "terminal", "body"),
            graphic.primitives.map(SymbolGraphicPrimitiveDeclaration::id),
        )
        val line = assertIs<SymbolGraphicPrimitiveDeclaration.Line>(graphic.primitives[0])
        assertEquals("lead", line.id)
        assertEquals(SymbolPoint(40.0, 0.0, span(0)), line.from.copy(span = span(0)))
        assertEquals(SymbolPoint(40.0, 14.0, span(0)), line.to.copy(span = span(0)))
        assertEquals("conductor", line.style)
        assertEquals(
            SOURCE.spanOf("line lead from (40, 0) to (40, 14) style conductor"),
            line.span,
        )
        val polyline = assertIs<SymbolGraphicPrimitiveDeclaration.Polyline>(graphic.primitives[1])
        assertEquals(
            listOf(SymbolPoint(10.0, 20.0, span(0)), SymbolPoint(20.0, 10.0, span(0)), SymbolPoint(30.0, 20.0, span(0))),
            polyline.points.map { it.copy(span = span(0)) },
        )
        assertEquals("symbol", polyline.style)
        assertEquals(
            SOURCE.spanOf("polyline contact points ((10, 20), (20, 10), (30, 20)) style symbol"),
            polyline.span,
        )
        val arc = assertIs<SymbolGraphicPrimitiveDeclaration.Arc>(graphic.primitives[2])
        assertEquals(SymbolPoint(40.0, 40.0, span(0)), arc.center.copy(span = span(0)))
        assertEquals(12.0, arc.radius)
        assertEquals(180.0, arc.startAngleDegrees)
        assertEquals(180.0, arc.sweepAngleDegrees)
        assertEquals("symbol", arc.style)
        assertEquals(
            SOURCE.spanOf("arc coil center (40, 40) radius 12 from 180 sweep 180 style symbol"),
            arc.span,
        )
        val circle = assertIs<SymbolGraphicPrimitiveDeclaration.Circle>(graphic.primitives[3])
        assertEquals(SymbolPoint(40.0, 0.0, span(0)), circle.center.copy(span = span(0)))
        assertEquals(3.0, circle.radius)
        assertEquals("terminal", circle.style)
        assertEquals(
            SOURCE.spanOf("circle terminal center (40, 0) radius 3 style terminal"),
            circle.span,
        )
        val rectangle = assertIs<SymbolGraphicPrimitiveDeclaration.Rectangle>(graphic.primitives[4])
        assertEquals(SymbolPoint(20.0, 20.0, span(0)), rectangle.origin.copy(span = span(0)))
        assertEquals(SymbolSize(40.0, 30.0, span(0)), rectangle.size.copy(span = span(0)))
        assertEquals("symbol", rectangle.style)
        assertEquals(
            SOURCE.spanOf("rectangle body at (20, 20) size (40, 30) style symbol"),
            rectangle.span,
        )

        val label = graphic.labels.single()
        assertEquals("deviceTag", label.id)
        assertEquals("device-tag", label.role.value)
        assertEquals("device-label", label.style)
        assertEquals(SymbolPoint(0.0, -14.0, span(0)), label.origin.copy(span = span(0)))
        assertEquals(SymbolSize(80.0, 12.0, span(0)), label.size.copy(span = span(0)))
        assertEquals(
            SOURCE.spanOf("label deviceTag at (0, -14) size (80, 12) role device-tag style device-label"),
            label.span,
        )
        assertEquals("label deviceTag at (0, -14) size (80, 12) role device-tag style device-label", slice(SOURCE, label.span))
    }

    @Test
    fun `parses explicit element label export`() {
        val result = AthenaLanguageParser().parse("element.athena", ELEMENT_SOURCE)
        val parsed = assertIs<ParseSuccess>(result, result.toString())
        val element = assertIs<ElementDeclaration>(parsed.ast.representationDeclarations.last())

        val exported = element.exportedLabels.single()
        assertEquals("deviceTag", exported.id)
        assertEquals("glyph", exported.childId.value)
        assertEquals("deviceTag", exported.childLabelId.value)
        assertEquals("glyph.deviceTag", slice(ELEMENT_SOURCE, exported.referenceSpan))
    }
}

private val SOURCE = """
    package com.engineeringood.m34.control

    symbol control_contact {
      identity "iec.control.contact"
      version "1.0.0"

      graphic {
        bounds (0, -14, 80, 94)
        line lead from (40, 0) to (40, 14) style conductor
        polyline contact points ((10, 20), (20, 10), (30, 20)) style symbol
        arc coil center (40, 40) radius 12 from 180 sweep 180 style symbol
        circle terminal center (40, 0) radius 3 style terminal
        rectangle body at (20, 20) size (40, 30) style symbol
        label deviceTag at (0, -14) size (80, 12) role device-tag style device-label
      }
    }
""".trimIndent()

private val ELEMENT_SOURCE = SOURCE + """

    element control_contact_element {
      identity "iec.control.contact.element"
      version "1.0.0"
      bounds (0, -14, 80, 94)

      child glyph {
        symbol "iec.control.contact"
        translate (0, 0)
        rotate 0
        scale (1, 1)
        zOrder 0
      }

      export label deviceTag from glyph.deviceTag
    }
"""

private fun span(offset: Int) = SourceSpan(
    SourcePosition(offset, 1, 1),
    SourcePosition(offset, 1, 1),
)

private fun slice(source: String, span: SourceSpan): String = source.substring(span.start.offset, span.end.offset)

private fun String.spanOf(token: String): SourceSpan {
    val start = indexOf(token)
    require(start >= 0) { "Token `$token` is missing." }

    fun position(offset: Int): SourcePosition {
        val prefix = substring(0, offset)
        return SourcePosition(
            offset = offset,
            line = prefix.count { it == '\n' } + 1,
            column = offset - prefix.lastIndexOf('\n'),
        )
    }

    return SourceSpan(position(start), position(start + token.length))
}
