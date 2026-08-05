package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaEngineeringAnatomySyntaxTest {
    @Test
    fun `parses entity exact values structure functions and exact port owners`() {
        val source =
            """
            system Conveyor {
              entity M1 {
                concept motor
                ratedPower 7.5 [unit.kilowatt]
                poleCount 4
                enabled true
                label "Drive motor"
                mode main
                source @Supply.main
                structure functional "Drive" display "="
                port shaft {
                  direction out
                  flow mechanicalPower
                  minimum 0
                  maximum unbounded
                }
                function electricalInput {
                  role input
                  port supply {
                    direction in
                    flow electricalPower
                    minimum 1
                    maximum 1
                    designationType terminal
                    designation "U1"
                  }
                }
              }
            }
            """.trimIndent()

        val success = assertIs<ParseSuccess>(AthenaLanguageParser().parse("src/conveyor.athena", source))
        val entity = assertIs<EntityDeclaration>(success.ast.declarations.single())
        assertEquals("M1", entity.name)
        assertIs<ScalarValue.Symbol>(entity.fields.single { it.name == "concept" }.value)
        val quantity = assertIs<ScalarValue.Quantity>(entity.fields.single { it.name == "ratedPower" }.value)
        assertEquals("7.5", quantity.exactText)
        assertEquals(listOf("unit", "kilowatt"), quantity.unit.parts)
        assertEquals(
            SourceSpan(
                start = SourcePosition(offset = 65, line = 4, column = 16),
                end = SourcePosition(offset = 84, line = 4, column = 35),
            ),
            quantity.span,
        )
        assertEquals("4", assertIs<ScalarValue.Integer>(entity.fields.single { it.name == "poleCount" }.value).exactText)
        assertEquals(true, assertIs<ScalarValue.Boolean>(entity.fields.single { it.name == "enabled" }.value).value)
        assertIs<ScalarValue.Text>(entity.fields.single { it.name == "label" }.value)
        assertIs<ScalarValue.Symbol>(entity.fields.single { it.name == "mode" }.value)
        assertEquals(
            listOf("Supply", "main"),
            assertIs<ScalarValue.Reference>(entity.fields.single { it.name == "source" }.value).target.parts,
        )
        assertEquals(listOf("functional"), entity.structureAssignments.single().aspect.parts)
        assertEquals("=", entity.structureAssignments.single().displayDesignation)
        assertEquals(listOf("M1", "shaft"), entity.nestedPorts.single().qualifiedName.parts)
        val function = entity.nestedFunctions.single()
        assertEquals(listOf("input"), function.role.parts)
        assertEquals(listOf("M1", "electricalInput", "supply"), function.nestedPorts.single().qualifiedName.parts)
        assertEquals(SourcePosition(offset = 389, line = 19, column = 7), function.nestedPorts.single().span.start)
        assertTrue(quantity.span.end.offset < function.nestedPorts.single().span.start.offset)
    }

    @Test
    fun `rejects retired device function port references and untyped decimal values`() {
        val parser = AthenaLanguageParser()
        assertIs<ParseFailure>(parser.parse("legacy-device.athena", "system Demo { device M1 { type motor } }"))
        assertIs<ParseFailure>(
            parser.parse(
                "legacy-function-ports.athena",
                "system Demo { entity M1 { concept motor function main { role main ports (M1.input) } } }",
            ),
        )
        assertIs<ParseFailure>(
            parser.parse("untyped-decimal.athena", "system Demo { entity M1 { concept motor ratio 1.5 } }"),
        )
    }
}
