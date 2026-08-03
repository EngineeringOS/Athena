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
import com.engineeringood.athena.projection.ProjectionSheetGrid
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.projection.ProjectionSheetSubject
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialAlignmentId
import com.engineeringood.athena.spatial.SpatialAlignmentSource
import com.engineeringood.athena.spatial.SpatialRegionId
import com.engineeringood.athena.spatial.SpatialGridReferenceSubject
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ProjectionSpatialCompilerTest {
    @Test
    fun `projection to spatial emits spatial document through typed transformation`() {
        val transformation: RealityTransformation<ProjectionDocument, SpatialDocument> =
            ProjectionSpatialCompiler()

        val result = transformation.transform(projectionDocument())

        val output = assertIs<RealityTransformationResult.Success<SpatialDocument>>(result).output
        assertEquals(listOf("projection/node/component:Q1", "projection/node/component:Supply"), output.occurrences.map {
            occurrence -> occurrence.occurrenceId.projectionId
        })
        assertEquals(2, output.occurrences.size)
        assertEquals(1, output.regions.size)
        assertEquals(4, output.anchorPositions.size)
        val sheetId = "engineering-projection/sheet/01-main"
        assertEquals(
            listOf(
                SpatialAlignmentId(
                    sheetId = sheetId,
                    source = SpatialAlignmentSource.Region(
                        SpatialRegionId(sheetId, "$sheetId/region/unassigned"),
                    ),
                ),
            ),
            output.alignments.map { alignment -> alignment.alignmentId },
        )
        assertEquals(listOf("lane:engineering-projection:main"), output.lanes.map { lane -> lane.laneId })
        assertEquals(1, output.grids.size)
        assertEquals(
            listOf(
                SpatialGridReferenceSubject.Occurrence(output.occurrences[0].occurrenceId),
                SpatialGridReferenceSubject.Occurrence(output.occurrences[1].occurrenceId),
            ),
            output.gridReferences.map { reference -> reference.subject },
        )
        assertEquals(listOf("B2", "B2"), output.gridReferences.map { reference -> reference.cellReference })
    }

    @Test
    fun `projection to spatial derives routes from projection connection identity`() {
        val result = ProjectionSpatialCompiler().transform(projectionDocument())
        val output = assertIs<RealityTransformationResult.Success<SpatialDocument>>(result).output

        assertEquals(1, output.routes.size)
        val route = output.routes.single()
        assertEquals("route:projection/connection/connection:Supply.L1-to-Q1.1", route.routeId)
        assertEquals("connection:Supply.L1-to-Q1.1", route.connectionId.value)
        assertEquals("lane:engineering-projection:main", route.laneId)
        assertEquals("anchor:projection/node/component:Supply:right", route.sourceAnchorId)
        assertEquals("anchor:projection/node/component:Q1:left", route.targetAnchorId)
        val anchorById = output.anchorPositions.associateBy { anchor -> anchor.anchorId }
        val sourceAnchor = requireNotNull(anchorById[route.sourceAnchorId])
        val targetAnchor = requireNotNull(anchorById[route.targetAnchorId])
        assertEquals(sourceAnchor.x, route.points.first().x)
        assertEquals(sourceAnchor.y, route.points.first().y)
        assertEquals(targetAnchor.x, route.points.last().x)
        assertEquals(targetAnchor.y, route.points.last().y)
    }

    @Test
    fun `projection to spatial publishes basic quality measurements`() {
        val result = ProjectionSpatialCompiler().transform(projectionDocument())
        val output = assertIs<RealityTransformationResult.Success<SpatialDocument>>(result).output

        assertContains(output.qualityMeasurements.map { measurement -> measurement.kind }, "overlap-count")
        assertContains(output.qualityMeasurements.map { measurement -> measurement.kind }, "route-count")
        assertTrue(output.qualityMeasurements.all { measurement -> measurement.value >= 0.0 })
    }

    @Test
    fun `projection to spatial reports plain diagnostics before presentation`() {
        val result = ProjectionSpatialCompiler().transform(
            ProjectionDocument(
                view = ViewDefinition(id = "", displayName = "Broken"),
                nodes = emptyList(),
                connections = emptyList(),
                sheets = emptyList(),
            ),
        )

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertFalse(failure.diagnostics.isEmpty())
        assertTrue(failure.diagnostics.any { diagnostic ->
            diagnostic.reality == "Projection Reality" &&
                diagnostic.message == "missing view identity"
        })
        assertTrue(failure.diagnostics.any { diagnostic ->
            diagnostic.reality == "Projection Reality" &&
                diagnostic.message == "missing sheet facts"
        })
    }

    @Test
    fun `projection to spatial fails closed when owning sheet grid is missing`() {
        val projection = projectionDocument()
        val result = ProjectionSpatialCompiler().transform(
            projection.copy(sheets = projection.sheets.map { sheet -> sheet.copy(grid = null) }),
        )

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertEquals(1, failure.diagnostics.size)
        assertEquals("Sheet engineering-projection/sheet/01-main grid", failure.diagnostics.single().subject)
        assertEquals("is missing", failure.diagnostics.single().problem)
        assertEquals(
            "Define a grid for Sheet engineering-projection/sheet/01-main before compiling Grid References.",
            failure.diagnostics.single().correction,
        )
        assertEquals(
            listOf("engineering-projection/sheet/01-main"),
            failure.diagnostics.single().sourceTrace?.projectionIds,
        )
    }

    @Test
    fun `new spatial transformation names avoid stale architecture terms`() {
        val names = listOf(ProjectionSpatialCompiler::class.simpleName.orEmpty())
        val banned = listOf("M39", "V0", "V1", "Evidence", "ProfessionalControlDrawing", "Compatibility")

        banned.forEach { token ->
            assertFalse(
                names.any { name -> name.contains(token) },
                "Transformation names must not contain `$token`: $names",
            )
        }
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
                    grid = ProjectionSheetGrid(gridId = "grid:main", rows = 3, columns = 4),
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
}
