package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import org.eclipse.lsp4j.InitializeParams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaComponentKnowledgeRequestTest {
    @Test
    @Suppress("DEPRECATION")
    fun `component knowledge request exposes runtime owned resolved knowledge through lsp`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-component-knowledge-ready-",
            sourceFileName = "component-knowledge.athena",
            sourceText = componentKnowledgeSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val payload = assertNotNull(
                    server.componentKnowledgeSession(AthenaComponentKnowledgeSessionParams()).get(),
                )

                assertEquals("ready", payload.status)
                assertEquals("factory-line", payload.projectName)
                assertEquals("system:ComponentKnowledge", payload.systemSemanticId)
                assertEquals("frontend -> LSP -> runtime/compiler", payload.semanticPath)
                assertEquals(listOf("com.engineeringood.athena.domain.electrical-runtime"), payload.contributingPluginIds)
                assertEquals(
                    listOf(
                        "electrical.contactor.power",
                        "electrical.motor.ac",
                        "electrical.plc.cpu",
                        "electrical.power-supply.dc24",
                        "electrical.relay.overload",
                    ),
                    payload.availableComponents.map { component -> component.conceptId },
                )
                assertEquals(
                    listOf("component:M1", "component:PLC1"),
                    payload.components.map { component -> component.semanticSubjectId },
                )
                assertEquals(
                    listOf("electrical.motor.ac", "electrical.plc.cpu"),
                    payload.components.map { component -> component.conceptId },
                )
                assertEquals(4, payload.semanticPorts.size)
                assertEquals(1, payload.physicalTraits.size)
                assertTrue(payload.diagnostics.isEmpty())
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `component knowledge request surfaces unresolved diagnostics deterministically`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-component-knowledge-unresolved-",
            sourceFileName = "component-knowledge-unresolved.athena",
            sourceText = """
                system ComponentKnowledgeUnresolved {
                  device X1 {
                    type Switch
                    model "unknown.vendor.part"
                  }
                }
            """.trimIndent(),
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val payload = assertNotNull(
                    server.componentKnowledgeSession(AthenaComponentKnowledgeSessionParams()).get(),
                )

                assertEquals("ready", payload.status)
                assertEquals("system:ComponentKnowledgeUnresolved", payload.systemSemanticId)
                assertEquals(5, payload.availableComponents.size)
                assertTrue(payload.components.isEmpty())
                assertEquals(
                    listOf("component.definition.unresolved"),
                    payload.diagnostics.map { diagnostic -> diagnostic.ruleId },
                )
                assertEquals(listOf("unknown.vendor.part"), payload.diagnostics.map { diagnostic -> diagnostic.subject })
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}

private val componentKnowledgeSource = """
    system ComponentKnowledge {
      device PLC1 {
        type Switch
        model "proof.cpu.313c"
      }

      device M1 {
        type Motor
        model "proof.motor.ac"
      }

      port PLC1.lplus {
        direction out
        signal Power24
      }

      port PLC1.m {
        direction in
        signal Common24
      }

      port PLC1.pe {
        direction in
        signal ProtectiveEarth
      }

      port PLC1.mpi {
        direction out
        signal MPIBus
      }
    }
""".trimIndent()
