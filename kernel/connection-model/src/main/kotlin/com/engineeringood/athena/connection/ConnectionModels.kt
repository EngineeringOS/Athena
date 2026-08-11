package com.engineeringood.athena.connection

import com.engineeringood.athena.ir.ConnectionEndpointRole
import com.engineeringood.athena.ir.ConnectionKind
import com.engineeringood.athena.ir.EngineeringNet
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity

/** Trace from one canonical connection fact back to authored Athena source. */
data class ConnectionSourceTrace(val provenance: SourceProvenance)

data class ConnectionEndpointFact(
    val portId: StableSemanticIdentity,
    val authoredPath: List<String>,
    val role: ConnectionEndpointRole,
    val trace: ConnectionSourceTrace,
) {
    init {
        require(portId.value.isNotBlank()) { "Connection endpoint Port identity must not be blank" }
        require(authoredPath.isNotEmpty() && authoredPath.all(String::isNotBlank)) {
            "Connection endpoint authored Port path must not be blank"
        }
    }
}

data class ResolvedConnectionSpecification(val properties: List<EngineeringProperty>)

data class ConnectionFact(
    val id: StableSemanticIdentity,
    val kind: ConnectionKind,
    val endpoints: List<ConnectionEndpointFact>,
    val specification: ResolvedConnectionSpecification,
    val trace: ConnectionSourceTrace,
) {
    init {
        require(id.value.isNotBlank()) { "Connection fact identity must not be blank" }
        require(endpoints.size == 2) { "Connection fact requires exactly two endpoints" }
    }
}

data class NetFact(
    val id: StableSemanticIdentity,
    val name: String,
    val kind: ConnectionKind,
    val endpoints: List<ConnectionEndpointFact>,
    val potentialOrSignal: List<String>?,
    val specification: ResolvedConnectionSpecification,
    val trace: ConnectionSourceTrace,
) {
    init {
        require(id.value.isNotBlank()) { "Net fact identity must not be blank" }
        require(name.isNotBlank()) { "Net fact name must not be blank" }
        require(endpoints.size >= 2) { "Net fact requires at least two endpoints" }
        require(endpoints.map { it.portId }.distinct().size == endpoints.size) {
            "Net fact endpoint identities must be distinct"
        }
    }
}

enum class TopologyOperatorKind { BRANCH, MERGE, PASS_THROUGH, INTERRUPTION }

data class TopologyOperator(
    val id: StableSemanticIdentity,
    val kind: TopologyOperatorKind,
    val netId: StableSemanticIdentity,
    val orderedEndpointIds: List<StableSemanticIdentity>,
    val trace: ConnectionSourceTrace,
) {
    init {
        require(id.value.isNotBlank()) { "Topology operator identity must not be blank" }
        require(netId.value.isNotBlank()) { "Topology operator Net identity must not be blank" }
        require(orderedEndpointIds.isNotEmpty()) { "Topology operator requires ordered endpoint identities" }
    }
}

@JvmInline
value class ConnectionDocumentDigest(val value: String)

