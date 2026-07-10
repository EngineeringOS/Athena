package com.engineeringood.athena.runtime

/**
 * Draft AI-originated command proposal that has not yet entered canonical project history.
 */
data class AthenaAiCommandProposalDraft(
    val summary: String,
    val rationale: String,
    val command: AthenaCommand,
)

/**
 * Runtime-owned pending AI command proposal awaiting explicit acceptance or rejection.
 */
data class AthenaAiCommandProposal(
    val proposalId: String,
    val summary: String,
    val rationale: String,
    val command: AthenaCommand,
)

/**
 * Serializable snapshot of the current pending AI proposal queue.
 */
data class AthenaAiProposalQueueSnapshot(
    val proposals: List<AthenaAiCommandProposal>,
    val nextProposalOrdinal: Int,
)

/**
 * Outcome of queuing one AI-originated command proposal.
 */
sealed interface AthenaAiCommandProposalSubmissionResult

/**
 * AI-originated command proposal accepted into the pending queue.
 */
data class AthenaAiCommandProposalSubmitted(
    val proposal: AthenaAiCommandProposal,
) : AthenaAiCommandProposalSubmissionResult

/**
 * Outcome of attempting to accept one pending AI proposal into canonical command history.
 */
sealed interface AthenaAiCommandProposalAcceptanceResult : AthenaMutationResult {
    /**
     * Stable proposal identifier associated with the acceptance attempt.
     */
    val proposalId: String
}

/**
 * Pending AI proposal was accepted and executed through the normal command runtime path.
 */
data class AthenaAiCommandProposalAccepted(
    val proposal: AthenaAiCommandProposal,
    val execution: AthenaCommandExecutionSuccess,
) : AthenaAiCommandProposalAcceptanceResult, AthenaMutationResult by execution {
    override val proposalId: String
        get() = proposal.proposalId
}

/**
 * Pending AI proposal could not be accepted, but canonical state was left unchanged.
 */
data class AthenaAiCommandProposalAcceptanceRejected(
    override val proposalId: String,
    val reason: String,
    override val projectName: String = "",
    override val mutationCategory: AthenaMutationCategory = AthenaMutationCategory.SEMANTIC_MUTATION,
) : AthenaAiCommandProposalAcceptanceResult {
    override val outcome: AthenaMutationOutcome = AthenaMutationOutcome.REJECTED
    override val validationFeedback: List<AthenaMutationValidationFeedback> = emptyList()
}

/**
 * Pending AI proposal produced runtime-owned validation feedback and remains outside canonical history.
 */
data class AthenaAiCommandProposalAcceptanceValidationFeedback(
    val proposal: AthenaAiCommandProposal,
    val execution: AthenaCommandExecutionValidationFeedback,
) : AthenaAiCommandProposalAcceptanceResult, AthenaMutationResult by execution {
    override val proposalId: String
        get() = proposal.proposalId
}

/**
 * Pending AI proposal could not be accepted because the runtime had no usable canonical state.
 */
data class AthenaAiCommandProposalAcceptanceUnavailable(
    override val proposalId: String,
    val reason: String,
    override val projectName: String = "",
    override val mutationCategory: AthenaMutationCategory = AthenaMutationCategory.SEMANTIC_MUTATION,
) : AthenaAiCommandProposalAcceptanceResult {
    override val outcome: AthenaMutationOutcome = AthenaMutationOutcome.UNAVAILABLE
    override val validationFeedback: List<AthenaMutationValidationFeedback> = emptyList()
}

/**
 * Outcome of attempting to reject one pending AI proposal.
 */
sealed interface AthenaAiCommandProposalRejectionResult

/**
 * Pending AI proposal was explicitly rejected and removed from the queue.
 */
data class AthenaAiCommandProposalRejected(
    val proposal: AthenaAiCommandProposal,
) : AthenaAiCommandProposalRejectionResult

/**
 * Requested AI proposal rejection could not be completed.
 */
data class AthenaAiCommandProposalRejectionRejected(
    val proposalId: String,
    val reason: String,
) : AthenaAiCommandProposalRejectionResult

/**
 * Runtime-owned optional AI proposal boundary.
 *
 * AI suggestions stay outside canonical project state until an explicit acceptance routes the proposal through the
 * same command runtime path used by every other mutation surface.
 */
