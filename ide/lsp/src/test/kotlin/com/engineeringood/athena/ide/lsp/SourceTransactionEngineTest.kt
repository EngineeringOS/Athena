package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.interaction.EditOperationEnvelope
import com.engineeringood.athena.interaction.EditOperationStatus
import com.engineeringood.athena.interaction.OperationRejectionReason
import com.engineeringood.athena.interaction.OperationTarget
import com.engineeringood.athena.interaction.RevisionDigest
import com.engineeringood.athena.interaction.SetStyle
import com.engineeringood.athena.interaction.SourceFilePatch
import com.engineeringood.athena.interaction.SourcePatchSet
import com.engineeringood.athena.interaction.SourceRevision
import com.engineeringood.athena.interaction.SourceTraceContext
import com.engineeringood.athena.interaction.StyleFields
import com.engineeringood.athena.interaction.StyleTarget
import com.engineeringood.athena.interaction.StyleTargetKind
import com.engineeringood.athena.presentation.InputRevision
import com.engineeringood.athena.presentation.SceneId
import kotlin.test.Test
import kotlin.test.assertEquals

class SourceTransactionEngineTest {
    @Test
    fun `publication failure restores exact inverse and appends no journal entry`() {
        val previous = revision('1', RevisionDigest.absent())
        val staged = revision('2', RevisionDigest.present("2".repeat(64)))
        val workspace = MemoryWorkspace("style-before")
        val journal = SessionOperationJournal()
        val engine = SourceTransactionEngine(
            workspace,
            { if (workspace.content == "style-after") staged else previous },
            { error("publication failed") },
            journal,
        )

        val result = engine.execute(
            operation(previous),
            SourcePatchSet(listOf(SourceFilePatch(STYLE_PATH, "style-before", "style-after"))),
            staged,
        ) {}

        assertEquals(EditOperationStatus.REJECTED, result.status)
        assertEquals(OperationRejectionReason.IO_FAILURE, result.rejection?.reason)
        assertEquals("style-before", workspace.content)
        assertEquals(2, workspace.publishCount)
        assertEquals(emptyList(), journal.entries())
    }

    @Test
    fun `staged failure and stale patch never publish or append`() {
        val previous = revision('1', RevisionDigest.absent())
        val staged = revision('2', RevisionDigest.present("2".repeat(64)))
        val workspace = MemoryWorkspace("changed-outside-transaction")
        val journal = SessionOperationJournal()
        val engine = SourceTransactionEngine(workspace, { previous }, { error("not reached") }, journal)
        val operation = operation(previous)
        val patch = SourcePatchSet(listOf(SourceFilePatch(STYLE_PATH, "style-before", "style-after")))

        val stale = engine.execute(operation, patch, staged) {}
        assertEquals(OperationRejectionReason.STALE, stale.rejection?.reason)

        workspace.content = "style-before"
        val failed = engine.execute(operation, patch, staged) {
            throw StagedOperationFailure(
                OperationRejectionReason.COMPILATION_FAILURE,
                diagnostic("route", "Invalid staged style.", "Correct style.", "style.invalid"),
            )
        }
        assertEquals(OperationRejectionReason.COMPILATION_FAILURE, failed.rejection?.reason)
        assertEquals(0, workspace.publishCount)
        assertEquals(emptyList(), journal.entries())
    }

    private fun operation(revision: SourceRevision) = EditOperationEnvelope(
        operationId = "00000000-0000-4000-8000-000000000044",
        sceneId = SceneId("scene:sha256:${"4".repeat(64)}"),
        sourceRevision = revision,
        target = OperationTarget(listOf("connection")),
        sourceTrace = SourceTraceContext("trace:sha256:${"5".repeat(64)}", "connection-subject"),
        requestedWritableFiles = listOf(STYLE_PATH),
        body = SetStyle("sheet-main", StyleTarget(StyleTargetKind.ROLE, "connection"), StyleFields(strokeWidth = 2)),
    )

    private fun revision(marker: Char, styleDigest: RevisionDigest) = SourceRevision(
        sceneInputRevision = InputRevision("input:sha256:${marker.toString().repeat(64)}"),
        sourceRootIdentity = "source-root:sha256:${"3".repeat(64)}",
        engineeringSourceDigest = "4".repeat(64),
        sheetDigest = "5".repeat(64),
        styleDigest = styleDigest,
        lockDigest = RevisionDigest.present("6".repeat(64)),
        packageItemDigests = emptyList(),
        compilerVersion = "compiler-1",
        sceneSchemaVersion = "scene-1",
        profileVersion = "profile-1",
    )

    private class MemoryWorkspace(var content: String?) : TransactionWorkspace {
        var publishCount = 0

        override fun read(relativePath: String): String? = content

        override fun publish(patchSet: SourcePatchSet) {
            publishCount += 1
            val patch = patchSet.files.single()
            check(content == patch.beforeUtf8)
            content = patch.afterUtf8
        }
    }

    private companion object {
        const val STYLE_PATH = "src/project.sheet.style.athena"
    }
}
