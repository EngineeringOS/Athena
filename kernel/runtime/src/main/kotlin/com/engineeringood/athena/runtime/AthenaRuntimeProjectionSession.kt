package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.CompilerSyntaxDiagnostic
import com.engineeringood.athena.projection.ProjectionDocument

internal fun AthenaExecutionContext.buildProjectionSession(): AthenaRuntimeProjectionSession =
    buildProjectionSession(compileActiveProject())

internal fun AthenaExecutionContext.buildProjectionSession(
    compilation: CompilerCompilationResult,
): AthenaRuntimeProjectionSession {
    val supportedViews = when (compilation) {
        is CompilerCompilationParseFailure -> emptyList()
        is CompilerCompilationSuccess -> compilation.projections.map { projection -> projection.view }.distinctBy { view -> view.id }
    }
    val activeViewId = selectActiveViewId(supportedViews.map { view -> view.id })
    activeViewId?.let(::replaceActiveProjectionViewId)
    return AthenaRuntimeProjectionSession(
        projectName = project.name,
        supportedViews = supportedViews,
        activeViewId = activeViewId,
        activeProjection = buildProjectionSnapshot(activeViewId, compilation),
    )
}

internal fun AthenaExecutionContext.switchProjectionView(viewId: String): AthenaRuntimeProjectionSwitchResult {
    val compilation = compileActiveProject()
    val session = projectProjectionSession()
    val sheetTarget = resolveProjectionSheetSwitchTarget(viewId, compilation)
    val targetViewId = sheetTarget?.viewId ?: viewId
    val supportedViewIds = session.supportedViews.map { view -> view.id }
    if (targetViewId !in supportedViewIds) {
        return AthenaRuntimeProjectionSwitchRejected(
            projectName = project.name,
            requestedViewId = viewId,
            supportedViewIds = supportedViewIds,
            reason = "Projection view or Sheet `$viewId` is unavailable for project `${project.name}`.",
        )
    }

    replaceActiveProjectionViewId(targetViewId)
    replaceActiveProjectionSheetId(sheetTarget?.sheetId)
    invalidateProjectionSession()
    return AthenaRuntimeProjectionSwitchSuccess(
        projectName = project.name,
        requestedViewId = viewId,
        session = projectProjectionSession(),
    )
}

private fun AthenaExecutionContext.selectActiveViewId(supportedViewIds: List<String>): String? {
    val current = currentActiveProjectionViewId()
    return current?.takeIf(supportedViewIds::contains) ?: supportedViewIds.firstOrNull()
}

private fun AthenaExecutionContext.buildProjectionSnapshot(
    viewId: String?,
    compilation: CompilerCompilationResult,
): AthenaRuntimeProjectionSnapshot = when (compilation) {
    is CompilerCompilationParseFailure -> AthenaRuntimeProjectionUnavailableSnapshot(
        viewId = viewId,
        reason = compilation.diagnostics.joinToString("; ") { diagnostic -> diagnostic.message },
        diagnostics = compilation.diagnostics.map(CompilerSyntaxDiagnostic::toRuntimeProjectionDiagnostic),
    )

    is CompilerCompilationSuccess -> {
        val projection = compilation.projections.firstOrNull { candidate -> candidate.view.id == viewId }
        if (projection == null) {
            AthenaRuntimeProjectionUnavailableSnapshot(
                viewId = viewId,
                reason = "Compiler produced no Projection Reality for `${viewId ?: "<none>"}`.",
            )
        } else {
            val activeSheetId = selectActiveSheetId(projection)
            AthenaRuntimeProjectionReadySnapshot(
                viewId = projection.view.id,
                projection = projection,
                activeSheetId = activeSheetId,
                spatialDocument = compilation.spatialDocuments.singleOrNull { spatial ->
                    activeSheetId != null && spatial.sheets.any { sheet -> sheet.sheetId == activeSheetId }
                },
            )
        }
    }
}

private fun AthenaExecutionContext.selectActiveSheetId(projection: ProjectionDocument): String? {
    val current = currentActiveProjectionSheetId()
    return current?.takeIf { sheetId -> projection.sheets.any { sheet -> sheet.sheetId.value == sheetId } }
        ?: projection.sheets.firstOrNull()?.sheetId?.value
}

private data class ProjectionSheetSwitchTarget(
    val viewId: String,
    val sheetId: String,
)

private fun resolveProjectionSheetSwitchTarget(
    requestedId: String,
    compilation: CompilerCompilationResult,
): ProjectionSheetSwitchTarget? {
    val success = compilation as? CompilerCompilationSuccess ?: return null
    return success.projections.asSequence().flatMap { projection ->
        projection.sheets.asSequence().map { sheet ->
            Triple(
                ProjectionSheetSwitchTarget(projection.view.id, sheet.sheetId.value),
                sheet.sheetId.value,
                sheet.displayName,
            )
        }
    }.firstOrNull { (_, sheetId, displayName) -> requestedId == sheetId || requestedId == displayName }
        ?.first
}

private fun CompilerSyntaxDiagnostic.toRuntimeProjectionDiagnostic(): AthenaRuntimeProjectionDiagnostic =
    AthenaRuntimeProjectionDiagnostic(
        severity = "error",
        code = "compiler.syntax",
        message = message,
        provenance = "$file:$line:$column",
    )
