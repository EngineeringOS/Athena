package com.engineeringood.athena.apps.composeviewer

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import java.nio.file.Files

class AthenaComposeViewerRuntimeTest {
    @Test
    fun `loads the default M2 operator proof viewer snapshot through Athena runtime`() {
        val snapshot = AthenaComposeViewerBootstrap.loadDefaultProjectSnapshot()
        val scene = requireNotNull(snapshot.scene)

        assertEquals("operator-proof", snapshot.projectName)
        assertEquals("OperatorProof", scene.systemName)
        assertEquals(480, scene.canvasWidth)
        assertEquals(172, scene.canvasHeight)
        assertEquals(2, scene.componentCount)
        assertEquals(0, scene.connectionCount)
        assertEquals("Athena", snapshot.descriptor.windowTitle)
        assertContains(snapshot.descriptor.statusLine, "OperatorProof")
        assertTrue(snapshot.sourcePath.endsWith("examples/m2/operator-proof.athena"))
        assertContains(snapshot.sourceText, "system OperatorProof")
        assertContains(snapshot.sourceText, "device PLC1")
    }

    @Test
    fun `smoke verifier renders the runtime loaded project identity`() {
        val smokeMessage = AthenaComposeViewerSmokeVerifier.verify(
            AthenaComposeViewerBootstrap.loadDefaultProjectSnapshot(),
        )

        assertContains(smokeMessage, "operator-proof")
        assertContains(smokeMessage, "OperatorProof")
        assertContains(smokeMessage, "rendered")
    }

    @Test
    fun `operator proof verifier completes the scripted M2 desktop flow`() {
        val proofMessage = AthenaComposeViewerBootstrap.operatorProofMessage()

        assertContains(proofMessage, "operator-proof")
        assertContains(proofMessage, "OperatorProof")
        assertContains(proofMessage, "component:PLC1")
        assertContains(proofMessage, "connection:PLC1.out->M1.in")
        assertContains(proofMessage, "cabinet")
        assertContains(proofMessage, "wiring")
        assertContains(proofMessage, "runtime-owned")
    }

    @Test
    fun `snapshot derives the first real workbench shell state`() {
        val shellState = AthenaComposeViewerBootstrap
            .loadDefaultProjectSnapshot()
            .toShellState()

        assertEquals("Athena", shellState.descriptor.windowTitle)
        assertEquals("operator-proof", shellState.workspaceName)
        assertEquals("OperatorProof", shellState.projectName)
        assertEquals(2, shellState.workspaceTreeItems.count { it.depth == 2 })
        assertEquals("examples/m2/operator-proof.athena", shellState.sourceDocument?.path)
        assertContains(shellState.sourceDocument?.lines?.first()?.content.orEmpty(), "system OperatorProof")
        assertContains(shellState.diagnosticsEntries.first(), "No active diagnostics")
        assertContains(shellState.consoleEntries.first(), "Runtime project activated")
        assertContains(shellState.inspectorGroups.first().fields.first().value, "OperatorProof")
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
