package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EngineeringNetSyntaxTest {
    @Test
    fun `net and scoped specification preserve authored meaning`() {
        val result = AthenaLanguageParser().parse(
            "net.athena",
            """
            system Demo {
              connection-spec project { conductorType copper }
              connection-spec signal Control24V { colorCode black }
              net StartCircuit signal {
                source PLC1.out
                sink KM1.coil
                pass X1.p1
                signal Control24V
                crossSection 0.75 [mm2]
              }
            }
            """.trimIndent(),
        )

        val declarations = assertIs<ParseSuccess>(result, (result as? ParseFailure)?.diagnostics?.joinToString { it.message }).ast.declarations
        val project = assertIs<ConnectionSpecificationDeclaration>(declarations[0])
        assertEquals(ConnectionSpecificationScope.PROJECT, project.scope)
        val signal = assertIs<ConnectionSpecificationDeclaration>(declarations[1])
        assertEquals(listOf("Control24V"), signal.subject?.parts)
        val net = assertIs<NetDeclaration>(declarations[2])
        assertEquals("StartCircuit", net.name)
        assertEquals(listOf(NetEndpointRole.SOURCE, NetEndpointRole.SINK, NetEndpointRole.PASS), net.endpoints.map { it.role })
        assertEquals(listOf("crossSection"), net.properties.map { it.name })
    }
}
