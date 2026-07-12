package com.engineeringood.athena.runtime

import com.engineeringood.athena.layout.ProjectionOwnershipContract

/**
 * Runtime-owned view descriptor hosted inside one active projection session.
 */
data class AthenaRuntimeProjectionView(
    val viewId: String,
    val displayName: String,
    val description: String,
    val familyId: String? = null,
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
 * Runtime-owned governed sheet summary for the active projection snapshot.
 */
data class AthenaRuntimeProjectionSheet(
    val sheetId: String,
    val displayName: String,
    val order: Int,
    val previousSheetId: String? = null,
    val nextSheetId: String? = null,
    val subjectSemanticIds: List<String> = emptyList(),
)

/**
 * Runtime-owned governed notation selection for one canonical subject in active projection.
 */
data class AthenaRuntimeProjectionNotationSubject(
    val semanticId: String,
    val symbolKey: String,
    val labelPolicy: String,
    val markerKeys: List<String> = emptyList(),
)

/**
 * Runtime-owned governed notation pack attached to active projection snapshot.
 */
data class AthenaRuntimeProjectionNotationPack(
    val packId: String,
    val displayName: String,
    val subjects: List<AthenaRuntimeProjectionNotationSubject> = emptyList(),
)

/**
 * Runtime-owned repeated-reference summary for one canonical subject in active projection.
 */
data class AthenaRuntimeProjectionCrossReference(
    val semanticId: String,
    val kind: String,
    val sheetIds: List<String> = emptyList(),
    val occurrenceIds: List<String> = emptyList(),
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
    val familyId: String? = null,
    val scene: AthenaRuntimeViewerScene,
    val activeSheetId: String? = null,
    val sheets: List<AthenaRuntimeProjectionSheet> = emptyList(),
    val notationPack: AthenaRuntimeProjectionNotationPack? = null,
    val crossReferences: List<AthenaRuntimeProjectionCrossReference> = emptyList(),
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
