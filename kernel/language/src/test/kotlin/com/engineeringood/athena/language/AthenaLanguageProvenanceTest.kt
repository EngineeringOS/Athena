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

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root")
        return current
    }
}
