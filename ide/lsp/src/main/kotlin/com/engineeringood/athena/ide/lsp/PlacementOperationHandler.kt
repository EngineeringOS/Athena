package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaDiagramSceneCompiler
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.interaction.AlignOccurrences
import com.engineeringood.athena.interaction.DistributeOccurrences
import com.engineeringood.athena.interaction.EditOperationBody
import com.engineeringood.athena.interaction.EditOperationEnvelope
import com.engineeringood.athena.interaction.EditOperationResult
import com.engineeringood.athena.interaction.LockAction
import com.engineeringood.athena.interaction.MoveOccurrence
import com.engineeringood.athena.interaction.OperationRejectionReason
import com.engineeringood.athena.interaction.SnapOccurrenceToGrid
import com.engineeringood.athena.interaction.SourceFilePatch
import com.engineeringood.athena.interaction.SourceTraceContext
import com.engineeringood.athena.interaction.SourcePatchSet
import com.engineeringood.athena.language.AthenaSheetCompanionParser
import com.engineeringood.athena.language.SheetCompanionFound
import com.engineeringood.athena.language.SheetCompanionParseSuccess
import com.engineeringood.athena.language.SheetCompanionSource
import com.engineeringood.athena.language.SheetCompanionEditor
import com.engineeringood.athena.language.SheetPlacementLockIntent
import com.engineeringood.athena.language.SheetPlacementWrite
import com.engineeringood.athena.presentation.AthenaDiagramScene
import com.engineeringood.athena.presentation.PublicationState
import com.engineeringood.athena.presentation.TraceRole
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/** Executes Presentation placement edits through the shared source transaction authority. */
internal class PlacementOperationHandler(
    private val host: AthenaLspSessionHostReady,
    private val publicationService: AthenaDiagramPublicationService,
    private val revisionService: SourceRevisionService,
    private val transactionEngine: SourceTransactionEngine,
    private val planner: PlacementOperationPlanner = PlacementOperationPlanner(),
    private val editor: SheetCompanionEditor = SheetCompanionEditor(),
) {
    fun apply(operation: EditOperationEnvelope, body: EditOperationBody): EditOperationResult {
        val currentRevision = revisionService.current()
        val publication = publicationService.current()
        val scene = publication.scene
        if (publication.state != PublicationState.READY || scene == null) {
            return rejected(
                operation,
                currentRevision,
                OperationRejectionReason.UNAVAILABLE,
                diagnostic(
                    operation.kind.name,
                    "Canonical Scene is unavailable for Presentation editing.",
                    "Refresh a READY engineering document and retry.",
                    "edit.operation.scene-unavailable",
                ),
            )
        }
        val sheet = host.sheetCompanionLocation() as? SheetCompanionFound
            ?: return rejected(
                operation,
                currentRevision,
                OperationRejectionReason.UNAVAILABLE,
                diagnostic(
                    host.sourcePath.fileName.toString(),
                    "Required same-basename Sheet Companion is unavailable.",
                    "Restore exact same-basename Sheet Companion before editing placements.",
                    "sheet.companion.unavailable",
                ),
            )
        val targetIds = targetIds(body)
        if (!matchesPrimaryTrace(scene, targetIds, operation.sourceTrace)) {
            return rejected(
                operation,
                currentRevision,
                OperationRejectionReason.INVALID,
                diagnostic(
                    operation.sourceTrace.subjectId,
                    "Selected occurrences do not match the supplied primary Source Trace.",
                    "Refresh the engineering document and submit trace context from a selected occurrence.",
                    "edit.operation.trace-invalid",
                ),
            )
        }
        val planned = runCatching { planner.plan(scene, body) }.getOrElse { failure ->
            val planning = failure as? PlacementOperationPlanningFailure
            return rejected(
                operation,
                currentRevision,
                OperationRejectionReason.INVALID,
                diagnostic(
                    planning?.subject ?: operation.kind.name,
                    planning?.problem ?: "Placement intent is invalid: ${failure.message ?: "unsupported value"}.",
                    planning?.correction ?: "Use current visible occurrences and a valid Sheet anchor.",
                    planning?.code ?: "edit.operation.placement-invalid",
                ),
            )
        }
        val before = Files.readString(sheet.path)
        val parsedBefore = AthenaSheetCompanionParser().parse(sheet.path.toString(), before)
            as? SheetCompanionParseSuccess
            ?: return rejected(
                operation,
                currentRevision,
                OperationRejectionReason.COMPILATION_FAILURE,
                diagnostic(
                    sheet.path.fileName.toString(),
                    "Current Sheet Companion does not parse.",
                    "Correct current Sheet placement before editing.",
                    "sheet.companion.invalid",
                ),
            )
        val edited = runCatching {
            editor.writePlacements(
                before,
                planned.map { placement ->
                    SheetPlacementWrite(
                        occurrenceId = authoredPlacementName(scene, parsedBefore.source, placement.occurrenceId),
                        point = com.engineeringood.athena.language.SheetPoint(
                            placement.point.x,
                            placement.point.y,
                            parsedBefore.source.span,
                        ),
                        lockIntent = placement.lockAction.toLanguageLockIntent(),
                    )
                },
            )
        }.getOrElse { failure ->
            return rejected(
                operation,
                currentRevision,
                OperationRejectionReason.INVALID,
                diagnostic(
                    operation.kind.name,
                    "Placement intent cannot be written: ${failure.message ?: "unsupported value"}.",
                    "Use valid current Sheet placements and retry.",
                    "edit.operation.placement-write-invalid",
                ),
            )
        }
        if (!edited.changed) {
            return rejected(
                operation,
                currentRevision,
                OperationRejectionReason.INVALID,
                diagnostic(
                    operation.kind.name,
                    "Presentation edit would not change authored Sheet placement.",
                    "Choose a different placement or cancel the edit.",
                    "edit.operation.noop",
                ),
            )
        }
        val parsed = AthenaSheetCompanionParser().parse(sheet.path.toString(), edited.updatedSource)
            as? SheetCompanionParseSuccess
            ?: return rejected(
                operation,
                currentRevision,
                OperationRejectionReason.COMPILATION_FAILURE,
                diagnostic(
                    sheet.path.fileName.toString(),
                    "Proposed Sheet Companion does not parse.",
                    "Correct the placement intent before retrying.",
                    "sheet.companion.invalid",
                ),
            )
        val stagedRevision = revisionService.current(
            sheetCompanionBytesOverride = edited.updatedSource.toByteArray(StandardCharsets.UTF_8),
        )
        val patchSet = SourcePatchSet(
            listOf(SourceFilePatch(relative(sheet.path), before, edited.updatedSource)),
        )
        return transactionEngine.execute(operation, patchSet, stagedRevision) {
            val compilation = host.executionContext.compiler().compile(host.sourcePath, parsed.source) as? CompilerCompilationSuccess
                ?: throw StagedOperationFailure(
                    OperationRejectionReason.COMPILATION_FAILURE,
                    diagnostic(
                        host.sourcePath.fileName.toString(),
                        "Engineering source cannot be compiled for placement validation.",
                        "Correct current engineering source before editing presentation placement.",
                        "edit.operation.source-invalid",
                    ),
                )
            val stagedSceneCompilation = AthenaDiagramSceneCompiler().compile(
                compilation,
                stagedRevision.sceneInputRevision,
                styleCompanion = host.styleCompanionSource(),
            )
            val rawStagedScene = stagedSceneCompilation.scene ?: stagedSceneCompilation.diagnostics.firstOrNull()
                ?.let { failure ->
                    throw StagedOperationFailure(
                        OperationRejectionReason.COMPILATION_FAILURE,
                        diagnostic(
                            failure.subject,
                            failure.problem,
                            failure.correction,
                            failure.code,
                        ),
                    )
                }
                ?: throw StagedOperationFailure(
                    OperationRejectionReason.COMPILATION_FAILURE,
                    diagnostic(
                        operation.kind.name,
                        "Proposed Sheet placement does not produce a complete Canonical Scene.",
                        "Use placement intent compatible with current engineering source and Sheet grid.",
                        "edit.operation.placement-compilation",
                    ),
                )
            val stagedScene = host.packageBackedScene(rawStagedScene)
                ?: throw StagedOperationFailure(
                    OperationRejectionReason.COMPILATION_FAILURE,
                    diagnostic(
                        operation.kind.name,
                        "Proposed Sheet placement does not preserve admitted package representations.",
                        "Restore current PACKAGE_READY bindings and package assets before editing placement.",
                        "edit.operation.package-scene-invalid",
                    ),
                )
            validateResultIdentity(scene, stagedScene, targetIds)
        }
    }

    private fun matchesPrimaryTrace(
        scene: AthenaDiagramScene,
        targetIds: List<String>,
        sourceTrace: SourceTraceContext,
    ): Boolean = scene.occurrences
        .filter { it.occurrenceId in targetIds }
        .any { occurrence ->
            occurrence.traceId.value == sourceTrace.traceId &&
                occurrence.subjectId == sourceTrace.subjectId &&
                scene.traces.singleOrNull { trace -> trace.traceId == occurrence.traceId }
                    ?.origins
                    ?.any { origin -> origin.primary && origin.subjectId == sourceTrace.subjectId } == true
        }

    private fun validateResultIdentity(
        before: AthenaDiagramScene,
        after: AthenaDiagramScene,
        targetIds: List<String>,
    ) {
        targetIds.forEach { occurrenceId ->
            val previous = before.occurrences.single { it.occurrenceId == occurrenceId }
            val resulting = after.occurrences.singleOrNull { it.occurrenceId == occurrenceId }
                ?: throw StagedOperationFailure(
                    OperationRejectionReason.COMPILATION_FAILURE,
                    diagnostic(
                        occurrenceId,
                        "Placement edit removed selected occurrence from Canonical Scene.",
                        "Use an edit that preserves current engineering occurrence identity.",
                        "edit.operation.identity-lost",
                    ),
                )
            if (
                previous.subjectId != resulting.subjectId ||
                previous.semanticId != resulting.semanticId ||
                previous.representationRef != resulting.representationRef ||
                previous.traceId != resulting.traceId
            ) {
                throw StagedOperationFailure(
                    OperationRejectionReason.COMPILATION_FAILURE,
                    diagnostic(
                        occurrenceId,
                        "Placement edit changed engineering or representation identity.",
                        "Use Presentation operations only for Sheet placement.",
                        "edit.operation.identity-changed",
                    ),
                )
            }
        }
    }

    private fun authoredPlacementName(
        scene: AthenaDiagramScene,
        source: SheetCompanionSource,
        occurrenceId: String,
    ): String {
        val occurrence = scene.occurrences.single { it.occurrenceId == occurrenceId }
        val placementLines = scene.traces
            .singleOrNull { it.traceId == occurrence.traceId }
            ?.origins
            ?.filter { origin -> origin.role == TraceRole.SHEET_PLACEMENT }
            ?.map { origin -> origin.startLine + 1 }
            .orEmpty()
        source.placements.firstOrNull { placement -> placement.span.start.line in placementLines }
            ?.let { return it.occurrence }
        return occurrence.labels
            .singleOrNull { label -> label.role == "occurrence" }
            ?.text
            ?.takeIf(String::isNotBlank)
            ?: throw IllegalArgumentException(
                "Occurrence `$occurrenceId` has no authored Sheet placement key or display label.",
            )
    }

    private fun targetIds(body: EditOperationBody): List<String> = when (body) {
        is MoveOccurrence -> listOf(body.occurrenceId)
        is SnapOccurrenceToGrid -> listOf(body.occurrenceId)
        is AlignOccurrences -> body.occurrenceIds
        is DistributeOccurrences -> body.occurrenceIds
        else -> error("Placement handler received ${body.kind}.")
    }

    private fun LockAction.toLanguageLockIntent(): SheetPlacementLockIntent = when (this) {
        LockAction.PRESERVE -> SheetPlacementLockIntent.PRESERVE
        LockAction.LOCK -> SheetPlacementLockIntent.LOCK
        LockAction.UNLOCK -> SheetPlacementLockIntent.UNLOCK
    }

    private fun relative(path: Path): String = host.repositoryRoot.toAbsolutePath().normalize()
        .relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/')
}
