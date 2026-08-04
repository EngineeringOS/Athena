package com.engineeringood.athena.routing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class OrthogonalRouteSolverTest {
    @Test
    fun `solver returns canonical boundary-safe detour independent of obstacle order`() {
        val obstacles = listOf(
            OrthogonalRouteObstacle("z", OrthogonalRouteRect(140, 60, 40, 80)),
            OrthogonalRouteObstacle("a", OrthogonalRouteRect(300, 20, 10, 10)),
        )
        val request = OrthogonalRouteRequest(
            requestId = "connection:A-B",
            source = OrthogonalRoutePoint(80, 100),
            target = OrthogonalRoutePoint(240, 100),
            sourceSide = OrthogonalRouteSide.RIGHT,
            targetSide = OrthogonalRouteSide.LEFT,
            drawingArea = OrthogonalRouteRect(0, 0, 320, 200),
            obstacles = obstacles,
        )

        val first = assertIs<OrthogonalRouteSolveResult.Success>(OrthogonalRouteSolver().solve(request))
        val second = assertIs<OrthogonalRouteSolveResult.Success>(
            OrthogonalRouteSolver().solve(request.copy(obstacles = obstacles.reversed())),
        )

        assertEquals(first, second)
        assertEquals(
            listOf(
                OrthogonalRoutePoint(80, 100),
                OrthogonalRoutePoint(81, 100),
                OrthogonalRoutePoint(81, 60),
                OrthogonalRoutePoint(180, 60),
                OrthogonalRoutePoint(180, 100),
                OrthogonalRoutePoint(239, 100),
                OrthogonalRoutePoint(240, 100),
            ),
            first.points,
        )
    }

    @Test
    fun `solver returns no path instead of intersecting or leaving drawing area`() {
        val result = OrthogonalRouteSolver().solve(
            OrthogonalRouteRequest(
                requestId = "connection:blocked",
                source = OrthogonalRoutePoint(80, 100),
                target = OrthogonalRoutePoint(240, 100),
                sourceSide = OrthogonalRouteSide.RIGHT,
                targetSide = OrthogonalRouteSide.LEFT,
                drawingArea = OrthogonalRouteRect(0, 0, 320, 200),
                obstacles = listOf(
                    OrthogonalRouteObstacle("all", OrthogonalRouteRect(0, 0, 320, 200)),
                ),
            ),
        )

        assertEquals(OrthogonalRouteSolveResult.NoPath, result)
    }

    @Test
    fun `clearance overflow returns no path instead of throwing`() {
        val result = OrthogonalRouteSolver().solve(
            OrthogonalRouteRequest(
                requestId = "connection:overflow",
                source = OrthogonalRoutePoint(Int.MIN_VALUE + 20, 20),
                target = OrthogonalRoutePoint(-10, 20),
                sourceSide = OrthogonalRouteSide.RIGHT,
                targetSide = OrthogonalRouteSide.LEFT,
                drawingArea = OrthogonalRouteRect(Int.MIN_VALUE, 0, Int.MAX_VALUE, 40),
                obstacles = listOf(
                    OrthogonalRouteObstacle("edge", OrthogonalRouteRect(Int.MIN_VALUE, 10, 10, 20)),
                ),
                obstacleClearance = 1,
            ),
        )

        assertEquals(OrthogonalRouteSolveResult.NoPath, result)
    }
}
