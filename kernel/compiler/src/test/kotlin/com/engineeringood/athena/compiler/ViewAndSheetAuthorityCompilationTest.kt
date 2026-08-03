package com.engineeringood.athena.compiler

import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionReality
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetId
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ViewAndSheetAuthorityCompilationTest {

    @Test
    fun `authored view compiles to one projection view with sheets and grid reference system`() {
        val success = compile(
            """
            system Demo {
              device D { }
              view schematic {
                sheet S1
                grid G1 { rows 3 columns 4 }
                region "Main" { occurrences [D] }
              }
            }
            """.trimIndent(),
        )

        val document = success.authoredProjectionViews.single()
        assertEquals("schematic", document.view.id)
        assertEquals(1, document.sheets.size)
        val sheet = document.sheets.single()
        assertEquals("schematic/sheet/S1", sheet.sheetId.value)
        val grid = sheet.grid
        assertTrue(grid != null, "Sheet must expose a grid reference system.")
        assertEquals(3, grid.rows)
        assertEquals(4, grid.columns)
        assertContains(grid.cellReferences(), "A1")
        assertContains(grid.cellReferences(), "C4")
        assertEquals(listOf("component:D"), sheet.subjects.map { subject -> subject.semanticId.value })
        assertEquals(listOf("Main"), sheet.regions.map { region -> region.name })
        assertTrue(success.authoredProjectionDiagnostics.isEmpty())
    }

    @Test
    fun `view with no sheets fails with a plain diagnostic`() {
        val success = compile(
            """
            system Demo {
              device D { }
              view emptyView { }
            }
            """.trimIndent(),
        )

        assertTrue(success.authoredProjectionViews.isEmpty())
        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "no sheets")
    }

    @Test
    fun `duplicate sheet identity fails with a plain diagnostic`() {
        val success = compile(
            """
            system Demo {
              device D { }
              view v {
                sheet A
                sheet A
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "duplicate sheet")
    }

    @Test
    fun `projection reality validation rejects empty sheets at model level`() {
        val document = ProjectionDocument(
            view = ViewDefinition(id = "schematic", displayName = "schematic"),
            nodes = emptyList(),
            connections = emptyList(),
            sheets = listOf(
                ProjectionSheet(
                    sheetId = ProjectionSheetId("schematic/sheet/1"),
                    displayName = "S1",
                    order = 1,
                ),
            ),
        )

        val result = ProjectionReality.validate(document)
        assertContains(result.issues.map { issue -> issue.message }.joinToString("\n"), "empty sheet")
    }

    private fun compile(source: String): CompilerCompilationSuccess {
        val path = Files.createTempFile("athena-m40-view-authority", ".athena")
        return assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path, source))
    }
}
