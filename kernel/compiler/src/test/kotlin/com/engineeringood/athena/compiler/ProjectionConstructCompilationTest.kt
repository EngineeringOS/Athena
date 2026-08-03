package com.engineeringood.athena.compiler

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ProjectionConstructCompilationTest {

    @Test
    fun `authored constructs attach to sheets with members and kinds`() {
        val success = compile(
            """
            system Demo {
              device Supply { port L1 { direction out signal Power role line } }
              device Breaker { port line { direction in signal Power role line } }
              view schematic {
                sheet S1
                region "Power" { occurrences [Supply, Breaker] }
                power-rail L1 [Supply.L1, Breaker.line]
                rung R1 [Supply.L1]
              }
            }
            """.trimIndent(),
        )

        assertTrue(success.authoredProjectionDiagnostics.isEmpty())
        val sheet = success.authoredProjectionViews.single().sheets.single()
        assertEquals(
            listOf("power-rail", "rung"),
            sheet.constructs.map { construct -> construct.kind },
        )
        assertEquals(listOf("Supply", "Breaker"), sheet.constructs.first().memberNames)
        assertEquals("Demo", success.document.system.name)
    }

    @Test
    fun `empty construct fails with a plain diagnostic`() {
        val success = compile(
            """
            system Demo {
              device Supply { port L1 { direction out signal Power role line } }
              view schematic {
                sheet S1
                region "Power" { occurrences [Supply] }
                rung R1 []
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "empty rung")
    }

    @Test
    fun `duplicate construct identity fails with a plain diagnostic`() {
        val success = compile(
            """
            system Demo {
              device Supply { port L1 { direction out signal Power role line } }
              view schematic {
                sheet S1
                region "Power" { occurrences [Supply] }
                power-rail L1 [Supply.L1]
                power-rail L1 [Supply.L1]
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "duplicate construct")
    }

    @Test
    fun `construct member outside sheet occurrences fails with a plain diagnostic`() {
        val success = compile(
            """
            system Demo {
              device Supply { port L1 { direction out signal Power role line } }
              view schematic {
                sheet S1
                region "Power" { occurrences [Supply] }
                rung R1 [Ghost.L1]
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "not an occurrence")
    }

    @Test
    fun `construct containing another construct fails as invalid nesting`() {
        val success = compile(
            """
            system Demo {
              device Supply { port L1 { direction out signal Power role line } }
              view schematic {
                sheet S1
                region "Power" { occurrences [Supply] }
                power-rail L1 [Supply.L1]
                rung R1 [L1]
              }
            }
            """.trimIndent(),
        )

        assertContains(success.authoredProjectionDiagnostics.joinToString("\n"), "must not contain other constructs")
    }

    @Test
    fun `grouped endpoint integrity keeps connections identical with and without constructs`() {
        val base =
            """
            system Demo {
              device Supply { port L1 { direction out signal Power role line } }
              device Breaker { port line { direction in signal Power role line } }
              %SHEET_PART%
            }
            """.trimIndent()
        val withConstructs = base.replace(
            "%SHEET_PART%",
            """
              view schematic {
                sheet S1
                region "Power" { occurrences [Supply, Breaker] }
                power-rail L1 [Supply.L1, Breaker.line]
              }
            """.trimIndent(),
        )
        val withoutConstructs = base.replace("%SHEET_PART%", "")

        val connectionsWith = compile(withConstructs).document.connections
        val connectionsWithout = compile(withoutConstructs).document.connections
        assertEquals(connectionsWithout, connectionsWith)
        assertTrue(compile(withConstructs).authoredProjectionViews.single().sheets.single().constructs.isNotEmpty())
    }

    private fun compile(source: String): CompilerCompilationSuccess {
        val path = Files.createTempFile("athena-m40-construct", ".athena")
        return assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path, source))
    }
}
