package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.runtime.AthenaAiReasoningContext
import com.engineeringood.athena.runtime.AthenaAiReasoningEvidenceRef
import com.engineeringood.athena.runtime.AthenaAiReasoningProposal
import com.engineeringood.athena.runtime.AthenaAiReasoningProposalDecisionResult
import com.engineeringood.athena.runtime.AthenaAiReasoningProposalDecisionUpdated
import com.engineeringood.athena.runtime.AthenaAiReasoningRequestCategory
import com.engineeringood.athena.runtime.AthenaAiReasoningSessionRuntimeService
import com.engineeringood.athena.runtime.AthenaAiReasoningSession
import com.engineeringood.athena.runtime.AthenaAiReasoningSessionRequest
import com.engineeringood.athena.runtime.AthenaAiReasoningSessionSubmitted

/**
 * Transport DTO for one Athena AI reasoning request through the LSP boundary.
 */
data class AthenaAiReasoningRequestParams(
    val requestCategory: String,
    val subjectSemanticIds: List<String> = emptyList(),
    val baseline: AthenaSemanticScmStateParams? = null,
)

/**
 * Transport DTO for one cited reasoning evidence item.
 */
data class AthenaAiReasoningEvidencePayload(
    val kind: String,
    val referenceId: String,
    val summary: String,
)

/**
 * Transport DTO for one runtime-owned reasoning proposal.
 */
data class AthenaAiReasoningProposalPayload(
    val proposalId: String,
    val proposalCategory: String,
    val providerStatus: String,
    val decisionState: String,
    val summary: String,
    val response: String,
    val providerId: String? = null,
    val subjectSemanticIds: List<String>,
    val evidence: List<AthenaAiReasoningEvidencePayload>,
)

/**
 * Transport DTO for one runtime-owned reasoning session.
 */
data class AthenaAiReasoningSessionPayload(
    val sessionId: String,
    val requestCategory: String,
    val providerStatus: String,
    val providerId: String? = null,
    val subjectSemanticIds: List<String>,
    val proposalId: String,
    val semanticPath: String,
)

/**
 * Transport DTO returned for one reasoning request submission.
 */
data class AthenaAiReasoningSubmissionPayload(
    val session: AthenaAiReasoningSessionPayload,
    val proposal: AthenaAiReasoningProposalPayload,
)

/**
 * Transport DTO for runtime-owned reasoning state inspection.
 */
data class AthenaAiReasoningStatePayload(
    val sessions: List<AthenaAiReasoningSessionPayload>,
    val proposals: List<AthenaAiReasoningProposalPayload>,
)

/**
 * Transport DTO for one explicit proposal-decision request.
 */
data class AthenaAiReasoningDecisionParams(
    val proposalId: String,
    val decision: String,
)

internal fun AthenaAiReasoningRequestParams.toRuntimeRequest(
    activation: AthenaLspSessionHostReady,
): AthenaAiReasoningSessionRequest {
    val category = requestCategory.toRuntimeCategory()
    val reviewSummary = baseline?.let { baselineParams ->
        activation.context.services.semanticScmStates()
            .inspect(
                session = activation.session,
                descriptor = baselineParams.toBaselineDescriptor(),
                locator = baselineParams.toBaselineLocator(),
            )
            .reviewSummary
    }
    return AthenaAiReasoningSessionRequest(
        requestCategory = category,
        subjectSemanticIds = subjectSemanticIds,
        reviewSummary = reviewSummary,
    )
}

internal fun AthenaLspSessionHostReady.reasoningStatePayload(
    semanticPath: String,
): AthenaAiReasoningStatePayload {
    val sessionRuntime = context.aiReasoningSessions()
    val reasoningRuntime = context.aiReasoningRuntime()
    return AthenaAiReasoningStatePayload(
        sessions = sessionRuntime.sessions(context).map { session -> session.toPayload(semanticPath) },
        proposals = reasoningRuntime.proposals(context).map { proposal -> proposal.toPayload() },
    )
}

internal fun AthenaAiReasoningDecisionParams.applyTo(
    activation: AthenaLspSessionHostReady,
): AthenaAiReasoningProposalDecisionResult {
    return when (decision.trim().lowercase()) {
        "accepted" -> activation.context.aiReasoningRuntime().markAccepted(
            activation.context,
            proposalId,
        )

        "dismissed" -> activation.context.aiReasoningRuntime().markDismissed(
            activation.context,
            proposalId,
        )

        else -> error("Athena AI reasoning decision must be either accepted or dismissed.")
    }
}

internal fun AthenaAiReasoningProposalDecisionResult.toPayload(): AthenaAiReasoningProposalPayload {
    return when (this) {
        is AthenaAiReasoningProposalDecisionUpdated -> proposal.toPayload()
        else -> error("Athena AI reasoning decision result is not representable as a successful proposal payload.")
    }
}

internal fun AthenaAiReasoningSessionSubmitted.toPayload(
    semanticPath: String,
): AthenaAiReasoningSubmissionPayload {
    return AthenaAiReasoningSubmissionPayload(
        session = session.toPayload(semanticPath),
        proposal = proposal.toPayload(),
    )
}

private fun AthenaAiReasoningSession.toPayload(
    semanticPath: String,
): AthenaAiReasoningSessionPayload {
    return AthenaAiReasoningSessionPayload(
        sessionId = sessionId,
        requestCategory = requestCategory.toProtocolValue(),
        providerStatus = providerStatus.toProtocolValue(),
        providerId = providerId,
        subjectSemanticIds = subjectSemanticIds,
        proposalId = proposalId,
        semanticPath = semanticPath,
    )
}

private fun AthenaAiReasoningProposal.toPayload(): AthenaAiReasoningProposalPayload {
    return AthenaAiReasoningProposalPayload(
        proposalId = proposalId,
        proposalCategory = proposalCategory.toProtocolValue(),
        providerStatus = providerStatus.toProtocolValue(),
        decisionState = decisionState.toProtocolValue(),
        summary = summary,
        response = response,
        providerId = providerId,
        subjectSemanticIds = context.subjectSemanticIds,
        evidence = context.evidence.map(AthenaAiReasoningEvidenceRef::toPayload),
    )
}

private fun AthenaAiReasoningEvidenceRef.toPayload(): AthenaAiReasoningEvidencePayload {
    return AthenaAiReasoningEvidencePayload(
        kind = kind.toProtocolValue(),
        referenceId = referenceId,
        summary = summary,
    )
}

private fun AthenaAiReasoningRequestCategory.toProtocolValue(): String {
    return name.lowercase().replace('_', '-')
}

private fun Enum<*>.toProtocolValue(): String {
    return name.lowercase().replace('_', '-')
}

private fun String.toRuntimeCategory(): AthenaAiReasoningRequestCategory {
    return when (trim().lowercase()) {
        "diagnostic-explanation" -> AthenaAiReasoningRequestCategory.DIAGNOSTIC_EXPLANATION
        "impact-summary" -> AthenaAiReasoningRequestCategory.IMPACT_SUMMARY
        "next-check" -> AthenaAiReasoningRequestCategory.NEXT_CHECK
        else -> error(
            "Athena AI reasoning requestCategory must be one of diagnostic-explanation, impact-summary, or next-check.",
        )
    }
}
