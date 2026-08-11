package com.engineeringood.athena.language

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SheetCompanionLanguageTest {
    private val parser = AthenaSheetCompanionParser()
    private val styleParser = AthenaSheetStyleCompanionParser()

    @Test
    fun `parses page frame snap title and canonical occurrence points`() {
        val result = parser.parse(
            "rolling-shutter.sheet.athena",
            """
                sheet "rolling-shutter" {
                  page format A3 landscape
                  frame: 17 * 16
                  snap: 1
                  title "Equipment d'un volet roulant"
                  "Q1" at (24, 16) lock
                  KM1 at (40, 32)
                  route "view:main/connection/connection:Q1.out-to-KM1.coil" via "bend-control" at (32, 24)
                }
            """.trimIndent(),
        )

        val source = assertIs<SheetCompanionParseSuccess>(result).source
        assertEquals("rolling-shutter", source.name)
        assertEquals("A3", source.page.format)
        assertEquals(SheetOrientation.LANDSCAPE, source.page.orientation)
        assertEquals(17, source.frame.columns)
        assertEquals(16, source.frame.rows)
        assertEquals(1, source.snap.step)
        assertEquals("Equipment d'un volet roulant", source.title?.text)
        assertEquals(2, source.placements.size)
        assertEquals("Q1", source.placements[0].occurrence)
        assertEquals(SheetPoint(24, 16, source.placements[0].span), source.placements[0].point)
        assertTrue(source.placements[0].locked)
        assertEquals(SheetPoint(40, 32, source.placements[1].span), source.placements[1].point)
        val route = source.routeConstraints.single()
        assertEquals("view:main/connection/connection:Q1.out-to-KM1.coil", route.projectionId)
        assertEquals("bend-control", route.targetId)
        assertEquals(SheetPoint(32, 24, route.span), route.point)
    }

    @Test
    fun `rejects duplicate route targets and raw viewport coordinates`() {
        val result = parser.parse(
            "broken.sheet.athena",
            """
                sheet broken {
                  page format A3 landscape
                  frame: 17 * 16
                  snap: 1
                  route "view/connection/C1" via "bend-main" at (24, 16)
                  route "view/connection/C1" via "bend-main" at (32, 16)
                  route "view/connection/C1" viewport (120px, 80px)
                }
            """.trimIndent(),
        )

        val failure = assertIs<SheetCompanionParseFailure>(result)
        assertTrue(failure.diagnostics.any { it.message.contains("route target `bend-main`") })
        assertTrue(failure.diagnostics.any { it.message.startsWith("Unknown Sheet Companion statement") })
    }

    @Test
    fun `rejects retired cell and micro syntax`() {
        val result = parser.parse(
            "sheet.sheet.athena",
            "sheet demo {\npage format A4 portrait\ngrid: 4 * 4 cell: 8\n\"Supply\" at A2 micro(8,8)\n}",
        )
        val failure = assertIs<SheetCompanionParseFailure>(result)
        assertTrue(failure.diagnostics.any { it.message.startsWith("Unknown Sheet Companion statement: `grid:") })
        assertTrue(failure.diagnostics.any { it.message.startsWith("Unknown Sheet Companion statement: `\"Supply\" at A2") })
    }

    @Test
    fun `rejects missing frame snap and invalid point declarations`() {
        val result = parser.parse(
            "broken.sheet.athena",
            "sheet broken {\n\"Supply\" at (0, 2)\n}",
        )
        val failure = assertIs<SheetCompanionParseFailure>(result)
        assertTrue(failure.diagnostics.any { it.message.contains("page declaration") })
        assertTrue(failure.diagnostics.any { it.message.contains("frame declaration") })
        assertTrue(failure.diagnostics.any { it.message.contains("snap declaration") })
        assertTrue(failure.diagnostics.any { it.message.contains("unknown Sheet point") })
    }

    @Test
    fun `rejects unknown statements deterministically`() {
        val result = parser.parse(
            "broken.sheet.athena",
            "sheet broken {\npage format A3 landscape\nframe: 4 * 4\nsnap: 1\ncoordinates 1 2\n}",
        )
        val failure = assertIs<SheetCompanionParseFailure>(result)
        assertEquals("Unknown Sheet Companion statement: `coordinates 1 2`.", failure.diagnostics.single().message)
    }

    @Test
    fun `rejects duplicate and non-positive placements`() {
        val result = parser.parse(
            "broken.sheet.athena",
            "sheet broken {\npage format A3 landscape\nframe: 4 * 4\nsnap: 1\nQ1 at (0,5)\nQ1 at (1,1)\n}",
        )
        val failure = assertIs<SheetCompanionParseFailure>(result)
        assertTrue(failure.diagnostics.any { it.message.contains("unknown Sheet point") })
        assertTrue(failure.diagnostics.any { it.message.contains("more than one authored placement") })
    }

    @Test
    fun `locates exact same basename companion and reports missing`() {
        val directory = Files.createTempDirectory("athena-sheet-locator")
        try {
            val source = directory.resolve("rolling-shutter.athena")
            Files.writeString(source, "system rolling_shutter { }")
            val missing = assertIs<SheetCompanionMissing>(SheetCompanionLocator.locate(source))
            assertEquals("rolling-shutter.sheet.athena", missing.expectedPath.fileName.toString())

            val companion = directory.resolve("rolling-shutter.sheet.athena")
            Files.writeString(companion, "sheet demo { }")
            assertEquals(companion, assertIs<SheetCompanionFound>(SheetCompanionLocator.locate(source)).path)
        } finally {
            Files.walk(directory).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists) }
        }
    }

    @Test
    fun `locates optional same basename style companion and ambiguity`() {
        val directory = Files.createTempDirectory("athena-sheet-style-locator")
        try {
            val sheet = directory.resolve("rolling-shutter.sheet.athena")
            Files.writeString(sheet, "sheet demo { }")
            val missing = assertIs<SheetCompanionMissing>(SheetStyleCompanionLocator.locate(sheet))
            assertEquals("rolling-shutter.sheet.style.athena", missing.expectedPath.fileName.toString())

            val style = directory.resolve("rolling-shutter.sheet.style.athena")
            Files.writeString(style, "style default { }")
            assertEquals(style, assertIs<SheetCompanionFound>(SheetStyleCompanionLocator.locate(sheet)).path)

            Files.delete(style)
            val casingVariant = directory.resolve("ROLLING-SHUTTER.SHEET.STYLE.ATHENA")
            Files.writeString(casingVariant, "style default { }")
            assertIs<SheetCompanionAmbiguous>(SheetStyleCompanionLocator.locate(sheet))

        } finally {
            Files.walk(directory).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists) }
        }
    }

    @Test
    fun `parses presentation only style companion`() {
        val result = styleParser.parse(
            "rolling-shutter.sheet.style.athena",
            """
                style default {
                  stroke: #20252bff
                  fill: #ffffffff
                  width: 2
                  dash: [4, 2]
                  cap: round
                  join: bevel
                  opacity: 220
                  font-size: 3
                  font-weight: 600
                  route-marker: end-arrow
                  port-display: marker-and-label
                }
            """.trimIndent(),
        )

        val source = assertIs<SheetStyleCompanionParseSuccess>(result).source
        val style = source.styles.single()
        assertEquals("default", style.name)
        assertEquals("#20252bff", style.strokeRgba)
        assertEquals("#ffffffff", style.fillRgba)
        assertEquals(2, style.strokeWidth)
        assertEquals(listOf(4, 2), style.dash)
        assertEquals("round", style.lineCap)
        assertEquals("bevel", style.lineJoin)
        assertEquals(220, style.opacity)
        assertEquals(3, style.fontSize)
        assertEquals(600, style.fontWeight)
        assertEquals("end-arrow", style.routeMarker)
        assertEquals("marker-and-label", style.portDisplay)
    }

    @Test
    fun `creates and minimally patches deterministic style companion text`() {
        val editor = SheetStyleCompanionEditor()
        val created = editor.setStyle(
            existingSource = null,
            target = "route",
            fields = SheetStyleFields(strokeRgba = "#225588ff", strokeWidth = 3, routeMarker = "end-arrow"),
        )
        assertEquals(
            "style \"route\" {\n  stroke: #225588ff\n  width: 3\n  route-marker: end-arrow\n}\n",
            created,
        )
        assertIs<SheetStyleCompanionParseSuccess>(styleParser.parse("sheet.sheet.style.athena", created))

        val patched = editor.setStyle(
            existingSource = "style \"default\" {\n  fill: #ffffffff\n}\n\n$created",
            target = "route",
            fields = SheetStyleFields(strokeWidth = 5, dash = listOf(6, 2)),
        )
        assertTrue(patched.contains("style \"default\" {\n  fill: #ffffffff\n}"))
        assertTrue(patched.contains("stroke: #225588ff"))
        assertTrue(patched.contains("width: 5"))
        assertTrue(patched.contains("dash: [6, 2]"))
        assertEquals(patched, editor.setStyle(patched, "route", SheetStyleFields(strokeWidth = 5, dash = listOf(6, 2))))
    }

    @Test
    fun `rejects invalid route marker and port display`() {
        val result = styleParser.parse(
            "rolling-shutter.sheet.style.athena",
            "style connection {\nroute-marker: triangle\nport-display: always\n}",
        )
        val failure = assertIs<SheetStyleCompanionParseFailure>(result)
        assertTrue(failure.diagnostics.any { it.message.contains("route-marker") })
        assertTrue(failure.diagnostics.any { it.message.contains("port-display") })
    }

    @Test
    fun `style edit preserves explicit annotation intents`() {
        val editor = SheetStyleCompanionEditor()
        val source = "style route {\n  width: 1\n  annotation: Control24V kind\n}"

        val edited = editor.setStyle(source, "route", SheetStyleFields(strokeWidth = 2))

        assertTrue(edited.contains("width: 2"))
        assertTrue(edited.contains("annotation: Control24V kind"))
        val parsed = assertIs<SheetStyleCompanionParseSuccess>(styleParser.parse("sheet.sheet.style.athena", edited)).source
        assertEquals(listOf("Control24V"), parsed.styles.single().annotations.map { it.subjectName })
    }

    @Test
    fun `rejects unknown style companion fields`() {
        val result = styleParser.parse(
            "rolling-shutter.sheet.style.athena",
            "style default {\nrelationship: power\n}",
        )

        val failure = assertIs<SheetStyleCompanionParseFailure>(result)
        assertEquals("Unknown Style Companion field `relationship`.", failure.diagnostics.single().message)
    }
}
