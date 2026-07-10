package com.engineeringood.athena.apps.composeviewer
import com.engineeringood.athena.composeruntime.AthenaComposeCommandOption
import com.engineeringood.athena.composeruntime.AthenaComposeCommandPanelState
import com.engineeringood.athena.composeruntime.AthenaComposeInspectorField
import com.engineeringood.athena.composeruntime.AthenaComposeInspectorGroup
import com.engineeringood.athena.composeruntime.AthenaComposeProjectionSessionState
import com.engineeringood.athena.composeruntime.AthenaComposeProjectionViewState
import com.engineeringood.athena.composeruntime.AthenaComposeShellIntent
import com.engineeringood.athena.composeruntime.AthenaComposeShellState
import com.engineeringood.athena.composeruntime.AthenaComposeSourceDocument
import com.engineeringood.athena.composeruntime.AthenaComposeWorkspaceTreeItem
import com.engineeringood.athena.composeruntime.AthenaSemanticViewerComponentBox
import com.engineeringood.athena.composeruntime.AthenaSemanticViewerConnectionLine
import com.engineeringood.athena.composeruntime.AthenaSemanticViewerScene
import com.engineeringood.athena.runtime.AthenaCommandExecutionValidationFeedback
import com.engineeringood.athena.runtime.AthenaCommandExecutionRejected
import com.engineeringood.athena.runtime.AthenaCommandExecutionSuccess
import com.engineeringood.athena.runtime.AthenaCommandExecutionUnavailable
import com.engineeringood.athena.runtime.AthenaConnectPortsCommand
import com.engineeringood.athena.runtime.AthenaEngineeringGraphNodeKind
import com.engineeringood.athena.runtime.AthenaEngineeringGraphProjection
import com.engineeringood.athena.runtime.AthenaEngineeringGraphReadyProjection
import com.engineeringood.athena.runtime.AthenaEngineeringGraphReferenceKind
import com.engineeringood.athena.runtime.AthenaExecutionContext
import com.engineeringood.athena.runtime.AthenaRuntimeIncrementalUpdateReport
import com.engineeringood.athena.runtime.AthenaSemanticDiffInspection
import com.engineeringood.athena.runtime.AthenaRuntimePluginInspectorGroup
import com.engineeringood.athena.runtime.AthenaRuntimePluginViewContribution
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionReadySnapshot
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSession
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSnapshot
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSwitchRejected
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSwitchSuccess
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionUnavailableSnapshot
import com.engineeringood.athena.runtime.AthenaRuntime
import java.nio.file.Files
import java.nio.file.Path

/**
 * Runtime-backed app-layer session for the first GUI command proof in the Compose viewer.
 */
