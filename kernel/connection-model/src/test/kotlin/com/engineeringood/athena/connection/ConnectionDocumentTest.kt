package com.engineeringood.athena.connection

import com.engineeringood.athena.ir.ConnectionEndpointRole
import com.engineeringood.athena.ir.ConnectionKind
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConnectionDocumentTest {
    private val trace = ConnectionSourceTrace(SourceProvenance("source.athena", 1, 1, 2, 1))

    @Test
    fun `canonical digest is stable across input order`() {
        val first = ConnectionDocument.canonical(
            listOf(connection("connection:b"), connection("connection:a")), emptyList(), emptyList(),
        )
        val second = ConnectionDocument.canonical(
            listOf(connection("connection:a"), connection("connection:b")), emptyList(), emptyList(),
        )

        assertEquals(first, second)
        assertTrue(first.digest.value.matches(Regex("[0-9a-f]{64}")))
        assertEquals("athena-connection-ir-c14n-v1", first.canonicalization)
    }

    @Test
    fun `topology operator preserves ordered endpoints without geometry`() {
        val operator = TopologyOperator(
            StableSemanticIdentity("operator:net:n1:branch"), TopologyOperatorKind.BRANCH,
            StableSemanticIdentity("net:n1"), listOf(StableSemanticIdentity("port:a"), StableSemanticIdentity("port:b")), trace,
        )
        val document = ConnectionDocument.canonical(emptyList(), emptyList(), listOf(operator))
        assertEquals(listOf("port:a", "port:b"), document.topologyOperators.single().orderedEndpointIds.map { it.value })
        assertTrue(ConnectionDocument::class.java.declaredFields.none { it.name in setOf("x", "y", "geometry", "konva", "viewport") })
    }

    private fun connection(id: String) = ConnectionFact(
        StableSemanticIdentity(id), ConnectionKind.SIGNAL,
        listOf(
            ConnectionEndpointFact(StableSemanticIdentity("port:a"), listOf("A", "out"), ConnectionEndpointRole.SOURCE, trace),
            ConnectionEndpointFact(StableSemanticIdentity("port:b"), listOf("B", "in"), ConnectionEndpointRole.SINK, trace),
        ),
        ResolvedConnectionSpecification(emptyList()), trace,
    )
}
