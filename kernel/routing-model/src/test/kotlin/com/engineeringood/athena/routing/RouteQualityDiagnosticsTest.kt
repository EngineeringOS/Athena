package com.engineeringood.athena.routing

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
                    connectionId = ElectricalConnectionId("connection:PLC1.DO1->HMI1.IN1"),
                    routeId = SchematicRouteId("route:satisfied"),
                    quality = RouteQuality.satisfied(),
                ),
                routeFact(
                    snapshotId = snapshotId,
                    connectionId = ElectricalConnectionId("connection:PLC1.DO2->XT1.1"),
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
        assertEquals(ElectricalConnectionId("connection:PLC1.DO2->XT1.1"), diagnostics.single().connectionId)
        assertEquals(SchematicRouteId("route:fallback"), diagnostics.single().routeId)
        assertEquals(RouteQualityState.FALLBACK, diagnostics.single().qualityState)
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
        assertEquals("m24:route-fact:SATISFIED:1-segment", satisfiedInspection.policySummary)
        assertEquals(RouteQualityState.FALLBACK, fallbackInspection.qualityState)
        assertEquals("m24:route-fact:FALLBACK:1-segment", fallbackInspection.policySummary)
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
