package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.interaction.AdjustConnectionRoute
import com.engineeringood.athena.interaction.ConnectPorts
import com.engineeringood.athena.interaction.ConnectionEditEndpoint
import com.engineeringood.athena.interaction.ConnectionEditEndpointRole
import com.engineeringood.athena.interaction.ConnectionEditKind
import com.engineeringood.athena.interaction.ConnectionRequirementIntent
import com.engineeringood.athena.interaction.ConnectionRequirementKind
import com.engineeringood.athena.interaction.ConnectionRequirementValue
import com.engineeringood.athena.interaction.EditOperationEnvelope
import com.engineeringood.athena.interaction.EditOperationStatus
import com.engineeringood.athena.interaction.LogicalRoutePoint
import com.engineeringood.athena.interaction.LogicalRouteTarget
import com.engineeringood.athena.interaction.LogicalRouteTargetKind
import com.engineeringood.athena.interaction.OperationTarget
import com.engineeringood.athena.interaction.ReconnectConnectionEndpoint
import com.engineeringood.athena.interaction.SourceTraceContext
import com.engineeringood.athena.interaction.Undo
import com.engineeringood.athena.interaction.Redo
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConnectionOperationHandlerTest {
    @Test
    fun `connect commits typed engineering source and invalid direction changes no accepted authority`() {
        withRepository { repository ->
            val server = initializedServer(repository.root)
            try {
                val context = assertNotNull(server.presentationEditContextDomain())
                val scene = scene(server)
                val sourcePort = port(scene, "port:S1.limitOpen.output")
                val beforeSource = Files.readString(repository.source)
                val beforeLock = Files.readString(repository.root.resolve("athena.lock"))
                val operation = EditOperationEnvelope(
                    operationId = UUID.randomUUID().toString(),
                    sceneId = assertNotNull(context.sceneId),
                    sourceRevision = context.sourceRevision,
                    target = OperationTarget(listOf("port:H1.openLamp.inlet", "port:S1.limitOpen.output").sorted()),
                    sourceTrace = sourceTrace(scene, sourcePort),
                    requestedWritableFiles = context.engineeringWritableFiles,
                    body = ConnectPorts(
                        connectionKind = ConnectionEditKind.SIGNAL,
                        endpoints = listOf(
                            ConnectionEditEndpoint(ConnectionEditEndpointRole.SOURCE, "port:S1.limitOpen.output"),
                            ConnectionEditEndpoint(ConnectionEditEndpointRole.SINK, "port:H1.openLamp.inlet"),
                        ),
                        requirements = emptyList(),
                    ),
                )

                val accepted = assertNotNull(server.applyEditOperation(operation).get())
                assertEquals(EditOperationStatus.ACCEPTED, accepted.status, accepted.rejection.toString())
                assertTrue(Files.readString(repository.source).contains("connect signal S1.limitOpen.output to H1.openLamp.inlet"))
                assertNotEquals(beforeSource, Files.readString(repository.source))
                assertNotEquals(beforeLock, Files.readString(repository.root.resolve("athena.lock")))
                assertEquals(1, server.acceptedOperationJournal().size)

                val current = assertNotNull(server.presentationEditContextDomain())
                val currentScene = scene(server)
                val invalidSource = port(currentScene, "port:S1.limitOpen.output")
                val sourceAfterAccepted = Files.readString(repository.source)
                val lockAfterAccepted = Files.readString(repository.root.resolve("athena.lock"))
                val journalAfterAccepted = server.acceptedOperationJournal()
                val invalid = operation.copy(
                    operationId = UUID.randomUUID().toString(),
                    sourceRevision = current.sourceRevision,
                    target = OperationTarget(listOf("port:PLC1.control.output", "port:S1.limitOpen.output").sorted()),
                    sourceTrace = sourceTrace(currentScene, invalidSource),
                    body = ConnectPorts(
                        ConnectionEditKind.WIRE,
                        listOf(
                            ConnectionEditEndpoint(ConnectionEditEndpointRole.SOURCE, "port:S1.limitOpen.output"),
                            ConnectionEditEndpoint(ConnectionEditEndpointRole.SINK, "port:PLC1.control.output"),
                        ),
                        listOf(crossSection()),
                    ),
                )
                val rejected = assertNotNull(server.applyEditOperation(invalid).get())
                assertEquals(EditOperationStatus.REJECTED, rejected.status)
                assertEquals("connection.edit.direction-incompatible", rejected.rejection?.diagnostics?.single()?.code)
                assertEquals(sourceAfterAccepted, Files.readString(repository.source))
                assertEquals(lockAfterAccepted, Files.readString(repository.root.resolve("athena.lock")))
                assertEquals(journalAfterAccepted, server.acceptedOperationJournal())
            } finally {
                server.shutdown().get()
            }
        }
    }

    @Test
    fun `reconnect replaces first class Connection endpoint and rejects stale retry atomically`() {
        withRepository { repository ->
            val server = initializedServer(repository.root)
            try {
                val context = assertNotNull(server.presentationEditContextDomain())
                val scene = scene(server)
                val connection = connection(scene, "port:Q1.protection.inlet", "port:Supply.main.L1")
                val connectionId = connection.getValue("connectionId") as String
                check(connectionId.matches(Regex("^connection:[A-Za-z0-9._:/>-]+$"))) { "Scene connection identity is not canonical: $connectionId" }
                val operation = EditOperationEnvelope(
                    operationId = UUID.randomUUID().toString(),
                    sceneId = assertNotNull(context.sceneId),
                    sourceRevision = context.sourceRevision,
                    target = OperationTarget(listOf(connectionId, "port:OL1.protection.inlet").sorted()),
                    sourceTrace = sourceTrace(scene, connection),
                    requestedWritableFiles = context.engineeringWritableFiles,
                    body = ReconnectConnectionEndpoint(connectionId, ConnectionEditEndpointRole.SINK, "port:OL1.protection.inlet"),
                )
                val accepted = assertNotNull(server.applyEditOperation(operation).get())
                assertEquals(EditOperationStatus.ACCEPTED, accepted.status, accepted.rejection.toString())
                val updated = Files.readString(repository.source)
                assertTrue(updated.contains("connect conductor Supply.main.L1 to OL1.protection.inlet"))
                assertTrue(!updated.contains("connect conductor Supply.main.L1 to Q1.protection.inlet"))
                assertEquals(1, server.acceptedOperationJournal().size)

                val stale = operation.copy(operationId = UUID.randomUUID().toString())
                val rejected = assertNotNull(server.applyEditOperation(stale).get())
                assertEquals(EditOperationStatus.REJECTED, rejected.status)
                assertEquals("edit.operation.revision-stale", rejected.rejection?.diagnostics?.single()?.code)
                assertEquals(updated, Files.readString(repository.source))
                assertEquals(1, server.acceptedOperationJournal().size)
            } finally {
                server.shutdown().get()
            }
        }
    }

    @Test
    fun `route adjustment writes Sheet only and preserves Connection IR digest`() {
        withRepository { repository ->
            val server = initializedServer(repository.root)
            try {
                val context = assertNotNull(server.presentationEditContextDomain())
                val scene = scene(server)
                val connection = connection(scene, "port:Q1.protection.inlet", "port:Supply.main.L1")
                val connectionId = connection.getValue("connectionId") as String
                check(connectionId.matches(Regex("^connection:[A-Za-z0-9._:/>-]+$"))) { "Scene connection identity is not canonical: $connectionId" }
                val compilation = assertNotNull(AthenaCompiler().compile(repository.source) as? CompilerCompilationSuccess)
                val projectionId = compilation.projections.flatMap { it.connections }
                    .single { it.semanticId.value == connectionId }.projectionId.value
                val sourceBefore = Files.readString(repository.source)
                val lockBefore = Files.readString(repository.root.resolve("athena.lock"))
                val sheetBefore = Files.readString(repository.sheet)
                val readModelBefore = assertNotNull(server.connectionReadModelDomain(AthenaConnectionReadModelParams()))
                val operation = EditOperationEnvelope(
                    operationId = UUID.randomUUID().toString(),
                    sceneId = assertNotNull(context.sceneId),
                    sourceRevision = context.sourceRevision,
                    target = OperationTarget(listOf(connectionId, projectionId).sorted()),
                    sourceTrace = sourceTrace(scene, connection),
                    requestedWritableFiles = context.routeWritableFiles,
                    body = AdjustConnectionRoute(
                        sheetId = assertNotNull(context.sheetId),
                        connectionId = connectionId,
                        projectionId = projectionId,
                        target = LogicalRouteTarget(LogicalRouteTargetKind.SEGMENT, 1),
                        point = LogicalRoutePoint(11, 12),
                    ),
                )

                val accepted = assertNotNull(server.applyEditOperation(operation).get())
                assertEquals(EditOperationStatus.ACCEPTED, accepted.status, accepted.rejection.toString())
                assertEquals(sourceBefore, Files.readString(repository.source))
                assertEquals(lockBefore, Files.readString(repository.root.resolve("athena.lock")))
                assertNotEquals(sheetBefore, Files.readString(repository.sheet))
                assertTrue(Files.readString(repository.sheet).contains("route \"$projectionId\" via \"segment:1\" at (11, 12)"))
                val readModelAfter = assertNotNull(server.connectionReadModelDomain(AthenaConnectionReadModelParams()))
                assertEquals(readModelBefore.connectionIrDigest, readModelAfter.connectionIrDigest)
                assertEquals(1, server.acceptedOperationJournal().size)

                val routeEntry = assertNotNull(accepted.acceptance).journalEntryId
                val undoContext = assertNotNull(server.presentationEditContextDomain())
                val undo = EditOperationEnvelope(
                    operationId = UUID.randomUUID().toString(),
                    sceneId = assertNotNull(undoContext.sceneId),
                    sourceRevision = undoContext.sourceRevision,
                    target = OperationTarget(listOf(routeEntry)),
                    sourceTrace = sourceTrace(scene(server), connection),
                    requestedWritableFiles = undoContext.routeWritableFiles,
                    body = Undo(routeEntry),
                )
                val undone = assertNotNull(server.applyEditOperation(undo).get())
                assertEquals(EditOperationStatus.ACCEPTED, undone.status, undone.rejection.toString())
                assertEquals(sheetBefore, Files.readString(repository.sheet))

                val redoContext = assertNotNull(server.presentationEditContextDomain())
                val redo = undo.copy(
                    operationId = UUID.randomUUID().toString(),
                    sceneId = assertNotNull(redoContext.sceneId),
                    sourceRevision = redoContext.sourceRevision,
                    sourceTrace = sourceTrace(scene(server), connection),
                    requestedWritableFiles = redoContext.routeWritableFiles,
                    body = Redo(routeEntry),
                )
                val redone = assertNotNull(server.applyEditOperation(redo).get())
                assertEquals(EditOperationStatus.ACCEPTED, redone.status, redone.rejection.toString())
                assertNotEquals(sheetBefore, Files.readString(repository.sheet))
                assertEquals(3, server.acceptedOperationJournal().size)
            } finally {
                server.shutdown().get()
            }
        }
    }

    private fun initializedServer(root: Path): AthenaLanguageServer = AthenaLanguageServer().also {
        it.initialize(workspaceInitializeParams(root)).get()
    }

    private fun crossSection() = ConnectionRequirementIntent(
        ConnectionRequirementKind.CROSS_SECTION,
        ConnectionRequirementValue.Quantity("0.75", "mm2"),
    )

    private fun scene(server: AthenaLanguageServer): Map<String, Any?> =
        @Suppress("UNCHECKED_CAST")
        (assertNotNull(assertNotNull(server.diagramScene(AthenaDiagramSceneParams()).get())["scene"]) as Map<String, Any?>)

    private fun port(scene: Map<String, Any?>, portId: String): Map<String, Any?> {
        @Suppress("UNCHECKED_CAST")
        val occurrences = scene.getValue("occurrences") as List<Map<String, Any?>>
        val ports = occurrences.flatMap { occurrence ->
            @Suppress("UNCHECKED_CAST")
            (occurrence.getValue("ports") as List<Map<String, Any?>>)
        }
        return ports.singleOrNull { it["semanticPortId"] == portId }
            ?: error("Scene port '$portId' not found. Available: ${ports.map { it["semanticPortId"] }}")
    }

    private fun connection(scene: Map<String, Any?>, sourceAnchorId: String, targetAnchorId: String): Map<String, Any?> {
        @Suppress("UNCHECKED_CAST")
        val connections = scene.getValue("connections") as List<Map<String, Any?>>
        val sourceAnchor = port(scene, sourceAnchorId)["anchorId"]
        val targetAnchor = port(scene, targetAnchorId)["anchorId"]
        return connections.singleOrNull {
            it["sourceAnchorId"] == sourceAnchor && it["targetAnchorId"] == targetAnchor
        } ?: error(
            "Scene connection '$sourceAnchorId -> $targetAnchorId' not found. Available: " +
                connections.map { "${it["sourceAnchorId"]} -> ${it["targetAnchorId"]}" },
        )
    }

    private fun sourceTrace(scene: Map<String, Any?>, selected: Map<String, Any?>): SourceTraceContext {
        val traceId = selected.getValue("traceId") as String
        @Suppress("UNCHECKED_CAST")
        val traces = scene.getValue("traces") as List<Map<String, Any?>>
        val trace = traces.single { it["traceId"] == traceId }
        @Suppress("UNCHECKED_CAST")
        val origins = trace.getValue("origins") as List<Map<String, Any?>>
        val subject = origins.first { it["primary"] == true }.getValue("subjectId") as String
        return SourceTraceContext(traceId, subject)
    }

    private fun withRepository(block: (ConnectionTestRepository) -> Unit) {
        val root = createTempDirectory("athena-m46-connection-edit-")
        val sourceRepository = repositoryRoot().resolve("examples/m46/rolling-shutter")
        try {
            Files.walk(sourceRepository).use { paths ->
                paths.forEach { source ->
                    val target = root.resolve(sourceRepository.relativize(source).toString())
                    if (Files.isDirectory(source)) Files.createDirectories(target) else Files.copy(source, target)
                }
            }
            val source = root.resolve("src/com/engineeringood/m46/rollingshutter/rolling-shutter.athena")
            val sheet = root.resolve("src/com/engineeringood/m46/rollingshutter/rolling-shutter.sheet.athena")
            val lock = AthenaCompiler().materializeRepositoryLock(root)
            check(lock.isValid) { lock.diagnostics.joinToString { it.message } }
            block(ConnectionTestRepository(root, source, sheet))
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    private fun repositoryRoot(): Path {
        var current = Path.of("").toAbsolutePath().normalize()
        while (current.parent != null) {
            if (Files.exists(current.resolve("settings.gradle.kts"))) return current
            current = current.parent
        }
        error("Could not locate Athena repository root.")
    }
}

private data class ConnectionTestRepository(val root: Path, val source: Path, val sheet: Path)
