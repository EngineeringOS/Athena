package com.engineeringood.athena.representation

import java.nio.file.Files
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NativeRepresentationLibraryLoaderTest {
    @Test
    fun `loads native properties asset without browser or qet runtime`() {
        val asset = Files.createTempFile("athena-native-symbol", ".properties")
        asset.writeText(
            """
            library.id=athena.native.iec-v0
            symbol.0.id=iec.motor.compact
            symbol.0.version=1.0.0
            symbol.0.lifecycle=ACTIVE
            symbol.0.kind=MOTOR_LOAD
            symbol.0.bounds.width=32
            symbol.0.bounds.height=32
            symbol.0.primitive.0.type=rectangle
            symbol.0.primitive.0.id=body
            symbol.0.primitive.0.x=0
            symbol.0.primitive.0.y=0
            symbol.0.primitive.0.width=32
            symbol.0.primitive.0.height=32
            symbol.0.terminal.0.id=terminal-1
            symbol.0.terminal.0.role=POWER_INPUT
            symbol.0.terminal.0.x=0
            symbol.0.terminal.0.y=16
            symbol.0.terminal.0.side=LEFT
            symbol.0.terminal.0.marker=CIRCLE
            symbol.0.terminal.0.number=1
            symbol.0.label-slot.0.id=device-tag
            symbol.0.label-slot.0.role=DEVICE_TAG
            symbol.0.label-anchor.0.id=device-tag
            symbol.0.label-anchor.0.role=DEVICE_TAG
            symbol.0.label-anchor.0.x=0
            symbol.0.label-anchor.0.y=-8
            symbol.0.variant.0.id=compact
            symbol.0.style-token.0.name=stroke
            symbol.0.style-token.0.value=iec.black
            """.trimIndent(),
        )

        val result = try {
            NativeRepresentationLibraryLoader().load(asset)
        } finally {
            asset.deleteIfExists()
        }

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val definition = result.library.definitions.single()
        assertEquals(RepresentationLibraryId("athena.native.iec-v0"), definition.libraryId)
        assertEquals(RepresentationSymbolId("iec.motor.compact"), definition.symbolId)
        assertEquals(RepresentationVersion("1.0.0"), definition.version)
        assertEquals(RepresentationLifecycleState.ACTIVE, definition.lifecycle.state)
        assertEquals(RepresentationSymbolKind.MOTOR_LOAD, definition.kind)
        assertEquals(PresentationBounds(GridUnit(32), GridUnit(32)), definition.anatomy.bounds)
        assertEquals(listOf(RepresentationVariantId("compact")), definition.variants)
        assertEquals(listOf(RepresentationStyleToken("stroke", "iec.black")), definition.styleTokens)
        assertEquals(PresentationPrimitiveId("body"), definition.anatomy.primitives.single().primitiveId)
        assertEquals(PresentationTerminalId("terminal-1"), definition.anatomy.terminals.single().terminalId)
        assertEquals(TerminalNumber("1"), definition.anatomy.terminals.single().notation.number)
        assertEquals(PresentationLabelAnchorId("device-tag"), definition.anatomy.labelAnchors.single().anchorId)
        assertEquals(RepresentationLabelSlotId("device-tag"), definition.labelSlots.single().slotId)
    }

    @Test
    fun `rejects qet elmt files as runtime asset format`() {
        val qetAsset = Files.createTempFile("01coming_arrow", ".elmt")
        qetAsset.writeText("<definition><uuid>{qet}</uuid></definition>")

        val result = try {
            NativeRepresentationLibraryLoader().load(qetAsset)
        } finally {
            qetAsset.deleteIfExists()
        }

        assertEquals(null, result.libraryOrNull)
        assertEquals(
            listOf("representation.library.invalid"),
            result.diagnostics.map { diagnostic -> diagnostic.code.wireValue },
        )
    }
}
