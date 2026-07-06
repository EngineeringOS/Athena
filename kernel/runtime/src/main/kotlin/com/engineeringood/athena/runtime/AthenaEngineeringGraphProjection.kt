package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.EngineeringReference

/**
 * Runtime-owned result for projecting the active project into a queryable engineering graph.
 */
sealed interface AthenaEngineeringGraphProjection {
    /**
     * Runtime project name associated with the projection request.
     */
    val projectName: String
}

/**
 * Successful runtime graph projection derived directly from canonical compiler output.
 */
data class AthenaEngineeringGraphReadyProjection(
    override val projectName: String,
    val graph: AthenaEngineeringGraph,
) : AthenaEngineeringGraphProjection

/**
 * Graph projection that could not be derived because the active source did not parse.
 */
data class AthenaEngineeringGraphUnavailableProjection(
    override val projectName: String,
    val reason: String,
) : AthenaEngineeringGraphProjection

/**
 * Runtime-owned graph projection over the active project's canonical semantic document.
 */
class AthenaEngineeringGraph(
    val systemSemanticId: String,
    val nodes: List<AthenaEngineeringGraphNode>,
    val relationships: List<AthenaEngineeringGraphRelationship>,
) {
    private val nodesById: Map<String, AthenaEngineeringGraphNode> = nodes.associateBy { it.semanticId }
    private val outgoingRelationshipsBySource: Map<String, List<AthenaEngineeringGraphRelationship>> =
        relationships.groupBy { it.sourceSemanticId }
    private val incomingRelationshipsByTarget: Map<String, List<AthenaEngineeringGraphRelationship>> =
        relationships.groupBy { it.targetSemanticId }

    /**
     * Looks up one graph node by its canonical semantic identity.
     */
    fun node(semanticId: String): AthenaEngineeringGraphNode? = nodesById[semanticId]

    /**
     * Returns all graph nodes with the requested runtime-facing kind.
     */
    fun nodesOfKind(kind: AthenaEngineeringGraphNodeKind): List<AthenaEngineeringGraphNode> {
        return nodes.filter { node -> node.kind == kind }
    }

    /**
     * Returns runtime graph relationships that originate from [semanticId].
     */
    fun relationshipsFrom(semanticId: String): List<AthenaEngineeringGraphRelationship> {
        return outgoingRelationshipsBySource[semanticId].orEmpty()
    }

    /**
     * Returns runtime graph relationships that point to [semanticId].
     */
    fun relationshipsTo(semanticId: String): List<AthenaEngineeringGraphRelationship> {
        return incomingRelationshipsByTarget[semanticId].orEmpty()
    }

    /**
     * Returns all runtime graph relationships with the requested kind.
     */
    fun relationshipsOfKind(kind: AthenaEngineeringGraphRelationshipKind): List<AthenaEngineeringGraphRelationship> {
        return relationships.filter { relationship -> relationship.kind == kind }
    }

    /**
     * Returns authored references carried by the identified semantic node.
     */
    fun referencesOf(semanticId: String): List<AthenaEngineeringGraphReference> {
        return node(semanticId)?.references.orEmpty()
    }

    /**
     * Resolves graph nodes referenced by the identified semantic node.
     */
    fun referencedNodes(semanticId: String): List<AthenaEngineeringGraphNode> {
        return referencesOf(semanticId)
            .mapNotNull { reference -> reference.resolvedSemanticId?.let(nodesById::get) }
    }

    /**
     * Returns direct runtime dependencies reachable from the identified semantic node.
     */
    fun dependenciesOf(semanticId: String): List<AthenaEngineeringGraphNode> {
        return relationshipsFrom(semanticId)
            .mapNotNull { relationship -> nodesById[relationship.targetSemanticId] }
    }

    /**
     * Returns relationships directly affected by the identified semantic node.
     */
    fun affectedRelationships(semanticId: String): List<AthenaEngineeringGraphRelationship> {
        return (relationshipsFrom(semanticId) + relationshipsTo(semanticId))
            .distinct()
    }

    /**
     * Returns all directly adjacent nodes regardless of relationship direction.
     */
    fun neighbors(semanticId: String): List<AthenaEngineeringGraphNode> {
        val neighborIds = buildList {
            relationshipsFrom(semanticId).forEach { add(it.targetSemanticId) }
            relationshipsTo(semanticId).forEach { add(it.sourceSemanticId) }
        }

        return neighborIds.distinct().mapNotNull(nodesById::get)
    }
}

/**
 * One semantic node exposed by the runtime graph projection.
 */
data class AthenaEngineeringGraphNode(
    val semanticId: String,
    val kind: AthenaEngineeringGraphNodeKind,
    val displayName: String,
    val properties: List<AthenaEngineeringGraphProperty> = emptyList(),
    val references: List<AthenaEngineeringGraphReference> = emptyList(),
)

/**
 * Runtime-facing semantic categories exposed through the engineering graph.
 */
enum class AthenaEngineeringGraphNodeKind {
    SYSTEM,
    COMPONENT,
    PORT,
    CONNECTION,
}

/**
 * One typed graph property copied from canonical semantic state.
 */
data class AthenaEngineeringGraphProperty(
    val name: String,
    val value: String,
)

/**
 * One authored reference carried by a semantic node in the runtime graph.
 */
data class AthenaEngineeringGraphReference(
    val kind: AthenaEngineeringGraphReferenceKind,
    val authoredPath: List<String>,
    val resolvedSemanticId: String?,
)

/**
 * Runtime-facing reference roles that preserve authored intent without inventing new semantic ownership.
 */
