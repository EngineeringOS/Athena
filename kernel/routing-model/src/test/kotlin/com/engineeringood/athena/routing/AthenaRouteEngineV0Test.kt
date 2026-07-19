package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AthenaRouteEngineV0Test {
    @Test
    fun `route engine emits deterministic grid aligned orthogonal facts from terminal anchors`() {
        val input = sampleInput(
            requests = listOf(
                sampleRequest(
                    routeId = SchematicRouteId("route:z"),
                    connectionId = ElectricalConnectionId("connection:z"),
                    sourceAnchor = sampleAnchor("anchor:z:source", "component:PLC1", "DO1", TerminalSide.RIGHT, 320, 180),
                    targetAnchor = sampleAnchor("anchor:z:target", "component:XT1", "1", TerminalSide.LEFT, 520, 260),
                ),
                sampleRequest(
                    routeId = SchematicRouteId("route:a"),
                    connectionId = ElectricalConnectionId("connection:a"),
                    sourceAnchor = sampleAnchor("anchor:a:source", "component:PS1", "L+", TerminalSide.RIGHT, 120, 80),
                    targetAnchor = sampleAnchor("anchor:a:target", "component:PLC1", "L+", TerminalSide.LEFT, 320, 80),
                ),
            ),
        )
        val engine = AthenaRouteEngineV0()

        val first = engine.solve(input)
        val second = engine.solve(input)

        assertEquals(first, second)
        assertEquals(listOf(SchematicRouteId("route:a"), SchematicRouteId("route:z")), first.routeFacts.map(RouteFact::routeId))
        first.routeFacts.flatMap(RouteFact::segments).forEach { segment ->
            assertTrue(segment.start.x % input.layoutContext.gridSize == 0)
            assertTrue(segment.start.y % input.layoutContext.gridSize == 0)
            assertTrue(segment.end.x % input.layoutContext.gridSize == 0)
            assertTrue(segment.end.y % input.layoutContext.gridSize == 0)
            assertTrue(
                segment.orientation == SchematicRouteSegmentOrientation.HORIZONTAL ||
                    segment.orientation == SchematicRouteSegmentOrientation.VERTICAL,
            )
        }
    }

    @Test
    fun `route engine attaches to terminal anchors and not component centers`() {
        val sourceAnchor = sampleAnchor("anchor:source", "component:PLC1", "DO1", TerminalSide.RIGHT, 320, 180)
        val targetAnchor = sampleAnchor("anchor:target", "component:XT1", "1", TerminalSide.LEFT, 520, 180)
        val input = sampleInput(
            requests = listOf(sampleRequest(sourceAnchor = sourceAnchor, targetAnchor = targetAnchor)),
            componentBounds = listOf(
                SchematicComponentBounds(
                    subjectId = StableSemanticIdentity("component:PLC1"),
                    occurrenceId = LayoutOccurrenceId("occurrence:component:PLC1"),
                    topLeft = SchematicRoutePoint(x = 240, y = 140),
                    width = 120,
                    height = 80,
                ),
            ),
        )

        val route = AthenaRouteEngineV0().solve(input).routeFacts.single()

        assertEquals(sourceAnchor, route.source)
        assertEquals(targetAnchor, route.target)
        assertEquals(sourceAnchor.gridPoint, route.segments.first().start)
        assertEquals(targetAnchor.gridPoint, route.segments.last().end)
        assertFalse(route.segments.any { segment -> segment.start == SchematicRoutePoint(x = 300, y = 180) })
    }

    private fun sampleInput(
        requests: List<AthenaRouteRequest>,
        componentBounds: List<SchematicComponentBounds> = emptyList(),
    ): AthenaRouteEngineInput {
        return AthenaRouteEngineInput(
            snapshotId = LayoutSnapshotId("snapshot:m24:route-engine"),
            layoutContext = SchematicRoutingLayoutContext(gridSize = 20),
            componentBounds = componentBounds,
            requests = requests,
        )
    }

    private fun sampleRequest(
        routeId: SchematicRouteId = SchematicRouteId("route:a"),
        connectionId: ElectricalConnectionId = ElectricalConnectionId("connection:a"),
        sourceAnchor: TerminalAnchorFact = sampleAnchor("anchor:a:source", "component:A", "out", TerminalSide.RIGHT, 120, 80),
        targetAnchor: TerminalAnchorFact = sampleAnchor("anchor:a:target", "component:B", "in", TerminalSide.LEFT, 320, 80),
    ): AthenaRouteRequest {
        return AthenaRouteRequest(
            routeId = routeId,
            connectionIntent = ElectricalConnectionIntent(
                connectionId = connectionId,
                sourceSubjectId = sourceAnchor.subjectId,
                sourcePortId = sourceAnchor.portId,
                targetSubjectId = targetAnchor.subjectId,
                targetPortId = targetAnchor.portId,
                role = ElectricalConnectionRole.CONTROL_SIGNAL,
                signalClass = ElectricalSignalClass.DIGITAL_OUTPUT,
            ),
            sourceAnchor = sourceAnchor,
            targetAnchor = targetAnchor,
            constraints = listOf(
                RouteConstraint(
                    constraintId = RouteConstraintId("constraint:${routeId.value}:orthogonal"),
                    kind = RouteConstraintKind.ORTHOGONAL_ONLY,
                    connectionId = connectionId,
                    priority = RouteConstraintPriority.REQUIRED,
                ),
            ),
        )
    }

    private fun sampleAnchor(
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
