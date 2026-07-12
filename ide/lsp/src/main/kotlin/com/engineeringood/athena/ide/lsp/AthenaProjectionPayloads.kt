package com.engineeringood.athena.ide.lsp

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
    val familyId: String? = null,
    val ownershipContract: AthenaProjectionOwnershipContractPayload = AthenaProjectionOwnershipContractPayload(),
)

/**
 * Renderer-neutral projection ownership contract exposed through the Athena LSP boundary.
 */
data class AthenaProjectionOwnershipContractPayload(
    val interactivity: String = "inspect_only",
    val displayScopes: List<String> = emptyList(),
    val semanticCommandIds: List<String> = emptyList(),
    val projectionCommandIds: List<String> = emptyList(),
    val transientInteractionKinds: List<String> = emptyList(),
    val persistedProjectionMetadataKeys: List<String> = emptyList(),
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
    val familyId: String? = null,
    val systemName: String,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val activeSheetId: String? = null,
    val sheets: List<AthenaProjectionSheetPayload> = emptyList(),
    val notationPack: AthenaProjectionNotationPackPayload? = null,
    val crossReferences: List<AthenaProjectionCrossReferencePayload> = emptyList(),
    val activeRenderContributions: List<AthenaProjectionRenderContributionPayload>,
    val components: List<AthenaProjectionComponentPayload>,
    val connections: List<AthenaProjectionConnectionPayload>,
    val labels: List<AthenaProjectionLabelPayload>,
)

/**
 * One governed sheet summary exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetPayload(
    val sheetId: String,
    val displayName: String,
    val order: Int,
    val previousSheetId: String? = null,
    val nextSheetId: String? = null,
    val subjectSemanticIds: List<String> = emptyList(),
)

/**
 * One governed notation subject exposed through Athena LSP boundary.
 */
data class AthenaProjectionNotationSubjectPayload(
    val semanticId: String,
    val symbolKey: String,
    val labelPolicy: String,
    val markerKeys: List<String> = emptyList(),
)

/**
 * One governed notation pack exposed through Athena LSP boundary.
 */
data class AthenaProjectionNotationPackPayload(
    val packId: String,
    val displayName: String,
    val subjects: List<AthenaProjectionNotationSubjectPayload> = emptyList(),
)

/**
 * One repeated-reference summary exposed through the Athena LSP boundary.
 */
data class AthenaProjectionCrossReferencePayload(
    val semanticId: String,
    val kind: String,
    val sheetIds: List<String> = emptyList(),
    val occurrenceIds: List<String> = emptyList(),
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
    val projectionId: String,
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
    val projectionId: String,
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
    val projectionId: String,
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
