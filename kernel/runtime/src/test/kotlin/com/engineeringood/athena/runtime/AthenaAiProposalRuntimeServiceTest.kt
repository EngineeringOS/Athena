package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaAiProposalRuntimeServiceTest {
    @Test
    fun `submitting an ai proposal keeps canonical state unchanged until explicit acceptance`() {
        val sourcePath = writeProject(twoConnectionFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "ai-proposals",
                sourcePath = sourcePath,
            )

            val submission = context.aiProposalRuntime().submit(
                context = context,
                draft = AthenaAiCommandProposalDraft(
                    summary = "Connect PLC1.out1 to M1.in",
                    rationale = "Suggested by the optional AI proof surface.",
                    command = AthenaConnectPortsCommand(
                        sourcePortSemanticId = "port:PLC1.out1",
                        targetPortSemanticId = "port:M1.in",
                    ),
                ),
            )

            val acceptedSubmission = assertIs<AthenaAiCommandProposalSubmitted>(submission)
            assertEquals("ai-proposal-0001", acceptedSubmission.proposal.proposalId)
            assertEquals(1, context.aiProposalRuntime().pendingProposals(context).size)
            assertTrue(assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document.connections.isEmpty())
            assertTrue(context.commandRuntime().history(context).records.isEmpty())
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `accepted ai proposal enters normal command history and diff inspection surfaces`() {
        val sourcePath = writeProject(twoConnectionFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "ai-proposals",
                sourcePath = sourcePath,
            )

            val proposal = assertIs<AthenaAiCommandProposalSubmitted>(
                context.aiProposalRuntime().submit(
                    context = context,
                    draft = AthenaAiCommandProposalDraft(
                        summary = "Connect PLC1.out1 to M1.in",
                        rationale = "Suggested by the optional AI proof surface.",
                        command = AthenaConnectPortsCommand(
                            sourcePortSemanticId = "port:PLC1.out1",
                            targetPortSemanticId = "port:M1.in",
                        ),
                    ),
                ),
            ).proposal

            val acceptance = context.aiProposalRuntime().acceptProposal(
                context = context,
                proposalId = proposal.proposalId,
            )

            val accepted = assertIs<AthenaAiCommandProposalAccepted>(acceptance)
            assertEquals("command-0001", accepted.execution.commandId)
            assertEquals(AthenaCommandOrigin.AI_ACCEPTED, accepted.execution.commandOrigin)
            assertEquals(1, accepted.execution.afterDocument.connections.size)
            assertTrue(context.aiProposalRuntime().pendingProposals(context).isEmpty())

            val history = context.commandRuntime().history(context)
            assertEquals(1, history.records.size)
            assertEquals(AthenaCommandOrigin.AI_ACCEPTED, history.records.single().commandOrigin)

            val latestInspection = assertNotNull(context.commandRuntime().latestInspection(context))
            assertEquals(AthenaSemanticDiffInspectionSource.COMMAND, latestInspection.source)
            assertEquals(listOf("command-0001"), latestInspection.affectedCommandIds)

            val serialized = context.commandRuntime().serializeHistory(context)
            assertContains(serialized, "\"commandOrigin\":\"AI_ACCEPTED\"")
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `rejected or validation failed ai proposals leave canonical state unchanged`() {
        val sourcePath = writeProject(twoConnectionFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "ai-proposals",
                sourcePath = sourcePath,
            )

            val invalidProposal = assertIs<AthenaAiCommandProposalSubmitted>(
                context.aiProposalRuntime().submit(
                    context = context,
                    draft = AthenaAiCommandProposalDraft(
                        summary = "Connect PLC1.out1 to Missing.in",
                        rationale = "Optional AI proof can still suggest invalid commands.",
                        command = AthenaConnectPortsCommand(
                            sourcePortSemanticId = "port:PLC1.out1",
                            targetPortSemanticId = "port:Missing.in",
                        ),
                    ),
                ),
            ).proposal

            val failedAcceptance = context.aiProposalRuntime().acceptProposal(
                context = context,
                proposalId = invalidProposal.proposalId,
            )

            val rejectedAcceptance = assertIs<AthenaAiCommandProposalAcceptanceRejected>(failedAcceptance)
            assertContains(rejectedAcceptance.reason, "port:Missing.in")
            assertTrue(assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document.connections.isEmpty())
            assertTrue(context.commandRuntime().history(context).records.isEmpty())
            assertEquals(1, context.aiProposalRuntime().pendingProposals(context).size)

            val manualRejection = context.aiProposalRuntime().rejectProposal(
                context = context,
                proposalId = invalidProposal.proposalId,
            )

            assertIs<AthenaAiCommandProposalRejected>(manualRejection)
            assertTrue(context.aiProposalRuntime().pendingProposals(context).isEmpty())
            assertTrue(assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document.connections.isEmpty())
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-ai-proposals-", ".athena")
        Files.writeString(path, source)
        return path
    }

    private fun twoConnectionFixture(): String {
        return """
            system AiProposalDemo {
              device PLC1 {
                type PLC
              }

              device M1 {
                type Motor
              }

              device M2 {
                type Motor
              }

              port PLC1.out1 {
                direction out
                signal Digital
              }

              port PLC1.out2 {
                direction out
                signal Digital
              }

              port M1.in {
                direction in
                signal Digital
              }

              port M2.in {
                direction in
                signal Digital
              }
            }
        """.trimIndent()
    }
}