/** Canonical, renderer-neutral derived connectivity document. */
data class ConnectionDocument private constructor(
    val connections: List<ConnectionFact>,
    val nets: List<NetFact>,
    val topologyOperators: List<TopologyOperator>,
    val canonicalization: String,
    val digest: ConnectionDocumentDigest,
) {
    companion object {
        const val CANONICALIZATION = "athena-connection-ir-c14n-v1"

        fun canonical(
            connections: List<ConnectionFact>,
            nets: List<NetFact>,
            topologyOperators: List<TopologyOperator>,
        ): ConnectionDocument {
            val canonicalConnections = connections.sortedBy { it.id.value }.map(::canonicalConnection)
            val canonicalNets = nets.sortedBy { it.id.value }.map(::canonicalNet)
            val canonicalOperators = topologyOperators.sortedBy { it.id.value }.map(::canonicalOperator)
            val payload = buildString {
                append(CANONICALIZATION).append('|')
                canonicalConnections.forEach { appendConnection(this, it) }
                canonicalNets.forEach { appendNet(this, it) }
                canonicalOperators.forEach { appendOperator(this, it) }
            }
            return ConnectionDocument(
                canonicalConnections,
                canonicalNets,
                canonicalOperators,
                CANONICALIZATION,
                ConnectionDocumentDigest(sha256(payload)),
            )
        }

        private fun canonicalConnection(connection: ConnectionFact): ConnectionFact = connection.copy(
            endpoints = connection.endpoints.sortedWith(endpointComparator),
            specification = ResolvedConnectionSpecification(
                connection.specification.properties.sortedBy { it.name },
            ),
        )

        private fun canonicalNet(net: NetFact): NetFact = net.copy(
            endpoints = net.endpoints.sortedWith(endpointComparator),
            specification = ResolvedConnectionSpecification(net.specification.properties.sortedBy { it.name }),
            potentialOrSignal = net.potentialOrSignal?.toList(),
        )

        private fun canonicalOperator(operator: TopologyOperator): TopologyOperator = operator.copy(
            orderedEndpointIds = operator.orderedEndpointIds.toList(),
        )

        private val endpointComparator = compareBy<ConnectionEndpointFact>({ roleOrder(it.role) }, { it.authoredPath.joinToString(".") }, { it.portId.value })

        private fun roleOrder(role: ConnectionEndpointRole): Int = when (role) {
            ConnectionEndpointRole.SOURCE -> 0
            ConnectionEndpointRole.SINK -> 1
            ConnectionEndpointRole.PASS -> 2
        }

        private fun appendConnection(builder: StringBuilder, connection: ConnectionFact) {
            builder.append("C|").append(connection.id.value).append('|').append(connection.kind.name).append('|')
            connection.endpoints.forEach { appendEndpoint(builder, it) }
            connection.specification.properties.forEach { appendProperty(builder, it) }
            builder.append(';')
        }

        private fun appendNet(builder: StringBuilder, net: NetFact) {
            builder.append("N|").append(net.id.value).append('|').append(net.name).append('|').append(net.kind.name).append('|')
            net.potentialOrSignal?.joinTo(builder, ".")
            builder.append('|')
            net.endpoints.forEach { appendEndpoint(builder, it) }
            net.specification.properties.forEach { appendProperty(builder, it) }
            builder.append(';')
        }

        private fun appendOperator(builder: StringBuilder, operator: TopologyOperator) {
            builder.append("O|").append(operator.id.value).append('|').append(operator.kind.name).append('|')
                .append(operator.netId.value).append('|')
            operator.orderedEndpointIds.forEach { builder.append(it.value).append(',') }
            builder.append(';')
        }

        private fun appendEndpoint(builder: StringBuilder, endpoint: ConnectionEndpointFact) {
            builder.append(endpoint.portId.value).append(':').append(endpoint.role.name).append(':')
                .append(endpoint.authoredPath.joinToString(".")).append(',')
        }

        private fun appendProperty(builder: StringBuilder, property: EngineeringProperty) {
            builder.append(property.name).append('=').append(property.value.toString()).append(',')
        }

        private fun sha256(value: String): String {
            val digest = java.security.MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { byte -> "%02x".format(byte) }
        }
    }
}

fun EngineeringNet.toNetFact(): NetFact = NetFact(
    id = id,
    name = name,
    kind = kind,
    endpoints = endpoints.map { endpoint ->
        ConnectionEndpointFact(endpoint.port.resolvedIdentity ?: StableSemanticIdentity("unresolved:${endpoint.port.authoredPath.joinToString(".")}"), endpoint.port.authoredPath, endpoint.role, ConnectionSourceTrace(endpoint.provenance))
    },
    potentialOrSignal = potentialOrSignal?.authoredPath,
    specification = ResolvedConnectionSpecification(effectiveProperties),
    trace = ConnectionSourceTrace(provenance),
)