class AthenaComposeViewerWorkbenchSession private constructor(
    private val workspaceRoot: Path,
    private val sourcePath: Path,
    private val sourceText: String,
    private val context: AthenaExecutionContext,
) {
    private var selectedSourcePortSemanticId: String? = null
    private var selectedTargetPortSemanticId: String? = null
    private var selectedRenderedSemanticId: String? = null
    private var commandPanelStatusMessage: String =
        "Choose a source port to begin the runtime-backed GUI mutation path."
    private val consoleEntries: MutableList<String> = mutableListOf(
        "Runtime project activated: ${context.project.name}",
        "Workbench session ready for GUI -> command runtime -> canonical semantics.",
    )
    private var shellState: AthenaComposeShellState = rebuildShellState()

    /**
     * Returns the latest runtime-backed shell state exposed to the Compose workbench.
     */
    fun shellState(): AthenaComposeShellState = shellState

    /**
     * Dispatches one typed shell intent and returns the refreshed runtime-backed shell state.
     */
    fun dispatch(intent: AthenaComposeShellIntent): AthenaComposeShellState {
        when (intent) {
            is AthenaComposeShellIntent.SwitchProjectionView -> switchProjectionView(intent.viewId)
            is AthenaComposeShellIntent.SelectRenderedSemantic -> selectRenderedSemantic(intent.semanticId)
            is AthenaComposeShellIntent.SelectSourcePort -> selectSourcePort(intent.semanticId)
            is AthenaComposeShellIntent.SelectTargetPort -> selectTargetPort(intent.semanticId)
            AthenaComposeShellIntent.ExecuteConnectPorts -> executeConnectPorts()
        }
        shellState = rebuildShellState()
        return shellState
    }

    private fun switchProjectionView(viewId: String) {
        when (val result = context.switchActiveProjectionView(viewId)) {
            is AthenaRuntimeProjectionSwitchSuccess -> {
                consoleEntries += "GUI projection view switched: ${result.requestedViewId}"
            }

            is AthenaRuntimeProjectionSwitchRejected -> {
                consoleEntries += "GUI projection view rejected: ${result.reason}"
            }
        }
    }

    private fun selectRenderedSemantic(semanticId: String?) {
        selectedRenderedSemanticId = semanticId
        consoleEntries += if (semanticId == null) {
            "GUI semantic selection cleared."
        } else {
            "GUI semantic selection updated: $semanticId"
        }
    }

    private fun selectSourcePort(semanticId: String) {
        val candidates = currentPortCandidates()
        val sourceCandidate = candidates.firstOrNull { candidate ->
            candidate.semanticId == semanticId && candidate.direction == PORT_DIRECTION_OUT
        }
        if (sourceCandidate == null) {
            commandPanelStatusMessage = "Source port `$semanticId` is not available for GUI connect."
            consoleEntries += "GUI source selection rejected: $semanticId"
            selectedSourcePortSemanticId = null
            selectedTargetPortSemanticId = null
            return
        }

        selectedSourcePortSemanticId = sourceCandidate.semanticId
        val compatibleTargets = compatibleTargetCandidates(sourceCandidate, candidates)
        if (selectedTargetPortSemanticId !in compatibleTargets.map { it.semanticId }.toSet()) {
            selectedTargetPortSemanticId = null
        }
        commandPanelStatusMessage = if (compatibleTargets.isEmpty()) {
            "No compatible target ports are available for ${sourceCandidate.label}."
        } else {
            "Selected ${sourceCandidate.label}. Choose a compatible target port."
        }
        consoleEntries += "GUI source selected: ${sourceCandidate.label}"
    }

    private fun selectTargetPort(semanticId: String) {
        val sourceCandidate = selectedSourceCandidate()
        if (sourceCandidate == null) {
            commandPanelStatusMessage = "Choose a source port before selecting a target port."
            consoleEntries += "GUI target selection ignored until a source port is selected."
            return
        }

        val targetCandidate = compatibleTargetCandidates(sourceCandidate, currentPortCandidates())
            .firstOrNull { candidate -> candidate.semanticId == semanticId }
        if (targetCandidate == null) {
            commandPanelStatusMessage = "Target port `$semanticId` is not compatible with ${sourceCandidate.label}."
            consoleEntries += "GUI target selection rejected: $semanticId"
            selectedTargetPortSemanticId = null
            return
        }

        selectedTargetPortSemanticId = targetCandidate.semanticId
        commandPanelStatusMessage = "Ready to connect ${sourceCandidate.label} to ${targetCandidate.label}."
        consoleEntries += "GUI target selected: ${targetCandidate.label}"
    }

    private fun executeConnectPorts() {
        val sourceSemanticId = selectedSourcePortSemanticId
        val targetSemanticId = selectedTargetPortSemanticId
        if (sourceSemanticId == null || targetSemanticId == null) {
            commandPanelStatusMessage = "Select both a source port and a target port before connecting."
            consoleEntries += "GUI connect command blocked: source and target selection are both required."
            return
        }

        when (
            val result = context.commandRuntime().execute(
                context = context,
                command = AthenaConnectPortsCommand(
                    sourcePortSemanticId = sourceSemanticId,
                    targetPortSemanticId = targetSemanticId,
                ),
            )
        ) {
            is AthenaCommandExecutionSuccess -> {
                val commandId = context.commandRuntime().history(context).records.lastOrNull()?.commandId ?: "command-unknown"
                val connectionSemanticId = result.changedSemanticIds.firstOrNull { semanticId ->
                    semanticId.startsWith("connection:")
                } ?: "connection-created"
                commandPanelStatusMessage = "Created $connectionSemanticId through the runtime command path."
                currentIncrementalReport()?.let { report ->
                    consoleEntries +=
                        "GUI incremental refresh: validation=${report.validationMode} layout=${report.layoutMode} geometry=${report.geometryMode} rendering=${report.renderingMode} changed=${report.changedSemanticIds.size}"
                }
                currentDiffInspection()?.historyConsequences?.firstOrNull()?.let { consequence ->
                    consoleEntries += "GUI history consequence: ${consequence.commandId} ${consequence.status.name}"
                }
                consoleEntries += "GUI connect command accepted: $commandId created $connectionSemanticId"
                selectedSourcePortSemanticId = null
                selectedTargetPortSemanticId = null
            }

            is AthenaCommandExecutionRejected -> {
                commandPanelStatusMessage = result.reason
                consoleEntries += "GUI connect command rejected: ${result.reason}"
            }

            is AthenaCommandExecutionValidationFeedback -> {
                val feedbackMessage = result.validationFeedback.joinToString(separator = "; ") { feedback ->
                    "${feedback.severity.name.lowercase()}: ${feedback.message}"
                }
                commandPanelStatusMessage = feedbackMessage
                consoleEntries += "GUI connect command validation feedback: $feedbackMessage"
            }

            is AthenaCommandExecutionUnavailable -> {
                commandPanelStatusMessage = result.reason
                consoleEntries += "GUI connect command unavailable: ${result.reason}"
            }
        }
    }

    private fun rebuildShellState(): AthenaComposeShellState {
        val projectionSession = context.projectProjectionSession()
        val scene = projectionSession.activeProjection.toComposeSceneOrNull()
        val unavailableReason = projectionSession.activeProjection.unavailableReasonOrNull()
        val graphProjection = context.projectEngineeringGraphProjection()
        val history = context.commandRuntime().history(context)
        val latestInspection = currentDiffInspection()
        val pluginViewContributions = currentPluginViewContributions()
        val portCandidates = currentPortCandidates(graphProjection)
        val selectedSource = portCandidates.firstOrNull { candidate -> candidate.semanticId == selectedSourcePortSemanticId }
        val sourceOptions = portCandidates
            .filter { candidate -> candidate.direction == PORT_DIRECTION_OUT }
            .map(AthenaWorkbenchPortCandidate::toComposeOption)
        val targetCandidates = selectedSource?.let { compatibleTargetCandidates(it, portCandidates) }.orEmpty()
        if (selectedSource == null) {
            selectedSourcePortSemanticId = null
            selectedTargetPortSemanticId = null
        } else if (selectedTargetPortSemanticId !in targetCandidates.map { candidate -> candidate.semanticId }.toSet()) {
            selectedTargetPortSemanticId = null
        }

        return AthenaComposeShellState(
            descriptor = AthenaComposeViewerBootstrap.shellDescriptor(
                projectName = context.project.name,
                scene = scene,
                unavailableReason = unavailableReason,
            ),
            workspaceName = context.project.name,
            projectName = scene?.systemName ?: context.project.name,
            projectionSession = projectionSession.toComposeProjectionSessionState(
                selectedSemanticId = selectedRenderedSemanticId,
            ),
            workspaceTreeItems = workspaceTreeItems(
                graphProjection = graphProjection,
                sourcePath = workspaceRoot.relativize(sourcePath).toString().replace('\\', '/'),
                scene = scene,
            ),
            sourceDocument = AthenaComposeSourceDocument.fromText(
                path = workspaceRoot.relativize(sourcePath).toString().replace('\\', '/'),
                text = sourceText,
            ),
            commandPanel = AthenaComposeCommandPanelState(
                sourcePortOptions = sourceOptions,
                selectedSourcePortSemanticId = selectedSourcePortSemanticId,
                targetPortOptions = targetCandidates.map(AthenaWorkbenchPortCandidate::toComposeOption),
                selectedTargetPortSemanticId = selectedTargetPortSemanticId,
                canExecute = selectedSourcePortSemanticId != null && selectedTargetPortSemanticId != null,
                statusMessage = commandPanelStatusMessage,
            ),
            inspectorGroups = inspectorGroups(
                projectionSession = projectionSession,
                scene = scene,
                historyCount = history.records.size,
                latestInspection = latestInspection,
                pluginViewContributions = pluginViewContributions,
            ),
            diagnosticsEntries = diagnosticsEntries(
                unavailableReason = unavailableReason,
                latestInspection = latestInspection,
                pluginViewContributions = pluginViewContributions,
            ),
            consoleEntries = consoleEntries.toList(),
            scene = scene,
        )
    }

    private fun currentIncrementalReport(): AthenaRuntimeIncrementalUpdateReport? {
        return context.incrementalUpdateReport()
    }

    private fun currentDiffInspection(): AthenaSemanticDiffInspection? {
        return context.latestSemanticDiffInspection()
    }

    private fun currentPluginViewContributions(): List<AthenaRuntimePluginViewContribution> {
        return context.pluginRuntimeServices().viewContributions(context)
    }

    private fun diagnosticsEntries(
        unavailableReason: String?,
        latestInspection: AthenaSemanticDiffInspection?,
        pluginViewContributions: List<AthenaRuntimePluginViewContribution>,
    ): List<String> {
        return buildList {
            if (unavailableReason != null) {
                add(unavailableReason)
            }
            addAll(context.activeDiagnosticsMessages())
            pluginViewContributions.forEach { contribution ->
                addAll(contribution.diagnosticsEntries)
            }
            context.incrementalUpdateReport()?.let { report ->
                add(
                    "Incremental validation: ${report.validationMode} | layout: ${report.layoutMode} | geometry: ${report.geometryMode} | rendering: ${report.renderingMode} | changed ids: ${report.changedSemanticIds.size}",
                )
            }
            latestInspection?.let { inspection ->
                add("Semantic diff: ${inspection.summaryText()}")
            }
            if (isEmpty()) {
                add("No active diagnostics.")
            }
        }
    }

    private fun currentPortCandidates(): List<AthenaWorkbenchPortCandidate> {
        return currentPortCandidates(context.projectEngineeringGraphProjection())
    }

    private fun currentPortCandidates(graphProjection: AthenaEngineeringGraphProjection): List<AthenaWorkbenchPortCandidate> {
        if (graphProjection !is AthenaEngineeringGraphReadyProjection) {
            return emptyList()
        }

        val graph = graphProjection.graph
        return graph.nodesOfKind(AthenaEngineeringGraphNodeKind.PORT)
            .map { portNode ->
                val ownerSemanticId = portNode.references
                    .firstOrNull { reference -> reference.kind == AthenaEngineeringGraphReferenceKind.OWNER }
                    ?.resolvedSemanticId
                val ownerName = ownerSemanticId?.let { semanticId -> graph.node(semanticId)?.displayName }
                    ?: portNode.references.firstOrNull()?.authoredPath?.joinToString(".")
                    ?: "Unknown"
                AthenaWorkbenchPortCandidate(
                    semanticId = portNode.semanticId,
                    label = "$ownerName.${portNode.displayName}",
                    direction = portNode.properties.firstOrNull { property -> property.name == "direction" }?.value,
                    signal = portNode.properties.firstOrNull { property -> property.name == "signal" }?.value,
                )
            }
            .sortedBy { candidate -> candidate.label }
    }

    private fun selectedSourceCandidate(): AthenaWorkbenchPortCandidate? {
        return selectedSourcePortSemanticId?.let { selectedSemanticId ->
            currentPortCandidates().firstOrNull { candidate -> candidate.semanticId == selectedSemanticId }
        }
    }

    private fun compatibleTargetCandidates(
        sourceCandidate: AthenaWorkbenchPortCandidate,
        allCandidates: List<AthenaWorkbenchPortCandidate>,
    ): List<AthenaWorkbenchPortCandidate> {
        val connectedPairs = connectedPortPairs()
        return allCandidates.filter { candidate ->
            candidate.semanticId != sourceCandidate.semanticId &&
                candidate.direction == PORT_DIRECTION_IN &&
                candidate.signal != null &&
                candidate.signal == sourceCandidate.signal &&
                connectedPairs.none { (sourceSemanticId, targetSemanticId) ->
                    sourceSemanticId == sourceCandidate.semanticId && targetSemanticId == candidate.semanticId
                }
        }
    }

    private fun connectedPortPairs(): Set<Pair<String, String>> {
        val graphProjection = context.projectEngineeringGraphProjection()
        if (graphProjection !is AthenaEngineeringGraphReadyProjection) {
            return emptySet()
        }

        return graphProjection.graph.nodesOfKind(AthenaEngineeringGraphNodeKind.CONNECTION)
            .mapNotNull { connectionNode ->
                val sourceSemanticId = connectionNode.references
                    .firstOrNull { reference -> reference.kind == AthenaEngineeringGraphReferenceKind.CONNECTION_SOURCE }
                    ?.resolvedSemanticId
                val targetSemanticId = connectionNode.references
                    .firstOrNull { reference -> reference.kind == AthenaEngineeringGraphReferenceKind.CONNECTION_TARGET }
                    ?.resolvedSemanticId
                if (sourceSemanticId == null || targetSemanticId == null) {
                    null
                } else {
                    sourceSemanticId to targetSemanticId
                }
            }
            .toSet()
    }

    private fun workspaceTreeItems(
        graphProjection: AthenaEngineeringGraphProjection,
        sourcePath: String,
        scene: AthenaSemanticViewerScene?,
    ): List<AthenaComposeWorkspaceTreeItem> {
        val graph = (graphProjection as? AthenaEngineeringGraphReadyProjection)?.graph
        val components = graph?.nodesOfKind(AthenaEngineeringGraphNodeKind.COMPONENT).orEmpty()
        return buildList {
            add(AthenaComposeWorkspaceTreeItem(label = "Workspace", depth = 0))
            add(
                AthenaComposeWorkspaceTreeItem(
                    label = context.project.name,
                    depth = 1,
                    meta = "project",
                    isSelected = true,
                ),
            )
            add(
                AthenaComposeWorkspaceTreeItem(
                    label = sourcePath.substringAfterLast('/'),
                    depth = 1,
                    meta = "source",
                ),
            )
            add(
                AthenaComposeWorkspaceTreeItem(
                    label = scene?.systemName ?: context.project.name,
                    depth = 1,
                    meta = "render",
                ),
            )
            components.forEach { component ->
                add(
                    AthenaComposeWorkspaceTreeItem(
                        label = component.displayName,
                        depth = 2,
                        meta = "component",
                    ),
                )
            }
        }
    }

    private fun inspectorGroups(
        projectionSession: AthenaRuntimeProjectionSession,
        scene: AthenaSemanticViewerScene?,
        historyCount: Int,
        latestInspection: AthenaSemanticDiffInspection?,
        pluginViewContributions: List<AthenaRuntimePluginViewContribution>,
    ): List<AthenaComposeInspectorGroup> {
        val shellProjectionSession = projectionSession.toComposeProjectionSessionState(
            selectedSemanticId = selectedRenderedSemanticId,
        )
        return buildList {
            add(
            AthenaComposeInspectorGroup(
                title = "Project",
                fields = listOf(
                    AthenaComposeInspectorField("System", scene?.systemName ?: "Unavailable"),
                    AthenaComposeInspectorField("Project key", context.project.name),
                    AthenaComposeInspectorField(
                        "Source",
                        workspaceRoot.relativize(sourcePath).toString().replace('\\', '/'),
                    ),
                ),
            ),
            )
            add(
            AthenaComposeInspectorGroup(
                title = "Runtime view",
                fields = listOf(
                    AthenaComposeInspectorField("Active view", shellProjectionSession.activeViewDisplayName ?: "Unavailable"),
                    AthenaComposeInspectorField(
                        "Supported views",
                        shellProjectionSession.supportedViews.joinToString(separator = ", ") { view -> view.displayName },
                    ),
                    AthenaComposeInspectorField(
                        "Projection",
                        if (shellProjectionSession.activeProjectionAvailable) "Ready" else "Unavailable",
                    ),
                    AthenaComposeInspectorField("Components", scene?.componentCount?.toString() ?: "0"),
                    AthenaComposeInspectorField("Connections", scene?.connectionCount?.toString() ?: "0"),
                    AthenaComposeInspectorField("History", historyCount.toString()),
                    AthenaComposeInspectorField("GUI source", selectedSourcePortSemanticId ?: "None"),
                    AthenaComposeInspectorField("GUI target", selectedTargetPortSemanticId ?: "None"),
                    AthenaComposeInspectorField("Selection", shellProjectionSession.selectedSemanticId ?: "None"),
                    AthenaComposeInspectorField(
                        "Selection in active view",
                        if (shellProjectionSession.selectedSemanticVisibleInActiveView) "Yes" else "No",
                    ),
                ),
            ),
            )
            latestInspection?.let { inspection ->
                add(
                    AthenaComposeInspectorGroup(
                        title = "Latest change",
                        fields = listOf(
                            AthenaComposeInspectorField("Source", inspection.source.name),
                            AthenaComposeInspectorField("Commands", inspection.affectedCommandIds.joinToString(", ")),
                            AthenaComposeInspectorField("Diff", inspection.summaryText()),
                        ),
                    ),
                )
            }
            pluginViewContributions.forEach { contribution ->
                addAll(contribution.inspectorGroups.map(AthenaRuntimePluginInspectorGroup::toComposeInspectorGroup))
            }
        }
    }

    companion object {
        /**
         * Opens one workbench session for a runtime-managed project inside the supplied workspace root.
         */
        fun open(
            workspaceRoot: Path,
            projectName: String,
            sourcePath: Path,
        ): AthenaComposeViewerWorkbenchSession {
            val normalizedWorkspaceRoot = workspaceRoot.toAbsolutePath().normalize()
            val normalizedSourcePath = sourcePath.toAbsolutePath().normalize()
            require(normalizedSourcePath.startsWith(normalizedWorkspaceRoot)) {
                "Compose viewer bootstrap source must stay inside the runtime workspace root: $normalizedSourcePath"
            }
            require(Files.exists(normalizedSourcePath)) {
                "Compose viewer bootstrap source does not exist: $normalizedSourcePath"
            }
            require(Files.isRegularFile(normalizedSourcePath)) {
                "Compose viewer bootstrap source is not a file: $normalizedSourcePath"
            }

            val runtime = AthenaRuntime()
            val workspace = runtime.openWorkspace(normalizedWorkspaceRoot)
            val context = workspace.activateProject(
                projectName = projectName,
                sourcePath = normalizedSourcePath,
            )
            return AthenaComposeViewerWorkbenchSession(
                workspaceRoot = normalizedWorkspaceRoot,
                sourcePath = normalizedSourcePath,
                sourceText = Files.readString(normalizedSourcePath),
                context = context,
            )
        }
    }
}

