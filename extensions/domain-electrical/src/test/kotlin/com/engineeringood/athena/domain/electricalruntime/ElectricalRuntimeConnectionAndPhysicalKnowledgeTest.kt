package com.engineeringood.athena.domain.electricalruntime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ElectricalRuntimeConnectionAndPhysicalKnowledgeTest {
    @Test
    fun `publishes the first plc cpu semantic-port slice with lplus m pe and mpi`() {
        val ports = plcCpuResolvedSemanticPorts()

        assertEquals(
            setOf("l+", "m", "pe", "mpi"),
            ports.map { resolved -> resolved.definition.roleId.value }.toSet(),
        )
        assertEquals(
            setOf("component:PLC1"),
            ports.map { resolved -> resolved.ownerSemanticId.value }.toSet(),
        )
        assertTrue(
            ports.any { resolved ->
                resolved.definition.roleId.value == "mpi" &&
                    resolved.definition.protocolIds.map { protocol -> protocol.value }.toSet() == setOf("mpi")
            },
        )
    }

    @Test
    fun `publishes at least one targeted physical-trait slice with dimensions and mounting type`() {
        val traits = siemensProofResolvedPhysicalTraits()
        val plcCpu = traits.single { resolved -> resolved.semanticSubjectId.value == "component:PLC1" }

        assertEquals("din-rail", plcCpu.definition.mountingTypeId.value)
        assertTrue(plcCpu.definition.size.widthMillimeters > 0)
        assertTrue(plcCpu.definition.size.heightMillimeters > 0)
        assertTrue(plcCpu.definition.size.depthMillimeters > 0)
        assertTrue(plcCpu.definition.installationMarkerIds.isNotEmpty())
    }
}
