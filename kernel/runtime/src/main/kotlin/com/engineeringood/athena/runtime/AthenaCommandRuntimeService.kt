package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Runtime-owned semantic command categories supported by the current M1 mutation slice.
 */
enum class AthenaCommandKind {
    CONNECT_PORTS,
}

/**
 * Runtime-owned command contract for semantic mutations over the active project.
 */
sealed interface AthenaCommand {
    /**
     * Stable command category used for inspectable execution reporting.
     */
    val commandKind: AthenaCommandKind
}

/**
 * Command that creates one canonical connection between two existing ports.
 */
data class AthenaConnectPortsCommand(
    val sourcePortSemanticId: String,
    val targetPortSemanticId: String,
) : AthenaCommand {
    override val commandKind: AthenaCommandKind = AthenaCommandKind.CONNECT_PORTS
}

/**
 * Inspectable runtime-owned result of one attempted semantic command execution.
 */
sealed interface AthenaCommandExecutionResult {
    /**
     * Runtime project name associated with the command attempt.
     */
    val projectName: String

    /**
     * Stable command category associated with the command attempt.
     */
    val commandKind: AthenaCommandKind

    /**
     * Stable origin classification for the command attempt.
     */
    val commandOrigin: AthenaCommandOrigin
}

/**
 * Successful semantic mutation over canonical project state.
 */
data class AthenaCommandExecutionSuccess(
    override val projectName: String,
    override val commandKind: AthenaCommandKind,
    override val commandOrigin: AthenaCommandOrigin,
    val commandId: String,
    val beforeDocument: EngineeringDocument,
    val afterDocument: EngineeringDocument,
    val changedSemanticIds: List<String>,
) : AthenaCommandExecutionResult

/**
 * Explicit rejection when the command was understood but could not be applied safely.
 */
data class AthenaCommandExecutionRejected(
    override val projectName: String,
    override val commandKind: AthenaCommandKind,
    override val commandOrigin: AthenaCommandOrigin,
    val reason: String,
    val changedSemanticIds: List<String> = emptyList(),
) : AthenaCommandExecutionResult

/**
 * Explicit runtime-unavailable result when no canonical semantic state exists to mutate.
 */
data class AthenaCommandExecutionUnavailable(
    override val projectName: String,
    override val commandKind: AthenaCommandKind,
    override val commandOrigin: AthenaCommandOrigin,
    val reason: String,
) : AthenaCommandExecutionResult

/**
 * Runtime-owned command service that applies semantic mutations over the active project's canonical state.
 */
class AthenaCommandRuntimeService internal constructor() {
    /**
     * Executes [command] against the current canonical project state in [context].
     */
    fun execute(
        context: AthenaExecutionContext,
        command: AthenaCommand,
        origin: AthenaCommandOrigin = AthenaCommandOrigin.STANDARD,
    ): AthenaCommandExecutionResult {
        return when (val compilation = context.compileActiveProject()) {
            is CompilerCompilationParseFailure -> AthenaCommandExecutionUnavailable(
                projectName = context.project.name,
                commandKind = command.commandKind,
                commandOrigin = origin,
                reason = compilation.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
            )

            is CompilerCompilationSuccess -> executeAgainstHistoryAwareState(
                context = context,
                document = compilation.document,
                command = command,
                origin = origin,
            )
        }
    }

    /**
     * Returns an inspectable command-history snapshot for the active project.
     */
    fun history(context: AthenaExecutionContext): AthenaCommandHistory {
        val state = context.commandHistoryState()
        return AthenaCommandHistory(
            records = state.records.mapIndexed { index, record ->
                AthenaCommandHistoryRecord(
                    commandId = record.commandId,
                    commandKind = record.command.commandKind,
                    commandOrigin = record.commandOrigin,
                    status = if (index < state.appliedRecordCount) {
                        AthenaCommandHistoryRecordStatus.APPLIED
                    } else {
                        AthenaCommandHistoryRecordStatus.UNDONE
                    },
                    command = record.command,
                    changedSemanticIds = record.changedSemanticIds,
                    beforeDocument = record.beforeDocument,
                    afterDocument = record.afterDocument,
                )
            },
            appliedRecordCount = state.appliedRecordCount,
        )
    }

