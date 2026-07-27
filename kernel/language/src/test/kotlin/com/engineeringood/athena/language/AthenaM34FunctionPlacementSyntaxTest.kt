package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaM34FunctionPlacementSyntaxTest {
    @Test
    fun `parses nested engineering function and typed drawing grid placement with exact values`() {
        val source =
            """
            system FunctionPlacement {
              device KM1 {
                type Contactor
                port A1 {
                  direction in
                  signal Control
                  terminal "A1"
                }
                port A2 {
                  direction out
                  signal Control
                  terminal "A2"
                }
                function coil {
                  role coil
                  ports (A1, KM1.A2)
                }
              }
              layout schematic {
                place KM1.coil at (7, 4) orientation vertical
                place KM1 at (5, 2) orientation horizontal
              }
            }
            """.trimIndent()

        val success = assertIs<ParseSuccess>(AthenaLanguageParser().parse("function-placement.athena", source))
        val device = assertIs<DeviceDeclaration>(success.ast.declarations[0])
        val function = device.nestedFunctions.single()

        assertEquals("coil", function.name)
        assertEquals("coil", function.role.value)
        assertEquals(listOf(listOf("A1"), listOf("KM1", "A2")), function.portReferences.map { it.parts })
        assertTrue(function.span.start.offset < function.role.span.start.offset)
        assertTrue(function.portReferences.all { reference -> reference.span.start.offset in function.span.start.offset..function.span.end.offset })

        val layout = assertIs<LayoutDeclaration>(success.ast.declarations[1])
        val functionPlacement = assertIs<LayoutStatement.PlaceAt>(layout.statements[0])
        assertEquals(listOf("KM1", "coil"), functionPlacement.subject.parts)
        assertEquals(DrawingGridPosition(column = 7, row = 4, span = functionPlacement.position.span), functionPlacement.position)
        assertEquals(LayoutOrientation.Vertical, functionPlacement.orientation)
        val componentPlacement = assertIs<LayoutStatement.PlaceAt>(layout.statements[1])
        assertEquals(listOf("KM1"), componentPlacement.subject.parts)
        assertEquals(LayoutOrientation.Horizontal, componentPlacement.orientation)
        assertTrue(functionPlacement.span.start.line < componentPlacement.span.start.line)
    }

    @Test
    fun `rejects non-positive fractional and missing-orientation drawing grid placement`() {
        for ((name, placement) in listOf(
            "zero column" to "place KM1 at (0, 2) orientation horizontal",
            "negative row" to "place KM1 at (2, -1) orientation horizontal",
            "fractional column" to "place KM1 at (2.5, 1) orientation horizontal",
            "missing orientation" to "place KM1 at (2, 1)",
        )) {
            val result = AthenaLanguageParser().parse(
                "$name.athena",
                "system Demo { device KM1 {} layout schematic { $placement } }",
            )

            assertIs<ParseFailure>(result, name)
        }
    }
}
