package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AcceptAuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringIntent
import com.engineeringood.athena.authoring.AuthoringPreview
import com.engineeringood.athena.authoring.AuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.AuthoringPreviewStatus

/**
 * Runtime-owned authoring preview orchestrator and governed transaction handoff.
 *
 * This service accepts guided authoring intents, expands them into deterministic inspectable
 * previews, records explicit review decisions, and invokes the sole supplied mutation authority for governed acceptance.
 */
class AthenaAuthoringSessionRuntimeService internal constructor() {
    /**
     * Records one runtime-owned preview for the supplied guided authoring intent.
     */
    @Synchronized
    fun submit(
        context: AthenaExecutionContext,
        intent: AuthoringIntent,
        previewFactory: ((AuthoringPreviewId) -> AuthoringPreview)? = null,
        governedPreviewFactory: ((AuthoringPreviewId) -> AthenaGovernedAuthoringPreviewContext)? = null,
    ): AthenaAuthoringPreviewSubmissionResult {
        require(previewFactory == null || governedPreviewFactory == null) {
            "Authoring submission must use either a preview factory or a governed preview factory, not both."
        }
        val state = context.authoringSessionState()
        val previewId = AuthoringPreviewId(
            "authoring-preview-${state.nextPreviewOrdinal.toString().padStart(4, '0')}",
        )
        val governedContext = governedPreviewFactory?.invoke(previewId)
        val preview = governedContext?.transaction?.preview
            ?: previewFactory?.invoke(previewId)
            ?: intent.toPreview(previewId)
        require(preview.previewId == previewId) { "Authoring preview factory must preserve the allocated preview id." }
        require(preview.intentId == intent.intentId) { "Authoring preview factory must preserve the submitted intent id." }
        val record = AthenaAuthoringSessionRecord(
            intent = intent,
            preview = preview,
            governedContext = governedContext,
        )
        context.replaceAuthoringSessionState(
            state.copy(
                records = state.records + record,
                nextPreviewOrdinal = state.nextPreviewOrdinal + 1,
            ),
        )
        return AthenaAuthoringPreviewSubmitted(record)
    }

    /**
     * Returns the current inspectable preview state for the active project.
     */
    fun state(context: AthenaExecutionContext): AthenaAuthoringSessionView {
        val records = context.authoringSessionState().records
        return AthenaAuthoringSessionView(
            records = records,
            pendingPreviewCount = records.count { record ->
                record.preview.status == AuthoringPreviewStatus.PENDING_REVIEW
            },
        )
    }

    /**
     * Returns a deterministic snapshot of stored guided authoring preview state.
     */
    fun snapshot(context: AthenaExecutionContext): AthenaAuthoringSessionSnapshot {
        val state = context.authoringSessionState()
        return AthenaAuthoringSessionSnapshot(
            records = state.records,
            nextPreviewOrdinal = state.nextPreviewOrdinal,
        )
    }

    /**
     * Restores stored guided authoring preview state from a runtime-owned snapshot.
     */
    fun restoreSession(
        context: AthenaExecutionContext,
        snapshot: AthenaAuthoringSessionSnapshot,
    ) {
        context.replaceAuthoringSessionState(
            AthenaAuthoringSessionState(
                records = snapshot.records,
                nextPreviewOrdinal = snapshot.nextPreviewOrdinal,
            ),
        )
    }

