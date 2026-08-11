package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.interaction.AlignOccurrences
import com.engineeringood.athena.interaction.AdjustConnectionRoute
import com.engineeringood.athena.interaction.BindPart
import com.engineeringood.athena.interaction.AddPackageDependency
import com.engineeringood.athena.interaction.ChangeSymbol
import com.engineeringood.athena.interaction.ConnectPorts
import com.engineeringood.athena.interaction.DistributeOccurrences
import com.engineeringood.athena.interaction.EditOperationBody
import com.engineeringood.athena.interaction.EditOperationEnvelope
import com.engineeringood.athena.interaction.EditOperationResult
import com.engineeringood.athena.interaction.MoveOccurrence
import com.engineeringood.athena.interaction.OperationRejectionReason
import com.engineeringood.athena.interaction.ReconnectConnectionEndpoint
import com.engineeringood.athena.interaction.Redo
import com.engineeringood.athena.interaction.SetStyle
import com.engineeringood.athena.interaction.InsertElementOccurrence
import com.engineeringood.athena.interaction.InsertMacroOccurrences
import com.engineeringood.athena.interaction.SnapOccurrenceToGrid
import com.engineeringood.athena.interaction.Undo
import com.engineeringood.athena.language.SheetCompanionFound
import com.engineeringood.athena.language.AthenaSheetCompanionParser
import com.engineeringood.athena.language.SheetCompanionParseSuccess
import com.engineeringood.athena.compiler.AthenaDiagramSceneCompiler
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.language.SheetStyleCompanionLocator
import com.engineeringood.athena.presentation.PublicationState
import java.nio.file.Path

