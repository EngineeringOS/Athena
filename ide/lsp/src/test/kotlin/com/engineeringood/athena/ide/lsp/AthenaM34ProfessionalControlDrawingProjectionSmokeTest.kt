package com.engineeringood.athena.ide.lsp

import java.nio.file.Files
import java.nio.file.Path
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.TextDocumentItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaM34ProfessionalControlDrawingProjectionSmokeTest {
    @Test
    fun `m34 repository publishes the governed professional control drawing`() {
        val sampleRoot = repositoryRoot().resolve("examples/m34/professional-control-drawing")
        val source = sampleRoot.resolve("src/com/engineeringood/m34/professional/01-control-drawing.athena")
        val server = AthenaLanguageServer()
        try {
            server.initialize(InitializeParams().apply { rootUri = sampleRoot.toUri().toString() }).get()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(source.toUri().toString(), "athena", 1, Files.readString(source)),
                ),
            )

            val session = assertNotNull(server.projectionSession(AthenaProjectionSessionParams()).get())
            assertEquals("ready", session.status)
            assertEquals("schematic", session.activeViewId)
            val presentation = assertNotNull(session.readyProjection?.presentation)
            assertEquals(1050, presentation.canvasWidth)
            assertEquals(720, presentation.canvasHeight)
            assertEquals(22, presentation.graphicOccurrences.size)
            assertEquals(34, presentation.connectors.size)
            assertTrue(presentation.graphicOccurrences.all { occurrence ->
                occurrence.graphic.primitives.isNotEmpty() &&
                    occurrence.authorities.graphic == "graphic-primitive-ir" &&
                    occurrence.authorities.placement == "semantic-layout-facts" &&
                    occurrence.authorities.material == "representation-material-resolver"
            })
            assertFalse(presentation.graphicOccurrences.any { occurrence ->
                occurrence.graphic.primitives.any { primitive -> primitive.kind == "fallback-box" }
            })

            val composition = assertNotNull(presentation.drawingComposition)
            assertEquals((1..17).map(Int::toString), composition.coordinateZones.filter { it.axis == "COLUMN" }.map { it.label })
            assertEquals(('A'..'H').map(Char::toString), composition.coordinateZones.filter { it.axis == "ROW" }.map { it.label })
            assertEquals(setOf("author", "title", "file", "date", "folio"), composition.title.fields.map { it.fieldId }.toSet())
            assertEquals(
                setOf("power-region", "control-region"),
                composition.structureFacts.filter { it.kind == "drawing-region" }.map { it.factId }.toSet(),
            )
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
