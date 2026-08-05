package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.runtime.AthenaRuntimeProjectionDiagnostic
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionReadySnapshot
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionSession
import com.engineeringood.athena.runtime.AthenaRuntimeProjectionUnavailableSnapshot
import java.nio.file.Files

internal fun AthenaLspSessionHostReady.toProjectionSessionPayload(
    snapshot: AthenaLspSessionSnapshot?,
    languageFeatures: AthenaLanguageFeatures? = null,
): AthenaProjectionSessionPayload = currentProjectionSession(snapshot, languageFeatures).toPayload(
    semanticPath = snapshot?.semanticPath ?: "frontend -> LSP -> runtime/compiler",
)

internal fun AthenaLspSessionHostReady.currentProjectionSession(
    snapshot: AthenaLspSessionSnapshot?,
    languageFeatures: AthenaLanguageFeatures?,
): AthenaRuntimeProjectionSession {
    val sourcePath = snapshot?.sourcePath
    val features = languageFeatures
    val trackedDocument = when {
        features == null || sourcePath == null -> null
        snapshot.lastOpenedDocumentUri != null -> features.trackedDocument(snapshot.lastOpenedDocumentUri)
            ?.takeIf { tracked -> tracked.path.toAbsolutePath().normalize().startsWith(snapshot.sourceRootPath.toAbsolutePath().normalize()) }
        else -> null
    } ?: sourcePath?.let { path -> features?.trackedDocumentByPath(path) }
        ?: sourcePath?.let { path -> features?.let { activeFeatures ->
            runCatching {
                activeFeatures.trackDocument(path.toUri().toString(), path, 0, Files.readString(path))
            }.getOrNull()
        } }
    return trackedDocument?.let { tracked -> context.previewProjectionSession(tracked.compilation) }
        ?: context.projectProjectionSession()
}

internal fun AthenaRuntimeProjectionSession.toPayload(semanticPath: String): AthenaProjectionSessionPayload {
    val active = activeProjection
    return AthenaProjectionSessionPayload(
        projectName = projectName,
        semanticPath = semanticPath,
        activeViewId = activeViewId,
        supportedViews = supportedViews.map { view ->
            AthenaProjectionViewPayload(view.id, view.displayName, view.description)
        },
        status = when (active) {
            is AthenaRuntimeProjectionReadySnapshot -> "ready"
            is AthenaRuntimeProjectionUnavailableSnapshot -> "unavailable"
        },
        projection = (active as? AthenaRuntimeProjectionReadySnapshot)?.toProjectionPayload(),
        spatial = (active as? AthenaRuntimeProjectionReadySnapshot)?.spatialDocument?.let { document ->
            AthenaSpatialDocumentSummaryPayload(
                sheets = document.sheets.map { sheet ->
                    AthenaSpatialSheetSummaryPayload(
                        sheetId = sheet.sheetId,
                        occurrenceCount = sheet.occurrences.size,
                        regionCount = sheet.regions.size,
                        constructCount = sheet.constructs.size,
                        anchorCount = sheet.anchors.size,
                        routeCount = sheet.routes.size,
                        gridReferenceCount = sheet.gridReferences.size,
                    )
                },
            )
        },
        unavailableReason = (active as? AthenaRuntimeProjectionUnavailableSnapshot)?.reason,
        diagnostics = (active as? AthenaRuntimeProjectionUnavailableSnapshot)?.diagnostics.orEmpty()
            .map(AthenaRuntimeProjectionDiagnostic::toPayload),
    )
}

private fun AthenaRuntimeProjectionReadySnapshot.toProjectionPayload(): AthenaProjectionDocumentPayload =
    AthenaProjectionDocumentPayload(
        viewId = viewId,
        activeSheetId = activeSheetId,
        nodes = projection.nodes.map { node ->
            AthenaProjectionNodePayload(node.projectionId.value, node.semanticId.value, node.label)
        },
        connections = projection.connections.map { connection ->
            AthenaProjectionConnectionPayload(
                projectionId = connection.projectionId.value,
                semanticId = connection.semanticId.value,
                sourceOccurrenceId = connection.source?.occurrencePortId?.occurrenceId?.value,
                sourcePortId = connection.source?.occurrencePortId?.portId?.value,
                targetOccurrenceId = connection.target?.occurrencePortId?.occurrenceId?.value,
                targetPortId = connection.target?.occurrencePortId?.portId?.value,
            )
        },
        sheets = projection.sheets.map { sheet ->
            AthenaProjectionSheetPayload(
                sheetId = sheet.sheetId.value,
                displayName = sheet.displayName,
                order = sheet.order,
                subjectSemanticIds = sheet.subjects.map { subject -> subject.semanticId.value },
            )
        },
    )

private fun AthenaRuntimeProjectionDiagnostic.toPayload(): AthenaProjectionDiagnosticPayload =
    AthenaProjectionDiagnosticPayload(severity, code, message, provenance)
