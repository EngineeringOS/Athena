package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.TextDocumentItem

class AthenaM18RepositoryProofCorpusLspTest {
    @Test
    @Suppress("DEPRECATION")
    fun `lsp publishes package aware diagnostics from repository backed m18 corpus source`() {
        val corpusSource = corpusRoot().resolve("valid-workspace/src/invalid-import.athena").readText()
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-m18-corpus-",
            packageName = "com.engineeringood.m18.root",
            sourceFileName = "invalid-import.athena",
            sourceText = corpusSource,
        )
        val repositoryRoot = repository.repositoryRoot
        val invalidImportPath = repository.seedSourcePath
        AthenaCompiler().materializeRepositoryLock(repositoryRoot)

        val client = AthenaRecordingLanguageClient()
        val server = AthenaLanguageServer()
        server.connect(client)
        try {
            server.initialize(
                InitializeParams().apply {
                    rootUri = repositoryRoot.toUri().toString()
                },
            ).get()

            val invalidUri = invalidImportPath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        invalidUri,
                        "athena",
                        1,
                        invalidImportPath.readText(),
                    ),
                ),
            )

            val invalidDiagnostics = assertNotNull(
                client.publishedDiagnostics.lastOrNull { diagnostics -> diagnostics.uri == invalidUri },
            )
            assertTrue(
                invalidDiagnostics.diagnostics.any { diagnostic ->
                    diagnostic.code?.left == "semantic.import.namespace.unavailable"
                },
                invalidDiagnostics.diagnostics.joinToString(separator = "\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
            )
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    private fun corpusRoot(): Path {
        val root = listOf(
            Path.of("examples", "m18", "repository-proof"),
            Path.of("..", "..", "examples", "m18", "repository-proof"),
        ).map { it.toAbsolutePath().normalize() }
            .firstOrNull(Files::isDirectory)
        assertNotNull(root, "Missing examples/m18/repository-proof")
        return root
    }
}