/**
 * Internal app-layer port candidate used to derive one GUI connect command panel from runtime projections.
 */
private data class AthenaWorkbenchPortCandidate(
    val semanticId: String,
    val label: String,
    val direction: String?,
    val signal: String?,
) {
    /**
     * Converts one runtime-derived port candidate into a shell command option.
     */
    fun toComposeOption(): AthenaComposeCommandOption {
        return AthenaComposeCommandOption(
            semanticId = semanticId,
            label = label,
            meta = listOfNotNull(direction, signal).joinToString(separator = " | ").ifBlank { "Unspecified" },
        )
    }
}

/**
 * Converts one runtime projection snapshot into the read-only Compose semantic scene when available.
 */
private fun AthenaRuntimeProjectionSnapshot.toComposeSceneOrNull(): AthenaSemanticViewerScene? {
    return when (this) {
        is AthenaRuntimeProjectionReadySnapshot -> AthenaSemanticViewerScene(
            systemName = scene.systemName,
            canvasWidth = scene.canvasWidth,
            canvasHeight = scene.canvasHeight,
            components = scene.components.map { component ->
                AthenaSemanticViewerComponentBox(
                    semanticId = component.semanticId,
                    label = component.label,
                    x = component.x,
                    y = component.y,
                    width = component.width,
                    height = component.height,
                )
            },
            connections = scene.connections.map { connection ->
                AthenaSemanticViewerConnectionLine(
                    semanticId = connection.semanticId,
                    x1 = connection.x1,
                    y1 = connection.y1,
                    x2 = connection.x2,
                    y2 = connection.y2,
                )
            },
        )

        is AthenaRuntimeProjectionUnavailableSnapshot -> null
    }
}

