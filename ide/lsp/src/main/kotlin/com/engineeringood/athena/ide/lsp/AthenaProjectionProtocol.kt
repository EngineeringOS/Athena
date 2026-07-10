package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.runtime.AthenaRuntimeProjectionReadySnapshot
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionDiagnostic
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionRenderContribution
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSession
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSurfaceMapping
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSnapshot
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSwitchRejected
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSwitchSuccess
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionUnavailableSnapshot
import com.engineeringood.athena.runtime.AthenaRuntimeViewerComponentBox
import com.engineeringood.athena.runtime.AthenaRuntimeViewerConnectionLine
import com.engineeringood.athena.runtime.AthenaRuntimeViewerLabel
import com.engineeringood.athena.runtime.AthenaRuntimeViewerScene

/**
 * Parameters for the Athena-owned projection-session request.
 *
 * The initial M7 slice does not need caller-controlled filtering yet, but the request remains typed
 * so later graphical work can evolve the boundary without reverting to untyped maps.
 */
class AthenaProjectionSessionParams

/**
 * Read-only runtime-owned projection session payload returned through the Athena LSP boundary.
 */
data class AthenaProjectionSessionPayload(
    val projectName: String,
    val semanticPath: String,
    val activeViewId: String,
    val supportedViews: List<AthenaProjectionViewPayload>,
    val governedCommands: List<AthenaProjectionGovernedCommandPayload>,
    val status: String,
    val readyProjection: AthenaProjectionReadyPayload? = null,
    val unavailableReason: String? = null,
    val diagnostics: List<AthenaProjectionDiagnosticPayload> = emptyList(),
)

/**
 * One runtime-owned supported projection view exposed to IDE clients.
 */
data class AthenaProjectionViewPayload(
    val viewId: String,
    val displayName: String,
    val description: String,
)

/**
 * One inspectable governed projection command published through the Athena LSP boundary.
 */
data class AthenaProjectionGovernedCommandPayload(
    val commandId: String,
    val displayName: String,
    val description: String,
    val requiredArguments: List<String> = emptyList(),
)

/**
 * Successful runtime-owned projection data for the active view.
 */
data class AthenaProjectionReadyPayload(
    val viewId: String,
    val systemName: String,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val activeRenderContributions: List<AthenaProjectionRenderContributionPayload>,
    val components: List<AthenaProjectionComponentPayload>,
    val connections: List<AthenaProjectionConnectionPayload>,
    val labels: List<AthenaProjectionLabelPayload>,
)

/**
 * One active render contribution exposed through the Athena LSP boundary for the current graphical view.
 */
data class AthenaProjectionRenderContributionPayload(
    val pluginId: String,
    val contributionId: String,
    val displayName: String,
    val description: String,
    val rendererTarget: String,
    val surfaceMappings: List<AthenaProjectionSurfaceMappingPayload>,
)

/**
 * One downstream surface mapping exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSurfaceMappingPayload(
    val surface: String,
    val tokens: Map<String, String> = emptyMap(),
)

/**
 * One projection-facing component box derived from the runtime-owned scene.
 */
