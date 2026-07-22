package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AcceptAuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringDiagnostic
import com.engineeringood.athena.authoring.AuthoringDiagnosticAuthority
import com.engineeringood.athena.authoring.AuthoringDiagnosticCode
import com.engineeringood.athena.authoring.AuthoringLifecycleState
import com.engineeringood.athena.authoring.AuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringRecoveryAction
import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.authoring.AuthoringTransactionValidation
import com.engineeringood.athena.authoring.AuthoringTransactionValidationStatus
import com.engineeringood.athena.authoring.AuthoringValidationStage
import com.engineeringood.athena.authoring.CancelAuthoringPreviewDecision
import com.engineeringood.athena.authoring.RejectAuthoringPreviewDecision
import com.engineeringood.athena.authoring.SemanticAuthoringResult
import com.engineeringood.athena.authoring.SemanticAuthoringTransaction

fun interface AuthoringTransactionValidationAuthority {
    fun validate(
        stage: AuthoringValidationStage,
        transaction: SemanticAuthoringTransaction,
    ): AuthoringStageValidationResult
}

sealed interface AuthoringStageValidationResult

data object AuthoringStageValidationPassed : AuthoringStageValidationResult

data class AuthoringStageValidationBlocked(
    val diagnostic: AuthoringDiagnostic,
) : AuthoringStageValidationResult

fun interface ActiveAuthoringRevisionAuthority {
    fun currentRevision(transaction: SemanticAuthoringTransaction): AuthoringRevisionGuard
}

fun interface SemanticAuthoringMutationAuthority {
    fun commit(transaction: SemanticAuthoringTransaction): AuthoringMutationResult
}

sealed interface AuthoringMutationResult

data class AuthoringMutationCommitted(
    val mutationId: String,
    val committedRevision: AuthoringRevisionGuard,
    val affectedSemanticIds: List<String> = emptyList(),
) : AuthoringMutationResult

data class AuthoringMutationBlocked(
    val diagnostic: AuthoringDiagnostic,
) : AuthoringMutationResult

fun interface SemanticAuthoringReprojectionAuthority {
    fun reproject(
        transaction: SemanticAuthoringTransaction,
        commit: AuthoringMutationCommitted,
    ): AuthoringReprojectionResult
}

sealed interface AuthoringReprojectionResult

data class AuthoringReprojectionSucceeded(
    val projectionOccurrenceIds: List<String> = emptyList(),
) : AuthoringReprojectionResult

data class AuthoringReprojectionFailed(
    val diagnostics: List<AuthoringDiagnostic>,
) : AuthoringReprojectionResult

