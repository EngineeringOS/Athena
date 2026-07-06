package com.engineeringood.athena.cli

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class AthenaAiProposalCliTest {
    @Test
    fun `cli can propose review accept and reject ai command candidates across one shot invocations`() {
        val sourcePath = writeProject(
            """
                system AiCliDemo {
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
            """.trimIndent(),
        )

        try {
            val proposal = BootstrapCli().run(
                listOf(
                    "ai-propose-connect",
                    sourcePath.toString(),
                    "PLC1.out1",
                    "M1.in",
                    "Connect PLC1.out1 to M1.in",
                ),
            )
            val pending = BootstrapCli().run(listOf("ai-proposals", sourcePath.toString()))
            val accept = BootstrapCli().run(listOf("ai-accept", sourcePath.toString(), "ai-proposal-0001"))
            val history = BootstrapCli().run(listOf("history", sourcePath.toString()))
            val diff = BootstrapCli().run(listOf("diff", sourcePath.toString()))
            BootstrapCli().run(
                listOf(
                    "ai-propose-connect",
                    sourcePath.toString(),
                    "PLC1.out2",
                    "M2.in",
                    "Connect PLC1.out2 to M2.in",
                ),
            )
            val reject = BootstrapCli().run(listOf("ai-reject", sourcePath.toString(), "ai-proposal-0002"))
            val pendingAfterReject = BootstrapCli().run(listOf("ai-proposals", sourcePath.toString()))

            assertContains(proposal, "AI proposal queued")
            assertContains(proposal, "Proposal: ai-proposal-0001")
            assertContains(pending, "Pending AI proposals: 1")
            assertContains(pending, "ai-proposal-0001 CONNECT_PORTS")

            assertContains(accept, "AI proposal accepted")
            assertContains(accept, "Proposal: ai-proposal-0001")
            assertContains(accept, "Command id: command-0001")
            assertContains(accept, "Origin: AI_ACCEPTED")
            assertContains(accept, "connections after: 1")

            assertContains(history, "History entries: 1")
            assertContains(history, "command-0001 CONNECT_PORTS APPLIED AI_ACCEPTED")
            assertContains(diff, "Latest semantic diff")
            assertContains(diff, "command-0001")

            assertContains(reject, "AI proposal rejected")
            assertContains(reject, "Proposal: ai-proposal-0002")
            assertContains(pendingAfterReject, "Pending AI proposals: 0")
        } finally {
            Files.deleteIfExists(AthenaCliSessionStore().sessionFilePath(sourcePath))
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `cli deletes the session sidecar when the ai proposal queue returns to empty without history`() {
        val sourcePath = writeProject(
            """
                system AiCliCleanupDemo {
                  device PLC1 {
                    type PLC
                  }

                  device M1 {
                    type Motor
                  }

                  port PLC1.out1 {
                    direction out
                    signal Digital
                  }

                  port M1.in {
                    direction in
                    signal Digital
                  }
                }
            """.trimIndent(),
        )

        try {
            val sessionPath = AthenaCliSessionStore().sessionFilePath(sourcePath)

            BootstrapCli().run(
                listOf(
                    "ai-propose-connect",
                    sourcePath.toString(),
                    "PLC1.out1",
                    "M1.in",
                    "Connect PLC1.out1 to M1.in",
                ),
            )
            assertContains(BootstrapCli().run(listOf("ai-reject", sourcePath.toString(), "ai-proposal-0001")), "AI proposal rejected")

            assertFalse(Files.exists(sessionPath), "Rejecting the last pending AI proposal should remove the empty session sidecar.")
            assertContains(BootstrapCli().run(listOf("ai-proposals", sourcePath.toString())), "Pending AI proposals: 0")
        } finally {
            Files.deleteIfExists(AthenaCliSessionStore().sessionFilePath(sourcePath))
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-cli-ai-proposals-", ".athena")
        Files.writeString(path, source)
        return path
    }
}
