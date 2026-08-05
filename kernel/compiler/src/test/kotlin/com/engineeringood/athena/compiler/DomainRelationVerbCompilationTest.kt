package com.engineeringood.athena.compiler

import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DomainRelationVerbCompilationTest {
    @Test
    fun `electrical relation verbs lower to typed relationships and independent flows`() {
        val path = java.nio.file.Files.createTempFile("athena-domain-relations-", ".athena")
        path.writeText(
            """
            system DomainRelations {
              entity Supply { concept PowerSource connectivity enabled }
              entity Breaker { concept Breaker connectivity enabled }
              entity Controller { concept Switch connectivity enabled }
              entity Terminal { concept Terminal connectivity enabled }
              entity EarthBar { concept ProtectiveEarth connectivity enabled }
              entity Motor { concept Motor connectivity enabled }
              entity Cabinet { concept Terminal connectivity enabled }

              port Supply.L1 { direction out flow power role line }
              port Breaker.input { direction in flow power role line }
              port Controller.DO1 { direction out flow control role flow }
              port Terminal.input { direction in flow control role flow }
              port EarthBar.PE { direction bidirectional flow pe role protective_earth }
              port Motor.PE { direction bidirectional flow pe role protective_earth }
              port Cabinet.PE { direction bidirectional flow pe role protective_earth }

              power Supply.L1 to Breaker.input
              control Controller.DO1 to Terminal.input
              earth EarthBar.PE to [Motor.PE, Cabinet.PE]
            }
            """.trimIndent(),
        )

        try {
            val result = assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path))

            assertTrue(result.semanticResult.isSemanticallyValid)
            assertEquals(3, result.document.relationships.size)
            assertEquals(
                listOf("source", "target"),
                result.document.relationships.first().participants.map { participant -> participant.role },
            )
            assertEquals(4, result.document.flows.size)
            assertEquals(
                listOf("power", "control", "earth", "earth"),
                result.document.flows.map { flow -> flow.definitionReference.authoredName.single() },
            )
            val earthRelationship = result.document.relationships.single { relationship ->
                relationship.definitionReference.authoredName.single() == "earth"
            }
            assertEquals(listOf("source", "target", "target-2"), earthRelationship.participants.map { it.role })
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
              entity A { concept Switch connectivity enabled }
              entity B { concept Switch connectivity enabled }
              entity C { concept Switch connectivity enabled }
              port A.out { direction out flow control role flow }
              port B.in { direction in flow control role flow }
              port C.in { direction in flow control role flow }

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
            assertEquals("pneumatic", result.document.relationships.single().definitionReference.authoredName.single())
        } finally {
            path.deleteIfExists()
        }
    }
}
