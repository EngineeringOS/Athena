package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import com.engineeringood.athena.layout.SchematicRouteLanePreference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class SchematicRoutingModelTest {
    @Test
    fun `rule based schematic route strategy emits endpoint aware route facts`() {
        val request = sampleRouteRequest()
        val result = RuleBasedSchematicRouteStrategy().solve(
            SchematicRouteSnapshot(
                snapshotId = LayoutSnapshotId("snapshot:m21:routing"),
                family = ElectricalProjectionFamily.SCHEMATIC,
                requests = listOf(request),
            ),
        )

        assertEquals(LayoutSnapshotId("snapshot:m21:routing"), result.snapshotId)
        assertEquals(ElectricalProjectionFamily.SCHEMATIC, result.family)
        assertEquals(1, result.routeFacts.size)

        val route = result.routeFacts.single()
        assertEquals(SchematicRouteId("route:signal/sensor-to-plc"), route.routeId)
        assertEquals(request.source, route.source)
        assertEquals(request.target, route.target)
        assertEquals(SchematicRouteLane(0), route.lane)
        assertEquals(
            listOf(
                SchematicRouteSegment(
                    start = SchematicRoutePoint(x = 0, y = 40),
                    end = SchematicRoutePoint(x = 160, y = 40),
                    orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
                ),
                SchematicRouteSegment(
                    start = SchematicRoutePoint(x = 160, y = 40),
                    end = SchematicRoutePoint(x = 160, y = 160),
                    orientation = SchematicRouteSegmentOrientation.VERTICAL,
                ),
                SchematicRouteSegment(
                    start = SchematicRoutePoint(x = 160, y = 160),
                    end = SchematicRoutePoint(x = 320, y = 160),
                    orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
                ),
            ),
            route.segments,
        )
    }

    @Test
    fun `route facts remain deterministic across repeated runs`() {
        val snapshot = SchematicRouteSnapshot(
            snapshotId = LayoutSnapshotId("snapshot:m21:routing"),
            family = ElectricalProjectionFamily.SCHEMATIC,
            requests = listOf(
                sampleRouteRequest(),
                sampleRouteRequest(
                    routeId = SchematicRouteId("route:signal/plc-to-terminal"),
                    sourceEndpointId = SchematicEndpointId("endpoint:plc-1/output"),
                    targetEndpointId = SchematicEndpointId("endpoint:xt-1/input"),
                    source = SchematicRoutePoint(x = 320, y = 240),
                    target = SchematicRoutePoint(x = 560, y = 240),
                ),
            ),
        )
        val strategy = RuleBasedSchematicRouteStrategy()

        val first = strategy.solve(snapshot)
        val second = strategy.solve(snapshot)

        assertEquals(first, second)
        assertEquals(
            listOf(SchematicRouteLane(0), SchematicRouteLane(1)),
            first.routeFacts.map(SchematicRouteFact::lane),
        )
    }

    @Test
    fun `route facts avoid zero length segments for close diagonal endpoints`() {
        val route = RuleBasedSchematicRouteStrategy().solve(
            SchematicRouteSnapshot(
                snapshotId = LayoutSnapshotId("snapshot:m21:routing"),
                family = ElectricalProjectionFamily.SCHEMATIC,
                requests = listOf(
                    sampleRouteRequest(
                        source = SchematicRoutePoint(x = 0, y = 0),
                        target = SchematicRoutePoint(x = 1, y = 1),
                    ),
                ),
            ),
        ).routeFacts.single()

        assertEquals(
            listOf(
                SchematicRouteSegment(
                    start = SchematicRoutePoint(x = 0, y = 0),
                    end = SchematicRoutePoint(x = 1, y = 0),
                    orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
                ),
                SchematicRouteSegment(
                    start = SchematicRoutePoint(x = 1, y = 0),
                    end = SchematicRoutePoint(x = 1, y = 1),
                    orientation = SchematicRouteSegmentOrientation.VERTICAL,
                ),
            ),
            route.segments,
        )
    }

    @Test
    fun `route facts honor schematic lane preference without leaving schematic scope`() {
        val route = RuleBasedSchematicRouteStrategy().solve(
            SchematicRouteSnapshot(
                snapshotId = LayoutSnapshotId("snapshot:m22:routing"),
                family = ElectricalProjectionFamily.SCHEMATIC,
                requests = listOf(
                    sampleRouteRequest(
                        routeId = SchematicRouteId("route:signal/hmi-to-plc"),
                        source = SchematicRoutePoint(x = 40, y = 40),
                        target = SchematicRoutePoint(x = 280, y = 160),
                        lanePreference = SchematicRouteLanePreference.VERTICAL_FIRST,
                    ),
                ),
            ),
        ).routeFacts.single()

        assertEquals(SchematicRouteSegmentOrientation.VERTICAL, route.segments.first().orientation)
        assertEquals(SchematicRoutePoint(x = 40, y = 160), route.segments.first().end)
        assertEquals(SchematicRouteSegmentOrientation.HORIZONTAL, route.segments.last().orientation)
    }

    @Test
    fun `route facts preserve governed endpoint identity`() {
        val request = sampleRouteRequest()
        val route = RuleBasedSchematicRouteStrategy().solve(
            SchematicRouteSnapshot(
                snapshotId = LayoutSnapshotId("snapshot:m21:routing"),
                family = ElectricalProjectionFamily.SCHEMATIC,
                requests = listOf(request),
            ),
        ).routeFacts.single()

        assertEquals(SchematicEndpointId("endpoint:sensor-s1/out"), route.source.endpointId)
        assertEquals(StableSemanticIdentity("component:sensor-s1"), route.source.subjectId)
        assertEquals(LayoutOccurrenceId("occurrence:schematic:sensor-s1"), route.source.occurrenceId)
        assertEquals(SchematicEndpointId("endpoint:plc-1/input"), route.target.endpointId)
        assertEquals(StableSemanticIdentity("component:plc-1"), route.target.subjectId)
        assertEquals(LayoutOccurrenceId("occurrence:schematic:plc-1"), route.target.occurrenceId)
    }

    @Test
    fun `rule based schematic route strategy rejects non schematic snapshots`() {
        val snapshot = SchematicRouteSnapshot(
            snapshotId = LayoutSnapshotId("snapshot:m21:routing"),
            family = ElectricalProjectionFamily.DOCUMENTATION,
            requests = listOf(sampleRouteRequest()),
        )

        assertFailsWith<IllegalArgumentException> {
            RuleBasedSchematicRouteStrategy().solve(snapshot)
        }
    }

    @Test
    fun `rule based schematic route strategy rejects same point endpoints`() {
        val snapshot = SchematicRouteSnapshot(
            snapshotId = LayoutSnapshotId("snapshot:m21:routing"),
            family = ElectricalProjectionFamily.SCHEMATIC,
            requests = listOf(
                sampleRouteRequest(
                    source = SchematicRoutePoint(x = 40, y = 40),
                    target = SchematicRoutePoint(x = 40, y = 40),
                ),
            ),
        )

        assertFailsWith<IllegalArgumentException> {
            RuleBasedSchematicRouteStrategy().solve(snapshot)
        }
    }

    @Test
    fun `routing model boundary avoids deferred scope and renderer contracts`() {
        val source = readModuleSource()
        val forbiddenTerms = listOf(
            "css",
            "dom",
            "canvas",
            "cabinet",
            "harness",
            "cable tray",
            "3d installation",
            "physical",
            "desktop",
            "drag",
            "ai layout",
            "elk",
            "dagre",
            "graphviz",
        )

        forbiddenTerms.forEach { forbidden ->
            assertFalse(
                source.lowercase().contains(forbidden),
                "Story 3.2 routing contract must not introduce `$forbidden` scope.",
            )
        }
    }

    private fun sampleRouteRequest(
        routeId: SchematicRouteId = SchematicRouteId("route:signal/sensor-to-plc"),
        sourceEndpointId: SchematicEndpointId = SchematicEndpointId("endpoint:sensor-s1/out"),
        targetEndpointId: SchematicEndpointId = SchematicEndpointId("endpoint:plc-1/input"),
        source: SchematicRoutePoint = SchematicRoutePoint(x = 0, y = 40),
        target: SchematicRoutePoint = SchematicRoutePoint(x = 320, y = 160),
        lanePreference: SchematicRouteLanePreference? = null,
    ): SchematicRouteRequest {
        return SchematicRouteRequest(
            routeId = routeId,
            lanePreference = lanePreference,
            source = SchematicRouteEndpointRef(
                endpointId = sourceEndpointId,
                subjectId = StableSemanticIdentity("component:sensor-s1"),
                occurrenceId = LayoutOccurrenceId("occurrence:schematic:sensor-s1"),
                anchor = source,
            ),
            target = SchematicRouteEndpointRef(
                endpointId = targetEndpointId,
                subjectId = StableSemanticIdentity("component:plc-1"),
                occurrenceId = LayoutOccurrenceId("occurrence:schematic:plc-1"),
                anchor = target,
            ),
        )
    }

    private fun readModuleSource(): String {
        val repoRoot = generateSequence(java.nio.file.Path.of("").toAbsolutePath()) { path -> path.parent }
            .first { path -> java.nio.file.Files.exists(path.resolve("settings.gradle.kts")) }
        return java.nio.file.Files.readString(
            repoRoot.resolve("kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/SchematicRoutingModel.kt"),
        )
    }
}
