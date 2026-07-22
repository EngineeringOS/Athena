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
    val presentation: AthenaPresentationDocumentPayload? = null,
    val activeSheetId: String? = null,
    val sheets: List<AthenaProjectionSheetPayload> = emptyList(),
    val sheetLayout: AthenaProjectionSheetLayoutPayload? = null,
    val notationPack: AthenaProjectionNotationPackPayload? = null,
    val crossReferences: List<AthenaProjectionCrossReferencePayload> = emptyList(),
    val electricalAnchors: List<AthenaProjectionElectricalAnchorPayload> = emptyList(),
    val electricalConnectionEndpoints: List<AthenaProjectionElectricalConnectionEndpointPayload> = emptyList(),
    val electricalRoutingCorridors: List<AthenaProjectionElectricalRoutingCorridorPayload> = emptyList(),
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
    val policyEvidence: AthenaProjectionSheetPolicyEvidencePayload? = null,
    val publication: AthenaProjectionSheetPublicationPayload = AthenaProjectionSheetPublicationPayload.fromProjectionState(
        sheetId = sheetId,
        displayName = displayName,
        order = order,
        subjectSemanticIds = subjectSemanticIds,
    ),
    val composition: AthenaProjectionSheetCompositionPayload = AthenaProjectionSheetCompositionPayload.fromProjectionState(
        sheetId = sheetId,
        displayName = displayName,
        order = order,
        subjectSemanticIds = subjectSemanticIds,
        publication = publication,
    ),
)

/**
 * Structured policy evidence for one governed sheet exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetPolicyEvidencePayload(
    val policyId: String,
    val policyVersion: String,
    val policyDeterministicIdentity: String,
    val sheetViewRole: String,
    val sheetViewRoleOrder: Int,
)

/**
 * One governed publication contract exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetPublicationPayload(
    val pageSize: AthenaProjectionSheetPageSizePayload,
    val frame: AthenaProjectionSheetFramePayload,
    val coordinateZones: List<AthenaProjectionSheetCoordinateZonePayload> = emptyList(),
    val titleBlock: AthenaProjectionSheetTitleBlockPayload,
    val revisionMetadata: AthenaProjectionSheetRevisionMetadataPayload,
    val viewComposition: AthenaProjectionSheetViewCompositionPayload,
) {
    companion object {
        fun fromProjectionState(
            sheetId: String,
            displayName: String,
            order: Int,
            subjectSemanticIds: List<String>,
        ): AthenaProjectionSheetPublicationPayload {
            val viewId = sheetId.substringBefore("/sheet/").ifBlank { sheetId }
            return AthenaProjectionSheetPublicationPayload(
                pageSize = AthenaProjectionSheetPageSizePayload(
                    format = "A3",
                    orientation = "landscape",
                ),
                frame = AthenaProjectionSheetFramePayload(
                    frameId = "engineering-sheet-frame",
                    style = "schematic",
                ),
                coordinateZones = listOf(
                    AthenaProjectionSheetCoordinateZonePayload(zoneId = "header", label = "Header", order = 0),
                    AthenaProjectionSheetCoordinateZonePayload(zoneId = "body", label = "Body", order = 1),
                    AthenaProjectionSheetCoordinateZonePayload(zoneId = "title-block", label = "Title Block", order = 2),
                ),
                titleBlock = AthenaProjectionSheetTitleBlockPayload(
                    sheetTitle = displayName,
                    sheetFamily = viewId,
                    sheetNumber = sheetId.substringAfterLast("/"),
                ),
                revisionMetadata = AthenaProjectionSheetRevisionMetadataPayload(
                    revisionCode = "A",
                    revisionNote = "Initial governed sheet publication",
                ),
                viewComposition = AthenaProjectionSheetViewCompositionPayload(
                    primaryViewId = viewId,
                    primarySheetOrder = order,
                    subjectSemanticIds = subjectSemanticIds,
                ),
            )
        }

        fun defaultFor(
            sheetId: String,
            displayName: String,
            order: Int,
            subjectSemanticIds: List<String>,
        ): AthenaProjectionSheetPublicationPayload {
            return fromProjectionState(
                sheetId = sheetId,
                displayName = displayName,
                order = order,
                subjectSemanticIds = subjectSemanticIds,
            )
        }
    }
}

/**
 * One governed sheet composition exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetCompositionPayload(
    val sheetId: String,
    val displayName: String,
    val order: Int,
    val subjectSemanticIds: List<String> = emptyList(),
    val representationFamilyId: String = "schematic-sheet",
    val publication: AthenaProjectionSheetPublicationPayload,
) {
    companion object {
        fun fromProjectionState(
            sheetId: String,
            displayName: String,
            order: Int,
            subjectSemanticIds: List<String>,
            representationFamilyId: String = "schematic-sheet",
            publication: AthenaProjectionSheetPublicationPayload = AthenaProjectionSheetPublicationPayload.fromProjectionState(
                sheetId = sheetId,
                displayName = displayName,
                order = order,
                subjectSemanticIds = subjectSemanticIds,
            ),
        ): AthenaProjectionSheetCompositionPayload {
            return AthenaProjectionSheetCompositionPayload(
                sheetId = sheetId,
                displayName = displayName,
                order = order,
                subjectSemanticIds = subjectSemanticIds,
                representationFamilyId = representationFamilyId,
                publication = publication,
            )
        }
    }
}

/**
 * One governed sheet layout exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetLayoutPayload(
    val sheetId: String,
    val displayName: String,
    val order: Int,
    val subjectSemanticIds: List<String> = emptyList(),
    val representationFamilyId: String = "schematic-sheet",
    val frame: AthenaProjectionSheetLayoutFramePayload,
    val placements: List<AthenaProjectionSheetLayoutPlacementPayload> = emptyList(),
    val routingGuidance: List<AthenaProjectionSheetLayoutRoutingGuidancePayload> = emptyList(),
    val labelLayouts: List<AthenaProjectionSheetLayoutLabelLayoutPayload> = emptyList(),
)

/**
 * One governed sheet layout frame exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetLayoutFramePayload(
    val canvasWidth: Int,
    val canvasHeight: Int,
    val gridMajorStep: Int = 120,
    val gridMinorStep: Int = 24,
)

/**
 * One governed sheet layout placement exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetLayoutPlacementPayload(
    val projectionId: String,
    val semanticId: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/**
 * One governed sheet layout routing guidance exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetLayoutRoutingGuidancePayload(
    val projectionConnectionId: String,
    val connectionSemanticId: String,
    val sourcePoint: AthenaProjectionPointPayload,
    val targetPoint: AthenaProjectionPointPayload,
    val routingStyle: String,
    val bendPoints: List<AthenaProjectionPointPayload> = emptyList(),
)

/**
 * One governed sheet layout label layout exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetLayoutLabelLayoutPayload(
    val projectionId: String,
    val semanticId: String,
    val label: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/**
 * One governed page size exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetPageSizePayload(
    val format: String,
    val orientation: String,
)

/**
 * One governed frame exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetFramePayload(
    val frameId: String,
    val style: String,
)

/**
 * One governed coordinate zone exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetCoordinateZonePayload(
    val zoneId: String,
    val label: String,
    val order: Int,
)

/**
 * One governed title block exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetTitleBlockPayload(
    val sheetTitle: String,
    val sheetFamily: String,
    val sheetNumber: String,
)

/**
 * One governed revision metadata block exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetRevisionMetadataPayload(
    val revisionCode: String,
    val revisionNote: String,
)

/**
 * One governed view composition exposed through the Athena LSP boundary.
 */
