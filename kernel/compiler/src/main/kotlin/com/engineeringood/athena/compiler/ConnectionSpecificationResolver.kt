package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.ConnectionSpecificationScope
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringConnectionSpecification
import com.engineeringood.athena.ir.EngineeringNet
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringValue

internal data class ConnectionSpecificationResolution(
    val netProperties: Map<String, List<EngineeringProperty>>,
    val connectionProperties: Map<String, List<EngineeringProperty>>,
    val invalidSubjects: Set<String>,
)

/** Resolves authored connection facts once, with fixed project-to-connection precedence. */
internal object ConnectionSpecificationResolver {
    fun resolve(
        specifications: List<EngineeringConnectionSpecification>,
        nets: List<EngineeringNet>,
        connections: List<EngineeringConnection>,
    ): ConnectionSpecificationResolution {
        val netResults = nets.associate { net -> net.id.value to resolveFor(net, specifications) }
        val connectionResults = connections.associate { connection -> connection.id.value to resolveFor(connection, specifications) }
        return ConnectionSpecificationResolution(
            netProperties = netResults.mapValues { it.value.properties },
            connectionProperties = connectionResults.mapValues { it.value.properties },
            invalidSubjects = (netResults + connectionResults).filterValues { it.invalid }.keys,
        )
    }

    private data class Result(val properties: List<EngineeringProperty>, val invalid: Boolean)

    private fun resolveFor(target: EngineeringNet, specifications: List<EngineeringConnectionSpecification>): Result {
        val matching = specifications.filter { specification ->
            when (specification.scope) {
                ConnectionSpecificationScope.PROJECT -> true
                ConnectionSpecificationScope.POTENTIAL,
                ConnectionSpecificationScope.SIGNAL -> specification.subject?.authoredPath == target.potentialOrSignal?.authoredPath
                ConnectionSpecificationScope.NET -> specification.subject?.authoredPath == listOf(target.name)
                ConnectionSpecificationScope.CONNECTION -> false
            }
        }
        return merge(target.properties, matching)
    }

    private fun resolveFor(target: EngineeringConnection, specifications: List<EngineeringConnectionSpecification>): Result {
        val endpointPaths = target.endpoints.map { it.port.authoredPath }
        val matching = specifications.filter { specification ->
            when (specification.scope) {
                ConnectionSpecificationScope.PROJECT -> true
                ConnectionSpecificationScope.CONNECTION -> specification.subject?.authoredPath?.let { it in endpointPaths } == true
                else -> false
            }
        }
        return merge(target.properties, matching)
    }

    private fun merge(
        direct: List<EngineeringProperty>,
        specifications: List<EngineeringConnectionSpecification>,
    ): Result {
        val byScope = specifications.groupBy { precedence(it.scope) }
        val merged = linkedMapOf<String, EngineeringProperty>()
        var invalid = false
        byScope.toSortedMap().forEach { (_, scoped) ->
            scoped.flatMap { it.properties }.groupBy { it.name }.forEach { (name, values) ->
                val distinct = values.map(::valueKey).distinct()
                if (distinct.size > 1) invalid = true
                if (values.isNotEmpty()) merged[name] = values.last()
            }
        }
        direct.forEach { merged[it.name] = it }
        return Result(merged.values.toList(), invalid)
    }

    private fun precedence(scope: ConnectionSpecificationScope): Int = when (scope) {
        ConnectionSpecificationScope.PROJECT -> 0
        ConnectionSpecificationScope.POTENTIAL, ConnectionSpecificationScope.SIGNAL -> 1
        ConnectionSpecificationScope.NET -> 2
        ConnectionSpecificationScope.CONNECTION -> 3
    }

    private fun valueKey(property: EngineeringProperty): String = when (val value = property.value) {
        is EngineeringValue.Quantity -> "quantity:${value.value}:${value.unit.authoredName.joinToString(".")}"
        is EngineeringValue.Integer -> "integer:${value.value}"
        is EngineeringValue.Boolean -> "boolean:${value.value}"
        is EngineeringValue.Text -> "text:${value.text}"
        is EngineeringValue.Symbol -> "symbol:${value.text}"
        is EngineeringValue.Reference -> "reference:${value.reference.authoredPath.joinToString(".")}"
    }
}
