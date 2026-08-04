package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationResult
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.CompilerRenderingBlocked
import com.engineeringood.athena.presentation.scopedToProjectionMembership
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.spatial.SpatialDocument

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
    val sheetSwitchTarget = resolveProjectionSheetSwitchTarget(viewId, compileActiveProject())
    val targetViewId = sheetSwitchTarget?.viewId ?: viewId
    val targetSheetId = sheetSwitchTarget?.sheetId
    if (targetViewId !in supportedViewIds) {
        return AthenaRuntimeProjectionSwitchRejected(
            projectName = project.name,
            requestedViewId = viewId,
            supportedViewIds = supportedViewIds,
            reason = "Runtime projection session does not support active view or sheet `$viewId` for project `${project.name}`.",
        )
    }
    if (targetViewId == currentSession.activeViewId && targetSheetId == currentActiveProjectionSheetId()) {
        return AthenaRuntimeProjectionSwitchSuccess(
            projectName = project.name,
            requestedViewId = viewId,
            session = currentSession,
        )
    }
    replaceActiveProjectionViewId(targetViewId)
    if (sheetSwitchTarget != null) {
        replaceActiveProjectionSheetId(targetSheetId)
    }
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
        supportedViews.firstOrNull { view -> view.viewId == "cabinet" }?.viewId
            ?: supportedViews.firstOrNull { view -> view.viewId == "documentation" }?.viewId
            ?: supportedViews.firstOrNull { view -> view.viewId == "schematic" }?.viewId
            ?: supportedViews.first().viewId
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
            val projection = compilation.authoredProjectionViews.firstOrNull { document -> document.view.id == viewId }
                ?: compilation.projections.firstOrNull { document -> document.view.id == viewId }
            val activeSheetId = projection?.let { candidate -> activeProjectionSheetId(candidate) }
            val presentationCandidates = compilation.presentations.filter { document -> document.view.id == viewId }
            val requiresSpatial = viewId == "schematic" ||
                presentationCandidates.any { document -> document.drawingComposition != null }
            val presentation = selectPresentationAuthority(
                candidates = presentationCandidates,
                activeSheetId = activeSheetId,
                authoritySheetId = { document -> document.drawingComposition?.sheetId },
                requireSheetAuthority = requiresSpatial,
            )
            val spatial = selectSpatialAuthority(
                candidates = compilation.spatialDocuments,
                activeSheetId = activeSheetId,
                authoritySheetIds = { document -> document.sheets.map { sheet -> sheet.sheetId } },
            )
            val rendering = compilation.rendering
            when {
                projection != null -> {
                    val activeProjection = projection.scopedToActiveSheet(activeSheetId)
                    val activePresentation = presentation?.scopedToProjection(activeProjection)
                    when {
                        requiresSpatial && spatial == null -> unavailableSpatialProjection(viewId, activeSheetId)
                        requiresSpatial && activePresentation == null -> unavailablePresentationProjection(viewId)
                        else -> {
                            val activeProjectionSheet = selectActiveProjectionSheet(
                                candidates = activeProjection.sheets,
                                activeSheetId = activeSheetId,
                                sheetId = { sheet -> sheet.sheetId.value },
                            )
                            val scene = if (spatial != null) {
                                activeProjection.toViewerScene(
                                    systemName = compilation.document.system.name,
                                    document = compilation.document,
                                    spatialDocument = spatial,
                                    activeSheetId = activeSheetId,
                                    placementOverrides = projectionPlacementOverrides(viewId),
                                )
                            } else {
                                AthenaRuntimeViewerScene(
                                    systemName = compilation.document.system.name,
                                    canvasWidth = 1,
                                    canvasHeight = 1,
                                    components = emptyList(),
                                    connections = emptyList(),
                                    labels = emptyList(),
                                )
                            }
                            AthenaRuntimeProjectionReadySnapshot(
                                viewId = viewId,
                                familyId = projection.view.familyContract.toRuntimeProjectionFamilyId(),
                                scene = scene,
                                presentation = activePresentation,
                                activeSheetId = activeSheetId,
                                sheets = projection.sheets.map { sheet -> sheet.toRuntimeProjectionSheet() },
                                notationPack = projection.notationPack?.toRuntimeProjectionNotationPack(),
                                crossReferences = projection.crossReferences.map { crossReference ->
                                    crossReference.toRuntimeProjectionCrossReference()
                                },
                                activeRenderContributions = activeProjectionRenderContributions(
                                    viewId = viewId,
                                    rendererTarget = GRAPH_WORKBENCH_RENDERER_TARGET,
                                ),
                                projectionRegionIds = activeProjectionSheet
                                    ?.regions
                                    ?.map { region -> region.regionId }
                                    .orEmpty(),
                                projectionConstructIds = activeProjectionSheet
                                    ?.constructs
                                    ?.map { construct -> construct.constructId.value }
                                    .orEmpty(),
                                spatialFacts = spatial?.toRuntimeSpatialFacts(viewId, activeSheetId),
                            )
                        }
                    }
                }

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

internal fun <T> selectSpatialAuthority(
    candidates: List<T>,
    activeSheetId: String?,
    authoritySheetIds: (T) -> List<String>,
): T? {
    if (activeSheetId == null) return null
    return candidates.singleOrNull { candidate -> activeSheetId in authoritySheetIds(candidate) }
}

internal fun <T> selectPresentationAuthority(
    candidates: List<T>,
    activeSheetId: String?,
    authoritySheetId: (T) -> String?,
    requireSheetAuthority: Boolean = false,
): T? {
    val sheetBackedCandidates = candidates.filter { candidate -> authoritySheetId(candidate) != null }
    return if (sheetBackedCandidates.isEmpty() && !requireSheetAuthority) {
        candidates.singleOrNull()
    } else {
        sheetBackedCandidates.singleOrNull { candidate -> authoritySheetId(candidate) == activeSheetId }
    }
}

internal fun <T> selectActiveProjectionSheet(
    candidates: List<T>,
    activeSheetId: String?,
    sheetId: (T) -> String,
): T? {
    if (activeSheetId == null) return null
    return candidates.singleOrNull { candidate -> sheetId(candidate) == activeSheetId }
}

private fun unavailableSpatialProjection(viewId: String, activeSheetId: String?): AthenaRuntimeProjectionUnavailableSnapshot {
    val reason = "Spatial Reality is unavailable for active Projection view `$viewId` and Sheet `${activeSheetId ?: "<missing>"}`."
    return AthenaRuntimeProjectionUnavailableSnapshot(
        viewId = viewId,
        reason = reason,
        diagnostics = listOf(
            AthenaRuntimeProjectionDiagnostic(
                severity = "error",
                code = "spatial.missing",
                message = "$reason Compile one canonical Spatial document before Presentation publication.",
            ),
        ),
    )
}

private fun unavailablePresentationProjection(viewId: String): AthenaRuntimeProjectionUnavailableSnapshot {
    val reason = "Presentation Reality is unavailable for active Projection view `$viewId`."
    return AthenaRuntimeProjectionUnavailableSnapshot(
        viewId = viewId,
        reason = reason,
        diagnostics = listOf(
            AthenaRuntimeProjectionDiagnostic(
                severity = "error",
                code = "presentation.missing",
                message = "$reason Preserve the compiler-owned Spatial-to-Presentation result.",
            ),
        ),
    )
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
    return success.projections
        .asSequence()
        .flatMap { projection ->
            projection.sheets.asSequence().map { sheet ->
                ProjectionSheetSwitchTarget(
                    viewId = projection.view.id,
                    sheetId = sheet.sheetId.value,
                )
            }
        }
        .firstOrNull { target -> target.sheetId == requestedId }
}

private fun AthenaExecutionContext.activeProjectionSheetId(
    projection: ProjectionDocument,
): String? {
    val storedSheetId = currentActiveProjectionSheetId()
    return if (storedSheetId != null && projection.sheets.any { sheet -> sheet.sheetId.value == storedSheetId }) {
        storedSheetId
    } else {
        projection.sheets.firstOrNull()?.sheetId?.value
    }
}

private fun ProjectionDocument.scopedToActiveSheet(
    activeSheetId: String?,
): ProjectionDocument {
    val activeSheet = activeSheetId?.let { selectedSheetId -> sheets.find { sheet -> sheet.sheetId.value == selectedSheetId } }
        ?: return this
    val nodeIds = activeSheet.subjects.flatMap { subject -> subject.nodeIds }.toSet()
    val connectionIds = activeSheet.subjects.flatMap { subject -> subject.connectionIds }.toSet()
    val activeSemanticIds = activeSheet.subjects.map { subject -> subject.semanticId }.toSet()
    return copy(
        nodes = nodes.filter { node -> node.projectionId in nodeIds },
        connections = connections.filter { connection -> connection.projectionId in connectionIds },
        resolvedSubjects = resolvedSubjects.filter { subject -> subject.semanticId in activeSemanticIds },
        notationPack = notationPack.let { pack ->
            pack?.copy(subjects = pack.subjects.filter { subject -> subject.semanticId in activeSemanticIds })
        },
    )
}

private fun com.engineeringood.athena.presentation.PresentationDocument.scopedToProjection(
    projection: ProjectionDocument,
): com.engineeringood.athena.presentation.PresentationDocument {
    val projectionIds = buildSet {
        addAll(projection.nodes.map { node -> node.projectionId.value })
        addAll(projection.connections.map { connection -> connection.projectionId.value })
    }
    val connectionSemanticIds = projection.connections.map { connection -> connection.semanticId.value }.toSet()
    val occurrenceSemanticIds = projection.nodes.map { node -> node.semanticId.value }.toSet()
    return scopedToProjectionMembership(
        sourceProjectionIds = projectionIds,
        connectionSemanticIds = connectionSemanticIds,
        occurrenceSemanticIds = occurrenceSemanticIds,
    )
}