data class AthenaProjectionSheetViewCompositionPayload(
    val primaryViewId: String,
    val primarySheetOrder: Int,
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
    val crossReferenceId: String,
    val sheetIds: List<String> = emptyList(),
    val occurrenceIds: List<String> = emptyList(),
    val links: List<AthenaProjectionCrossReferenceLinkPayload> = emptyList(),
)

data class AthenaProjectionCrossReferenceLinkPayload(
    val semanticId: String,
    val sourceSheetId: String,
    val targetSheetId: String,
    val sourceOccurrenceId: String,
    val targetOccurrenceId: String,
    val compactNotation: String,
)

/**
 * One typed projection point exposed through the Athena LSP boundary.
 */
data class AthenaProjectionPointPayload(
    val x: Int,
    val y: Int,
)

/**
 * One typed electrical anchor occurrence exposed through the Athena LSP boundary.
 */
data class AthenaProjectionElectricalAnchorPayload(
    val anchorId: String,
    val portSemanticId: String,
    val ownerSemanticId: String,
    val nodeId: String,
    val labelId: String? = null,
    val x: Int,
    val y: Int,
    val side: String,
)

/**
 * One typed electrical connection endpoint occurrence exposed through the Athena LSP boundary.
 */
data class AthenaProjectionElectricalConnectionEndpointPayload(
    val endpointId: String,
    val projectionConnectionId: String,
    val connectionSemanticId: String,
    val endpointRole: String,
    val portSemanticId: String,
    val anchorId: String,
)

/**
 * One preferred electrical routing corridor exposed through the Athena LSP boundary.
 *
 * The corridor is guidance for downstream renderers only and does not become engineering truth.
 */
data class AthenaProjectionElectricalRoutingCorridorPayload(
    val corridorId: String,
    val projectionConnectionId: String,
    val connectionSemanticId: String,
    val sourceAnchorId: String,
    val targetAnchorId: String,
    val routingStyle: String,
    val preferredBendPoints: List<AthenaProjectionPointPayload> = emptyList(),
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
