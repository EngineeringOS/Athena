package com.engineeringood.athena.apps.composeviewer

import com.engineeringood.athena.composeruntime.AthenaComposeShellIntent
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.nio.file.Files
import java.nio.file.Path

class AthenaComposeViewerWorkbenchSessionTest {
    @Test
    fun `gui session dispatches connect ports through runtime and refreshes shell state`() {
        val sourcePath = writeProject(
            """
                system Connectable {
                  device PLC1 {
                    type PLC
                  }

                  device M1 {
                    type Motor
                  }

                  port PLC1.out {
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
            val session = AthenaComposeViewerWorkbenchSession.open(
                workspaceRoot = sourcePath.parent,
                projectName = "connectable",
                sourcePath = sourcePath,
            )

            val initialShellState = session.shellState()
            val initialPanel = assertNotNull(initialShellState.commandPanel)
            assertEquals(0, initialShellState.scene?.connectionCount)
            assertContains(initialShellState.sourceDocument?.text.orEmpty(), "port PLC1.out")
            assertTrue(initialPanel.sourcePortOptions.any { option -> option.semanticId == "port:PLC1.out" })
            assertContains(
                initialShellState.inspectorGroups.joinToString(separator = " ") { group -> group.title },
                "Electrical runtime",
            )
            assertContains(
                initialShellState.diagnosticsEntries.joinToString(separator = " "),
                "Electrical runtime plugin",
            )
            assertTrue(initialPanel.targetPortOptions.isEmpty())
            assertFalse(initialPanel.canExecute)

            session.dispatch(
                AthenaComposeShellIntent.SelectSourcePort(
                    semanticId = "port:PLC1.out",
                ),
            )
            val sourceSelectedState = session.shellState()
            val sourceSelectedPanel = assertNotNull(sourceSelectedState.commandPanel)
            assertEquals("port:PLC1.out", sourceSelectedPanel.selectedSourcePortSemanticId)
            assertTrue(sourceSelectedPanel.targetPortOptions.any { option -> option.semanticId == "port:M1.in" })

            session.dispatch(
                AthenaComposeShellIntent.SelectTargetPort(
                    semanticId = "port:M1.in",
                ),
            )
            val targetSelectedState = session.shellState()
            val targetSelectedPanel = assertNotNull(targetSelectedState.commandPanel)
            assertTrue(targetSelectedPanel.canExecute)

            session.dispatch(AthenaComposeShellIntent.ExecuteConnectPorts)
            val connectedState = session.shellState()
            val connectedPanel = assertNotNull(connectedState.commandPanel)

            assertEquals(1, connectedState.scene?.connectionCount)
            assertContains(connectedPanel.statusMessage, "connection:PLC1.out->M1.in")
            assertContains(connectedState.consoleEntries.last(), "command-0001")
            assertContains(connectedState.consoleEntries.joinToString(separator = " "), "incremental")
            assertContains(connectedState.consoleEntries.joinToString(separator = " "), "history consequence")
            assertContains(connectedState.inspectorGroups.joinToString(separator = " ") { group ->
                group.fields.joinToString(separator = " ") { field -> "${field.label} ${field.value}" }
            }, "History 1")
            assertContains(connectedState.inspectorGroups.joinToString(separator = " ") { group ->
                group.title + " " + group.fields.joinToString(separator = " ") { field -> "${field.label} ${field.value}" }
            }, "Latest change")
            assertContains(connectedState.inspectorGroups.joinToString(separator = " ") { group ->
                group.fields.joinToString(separator = " ") { field -> "${field.label} ${field.value}" }
            }, "command-0001")
            assertContains(connectedState.diagnosticsEntries.joinToString(separator = " "), "Incremental")
            assertContains(connectedState.diagnosticsEntries.joinToString(separator = " "), "Semantic diff")
            assertContains(connectedState.sourceDocument?.text.orEmpty(), "port PLC1.out")
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `gui session exposes only compatible runtime derived target ports for selected source`() {
        val sourcePath = writeProject(
            """
                system Connectable {
                  device PLC1 {
                    type PLC
                  }

                  device M1 {
                    type Motor
                  }

                  device Lamp1 {
                    type Lamp
                  }

                  port PLC1.out {
                    direction out
                    signal Digital
                  }

                  port M1.in {
                    direction in
                    signal Digital
                  }

                  port Lamp1.in {
                    direction in
                    signal Analog
                  }

                  port Lamp1.out {
                    direction out
                    signal Digital
                  }
                }
            """.trimIndent(),
        )

        try {
            val session = AthenaComposeViewerWorkbenchSession.open(
                workspaceRoot = sourcePath.parent,
                projectName = "connectable",
                sourcePath = sourcePath,
            )

            session.dispatch(
                AthenaComposeShellIntent.SelectSourcePort(
                    semanticId = "port:PLC1.out",
                ),
            )

            val panel = assertNotNull(session.shellState().commandPanel)
            val targetIds = panel.targetPortOptions.map { option -> option.semanticId }.sorted()

            assertEquals(listOf("port:M1.in"), targetIds)
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-compose-viewer-workbench-", ".athena")
        Files.writeString(path, source)
        return path
    }
}
