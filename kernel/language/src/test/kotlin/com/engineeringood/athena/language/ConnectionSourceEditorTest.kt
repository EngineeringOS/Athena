package com.engineeringood.athena.language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ConnectionSourceEditorTest {
    private val parser = AthenaLanguageParser()
    private val editor = ConnectionSourceEditor(parser)

    @Test
    fun `inserts explicit typed connection deterministically before projection declarations`() {
        val source = """
            package com.example

            system Demo {
              port PLC1.out { direction out flow control }
              port KM1.coil { direction in flow control }
              view schematic {
                sheet S1
              }
            }
        """.trimIndent() + "\n"
        val insertion = ConnectionSourceInsertion(
            kind = ConnectionSourceKind.WIRE,
            sourcePort = "PLC1.out",
            targetPort = "KM1.coil",
            properties = listOf(
                ConnectionSourceProperty("crossSection", ConnectionSourceValue.Quantity("0.75", "mm2")),
                ConnectionSourceProperty("conductorType", ConnectionSourceValue.Symbol("copper")),
            ),
        )

        val first = editor.insertConnection("demo.athena", source, insertion)
        val second = editor.insertConnection("demo.athena", source, insertion)

        assertTrue(first.changed)
        assertEquals(first.updatedSource, second.updatedSource)
        assertTrue(
            first.updatedSource.contains(
                "  connect wire PLC1.out to KM1.coil {\n" +
                    "    conductorType copper\n" +
                    "    crossSection 0.75 [mm2]\n" +
                    "  }\n" +
                    "  view schematic {",
            ),
        )
        val parsed = assertIs<ParseSuccess>(parser.parse("demo.athena", first.updatedSource))
        val connection = parsed.ast.declarations.filterIsInstance<ConnectionDeclaration>().single()
        assertEquals("wire", connection.kind.value)
        assertEquals(listOf("PLC1", "out"), connection.source.parts)
        assertEquals(listOf("KM1", "coil"), connection.target.parts)
        assertEquals(listOf("conductorType", "crossSection"), connection.properties.map(PropertyAssignment::name))
    }

    @Test
    fun `insertion preserves CRLF and rejects an already authored connection`() {
        val source = "system Demo {\r\n  port PLC1.out { direction out }\r\n}\r\n"
        val insertion = ConnectionSourceInsertion(
            kind = ConnectionSourceKind.SIGNAL,
            sourcePort = "PLC1.out",
            targetPort = "KM1.coil",
        )

        val inserted = editor.insertConnection("demo.athena", source, insertion)

        assertFalse(Regex("(?<!\\r)\\n").containsMatchIn(inserted.updatedSource))
        val failure = assertFailsWith<IllegalArgumentException> {
            editor.insertConnection("demo.athena", inserted.updatedSource, insertion)
        }
        assertEquals("Connection `signal PLC1.out to KM1.coil` is already authored.", failure.message)
    }

    @Test
    fun `reconnect patches only selected ConnectionDeclaration endpoint span`() {
        val source = """
            system Demo {
              controls PLC1.out to KM1.coil
              connect signal PLC1.out to KM1.coil
              connect wire PLC1.out to KM1.coil {
                crossSection 0.75 [mm2]
              }
            }
        """.trimIndent()
        val parsed = assertIs<ParseSuccess>(parser.parse("demo.athena", source))
        val wire = parsed.ast.declarations.filterIsInstance<ConnectionDeclaration>()
            .single { it.kind.value == "wire" }

        val result = editor.reconnectConnection(
            "demo.athena",
            source,
            ConnectionSourceReconnect(
                declarationSpan = wire.span,
                endpoint = ConnectionSourceEndpoint.TARGET,
                replacementPort = "KM2.coil",
            ),
        )

        assertTrue(result.changed)
        assertTrue(result.updatedSource.contains("controls PLC1.out to KM1.coil"))
        assertTrue(result.updatedSource.contains("connect signal PLC1.out to KM1.coil"))
        assertTrue(result.updatedSource.contains("connect wire PLC1.out to KM2.coil"))
        assertTrue(result.updatedSource.contains("crossSection 0.75 [mm2]"))
        val updated = assertIs<ParseSuccess>(parser.parse("demo.athena", result.updatedSource))
        val updatedWire = updated.ast.declarations.filterIsInstance<ConnectionDeclaration>()
            .single { it.kind.value == "wire" }
        assertEquals(listOf("KM2", "coil"), updatedWire.target.parts)
    }

    @Test
    fun `reconnect supports source endpoint and reports no-op without rewriting source`() {
        val source = "system Demo { connect signal PLC1.out to KM1.coil }"
        val declaration = connection(source)

        val noOp = editor.reconnectConnection(
            "demo.athena",
            source,
            ConnectionSourceReconnect(declaration.span, ConnectionSourceEndpoint.SOURCE, "PLC1.out"),
        )
        val changed = editor.reconnectConnection(
            "demo.athena",
            source,
            ConnectionSourceReconnect(declaration.span, ConnectionSourceEndpoint.SOURCE, "PLC2.out"),
        )

        assertFalse(noOp.changed)
        assertEquals(source, noOp.updatedSource)
        assertEquals("system Demo { connect signal PLC2.out to KM1.coil }", changed.updatedSource)
    }

    @Test
    fun `reconnect refuses relation spans repeated endpoints and source injection`() {
        val source = """
            system Demo {
              controls PLC1.out to KM1.coil
              connect signal PLC1.out to KM1.coil
            }
        """.trimIndent()
        val parsed = assertIs<ParseSuccess>(parser.parse("demo.athena", source))
        val relation = parsed.ast.declarations.filterIsInstance<RelationDeclaration>().single()
        val connection = parsed.ast.declarations.filterIsInstance<ConnectionDeclaration>().single()

        val wrongDeclaration = assertFailsWith<IllegalArgumentException> {
            editor.reconnectConnection(
                "demo.athena",
                source,
                ConnectionSourceReconnect(relation.span, ConnectionSourceEndpoint.TARGET, "KM2.coil"),
            )
        }
        assertEquals("Selected source span does not identify one Connection declaration.", wrongDeclaration.message)

        val repeated = assertFailsWith<IllegalArgumentException> {
            editor.reconnectConnection(
                "demo.athena",
                source,
                ConnectionSourceReconnect(connection.span, ConnectionSourceEndpoint.TARGET, "PLC1.out"),
            )
        }
        assertEquals("Connection endpoints must reference distinct Ports.", repeated.message)

        assertFailsWith<IllegalArgumentException> {
            ConnectionSourceReconnect(connection.span, ConnectionSourceEndpoint.TARGET, "KM2.coil\nconnect wire A.x to B.y")
        }
    }

    @Test
    fun `typed property values render without accepting free-form source`() {
        val source = "system Demo {\n}\n"
        val result = editor.insertConnection(
            "demo.athena",
            source,
            ConnectionSourceInsertion(
                kind = ConnectionSourceKind.CONDUCTOR,
                sourcePort = "Supply.L1",
                targetPort = "Q1.line",
                properties = listOf(
                    ConnectionSourceProperty("enabled", ConnectionSourceValue.Boolean(true)),
                    ConnectionSourceProperty("label", ConnectionSourceValue.Text("Main feed")),
                    ConnectionSourceProperty("cores", ConnectionSourceValue.Number("3")),
                    ConnectionSourceProperty("potential", ConnectionSourceValue.Reference("L1")),
                ),
            ),
        )

        assertTrue(result.updatedSource.contains("enabled true"))
        assertTrue(result.updatedSource.contains("label \"Main feed\""))
        assertTrue(result.updatedSource.contains("cores 3"))
        assertTrue(result.updatedSource.contains("potential @L1"))
        assertIs<ParseSuccess>(parser.parse("demo.athena", result.updatedSource))

        assertFailsWith<IllegalArgumentException> {
            ConnectionSourceProperty("label", ConnectionSourceValue.Text("bad\"value"))
        }
        assertFailsWith<IllegalArgumentException> {
            ConnectionSourceProperty("crossSection } connect wire A.x to B.y {", ConnectionSourceValue.Number("1"))
        }
    }

    private fun connection(source: String): ConnectionDeclaration {
        val parsed = assertIs<ParseSuccess>(parser.parse("demo.athena", source))
        return parsed.ast.declarations.filterIsInstance<ConnectionDeclaration>().single()
    }
}
