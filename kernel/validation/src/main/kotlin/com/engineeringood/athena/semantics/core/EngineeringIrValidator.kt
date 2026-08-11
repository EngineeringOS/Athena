package com.engineeringood.athena.semantics.core

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringEntity
import com.engineeringood.athena.ir.EngineeringFunction
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringPortOwner
import com.engineeringood.athena.ir.EngineeringPortDirection
import com.engineeringood.athena.ir.ConnectionEndpointRole
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringNet
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringRelationship
import com.engineeringood.athena.ir.EngineeringSubjectReference
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity

/** Deterministic semantic validation pass over canonical Engineering IR for the current M0 slice. */
class EngineeringIrValidator {
    /** Validates [document] and emits provenance-rich diagnostics without mutating the canonical IR. */
    fun validate(
        document: EngineeringDocument,
        scope: EngineeringIrValidationScope? = null,
    ): SemanticValidationResult {
        val entitiesByName = document.entities.groupBy { it.name }
        val functionsByPath = document.functions.groupBy { function -> authoredFunctionPath(function) }
        val portsByPath = document.ports.groupBy { authoredPortPath(it) }
        val diagnostics = buildList {
            addAll(duplicateEntityDiagnostics(document.entities, scope))
            addAll(portOwnerDiagnostics(document.ports, entitiesByName, functionsByPath, scope))
            addAll(duplicatePortDiagnostics(document.ports, scope))
            addAll(relationshipParticipantDiagnostics(document.relationships, portsByPath, functionsByPath, entitiesByName, scope))
            addAll(duplicateRelationshipDiagnostics(document.relationships, scope))
            addAll(functionOwnerDiagnostics(document.functions, entitiesByName, scope))
            addAll(duplicateFunctionDiagnostics(document.functions, scope))
            addAll(connectionEndpointDiagnostics(document.connections, document.ports, scope))
            addAll(duplicateConnectionDiagnostics(document.connections, scope))
            addAll(netEndpointDiagnostics(document.nets, document.ports, scope))
            addAll(duplicateNetDiagnostics(document.nets, scope))
        }

        return SemanticValidationResult(
            diagnostics = diagnostics,
            continuationDecision = if (diagnostics.any { it.severity == SemanticDiagnosticSeverity.ERROR }) {
                SemanticContinuationDecision.STOP_DOWNSTREAM
            } else {
                SemanticContinuationDecision.CONTINUE
            },
        )
    }

    private fun duplicateEntityDiagnostics(
        entities: List<EngineeringEntity>,
        scope: EngineeringIrValidationScope?,
    ): List<SemanticDiagnostic> {
        return entities
            .groupBy { it.name }
            .values
            .filter { duplicates -> scope == null || duplicates.any { entity -> scope.includes(entity.id.value) } }
            .filter { it.size > 1 }
            .flatMap { duplicates ->
                duplicates.map { entity ->
                    errorDiagnostic(
                        ruleId = "uniqueness.entity.duplicate-authored-key",
                        category = SemanticDiagnosticCategory.UNIQUENESS,
                        subjectIdentity = entity.id,
                        provenance = entity.provenance,
                        message = "Duplicate Entity authored key `${entity.name}` is not semantically unique.",
                    )
                }
            }
    }

    private fun portOwnerDiagnostics(
        ports: List<EngineeringPort>,
        entitiesByName: Map<String, List<EngineeringEntity>>,
        functionsByPath: Map<String, List<EngineeringFunction>>,
        scope: EngineeringIrValidationScope?,
    ): List<SemanticDiagnostic> {
        return ports
            .asSequence()
            .filter { port -> scope == null || scope.includes(port.id.value) }
            .mapNotNull { port ->
                val ownerReference = port.owner.reference()
                val candidateCount = when (port.owner) {
                    is EngineeringPortOwner.Entity -> entitiesByName[authoredPath(ownerReference)]?.size ?: 0
                    is EngineeringPortOwner.Function -> functionsByPath[authoredPath(ownerReference)]?.size ?: 0
                }
                classifyReference(
                    reference = ownerReference,
                    candidateCount = candidateCount,
                )?.let { classification ->
                    errorDiagnostic(
                        ruleId = classification.ruleId,
                        category = SemanticDiagnosticCategory.REFERENCE,
                        subjectIdentity = port.id,
                        provenance = ownerReference.provenance,
                        message = "Port owner `${authoredPath(ownerReference)}` ${classification.messageFragment}.",
                    )
                }
            }
            .toList()
    }

