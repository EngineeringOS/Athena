package com.engineeringood.athena.compiler

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ReadingOrderProjectionSelectionTest {

    @Test
    fun `reading order reassigns sheet order deterministically`() {
        val source =
            """
            system Demo {
              device Supply { }
              device Breaker { }
              view schematic {
                sheet S1
                region "R1" { occurrences [Supply] }
                sheet S2
                region "R2" { occurrences [Breaker] }
                reading-order [S2, S1]
              }
            }
            """.trimIndent()

        val first = compile(source)
        val second = compile(source)
        assertTrue(first.authoredProjectionDiagnostics.isEmpty())
        val orders = { result: CompilerCompilationSuccess ->
            result.authoredProjectionViews.single().sheets.sortedBy { sheet -> sheet.sheetId.value }
                .map { sheet -> sheet.sheetId.value.substringAfterLast("/") to sheet.order }
        }
        assertEquals(listOf("S1" to 2, "S2" to 1), orders(first))
        assertEquals(orders(first), orders(second))
    }

    @Test
    fun `duplicate sheet in reading order fails with a plain diagnostic`() {
        val success = compile(
            """
            system Demo {
              device Supply { }
              view schematic {
                sheet S1
                region "R1" { occurrences [Supply] }
                reading-order [S1, S1]
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "more than once")
    }

    @Test
    fun `unknown sheet in reading order fails with a plain diagnostic`() {
        val success = compile(
            """
            system Demo {
              device Supply { }
              view schematic {
                sheet S1
                region "R1" { occurrences [Supply] }
                reading-order [S9]
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "unknown sheet")
    }

    @Test
    fun `reading order that is not a permutation fails with a plain diagnostic`() {
        val success = compile(
            """
            system Demo {
              device Supply { }
              view schematic {
                sheet S1
                sheet S2
                region "R1" { occurrences [Supply] }
                reading-order [S1]
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "permutation")
    }

    @Test
    fun `view plus projection policy fails closed as a competing selection surface`() {
        val success = compile(
            """
            system Demo {
              device Supply { }
              projection OldPolicy {
                target connection-drawing
              }
              view schematic {
                sheet S1
                region "R1" { occurrences [Supply] }
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "sole M40 projection selection surface")
    }

    private fun compile(source: String): CompilerCompilationSuccess {
        val path = Files.createTempFile("athena-m40-reading-order", ".athena")
        return assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path, source))
    }
}
