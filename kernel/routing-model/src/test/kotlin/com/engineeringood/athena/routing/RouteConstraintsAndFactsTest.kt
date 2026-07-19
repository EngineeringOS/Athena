package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RouteConstraintsAndFactsTest {
    @Test
    fun `route constraint vocabulary covers schematic routing preferences`() {
        assertTrue(RouteConstraintKind.entries.contains(RouteConstraintKind.ORTHOGONAL_ONLY))
        assertTrue(RouteConstraintKind.entries.contains(RouteConstraintKind.GRID_SNAP))
        assertTrue(RouteConstraintKind.entries.contains(RouteConstraintKind.AVOID_NODE))
        assertTrue(RouteConstraintKind.entries.contains(RouteConstraintKind.PREFERRED_EXIT_SIDE))
        assertTrue(RouteConstraintKind.entries.contains(RouteConstraintKind.PREFERRED_ENTRY_SIDE))
        assertTrue(RouteConstraintKind.entries.contains(RouteConstraintKind.ROUTE_LANE))
        assertTrue(RouteConstraintKind.entries.contains(RouteConstraintKind.ROUTE_BUNDLE))
        assertTrue(RouteConstraintKind.entries.contains(RouteConstraintKind.TERMINAL_ORDER))
        assertTrue(RouteConstraintKind.entries.contains(RouteConstraintKind.CROSSING_POLICY))
        assertTrue(RouteConstraintKind.entries.contains(RouteConstraintKind.LABEL_CLEARANCE))
    }

    @Test
    fun `route facts canonical snapshot sorts facts deterministically`() {
        val first = sampleRouteFact(
            routeId = SchematicRouteId("route:z"),
            connectionId = ElectricalConnectionId("connection:z"),
            sourceAnchorId = "anchor:z:source",
            targetAnchorId = "anchor:z:target",
        )
        val second = sampleRouteFact(
            routeId = SchematicRouteId("route:a"),
            connectionId = ElectricalConnectionId("connection:a"),
            sourceAnchorId = "anchor:a:source",
            targetAnchorId = "anchor:a:target",
        )

        val snapshot = RouteFactSnapshot.canonical(
            snapshotId = LayoutSnapshotId("snapshot:m24:route"),
            family = "schematic",
            routeFacts = listOf(first, second),
        )

        assertEquals(listOf(second, first), snapshot.routeFacts)
    }

    @Test
    fun `route facts preserve equality and quality state without canvas truth`() {
        val fact = sampleRouteFact()
        val copy = fact.copy()

        assertEquals(fact, copy)
        assertEquals(RouteQualityState.SATISFIED, fact.quality.state)
        assertFalse(fact.quality.isDegraded)
        assertFalse(fact.hasCanvasTruth())
    }

    private fun sampleRouteFact(
        routeId: SchematicRouteId = SchematicRouteId("route:a"),
        connectionId: ElectricalConnectionId = ElectricalConnectionId("connection:a"),
        sourceAnchorId: String = "anchor:a:source",
        targetAnchorId: String = "anchor:a:target",
    ): RouteFact {
        return RouteFact(
            routeId = routeId,
            snapshotId = LayoutSnapshotId("snapshot:m24:route"),
            connectionId = connectionId,
            source = sampleAnchor(sourceAnchorId, "component:A", "port:A.out"),
            target = sampleAnchor(targetAnchorId, "component:B", "port:B.in"),
            segments = listOf(
                SchematicRouteSegment(
                    start = SchematicRoutePoint(x = 0, y = 0),
                    end = SchematicRoutePoint(x = 40, y = 0),
                    orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
                ),
            ),
            constraints = listOf(
                RouteConstraint(
                    constraintId = RouteConstraintId("constraint:${routeId.value}"),
                    kind = RouteConstraintKind.ORTHOGONAL_ONLY,
                    connectionId = connectionId,
                ),
            ),
            labels = listOf(
                RouteLabelFact(
                    labelId = SchematicLabelId("label:${routeId.value}"),
                    text = routeId.value,
                    anchorRouteId = routeId,
                    placement = SchematicLabelPlacement(
                        origin = SchematicRoutePoint(x = 20, y = 20),
                        relation = SchematicLabelAnchorRelation.ABOVE,
                    ),
                ),
            ),
            quality = RouteQuality.satisfied(),
        )
    }

    private fun sampleAnchor(
        anchorId: String,
        subjectId: String,
        portId: String,
    ): TerminalAnchorFact {
        return TerminalAnchorFact(
            anchorId = TerminalAnchorId(anchorId),
            subjectId = StableSemanticIdentity(subjectId),
            occurrenceId = LayoutOccurrenceId("occurrence:$subjectId"),
            portId = ElectricalPortId(portId),
            portRole = ElectricalPortRole.INPUT,
            side = TerminalSide.LEFT,
            point = SchematicRoutePoint(x = 0, y = 0),
            gridPoint = SchematicRoutePoint(x = 0, y = 0),
            policySource = "m24:schematic-default",
        )
    }
}
