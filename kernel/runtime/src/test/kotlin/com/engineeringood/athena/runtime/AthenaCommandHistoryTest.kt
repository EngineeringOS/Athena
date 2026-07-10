package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaCommandHistoryTest {
    @Test
    fun `records successful commands with stable identifiers and deterministic serialization`() {
        val sourcePath = writeProject(twoConnectionFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "history",
                sourcePath = sourcePath,
            )

            executeConnect(context, "port:PLC1.out1", "port:M1.in")
            executeConnect(context, "port:PLC1.out2", "port:M2.in")

            val history = context.commandRuntime().history(context)

            assertEquals(2, history.records.size)
            assertEquals(2, history.appliedRecordCount)
            assertEquals(listOf("command-0001", "command-0002"), history.records.map { it.commandId })
            assertTrue(history.records.all { it.mutationCategory == AthenaMutationCategory.SEMANTIC_MUTATION })
            assertEquals(
                listOf(AthenaCommandHistoryRecordStatus.APPLIED, AthenaCommandHistoryRecordStatus.APPLIED),
                history.records.map { it.status },
            )
            assertEquals(
                listOf(
                    listOf("connection:PLC1.out1->M1.in", "port:M1.in", "port:PLC1.out1"),
                    listOf("connection:PLC1.out2->M2.in", "port:M2.in", "port:PLC1.out2"),
                ),
                history.records.map { it.changedSemanticIds.sorted() },
            )

            val serialized = context.commandRuntime().serializeHistory(context)

            assertContains(serialized, "\"commandId\":\"command-0001\"")
            assertContains(serialized, "\"commandId\":\"command-0002\"")
            assertContains(serialized, "\"mutationCategory\":\"SEMANTIC_MUTATION\"")
            assertContains(serialized, "\"commandKind\":\"CONNECT_PORTS\"")
            assertContains(serialized, "\"commandOrigin\":\"STANDARD\"")
            assertContains(serialized, "\"sourcePortSemanticId\":\"port:PLC1.out1\"")
            assertContains(serialized, "\"targetPortSemanticId\":\"port:M2.in\"")
            assertEquals(serialized, context.commandRuntime().serializeHistory(context))
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `undo and redo restore canonical runtime state through recorded command history`() {
        val sourcePath = writeProject(twoConnectionFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "history",
                sourcePath = sourcePath,
            )

            executeConnect(context, "port:PLC1.out1", "port:M1.in")
            executeConnect(context, "port:PLC1.out2", "port:M2.in")

            val undo = context.commandRuntime().undo(context)

            val undoSuccess = assertIs<AthenaCommandHistoryMutationSuccess>(undo)
            assertEquals(AthenaMutationCategory.SEMANTIC_MUTATION, undoSuccess.mutationCategory)
            assertEquals(AthenaCommandHistoryOperation.UNDO, undoSuccess.operation)
            assertEquals(AthenaMutationOutcome.ACCEPTED, undoSuccess.outcome)
            assertEquals(listOf("command-0002"), undoSuccess.affectedCommandIds)
            assertEquals(2, undoSuccess.beforeDocument.connections.size)
            assertEquals(1, undoSuccess.afterDocument.connections.size)
            assertEquals(1, assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document.connections.size)
            assertEquals(1, assertIs<AthenaRuntimeViewerReadyProjection>(context.projectViewerProjection()).scene.connections.size)

            val historyAfterUndo = context.commandRuntime().history(context)
            assertEquals(1, historyAfterUndo.appliedRecordCount)
            assertEquals(
                listOf(AthenaCommandHistoryRecordStatus.APPLIED, AthenaCommandHistoryRecordStatus.UNDONE),
                historyAfterUndo.records.map { it.status },
            )

            val redo = context.commandRuntime().redo(context)

            val redoSuccess = assertIs<AthenaCommandHistoryMutationSuccess>(redo)
            assertEquals(AthenaMutationCategory.SEMANTIC_MUTATION, redoSuccess.mutationCategory)
            assertEquals(AthenaCommandHistoryOperation.REDO, redoSuccess.operation)
            assertEquals(AthenaMutationOutcome.ACCEPTED, redoSuccess.outcome)
            assertEquals(listOf("command-0002"), redoSuccess.affectedCommandIds)
            assertEquals(1, redoSuccess.beforeDocument.connections.size)
            assertEquals(2, redoSuccess.afterDocument.connections.size)
            assertEquals(2, assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document.connections.size)
            assertEquals(2, assertIs<AthenaRuntimeViewerReadyProjection>(context.projectViewerProjection()).scene.connections.size)
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `replay reapplies the full recorded command sequence after undo without caller reconstruction`() {
        val sourcePath = writeProject(twoConnectionFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "history",
                sourcePath = sourcePath,
            )

            executeConnect(context, "port:PLC1.out1", "port:M1.in")
            executeConnect(context, "port:PLC1.out2", "port:M2.in")

            assertIs<AthenaCommandHistoryMutationSuccess>(context.commandRuntime().undo(context))
            assertIs<AthenaCommandHistoryMutationSuccess>(context.commandRuntime().undo(context))
            assertTrue(assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document.connections.isEmpty())

            val replay = context.commandRuntime().replay(context)

            val replaySuccess = assertIs<AthenaCommandHistoryMutationSuccess>(replay)
            assertEquals(AthenaMutationCategory.SEMANTIC_MUTATION, replaySuccess.mutationCategory)
            assertEquals(AthenaCommandHistoryOperation.REPLAY, replaySuccess.operation)
            assertEquals(AthenaMutationOutcome.ACCEPTED, replaySuccess.outcome)
            assertEquals(listOf("command-0001", "command-0002"), replaySuccess.affectedCommandIds)
            assertEquals(0, replaySuccess.beforeDocument.connections.size)
            assertEquals(2, replaySuccess.afterDocument.connections.size)
            assertEquals(2, assertIs<CompilerCompilationSuccess>(context.compileActiveProject()).document.connections.size)

            val historyAfterReplay = context.commandRuntime().history(context)
            assertEquals(2, historyAfterReplay.appliedRecordCount)
            assertTrue(historyAfterReplay.records.all { it.status == AthenaCommandHistoryRecordStatus.APPLIED })
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `history operations reject requests that have no applicable recorded command`() {
        val sourcePath = writeProject(twoConnectionFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "history",
                sourcePath = sourcePath,
            )

            val undo = context.commandRuntime().undo(context)
            val redo = context.commandRuntime().redo(context)

            val undoRejected = assertIs<AthenaCommandHistoryMutationRejected>(undo)
            val redoRejected = assertIs<AthenaCommandHistoryMutationRejected>(redo)
            assertEquals(AthenaMutationCategory.SEMANTIC_MUTATION, undoRejected.mutationCategory)
            assertEquals(AthenaMutationCategory.SEMANTIC_MUTATION, redoRejected.mutationCategory)
            assertEquals(AthenaMutationOutcome.REJECTED, undoRejected.outcome)
            assertEquals(AthenaMutationOutcome.REJECTED, redoRejected.outcome)
            assertContains(undoRejected.reason, "No applied command")
            assertContains(redoRejected.reason, "No undone command")
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun executeConnect(
        context: AthenaExecutionContext,
        sourcePortSemanticId: String,
        targetPortSemanticId: String,
    ) {
        val result = context.commandRuntime().execute(
            context = context,
            command = AthenaConnectPortsCommand(
                sourcePortSemanticId = sourcePortSemanticId,
                targetPortSemanticId = targetPortSemanticId,
            ),
        )

        assertIs<AthenaCommandExecutionSuccess>(result)
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-command-history-", ".athena")
        Files.writeString(path, source)
        return path
    }

    private fun twoConnectionFixture(): String {
        return """
            system HistoryDemo {
              device PLC1 {
                type Switch
              }

              device M1 {
                type Motor
              }

              device M2 {
                type Motor
              }

              port PLC1.out1 {
                direction out
                signal Digital
              }

              port PLC1.out2 {
                direction out
                signal Digital
              }

              port M1.in {
                direction in
                signal Digital
              }

              port M2.in {
                direction in
                signal Digital
              }
            }
        """.trimIndent()
    }
}
