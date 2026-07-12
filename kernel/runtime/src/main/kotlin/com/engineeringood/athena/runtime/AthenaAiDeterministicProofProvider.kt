package com.engineeringood.athena.runtime

/**
 * Deterministic mock provider used for governed M10 proof flows before live model reliance.
 *
 * The provider never infers engineering truth outside Athena-owned evidence. It only renders
 * stable proposal text from the already assembled reasoning context.
 */
class AthenaAiDeterministicProofProvider : AthenaAiReasoningProvider {
    override fun submit(request: AthenaAiReasoningProviderRequest): AthenaAiReasoningProviderOutcome {
        val context = request.context
        return when (context.requestCategory) {
            AthenaAiReasoningRequestCategory.DIAGNOSTIC_EXPLANATION ->
                diagnosticExplanation(context)

            AthenaAiReasoningRequestCategory.IMPACT_SUMMARY ->
                impactSummary(context)

            AthenaAiReasoningRequestCategory.NEXT_CHECK ->
                nextCheck(context)
        }
    }

    private fun diagnosticExplanation(
        context: AthenaAiReasoningContext,
    ): AthenaAiReasoningProviderOutcome {
        val diagnostic = context.evidence.firstOrNull { evidence ->
            evidence.kind == AthenaAiReasoningEvidenceKind.DIAGNOSTIC
        } ?: return AthenaAiReasoningProviderUnavailable(
            reason = "No governed diagnostic evidence is available for this explanation request.",
            providerId = PROVIDER_ID,
        )
        val supportingEvidence = context.evidence.filter { evidence ->
            evidence.kind == AthenaAiReasoningEvidenceKind.DERIVED_CONTEXT ||
                evidence.kind == AthenaAiReasoningEvidenceKind.CAPABILITY_FACT ||
                evidence.kind == AthenaAiReasoningEvidenceKind.CONSTRAINT_EVALUATION
        }.take(4)
        return AthenaAiReasoningProviderSuccess(
            summary = "Grounded diagnostic explanation for ${context.primarySubjectLabel()}",
            response = buildString {
                appendLine("Observed diagnostic:")
                appendLine("- ${diagnostic.summary} [${diagnostic.referenceId}]")
                if (supportingEvidence.isNotEmpty()) {
                    appendLine("Supporting governed evidence:")
                    supportingEvidence.forEach { evidence ->
                        appendLine("- ${evidence.summary} [${evidence.referenceId}]")
                    }
                }
                append("Interpretation: the current engineering state remains governed by the cited evidence above.")
            }.trim(),
            providerId = PROVIDER_ID,
        )
    }

    private fun impactSummary(
        context: AthenaAiReasoningContext,
    ): AthenaAiReasoningProviderOutcome {
        val impactEvidence = context.evidence.filter { evidence ->
            evidence.kind == AthenaAiReasoningEvidenceKind.IMPACT_CONSEQUENCE
        }
        val reviewEvidence = context.evidence.filter { evidence ->
            evidence.kind == AthenaAiReasoningEvidenceKind.REVIEW_ENTRY
        }
        if (impactEvidence.isEmpty() && reviewEvidence.isEmpty()) {
            return AthenaAiReasoningProviderUnavailable(
                reason = "No governed impact or review evidence is available for this impact summary request.",
                providerId = PROVIDER_ID,
            )
        }
        return AthenaAiReasoningProviderSuccess(
            summary = "Grounded impact summary for ${context.primarySubjectLabel()}",
            response = buildString {
                appendLine("Direct focus subjects:")
                context.subjectSemanticIds.takeIf { it.isNotEmpty() }?.forEach { subjectSemanticId ->
                    appendLine("- $subjectSemanticId")
                }
                if (impactEvidence.isNotEmpty()) {
                    appendLine("Downstream impact:")
                    impactEvidence.forEach { evidence ->
                        appendLine("- ${evidence.summary} [${evidence.referenceId}]")
                    }
                }
                if (reviewEvidence.isNotEmpty()) {
                    appendLine("Review facts to inspect next:")
                    reviewEvidence.forEach { evidence ->
                        appendLine("- ${evidence.summary} [${evidence.referenceId}]")
                    }
                }
            }.trim(),
            providerId = PROVIDER_ID,
        )
    }

    private fun nextCheck(
        context: AthenaAiReasoningContext,
    ): AthenaAiReasoningProviderOutcome {
        val checks = linkedSetOf<String>()
        if (context.evidence.any { evidence -> evidence.summary.contains("breaker", ignoreCase = true) }) {
            checks += "Review breaker rated current against the governed required current."
        }
        if (context.evidence.any { evidence -> evidence.summary.contains("cable", ignoreCase = true) }) {
            checks += "Review cable allowable current margin against the focused load."
        }
        if (context.evidence.any { evidence -> evidence.summary.contains("relay", ignoreCase = true) }) {
            checks += "Review overload relay sizing and trip-setting margin."
        }
        if (context.evidence.any { evidence -> evidence.kind == AthenaAiReasoningEvidenceKind.CONSTRAINT_EVALUATION }) {
            checks += "Inspect failed or warning constraint evaluations before changing authored source."
        }
        if (checks.isEmpty()) {
            return AthenaAiReasoningProviderUnavailable(
                reason = "No safe governed next-check suggestion can be derived from the available evidence.",
                providerId = PROVIDER_ID,
            )
        }
        return AthenaAiReasoningProviderSuccess(
            summary = "Review-ready next checks for ${context.primarySubjectLabel()}",
            response = buildString {
                appendLine("Recommended next checks:")
                checks.forEach { check ->
                    appendLine("- $check")
                }
                append("Advisory only: use existing semantic or mutation workflows for any real change.")
            }.trim(),
            providerId = PROVIDER_ID,
        )
    }

    private fun AthenaAiReasoningContext.primarySubjectLabel(): String {
        return subjectSemanticIds.firstOrNull() ?: projectName
    }

    companion object {
        const val PROVIDER_ID: String = "athena-deterministic-proof"
    }
}
