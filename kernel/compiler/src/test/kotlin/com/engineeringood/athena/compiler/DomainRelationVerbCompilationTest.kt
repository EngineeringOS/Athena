package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringPropertyValue
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DomainRelationVerbCompilationTest {
    @Test
    fun `electrical relation verbs lower to existing engineering connections with relation contract`() {
        val path = java.nio.file.Files.createTempFile("athena-domain-relations-", ".athena")
        path.writeText(
            """
            system DomainRelations {
              device Supply { type PowerSource connectivity enabled }
              device Breaker { type Breaker connectivity enabled }
              device Controller { type Switch connectivity enabled }
              device Terminal { type Terminal connectivity enabled }
              device EarthBar { type ProtectiveEarth connectivity enabled }
              device Motor { type Motor connectivity enabled }
              device Cabinet { type Terminal connectivity enabled }

              port Supply.L1 { direction out signal power role line }
              port Breaker.input { direction in signal power role line }
              port Controller.DO1 { direction out signal control role signal }
              port Terminal.input { direction in signal control role signal }
              port EarthBar.PE { direction passive signal pe role protective_earth }
              port Motor.PE { direction passive signal pe role protective_earth }
              port Cabinet.PE { direction passive signal pe role protective_earth }

              power Supply.L1 to Breaker.input
              control Controller.DO1 to Terminal.input
              earth EarthBar.PE to [Motor.PE, Cabinet.PE]
            }
            """.trimIndent(),
        )

        try {
            val result = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path))

            assertTrue(result.semanticResult.isSemanticallyValid)
            assertEquals(4, result.document.connections.size)
            assertEquals(
                listOf("power", "control", "earth", "earth"),
                result.document.connections.map { connection ->
                    assertIs<EngineeringPropertyValue.Symbol>(
                        connection.properties.single { property -> property.name == "relation.kind" }.value,
                    ).text
                },
            )
            val earthNetwork = result.document.connectionNetworks.single { network -> network.name == "earth_EarthBar_PE" }
            assertEquals(2, earthNetwork.members.size)
            assertTrue(earthNetwork.members.all { member -> member.connectionReference.provenance == earthNetwork.provenance })
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `unknown electrical relation reports active domain and available relation words`() {
        val path = java.nio.file.Files.createTempFile("athena-unknown-domain-relation-", ".athena")
        path.writeText(
            """
            system UnknownDomainRelation {
              device A { type Switch connectivity enabled }
              device B { type Switch connectivity enabled }
              device C { type Switch connectivity enabled }
              port A.out { direction out signal control role signal }
              port B.in { direction in signal control role signal }
              port C.in { direction in signal control role signal }

              pneumatic A.out to [B.in, C.in]
            }
            """.trimIndent(),
        )

        try {
            val result = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path))

            val messages = result.validationBreakdown.domainDiagnostics.map { diagnostic -> diagnostic.message }
            assertTrue(messages.any { message ->
                "pneumatic" in message &&
                    "com.engineeringood.athena.domain.electrical-runtime" in message &&
                    "power, control, earth" in message
            }, "Actual diagnostics: $messages")
            assertEquals(emptyList(), result.document.connections)
            assertEquals(emptyList(), result.document.connectionNetworks)
        } finally {
            path.deleteIfExists()
        }
    }
}
