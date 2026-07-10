package com.engineeringood.athena.ide.lsp

import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.Range

/**
 * Verifies that Athena LSP exposes a read-only semantic inspection snapshot for the latest tracked document state.
 */
class AthenaSemanticInspectionTest {
    @Test
    @Suppress("DEPRECATION")
    fun `semantic inspection follows latest tracked document state`() {
        val repository = createGovernedTestRepository("athena-lsp-inspection-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath

        val invalidText = """
            system FactoryLine {
              connect Motor1.out -> Missing.in
            }
        """.trimIndent()
        val validText = """
            system FactoryLine {
              device Motor1 {
                type Motor
              }

              device Missing {
                type Motor
              }

              port Motor1.out {
                direction out
                signal Digital
              }

              port Missing.in {
                direction in
                signal Digital
              }

              connect Motor1.out -> Missing.in
            }
        """.trimIndent()

        val server = AthenaLanguageServer()
        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = repositoryRoot.toUri().toString()
                },
            ).get()

            val documentUri = sourcePath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        documentUri,
                        "athena",
                        1,
                        invalidText,
                    ),
                ),
            )

            val invalidInspection = server.semanticInspection(
                AthenaSemanticInspectionParams(
                    AthenaSemanticInspectionTextDocument(documentUri),
                ),
            ).get()
            assertEquals("diagnostics", invalidInspection?.status)
            assertEquals(1, invalidInspection?.version)
            assertEquals(2, invalidInspection?.diagnosticsCount)
            assertEquals(0, invalidInspection?.componentCount)
            assertEquals(1, invalidInspection?.connectionCount)

            server.textDocumentService.didChange(
                DidChangeTextDocumentParams(
                    VersionedTextDocumentIdentifier(documentUri, 2),
                    listOf(TextDocumentContentChangeEvent(validText)),
                ),
            )

            val validInspection = server.semanticInspection(
                AthenaSemanticInspectionParams(
                    AthenaSemanticInspectionTextDocument(documentUri),
                ),
            ).get()
            assertNotNull(validInspection)
            assertEquals("ready", validInspection.status)
            assertEquals(2, validInspection.version)
            assertEquals("FactoryLine", validInspection.systemName)
            assertEquals(0, validInspection.diagnosticsCount)
            assertEquals(2, validInspection.componentCount)
            assertEquals(2, validInspection.portCount)
            assertEquals(1, validInspection.connectionCount)
            assertEquals(listOf("Missing", "Motor1"), validInspection.components.map { component -> component.name })
            assertTrue(validInspection.ports.any { port -> port.path == "Motor1.out" })
            assertEquals("Motor1.out", validInspection.connections.single().fromPath)
            assertEquals("Missing.in", validInspection.connections.single().toPath)
            assertEquals(
                Range(
                    org.eclipse.lsp4j.Position(5, 2),
                    org.eclipse.lsp4j.Position(7, 3),
                ),
                validInspection.components.first { component -> component.name == "Missing" }.sourceRange,
            )
            assertEquals(
                Range(
                    org.eclipse.lsp4j.Position(9, 2),
                    org.eclipse.lsp4j.Position(12, 3),
                ),
                validInspection.ports.first { port -> port.path == "Motor1.out" }.sourceRange,
            )
            assertEquals(
                Range(
                    org.eclipse.lsp4j.Position(19, 2),
                    org.eclipse.lsp4j.Position(19, 34),
                ),
                validInspection.connections.single().sourceRange,
            )
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}
