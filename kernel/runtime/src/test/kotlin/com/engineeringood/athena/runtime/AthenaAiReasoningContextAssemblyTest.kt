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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AthenaAiReasoningContextAssemblyTest {
    @Test
    fun `assembling same reasoning context twice yields identical deterministic output`() {
        val sourcePath = writeProject(knowledgeFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "ai-reasoning",
                sourcePath = sourcePath,
            )

            val request = AthenaAiReasoningContextRequest(
                requestCategory = AthenaAiReasoningRequestCategory.DIAGNOSTIC_EXPLANATION,
                subjectSemanticIds = listOf("component:M1", "component:M1"),
            )

            val first = context.aiReasoningRuntime().assembleContext(context, request)
            val second = context.aiReasoningRuntime().assembleContext(context, request)

            assertEquals(first, second)
            assertEquals(listOf("component:M1"), first.subjectSemanticIds)
            assertTrue(first.evidence.any { evidence -> evidence.kind == AthenaAiReasoningEvidenceKind.DERIVED_CONTEXT })
            assertTrue(first.evidence.any { evidence -> evidence.kind == AthenaAiReasoningEvidenceKind.CAPABILITY_FACT })
            assertTrue(first.evidence.any { evidence -> evidence.kind == AthenaAiReasoningEvidenceKind.CONSTRAINT_EVALUATION })
            assertTrue(first.evidence.any { evidence -> evidence.kind == AthenaAiReasoningEvidenceKind.DIAGNOSTIC })
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `impact summary context includes governed impact and review facts when available`() {
        val sourcePath = writeProject(knowledgeFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
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

            val assembled = context.aiReasoningRuntime().assembleContext(
                context = context,
                request = AthenaAiReasoningContextRequest(
                    requestCategory = AthenaAiReasoningRequestCategory.IMPACT_SUMMARY,
                    subjectSemanticIds = listOf("component:M1"),
                    reviewSummary = reviewSummary,
                ),
            )

            assertTrue(assembled.evidence.any { evidence -> evidence.kind == AthenaAiReasoningEvidenceKind.IMPACT_CONSEQUENCE })
            assertTrue(assembled.evidence.any { evidence ->
                evidence.kind == AthenaAiReasoningEvidenceKind.REVIEW_ENTRY &&
                    evidence.referenceId == "review-fact:m1-protection"
            })
            assertEquals(listOf("component:M1", "component:QF1"), assembled.subjectSemanticIds)
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-ai-reasoning-context-", ".athena")
        Files.writeString(path, source)
        return path
    }

    private fun knowledgeFixture(): String {
        return """
            system AiReasoningContextDemo {
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
