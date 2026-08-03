package com.engineeringood.athena.compiler

import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicStyleTokenId
import com.engineeringood.athena.representation.PresentationLabelRole
import com.engineeringood.athena.representation.RepresentationAnchorRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaM34DrawingPrimitiveCompilerTest {
    private val compiler = AthenaRepresentationSourceCompiler()

    @Test
    fun `lowers all native drawing forms and exported dynamic label through canonical contracts`() {
        val result = compiler.compile("control-material.athena", VALID_MATERIAL)

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.joinToString("\n"))
        val symbol = result.definitions.single { it.symbolId.value == "iec.control.contact" }
        assertEquals(
            listOf(
                GraphicPrimitive.Line(
                    GraphicPrimitiveId("lead"),
                    GraphicBounds(39.9995, 0.0, 0.001, 14.0),
                    GraphicPoint(40.0, 0.0),
                    GraphicPoint(40.0, 14.0),
                    GraphicStyleTokenId("conductor"),
                ),
                GraphicPrimitive.Polyline(
                    GraphicPrimitiveId("contact"),
                    GraphicBounds(10.0, 10.0, 20.0, 10.0),
                    listOf(GraphicPoint(10.0, 20.0), GraphicPoint(20.0, 10.0), GraphicPoint(30.0, 20.0)),
                    GraphicStyleTokenId("symbol"),
                ),
                GraphicPrimitive.Arc(
                    GraphicPrimitiveId("coil"),
                    GraphicBounds(28.0, 28.0, 24.0, 24.0),
                    GraphicPoint(40.0, 40.0),
                    12.0,
                    180.0,
                    180.0,
                    GraphicStyleTokenId("symbol"),
                ),
                GraphicPrimitive.Circle(
                    GraphicPrimitiveId("terminal"),
                    GraphicBounds(37.0, -3.0, 6.0, 6.0),
                    GraphicPoint(40.0, 0.0),
                    3.0,
                    GraphicStyleTokenId("terminal"),
                ),
                GraphicPrimitive.Rectangle(
                    GraphicPrimitiveId("body"),
                    GraphicBounds(20.0, 20.0, 40.0, 30.0),
                    0.0,
                    GraphicStyleTokenId("symbol"),
                ),
            ),
            symbol.graphicBody.primitives,
        )
        assertEquals(
            listOf("conductor", "device-label", "reference-label", "symbol", "terminal"),
            symbol.graphicBody.styleTokens.map { style -> style.styleTokenId.value },
        )
        val anchor = symbol.anchors.single()
        assertEquals("top", anchor.anchorId.value)
        assertEquals(GraphicPrimitiveId("lead"), anchor.primitiveId)
        assertEquals(GraphicPoint(40.0, 0.0), anchor.point)
        assertEquals(RepresentationAnchorRole.TERMINAL, anchor.role)

        val symbolLabel = symbol.labelSlots.single { slot -> slot.slotId.value == "deviceTag" }
        assertEquals("deviceTag", symbolLabel.slotId.value)
        assertEquals(PresentationLabelRole.DEVICE_TAG, symbolLabel.role)
        assertEquals(0.0, assertNotNull(symbolLabel.bounds).x)
        assertEquals(-14.0, assertNotNull(symbolLabel.bounds).y)
        assertEquals(GraphicStyleTokenId("device-label"), symbolLabel.styleTokenId)

        val element = result.definitions.single { it.symbolId.value == "iec.control.contact.element" }
        val exported = element.labelSlots.single()
        assertEquals("deviceTag", exported.slotId.value)
        assertEquals(GraphicPoint(10.0, 13.0), exported.origin)
        assertEquals(GraphicBounds(10.0, 13.0, 40.0, 6.0), exported.bounds)
        assertEquals(symbolLabel.styleTokenId, exported.styleTokenId)
        assertEquals("glyph", element.intrinsicComposition?.exportedLabelSlots?.single()?.childId?.value)
        val exportedAnchor = element.anchors.single()
        assertEquals("top", exportedAnchor.anchorId.value)
        assertEquals(GraphicPrimitiveId("glyph.lead"), exportedAnchor.primitiveId)
        assertEquals(GraphicPoint(30.0, 20.0), exportedAnchor.point)
    }

    @Test
    fun `invalid native geometry and labels fail closed with deterministic source diagnostics`() {
        val invalid = VALID_MATERIAL
            .replace("points ((10, 20), (20, 10), (30, 20))", "points ((10, 20))")
            .replace("radius 12 from 180 sweep 180", "radius 0 from 180 sweep 0")
            .replace("radius 3 style terminal", "radius -3 style missing")
            .replace("size (40, 30) style symbol", "size (0, 30) style symbol")
            .replace("label contact at", "label deviceTag at")
            .replace("role device-tag", "role unknown-role")

        val first = compiler.compile("invalid-control-material.athena", invalid)
        val second = compiler.compile("invalid-control-material.athena", invalid)

        assertTrue(first.definitions.isEmpty())
        assertEquals(first.diagnostics, second.diagnostics)
        assertEquals(
            setOf(
                "symbol.label.id.duplicate",
                "symbol.label.role.unknown",
                "symbol.primitive.arc.invalid",
                "symbol.primitive.circle.invalid",
                "symbol.primitive.polyline.invalid",
                "symbol.primitive.rectangle.invalid",
                "symbol.style.unknown",
            ),
            first.diagnostics.map { it.code }.toSet(),
        )
        assertTrue(first.diagnostics.all { it.span.end.offset > it.span.start.offset })
        assertEquals(
            mapOf(
                "symbol.label.id.duplicate" to 15,
                "symbol.label.role.unknown" to 14,
                "symbol.primitive.arc.invalid" to 11,
                "symbol.primitive.circle.invalid" to 12,
                "symbol.primitive.polyline.invalid" to 10,
                "symbol.primitive.rectangle.invalid" to 13,
                "symbol.style.unknown" to 12,
            ),
            first.diagnostics.associate { diagnostic -> diagnostic.code to diagnostic.span.start.line },
        )
    }
}

private val VALID_MATERIAL = """
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
        label contact at (0, 68) size (80, 12) role reference style reference-label
      }

      anchor top {
        ref "lead"
        point (40, 0)
        role terminal
        direction bidirectional
        signal Control.family
      }
    }

    element control_contact_element {
      identity "iec.control.contact.element"
      version "1.0.0"
      bounds (0, -14, 80, 94)

      child glyph {
        symbol "iec.control.contact"
        translate (10, 20)
        rotate 0
        scale (0.5, 0.5)
        zOrder 0
      }

      export anchor top from glyph.top
      export label deviceTag from glyph.deviceTag
    }
""".trimIndent()