/** Validates typed edit intent and routes supported operations through source transactions. */
class EditOperationService(
    private val host: AthenaLspSessionHostReady,
    private val publicationService: AthenaDiagramPublicationService,
) {
    private val revisionService = SourceRevisionService(host)
    private val journal = SessionOperationJournal()
    private val transactionEngine = SourceTransactionEngine(host, publicationService, revisionService, journal)
    private val setStyleHandler = SetStyleOperationHandler(host, publicationService, revisionService, transactionEngine)
    private val placementHandler = PlacementOperationHandler(host, publicationService, revisionService, transactionEngine)
    private val representationAndEngineeringHandler = RepresentationAndEngineeringOperationHandler(host, publicationService, revisionService, transactionEngine)
    private val connectionHandler = ConnectionOperationHandler(host, publicationService, revisionService, transactionEngine)

    @Synchronized
    fun apply(operation: EditOperationEnvelope): EditOperationResult {
        val currentRevision = revisionService.current()
        when (val replay = journal.replay(operation)) {
            is ReplayLookup.Exact -> return replay.result
            ReplayLookup.Conflict -> return rejected(
                operation,
                currentRevision,
                OperationRejectionReason.CONFLICT,
                diagnostic(
                    operation.operationId,
                    "Operation ID was already accepted with different content.",
                    "Submit changed intent with a new operation UUID.",
                    "edit.operation.duplicate-conflict",
                ),
            )
            ReplayLookup.Missing -> Unit
        }
        if (operation.sourceRevision != currentRevision) {
            return rejected(
                operation,
                currentRevision,
                OperationRejectionReason.STALE,
                diagnostic(
                    operation.kind.name,
                    "Source Revision no longer matches governed repository inputs.",
                    "Refresh current engineering source and retry the edit.",
                    "edit.operation.revision-stale",
                ),
            )
        }
        val publication = publicationService.current()
        if (publication.state != PublicationState.READY || publication.scene?.sceneId != operation.sceneId) {
            return rejected(
                operation,
                currentRevision,
                OperationRejectionReason.STALE,
                diagnostic(
                    operation.kind.name,
                    "Canonical Scene no longer matches this edit operation.",
                    "Refresh the engineering document and retry.",
                    "edit.operation.scene-stale",
                ),
            )
        }
        val expectedTargets = targetIdentities(operation.body)
        if (operation.target.identities != expectedTargets) {
            return rejected(
                operation,
                currentRevision,
                OperationRejectionReason.INVALID,
                diagnostic(
                    operation.kind.name,
                    "Operation target identities do not match typed intent.",
                    "Submit sorted target identities derived from selected engineering objects.",
                    "edit.operation.target-invalid",
                ),
            )
        }
        val writableFiles = deriveWritableFiles(operation.body) ?: when (val body = operation.body) {
            is Undo -> journal.find(body.journalEntryId)?.writableFiles
            is Redo -> journal.find(body.journalEntryId)?.writableFiles
            else -> null
        }
        if (writableFiles == null || operation.requestedWritableFiles != writableFiles) {
            return rejected(
                operation,
                currentRevision,
                OperationRejectionReason.INVALID,
                diagnostic(
                    operation.kind.name,
                    "Requested writable files do not match server-owned operation scope.",
                    "Refresh operation context and submit only server-advertised writable files.",
                    "edit.operation.writable-files-invalid",
                ),
            )
        }
        return when (val body = operation.body) {
            is SetStyle -> setStyleHandler.apply(operation, body)
            is MoveOccurrence,
            is AlignOccurrences,
            is DistributeOccurrences,
            is SnapOccurrenceToGrid,
                -> placementHandler.apply(operation, body)
            is ChangeSymbol,
            is InsertElementOccurrence,
            is InsertMacroOccurrences,
            is BindPart,
            is AddPackageDependency,
                -> representationAndEngineeringHandler.apply(operation, body)
            is ConnectPorts,
            is ReconnectConnectionEndpoint,
            is AdjustConnectionRoute,
                -> connectionHandler.apply(operation, body)
            is Undo -> applyJournal(operation, body.journalEntryId, inverse = true)
            is Redo -> applyJournal(operation, body.journalEntryId, inverse = false)
        }
    }

    fun journalEntries() = journal.entries()

    private fun applyJournal(operation: EditOperationEnvelope, entryId: String, inverse: Boolean): EditOperationResult {
        val entry = journal.find(entryId) ?: return rejected(operation, revisionService.current(), OperationRejectionReason.INVALID,
            diagnostic(entryId, "Operation Journal entry does not exist.", "Select an accepted journal entry and retry.", "edit.journal.entry-missing"))
        val current = revisionService.current()
        if ((!inverse && current != entry.previousSourceRevision) || (inverse && current != entry.resultingSourceRevision)) {
            return rejected(operation, revisionService.current(), OperationRejectionReason.STALE,
                diagnostic(entryId, "Journal operation revision no longer matches current source.", "Refresh current source before Undo or Redo.", "edit.journal.revision-stale"))
        }
        val patchSet = if (inverse) entry.inversePatchSet else entry.forwardPatchSet
        val stagedRevision = revisionService.current()
        return transactionEngine.execute(operation, patchSet, stagedRevision) {
            validateJournalPatch(patchSet, stagedRevision)
        }
    }

    private fun validateJournalPatch(patchSet: com.engineeringood.athena.interaction.SourcePatchSet, revision: com.engineeringood.athena.interaction.SourceRevision) {
        val sourcePatch = patchSet.files.singleOrNull { it.relativePath == relative(host.sourcePath) }
        val sourceAfter = sourcePatch?.afterUtf8
        val stagedCompilation = if (sourceAfter != null) {
            host.executionContext.compiler().compile(host.sourcePath, sourceAfter)
        } else host.executionContext.compiler().compile(host.sourcePath)
        val compilation = stagedCompilation as? CompilerCompilationSuccess
            ?: throw StagedOperationFailure(
                OperationRejectionReason.COMPILATION_FAILURE,
                diagnostic(host.sourcePath.fileName.toString(), "Undo or Redo leaves engineering source invalid.", "Restore a transaction that compiles and validates.", "edit.journal.source-invalid"),
            )
        val sheetPatch = patchSet.files.singleOrNull { it.relativePath.endsWith(".sheet.athena") }
        val sheetSource = sheetPatch?.afterUtf8?.let { text ->
            AthenaSheetCompanionParser().parse(sheetPatch.relativePath, text) as? SheetCompanionParseSuccess
                ?: throw StagedOperationFailure(
                    OperationRejectionReason.COMPILATION_FAILURE,
                    diagnostic(sheetPatch.relativePath, "Undo or Redo leaves Sheet Companion invalid.", "Restore a transaction with valid Sheet placement source.", "edit.journal.sheet-invalid"),
                )
        }
        val sceneCompilation = if (sheetSource != null) {
            val withSheet = host.executionContext.compiler().compile(host.sourcePath, sheetSource.source)
            (withSheet as? CompilerCompilationSuccess)?.let { compiled ->
                AthenaDiagramSceneCompiler().compile(compiled, revision.sceneInputRevision, styleCompanion = host.styleCompanionSource())
            }
        } else {
            AthenaDiagramSceneCompiler().compile(compilation, revision.sceneInputRevision, styleCompanion = host.styleCompanionSource())
        }
        val scene = sceneCompilation?.scene
        if (scene == null) {
            throw StagedOperationFailure(
                OperationRejectionReason.COMPILATION_FAILURE,
                diagnostic("Canonical Scene", "Undo or Redo does not produce a complete scene.", "Restore a transaction compatible with current engineering source.", "edit.journal.scene-invalid"),
            )
        }
    }

    private fun deriveWritableFiles(body: EditOperationBody): List<String>? = when (body) {
        is MoveOccurrence, is AlignOccurrences, is DistributeOccurrences, is SnapOccurrenceToGrid ->
            (host.sheetCompanionLocation() as? SheetCompanionFound)?.let { listOf(relative(it.path)) }
        is SetStyle -> (host.sheetCompanionLocation() as? SheetCompanionFound)?.let { sheet ->
            listOf(relative(SheetStyleCompanionLocator.locate(sheet.path).expectedPath))
        }
        is ChangeSymbol -> listOf(relative(host.representationBindingCompanionPath()))
        is ConnectPorts, is ReconnectConnectionEndpoint -> listOf(relative(host.sourcePath))
        is AdjustConnectionRoute -> (host.sheetCompanionLocation() as? SheetCompanionFound)?.let { listOf(relative(it.path)) }
        is BindPart -> listOf(relative(host.functionPartBindingCompanionPath()))
        is AddPackageDependency -> listOf(relative(host.manifestPath))
        is InsertElementOccurrence -> listOf(
            relative(host.representationBindingCompanionPath()),
            (host.sheetCompanionLocation() as? SheetCompanionFound)?.let { relative(it.path) },
        ).filterNotNull().sorted()
        is InsertMacroOccurrences -> listOf(
            relative(host.representationBindingCompanionPath()),
            (host.sheetCompanionLocation() as? SheetCompanionFound)?.let { relative(it.path) },
        ).filterNotNull().sorted()
        is Undo, is Redo -> null
    }

    private fun targetIdentities(body: EditOperationBody): List<String> = when (body) {
        is MoveOccurrence -> listOf(body.occurrenceId)
        is AlignOccurrences -> body.occurrenceIds
        is DistributeOccurrences -> body.occurrenceIds
        is SnapOccurrenceToGrid -> listOf(body.occurrenceId)
        is SetStyle -> listOf(body.target.id)
        is ChangeSymbol -> listOf(body.occurrenceId)
        is ConnectPorts -> body.endpoints.map { it.portId }.sorted()
        is ReconnectConnectionEndpoint -> listOf(body.connectionId, body.replacementPortId).sorted()
        is AdjustConnectionRoute -> listOf(body.connectionId, body.projectionId).sorted()
        is BindPart -> listOf(body.functionId)
        is AddPackageDependency -> listOf(body.packageName)
        is InsertElementOccurrence -> listOf(body.functionId)
        is InsertMacroOccurrences -> body.functionIdsBySlot.values.sorted()
        is Undo -> listOf(body.journalEntryId)
        is Redo -> listOf(body.journalEntryId)
    }

    private fun relative(path: Path): String = host.repositoryRoot
        .toAbsolutePath()
        .normalize()
        .relativize(path.toAbsolutePath().normalize())
        .toString()
        .replace('\\', '/')
}
