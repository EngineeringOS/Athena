package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.CompilerSyntaxDiagnostic
import com.engineeringood.athena.compiler.CompilerRenderingBlocked
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.layout.ProjectionOwnershipContract
import com.engineeringood.athena.plugin.AthenaRenderSurfaceMapping
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionLabel
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.semantics.core.SemanticDiagnostic

/**
 * Runtime-owned view descriptor hosted inside one active projection session.
 */
data class AthenaRuntimeProjectionView(
    val viewId: String,
    val displayName: String,
    val description: String,
    val ownershipContract: ProjectionOwnershipContract = ProjectionOwnershipContract(),
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
 * Inspectable runtime-owned diagnostic attached to an unavailable projection snapshot.
 */
data class AthenaRuntimeProjectionDiagnostic(
    val severity: String,
    val code: String,
    val message: String,
    val provenance: String? = null,
)

/**
 * Runtime-owned downstream surface mapping for one active graphical projection contribution.
 */
data class AthenaRuntimeProjectionSurfaceMapping(
    val surface: String,
    val tokens: Map<String, String> = emptyMap(),
)

/**
 * Runtime-owned active render contribution attached to one graphical projection snapshot.
 */
data class AthenaRuntimeProjectionRenderContribution(
    val pluginId: String,
    val contributionId: String,
    val displayName: String,
    val description: String,
    val rendererTarget: String,
    val surfaceMappings: List<AthenaRuntimeProjectionSurfaceMapping> = emptyList(),
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
    val activeRenderContributions: List<AthenaRuntimeProjectionRenderContribution> = emptyList(),
) : AthenaRuntimeProjectionSnapshot

/**
 * Unavailable runtime-owned snapshot for one active view.
 */
data class AthenaRuntimeProjectionUnavailableSnapshot(
    override val viewId: String,
    val reason: String,
    val diagnostics: List<AthenaRuntimeProjectionDiagnostic> = emptyList(),
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
    return buildProjectionSession(compilation = compileActiveProject())
}

/**
 * Builds the runtime-owned projection session for the active project from one supplied compilation result.
 */
internal fun AthenaExecutionContext.buildProjectionSession(
    compilation: CompilerCompilationResult,
): AthenaRuntimeProjectionSession {
    val supportedViews = compiler().supportedViewDefinitions().map { definition ->
        AthenaRuntimeProjectionView(
            viewId = definition.id,
            displayName = definition.displayName,
            description = definition.description.orEmpty(),
            ownershipContract = definition.ownershipContract,
        )
    }
    require(supportedViews.isNotEmpty()) {
        "Runtime projection session requires at least one supported view definition for project `${project.name}`."
    }
    val activeViewId = activeProjectionViewId(supportedViews)
    replaceActiveProjectionViewId(activeViewId)
    return AthenaRuntimeProjectionSession(
        projectName = project.name,
        supportedViews = supportedViews,
        activeViewId = activeViewId,
        activeProjection = buildProjectionSnapshot(
            viewId = activeViewId,
            compilation = compilation,
        ),
    )
}

/**
 * Attempts to switch the runtime-owned active projection view for the active project.
 */
internal fun AthenaExecutionContext.switchProjectionView(viewId: String): AthenaRuntimeProjectionSwitchResult {
    val currentSession = projectProjectionSession()
    val supportedViews = currentSession.supportedViews
    val supportedViewIds = supportedViews.map { view -> view.viewId }
    if (viewId !in supportedViewIds) {
        return AthenaRuntimeProjectionSwitchRejected(
            projectName = project.name,
            requestedViewId = viewId,
            supportedViewIds = supportedViewIds,
            reason = "Runtime projection session does not support active view `$viewId` for project `${project.name}`.",
        )
    }
    if (viewId == currentSession.activeViewId) {
        return AthenaRuntimeProjectionSwitchSuccess(
            projectName = project.name,
            requestedViewId = viewId,
            session = currentSession,
        )
    }
    replaceActiveProjectionViewId(viewId)
    invalidateProjectionSession()
    return AthenaRuntimeProjectionSwitchSuccess(
        projectName = project.name,
        requestedViewId = viewId,
        session = projectProjectionSession(),
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
    compilation: CompilerCompilationResult,
): AthenaRuntimeProjectionSnapshot {
    return when (compilation) {
        is CompilerCompilationParseFailure -> {
            val diagnostics = compilation.diagnostics.map(CompilerSyntaxDiagnostic::toProjectionDiagnostic)
            AthenaRuntimeProjectionUnavailableSnapshot(
                viewId = viewId,
                reason = diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
                diagnostics = diagnostics,
            )
        }

        is CompilerCompilationSuccess -> {
            val projection = compilation.projections.firstOrNull { document -> document.view.id == viewId }
            val rendering = compilation.rendering
            when {
                projection != null -> AthenaRuntimeProjectionReadySnapshot(
                    viewId = viewId,
                    scene = projection.toViewerScene(
                        systemName = compilation.document.system.name,
                        document = compilation.document,
                        placementOverrides = projectionPlacementOverrides(viewId),
                    ),
                    activeRenderContributions = activeProjectionRenderContributions(
                        viewId = viewId,
                        rendererTarget = GRAPH_WORKBENCH_RENDERER_TARGET,
                    ),
                )

                rendering is CompilerRenderingBlocked -> {
                    val diagnostics = compilation.semanticResult.diagnostics.map(SemanticDiagnostic::toProjectionDiagnostic)
                        .ifEmpty {
                            listOf(
                                AthenaRuntimeProjectionDiagnostic(
                                    severity = "error",
                                    code = "rendering.blocked",
                                    message = rendering.reason,
                                ),
                            )
                        }
                    AthenaRuntimeProjectionUnavailableSnapshot(
                        viewId = viewId,
                        reason = rendering.reason,
                        diagnostics = diagnostics,
                    )
                }

                else -> {
                    val reason = "No geometry-backed runtime projection is available for supported view `$viewId`."
                    AthenaRuntimeProjectionUnavailableSnapshot(
                        viewId = viewId,
                        reason = reason,
                        diagnostics = listOf(
                            AthenaRuntimeProjectionDiagnostic(
                                severity = "error",
                                code = "projection.geometry-unavailable",
                                message = reason,
                            ),
                        ),
                    )
                }
            }
        }
    }
}

private fun CompilerSyntaxDiagnostic.toProjectionDiagnostic(): AthenaRuntimeProjectionDiagnostic {
    return AthenaRuntimeProjectionDiagnostic(
        severity = "error",
        code = "compiler.syntax",
        message = message,
        provenance = "$file:$line:$column",
    )
}

private fun SemanticDiagnostic.toProjectionDiagnostic(): AthenaRuntimeProjectionDiagnostic {
    return AthenaRuntimeProjectionDiagnostic(
        severity = severity.name.lowercase(),
        code = ruleId.value,
        message = message,
        provenance = "${provenance.file}:${provenance.startLine}:${provenance.startColumn}",
    )
}

private fun AthenaExecutionContext.activeProjectionRenderContributions(
    viewId: String,
    rendererTarget: String,
): List<AthenaRuntimeProjectionRenderContribution> {
    return pluginRuntimeServices().renderContributions().flatMap { contributionSet ->
        contributionSet.renderContributions.mapNotNull { contribution ->
            val supportsView = contribution.viewIds.isEmpty() || viewId in contribution.viewIds
            val supportsTarget = contribution.rendererTargets.isEmpty() || rendererTarget in contribution.rendererTargets
            if (!supportsView || !supportsTarget) {
                null
            } else {
                AthenaRuntimeProjectionRenderContribution(
                    pluginId = contributionSet.pluginId,
                    contributionId = contribution.contributionId,
                    displayName = contribution.displayName,
                    description = contribution.description,
                    rendererTarget = rendererTarget,
                    surfaceMappings = contribution.surfaceMappings.map(AthenaRenderSurfaceMapping::toRuntimeProjectionSurfaceMapping),
                )
            }
        }
    }
}

private fun AthenaRenderSurfaceMapping.toRuntimeProjectionSurfaceMapping(): AthenaRuntimeProjectionSurfaceMapping {
    return AthenaRuntimeProjectionSurfaceMapping(
        surface = surface.name.lowercase(),
        tokens = tokens.toSortedMap(),
    )
}

private fun ProjectionDocument.toViewerScene(
    systemName: String,
    document: EngineeringDocument,
    placementOverrides: Map<String, AthenaGraphPlacement> = emptyMap(),
): AthenaRuntimeViewerScene {
    return AthenaRuntimeViewerScene(
        systemName = systemName,
        canvasWidth = canvasWidth,
        canvasHeight = canvasHeight,
        components = nodes.map(ProjectionNode::toViewerComponent),
        connections = connections.map(ProjectionConnection::toViewerConnection),
        labels = labels.map(ProjectionLabel::toViewerLabel),
    ).withPlacementOverrides(
        document = document,
        placementOverrides = placementOverrides,
    )
}

private const val GRAPH_WORKBENCH_RENDERER_TARGET = "graph-workbench"

private fun ProjectionNode.toViewerComponent(): AthenaRuntimeViewerComponentBox {
    return AthenaRuntimeViewerComponentBox(
        semanticId = semanticId.value,
        label = label,
        x = bounds.x,
        y = bounds.y,
        width = bounds.width,
        height = bounds.height,
    )
}

private fun ProjectionConnection.toViewerConnection(): AthenaRuntimeViewerConnectionLine {
    return AthenaRuntimeViewerConnectionLine(
        semanticId = semanticId.value,
        x1 = start.x,
        y1 = start.y,
        x2 = end.x,
        y2 = end.y,
    )
}

private fun ProjectionLabel.toViewerLabel(): AthenaRuntimeViewerLabel {
    return AthenaRuntimeViewerLabel(
        semanticId = semanticId.value,
        label = label,
        x = bounds.x,
        y = bounds.y,
        width = bounds.width,
        height = bounds.height,
    )
}

private fun AthenaRuntimeViewerScene.withPlacementOverrides(
    document: EngineeringDocument,
    placementOverrides: Map<String, AthenaGraphPlacement>,
): AthenaRuntimeViewerScene {
    if (placementOverrides.isEmpty()) {
        return this
    }

    val componentDeltas = components.mapNotNull { component ->
        val override = placementOverrides[component.semanticId] ?: return@mapNotNull null
        component.semanticId to ProjectionDelta(
            deltaX = override.x - component.x,
            deltaY = override.y - component.y,
        )
    }.toMap()
    if (componentDeltas.isEmpty()) {
        return this
    }

    val ownerByPortSemanticId = document.ports.associate { port ->
        port.id.value to port.ownerReference.resolvedIdentity?.value
    }
    val connectionsBySemanticId = document.connections.associateBy { connection -> connection.id.value }

    return copy(
        components = components.map { component ->
            placementOverrides[component.semanticId]?.let { override ->
                component.copy(
                    x = override.x,
                    y = override.y,
                )
            } ?: component
        },
        labels = labels.map { label ->
            val ownerSemanticId = ownerByPortSemanticId[label.semanticId]
            val delta = ownerSemanticId?.let(componentDeltas::get)
            if (delta == null) {
                label
            } else {
                label.copy(
                    x = label.x + delta.deltaX,
                    y = label.y + delta.deltaY,
                )
            }
        },
        connections = connections.map { connection ->
            val engineeringConnection = connectionsBySemanticId[connection.semanticId]
            val sourceOwnerSemanticId = engineeringConnection?.from?.resolvedIdentity?.value?.let(ownerByPortSemanticId::get)
            val targetOwnerSemanticId = engineeringConnection?.to?.resolvedIdentity?.value?.let(ownerByPortSemanticId::get)
            val sourceDelta = sourceOwnerSemanticId?.let(componentDeltas::get)
            val targetDelta = targetOwnerSemanticId?.let(componentDeltas::get)
            if (sourceDelta == null && targetDelta == null) {
                connection
            } else {
                connection.copy(
                    x1 = connection.x1 + (sourceDelta?.deltaX ?: 0),
                    y1 = connection.y1 + (sourceDelta?.deltaY ?: 0),
                    x2 = connection.x2 + (targetDelta?.deltaX ?: 0),
                    y2 = connection.y2 + (targetDelta?.deltaY ?: 0),
                )
            }
        },
    )
}

private data class ProjectionDelta(
    val deltaX: Int,
    val deltaY: Int,
)
