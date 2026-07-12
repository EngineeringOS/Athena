package com.engineeringood.athena.ide.lsp

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.InitializeParams

class AthenaM10ProofCorpusTest {
    @Test
    @Suppress("DEPRECATION")
    fun `m10 proof corpus verifies diagnostic explanation impact summary and next check deterministically`() {
        val repoRoot = resolveRepoRoot()
        val current = repoRoot.resolve("examples/m10/reasoning-proof/current")
        val server = AthenaLanguageServer()

        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = current.toUri().toString()
                },
            ).get()

            val explanation = server.aiReasoning(
                AthenaAiReasoningRequestParams(
                    requestCategory = "diagnostic-explanation",
                    subjectSemanticIds = listOf("component:M1"),
                ),
            ).get()
            val impact = server.aiReasoning(
                AthenaAiReasoningRequestParams(
                    requestCategory = "impact-summary",
                    subjectSemanticIds = listOf("component:M1"),
                    baseline = AthenaSemanticScmStateParams(
                        adapterId = "scm-git",
                        locator = "../baseline",
                        locatorLabel = "M10 baseline",
                        baselineId = "m10-proof-baseline",
                        baselineLabel = "M10 baseline",
                    ),
                ),
            ).get()
            val scmState = server.semanticScmState(
                AthenaSemanticScmStateParams(
                    adapterId = "scm-git",
                    locator = "../baseline",
                    locatorLabel = "M10 baseline",
                    baselineId = "m10-proof-baseline",
                    baselineLabel = "M10 baseline",
                ),
            ).get()
            val nextCheck = server.aiReasoning(
                AthenaAiReasoningRequestParams(
                    requestCategory = "next-check",
                    subjectSemanticIds = listOf("component:M1"),
                    baseline = AthenaSemanticScmStateParams(
                        adapterId = "scm-git",
                        locator = "../baseline",
                        locatorLabel = "M10 baseline",
                        baselineId = "m10-proof-baseline",
                        baselineLabel = "M10 baseline",
                    ),
                ),
            ).get()
            val state = server.aiReasoningState().get()
            val explanationPayload = assertNotNull(explanation)
            val impactPayload = assertNotNull(impact)
            val scmStatePayload = assertNotNull(scmState)
            val nextCheckPayload = assertNotNull(nextCheck)
            val statePayload = assertNotNull(state)

            assertEquals("athena-deterministic-proof", explanationPayload.proposal.providerId)
            assertEquals("success", explanationPayload.session.providerStatus)
            assertEquals("ready", scmStatePayload.status)
            assertNotNull(scmStatePayload.review)
            assertTrue(explanationPayload.proposal.response.contains("Observed diagnostic:"))
            assertTrue(
                impactPayload.proposal.response.contains("Downstream impact:") ||
                    impactPayload.proposal.response.contains("Review facts to inspect next:"),
            )
            assertTrue(nextCheckPayload.proposal.response.contains("Recommended next checks:"))
            assertTrue(impactPayload.proposal.evidence.any { evidence -> evidence.kind == "impact-consequence" })
            assertTrue(impactPayload.proposal.evidence.any { evidence -> evidence.kind == "review-entry" })
            assertEquals(3, statePayload.sessions.size)
            assertEquals(3, statePayload.proposals.size)
            assertTrue(statePayload.proposals.all { proposal -> proposal.providerStatus == "success" })
        } finally {
            server.shutdown().get()
        }
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        check(Files.exists(current.resolve("settings.gradle.kts"))) {
            "Could not locate repository root for M10 proof corpus test."
        }
        return current
    }
}
