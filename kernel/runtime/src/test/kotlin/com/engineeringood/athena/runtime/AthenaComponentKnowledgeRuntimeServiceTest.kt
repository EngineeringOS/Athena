package com.engineeringood.athena.runtime

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaComponentKnowledgeRuntimeServiceTest {
    @Test
    fun `inspect publishes deterministic resolved component knowledge through the runtime seam`() {
        val sourcePath = writeProject(
            """
                system ComponentKnowledgeDemo {
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
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "component-knowledge-demo",
                sourcePath = sourcePath,
            )

            val first = context.componentKnowledgeRuntime().inspect(context)
            val second = context.componentKnowledgeRuntime().inspect(context)

            assertEquals(first, second)
            val ready = assertIs<AthenaComponentKnowledgeReady>(first)
            assertEquals("system:ComponentKnowledgeDemo", ready.systemSemanticId)
            assertEquals(listOf("com.engineeringood.athena.domain.electrical-runtime"), ready.contributingPluginIds)
            assertEquals(5, ready.activeConceptCount)
            assertEquals(6, ready.activeImplementationCount)
            assertEquals(
                listOf(
                    "electrical.contactor.power",
                    "electrical.motor.ac",
                    "electrical.plc.cpu",
                    "electrical.power-supply.dc24",
                    "electrical.relay.overload",
                ),
                ready.availableComponents.map { entry -> entry.concept.conceptId.value },
            )
            assertEquals(
                listOf("component:M1", "component:PLC1"),
                ready.components.map { entry -> entry.resolvedComponent.semanticSubjectId.value },
            )
            assertEquals(
                listOf("electrical.motor.ac", "electrical.plc.cpu"),
                ready.components.map { entry -> entry.resolvedComponent.concept.conceptId.value },
            )
            assertEquals(4, ready.semanticPorts.size)
            assertEquals(listOf("component:PLC1"), ready.physicalTraits.map { trait -> trait.semanticSubjectId.value })
            assertTrue(ready.diagnostics.isEmpty())
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `inspect surfaces unresolved diagnostics without frontend re-resolution`() {
        val sourcePath = writeProject(
            """
                system ComponentKnowledgeFailure {
                  device X1 {
                    type Switch
                    model "unknown.vendor.part"
                  }
                }
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "component-knowledge-failure",
                sourcePath = sourcePath,
            )

            val result = context.componentKnowledgeRuntime().inspect(context)

            val ready = assertIs<AthenaComponentKnowledgeReady>(result)
            assertEquals("system:ComponentKnowledgeFailure", ready.systemSemanticId)
            assertEquals(5, ready.availableComponents.size)
            assertTrue(ready.components.isEmpty())
            assertEquals(
                listOf("component.definition.unresolved"),
                ready.diagnostics.map { diagnostic -> diagnostic.ruleId.value },
            )
            assertEquals(listOf("unknown.vendor.part"), ready.diagnostics.map { diagnostic -> diagnostic.subject })
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-component-knowledge-", ".athena")
        Files.writeString(path, source)
        return path
    }
}
