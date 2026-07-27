package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DrawingSymbolAnatomyContractTest {
    @Test
    fun `valid anatomy captures generic identity package policy and interaction geometry`() {
        val anatomy = validAnatomy()

        val result = DrawingSymbolAnatomyValidator.validate(anatomy)

        assertTrue(result.isValid, result.diagnostics.toString())
        val payload = anatomy.toTransportPayload()
        assertEquals("drawing.symbol.v1", payload.identity)
        assertEquals("1.0.0", payload.version)
        assertEquals("athena.native.symbols", payload.packageId)
        assertEquals(listOf("electrical", "industrial-control"), payload.profileTags)
        assertEquals("input", payload.anchors.single().anchorId)
        assertTrue(payload.anchors.single().required)
        assertEquals("power-input", payload.anchors.single().terminalRole)
        assertEquals("device-tag", payload.labelSlots.single().slotId)
        assertEquals("folio-ref", payload.referenceSlots.single().slotId)
        assertEquals(listOf("line"), payload.primitives.map { it.kind })
        assertEquals(20.0, payload.primitives.single().end?.x)
        assertEquals(32, payload.hotspots.single().bounds.width)
        assertEquals(listOf("horizontal", "vertical"), payload.orientations)
    }

    @Test
    fun `validator rejects missing required anatomy sections with stable diagnostics`() {
        val result = DrawingSymbolAnatomyValidator.validate(
            DrawingSymbolAnatomy(
                identity = null,
                version = null,
                packageId = null,
                domainTags = emptySet(),
                profileTags = emptySet(),
                lifecycle = null,
                primitives = emptyList(),
                anchors = emptyList(),
                labelSlots = emptyList(),
                referenceSlots = emptyList(),
                hotspots = emptyList(),
                bounds = null,
                orientations = emptySet(),
                provenance = null,
            ),
        )

        assertFalse(result.isValid)
        assertEquals(
            listOf(
                "drawing.symbol.anchor.missing",
                "drawing.symbol.bounds.missing",
                "drawing.symbol.domain-tags.missing",
                "drawing.symbol.hotspot.missing",
                "drawing.symbol.identity.missing",
                "drawing.symbol.label-slot.missing",
                "drawing.symbol.lifecycle.missing",
                "drawing.symbol.orientation.missing",
                "drawing.symbol.package.missing",
                "drawing.symbol.primitive.missing",
                "drawing.symbol.profile-tags.missing",
                "drawing.symbol.provenance.missing",
                "drawing.symbol.reference-slot.missing",
                "drawing.symbol.version.missing",
            ),
            result.toTransportPayload().map { it.getValue("code") },
        )
    }

    @Test
    fun `validator rejects semantic source and renderer authority claims`() {
        val result = DrawingSymbolAnatomyValidator.validate(
            validAnatomy().copy(
                forbiddenAuthorityClaims = setOf(
                    DrawingSymbolForbiddenAuthority.ENGINEERING_TRUTH,
                    DrawingSymbolForbiddenAuthority.SOURCE_MUTATION,
                    DrawingSymbolForbiddenAuthority.DOM_SELECTOR,
                    DrawingSymbolForbiddenAuthority.SVG_PATH,
                    DrawingSymbolForbiddenAuthority.ATHENA_VISUAL_SYNTAX,
                ),
            ),
        )

        assertFalse(result.isValid)
        assertEquals(
            listOf("drawing.symbol.authority-forbidden"),
            result.diagnostics.map { it.code.wireValue }.distinct(),
        )
        val message = result.diagnostics.single().message
        DrawingSymbolForbiddenAuthority.entries.forEach { authority ->
            assertTrue(message.contains(authority.name), "Missing forbidden authority ${authority.name}")
        }
    }

    @Test
    fun `validator rejects duplicate ids optional-only anchors and invalid interaction bounds`() {
        val anatomy = validAnatomy()
        val result = DrawingSymbolAnatomyValidator.validate(
            anatomy.copy(
                anchors = listOf(
                    anatomy.anchors.single().copy(required = false),
                    anatomy.anchors.single().copy(required = false),
                ),
                labelSlots = listOf(anatomy.labelSlots.single(), anatomy.labelSlots.single()),
                hotspots = listOf(
                    DrawingSymbolHotspot(
                        DrawingSymbolHotspotId("body"),
                        DrawingSymbolBounds(30, 30, 8, 8),
                    ),
                ),
            ),
        )

        assertFalse(result.isValid)
        assertEquals(
            listOf(
                "drawing.symbol.anchor.duplicate",
                "drawing.symbol.anchor.required-missing",
                "drawing.symbol.hotspot.out-of-bounds",
                "drawing.symbol.label-slot.duplicate",
            ),
            result.toTransportPayload().map { it.getValue("code") },
        )
    }

    private fun validAnatomy(): DrawingSymbolAnatomy = DrawingSymbolAnatomy(
        identity = DrawingSymbolIdentity("drawing.symbol.v1"),
        version = DrawingSymbolVersion("1.0.0"),
        packageId = DrawingSymbolPackageId("athena.native.symbols"),
        domainTags = setOf(DrawingSymbolTag("electrical")),
        profileTags = setOf(DrawingSymbolTag("industrial-control"), DrawingSymbolTag("electrical")),
        lifecycle = DrawingSymbolLifecycle.ACTIVE,
        primitives = listOf(
            GraphicPrimitive.Line(
                primitiveId = GraphicPrimitiveId("body-line"),
                bounds = GraphicBounds(0.0, 0.0, 20.0, 1.0),
                start = GraphicPoint(0.0, 0.0),
                end = GraphicPoint(20.0, 0.0),
                styleTokenId = GraphicStyleTokenId("drawing.default"),
            ),
        ),
        anchors = listOf(
            DrawingSymbolAnchor(
                anchorId = DrawingSymbolAnchorId("input"),
                point = DrawingSymbolPoint(0, 16),
                role = DrawingSymbolAnchorRole.TERMINAL,
                required = true,
                terminalRole = DrawingSymbolTerminalRole("power-input"),
            ),
        ),
        labelSlots = listOf(
            DrawingSymbolLabelSlot(
                DrawingSymbolSlotId("device-tag"),
                DrawingSymbolLabelRole.DEVICE_TAG,
                required = true,
            ),
        ),
        referenceSlots = listOf(
            DrawingSymbolReferenceSlot(
                DrawingSymbolSlotId("folio-ref"),
                DrawingSymbolReferenceRole.CONTINUATION,
                required = false,
            ),
        ),
        hotspots = listOf(
            DrawingSymbolHotspot(
                hotspotId = DrawingSymbolHotspotId("body"),
                bounds = DrawingSymbolBounds(0, 0, 32, 32),
            ),
        ),
        bounds = DrawingSymbolBounds(0, 0, 32, 32),
        orientations = setOf(DrawingSymbolOrientation.HORIZONTAL, DrawingSymbolOrientation.VERTICAL),
        provenance = DrawingSymbolProvenance("athena-native", "1.0.0"),
    )
}
