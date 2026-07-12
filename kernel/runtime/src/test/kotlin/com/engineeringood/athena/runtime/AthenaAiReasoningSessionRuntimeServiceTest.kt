package com.engineeringood.athena.runtime

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AthenaAiReasoningSessionRuntimeServiceTest {
    @Test
    fun `submitting a successful reasoning session records provider-neutral session and proposal`() {
        val sourcePath = writeProject(knowledgeFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "ai-reasoning",
                sourcePath = sourcePath,
            )
            val canonicalCompilation = context.compileActiveProject()
            var capturedRequest: AthenaAiReasoningProviderRequest? = null

            val submitted = assertIs<AthenaAiReasoningSessionSubmitted>(
                context.aiReasoningSessions().submit(
                    context = context,
                    request = AthenaAiReasoningSessionRequest(
                        requestCategory = AthenaAiReasoningRequestCategory.DIAGNOSTIC_EXPLANATION,
                        subjectSemanticIds = listOf("component:M1"),
                    ),
                    provider = AthenaAiReasoningProvider { request ->
                        capturedRequest = request
                        AthenaAiReasoningProviderSuccess(
                            summary = "Breaker sizing explanation",
                            response = "Breaker QF1 current is below the required load current for M1.",
                            providerId = "mock-provider",
                        )
                    },
                ),
            )

            assertEquals("ai-reasoning-session-0001", submitted.session.sessionId)
            assertEquals("ai-reasoning-0001", submitted.proposal.proposalId)
            assertEquals(AthenaAiReasoningProviderResultStatus.SUCCESS, submitted.session.providerStatus)
            assertEquals("mock-provider", submitted.session.providerId)
            assertEquals(listOf("component:M1"), submitted.session.subjectSemanticIds)
            assertEquals("ai-reasoning-session-0001", capturedRequest?.sessionId)
            assertEquals(listOf("component:M1"), capturedRequest?.context?.subjectSemanticIds)
            assertSame(canonicalCompilation, context.compileActiveProject())
            assertTrue(context.commandRuntime().history(context).records.isEmpty())
            assertEquals(listOf(submitted.session), context.aiReasoningSessions().sessions(context))
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `provider unavailable result records unavailable proposal state without semantic mutation`() {
        val sourcePath = writeProject(knowledgeFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "ai-reasoning",
                sourcePath = sourcePath,
            )

            val submitted = assertIs<AthenaAiReasoningSessionSubmitted>(
                context.aiReasoningSessions().submit(
                    context = context,
                    request = AthenaAiReasoningSessionRequest(
                        requestCategory = AthenaAiReasoningRequestCategory.IMPACT_SUMMARY,
                        subjectSemanticIds = listOf("component:M1"),
                    ),
                    provider = AthenaAiReasoningProvider {
                        AthenaAiReasoningProviderUnavailable(
                            reason = "No AI provider is configured for this workspace.",
                        )
                    },
                ),
            )

            assertEquals(AthenaAiReasoningProviderResultStatus.UNAVAILABLE, submitted.session.providerStatus)
            assertEquals(AthenaAiReasoningProviderResultStatus.UNAVAILABLE, submitted.proposal.providerStatus)
            assertEquals(AthenaAiReasoningProposalDecisionState.UNAVAILABLE, submitted.proposal.decisionState)
            assertEquals("Impact summary unavailable", submitted.proposal.summary)
            assertEquals("No AI provider is configured for this workspace.", submitted.proposal.response)
            assertTrue(context.commandRuntime().history(context).records.isEmpty())
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `failed provider outcome can be snapshotted and restored through runtime session state`() {
        val sourcePath = writeProject(knowledgeFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "ai-reasoning",
                sourcePath = sourcePath,
            )

            val submitted = assertIs<AthenaAiReasoningSessionSubmitted>(
                context.aiReasoningSessions().submit(
                    context = context,
                    request = AthenaAiReasoningSessionRequest(
                        requestCategory = AthenaAiReasoningRequestCategory.NEXT_CHECK,
                        subjectSemanticIds = listOf("component:M1"),
                    ),
                    provider = AthenaAiReasoningProvider {
                        AthenaAiReasoningProviderFailure(
                            reason = "Mock provider timed out during next-check generation.",
                            providerId = "mock-provider",
                        )
                    },
                ),
            )

            val snapshot = context.aiReasoningSessions().snapshot(context)
            val restoredContext = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "ai-reasoning-restored",
                sourcePath = sourcePath,
            )
            val restoredService = AthenaAiReasoningSessionRuntimeService()
            restoredService.restoreSessions(restoredContext, snapshot)

            assertEquals(AthenaAiReasoningProviderResultStatus.FAILED, submitted.session.providerStatus)
            assertEquals(AthenaAiReasoningProposalDecisionState.UNAVAILABLE, submitted.proposal.decisionState)
            assertEquals(listOf(submitted.session), restoredService.sessions(restoredContext))
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-ai-reasoning-session-", ".athena")
        Files.writeString(path, source)
        return path
    }

    private fun knowledgeFixture(): String {
        return """
            system AiReasoningSessionDemo {
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
