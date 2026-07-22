package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AuthoringDiagnostic
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.CreateSemanticEntityIntent
import com.engineeringood.athena.authoring.SemanticAuthoringTransaction
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionId
import com.engineeringood.athena.compiler.BackendAuthoringSourceDocument
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlan
import com.engineeringood.athena.component.EngineeringConceptTemplate
import com.engineeringood.athena.interaction.AuthoringCapabilityEvidence

data class GovernedEntityCreationPreviewRequest(
    val transactionId: SemanticAuthoringTransactionId,
    val previewId: AuthoringPreviewId,
    val intent: CreateSemanticEntityIntent,
    val capabilityEvidence: AuthoringCapabilityEvidence,
    val document: BackendAuthoringSourceDocument,
)

fun interface GovernedEntityCreationProjectionAuthority {
    fun resolve(
        intent: CreateSemanticEntityIntent,
        template: EngineeringConceptTemplate,
        canonicalTag: String,
    ): GovernedEntityCreationProjectionResult
}

sealed interface GovernedEntityCreationProjectionResult

data class GovernedEntityCreationProjectionResolved(
    val representationId: String,
    val compositionTargetId: String,
    val projectionOccurrenceIds: List<String>,
) : GovernedEntityCreationProjectionResult

data class GovernedEntityCreationRepresentationUnresolved(
    val reason: String,
) : GovernedEntityCreationProjectionResult

data class GovernedEntityCreationCompositionUnsatisfied(
    val representationId: String,
    val reason: String,
) : GovernedEntityCreationProjectionResult

sealed interface GovernedEntityCreationPreviewResult {
    val transaction: SemanticAuthoringTransaction
    val validationAuthority: AuthoringTransactionValidationAuthority
}

data class GovernedEntityCreationPreviewReady(
    override val transaction: SemanticAuthoringTransaction,
    val sourceEditPlan: BackendAuthoringSourceEditPlan,
    override val validationAuthority: AuthoringTransactionValidationAuthority,
) : GovernedEntityCreationPreviewResult

data class GovernedEntityCreationPreviewBlocked(
    override val transaction: SemanticAuthoringTransaction,
    val diagnostics: List<AuthoringDiagnostic>,
    override val validationAuthority: AuthoringTransactionValidationAuthority,
) : GovernedEntityCreationPreviewResult
