package com.engineeringood.athena.runtime

import com.engineeringood.athena.layout.ProjectionOwnershipContract
import com.engineeringood.athena.presentation.PresentationDocument

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
data class AthenaRuntimeProjectionSheetPublication(
    val pageSize: AthenaRuntimeProjectionSheetPageSize,
    val frame: AthenaRuntimeProjectionSheetFrame,
    val coordinateZones: List<AthenaRuntimeProjectionSheetCoordinateZone> = emptyList(),
    val titleBlock: AthenaRuntimeProjectionSheetTitleBlock,
    val revisionMetadata: AthenaRuntimeProjectionSheetRevisionMetadata,
    val viewComposition: AthenaRuntimeProjectionSheetViewComposition,
) {
    companion object {
        fun fromProjectionState(
            sheetId: String,
            displayName: String,
            order: Int,
            subjectSemanticIds: List<String>,
        ): AthenaRuntimeProjectionSheetPublication {
            val viewId = sheetId.substringBefore("/sheet/").ifBlank { sheetId }
            return AthenaRuntimeProjectionSheetPublication(
                pageSize = AthenaRuntimeProjectionSheetPageSize(format = "A3", orientation = "landscape"),
                frame = AthenaRuntimeProjectionSheetFrame(
                    frameId = "engineering-sheet-frame",
                    style = "schematic",
                ),
                coordinateZones = listOf(
                    AthenaRuntimeProjectionSheetCoordinateZone(zoneId = "header", label = "Header", order = 0),
                    AthenaRuntimeProjectionSheetCoordinateZone(zoneId = "body", label = "Body", order = 1),
                    AthenaRuntimeProjectionSheetCoordinateZone(zoneId = "title-block", label = "Title Block", order = 2),
                ),
                titleBlock = AthenaRuntimeProjectionSheetTitleBlock(
                    sheetTitle = displayName,
                    sheetFamily = viewId,
                    sheetNumber = sheetId.substringAfterLast("/"),
                ),
                revisionMetadata = AthenaRuntimeProjectionSheetRevisionMetadata(
                    revisionCode = "A",
                    revisionNote = "Initial governed sheet publication",
                ),
                viewComposition = AthenaRuntimeProjectionSheetViewComposition(
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
        ): AthenaRuntimeProjectionSheetPublication {
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
 * Runtime-owned governed sheet composition for the active projection snapshot.
 */
data class AthenaRuntimeProjectionSheetComposition(
    val sheetId: String,
    val displayName: String,
    val order: Int,
    val subjectSemanticIds: List<String> = emptyList(),
    val representationFamilyId: String = "schematic-sheet",
    val publication: AthenaRuntimeProjectionSheetPublication,
) {
    companion object {
        fun fromProjectionState(
            sheetId: String,
            displayName: String,
            order: Int,
            subjectSemanticIds: List<String>,
            representationFamilyId: String = "schematic-sheet",
            publication: AthenaRuntimeProjectionSheetPublication = AthenaRuntimeProjectionSheetPublication.fromProjectionState(
                sheetId = sheetId,
                displayName = displayName,
                order = order,
                subjectSemanticIds = subjectSemanticIds,
            ),
        ): AthenaRuntimeProjectionSheetComposition {
            return AthenaRuntimeProjectionSheetComposition(
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
 * Runtime-owned structured evidence for the document projection policy that produced one sheet.
 */
data class AthenaRuntimeProjectionSheetPolicyEvidence(
    val policyId: String,
    val policyVersion: String,
    val policyDeterministicIdentity: String,
    val sheetViewRole: String,
    val sheetViewRoleOrder: Int,
)

/**
 * Runtime-owned governed layout facts for the active sheet.
 */
data class AthenaRuntimeProjectionSheetLayout(
    val sheetId: String,
    val displayName: String,
    val order: Int,
    val subjectSemanticIds: List<String> = emptyList(),
    val representationFamilyId: String = "schematic-sheet",
    val frame: AthenaRuntimeProjectionSheetLayoutFrame,
    val placements: List<AthenaRuntimeProjectionSheetLayoutPlacement> = emptyList(),
    val routingGuidance: List<AthenaRuntimeProjectionSheetLayoutRoutingGuidance> = emptyList(),
    val labelLayouts: List<AthenaRuntimeProjectionSheetLayoutLabelLayout> = emptyList(),
)

/**
 * Runtime-owned governed frame facts for the active sheet layout.
 */
data class AthenaRuntimeProjectionSheetLayoutFrame(
    val canvasWidth: Int,
    val canvasHeight: Int,
    val gridMajorStep: Int = 120,
    val gridMinorStep: Int = 24,
)

/**
 * Runtime-owned governed placement facts for the active sheet layout.
 */
data class AthenaRuntimeProjectionSheetLayoutPlacement(
    val projectionId: String,
    val semanticId: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/**
 * Runtime-owned governed routing guidance for the active sheet layout.
 */
data class AthenaRuntimeProjectionSheetLayoutRoutingGuidance(
    val projectionConnectionId: String,
    val connectionSemanticId: String,
    val sourcePoint: AthenaRuntimeProjectionPoint,
    val targetPoint: AthenaRuntimeProjectionPoint,
    val routingStyle: String,
    val bendPoints: List<AthenaRuntimeProjectionPoint> = emptyList(),
)

/**
 * Runtime-owned governed label layout for the active sheet layout.
 */
data class AthenaRuntimeProjectionSheetLayoutLabelLayout(
    val projectionId: String,
    val semanticId: String,
    val label: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/**
 * Runtime-owned governed page size for one sheet in active projection.
 */
data class AthenaRuntimeProjectionSheetPageSize(
    val format: String,
    val orientation: String,
)

/**
 * Runtime-owned governed frame semantics for one sheet in active projection.
 */
data class AthenaRuntimeProjectionSheetFrame(
    val frameId: String,
    val style: String,
)

/**
 * Runtime-owned governed coordinate zone on one sheet in active projection.
 */
data class AthenaRuntimeProjectionSheetCoordinateZone(
    val zoneId: String,
    val label: String,
    val order: Int,
)

/**
 * Runtime-owned governed title block semantics for one sheet in active projection.
 */
data class AthenaRuntimeProjectionSheetTitleBlock(
    val sheetTitle: String,
    val sheetFamily: String,
    val sheetNumber: String,
)

/**
 * Runtime-owned governed revision metadata for one sheet in active projection.
 */
data class AthenaRuntimeProjectionSheetRevisionMetadata(
    val revisionCode: String,
    val revisionNote: String,
)

/**
 * Runtime-owned governed view composition for one sheet in active projection.
 */
data class AthenaRuntimeProjectionSheetViewComposition(
    val primaryViewId: String,
    val primarySheetOrder: Int,
    val subjectSemanticIds: List<String> = emptyList(),
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
    val policyEvidence: AthenaRuntimeProjectionSheetPolicyEvidence? = null,
    val publication: AthenaRuntimeProjectionSheetPublication = AthenaRuntimeProjectionSheetPublication.fromProjectionState(
        sheetId = sheetId,
        displayName = displayName,
        order = order,
        subjectSemanticIds = subjectSemanticIds,
    ),
    val composition: AthenaRuntimeProjectionSheetComposition = AthenaRuntimeProjectionSheetComposition.fromProjectionState(
        sheetId = sheetId,
        displayName = displayName,
        order = order,
        subjectSemanticIds = subjectSemanticIds,
        publication = publication,
    ),
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
    val crossReferenceId: String,
    val sheetIds: List<String> = emptyList(),
    val occurrenceIds: List<String> = emptyList(),
    val links: List<AthenaRuntimeProjectionCrossReferenceLink> = emptyList(),
)

data class AthenaRuntimeProjectionCrossReferenceLink(
    val semanticId: String,
    val sourceSheetId: String,
    val targetSheetId: String,
    val sourceOccurrenceId: String,
    val targetOccurrenceId: String,
    val compactNotation: String,
)

/**
 * Runtime-owned projection point used by electrical anchor and corridor payloads.
 */
data class AthenaRuntimeProjectionPoint(
    val x: Int,
    val y: Int,
)

/**
 * Runtime-owned typed electrical anchor occurrence for one canonical port in active projection.
 */
data class AthenaRuntimeProjectionElectricalAnchor(
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
 * Runtime-owned typed electrical connection endpoint occurrence in active projection.
 */
data class AthenaRuntimeProjectionElectricalConnectionEndpoint(
    val endpointId: String,
    val projectionConnectionId: String,
    val connectionSemanticId: String,
    val endpointRole: String,
    val portSemanticId: String,
    val anchorId: String,
)

/**
 * Runtime-owned preferred routing corridor guidance for one electrical connection occurrence.
 *
 * The corridor remains renderer guidance only. It does not replace canonical endpoint truth or
 * claim ownership over the final rendered path.
 */
data class AthenaRuntimeProjectionElectricalRoutingCorridor(
    val corridorId: String,
    val projectionConnectionId: String,
    val connectionSemanticId: String,
    val sourceAnchorId: String,
    val targetAnchorId: String,
    val routingStyle: String,
    val preferredBendPoints: List<AthenaRuntimeProjectionPoint> = emptyList(),
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
    val presentation: PresentationDocument? = null,
    val activeSheetId: String? = null,
    val sheets: List<AthenaRuntimeProjectionSheet> = emptyList(),
    val notationPack: AthenaRuntimeProjectionNotationPack? = null,
    val crossReferences: List<AthenaRuntimeProjectionCrossReference> = emptyList(),
    val electricalAnchors: List<AthenaRuntimeProjectionElectricalAnchor> = emptyList(),
    val electricalConnectionEndpoints: List<AthenaRuntimeProjectionElectricalConnectionEndpoint> = emptyList(),
    val electricalRoutingCorridors: List<AthenaRuntimeProjectionElectricalRoutingCorridor> = emptyList(),
    val activeRenderContributions: List<AthenaRuntimeProjectionRenderContribution> = emptyList(),
    val sheetLayout: AthenaRuntimeProjectionSheetLayout? = null,
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
