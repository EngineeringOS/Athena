package com.engineeringood.athena.ide.lsp

import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.SymbolKind
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies the first M4 authoring and navigation feature set served through Athena LSP.
 */
class AthenaAuthoringSupportTest {
    @Test
    @Suppress("DEPRECATION")
    fun `completion document symbols definition and references are served from Athena LSP`() {
        val repository = createGovernedTestRepository("athena-lsp-authoring-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        val incompleteSourceText = """
            system FactoryLine {
              device PLC1 {
                type Switch
              }

              port PLC1.out {
                direction out
                signal Digital
              }

              con
              connect PLC1.out -> PLC1.out
            }
        """.trimIndent()
        val validSourceText = """
            system FactoryLine {
              device PLC1 {
                type Switch
              }

              port PLC1.out {
                direction out
                signal Digital
              }

              connect PLC1.out -> PLC1.out
            }
        """.trimIndent()
        sourcePath.writeText(validSourceText)

        val server = AthenaLanguageServer()
        try {
            server.initialize(org.eclipse.lsp4j.InitializeParams().apply {
                rootUri = repositoryRoot.toUri().toString()
            }).get()

            val documentUri = sourcePath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        documentUri,
                        "athena",
                        1,
                        incompleteSourceText,
                    ),
                ),
            )

            val completion = server.textDocumentService.completion(
                CompletionParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = Position(9, 5)
                },
            ).get()
            val completionItems = completion.right.items
            assertTrue(completionItems.any { item -> item.label == "connect" })

            server.textDocumentService.didChange(
                DidChangeTextDocumentParams().apply {
                    textDocument = VersionedTextDocumentIdentifier(documentUri, 2)
                    contentChanges = listOf(TextDocumentContentChangeEvent(validSourceText))
                },
            )

            val symbols = server.textDocumentService.documentSymbol(
                DocumentSymbolParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                },
            ).get()
            val systemSymbol = symbols.single().right
            assertEquals("FactoryLine", systemSymbol.name)
            assertEquals(listOf("PLC1", "PLC1.out", "connect PLC1.out -> PLC1.out"), systemSymbol.children.map { child -> child.name })

            val definition = server.textDocumentService.definition(
                DefinitionParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = Position(10, 17)
                },
            ).get()
            val definitionRange = definition.left.single().range
            assertEquals(5, definitionRange.start.line)

            val references = server.textDocumentService.references(
                ReferenceParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                    position = Position(10, 17)
                    context = ReferenceContext(true)
                },
            ).get()
            assertEquals(3, references.size)
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `document symbols nest compact authored ports under their owning device`() {
        val repository = createGovernedTestRepository("athena-lsp-nested-port-outline-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        val sourceText = """
            system CompactDeviceOutline {
              device SpareTerminalXT99 {
                type Switch
                model "SPARE-XT"

                port in1 {
                  direction in
                  signal Digital
                }
              }
            }
        """.trimIndent()
        sourcePath.writeText(sourceText)

        val server = AthenaLanguageServer()
        try {
            server.initialize(org.eclipse.lsp4j.InitializeParams().apply {
                rootUri = repositoryRoot.toUri().toString()
            }).get()

            val documentUri = sourcePath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        documentUri,
                        "athena",
                        1,
                        sourceText,
                    ),
                ),
            )

            val symbols = server.textDocumentService.documentSymbol(
                DocumentSymbolParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                },
            ).get()
            val systemSymbol = symbols.single().right
            val deviceSymbol = systemSymbol.children.single { child -> child.name == "SpareTerminalXT99" }
            val portSymbol = deviceSymbol.children.single { child -> child.name == "in1" }

            assertEquals(
                listOf("type", "model", "in1"),
                deviceSymbol.children.map { child -> child.name },
            )
            assertEquals(SymbolKind.Field, portSymbol.kind)
            assertEquals("port", portSymbol.detail)
            assertEquals(
                listOf("direction", "signal"),
                portSymbol.children.map { child -> child.name },
            )
            assertTrue(systemSymbol.children.none { child -> child.name == "SpareTerminalXT99.in1" })
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `document symbols expose grouped connect block and child edges`() {
        val repository = createGovernedTestRepository("athena-lsp-grouped-connect-outline-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        val sourceText = """
            system GroupedConnectOutline {
              device PLC1 {
                type Switch
              }
              device M1 {
                type Motor
              }
              port PLC1.out {
                direction out
                signal Digital
              }
              port M1.in {
                direction in
                signal Digital
              }

              connect control_feed {
                PLC1.out -> M1.in
              }
            }
        """.trimIndent()
        sourcePath.writeText(sourceText)

        val server = AthenaLanguageServer()
        try {
            server.initialize(org.eclipse.lsp4j.InitializeParams().apply {
                rootUri = repositoryRoot.toUri().toString()
            }).get()

            val documentUri = sourcePath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        documentUri,
                        "athena",
                        1,
                        sourceText,
                    ),
                ),
            )

            val symbols = server.textDocumentService.documentSymbol(
                DocumentSymbolParams().apply {
                    textDocument = TextDocumentIdentifier(documentUri)
                },
            ).get()
            val systemSymbol = symbols.single().right
            val groupSymbol = systemSymbol.children.single { child -> child.name == "connect control_feed" }

            assertEquals(SymbolKind.Module, groupSymbol.kind)
            assertEquals("connect group", groupSymbol.detail)
            assertEquals(
                listOf("PLC1.out -> M1.in"),
                groupSymbol.children.map { child -> child.name },
            )
            assertEquals("connect edge", groupSymbol.children.single().detail)
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}