    /**
     * Serializes the active project's command history into a deterministic runtime-owned JSON payload.
     */
    fun serializeHistory(context: AthenaExecutionContext): String {
        val history = history(context)
        return buildString {
            append("{\"appliedRecordCount\":")
            append(history.appliedRecordCount)
            append(",\"records\":[")
            append(
                history.records.joinToString(separator = ",") { record ->
                    buildString {
                        append("{\"commandId\":\"")
                        append(record.commandId.jsonEscaped())
                        append("\",\"commandKind\":\"")
                        append(record.commandKind.name.jsonEscaped())
                        append("\",\"commandOrigin\":\"")
                        append(record.commandOrigin.name.jsonEscaped())
                        append("\",\"status\":\"")
                        append(record.status.name.jsonEscaped())
                        append("\",\"payload\":")
                        append(record.command.serializedPayloadJson())
                        append(",\"changedSemanticIds\":[")
                        append(record.changedSemanticIds.sorted().joinToString(separator = ",") { semanticId ->
                            "\"${semanticId.jsonEscaped()}\""
                        })
                        append("],\"beforeConnectionCount\":")
                        append(record.beforeDocument.connections.size)
                        append(",\"afterConnectionCount\":")
                        append(record.afterDocument.connections.size)
                        append("}")
                    }
                },
            )
            append("]}")
        }
    }

    /**
     * Returns the latest runtime-owned semantic diff inspection captured for the active project.
     */
    fun latestInspection(context: AthenaExecutionContext): AthenaSemanticDiffInspection? {
        return context.latestSemanticDiffInspection()
    }

    /**
     * Reconstructs the diff and current history consequence for one recorded command id.
     */
    fun inspectCommandHistoryConsequence(
        context: AthenaExecutionContext,
        commandId: String,
    ): AthenaSemanticDiffInspection? {
        val history = history(context)
        val record = history.records.firstOrNull { candidate -> candidate.commandId == commandId } ?: return null
        return buildSemanticDiffInspection(
            projectName = context.project.name,
            source = AthenaSemanticDiffInspectionSource.COMMAND,
            affectedCommandIds = listOf(record.commandId),
            affectedSemanticIds = record.changedSemanticIds,
            beforeDocument = record.beforeDocument,
            afterDocument = record.afterDocument,
            history = history,
            projectionConsequences = projectionConsequencesFor(
                context = context,
                affectedSemanticIds = record.changedSemanticIds,
            ),
        )
    }

    /**
     * Restores the prior canonical runtime state using the most recently applied command history record.
     */
    fun undo(context: AthenaExecutionContext): AthenaCommandHistoryMutationResult {
        val state = context.commandHistoryState()
        if (state.appliedRecordCount == 0) {
            return AthenaCommandHistoryMutationRejected(
                projectName = context.project.name,
                operation = AthenaCommandHistoryOperation.UNDO,
                reason = "No applied command exists to undo.",
            )
        }

        return when (val compilation = context.compileActiveProject()) {
            is CompilerCompilationParseFailure -> AthenaCommandHistoryMutationUnavailable(
                projectName = context.project.name,
                operation = AthenaCommandHistoryOperation.UNDO,
                reason = compilation.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
            )

            is CompilerCompilationSuccess -> {
                val record = state.records[state.appliedRecordCount - 1]
                context.replaceActiveProjectDocument(
                    document = record.beforeDocument,
                    changedSemanticIds = record.changedSemanticIds,
                )
                val updatedState = state.copy(appliedRecordCount = state.appliedRecordCount - 1)
                context.replaceCommandHistoryState(
                    updatedState,
                )
                context.replaceLatestSemanticDiffInspection(
                    buildSemanticDiffInspection(
                        projectName = context.project.name,
                        source = AthenaSemanticDiffInspectionSource.UNDO,
                        affectedCommandIds = listOf(record.commandId),
                        affectedSemanticIds = record.changedSemanticIds,
                        beforeDocument = compilation.document,
                        afterDocument = record.beforeDocument,
                        history = history(context),
                        projectionConsequences = projectionConsequencesFor(
                            context = context,
                            affectedSemanticIds = record.changedSemanticIds,
                        ),
                    ),
                )
                AthenaCommandHistoryMutationSuccess(
                    projectName = context.project.name,
                    operation = AthenaCommandHistoryOperation.UNDO,
                    affectedCommandIds = listOf(record.commandId),
                    beforeDocument = compilation.document,
                    afterDocument = record.beforeDocument,
                )
            }
        }
    }

