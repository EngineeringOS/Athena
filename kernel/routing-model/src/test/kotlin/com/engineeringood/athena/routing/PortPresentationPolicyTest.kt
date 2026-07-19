package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutOccurrenceId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame

class PortPresentationPolicyTest {
    @Test
    fun `preferred sides are policy owned for the M24 port roles`() {
        val policy = samplePolicy()

        assertEquals(TerminalSide.LEFT, policy.preferredSideFor(ElectricalPortRole.INPUT))
        assertEquals(TerminalSide.RIGHT, policy.preferredSideFor(ElectricalPortRole.OUTPUT))
        assertEquals(TerminalSide.TOP, policy.preferredSideFor(ElectricalPortRole.POWER))
        assertEquals(TerminalSide.BOTTOM, policy.preferredSideFor(ElectricalPortRole.GROUND))
        assertEquals(TerminalSide.RIGHT, policy.preferredSideFor(ElectricalPortRole.BIDIRECTIONAL))
        assertEquals(TerminalSide.LEFT, policy.preferredSideFor(ElectricalPortRole.TERMINAL))
    }

    @Test
    fun `terminal anchors carry subject port occurrence side grid point and policy source`() {
        val policy = samplePolicy()
        val anchor = policy.terminalAnchor(
            anchorId = TerminalAnchorId("anchor:PLC1:DO1"),
            subjectId = StableSemanticIdentity("component:PLC1"),
            occurrenceId = LayoutOccurrenceId("occurrence:schematic:PLC1"),
            portId = ElectricalPortId("DO1"),
            portRole = ElectricalPortRole.OUTPUT,
            gridPoint = SchematicRoutePoint(x = 320, y = 180),
        )

        assertEquals(TerminalAnchorId("anchor:PLC1:DO1"), anchor.anchorId)
        assertEquals(StableSemanticIdentity("component:PLC1"), anchor.subjectId)
        assertEquals(LayoutOccurrenceId("occurrence:schematic:PLC1"), anchor.occurrenceId)
        assertEquals(ElectricalPortId("DO1"), anchor.portId)
        assertEquals(ElectricalPortRole.OUTPUT, anchor.portRole)
        assertEquals(TerminalSide.RIGHT, anchor.side)
        assertEquals(SchematicRoutePoint(x = 320, y = 180), anchor.gridPoint)
        assertEquals("m24:schematic-default", anchor.policySource)
        assertSame(anchor.point, anchor.gridPoint)
    }

    @Test
    fun `policy side selection is deterministic`() {
        val policy = samplePolicy()

        val first = policy.preferredSideFor(ElectricalPortRole.INPUT)
        val second = policy.preferredSideFor(ElectricalPortRole.INPUT)

        assertEquals(first, second)
        assertFalse(first == TerminalSide.BOTTOM && second == TerminalSide.TOP)
    }

    private fun samplePolicy(): PortPresentationPolicy {
        return PortPresentationPolicy(
            rules = listOf(
                PortPresentationRule(ElectricalPortRole.INPUT, TerminalSide.LEFT),
                PortPresentationRule(ElectricalPortRole.OUTPUT, TerminalSide.RIGHT),
                PortPresentationRule(ElectricalPortRole.POWER, TerminalSide.TOP),
                PortPresentationRule(ElectricalPortRole.GROUND, TerminalSide.BOTTOM),
                PortPresentationRule(ElectricalPortRole.BIDIRECTIONAL, TerminalSide.RIGHT),
                PortPresentationRule(ElectricalPortRole.TERMINAL, TerminalSide.LEFT),
            ),
            fallbackSide = TerminalSide.RIGHT,
            policySource = "m24:schematic-default",
        )
    }
}
