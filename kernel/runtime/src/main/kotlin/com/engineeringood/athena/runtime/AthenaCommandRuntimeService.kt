package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.reuse.SemanticMacroParameterValue

/**
 * Runtime-owned semantic command categories supported by the current M1 mutation slice.
 */
enum class AthenaCommandKind {
    CONNECT_PORTS,
    APPLY_SEMANTIC_MACRO_BUNDLE,
}

/**
 * Runtime-owned command contract for semantic mutations over the active project.
 */
sealed interface AthenaCommand {
    /**
     * Stable command category used for inspectable execution reporting.
     */
    val commandKind: AthenaCommandKind

    /**
     * Explicit mutation category carried through the shared runtime-owned mutation model.
     */
    val mutationCategory: AthenaMutationCategory
        get() = commandKind.defaultMutationCategory()
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
 * Explicit closeout contract for retained non-Theia relationship mutation callers.
 *
 * M32 does not let product authoring surfaces treat [AthenaConnectPortsCommand] as a second
 * relationship authoring model. CLI, desktop Compose, and electrical runtime callers may retain
 * this command only as a runtime-owned compatibility command while product authoring continues to
 * enter through [SemanticRelationshipIntent].
 */
data class AthenaRelationshipMutationCompatibilityContract(
    val contractId: String,
    val retainedCommandKind: AthenaCommandKind,
    val retainedRuntimeCommandClass: String,
    val productAuthoringIntentClass: String,
    val mutableSourceAuthority: Boolean,
    val retainedSurfaces: Set<String>,
    val retainedSurfacePolicy: String,
)

/**
 * Publishes the retained compatibility status for the low-level connect command.
 */
fun AthenaConnectPortsCommand.compatibilityContract(): AthenaRelationshipMutationCompatibilityContract {
    return AthenaRelationshipMutationCompatibilityContract(
        contractId = "legacy-connect-ports-runtime-command-v1",
        retainedCommandKind = commandKind,
        retainedRuntimeCommandClass = AthenaConnectPortsCommand::class.qualifiedName.orEmpty(),
        productAuthoringIntentClass = SemanticRelationshipIntent::class.qualifiedName.orEmpty(),
        mutableSourceAuthority = false,
        retainedSurfaces = setOf("cli", "desktop-compose", "domain-electrical-runtime"),
        retainedSurfacePolicy = "Retained only as a runtime-owned compatibility command; not a product authoring contract.",
    )
}

/**
 * Command that commits one prepared Semantic Macro mutation bundle through the sole runtime mutation authority.
 */
data class AthenaApplySemanticMacroBundleCommand(
    val bundle: AthenaSemanticMacroMutationBundle,
) : AthenaCommand {
    override val commandKind: AthenaCommandKind = AthenaCommandKind.APPLY_SEMANTIC_MACRO_BUNDLE
}

/**
 * Inspectable runtime-owned result of one attempted semantic command execution.
 */
sealed interface AthenaCommandExecutionResult : AthenaMutationResult {
    /**
     * Stable command category associated with the command attempt.
     */
    val commandKind: AthenaCommandKind

    /**
     * Stable origin classification for the command attempt.
     */
    val commandOrigin: AthenaCommandOrigin

