package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteQualityDiagnosticsTest {
    @Test
    fun `satisfied routes stay clean while fallback routes publish diagnostics and inspection payloads`() {
        val snapshotId = LayoutSnapshotId("snapshot:m24:route-quality")
        val fallbackConstraintId = RouteConstraintId("constraint:route:fallback:preferred-side-stub")
        val snapshot = RouteFactSnapshot.canonical(
            snapshotId = snapshotId,
            family = "schematic",
            routeFacts = listOf(
                routeFact(
                    snapshotId = snapshotId,
                    connectionId = ElectricalConnectionId("connection:test:plc1_do1_to_hmi1_in1"),
                    routeId = SchematicRouteId("route:satisfied"),
                    quality = RouteQuality.satisfied(),
                ),
                routeFact(
                    snapshotId = snapshotId,
                    connectionId = ElectricalConnectionId("connection:test:plc1_do2_to_xt1_1"),
                    routeId = SchematicRouteId("route:fallback"),
                    quality = RouteQuality.fallback(
                        failedConstraintIds = listOf(fallbackConstraintId),
                        message = "Route fell back because preferred terminal side was outside sheet bounds.",
                    ),
                ),
            ),
        )

        val diagnostics = RouteQualityDiagnosticPublisher().diagnosticsFor(snapshot)
        val inspection = RouteQualityDiagnosticPublisher().inspectionPayloadFor(snapshot)

        assertEquals(1, diagnostics.size)
        assertEquals(ElectricalConnectionId("connection:test:plc1_do2_to_xt1_1"), diagnostics.single().connectionId)
        assertEquals(SchematicRouteId("route:fallback"), diagnostics.single().routeId)
        assertEquals(RouteQualityState.DEGRADED, diagnostics.single().qualityState)
        assertEquals(listOf(fallbackConstraintId), diagnostics.single().failedConstraintIds)
        assertEquals(listOf(RouteConstraintKind.PREFERRED_EXIT_SIDE), diagnostics.single().failedConstraintFamilies)
        assertTrue(diagnostics.single().message.contains("preferred terminal side"))
        assertEquals(2, inspection.routes.size)
        val satisfiedInspection = inspection.routes.first { route -> route.routeId == SchematicRouteId("route:satisfied") }
        val fallbackInspection = inspection.routes.first { route -> route.routeId == SchematicRouteId("route:fallback") }
        assertEquals(RouteQualityState.SATISFIED, satisfiedInspection.qualityState)
        assertEquals(ElectricalPortId("PLC1.DO1"), satisfiedInspection.sourcePortId)
        assertEquals(ElectricalPortId("HMI1.IN1"), satisfiedInspection.targetPortId)
        assertEquals(StableSemanticIdentity("port:PLC1.DO1"), satisfiedInspection.sourcePortSemanticId)
        assertEquals(StableSemanticIdentity("port:HMI1.IN1"), satisfiedInspection.targetPortSemanticId)
        assertEquals("route-fact:SATISFIED:1-segment", satisfiedInspection.policySummary)
        assertEquals(RouteQualityState.DEGRADED, fallbackInspection.qualityState)
        assertEquals("route-fact:DEGRADED:1-segment", fallbackInspection.policySummary)
    }

    private fun routeFact(
        snapshotId: LayoutSnapshotId,
        connectionId: ElectricalConnectionId,
        routeId: SchematicRouteId,
        quality: RouteQuality,
    ): RouteFact {
        val source = anchor("PLC1", "DO1", TerminalSide.RIGHT, 320, 180)
        val target = anchor("HMI1", "IN1", TerminalSide.LEFT, 520, 180)
        return RouteFact(
            routeId = routeId,
            snapshotId = snapshotId,
            connectionId = connectionId,
            routeIntentId = RouteIntentId("intent:${connectionId.value}"),
            bundleId = RouteBundleId("bundle:${connectionId.value}"),
            selectedChannelIds = listOf("channel:main"),
            plannerId = "athena-native",
            compilerSnapshotId = "compiler:${routeId.value}",
            provenance = SourceProvenance("routes.athena", 1, 1, 1, 32),
            qualityMetrics = RouteQualityMetrics(
                crossingCount = 0,
                bendCount = 0,
                length = 200,
                channelChangeCount = 0,
                bundleContinuityPenalty = 0,
                labelClearanceViolationCount = 0,
            ),
            source = source,
            target = target,
            segments = listOf(
                SchematicRouteSegment(
                    start = source.gridPoint,
                    end = target.gridPoint,
                    orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
                ),
            ),
            constraints = listOf(
                RouteConstraint(
                    constraintId = RouteConstraintId("constraint:${routeId.value}:preferred-side-stub"),
                    kind = RouteConstraintKind.PREFERRED_EXIT_SIDE,
                    connectionId = connectionId,
                ),
            ),
            quality = quality,
        )
    }

    private fun anchor(
        subject: String,
        port: String,
        side: TerminalSide,
        x: Int,
        y: Int,
    ): TerminalAnchorFact {
        return TerminalAnchorFact(
            anchorId = TerminalAnchorId("anchor:$subject:$port"),
            subjectId = StableSemanticIdentity("component:$subject"),
            occurrenceId = LayoutOccurrenceId("occurrence:component:$subject"),
            portId = ElectricalPortId("$subject.$port"),
            portSemanticId = StableSemanticIdentity("port:$subject.$port"),
            portRole = ElectricalPortRole.OUTPUT,
            side = side,
            point = SchematicRoutePoint(x = x, y = y),
            gridPoint = SchematicRoutePoint(x = x, y = y),
        )
    }
}
