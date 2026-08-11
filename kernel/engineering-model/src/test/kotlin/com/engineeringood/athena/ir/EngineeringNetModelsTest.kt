package com.engineeringood.athena.ir

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EngineeringNetModelsTest {
    private val provenance = SourceProvenance("net.athena", 1, 1, 8, 2)

    @Test
    fun `net preserves multi-endpoint identity without pairwise flattening`() {
        val net = EngineeringNet(
            id = StableSemanticIdentity("net:net.athena:StartCircuit"),
            name = "StartCircuit",
            kind = ConnectionKind.SIGNAL,
            endpoints = listOf(endpoint("PLC1.out", ConnectionEndpointRole.SOURCE), endpoint("KM1.coil", ConnectionEndpointRole.SINK), endpoint("X1.1", ConnectionEndpointRole.PASS)),
            potentialOrSignal = EngineeringReference(listOf("Control24V"), null, provenance),
            properties = emptyList(),
            provenance = provenance,
        )

        assertEquals(3, net.endpoints.size)
        assertEquals("Control24V", net.potentialOrSignal?.authoredPath?.single())
    }

    @Test
    fun `net rejects fewer than two or repeated endpoint ports`() {
        assertFailsWith<IllegalArgumentException> { net(listOf(endpoint("PLC1.out", ConnectionEndpointRole.SOURCE))) }
        assertFailsWith<IllegalArgumentException> {
            net(listOf(endpoint("PLC1.out", ConnectionEndpointRole.SOURCE), endpoint("PLC1.out", ConnectionEndpointRole.SINK)))
        }
    }

    private fun net(endpoints: List<ConnectionEndpoint>) = EngineeringNet(
        StableSemanticIdentity("net:test"), "test", ConnectionKind.WIRE, endpoints, null, emptyList(), provenance,
    )

    private fun endpoint(path: String, role: ConnectionEndpointRole) = ConnectionEndpoint(
        EngineeringReference(path.split('.'), StableSemanticIdentity("port:$path"), provenance), role, provenance,
    )
}