class AthenaAiProposalRuntimeService internal constructor() {
    /**
     * Queues one AI-originated command proposal without mutating canonical project state.
     */
    fun submit(
        context: AthenaExecutionContext,
        draft: AthenaAiCommandProposalDraft,
    ): AthenaAiCommandProposalSubmissionResult {
        val state = context.aiProposalState()
        val proposal = AthenaAiCommandProposal(
            proposalId = "ai-proposal-${state.nextProposalOrdinal.toString().padStart(4, '0')}",
            summary = draft.summary,
            rationale = draft.rationale,
            command = draft.command,
        )
        context.replaceAiProposalState(
            state.copy(
                proposals = state.proposals + proposal,
                nextProposalOrdinal = state.nextProposalOrdinal + 1,
            ),
        )
        return AthenaAiCommandProposalSubmitted(proposal)
    }

    /**
     * Returns the current runtime-owned pending AI proposal queue.
     */
    fun pendingProposals(context: AthenaExecutionContext): List<AthenaAiCommandProposal> {
        return context.aiProposalState().proposals
    }

    /**
     * Returns a deterministic snapshot of the current pending AI proposal queue.
     */
    fun snapshot(context: AthenaExecutionContext): AthenaAiProposalQueueSnapshot {
        val state = context.aiProposalState()
        return AthenaAiProposalQueueSnapshot(
            proposals = state.proposals,
            nextProposalOrdinal = state.nextProposalOrdinal,
        )
    }

    /**
     * Restores the current pending AI proposal queue from persisted runtime-owned state.
     */
    fun restorePendingProposals(
        context: AthenaExecutionContext,
        snapshot: AthenaAiProposalQueueSnapshot,
    ) {
        context.replaceAiProposalState(
            AthenaAiProposalState(
                proposals = snapshot.proposals,
                nextProposalOrdinal = snapshot.nextProposalOrdinal,
            ),
        )
    }

    /**
     * Accepts one pending AI proposal and routes it through the standard command runtime path.
     */
    fun acceptProposal(
        context: AthenaExecutionContext,
        proposalId: String,
    ): AthenaAiCommandProposalAcceptanceResult {
        val state = context.aiProposalState()
        val proposal = state.proposals.firstOrNull { candidate -> candidate.proposalId == proposalId }
            ?: return AthenaAiCommandProposalAcceptanceRejected(
                proposalId = proposalId,
                reason = "No pending AI proposal matches `$proposalId`.",
                projectName = context.project.name,
            )

        return when (
            val execution = context.commandRuntime().execute(
                context = context,
                command = proposal.command,
                origin = AthenaCommandOrigin.AI_ACCEPTED,
            )
        ) {
            is AthenaCommandExecutionSuccess -> {
                context.replaceAiProposalState(
                    state.copy(
                        proposals = state.proposals.filterNot { candidate -> candidate.proposalId == proposalId },
                    ),
                )
                AthenaAiCommandProposalAccepted(
                    proposal = proposal,
                    execution = execution,
                )
            }

            is AthenaCommandExecutionRejected -> AthenaAiCommandProposalAcceptanceRejected(
                proposalId = proposalId,
                reason = execution.reason,
                projectName = context.project.name,
                mutationCategory = proposal.command.mutationCategory,
            )

            is AthenaCommandExecutionValidationFeedback -> AthenaAiCommandProposalAcceptanceValidationFeedback(
                proposal = proposal,
                execution = execution,
            )

            is AthenaCommandExecutionUnavailable -> AthenaAiCommandProposalAcceptanceUnavailable(
                proposalId = proposalId,
                reason = execution.reason,
                projectName = context.project.name,
                mutationCategory = proposal.command.mutationCategory,
            )
        }
    }

    /**
     * Explicitly rejects one pending AI proposal without mutating canonical project state.
     */
    fun rejectProposal(
        context: AthenaExecutionContext,
        proposalId: String,
    ): AthenaAiCommandProposalRejectionResult {
        val state = context.aiProposalState()
        val proposal = state.proposals.firstOrNull { candidate -> candidate.proposalId == proposalId }
            ?: return AthenaAiCommandProposalRejectionRejected(
                proposalId = proposalId,
                reason = "No pending AI proposal matches `$proposalId`.",
            )

        context.replaceAiProposalState(
            state.copy(
                proposals = state.proposals.filterNot { candidate -> candidate.proposalId == proposalId },
            ),
        )
        return AthenaAiCommandProposalRejected(proposal)
    }
}

/**
 * Internal runtime-owned pending AI proposal state for one active project.
 */
internal data class AthenaAiProposalState(
    val proposals: List<AthenaAiCommandProposal> = emptyList(),
    val nextProposalOrdinal: Int = 1,
)
