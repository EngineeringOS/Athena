package com.engineeringood.athena.compiler

import com.engineeringood.athena.runtime.AthenaHostedPluginRuntimeServices
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaProjectionPresentationComponentKnowledgeIntegrationTest {
    @Test
    fun `compile exposes resolved component knowledge to projection and presentation consumers`() {
        val pluginServices = AthenaHostedPluginRuntimeServices()
        val compiler = AthenaCompiler(
            hostedPluginDiscoveryReport = pluginServices.discoveryReport(),
            hostedDomainPlugins = pluginServices.domainSemanticsContributions().map { contribution -> contribution.domainPlugin },
        )

        val result = compiler.compile(
            path = Path.of("projection-presentation-component-knowledge.athena"),
            sourceText = """
                system ComponentKnowledgeProjectionDemo {
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
        val cabinetProjection = success.projections.first { projection -> projection.view.id == "cabinet" }
        val cabinetPresentation = success.presentations.first { presentation -> presentation.view.id == "cabinet" }

        assertEquals(
            listOf("component:M1", "component:PLC1"),
            cabinetProjection.resolvedSubjects.map { resolved -> resolved.semanticId.value },
        )
        val plcProjection = cabinetProjection.resolvedSubjects.first { resolved -> resolved.semanticId.value == "component:PLC1" }
        assertEquals("electrical.plc.cpu", plcProjection.conceptId)
        assertEquals("proof.cpu.313c", plcProjection.vendorPartNumber)
        assertEquals("din-rail", plcProjection.mountingTypeId)
        val plcProjectionSize = assertNotNull(plcProjection.physicalSize)
        assertTrue(plcProjectionSize.widthMillimeters > 0)

        assertEquals(
            listOf("component:M1", "component:PLC1"),
            cabinetPresentation.resolvedSubjects.map { resolved -> resolved.semanticId.value },
        )
        val plcPresentation = cabinetPresentation.resolvedSubjects.first { resolved ->
            resolved.semanticId.value == "component:PLC1"
        }
        assertEquals("electrical.plc.cpu", plcPresentation.conceptId)
        assertEquals("proof.cpu.313c", plcPresentation.vendorPartNumber)
        assertEquals("din-rail", plcPresentation.mountingTypeId)
        assertNotNull(plcPresentation.physicalSize)
        assertTrue(plcPresentation.installationMarkerIds.isNotEmpty())
    }
}
