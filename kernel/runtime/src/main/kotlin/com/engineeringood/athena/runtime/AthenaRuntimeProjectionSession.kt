package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.CompilerRenderingBlocked
import com.engineeringood.athena.geometry.GeometryDocument
import com.engineeringood.athena.geometry.GeometryElement
import com.engineeringood.athena.geometry.GeometryPoint

/**
 * Runtime-owned view descriptor hosted inside one active projection session.
 */
data class AthenaRuntimeProjectionView(
    val viewId: String,
    val displayName: String,
    val description: String,
)

/**
 * Runtime-owned projection session for one active project.
 */
data class AthenaRuntimeProjectionSession(
    val projectName: String,
    val supportedViews: List<AthenaRuntimeProjectionView>,
    val activeViewId: String,
    val activeProjection: AthenaRuntimeProjectionSnapshot,
)

/**
 * Runtime-owned projection snapshot for one active view.
 */
sealed interface AthenaRuntimeProjectionSnapshot {
    /**
     * Active view identifier associated with the snapshot.
     */
    val viewId: String
}

/**
 * Successful runtime-owned snapshot for one active view.
 */
data class AthenaRuntimeProjectionReadySnapshot(
    override val viewId: String,
    val scene: AthenaRuntimeViewerScene,
) : AthenaRuntimeProjectionSnapshot

/**
 * Unavailable runtime-owned snapshot for one active view.
 */
data class AthenaRuntimeProjectionUnavailableSnapshot(
    override val viewId: String,
    val reason: String,
) : AthenaRuntimeProjectionSnapshot

/**
 * Runtime-owned result of attempting to switch the active projection view.
 */
sealed interface AthenaRuntimeProjectionSwitchResult {
    /**
     * Active project name associated with the switch attempt.
     */
    val projectName: String

    /**
     * Requested active view id for the switch attempt.
     */
    val requestedViewId: String
}

/**
 * Successful runtime-owned active-view switch.
 */
data class AthenaRuntimeProjectionSwitchSuccess(
    override val projectName: String,
    override val requestedViewId: String,
    val session: AthenaRuntimeProjectionSession,
) : AthenaRuntimeProjectionSwitchResult

/**
 * Rejected runtime-owned active-view switch for an unsupported view id.
 */
data class AthenaRuntimeProjectionSwitchRejected(
    override val projectName: String,
    override val requestedViewId: String,
    val supportedViewIds: List<String>,
    val reason: String,
) : AthenaRuntimeProjectionSwitchResult

/**
 * Builds the runtime-owned projection session for the active project.
 */
internal fun AthenaExecutionContext.buildProjectionSession(): AthenaRuntimeProjectionSession {
    val supportedViews = compiler().supportedViewDefinitions().map { definition ->
        AthenaRuntimeProjectionView(
            viewId = definition.id,
            displayName = definition.displayName,
            description = definition.description.orEmpty(),
        )
    }
    require(supportedViews.isNotEmpty()) {
        "Runtime projection session requires at least one supported view definition for project `${project.name}`."
    }
    val activeViewId = activeProjectionViewId(supportedViews)
    return AthenaRuntimeProjectionSession(
        projectName = project.name,
        supportedViews = supportedViews,
        activeViewId = activeViewId,
        activeProjection = buildProjectionSnapshot(
            viewId = activeViewId,
            compilation = compileActiveProject(),
        ),
    )
}

/**
 * Attempts to switch the runtime-owned active projection view for the active project.
 */
internal fun AthenaExecutionContext.switchProjectionView(viewId: String): AthenaRuntimeProjectionSwitchResult {
    val supportedViews = compiler().supportedViewDefinitions().map { definition ->
        AthenaRuntimeProjectionView(
            viewId = definition.id,
            displayName = definition.displayName,
            description = definition.description.orEmpty(),
        )
    }
    val supportedViewIds = supportedViews.map { view -> view.viewId }
    if (viewId !in supportedViewIds) {
        return AthenaRuntimeProjectionSwitchRejected(
            projectName = project.name,
            requestedViewId = viewId,
            supportedViewIds = supportedViewIds,
            reason = "Runtime projection session does not support active view `$viewId` for project `${project.name}`.",
        )
    }
    replaceActiveProjectionViewId(viewId)
    return AthenaRuntimeProjectionSwitchSuccess(
        projectName = project.name,
        requestedViewId = viewId,
        session = buildProjectionSession(),
    )
}