enum class AthenaEngineeringGraphReferenceKind {
    OWNER,
    CONNECTION_SOURCE,
    CONNECTION_TARGET,
}

/**
 * Directed relationship between two existing canonical semantic identities in the runtime graph.
 */
data class AthenaEngineeringGraphRelationship(
    val kind: AthenaEngineeringGraphRelationshipKind,
    val sourceSemanticId: String,
    val targetSemanticId: String,
)

/**
 * Runtime-facing relationship kinds used by the engineering graph projection.
 */
enum class AthenaEngineeringGraphRelationshipKind {
    SYSTEM_CONTAINS_COMPONENT,
    COMPONENT_OWNS_PORT,
    CONNECTION_REFERENCE,
}

/**
 * Derives a runtime-owned engineering graph projection from one active-project compilation result.
 */
internal fun CompilerCompilationResult.toEngineeringGraphProjection(
    projectName: String,
): AthenaEngineeringGraphProjection {
    return when (val compilation = this) {
        is CompilerCompilationParseFailure -> AthenaEngineeringGraphUnavailableProjection(
            projectName = projectName,
            reason = compilation.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
        )

        is CompilerCompilationSuccess -> AthenaEngineeringGraphReadyProjection(
            projectName = projectName,
            graph = compilation.toEngineeringGraph(),
        )
    }
}

/**
 * Builds the runtime graph projection directly from canonical IR identities and references.
 */
private fun CompilerCompilationSuccess.toEngineeringGraph(): AthenaEngineeringGraph {
    val nodes = buildList {
        add(
            AthenaEngineeringGraphNode(
                semanticId = document.system.id.value,
                kind = AthenaEngineeringGraphNodeKind.SYSTEM,
                displayName = document.system.name,
            ),
        )

        document.components.forEach { component ->
            add(
                AthenaEngineeringGraphNode(
                    semanticId = component.id.value,
                    kind = AthenaEngineeringGraphNodeKind.COMPONENT,
                    displayName = component.name,
                    properties = component.properties.toGraphProperties(),
                ),
            )
        }

        document.ports.forEach { port ->
            add(
                AthenaEngineeringGraphNode(
                    semanticId = port.id.value,
                    kind = AthenaEngineeringGraphNodeKind.PORT,
                    displayName = port.name,
                    properties = port.properties.toGraphProperties(),
                    references = listOf(
                        port.ownerReference.toGraphReference(AthenaEngineeringGraphReferenceKind.OWNER),
                    ),
                ),
            )
        }

        document.connections.forEach { connection ->
            add(
                AthenaEngineeringGraphNode(
                    semanticId = connection.id.value,
                    kind = AthenaEngineeringGraphNodeKind.CONNECTION,
                    displayName = "${connection.from.authoredPath.joinToString(".")} -> ${connection.to.authoredPath.joinToString(".")}",
                    references = listOf(
                        connection.from.toGraphReference(AthenaEngineeringGraphReferenceKind.CONNECTION_SOURCE),
                        connection.to.toGraphReference(AthenaEngineeringGraphReferenceKind.CONNECTION_TARGET),
                    ),
                ),
            )
        }
    }

    val relationships = buildList {
        document.components.forEach { component ->
            add(
                AthenaEngineeringGraphRelationship(
                    kind = AthenaEngineeringGraphRelationshipKind.SYSTEM_CONTAINS_COMPONENT,
                    sourceSemanticId = document.system.id.value,
                    targetSemanticId = component.id.value,
                ),
            )
        }

        document.ports.forEach { port ->
            port.ownerReference.resolvedIdentity?.let { ownerIdentity ->
                add(
                    AthenaEngineeringGraphRelationship(
                        kind = AthenaEngineeringGraphRelationshipKind.COMPONENT_OWNS_PORT,
                        sourceSemanticId = ownerIdentity.value,
                        targetSemanticId = port.id.value,
                    ),
                )
            }
        }

        document.connections.forEach { connection ->
            connection.from.resolvedIdentity?.let { sourceIdentity ->
                add(
                    AthenaEngineeringGraphRelationship(
                        kind = AthenaEngineeringGraphRelationshipKind.CONNECTION_REFERENCE,
                        sourceSemanticId = connection.id.value,
                        targetSemanticId = sourceIdentity.value,
                    ),
                )
            }

            connection.to.resolvedIdentity?.let { targetIdentity ->
                add(
                    AthenaEngineeringGraphRelationship(
                        kind = AthenaEngineeringGraphRelationshipKind.CONNECTION_REFERENCE,
                        sourceSemanticId = connection.id.value,
                        targetSemanticId = targetIdentity.value,
                    ),
                )
            }
        }
    }

    return AthenaEngineeringGraph(
        systemSemanticId = document.system.id.value,
        nodes = nodes,
        relationships = relationships,
    )
}

/**
 * Converts canonical authored properties into the runtime graph property surface.
 */
private fun List<EngineeringProperty>.toGraphProperties(): List<AthenaEngineeringGraphProperty> {
    return map { property ->
        AthenaEngineeringGraphProperty(
            name = property.name,
            value = when (val value = property.value) {
                is EngineeringPropertyValue.Symbol -> value.text
                is EngineeringPropertyValue.Text -> value.text
            },
        )
    }
}

/**
 * Converts one canonical authored reference into the runtime graph reference surface.
 */
private fun EngineeringReference.toGraphReference(kind: AthenaEngineeringGraphReferenceKind): AthenaEngineeringGraphReference {
    return AthenaEngineeringGraphReference(
        kind = kind,
        authoredPath = authoredPath,
        resolvedSemanticId = resolvedIdentity?.value,
    )
}
