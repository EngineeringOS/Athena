package com.engineeringood.athena.presentation

import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import com.engineeringood.athena.representation.PhysicalTerminalId
import com.engineeringood.athena.representation.PresentationRouteAnchor
import com.engineeringood.athena.representation.PresentationRouteAnchorId
import com.engineeringood.athena.representation.PresentationSide
import com.engineeringood.athena.representation.PresentationTerminalFact
import com.engineeringood.athena.representation.PresentationTerminalId
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationSubjectId
import com.engineeringood.athena.representation.SemanticPortId
import com.engineeringood.athena.representation.TerminalMarker
import com.engineeringood.athena.representation.TerminalNotation
import com.engineeringood.athena.representation.TerminalNumber
import com.engineeringood.athena.routing.ElectricalConnectionId
import com.engineeringood.athena.routing.ElectricalPortId
import com.engineeringood.athena.routing.ElectricalPortRole
import com.engineeringood.athena.routing.RouteFact
import com.engineeringood.athena.routing.RouteFactSnapshot
import com.engineeringood.athena.routing.RouteQualityState
import com.engineeringood.athena.routing.SchematicRouteId
import com.engineeringood.athena.routing.SchematicRoutePoint
import com.engineeringood.athena.routing.SchematicRouteSegment
import com.engineeringood.athena.routing.SchematicRouteSegmentOrientation
import com.engineeringood.athena.routing.TerminalAnchorFact
import com.engineeringood.athena.routing.TerminalAnchorId
import com.engineeringood.athena.routing.TerminalSide
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PresentationRouteAttachmentContractTest {
    @Test
    fun `route facts attach to presentation terminal anchors and preserve quality`() {
        val snapshotId = LayoutSnapshotId("snapshot:m25")
        val routeFact = RouteFact(
            routeId = SchematicRouteId("route:PLC1-Q1"),
            snapshotId = snapshotId,
            connectionId = ElectricalConnectionId("PLC1.Q1->XT1.1"),
            source = terminalAnchor("anchor:PLC1:Q1.0", "PLC1", "Q1.0", TerminalSide.RIGHT),
            target = terminalAnchor("anchor:XT1:1", "XT1", "1", TerminalSide.LEFT),
            segments = listOf(
                SchematicRouteSegment(
                    start = SchematicRoutePoint(80, 24),
                    end = SchematicRoutePoint(140, 24),
                    orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
                ),
            ),
        )
        val attachments = attachRoutesToPresentationTerminals(
            routeFactSnapshot = RouteFactSnapshot.canonical(
                snapshotId = snapshotId,
                family = "schematic-sheet",
                routeFacts = listOf(routeFact),
            ),
            terminals = listOf(
                presentationTerminal("terminal:PLC1:Q1.0", "PLC1", "Q1.0", "anchor:PLC1:Q1.0"),
                presentationTerminal("terminal:XT1:1", "XT1", "1", "anchor:XT1:1"),
            ),
        )

        assertEquals(1, attachments.size)
        assertEquals("terminal:PLC1:Q1.0", attachments.single().sourcePresentationTerminalId.value)
        assertEquals("terminal:XT1:1", attachments.single().targetPresentationTerminalId.value)
        assertEquals(RouteQualityState.SATISFIED, attachments.single().routeQuality)
        assertFalse(attachments.single().usesCenterFallback)
    }

    private fun terminalAnchor(
        anchorId: String,
        subjectId: String,
        portId: String,
        side: TerminalSide,
    ): TerminalAnchorFact = TerminalAnchorFact(
        anchorId = TerminalAnchorId(anchorId),
        subjectId = com.engineeringood.athena.ir.StableSemanticIdentity(subjectId),
        occurrenceId = LayoutOccurrenceId("occurrence:$subjectId"),
        portId = ElectricalPortId(portId),
        portSemanticId = com.engineeringood.athena.ir.StableSemanticIdentity("port:$subjectId.$portId"),
        portRole = ElectricalPortRole.OUTPUT,
        side = side,
        point = SchematicRoutePoint(80, 24),
        gridPoint = SchematicRoutePoint(80, 20),
        policySource = "test",
    )

    private fun presentationTerminal(
        terminalId: String,
        subjectId: String,
        portId: String,
        anchorId: String,
    ): PresentationTerminalFact = PresentationTerminalFact(
        presentationTerminalId = PresentationTerminalId(terminalId),
        subjectId = RepresentationSubjectId(subjectId),
        occurrenceId = RepresentationOccurrenceId("$subjectId@schematic-sheet"),
        portId = SemanticPortId(portId),
        physicalTerminalId = PhysicalTerminalId("$subjectId:$portId"),
        side = PresentationSide.RIGHT,
        routeAnchor = PresentationRouteAnchor(
            anchorId = PresentationRouteAnchorId(anchorId),
            point = com.engineeringood.athena.representation.PresentationPoint(
                com.engineeringood.athena.representation.GridUnit(80),
                com.engineeringood.athena.representation.GridUnit(24),
            ),
        ),
        notation = TerminalNotation(TerminalMarker.CIRCLE, TerminalNumber(portId)),
    )
}
