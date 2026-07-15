package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.SymbolKind
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

class AthenaPackageAwareSymbolsTest {
    @Test
    @Suppress("DEPRECATION")
    fun `document symbols expose package root for package aware source units`() {
        val consumerText = """
            package com.root

            system Consumer {
              device Local {}
              port Local.in {}
              connect Shared.out -> Local.in
            }
        """.trimIndent()
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-package-symbols-",
            packageName = "com.root",
            sourceFileName = "consumer.athena",
            sourceText = consumerText,
        )
        val repositoryRoot = repository.repositoryRoot
        val consumerPath = repository.seedSourcePath
        repository.sourceRoot.resolve("provider.athena").writeText(
            """
                package com.root

                system Provider {
                  device Shared {}
                  port Shared.out {}
                }
            """.trimIndent(),
        )
        AthenaCompiler().materializeRepositoryLock(repositoryRoot)

        val server = AthenaLanguageServer()
        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = repositoryRoot.toUri().toString()
                },
            ).get()

            val consumerUri = consumerPath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        consumerUri,
                        "athena",
                        1,
                        consumerText,
                    ),
                ),
            )

            val symbols = server.textDocumentService.documentSymbol(
                DocumentSymbolParams(TextDocumentIdentifier(consumerUri)),
            ).get()

            val packageSymbol = symbols.single().right
            assertEquals("com.root", packageSymbol.name)
            assertEquals(SymbolKind.Package, packageSymbol.kind)
            val systemSymbol = packageSymbol.children.single()
            assertEquals("Consumer", systemSymbol.name)
            assertEquals(SymbolKind.Module, systemSymbol.kind)
            assertEquals(
                listOf("Local", "Local.in", "connect Shared.out -> Local.in"),
                systemSymbol.children.map { symbol -> symbol.name },
            )
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}
