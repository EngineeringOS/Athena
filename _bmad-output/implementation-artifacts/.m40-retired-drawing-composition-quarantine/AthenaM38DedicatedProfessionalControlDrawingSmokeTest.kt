package com.engineeringood.athena.ide.lsp

import java.nio.file.Files
import java.nio.file.Path
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.TextDocumentItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaM38DedicatedProfessionalControlDrawingSmokeTest {
    @Test
    fun `m38 dedicated sample opens schematic backed control drawing after source didOpen`() {
        val sampleRoot = repositoryRoot().resolve("examples/m38/professional-control-drawing")
        val source = sampleRoot.resolve(
            "src/com/engineeringood/m38/professionalcontroldrawing/01-professional-control-drawing.athena",
        )
        val server = AthenaLanguageServer()
        try {
            server.initialize(InitializeParams().apply { rootUri = sampleRoot.toUri().toString() }).get()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(source.toUri().toString(), "athena", 1, Files.readString(source)),
                ),
            )

            val command = assertNotNull(
                server.projectionCommand(
                    AthenaProjectionCommandParams(
                        commandId = "switch-active-view",
                        viewId = "schematic",
                    ),
                ).get(),
            )
            assertEquals("applied", command.status, command.reason.orEmpty())
            val session = assertNotNull(command.session)
            assertEquals("ready", session.status, session.unavailableReason ?: session.diagnostics.joinToString("\n") { diagnostic -> diagnostic.message })
            assertEquals("schematic", session.activeViewId)
            assertTrue(session.supportedViews.any { view -> view.viewId == "schematic" && view.displayName == "Control Drawing" })
            val ready = assertNotNull(session.readyProjection)
            assertEquals("schematic", ready.viewId)
            assertEquals("electrical/schematic", ready.familyId)
            assertNotNull(ready.presentation)
            assertTrue(ready.presentation.connectors.isNotEmpty())
        } finally {
            server.shutdown().get()
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
