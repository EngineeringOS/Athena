package com.engineeringood.athena.cli

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains

class CommandHistoryCliTest {
    @Test
    fun `cli history commands restore persisted runtime state across one shot invocations`() {
        val sourcePath = writeProject(
            """
                system HistoryDemo {
                  device PLC1 {
                    type Switch
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
            BootstrapCli().run(listOf("connect", sourcePath.toString(), "PLC1.out1", "M1.in"))
            BootstrapCli().run(listOf("connect", sourcePath.toString(), "PLC1.out2", "M2.in"))

            val history = BootstrapCli().run(listOf("history", sourcePath.toString()))
            val diffAfterCommand = BootstrapCli().run(listOf("diff", sourcePath.toString()))
            val undo = BootstrapCli().run(listOf("undo", sourcePath.toString()))
            val diffAfterUndo = BootstrapCli().run(listOf("diff", sourcePath.toString()))
            val consequences = BootstrapCli().run(
                listOf("history-consequences", sourcePath.toString(), "command-0002"),
            )
            val redo = BootstrapCli().run(listOf("redo", sourcePath.toString()))
            BootstrapCli().run(listOf("undo", sourcePath.toString()))
            BootstrapCli().run(listOf("undo", sourcePath.toString()))
            val replay = BootstrapCli().run(listOf("replay", sourcePath.toString()))
            val serialized = BootstrapCli().run(listOf("serialize-history", sourcePath.toString()))

            assertContains(history, "History entries: 2")
            assertContains(history, "Applied entries: 2")
            assertContains(history, "command-0001 CONNECT_PORTS APPLIED")
            assertContains(history, "command-0002 CONNECT_PORTS APPLIED")

            assertContains(diffAfterCommand, "Latest semantic diff")
            assertContains(diffAfterCommand, "Source: COMMAND")
            assertContains(diffAfterCommand, "command-0002")
            assertContains(diffAfterCommand, "ADDED connection:PLC1.out2->M2.in")

            assertContains(undo, "Undo successful")
            assertContains(undo, "Command: command-0002")
            assertContains(undo, "connections before: 2")
            assertContains(undo, "connections after: 1")

            assertContains(diffAfterUndo, "Latest semantic diff")
            assertContains(diffAfterUndo, "Source: UNDO")
            assertContains(diffAfterUndo, "REMOVED connection:PLC1.out2->M2.in")

            assertContains(consequences, "History consequence")
            assertContains(consequences, "command-0002")
            assertContains(consequences, "Status: UNDONE")
            assertContains(consequences, "ADDED connection:PLC1.out2->M2.in")

            assertContains(redo, "Redo successful")
            assertContains(redo, "Command: command-0002")
            assertContains(redo, "connections before: 1")
            assertContains(redo, "connections after: 2")

            assertContains(replay, "Replay successful")
            assertContains(replay, "Commands: command-0001, command-0002")
            assertContains(replay, "connections before: 0")
            assertContains(replay, "connections after: 2")

            assertContains(serialized, "\"commandId\":\"command-0001\"")
            assertContains(serialized, "\"commandId\":\"command-0002\"")
            assertContains(serialized, "\"status\":\"APPLIED\"")
        } finally {
            Files.deleteIfExists(AthenaCliSessionStore().sessionFilePath(sourcePath))
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-cli-history-", ".athena")
        Files.writeString(path, source)
        return path
    }
}
