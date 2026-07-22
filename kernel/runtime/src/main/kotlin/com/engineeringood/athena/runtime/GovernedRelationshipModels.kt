package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AuthoringDiagnostic
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.AuthoringRelationshipRoutePreviewEvidence
import com.engineeringood.athena.authoring.AuthoringTransactionProvenance
import com.engineeringood.athena.authoring.SemanticAuthoringTransaction
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionId
import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.compiler.BackendAuthoringSourceDocument
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlan
import com.engineeringood.athena.interaction.AuthoringCapabilityEvidence
import com.engineeringood.athena.ir.EngineeringDocument

data class GovernedRelationshipPreviewRequest(
    val transactionId: SemanticAuthoringTransactionId,
    val previewId: AuthoringPreviewId,
    val intent: SemanticRelationshipIntent,
    val capabilityEvidence: AuthoringCapabilityEvidence,
    val provenance: AuthoringTransactionProvenance,
    val sourceDocument: BackendAuthoringSourceDocument,
    val semanticDocument: EngineeringDocument,
)

fun interface GovernedRelationshipRoutePreviewAuthority {
    fun preview(
        intent: SemanticRelationshipIntent,
        document: EngineeringDocument,
    ): AuthoringRelationshipRoutePreviewEvidence?
}

sealed interface GovernedRelationshipPreviewResult {
    val transaction: SemanticAuthoringTransaction
    val validationAuthority: AuthoringTransactionValidationAuthority
}

data class GovernedRelationshipPreviewReady(
    override val transaction: SemanticAuthoringTransaction,
    val sourceEditPlan: BackendAuthoringSourceEditPlan,
    override val validationAuthority: AuthoringTransactionValidationAuthority,
) : GovernedRelationshipPreviewResult

data class GovernedRelationshipPreviewBlocked(
    override val transaction: SemanticAuthoringTransaction,
    val diagnostics: List<AuthoringDiagnostic>,
    override val validationAuthority: AuthoringTransactionValidationAuthority,
) : GovernedRelationshipPreviewResult
