package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier

class AthenaProjectionRequestTest {
    @Test
    @Suppress("DEPRECATION")
    fun `projection session request exposes runtime owned projection state and governed command allowlist`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-projection-ready-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = demoCabinetSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val payload = server.projectionSession(
                    AthenaProjectionSessionParams(),
                ).get()

                assertNotNull(payload)
                assertEquals("ready", payload.status)
                assertEquals("factory-line", payload.projectName)
                assertEquals(listOf("cabinet", "wiring"), payload.supportedViews.map { view -> view.viewId })
                assertEquals("cabinet", payload.activeViewId)
                assertEquals(
                    listOf("switch-active-view"),
                    payload.governedCommands.map { command -> command.commandId },
                )
                assertTrue(payload.diagnostics.isEmpty())
                assertNull(payload.unavailableReason)

                val readyProjection = assertNotNull(payload.readyProjection)
                assertEquals("DemoCabinet", readyProjection.systemName)
                assertEquals(480, readyProjection.canvasWidth)
                assertEquals(172, readyProjection.canvasHeight)
                assertEquals(
                    listOf("electrical-runtime.render.cabinet"),
                    readyProjection.activeRenderContributions.map { contribution -> contribution.contributionId },
                )
                assertEquals(2, readyProjection.components.size)
                assertEquals(1, readyProjection.connections.size)
                assertEquals(2, readyProjection.labels.size)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `projection command request switches active view through explicit allowlist`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-projection-command-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = demoCabinetSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val commandPayload = server.projectionCommand(
                    AthenaProjectionCommandParams(
                        commandId = "switch-active-view",
                        viewId = "wiring",
                    ),
                ).get()

                assertNotNull(commandPayload)
                assertEquals("applied", commandPayload.status)
                assertEquals("switch-active-view", commandPayload.commandId)
                assertNull(commandPayload.reason)

                val updatedSession = assertNotNull(commandPayload.session)
                assertEquals("wiring", updatedSession.activeViewId)
                assertEquals("ready", updatedSession.status)
                val readyProjection = assertNotNull(updatedSession.readyProjection)
                assertEquals("wiring", readyProjection.viewId)
                assertEquals(
                    listOf("electrical-runtime.render.wiring"),
                    readyProjection.activeRenderContributions.map { contribution -> contribution.contributionId },
                )

                val queriedSession = server.projectionSession(
                    AthenaProjectionSessionParams(),
                ).get()

                assertNotNull(queriedSession)
                assertEquals("wiring", queriedSession.activeViewId)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `projection command request rejects unallowlisted command ids explicitly`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-projection-command-rejected-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = demoCabinetSource,
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val payload = server.projectionCommand(
                    AthenaProjectionCommandParams(
                        commandId = "execute-plugin-command",
                    ),
                ).get()

                assertNotNull(payload)
                assertEquals("rejected", payload.status)
                assertEquals("execute-plugin-command", payload.commandId)
                assertTrue(payload.reason.orEmpty().contains("allowlist", ignoreCase = true))
                assertNull(payload.session)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `projection session request surfaces unavailable state when canonical projection is invalid`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-projection-unavailable-",
            sourceFileName = "broken-demo.athena",
            sourceText = "system Broken {",
        )
        val repositoryRoot = repository.repositoryRoot
        try {
            AthenaCompiler().materializeRepositoryLock(repositoryRoot)

            val server = AthenaLanguageServer()
            try {
                server.initialize(
                    InitializeParams().apply {
                        rootUri = repositoryRoot.toUri().toString()
                    },
                ).get()

                val payload = server.projectionSession(
                    AthenaProjectionSessionParams(),
                ).get()

                assertNotNull(payload)
                assertEquals("unavailable", payload.status)
                assertNull(payload.readyProjection)
                assertTrue(payload.unavailableReason.orEmpty().isNotBlank())
                assertTrue(payload.diagnostics.isNotEmpty())
                assertEquals(
                    listOf("compiler.syntax"),
                    payload.diagnostics.map { diagnostic -> diagnostic.code }.distinct(),
                )
                assertTrue(
                    payload.diagnostics.all { diagnostic -> diagnostic.severity == "error" },
                )
                assertTrue(
                    payload.diagnostics.any { diagnostic ->
                        diagnostic.provenance.orEmpty().contains("broken-demo.athena")
                    },
                )
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `projection session request follows the latest tracked dirty document state`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-lsp-projection-dirty-",
            sourceFileName = "demo-cabinet.athena",
            sourceText = demoCabinetSource,
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
                            demoCabinetSource,
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

                val payload = server.projectionSession(
                    AthenaProjectionSessionParams(),
                ).get()

                assertNotNull(payload)
                assertEquals("ready", payload.status)
                assertEquals("cabinet", payload.activeViewId)
                val readyProjection = assertNotNull(payload.readyProjection)
                assertEquals(2, readyProjection.components.size)
                assertEquals(0, readyProjection.connections.size)
                assertEquals(2, readyProjection.labels.size)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }
}

private val demoCabinetSource = """
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
