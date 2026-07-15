package com.engineeringood.athena.language

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaLanguageParserTest {
    @Test
    fun `parses the demo cabinet example into a syntax-only ast`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val source = Files.readString(examplePath)

        val result = AthenaLanguageParser().parse(examplePath.toString(), source)

        val success = assertIs<ParseSuccess>(result)
        val deviceOne = success.ast.declarations[0] as DeviceDeclaration
        val deviceTwo = success.ast.declarations[1] as DeviceDeclaration
        val portOne = success.ast.declarations[2] as PortDeclaration
        val portTwo = success.ast.declarations[3] as PortDeclaration
        val connection = success.ast.declarations[4] as ConnectionDeclaration
        assertEquals(
            SourceFileAst(
                system = SystemDeclaration("DemoCabinet", success.ast.system.span),
                declarations = listOf(
                    DeviceDeclaration(
                        name = "PLC1",
                        fields = listOf(
                            PropertyAssignment("type", ScalarValue.Identifier("Switch", deviceOne.fields[0].value.span), deviceOne.fields[0].span),
                            PropertyAssignment("model", ScalarValue.StringLiteral("S7-1200", deviceOne.fields[1].value.span), deviceOne.fields[1].span),
                        ),
                        span = deviceOne.span,
                    ),
                    DeviceDeclaration(
                        name = "M1",
                        fields = listOf(
                            PropertyAssignment("type", ScalarValue.Identifier("Motor", deviceTwo.fields[0].value.span), deviceTwo.fields[0].span),
                        ),
                        span = deviceTwo.span,
                    ),
                    PortDeclaration(
                        qualifiedName = QualifiedName(listOf("PLC1", "out"), portOne.qualifiedName.span),
                        fields = listOf(
                            PropertyAssignment("direction", ScalarValue.Identifier("out", portOne.fields[0].value.span), portOne.fields[0].span),
                            PropertyAssignment("signal", ScalarValue.Identifier("Digital", portOne.fields[1].value.span), portOne.fields[1].span),
                        ),
                        span = portOne.span,
                    ),
                    PortDeclaration(
                        qualifiedName = QualifiedName(listOf("M1", "in"), portTwo.qualifiedName.span),
                        fields = listOf(
                            PropertyAssignment("direction", ScalarValue.Identifier("in", portTwo.fields[0].value.span), portTwo.fields[0].span),
                            PropertyAssignment("signal", ScalarValue.Identifier("Digital", portTwo.fields[1].value.span), portTwo.fields[1].span),
                        ),
                        span = portTwo.span,
                    ),
                    ConnectionDeclaration(
                        from = QualifiedName(listOf("PLC1", "out"), connection.from.span),
                        to = QualifiedName(listOf("M1", "in"), connection.to.span),
                        span = connection.span,
                    ),
                ),
                span = success.ast.span,
            ),
            success.ast,
        )
    }

    @Test
    fun `parses deterministically for identical source input`() {
        val source = """
            system DemoCabinet {
              device PLC1 {
                type PLC
                model "S7-1200"
              }
            
              port PLC1.out {
                direction out
                signal Digital
              }
            
              connect PLC1.out -> PLC1.out
            }
        """.trimIndent()

        val parser = AthenaLanguageParser()

        val first = parser.parse("demo.athena", source)
        val second = parser.parse("demo.athena", source)

        assertEquals(first, second)
    }

    @Test
    fun `reports syntax diagnostics with file line and column provenance`() {
        val source = """
            system DemoCabinet {
              device PLC1 {
                type PLC
              }
            
              connect PLC1.out M1.in
            }
        """.trimIndent()

        val result = AthenaLanguageParser().parse("broken.athena", source)

        val failure = assertIs<ParseFailure>(result)
        assertEquals(1, failure.diagnostics.size)
        assertEquals("broken.athena", failure.diagnostics.single().file)
        assertEquals(6, failure.diagnostics.single().line)
        assertTrue(failure.diagnostics.single().column > 0)
        assertTrue(failure.diagnostics.single().message.contains("->"))
    }

    @Test
    fun `reports a typed diagnostic for an unterminated string literal without crashing`() {
        val source = """
            system DemoCabinet {
              device PLC1 {
                model "S7-1200
              }
            }
        """.trimIndent()

        val result = AthenaLanguageParser().parse("unterminated-string.athena", source)

        val failure = assertIs<ParseFailure>(result)
        assertTrue(failure.diagnostics.isNotEmpty())
        val diagnostic = failure.diagnostics.first()
        assertEquals("unterminated-string.athena", diagnostic.file)
        assertTrue(diagnostic.line > 0, "Expected a real line, got ${diagnostic.line}")
        assertTrue(diagnostic.column > 0, "Expected a real column, got ${diagnostic.column}")
        assertTrue(diagnostic.message.isNotBlank())
    }

    @Test
    fun `reports a typed diagnostic for a missing closing brace without crashing`() {
        val source = """
            system DemoCabinet {
              device PLC1 {
                type Switch
        """.trimIndent()

        val result = AthenaLanguageParser().parse("missing-brace.athena", source)

        val failure = assertIs<ParseFailure>(result)
        assertTrue(failure.diagnostics.isNotEmpty())
        val diagnostic = failure.diagnostics.first()
        assertEquals("missing-brace.athena", diagnostic.file)
        assertTrue(diagnostic.line > 0, "Expected a real line, got ${diagnostic.line}")
        assertTrue(diagnostic.column > 0, "Expected a real column, got ${diagnostic.column}")
        assertTrue(diagnostic.message.isNotBlank())
    }

    @Test
    fun `reports failures deterministically for identical malformed source input`() {
        val source = """
            system DemoCabinet {
              connect PLC1.out M1.in
            }
        """.trimIndent()

        val parser = AthenaLanguageParser()

        val first = parser.parse("broken.athena", source)
        val second = parser.parse("broken.athena", source)

        assertIs<ParseFailure>(first)
        assertEquals(first, second)
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root from ${Path.of("").toAbsolutePath().name}")
        return current
    }
}
