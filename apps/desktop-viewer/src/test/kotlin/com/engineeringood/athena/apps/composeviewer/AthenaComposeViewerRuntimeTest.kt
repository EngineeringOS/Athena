package com.engineeringood.athena.apps.composeviewer

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import java.nio.file.Files

class AthenaComposeViewerRuntimeTest {
    @Test
    fun `loads the demo cabinet viewer snapshot through Athena runtime`() {
        val snapshot = AthenaComposeViewerBootstrap.loadDefaultProjectSnapshot()
        val scene = requireNotNull(snapshot.scene)

        assertEquals("demo-cabinet", snapshot.projectName)
        assertEquals("DemoCabinet", scene.systemName)
        assertEquals(480, scene.canvasWidth)
        assertEquals(172, scene.canvasHeight)
        assertEquals(2, scene.componentCount)
        assertEquals(1, scene.connectionCount)
        assertEquals("Athena", snapshot.descriptor.windowTitle)
        assertContains(snapshot.descriptor.statusLine, "DemoCabinet")
        assertTrue(snapshot.sourcePath.endsWith("examples/m0/demo-cabinet.athena"))
        assertContains(snapshot.sourceText, "system DemoCabinet")
        assertContains(snapshot.sourceText, "device PLC1")
    }

    @Test
    fun `smoke verifier renders the runtime loaded project identity`() {
        val smokeMessage = AthenaComposeViewerSmokeVerifier.verify(
            AthenaComposeViewerBootstrap.loadDefaultProjectSnapshot(),
        )

        assertContains(smokeMessage, "demo-cabinet")
        assertContains(smokeMessage, "DemoCabinet")
        assertContains(smokeMessage, "rendered")
    }

    @Test
    fun `snapshot derives the first real workbench shell state`() {
        val shellState = AthenaComposeViewerBootstrap
            .loadDefaultProjectSnapshot()
            .toShellState()

        assertEquals("Athena", shellState.descriptor.windowTitle)
        assertEquals("demo-cabinet", shellState.workspaceName)
        assertEquals("DemoCabinet", shellState.projectName)
        assertEquals(2, shellState.workspaceTreeItems.count { it.depth == 2 })
        assertEquals("examples/m0/demo-cabinet.athena", shellState.sourceDocument?.path)
        assertContains(shellState.sourceDocument?.lines?.first()?.content.orEmpty(), "system DemoCabinet")
        assertContains(shellState.diagnosticsEntries.first(), "No active diagnostics")
        assertContains(shellState.consoleEntries.first(), "Runtime project activated")
        assertContains(shellState.inspectorGroups.first().fields.first().value, "DemoCabinet")
    }

    @Test
    fun `bootstrap rejects sources outside the runtime workspace root`() {
        val sourcePath = Files.createTempFile("athena-compose-viewer-outside-", ".athena")
        Files.writeString(sourcePath, "system Outside { }")

        try {
            val exception = assertFailsWith<IllegalArgumentException> {
                AthenaComposeViewerBootstrap.loadProjectSnapshot(
                    workspaceRoot = sourcePath.parent.resolve("workspace-root"),
                    projectName = "outside",
                    sourcePath = sourcePath,
                )
            }

            assertContains(exception.message.orEmpty(), "workspace root")
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }
}
