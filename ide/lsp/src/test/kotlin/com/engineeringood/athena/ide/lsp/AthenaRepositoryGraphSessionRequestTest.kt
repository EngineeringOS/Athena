package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.TextDocumentItem

class AthenaRepositoryGraphSessionRequestTest {
    @Test
    @Suppress("DEPRECATION")
    fun `repository graph session request exposes runtime owned canonical package state`() {
        val repository = createGovernedTestRepository("athena-lsp-repository-session-")
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
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
                            "system FactoryLine { }",
                        ),
                    ),
                )

                val payload = server.repositoryGraphSession(
                    AthenaRepositoryGraphSessionParams(),
                ).get()

                assertNotNull(payload)
                assertEquals(repositoryRoot.toAbsolutePath().normalize().toString(), payload.repositoryRoot)
                assertEquals("com.engineeringood.factory-line", payload.primaryPackageName)
                assertEquals("current", payload.lockState)
                assertTrue(payload.isValid)
                assertEquals(sourcePath.toUri().toString(), payload.lastOpenedDocumentUri)
                assertTrue(payload.manifestDependencies.isEmpty())
                assertEquals(
                    listOf("com.engineeringood.factory-line"),
                    payload.resolvedPackages.map { resolvedPackage -> resolvedPackage.name },
                )
                assertTrue(payload.resolvedPackages.first().directDependencies.isEmpty())
                assertTrue(payload.diagnostics.isEmpty())
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `repository graph session request surfaces stale lock diagnostics`() {
        val repository = createGovernedTestRepository("athena-lsp-repository-session-invalid-")
        val repositoryRoot = repository.repositoryRoot
        try {
            repositoryRoot.resolve("athena.lock").writeText(
                """
                    version: nope
                    primaryPackage:
                      name: com.engineeringood.factory-line
                      version: 1.0.0
                """.trimIndent(),
            )

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val payload = server.repositoryGraphSession(
                    AthenaRepositoryGraphSessionParams(),
                ).get()

                assertNotNull(payload)
                assertEquals("invalid", payload.lockState)
                assertTrue(!payload.isValid)
                assertTrue(
                    payload.diagnostics.any { diagnostic -> diagnostic.code == "repository.lock.version.invalid" },
                )
                assertEquals(
                    listOf("com.engineeringood.factory-line"),
                    payload.resolvedPackages.map { resolvedPackage -> resolvedPackage.name },
                )
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}
