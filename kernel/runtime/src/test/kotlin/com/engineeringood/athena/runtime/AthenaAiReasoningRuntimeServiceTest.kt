package com.engineeringood.athena.runtime

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaAiReasoningRuntimeServiceTest {
    @Test
    fun `recording a reasoning proposal normalizes governed evidence and starts unresolved`() {
        val sourcePath = writeProject(validFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "ai-reasoning",
                sourcePath = sourcePath,
            )

            val recorded = assertIs<AthenaAiReasoningProposalRecorded>(
                context.aiReasoningRuntime().recordProposal(
                    context = context,
                    draft = AthenaAiReasoningProposalDraft(
                        proposalCategory = AthenaAiReasoningProposalCategory.DIAGNOSTIC_EXPLANATION,
                        providerStatus = AthenaAiReasoningProviderResultStatus.SUCCESS,
                        summary = "Explain breaker insufficiency",
                        response = "Breaker current is lower than required load current.",
                        context = AthenaAiReasoningContext(
                            projectName = "ai-reasoning",
                            requestCategory = AthenaAiReasoningRequestCategory.DIAGNOSTIC_EXPLANATION,
                            subjectSemanticIds = listOf("component:QF1", "component:M1", "component:QF1"),
                            evidence = listOf(
                                AthenaAiReasoningEvidenceRef(
                                    kind = AthenaAiReasoningEvidenceKind.DIAGNOSTIC,
                                    referenceId = "diagnostic:breaker-insufficient",
                                    summary = "Breaker QF1 undersized for M1",
                                ),
                                AthenaAiReasoningEvidenceRef(
                                    kind = AthenaAiReasoningEvidenceKind.CAPABILITY_FACT,
                                    referenceId = "fact:required-current",
                                    summary = "Required current exceeds breaker rating",
                                ),
                            ),
                        ),
                    ),
                ),
            )

            assertEquals("ai-reasoning-0001", recorded.proposal.proposalId)
            assertEquals(
                listOf("component:M1", "component:QF1"),
                recorded.proposal.context.subjectSemanticIds,
            )
            assertEquals(
                listOf(
                    AthenaAiReasoningEvidenceKind.CAPABILITY_FACT,
                    AthenaAiReasoningEvidenceKind.DIAGNOSTIC,
                ),
                recorded.proposal.context.evidence.map { evidence -> evidence.kind },
            )
            assertEquals(
                AthenaAiReasoningProposalDecisionState.UNRESOLVED,
                recorded.proposal.decisionState,
            )
            assertEquals(
                AthenaAiReasoningProviderResultStatus.SUCCESS,
                recorded.proposal.providerStatus,
            )
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `provider failure keeps reasoning audit data but surfaces unavailable decision state`() {
        val sourcePath = writeProject(validFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "ai-reasoning",
                sourcePath = sourcePath,
            )

            val recorded = assertIs<AthenaAiReasoningProposalRecorded>(
                context.aiReasoningRuntime().recordProposal(
                    context = context,
                    draft = AthenaAiReasoningProposalDraft(
                        proposalCategory = AthenaAiReasoningProposalCategory.IMPACT_SUMMARY,
                        providerStatus = AthenaAiReasoningProviderResultStatus.FAILED,
                        summary = "Impact summary unavailable",
                        response = "Provider timed out while generating summary.",
                        context = AthenaAiReasoningContext(
                            projectName = "ai-reasoning",
                            requestCategory = AthenaAiReasoningRequestCategory.IMPACT_SUMMARY,
                            subjectSemanticIds = listOf("component:M1"),
                            evidence = listOf(
                                AthenaAiReasoningEvidenceRef(
                                    kind = AthenaAiReasoningEvidenceKind.IMPACT_CONSEQUENCE,
                                    referenceId = "impact:breaker-review",
                                    summary = "Breaker review required",
                                ),
                            ),
                        ),
                    ),
                ),
            )

            assertEquals(
                AthenaAiReasoningProposalDecisionState.UNAVAILABLE,
                recorded.proposal.decisionState,
            )
            assertEquals(
                AthenaAiReasoningProviderResultStatus.FAILED,
                recorded.proposal.providerStatus,
            )
            assertEquals(1, context.aiReasoningRuntime().proposals(context).size)

            val restoredRuntime = AthenaAiReasoningRuntimeService()
            val restoredContext = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "ai-reasoning-restored",
                sourcePath = sourcePath,
            )
            restoredRuntime.restoreProposals(restoredContext, context.aiReasoningRuntime().snapshot(context))

            assertEquals(
                context.aiReasoningRuntime().proposals(context),
                restoredRuntime.proposals(restoredContext),
            )
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `accepting or dismissing a reasoning proposal updates state without touching command history`() {
        val sourcePath = writeProject(validFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "ai-reasoning",
                sourcePath = sourcePath,
            )

            val acceptedCandidate = assertIs<AthenaAiReasoningProposalRecorded>(
                context.aiReasoningRuntime().recordProposal(
                    context = context,
                    draft = successDraft("Explain breaker insufficiency", "Diagnostic explanation"),
                ),
            ).proposal
            val dismissedCandidate = assertIs<AthenaAiReasoningProposalRecorded>(
                context.aiReasoningRuntime().recordProposal(
                    context = context,
                    draft = successDraft("Suggest next checks", "Review-ready checklist"),
                ),
            ).proposal

            val accepted = assertIs<AthenaAiReasoningProposalDecisionUpdated>(
                context.aiReasoningRuntime().markAccepted(context, acceptedCandidate.proposalId),
            )
            val dismissed = assertIs<AthenaAiReasoningProposalDecisionUpdated>(
                context.aiReasoningRuntime().markDismissed(context, dismissedCandidate.proposalId),
            )

            assertEquals(AthenaAiReasoningProposalDecisionState.ACCEPTED, accepted.proposal.decisionState)
            assertEquals(AthenaAiReasoningProposalDecisionState.DISMISSED, dismissed.proposal.decisionState)
            assertTrue(context.commandRuntime().history(context).records.isEmpty())
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun successDraft(summary: String, response: String): AthenaAiReasoningProposalDraft {
        return AthenaAiReasoningProposalDraft(
            proposalCategory = AthenaAiReasoningProposalCategory.NEXT_CHECK,
            providerStatus = AthenaAiReasoningProviderResultStatus.SUCCESS,
            summary = summary,
            response = response,
            context = AthenaAiReasoningContext(
                projectName = "ai-reasoning",
                requestCategory = AthenaAiReasoningRequestCategory.NEXT_CHECK,
                subjectSemanticIds = listOf("component:M1"),
                evidence = listOf(
                    AthenaAiReasoningEvidenceRef(
                        kind = AthenaAiReasoningEvidenceKind.DIAGNOSTIC,
                        referenceId = "diagnostic:breaker-insufficient",
                        summary = "Breaker QF1 undersized for M1",
                    ),
                ),
            ),
        )
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-ai-reasoning-", ".athena")
        Files.writeString(path, source)
        return path
    }

    private fun validFixture(): String {
        return """
            system AiReasoningDemo {
              device QF1 {
                type Switch
              }

              device M1 {
                type Motor
              }
            }
        """.trimIndent()
    }
}
