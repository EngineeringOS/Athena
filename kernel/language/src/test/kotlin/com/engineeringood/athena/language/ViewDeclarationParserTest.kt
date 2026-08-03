package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ViewDeclarationParserTest {

    @Test
    fun `parses authored view with sheets and grid reference system`() {
        val source =
            """
            system ViewDemo {
              device D { }
              view schematic {
                sheet S1
                sheet S2
                grid G1 { rows 3 columns 4 }
                region "Power Distribution" { occurrences [Supply, Breaker] }
                reading-order [S2, S1]
                power-rail L1 [Supply.L1, Breaker.line]
                rung R1 [Supply.L1]
                branch [Supply.L1, Drive.L]
                wire-bundle W1 [L1, N]
                terminal-strip X1 [Drive.L]
                contact-group C1 [K13, K14]
                coil-group K1 [A1, A2]
              }
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("view-declaration.athena", source)

        val success = assertIs<ParseSuccess>(result)
        val view = assertIs<ViewDeclaration>(success.ast.declarations.filterIsInstance<ViewDeclaration>().single())
        assertEquals("schematic", view.name)
        assertEquals(listOf("S1", "S2"), view.sheets.map { sheet -> sheet.name })
        val grid = assertNotNull(view.grid)
        assertEquals("G1", grid.name)
        assertEquals(3, grid.rows)
        assertEquals(4, grid.columns)
        assertEquals(listOf("Power Distribution"), view.regions.map { region -> region.name })
        assertEquals(listOf("Supply", "Breaker"), view.regions.single().occurrences)
        assertEquals(listOf("S2", "S1"), view.readingOrder)
        assertEquals(
            listOf("power-rail", "rung", "branch", "wire-bundle", "terminal-strip", "contact-group", "coil-group"),
            view.constructs.map { construct -> construct.kind },
        )
        assertEquals(listOf("Supply.L1", "Breaker.line"), view.constructs.first().occurrences)
        assertTrue(view.span.start.line < view.span.end.line)
    }

    @Test
    fun `parses view without grid`() {
        val source =
            """
            system ViewDemo {
              view plain {
                sheet A
              }
            }
            """.trimIndent()

        val success = assertIs<ParseSuccess>(AthenaLanguageParser().parse("view-no-grid.athena", source))
        val view = assertIs<ViewDeclaration>(success.ast.declarations.filterIsInstance<ViewDeclaration>().single())
        assertNull(view.grid)
        assertEquals(1, view.sheets.size)
    }
}