    /**
     * Reapplies the next undone command through the same runtime-owned mutation rules as normal execution.
     */
    fun redo(context: AthenaExecutionContext): AthenaCommandHistoryMutationResult {
        val state = context.commandHistoryState()
        if (state.appliedRecordCount >= state.records.size) {
            return AthenaCommandHistoryMutationRejected(
                projectName = context.project.name,
                operation = AthenaCommandHistoryOperation.REDO,
                reason = "No undone command exists to redo.",
            )
        }

        return when (val compilation = context.compileActiveProject()) {
            is CompilerCompilationParseFailure -> AthenaCommandHistoryMutationUnavailable(
                projectName = context.project.name,
                operation = AthenaCommandHistoryOperation.REDO,
                reason = compilation.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
            )

            is CompilerCompilationSuccess -> {
                val record = state.records[state.appliedRecordCount]
                when (val mutation = applyCommand(context, compilation.document, record.command)) {
                    is AthenaCommandApplicationRejected -> AthenaCommandHistoryMutationRejected(
                        projectName = context.project.name,
                        operation = AthenaCommandHistoryOperation.REDO,
                        reason = mutation.reason,
                    )

                    is AthenaCommandApplicationSuccess -> {
                        context.replaceActiveProjectDocument(
                            document = mutation.afterDocument,
                            changedSemanticIds = mutation.changedSemanticIds,
                        )
                        val updatedState = state.copy(appliedRecordCount = state.appliedRecordCount + 1)
                        context.replaceCommandHistoryState(
                            updatedState,
                        )
                        context.replaceLatestSemanticDiffInspection(
                            buildSemanticDiffInspection(
                                projectName = context.project.name,
                                source = AthenaSemanticDiffInspectionSource.REDO,
                                affectedCommandIds = listOf(record.commandId),
                                affectedSemanticIds = mutation.changedSemanticIds,
                                beforeDocument = compilation.document,
                                afterDocument = mutation.afterDocument,
                                history = history(context),
                                projectionConsequences = projectionConsequencesFor(
                                    context = context,
                                    affectedSemanticIds = mutation.changedSemanticIds,
                                ),
                            ),
                        )
                        AthenaCommandHistoryMutationSuccess(
                            projectName = context.project.name,
                            operation = AthenaCommandHistoryOperation.REDO,
                            affectedCommandIds = listOf(record.commandId),
                            beforeDocument = compilation.document,
                            afterDocument = mutation.afterDocument,
                        )
                    }
                }
            }
        }
    }

    /**
     * Rebuilds canonical runtime state by replaying the full recorded command sequence from the stored baseline.
     */
    fun replay(context: AthenaExecutionContext): AthenaCommandHistoryMutationResult {
        val state = context.commandHistoryState()
        if (state.records.isEmpty()) {
            return AthenaCommandHistoryMutationRejected(
                projectName = context.project.name,
                operation = AthenaCommandHistoryOperation.REPLAY,
                reason = "No recorded command sequence exists to replay.",
            )
        }

        val baselineDocument = state.baselineDocument ?: return AthenaCommandHistoryMutationRejected(
            projectName = context.project.name,
            operation = AthenaCommandHistoryOperation.REPLAY,
            reason = "No recorded baseline document exists to replay from.",
        )

        return when (val compilation = context.compileActiveProject()) {
            is CompilerCompilationParseFailure -> AthenaCommandHistoryMutationUnavailable(
                projectName = context.project.name,
                operation = AthenaCommandHistoryOperation.REPLAY,
                reason = compilation.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
            )

            is CompilerCompilationSuccess -> {
                var replayedDocument = baselineDocument
                for (record in state.records) {
                    when (val mutation = applyCommand(context, replayedDocument, record.command)) {
                        is AthenaCommandApplicationRejected -> return AthenaCommandHistoryMutationRejected(
                            projectName = context.project.name,
                            operation = AthenaCommandHistoryOperation.REPLAY,
                            reason = mutation.reason,
                        )

                        is AthenaCommandApplicationSuccess -> replayedDocument = mutation.afterDocument
                    }
                }

                context.replaceActiveProjectDocument(
                    document = replayedDocument,
                    changedSemanticIds = state.records.flatMap { record -> record.changedSemanticIds }.distinct(),
                )
                val updatedState = state.copy(appliedRecordCount = state.records.size)
                context.replaceCommandHistoryState(
                    updatedState,
                )
                context.replaceLatestSemanticDiffInspection(
                    buildSemanticDiffInspection(
                        projectName = context.project.name,
                        source = AthenaSemanticDiffInspectionSource.REPLAY,
                        affectedCommandIds = state.records.map { record -> record.commandId },
                        affectedSemanticIds = state.records.flatMap { record -> record.changedSemanticIds }.distinct(),
                        beforeDocument = compilation.document,
                        afterDocument = replayedDocument,
                        history = history(context),
                        projectionConsequences = projectionConsequencesFor(
                            context = context,
                            affectedSemanticIds = state.records.flatMap { record -> record.changedSemanticIds }.distinct(),
                        ),
                    ),
                )
                AthenaCommandHistoryMutationSuccess(
                    projectName = context.project.name,
                    operation = AthenaCommandHistoryOperation.REPLAY,
                    affectedCommandIds = state.records.map { it.commandId },
                    beforeDocument = compilation.document,
                    afterDocument = replayedDocument,
                )
            }
        }
    }

