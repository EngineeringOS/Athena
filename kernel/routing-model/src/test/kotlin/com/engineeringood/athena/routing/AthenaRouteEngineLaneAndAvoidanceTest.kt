package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AthenaRouteEngineLaneAndAvoidanceTest {
    @Test
    fun `lane assignment is deterministic for clear routes`() {
        val result = AthenaRouteEngineV0().solve(
            input(
                requests = listOf(
                    request("a", 120, 80, 320, 80),
                    request("z", 120, 160, 320, 160),
                ),
            ),
        )

        assertEquals(listOf(SchematicRouteLane(0), SchematicRouteLane(1)), result.routeFacts.map(RouteFact::lane))
    }

    @Test
    fun `route uses a clear lane to avoid an obvious component body`() {
        val obstacle = SchematicComponentBounds(
            subjectId = StableSemanticIdentity("component:OBSTACLE"),
            occurrenceId = LayoutOccurrenceId("occurrence:component:OBSTACLE"),
            topLeft = SchematicRoutePoint(x = 220, y = 120),
            width = 80,
            height = 80,
        )
        val result = AthenaRouteEngineV0().solve(
            input(
                componentBounds = listOf(obstacle),
                requests = listOf(request("a", 120, 180, 400, 180)),
            ),
        )
        val route = result.routeFacts.single()

        assertEquals(SchematicRouteLane(0), route.lane)
        assertFalse(route.segments.any { segment -> obstacle.intersects(segment) })
        assertEquals(RouteQualityState.SATISFIED, route.quality.state)
    }

    @Test
    fun `route quality is degraded when no component avoiding lane is available`() {
        val obstacle = SchematicComponentBounds(
            subjectId = StableSemanticIdentity("component:BLOCKING_OBSTACLE"),
            occurrenceId = LayoutOccurrenceId("occurrence:component:BLOCKING_OBSTACLE"),
            topLeft = SchematicRoutePoint(x = 220, y = 0),
            width = 80,
            height = 140,
        )
        val result = AthenaRouteEngineV0().solve(
            input(
                componentBounds = listOf(obstacle),
                requests = listOf(request("blocked", 120, 80, 400, 80)),
            ),
        )
        val route = result.routeFacts.single()

        assertTrue(route.segments.any { segment -> obstacle.intersects(segment) })
        assertEquals(RouteQualityState.DEGRADED, route.quality.state)
        assertEquals(listOf(RouteConstraintId("constraint:blocked:avoid")), route.quality.failedConstraintIds)
    }

    private fun input(
        requests: List<AthenaRouteRequest>,
        componentBounds: List<SchematicComponentBounds> = emptyList(),
    ): AthenaRouteEngineInput {
        return AthenaRouteEngineInput(
            snapshotId = LayoutSnapshotId("snapshot:m24:lanes"),
            layoutContext = SchematicRoutingLayoutContext(gridSize = 20),
            componentBounds = componentBounds,
            requests = requests,
        )
    }

    private fun request(
        suffix: String,
        sourceX: Int,
        sourceY: Int,
        targetX: Int,
        targetY: Int,
    ): AthenaRouteRequest {
        val connectionId = ElectricalConnectionId("connection:$suffix")
        val source = anchor("anchor:$suffix:source", "component:S$suffix", "out", TerminalSide.RIGHT, sourceX, sourceY)
        val target = anchor("anchor:$suffix:target", "component:T$suffix", "in", TerminalSide.LEFT, targetX, targetY)
        return AthenaRouteRequest(
            routeId = SchematicRouteId("route:$suffix"),
            connectionIntent = ElectricalConnectionIntent(
                connectionId = connectionId,
                sourceSubjectId = source.subjectId,
                sourcePortId = source.portId,
                targetSubjectId = target.subjectId,
                targetPortId = target.portId,
                role = ElectricalConnectionRole.CONTROL_SIGNAL,
            ),
            sourceAnchor = source,
            targetAnchor = target,
            constraints = listOf(
                RouteConstraint(
                    constraintId = RouteConstraintId("constraint:$suffix:avoid"),
                    kind = RouteConstraintKind.AVOID_NODE,
                    connectionId = connectionId,
                ),
            ),
        )
    }

    private fun anchor(
        anchorId: String,
        subjectId: String,
        portId: String,
        side: TerminalSide,
        x: Int,
        y: Int,
    ): TerminalAnchorFact {
        return TerminalAnchorFact(
            anchorId = TerminalAnchorId(anchorId),
            subjectId = StableSemanticIdentity(subjectId),
            occurrenceId = LayoutOccurrenceId("occurrence:$subjectId"),
            portId = ElectricalPortId(portId),
            portRole = ElectricalPortRole.OUTPUT,
            side = side,
            point = SchematicRoutePoint(x = x, y = y),
            gridPoint = SchematicRoutePoint(x = x, y = y),
            policySource = "m24:schematic-default",
        )
    }
}
