package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class M24RoutingContractTest {
    @Test
    fun `contracts represent governed terminal-anchor route proof`() {
        val connection = ElectricalConnectionIntent(
            connectionId = ElectricalConnectionId("connection:plc1-do1-to-xt1-1"),
            sourceSubjectId = StableSemanticIdentity("component:PLC1"),
            sourcePortId = ElectricalPortId("DO1"),
            targetSubjectId = StableSemanticIdentity("component:XT1"),
            targetPortId = ElectricalPortId("1"),
            role = ElectricalConnectionRole.CONTROL_SIGNAL,
            signalClass = ElectricalSignalClass.DIGITAL_OUTPUT,
        )
        val policy = RoutingPolicy(
            policyId = RoutingPolicyId("routing-policy:schematic-control-v0"),
            orthogonalOnly = true,
            gridSize = 20,
            defaultLaneSpacing = 40,
            portPresentationPolicy = PortPresentationPolicy(
                rules = listOf(
                    PortPresentationRule(
                        portRole = ElectricalPortRole.OUTPUT,
                        preferredSide = TerminalSide.RIGHT,
                    ),
                    PortPresentationRule(
                        portRole = ElectricalPortRole.INPUT,
                        preferredSide = TerminalSide.LEFT,
                    ),
                ),
            ),
        )
        val sourceAnchor = TerminalAnchorFact(
            anchorId = TerminalAnchorId("anchor:PLC1:DO1"),
            subjectId = StableSemanticIdentity("component:PLC1"),
            occurrenceId = LayoutOccurrenceId("occurrence:schematic:PLC1"),
            portId = ElectricalPortId("DO1"),
            portRole = ElectricalPortRole.OUTPUT,
            side = policy.preferredSideFor(ElectricalPortRole.OUTPUT),
            point = SchematicRoutePoint(x = 320, y = 180),
        )
        val targetAnchor = TerminalAnchorFact(
            anchorId = TerminalAnchorId("anchor:XT1:1"),
            subjectId = StableSemanticIdentity("component:XT1"),
            occurrenceId = LayoutOccurrenceId("occurrence:schematic:XT1"),
            portId = ElectricalPortId("1"),
            portRole = ElectricalPortRole.INPUT,
            side = policy.preferredSideFor(ElectricalPortRole.INPUT),
            point = SchematicRoutePoint(x = 520, y = 180),
        )
        val constraint = RouteConstraint(
            constraintId = RouteConstraintId("constraint:plc1-do1-to-xt1-1:orthogonal"),
            kind = RouteConstraintKind.ORTHOGONAL_ONLY,
            connectionId = connection.connectionId,
            priority = RouteConstraintPriority.REQUIRED,
        )
        val route = RouteFact(
            routeId = SchematicRouteId("route:plc1-do1-to-xt1-1"),
            snapshotId = LayoutSnapshotId("snapshot:m24:routing"),
            connectionId = connection.connectionId,
            source = sourceAnchor,
            target = targetAnchor,
            segments = listOf(
                SchematicRouteSegment(
                    start = sourceAnchor.point,
                    end = targetAnchor.point,
                    orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
                ),
            ),
            constraints = listOf(constraint),
            labels = listOf(
                RouteLabelFact(
                    labelId = SchematicLabelId("label:plc1-do1-to-xt1-1"),
                    text = "DO1 -> XT1:1",
                    anchorRouteId = SchematicRouteId("route:plc1-do1-to-xt1-1"),
                    placement = SchematicLabelPlacement(
                        origin = SchematicRoutePoint(x = 400, y = 160),
                        relation = SchematicLabelAnchorRelation.ABOVE,
                    ),
                ),
            ),
            quality = RouteQuality.satisfied(),
        )

        assertEquals(TerminalSide.RIGHT, sourceAnchor.side)
        assertEquals(TerminalSide.LEFT, targetAnchor.side)
        assertTrue(route.quality.isSatisfied)
        assertEquals(listOf(RouteConstraintKind.ORTHOGONAL_ONLY), route.constraints.map(RouteConstraint::kind))
        assertEquals("DO1 -> XT1:1", route.labels.single().text)
    }
}
