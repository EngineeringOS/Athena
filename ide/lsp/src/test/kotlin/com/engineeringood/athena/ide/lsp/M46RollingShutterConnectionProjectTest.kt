package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.presentation.PublicationState
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class M46RollingShutterConnectionProjectTest {
    @Test
    fun `M46 repository opens READY with package-backed connection scene`() {
        val repository = repositoryRoot().resolve("examples/m46/rolling-shutter")
        val host = AthenaLspSessionHost()
        try {
            val ready = assertIs<AthenaLspSessionHostReady>(host.activateRepository(repository))
            assertEquals(repository.toAbsolutePath().normalize(), ready.repositoryRoot)
            val publication = ready.diagramScenePublication()
            assertEquals(PublicationState.READY, publication.state, publication.diagnostics.joinToString { it.problem })
            val scene = assertNotNull(publication.scene)
            assertTrue(scene.occurrences.size >= 13)
            assertTrue(scene.connections.isNotEmpty())
            assertTrue(scene.connections.any { connection -> connection.markers.any { it.kind.name == "JUNCTION" } })
            assertTrue(scene.connections.any { connection -> connection.markers.any { it.kind.name == "INTERRUPTION_START" } })
            assertTrue(scene.connections.any { connection -> connection.markers.any { it.kind.name == "CROSSING" } })
            assertTrue(
                scene.connections.any { connection -> connection.segments.any { it.kind.name == "SHARED" } },
                scene.connections.joinToString("\n") { connection ->
                    "${connection.connectionId}: " + connection.segments.joinToString { segment ->
                        "${segment.kind}:${segment.start}->${segment.end}"
                    }
                },
            )
            assertTrue(scene.connections.any { it.annotations.isNotEmpty() })
            assertEquals(17, scene.plotFrame.columns)
            assertEquals(16, scene.plotFrame.rows)
            assertEquals(0, scene.page.pageBounds.x)
            assertTrue(scene.decorations.none { it.kind.name == "COORDINATE_LABEL" && it.text?.contains("anchor:") == true })
            assertTrue(scene.decorations.none { it.text?.contains(".athena") == true })
        } finally {
            host.shutdown()
        }
    }

    private fun repositoryRoot(): Path {
        var current = Path.of("").toAbsolutePath().normalize()
        while (current.parent != null) {
            if (Files.exists(current.resolve("settings.gradle.kts"))) return current
            current = current.parent
        }
        error("Could not locate Athena repository root.")
    }
}
