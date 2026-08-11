package com.engineeringood.athena.spatial

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConnectionRouteTopologyContractTest {
    private val firstRoute = ConnectionRoutePlanId("sheet:main", "projection:A")
    private val secondRoute = ConnectionRoutePlanId("sheet:main", "projection:B")

    @Test
    fun `junction and crossing remain distinct explicit topology facts`() {
        val point = SpatialPoint(50, 50)
        val junction = ConnectionJunction(
            id = ConnectionJunctionId("sheet:main", "net:control", point),
            netId = StableSemanticIdentity("net:control"),
            routeIds = listOf(firstRoute, secondRoute),
            point = point,
            sourceTrace = trace(),
        )
        val crossing = ConnectionCrossing(
            id = ConnectionCrossingId("sheet:main", firstRoute, secondRoute, point),
            routeIds = listOf(secondRoute, firstRoute),
            point = point,
            bridgeOwnerRouteId = firstRoute,
            sourceTrace = trace(),
        )

        assertEquals(listOf(firstRoute, secondRoute).sortedBy { it.value }, junction.routeIds)
        assertEquals(listOf(firstRoute, secondRoute).sortedBy { it.value }, crossing.routeIds)
        assertEquals(firstRoute, crossing.bridgeOwnerRouteId)
    }

    @Test
    fun `shared segment and interruption retain semantic identity and stable order`() {
        val segment = ConnectionRouteSegment(SpatialPoint(20, 40), SpatialPoint(80, 40))
        val shared = ConnectionSharedSegment(
            id = ConnectionSharedSegmentId("sheet:main", "net:control", segment),
            netId = StableSemanticIdentity("net:control"),
            routeIds = listOf(secondRoute, firstRoute),
            segment = segment,
            sourceTrace = trace(),
        )
        val interruption = ConnectionInterruptionAnchors(
            id = ConnectionInterruptionId("sheet:main", "net:control", "continuation:A", "continuation:B"),
            semanticId = StableSemanticIdentity("net:control"),
            continuationIds = listOf("continuation:A", "continuation:B"),
            points = listOf(SpatialPoint(10, 20), SpatialPoint(90, 20)),
            sourceTrace = trace(),
        )

        assertEquals(listOf(firstRoute, secondRoute).sortedBy { it.value }, shared.routeIds)
        assertEquals(listOf("continuation:A", "continuation:B"), interruption.continuationIds)
    }

    @Test
    fun `topology plan rejects junction crossing collision and unpaired interruption`() {
        val point = SpatialPoint(50, 50)
        val junction = ConnectionJunction(
            ConnectionJunctionId("sheet:main", "net:control", point),
            StableSemanticIdentity("net:control"),
            listOf(firstRoute, secondRoute),
            point,
            trace(),
        )
        val crossing = ConnectionCrossing(
            ConnectionCrossingId("sheet:main", firstRoute, secondRoute, point),
            listOf(firstRoute, secondRoute),
            point,
            firstRoute,
            trace(),
        )

        assertFailsWith<IllegalArgumentException> {
            ConnectionRouteTopologyPlan(junctions = listOf(junction), crossings = listOf(crossing))
        }
        assertFailsWith<IllegalArgumentException> {
            ConnectionInterruptionAnchors(
                ConnectionInterruptionId("sheet:main", "net:control", "continuation:A", "continuation:B"),
                StableSemanticIdentity("net:control"),
                listOf("continuation:A"),
                listOf(SpatialPoint(10, 20)),
                trace(),
            )
        }
    }

    private fun trace() = SpatialSourceTrace(
        projectionIds = listOf("sheet:main", "net:control"),
        geometryElementIds = listOf(GeometryElementId("geometry:net:control")),
    )
}
