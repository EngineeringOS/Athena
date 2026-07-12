package com.engineeringood.athena.runtime

/**
 * Stable request categories for the first governed AI reasoning proof.
 */
enum class AthenaAiReasoningRequestCategory {
    DIAGNOSTIC_EXPLANATION,
    IMPACT_SUMMARY,
    NEXT_CHECK,
}

/**
 * Stable proposal categories emitted by Athena-owned reasoning flows.
 */
enum class AthenaAiReasoningProposalCategory {
    DIAGNOSTIC_EXPLANATION,
    IMPACT_SUMMARY,
    NEXT_CHECK,
}

/**
 * Typed evidence kinds that can be cited by one reasoning proposal.
 */
enum class AthenaAiReasoningEvidenceKind {
    SEMANTIC_IDENTITY,
    DERIVED_CONTEXT,
    CAPABILITY_FACT,
    CONSTRAINT_EVALUATION,
    DIAGNOSTIC,
    IMPACT_CONSEQUENCE,
    REVIEW_ENTRY,
}

/**
 * Provider-neutral result status for one reasoning request attempt.
 */
enum class AthenaAiReasoningProviderResultStatus {
    SUCCESS,
    UNAVAILABLE,
    FAILED,
}

/**
 * Explicit user-facing decision state for one reasoning proposal.
 */
enum class AthenaAiReasoningProposalDecisionState {
    ACCEPTED,
    DISMISSED,
    UNRESOLVED,
    UNAVAILABLE,
}

/**
 * Typed governed evidence reference preserved with one reasoning proposal.
 */
data class AthenaAiReasoningEvidenceRef(
    val kind: AthenaAiReasoningEvidenceKind,
    val referenceId: String,
    val summary: String,
)

/**
 * Deterministic Athena-owned reasoning context package assembled above governed semantic outputs.
 */
data class AthenaAiReasoningContext(
    val projectName: String,
    val requestCategory: AthenaAiReasoningRequestCategory,
    val subjectSemanticIds: List<String>,
    val evidence: List<AthenaAiReasoningEvidenceRef>,
)

/**
 * Draft reasoning proposal recorded after Athena has already assembled governed evidence and provider outcome.
 */
data class AthenaAiReasoningProposalDraft(
    val proposalCategory: AthenaAiReasoningProposalCategory,
    val providerStatus: AthenaAiReasoningProviderResultStatus,
    val summary: String,
    val response: String,
    val context: AthenaAiReasoningContext,
    val providerId: String? = null,
)

/**
 * Runtime-owned reasoning proposal preserved for later inspection and explicit decision tracking.
 */
data class AthenaAiReasoningProposal(
    val proposalId: String,
    val proposalCategory: AthenaAiReasoningProposalCategory,
    val providerStatus: AthenaAiReasoningProviderResultStatus,
    val decisionState: AthenaAiReasoningProposalDecisionState,
    val summary: String,
    val response: String,
    val context: AthenaAiReasoningContext,
    val providerId: String? = null,
)

/**
 * Serializable snapshot of runtime-owned reasoning proposals for one active project.
 */
data class AthenaAiReasoningProposalSnapshot(
    val proposals: List<AthenaAiReasoningProposal>,
    val nextProposalOrdinal: Int,
)

/**
 * Outcome of recording one reasoning proposal.
 */
sealed interface AthenaAiReasoningProposalRecordingResult

/**
 * Reasoning proposal was recorded into runtime-owned inspection state.
 */
data class AthenaAiReasoningProposalRecorded(
    val proposal: AthenaAiReasoningProposal,
) : AthenaAiReasoningProposalRecordingResult

/**
 * Outcome of changing one stored reasoning proposal decision state.
 */
sealed interface AthenaAiReasoningProposalDecisionResult

/**
 * Reasoning proposal decision state was updated successfully.
 */
data class AthenaAiReasoningProposalDecisionUpdated(
    val proposal: AthenaAiReasoningProposal,
) : AthenaAiReasoningProposalDecisionResult

/**
 * Requested reasoning proposal decision transition could not be completed.
 */
data class AthenaAiReasoningProposalDecisionRejected(
    val proposalId: String,
    val reason: String,
) : AthenaAiReasoningProposalDecisionResult

/**
 * Runtime-owned optional AI reasoning boundary.
 *
 * This service preserves typed, inspectable reasoning proposals without granting them command or semantic authority.
 */
class AthenaAiReasoningRuntimeService internal constructor() {
    private val contextAssembler = AthenaAiReasoningContextAssembler()

    /**
     * Assembles one deterministic reasoning context package from runtime-owned semantic outputs.
     */
    fun assembleContext(
        context: AthenaExecutionContext,
        request: AthenaAiReasoningContextRequest,
    ): AthenaAiReasoningContext {
        return contextAssembler.assemble(context, request)
    }

