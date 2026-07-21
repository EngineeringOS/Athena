package com.engineeringood.athena.language

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaLanguageProvenanceTest {
    @Test
    fun `system span covers the full system block`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val source = Files.readString(examplePath)

        val result = AthenaLanguageParser().parse(examplePath.toString(), source)

        val success = assertIs<ParseSuccess>(result)
        assertEquals(1, success.ast.system.span.start.line)
        assertEquals(1, success.ast.system.span.start.column)
        assertEquals(22, success.ast.system.span.end.line)
        assertEquals(2, success.ast.system.span.end.column)
        assertEquals(success.ast.span, success.ast.system.span)
    }

    @Test
    fun `preserves exact device and string-literal spans on the antlr path`() {
        val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val source = Files.readString(examplePath)

        val result = AthenaLanguageParser().parse(examplePath.toString(), source)

        val success = assertIs<ParseSuccess>(result)
        val firstDevice = success.ast.declarations[0] as DeviceDeclaration
        assertEquals("PLC1", firstDevice.name)
        // `device` keyword through the device's closing brace (half-open, 1-based columns).
        assertEquals(2, firstDevice.span.start.line)
        assertEquals(3, firstDevice.span.start.column)
        assertEquals(5, firstDevice.span.end.line)
        assertEquals(4, firstDevice.span.end.column)

        val modelValue = firstDevice.fields[1].value as ScalarValue.StringLiteral
        assertEquals("S7-1200", modelValue.text)
        // The span covers the surrounding quotes; `end` points immediately after the closing quote.
        assertEquals(4, modelValue.span.start.line)
        assertEquals(11, modelValue.span.start.column)
        assertEquals(4, modelValue.span.end.line)
        assertEquals(20, modelValue.span.end.column)
    }

    @Test
    fun `rejects over-qualified port declarations`() {
        val source = """
            system InvalidQualifiedPort {
              port Cabinet.PLC1.out {
                direction out
              }
            }
        """.trimIndent()

        val result = AthenaLanguageParser().parse("invalid-port-qualified.athena", source)

        val failure = assertIs<ParseFailure>(result)
        assertTrue(failure.diagnostics.single().message.contains("owner.port"))
    }

    @Test
    fun `requires qualified connection endpoints independently of port parsing`() {
        val source = """
            system InvalidConnectionQualifiedNames {
              port PLC1.out {
                direction out
              }

              connect PLC1.out -> M1
            }
        """.trimIndent()

        val result = AthenaLanguageParser().parse("invalid-connect-qualified.athena", source)

        val failure = assertIs<ParseFailure>(result)
        assertTrue(failure.diagnostics.single().message.contains("owner.port"))
    }

    @Test
    fun `requires qualified grouped connection endpoints independently of group parsing`() {
        val source = """
            system InvalidGroupedConnectionQualifiedNames {
              connect control_feed {
                PLC1.out -> M1
              }
            }
        """.trimIndent()

        val result = AthenaLanguageParser().parse("invalid-grouped-connect-qualified.athena", source)

        val failure = assertIs<ParseFailure>(result)
        assertTrue(failure.diagnostics.single().message.contains("owner.port"))
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root")
        return current
    }
}
