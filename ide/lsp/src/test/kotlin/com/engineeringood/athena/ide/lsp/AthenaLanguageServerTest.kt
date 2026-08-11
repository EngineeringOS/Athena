package com.engineeringood.athena.ide.lsp

import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import java.util.concurrent.ExecutionException
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Verifies the governed M5 Athena LSP bootstrap contract.
 */
class AthenaLanguageServerTest {
    @Test
    fun `sheet companion has outline and no project parser diagnostic`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-sheet-lsp-",
            sourceFileName = "rolling-shutter.athena",
            sourceText = "system RollingShutter { }",
        )
        val sheetPath = repository.sourceRoot.resolve("com/engineeringood/factoryline/rolling-shutter.sheet.athena")
        val sheetText = """
            sheet rolling-shutter {
              page format A3 landscape
              frame: 17 * 16
              snap: 1
              title "Rolling Shutter"
              "Supply" at (8, 12)
            }
        """.trimIndent()
        sheetPath.writeText(sheetText)
        val server = AthenaLanguageServer()
        try {
            server.initialize(workspaceInitializeParams(repository.repositoryRoot)).get()
            val uri = sheetPath.toUri().toString()
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(TextDocumentItem(uri, "athena-sheet", 1, sheetText)),
            )
            val symbols = server.textDocumentService.documentSymbol(
                DocumentSymbolParams(TextDocumentIdentifier(uri)),
            ).get()
            assertEquals("sheet rolling-shutter", symbols.single().right.name)
            assertEquals(5, symbols.single().right.children.size)
            assertTrue(server.trackedDocument(uri)?.sheetCompanion is com.engineeringood.athena.language.SheetCompanionParseSuccess)
        } finally {
            server.shutdown().get()
            repository.repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `initialize activates one governed repository-backed runtime session`() {
        val repositoryRoot = createTempDirectory("athena-lsp-")
        repositoryRoot.resolve("athena.yaml").writeText(
            """
                primaryPackage:
                  name: com.engineeringood.factoryline
                  version: 0.1.0
                  sourceRoot: src
            """.trimIndent(),
        )
        repositoryRoot.resolve("athena.lock").writeText(
            """
                version: 1
                primaryPackage:
                  name: com.engineeringood.factoryline
                  version: 0.1.0
                packages:
                  - name: com.engineeringood.factoryline
                    version: 0.1.0
                    sourceRoot: src
                    dependencies: []
            """.trimIndent(),
        )
        val sourceRoot = repositoryRoot.resolve("src").createDirectories()
        val packageRoot = sourceRoot.resolve("com/engineeringood/factoryline").createDirectories()
        val sourcePath = packageRoot.resolve("a-control.athena")
        val openedSourcePath = packageRoot.resolve("factoryline.athena")
        sourcePath.writeText(governedAthenaSource("system Control { }"))
        openedSourcePath.writeText(governedAthenaSource("system FactoryLine { }"))

        val server = AthenaLanguageServer()
        try {
            val result = server.initialize(workspaceInitializeParams(repositoryRoot)).get()

            val transportPayload = result.capabilities.experimental as Map<*, *>
            assertEquals(repositoryRoot.toAbsolutePath().normalize().toString(), transportPayload["repositoryRoot"])
            assertEquals(repositoryRoot.resolve("athena.yaml").toAbsolutePath().normalize().toString(), transportPayload["manifestPath"])
            assertEquals(repositoryRoot.resolve("athena.lock").toAbsolutePath().normalize().toString(), transportPayload["lockPath"])
            assertEquals(sourceRoot.toAbsolutePath().normalize().toString(), transportPayload["sourceRootPath"])
            assertEquals(sourcePath.toAbsolutePath().normalize().toString(), transportPayload["sourcePath"])
            assertEquals("factoryline", transportPayload["projectName"])
            assertEquals("com.engineeringood.factoryline", transportPayload["primaryPackageName"])
            assertNotNull(result.capabilities.textDocumentSync)

            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(
                        openedSourcePath.toUri().toString(),
                        "athena",
                        1,
                        "system FactoryLine { }",
                    ),
                ),
            )

            assertEquals(
                openedSourcePath.toUri().toString(),
                server.currentSessionSnapshot()?.lastOpenedDocumentUri,
            )
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `initialize rejects invalid repositories when the governed source root has no authored source`() {
        val repositoryRoot = createTempDirectory("athena-lsp-invalid-")
        repositoryRoot.resolve("athena.yaml").writeText(
            """
                primaryPackage:
                  name: com.engineeringood.factoryline
                  version: 0.1.0
                  sourceRoot: src
            """.trimIndent(),
        )
        repositoryRoot.resolve("src").createDirectories()

        val server = AthenaLanguageServer()
        try {
            val exception = assertFailsWith<ExecutionException> {
                server.initialize(workspaceInitializeParams(repositoryRoot)).get()
            }

            val message = exception.cause?.message.orEmpty()
            assertTrue(message.contains("does not contain an authored `.athena` source"))
            assertTrue(message.contains("src/"))
        } finally {
            server.shutdown().get()
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}
