package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringValue

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
    ENTITY,
    PORT,
    RELATIONSHIP,
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
    RELATIONSHIP_PARTICIPANT,
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
    SYSTEM_CONTAINS_ENTITY,
    ENTITY_OWNS_PORT,
    RELATIONSHIP_PARTICIPANT,
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

        document.entities.forEach { entity ->
            add(
                AthenaEngineeringGraphNode(
                    semanticId = entity.id.value,
                    kind = AthenaEngineeringGraphNodeKind.ENTITY,
                    displayName = entity.name,
                    properties = entity.properties.toGraphProperties(),
                ),
            )
        }

        document.ports.forEach { port ->
            add(
                AthenaEngineeringGraphNode(
                    semanticId = port.id.value,
                    kind = AthenaEngineeringGraphNodeKind.PORT,
                    displayName = port.name,
                    properties = listOf(
                        AthenaEngineeringGraphProperty("direction", port.direction.name.lowercase()),
                        AthenaEngineeringGraphProperty(
                            "flows",
                            port.admittedFlowReferences.joinToString(",") { flow -> flow.authoredName.joinToString(".") },
                        ),
                    ) + port.properties.toGraphProperties(),
                    references = listOf(
                        port.owner.reference.toGraphReference(AthenaEngineeringGraphReferenceKind.OWNER),
                    ),
                ),
            )
        }

        document.relationships.forEach { relationship ->
            add(
                AthenaEngineeringGraphNode(
                    semanticId = relationship.id.value,
                    kind = AthenaEngineeringGraphNodeKind.RELATIONSHIP,
                    displayName = relationship.definitionReference.authoredName.joinToString("."),
                    properties = relationship.properties.toGraphProperties(),
                    references = relationship.participants.map { participant ->
                        participant.subject.reference.toGraphReference(
                            AthenaEngineeringGraphReferenceKind.RELATIONSHIP_PARTICIPANT,
                        )
                    },
                ),
            )
        }

    }

    val relationships = buildList {
        document.entities.forEach { entity ->
            add(
                AthenaEngineeringGraphRelationship(
                    kind = AthenaEngineeringGraphRelationshipKind.SYSTEM_CONTAINS_ENTITY,
                    sourceSemanticId = document.system.id.value,
                    targetSemanticId = entity.id.value,
                ),
            )
        }

        document.ports.forEach { port ->
            port.owner.reference.resolvedIdentity?.let { ownerIdentity ->
                add(
                    AthenaEngineeringGraphRelationship(
                        kind = AthenaEngineeringGraphRelationshipKind.ENTITY_OWNS_PORT,
                        sourceSemanticId = ownerIdentity.value,
                        targetSemanticId = port.id.value,
                    ),
                )
            }
        }

        document.relationships.forEach { relationship ->
            relationship.participants.forEach { participant ->
                participant.subject.reference.resolvedIdentity?.let { subjectIdentity ->
                    add(
                        AthenaEngineeringGraphRelationship(
                            kind = AthenaEngineeringGraphRelationshipKind.RELATIONSHIP_PARTICIPANT,
                            sourceSemanticId = relationship.id.value,
                            targetSemanticId = subjectIdentity.value,
                        ),
                    )
                }
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
                is EngineeringValue.Quantity -> "${value.value} ${value.unit.authoredName.joinToString(".")}"
                is EngineeringValue.Integer -> value.value.toString()
                is EngineeringValue.Boolean -> value.value.toString()
                is EngineeringValue.Text -> value.text
                is EngineeringValue.Symbol -> value.text
                is EngineeringValue.Reference -> value.reference.authoredPath.joinToString(".")
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
