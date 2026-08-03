package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.projection.ProjectionSheetSubject
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRoute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpatialQualityCompilerTest {
    @Test
    fun `spatial quality compiler emits required baseline measurements`() {
        val measurements = SpatialQualityCompiler().measure(
            occurrences = occurrences(),
            lanes = lanes(),
            routes = routes(),
        )
        val kinds = measurements.map { measurement -> measurement.kind }

        assertContains(kinds, "overlap-count")
        assertContains(kinds, "body-intersection-count")
        assertContains(kinds, "crossing-count")
        assertContains(kinds, "twist-count")
        assertContains(kinds, "lane-use-count")
        assertContains(kinds, "label-pressure")
        assertEquals(kinds.distinct().size, kinds.size)
    }

    @Test
    fun `spatial quality measurements are non negative facts`() {
        val measurements = SpatialQualityCompiler().measure(occurrences(), lanes(), routes())

        assertTrue(measurements.all { measurement -> measurement.value >= 0.0 })
    }

    @Test
    fun `spatial quality facts do not claim professional routing`() {
        val text = SpatialQualityCompiler().measure(occurrences(), lanes(), routes())
            .joinToString(" ") { measurement -> measurement.kind }

        assertFalse(text.contains("professional", ignoreCase = true))
        assertFalse(text.contains("optimized", ignoreCase = true))
        assertFalse(text.contains("eplan", ignoreCase = true))
    }

    @Test
    fun `projection to spatial carries quality facts from spatial authority`() {
        val output = projectionToSpatialOutput()
        val kinds = output.qualityMeasurements.map { measurement -> measurement.kind }

        assertContains(kinds, "overlap-count")
        assertContains(kinds, "body-intersection-count")
        assertContains(kinds, "crossing-count")
        assertContains(kinds, "twist-count")
        assertContains(kinds, "lane-use-count")
        assertContains(kinds, "label-pressure")
    }

    @Test
    fun `new spatial quality names avoid stale architecture terms`() {
        val names = listOf(SpatialQualityCompiler::class.simpleName.orEmpty())
        val banned = listOf("M39", "V0", "V1", "Evidence", "ProfessionalControlDrawing", "Compatibility")

        banned.forEach { token ->
            assertFalse(
                names.any { name -> name.contains(token) },
                "Spatial quality names must not contain `$token`: $names",
            )
        }
    }

    private fun projectionToSpatialOutput(): SpatialDocument {
        val result = ProjectionSpatialCompiler().transform(projectionDocument())
        return kotlin.test.assertIs<RealityTransformationResult.Success<SpatialDocument>>(result).output
    }

    private fun projectionDocument(): ProjectionDocument {
        val view = ViewDefinition(id = "engineering-projection", displayName = "Engineering Projection")
        val supplyNode = ProjectionNode(
            projectionId = ProjectionNodeId("projection/node/component:Supply"),
            semanticId = StableSemanticIdentity("component:Supply"),
            label = "Supply",
            originGeometryElementId = GeometryElementId("origin:Supply"),
        )
        val breakerNode = ProjectionNode(
            projectionId = ProjectionNodeId("projection/node/component:Q1"),
            semanticId = StableSemanticIdentity("component:Q1"),
            label = "Q1",
            originGeometryElementId = GeometryElementId("origin:Q1"),
        )
        val connection = ProjectionConnection(
            projectionId = ProjectionConnectionId("projection/connection/connection:Supply.L1-to-Q1.1"),
            semanticId = StableSemanticIdentity("connection:Supply.L1-to-Q1.1"),
            originGeometryElementId = GeometryElementId("origin:connection"),
        )
        val subjects = listOf(
            ProjectionSheetSubject(supplyNode.semanticId, nodeIds = listOf(supplyNode.projectionId)),
            ProjectionSheetSubject(breakerNode.semanticId, nodeIds = listOf(breakerNode.projectionId)),
            ProjectionSheetSubject(connection.semanticId, connectionIds = listOf(connection.projectionId)),
        )
        val sheetId = ProjectionSheetId("engineering-projection/sheet/01-main")
        return ProjectionDocument(
            view = view,
            nodes = listOf(supplyNode, breakerNode),
            connections = listOf(connection),
            sheets = listOf(
                ProjectionSheet(
                    sheetId = sheetId,
                    displayName = "Engineering Projection Main",
                    order = 0,
                    subjects = subjects,
                    publication = ProjectionSheetPublication.fromProjectionState(
                        sheetId = sheetId,
                        displayName = "Engineering Projection Main",
                        order = 0,
                        subjects = subjects,
                    ),
                ),
            ),
        )
    }

    private fun occurrences(): List<SpatialOccurrenceGeometry> = listOf(
        testSpatialOccurrence("occurrence:A", "component:A", 0, 0),
        testSpatialOccurrence("occurrence:B", "component:B", 40, 0),
    )

    private fun lanes(): List<SpatialLane> = listOf(SpatialLane("lane:main", "horizontal"))

    private fun routes(): List<SpatialRoute> = listOf(
        SpatialRoute(
            routeId = "route:A-B",
            connectionId = StableSemanticIdentity("connection:A-B"),
            sourceOccurrenceId = "occurrence:A",
            targetOccurrenceId = "occurrence:B",
            sourceAnchorId = "anchor:A:right",
            targetAnchorId = "anchor:B:left",
            laneId = "lane:main",
            points = listOf(SpatialPoint(80.0, 20.0), SpatialPoint(40.0, 20.0)),
        ),
    )
}