data class AthenaProjectionComponentPayload(
    val semanticId: String,
    val label: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/**
 * One projection-facing connection line derived from the runtime-owned scene.
 */
data class AthenaProjectionConnectionPayload(
    val semanticId: String,
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int,
)

/**
 * One projection-facing semantic label derived from the runtime-owned scene.
 */
data class AthenaProjectionLabelPayload(
    val semanticId: String,
    val label: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/**
 * One inspectable projection diagnostic exposed through the Athena LSP boundary.
 */
data class AthenaProjectionDiagnosticPayload(
    val severity: String,
    val code: String,
    val message: String,
    val provenance: String? = null,
)

/**
 * Parameters for one governed projection command request.
 */
data class AthenaProjectionCommandParams(
    val commandId: String,
    val viewId: String? = null,
)

/**
 * Result payload for one governed projection command request.
 */
data class AthenaProjectionCommandPayload(
    val commandId: String,
    val status: String,
    val reason: String? = null,
    val session: AthenaProjectionSessionPayload? = null,
)

internal fun AthenaLspSessionHostReady.toProjectionSessionPayload(
    snapshot: AthenaLspSessionSnapshot?,
    languageFeatures: AthenaLanguageFeatures? = null,
): AthenaProjectionSessionPayload {
    return currentProjectionSession(
        snapshot = snapshot,
        languageFeatures = languageFeatures,
    ).toPayload(
        semanticPath = snapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler",
    )
}

internal fun AthenaLspSessionHostReady.executeProjectionCommand(
    params: AthenaProjectionCommandParams,
    snapshot: AthenaLspSessionSnapshot?,
    languageFeatures: AthenaLanguageFeatures? = null,
): AthenaProjectionCommandPayload {
    if (params.commandId != SWITCH_ACTIVE_VIEW_COMMAND_ID) {
        return AthenaProjectionCommandPayload(
            commandId = params.commandId,
            status = "rejected",
            reason = "Projection command `${params.commandId}` is not in the Athena allowlist.",
        )
    }

    val requestedViewId = params.viewId
        ?: return AthenaProjectionCommandPayload(
            commandId = params.commandId,
            status = "rejected",
            reason = "Projection command `${params.commandId}` requires `viewId`.",
        )

    return when (val result = context.switchActiveProjectionView(requestedViewId)) {
        is AthenaRuntimeProjectionSwitchSuccess -> AthenaProjectionCommandPayload(
            commandId = params.commandId,
            status = "applied",
            session = currentProjectionSession(
                snapshot = snapshot,
                languageFeatures = languageFeatures,
            ).toPayload(
                semanticPath = snapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler",
            ),
        )

        is AthenaRuntimeProjectionSwitchRejected -> AthenaProjectionCommandPayload(
            commandId = params.commandId,
            status = "rejected",
            reason = result.reason,
            session = currentProjectionSession(
                snapshot = snapshot,
                languageFeatures = languageFeatures,
            ).toPayload(
                semanticPath = snapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler",
            ),
        )
    }
}

private fun AthenaLspSessionHostReady.currentProjectionSession(
    snapshot: AthenaLspSessionSnapshot?,
    languageFeatures: AthenaLanguageFeatures?,
): AthenaRuntimeProjectionSession {
    val sourcePath = snapshot?.sourcePath
    val trackedDocument = when {
        languageFeatures == null || sourcePath == null -> null
        snapshot.lastOpenedDocumentUri != null -> {
            languageFeatures.trackedDocument(snapshot.lastOpenedDocumentUri)
                ?.takeIf { tracked -> tracked.path.normalize() == sourcePath.normalize() }
        }

        else -> null
    } ?: sourcePath?.let { path -> languageFeatures?.trackedDocumentByPath(path) }
    return trackedDocument?.let { tracked ->
        context.previewProjectionSession(tracked.compilation)
    } ?: context.projectProjectionSession()
}

private fun AthenaRuntimeProjectionSession.toPayload(
    semanticPath: String,
): AthenaProjectionSessionPayload {
    val projection = activeProjection
    return AthenaProjectionSessionPayload(
        projectName = projectName,
        semanticPath = semanticPath,
        activeViewId = activeViewId,
        supportedViews = supportedViews.map { view ->
            AthenaProjectionViewPayload(
                viewId = view.viewId,
                displayName = view.displayName,
                description = view.description,
            )
        },
        governedCommands = projectionGovernedCommands(),
        status = projection.statusValue(),
        readyProjection = projection.toReadyPayload(),
        unavailableReason = projection.toUnavailableReason(),
        diagnostics = projection.toDiagnostics(),
    )
}

private fun AthenaRuntimeProjectionSnapshot.statusValue(): String {
    return when (this) {
        is AthenaRuntimeProjectionReadySnapshot -> "ready"
        is AthenaRuntimeProjectionUnavailableSnapshot -> "unavailable"
    }
}

private fun AthenaRuntimeProjectionSnapshot.toReadyPayload(): AthenaProjectionReadyPayload? {
    return when (this) {
        is AthenaRuntimeProjectionReadySnapshot -> scene.toPayload(
            viewId = viewId,
            activeRenderContributions = activeRenderContributions,
        )
        is AthenaRuntimeProjectionUnavailableSnapshot -> null
    }
}

private fun AthenaRuntimeProjectionSnapshot.toUnavailableReason(): String? {
    return when (this) {
        is AthenaRuntimeProjectionReadySnapshot -> null
        is AthenaRuntimeProjectionUnavailableSnapshot -> reason
    }
}

private fun AthenaRuntimeProjectionSnapshot.toDiagnostics(): List<AthenaProjectionDiagnosticPayload> {
    return when (this) {
        is AthenaRuntimeProjectionReadySnapshot -> emptyList()
        is AthenaRuntimeProjectionUnavailableSnapshot -> diagnostics.map(AthenaRuntimeProjectionDiagnostic::toPayload)
    }
}

private fun AthenaRuntimeProjectionDiagnostic.toPayload(): AthenaProjectionDiagnosticPayload {
    return AthenaProjectionDiagnosticPayload(
        severity = severity,
        code = code,
        message = message,
        provenance = provenance,
    )
}

private fun AthenaRuntimeViewerScene.toPayload(
    viewId: String,
    activeRenderContributions: List<AthenaRuntimeProjectionRenderContribution>,
): AthenaProjectionReadyPayload {
    return AthenaProjectionReadyPayload(
        viewId = viewId,
        systemName = systemName,
        canvasWidth = canvasWidth,
        canvasHeight = canvasHeight,
        activeRenderContributions = activeRenderContributions.map(AthenaRuntimeProjectionRenderContribution::toPayload),
        components = components.map(AthenaRuntimeViewerComponentBox::toPayload),
        connections = connections.map(AthenaRuntimeViewerConnectionLine::toPayload),
        labels = labels.map(AthenaRuntimeViewerLabel::toPayload),
    )
}

private fun AthenaRuntimeProjectionRenderContribution.toPayload(): AthenaProjectionRenderContributionPayload {
    return AthenaProjectionRenderContributionPayload(
        pluginId = pluginId,
        contributionId = contributionId,
        displayName = displayName,
        description = description,
        rendererTarget = rendererTarget,
        surfaceMappings = surfaceMappings.map(AthenaRuntimeProjectionSurfaceMapping::toPayload),
    )
}

private fun AthenaRuntimeProjectionSurfaceMapping.toPayload(): AthenaProjectionSurfaceMappingPayload {
    return AthenaProjectionSurfaceMappingPayload(
        surface = surface,
        tokens = tokens.toSortedMap(),
    )
}

private fun AthenaRuntimeViewerComponentBox.toPayload(): AthenaProjectionComponentPayload {
    return AthenaProjectionComponentPayload(
        semanticId = semanticId,
        label = label,
        x = x,
        y = y,
        width = width,
        height = height,
    )
}

private fun AthenaRuntimeViewerConnectionLine.toPayload(): AthenaProjectionConnectionPayload {
    return AthenaProjectionConnectionPayload(
        semanticId = semanticId,
        x1 = x1,
        y1 = y1,
        x2 = x2,
        y2 = y2,
    )
}

private fun AthenaRuntimeViewerLabel.toPayload(): AthenaProjectionLabelPayload {
    return AthenaProjectionLabelPayload(
        semanticId = semanticId,
        label = label,
        x = x,
        y = y,
        width = width,
        height = height,
    )
}

private fun projectionGovernedCommands(): List<AthenaProjectionGovernedCommandPayload> {
    return listOf(
        AthenaProjectionGovernedCommandPayload(
            commandId = SWITCH_ACTIVE_VIEW_COMMAND_ID,
            displayName = "Switch active view",
            description = "Switches the runtime-owned active projection view without opening a generic runtime tunnel.",
            requiredArguments = listOf("viewId"),
        ),
    )
}

private const val SWITCH_ACTIVE_VIEW_COMMAND_ID = "switch-active-view"
