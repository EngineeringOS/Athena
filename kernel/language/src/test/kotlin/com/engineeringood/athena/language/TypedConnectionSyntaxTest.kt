package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypedConnectionSyntaxTest {
    @Test
    fun `connect wire phrase is explicit connection declaration`() {
        val result = AthenaLanguageParser().parse(
            "typed-connection.athena",
            """
            system Demo {
              connect wire PLC1.out to KM1.power
            }
            """.trimIndent(),
        )
        val success = assertIs<ParseSuccess>(result)
        val declaration = assertIs<ConnectionDeclaration>(success.ast.declarations.single())
        assertEquals("wire", declaration.kind.value)
        assertEquals(listOf("PLC1", "out"), declaration.source.parts)
        assertEquals(listOf("KM1", "power"), declaration.target.parts)
        assertTrue(declaration.properties.isEmpty())
    }

    @Test
    fun `connection property block preserves authored typed values`() {
        val declaration = parseConnection(
            """
            connect wire PLC1.out to KM1.coil {
              colorCode black
              core 3
            }
            """.trimIndent(),
        )

        assertEquals(listOf("colorCode", "core"), declaration.properties.map { it.name })
        assertEquals("black", assertIs<ScalarValue.Symbol>(declaration.properties[0].value).text)
        assertEquals("3", assertIs<ScalarValue.Integer>(declaration.properties[1].value).exactText)
    }

    @Test
    fun `arrow and to separators preserve equal connection meaning`() {
        val withTo = parseConnection("connect signal PLC1.out to KM1.coil")
        val withArrow = parseConnection("connect signal PLC1.out -> KM1.coil")

        assertEquals(withTo.kind.value, withArrow.kind.value)
        assertEquals(withTo.source.parts, withArrow.source.parts)
        assertEquals(withTo.target.parts, withArrow.target.parts)
    }

    @Test
    fun `unsupported connection kind fails with correction`() {
        val result = AthenaLanguageParser().parse(
            "unsupported-connection.athena",
            "system Demo { connect magic PLC1.out to KM1.coil }",
        )

        val failure = assertIs<ParseFailure>(result)
        assertTrue(failure.diagnostics.single().message.contains("Connection kind 'magic' is not supported"))
        assertTrue(failure.diagnostics.single().message.contains("wire"))
    }

    @Test
    fun `repeated endpoint fails with exact correction`() {
        val result = AthenaLanguageParser().parse(
            "self-connection.athena",
            "system Demo { connect wire PLC1.out to PLC1.out }",
        )

        val failure = assertIs<ParseFailure>(result)
        assertTrue(failure.diagnostics.single().message.contains("PLC1.out"))
        assertTrue(failure.diagnostics.single().message.contains("two distinct Engineering Ports"))
    }

    @Test
    fun `malformed connection fails with human correction`() {
        val result = AthenaLanguageParser().parse(
            "malformed-connection.athena",
            "system Demo {\n  connect wire PLC1.out to\n}",
        )

        val failure = assertIs<ParseFailure>(result)
        assertTrue(failure.diagnostics.any { diagnostic ->
            diagnostic.message.contains("Connection syntax is incomplete") &&
                diagnostic.message.contains("connect <kind> <source-port> to <target-port>")
        }, failure.diagnostics.joinToString { it.message })
    }

    private fun parseConnection(phrase: String): ConnectionDeclaration {
        val result = AthenaLanguageParser().parse("connection.athena", "system Demo { $phrase }")
        return assertIs<ConnectionDeclaration>(assertIs<ParseSuccess>(result).ast.declarations.single())
    }
}