    /**
     * Records one governed reasoning proposal for later inspection and explicit user decision.
     */
    fun recordProposal(
        context: AthenaExecutionContext,
        draft: AthenaAiReasoningProposalDraft,
    ): AthenaAiReasoningProposalRecordingResult {
        val state = context.aiReasoningProposalState()
        val proposal = AthenaAiReasoningProposal(
            proposalId = "ai-reasoning-${state.nextProposalOrdinal.toString().padStart(4, '0')}",
            proposalCategory = draft.proposalCategory,
            providerStatus = draft.providerStatus,
            decisionState = draft.providerStatus.initialDecisionState(),
            summary = draft.summary,
            response = draft.response,
            context = draft.context.normalized(),
            providerId = draft.providerId,
        )
        context.replaceAiReasoningProposalState(
            state.copy(
                proposals = state.proposals + proposal,
                nextProposalOrdinal = state.nextProposalOrdinal + 1,
            ),
        )
        return AthenaAiReasoningProposalRecorded(proposal)
    }

    /**
     * Returns every stored reasoning proposal for the active project.
     */
    fun proposals(context: AthenaExecutionContext): List<AthenaAiReasoningProposal> {
        return context.aiReasoningProposalState().proposals
    }

    /**
     * Returns a deterministic snapshot of stored reasoning proposals.
     */
    fun snapshot(context: AthenaExecutionContext): AthenaAiReasoningProposalSnapshot {
        val state = context.aiReasoningProposalState()
        return AthenaAiReasoningProposalSnapshot(
            proposals = state.proposals,
            nextProposalOrdinal = state.nextProposalOrdinal,
        )
    }

    /**
     * Restores stored reasoning proposals from a runtime-owned snapshot.
     */
    fun restoreProposals(
        context: AthenaExecutionContext,
        snapshot: AthenaAiReasoningProposalSnapshot,
    ) {
        context.replaceAiReasoningProposalState(
            AthenaAiReasoningProposalState(
                proposals = snapshot.proposals,
                nextProposalOrdinal = snapshot.nextProposalOrdinal,
            ),
        )
    }

    /**
     * Marks one stored reasoning proposal as accepted for review guidance.
     */
    fun markAccepted(
        context: AthenaExecutionContext,
        proposalId: String,
    ): AthenaAiReasoningProposalDecisionResult {
        return updateDecisionState(
            context = context,
            proposalId = proposalId,
            decisionState = AthenaAiReasoningProposalDecisionState.ACCEPTED,
        )
    }

    /**
     * Marks one stored reasoning proposal as dismissed by the operator.
     */
    fun markDismissed(
        context: AthenaExecutionContext,
        proposalId: String,
    ): AthenaAiReasoningProposalDecisionResult {
        return updateDecisionState(
            context = context,
            proposalId = proposalId,
            decisionState = AthenaAiReasoningProposalDecisionState.DISMISSED,
        )
    }

    private fun updateDecisionState(
        context: AthenaExecutionContext,
        proposalId: String,
        decisionState: AthenaAiReasoningProposalDecisionState,
    ): AthenaAiReasoningProposalDecisionResult {
        val state = context.aiReasoningProposalState()
        val proposal = state.proposals.firstOrNull { candidate -> candidate.proposalId == proposalId }
            ?: return AthenaAiReasoningProposalDecisionRejected(
                proposalId = proposalId,
                reason = "No stored AI reasoning proposal matches `$proposalId`.",
            )

        if (proposal.decisionState == AthenaAiReasoningProposalDecisionState.UNAVAILABLE) {
            return AthenaAiReasoningProposalDecisionRejected(
                proposalId = proposalId,
                reason = "AI reasoning proposal `$proposalId` is unavailable and cannot change decision state.",
            )
        }

        val updatedProposal = proposal.copy(decisionState = decisionState)
        context.replaceAiReasoningProposalState(
            state.copy(
                proposals = state.proposals.map { candidate ->
                    if (candidate.proposalId == proposalId) {
                        updatedProposal
                    } else {
                        candidate
                    }
                },
            ),
        )
        return AthenaAiReasoningProposalDecisionUpdated(updatedProposal)
    }
}

/**
 * Internal runtime-owned AI reasoning proposal state for one active project.
 */
internal data class AthenaAiReasoningProposalState(
    val proposals: List<AthenaAiReasoningProposal> = emptyList(),
    val nextProposalOrdinal: Int = 1,
)

private fun AthenaAiReasoningProviderResultStatus.initialDecisionState(): AthenaAiReasoningProposalDecisionState {
    return when (this) {
        AthenaAiReasoningProviderResultStatus.SUCCESS -> AthenaAiReasoningProposalDecisionState.UNRESOLVED
        AthenaAiReasoningProviderResultStatus.UNAVAILABLE,
        AthenaAiReasoningProviderResultStatus.FAILED,
        -> AthenaAiReasoningProposalDecisionState.UNAVAILABLE
    }
}

private fun AthenaAiReasoningContext.normalized(): AthenaAiReasoningContext {
    return copy(
        subjectSemanticIds = subjectSemanticIds.distinct().sorted(),
        evidence = evidence.distinct().sortedWith(
            compareBy<AthenaAiReasoningEvidenceRef>({ it.kind.name }, { it.referenceId }, { it.summary }),
        ),
    )
}
