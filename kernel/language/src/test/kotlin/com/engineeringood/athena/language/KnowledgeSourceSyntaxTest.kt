package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class KnowledgeSourceSyntaxTest {
    @Test
    fun `parses package domain and typed knowledge declarations through shared parser`() {
        val source = """
            package com.example.conveyor
            domain electrical {
              concept motor { kind rotating }
              part contactorA concept contactor { ratedCurrent 20 [unit.ampere] }
              capability switchedPower provides { medium electricalPower }
              relationship supplies {
                role provider level port
                role consumer level port
                connectivity true
              }
              flow electricalPower relationship supplies source provider sink consumer medium electricalPower
              dimension current bases [electrical.current]
              unit ampere dimension electrical.current scale 1
              formula protectedCurrent = round-up(ratedCurrent, 5)
              constraint rating protectedCurrent >= ratedCurrent
            }
        """.trimIndent()

        val success = assertIs<ParseSuccess>(AthenaLanguageParser().parse("packages/electrical.athena", source))
        assertEquals("electrical", success.ast.knowledgeDomain)
        val declarations = success.ast.knowledgeDeclarations
        assertIs<KnowledgeConceptDeclaration>(declarations[0])
        val part = assertIs<KnowledgePartDeclaration>(declarations[1])
        assertEquals(listOf("contactor"), part.concept.parts)
        assertIs<KnowledgeRelationshipDeclaration>(declarations[3])
        assertIs<KnowledgeFlowDeclaration>(declarations[4])
        assertIs<KnowledgeFormulaDeclaration>(declarations[7])
        assertIs<KnowledgeConstraintDeclaration>(declarations[8])
    }

    @Test
    fun `rejects arbitrary script syntax and legacy properties as knowledge source`() {
        val parser = AthenaLanguageParser()
        assertIs<ParseFailure>(parser.parse("script.athena", "domain electrical { formula x = while (true) {} }"))
        assertIs<ParseFailure>(parser.parse("legacy.properties", "domain electrical { component motor {} }"))
    }
}
