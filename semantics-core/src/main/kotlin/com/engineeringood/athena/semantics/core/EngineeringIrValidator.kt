package com.engineeringood.athena.semantics.core

import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringIrDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity

/** Deterministic semantic validation pass over canonical Engineering IR for the current M0 slice. */
class EngineeringIrValidator {
    /** Validates [document] and emits provenance-rich diagnostics without mutating the canonical IR. */
    fun validate(document: EngineeringIrDocument): SemanticValidationResult {
        val componentsByName = document.components.groupBy { it.name }
        val portsByPath = document.ports.groupBy { authoredPortPath(it) }
        val diagnostics = buildList {
            addAll(duplicateComponentDiagnostics(document.components))
            addAll(portOwnerDiagnostics(document.ports, componentsByName))
            addAll(duplicatePortDiagnostics(document.ports))
            addAll(connectionReferenceDiagnostics(document.connections, portsByPath))
            addAll(duplicateConnectionDiagnostics(document.connections))
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

    private fun duplicateComponentDiagnostics(components: List<EngineeringComponent>): List<SemanticDiagnostic> {
        return components
            .groupBy { it.name }
            .values
            .filter { it.size > 1 }
            .flatMap { duplicates ->
                duplicates.map { component ->
                    errorDiagnostic(
                        ruleId = "uniqueness.component.duplicate-authored-key",
                        category = SemanticDiagnosticCategory.UNIQUENESS,
                        subjectIdentity = component.id,
                        provenance = component.provenance,
                        message = "Duplicate component authored key `${component.name}` is not semantically unique.",
                    )
                }
            }
    }

    private fun portOwnerDiagnostics(
        ports: List<EngineeringPort>,
        componentsByName: Map<String, List<EngineeringComponent>>,
    ): List<SemanticDiagnostic> {
        return ports.mapNotNull { port ->
            classifyReference(
                reference = port.ownerReference,
                candidateCount = componentsByName[authoredComponentPath(port.ownerReference)]?.size ?: 0,
            )?.let { classification ->
                errorDiagnostic(
                    ruleId = classification.ruleId,
                    category = SemanticDiagnosticCategory.REFERENCE,
                    subjectIdentity = port.id,
                    provenance = port.ownerReference.provenance,
                    message = "Port owner `${authoredComponentPath(port.ownerReference)}` ${classification.messageFragment}.",
                )
            }
        }
    }

    private fun duplicatePortDiagnostics(ports: List<EngineeringPort>): List<SemanticDiagnostic> {
        return ports
            .groupBy { authoredPortPath(it) }
            .values
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

    private fun connectionReferenceDiagnostics(
        connections: List<EngineeringConnection>,
        portsByPath: Map<String, List<EngineeringPort>>,
    ): List<SemanticDiagnostic> {
        return buildList {
            connections.forEach { connection ->
                classifyReference(connection.from, portsByPath[authoredPath(connection.from)]?.size ?: 0)?.let { classification ->
                    add(
                        errorDiagnostic(
                            ruleId = classification.ruleIdForConnectionEndpoint,
                            category = SemanticDiagnosticCategory.REFERENCE,
                            subjectIdentity = connection.id,
                            provenance = connection.from.provenance,
                            message = "Connection endpoint `${authoredPath(connection.from)}` ${classification.messageFragment}.",
                        ),
                    )
                }
                classifyReference(connection.to, portsByPath[authoredPath(connection.to)]?.size ?: 0)?.let { classification ->
                    add(
                        errorDiagnostic(
                            ruleId = classification.ruleIdForConnectionEndpoint,
                            category = SemanticDiagnosticCategory.REFERENCE,
                            subjectIdentity = connection.id,
                            provenance = connection.to.provenance,
                            message = "Connection endpoint `${authoredPath(connection.to)}` ${classification.messageFragment}.",
                        ),
                    )
                }
            }
        }
    }

    private fun duplicateConnectionDiagnostics(connections: List<EngineeringConnection>): List<SemanticDiagnostic> {
        return connections
            .groupBy { authoredConnectionPath(it) }
            .values
            .filter { it.size > 1 }
            .flatMap { duplicates ->
                duplicates.map { connection ->
                    errorDiagnostic(
                        ruleId = "uniqueness.connection.duplicate-authored-key",
                        category = SemanticDiagnosticCategory.UNIQUENESS,
                        subjectIdentity = connection.id,
                        provenance = connection.provenance,
                        message = "Duplicate connection authored key `${authoredConnectionPath(connection)}` is not semantically unique.",
                    )
                }
            }
    }

    private fun authoredConnectionPath(connection: EngineeringConnection): String {
        return "${authoredPath(connection.from)}->${authoredPath(connection.to)}"
    }

    private fun classifyReference(reference: EngineeringReference, candidateCount: Int): ReferenceClassification? {
        if (reference.resolvedIdentity != null) {
            return null
        }
        return when {
            candidateCount > 1 -> ReferenceClassification.AMBIGUOUS
            else -> ReferenceClassification.UNRESOLVED
        }
    }

    private fun authoredComponentPath(reference: EngineeringReference): String = authoredPath(reference)

    private fun authoredPortPath(port: EngineeringPort): String = authoredPath(port.ownerReference.authoredPath + port.name)

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

private enum class ReferenceClassification(
    val ruleId: String,
    val ruleIdForConnectionEndpoint: String,
    val messageFragment: String,
) {
    UNRESOLVED(
        ruleId = "reference.port-owner.unresolved",
        ruleIdForConnectionEndpoint = "reference.connection-endpoint.unresolved",
        messageFragment = "does not resolve to any canonical semantic object",
    ),
    AMBIGUOUS(
        ruleId = "reference.port-owner.ambiguous",
        ruleIdForConnectionEndpoint = "reference.connection-endpoint.ambiguous",
        messageFragment = "resolves ambiguously to more than one canonical semantic object",
    ),
}