    private fun duplicatePortDiagnostics(
        ports: List<EngineeringPort>,
        scope: EngineeringIrValidationScope?,
    ): List<SemanticDiagnostic> {
        return ports
            .groupBy { authoredPortPath(it) }
            .values
            .filter { duplicates -> scope == null || duplicates.any { port -> scope.includes(port.id.value) } }
            .filter { it.size > 1 }
            .flatMap { duplicates ->
                duplicates.map { port ->
                    errorDiagnostic(
                        ruleId = "uniqueness.port.duplicate-authored-key",
                        category = SemanticDiagnosticCategory.UNIQUENESS,
                        subjectIdentity = port.id,
                        provenance = port.provenance,
                        message = "Duplicate port authored key `${authoredPortPath(port)}` is not semantically unique.",
                    )
                }
            }
    }

    private fun relationshipParticipantDiagnostics(
        relationships: List<EngineeringRelationship>,
        portsByPath: Map<String, List<EngineeringPort>>,
        functionsByPath: Map<String, List<EngineeringFunction>>,
        entitiesByName: Map<String, List<EngineeringEntity>>,
        scope: EngineeringIrValidationScope?,
    ): List<SemanticDiagnostic> = buildList {
        relationships.asSequence()
            .filter { relationship -> scope == null || scope.includes(relationship.id.value) }
            .forEach { relationship ->
                relationship.participants.forEach { participant ->
                    val reference = participant.subject.reference
                    val candidates = when (participant.subject) {
                        is EngineeringSubjectReference.Port -> portsByPath[authoredPath(reference)]?.size ?: 0
                        is EngineeringSubjectReference.Function -> functionsByPath[authoredPath(reference)]?.size ?: 0
                        is EngineeringSubjectReference.Entity -> entitiesByName[authoredPath(reference)]?.size ?: 0
                        is EngineeringSubjectReference.Unresolved -> 0
                    }
                    classifyReference(reference, candidates)?.let { classification ->
                        add(errorDiagnostic(
                            ruleId = "reference.relationship-participant." + classification.name.lowercase(),
                            category = SemanticDiagnosticCategory.REFERENCE,
                            subjectIdentity = relationship.id,
                            provenance = reference.provenance,
                            message = "Relationship role " + participant.role + " subject " + authoredPath(reference) + " " + classification.messageFragment + ".",
                        ))
                    }
                }
            }
    }

    private fun duplicateRelationshipDiagnostics(
        relationships: List<EngineeringRelationship>,
        scope: EngineeringIrValidationScope?,
    ): List<SemanticDiagnostic> = relationships
        .groupBy { relationship -> relationship.participants.joinToString("|") { participant -> participant.role + ":" + authoredPath(participant.subject.reference) } }
        .values
        .filter { duplicates -> duplicates.size > 1 }
        .filter { duplicates -> scope == null || duplicates.any { relationship -> scope.includes(relationship.id.value) } }
        .flatMap { duplicates -> duplicates.map { relationship ->
            errorDiagnostic(
                ruleId = "uniqueness.relationship.duplicate-authored-key",
                category = SemanticDiagnosticCategory.UNIQUENESS,
                subjectIdentity = relationship.id,
                provenance = relationship.provenance,
                message = "Duplicate Relationship participant key is not semantically unique.",
            )
        } }

    private fun functionOwnerDiagnostics(
        functions: List<EngineeringFunction>,
        entitiesByName: Map<String, List<EngineeringEntity>>,
        scope: EngineeringIrValidationScope?,
    ): List<SemanticDiagnostic> = functions.mapNotNull { function ->
        if (scope != null && !scope.includes(function.id.value)) return@mapNotNull null
        classifyReference(
            function.owner.reference,
            entitiesByName[authoredPath(function.owner.reference)]?.size ?: 0,
        )?.let { classification ->
            errorDiagnostic(
                ruleId = "reference.function-owner.${classification.name.lowercase()}",
                category = SemanticDiagnosticCategory.REFERENCE,
                subjectIdentity = function.id,
                provenance = function.owner.reference.provenance,
                message = "Function owner `${authoredPath(function.owner.reference)}` ${classification.messageFragment}.",
            )
        }
    }

