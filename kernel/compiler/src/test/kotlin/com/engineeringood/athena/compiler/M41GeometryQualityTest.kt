package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionEndpoint
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionOccurrencePortId
import com.engineeringood.athena.spatial.SpatialBoundarySide
import com.engineeringood.athena.projection.ProjectionSheetGrid
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRoute
import com.engineeringood.athena.spatial.SpatialReality
import com.engineeringood.athena.spatial.SpatialRect
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
                    source = ProjectionConnectionEndpoint(
                        ProjectionOccurrencePortId(
                            ProjectionNodeId("schematic/occurrence/A"),
                            StableSemanticIdentity("port:A.out"),
                        ),
                    ),
                    target = ProjectionConnectionEndpoint(
                        ProjectionOccurrencePortId(
                            ProjectionNodeId("schematic/occurrence/B"),
                            StableSemanticIdentity("port:B.in"),
                        ),
                    ),
                ),
            ),
        )

        val result = SpatialRouteCompiler().compile(
            projection = projection,
            sheets = emptyList(),
            occurrences = emptyList(),
            anchors = emptyList(),
        )

        assertTrue(result.diagnostics.isNotEmpty())
        assertTrue(result.diagnostics.joinToString("\n") { diagnostic -> diagnostic.problem }.contains("no resolved"))
    }

    @Test
    fun `crossings are measured and never minimized in M41`() {
        val lanes = listOf(testSpatialLane("r1", "r2"))
        val routes = listOf(
            testSpatialRoute(
                routeId = "r1",
                connectionId = "connection:c1",
                sourceAnchorId = testSpatialAnchorId("o1"),
                targetAnchorId = testSpatialAnchorId("o2"),
                points = listOf(SpatialPoint(0, 20), SpatialPoint(100, 20)),
            ),
            testSpatialRoute(
                routeId = "r2",
                connectionId = "connection:c2",
                sourceAnchorId = testSpatialAnchorId("o3"),
                targetAnchorId = testSpatialAnchorId("o4"),
                points = listOf(SpatialPoint(50, 0), SpatialPoint(50, 40)),
            ),
        )
        val measurements = SpatialQualityCompiler().measure(
            drawingArea = SpatialRect(0, 0, 120, 60),
            occurrences = emptyList(),
            constructs = emptyList(),
            lanes = lanes,
            routes = routes,
        )
        assertTrue(
            measurements.routeCrossingCount >= 1,
            "M41 must not minimize crossings; measured=${measurements.routeCrossingCount}",
        )
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
        val sheet = spatial.sheets.single()
        assertTrue(sheet.occurrences.all { occurrence -> occurrence.occurrenceId.projectionId.isNotBlank() })
        assertTrue(sheet.anchors.all { anchor -> anchor.anchorId.value.isNotBlank() })
        assertTrue(sheet.alignments.all { alignment -> alignment.alignmentId.sheetId.isNotBlank() })
        assertTrue(sheet.lanes.all { lane -> lane.laneId.value.isNotBlank() })
        assertTrue(sheet.routes.all { route -> route.routeId.value.isNotBlank() })
    }

    @Test
    fun `route without anchors fails geometry validation`() {
        val lanes = listOf(testSpatialLane("r1"))
        val occurrences = listOf(testSpatialOccurrence("o1", "component:A", 0, 0))
        val spatial = SpatialDocument(listOf(testSpatialSheet(
            occurrences = occurrences,
            regions = listOf(testSpatialRegion(occurrences)),
            anchors = listOf(
                testSpatialAnchor("o1", "port:o1", SpatialPoint(0, 20), SpatialBoundarySide.LEFT),
            ),
            lanes = lanes,
            routes = listOf(
                testSpatialRoute(
                    routeId = "r1",
                    connectionId = "connection:c1",
                    sourceAnchorId = testSpatialAnchorId("o1", "port:missing-source"),
                    targetAnchorId = testSpatialAnchorId("o1", "port:missing-target"),
                    points = listOf(SpatialPoint(0, 20), SpatialPoint(80, 20)),
                ),
            ),
        )))
        val result = SpatialReality.validate(spatial)
        assertContains(
            result.diagnostics.map { diagnostic -> diagnostic.problem }.joinToString("\n"),
            "does not resolve both endpoint Anchors exactly once",
        )
    }

    @Test
    fun `anchor without occurrence geometry fails validation`() {
        val occurrences = listOf(testSpatialOccurrence("o1", "component:A", 0, 0))
        val spatial = SpatialDocument(listOf(testSpatialSheet(
            occurrences = occurrences,
            regions = listOf(testSpatialRegion(occurrences)),
            anchors = listOf(
                testSpatialAnchor("ghost", "port:ghost", SpatialPoint(0, 0), SpatialBoundarySide.LEFT),
            ),
        )))
        val result = SpatialReality.validate(spatial)
        assertContains(
            result.diagnostics.map { diagnostic -> diagnostic.problem }.joinToString("\n"),
            "subject Occurrence ghost resolves to 0 geometry facts",
        )
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
                    grid = ProjectionSheetGrid("grid:S1", rows = 8, columns = 12),
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