/**
 * Converts one runtime projection session into the shell-facing projection-session contract.
 */
private fun AthenaRuntimeProjectionSession.toComposeProjectionSessionState(
    selectedSemanticId: String?,
): AthenaComposeProjectionSessionState {
    val scene = activeProjection.toComposeSceneOrNull()
    return AthenaComposeProjectionSessionState(
        supportedViews = supportedViews.map { view ->
            AthenaComposeProjectionViewState(
                viewId = view.viewId,
                displayName = view.displayName,
                description = view.description,
            )
        },
        activeViewId = activeViewId,
        activeViewDisplayName = supportedViews.firstOrNull { view -> view.viewId == activeViewId }?.displayName,
        activeProjectionAvailable = scene != null,
        selectedSemanticId = selectedSemanticId,
        selectedSemanticVisibleInActiveView = selectedSemanticId != null && scene?.containsSemanticId(selectedSemanticId) == true,
    )
}

/**
 * Returns the runtime projection unavailability reason when no semantic scene could be derived.
 */
private fun AthenaRuntimeProjectionSnapshot.unavailableReasonOrNull(): String? {
    return when (this) {
        is AthenaRuntimeProjectionReadySnapshot -> null
        is AthenaRuntimeProjectionUnavailableSnapshot -> reason
    }
}