    private fun duplicateFunctionDiagnostics(
        functions: List<EngineeringFunction>,
        scope: EngineeringIrValidationScope?,
    ): List<SemanticDiagnostic> = functions
        .groupBy(::authoredFunctionPath)
        .values
        .filter { duplicates -> duplicates.size > 1 }
        .filter { duplicates -> scope == null || duplicates.any { function -> scope.includes(function.id.value) } }
        .flatMap { duplicates ->
            duplicates.map { function ->
                errorDiagnostic(
                    ruleId = "uniqueness.function.duplicate-authored-key",
                    category = SemanticDiagnosticCategory.UNIQUENESS,
                    subjectIdentity = function.id,
                    provenance = function.provenance,
                    message = "Duplicate Function authored key `${authoredFunctionPath(function)}` is not semantically unique.",
                )
            }
        }

    private fun connectionEndpointDiagnostics(
        connections: List<EngineeringConnection>,
        ports: List<EngineeringPort>,
        scope: EngineeringIrValidationScope?,
    ): List<SemanticDiagnostic> {
        val portsById = ports.associateBy { it.id }
        return connections.asSequence()
            .filter { connection -> scope == null || scope.includes(connection.id.value) }
            .flatMap { connection ->
                connection.endpoints.asSequence().mapNotNull { endpoint ->
                    val port = endpoint.port.resolvedIdentity?.let(portsById::get)
                    when {
                        port == null -> errorDiagnostic(
                            ruleId = "reference.connection-endpoint.unresolved",
                            category = SemanticDiagnosticCategory.REFERENCE,
                            subjectIdentity = connection.id,
                            provenance = endpoint.provenance,
                            message = "Connection endpoint `${authoredPath(endpoint.port)}` does not resolve to an Engineering Port. Declare that Port or correct this connection path.",
                        )
                        endpoint.role == ConnectionEndpointRole.SOURCE && port.direction == EngineeringPortDirection.INPUT -> errorDiagnostic(
                            ruleId = "direction.connection-source.illegal",
                            category = SemanticDiagnosticCategory.RELATIONSHIP,
                            subjectIdentity = connection.id,
                            provenance = endpoint.provenance,
                            message = "Connection source `${authoredPath(endpoint.port)}` cannot use an input Port. Change its direction to output or bidirectional, or choose another source Port.",
                        )
                        endpoint.role == ConnectionEndpointRole.SINK && port.direction == EngineeringPortDirection.OUTPUT -> errorDiagnostic(
                            ruleId = "direction.connection-sink.illegal",
                            category = SemanticDiagnosticCategory.RELATIONSHIP,
                            subjectIdentity = connection.id,
                            provenance = endpoint.provenance,
                            message = "Connection sink `${authoredPath(endpoint.port)}` cannot use an output Port. Change its direction to input or bidirectional, or choose another sink Port.",
                        )
                        else -> null
                    }
                }
            }
            .toList()
    }

    private fun duplicateConnectionDiagnostics(
        connections: List<EngineeringConnection>,
        scope: EngineeringIrValidationScope?,
    ): List<SemanticDiagnostic> = connections
        .groupBy { it.id }
        .values
        .filter { duplicates -> duplicates.size > 1 }
        .filter { duplicates -> scope == null || duplicates.any { connection -> scope.includes(connection.id.value) } }
        .flatMap { duplicates -> duplicates.map { connection ->
            errorDiagnostic(
                ruleId = "uniqueness.connection.duplicate-authored-key",
                category = SemanticDiagnosticCategory.UNIQUENESS,
                subjectIdentity = connection.id,
                provenance = connection.provenance,
                message = "Duplicate Engineering Connection authored key `${connection.id.value}` is not semantically unique.",
            )
        } }

