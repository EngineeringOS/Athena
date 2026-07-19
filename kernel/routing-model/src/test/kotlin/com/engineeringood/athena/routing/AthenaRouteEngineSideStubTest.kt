package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertEquals

class AthenaRouteEngineSideStubTest {
    @Test
    fun `output and input anchors produce short side stubs before long route segments`() {
        val route = AthenaRouteEngineV0().solve(
            input(
                sourceAnchor = anchor("anchor:plc:do1", "component:PLC1", "DO1", ElectricalPortRole.OUTPUT, TerminalSide.RIGHT, 320, 180),
                targetAnchor = anchor("anchor:xt1:1", "component:XT1", "1", ElectricalPortRole.INPUT, TerminalSide.LEFT, 520, 260),
            ),
        ).routeFacts.single()

        assertEquals(
            SchematicRouteSegment(
                start = SchematicRoutePoint(x = 320, y = 180),
                end = SchematicRoutePoint(x = 340, y = 180),
                orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
            ),
            route.segments.first(),
        )
        assertEquals(
            SchematicRouteSegment(
                start = SchematicRoutePoint(x = 500, y = 260),
                end = SchematicRoutePoint(x = 520, y = 260),
                orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
            ),
            route.segments.last(),
        )
    }

    @Test
    fun `power and terminal block anchors use their preferred sides`() {
        val route = AthenaRouteEngineV0().solve(
            input(
                sourceAnchor = anchor("anchor:ps1:lplus", "component:PS1", "L+", ElectricalPortRole.POWER, TerminalSide.TOP, 160, 100),
                targetAnchor = anchor("anchor:xt1:pwr", "component:XT1", "L+", ElectricalPortRole.TERMINAL, TerminalSide.LEFT, 360, 220),
            ),
        ).routeFacts.single()

        assertEquals(
            SchematicRouteSegment(
                start = SchematicRoutePoint(x = 160, y = 100),
                end = SchematicRoutePoint(x = 160, y = 80),
                orientation = SchematicRouteSegmentOrientation.VERTICAL,
            ),
            route.segments.first(),
        )
        assertEquals(
            SchematicRouteSegment(
                start = SchematicRoutePoint(x = 340, y = 220),
                end = SchematicRoutePoint(x = 360, y = 220),
                orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
            ),
            route.segments.last(),
        )
    }

    @Test
    fun `stub fallback is explicit when preferred side would leave sheet bounds`() {
        val route = AthenaRouteEngineV0().solve(
            input(
                sourceAnchor = anchor("anchor:left-edge", "component:LEFT", "in", ElectricalPortRole.INPUT, TerminalSide.LEFT, 0, 100),
                targetAnchor = anchor("anchor:target", "component:T", "out", ElectricalPortRole.OUTPUT, TerminalSide.RIGHT, 200, 100),
            ),
        ).routeFacts.single()

        assertEquals(RouteQualityState.DEGRADED, route.quality.state)
        assertEquals(SchematicRoutePoint(x = 0, y = 100), route.segments.first().start)
    }

    private fun input(
        sourceAnchor: TerminalAnchorFact,
        targetAnchor: TerminalAnchorFact,
    ): AthenaRouteEngineInput {
        val connectionId = ElectricalConnectionId("connection:${sourceAnchor.portId.value}->${targetAnchor.portId.value}")
        return AthenaRouteEngineInput(
            snapshotId = LayoutSnapshotId("snapshot:m24:side-stubs"),
            layoutContext = SchematicRoutingLayoutContext(gridSize = 20),
            requests = listOf(
                AthenaRouteRequest(
                    routeId = SchematicRouteId("route:${sourceAnchor.portId.value}->${targetAnchor.portId.value}"),
                    connectionIntent = ElectricalConnectionIntent(
                        connectionId = connectionId,
                        sourceSubjectId = sourceAnchor.subjectId,
                        sourcePortId = sourceAnchor.portId,
                        targetSubjectId = targetAnchor.subjectId,
                        targetPortId = targetAnchor.portId,
                        role = ElectricalConnectionRole.CONTROL_SIGNAL,
                    ),
                    sourceAnchor = sourceAnchor,
                    targetAnchor = targetAnchor,
                    constraints = listOf(
                        RouteConstraint(
                            constraintId = RouteConstraintId("constraint:${connectionId.value}:side-stub"),
                            kind = RouteConstraintKind.PREFERRED_EXIT_SIDE,
                            connectionId = connectionId,
                            priority = RouteConstraintPriority.REQUIRED,
                        ),
                    ),
                ),
            ),
        )
    }

    private fun anchor(
        anchorId: String,
        subjectId: String,
        portId: String,
        role: ElectricalPortRole,
        side: TerminalSide,
        x: Int,
        y: Int,
    ): TerminalAnchorFact {
        return TerminalAnchorFact(
            anchorId = TerminalAnchorId(anchorId),
            subjectId = StableSemanticIdentity(subjectId),
            occurrenceId = LayoutOccurrenceId("occurrence:$subjectId"),
            portId = ElectricalPortId(portId),
            portRole = role,
            side = side,
            point = SchematicRoutePoint(x = x, y = y),
            gridPoint = SchematicRoutePoint(x = x, y = y),
            policySource = "m24:schematic-default",
        )
    }
}