    private fun executeAgainstHistoryAwareState(
        context: AthenaExecutionContext,
        document: EngineeringDocument,
        command: AthenaCommand,
        origin: AthenaCommandOrigin,
    ): AthenaCommandExecutionResult {
        return when (val mutation = applyCommand(context, document, command)) {
            is AthenaCommandApplicationRejected -> rejection(context, command, origin, mutation.reason)
            is AthenaCommandApplicationSuccess -> {
                context.replaceActiveProjectDocument(
                    document = mutation.afterDocument,
                    changedSemanticIds = mutation.changedSemanticIds,
                )
                val recordedCommand = recordSuccessfulCommand(
                    context = context,
                    command = command,
                    beforeDocument = document,
                    afterDocument = mutation.afterDocument,
                    changedSemanticIds = mutation.changedSemanticIds,
                    origin = origin,
                )
                context.replaceLatestSemanticDiffInspection(
                    buildSemanticDiffInspection(
                        projectName = context.project.name,
                        source = AthenaSemanticDiffInspectionSource.COMMAND,
                        affectedCommandIds = listOf(recordedCommand.commandId),
                        affectedSemanticIds = mutation.changedSemanticIds,
                        beforeDocument = document,
                        afterDocument = mutation.afterDocument,
                        history = history(context),
                        projectionConsequences = projectionConsequencesFor(
                            context = context,
                            affectedSemanticIds = mutation.changedSemanticIds,
                        ),
                    ),
                )
                AthenaCommandExecutionSuccess(
                    projectName = context.project.name,
                    commandKind = command.commandKind,
                    commandOrigin = origin,
                    commandId = recordedCommand.commandId,
                    beforeDocument = document,
                    afterDocument = mutation.afterDocument,
                    changedSemanticIds = mutation.changedSemanticIds,
                )
            }
        }
    }

    private fun rejection(
        context: AthenaExecutionContext,
        command: AthenaCommand,
        origin: AthenaCommandOrigin,
        reason: String,
    ): AthenaCommandExecutionRejected {
        return AthenaCommandExecutionRejected(
            projectName = context.project.name,
            commandKind = command.commandKind,
            commandOrigin = origin,
            reason = reason,
        )
    }

    private fun applyCommand(
        context: AthenaExecutionContext,
        document: EngineeringDocument,
        command: AthenaCommand,
    ): AthenaCommandApplicationResult {
        return when (command) {
            is AthenaConnectPortsCommand -> applyConnectPorts(
                context = context,
                document = document,
                command = command,
            )
        }
    }

    private fun applyConnectPorts(
        context: AthenaExecutionContext,
        document: EngineeringDocument,
        command: AthenaConnectPortsCommand,
    ): AthenaCommandApplicationResult {
        val portsBySemanticId = document.ports.associateBy { port -> port.id.value }
        val sourcePort = portsBySemanticId[command.sourcePortSemanticId]
            ?: return AthenaCommandApplicationRejected("Source port `${command.sourcePortSemanticId}` does not exist.")
        val targetPort = portsBySemanticId[command.targetPortSemanticId]
            ?: return AthenaCommandApplicationRejected("Target port `${command.targetPortSemanticId}` does not exist.")

        val connectionIdentity = StableSemanticIdentity(
            "connection:${sourcePort.authoredPath()}->${targetPort.authoredPath()}",
        )

        if (document.connections.any { connection -> connection.id == connectionIdentity }) {
            return AthenaCommandApplicationRejected("Connection `${connectionIdentity.value}` already exists.")
        }

        return AthenaCommandApplicationSuccess(
            afterDocument = document.copy(
                connections = document.connections + EngineeringConnection(
                    id = connectionIdentity,
                    from = sourcePort.asResolvedReference(runtimeCommandProvenance(context, command)),
                    to = targetPort.asResolvedReference(runtimeCommandProvenance(context, command)),
                    provenance = runtimeCommandProvenance(context, command),
                ),
            ),
            changedSemanticIds = listOf(
                connectionIdentity.value,
                sourcePort.id.value,
                targetPort.id.value,
            ),
        )
    }

