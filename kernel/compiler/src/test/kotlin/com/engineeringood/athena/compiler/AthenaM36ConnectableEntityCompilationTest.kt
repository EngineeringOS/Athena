package com.engineeringood.athena.compiler

import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertIs

class AthenaM36ConnectableEntityCompilationTest {
    @Test
    fun `reports a typed source diagnostic for an admitted connectable port without direction`() {
        val path = Files.createTempFile("athena-m36-connectable-", ".athena")
        path.writeText(
            """
            system M36Connectable {
              device Drive {
                type Switch
                connectable enabled
                interface power_input
              }

              port Drive.L1 {
                signal Digital
                role line
              }
            }
            """.trimIndent(),
        )

        try {
            val result = AthenaCompiler().compile(path)

            val success = assertIs<CompilerCompilationSuccess>(result)
            assertContains(
                success.semanticResult.diagnostics.map { it.ruleId.value },
                "connectable.port.direction.missing",
                message = "components=${success.document.components}; diagnostics=${success.semanticResult.diagnostics}",
            )
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `reports both typed port spans for an incompatible admitted connection`() {
        val path = Files.createTempFile("athena-m36-connection-", ".athena")
        path.writeText(
            """
            system M36Connection {
              device Source { type Switch connectable enabled }
              device Target { type Switch connectable enabled }
              port Source.out { direction in signal Digital role control }
              port Target.in { direction in signal Digital role control }
              connect invalid Source.out -> Target.in
            }
            """.trimIndent(),
        )

        try {
            val result = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path))
            val diagnostics = result.semanticResult.diagnostics.filter {
                it.ruleId.value == "connectable.connection.direction.incompatible"
            }
            assertContains(diagnostics.map { it.category.name }, "CONNECTION")
            assertContains(diagnostics.map { it.provenance.startLine }, 4)
            assertContains(diagnostics.map { it.provenance.startLine }, 5)
        } finally {
            path.deleteIfExists()
        }
    }
}
