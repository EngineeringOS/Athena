package com.engineeringood.athena.runtime

import com.engineeringood.athena.ir.EngineeringImpactConsequence
import com.engineeringood.athena.ir.EngineeringImpactConsequences
import com.engineeringood.athena.ir.EngineeringImpactReasonKind
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticReviewEntry
import com.engineeringood.athena.scm.SemanticReviewEntryKind
import com.engineeringood.athena.scm.SemanticReviewFactKind
import com.engineeringood.athena.scm.SemanticReviewFactReference
import com.engineeringood.athena.scm.SemanticReviewSummary
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaAiDeterministicProofProviderTest {
    private val provider = AthenaAiDeterministicProofProvider()

    @Test
    fun `diagnostic explanation stays grounded in governed evidence`() {
        val sourcePath = writeProject(knowledgeFixture())

        try {
            val context = AthenaRuntime().openWorkspace(sourcePath.parent).activateProject(
                projectName = "ai-reasoning",
                sourcePath = sourcePath,
            )
            val reasoningContext = context.aiReasoningRuntime().assembleContext(
                context = context,
                request = AthenaAiReasoningContextRequest(
                    requestCategory = AthenaAiReasoningRequestCategory.DIAGNOSTIC_EXPLANATION,
                    subjectSemanticIds = listOf("component:M1"),
                ),
            )

            val outcome = assertIs<AthenaAiReasoningProviderSuccess>(
                provider.submit(
                    AthenaAiReasoningProviderRequest(
                        sessionId = "session-1",
                        context = reasoningContext,
                    ),
                ),
            )

            assertEquals(AthenaAiDeterministicProofProvider.PROVIDER_ID, outcome.providerId)
            assertContains(outcome.summary, "component:M1")
            assertContains(outcome.response, "Observed diagnostic:")
            assertContains(outcome.response, "Supporting governed evidence:")
            assertContains(outcome.response, "[diagnostic:")
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `impact summary and next check stay reconstructable from governed facts`() {
        val sourcePath = writeProject(knowledgeFixture())

        try {
            val context = AthenaRuntime().openWorkspace(sourcePath.parent).activateProject(
                projectName = "ai-reasoning",
                sourcePath = sourcePath,
            )
            val reviewSummary = SemanticReviewSummary(
                baseline = SemanticBaselineDescriptor(
                    baselineId = "baseline-001",
                    label = "before",
                ),
                engineeringImpactConsequences = EngineeringImpactConsequences.canonical(
                    listOf(
                        EngineeringImpactConsequence(
                            affectedSubjectIdentity = StableSemanticIdentity("component:M1"),
                            triggerSubjectIdentities = listOf(StableSemanticIdentity("component:QF1")),
                            reasonKinds = listOf(EngineeringImpactReasonKind.CONSTRAINT_EVALUATION_CHANGED),
                        ),
                    ),
                ),
                entries = listOf(
                    SemanticReviewEntry(
                        kind = SemanticReviewEntryKind.ENGINEERING_IMPACT,
                        message = "Protection review required for M1.",
                        subjectIdentity = StableSemanticIdentity("component:M1"),
                        factReferences = listOf(
                            SemanticReviewFactReference(
                                factKind = SemanticReviewFactKind.ENGINEERING_IMPACT,
                                identifier = "review-fact:m1-protection",
                                subjectIdentity = StableSemanticIdentity("component:M1"),
                            ),
                        ),
                    ),
                ),
            )

            val impactContext = context.aiReasoningRuntime().assembleContext(
                context = context,
                request = AthenaAiReasoningContextRequest(
                    requestCategory = AthenaAiReasoningRequestCategory.IMPACT_SUMMARY,
                    subjectSemanticIds = listOf("component:M1"),
                    reviewSummary = reviewSummary,
                ),
            )
            val nextCheckContext = context.aiReasoningRuntime().assembleContext(
                context = context,
                request = AthenaAiReasoningContextRequest(
                    requestCategory = AthenaAiReasoningRequestCategory.NEXT_CHECK,
                    subjectSemanticIds = listOf("component:M1"),
                    reviewSummary = reviewSummary,
                ),
            )

            val impactOutcome = assertIs<AthenaAiReasoningProviderSuccess>(
                provider.submit(AthenaAiReasoningProviderRequest("impact-session", impactContext)),
            )
            val nextCheckOutcome = assertIs<AthenaAiReasoningProviderSuccess>(
                provider.submit(AthenaAiReasoningProviderRequest("next-check-session", nextCheckContext)),
            )

            assertContains(impactOutcome.response, "Downstream impact:")
            assertContains(impactOutcome.response, "Review facts to inspect next:")
            assertContains(nextCheckOutcome.response, "Recommended next checks:")
            assertTrue(nextCheckOutcome.response.contains("breaker", ignoreCase = true))
            assertTrue(nextCheckOutcome.response.contains("constraint", ignoreCase = true))
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-ai-proof-provider-", ".athena")
        Files.writeString(path, source)
        return path
    }

    private fun knowledgeFixture(): String {
        return """
            system AiReasoningProof {
              device M1 {
                type Motor
                power "7.5kW"
                voltage "400V"
                powerFactor "0.86"
                efficiency "0.92"
                breakerRatedCurrent "10A"
                cableAllowedCurrent "12A"
                relayRatedCurrent "13A"
              }
            }
        """.trimIndent()
    }
}
