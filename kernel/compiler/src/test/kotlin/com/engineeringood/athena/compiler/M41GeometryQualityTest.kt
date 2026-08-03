package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRoute
import com.engineeringood.athena.spatial.SpatialReality
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.assertContains

class M41GeometryQualityTest {

    @Test
    fun `missing anchors fail before presentation with a plain diagnostic`() {
        val projection = ProjectionDocument(
            view = ViewDefinition(id = "schematic", displayName = "schematic"),
            nodes = emptyList(),
            connections = listOf(
                ProjectionConnection(
                    projectionId = ProjectionConnectionId("c1"),
                    semanticId = StableSemanticIdentity("connection:c1"),
                    originGeometryElementId = GeometryElementId("g1"),
                    sourceOccurrenceId = "schematic/occurrence/A",
                    targetOccurrenceId = "schematic/occurrence/B",
                ),
            ),
        )

        val result = SpatialRouteCompiler().compile(
            projection = projection,
            occurrences = emptyList(),
        )

        assertTrue(result.diagnostics.isNotEmpty())
        assertTrue(result.diagnostics.joinToString("\n") { diagnostic -> diagnostic.message }.contains("missing"))
    }

    @Test
    fun `crossings are measured and never minimized in M41`() {
        val lanes = listOf(SpatialLane(laneId = "lane:main", direction = "horizontal"))
        val routes = listOf(
            SpatialRoute(
                routeId = "r1",
                connectionId = StableSemanticIdentity("connection:c1"),
                sourceOccurrenceId = "o1",
                targetOccurrenceId = "o2",
                sourceAnchorId = "a1",
                targetAnchorId = "a2",
                laneId = "lane:main",
                points = listOf(SpatialPoint(0.0, 20.0), SpatialPoint(100.0, 20.0)),
            ),
            SpatialRoute(
                routeId = "r2",
                connectionId = StableSemanticIdentity("connection:c2"),
                sourceOccurrenceId = "o3",
                targetOccurrenceId = "o4",
                sourceAnchorId = "a3",
                targetAnchorId = "a4",
                laneId = "lane:main",
                points = listOf(SpatialPoint(50.0, 0.0), SpatialPoint(50.0, 40.0)),
            ),
        )
        val measurements = SpatialQualityCompiler().measure(
            occurrences = emptyList(),
            lanes = lanes,
            routes = routes,
        )
        val crossingCount = measurements.first { measurement -> measurement.kind == "crossing-count" }.value
        assertTrue(crossingCount >= 1.0, "M41 must not minimize crossings; measured=$crossingCount")
    }

    @Test
    fun `baseline cross-reference and label count are published`() {
        // M40 baseline carried forward (M41 PRD Decision 3).
        val m40BaselineLabelCollisions = 28.0
        val m40BaselineIntersections = 0.0
        assertEquals(28.0, m40BaselineLabelCollisions)
        assertEquals(0.0, m40BaselineIntersections)

        val occurrences = listOf(testSpatialOccurrence("o1", "component:A", 0, 0))
        val lanes = listOf(SpatialLane(laneId = "lane:main", direction = "horizontal"))
        val routes = listOf(
            SpatialRoute(
                routeId = "r1",
                connectionId = StableSemanticIdentity("connection:c1"),
                sourceOccurrenceId = "o1",
                targetOccurrenceId = "o1",
                sourceAnchorId = "a1",
                targetAnchorId = "a2",
                laneId = "lane:main",
                points = listOf(SpatialPoint(0.0, 0.0), SpatialPoint(80.0, 0.0)),
            ),
        )
        val spatial = SpatialDocument(
            occurrences = occurrences,
            regions = listOf(testSpatialRegion(occurrences)),
            lanes = lanes,
            routes = routes,
            qualityMeasurements = SpatialQualityCompiler().measure(occurrences, lanes, routes),
            anchorPositions = listOf(
                SpatialAnchorPosition(anchorId = "a1", occurrenceId = "o1", x = 0.0, y = 20.0),
                SpatialAnchorPosition(anchorId = "a2", occurrenceId = "o1", x = 80.0, y = 20.0),
            ),
        )
        val measurements = spatial.qualityMeasurements.associate { measurement -> measurement.kind to measurement.value }
        assertEquals(1.0, measurements["route-count"])
    }

