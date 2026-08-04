package com.engineeringood.athena.spatial

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class SpatialRoutingModelsTest {
    @Test
    fun `Route source trace is required immutable and equality bearing`() {
        val sourceOccurrence = occurrence("occurrence:A", 0)
        val targetOccurrence = occurrence("occurrence:B", 140)
        val source = anchor(sourceOccurrence, "port:A.out", SpatialBoundarySide.RIGHT, SpatialPoint(80, 20))
        val target = anchor(targetOccurrence, "port:B.in", SpatialBoundarySide.LEFT, SpatialPoint(140, 20))
        val routeId = SpatialRouteId("sheet:main", "connection:A-B")
        val laneId = SpatialLaneId("sheet:main", SpatialLaneOrientation.HORIZONTAL, 20)
        val projectionIds = mutableListOf(
            "sheet:main",
            "connection:A-B",
            "occurrence:A",
            "port:A.out",
            "occurrence:B",
            "port:B.in",
        )
        val geometryIds = mutableListOf(GeometryElementId("geometry:connection:A-B"))
        val sourceTrace = SpatialSourceTrace(projectionIds, geometryIds)
        val route = SpatialRoute(
            routeId = routeId,
            sheetId = "sheet:main",
            connectionId = StableSemanticIdentity("connection:A-B"),
            sourceAnchorId = source.anchorId,
            targetAnchorId = target.anchorId,
            laneId = laneId,
            sourceTrace = sourceTrace,
            points = listOf(source.point, target.point),
        )

        projectionIds.clear()
        geometryIds.clear()

        assertEquals("sheet:main", route.sourceAnchorId.occurrenceId.sheetId)
        assertEquals("occurrence:A", route.sourceAnchorId.occurrenceId.projectionId)
        assertEquals("port:A.out", route.sourceAnchorId.portId.value)
        assertEquals(6, route.sourceTrace.projectionIds.size)
        assertEquals(1, route.sourceTrace.geometryElementIds.size)
        assertEquals(route, route.copy())
        assertNotEquals(
            route,
            route.copy(
                sourceTrace = SpatialSourceTrace(
                    projectionIds = listOf("sheet:main", "connection:other"),
                    geometryElementIds = listOf(GeometryElementId("geometry:other")),
                ),
            ),
        )
    }

    @Test
    fun `public basic Route and Lane facts expose no professional optimization surface`() {
        val propertyNames = (SpatialRoute::class.java.declaredFields + SpatialLane::class.java.declaredFields)
            .map { field -> field.name.lowercase() }
        val forbidden = listOf(
            "planner",
            "optimizer",
            "bundle",
            "trunk",
            "continuation",
            "electricalrole",
            "bendscore",
            "crossingscore",
            "label",
            "solvertuning",
        )

        forbidden.forEach { token ->
            assertFalse(propertyNames.any { name -> token in name }, "Forbidden routing surface `$token`: $propertyNames")
        }
    }

    @Test
    fun `route and lane identities are sheet qualified collision safe facts`() {
        val route = SpatialRouteId("sheet:a", "connection:a:b")
        val otherRoute = SpatialRouteId("sheet", "a:connection:a:b")
        val lane = SpatialLaneId("sheet:a", SpatialLaneOrientation.HORIZONTAL, 20)

        assertNotEquals(route, otherRoute)
        assertNotEquals(route.value, otherRoute.value)
        assertEquals(
            "route:sheet=sheet%3Aa:connection=connection%3Aa%3Ab",
            route.value,
        )
        assertEquals(
            "lane:sheet=sheet%3Aa:orientation=horizontal:coordinate=20",
            lane.value,
        )
    }

    @Test
    fun `lane requires used unique same sheet routes and matching channel identity`() {
        val laneId = SpatialLaneId("sheet:main", SpatialLaneOrientation.HORIZONTAL, 20)
        val routeId = SpatialRouteId("sheet:main", "connection:A-B")
        val routeIds = mutableListOf(routeId)
        val lane = SpatialLane(laneId, "sheet:main", SpatialLaneOrientation.HORIZONTAL, 20, routeIds)

        routeIds.clear()

        assertEquals(listOf(routeId), lane.routeIds)

        assertFailsWith<IllegalArgumentException> {
            SpatialLane(laneId, "sheet:main", SpatialLaneOrientation.HORIZONTAL, 20, emptyList())
        }
        assertFailsWith<IllegalArgumentException> {
            SpatialLane(laneId, "sheet:main", SpatialLaneOrientation.HORIZONTAL, 20, listOf(routeId, routeId))
        }
        assertFailsWith<IllegalArgumentException> {
            SpatialLane(
                laneId,
                "sheet:main",
                SpatialLaneOrientation.HORIZONTAL,
                20,
                listOf(SpatialRouteId("sheet:other", "connection:A-B")),
            )
        }
    }

    @Test
    fun `Route points are immutable equality bearing geometry`() {
        val source = anchor(occurrence("occurrence:A", 0), "port:A.out", SpatialBoundarySide.RIGHT, SpatialPoint(80, 20))
        val target = anchor(occurrence("occurrence:B", 140), "port:B.in", SpatialBoundarySide.LEFT, SpatialPoint(140, 20))
        val points = mutableListOf(source.point, target.point)
        val route = SpatialRoute(
            routeId = SpatialRouteId("sheet:main", "connection:A-B"),
            sheetId = "sheet:main",
            connectionId = StableSemanticIdentity("connection:A-B"),
            sourceAnchorId = source.anchorId,
            targetAnchorId = target.anchorId,
            laneId = SpatialLaneId("sheet:main", SpatialLaneOrientation.HORIZONTAL, 20),
            sourceTrace = trace(),
            points = points,
        )

        points.clear()

        assertEquals(listOf(source.point, target.point), route.points)
        assertEquals(route, route.copy())
    }

    @Test
    fun `anchor identity contains sheet occurrence and semantic port but excludes side and point`() {
        val occurrence = SpatialOccurrenceId("sheet:main", "occurrence:Q1")
        val port = StableSemanticIdentity("port:Q1.1")
        val id = SpatialAnchorId("sheet:main", occurrence, port)
        val anchor = SpatialAnchorPosition(
            anchorId = id,
            sheetId = "sheet:main",
            subject = SpatialOccurrencePortSubject(occurrence, port),
            side = SpatialBoundarySide.RIGHT,
            point = SpatialPoint(90, 40),
            sourceTrace = trace(),
        )

        assertEquals(
            "anchor:sheet=sheet%3Amain:occurrence=occurrence%3AQ1:port=port%3AQ1.1",
            id.value,
        )
        assertEquals(id, anchor.copy(side = SpatialBoundarySide.LEFT, point = SpatialPoint(10, 40)).anchorId)
        assertFalse(SpatialAnchorId::class.java.declaredFields.any { field -> field.name in setOf("side", "point", "x", "y") })
        assertEquals(Int::class.javaPrimitiveType, SpatialPoint::class.java.getDeclaredField("x").type)
        assertEquals(Int::class.javaPrimitiveType, SpatialPoint::class.java.getDeclaredField("y").type)
    }

    @Test
    fun `serialized anchor identity keeps delimiter bearing typed parts distinct`() {
        val first = SpatialAnchorId(
            sheetId = "sheet:a",
            occurrenceId = SpatialOccurrenceId("sheet:a", "b"),
            portId = StableSemanticIdentity("port:c"),
        )
        val second = SpatialAnchorId(
            sheetId = "sheet",
            occurrenceId = SpatialOccurrenceId("sheet", "a:b"),
            portId = StableSemanticIdentity("port:c"),
        )

        assertNotEquals(first, second)
        assertNotEquals(first.value, second.value)
    }

    @Test
    fun `anchor position rejects contradictory sheet or subject identity`() {
        val occurrence = SpatialOccurrenceId("sheet:main", "occurrence:Q1")
        val port = StableSemanticIdentity("port:Q1.1")
        val id = SpatialAnchorId("sheet:main", occurrence, port)

        assertFailsWith<IllegalArgumentException> {
            SpatialAnchorPosition(
                id,
                "sheet:other",
                SpatialOccurrencePortSubject(occurrence, port),
                SpatialBoundarySide.RIGHT,
                SpatialPoint(90, 40),
                trace(),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            SpatialAnchorPosition(
                id,
                "sheet:main",
                SpatialOccurrencePortSubject(occurrence, StableSemanticIdentity("port:Q1.2")),
                SpatialBoundarySide.RIGHT,
                SpatialPoint(90, 40),
                trace(),
            )
        }
    }

    @Test
    fun `spatial reality rejects anchor point away from its declared occurrence boundary side`() {
        val occurrence = SpatialOccurrenceGeometry(
            occurrenceId = SpatialOccurrenceId("sheet:main", "occurrence:Q1"),
            subjectId = StableSemanticIdentity("component:Q1"),
            sheetId = "sheet:main",
            regionId = "region:main",
            rectangle = SpatialRect(10, 20, 80, 40),
            placementReason = SpatialPlacementReason(listOf("test placement")),
            sourceTrace = trace(),
        )
        val port = StableSemanticIdentity("port:Q1.1")
        val id = SpatialAnchorId("sheet:main", occurrence.occurrenceId, port)
        val invalid = SpatialAnchorPosition(
            anchorId = id,
            sheetId = "sheet:main",
            subject = SpatialOccurrencePortSubject(occurrence.occurrenceId, port),
            side = SpatialBoundarySide.RIGHT,
            point = SpatialPoint(89, 40),
            sourceTrace = trace(),
        )

        val result = SpatialReality.validate(
            spatialDocument(
                occurrences = listOf(occurrence),
                anchors = listOf(invalid),
            ),
        )

        assertContains(
            result.diagnostics.map(SpatialDiagnostic::problem),
            "point (89,40) is not strictly on declared right boundary",
        )
    }

    @Test
    fun `spatial validation reports an Anchor owned by another Sheet`() {
        val occurrence = occurrence("occurrence:Q1", 10)
        val foreignOccurrenceId = SpatialOccurrenceId("sheet:other", occurrence.occurrenceId.projectionId)
        val port = StableSemanticIdentity("port:Q1.1")
        val invalid = SpatialAnchorPosition(
            anchorId = SpatialAnchorId("sheet:other", foreignOccurrenceId, port),
            sheetId = "sheet:other",
            subject = SpatialOccurrencePortSubject(foreignOccurrenceId, port),
            side = SpatialBoundarySide.RIGHT,
            point = SpatialPoint(90, 20),
            sourceTrace = trace(),
        )

        val result = SpatialReality.validate(
            spatialDocument(
                occurrences = listOf(occurrence),
                anchors = listOf(invalid),
            ),
        )

        assertContains(
            result.diagnostics,
            SpatialDiagnostic(
                subject = "Sheet sheet:main",
                problem = "contains Anchor ${invalid.anchorId.value} owned by Sheet sheet:other",
                correction = "Keep every Spatial fact inside its exact owning Sheet root.",
                sourceTrace = invalid.sourceTrace,
            ),
        )
    }

    @Test
    fun `spatial reality rejects route endpoint points that differ from typed anchors`() {
        val sourceOccurrence = occurrence("occurrence:A", 0)
        val targetOccurrence = occurrence("occurrence:B", 140)
        val source = anchor(sourceOccurrence, "port:A.out", SpatialBoundarySide.RIGHT, SpatialPoint(80, 20))
        val target = anchor(targetOccurrence, "port:B.in", SpatialBoundarySide.LEFT, SpatialPoint(140, 20))
        val routeId = SpatialRouteId("sheet:main", "route:A-B")
        val laneId = SpatialLaneId("sheet:main", SpatialLaneOrientation.HORIZONTAL, 20)
        val result = SpatialReality.validate(
            spatialDocument(
                occurrences = listOf(sourceOccurrence, targetOccurrence),
                anchors = listOf(source, target),
                lanes = listOf(
                    SpatialLane(
                        laneId,
                        "sheet:main",
                        SpatialLaneOrientation.HORIZONTAL,
                        20,
                        listOf(routeId),
                    ),
                ),
                routes = listOf(
                    SpatialRoute(
                        routeId = routeId,
                        sheetId = "sheet:main",
                        connectionId = StableSemanticIdentity("connection:A-B"),
                        sourceAnchorId = source.anchorId,
                        targetAnchorId = target.anchorId,
                        laneId = laneId,
                        sourceTrace = trace(),
                        points = listOf(SpatialPoint(79, 20), SpatialPoint(140, 20)),
                    ),
                ),
            ),
        )

        assertContains(
            result.diagnostics.map(SpatialDiagnostic::problem),
            "endpoint points do not equal source and target Anchor points",
        )
    }

    private fun occurrence(projectionId: String, x: Int): SpatialOccurrenceGeometry = SpatialOccurrenceGeometry(
        occurrenceId = SpatialOccurrenceId("sheet:main", projectionId),
        subjectId = StableSemanticIdentity("component:${projectionId.substringAfter(':')}"),
        sheetId = "sheet:main",
        regionId = "region:main",
        rectangle = SpatialRect(x, 0, 80, 40),
        placementReason = SpatialPlacementReason(listOf("test placement")),
        sourceTrace = trace(),
    )

    private fun anchor(
        occurrence: SpatialOccurrenceGeometry,
        portId: String,
        side: SpatialBoundarySide,
        point: SpatialPoint,
    ): SpatialAnchorPosition {
        val port = StableSemanticIdentity(portId)
        val id = SpatialAnchorId(occurrence.sheetId, occurrence.occurrenceId, port)
        return SpatialAnchorPosition(
            anchorId = id,
            sheetId = occurrence.sheetId,
            subject = SpatialOccurrencePortSubject(occurrence.occurrenceId, port),
            side = side,
            point = point,
            sourceTrace = trace(),
        )
    }

    private fun trace(): SpatialSourceTrace = SpatialSourceTrace(
        projectionIds = listOf("sheet:main", "occurrence:Q1", "port:Q1.1"),
        geometryElementIds = listOf(GeometryElementId("geometry:Q1")),
    )

    private fun spatialDocument(
        occurrences: List<SpatialOccurrenceGeometry>,
        anchors: List<SpatialAnchorPosition>,
        lanes: List<SpatialLane> = emptyList(),
        routes: List<SpatialRoute> = emptyList(),
    ): SpatialDocument {
        val sheetId = "sheet:main"
        val drawingArea = SpatialRect(0, 0, 1200, 800)
        return SpatialDocument(
            listOf(
                SpatialSheet(
                    sheetId = sheetId,
                    extent = drawingArea,
                    drawingArea = drawingArea,
                    grid = SpatialGridDefinition(sheetId, "grid:main", drawingArea, 3, 4, trace()),
                    occurrences = occurrences,
                    regions = emptyList(),
                    constructs = emptyList(),
                    alignments = emptyList(),
                    anchors = anchors,
                    lanes = lanes,
                    routes = routes,
                    gridReferences = emptyList(),
                    quality = SpatialQualitySnapshot(
                        SpatialQualitySnapshotId(sheetId),
                        sheetId,
                        zeroSpatialQualityMetrics(),
                        trace(),
                    ),
                    sourceTrace = trace(),
                ),
            ),
        )
    }
}
