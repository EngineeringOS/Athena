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

class AthenaM35DedicatedCabinetProjectionSmokeTest {
    @Test
    fun `m35 dedicated sample opens the governed cabinet surface only`() {
        val sampleRoot = repositoryRoot().resolve("examples/m35/physical-installation-cabinet")
        val source = sampleRoot.resolve(
            "src/com/engineeringood/m35/physicalinstallationcabinet/01-physical-installation-cabinet.athena",
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
            assertEquals("M35PhysicalInstallationCabinet", ready.systemName)
            val presentation = assertNotNull(ready.presentation)
            assertEquals(6, presentation.graphicOccurrences.size)
            assertTrue(presentation.graphicOccurrences.any { occurrence ->
                occurrence.semanticSubjectId == "component:PFEA112" &&
                    occurrence.definitionId == "vendor.abb.pfea112.element" &&
                    occurrence.terminalBindings.any { binding -> binding.anchorId == "signalOut" }
            })
            val graphicOccurrence = presentation.graphicOccurrences.first()
            assertNotNull(graphicOccurrence.trace)
            assertTrue(graphicOccurrence.trace!!.sourceProvenance.isNotEmpty())
            assertTrue(graphicOccurrence.terminalBindings.first().trace != null)
            val routeSnapshot = assertNotNull(presentation.routeFactSnapshot)
            assertTrue(routeSnapshot.routeFacts.isNotEmpty())
            assertNotNull(routeSnapshot.routeFacts.first().trace)
            val composition = assertNotNull(presentation.drawingComposition)
            assertTrue(composition.structureFacts.any { it.trace != null })
            assertTrue(presentation.connectors.size >= 7)
        } finally {
            server.shutdown().get()
        }
    }

    @Test
    fun `m35 cabinet projection is source backed before editor didOpen`() {
        val sampleRoot = repositoryRoot().resolve("examples/m35/physical-installation-cabinet")
        val server = AthenaLanguageServer()
        try {
            server.initialize(InitializeParams().apply { rootUri = sampleRoot.toUri().toString() }).get()

            val session = assertNotNull(server.projectionSession(AthenaProjectionSessionParams()).get())
            assertEquals("ready", session.status, session.unavailableReason ?: session.diagnostics.joinToString("\n") { diagnostic -> diagnostic.message })
            assertEquals("cabinet", session.activeViewId)
            val presentation = assertNotNull(assertNotNull(session.readyProjection).presentation)
            assertEquals(6, presentation.graphicOccurrences.size)
            assertTrue(presentation.graphicOccurrences.any { occurrence ->
                occurrence.semanticSubjectId == "component:PFEA112" &&
                    occurrence.definitionId == "vendor.abb.pfea112.element"
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