private fun AthenaSemanticViewerScene.containsSemanticId(semanticId: String): Boolean {
    return components.any { component -> component.semanticId == semanticId } ||
        connections.any { connection -> connection.semanticId == semanticId }
}

private fun AthenaSemanticDiffInspection.summaryText(): String {
    val primaryEntry = entries.firstOrNull { entry -> entry.changeKind.name != "CONTEXT" } ?: entries.firstOrNull()
    val primarySummary = primaryEntry?.let { entry -> "${entry.changeKind.name} ${entry.semanticId}" } ?: "No semantic diff entries."
    val projectionSummary = projectionConsequences.joinToString(separator = ", ") { consequence ->
        buildString {
            append(consequence.layer.name.lowercase())
            append(" ")
            append(consequence.affectedViewIds.joinToString(separator = "/"))
            consequence.mode?.let { mode -> append(" ($mode)") }
        }
    }.ifBlank { "no projection consequences" }
    val linkedCommands = historyConsequences.joinToString(separator = ", ") { consequence ->
        "${consequence.commandId} ${consequence.status.name}"
    }.ifBlank { "no linked command history" }
    return "$primarySummary | projection: $projectionSummary | history: $linkedCommands"
}

private fun AthenaRuntimePluginInspectorGroup.toComposeInspectorGroup(): AthenaComposeInspectorGroup {
    return AthenaComposeInspectorGroup(
        title = title,
        fields = fields.map { field ->
            AthenaComposeInspectorField(
                label = field.label,
                value = field.value,
            )
        },
    )
}

private const val PORT_DIRECTION_IN = "in"
private const val PORT_DIRECTION_OUT = "out"
