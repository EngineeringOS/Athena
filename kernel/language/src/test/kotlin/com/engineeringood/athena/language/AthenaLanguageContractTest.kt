package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaLanguageContractTest {
    @Test
    fun `valid source returns only ParseSuccess carrying Athena-owned SourceFileAst`() {
        val source =
            """
            system Demo {
              device PLC1 {
                type PLC
              }
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("contract-valid.athena", source)

        val success = assertIs<ParseSuccess>(result)
        assertIs<SourceFileAst>(success.ast)
        assertEquals("Demo", success.ast.system.name)
        assertTrue(success.ast.declarations.single() is DeviceDeclaration)
    }

    @Test
    fun `invalid source returns only ParseFailure with Athena-owned SyntaxDiagnostic fields`() {
        val source =
            """
            system Demo {
              device
            }
            """.trimIndent()

        val result = AthenaLanguageParser().parse("contract-invalid.athena", source)

        val failure = assertIs<ParseFailure>(result)
        val diagnostic = failure.diagnostics.single()
        assertEquals("contract-invalid.athena", diagnostic.file)
        assertTrue(diagnostic.line >= 1)
        assertTrue(diagnostic.column >= 1)
        assertTrue(diagnostic.message.isNotBlank())
        assertIs<SourceSpan>(diagnostic.span)
        assertIs<SourcePosition>(diagnostic.span.start)
        assertIs<SourcePosition>(diagnostic.span.end)
    }

    @Test
    fun `SyntaxDiagnostic exposes only provenance fields and never parser-internal carriers`() {
        val diagnosticPropertyNames =
            SyntaxDiagnostic::class.java.declaredFields
                .filter { field -> !field.isSynthetic }
                .map { field -> field.name }
                .toSet()

        assertEquals(
            setOf("file", "line", "column", "message", "span"),
            diagnosticPropertyNames,
        )
        assertTrue("token" !in diagnosticPropertyNames)
        assertTrue("tokens" !in diagnosticPropertyNames)
        assertTrue("kind" !in diagnosticPropertyNames)
    }
}
