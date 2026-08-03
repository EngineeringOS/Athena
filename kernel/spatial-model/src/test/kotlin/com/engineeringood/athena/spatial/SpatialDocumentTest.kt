package com.engineeringood.athena.spatial

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SpatialDocumentTest {
    @Test
    fun `spatial document declares authority identity and required facts`() {
        val document = SpatialDocument(
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
            anchorPositions = listOf(
                SpatialAnchorPosition(
                    anchorId = "anchor:Q1:1",
                    occurrenceId = "occurrence:Q1",
                    x = 12.0,
                    y = 24.0,
                ),
            ),
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
            lanes = listOf(SpatialLane("lane:power", "horizontal")),
            routes = listOf(
                SpatialRoute(
                    routeId = "route:Q1-M1",
                    connectionId = StableSemanticIdentity("connection:Q1-M1"),
                    sourceOccurrenceId = "occurrence:Q1",
                    targetOccurrenceId = "occurrence:M1",
                    sourceAnchorId = "anchor:Q1:1",
                    targetAnchorId = "anchor:M1:1",
                    laneId = "lane:power",
                    points = listOf(
                        SpatialPoint(12.0, 24.0),
                        SpatialPoint(80.0, 24.0),
                    ),
                ),
            ),
            qualityMeasurements = listOf(SpatialQualityMeasurement("overlap-count", 0.0)),
        )

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
        assertContains(SpatialReality.ownedFacts, "quality measurement")
        assertContains(SpatialReality.requiredFacts, "occurrence geometry identity")
        assertContains(SpatialReality.requiredFacts, "route identity")
        assertContains(SpatialReality.requiredFacts, "lane identity")
        assertEquals(1, document.occurrences.size)
        assertEquals(1, document.routes.size)
        assertEquals(1, document.qualityMeasurements.size)
        assertFalse(SpatialReality.ownedFacts.any { fact -> fact.contains("stroke", ignoreCase = true) })
    }

    @Test
    fun `spatial validation reports missing facts in plain language`() {
        val result = SpatialReality.validate(
            SpatialDocument(
                routes = listOf(
                    SpatialRoute(
                        routeId = "route:broken",
                        connectionId = StableSemanticIdentity(""),
                        sourceOccurrenceId = "occurrence:broken-source",
                        targetOccurrenceId = "occurrence:broken-target",
                        sourceAnchorId = "anchor:broken-source",
                        targetAnchorId = "anchor:broken-target",
                        laneId = "lane:missing",
                        points = listOf(SpatialPoint(0.0, 0.0), SpatialPoint(10.0, 0.0)),
                    ),
                ),
            ),
        )

        assertFalse(result.isValid)
        assertContains(
            result.issues.map { issue -> issue.reality to issue.message },
            "Spatial Reality" to "missing occurrence geometry facts",
        )
        assertContains(
            result.issues.map { issue -> issue.reality to issue.message },
            "Spatial Reality" to "missing Region geometry facts",
        )
        assertContains(
            result.issues.map { issue -> issue.reality to issue.message },
            "Spatial Reality" to "missing anchor position facts",
        )
        assertContains(
            result.issues.map { issue -> issue.reality to issue.message },
            "Spatial Reality" to "missing route identity",
        )
        assertContains(
            result.issues.map { issue -> issue.reality to issue.message },
            "Spatial Reality" to "missing lane identity",
        )
    }
}