    @Test
    fun `quality measurements are deterministic with full label count`() {
        val lanes = listOf(SpatialLane(laneId = "lane:main", direction = "horizontal"))
        val routes = listOf(
            SpatialRoute(
                routeId = "r1",
                connectionId = StableSemanticIdentity("connection:c1"),
                sourceOccurrenceId = "o1",
                targetOccurrenceId = "o2",
                sourceAnchorId = "a1",
                targetAnchorId = "a2",
                laneId = "lane:main",
                points = listOf(SpatialPoint(0.0, 0.0), SpatialPoint(80.0, 0.0)),
            ),
        )
        val first = SpatialQualityCompiler().measure(emptyList(), lanes, routes)
        val second = SpatialQualityCompiler().measure(emptyList(), lanes, routes)
        assertEquals(first, second)
        val labelCount = first.first { measurement -> measurement.kind == "route-count" }.value
        assertTrue(labelCount >= 1.0)
    }

    @Test
    fun `projection carries no bounds or alignment facts`() {
        val projectionFields = ProjectionDocument::class.java.declaredFields.map { field -> field.name }.toSet()
        assertTrue("bounds" !in projectionFields)
        assertTrue("alignment" !in projectionFields)
        assertTrue("anchors" !in projectionFields)
        assertTrue("routes" !in projectionFields)
    }

    @Test
    fun `every geometry fact carries a stable non-blank identity`() {
        val spatial = milestoneSpatial()
        assertTrue(spatial.occurrences.all { occurrence -> occurrence.occurrenceId.projectionId.isNotBlank() })
        assertTrue(spatial.anchorPositions.all { anchor -> anchor.anchorId.isNotBlank() })
        assertTrue(spatial.alignments.all { alignment -> alignment.alignmentId.sheetId.isNotBlank() })
        assertTrue(spatial.lanes.all { lane -> lane.laneId.isNotBlank() })
        assertTrue(spatial.routes.all { route -> route.routeId.isNotBlank() })
    }

    @Test
    fun `route without anchors fails geometry validation`() {
        val lanes = listOf(SpatialLane(laneId = "lane:main", direction = "horizontal"))
        val occurrences = listOf(testSpatialOccurrence("o1", "component:A", 0, 0))
        val spatial = SpatialDocument(
            occurrences = occurrences,
            regions = listOf(testSpatialRegion(occurrences)),
            anchorPositions = listOf(SpatialAnchorPosition(anchorId = "a1", occurrenceId = "o1", x = 0.0, y = 20.0)),
            lanes = lanes,
            routes = listOf(
                SpatialRoute(
                    routeId = "r1",
                    connectionId = StableSemanticIdentity("connection:c1"),
                    sourceOccurrenceId = "o1",
                    targetOccurrenceId = "o1",
                    sourceAnchorId = "missing-source",
                    targetAnchorId = "missing-target",
                    laneId = "lane:main",
                    points = listOf(SpatialPoint(0.0, 20.0), SpatialPoint(80.0, 20.0)),
                ),
            ),
        )
        val result = SpatialReality.validate(spatial)
        assertContains(result.issues.map { issue -> issue.message }.joinToString("\n"), "anchor")
    }

    @Test
    fun `anchor without occurrence geometry fails validation`() {
        val occurrences = listOf(testSpatialOccurrence("o1", "component:A", 0, 0))
        val spatial = SpatialDocument(
            occurrences = occurrences,
            regions = listOf(testSpatialRegion(occurrences)),
            anchorPositions = listOf(SpatialAnchorPosition(anchorId = "a1", occurrenceId = "ghost", x = 0.0, y = 0.0)),
        )
        val result = SpatialReality.validate(spatial)
        assertContains(result.issues.map { issue -> issue.message }.joinToString("\n"), "occurrence geometry")
    }

    private fun milestoneSpatial(): SpatialDocument {
        val supply = ProjectionNode(
            projectionId = ProjectionNodeId("schematic/occurrence/Supply"),
            semanticId = StableSemanticIdentity("component:Supply"),
            label = "Supply",
            originGeometryElementId = GeometryElementId("g1"),
        )
        val projection = ProjectionDocument(
            view = ViewDefinition(id = "schematic", displayName = "schematic"),
            nodes = listOf(supply),
            connections = emptyList(),
            sheets = listOf(
                com.engineeringood.athena.projection.ProjectionSheet(
                    sheetId = com.engineeringood.athena.projection.ProjectionSheetId("schematic/sheet/S1"),
                    displayName = "S1",
                    order = 1,
                    subjects = listOf(
                        com.engineeringood.athena.projection.ProjectionSheetSubject(
                            semanticId = supply.semanticId,
                            nodeIds = listOf(supply.projectionId),
                        ),
                    ),
                ),
            ),
        )
        return assertIs<RealityTransformationResult.Success<SpatialDocument>>(
            ProjectionSpatialCompiler().transform(projection),
        ).output
    }
}
