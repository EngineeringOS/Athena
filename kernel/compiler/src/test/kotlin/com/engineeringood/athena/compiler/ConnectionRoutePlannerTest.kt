package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.LogicalRouteConstraint
import com.engineeringood.athena.projection.LogicalRoutePoint
import com.engineeringood.athena.projection.LogicalRouteTargetId
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConnectionRoutePlannerTest {
    private val planner = ConnectionRoutePlanner()
    private val drawingArea = SpatialRect(0, 0, 100, 100)

    @Test
    fun `planner avoids hard obstacle interiors with deterministic orthogonal geometry`() {
        val geometry = requireNotNull(
            planner.planGeometry(
                source = SpatialPoint(10, 50),
                target = SpatialPoint(90, 50),
                drawingArea = drawingArea,
                keepOuts = listOf(SpatialRect(40, 40, 20, 20)),
            ),
        )

        assertEquals(
            listOf(SpatialPoint(10, 50), SpatialPoint(10, 39), SpatialPoint(90, 39), SpatialPoint(90, 50)),
            geometry.points,
        )
        assertEquals(0, geometry.quality.hardObstacleViolations)
        assertEquals(2, geometry.quality.bendCount)
        assertTrue(geometry.quality.manhattanLength > 0)
    }

    @Test
    fun `planner uses stable geometry identity as final tie breaker`() {
        val geometry = requireNotNull(
            planner.planGeometry(
                source = SpatialPoint(10, 10),
                target = SpatialPoint(90, 90),
                drawingArea = drawingArea,
                keepOuts = emptyList(),
            ),
        )

        assertEquals("10,10;10,90;90,90", geometry.quality.geometryIdentity)
    }

    @Test
    fun `equal inputs produce equal route geometry across planner instances`() {
        val first = planner.planGeometry(
            SpatialPoint(10, 50),
            SpatialPoint(90, 50),
            drawingArea,
            listOf(SpatialRect(40, 40, 20, 20)),
        )
        val second = ConnectionRoutePlanner().planGeometry(
            SpatialPoint(10, 50),
            SpatialPoint(90, 50),
            drawingArea,
            listOf(SpatialRect(40, 40, 20, 20)),
        )

        assertEquals(first, second)
    }

    @Test
    fun `planner fails closed when no legal in bounds path exists`() {
        assertNull(
            planner.planGeometry(
                source = SpatialPoint(10, 50),
                target = SpatialPoint(90, 50),
                drawingArea = drawingArea,
                keepOuts = listOf(SpatialRect(0, 0, 100, 100)),
            ),
        )
    }

    @Test
    fun `planner consumes logical via constraints in authored order`() {
        val geometry = requireNotNull(
            planner.planGeometry(
                source = SpatialPoint(10, 10),
                target = SpatialPoint(90, 90),
                drawingArea = drawingArea,
                keepOuts = emptyList(),
                logicalConstraints = listOf(
                    LogicalRouteConstraint.Via(LogicalRouteTargetId("bend-a"), LogicalRoutePoint(30, 20)),
                    LogicalRouteConstraint.Via(LogicalRouteTargetId("bend-b"), LogicalRoutePoint(70, 80)),
                ),
            ),
        )

        val firstWaypoint = SpatialPoint(29, 19)
        val secondWaypoint = SpatialPoint(69, 79)
        assertTrue(firstWaypoint in geometry.points)
        assertTrue(secondWaypoint in geometry.points)
        assertTrue(geometry.points.indexOf(firstWaypoint) < geometry.points.indexOf(secondWaypoint))
    }
}
