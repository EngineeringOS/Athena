package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RouteLabelPlacementCompilerTest {
    private val compiler = RouteLabelPlacementCompiler()

    @Test
    fun `accepted route label carries bounds class attachment provenance and snapshot`() {
        val snapshot = snapshotWithLabels(
            routeLabel("label:route:a", "K1:A1", SchematicRoutePoint(30, 20)),
        )

        val result = compiler.compile(
            RouteLabelPlacementRequest(
                snapshot = snapshot,
                frameBounds = DrawingBounds(0, 0, 200, 120),
            ),
        )

        assertTrue(result.successful)
        val label = result.labels.single()
        assertEquals(SchematicLabelId("label:route:a"), label.labelId)
        assertEquals("K1:A1", label.text)
        assertEquals(DrawingBounds(15, 15, 30, 10), label.bounds)
        assertEquals(SchematicRoutePoint(30, 20), label.attachmentPoint)
        assertEquals("label:route", label.labelClassId)
        assertEquals(emptyList(), label.collisions)
        assertEquals("snapshot:labels", label.compilerSnapshotId)
        assertEquals(SourceProvenance("routes.athena", 1, 1, 1, 20), label.provenance)
    }

    @Test
    fun `label collisions are diagnosed against drawing structures`() {
        val snapshot = snapshotWithLabels(
            routeLabel("label:component", "CMP", SchematicRoutePoint(20, 20)),
            routeLabel("label:route", "RTE", SchematicRoutePoint(60, 50)),
            routeLabel("label:a", "A", SchematicRoutePoint(90, 20)),
            routeLabel("label:b", "B", SchematicRoutePoint(90, 20)),
            routeLabel("label:frame", "OUT", SchematicRoutePoint(0, 0)),
            routeLabel("label:grid", "GRID", SchematicRoutePoint(120, 20)),
            routeLabel("label:title", "TITLE", SchematicRoutePoint(150, 90)),
        )

        val result = compiler.compile(
            RouteLabelPlacementRequest(
                snapshot = snapshot,
                frameBounds = DrawingBounds(10, 10, 20, 8),
                componentBounds = listOf(DrawingBounds(15, 15, 40, 30)),
                routeSegments = listOf(
                    SchematicRouteSegment(
                        start = SchematicRoutePoint(50, 50),
                        end = SchematicRoutePoint(120, 50),
                        orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
                    ),
                ),
                gridLabelBounds = listOf(DrawingBounds(118, 18, 40, 14)),
                titleBlockBounds = listOf(DrawingBounds(145, 85, 60, 24)),
            ),
        )

        assertFalse(result.successful)
        val codes = result.diagnostics.map { diagnostic -> diagnostic.code }
        assertContains(codes, "drawing.label.collision.frame")
        assertTrue(codes.isNotEmpty())
    }

    @Test
    fun `label placement separates colliding route labels when structure stays clear`() {
        val snapshot = snapshotWithLabels(
            routeLabel("label:a", "MOTOR_UP", SchematicRoutePoint(90, 20)),
            routeLabel("label:b", "MOTOR_DOWN", SchematicRoutePoint(90, 20)),
        )

        val result = compiler.compile(
            RouteLabelPlacementRequest(
                snapshot = snapshot,
                frameBounds = DrawingBounds(0, 0, 240, 120),
            ),
        )

        assertTrue(result.successful)
        assertEquals(2, result.labels.map { label -> label.bounds }.toSet().size)
        assertTrue(result.labels.all { label -> label.collisions.isEmpty() })
    }

    @Test
    fun `label placement moves route labels away from terminal text bounds`() {
        val snapshot = snapshotWithLabels(
            routeLabel("label:route:control", "control_supply", SchematicRoutePoint(90, 20)),
        )

        val result = compiler.compile(
            RouteLabelPlacementRequest(
                snapshot = snapshot,
                frameBounds = DrawingBounds(0, 0, 240, 120),
                textBounds = listOf(DrawingBounds(90, 20, 28, 10)),
            ),
        )

        assertTrue(result.successful)
        val label = result.labels.single()
        assertEquals(emptyList(), label.collisions)
        assertFalse(label.bounds.intersects(DrawingBounds(90, 20, 28, 10)))
        assertFalse(label.attachmentPoint == SchematicRoutePoint(90, 20))
    }

    @Test
    fun `label placement is deterministic`() {
        val request = RouteLabelPlacementRequest(
            snapshot = snapshotWithLabels(routeLabel("label:route:a", "K1:A1", SchematicRoutePoint(30, 20))),
            frameBounds = DrawingBounds(0, 0, 200, 120),
        )

        val first = compiler.compile(request)
        val second = compiler.compile(request)

        assertEquals(first, second)
    }

    @Test
    fun `unresolved label blocks success`() {
        val result = compiler.compile(
            RouteLabelPlacementRequest(
                snapshot = snapshotWithLabels(
                    RouteLabelFact(
                        labelId = SchematicLabelId("label:bad"),
                        text = "BAD",
                        anchorRouteId = SchematicRouteId("route:missing"),
                        placement = SchematicLabelPlacement(SchematicRoutePoint(20, 20), SchematicLabelAnchorRelation.ABOVE),
                    ),
                ),
                frameBounds = DrawingBounds(0, 0, 200, 120),
            ),
        )

        assertFalse(result.successful)
        assertContains(result.diagnostics.map { diagnostic -> diagnostic.code }, "drawing.label.unresolved")
    }

    @Test
    fun `label payload contains compiled label facts only`() {
        val result = compiler.compile(
            RouteLabelPlacementRequest(
                snapshot = snapshotWithLabels(routeLabel("label:route:a", "K1:A1", SchematicRoutePoint(30, 20))),
                frameBounds = DrawingBounds(0, 0, 200, 120),
            ),
        )

        val payload = compiler.normalize(result)

        assertEquals("athena", payload.authority)
        assertEquals(listOf("label:route:a"), payload.labels.map { label -> label.labelId })
        assertTrue(payload.rendererPlacements.isEmpty())
        assertTrue(payload.rawMarkupFragments.isEmpty())
    }

    private fun snapshotWithLabels(vararg labels: RouteLabelFact): RouteFactSnapshot {
        val route = routeFact(labels.toList())
        return RouteFactSnapshot.canonical(
            snapshotId = LayoutSnapshotId("snapshot:labels"),
            family = "schematic",
            routeFacts = listOf(route),
        )
    }

    private fun routeFact(labels: List<RouteLabelFact>): RouteFact {
        return RouteFact(
            routeId = SchematicRouteId("route:a"),
            snapshotId = LayoutSnapshotId("snapshot:labels"),
            connectionId = ElectricalConnectionId("connection:a"),
            routeIntentId = RouteIntentId("route-intent:a"),
            bundleId = RouteBundleId("bundle:a"),
            selectedChannelIds = emptyList(),
            plannerId = "athena-route-engine",
            compilerSnapshotId = "snapshot:labels",
            provenance = SourceProvenance("routes.athena", 1, 1, 1, 20),
            qualityMetrics = RouteQualityMetrics(0, 0, 60, 0, 0, 0),
            source = TerminalAnchorFact(
                anchorId = TerminalAnchorId("anchor:source"),
                subjectId = com.engineeringood.athena.ir.StableSemanticIdentity("component:source"),
                occurrenceId = com.engineeringood.athena.layout.LayoutOccurrenceId("occurrence:source"),
                portId = ElectricalPortId("port:source"),
                portRole = ElectricalPortRole.OUTPUT,
                side = TerminalSide.RIGHT,
                point = SchematicRoutePoint(20, 20),
            ),
            target = TerminalAnchorFact(
                anchorId = TerminalAnchorId("anchor:target"),
                subjectId = com.engineeringood.athena.ir.StableSemanticIdentity("component:target"),
                occurrenceId = com.engineeringood.athena.layout.LayoutOccurrenceId("occurrence:target"),
                portId = ElectricalPortId("port:target"),
                portRole = ElectricalPortRole.INPUT,
                side = TerminalSide.LEFT,
                point = SchematicRoutePoint(80, 20),
            ),
            segments = listOf(
                SchematicRouteSegment(
                    start = SchematicRoutePoint(20, 20),
                    end = SchematicRoutePoint(80, 20),
                    orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
                ),
            ),
            labels = labels,
        )
    }

    private fun routeLabel(id: String, text: String, origin: SchematicRoutePoint): RouteLabelFact =
        RouteLabelFact(
            labelId = SchematicLabelId(id),
            text = text,
            anchorRouteId = SchematicRouteId("route:a"),
            placement = SchematicLabelPlacement(origin, SchematicLabelAnchorRelation.ABOVE),
        )
}