    /**
     * Applies one explicit preview decision without mutating canonical engineering truth.
     */
    @Synchronized
    fun applyDecision(
        context: AthenaExecutionContext,
        decision: AuthoringPreviewDecision,
        governedAuthorities: AthenaGovernedAuthoringDecisionAuthorities? = null,
    ): AthenaAuthoringPreviewDecisionResult {
        val state = context.authoringSessionState()
        val recordIndex = state.records.indexOfFirst { record ->
            record.preview.previewId == decision.previewId
        }
        if (recordIndex < 0) {
            return AthenaAuthoringPreviewDecisionUnavailable(
                previewId = decision.previewId,
                reason = "Authoring preview `${decision.previewId.value}` is not present in the active runtime session.",
            )
        }

        val record = state.records[recordIndex]
        if (record.preview.intentId != decision.intentId) {
            return AthenaAuthoringPreviewDecisionUnavailable(
                previewId = decision.previewId,
                reason = "Authoring preview `${decision.previewId.value}` does not match intent `${decision.intentId.value}`.",
            )
        }

        if (record.preview.status != AuthoringPreviewStatus.PENDING_REVIEW) {
            return AthenaAuthoringPreviewDecisionUnavailable(
                previewId = decision.previewId,
                reason = "Authoring preview `${decision.previewId.value}` is already ${record.preview.status.name.lowercase().replace('_', '-')} and cannot be decided again.",
            )
        }

        if (decision is AcceptAuthoringPreviewDecision && !record.preview.acceptanceEligibility.eligible) {
            return AthenaAuthoringPreviewDecisionUnavailable(
                previewId = decision.previewId,
                reason = record.preview.acceptanceEligibility.diagnostics
                    .joinToString(separator = "; ") { diagnostic -> "${diagnostic.code.value}: ${diagnostic.message}" },
            )
        }

        if (decision is AcceptAuthoringPreviewDecision && record.governedContext != null && governedAuthorities == null) {
            return AthenaAuthoringPreviewDecisionUnavailable(
                previewId = decision.previewId,
                reason = "Governed authoring preview `${decision.previewId.value}` requires active revision, mutation, and reprojection authorities.",
            )
        }

        val decidedTransaction = record.governedContext?.let { governed ->
            if (decision !is AcceptAuthoringPreviewDecision) {
                return@let SemanticAuthoringTransactionRuntime(
                    validationAuthority = governed.validationAuthority,
                    revisionAuthority = ActiveAuthoringRevisionAuthority { governed.transaction.revisionGuard },
                    mutationAuthority = SemanticAuthoringMutationAuthority {
                        error("Reject/cancel must not call mutation authority.")
                    },
                    reprojectionAuthority = SemanticAuthoringReprojectionAuthority { _, _ ->
                        error("Reject/cancel must not call reprojection authority.")
                    },
                ).decide(governed.transaction, decision)
            }
            val authorities = requireNotNull(governedAuthorities)
            SemanticAuthoringTransactionRuntime(
                validationAuthority = AuthoringTransactionValidationAuthority { stage, transaction ->
                    when (val governedValidation = governed.validationAuthority.validate(stage, transaction)) {
                        is AuthoringStageValidationBlocked -> governedValidation
                        AuthoringStageValidationPassed -> authorities.acceptanceValidationAuthority
                            ?.validate(stage, transaction)
                            ?: AuthoringStageValidationPassed
                    }
                },
                revisionAuthority = authorities.revisionAuthority,
                mutationAuthority = authorities.mutationAuthority,
                reprojectionAuthority = authorities.reprojectionAuthority,
            ).decide(governed.transaction, decision)
        }

        val updatedRecord = record.copy(
            preview = record.preview.copy(
                status = decidedTransaction?.lifecycleState?.toPreviewStatus() ?: decision.toPreviewStatus(),
            ),
            governedContext = record.governedContext?.copy(
                transaction = requireNotNull(decidedTransaction),
            ),
        )
        val updatedRecords = state.records.toMutableList().apply {
            set(recordIndex, updatedRecord)
        }.toList()
        context.replaceAuthoringSessionState(
            state.copy(records = updatedRecords),
        )
        return AthenaAuthoringPreviewDecisionUpdated(
            record = updatedRecord,
            transaction = decidedTransaction,
        )
    }
}

/**
 * Internal runtime-owned guided authoring preview state for one active project.
 */
internal data class AthenaAuthoringSessionState(
    val records: List<AthenaAuthoringSessionRecord> = emptyList(),
    val nextPreviewOrdinal: Int = 1,
)
