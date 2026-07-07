package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.plugin.AthenaApprovedPluginInventory
import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringSystem
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.plugin.AthenaDomainLoweringContribution

/** Lowers the syntax-only AST into the first canonical Engineering IR document. */
class EngineeringIrLowerer(
    private val domainSemantics: AthenaDomainSemanticsCoordinator = AthenaDomainSemanticsCoordinator(
        AthenaApprovedPluginInventory.EMPTY,
    ),
) {
    /** Lowers [source] deterministically into the canonical semantic document used by later compiler passes. */
    fun lower(source: CompilerSourceDocument): EngineeringDocument {
        val contribution = domainSemantics.lower(source)

        val components = contribution.components.withDuplicateOrdinals { it.name }.map { (blueprint, duplicateOrdinal) ->
            EngineeringComponent(
                id = componentIdentity(blueprint.name, duplicateOrdinal),
                name = blueprint.name,
                kind = blueprint.kind,
                properties = blueprint.properties,
                provenance = blueprint.provenance,
            )
        }
        val componentIdsByAuthoredPath = components.uniqueResolutionMap(keySelector = { it.name }, idSelector = { it.id })

        val ports = contribution.ports.withDuplicateOrdinals { pathKey(it.ownerPath + it.name) }.map { (blueprint, duplicateOrdinal) ->
            EngineeringPort(
                id = portIdentity(blueprint.ownerPath + blueprint.name, duplicateOrdinal),
                ownerReference = EngineeringReference(
                    authoredPath = blueprint.ownerPath,
                    resolvedIdentity = componentIdsByAuthoredPath[pathKey(blueprint.ownerPath)],
                    provenance = blueprint.ownerProvenance,
                ),
                name = blueprint.name,
                properties = blueprint.properties,
                provenance = blueprint.provenance,
            )
        }
        val portIdsByAuthoredPath = ports.uniqueResolutionMap(
            keySelector = { pathKey(it.ownerReference.authoredPath + it.name) },
            idSelector = { it.id },
        )

        val connections = contribution.connections.withDuplicateOrdinals {
            "${pathKey(it.fromPath)}->${pathKey(it.toPath)}"
        }.map { (blueprint, duplicateOrdinal) ->
            EngineeringConnection(
                id = connectionIdentity(blueprint.fromPath, blueprint.toPath, duplicateOrdinal),
                from = EngineeringReference(
                    authoredPath = blueprint.fromPath,
                    resolvedIdentity = portIdsByAuthoredPath[pathKey(blueprint.fromPath)],
                    provenance = blueprint.fromProvenance,
                ),
                to = EngineeringReference(
                    authoredPath = blueprint.toPath,
                    resolvedIdentity = portIdsByAuthoredPath[pathKey(blueprint.toPath)],
                    provenance = blueprint.toProvenance,
                ),
                provenance = blueprint.provenance,
            )
        }

        return EngineeringDocument(
            system = EngineeringSystem(
                id = systemIdentity(source.ast.system.name),
                name = source.ast.system.name,
                provenance = source.ast.system.span.toProvenance(source.file),
            ),
            components = components,
            ports = ports,
            connections = connections,
        )
    }

    private fun systemIdentity(name: String): StableSemanticIdentity = StableSemanticIdentity("system:$name")

    private fun componentIdentity(name: String, duplicateOrdinal: Int): StableSemanticIdentity {
        return StableSemanticIdentity(withDuplicateSuffix("component:$name", duplicateOrdinal))
    }

    private fun portIdentity(path: List<String>, duplicateOrdinal: Int): StableSemanticIdentity {
        return StableSemanticIdentity(withDuplicateSuffix("port:${pathKey(path)}", duplicateOrdinal))
    }

    private fun connectionIdentity(from: List<String>, to: List<String>, duplicateOrdinal: Int): StableSemanticIdentity {
        return StableSemanticIdentity(
            withDuplicateSuffix("connection:${pathKey(from)}->${pathKey(to)}", duplicateOrdinal),
        )
    }

    private fun pathKey(parts: List<String>): String = parts.joinToString(".")

    private fun withDuplicateSuffix(baseIdentity: String, duplicateOrdinal: Int): String {
        return if (duplicateOrdinal == 1) baseIdentity else "$baseIdentity#$duplicateOrdinal"
    }
}

/** Converts a syntax-layer span into stable provenance carried by canonical semantic objects. */
private fun SourceSpan.toProvenance(file: String): SourceProvenance {
    return SourceProvenance(
        file = file,
        startLine = start.line,
        startColumn = start.column,
        endLine = end.line,
        endColumn = end.column,
    )
}

/** Tags authored declarations deterministically when duplicate semantic keys occur in one source. */
private fun <T> List<T>.withDuplicateOrdinals(keySelector: (T) -> String): List<Pair<T, Int>> {
    val countsByKey = mutableMapOf<String, Int>()
    return map { value ->
        val key = keySelector(value)
        val duplicateOrdinal = countsByKey.getOrDefault(key, 0) + 1
        countsByKey[key] = duplicateOrdinal
        value to duplicateOrdinal
    }
}

/** Resolves authored paths only when they map to a single canonical semantic identity. */
private fun <T> List<T>.uniqueResolutionMap(
    keySelector: (T) -> String,
    idSelector: (T) -> StableSemanticIdentity,
): Map<String, StableSemanticIdentity> {
    return groupBy(keySelector)
        .mapNotNull { (key, values) ->
            values.singleOrNull()?.let { key to idSelector(it) }
        }
        .toMap()
}
