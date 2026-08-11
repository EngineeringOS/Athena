package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.interaction.EditOperationEnvelope
import com.engineeringood.athena.interaction.EditOperationStatus
import com.engineeringood.athena.interaction.LockAction
import com.engineeringood.athena.interaction.MoveOccurrence
import com.engineeringood.athena.interaction.OperationTarget
import com.engineeringood.athena.interaction.Redo
import com.engineeringood.athena.interaction.SheetPoint
import com.engineeringood.athena.interaction.SourceTraceContext
import com.engineeringood.athena.interaction.Undo
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class JournalUndoRedoTest {
    @Test
    fun `accepted move can undo and redo as source transactions while stale redo fails closed`() {
        val repository = copyRepository()
        val sheet = repository.resolve("src/com/engineeringood/m45/rollingshutter/rolling-shutter.sheet.athena")
        val server = AthenaLanguageServer()
        try {
            server.initialize(workspaceInitializeParams(repository)).get()
            val original = Files.readString(sheet)
            val scene = scene(server)
            val occurrence = occurrence(scene, "Supply.main")
            val context = assertNotNull(server.presentationEditContextDomain())
            val writable = relative(repository, sheet)
            val move = EditOperationEnvelope(
                operationId = UUID.randomUUID().toString(),
                sceneId = assertNotNull(context.sceneId),
                sourceRevision = context.sourceRevision,
                target = OperationTarget(listOf(occurrence["occurrenceId"] as String)),
                sourceTrace = SourceTraceContext(occurrence["traceId"] as String, occurrence["subjectId"] as String),
                requestedWritableFiles = listOf(writable),
                body = MoveOccurrence(assertNotNull(context.sheetId), occurrence["occurrenceId"] as String, SheetPoint(12, 12), LockAction.LOCK),
            )
            val moved = assertNotNull(server.applyEditOperation(move).get())
            assertEquals(EditOperationStatus.ACCEPTED, moved.status, moved.rejection.toString())
            val movedSource = Files.readString(sheet)
            assertNotEquals(original, movedSource)
            val moveEntry = assertNotNull(moved.acceptance).journalEntryId

            val undoContext = assertNotNull(server.presentationEditContextDomain())
            val undo = journalOperation(undoContext, occurrence, writable, Undo(moveEntry))
            val undone = assertNotNull(server.applyEditOperation(undo).get())
            assertEquals(EditOperationStatus.ACCEPTED, undone.status, undone.rejection.toString())
            assertEquals(original, Files.readString(sheet))

            val redoContext = assertNotNull(server.presentationEditContextDomain())
            val redo = journalOperation(redoContext, occurrence, writable, Redo(moveEntry))
            val redone = assertNotNull(server.applyEditOperation(redo).get())
            assertEquals(EditOperationStatus.ACCEPTED, redone.status, redone.rejection.toString())
            assertEquals(movedSource, Files.readString(sheet))
            assertEquals(3, server.acceptedOperationJournal().size)

            val staleRedo = journalOperation(redoContext, occurrence, writable, Redo(moveEntry))
            val rejected = assertNotNull(server.applyEditOperation(staleRedo).get())
            assertEquals(EditOperationStatus.REJECTED, rejected.status)
            assertEquals("edit.operation.revision-stale", rejected.rejection?.diagnostics?.single()?.code)
            assertEquals(movedSource, Files.readString(sheet))
            assertEquals(3, server.acceptedOperationJournal().size)
        } finally {
            server.shutdown().get()
            repository.toFile().deleteRecursively()
        }
    }

    private fun journalOperation(
        context: AthenaPresentationEditContextPayload,
        occurrence: Map<*, *>,
        writable: String,
        body: com.engineeringood.athena.interaction.EditOperationBody,
    ) = EditOperationEnvelope(
        operationId = UUID.randomUUID().toString(),
        sceneId = assertNotNull(context.sceneId),
        sourceRevision = context.sourceRevision,
        target = OperationTarget(listOf((body as? Undo)?.journalEntryId ?: (body as Redo).journalEntryId)),
        sourceTrace = SourceTraceContext(occurrence["traceId"] as String, occurrence["subjectId"] as String),
        requestedWritableFiles = listOf(writable),
        body = body,
    )

    private fun scene(server: AthenaLanguageServer): Map<*, *> =
        assertNotNull(assertNotNull(server.diagramScene(AthenaDiagramSceneParams()).get())["scene"] as? Map<*, *>)

    private fun occurrence(scene: Map<*, *>, name: String): Map<*, *> =
        (scene["occurrences"] as List<*>).map { it as Map<*, *> }
            .single { (it["occurrenceId"] as String).endsWith("/occurrence/$name") }

    private fun relative(root: Path, path: Path): String = root.relativize(path).toString().replace('\\', '/')

    private fun copyRepository(): Path {
        val source = repositoryRoot().resolve("examples/m45/rolling-shutter")
        val target = createTempDirectory("athena-m45-journal-")
        Files.walk(source).use { paths ->
            paths.forEach { path ->
                val destination = target.resolve(source.relativize(path).toString())
                if (Files.isDirectory(path)) Files.createDirectories(destination) else Files.copy(path, destination)
            }
        }
        AthenaCompiler().materializeRepositoryLock(target)
        return target
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