/**
 * Converts one runtime-owned projection snapshot into the legacy viewer projection surface.
 */
internal fun AthenaRuntimeProjectionSnapshot.toViewerProjection(projectName: String): AthenaRuntimeViewerProjection {
    return when (this) {
        is AthenaRuntimeProjectionReadySnapshot -> AthenaRuntimeViewerReadyProjection(
            projectName = projectName,
            scene = scene,
        )

        is AthenaRuntimeProjectionUnavailableSnapshot -> AthenaRuntimeViewerUnavailableProjection(
            projectName = projectName,
            reason = reason,
        )
    }
}

private fun AthenaExecutionContext.activeProjectionViewId(
    supportedViews: List<AthenaRuntimeProjectionView>,
): String {
    val storedViewId = currentActiveProjectionViewId()
    return if (storedViewId != null && supportedViews.any { view -> view.viewId == storedViewId }) {
        storedViewId
    } else {
        supportedViews.first().viewId
    }
}

private fun AthenaExecutionContext.buildProjectionSnapshot(
    viewId: String,
    compilation: com.engineeringood.athena.compiler.CompilerCompilationResult,
): AthenaRuntimeProjectionSnapshot {
    return when (compilation) {
        is CompilerCompilationParseFailure -> AthenaRuntimeProjectionUnavailableSnapshot(
            viewId = viewId,
            reason = compilation.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
        )

        is CompilerCompilationSuccess -> {
            val geometry = compilation.geometries.firstOrNull { document -> document.viewId == viewId }
            val rendering = compilation.rendering
            when {
                geometry != null -> AthenaRuntimeProjectionReadySnapshot(
                    viewId = viewId,
                    scene = geometry.toViewerScene(compilation.document.system.name),
                )

                rendering is CompilerRenderingBlocked -> AthenaRuntimeProjectionUnavailableSnapshot(
                    viewId = viewId,
                    reason = rendering.reason,
                )

                else -> AthenaRuntimeProjectionUnavailableSnapshot(
                    viewId = viewId,
                    reason = "No geometry-backed runtime projection is available for supported view `$viewId`.",
                )
            }
        }
    }
}

private fun GeometryDocument.toViewerScene(systemName: String): AthenaRuntimeViewerScene {
    return AthenaRuntimeViewerScene(
        systemName = systemName,
        canvasWidth = canvasWidth,
        canvasHeight = canvasHeight,
        components = elements
            .filter { element -> element.kind == com.engineeringood.athena.geometry.GeometryElementKind.BOX }
            .map { element ->
                AthenaRuntimeViewerComponentBox(
                    semanticId = element.semanticId.value,
                    label = element.label.orEmpty(),
                    x = element.bounds.x,
                    y = element.bounds.y,
                    width = element.bounds.width,
                    height = element.bounds.height,
                )
            },
        connections = elements
            .filter { element -> element.kind == com.engineeringood.athena.geometry.GeometryElementKind.PATH }
            .map { element ->
                val start = element.connectionStart()
                val end = element.connectionEnd()
                AthenaRuntimeViewerConnectionLine(
                    semanticId = element.semanticId.value,
                    x1 = start.x,
                    y1 = start.y,
                    x2 = end.x,
                    y2 = end.y,
                )
            },
    )
}

private fun GeometryElement.connectionStart(): GeometryPoint {
    return points.firstOrNull()
        ?: GeometryPoint(
            x = bounds.x,
            y = bounds.y + bounds.height / 2,
        )
}

private fun GeometryElement.connectionEnd(): GeometryPoint {
    return points.lastOrNull()
        ?: GeometryPoint(
            x = bounds.x + bounds.width,
            y = bounds.y + bounds.height / 2,
        )
}
