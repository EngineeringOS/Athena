package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionReality
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetCoordinateZone
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetPageSize
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.projection.ProjectionSheetFrame
import com.engineeringood.athena.projection.ProjectionSheetRevisionMetadata
import com.engineeringood.athena.projection.ProjectionSheetTitleBlock
import com.engineeringood.athena.projection.ProjectionSheetViewComposition
import com.engineeringood.athena.layout.ViewDefinition
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthoredProjectionTransformationTest {

    @Test
    fun `typed transformation emits projection only with trace and determinism`() {
        val source =
            """
            system Demo {
              device Supply { port L1 { direction out signal Power role line } }
              device Breaker { port line { direction in signal Power role line } }
              connect feed Supply.L1 to Breaker.line
              view schematic {
                sheet S1
                region "Power" { occurrences [Supply, Breaker] }
                power-rail L1 [Supply.L1, Breaker.line]
              }
            }
            """.trimIndent()
        val engineering = compile(source).document

        val transformation = AuthoredProjectionTransformation()
        val first = assertIs<RealityTransformationResult.Success<ProjectionDocument>>(transformation.transform(engineering))
        val second = assertIs<RealityTransformationResult.Success<ProjectionDocument>>(transformation.transform(engineering))

        assertEquals(first.output, second.output)
        assertEquals("schematic", first.output.view.id)
        assertEquals(listOf("Supply", "Breaker"), first.output.nodes.map { node -> node.label })
        assertEquals(
            listOf("port:Supply.L1", "port:Breaker.line"),
            first.output.occurrencePorts.map { port -> port.occurrencePortId.portId.value },
        )
        val connection = first.output.connections.single()
        assertEquals("port:Supply.L1", connection.source?.occurrencePortId?.portId?.value)
        assertEquals("port:Breaker.line", connection.target?.occurrencePortId?.portId?.value)
        val subjectIds = first.output.sheets.single().subjects.map { it.semanticId.value }
        assertEquals(listOf("component:Supply", "component:Breaker"), subjectIds.take(2))
        assertTrue(subjectIds.single { subjectId -> subjectId.startsWith("connection:") }.endsWith(":feed"))
        assertEquals(listOf("power-rail"), first.output.sheets.single().constructs.map { it.kind })
    }

    @Test
    fun `boundary validator rejects layout-box zones and accepts logical zones`() {
        val logical = ProjectionDocument(
            view = ViewDefinition(id = "schematic", displayName = "schematic"),
            nodes = emptyList(),
            connections = emptyList(),
            sheets = listOf(sheetWithZones(listOf(ProjectionSheetCoordinateZone("body", "Body", 1)))),
        )
        assertTrue(ProjectionBoundaryValidator.report(logical).isEmpty())

        val layoutBox = ProjectionDocument(
            view = ViewDefinition(id = "schematic", displayName = "schematic"),
            nodes = emptyList(),
            connections = emptyList(),
            sheets = listOf(sheetWithZones(listOf(ProjectionSheetCoordinateZone("layout-box", "Layout Box", 1)))),
        )
        val issues = ProjectionBoundaryValidator.report(layoutBox)
        assertEquals(1, issues.size)
        assertTrue(issues.single().message.contains("layout-box"))
    }

    private fun sheetWithZones(zones: List<ProjectionSheetCoordinateZone>): ProjectionSheet {
        val publication = ProjectionSheetPublication(
            pageSize = ProjectionSheetPageSize(format = "A3", orientation = "landscape"),
            frame = ProjectionSheetFrame(frameId = "frame", style = "schematic"),
            coordinateZones = zones,
            titleBlock = ProjectionSheetTitleBlock(sheetTitle = "S1", sheetFamily = "schematic", sheetNumber = "1"),
            revisionMetadata = ProjectionSheetRevisionMetadata(revisionCode = "A", revisionNote = "test"),
            viewComposition = ProjectionSheetViewComposition(primaryViewId = "schematic", primarySheetOrder = 1),
        )
        return ProjectionSheet(
            sheetId = ProjectionSheetId("schematic/sheet/S1"),
            displayName = "S1",
            order = 1,
            publication = publication,
        )
    }

    private fun compile(source: String): CompilerCompilationSuccess {
        val path = Files.createTempFile("athena-m40-transformation", ".athena")
        return assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path, source))
    }
}
