package com.engineeringood.athena.projection

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.ConnectionEndpointRole
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ConnectionProjectionContractTest {
    @Test
    fun `projection keeps canonical identity and logical constraints without geometry`() {
        val projection = ConnectionProjection(
            projectionId = ConnectionProjectionId("view/sheet/01/connection/C1"),
            semanticId = StableSemanticIdentity("connection:C1"),
            identityKind = ConnectionProjectionIdentityKind.CONNECTION,
            role = ConnectionProjectionRole.CONNECTION,
            originGeometryElementId = GeometryElementId("source:C1"),
            participants = listOf(
                ConnectionProjectionParticipant(ConnectionEndpointRole.SOURCE, ConnectionProjectionEndpoint(ProjectionOccurrencePortId(ProjectionNodeId("Q1"), StableSemanticIdentity("Q1.out")))),
                ConnectionProjectionParticipant(ConnectionEndpointRole.SINK, ConnectionProjectionEndpoint(ProjectionOccurrencePortId(ProjectionNodeId("M1"), StableSemanticIdentity("M1.in")))),
            ),
            logicalRouteConstraints = listOf(
                LogicalRouteConstraint.Via(
                    targetId = LogicalRouteTargetId("bend-main"),
                    point = LogicalRoutePoint(column = 24, row = 16),
                ),
            ),
        )

        assertTrue(projection.semanticId.value == "connection:C1")
        val constraint = projection.logicalRouteConstraints.single() as LogicalRouteConstraint.Via
        assertEquals("bend-main", constraint.targetId.value)
        assertEquals(LogicalRoutePoint(column = 24, row = 16), constraint.point)
        val fields = ConnectionProjection::class.java.declaredFields.map { it.name }.toSet()
        assertTrue("points" !in fields && "x" !in fields && "y" !in fields && "style" !in fields)
    }

    @Test
    fun `projection rejects duplicate stable logical route targets`() {
        val endpointA = ConnectionProjectionEndpoint(
            ProjectionOccurrencePortId(ProjectionNodeId("Q1"), StableSemanticIdentity("Q1.out")),
        )
        val endpointB = ConnectionProjectionEndpoint(
            ProjectionOccurrencePortId(ProjectionNodeId("M1"), StableSemanticIdentity("M1.in")),
        )
        assertFailsWith<IllegalArgumentException> {
            ConnectionProjection(
                projectionId = ConnectionProjectionId("view/connection/C1"),
                semanticId = StableSemanticIdentity("connection:C1"),
                originGeometryElementId = GeometryElementId("source:C1"),
                participants = listOf(
                    ConnectionProjectionParticipant(ConnectionEndpointRole.SOURCE, endpointA),
                    ConnectionProjectionParticipant(ConnectionEndpointRole.SINK, endpointB),
                ),
                logicalRouteConstraints = listOf(
                    LogicalRouteConstraint.Via(LogicalRouteTargetId("bend-main"), LogicalRoutePoint(24, 16)),
                    LogicalRouteConstraint.Via(LogicalRouteTargetId("bend-main"), LogicalRoutePoint(32, 16)),
                ),
            )
        }
    }

    @Test
    fun `projection rejects duplicate selected occurrence ports`() {
        val endpoint = ConnectionProjectionEndpoint(
            ProjectionOccurrencePortId(ProjectionNodeId("Q1"), StableSemanticIdentity("Q1.out")),
        )
        assertFailsWith<IllegalArgumentException> {
            ConnectionProjection(
                projectionId = ConnectionProjectionId("view/connection/C1"),
                semanticId = StableSemanticIdentity("connection:C1"),
                identityKind = ConnectionProjectionIdentityKind.CONNECTION,
                role = ConnectionProjectionRole.CONNECTION,
                originGeometryElementId = GeometryElementId("source:C1"),
                participants = listOf(
                    ConnectionProjectionParticipant(ConnectionEndpointRole.SOURCE, endpoint),
                    ConnectionProjectionParticipant(ConnectionEndpointRole.SINK, endpoint),
                ),
            )
        }
    }

    @Test
    fun `net projection accepts repeated endpoint roles on distinct occurrence ports`() {
        val projection = ConnectionProjection(
            projectionId = ConnectionProjectionId("view/net/Control24V"),
            semanticId = StableSemanticIdentity("net:Control24V"),
            identityKind = ConnectionProjectionIdentityKind.NET,
            role = ConnectionProjectionRole.NET,
            originGeometryElementId = GeometryElementId("source:Control24V"),
            participants = listOf(
                participant("PLC1", "PLC1.out", ConnectionEndpointRole.SOURCE),
                participant("KM1", "KM1.coil", ConnectionEndpointRole.SINK),
                participant("H1", "H1.in", ConnectionEndpointRole.SINK),
            ),
        )

        assertEquals(
            listOf(ConnectionEndpointRole.SOURCE, ConnectionEndpointRole.SINK, ConnectionEndpointRole.SINK),
            projection.participants.map { it.role },
        )
    }

    private fun participant(
        occurrence: String,
        port: String,
        role: ConnectionEndpointRole,
    ) = ConnectionProjectionParticipant(
        role,
        ConnectionProjectionEndpoint(
            ProjectionOccurrencePortId(ProjectionNodeId(occurrence), StableSemanticIdentity(port)),
        ),
    )
}
