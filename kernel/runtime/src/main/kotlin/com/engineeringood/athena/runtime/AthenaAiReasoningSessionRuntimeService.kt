package com.engineeringood.athena.runtime

import com.engineeringood.athena.scm.SemanticReviewSummary

/**
 * Runtime-owned request to start one provider-neutral AI reasoning session.
 */
data class AthenaAiReasoningSessionRequest(
    val requestCategory: AthenaAiReasoningRequestCategory,
    val subjectSemanticIds: List<String> = emptyList(),
    val reviewSummary: SemanticReviewSummary? = null,
)

/**
 * Provider-neutral request passed downstream after Athena has assembled governed reasoning context.
 */
data class AthenaAiReasoningProviderRequest(
    val sessionId: String,
    val context: AthenaAiReasoningContext,
)

/**
 * Replaceable downstream provider contract for one reasoning invocation.
 */
fun interface AthenaAiReasoningProvider {
    /**
     * Produces one typed provider outcome from a governed Athena-owned request.
     */
    fun submit(request: AthenaAiReasoningProviderRequest): AthenaAiReasoningProviderOutcome
}

/**
 * Provider-neutral outcome for one reasoning invocation.
 */
sealed interface AthenaAiReasoningProviderOutcome {
    val providerStatus: AthenaAiReasoningProviderResultStatus
    val providerId: String?
}

/**
 * Provider successfully returned one reasoning proposal payload.
 */
data class AthenaAiReasoningProviderSuccess(
    val summary: String,
    val response: String,
    override val providerId: String? = null,
) : AthenaAiReasoningProviderOutcome {
    override val providerStatus: AthenaAiReasoningProviderResultStatus =
        AthenaAiReasoningProviderResultStatus.SUCCESS
}

/**
 * Provider path was unavailable, for example because no provider is configured or reachable.
 */
data class AthenaAiReasoningProviderUnavailable(
    val reason: String,
    override val providerId: String? = null,
) : AthenaAiReasoningProviderOutcome {
    override val providerStatus: AthenaAiReasoningProviderResultStatus =
        AthenaAiReasoningProviderResultStatus.UNAVAILABLE
}

/**
 * Provider path was reached but failed to produce a usable reasoning proposal.
 */
data class AthenaAiReasoningProviderFailure(
    val reason: String,
    override val providerId: String? = null,
) : AthenaAiReasoningProviderOutcome {
    override val providerStatus: AthenaAiReasoningProviderResultStatus =
        AthenaAiReasoningProviderResultStatus.FAILED
}

/**
 * Runtime-owned session record for one reasoning invocation attempt.
 */
data class AthenaAiReasoningSession(
    val sessionId: String,
    val requestCategory: AthenaAiReasoningRequestCategory,
    val subjectSemanticIds: List<String>,
    val providerStatus: AthenaAiReasoningProviderResultStatus,
    val providerId: String? = null,
    val proposalId: String,
)

/**
 * Serializable snapshot of runtime-owned reasoning sessions for one active project.
 */
data class AthenaAiReasoningSessionSnapshot(
    val sessions: List<AthenaAiReasoningSession>,
    val nextSessionOrdinal: Int,
)

/**
 * Outcome of submitting one runtime-owned reasoning session.
 */
sealed interface AthenaAiReasoningSessionSubmissionResult

/**
 * Runtime recorded one reasoning session and its linked proposal successfully.
 */
data class AthenaAiReasoningSessionSubmitted(
    val session: AthenaAiReasoningSession,
    val proposal: AthenaAiReasoningProposal,
) : AthenaAiReasoningSessionSubmissionResult

/**
 * Runtime-owned session orchestrator above deterministic context assembly and below provider transports.
 */
class AthenaAiReasoningSessionRuntimeService internal constructor() {
    /**
     * Submits one provider-neutral reasoning session without mutating canonical engineering truth.
     */
    fun submit(
        context: AthenaExecutionContext,
        request: AthenaAiReasoningSessionRequest,
        provider: AthenaAiReasoningProvider,
    ): AthenaAiReasoningSessionSubmissionResult {
        val state = context.aiReasoningSessionState()
        val sessionId = "ai-reasoning-session-${state.nextSessionOrdinal.toString().padStart(4, '0')}"
        val reasoningContext = context.aiReasoningRuntime().assembleContext(
            context = context,
            request = request.toContextRequest(),
        )
        val providerOutcome = provider.submit(
            AthenaAiReasoningProviderRequest(
                sessionId = sessionId,
                context = reasoningContext,
            ),
        )
        val proposal = when (
            val result = context.aiReasoningRuntime().recordProposal(
                context = context,
                draft = providerOutcome.toProposalDraft(reasoningContext),
            )
        ) {
            is AthenaAiReasoningProposalRecorded -> result.proposal
        }
        val session = AthenaAiReasoningSession(
            sessionId = sessionId,
            requestCategory = request.requestCategory,
            subjectSemanticIds = reasoningContext.subjectSemanticIds,
            providerStatus = providerOutcome.providerStatus,
            providerId = providerOutcome.providerId,
            proposalId = proposal.proposalId,
        )
        context.replaceAiReasoningSessionState(
            state.copy(
                sessions = state.sessions + session,
                nextSessionOrdinal = state.nextSessionOrdinal + 1,
            ),
        )
        return AthenaAiReasoningSessionSubmitted(
            session = session,
            proposal = proposal,
        )
    }

