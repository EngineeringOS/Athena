package com.engineeringood.athena.ide.lsp

import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
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
    @Suppress("DEPRECATION")
    fun `initialize activates one governed repository-backed runtime session`() {
        val repositoryRoot = createTempDirectory("athena-lsp-")
        repositoryRoot.resolve("athena.yaml").writeText(
            """
                primaryPackage:
                  name: com.engineeringood.factory-line
                  version: 0.1.0
                  sourceRoot: src
            """.trimIndent(),
        )
        repositoryRoot.resolve("athena.lock").writeText(
            """
                version: 1
                primaryPackage:
                  name: com.engineeringood.factory-line
                  version: 0.1.0
                packages:
                  - name: com.engineeringood.factory-line
                    version: 0.1.0
                    sourceRoot: src
                    dependencies: []
            """.trimIndent(),
        )
        val sourceRoot = repositoryRoot.resolve("src").createDirectories()
        val sourcePath = sourceRoot.resolve("a-control.athena")
        val openedSourcePath = sourceRoot.resolve("factory-line.athena")
        sourcePath.writeText("system Control { }")
        openedSourcePath.writeText("system FactoryLine { }")

        val server = AthenaLanguageServer()
        try {
            val result = server.initialize(
                InitializeParams().apply {
                    rootUri = repositoryRoot.toUri().toString()
                },
            ).get()

            val transportPayload = result.capabilities.experimental as Map<*, *>
            assertEquals(repositoryRoot.toAbsolutePath().normalize().toString(), transportPayload["repositoryRoot"])
            assertEquals(repositoryRoot.resolve("athena.yaml").toAbsolutePath().normalize().toString(), transportPayload["manifestPath"])
            assertEquals(repositoryRoot.resolve("athena.lock").toAbsolutePath().normalize().toString(), transportPayload["lockPath"])
            assertEquals(sourceRoot.toAbsolutePath().normalize().toString(), transportPayload["sourceRootPath"])
            assertEquals(sourcePath.toAbsolutePath().normalize().toString(), transportPayload["sourcePath"])
            assertEquals("factory-line", transportPayload["projectName"])
            assertEquals("com.engineeringood.factory-line", transportPayload["primaryPackageName"])
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
    @Suppress("DEPRECATION")
    fun `initialize rejects invalid repositories when the governed source root has no authored source`() {
        val repositoryRoot = createTempDirectory("athena-lsp-invalid-")
        repositoryRoot.resolve("athena.yaml").writeText(
            """
                primaryPackage:
                  name: com.engineeringood.factory-line
                  version: 0.1.0
                  sourceRoot: src
            """.trimIndent(),
        )
        repositoryRoot.resolve("src").createDirectories()

        val server = AthenaLanguageServer()
        try {
            val exception = assertFailsWith<ExecutionException> {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()
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
