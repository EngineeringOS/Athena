package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.TextDocumentItem
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AthenaConnectionReadModelRequestTest {
    @Test
    fun `tracked primary source change publishes stale instead of rereading accepted disk source`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-connection-read-model-buffer-",
            sourceText = connectionSource(),
        )
        try {
            AthenaCompiler().materializeRepositoryLock(repository.repositoryRoot)
            val server = AthenaLanguageServer()
            try {
                server.initialize(workspaceInitializeParams(repository.repositoryRoot)).get()
                val uri = repository.seedSourcePath.toUri().toString()
                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(TextDocumentItem(uri, "athena", 1, connectionSource())),
                )
                val params = AthenaConnectionReadModelParams(AthenaConnectionReadModelTextDocument(uri))
                val accepted = assertNotNull(server.connectionReadModelDomain(params))
                assertEquals(ConnectionReadModelPublicationState.READY, accepted.state)

                server.textDocumentService.didOpen(
                    DidOpenTextDocumentParams(
                        TextDocumentItem(uri, "athena", 2, "system FactoryLine { connect wire Missing.out to Missing.in }"),
                    ),
                )
                val stale = assertNotNull(server.connectionReadModelDomain(params))

                assertEquals(ConnectionReadModelPublicationState.STALE, stale.state)
                assertNotEquals(stale.attemptedInputRevision, stale.acceptedInputRevision)
                assertEquals(accepted.items, stale.items)
            } finally {
                server.shutdown().get()
            }
        } finally {
            repository.repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `request publishes deterministic complete connection and net facts independently of placement`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-connection-read-model-",
            sourceText = connectionSource(),
        )
        try {
            AthenaCompiler().materializeRepositoryLock(repository.repositoryRoot)
            val server = AthenaLanguageServer()
            try {
                server.initialize(workspaceInitializeParams(repository.repositoryRoot)).get()

                val payload = assertNotNull(server.connectionReadModelDomain(AthenaConnectionReadModelParams()))

                assertEquals(ConnectionReadModelPublicationState.READY, payload.state, payload.diagnostics.toString())
                assertEquals(payload.attemptedInputRevision, payload.acceptedInputRevision)
                assertTrue(payload.connectionIrDigest?.isNotBlank() == true)
                assertEquals(
                    listOf(ConnectionReadModelItemKind.CONNECTION, ConnectionReadModelItemKind.NET),
                    payload.items.map { it.itemKind },
                )
                val connection = payload.items.first()
                assertEquals("WIRE", connection.connectionKind)
                assertEquals(listOf("SOURCE", "SINK"), connection.endpoints.map { it.role })
                assertEquals(listOf("PLC1.out", "KM1.coil"), connection.endpoints.map { it.authoredPath })
                assertEquals(ConnectionReadModelValidationState.VALID, connection.validation.state)
                assertTrue(!connection.placed)
                assertEquals(0, connection.projectionCount)
                assertTrue(connection.projectionTraceIds.isEmpty())
                assertTrue(connection.sourceTrace.relativePath.endsWith("factoryline.athena"))

                val net = payload.items.last()
                assertEquals("StartCircuit", net.displayName)
                assertEquals("SIGNAL", net.connectionKind)
                assertEquals("Control24V", net.potentialOrSignal)
                assertEquals(listOf("crossSection"), net.resolvedSpecifications.map { it.name })
                assertEquals(3, net.endpoints.size)
                assertTrue(net.endpoints.all { it.sourceTrace.relativePath.endsWith("factoryline.athena") })
            } finally {
                server.shutdown().get()
            }
        } finally {
            repository.repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `failed replacement is stale with explicit attempted and accepted revisions`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-connection-read-model-stale-",
            sourceText = connectionSource(),
        )
        try {
            val server = AthenaLanguageServer()
            try {
                server.initialize(workspaceInitializeParams(repository.repositoryRoot)).get()
                val accepted = assertNotNull(server.connectionReadModelDomain(AthenaConnectionReadModelParams()))
                assertEquals(ConnectionReadModelPublicationState.READY, accepted.state, accepted.diagnostics.toString())

                repository.seedSourcePath.writeText(
                    governedAthenaSource("system FactoryLine { connect wire Missing.out to Missing.in }"),
                )
                val stale = assertNotNull(server.connectionReadModelDomain(AthenaConnectionReadModelParams()))

                assertEquals(ConnectionReadModelPublicationState.STALE, stale.state)
                assertNotEquals(stale.attemptedInputRevision, stale.acceptedInputRevision)
                assertEquals(accepted.acceptedInputRevision, stale.acceptedInputRevision)
                assertEquals(accepted.items, stale.items)
                assertTrue(stale.diagnostics.isNotEmpty())
            } finally {
                server.shutdown().get()
            }
        } finally {
            repository.repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `failed first compilation is unavailable without fabricated accepted state`() {
        val repository = createGovernedTestRepository(
            prefix = "athena-connection-read-model-unavailable-",
            sourceText = "system FactoryLine { connect wire Missing.out to Missing.in }",
        )
        try {
            AthenaCompiler().materializeRepositoryLock(repository.repositoryRoot)
            val server = AthenaLanguageServer()
            try {
                server.initialize(workspaceInitializeParams(repository.repositoryRoot)).get()

                val unavailable = assertNotNull(server.connectionReadModelDomain(AthenaConnectionReadModelParams()))

                assertEquals(ConnectionReadModelPublicationState.UNAVAILABLE, unavailable.state)
                assertEquals(null, unavailable.acceptedInputRevision)
                assertTrue(unavailable.items.isEmpty())
                assertTrue(unavailable.diagnostics.isNotEmpty())
            } finally {
                server.shutdown().get()
            }
        } finally {
            repository.repositoryRoot.toFile().deleteRecursively()
        }
    }

    private fun connectionSource(): String = """
        system FactoryLine {
          entity PLC1 { concept controller }
          entity KM1 { concept contactor }
          entity X1 { concept terminal }
          port PLC1.out { direction out flow control }
          port KM1.coil { direction in flow control }
          port X1.p1 { direction bidirectional flow control }
          connect wire PLC1.out to KM1.coil {
            crossSection 0.75 [mm2]
          }
          net StartCircuit signal {
            source PLC1.out
            sink KM1.coil
            pass X1.p1
            signal Control24V
            crossSection 0.75 [mm2]
          }
        }
    """.trimIndent()

}
