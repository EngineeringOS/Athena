package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.ConnectionDocument
import com.engineeringood.athena.connection.ConnectionEndpointFact
import com.engineeringood.athena.connection.ConnectionFact
import com.engineeringood.athena.connection.ConnectionSourceTrace
import com.engineeringood.athena.connection.NetFact
import com.engineeringood.athena.connection.ResolvedConnectionSpecification
import com.engineeringood.athena.connection.TopologyOperator
import com.engineeringood.athena.connection.TopologyOperatorKind
import com.engineeringood.athena.ir.ConnectionEndpoint
import com.engineeringood.athena.ir.ConnectionEndpointRole
import com.engineeringood.athena.ir.ConnectionKind
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.semantics.core.SemanticValidationResult

sealed interface ConnectionIrCompilation {
    data class Success(val document: ConnectionDocument) : ConnectionIrCompilation
    data class Failure(val diagnostics: List<String>) : ConnectionIrCompilation
}

/** Compiles validated Engineering Reality into one canonical renderer-neutral connection document. */
class ConnectionIrCompiler {
    fun compile(
        engineering: EngineeringDocument,
        semanticResult: SemanticValidationResult,
    ): ConnectionIrCompilation {
        if (!semanticResult.isSemanticallyValid) {
            return ConnectionIrCompilation.Failure(semanticResult.diagnostics.map { it.message }.distinct().sorted())
        }
        val diagnostics = validate(engineering)
        if (diagnostics.isNotEmpty()) return ConnectionIrCompilation.Failure(diagnostics.sorted())

        val connections = engineering.connections.map { connection ->
            ConnectionFact(
                id = connection.id,
                kind = connection.kind,
                endpoints = connection.endpoints.map(::endpointFact),
                specification = ResolvedConnectionSpecification(connection.effectiveProperties),
                trace = ConnectionSourceTrace(connection.provenance),
            )
        }
        val nets = engineering.nets.map { net ->
            NetFact(
                id = net.id,
                name = net.name,
                kind = net.kind,
                endpoints = net.endpoints.map(::endpointFact),
                potentialOrSignal = net.potentialOrSignal?.authoredPath,
                specification = ResolvedConnectionSpecification(net.effectiveProperties),
                trace = ConnectionSourceTrace(net.provenance),
            )
        }
        val topology = engineering.nets.flatMap { net ->
            deriveTopology(
                net.id,
                net.kind,
                net.endpoints,
                ConnectionSourceTrace(net.provenance),
            )
        }
        return ConnectionIrCompilation.Success(ConnectionDocument.canonical(connections, nets, topology))
    }

    private fun validate(engineering: EngineeringDocument): List<String> = buildList {
        val portIds = engineering.ports.map { it.id }.toSet()
        (engineering.connections.flatMap { it.endpoints } + engineering.nets.flatMap { it.endpoints }).forEach { endpoint ->
            val resolved = endpoint.port.resolvedIdentity
            if (resolved == null || resolved !in portIds) {
                val path = endpoint.port.authoredPath.joinToString(".")
                add("Connection endpoint `$path` is unresolved. Declare that Engineering Port before publishing Connection IR.")
            }
        }
        engineering.nets.filter { it.endpoints.size < 2 }.forEach { net ->
            add("Net `${net.name}` requires at least two distinct Engineering Ports before publishing Connection IR.")
        }
    }

    private fun endpointFact(endpoint: ConnectionEndpoint): ConnectionEndpointFact = ConnectionEndpointFact(
        portId = requireNotNull(endpoint.port.resolvedIdentity),
        authoredPath = endpoint.port.authoredPath,
        role = endpoint.role,
        trace = ConnectionSourceTrace(endpoint.provenance),
    )

    private fun deriveTopology(
        netId: StableSemanticIdentity,
        kind: ConnectionKind,
        endpoints: List<ConnectionEndpoint>,
        trace: ConnectionSourceTrace,
    ): List<TopologyOperator> = buildList {
        val orderedIds = endpoints.map { requireNotNull(it.port.resolvedIdentity) }
        if (endpoints.size > 2) {
            val kindForMultiplicity = if (endpoints.count { it.role == ConnectionEndpointRole.SOURCE } > 1) {
                TopologyOperatorKind.MERGE
            } else {
                TopologyOperatorKind.BRANCH
            }
            add(operator(netId, kindForMultiplicity, orderedIds, trace))
        }
        endpoints.filter { it.role == ConnectionEndpointRole.PASS }.forEachIndexed { index, endpoint ->
            add(operator(netId, TopologyOperatorKind.PASS_THROUGH, listOf(requireNotNull(endpoint.port.resolvedIdentity)), trace, index + 1))
        }
        if (kind == ConnectionKind.JUMPER) {
            add(operator(netId, TopologyOperatorKind.INTERRUPTION, orderedIds, trace))
        }
    }

    private fun operator(
        netId: StableSemanticIdentity,
        kind: TopologyOperatorKind,
        endpoints: List<StableSemanticIdentity>,
        trace: ConnectionSourceTrace,
        ordinal: Int = 1,
    ): TopologyOperator = TopologyOperator(
        id = StableSemanticIdentity("operator:${netId.value}:${kind.name.lowercase()}:$ordinal"),
        kind = kind,
        netId = netId,
        orderedEndpointIds = endpoints,
        trace = trace,
    )
}
