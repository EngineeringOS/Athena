package com.engineeringood.athena.runtime

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaRuntimeViewerProjectionTest {
    @Test
    fun `runtime projects the demo cabinet viewer scene without leaking compiler internals`() {
        val sourcePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
        val runtime = AthenaRuntime()
        val context = runtime.openWorkspace(resolveRepoRoot()).activateProject(
            projectName = "demo-cabinet",
            sourcePath = sourcePath,
        )
        assertIs<AthenaRuntimeProjectionSwitchSuccess>(context.switchActiveProjectionView("cabinet"))

        val projection = context.projectViewerProjection()

        val ready = assertIs<AthenaRuntimeViewerReadyProjection>(projection)
        assertEquals("demo-cabinet", ready.projectName)
        assertEquals("DemoCabinet", ready.scene.systemName)
        assertEquals(480, ready.scene.canvasWidth)
        assertEquals(172, ready.scene.canvasHeight)
        assertEquals(2, ready.scene.components.size)
        assertEquals(1, ready.scene.connections.size)
    }

    @Test
    fun `runtime surfaces parse failures as unavailable viewer projections`() {
        val brokenPath = Files.createTempFile("athena-runtime-viewer-broken-", ".athena")
        Files.writeString(
            brokenPath,
            """
                system Broken {
                  connect P1.out P2.in
                }
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(brokenPath.parent).activateProject(
                projectName = "broken",
                sourcePath = brokenPath,
            )

            val projection = context.projectViewerProjection()

            val unavailable = assertIs<AthenaRuntimeViewerUnavailableProjection>(projection)
            assertEquals("broken", unavailable.projectName)
            assertTrue(unavailable.reason.isNotBlank())
            assertContains(unavailable.reason, "missing '->'")
        } finally {
            Files.deleteIfExists(brokenPath)
        }
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }
}
