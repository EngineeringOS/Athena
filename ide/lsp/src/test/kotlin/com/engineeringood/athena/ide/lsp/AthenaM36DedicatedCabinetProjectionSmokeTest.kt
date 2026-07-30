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

class AthenaM36DedicatedCabinetProjectionSmokeTest {
    @Test
    fun `m36 dedicated sample opens the governed cabinet surface only`() {
        val sampleRoot = repositoryRoot().resolve("examples/m36/connectivity-cabinet")
        val source = sampleRoot.resolve(
            "src/com/engineeringood/m36/connectivitycabinet/01-connectivity-cabinet.athena",
        )
        val server = AthenaLanguageServer()
        try {
            server.initialize(InitializeParams().apply { rootUri = sampleRoot.toUri().toString() }).get()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(source.toUri().toString(), "athena", 1, Files.readString(source)),
                ),
            )

            val session = assertNotNull(server.projectionSession(AthenaProjectionSessionParams()).get())
            assertEquals("ready", session.status, session.unavailableReason ?: session.diagnostics.joinToString("\n") { diagnostic -> diagnostic.message })
            assertEquals("cabinet", session.activeViewId)
            assertEquals(listOf("cabinet"), session.supportedViews.map { view -> view.viewId })
            assertEquals("Cabinet", session.supportedViews.single().displayName)
            val ready = assertNotNull(session.readyProjection)
            assertEquals("cabinet", ready.viewId)
            assertEquals("electrical/cabinet", ready.familyId)
            assertTrue(ready.presentation != null)
            val presentation = assertNotNull(ready.presentation)
            assertTrue(presentation.graphicOccurrences.size >= 20)
            assertTrue(presentation.connectors.size >= 30)
            assertTrue(
                presentation.graphicOccurrences.any { occurrence ->
                    occurrence.definitionId == "vendor.abb.pfea112.element" &&
                        occurrence.terminalBindings.any { binding -> binding.anchorId == "signalOut" }
                },
            )
            assertTrue(
                presentation.graphicOccurrences.any { occurrence ->
                    occurrence.definitionId == "iec.control-junction.element"
                },
            )
            assertNotNull(presentation.routeFactSnapshot)
            assertTrue(presentation.routeFactSnapshot!!.routeFacts.size >= 30)
            assertNotNull(presentation.drawingComposition)
            assertTrue(presentation.drawingComposition!!.structureFacts.any { it.trace != null })
        } finally {
            server.shutdown().get()
        }
    }

    @Test
    fun `m36 cabinet projection is source backed before editor didOpen`() {
        val sampleRoot = repositoryRoot().resolve("examples/m36/connectivity-cabinet")
        val server = AthenaLanguageServer()
        try {
            server.initialize(InitializeParams().apply { rootUri = sampleRoot.toUri().toString() }).get()

            val session = assertNotNull(server.projectionSession(AthenaProjectionSessionParams()).get())
            assertEquals("ready", session.status, session.unavailableReason ?: session.diagnostics.joinToString("\n") { diagnostic -> diagnostic.message })
            assertEquals("cabinet", session.activeViewId)
            val presentation = assertNotNull(assertNotNull(session.readyProjection).presentation)
            assertTrue(presentation.graphicOccurrences.size >= 20)
            assertTrue(presentation.connectors.size >= 30)
            assertTrue(presentation.graphicOccurrences.any { occurrence ->
                occurrence.semanticSubjectId.contains("ControlJunction35") ||
                    occurrence.definitionId == "iec.control-junction.element"
            })
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
