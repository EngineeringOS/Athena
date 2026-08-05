package com.engineeringood.athena.runtime

import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.spatial.SpatialDocument

/** Runtime selection over compiler-owned Projection and Spatial facts. */
data class AthenaRuntimeProjectionSession(
    val projectName: String,
    val supportedViews: List<ViewDefinition>,
    val activeViewId: String?,
    val activeProjection: AthenaRuntimeProjectionSnapshot,
)

data class AthenaRuntimeProjectionDiagnostic(
    val severity: String,
    val code: String,
    val message: String,
    val provenance: String? = null,
)

sealed interface AthenaRuntimeProjectionSnapshot {
    val viewId: String?
}

data class AthenaRuntimeProjectionReadySnapshot(
    override val viewId: String,
    val projection: ProjectionDocument,
    val activeSheetId: String?,
    val spatialDocument: SpatialDocument?,
) : AthenaRuntimeProjectionSnapshot

data class AthenaRuntimeProjectionUnavailableSnapshot(
    override val viewId: String?,
    val reason: String,
    val diagnostics: List<AthenaRuntimeProjectionDiagnostic> = emptyList(),
) : AthenaRuntimeProjectionSnapshot

sealed interface AthenaRuntimeProjectionSwitchResult {
    val projectName: String
    val requestedViewId: String
}

data class AthenaRuntimeProjectionSwitchSuccess(
    override val projectName: String,
    override val requestedViewId: String,
    val session: AthenaRuntimeProjectionSession,
) : AthenaRuntimeProjectionSwitchResult

data class AthenaRuntimeProjectionSwitchRejected(
    override val projectName: String,
    override val requestedViewId: String,
    val supportedViewIds: List<String>,
    val reason: String,
) : AthenaRuntimeProjectionSwitchResult
