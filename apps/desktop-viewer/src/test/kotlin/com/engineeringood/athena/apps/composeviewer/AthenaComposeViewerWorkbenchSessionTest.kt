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
                    type Switch
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
            assertContains(connectedState.diagnosticsEntries.joinToString(separator = " "), "projection:")
            assertContains(connectedState.diagnosticsEntries.joinToString(separator = " "), "rendering cabinet")
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
                    type Switch
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

    @Test
    fun `desktop session exposes runtime owned projection views and switches the active view`() {
        val session = AthenaComposeViewerBootstrap.openDefaultWorkbenchSession()

        val initialState = session.shellState()
        val initialProjectionSession = assertNotNull(initialState.projectionSession)

        assertEquals(listOf("cabinet", "wiring"), initialProjectionSession.supportedViews.map { view -> view.viewId })
        assertEquals("cabinet", initialProjectionSession.activeViewId)
        assertEquals(480, initialState.scene?.canvasWidth)
        assertEquals(172, initialState.scene?.canvasHeight)
        assertEquals(0, initialState.scene?.connectionCount)

        session.dispatch(
            AthenaComposeShellIntent.SwitchProjectionView(
                viewId = "wiring",
            ),
        )

        val switchedState = session.shellState()
        val switchedProjectionSession = assertNotNull(switchedState.projectionSession)

        assertEquals("wiring", switchedProjectionSession.activeViewId)
        assertEquals(490, switchedState.scene?.canvasWidth)
        assertEquals(244, switchedState.scene?.canvasHeight)
        assertContains(switchedState.consoleEntries.joinToString(separator = " "), "wiring")
    }

    @Test
    fun `desktop session keeps canonical semantic selection across supported view switching`() {
        val session = AthenaComposeViewerBootstrap.openDefaultWorkbenchSession()

        session.dispatch(
            AthenaComposeShellIntent.SelectRenderedSemantic(
                semanticId = "component:PLC1",
            ),
        )
        val selectedCabinetState = session.shellState()
        assertEquals("component:PLC1", selectedCabinetState.projectionSession?.selectedSemanticId)

        session.dispatch(
            AthenaComposeShellIntent.SwitchProjectionView(
                viewId = "wiring",
            ),
        )
        val selectedWiringState = session.shellState()
        val switchedProjectionSession = assertNotNull(selectedWiringState.projectionSession)

        assertEquals("component:PLC1", switchedProjectionSession.selectedSemanticId)
        assertTrue(selectedWiringState.scene?.components?.any { component -> component.semanticId == "component:PLC1" } == true)
        assertContains(
            selectedWiringState.inspectorGroups.joinToString(separator = " ") { group ->
                group.fields.joinToString(separator = " ") { field -> "${field.label} ${field.value}" }
            },
            "component:PLC1",
        )
    }

    @Test
    fun `default desktop session completes the final M2 operator proof flow`() {
        val session = AthenaComposeViewerBootstrap.openDefaultWorkbenchSession()

        val initialState = session.shellState()
        assertEquals("OperatorProof", initialState.projectName)
        assertEquals(0, initialState.scene?.connectionCount)
        assertContains(initialState.sourceDocument?.path.orEmpty(), "examples/m2/operator-proof.athena")

        session.dispatch(
            AthenaComposeShellIntent.SelectRenderedSemantic(
                semanticId = "component:PLC1",
            ),
        )
        val selectedCabinetState = session.shellState()
        assertEquals("component:PLC1", selectedCabinetState.projectionSession?.selectedSemanticId)

        session.dispatch(
            AthenaComposeShellIntent.SwitchProjectionView(
                viewId = "wiring",
            ),
        )
        val selectedWiringState = session.shellState()
        assertEquals("wiring", selectedWiringState.projectionSession?.activeViewId)
        assertEquals("component:PLC1", selectedWiringState.projectionSession?.selectedSemanticId)
        assertTrue(selectedWiringState.projectionSession?.selectedSemanticVisibleInActiveView == true)

        session.dispatch(
            AthenaComposeShellIntent.SelectSourcePort(
                semanticId = "port:PLC1.out",
            ),
        )
        session.dispatch(
            AthenaComposeShellIntent.SelectTargetPort(
                semanticId = "port:M1.in",
            ),
        )
        session.dispatch(AthenaComposeShellIntent.ExecuteConnectPorts)

        val connectedWiringState = session.shellState()
        assertEquals(1, connectedWiringState.scene?.connectionCount)
        assertContains(connectedWiringState.commandPanel?.statusMessage.orEmpty(), "connection:PLC1.out->M1.in")
        assertContains(connectedWiringState.diagnosticsEntries.joinToString(separator = " "), "projection:")
        assertContains(connectedWiringState.diagnosticsEntries.joinToString(separator = " "), "history: command-0001 APPLIED")

        session.dispatch(
            AthenaComposeShellIntent.SwitchProjectionView(
                viewId = "cabinet",
            ),
        )
        val connectedCabinetState = session.shellState()
        assertEquals("cabinet", connectedCabinetState.projectionSession?.activeViewId)
        assertEquals(1, connectedCabinetState.scene?.connectionCount)
        assertEquals("component:PLC1", connectedCabinetState.projectionSession?.selectedSemanticId)
        assertTrue(connectedCabinetState.projectionSession?.selectedSemanticVisibleInActiveView == true)
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-compose-viewer-workbench-", ".athena")
        Files.writeString(path, source)
        return path
    }
}
