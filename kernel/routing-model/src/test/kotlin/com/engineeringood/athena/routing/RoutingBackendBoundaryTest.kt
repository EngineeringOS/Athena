package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class RoutingBackendBoundaryTest {
    @Test
    fun `Athena v0 backend boundary normalizes deterministic route facts without authority drift`() {
        val input = sampleInput(
            listOf(
                sampleRequest(
                    routeId = SchematicRouteId("route:z"),
                    connectionId = ElectricalConnectionId("connection:z"),
                    sourceAnchor = sampleAnchor("anchor:z:source", "component:PLC1", "DO1", TerminalSide.RIGHT, 120, 80),
                    targetAnchor = sampleAnchor("anchor:z:target", "component:XT1", "1", TerminalSide.LEFT, 320, 80),
                ),
                sampleRequest(
                    routeId = SchematicRouteId("route:a"),
                    connectionId = ElectricalConnectionId("connection:a"),
                    sourceAnchor = sampleAnchor("anchor:a:source", "component:PS1", "L+", TerminalSide.RIGHT, 120, 160),
                    targetAnchor = sampleAnchor("anchor:a:target", "component:QF1", "LINE", TerminalSide.LEFT, 320, 160),
                ),
            ),
        )

        val snapshot = RoutingBackendBoundary().solve(input)

        assertEquals(LayoutSnapshotId("snapshot:m27:routing-backend"), snapshot.snapshotId)
        assertEquals("schematic", snapshot.family)
        assertEquals(listOf(SchematicRouteId("route:a"), SchematicRouteId("route:z")), snapshot.routeFacts.map(RouteFact::routeId))
        assertFalse(snapshot.routeFacts.any(RouteFact::hasCanvasTruth))
    }

    @Test
    fun `backend boundary rejects backend authority claims`() {
        val input = sampleInput(listOf(sampleRequest()))
        val unsafeBackend = object : RoutingBackendAdapter {
            override val backendId: RoutingBackendId = RoutingBackendId("unsafe-external-router")

            override fun solve(input: AthenaRouteEngineInput): RoutingBackendResult = RoutingBackendResult(
                backendId = backendId,
                routeFacts = AthenaV0RoutingBackendAdapter().solve(input).routeFacts,
                authorityClaims = RoutingBackendAuthorityClaims(ownsSemanticConnectionMeaning = true),
            )
        }

        assertFailsWith<IllegalArgumentException> {
            RoutingBackendBoundary(unsafeBackend).solve(input)
        }
    }

    @Test
    fun `backend boundary rejects route facts for unrequested routes`() {
        val input = sampleInput(listOf(sampleRequest()))
        val unsafeBackend = object : RoutingBackendAdapter {
            override val backendId: RoutingBackendId = RoutingBackendId("invented-route-router")

            override fun solve(input: AthenaRouteEngineInput): RoutingBackendResult {
                val routeFact = AthenaV0RoutingBackendAdapter().solve(input).routeFacts.single()
                return RoutingBackendResult(
                    backendId = backendId,
                    routeFacts = listOf(routeFact.copy(routeId = SchematicRouteId("route:invented"))),
                )
            }
        }

        assertFailsWith<IllegalArgumentException> {
            RoutingBackendBoundary(unsafeBackend).solve(input)
        }
    }

    private fun sampleInput(requests: List<AthenaRouteRequest>): AthenaRouteEngineInput = AthenaRouteEngineInput(
        snapshotId = LayoutSnapshotId("snapshot:m27:routing-backend"),
        layoutContext = SchematicRoutingLayoutContext(gridSize = 20),
        requests = requests,
    )

    private fun sampleRequest(
        routeId: SchematicRouteId = SchematicRouteId("route:a"),
        connectionId: ElectricalConnectionId = ElectricalConnectionId("connection:a"),
        sourceAnchor: TerminalAnchorFact = sampleAnchor("anchor:a:source", "component:A", "out", TerminalSide.RIGHT, 120, 80),
        targetAnchor: TerminalAnchorFact = sampleAnchor("anchor:a:target", "component:B", "in", TerminalSide.LEFT, 320, 80),
    ): AthenaRouteRequest = AthenaRouteRequest(
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

    private fun sampleAnchor(
        anchorId: String,
        subjectId: String,
        portId: String,
        side: TerminalSide,
        x: Int,
        y: Int,
    ): TerminalAnchorFact = TerminalAnchorFact(
        anchorId = TerminalAnchorId(anchorId),
        subjectId = StableSemanticIdentity(subjectId),
        occurrenceId = LayoutOccurrenceId("occurrence:$subjectId"),
        portId = ElectricalPortId(portId),
        portRole = ElectricalPortRole.OUTPUT,
        side = side,
        point = SchematicRoutePoint(x = x, y = y),
        gridPoint = SchematicRoutePoint(x = x, y = y),
        policySource = "m27:routing-backend-boundary",
    )
}
