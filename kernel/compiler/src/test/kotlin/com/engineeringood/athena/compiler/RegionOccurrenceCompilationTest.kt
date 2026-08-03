package com.engineeringood.athena.compiler

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RegionOccurrenceCompilationTest {

    @Test
    fun `regions group occurrences traceable to engineering subjects`() {
        val success = compile(
            """
            system Demo {
              device Supply { }
              device Breaker { }
              view schematic {
                sheet S1
                region "Power Distribution" { occurrences [Supply, Breaker] }
              }
            }
            """.trimIndent(),
        )

        assertTrue(success.authoredProjectionDiagnostics.isEmpty())
        val document = success.authoredProjectionViews.single()
        val sheet = document.sheets.single()
        assertEquals(listOf("component:Supply", "component:Breaker"), sheet.subjects.map { subject -> subject.semanticId.value })
        val region = sheet.regions.single()
        assertEquals("Power Distribution", region.name)
        assertEquals(listOf("Supply", "Breaker"), region.occurrenceNames)
    }

    @Test
    fun `occurrence without engineering source fails with a named diagnostic`() {
        val success = compile(
            """
            system Demo {
              device Supply { }
              view schematic {
                sheet S1
                region "Bad" { occurrences [Ghost] }
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "without engineering source")
    }

    @Test
    fun `duplicate occurrence identity fails with a named diagnostic`() {
        val success = compile(
            """
            system Demo {
              device Supply { }
              view schematic {
                sheet S1
                region "Dupe" { occurrences [Supply, Supply] }
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "duplicate occurrence")
    }

    @Test
    fun `empty region fails with a plain diagnostic`() {
        val success = compile(
            """
            system Demo {
              device Supply { }
              view schematic {
                sheet S1
                region "Empty" { occurrences [] }
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "empty region")
    }

    @Test
    fun `region before any sheet fails with a plain diagnostic`() {
        val success = compile(
            """
            system Demo {
              device Supply { }
              view schematic {
                region "Early" { occurrences [Supply] }
                sheet S1
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "before any sheet")
    }

    @Test
    fun `sheet with no occurrences fails with a plain diagnostic`() {
        val success = compile(
            """
            system Demo {
              device Supply { }
              view schematic {
                sheet S1
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "no occurrences")
    }

    private fun compile(source: String): CompilerCompilationSuccess {
        val path = Files.createTempFile("athena-m40-region", ".athena")
        return assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path, source))
    }
}
