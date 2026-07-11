package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaSourceMutationRequestTest {
    @Test
    @Suppress("DEPRECATION")
    fun `source mutation request exposes accepted runtime-owned payload for latest tracked document and ignores stale edits`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-source-mutation-accepted-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = sourceMutationDemoCabinetSource,
        )
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

                val documentUri = sourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(
                            documentUri,
                            "athena",
                            1,
                            sourceMutationDemoCabinetSource,
                        ),
                    ),
                )
                server.textDocumentService.didChange(
                    DidChangeTextDocumentParams(
                        VersionedTextDocumentIdentifier(documentUri, 2),
                        listOf(
                            TextDocumentContentChangeEvent(
                                """
                                    system DemoCabinet {
                                      device PLC1 {
                                        type Switch
                                        model "S7-1200"
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
                                    }
                                """.trimIndent(),
                            ),
                        ),
                    ),
                )
                server.textDocumentService.didChange(
                    DidChangeTextDocumentParams(
                        VersionedTextDocumentIdentifier(documentUri, 1),
                        listOf(TextDocumentContentChangeEvent(sourceMutationDemoCabinetSource)),
                    ),
                )

                val payload = assertNotNull(
                    server.sourceMutationEvaluation(
                        AthenaSourceMutationParams(
                            textDocument = AthenaSourceMutationTextDocument(documentUri),
                        ),
                    ).get(),
                )
                assertEquals(documentUri, payload.uri)
                assertEquals(2, payload.version)
                assertEquals("semantic-mutation", payload.mutationCategory)
                assertEquals("accepted", payload.outcome)
                assertContains(payload.changedSemanticIds, "connection:PLC1.out->M1.in")
                assertTrue(payload.validationFeedback.isEmpty())
                val inspection = assertNotNull(payload.inspection)
                assertEquals("source", inspection.source)
                assertContains(inspection.affectedSemanticIds, "connection:PLC1.out->M1.in")
                val semanticReview = assertNotNull(payload.semanticReview)
                assertEquals(1, semanticReview.authoredChangeCount)
                assertTrue(semanticReview.reviewSummary.entryCount > 0)
                assertTrue(semanticReview.commitIntent.entryCount > 0)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `source mutation request surfaces validation feedback for invalid tracked text`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-source-mutation-invalid-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = sourceMutationDemoCabinetSource,
        )
        val repositoryRoot = repository.repositoryRoot
        val sourcePath = repository.seedSourcePath

        try {
            val server = AthenaLanguageServer()
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
                            sourceMutationDemoCabinetSource,
                        ),
                    ),
                )
                server.textDocumentService.didChange(
                    DidChangeTextDocumentParams(
                        VersionedTextDocumentIdentifier(documentUri, 2),
                        listOf(
                            TextDocumentContentChangeEvent(
                                """
                                    system DemoCabinet {
                                      device PLC1 {
                                        type Switch
                                      }

                                      port PLC1.out {
                                        direction out
                                        signal Digital
                                      }

                                      connect PLC1.out -> M1.in
                                    }
                                """.trimIndent(),
                            ),
                        ),
                    ),
                )

                val payload = assertNotNull(
                    server.sourceMutationEvaluation(
                        AthenaSourceMutationParams(
                            textDocument = AthenaSourceMutationTextDocument(documentUri),
                        ),
                    ).get(),
                )
                assertEquals(documentUri, payload.uri)
                assertEquals(2, payload.version)
                assertEquals("validation-feedback", payload.outcome)
                assertTrue(payload.validationFeedback.isNotEmpty())
                assertTrue(payload.validationFeedback.all { feedback -> feedback.code.isNotBlank() })
                assertContains(
                    payload.validationFeedback.flatMap { feedback -> feedback.relatedSemanticIds },
                    "connection:PLC1.out->M1.in",
                )
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `source mutation request returns a structured unavailable payload when the lsp session is inactive`() {
        val sourcePath = Files.createTempFile("athena-lsp-source-mutation-inactive-", ".athena")

        try {
            val server = AthenaLanguageServer()
            val payload = assertNotNull(
                server.sourceMutationEvaluation(
                    AthenaSourceMutationParams(
                        textDocument = AthenaSourceMutationTextDocument(sourcePath.toUri().toString()),
                    ),
                ).get(),
            )

            assertEquals(sourcePath.toUri().toString(), payload.uri)
            assertEquals("semantic-mutation", payload.mutationCategory)
            assertEquals("unavailable", payload.outcome)
            assertEquals(0, payload.version)
            assertContains(payload.reason.orEmpty().lowercase(), "inactive")
            assertTrue(payload.projectName.isNotBlank())
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `source mutation request returns unavailable payload for untracked documents inside an active repository`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-source-mutation-untracked-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = sourceMutationDemoCabinetSource,
        )
        val repositoryRoot = repository.repositoryRoot
        val untrackedPath = repositoryRoot.resolve("src/untracked.athena")

        try {
            Files.createDirectories(untrackedPath.parent)
            Files.writeString(untrackedPath, sourceMutationDemoCabinetSource)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val payload = assertNotNull(
                    server.sourceMutationEvaluation(
                        AthenaSourceMutationParams(
                            textDocument = AthenaSourceMutationTextDocument(untrackedPath.toUri().toString()),
                        ),
                    ).get(),
                )

                assertEquals(untrackedPath.toUri().toString(), payload.uri)
                assertEquals("factory-line", payload.projectName)
                assertEquals("semantic-mutation", payload.mutationCategory)
                assertEquals("unavailable", payload.outcome)
                assertEquals(0, payload.version)
                assertContains(payload.reason.orEmpty(), "tracked dirty document")
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}

private val sourceMutationDemoCabinetSource = """
    system DemoCabinet {
      device PLC1 {
        type Switch
        model "S7-1200"
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

      connect PLC1.out -> M1.in
    }
""".trimIndent()
