package com.engineeringood.athena.compiler

import com.engineeringood.athena.runtime.AthenaHostedPluginRuntimeServices
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaCompilerComponentKnowledgeIntegrationTest {
    @Test
    fun `compile enriches governed knowledge context with resolved component knowledge`() {
        val pluginServices = AthenaHostedPluginRuntimeServices()
        val compiler = AthenaCompiler(
            hostedPluginDiscoveryReport = pluginServices.discoveryReport(),
            hostedDomainPlugins = pluginServices.domainSemanticsContributions().map { contribution -> contribution.domainPlugin },
        )

        val result = compiler.compile(
            path = Path.of("component-knowledge-integration.athena"),
            sourceText = """
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

        val success = assertIs<CompilerCompilationSuccess>(result)
        val knowledgeContext = success.knowledgeContext

        assertEquals(
            listOf("com.engineeringood.athena.domain.electrical-runtime"),
            knowledgeContext.componentKnowledgeContributors,
        )
        assertEquals(5, knowledgeContext.activeComponentConceptCount)
        assertEquals(5, knowledgeContext.activeComponentImplementationCount)
        assertEquals(
            listOf("component:M1", "component:PLC1"),
            knowledgeContext.resolvedComponents.map { resolved -> resolved.semanticSubjectId.value },
        )
        assertEquals(
            listOf("electrical.motor.ac", "electrical.plc.cpu"),
            knowledgeContext.resolvedComponents.map { resolved -> resolved.concept.conceptId.value },
        )
        assertEquals(
            listOf("proof.motor.ac", "proof.cpu.313c"),
            knowledgeContext.resolvedImplementations.map { resolved -> resolved.implementation.vendorPartNumber.value },
        )
        assertEquals(4, knowledgeContext.resolvedSemanticPorts.size)
        assertEquals(
            "component:PLC1",
            knowledgeContext.resolvedPhysicalTraits.single().semanticSubjectId.value,
        )
        assertTrue(knowledgeContext.componentKnowledgeDiagnostics.isEmpty())

        assertNotNull(knowledgeContext.resolvedComponent("component:PLC1"))
        assertEquals(4, knowledgeContext.resolvedSemanticPortsForOwner("component:PLC1").size)
        assertNotNull(knowledgeContext.resolvedPhysicalTrait("component:PLC1"))
    }
}
