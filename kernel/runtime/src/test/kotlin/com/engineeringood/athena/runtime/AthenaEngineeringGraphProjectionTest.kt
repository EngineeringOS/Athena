package com.engineeringood.athena.runtime

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AthenaEngineeringGraphProjectionTest {
    @Test
    fun `runtime graph remains available when semantic validation blocks rendering`() {
        val brokenPath = Files.createTempFile("athena-runtime-graph-semantic-", ".athena")
        Files.writeString(
            brokenPath,
            """
                system Broken {
                  entity PLC1 {
                    concept Switch
                  }

                  port Missing.out {
                    direction out
                    flow Digital
                  }
                }
            """.trimIndent(),
        )

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(brokenPath.parent).activateProject(
                projectName = "broken",
                sourcePath = brokenPath,
            )

            val projection = context.projectEngineeringGraphProjection()

            val ready = assertIs<AthenaEngineeringGraphReadyProjection>(projection)
            assertNull(ready.graph.node("port:Missing.out"))
            assertTrue(ready.graph.nodesOfKind(AthenaEngineeringGraphNodeKind.PORT).isEmpty())
            assertTrue(ready.graph.referencedNodes("port:Missing.out").isEmpty())
        } finally {
            Files.deleteIfExists(brokenPath)
        }
    }

    @Test
    fun `runtime surfaces parse failures as unavailable engineering graph projections`() {
        val brokenPath = Files.createTempFile("athena-runtime-graph-parse-", ".athena")
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

            val projection = context.projectEngineeringGraphProjection()

            val unavailable = assertIs<AthenaEngineeringGraphUnavailableProjection>(projection)
            assertEquals("broken", unavailable.projectName)
            assertTrue(unavailable.reason.isNotBlank())
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
