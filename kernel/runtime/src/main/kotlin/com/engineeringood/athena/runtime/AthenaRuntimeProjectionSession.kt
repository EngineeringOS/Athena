package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.CompilerRenderingBlocked
import com.engineeringood.athena.semantics.core.SemanticDiagnostic

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
            familyId = definition.familyContract.toRuntimeProjectionFamilyId(),
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
    val supportedViewIds = currentSession.supportedViews.map { view -> view.viewId }
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
            val diagnostics = compilation.diagnostics.map { diagnostic -> diagnostic.toProjectionDiagnostic() }
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
                    familyId = projection.view.familyContract.toRuntimeProjectionFamilyId(),
                    scene = projection.toViewerScene(
                        systemName = compilation.document.system.name,
                        document = compilation.document,
                        placementOverrides = projectionPlacementOverrides(viewId),
                    ),
                    activeSheetId = projection.sheets.firstOrNull()?.sheetId?.value,
                    sheets = projection.sheets.map { sheet -> sheet.toRuntimeProjectionSheet() },
                    notationPack = projection.notationPack?.toRuntimeProjectionNotationPack(),
                    crossReferences = projection.crossReferences.map { crossReference ->
                        crossReference.toRuntimeProjectionCrossReference()
                    },
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
