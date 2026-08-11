package com.engineeringood.athena.compiler

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ConnectionProjectionCompilerTest {
    @Test
    fun `accepted connection identity survives two authored views`() {
        val compilation = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(Path.of("src/projection.athena"), source()),
        )

        assertTrue(compilation.projectionDiagnostics.isEmpty(), compilation.projectionDiagnostics.joinToString())
        assertEquals(2, compilation.projections.size)
        val projections = compilation.projections.map { it.connections.single() }
        assertEquals(1, projections.map { it.semanticId }.toSet().size)
        assertTrue(projections.first().semanticId.value.isNotBlank())
        assertEquals(1, projections.map { it.identityKind }.toSet().size)
        assertEquals(
            projections.first().participants.map { it.endpoint.occurrencePortId.portId },
            projections[1].participants.map { it.endpoint.occurrencePortId.portId },
        )
        assertTrue(projections.all { it.sourceTrace.projectionIds.contains(it.semanticId.value) })
    }

    @Test
    fun `generic relationship has no connection projection`() {
        val compilation = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(
                Path.of("src/projection.athena"),
                source().replace("connect wire PLC1.out to KM1.coil { crossSection 0.75 [mm2] conductorType copper }", "controls PLC1.out to KM1.coil"),
            ),
        )
        assertTrue(compilation.projections.all { it.connections.isEmpty() })
    }

    private fun source() = """
        system Control {
          entity PLC1 { concept controller }
          entity KM1 { concept contactor }
          port PLC1.out { direction out flow control }
          port KM1.coil { direction in flow control }
          connect wire PLC1.out to KM1.coil { crossSection 0.75 [mm2] conductorType copper }
          view schematic {
            sheet S1
            region "Power" { occurrences [PLC1, KM1] }
          }
          view service {
            sheet S1
            region "Power" { occurrences [PLC1, KM1] }
          }
        }
    """.trimIndent()
}
