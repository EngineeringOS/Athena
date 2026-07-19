package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TerminalLayerContractTest {
    @Test
    fun `semantic port physical terminal and presentation terminal stay distinct`() {
        val semanticPort = SemanticPortRef(
            subjectId = RepresentationSubjectId("PLC1"),
            portId = SemanticPortId("Q1.0"),
            role = TerminalPresentationRole.DIGITAL_OUTPUT,
        )
        val physicalTerminal = PhysicalTerminalRef(
            terminalId = PhysicalTerminalId("X1:14"),
            semanticPort = semanticPort,
        )
        val presentationTerminal = PresentationTerminalFact(
            presentationTerminalId = PresentationTerminalId("PLC1-Q1.0-terminal"),
            subjectId = semanticPort.subjectId,
            occurrenceId = RepresentationOccurrenceId("PLC1@schematic-sheet"),
            portId = semanticPort.portId,
            physicalTerminalId = physicalTerminal.terminalId,
            side = PresentationSide.RIGHT,
            routeAnchor = PresentationRouteAnchor(
                anchorId = PresentationRouteAnchorId("anchor:PLC1:Q1.0"),
                point = PresentationPoint(GridUnit(80), GridUnit(24)),
            ),
            notation = TerminalNotation(
                marker = TerminalMarker.CIRCLE,
                number = TerminalNumber("14"),
            ),
        )

        assertEquals("Q1.0", semanticPort.portId.value)
        assertEquals("X1:14", physicalTerminal.terminalId.value)
        assertEquals("14", presentationTerminal.notation.number.value)
        assertFalse(presentationTerminal.terminalNumberDerivedFromRendererText)
    }

    @Test
    fun `mandatory family terminal notation requires marker plus number`() {
        val terminal = PresentationTerminalFact(
            presentationTerminalId = PresentationTerminalId("terminal-block-1"),
            subjectId = RepresentationSubjectId("XT1"),
            occurrenceId = RepresentationOccurrenceId("XT1@schematic-sheet"),
            portId = SemanticPortId("1"),
            physicalTerminalId = PhysicalTerminalId("XT1:1"),
            side = PresentationSide.LEFT,
            routeAnchor = PresentationRouteAnchor(
                anchorId = PresentationRouteAnchorId("anchor:XT1:1"),
                point = PresentationPoint(GridUnit(0), GridUnit(12)),
            ),
            notation = TerminalNotation(
                marker = TerminalMarker.SQUARE,
                number = TerminalNumber("1"),
            ),
        )

        assertEquals(TerminalMarker.SQUARE, terminal.notation.marker)
        assertEquals("1", terminal.notation.number.value)
    }
}
