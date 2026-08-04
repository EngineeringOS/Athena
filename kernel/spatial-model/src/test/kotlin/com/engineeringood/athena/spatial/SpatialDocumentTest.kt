package com.engineeringood.athena.spatial

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class SpatialDocumentTest {
    @Test
    fun `spatial document requires at least one Sheet root`() {
        val error = assertFailsWith<IllegalArgumentException> {
            SpatialDocument(emptyList())
        }

        assertEquals("Spatial document must contain at least one Sheet.", error.message)
    }

    @Test
    fun `spatial document publishes only immutable typed Sheet roots`() {
        val sheetId = "sheet:main"
        val occurrence = SpatialOccurrenceGeometry(
            occurrenceId = SpatialOccurrenceId(sheetId, "occurrence:Q1"),
            subjectId = StableSemanticIdentity("component:Q1"),
            sheetId = sheetId,
            regionId = "region:rail-a",
            rectangle = SpatialRect(10, 20, 80, 40),
            placementReason = SpatialPlacementReason(listOf("authored Region order")),
            sourceTrace = trace(sheetId, "occurrence:Q1"),
        )
        val regionId = SpatialRegionId(sheetId, "region:rail-a")
        val region = SpatialRegionGeometry(
            regionId = regionId,
            sheetId = sheetId,
            memberOccurrenceIds = listOf(occurrence.occurrenceId),
            bounds = SpatialRect(0, 10, 100, 60),
            sourceTrace = trace(sheetId, "region:rail-a"),
        )
        val constructId = SpatialConstructId(sheetId, "construct:rail-a")
        val construct = SpatialConstructGeometry(
            constructId = constructId,
            sheetId = sheetId,
            kind = "power-rail",
            name = "L1",
            memberOccurrenceIds = listOf(occurrence.occurrenceId),
            envelope = SpatialRect(0, 10, 100, 60),
            sourceTrace = trace(sheetId, "construct:rail-a"),
        )
        val alignmentSource = SpatialAlignmentSource.Region(regionId)
        val alignment = SpatialAlignment(
            alignmentId = SpatialAlignmentId(sheetId, alignmentSource),
            sheetId = sheetId,
            constraintSource = alignmentSource,
            occurrenceIds = listOf(occurrence.occurrenceId),
            sourceTrace = trace(sheetId, "region:rail-a"),
        )
        val anchor = anchor(sheetId, "occurrence:Q1", "port:Q1.1", SpatialPoint(10, 40))
        val routeId = SpatialRouteId(sheetId, "route:Q1-M1")
        val laneId = SpatialLaneId(sheetId, SpatialLaneOrientation.HORIZONTAL, 40)
        val lane = SpatialLane(laneId, sheetId, SpatialLaneOrientation.HORIZONTAL, 40, listOf(routeId))
        val route = SpatialRoute(
            routeId = routeId,
            sheetId = sheetId,
            connectionId = StableSemanticIdentity("connection:Q1-M1"),
            sourceAnchorId = anchor.anchorId,
            targetAnchorId = anchorId(sheetId, "occurrence:M1", "port:M1.1"),
            laneId = laneId,
            sourceTrace = routeTrace("route:Q1-M1"),
            points = listOf(SpatialPoint(10, 40), SpatialPoint(100, 40)),
        )
        val gridSubject = SpatialGridReferenceSubject.Occurrence(occurrence.occurrenceId)
        val gridReference = SpatialGridReference(
            gridReferenceId = SpatialGridReferenceId(sheetId, gridSubject),
            sheetId = sheetId,
            gridId = "grid:main",
            subject = gridSubject,
            rowIndex = 0,
            rowLabel = "A",
            columnIndex = 0,
            columnNumber = 1,
            cellReference = "A1",
            sourceTrace = occurrence.sourceTrace,
        )
        val qualityMetrics = zeroSpatialQualityMetrics()
        val quality = SpatialQualitySnapshot(
            qualitySnapshotId = SpatialQualitySnapshotId(sheetId),
            sheetId = sheetId,
            metrics = qualityMetrics,
            sourceTrace = trace(sheetId, "quality"),
        )
        val occurrences = mutableListOf(occurrence)
        val regions = mutableListOf(region)
        val constructs = mutableListOf(construct)
        val alignments = mutableListOf(alignment)
        val anchors = mutableListOf(anchor)
        val lanes = mutableListOf(lane)
        val routes = mutableListOf(route)
        val gridReferences = mutableListOf(gridReference)
        val sheet = SpatialSheet(
            sheetId = sheetId,
            extent = SpatialRect(0, 0, 1200, 800),
            drawingArea = SpatialRect(40, 60, 1120, 640),
            grid = SpatialGridDefinition(sheetId, "grid:main", SpatialRect(40, 60, 1120, 640), 3, 4, trace(sheetId, "grid:main")),
            occurrences = occurrences,
            regions = regions,
            constructs = constructs,
            alignments = alignments,
            anchors = anchors,
            lanes = lanes,
            routes = routes,
            gridReferences = gridReferences,
            quality = quality,
            sourceTrace = trace(sheetId, "sheet"),
        )
        val sheets = mutableListOf(sheet)
        val document = SpatialDocument(sheets)

        sheets.clear()
        occurrences.clear()
        regions.clear()
        constructs.clear()
        alignments.clear()
        anchors.clear()
        lanes.clear()
        routes.clear()
        gridReferences.clear()

        assertEquals(listOf(sheet), document.sheets)
        assertEquals(listOf(occurrence), document.sheets.single().occurrences)
        assertEquals(listOf(region), document.sheets.single().regions)
        assertEquals(listOf(construct), document.sheets.single().constructs)
        assertEquals(listOf(alignment), document.sheets.single().alignments)
        assertEquals(listOf(anchor), document.sheets.single().anchors)
        assertEquals(listOf(lane), document.sheets.single().lanes)
        assertEquals(listOf(route), document.sheets.single().routes)
        assertEquals(listOf(gridReference), document.sheets.single().gridReferences)
        assertEquals(qualityMetrics, document.sheets.single().quality.metrics)
        assertEquals(quality, document.sheets.single().quality)
        assertFalse(
            SpatialDocument::class.java.declaredFields.any { field ->
                field.name in setOf(
                    "grids",
                    "occurrences",
                    "regions",
                    "constructs",
                    "anchorPositions",
                    "alignments",
                    "lanes",
                    "routes",
                    "qualityMeasurements",
                    "gridReferences",
                )
            },
        )
        assertFalse(
            SpatialDocument::class.java.declaredMethods.any { method ->
                method.name in setOf(
                    "getGrids",
                    "getOccurrences",
                    "getRegions",
                    "getConstructs",
                    "getAnchorPositions",
                    "getAlignments",
                    "getLanes",
                    "getRoutes",
                    "getQualityMeasurements",
                    "getGridReferences",
                )
            },
        )
    }

    @Test
    fun `spatial document declares authority identity and required facts`() {
        val q1Anchor = anchor("sheet:main", "occurrence:Q1", "port:Q1.1", SpatialPoint(12, 24))
        val routeId = SpatialRouteId("sheet:main", "route:Q1-M1")
        val laneId = SpatialLaneId("sheet:main", SpatialLaneOrientation.HORIZONTAL, 24)
        val sheet = testSheet(
            occurrences = listOf(
                SpatialOccurrenceGeometry(
                    occurrenceId = SpatialOccurrenceId("sheet:main", "occurrence:Q1"),
                    subjectId = StableSemanticIdentity("component:Q1"),
                    sheetId = "sheet:main",
                    regionId = "region:rail-a",
                    rectangle = SpatialRect(10, 20, 80, 40),
                    placementReason = SpatialPlacementReason(listOf("authored Region order")),
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf("sheet:main", "region:rail-a", "occurrence:Q1"),
                        geometryElementIds = listOf(GeometryElementId("projection:Q1")),
                    ),
                ),
            ),
            regions = listOf(
                SpatialRegionGeometry(
                    regionId = SpatialRegionId("sheet:main", "region:rail-a"),
                    sheetId = "sheet:main",
                    memberOccurrenceIds = listOf(SpatialOccurrenceId("sheet:main", "occurrence:Q1")),
                    bounds = SpatialRect(0, 0, 120, 80),
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf("sheet:main", "region:rail-a", "occurrence:Q1"),
                        geometryElementIds = listOf(GeometryElementId("projection:region:rail-a")),
                    ),
                ),
            ),
            anchors = listOf(q1Anchor),
            alignments = listOf(
                SpatialAlignment(
                    alignmentId = SpatialAlignmentId(
                        sheetId = "sheet:main",
                        source = SpatialAlignmentSource.Region(
                            SpatialRegionId("sheet:main", "region:rail-a"),
                        ),
                    ),
                    sheetId = "sheet:main",
                    constraintSource = SpatialAlignmentSource.Region(
                        SpatialRegionId("sheet:main", "region:rail-a"),
                    ),
                    occurrenceIds = listOf(SpatialOccurrenceId("sheet:main", "occurrence:Q1")),
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf("sheet:main", "region:rail-a", "occurrence:Q1"),
                        geometryElementIds = listOf(GeometryElementId("projection:region:rail-a")),
                    ),
                ),
            ),
            lanes = listOf(
                SpatialLane(
                    laneId,
                    "sheet:main",
                    SpatialLaneOrientation.HORIZONTAL,
                    24,
                    listOf(routeId),
                ),
            ),
            routes = listOf(
                SpatialRoute(
                    routeId = routeId,
                    sheetId = "sheet:main",
                    connectionId = StableSemanticIdentity("connection:Q1-M1"),
                    sourceAnchorId = q1Anchor.anchorId,
                    targetAnchorId = anchorId("sheet:main", "occurrence:M1", "port:M1.1"),
                    laneId = laneId,
                    sourceTrace = routeTrace("route:Q1-M1"),
                    points = listOf(
                        SpatialPoint(12, 24),
                        SpatialPoint(80, 24),
                    ),
                ),
            ),
            quality = SpatialQualitySnapshot(
                qualitySnapshotId = SpatialQualitySnapshotId("sheet:main"),
                sheetId = "sheet:main",
                metrics = zeroSpatialQualityMetrics(),
                sourceTrace = trace("sheet:main", "quality"),
            ),
        )
        val document = SpatialDocument(listOf(sheet))

        assertEquals("SpatialDocument", SpatialReality.rootName)
        assertEquals("spatial compiler", SpatialReality.authority)
        assertEquals("Spatial Reality", SpatialReality.declaration.name)
        assertContains(SpatialReality.ownedFacts, "occurrence geometry")
        assertContains(SpatialReality.ownedFacts, "Region geometry")
        assertContains(SpatialReality.ownedFacts, "Construct geometry")
        assertContains(SpatialReality.ownedFacts, "anchor position")
        assertContains(SpatialReality.ownedFacts, "alignment")
        assertContains(SpatialReality.ownedFacts, "lane")
        assertContains(SpatialReality.ownedFacts, "route")
        assertContains(SpatialReality.ownedFacts, "grid definition")
        assertContains(SpatialReality.ownedFacts, "Grid Reference")
        assertContains(SpatialReality.ownedFacts, "quality snapshot")
        assertFalse("quality measurement" in SpatialReality.ownedFacts)
        assertContains(
            SpatialReality.identityRules.map { rule -> rule.fact },
            "quality snapshot",
        )
        assertContains(SpatialReality.requiredFacts, "occurrence geometry identity")
        assertContains(SpatialReality.requiredFacts, "route identity")
        assertContains(SpatialReality.requiredFacts, "lane identity")
        assertEquals(1, sheet.occurrences.size)
        assertEquals(1, sheet.routes.size)
        assertEquals(zeroSpatialQualityMetrics(), sheet.quality.metrics)
        assertFalse(SpatialReality.ownedFacts.any { fact -> fact.contains("stroke", ignoreCase = true) })
    }

    @Test
    fun `spatial validation reports missing facts in plain language`() {
        val result = SpatialReality.validate(
            SpatialDocument(
                listOf(
                    testSheet(
                        routes = listOf(
                    SpatialRoute(
                        routeId = SpatialRouteId("sheet:main", "route:broken"),
                        sheetId = "sheet:main",
                        connectionId = StableSemanticIdentity(""),
                        sourceAnchorId = anchorId("sheet:main", "occurrence:broken-source", "port:broken-source"),
                        targetAnchorId = anchorId("sheet:main", "occurrence:broken-target", "port:broken-target"),
                        laneId = SpatialLaneId("sheet:main", SpatialLaneOrientation.HORIZONTAL, 0),
                        sourceTrace = routeTrace("route:broken"),
                        points = listOf(SpatialPoint(0, 0), SpatialPoint(10, 0)),
                    ),
                        ),
                    ),
                ),
            ),
        )

        assertFalse(result.isValid)
        assertContains(result.diagnostics.map(SpatialDiagnostic::subject), "Sheet sheet:main")
        assertContains(
            result.diagnostics.map(SpatialDiagnostic::problem),
            "has no Occurrence geometry facts",
        )
        assertContains(
            result.diagnostics.map(SpatialDiagnostic::problem),
            "has no Region geometry facts",
        )
        assertFalse(result.diagnostics.any { diagnostic ->
            diagnostic.subject.isBlank() || diagnostic.problem.isBlank() || diagnostic.correction.isBlank()
        })
    }

    private fun anchorId(sheetId: String, occurrenceId: String, portId: String): SpatialAnchorId =
        SpatialAnchorId(
            sheetId = sheetId,
            occurrenceId = SpatialOccurrenceId(sheetId, occurrenceId),
            portId = StableSemanticIdentity(portId),
        )

    private fun anchor(
        sheetId: String,
        occurrenceId: String,
        portId: String,
        point: SpatialPoint,
    ): SpatialAnchorPosition {
        val id = anchorId(sheetId, occurrenceId, portId)
        return SpatialAnchorPosition(
            anchorId = id,
            sheetId = sheetId,
            subject = SpatialOccurrencePortSubject(id.occurrenceId, id.portId),
            side = SpatialBoundarySide.LEFT,
            point = point,
            sourceTrace = SpatialSourceTrace(
                projectionIds = listOf(sheetId, occurrenceId, portId),
                geometryElementIds = listOf(GeometryElementId("geometry:$occurrenceId:$portId")),
            ),
        )
    }

    private fun routeTrace(routeId: String): SpatialSourceTrace = SpatialSourceTrace(
        projectionIds = listOf("sheet:main", routeId, "occurrence:source", "port:source", "occurrence:target", "port:target"),
        geometryElementIds = listOf(GeometryElementId("geometry:$routeId")),
    )

    private fun trace(sheetId: String, subjectId: String): SpatialSourceTrace = SpatialSourceTrace(
        projectionIds = listOf(sheetId, subjectId),
        geometryElementIds = listOf(GeometryElementId("geometry:$subjectId")),
    )

    private fun testSheet(
        occurrences: List<SpatialOccurrenceGeometry> = emptyList(),
        regions: List<SpatialRegionGeometry> = emptyList(),
        constructs: List<SpatialConstructGeometry> = emptyList(),
        alignments: List<SpatialAlignment> = emptyList(),
        anchors: List<SpatialAnchorPosition> = emptyList(),
        lanes: List<SpatialLane> = emptyList(),
        routes: List<SpatialRoute> = emptyList(),
        gridReferences: List<SpatialGridReference> = emptyList(),
        quality: SpatialQualitySnapshot = SpatialQualitySnapshot(
            qualitySnapshotId = SpatialQualitySnapshotId("sheet:main"),
            sheetId = "sheet:main",
            metrics = zeroSpatialQualityMetrics(),
            sourceTrace = trace("sheet:main", "quality"),
        ),
    ): SpatialSheet = SpatialSheet(
        sheetId = "sheet:main",
        extent = SpatialRect(0, 0, 1200, 800),
        drawingArea = SpatialRect(40, 60, 1120, 640),
        grid = SpatialGridDefinition(
            sheetId = "sheet:main",
            gridId = "grid:main",
            drawingArea = SpatialRect(40, 60, 1120, 640),
            rows = 3,
            columns = 4,
            sourceTrace = trace("sheet:main", "grid:main"),
        ),
        occurrences = occurrences,
        regions = regions,
        constructs = constructs,
        alignments = alignments,
        anchors = anchors,
        lanes = lanes,
        routes = routes,
        gridReferences = gridReferences,
        quality = quality,
        sourceTrace = trace("sheet:main", "sheet"),
    )
}
