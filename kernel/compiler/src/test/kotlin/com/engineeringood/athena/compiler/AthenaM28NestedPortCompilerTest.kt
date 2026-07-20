package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.StableSemanticIdentity
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AthenaM28NestedPortCompilerTest {
    @Test
    fun `nested device-owned ports lower to canonical owner dot port identity`() {
        val source =
            """
            system M28NestedPortProof {
              device SpareTerminalXT99 {
                type Switch
                model "SPARE-XT"

                port in1 {
                  direction in
                  signal Digital
                }
              }
            }
            """.trimIndent()

        val success = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(Path.of("m28-nested-port.athena"), source),
        )

        val port = success.document.ports.single()
        assertEquals(StableSemanticIdentity("port:SpareTerminalXT99.in1"), port.id)
        assertEquals(listOf("SpareTerminalXT99"), port.ownerReference.authoredPath)
        assertEquals("in1", port.name)
        assertEquals("m28-nested-port.athena", port.provenance.file)
        assertEquals(6, port.provenance.startLine)
    }
}
