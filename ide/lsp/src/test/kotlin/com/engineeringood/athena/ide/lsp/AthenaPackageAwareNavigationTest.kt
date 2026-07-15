package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

class AthenaPackageAwareNavigationTest {
    @Test
    @Suppress("DEPRECATION")
    fun `definition and references cross governed source units through project semantic snapshot`() {
        val consumerText = """
            package com.root

            system Consumer {
              device Local {}
              port Local.in {}
              connect Shared.out -> Local.in
            }
        """.trimIndent()
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-package-navigation-",
            packageName = "com.root",
            sourceFileName = "consumer.athena",
            sourceText = consumerText,
        )
        val repositoryRoot = repository.repositoryRoot
        val consumerPath = repository.seedSourcePath
        val providerPath = repository.sourceRoot.resolve("provider.athena")
        val providerText = """
            package com.root

            system Provider {
              device Shared {}
              port Shared.out {}
            }
        """.trimIndent()
        providerPath.writeText(providerText)
        AthenaCompiler().materializeRepositoryLock(repositoryRoot)

        val server = AthenaLanguageServer()
        try {
            server.initialize(
                org.eclipse.lsp4j.InitializeParams().apply {
                    rootUri = repositoryRoot.toUri().toString()
                },
            ).get()

            val consumerUri = consumerPath.toUri().toString()
            val providerUri = providerPath.toUri().toString()
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

            val sharedReferencePosition = Position(5, 12)
            val definition = server.textDocumentService.definition(
                DefinitionParams(TextDocumentIdentifier(consumerUri), sharedReferencePosition),
            ).get().left
            assertEquals(1, definition.size)
            assertEquals(providerUri, definition.single().uri)
            assertEquals(4, definition.single().range.start.line)
            assertEquals(2, definition.single().range.start.character)

            val referencesWithoutDeclaration = server.textDocumentService.references(
                ReferenceParams(
                    TextDocumentIdentifier(consumerUri),
                    sharedReferencePosition,
                    ReferenceContext(false),
                ),
            ).get()
            assertEquals(listOf(consumerUri), referencesWithoutDeclaration.map { location -> location.uri })

            val referencesWithDeclaration = server.textDocumentService.references(
                ReferenceParams(
                    TextDocumentIdentifier(consumerUri),
                    sharedReferencePosition,
                    ReferenceContext(true),
                ),
            ).get()
            assertEquals(listOf(providerUri, consumerUri), referencesWithDeclaration.map { location -> location.uri })
            assertEquals(listOf(4, 5), referencesWithDeclaration.map { location -> location.range.start.line })
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}
