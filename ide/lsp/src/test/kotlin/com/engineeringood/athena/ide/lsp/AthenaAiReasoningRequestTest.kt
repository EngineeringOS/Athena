package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.runtime.AthenaAiReasoningProvider
import com.engineeringood.athena.runtime.AthenaAiReasoningProviderSuccess
import com.engineeringood.athena.runtime.AthenaAiReasoningProviderUnavailable
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.InitializeParams

class AthenaAiReasoningRequestTest {
    @Test
    @Suppress("DEPRECATION")
    fun `ai reasoning request flows through LSP and returns unavailable transport payloads from runtime owned session state`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-ai-reasoning-",
            sourceText = knowledgeSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer(
                aiReasoningProvider = AthenaAiReasoningProvider {
                    AthenaAiReasoningProviderUnavailable(
                        reason = "No AI reasoning provider is configured for the Athena LSP session.",
                    )
                },
            )
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val payload = server.aiReasoning(
                    AthenaAiReasoningRequestParams(
                        requestCategory = "diagnostic-explanation",
                        subjectSemanticIds = listOf("component:M1"),
                    ),
                ).get()

                assertNotNull(payload)
                assertEquals("diagnostic-explanation", payload.session.requestCategory)
                assertEquals("unavailable", payload.session.providerStatus)
                assertEquals("diagnostic-explanation", payload.proposal.proposalCategory)
                assertEquals("unavailable", payload.proposal.providerStatus)
                assertEquals("unavailable", payload.proposal.decisionState)
                assertEquals(listOf("component:M1"), payload.proposal.subjectSemanticIds)
                assertTrue(payload.proposal.evidence.any { evidence -> evidence.kind == "derived-context" })
                assertTrue(payload.proposal.evidence.any { evidence -> evidence.kind == "diagnostic" })
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `impact summary request can resolve review context inside LSP before provider submission`() {
        val root = createTempDirectory("athena-lsp-ai-impact-")
        val current = root.resolve("current")
        val baseline = root.resolve("baseline")
        var capturedEvidenceKinds: List<String> = emptyList()
        try {
            writeAiReasoningSemanticScmFixture(
                repositoryRoot = baseline,
                sourceText = aiReasoningSemanticScmKnowledgeBaselineSource,
            )
            writeAiReasoningSemanticScmFixture(
                repositoryRoot = current,
                sourceText = aiReasoningSemanticScmKnowledgeChangedSource,
            )
            AthenaCompiler().materializeRepositoryLock(baseline)
            AthenaCompiler().materializeRepositoryLock(current)

            val server = AthenaLanguageServer(
                aiReasoningProvider = AthenaAiReasoningProvider { request ->
                    capturedEvidenceKinds = request.context.evidence.map { evidence -> evidence.kind.name }
                    AthenaAiReasoningProviderSuccess(
                        summary = "Impact summary ready",
                        response = "Breaker, cable, and relay review remain required for M1.",
                        providerId = "mock-provider",
                    )
                },
            )
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = current.toUri().toString()
                    },
                ).get()

                val payload = server.aiReasoning(
                    AthenaAiReasoningRequestParams(
                        requestCategory = "impact-summary",
                        subjectSemanticIds = listOf("component:M1"),
                        baseline = AthenaSemanticScmStateParams(
                            adapterId = "scm-git",
                            locator = "../baseline",
                            baselineId = "baseline-ai-impact",
                            baselineLabel = "Baseline AI impact",
                        ),
                    ),
                ).get()

                assertNotNull(payload)
                assertEquals("success", payload.session.providerStatus)
                assertEquals("impact-summary", payload.proposal.proposalCategory)
                assertEquals("success", payload.proposal.providerStatus)
                assertEquals("unresolved", payload.proposal.decisionState)
                assertEquals("mock-provider", payload.proposal.providerId)
                assertTrue(capturedEvidenceKinds.contains("IMPACT_CONSEQUENCE"))
                assertTrue(capturedEvidenceKinds.contains("REVIEW_ENTRY"))
                assertTrue(payload.proposal.evidence.any { evidence -> evidence.kind == "impact-consequence" })
                assertTrue(payload.proposal.evidence.any { evidence -> evidence.kind == "review-entry" })
            } finally {
                server.shutdown().get()
            }
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `ai reasoning state and decisions stay inspectable through LSP`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-ai-state-",
            sourceText = knowledgeSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val submission = server.aiReasoning(
                    AthenaAiReasoningRequestParams(
                        requestCategory = "next-check",
                        subjectSemanticIds = listOf("component:M1"),
                    ),
                ).get()
                assertNotNull(submission)

                val initialState = server.aiReasoningState().get()
                assertNotNull(initialState)
                assertEquals(1, initialState.sessions.size)
                assertEquals(1, initialState.proposals.size)
                assertEquals("unresolved", initialState.proposals.single().decisionState)

                val updatedProposal = server.aiReasoningDecision(
                    AthenaAiReasoningDecisionParams(
                        proposalId = submission.proposal.proposalId,
                        decision = "accepted",
                    ),
                ).get()

                assertNotNull(updatedProposal)
                assertEquals("accepted", updatedProposal.decisionState)

                val updatedState = server.aiReasoningState().get()
                assertNotNull(updatedState)
                assertEquals("accepted", updatedState.proposals.single().decisionState)
                assertEquals("success", updatedState.sessions.single().providerStatus)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}

private val knowledgeSource = """
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

private fun writeAiReasoningSemanticScmFixture(
    repositoryRoot: java.nio.file.Path,
    sourceText: String,
) {
    repositoryRoot.createDirectories()
    repositoryRoot.resolve("athena.yaml").writeText(
        """
            primaryPackage:
              name: com.engineeringood.demo
              version: 1.0.0
              sourceRoot: src
        """.trimIndent(),
    )
    repositoryRoot.resolve("athena.lock").writeText("# lock")
    val sourceRoot = repositoryRoot.resolve("src").createDirectories()
    sourceRoot.resolve("demo.athena").writeText(sourceText)
}

private val aiReasoningSemanticScmKnowledgeBaselineSource = """
    system MotorImpactProof {
      device M1 {
        type Motor
        power "7.5kw"
        voltage "400V"
        powerFactor "0.86"
        efficiency "0.92"
        breakerRatedCurrent "10A"
        cableAllowedCurrent "12A"
        relayRatedCurrent "13A"
      }
    }
""".trimIndent()

private val aiReasoningSemanticScmKnowledgeChangedSource = """
    system MotorImpactProof {
      device M1 {
        type Motor
        power "9kw"
        voltage "400V"
        powerFactor "0.86"
        efficiency "0.92"
        breakerRatedCurrent "10A"
        cableAllowedCurrent "12A"
        relayRatedCurrent "13A"
      }
    }
""".trimIndent()
