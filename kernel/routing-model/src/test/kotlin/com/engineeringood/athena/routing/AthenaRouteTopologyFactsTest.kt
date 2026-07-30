package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import com.engineeringood.athena.layout.LayoutSourceSpan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AthenaRouteTopologyFactsTest {
    @Test
    fun `shared semantic endpoint produces junction while unrelated intersection produces crossing`() {
        val sharedPort = StableSemanticIdentity("port:source.out")
        val routes = AthenaRouteEngine().solve(
            AthenaRouteEngineInput(
                snapshotId = LayoutSnapshotId("snapshot:topology"),
                layoutContext = SchematicRoutingLayoutContext(gridSize = 20),
                requests = listOf(
                    request("branch-a", anchor("source-a", "source", "out", 100, 100, sharedPort), anchor("a", "a", "in", 300, 100)),
                    request("branch-b", anchor("source-b", "source", "out", 100, 100, sharedPort), anchor("b", "b", "in", 300, 200)),
                    request("cross", anchor("cross-source", "cross-source", "out", 200, 40), anchor("cross-target", "cross-target", "in", 200, 260)),
                ),
            ),
        )

        assertEquals(1, routes.junctionFacts.size)
        assertEquals(SchematicRoutePoint(100, 100), routes.junctionFacts.single().point)
        assertEquals(setOf(SchematicRouteId("route:branch-a"), SchematicRouteId("route:branch-b")), routes.junctionFacts.single().routeIds.toSet())
        assertTrue(routes.crossingFacts.isNotEmpty())
        assertTrue(routes.crossingFacts.all { crossing -> crossing.routeIds.size == 2 })
        assertTrue(routes.crossingFacts.all { crossing -> crossing.joined.not() })
        assertFalse(routes.crossingFacts.any { crossing -> crossing.point == routes.junctionFacts.single().point })
    }

    private fun request(
        id: String,
        source: TerminalAnchorFact,
        target: TerminalAnchorFact,
    ): AthenaRouteRequest {
        val connectionId = ElectricalConnectionId("connection:$id")
        return AthenaRouteRequest(
            routeId = SchematicRouteId("route:$id"),
            connectionIntent = ElectricalConnectionIntent(
                connectionId = connectionId,
                sourceSubjectId = source.subjectId,
                sourcePortId = source.portId,
                sourcePortSemanticId = source.portSemanticId,
                targetSubjectId = target.subjectId,
                targetPortId = target.portId,
                targetPortSemanticId = target.portSemanticId,
                role = ElectricalConnectionRole.CONTROL_SIGNAL,
                signalClass = ElectricalSignalClass.CONTROL,
                sourceSpan = LayoutSourceSpan("routes.athena", 1, 1, 1, 32),
            ),
            sourceAnchor = source,
            targetAnchor = target,
        )
    }

    private fun anchor(
        anchorId: String,
        owner: String,
        port: String,
        x: Int,
        y: Int,
        semanticPortId: StableSemanticIdentity = StableSemanticIdentity("port:$owner.$port"),
    ): TerminalAnchorFact = TerminalAnchorFact(
        anchorId = TerminalAnchorId("anchor:$anchorId"),
        subjectId = StableSemanticIdentity("component:$owner"),
        occurrenceId = LayoutOccurrenceId("occurrence:$owner"),
        portId = ElectricalPortId(port),
        portSemanticId = semanticPortId,
        portRole = ElectricalPortRole.OUTPUT,
        side = TerminalSide.RIGHT,
        point = SchematicRoutePoint(x, y),
        gridPoint = SchematicRoutePoint(x, y),
        policySource = "test:topology",
    )
}
