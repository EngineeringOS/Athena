package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.ConnectionEndpointRole
import com.engineeringood.athena.ir.ConnectionKind
import com.engineeringood.athena.ir.EngineeringValue
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EngineeringConnectionLoweringTest {
    @Test
    fun `lowers explicit binary connection with stable port endpoints`() {
        val first = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(Path.of("src/control.athena"), source()))
        val second = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(Path.of("src/control.athena"), source()))
        val connection = first.document.connections.single()

        assertEquals(ConnectionKind.WIRE, connection.kind)
        assertEquals(listOf(ConnectionEndpointRole.SOURCE, ConnectionEndpointRole.SINK), connection.endpoints.map { it.role })
        assertEquals(listOf("PLC1", "out"), connection.endpoints[0].port.authoredPath)
        assertEquals(listOf("KM1", "coil"), connection.endpoints[1].port.authoredPath)
        assertEquals(connection.id, second.document.connections.single().id)
        assertEquals(0, first.document.relationships.size)
    }

    @Test
    fun `closed hyphenated kind lowers without renderer vocabulary`() {
        val result = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(Path.of("src/control.athena"), source().replace("connect wire", "connect cable-core")),
        )

        assertEquals(ConnectionKind.CABLE_CORE, result.document.connections.single().kind)
    }

    @Test
    fun `all closed connection kinds lower exactly`() {
        val authoredKinds = mapOf(
            "conductor" to ConnectionKind.CONDUCTOR,
            "wire" to ConnectionKind.WIRE,
            "cable-core" to ConnectionKind.CABLE_CORE,
            "jumper" to ConnectionKind.JUMPER,
            "busbar" to ConnectionKind.BUSBAR,
            "signal" to ConnectionKind.SIGNAL,
        )

        authoredKinds.forEach { (authored, expected) ->
            val result = assertIs<CompilerCompilationSuccess>(
                AthenaCompiler().compile(Path.of("src/control.athena"), source().replace("connect wire", "connect $authored")),
            )
            assertEquals(expected, result.document.connections.single().kind)
        }
    }

    @Test
    fun `authored connection properties lower as typed engineering values`() {
        val result = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(
                Path.of("src/control.athena"),
                source().replace(
                    "connect wire PLC1.out to KM1.coil { crossSection 0.75 [mm2] conductorType copper }",
                    "connect wire PLC1.out to KM1.coil { colorCode black crossSection 0.75 [mm2] conductorType copper }",
                ),
            ),
        )

        val properties = result.document.connections.single().properties
        assertEquals("black", assertIs<EngineeringValue.Symbol>(properties[0].value).text)
        assertTrue(properties.any { it.name == "crossSection" && it.value is EngineeringValue.Quantity })
    }

    @Test
    fun `unresolved endpoint stops downstream with exact port diagnostic`() {
        val result = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(
                Path.of("src/control.athena"),
                source().replace("connect wire PLC1.out to KM1.coil", "connect wire PLC1.out to KM1.missing"),
            ),
        )

        assertTrue(result.semanticResult.diagnostics.any { diagnostic ->
            diagnostic.ruleId.value == "reference.connection-endpoint.unresolved" &&
                diagnostic.message.contains("KM1.missing") &&
                diagnostic.message.contains("Declare Port `KM1.missing` or correct this connection path")
        })
        assertTrue(result.document.connections.isEmpty())
        assertTrue(result.projections.isEmpty())
    }

    @Test
    fun `port direction validates independently from endpoint role`() {
        val result = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(
                Path.of("src/control.athena"),
                source().replace("direction out", "direction in"),
            ),
        )

        assertEquals(ConnectionEndpointRole.SOURCE, result.document.connections.single().endpoints.first().role)
        assertTrue(result.semanticResult.diagnostics.any { it.ruleId.value == "direction.connection-source.illegal" })
    }

    @Test
    fun `generic relationship never becomes engineering connection`() {
        val result = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(
                Path.of("src/control.athena"),
                source().replace(
                    "connect wire PLC1.out to KM1.coil { crossSection 0.75 [mm2] conductorType copper }",
                    "controls PLC1.out to KM1.coil",
                ),
            ),
        )

        assertTrue(result.document.connections.isEmpty())
        assertEquals(1, result.document.relationships.size)
    }

    @Test
    fun `net preserves membership without synthesizing pairwise connections`() {
        val result = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(Path.of("src/control.athena"), netSource()),
        )

        val net = result.document.nets.single()
        assertEquals("StartCircuit", net.name)
        assertEquals(ConnectionKind.SIGNAL, net.kind)
        assertEquals(
            listOf(ConnectionEndpointRole.SOURCE, ConnectionEndpointRole.SINK, ConnectionEndpointRole.PASS),
            net.endpoints.map { it.role },
        )
        assertEquals(3, net.endpoints.size)
        assertTrue(result.document.connections.isEmpty())
        assertEquals(net.id, assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(Path.of("src/control.athena"), netSource()),
        ).document.nets.single().id)
    }

    @Test
    fun `wire and cable core require physical facts`() {
        val wire = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(
                Path.of("src/control.athena"),
                netSource().replace("net StartCircuit signal", "net StartCircuit wire")
                    .replace("crossSection 0.75 [mm2]", ""),
            ),
        )
        assertTrue(wire.semanticResult.diagnostics.any { it.ruleId.value == "connectivity.wire.cross-section.missing" })

        val cable = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(
                Path.of("src/control.athena"),
                netSource().replace("net StartCircuit signal", "net StartCircuit cable-core")
                    .replace("crossSection 0.75 [mm2]", "crossSection 0.75 [mm2]"),
            ),
        )
        assertTrue(cable.semanticResult.diagnostics.any { it.ruleId.value == "connectivity.cable-core.conductor-type.missing" })
    }

    @Test
    fun `unknown connection fact fails closed with correction`() {
        val result = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(Path.of("src/control.athena"), netSource().replace("crossSection 0.75 [mm2]", "paintColor red")),
        )
        assertTrue(result.semanticResult.diagnostics.any { diagnostic ->
            diagnostic.ruleId.value == "connectivity.fact.unknown" && diagnostic.message.contains("paintColor")
        })
        assertTrue(result.document.nets.isEmpty())
    }

    @Test
    fun `specification precedence keeps most specific authored provenance`() {
        val source = netSource().replace(
            "connection-spec project { conductorType copper }",
            "connection-spec project { colorCode black conductorType copper }\n          connection-spec signal Control24V { colorCode red }",
        ).replace("net StartCircuit signal {", "net StartCircuit signal {\n            signal Control24V")
        val result = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(Path.of("src/control.athena"), source))
        val color = result.document.nets.single().effectiveProperties.single { it.name == "colorCode" }
        assertEquals("red", assertIs<EngineeringValue.Symbol>(color.value).text)
        assertTrue(color.provenance.startLine > 0)
    }

    @Test
    fun `contradictory same-scope specification blocks net publication`() {
        val source = netSource().replace(
            "connection-spec project { conductorType copper }",
            "connection-spec project { colorCode black conductorType copper }\n          connection-spec project { colorCode red }",
        )
        val result = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(Path.of("src/control.athena"), source))
        assertTrue(result.semanticResult.diagnostics.any { it.ruleId.value == "connectivity.specification.contradictory" })
        assertTrue(result.document.nets.isEmpty())
    }

    private fun source(): String =
        """
        system Control {
          entity PLC1 { concept controller }
          entity KM1 { concept contactor }
          port PLC1.out { direction out flow control }
          port KM1.coil { direction in flow control }
          connect wire PLC1.out to KM1.coil { crossSection 0.75 [mm2] conductorType copper }
        }
        """.trimIndent()

    private fun netSource(): String =
        """
        system Control {
          entity PLC1 { concept controller }
          entity KM1 { concept contactor }
          entity X1 { concept terminal }
          port PLC1.out { direction out flow control }
          port KM1.coil { direction in flow control }
          port X1.p1 { direction bidirectional flow control }
          connection-spec project { conductorType copper }
          net StartCircuit signal {
            source PLC1.out
            sink KM1.coil
            pass X1.p1
            crossSection 0.75 [mm2]
          }
        }
        """.trimIndent()
}
