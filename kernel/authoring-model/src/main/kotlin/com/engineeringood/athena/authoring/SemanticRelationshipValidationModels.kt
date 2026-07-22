package com.engineeringood.athena.authoring

import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.StableSemanticIdentity

/** Narrow source state gate used before a semantic relationship preview may become persistent. */
enum class SemanticRelationshipSourceState {
    VALID,
    DIRTY,
    INVALID,
}

/** One governed diagnostic emitted while validating a semantic relationship authoring request. */
data class SemanticRelationshipValidationDiagnostic(
    val code: String,
    val message: String,
    val subjectIds: Set<StableSemanticIdentity> = emptySet(),
)

/** Input for validating one semantic relationship authoring request against compiled engineering truth. */
data class SemanticRelationshipValidationRequest(
    val intent: SemanticRelationshipIntent,
    val document: EngineeringDocument,
    val sourceState: SemanticRelationshipSourceState = SemanticRelationshipSourceState.VALID,
    val sourceText: String? = null,
)

/** Result of validating one relationship intent before preview or persistence. */
data class SemanticRelationshipValidationResult(
    val previewEligible: Boolean,
    val persistenceEligible: Boolean,
    val diagnostics: List<SemanticRelationshipValidationDiagnostic>,
    val sourceTextAfterValidation: String? = null,
)

/** M28 v0 validator for electrical semantic relationship authoring. */
class ElectricalSemanticRelationshipCompatibilityValidator {
    fun validate(request: SemanticRelationshipValidationRequest): SemanticRelationshipValidationResult {
        val sourceStateDiagnostic = request.sourceState.toDiagnostic()
        if (sourceStateDiagnostic != null) {
            return rejected(request, sourceStateDiagnostic)
        }

        if (request.intent.relationshipType != ElectricalConnectionRelationship) {
            return rejected(
                request,
                SemanticRelationshipValidationDiagnostic(
                    code = "semantic.relationship.type.unsupported",
                    message = "Relationship type `${request.intent.relationshipType.value}` is not supported by the electrical validator.",
                    subjectIds = request.subjectIds(),
                ),
            )
        }

        if (request.intent.sourceSubjectId.value.isBlank() || request.intent.targetSubjectId.value.isBlank()) {
            return rejected(
                request,
                SemanticRelationshipValidationDiagnostic(
                    code = "semantic.relationship.subject.malformed",
                    message = "Relationship subjects must use non-blank canonical semantic identities.",
                    subjectIds = request.subjectIds(),
                ),
            )
        }

        if (request.intent.sourceSubjectId == request.intent.targetSubjectId) {
            return rejected(
                request,
                SemanticRelationshipValidationDiagnostic(
                    code = "semantic.relationship.self",
                    message = "A semantic relationship cannot connect one subject to itself.",
                    subjectIds = request.subjectIds(),
                ),
            )
        }

        val sourcePort = request.document.portById(request.intent.sourceSubjectId)
        val targetPort = request.document.portById(request.intent.targetSubjectId)
        if (sourcePort == null || targetPort == null) {
            return rejected(
                request,
                SemanticRelationshipValidationDiagnostic(
                    code = "semantic.relationship.subject.missing",
                    message = "Both relationship subjects must resolve to existing semantic ports.",
                    subjectIds = request.subjectIds(),
                ),
            )
        }

        if (sourcePort.ownerReference.resolvedIdentity == null || targetPort.ownerReference.resolvedIdentity == null) {
            return rejected(
                request,
                SemanticRelationshipValidationDiagnostic(
                    code = "semantic.relationship.owner.ambiguous",
                    message = "Relationship subjects must have deterministic component ownership before persistence.",
                    subjectIds = request.subjectIds(),
                ),
            )
        }

        if (request.document.hasConnection(request.intent.sourceSubjectId, request.intent.targetSubjectId)) {
            return rejected(
                request,
                SemanticRelationshipValidationDiagnostic(
                    code = "semantic.relationship.duplicate",
                    message = "The requested semantic relationship already exists.",
                    subjectIds = request.subjectIds(),
                ),
            )
        }

        val sourceDirection = sourcePort.symbolProperty("direction")
        val targetDirection = targetPort.symbolProperty("direction")
        if (sourceDirection != "out" || targetDirection != "in") {
            return rejected(
                request,
                SemanticRelationshipValidationDiagnostic(
                    code = "semantic.relationship.electrical.direction",
                    message = "Electrical relationship authoring requires an output subject connected to an input subject.",
                    subjectIds = request.subjectIds(),
                ),
            )
        }

        val sourceSignal = sourcePort.symbolProperty("signal")
        val targetSignal = targetPort.symbolProperty("signal")
        if (sourceSignal == null || targetSignal == null || sourceSignal != targetSignal) {
            return rejected(
                request,
                SemanticRelationshipValidationDiagnostic(
                    code = "semantic.relationship.electrical.signal",
                    message = "Electrical relationship authoring requires matching signal types.",
                    subjectIds = request.subjectIds(),
                ),
            )
        }

        return SemanticRelationshipValidationResult(
            previewEligible = true,
            persistenceEligible = true,
            diagnostics = emptyList(),
            sourceTextAfterValidation = request.sourceText,
        )
    }

    private fun rejected(
        request: SemanticRelationshipValidationRequest,
        diagnostic: SemanticRelationshipValidationDiagnostic,
    ): SemanticRelationshipValidationResult {
        return SemanticRelationshipValidationResult(
            previewEligible = false,
            persistenceEligible = false,
            diagnostics = listOf(diagnostic),
            sourceTextAfterValidation = request.sourceText,
        )
    }
}

private fun SemanticRelationshipSourceState.toDiagnostic(): SemanticRelationshipValidationDiagnostic? {
    return when (this) {
        SemanticRelationshipSourceState.VALID -> null
        SemanticRelationshipSourceState.DIRTY -> SemanticRelationshipValidationDiagnostic(
            code = "semantic.relationship.source.dirty",
            message = "Relationship authoring is blocked while source buffers are dirty.",
        )
        SemanticRelationshipSourceState.INVALID -> SemanticRelationshipValidationDiagnostic(
            code = "semantic.relationship.source.invalid",
            message = "Relationship authoring is blocked while the semantic source is invalid.",
        )
    }
}

private fun SemanticRelationshipValidationRequest.subjectIds(): Set<StableSemanticIdentity> {
    return setOf(intent.sourceSubjectId, intent.targetSubjectId)
}

private fun EngineeringDocument.portById(portId: StableSemanticIdentity): EngineeringPort? {
    return ports.singleOrNull { port -> port.id == portId }
}

private fun EngineeringDocument.hasConnection(
    sourcePortId: StableSemanticIdentity,
    targetPortId: StableSemanticIdentity,
): Boolean {
    return connections.any { connection ->
        connection.resolvedSourceId() == sourcePortId && connection.resolvedTargetId() == targetPortId
    }
}

private fun EngineeringConnection.resolvedSourceId(): StableSemanticIdentity? = from.resolvedIdentity

private fun EngineeringConnection.resolvedTargetId(): StableSemanticIdentity? = to.resolvedIdentity

private fun EngineeringPort.symbolProperty(name: String): String? {
    return properties.lastOrNull { property -> property.name == name }?.value?.let { value ->
        when (value) {
            is EngineeringPropertyValue.Symbol -> value.text
            is EngineeringPropertyValue.Text -> null
        }
    }
}
