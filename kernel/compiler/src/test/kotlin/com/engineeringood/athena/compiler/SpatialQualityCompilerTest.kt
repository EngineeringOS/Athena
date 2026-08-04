package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.spatial.SpatialConstructGeometry
import com.engineeringood.athena.spatial.SpatialConstructId
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialLaneId
import com.engineeringood.athena.spatial.SpatialLaneOrientation
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialRoute
import com.engineeringood.athena.spatial.SpatialSourceTrace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SpatialQualityCompilerTest {
    @Test
    fun `overlap counts only unordered rectangle pairs with positive shared area`() {
        val separated = occurrence("occurrence:separated", 40, 0, 10, 10)
        val origin = occurrence("occurrence:origin", 0, 0, 10, 10)
        val edgeTouch = occurrence("occurrence:edge", 10, 0, 10, 10)
        val cornerTouch = occurrence("occurrence:corner", 10, 10, 10, 10)
        val positive = occurrence("occurrence:positive", 9, 2, 2, 6)
        val nested = occurrence("occurrence:nested", 2, 2, 2, 2)
        val occurrences = listOf(separated, origin, edgeTouch, cornerTouch, positive, nested)

        val forward = measure(occurrences = occurrences)
        val reversed = measure(occurrences = occurrences.reversed())

        assertEquals(3, forward.occurrenceOverlapCount)
        assertEquals(forward, reversed)
    }

    @Test
    fun `containment counts each Construct member relationship outside its own envelope`() {
        val contained = occurrence("occurrence:contained", 0, 0, 10, 10)
        val outside = occurrence("occurrence:outside", 19, 0, 5, 5)
        val shared = occurrence("occurrence:shared", 30, 30, 5, 5)
        val occurrences = listOf(contained, outside, shared)
        val constructs = listOf(
            construct(
                "construct:first",
                listOf(contained, outside, shared),
                SpatialRect(0, 0, 20, 20),
            ),
            construct(
                "construct:second",
                listOf(outside, shared),
                SpatialRect(19, 0, 16, 35),
            ),
        )

        val forward = measure(occurrences = occurrences, constructs = constructs)
        val reversed = measure(occurrences = occurrences.reversed(), constructs = constructs.reversed())

        assertEquals(2, forward.constructContainmentFailureCount)
        assertEquals(forward, reversed)
    }

    @Test
    fun `body intersection counts defective segments not vertices or bodies`() {
        val firstBody = occurrence("occurrence:first-body", 0, 0, 10, 10)
        val secondBody = occurrence("occurrence:second-body", 20, 0, 10, 10)
        val lowerBody = occurrence("occurrence:lower-body", 20, 20, 10, 10)
        val crossingBoth = route(
            "route:crossing-both",
            listOf(SpatialPoint(-10, 5), SpatialPoint(40, 5)),
        )
        val twoDefectiveSegments = route(
            "route:two-segments",
            listOf(
                SpatialPoint(-10, 5),
                SpatialPoint(40, 5),
                SpatialPoint(40, 25),
                SpatialPoint(-10, 25),
            ),
        )

        assertEquals(
            1,
            measure(occurrences = listOf(firstBody, secondBody), routes = listOf(crossingBoth))
                .routeBodyIntersectionCount,
            "one segment must count once even when it crosses two bodies",
        )
        assertEquals(
            2,
            measure(
                occurrences = listOf(firstBody, secondBody, lowerBody),
                routes = listOf(twoDefectiveSegments),
            ).routeBodyIntersectionCount,
        )
    }

    @Test
    fun `body intersection excludes boundary contact and endpoint owner bodies`() {
        val body = occurrence("occurrence:body", 0, 0, 10, 10)
        val boundaryRoute = route(
            "route:boundary",
            listOf(SpatialPoint(-10, 0), SpatialPoint(20, 0)),
        )
        val endpointOwnerRoute = route(
            id = "route:endpoint-owner",
            points = listOf(SpatialPoint(0, 5), SpatialPoint(20, 5)),
            sourceOccurrenceId = body.occurrenceId.projectionId,
        )

        assertEquals(
            0,
            measure(occurrences = listOf(body), routes = listOf(boundaryRoute)).routeBodyIntersectionCount,
        )
        assertEquals(
            0,
            measure(occurrences = listOf(body), routes = listOf(endpointOwnerRoute)).routeBodyIntersectionCount,
        )
    }

    @Test
    fun `crossing counts distinct Route pair and perpendicular point tuples`() {
        val twoHorizontalPasses = route(
            "route:two-passes",
            listOf(
                SpatialPoint(0, 5),
                SpatialPoint(20, 5),
                SpatialPoint(20, 15),
                SpatialPoint(0, 15),
            ),
        )
        val vertical = route(
            "route:vertical",
            listOf(SpatialPoint(10, 0), SpatialPoint(10, 20)),
        )
        val splitHorizontal = route(
            "route:split",
            listOf(SpatialPoint(0, 25), SpatialPoint(10, 25), SpatialPoint(20, 25)),
        )
        val throughSplit = route(
            "route:through-split",
            listOf(SpatialPoint(10, 20), SpatialPoint(10, 30)),
        )
        val routes = listOf(twoHorizontalPasses, vertical, splitHorizontal, throughSplit)

        val forward = measure(routes = routes)
        val reversed = measure(routes = routes.reversed())

        assertEquals(3, forward.routeCrossingCount)
        assertEquals(forward, reversed)
    }

    @Test
    fun `crossing excludes shared engineering vertices shared Anchors and collinear travel`() {
        val junction = SpatialPoint(10, 10)
        val sharedVertexHorizontal = route(
            "route:shared-vertex-horizontal",
            listOf(SpatialPoint(0, 10), junction, SpatialPoint(20, 10)),
        )
        val sharedVertexVertical = route(
            "route:shared-vertex-vertical",
            listOf(SpatialPoint(10, 0), junction, SpatialPoint(10, 20)),
        )
        val sharedAnchor = testSpatialAnchorId("occurrence:junction", "port:junction")
        val endpointHorizontal = route(
            id = "route:endpoint-horizontal",
            points = listOf(SpatialPoint(0, 30), SpatialPoint(10, 30)),
            targetAnchorId = sharedAnchor,
        )
        val endpointVertical = route(
            id = "route:endpoint-vertical",
            points = listOf(SpatialPoint(10, 30), SpatialPoint(10, 40)),
            sourceAnchorId = sharedAnchor,
        )
        val collinearA = route(
            "route:collinear-a",
            listOf(SpatialPoint(0, 50), SpatialPoint(20, 50)),
        )
        val collinearB = route(
            "route:collinear-b",
            listOf(SpatialPoint(10, 50), SpatialPoint(30, 50)),
        )

        assertEquals(
            0,
            measure(routes = listOf(sharedVertexHorizontal, sharedVertexVertical)).routeCrossingCount,
        )
        assertEquals(
            0,
            measure(routes = listOf(endpointHorizontal, endpointVertical)).routeCrossingCount,
        )
        assertEquals(0, measure(routes = listOf(collinearA, collinearB)).routeCrossingCount)
    }

    @Test
    fun `twist counts each segment that changes both coordinates`() {
        val route = route(
            "route:twist",
            listOf(
                SpatialPoint(0, 0),
                SpatialPoint(10, 0),
                SpatialPoint(10, 10),
                SpatialPoint(20, 20),
                SpatialPoint(20, 30),
            ),
        )
        val reversed = route(
            "route:twist",
            route.points.reversed(),
        )

        assertEquals(1, measure(routes = listOf(route)).twistCount)
        assertEquals(1, measure(routes = listOf(reversed)).twistCount)
    }

    @Test
    fun `Lane use counts actual Route assignments to existing Lanes`() {
        val firstLaneId = testSpatialLaneId(coordinate = 10)
        val secondLaneId = testSpatialLaneId(coordinate = 20)
        val missingLaneId = testSpatialLaneId(coordinate = 30)
        val firstLane = testSpatialLane("route:first", "route:second", coordinate = 10)
        val secondLane = testSpatialLane("route:third", coordinate = 20)
        val phantomLane = testSpatialLane("route:phantom", coordinate = 40)
        val routes = listOf(
            route("route:first", listOf(SpatialPoint(0, 10), SpatialPoint(10, 10)), laneId = firstLaneId),
            route("route:second", listOf(SpatialPoint(10, 10), SpatialPoint(20, 10)), laneId = firstLaneId),
            route("route:third", listOf(SpatialPoint(0, 20), SpatialPoint(10, 20)), laneId = secondLaneId),
            route("route:missing", listOf(SpatialPoint(0, 30), SpatialPoint(10, 30)), laneId = missingLaneId),
        )
        val lanes = listOf(firstLane, secondLane, phantomLane)

        val forward = measure(lanes = lanes, routes = routes)
        val reversed = measure(lanes = lanes.reversed(), routes = routes.reversed())

        assertEquals(2, forward.usedLaneCount)
        assertEquals(2, forward.peakRoutesPerLane)
        assertEquals(forward, reversed)
        assertEquals(0, measure().usedLaneCount)
        assertEquals(0, measure().peakRoutesPerLane)
    }

    @Test
    fun `Density and Occupancy use each Drawing Area and Occurrence rectangle union`() {
        val occurrence = occurrence("occurrence:one", 0, 0, 10, 10)
        val small = measure(
            drawingArea = SpatialRect(0, 0, 100, 100),
            occurrences = listOf(occurrence),
        )
        val large = measure(
            drawingArea = SpatialRect(0, 0, 200, 100),
            occurrences = listOf(occurrence),
        )
        val empty = measure(drawingArea = SpatialRect(0, 0, 100, 100))

        assertEquals(0.0001, small.density)
        assertEquals(0.01, small.occupancy)
        assertEquals(0.00005, large.density)
        assertEquals(0.005, large.occupancy)
        assertEquals(0.0, empty.density)
        assertEquals(0.0, empty.occupancy)
    }

    @Test
    fun `Occupancy counts touching partial nested and repeated overlap once`() {
        val first = occurrence("occurrence:first", 0, 0, 10, 10)
        val touching = occurrence("occurrence:touching", 10, 0, 10, 10)
        val partial = occurrence("occurrence:partial", 5, 0, 10, 10)
        val lower = occurrence("occurrence:lower", 0, 5, 10, 10)
        val nested = occurrence("occurrence:nested", 2, 2, 2, 2)
        val drawingArea = SpatialRect(0, 0, 100, 100)

        assertEquals(
            0.02,
            measure(drawingArea, listOf(first, touching)).occupancy,
            "edge-touching rectangles must contribute both complete areas",
        )
        assertEquals(
            0.015,
            measure(drawingArea, listOf(first, partial)).occupancy,
            "partial overlap must contribute shared area once",
        )
        assertEquals(
            0.01,
            measure(drawingArea, listOf(first, nested)).occupancy,
            "nested area must not inflate union",
        )
        val repeatedOverlap = listOf(first, partial, lower)
        val forward = measure(drawingArea, repeatedOverlap)
        val reversed = measure(drawingArea, repeatedOverlap.reversed())
        assertEquals(0.02, forward.occupancy)
        assertEquals(forward, reversed)
    }

    @Test
    fun `Construct overlays do not contribute to Occupancy`() {
        val occurrence = occurrence("occurrence:one", 10, 10, 10, 10)
        val tight = construct("construct:tight", listOf(occurrence), SpatialRect(10, 10, 10, 10))
        val broad = construct("construct:broad", listOf(occurrence), SpatialRect(0, 0, 100, 100))

        val withoutOverlay = measure(occurrences = listOf(occurrence))
        val withTightOverlay = measure(occurrences = listOf(occurrence), constructs = listOf(tight))
        val withBroadOverlay = measure(occurrences = listOf(occurrence), constructs = listOf(broad))

        assertEquals(withoutOverlay.occupancy, withTightOverlay.occupancy)
        assertEquals(withoutOverlay.occupancy, withBroadOverlay.occupancy)
    }

    @Test
    fun `area arithmetic remains finite at maximum positive Int dimensions`() {
        val maximum = SpatialRect(0, 0, Int.MAX_VALUE, Int.MAX_VALUE)
        val occurrence = occurrence(
            "occurrence:maximum",
            maximum.x,
            maximum.y,
            maximum.width,
            maximum.height,
        )

        val metrics = measure(drawingArea = maximum, occurrences = listOf(occurrence))

        assertEquals(1.0, metrics.occupancy)
        assertEquals(1.0 / (Int.MAX_VALUE.toLong() * Int.MAX_VALUE.toLong()).toDouble(), metrics.density)
    }

    @Test
    fun `spatial quality public names contain no stale architecture or label metric`() {
        val names = listOf(
            SpatialQualityCompiler::class.simpleName.orEmpty(),
            measure().javaClass.declaredFields.joinToString(" ") { field -> field.name },
        ).joinToString(" ")
        val banned = listOf(
            "M39",
            "V0",
            "V1",
            "Evidence",
            "ProfessionalControlDrawing",
            "Compatibility",
            "label",
        )

        banned.forEach { token ->
            assertFalse(names.contains(token, ignoreCase = true), "Quality names must not contain `$token`: $names")
        }
    }

    private fun measure(
        drawingArea: SpatialRect = SpatialRect(0, 0, 100, 100),
        occurrences: List<SpatialOccurrenceGeometry> = emptyList(),
        constructs: List<SpatialConstructGeometry> = emptyList(),
        lanes: List<SpatialLane> = emptyList(),
        routes: List<SpatialRoute> = emptyList(),
    ) = SpatialQualityCompiler().measure(
        drawingArea = drawingArea,
        occurrences = occurrences,
        constructs = constructs,
        lanes = lanes,
        routes = routes,
    )

    private fun occurrence(
        id: String,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ): SpatialOccurrenceGeometry = testSpatialOccurrence(
        projectionId = id,
        subjectId = "subject:$id",
        x = x,
        y = y,
        width = width,
        height = height,
    )

    private fun construct(
        id: String,
        members: List<SpatialOccurrenceGeometry>,
        envelope: SpatialRect,
    ): SpatialConstructGeometry = SpatialConstructGeometry(
        constructId = SpatialConstructId(TEST_SPATIAL_SHEET_ID, id),
        sheetId = TEST_SPATIAL_SHEET_ID,
        kind = "test-construct",
        name = id,
        memberOccurrenceIds = members.map(SpatialOccurrenceGeometry::occurrenceId),
        envelope = envelope,
        sourceTrace = SpatialSourceTrace(
            projectionIds = listOf(TEST_SPATIAL_SHEET_ID, id) +
                members.map { occurrence -> occurrence.occurrenceId.projectionId },
            geometryElementIds = listOf(GeometryElementId("geometry:$id")),
        ),
    )

    private fun route(
        id: String,
        points: List<SpatialPoint>,
        sourceOccurrenceId: String = "occurrence:source:$id",
        targetOccurrenceId: String = "occurrence:target:$id",
        sourceAnchorId: com.engineeringood.athena.spatial.SpatialAnchorId = testSpatialAnchorId(sourceOccurrenceId),
        targetAnchorId: com.engineeringood.athena.spatial.SpatialAnchorId = testSpatialAnchorId(targetOccurrenceId),
        laneId: SpatialLaneId = testSpatialLaneId(SpatialLaneOrientation.HORIZONTAL, 0),
    ): SpatialRoute = testSpatialRoute(
        routeId = id,
        connectionId = "connection:$id",
        sourceAnchorId = sourceAnchorId,
        targetAnchorId = targetAnchorId,
        points = points,
        laneId = laneId,
    )
}
