package com.engineeringood.athena.spatial

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class ConnectionRoutePlanContractTest {
    @Test
    fun `route plan exposes typed orthogonal segments and stable geometry identity`() {
        val source = anchor("occurrence:A", "port:A.out", SpatialPoint(20, 40))
        val target = anchor("occurrence:B", "port:B.in", SpatialPoint(100, 80))
        val segments = listOf(
            ConnectionRouteSegment(source.point, SpatialPoint(100, 40)),
            ConnectionRouteSegment(SpatialPoint(100, 40), target.point),
        )
        val plan = ConnectionRoutePlan(
            routeId = ConnectionRoutePlanId("sheet:main", "projection:connection:A-B"),
            sheetId = "sheet:main",
            connectionId = StableSemanticIdentity("connection:A-B"),
            projectionConnectionId = "projection:connection:A-B",
            sourceAnchorId = source.anchorId,
            targetAnchorId = target.anchorId,
            points = listOf(source.point, SpatialPoint(100, 40), target.point),
            quality = ConnectionRouteQuality.valid(segments),
            sourceTrace = trace(),
            laneId = SpatialLaneId("sheet:main", SpatialLaneOrientation.HORIZONTAL, 40),
        )

        assertEquals(segments, plan.segments)
        assertEquals("20,40;100,40;100,80", plan.geometryIdentity)
        assertEquals(plan, plan.copy())
    }

    @Test
    fun `route segment exposes diagonal and zero length invalidity for fail closed validation`() {
        val source = anchor("occurrence:A", "port:A.out", SpatialPoint(20, 40))
        assertFalse(ConnectionRouteSegment(source.point, SpatialPoint(50, 50)).isPositiveOrthogonal)
        assertFalse(ConnectionRouteSegment(source.point, source.point).isPositiveOrthogonal)
    }

    @Test
    fun `route plan rejects duplicate endpoint anchors and out of bounds geometry`() {
        val source = anchor("occurrence:A", "port:A.out", SpatialPoint(20, 40))
        val segment = ConnectionRouteSegment(source.point, SpatialPoint(100, 40))
        assertFailsWith<IllegalArgumentException> {
            ConnectionRoutePlan(
                routeId = ConnectionRoutePlanId("sheet:main", "projection:connection:A-B"),
                sheetId = "sheet:main",
                connectionId = StableSemanticIdentity("connection:A-B"),
                projectionConnectionId = "projection:connection:A-B",
                sourceAnchorId = source.anchorId,
                targetAnchorId = source.anchorId,
                points = listOf(source.point, SpatialPoint(100, 40)),
                quality = ConnectionRouteQuality.valid(listOf(segment)),
                sourceTrace = trace(),
                laneId = SpatialLaneId("sheet:main", SpatialLaneOrientation.HORIZONTAL, 40),
                drawingArea = SpatialRect(0, 0, 50, 50),
            )
        }
    }

    private fun anchor(occurrenceId: String, portId: String, point: SpatialPoint): SpatialAnchorPosition {
        val occurrence = SpatialOccurrenceId("sheet:main", occurrenceId)
        val port = StableSemanticIdentity(portId)
        return SpatialAnchorPosition(
            anchorId = SpatialAnchorId("sheet:main", occurrence, port),
            sheetId = "sheet:main",
            subject = SpatialOccurrencePortSubject(occurrence, port),
            side = SpatialBoundarySide.RIGHT,
            point = point,
            sourceTrace = trace(),
        )
    }

    private fun trace() = SpatialSourceTrace(
        projectionIds = listOf("sheet:main", "projection:connection:A-B"),
        geometryElementIds = listOf(GeometryElementId("geometry:connection:A-B")),
    )
}
