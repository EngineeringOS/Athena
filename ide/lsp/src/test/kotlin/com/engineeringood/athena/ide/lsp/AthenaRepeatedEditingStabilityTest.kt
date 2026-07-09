package com.engineeringood.athena.ide.lsp

import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies that repeated edits keep diagnostics and navigation aligned with the latest Athena-owned document state.
 */
class AthenaRepeatedEditingStabilityTest {
    @Test
    @Suppress("DEPRECATION")
    fun `repeated edits keep diagnostics and definition state aligned to latest document version`() {
        val repository = createGovernedTestRepository("athena-lsp-repeated-edit-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath

        val invalidOpenText = """
            system FactoryLine {
              connect PLC1.out -> PLC1.out
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
        val invalidChangedText = """
            system FactoryLine {
              device Motor1 {
                type Motor
              }

              device Missing {
                type Motor
              }

              port Missing.in {
                direction in
                signal Digital
              }

              connect Motor1.out -> Missing.in
            }
        """.trimIndent()

        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        server.connect(client)

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
                        invalidOpenText,
                    ),
                ),
            )
            assertEquals(1, server.trackedDocument(documentUri)?.version)
            assertEquals(2, client.publishedDiagnostics.last().diagnostics.size)

            server.textDocumentService.didChange(
                DidChangeTextDocumentParams(
                    VersionedTextDocumentIdentifier(documentUri, 2),
                    listOf(TextDocumentContentChangeEvent(validText)),
                ),
            )

            val validDefinition = server.textDocumentService.definition(
                DefinitionParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = Position(19, 17)
                },
            ).get()
            assertEquals(2, server.trackedDocument(documentUri)?.version)
            assertEquals(0, client.publishedDiagnostics.last().diagnostics.size)
            assertEquals(2, client.publishedDiagnostics.last().version)
            assertEquals(1, validDefinition.left.size)

            server.textDocumentService.didChange(
                DidChangeTextDocumentParams(
                    VersionedTextDocumentIdentifier(documentUri, 3),
                    listOf(TextDocumentContentChangeEvent(invalidChangedText)),
                ),
            )

            val invalidDefinition = server.textDocumentService.definition(
                DefinitionParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = Position(14, 17)
                },
            ).get()
            assertEquals(3, server.trackedDocument(documentUri)?.version)
            assertTrue(client.publishedDiagnostics.last().diagnostics.isNotEmpty())
            assertEquals(3, client.publishedDiagnostics.last().version)
            assertEquals(0, invalidDefinition.left.size)

            server.textDocumentService.didChange(
                DidChangeTextDocumentParams(
                    VersionedTextDocumentIdentifier(documentUri, 4),
                    listOf(TextDocumentContentChangeEvent(validText)),
                ),
            )

            server.textDocumentService.didChange(
                DidChangeTextDocumentParams(
                    VersionedTextDocumentIdentifier(documentUri, 3),
                    listOf(TextDocumentContentChangeEvent(invalidChangedText)),
                ),
            )

            val recoveredDefinition = server.textDocumentService.definition(
                DefinitionParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = Position(19, 17)
                },
            ).get()
            assertEquals(4, server.trackedDocument(documentUri)?.version)
            assertEquals(0, client.publishedDiagnostics.last().diagnostics.size)
            assertEquals(4, client.publishedDiagnostics.last().version)
            assertEquals(1, recoveredDefinition.left.size)
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}
