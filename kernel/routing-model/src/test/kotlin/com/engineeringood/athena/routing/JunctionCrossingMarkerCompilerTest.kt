package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class JunctionCrossingMarkerCompilerTest {
    private val compiler = JunctionCrossingMarkerCompiler()
    private val profile = DrawingStandardProfile.standardProfessional()

    @Test
    fun `explicit junction fact emits profile-selected junction marker`() {
        val snapshot = snapshot(
            junctionFacts = listOf(
                RouteJunctionFact(
                    junctionId = "junction:terminal:x1",
                    point = SchematicRoutePoint(50, 50),
                    routeIds = listOf(SchematicRouteId("route:a"), SchematicRouteId("route:b")),
                    semanticPortId = "terminal:X1.1",
                ),
            ),
        )

        val result = assertIs<JunctionCrossingMarkerCompilation.Success>(
            compiler.compile(snapshot, profile, selectedPolicyId = "ControlDrawingProjection"),
        )

        val marker = result.markers.single()
        assertEquals(JunctionCrossingMarkerKind.JUNCTION_DOT, marker.kind)
        assertEquals("marker:junction-dot", marker.markerClassId)
        assertEquals(listOf("route:a", "route:b"), marker.routeIds)
        assertEquals("terminal:X1.1", marker.semanticId)
        assertEquals("ControlDrawingProjection", marker.selectedPolicyId)
        assertEquals("snapshot:markers", marker.compilerSnapshotId)
    }

    @Test
    fun `explicit crossing fact emits disconnected crossing marker and never implies join`() {
        val snapshot = snapshot(
            crossingFacts = listOf(
                RouteCrossingFact(
                    crossingId = "crossing:route-a:route-b",
                    point = SchematicRoutePoint(70, 70),
                    routeIds = listOf(SchematicRouteId("route:a"), SchematicRouteId("route:b")),
                ),
            ),
        )

        val result = assertIs<JunctionCrossingMarkerCompilation.Success>(
            compiler.compile(snapshot, profile, selectedPolicyId = "ControlDrawingProjection"),
        )

        val marker = result.markers.single()
        assertEquals(JunctionCrossingMarkerKind.DISCONNECTED_CROSSING, marker.kind)
        assertEquals("marker:disconnected-crossing", marker.markerClassId)
        assertEquals(false, marker.joined)
    }

    @Test
    fun `contradictory junction and crossing evidence fails before rendering`() {
        val snapshot = snapshot(
            junctionFacts = listOf(
                RouteJunctionFact(
                    junctionId = "junction:terminal:x1",
                    point = SchematicRoutePoint(50, 50),
                    routeIds = listOf(SchematicRouteId("route:a"), SchematicRouteId("route:b")),
                    semanticPortId = "terminal:X1.1",
                ),
            ),
            crossingFacts = listOf(
                RouteCrossingFact(
                    crossingId = "crossing:route-a:route-b",
                    point = SchematicRoutePoint(50, 50),
                    routeIds = listOf(SchematicRouteId("route:a"), SchematicRouteId("route:b")),
                ),
            ),
        )

        val diagnostics = assertIs<JunctionCrossingMarkerCompilation.Failure>(
            compiler.compile(snapshot, profile, selectedPolicyId = "ControlDrawingProjection"),
        ).diagnostics

        assertContains(diagnostics.map { diagnostic -> diagnostic.code }, "drawing.marker.topology.contradictory")
    }

    @Test
    fun `marker payload carries compiled facts only`() {
        val snapshot = snapshot(
            crossingFacts = listOf(
                RouteCrossingFact(
                    crossingId = "crossing:route-a:route-b",
                    point = SchematicRoutePoint(70, 70),
                    routeIds = listOf(SchematicRouteId("route:a"), SchematicRouteId("route:b")),
                ),
            ),
        )
        val result = assertIs<JunctionCrossingMarkerCompilation.Success>(
            compiler.compile(snapshot, profile, selectedPolicyId = "ControlDrawingProjection"),
        )

        val payload = compiler.normalize(result)

        assertEquals("athena", payload.authority)
        assertEquals(listOf("crossing:route-a:route-b"), payload.markers.map { marker -> marker.markerId })
        assertTrue(payload.rendererTopologyInferences.isEmpty())
        assertTrue(payload.rawGeometryFragments.isEmpty())
    }

    private fun snapshot(
        junctionFacts: List<RouteJunctionFact> = emptyList(),
        crossingFacts: List<RouteCrossingFact> = emptyList(),
    ): RouteFactSnapshot {
        return RouteFactSnapshot.canonical(
            snapshotId = LayoutSnapshotId("snapshot:markers"),
            family = "schematic",
            routeFacts = listOf(routeFact("route:a"), routeFact("route:b")),
            junctionFacts = junctionFacts,
            crossingFacts = crossingFacts,
        )
    }

    private fun routeFact(routeIdValue: String): RouteFact {
        val routeId = SchematicRouteId(routeIdValue)
        val connectionId = ElectricalConnectionId("connection:${routeIdValue.substringAfter(':')}")
        return RouteFact(
            routeId = routeId,
            snapshotId = LayoutSnapshotId("snapshot:markers"),
            connectionId = connectionId,
            routeIntentId = RouteIntentId("route-intent:${routeIdValue.substringAfter(':')}"),
            bundleId = RouteBundleId("bundle:${routeIdValue.substringAfter(':')}"),
            selectedChannelIds = emptyList(),
            plannerId = "athena-route-engine",
            compilerSnapshotId = "snapshot:markers",
            provenance = SourceProvenance("routes.athena", 1, 1, 1, 20),
            qualityMetrics = RouteQualityMetrics(0, 0, 60, 0, 0, 0),
            source = anchor("source:$routeIdValue", 20, 20),
            target = anchor("target:$routeIdValue", 80, 20),
            segments = listOf(
                SchematicRouteSegment(
                    start = SchematicRoutePoint(20, 20),
                    end = SchematicRoutePoint(80, 20),
                    orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
                ),
            ),
        )
    }

    private fun anchor(name: String, x: Int, y: Int): TerminalAnchorFact {
        return TerminalAnchorFact(
            anchorId = TerminalAnchorId("anchor:$name"),
            subjectId = StableSemanticIdentity("component:$name"),
            occurrenceId = LayoutOccurrenceId("occurrence:$name"),
            portId = ElectricalPortId("port:$name"),
            portRole = ElectricalPortRole.OUTPUT,
            side = TerminalSide.RIGHT,
            point = SchematicRoutePoint(x, y),
            gridPoint = SchematicRoutePoint(x, y),
            policySource = "marker-test",
        )
    }
}
