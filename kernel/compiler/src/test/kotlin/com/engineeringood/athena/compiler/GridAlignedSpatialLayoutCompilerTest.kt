package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.layout.SheetPlacementConstraint
import com.engineeringood.athena.layout.SheetPlacementPoint
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetGrid
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetSubject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GridAlignedSpatialLayoutCompilerTest {
    @Test
    fun `canonical Sheet point lowers directly from drawing origin and preserves identity`() {
        val projection = projection()
        val constraint = constraint("Q1", x = 24, y = 16)

        val result = GridAlignedSpatialLayoutCompiler().compile(projection, listOf(constraint))

        assertTrue(result.diagnostics.isEmpty())
        val occurrence = result.occurrences.single { it.occurrenceId.projectionId == "node:Q1" }
        assertEquals(63, occurrence.rectangle.x)
        assertEquals(75, occurrence.rectangle.y)
        assertEquals(StableSemanticIdentity("entity:Q1"), occurrence.subjectId)
        assertTrue(occurrence.placementReason.text.contains("authored Sheet point 24,16"))
    }

    @Test
    fun `same canonical point is unchanged when Plot Frame count changes`() {
        val projection = projection()
        val constraint = constraint("Q1", x = 24, y = 16)

        val baseline = GridAlignedSpatialLayoutCompiler().compile(projection, listOf(constraint))
        val resizedFrameProjection = projection(columns = 10, rows = 12)
        val changedFrame = GridAlignedSpatialLayoutCompiler().compile(resizedFrameProjection, listOf(constraint))

        assertTrue(baseline.diagnostics.isEmpty())
        assertTrue(changedFrame.diagnostics.isEmpty())
        assertEquals(
            baseline.occurrences.single { it.occurrenceId.projectionId == "node:Q1" }.rectangle,
            changedFrame.occurrences.single { it.occurrenceId.projectionId == "node:Q1" }.rectangle,
        )
    }

    @Test
    fun `authored logical Sheet point uses the same bounded occurrence footprint as automatic layout`() {
        val projection = projection()
        val page = SpatialPageGeometryProfiles.logical(
            grid = requireNotNull(projection.sheets.single().grid),
            pageSize = projection.sheets.single().publication.pageSize,
        )

        val result = GridAlignedSpatialLayoutCompiler().compile(
            projection = projection,
            constraints = listOf(constraint("Q1", x = 24, y = 16)),
            pageGeometries = mapOf(projection.sheets.single().sheetId.value to page),
        )

        assertTrue(result.diagnostics.isEmpty())
        assertTrue(result.occurrences.single { it.occurrenceId.projectionId == "node:Q1" }.rectangle.isInside(page.drawingArea))
    }

    @Test
    fun `out of bounds and overlap fail closed`() {
        val projection = projection()
        val constraints = listOf(
            constraint("Q1", x = 24, y = 16),
            constraint("KM1", x = 24, y = 16),
        )

        val result = GridAlignedSpatialLayoutCompiler().compile(projection, constraints)

        assertTrue(result.occurrences.isEmpty())
        assertTrue(result.diagnostics.any { it.problem.contains("overlap") })
    }

    @Test
    fun `point outside drawing area fails closed`() {
        val result = GridAlignedSpatialLayoutCompiler().compile(
            projection(),
            listOf(constraint("Q1", x = 1200, y = 1)),
        )

        assertTrue(result.occurrences.isEmpty())
        assertEquals("derived rectangle is outside the Sheet drawing area.", result.diagnostics.single().problem)
    }

    @Test
    fun `reordered inputs produce equal output`() {
        val projection = projection()
        val first = listOf(
            constraint("KM1", x = 64, y = 48),
            constraint("Q1", x = 24, y = 16),
        )
        val forward = GridAlignedSpatialLayoutCompiler().compile(projection, first)
        val reversed = GridAlignedSpatialLayoutCompiler().compile(projection, first.reversed())
        assertEquals(forward, reversed)
    }

    @Test
    fun `explicit Sheet placement admits a region beyond automatic vertical capacity`() {
        val nodes = (1..12).map { index -> node("Control$index") }
        val sheetId = ProjectionSheetId("view/test/sheet/1")
        val projection = ProjectionDocument(
            view = ViewDefinition("view:test", "Test"),
            nodes = nodes,
            connections = emptyList(),
            sheets = listOf(
                ProjectionSheet(
                    sheetId = sheetId,
                    displayName = "Test",
                    order = 0,
                    subjects = nodes.map { node -> ProjectionSheetSubject(node.semanticId, listOf(node.projectionId)) },
                    regions = listOf(
                        com.engineeringood.athena.projection.ProjectionRegion(
                            regionId = "view/test/control",
                            name = "Control",
                            occurrenceNames = nodes.map(ProjectionNode::label),
                        ),
                    ),
                    grid = ProjectionSheetGrid("grid:test", rows = 16, columns = 17),
                ),
            ),
        )
        val constraints = nodes.mapIndexed { index, node ->
            SheetPlacementConstraint(
                sheetId = sheetId.value,
                occurrenceName = node.label,
                occurrenceId = LayoutOccurrenceId(node.projectionId.value),
                point = SheetPlacementPoint(8 + (index % 4) * 16, 8 + (index / 4) * 20),
                snapStep = 4,
                locked = false,
                sourceSpan = LayoutSourceSpan("test.sheet.athena", index + 1, 1, index + 1, 20),
            )
        }

        val result = GridAlignedSpatialLayoutCompiler().compile(
            projection = projection,
            constraints = constraints,
            pageGeometries = mapOf(
                sheetId.value to SpatialPageGeometryProfiles.logical(
                    grid = requireNotNull(projection.sheets.single().grid),
                    pageSize = projection.sheets.single().publication.pageSize,
                ),
            ),
        )

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(nodes.size, result.occurrences.size)
    }

    private fun constraint(
        occurrence: String,
        x: Int,
        y: Int,
    ): SheetPlacementConstraint = SheetPlacementConstraint(
        sheetId = "view/test/sheet/1",
        occurrenceName = occurrence,
        occurrenceId = LayoutOccurrenceId("node:$occurrence"),
        point = SheetPlacementPoint(x, y),
        snapStep = 1,
        locked = false,
        sourceSpan = LayoutSourceSpan("test.sheet.athena", 4, 1, 4, 20),
    )

    private fun projection(columns: Int = 17, rows: Int = 8): ProjectionDocument {
        val sheetId = ProjectionSheetId("view/test/sheet/1")
        val q1 = node("Q1")
        val km1 = node("KM1")
        return ProjectionDocument(
            view = ViewDefinition("view:test", "Test"),
            nodes = listOf(q1, km1),
            connections = emptyList(),
            sheets = listOf(
                ProjectionSheet(
                    sheetId = sheetId,
                    displayName = "Test",
                    order = 0,
                    subjects = listOf(
                        ProjectionSheetSubject(q1.semanticId, listOf(q1.projectionId)),
                        ProjectionSheetSubject(km1.semanticId, listOf(km1.projectionId)),
                    ),
                    grid = ProjectionSheetGrid("grid:test", rows = rows, columns = columns),
                ),
            ),
        )
    }

    private fun node(label: String): ProjectionNode = ProjectionNode(
        projectionId = ProjectionNodeId("node:$label"),
        semanticId = StableSemanticIdentity("entity:$label"),
        label = label,
        originGeometryElementId = GeometryElementId("geometry:$label"),
    )
}
