package com.engineeringood.athena.interaction

import com.engineeringood.athena.presentation.InputRevision
import com.engineeringood.athena.presentation.SceneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EditOperationContractTest {
    @Test
    fun `operation kinds own fixed authority classes`() {
        assertEquals(
            mapOf(
                EditOperationKind.MOVE_OCCURRENCE to EditAuthorityClass.PRESENTATION,
                EditOperationKind.ALIGN_OCCURRENCES to EditAuthorityClass.PRESENTATION,
                EditOperationKind.DISTRIBUTE_OCCURRENCES to EditAuthorityClass.PRESENTATION,
                EditOperationKind.SNAP_OCCURRENCE_TO_GRID to EditAuthorityClass.PRESENTATION,
                EditOperationKind.SET_STYLE to EditAuthorityClass.PRESENTATION,
                EditOperationKind.CHANGE_SYMBOL to EditAuthorityClass.REPRESENTATION,
                EditOperationKind.CONNECT_PORTS to EditAuthorityClass.ENGINEERING,
                EditOperationKind.RECONNECT_CONNECTION_ENDPOINT to EditAuthorityClass.ENGINEERING,
                EditOperationKind.ADJUST_CONNECTION_ROUTE to EditAuthorityClass.PRESENTATION,
                EditOperationKind.BIND_PART to EditAuthorityClass.ENGINEERING,
                EditOperationKind.ADD_PACKAGE_DEPENDENCY to EditAuthorityClass.ENGINEERING,
                EditOperationKind.INSERT_ELEMENT_OCCURRENCE to EditAuthorityClass.REPRESENTATION,
                EditOperationKind.INSERT_MACRO_OCCURRENCES to EditAuthorityClass.REPRESENTATION,
                EditOperationKind.UNDO to EditAuthorityClass.JOURNAL,
                EditOperationKind.REDO to EditAuthorityClass.JOURNAL,
            ),
            EditOperationKind.entries.associateWith { it.authorityClass },
        )
    }

    @Test
    fun `envelope requires stable trace full revision and normalized writable files`() {
        val operation = setStyleOperation()
        assertEquals(EditAuthorityClass.PRESENTATION, operation.authorityClass)
        assertEquals(listOf("src/project.sheet.style.athena"), operation.requestedWritableFiles)

        assertFailsWith<IllegalArgumentException> { operation.copy(requestedWritableFiles = listOf("../escape.athena")) }
        assertFailsWith<IllegalArgumentException> { operation.copy(requestedWritableFiles = listOf("C:/escape.athena")) }
        assertFailsWith<IllegalArgumentException> { operation.copy(requestedWritableFiles = listOf("b.athena", "a.athena")) }
        assertFailsWith<IllegalArgumentException> { operation.copy(requestedWritableFiles = listOf("a.athena", "a.athena")) }
        assertFailsWith<IllegalArgumentException> { operation.copy(sourceTrace = operation.sourceTrace.copy(traceId = "bad")) }
    }

    @Test
    fun `structured revision changes when any governed component changes`() {
        val revision = revision()
        val variants = listOf(
            revision.copy(sceneInputRevision = InputRevision("input:sha256:${"9".repeat(64)}")),
            revision.copy(sourceRootIdentity = "source-root:sha256:${"9".repeat(64)}"),
            revision.copy(engineeringSourceDigest = "9".repeat(64)),
            revision.copy(sheetDigest = "9".repeat(64)),
            revision.copy(styleDigest = RevisionDigest.present("9".repeat(64))),
            revision.copy(lockDigest = RevisionDigest.absent()),
            revision.copy(packageItemDigests = listOf(PackageItemDigest("pkg", "item", "9".repeat(64)))),
            revision.copy(compilerVersion = "compiler-2"),
            revision.copy(sceneSchemaVersion = "scene-2"),
            revision.copy(profileVersion = "profile-2"),
        )
        assertTrue(variants.all { it != revision })
        assertEquals(variants.size, variants.map { it.token }.distinct().size)
    }

    @Test
    fun `patch set has exact inverse and journal captures accepted transaction`() {
        val forward = SourcePatchSet(
            listOf(SourceFilePatch("src/project.sheet.style.athena", beforeUtf8 = null, afterUtf8 = "style \"connection\" {}\n")),
        )
        val inverse = forward.inverse()
        assertEquals(forward, inverse.inverse())

        val operation = setStyleOperation()
        val result = revision().copy(styleDigest = RevisionDigest.present("8".repeat(64)))
        val entry = OperationJournalEntry(
            journalEntryId = "journal:sha256:${"7".repeat(64)}",
            sequence = 1,
            operationId = operation.operationId,
            operationKind = operation.kind,
            authorityClass = operation.authorityClass,
            target = operation.target,
            sourceTrace = operation.sourceTrace,
            previousSourceRevision = operation.sourceRevision,
            resultingSourceRevision = result,
            writableFiles = operation.requestedWritableFiles,
            forwardPatchSet = forward,
            inversePatchSet = inverse,
            stagedCompileResult = StagedCompileResult.ACCEPTED,
            publicationCorrelationId = "publication:00000000-0000-4000-8000-000000000002",
        )
        assertEquals(forward, entry.inversePatchSet.inverse())
        assertEquals(EditAuthorityClass.PRESENTATION, entry.authorityClass)
    }

    private fun setStyleOperation() = EditOperationEnvelope(
        operationId = "00000000-0000-4000-8000-000000000001",
        sceneId = SceneId("scene:sha256:${"1".repeat(64)}"),
        sourceRevision = revision(),
        target = OperationTarget(listOf("role:connection", "sheet-main")),
        sourceTrace = SourceTraceContext("trace:sha256:${"4".repeat(64)}", "sheet-main"),
        requestedWritableFiles = listOf("src/project.sheet.style.athena"),
        body = SetStyle(
            sheetId = "sheet-main",
            target = StyleTarget(StyleTargetKind.ROLE, "connection"),
            fields = StyleFields(strokeRgba = "#225588ff", strokeWidth = 3, routeMarker = RouteMarker.END_ARROW),
        ),
    )

    private fun revision() = SourceRevision(
        sceneInputRevision = InputRevision("input:sha256:${"2".repeat(64)}"),
        sourceRootIdentity = "source-root:sha256:${"3".repeat(64)}",
        engineeringSourceDigest = "4".repeat(64),
        sheetDigest = "5".repeat(64),
        styleDigest = RevisionDigest.absent(),
        lockDigest = RevisionDigest.present("6".repeat(64)),
        packageItemDigests = listOf(PackageItemDigest("pkg", "item", "7".repeat(64))),
        compilerVersion = "compiler-1",
        sceneSchemaVersion = "scene-1",
        profileVersion = "profile-1",
    )
}
