package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DrawingSymbolPrimitiveCompilerTest {
    @Test
    fun `compiler preserves primitive scene and emits deterministic anatomy proof`() {
        val anatomy = anatomy()
        val result = DrawingSymbolPrimitiveCompiler().compile(request(anatomy))

        assertTrue(result.isValid, result.diagnostics.toString())
        assertEquals(anatomy.primitives, result.document?.primitives)
        assertEquals(bounds, result.document?.bounds)
        assertEquals(styles, result.document?.styleTokens)
        assertEquals("descriptor.test", result.proof?.descriptorId)
        assertEquals("athena-native:resource.test", result.proof?.resourceHandle)
        assertEquals(listOf("group", "line", "rotated", "inner-line"), result.proof?.primitiveIds)
        assertEquals(listOf("line", "load"), result.proof?.anchorIds)
        assertEquals(listOf("device-tag"), result.proof?.labelSlotIds)
        assertEquals(listOf("cross-reference"), result.proof?.referenceSlotIds)
        assertEquals(result, DrawingSymbolPrimitiveCompiler().compile(request(anatomy)))
    }

    @Test
    fun `compiler fails closed for invalid anatomy blank handle and unsupported primitive kind`() {
        val invalidAnatomy = anatomy().copy(anchors = emptyList())
        val invalid = DrawingSymbolPrimitiveCompiler().compile(request(invalidAnatomy).copy(resourceHandle = " "))
        val unsupported = DrawingSymbolPrimitiveCompiler(
            supportedPrimitiveKinds = GraphicPrimitiveKind.values().toSet() - GraphicPrimitiveKind.TRANSFORM,
        ).compile(request(anatomy()))

        assertFalse(invalid.isValid)
        assertNull(invalid.document)
        assertEquals(
            listOf("drawing.symbol.anchor.missing", "drawing.symbol.compile.resource-handle.invalid"),
            invalid.diagnostics.map { it.code },
        )
        assertFalse(unsupported.isValid)
        assertNull(unsupported.document)
        assertEquals(
            listOf("drawing.symbol.compile.primitive-kind.unsupported"),
            unsupported.diagnostics.map { it.code },
        )
    }

    @Test
    fun `compiler fails closed when compiled primitive IR does not resolve styles`() {
        val invalid = DrawingSymbolPrimitiveCompiler().compile(request(anatomy()).copy(styleTokens = emptyList()))

        assertFalse(invalid.isValid)
        assertNull(invalid.document)
        assertNull(invalid.proof)
        assertEquals(
            listOf(
                "graphic.ir.style-token.missing",
                "graphic.ir.style-token.unresolved",
                "graphic.ir.style-token.unresolved",
            ),
            invalid.diagnostics.map { it.code },
        )
    }

    @Test
    fun `compiler diagnoses a cyclic primitive scene before proof flattening`() {
        val children = mutableListOf<GraphicPrimitive>()
        val cyclicGroup = GraphicPrimitive.Group(GraphicPrimitiveId("cycle"), bounds, children)
        children += cyclicGroup

        val result = DrawingSymbolPrimitiveCompiler().compile(request(anatomy().copy(primitives = listOf(cyclicGroup))))

        assertFalse(result.isValid)
        assertNull(result.document)
        assertNull(result.proof)
        assertEquals(listOf("graphic.ir.nesting.cycle"), result.diagnostics.map { it.code })
    }

    private fun request(anatomy: DrawingSymbolAnatomy) = DrawingSymbolPrimitiveCompilationRequest(
        descriptorId = "descriptor.test",
        resourceHandle = "athena-native:resource.test",
        anatomy = anatomy,
        styleTokens = styles,
    )

    private fun anatomy(): DrawingSymbolAnatomy = DrawingSymbolAnatomy(
        identity = DrawingSymbolIdentity("test.symbol"),
        version = DrawingSymbolVersion("1.0.0"),
        packageId = DrawingSymbolPackageId("com.athena.test"),
        domainTags = setOf(DrawingSymbolTag("test")),
        profileTags = setOf(DrawingSymbolTag("test-profile")),
        lifecycle = DrawingSymbolLifecycle.ACTIVE,
        primitives = listOf(
            GraphicPrimitive.Group(
                GraphicPrimitiveId("group"),
                bounds,
                listOf(
                    line("line", 0.0, 12.0, 30.0, 12.0),
                    GraphicPrimitive.Transformed(
                        GraphicPrimitiveId("rotated"),
                        GraphicBounds(30.0, 0.0, 50.0, 48.0),
                        GraphicTransform.Rotation(90.0, GraphicPoint(40.0, 24.0)),
                        line("inner-line", 30.0, 24.0, 80.0, 24.0),
                    ),
                ),
            ),
        ),
        anchors = listOf(
            anchor("line", 0, 24),
            anchor("load", 80, 24),
        ),
        labelSlots = listOf(DrawingSymbolLabelSlot(DrawingSymbolSlotId("device-tag"), DrawingSymbolLabelRole.DEVICE_TAG, true)),
        referenceSlots = listOf(
            DrawingSymbolReferenceSlot(DrawingSymbolSlotId("cross-reference"), DrawingSymbolReferenceRole.CROSS_REFERENCE, false),
        ),
        hotspots = listOf(DrawingSymbolHotspot(DrawingSymbolHotspotId("body"), DrawingSymbolBounds(0, 0, 80, 48))),
        bounds = DrawingSymbolBounds(0, 0, 80, 48),
        orientations = setOf(DrawingSymbolOrientation.HORIZONTAL),
        provenance = DrawingSymbolProvenance("athena-owned-test", "1.0.0"),
    )

    private fun anchor(id: String, x: Int, y: Int) = DrawingSymbolAnchor(
        DrawingSymbolAnchorId(id),
        DrawingSymbolPoint(x, y),
        DrawingSymbolAnchorRole.TERMINAL,
        true,
        DrawingSymbolTerminalRole(id),
    )

    private fun line(id: String, x1: Double, y1: Double, x2: Double, y2: Double) = GraphicPrimitive.Line(
        GraphicPrimitiveId(id),
        GraphicBounds(x1, y1 - 1.0, x2 - x1, 2.0),
        GraphicPoint(x1, y1),
        GraphicPoint(x2, y2),
        styleId,
    )

    private companion object {
        val bounds = GraphicBounds(0.0, 0.0, 80.0, 48.0)
        val styleId = GraphicStyleTokenId("drawing.default")
        val styles = listOf(
            GraphicStyleToken(
                styleId,
                GraphicPaintToken("drawing.foreground"),
                1.6,
                GraphicFill.TRANSPARENT,
                GraphicLineCap.ROUND,
                GraphicLineJoin.ROUND,
            ),
        )
    }
}
