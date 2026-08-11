package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SheetCompanionEditorTest {
    private val parser = AthenaSheetCompanionParser()
    private val editor = SheetCompanionEditor()

    @Test
    fun `updates existing points and inserts missing points with natural syntax`() {
        val source = """
            sheet "rolling-shutter" {
              page format A3 landscape
              frame: 17 * 16
              snap: 1
              title "Equipment d'un volet roulant"
              "Motor" at (32, 16) lock
              "Supply" at (4, 8)
            }
        """.trimIndent() + "\n"

        val result = editor.writePlacements(
            existingSource = source,
            placements = listOf(
                SheetPlacementWrite("Supply", SheetPoint(12, 20), lockIntent = SheetPlacementLockIntent.LOCK),
                SheetPlacementWrite("KM1", SheetPoint(20, 28)),
            ),
        )

        assertTrue(result.changed)
        assertEquals(listOf("KM1", "Supply"), result.changedOccurrences)
        assertEquals(
            """
                sheet "rolling-shutter" {
                  page format A3 landscape
                  frame: 17 * 16
                  snap: 1
                  title "Equipment d'un volet roulant"
                  "KM1" at (20, 28)
                  "Motor" at (32, 16) lock
                  "Supply" at (12, 20) lock
                }
            """.trimIndent() + "\n",
            result.updatedSource,
        )
        assertIs<SheetCompanionParseSuccess>(parser.parse("rolling-shutter.sheet.athena", result.updatedSource))
    }

    @Test
    fun `applies preserve lock unlock and no-op detection deterministically`() {
        val source = """
            sheet demo {
              page format A4 portrait
              frame: 4 * 4
              snap: 1
              "Q1" at (1, 1) lock
              "M1" at (2, 2)
            }
        """.trimIndent() + "\n"

        val preserved = editor.writePlacements(
            source,
            listOf(SheetPlacementWrite("Q1", SheetPoint(4, 4))),
        )
        assertTrue(preserved.updatedSource.contains("\"Q1\" at (4, 4) lock"))

        val unlocked = editor.writePlacements(
            preserved.updatedSource,
            listOf(SheetPlacementWrite("Q1", SheetPoint(4, 4), lockIntent = SheetPlacementLockIntent.UNLOCK)),
        )
        assertTrue(unlocked.changed)
        assertTrue(unlocked.updatedSource.contains("\"Q1\" at (4, 4)\n"))
        assertFalse(unlocked.updatedSource.contains("\"Q1\" at (4, 4) lock"))

        val noOp = editor.writePlacements(
            unlocked.updatedSource,
            listOf(SheetPlacementWrite("Q1", SheetPoint(4, 4))),
        )
        assertFalse(noOp.changed)
        assertEquals(unlocked.updatedSource, noOp.updatedSource)
        assertEquals(emptyList(), noOp.changedOccurrences)
    }

    @Test
    fun `rejects non-positive point before writing`() {
        val source = """
            sheet demo {
              page format A4 portrait
              frame: 4 * 4
              snap: 1
            }
        """.trimIndent() + "\n"

        assertFailsWith<IllegalArgumentException> {
            editor.writePlacements(source, listOf(SheetPlacementWrite("Q1", SheetPoint(0, 1))))
        }.also {
            assertTrue(it.message.orEmpty().contains("Sheet point coordinates must be positive"))
        }
    }

    @Test
    fun `updates and inserts stable route waypoints without renderer coordinates`() {
        val source = """
            sheet demo {
              page format A4 portrait
              frame: 17 * 16
              snap: 1
              route "view/connection/C1" via "bend-a" at (8, 12)
            }
        """.trimIndent() + "\n"

        val result = editor.writeRouteConstraints(
            source,
            listOf(
                SheetRouteConstraintWrite("view/connection/C1", "bend-a", SheetPoint(16, 20)),
                SheetRouteConstraintWrite("view/connection/C1", "bend-b", SheetPoint(24, 20)),
            ),
        )

        assertTrue(result.changed)
        assertEquals(listOf("view/connection/C1/bend-a", "view/connection/C1/bend-b"), result.changedTargets)
        assertTrue(result.updatedSource.contains("route \"view/connection/C1\" via \"bend-a\" at (16, 20)"))
        assertTrue(result.updatedSource.contains("route \"view/connection/C1\" via \"bend-b\" at (24, 20)"))
        assertFalse(result.updatedSource.contains("px"))
        assertIs<SheetCompanionParseSuccess>(parser.parse("demo.sheet.athena", result.updatedSource))
    }
}
