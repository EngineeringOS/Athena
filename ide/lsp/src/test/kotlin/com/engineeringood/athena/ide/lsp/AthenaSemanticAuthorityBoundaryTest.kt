package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.diagnosticMessages
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.TextDocumentItem
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Story 4.1 guardrail: proves LSP diagnostics derive exclusively from the compiler parser path.
 *
 * These tests are the mechanical enforcement of AD-108 (LSP semantic diagnostics stay on the compiler
 * parser path) and AD-107 (Tree-sitter owns syntax UX only). They must fail if a future change makes
 * published diagnostics, `toLspDiagnostics`, or the semantic-inspection payload depend on any source
 * other than `com.engineeringood.athena.compiler` and `com.engineeringood.athena.semantics.core`.
 */
class AthenaSemanticAuthorityBoundaryTest {
    @Test
    @Suppress("DEPRECATION")
    fun `published diagnostics for a valid m0 fixture match the compiler result exactly`() {
        assertPublishedDiagnosticsMatchCompilerResult(
            fixtureRelativePath = "examples/m0/demo-cabinet.athena",
            expectedDiagnosticCount = 0,
        )
    }

    @Test
    @Suppress("DEPRECATION")
    fun `published diagnostics for an invalid m0 fixture match the compiler result exactly`() {
        assertPublishedDiagnosticsMatchCompilerResult(
            fixtureRelativePath = "examples/m0/invalid-semantic-cabinet.athena",
            expectedDiagnosticCount = 3,
        )
    }

    @Test
    fun `lsp diagnostics source files never name Tree-sitter as a diagnostics or semantic source`() {
        val repoRoot = resolveRepoRoot()
        val guardedSources = listOf(
            repoRoot.resolve("ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt"),
            repoRoot.resolve("ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt"),
        )
        val forbiddenTokens = listOf("TreeSitter", "treeSitter", "tree_sitter", "tree-sitter")

        guardedSources.forEach { sourcePath ->
            assertTrue(Files.exists(sourcePath), "Expected guarded LSP source at `$sourcePath`.")
            val text = Files.readString(sourcePath)
            forbiddenTokens.forEach { token ->
                // The only allowed mentions are in guardrail KDoc explaining what must NOT happen.
                val offendingLines = text.lineSequence()
                    .filter { line -> line.contains(token, ignoreCase = false) }
                    .filterNot { line -> line.trimStart().startsWith("*") }
                assertTrue(
                    offendingLines.none(),
                    "`${sourcePath.fileName}` references `$token` outside guardrail KDoc: $offendingLines",
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun assertPublishedDiagnosticsMatchCompilerResult(
        fixtureRelativePath: String,
        expectedDiagnosticCount: Int,
    ) {
        val fixtureText = Files.readString(resolveRepoRoot().resolve(fixtureRelativePath))
        val repository = createGovernedTestRepository("athena-lsp-authority-boundary-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        val documentUri = sourcePath.toUri().toString()

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
                    TextDocumentItem(documentUri, "athena", 1, fixtureText),
                ),
            )

            val published = client.publishedDiagnostics.last()
            val trackedDocument = assertNotNull(server.trackedDocument(documentUri))

            // The published diagnostics must be exactly the compiler result's diagnostics for the same
            // compilation: no extra, no missing, no reordered diagnostics from any other source.
            assertEquals(
                trackedDocument.compilation.diagnosticMessages(),
                published.diagnostics.map { diagnostic -> diagnostic.message },
            )
            assertEquals(expectedDiagnosticCount, published.diagnostics.size)
            assertTrue(
                published.diagnostics.all { diagnostic ->
                    diagnostic.source in setOf("Athena syntax", "Athena semantic", "Athena knowledge")
                },
                "Every published diagnostic must carry a compiler-owned source label.",
            )
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) {
            current = current.parent
        }
        assertTrue(Files.exists(current.resolve("settings.gradle.kts")), "Could not locate repository root")
        return current
    }
}