    override val mutationCategory: AthenaMutationCategory
        get() = commandKind.defaultMutationCategory()
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
) : AthenaCommandExecutionResult {
    override val outcome: AthenaMutationOutcome = AthenaMutationOutcome.ACCEPTED
    override val validationFeedback: List<AthenaMutationValidationFeedback> = emptyList()
}

/**
 * Explicit rejection when the command was understood but could not be applied safely.
 */
data class AthenaCommandExecutionRejected(
    override val projectName: String,
    override val commandKind: AthenaCommandKind,
    override val commandOrigin: AthenaCommandOrigin,
    val reason: String,
    val changedSemanticIds: List<String> = emptyList(),
) : AthenaCommandExecutionResult {
    override val outcome: AthenaMutationOutcome = AthenaMutationOutcome.REJECTED
    override val validationFeedback: List<AthenaMutationValidationFeedback> = emptyList()
}

/**
 * Explicit validation feedback when Athena accepts the request boundary but requires caller-visible
 * validation guidance before a canonical mutation can proceed.
 */
data class AthenaCommandExecutionValidationFeedback(
    override val projectName: String,
    override val commandKind: AthenaCommandKind,
    override val commandOrigin: AthenaCommandOrigin,
    override val validationFeedback: List<AthenaMutationValidationFeedback>,
    val changedSemanticIds: List<String> = emptyList(),
) : AthenaCommandExecutionResult {
    init {
        require(validationFeedback.isNotEmpty()) {
            "Validation feedback results must include at least one feedback item."
        }
    }

    override val outcome: AthenaMutationOutcome = AthenaMutationOutcome.VALIDATION_FEEDBACK
}

/**
 * Explicit runtime-unavailable result when no canonical semantic state exists to mutate.
 */
data class AthenaCommandExecutionUnavailable(
    override val projectName: String,
    override val commandKind: AthenaCommandKind,
    override val commandOrigin: AthenaCommandOrigin,
    val reason: String,
) : AthenaCommandExecutionResult {
    override val outcome: AthenaMutationOutcome = AthenaMutationOutcome.UNAVAILABLE
    override val validationFeedback: List<AthenaMutationValidationFeedback> = emptyList()
}

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
                        append("\",\"mutationCategory\":\"")
                        append(record.mutationCategory.name.jsonEscaped())
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
            is AthenaApplySemanticMacroBundleCommand -> applySemanticMacroBundle(
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
        (
            validateCompatiblePortConnection(
                context = context,
                sourcePortSemanticId = sourcePort.id.value,
                targetPortSemanticId = targetPort.id.value,
            )
                ?: validateCompatiblePortConnection(
                    sourcePort = sourcePort,
                    targetPort = targetPort,
                )
            )?.let { reason ->
            return AthenaCommandApplicationRejected(reason)
        }

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

    private fun applySemanticMacroBundle(
        context: AthenaExecutionContext,
        document: EngineeringDocument,
        command: AthenaApplySemanticMacroBundleCommand,
    ): AthenaCommandApplicationResult {
        val bundle = command.bundle
        val componentIds = document.components.map { component -> component.id.value }.toSet()
        val portIds = document.ports.map { port -> port.id.value }.toSet()
        val connectionIds = document.connections.map { connection -> connection.id.value }.toSet()

        val createComponents = bundle.operations.filterIsInstance<AthenaSemanticMacroCreateComponentOperation>()
        val createPorts = bundle.operations.filterIsInstance<AthenaSemanticMacroCreatePortOperation>()
        val createConnections = bundle.operations.filterIsInstance<AthenaSemanticMacroCreateConnectionOperation>()

        val duplicateComponentId = createComponents
            .map { operation -> operation.subjectId.value }
            .firstOrNull { semanticId -> semanticId in componentIds }
        if (duplicateComponentId != null) {
            return AthenaCommandApplicationRejected("Component `${duplicateComponentId}` already exists.")
        }

        val duplicatePortId = createPorts
            .map { operation -> operation.subjectId.value }
            .firstOrNull { semanticId -> semanticId in portIds }
        if (duplicatePortId != null) {
            return AthenaCommandApplicationRejected("Port `${duplicatePortId}` already exists.")
        }

        val duplicateConnectionId = createConnections
            .map { operation -> operation.subjectId.value }
            .firstOrNull { semanticId -> semanticId in connectionIds }
        if (duplicateConnectionId != null) {
            return AthenaCommandApplicationRejected("Connection `${duplicateConnectionId}` already exists.")
        }

        val provenance = runtimeCommandProvenance(context, command)
        val componentById = document.components.associateBy { component -> component.id.value }.toMutableMap()
        val nextComponents = document.components.toMutableList()
        createComponents.sortedBy { operation -> operation.subjectId.value }.forEach { operation ->
            val component = EngineeringComponent(
                id = operation.subjectId,
                name = operation.summary ?: operation.templateId,
                kind = operation.conceptId,
                properties = operation.toEngineeringProperties(),
                provenance = provenance,
            )
            componentById[component.id.value] = component
            nextComponents += component
        }

        val portById = document.ports.associateBy { port -> port.id.value }.toMutableMap()
        val nextPorts = document.ports.toMutableList()
        createPorts.sortedBy { operation -> operation.subjectId.value }.forEach { operation ->
            val owner = componentById[operation.componentSubjectId.value]
                ?: return AthenaCommandApplicationRejected(
                    "Semantic Macro port `${operation.subjectId.value}` references missing component `${operation.componentSubjectId.value}`.",
                )
            val port = EngineeringPort(
                id = operation.subjectId,
                ownerReference = EngineeringReference(
                    authoredPath = listOf(owner.name),
                    resolvedIdentity = owner.id,
                    provenance = provenance,
                ),
                name = operation.portRoleId,
                properties = listOf(
                    EngineeringProperty("role", EngineeringPropertyValue.Symbol(operation.portRoleId)),
                    EngineeringProperty("templateId", EngineeringPropertyValue.Symbol(operation.componentTemplateId)),
                ),
                provenance = provenance,
            )
            portById[port.id.value] = port
            nextPorts += port
        }

        val nextConnections = document.connections.toMutableList()
        createConnections.sortedBy { operation -> operation.subjectId.value }.forEach { operation ->
            val fromPort = portById[operation.fromPortSubjectId.value]
                ?: return AthenaCommandApplicationRejected(
                    "Semantic Macro connection `${operation.subjectId.value}` references missing source port `${operation.fromPortSubjectId.value}`.",
                )
            val toPort = portById[operation.toPortSubjectId.value]
                ?: return AthenaCommandApplicationRejected(
                    "Semantic Macro connection `${operation.subjectId.value}` references missing target port `${operation.toPortSubjectId.value}`.",
                )
            nextConnections += EngineeringConnection(
                id = operation.subjectId,
                from = fromPort.asResolvedReference(provenance),
                to = toPort.asResolvedReference(provenance),
                provenance = provenance,
            )
        }

        return AthenaCommandApplicationSuccess(
            afterDocument = document.copy(
                components = nextComponents.toList(),
                ports = nextPorts.toList(),
                connections = nextConnections.toList(),
            ),
            changedSemanticIds = bundle.affectedSemanticIds.map { identity -> identity.value }.sorted(),
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
 * Applies the first narrow semantic-port compatibility guard for guided connection authoring.
 *
 * The guard stays downstream of active component knowledge and keeps the command runtime from
 * accepting obviously incompatible graph-originated connects when semantic-port metadata is
 * available for both endpoints.
 */
private fun validateCompatiblePortConnection(
    context: AthenaExecutionContext,
    sourcePortSemanticId: String,
    targetPortSemanticId: String,
): String? {
    val componentKnowledge = context.componentKnowledgeRuntime().inspect(context) as? AthenaComponentKnowledgeReady ?: return null
    val portsBySemanticId = componentKnowledge.semanticPorts.associateBy { port -> port.portSemanticId.value }
    val sourcePort = portsBySemanticId[sourcePortSemanticId] ?: return null
    val targetPort = portsBySemanticId[targetPortSemanticId] ?: return null

    if (sourcePort.definition.signalFamilyId != targetPort.definition.signalFamilyId) {
        return "Port `${sourcePortSemanticId}` cannot connect to `${targetPortSemanticId}` because their signal families differ."
    }
    if (!directionsAreCompatible(sourcePort.definition.direction.name, targetPort.definition.direction.name)) {
        return "Port `${sourcePortSemanticId}` cannot connect to `${targetPortSemanticId}` because their semantic directions are incompatible."
    }
    if (sourcePort.definition.protocolIds.isNotEmpty() || targetPort.definition.protocolIds.isNotEmpty()) {
        val sharedProtocols = sourcePort.definition.protocolIds.intersect(targetPort.definition.protocolIds)
        if (sharedProtocols.isEmpty()) {
            return "Port `${sourcePortSemanticId}` cannot connect to `${targetPortSemanticId}` because they do not share a compatible protocol."
        }
    }
    return null
}

private fun validateCompatiblePortConnection(
    sourcePort: EngineeringPort,
    targetPort: EngineeringPort,
): String? {
    val sourceSignal = sourcePort.symbolProperty("signal") ?: return null
    val targetSignal = targetPort.symbolProperty("signal") ?: return null
    if (sourceSignal != targetSignal) {
        return "Port `${sourcePort.id.value}` cannot connect to `${targetPort.id.value}` because their signal families differ."
    }
    val sourceDirection = sourcePort.symbolProperty("direction") ?: return null
    val targetDirection = targetPort.symbolProperty("direction") ?: return null
    if (!directionsAreCompatible(sourceDirection, targetDirection)) {
        return "Port `${sourcePort.id.value}` cannot connect to `${targetPort.id.value}` because their semantic directions are incompatible."
    }
    return null
}

private fun directionsAreCompatible(sourceDirection: String, targetDirection: String): Boolean {
    return when (sourceDirection.lowercase()) {
        "output", "out" -> targetDirection.lowercase() in setOf("input", "in", "passive", "bidirectional")
        "input", "in" -> targetDirection.lowercase() in setOf("output", "out", "passive", "bidirectional")
        "bidirectional" -> targetDirection.lowercase() in setOf("output", "out", "input", "in", "bidirectional")
        "passive" -> true
        else -> false
    }
}

private fun EngineeringPort.symbolProperty(name: String): String? {
    return properties
        .firstOrNull { property -> property.name == name }
        ?.value
        ?.let { value ->
            when (value) {
                is EngineeringPropertyValue.Symbol -> value.text
                is EngineeringPropertyValue.Text -> value.text
            }
        }
}

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
        is AthenaApplySemanticMacroBundleCommand -> {
            "{\"bundleId\":\"${bundle.bundleId.jsonEscaped()}\",\"previewId\":\"${bundle.previewId.value.jsonEscaped()}\",\"expansionId\":\"${bundle.acceptedExpansion.expansionId.value.jsonEscaped()}\",\"macroId\":\"${bundle.acceptedExpansion.origin.macroId.value.jsonEscaped()}\",\"instantiationId\":\"${bundle.acceptedExpansion.origin.instantiationId.value.jsonEscaped()}\",\"operationCount\":${bundle.operations.size}}"
        }
    }
}

private fun AthenaSemanticMacroCreateComponentOperation.toEngineeringProperties(): List<EngineeringProperty> {
    val properties = linkedMapOf<String, EngineeringPropertyValue>()
    properties["templateId"] = EngineeringPropertyValue.Symbol(templateId)
    implementationId?.let { implementation ->
        properties["implementationId"] = EngineeringPropertyValue.Symbol(implementation)
    }
    if (tags.isNotEmpty()) {
        properties["tags"] = EngineeringPropertyValue.Text(tags.toList().sorted().joinToString(","))
    }
    this.properties.toSortedMap().forEach { (name, value) ->
        properties[name] = value.toEngineeringPropertyValue()
    }
    return properties.entries.map { (name, value) -> EngineeringProperty(name, value) }
}

private fun SemanticMacroParameterValue.toEngineeringPropertyValue(): EngineeringPropertyValue {
    return when (this) {
        is SemanticMacroParameterValue.Text -> EngineeringPropertyValue.Text(text)
        is SemanticMacroParameterValue.Symbol -> EngineeringPropertyValue.Symbol(text)
        is SemanticMacroParameterValue.BooleanValue -> EngineeringPropertyValue.Symbol(value.toString())
        is SemanticMacroParameterValue.IntegerValue -> EngineeringPropertyValue.Text(value.toString())
    }
}
