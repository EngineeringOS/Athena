package com.engineeringood.athena.composeruntime

/**
 * Deterministic workbench state consumed by the Compose shell.
 */
data class AthenaComposeShellState(
    val descriptor: AthenaComposeShellDescriptor = AthenaComposeShellDescriptor(),
    val workspaceName: String = "No workspace",
    val projectName: String = "No active project",
    val projectionSession: AthenaComposeProjectionSessionState? = null,
    val workspaceTreeItems: List<AthenaComposeWorkspaceTreeItem> = emptyList(),
    val sourceDocument: AthenaComposeSourceDocument? = null,
    val commandPanel: AthenaComposeCommandPanelState? = null,
    val inspectorGroups: List<AthenaComposeInspectorGroup> = emptyList(),
    val diagnosticsEntries: List<String> = listOf("No active diagnostics."),
    val consoleEntries: List<String> = listOf("Runtime shell ready."),
    val scene: AthenaSemanticViewerScene? = null,
)

/**
 * Viewer-facing projection-session metadata exposed to the Compose shell.
 */
data class AthenaComposeProjectionSessionState(
    val supportedViews: List<AthenaComposeProjectionViewState> = emptyList(),
    val activeViewId: String? = null,
    val activeViewDisplayName: String? = null,
    val activeProjectionAvailable: Boolean = false,
    val selectedSemanticId: String? = null,
    val selectedSemanticVisibleInActiveView: Boolean = false,
)

/**
 * One supported view option exposed to the Compose shell from runtime-owned contracts.
 */
data class AthenaComposeProjectionViewState(
    val viewId: String,
    val displayName: String,
    val description: String,
)

/**
 * Typed command panel state for the first GUI-backed semantic mutation proof.
 */
data class AthenaComposeCommandPanelState(
    val title: String = "Connect Ports",
    val sourcePortOptions: List<AthenaComposeCommandOption> = emptyList(),
    val selectedSourcePortSemanticId: String? = null,
    val targetPortOptions: List<AthenaComposeCommandOption> = emptyList(),
    val selectedTargetPortSemanticId: String? = null,
    val canExecute: Boolean = false,
    val actionLabel: String = "Connect Ports",
    val statusMessage: String = "Choose a source port to begin the runtime-backed GUI mutation path.",
)

/**
 * One command-option row shown in the first GUI mutation panel.
 */
data class AthenaComposeCommandOption(
    val semanticId: String,
    val label: String,
    val meta: String,
)

/**
 * Typed user intents emitted by the shell for the first GUI semantic mutation path.
 */
sealed interface AthenaComposeShellIntent {
    /**
     * Requests runtime-owned switching of the active desktop projection view.
     */
    data class SwitchProjectionView(val viewId: String) : AthenaComposeShellIntent

    /**
     * Updates the desktop-side inspected semantic identity from the active render surface.
     */
    data class SelectRenderedSemantic(val semanticId: String?) : AthenaComposeShellIntent

    /**
     * Selects the source port for the pending connect-ports command.
     */
    data class SelectSourcePort(val semanticId: String) : AthenaComposeShellIntent

    /**
     * Selects the target port for the pending connect-ports command.
     */
    data class SelectTargetPort(val semanticId: String) : AthenaComposeShellIntent

    /**
     * Executes the first GUI-backed connect-ports command.
     */
    data object ExecuteConnectPorts : AthenaComposeShellIntent
}

/**
 * One row in the workspace tree hierarchy.
 */
data class AthenaComposeWorkspaceTreeItem(
    val label: String,
    val depth: Int,
    val meta: String? = null,
    val isSelected: Boolean = false,
)

/**
 * Source-oriented text shown in the central authored pane.
 */
data class AthenaComposeSourceDocument(
    val path: String,
    val lines: List<AthenaComposeSourceLine>,
) {
    /**
     * Full source text reconstructed from numbered lines.
     */
    val text: String
        get() = lines.joinToString(separator = "\n") { line -> line.content }

    companion object {
        /**
         * Builds a numbered source document from authored text.
         */
        fun fromText(
            path: String,
            text: String,
        ): AthenaComposeSourceDocument {
            return AthenaComposeSourceDocument(
                path = path,
                lines = text.lines().mapIndexed { index, content ->
                    AthenaComposeSourceLine(
                        number = index + 1,
                        content = content,
                    )
                },
            )
        }
    }
}

/**
 * One numbered authored source line.
 */
data class AthenaComposeSourceLine(
    val number: Int,
    val content: String,
)

/**
 * One inspector group displayed in the right dock.
 */
data class AthenaComposeInspectorGroup(
    val title: String,
    val fields: List<AthenaComposeInspectorField>,
)

/**
 * One inspector field/value pair.
 */
data class AthenaComposeInspectorField(
    val label: String,
    val value: String,
)
