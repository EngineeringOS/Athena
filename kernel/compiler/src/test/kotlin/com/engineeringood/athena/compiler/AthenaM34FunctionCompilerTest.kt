package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringFunctionRole
import com.engineeringood.athena.ir.StableSemanticIdentity
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AthenaM34FunctionCompilerTest {
    @Test
    fun `nested function lowers through domain blueprints without duplicating port truth`() {
        val source =
            """
            system FunctionLowering {
              device KM1 {
                type Switch
                port A1 {
                  direction in
                  signal Control
                  terminal "A1"
                }
                port A2 {
                  direction out
                  signal Control
                  terminal "A2"
                }
                function coil {
                  role coil
                  ports (A1, KM1.A2)
                }
              }
            }
            """.trimIndent()

        val success = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(Path.of("m34-function.athena"), source),
        )
        val function = success.document.functions.single()

        assertEquals(StableSemanticIdentity("function:KM1.coil"), function.id)
        assertEquals(StableSemanticIdentity("component:KM1"), function.ownerReference.resolvedIdentity)
        assertEquals(EngineeringFunctionRole("coil"), function.role)
        assertEquals(
            listOf(StableSemanticIdentity("port:KM1.A1"), StableSemanticIdentity("port:KM1.A2")),
            function.portReferences.map { reference -> reference.resolvedIdentity },
        )
        assertEquals(14, function.provenance.startLine)
        assertEquals(emptyList(), success.semanticResult.diagnostics)
    }
}