    private fun netEndpointDiagnostics(
        nets: List<EngineeringNet>,
        ports: List<EngineeringPort>,
        scope: EngineeringIrValidationScope?,
    ): List<SemanticDiagnostic> {
        val portsById = ports.associateBy { it.id }
        return nets.asSequence()
            .filter { net -> scope == null || scope.includes(net.id.value) }
            .flatMap { net -> net.endpoints.asSequence().mapNotNull { endpoint ->
                val port = endpoint.port.resolvedIdentity?.let(portsById::get)
                when {
                    port == null -> errorDiagnostic(
                        "reference.net-endpoint.unresolved", SemanticDiagnosticCategory.REFERENCE, net.id, endpoint.provenance,
                        "Net endpoint `${authoredPath(endpoint.port)}` does not resolve to an Engineering Port. Declare that Port or correct this Net path.",
                    )
                    endpoint.role == ConnectionEndpointRole.SOURCE && port.direction == EngineeringPortDirection.INPUT -> errorDiagnostic(
                        "direction.net-source.illegal", SemanticDiagnosticCategory.RELATIONSHIP, net.id, endpoint.provenance,
                        "Net source `${authoredPath(endpoint.port)}` cannot use an input Port. Change its direction to output or bidirectional, or choose another source Port.",
                    )
                    endpoint.role == ConnectionEndpointRole.SINK && port.direction == EngineeringPortDirection.OUTPUT -> errorDiagnostic(
                        "direction.net-sink.illegal", SemanticDiagnosticCategory.RELATIONSHIP, net.id, endpoint.provenance,
                        "Net sink `${authoredPath(endpoint.port)}` cannot use an output Port. Change its direction to input or bidirectional, or choose another sink Port.",
                    )
                    else -> null
                }
            } }
            .toList()
    }

    private fun duplicateNetDiagnostics(
        nets: List<EngineeringNet>,
        scope: EngineeringIrValidationScope?,
    ): List<SemanticDiagnostic> = nets.groupBy { it.id }.values
        .filter { it.size > 1 }
        .filter { duplicates -> scope == null || duplicates.any { scope.includes(it.id.value) } }
        .flatMap { duplicates -> duplicates.map { net ->
            errorDiagnostic(
                "uniqueness.net.duplicate-authored-key", SemanticDiagnosticCategory.UNIQUENESS, net.id, net.provenance,
                "Duplicate Engineering Net authored key `${net.name}` is not semantically unique.",
            )
        } }

    private fun classifyReference(reference: EngineeringReference, candidateCount: Int): ReferenceClassification? {
        if (reference.resolvedIdentity != null) {
            return null
        }
        return when {
            candidateCount > 1 -> ReferenceClassification.AMBIGUOUS
            else -> ReferenceClassification.UNRESOLVED
        }
    }

    private fun authoredPortPath(port: EngineeringPort): String = authoredPath(port.owner.reference().authoredPath + port.name)

    private fun authoredFunctionPath(function: EngineeringFunction): String =
        authoredPath(function.owner.reference.authoredPath + function.name)

    private fun authoredPath(reference: EngineeringReference): String = authoredPath(reference.authoredPath)

    private fun authoredPath(parts: List<String>): String = parts.joinToString(".")

    private fun errorDiagnostic(
        ruleId: String,
        category: SemanticDiagnosticCategory,
        subjectIdentity: StableSemanticIdentity?,
        provenance: SourceProvenance,
        message: String,
    ): SemanticDiagnostic {
        return SemanticDiagnostic(
            severity = SemanticDiagnosticSeverity.ERROR,
            ruleId = SemanticRuleId(ruleId),
            category = category,
            subjectIdentity = subjectIdentity,
            provenance = provenance,
            message = message,
        )
    }
}

/**
 * Optional semantic validation scope used to rerun only the affected identities after a runtime mutation.
 */
data class EngineeringIrValidationScope(
    val semanticIds: Set<String>,
) {
    /**
     * Returns `true` when [semanticId] is included in the scoped validation surface.
     */
    fun includes(semanticId: String): Boolean = semanticId in semanticIds
}

private fun EngineeringPortOwner.reference(): EngineeringReference = when (this) {
    is EngineeringPortOwner.Entity -> entity.reference
    is EngineeringPortOwner.Function -> function.reference
}

private enum class ReferenceClassification(
    val ruleId: String,
    val messageFragment: String,
) {
    UNRESOLVED(
        ruleId = "reference.port-owner.unresolved",
        messageFragment = "does not resolve to any canonical semantic object",
    ),
    AMBIGUOUS(
        ruleId = "reference.port-owner.ambiguous",
        messageFragment = "resolves ambiguously to more than one canonical semantic object",
    ),
}
