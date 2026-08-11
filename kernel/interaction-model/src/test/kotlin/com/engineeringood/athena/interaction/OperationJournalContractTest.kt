package com.engineeringood.athena.interaction

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OperationJournalContractTest {
    @Test
    fun `journal appends accepted entries once in monotonic order`() {
        val journal = InMemoryOperationJournal()
        val first = journal.append(entry(1, "00000000-0000-4000-8000-000000000001"))
        assertEquals(first, journal.append(first))
        assertEquals(1, journal.entries().size)
        assertFailsWith<IllegalArgumentException> {
            journal.append(entry(1, "00000000-0000-4000-8000-000000000002"))
        }
    }

    private fun entry(sequence: Long, operationId: String): OperationJournalEntry {
        val operation = EditOperationTestFixtures.setStyle(operationId)
        val patch = SourcePatchSet(listOf(SourceFilePatch("src/project.sheet.style.athena", null, "style \"connection\" {}\n")))
        return OperationJournalEntry(
            journalEntryId = "journal:sha256:${operationId.filter(Char::isDigit).padEnd(64, '0').take(64)}",
            sequence = sequence,
            operationId = operationId,
            operationKind = operation.kind,
            authorityClass = operation.authorityClass,
            target = operation.target,
            sourceTrace = operation.sourceTrace,
            previousSourceRevision = operation.sourceRevision,
            resultingSourceRevision = operation.sourceRevision,
            writableFiles = operation.requestedWritableFiles,
            forwardPatchSet = patch,
            inversePatchSet = patch.inverse(),
            stagedCompileResult = StagedCompileResult.ACCEPTED,
            publicationCorrelationId = "publication:$operationId",
        )
    }
}

private object EditOperationTestFixtures {
    fun setStyle(operationId: String): EditOperationEnvelope = EditOperationEnvelope(
        operationId = operationId,
        sceneId = com.engineeringood.athena.presentation.SceneId("scene:sha256:${"1".repeat(64)}"),
        sourceRevision = SourceRevision(
            com.engineeringood.athena.presentation.InputRevision("input:sha256:${"2".repeat(64)}"),
            "source-root:sha256:${"3".repeat(64)}",
            "4".repeat(64),
            "5".repeat(64),
            RevisionDigest.absent(),
            RevisionDigest.present("6".repeat(64)),
            emptyList(),
            "compiler-1",
            "scene-1",
            "profile-1",
        ),
        target = OperationTarget(listOf("role:connection")),
        sourceTrace = SourceTraceContext("trace:sha256:${"7".repeat(64)}", "sheet-main"),
        requestedWritableFiles = listOf("src/project.sheet.style.athena"),
        body = SetStyle("sheet-main", StyleTarget(StyleTargetKind.ROLE, "connection"), StyleFields(strokeWidth = 2)),
    )
}
