package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.ConnectionDocument
import com.engineeringood.athena.connection.ConnectionSourceTrace
import com.engineeringood.athena.connection.TopologyOperator
import com.engineeringood.athena.connection.TopologyOperatorKind
import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.spatial.ConnectionRoutePlan
import com.engineeringood.athena.spatial.ConnectionRoutePlanId
import com.engineeringood.athena.spatial.SpatialAnchorId
import com.engineeringood.athena.spatial.SpatialBoundarySide
import com.engineeringood.athena.spatial.SpatialLaneId
import com.engineeringood.athena.spatial.SpatialLaneOrientation
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialOccurrencePortSubject
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialSourceTrace
import com.engineeringood.athena.projection.ConnectionProjection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConnectionRouteTopologyPlannerTest {
    private val trace = SpatialSourceTrace(listOf("sheet:main"), listOf(GeometryElementId("geometry:route")))
    private val emptyProjection = emptyList<ConnectionProjection>()

    @Test
    fun `joined net perpendicular routes emit one junction and no crossing`() {
        val first = route("net:n1", "A", listOf(SpatialPoint(10, 50), SpatialPoint(90, 50)))
        val second = route("net:n1", "B", listOf(SpatialPoint(50, 10), SpatialPoint(50, 90)))
        val topology = ConnectionRouteTopologyPlanner().plan(connectionDocument(TopologyOperatorKind.BRANCH, "net:n1"), emptyProjection, listOf(first, second))

        assertEquals(1, topology.junctions.size)
        assertEquals(SpatialPoint(50, 50), topology.junctions.single().point)
        assertTrue(topology.crossings.isEmpty())
    }

    @Test
    fun `unrelated crossing emits bridge owner without junction`() {
        val first = route("net:n1", "A", listOf(SpatialPoint(10, 50), SpatialPoint(90, 50)))
        val second = route("net:n2", "B", listOf(SpatialPoint(50, 10), SpatialPoint(50, 90)))
        val topology = ConnectionRouteTopologyPlanner().plan(null, emptyProjection, listOf(first, second))

        assertTrue(topology.junctions.isEmpty())
        assertEquals(1, topology.crossings.size)
        assertEquals(first.routeId, topology.crossings.single().bridgeOwnerRouteId)
    }

    @Test
    fun `shared net segment is canonicalized once and interruption is paired`() {
        val first = route("net:n1", "A", listOf(SpatialPoint(10, 40), SpatialPoint(80, 40)))
        val second = route("net:n1", "B", listOf(SpatialPoint(10, 40), SpatialPoint(80, 40)))
        val topology = ConnectionRouteTopologyPlanner().plan(connectionDocument(TopologyOperatorKind.INTERRUPTION, "net:n1"), emptyProjection, listOf(first, second))

        assertEquals(1, topology.sharedSegments.size)
        assertEquals(1, topology.interruptions.size)
        assertEquals(listOf("continuation:A", "continuation:B"), topology.interruptions.single().continuationIds)
        assertFalse(topology.junctions.any { it.point == SpatialPoint(10, 40) })
    }

    private fun connectionDocument(kind: TopologyOperatorKind, netId: String): ConnectionDocument {
        val trace = ConnectionSourceTrace(SourceProvenance("source.athena", 1, 1, 1, 1))
        return ConnectionDocument.canonical(
            connections = emptyList(),
            nets = emptyList(),
            topologyOperators = listOf(
                TopologyOperator(
                    StableSemanticIdentity("operator:$netId:${kind.name.lowercase()}"),
                    kind,
                    StableSemanticIdentity(netId),
                    listOf(StableSemanticIdentity("continuation:A"), StableSemanticIdentity("continuation:B")),
                    trace,
                ),
            ),
        )
    }

    private fun route(semanticId: String, suffix: String, points: List<SpatialPoint>): ConnectionRoutePlan {
        val sourceOccurrence = SpatialOccurrenceId("sheet:main", "occurrence:${suffix}Source")
        val targetOccurrence = SpatialOccurrenceId("sheet:main", "occurrence:${suffix}Target")
        val sourcePort = StableSemanticIdentity("port:${suffix}.out")
        val targetPort = StableSemanticIdentity("port:${suffix}.in")
        return ConnectionRoutePlan(
            routeId = ConnectionRoutePlanId("sheet:main", "projection:$suffix"),
            sheetId = "sheet:main",
            connectionId = StableSemanticIdentity(semanticId),
            projectionConnectionId = "projection:$suffix",
            sourceAnchorId = SpatialAnchorId("sheet:main", sourceOccurrence, sourcePort),
            targetAnchorId = SpatialAnchorId("sheet:main", targetOccurrence, targetPort),
            points = points,
            sourceTrace = trace,
            laneId = SpatialLaneId("sheet:main", if (points.first().y == points.last().y) SpatialLaneOrientation.HORIZONTAL else SpatialLaneOrientation.VERTICAL, if (points.first().y == points.last().y) points.first().y else points.first().x),
        )
    }
}
