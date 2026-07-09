package com.engineeringood.athena.ide.lsp

import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.eclipse.lsp4j.services.LanguageClient
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies that Athena LSP publishes JVM-sourced diagnostics for open and change events.
 */
class AthenaDiagnosticsPublishingTest {
    @Test
    @Suppress("DEPRECATION")
    fun `publish diagnostics for invalid open text and clear after valid change`() {
        val repository = createGovernedTestRepository("athena-lsp-diagnostics-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath

        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        server.connect(client)

        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = repositoryRoot.toUri().toString()
                },
            ).get()

            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        sourcePath.toUri().toString(),
                        "athena",
                        1,
                        "system FactoryLine {\n  connect Motor1.out -> Missing.in\n}",
                    ),
                ),
            )

            assertTrue(client.publishedDiagnostics.isNotEmpty())
            val invalidOpen = client.publishedDiagnostics.last()
            assertEquals(2, invalidOpen.diagnostics.size)
            assertTrue(invalidOpen.diagnostics.all { diagnostic -> diagnostic.source == "Athena semantic" })

            server.textDocumentService.didChange(
                DidChangeTextDocumentParams(
                    VersionedTextDocumentIdentifier(sourcePath.toUri().toString(), 2),
                    listOf(
                        TextDocumentContentChangeEvent(
                            "system FactoryLine {\n  device Motor1 {\n    type Motor\n  }\n  device Missing {\n    type Motor\n  }\n  port Motor1.out {\n    direction out\n    signal Digital\n  }\n  port Missing.in {\n    direction in\n    signal Digital\n  }\n  connect Motor1.out -> Missing.in\n}",
                        ),
                    ),
                ),
            )

            val validChange = client.publishedDiagnostics.last()
            assertEquals(0, validChange.diagnostics.size)
            assertTrue(
                client.loggedMessages.any { message ->
                    message.contains("Athena diagnostics published from JVM stack")
                },
            )
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}

class AthenaRecordingLanguageClient : LanguageClient {
    val publishedDiagnostics = mutableListOf<PublishDiagnosticsParams>()
    val loggedMessages = mutableListOf<String>()

    override fun telemetryEvent(`object`: Any?) = Unit

    override fun publishDiagnostics(diagnostics: PublishDiagnosticsParams) {
        publishedDiagnostics += diagnostics
    }

    override fun showMessage(messageParams: MessageParams) = Unit

    override fun showMessageRequest(requestParams: ShowMessageRequestParams): CompletableFuture<MessageActionItem> {
        return CompletableFuture.completedFuture(null)
    }

    override fun logMessage(message: MessageParams) {
        loggedMessages += message.message
    }
}