    /**
     * Returns every stored runtime-owned reasoning session for the active project.
     */
    fun sessions(context: AthenaExecutionContext): List<AthenaAiReasoningSession> {
        return context.aiReasoningSessionState().sessions
    }

    /**
     * Returns a deterministic snapshot of stored reasoning sessions.
     */
    fun snapshot(context: AthenaExecutionContext): AthenaAiReasoningSessionSnapshot {
        val state = context.aiReasoningSessionState()
        return AthenaAiReasoningSessionSnapshot(
            sessions = state.sessions,
            nextSessionOrdinal = state.nextSessionOrdinal,
        )
    }

    /**
     * Restores stored reasoning sessions from a runtime-owned snapshot.
     */
    fun restoreSessions(
        context: AthenaExecutionContext,
        snapshot: AthenaAiReasoningSessionSnapshot,
    ) {
        context.replaceAiReasoningSessionState(
            AthenaAiReasoningSessionState(
                sessions = snapshot.sessions,
                nextSessionOrdinal = snapshot.nextSessionOrdinal,
            ),
        )
    }
}

/**
 * Internal runtime-owned AI reasoning session state for one active project.
 */
internal data class AthenaAiReasoningSessionState(
    val sessions: List<AthenaAiReasoningSession> = emptyList(),
    val nextSessionOrdinal: Int = 1,
)

private fun AthenaAiReasoningSessionRequest.toContextRequest(): AthenaAiReasoningContextRequest {
    return AthenaAiReasoningContextRequest(
        requestCategory = requestCategory,
        subjectSemanticIds = subjectSemanticIds,
        reviewSummary = reviewSummary,
    )
}

private fun AthenaAiReasoningProviderOutcome.toProposalDraft(
    context: AthenaAiReasoningContext,
): AthenaAiReasoningProposalDraft {
    return when (this) {
        is AthenaAiReasoningProviderSuccess -> AthenaAiReasoningProposalDraft(
            proposalCategory = context.requestCategory.toProposalCategory(),
            providerStatus = providerStatus,
            summary = summary,
            response = response,
            context = context,
            providerId = providerId,
        )

        is AthenaAiReasoningProviderUnavailable -> AthenaAiReasoningProposalDraft(
            proposalCategory = context.requestCategory.toProposalCategory(),
            providerStatus = providerStatus,
            summary = context.requestCategory.unavailableSummary(),
            response = reason,
            context = context,
            providerId = providerId,
        )

        is AthenaAiReasoningProviderFailure -> AthenaAiReasoningProposalDraft(
            proposalCategory = context.requestCategory.toProposalCategory(),
            providerStatus = providerStatus,
            summary = context.requestCategory.failureSummary(),
            response = reason,
            context = context,
            providerId = providerId,
        )
    }
}

private fun AthenaAiReasoningRequestCategory.toProposalCategory(): AthenaAiReasoningProposalCategory {
    return when (this) {
        AthenaAiReasoningRequestCategory.DIAGNOSTIC_EXPLANATION ->
            AthenaAiReasoningProposalCategory.DIAGNOSTIC_EXPLANATION

        AthenaAiReasoningRequestCategory.IMPACT_SUMMARY ->
            AthenaAiReasoningProposalCategory.IMPACT_SUMMARY

        AthenaAiReasoningRequestCategory.NEXT_CHECK ->
            AthenaAiReasoningProposalCategory.NEXT_CHECK
    }
}

private fun AthenaAiReasoningRequestCategory.unavailableSummary(): String {
    return when (this) {
        AthenaAiReasoningRequestCategory.DIAGNOSTIC_EXPLANATION -> "Diagnostic explanation unavailable"
        AthenaAiReasoningRequestCategory.IMPACT_SUMMARY -> "Impact summary unavailable"
        AthenaAiReasoningRequestCategory.NEXT_CHECK -> "Next check unavailable"
    }
}

private fun AthenaAiReasoningRequestCategory.failureSummary(): String {
    return when (this) {
        AthenaAiReasoningRequestCategory.DIAGNOSTIC_EXPLANATION -> "Diagnostic explanation failed"
        AthenaAiReasoningRequestCategory.IMPACT_SUMMARY -> "Impact summary failed"
        AthenaAiReasoningRequestCategory.NEXT_CHECK -> "Next check failed"
    }
}