/** Orchestrates one revision-safe authoring decision without owning source planning details. */
class SemanticAuthoringTransactionRuntime(
    private val validationAuthority: AuthoringTransactionValidationAuthority,
    private val revisionAuthority: ActiveAuthoringRevisionAuthority,
    private val mutationAuthority: SemanticAuthoringMutationAuthority,
    private val reprojectionAuthority: SemanticAuthoringReprojectionAuthority,
) {
    fun decide(
        transaction: SemanticAuthoringTransaction,
        decision: AuthoringPreviewDecision,
    ): SemanticAuthoringTransaction {
        require(decision.intentId == transaction.intent.intentId) { "Authoring decision intent does not match transaction intent." }
        require(decision.previewId == transaction.preview?.previewId) { "Authoring decision preview does not match transaction preview." }

        return when (decision) {
            is RejectAuthoringPreviewDecision -> terminalWithoutMutation(
                transaction = transaction,
                decision = decision,
                lifecycleState = AuthoringLifecycleState.REJECTED,
            )
            is CancelAuthoringPreviewDecision -> terminalWithoutMutation(
                transaction = transaction,
                decision = decision,
                lifecycleState = AuthoringLifecycleState.CANCELLED,
            )
            is AcceptAuthoringPreviewDecision -> accept(transaction, decision)
        }
    }

    private fun accept(
        transaction: SemanticAuthoringTransaction,
        decision: AcceptAuthoringPreviewDecision,
    ): SemanticAuthoringTransaction {
        val completedStages = mutableListOf<AuthoringValidationStage>()
        for (stage in AuthoringValidationStage.entries) {
            if (stage == AuthoringValidationStage.REVISION_GUARD) {
                val activeRevision = revisionAuthority.currentRevision(transaction)
                if (activeRevision != transaction.revisionGuard) {
                    return validationFailure(
                        transaction = transaction,
                        decision = decision,
                        completedStages = completedStages,
                        lifecycleState = AuthoringLifecycleState.STALE,
                        diagnostic = AuthoringDiagnostic(
                            code = AuthoringDiagnosticCode.REVISION_GUARD_MISMATCH,
                            message = "Active source or semantic snapshot no longer matches the authoring preview Revision Guard.",
                            authority = AuthoringDiagnosticAuthority.REVISION_GUARD,
                            lifecycleStage = AuthoringLifecycleState.STALE,
                            recoveryAction = AuthoringRecoveryAction.REFRESH_PREVIEW,
                        ),
                    )
                }
            }

            when (val stageResult = validationAuthority.validate(stage, transaction)) {
                AuthoringStageValidationPassed -> completedStages += stage
                is AuthoringStageValidationBlocked -> {
                    return validationFailure(
                        transaction = transaction,
                        decision = decision,
                        completedStages = completedStages,
                        lifecycleState = AuthoringLifecycleState.BLOCKED,
                        diagnostic = stageResult.diagnostic,
                    )
                }
            }
        }

        val validation = AuthoringTransactionValidation(
            status = AuthoringTransactionValidationStatus.VALID,
            completedStages = completedStages,
        )
        val commit = when (val mutation = mutationAuthority.commit(transaction)) {
            is AuthoringMutationCommitted -> mutation
            is AuthoringMutationBlocked -> return validationFailure(
                transaction = transaction,
                decision = decision,
                completedStages = completedStages,
                lifecycleState = AuthoringLifecycleState.BLOCKED,
                diagnostic = mutation.diagnostic,
            )
        }
        return when (val reprojection = reprojectionAuthority.reproject(transaction, commit)) {
            is AuthoringReprojectionSucceeded -> transaction.copy(
                decision = decision,
                validation = validation,
                lifecycleState = AuthoringLifecycleState.REPROJECTED,
                mutationId = commit.mutationId,
                result = SemanticAuthoringResult(
                    lifecycleState = AuthoringLifecycleState.REPROJECTED,
                    committedRevision = commit.committedRevision,
                    mutationId = commit.mutationId,
                    affectedSemanticIds = commit.affectedSemanticIds,
                    projectionOccurrenceIds = reprojection.projectionOccurrenceIds,
                ),
                diagnostics = emptyList(),
            )
            is AuthoringReprojectionFailed -> transaction.copy(
                decision = decision,
                validation = validation,
                lifecycleState = AuthoringLifecycleState.PROJECTION_FAILED,
                mutationId = commit.mutationId,
                result = SemanticAuthoringResult(
                    lifecycleState = AuthoringLifecycleState.PROJECTION_FAILED,
                    committedRevision = commit.committedRevision,
                    mutationId = commit.mutationId,
                    affectedSemanticIds = commit.affectedSemanticIds,
                    diagnostics = reprojection.diagnostics,
                ),
                diagnostics = reprojection.diagnostics,
            )
        }
    }

    private fun validationFailure(
        transaction: SemanticAuthoringTransaction,
        decision: AuthoringPreviewDecision,
        completedStages: List<AuthoringValidationStage>,
        lifecycleState: AuthoringLifecycleState,
        diagnostic: AuthoringDiagnostic,
    ): SemanticAuthoringTransaction = transaction.copy(
        decision = decision,
        validation = AuthoringTransactionValidation(
            status = AuthoringTransactionValidationStatus.BLOCKED,
            completedStages = completedStages,
            diagnostics = listOf(diagnostic),
        ),
        lifecycleState = lifecycleState,
        result = SemanticAuthoringResult(
            lifecycleState = lifecycleState,
            diagnostics = listOf(diagnostic),
        ),
        diagnostics = listOf(diagnostic),
    )

    private fun terminalWithoutMutation(
        transaction: SemanticAuthoringTransaction,
        decision: AuthoringPreviewDecision,
        lifecycleState: AuthoringLifecycleState,
    ): SemanticAuthoringTransaction = transaction.copy(
        decision = decision,
        lifecycleState = lifecycleState,
        result = SemanticAuthoringResult(lifecycleState = lifecycleState),
    )
}
