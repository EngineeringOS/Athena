package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.presentation.PresentationOccurrenceId
import com.engineeringood.athena.presentation.PresentationPoint
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConnectionPaintCompilerTest {
    @Test
    fun `connection paint compiler derives concrete default line label and marker facts`() {
        val points = listOf(PresentationPoint(80, 20), PresentationPoint(160, 20))
        val paint = ConnectionPaintCompiler().compile(
            route = route(),
            routePoints = points,
            connectorId = PresentationOccurrenceId("paint:route:main-feed"),
        )

        assertEquals("spatial-route", paint.line.classId)
        assertEquals("CONNECTION", paint.line.lineKind)
        assertEquals("line:connection", paint.line.lineStyleId)
        assertEquals(1.5, paint.line.weight)
        assertEquals("solid", paint.line.style)
        assertEquals("connection", paint.line.colorKey)
        assertEquals("route:main-feed", paint.labels.single().text)
        assertEquals(PresentationPoint(160, 20), paint.labels.single().point)
        assertEquals(emptyList(), paint.markers)
        assertEquals(emptyList(), paint.markerIds)
    }

    @Test
    fun `optional source properties override only presentation paint facts`() {
        val route = route()
        val sourceRoute = route.copy()
        val paint = ConnectionPaintCompiler(
            overrides = mapOf(
                route.routeId to ConnectionPaintOverride(
                    style = "dot",
                    label = "main feed",
                    position = "right top",
                ),
            ),
        ).compile(
            route = route,
            routePoints = listOf(PresentationPoint(80, 20), PresentationPoint(160, 20)),
            connectorId = PresentationOccurrenceId("paint:route:main-feed"),
        )

        assertEquals(sourceRoute, route)
        assertEquals("dot", paint.line.style)
        assertEquals("line:dot", paint.line.lineStyleId)
        assertEquals("main feed", paint.labels.single().text)
        assertEquals(PresentationPoint(168, 8), paint.labels.single().point)
    }

    @Test
    fun `invalid paint override fails closed before renderer publication`() {
        val route = route()

        assertFailsWith<IllegalArgumentException> {
            ConnectionPaintCompiler(
                overrides = mapOf(route.routeId to ConnectionPaintOverride(style = " ")),
            ).compile(
                route = route,
                routePoints = listOf(PresentationPoint(80, 20), PresentationPoint(160, 20)),
                connectorId = PresentationOccurrenceId("paint:route:main-feed"),
            )
        }
    }

    @Test
    fun `new connection paint names stay direct and clean`() {
        val names = listOf(
            ConnectionPaintCompiler::class.simpleName.orEmpty(),
            ConnectionPaint::class.simpleName.orEmpty(),
            ConnectionPaintOverride::class.simpleName.orEmpty(),
        )
        val banned = listOf("M39", "V0", "V1", "Evidence", "ProfessionalControlDrawing", "Compatibility")

        banned.forEach { token ->
            assertFalse(
                names.any { name -> name.contains(token) },
                "Connection paint names must not contain `$token`: $names",
            )
        }
        assertTrue(names.all { name -> name.length <= 32 })
    }

    private fun route(): SpatialRoute =
        SpatialRoute(
            routeId = "route:main-feed",
            connectionId = StableSemanticIdentity("connection:Supply.L1-to-Q1.1"),
            sourceOccurrenceId = "supply",
            targetOccurrenceId = "breaker",
            sourceAnchorId = "anchor:supply:right",
            targetAnchorId = "anchor:breaker:left",
            laneId = "lane:main",
            points = listOf(SpatialPoint(80.0, 20.0), SpatialPoint(160.0, 20.0)),
        )
}
