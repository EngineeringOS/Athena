package com.engineeringood.athena.connection

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SemanticPortModelContractTest {
    @Test
    fun `semantic port definition expresses role direction signal family and optional protocols`() {
        val definition = SemanticPortDefinition(
            portTypeId = SemanticPortTypeId("electrical.signal.plc-bus"),
            displayName = "PLC bus port",
            roleId = SemanticPortRoleId("mpi"),
            direction = SemanticPortDirection.BIDIRECTIONAL,
            signalFamilyId = SemanticSignalFamilyId("electrical.communication"),
            protocolIds = setOf(SemanticProtocolId("mpi")),
            summary = "Bidirectional PLC communication port.",
        )

        assertEquals("electrical.signal.plc-bus", definition.portTypeId.value)
        assertEquals("mpi", definition.roleId.value)
        assertEquals(SemanticPortDirection.BIDIRECTIONAL, definition.direction)
        assertEquals("electrical.communication", definition.signalFamilyId.value)
        assertEquals(setOf("mpi"), definition.protocolIds.map { protocol -> protocol.value }.toSet())
    }

    @Test
    fun `resolved semantic port stays anchored to canonical port identity instead of frontend state`() {
        val resolved = ResolvedSemanticPortDefinition(
            portSemanticId = StableSemanticIdentity("port:PLC1.MPI"),
            ownerSemanticId = StableSemanticIdentity("component:PLC1"),
            definition = SemanticPortDefinition(
                portTypeId = SemanticPortTypeId("electrical.signal.plc-bus"),
                displayName = "PLC bus port",
                roleId = SemanticPortRoleId("mpi"),
                direction = SemanticPortDirection.BIDIRECTIONAL,
                signalFamilyId = SemanticSignalFamilyId("electrical.communication"),
                protocolIds = setOf(SemanticProtocolId("mpi")),
            ),
        )

        assertEquals("port:PLC1.MPI", resolved.portSemanticId.value)
        assertEquals("component:PLC1", resolved.ownerSemanticId.value)
        assertTrue(resolved.definition.protocolIds.isNotEmpty())
    }
}
