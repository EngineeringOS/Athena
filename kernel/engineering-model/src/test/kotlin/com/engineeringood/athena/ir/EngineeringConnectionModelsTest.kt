package com.engineeringood.athena.ir

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EngineeringConnectionModelsTest {
    private val provenance = SourceProvenance("connection.athena", 1, 1, 1, 30)

    @Test
    fun `binary connection keeps endpoint role separate from port direction`() {
        val source = EngineeringReference(listOf("PLC1", "out"), StableSemanticIdentity("port:PLC1.out"), provenance)
        val sink = EngineeringReference(listOf("KM1", "power"), StableSemanticIdentity("port:KM1.power"), provenance)
        val connection = EngineeringConnection(
            id = StableSemanticIdentity("connection:connection.athena:wire:PLC1.out:KM1.power"),
            kind = ConnectionKind.WIRE,
            endpoints = listOf(
                ConnectionEndpoint(source, ConnectionEndpointRole.SOURCE, provenance),
                ConnectionEndpoint(sink, ConnectionEndpointRole.SINK, provenance),
            ),
            properties = emptyList(),
            provenance = provenance,
        )

        assertEquals(2, connection.endpoints.size)
        assertEquals(ConnectionEndpointRole.SOURCE, connection.endpoints[0].role)
        assertEquals("port:KM1.power", connection.endpoints[1].port.resolvedIdentity?.value)
    }

    @Test
    fun `binary connection rejects anything other than two endpoints`() {
        val endpoint = ConnectionEndpoint(
            EngineeringReference(listOf("PLC1", "out"), StableSemanticIdentity("port:PLC1.out"), provenance),
            ConnectionEndpointRole.SOURCE,
            provenance,
        )
        assertFailsWith<IllegalArgumentException> {
            EngineeringConnection(
                StableSemanticIdentity("connection:invalid"),
                ConnectionKind.WIRE,
                listOf(endpoint),
                emptyList(),
                provenance,
            )
        }
    }

    @Test
    fun `binary connection rejects repeated or blank endpoint paths`() {
        val port = EngineeringReference(listOf("PLC1", "out"), StableSemanticIdentity("port:PLC1.out"), provenance)
        assertFailsWith<IllegalArgumentException> {
            EngineeringConnection(
                StableSemanticIdentity("connection:self"),
                ConnectionKind.WIRE,
                listOf(
                    ConnectionEndpoint(port, ConnectionEndpointRole.SOURCE, provenance),
                    ConnectionEndpoint(port, ConnectionEndpointRole.SINK, provenance),
                ),
                emptyList(),
                provenance,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            ConnectionEndpoint(
                EngineeringReference(listOf("PLC1", ""), null, provenance),
                ConnectionEndpointRole.SOURCE,
                provenance,
            )
        }
    }
}
