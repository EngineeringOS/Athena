package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RepresentationModelContractTest {
    @Test
    fun `presentation anatomy can represent mandatory M25 families without fallback`() {
        val anatomies = listOf(
            schematicAnatomy("plc-controller"),
            schematicAnatomy("terminal-block"),
            schematicAnatomy("power-supply"),
            schematicAnatomy("load-actuator"),
        )

        assertEquals(4, anatomies.size)
        assertTrue(anatomies.all { it.context == RepresentationContext.ELECTRICAL_SCHEMATIC })
        assertTrue(anatomies.all { it.terminals.isNotEmpty() })
        assertTrue(anatomies.all { anatomy -> anatomy.terminals.all { it.notation.number.value.isNotBlank() } })
    }

    @Test
    fun `symbol anatomy is electrical schematic subset of presentation anatomy`() {
        val anatomy = schematicAnatomy("plc-controller")
        val symbol = SymbolAnatomy(
            familyId = SymbolFamilyId("plc-controller"),
            anatomy = anatomy,
        )

        assertEquals(anatomy, symbol.anatomy)
        assertEquals(RepresentationContext.ELECTRICAL_SCHEMATIC, symbol.anatomy.context)
        assertEquals("plc-controller", symbol.familyId.value)
    }

    @Test
    fun `representation model does not own renderer or qelectrotech truth`() {
        val anatomy = schematicAnatomy("terminal-block")

        assertFalse(anatomy.hasRendererTruth())
        assertFalse(anatomy.hasQElectroTechRuntimeDependency())
    }

    private fun schematicAnatomy(family: String): PresentationAnatomy = PresentationAnatomy(
        representationId = RepresentationId("athena-industrial-control-v0:$family"),
        context = RepresentationContext.ELECTRICAL_SCHEMATIC,
        bounds = PresentationBounds(width = GridUnit(80), height = GridUnit(48)),
        hotspot = PresentationHotspot(point = PresentationPoint(GridUnit(0), GridUnit(0))),
        primitives = listOf(
            PresentationPrimitive.Rectangle(
                primitiveId = PresentationPrimitiveId("$family-body"),
                origin = PresentationPoint(GridUnit(0), GridUnit(0)),
                size = PresentationSize(GridUnit(80), GridUnit(48)),
            ),
        ),
        terminals = listOf(
            PresentationTerminalPoint(
                terminalId = PresentationTerminalId("$family-t1"),
                role = TerminalPresentationRole.DIGITAL_OUTPUT,
                localPoint = PresentationPoint(GridUnit(80), GridUnit(24)),
                side = PresentationSide.RIGHT,
                notation = TerminalNotation(
                    marker = TerminalMarker.CIRCLE,
                    number = TerminalNumber("1"),
                ),
            ),
        ),
        labelAnchors = listOf(
            PresentationLabelAnchor(
                anchorId = PresentationLabelAnchorId("$family-tag"),
                role = PresentationLabelRole.DEVICE_TAG,
                point = PresentationPoint(GridUnit(0), GridUnit(-12)),
            ),
        ),
    )
}