    private fun recordSuccessfulCommand(
        context: AthenaExecutionContext,
        command: AthenaCommand,
        beforeDocument: EngineeringDocument,
        afterDocument: EngineeringDocument,
        changedSemanticIds: List<String>,
        origin: AthenaCommandOrigin,
    ): AthenaRecordedCommand {
        val currentState = context.commandHistoryState()
        val retainedRecords = currentState.records.take(currentState.appliedRecordCount)
        val commandId = "command-${currentState.nextCommandOrdinal.toString().padStart(4, '0')}"
        val recordedCommand = AthenaRecordedCommand(
            commandId = commandId,
            commandOrigin = origin,
            command = command,
            changedSemanticIds = changedSemanticIds,
            beforeDocument = beforeDocument,
            afterDocument = afterDocument,
        )
        context.replaceCommandHistoryState(
            currentState.copy(
                baselineDocument = currentState.baselineDocument ?: beforeDocument,
                records = retainedRecords + recordedCommand,
                appliedRecordCount = retainedRecords.size + 1,
                nextCommandOrdinal = currentState.nextCommandOrdinal + 1,
            ),
        )
        return recordedCommand
    }
}

/**
 * Resolves projection refresh consequences for one semantic inspection request without introducing a second history model.
 */
private fun projectionConsequencesFor(
    context: AthenaExecutionContext,
    affectedSemanticIds: List<String>,
): List<AthenaProjectionRefreshConsequence> {
    val normalizedSemanticIds = affectedSemanticIds.distinct().sorted()
    val currentReport = context.incrementalUpdateReport()
    if (currentReport != null && currentReport.changedSemanticIds.sorted() == normalizedSemanticIds) {
        return currentReport.toProjectionConsequences()
    }

    val supportedViewIds = context.projectProjectionSession().supportedViews.map { view -> view.viewId }.distinct().sorted()
    if (supportedViewIds.isEmpty()) {
        return emptyList()
    }
    return listOf(
        AthenaProjectionRefreshConsequence(
            layer = AthenaProjectionRefreshConsequenceLayer.LAYOUT,
            mode = null,
            affectedViewIds = supportedViewIds,
            affectedSemanticIds = normalizedSemanticIds,
        ),
        AthenaProjectionRefreshConsequence(
            layer = AthenaProjectionRefreshConsequenceLayer.GEOMETRY,
            mode = null,
            affectedViewIds = supportedViewIds,
            affectedSemanticIds = normalizedSemanticIds,
        ),
    )
}

/**
 * Returns the authored semantic path for one canonical port.
 */
private fun EngineeringPort.authoredPath(): String = (ownerReference.authoredPath + name).joinToString(".")

/**
 * Converts one canonical port into a resolved reference suitable for command-created connections.
 */
private fun EngineeringPort.asResolvedReference(provenance: SourceProvenance): EngineeringReference {
    return EngineeringReference(
        authoredPath = ownerReference.authoredPath + name,
        resolvedIdentity = id,
        provenance = provenance,
    )
}

/**
 * Creates synthetic provenance for command-originated semantic objects that do not come from authored DSL text.
 */
private fun runtimeCommandProvenance(
    context: AthenaExecutionContext,
    command: AthenaCommand,
): SourceProvenance {
    return SourceProvenance(
        file = "runtime://${context.project.name}/${command.commandKind.name.lowercase()}",
        startLine = 0,
        startColumn = 0,
        endLine = 0,
        endColumn = 0,
    )
}

/**
 * Internal result of applying one semantic command over a canonical engineering document.
 */
private sealed interface AthenaCommandApplicationResult

/**
 * Successful in-memory semantic mutation over a canonical engineering document.
 */
private data class AthenaCommandApplicationSuccess(
    val afterDocument: EngineeringDocument,
    val changedSemanticIds: List<String>,
) : AthenaCommandApplicationResult

/**
 * Rejected in-memory semantic mutation over a canonical engineering document.
 */
private data class AthenaCommandApplicationRejected(
    val reason: String,
) : AthenaCommandApplicationResult

/**
 * Escapes one string for deterministic JSON emission without adding new dependencies.
 */
private fun String.jsonEscaped(): String {
    return buildString {
        this@jsonEscaped.forEach { character ->
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(character)
            }
        }
    }
}

/**
 * Serializes one runtime-owned command payload into a deterministic JSON object.
 */
private fun AthenaCommand.serializedPayloadJson(): String {
    return when (this) {
        is AthenaConnectPortsCommand -> {
            "{\"sourcePortSemanticId\":\"${sourcePortSemanticId.jsonEscaped()}\",\"targetPortSemanticId\":\"${targetPortSemanticId.jsonEscaped()}\"}"
        }
    }
}
