package com.engineeringood.athena.compiler

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ConnectionIrCompilerTest {
    @Test
    fun `valid net publishes canonical connection document and branch operator`() {
        val compilation = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(Path.of("src/net.athena"), source()))
        val result = ConnectionIrCompiler().compile(compilation.document, compilation.semanticResult)
        val success = assertIs<ConnectionIrCompilation.Success>(result)
        assertEquals("athena-connection-ir-c14n-v1", success.document.canonicalization)
        assertEquals(1, success.document.nets.size)
        assertTrue(success.document.topologyOperators.any { it.kind.name == "BRANCH" })
        assertEquals(success.document.digest, assertIs<ConnectionIrCompilation.Success>(
            ConnectionIrCompiler().compile(compilation.document, compilation.semanticResult),
        ).document.digest)
    }

    @Test
    fun `invalid semantic result publishes no replacement`() {
        val compilation = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(Path.of("src/net.athena"), source().replace("direction out", "direction in")))
        val result = ConnectionIrCompiler().compile(compilation.document, compilation.semanticResult)
        assertIs<ConnectionIrCompilation.Failure>(result)
    }

    private fun source() = """
        system Control {
          entity PLC1 { concept controller }
          entity KM1 { concept contactor }
          entity X1 { concept terminal }
          port PLC1.out { direction out flow control }
          port KM1.coil { direction in flow control }
          port X1.p1 { direction bidirectional flow control }
          net StartCircuit signal {
            source PLC1.out
            sink KM1.coil
            pass X1.p1
            crossSection 0.75 [mm2]
          }
        }
    """.trimIndent()
}
