package com.engineeringood.athena.runtime

import com.engineeringood.athena.ir.EngineeringIrDocument

/**
 * Stable origin classification for commands that reach canonical runtime history.
 */
enum class AthenaCommandOrigin {
    STANDARD,
    AI_ACCEPTED,
}

/**
 * Runtime-visible status of one recorded command relative to the currently active project state.
 */
enum class AthenaCommandHistoryRecordStatus {
    APPLIED,
    UNDONE,
}

/**
 * Inspectable runtime-owned record for one command that has been accepted into history.
 */
data class AthenaCommandHistoryRecord(
    val commandId: String,
    val commandKind: AthenaCommandKind,
    val commandOrigin: AthenaCommandOrigin,
    val status: AthenaCommandHistoryRecordStatus,
    val command: AthenaCommand,
    val changedSemanticIds: List<String>,
    val beforeDocument: EngineeringIrDocument,
    val afterDocument: EngineeringIrDocument,
)

/**
 * Runtime-owned history snapshot for the active project.
 */
data class AthenaCommandHistory(
    val records: List<AthenaCommandHistoryRecord>,
    val appliedRecordCount: Int,
)

/**
 * Runtime-owned history mutation operations supported in the current M1 slice.
 */
enum class AthenaCommandHistoryOperation {
    UNDO,
    REDO,
    REPLAY,
}

/**
 * Inspectable result of one history-backed runtime mutation operation.
 */
sealed interface AthenaCommandHistoryMutationResult {
    /**
     * Runtime project name associated with the history operation.
     */
    val projectName: String

    /**
     * The history mutation operation that was attempted.
     */
    val operation: AthenaCommandHistoryOperation
}

/**
 * Successful history-backed mutation that changed canonical runtime state.
 */
data class AthenaCommandHistoryMutationSuccess(
    override val projectName: String,
    override val operation: AthenaCommandHistoryOperation,
    val affectedCommandIds: List<String>,
    val beforeDocument: EngineeringIrDocument,
    val afterDocument: EngineeringIrDocument,
) : AthenaCommandHistoryMutationResult

/**
 * Explicit rejection when the requested history operation had no applicable command to act on.
 */
data class AthenaCommandHistoryMutationRejected(
    override val projectName: String,
    override val operation: AthenaCommandHistoryOperation,
    val reason: String,
) : AthenaCommandHistoryMutationResult

/**
 * Explicit runtime-unavailable result when the active project has no usable canonical state for history operations.
 */
data class AthenaCommandHistoryMutationUnavailable(
    override val projectName: String,
    override val operation: AthenaCommandHistoryOperation,
    val reason: String,
) : AthenaCommandHistoryMutationResult

/**
 * Internal runtime-owned state for command history tracking over the active project.
 */
internal data class AthenaCommandHistoryState(
    val baselineDocument: EngineeringIrDocument? = null,
    val records: List<AthenaRecordedCommand> = emptyList(),
    val appliedRecordCount: Int = 0,
    val nextCommandOrdinal: Int = 1,
)

/**
 * Internal immutable command journal entry used to rebuild public history snapshots.
 */
internal data class AthenaRecordedCommand(
    val commandId: String,
    val commandOrigin: AthenaCommandOrigin,
    val command: AthenaCommand,
    val changedSemanticIds: List<String>,
    val beforeDocument: EngineeringIrDocument,
    val afterDocument: EngineeringIrDocument,
)
