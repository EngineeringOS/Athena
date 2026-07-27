package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.projection.ProjectionCrossReference
import com.engineeringood.athena.projection.ProjectionCrossReferenceId
import com.engineeringood.athena.representation.DrawingSymbolAnchorId
import com.engineeringood.athena.representation.DrawingSymbolIdentity
import com.engineeringood.athena.representation.DrawingSymbolSlotId
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint

enum class DrawingSheetReferencePlacementRole {
    SOURCE,
    TARGET,
}

data class DrawingSheetReferencePlacementInput(
    val placementId: String,
    val crossReferenceId: ProjectionCrossReferenceId,
    val linkSourceOccurrenceId: String,
    val occurrenceId: String,
    val subjectId: String,
    val role: DrawingSheetReferencePlacementRole,
    val representationIdentity: DrawingSymbolIdentity,
    val bounds: GraphicBounds,
    val anchorId: DrawingSymbolAnchorId,
    val anchorPoint: GraphicPoint,
    val availableAnchorIds: Set<DrawingSymbolAnchorId>,
    val labelSlotId: DrawingSymbolSlotId,
    val availableLabelSlotIds: Set<DrawingSymbolSlotId>,
    val sheetReferenceSlotId: DrawingSymbolSlotId,
    val zoneReferenceSlotId: DrawingSymbolSlotId?,
    val availableReferenceSlotIds: Set<DrawingSymbolSlotId>,
    val zoneId: String,
)

data class DrawingSheetReferenceRequest(
    val sheetPlan: DrawingSheetCompositionPlan,
    val structurePlan: DrawingSheetStructurePlan,
    val references: List<ProjectionCrossReference>,
    val placements: List<DrawingSheetReferencePlacementInput>,
)

data class DrawingSheetReferenceTargetFact(
    val targetId: String,
    val referenceId: String,
    val semanticSubjectId: String,
    val sourceSheetId: String,
    val targetSheetId: String,
    val sourceOccurrenceId: String,
    val targetOccurrenceId: String,
    val compactNotation: String,
    val authority: String,
)

data class DrawingSheetReferencePlacementFact(
    val placementId: String,
    val referenceId: String,
    val occurrenceId: String,
    val subjectId: String,
    val role: DrawingSheetReferencePlacementRole,
    val representationIdentity: DrawingSymbolIdentity,
    val bounds: GraphicBounds,
    val anchorId: DrawingSymbolAnchorId,
    val anchorPoint: GraphicPoint,
    val labelSlotId: DrawingSymbolSlotId,
    val sheetReferenceSlotId: DrawingSymbolSlotId,
    val zoneReferenceSlotId: DrawingSymbolSlotId?,
    val availableReferenceSlotIds: List<DrawingSymbolSlotId>,
    val zoneId: String,
    val compactNotation: String,
    val projectionAuthority: String,
    val representationAuthority: String,
    val boundsAuthority: String,
)

data class DrawingSheetReferencePlan(
    val sheetId: String,
    val targets: List<DrawingSheetReferenceTargetFact>,
    val placements: List<DrawingSheetReferencePlacementFact>,
)

data class DrawingSheetReferenceProof(
    val sheetId: String,
    val referenceIds: List<String>,
    val placementIds: List<String>,
    val markerRepresentationIdentities: List<String>,
    val projectionAuthority: String,
    val representationAuthority: String,
    val boundsAuthority: String,
    val structureAuthority: String,
)

data class DrawingSheetReferenceDiagnostic(
    val code: String,
    val authority: String,
    val subject: String,
    val message: String,
)

data class DrawingSheetReferenceResult(
    val plan: DrawingSheetReferencePlan?,
    val proof: DrawingSheetReferenceProof?,
    val diagnostics: List<DrawingSheetReferenceDiagnostic>,
) {
    val isValid: Boolean
        get() = plan != null && proof != null && diagnostics.isEmpty()

    fun toTransportPayload(): DrawingSheetReferenceTransportPayload? {
        val resolvedPlan = plan ?: return null
        val resolvedProof = proof ?: return null
        if (diagnostics.isNotEmpty()) return null
        return DrawingSheetReferenceTransportPayload(
            sheetId = resolvedPlan.sheetId,
            targets = resolvedPlan.targets.map {
                DrawingSheetReferenceTargetPayload(
                    it.targetId, it.referenceId, it.semanticSubjectId, it.sourceSheetId, it.targetSheetId,
                    it.sourceOccurrenceId, it.targetOccurrenceId, it.compactNotation, it.authority,
                )
            },
            placements = resolvedPlan.placements.map { it.toPayload() },
            markerRepresentationIdentities = resolvedProof.markerRepresentationIdentities,
            projectionAuthority = resolvedProof.projectionAuthority,
            representationAuthority = resolvedProof.representationAuthority,
            boundsAuthority = resolvedProof.boundsAuthority,
            structureAuthority = resolvedProof.structureAuthority,
        )
    }
}

data class DrawingSheetReferenceTargetPayload(
    val targetId: String,
    val referenceId: String,
    val semanticSubjectId: String,
    val sourceSheetId: String,
    val targetSheetId: String,
    val sourceOccurrenceId: String,
    val targetOccurrenceId: String,
    val compactNotation: String,
    val authority: String,
)

data class DrawingSheetReferencePlacementPayload(
    val placementId: String,
    val referenceId: String,
    val occurrenceId: String,
    val subjectId: String,
    val role: String,
    val representationIdentity: String,
    val bounds: DrawingSheetBoundsPayload,
    val anchorId: String,
    val anchorPoint: DrawingSheetStructurePointPayload,
    val labelSlotId: String,
    val sheetReferenceSlotId: String,
    val zoneReferenceSlotId: String?,
    val availableReferenceSlotIds: List<String>,
    val zoneId: String,
    val compactNotation: String,
)

data class DrawingSheetReferenceTransportPayload(
    val sheetId: String,
    val targets: List<DrawingSheetReferenceTargetPayload>,
    val placements: List<DrawingSheetReferencePlacementPayload>,
    val markerRepresentationIdentities: List<String>,
    val projectionAuthority: String,
    val representationAuthority: String,
    val boundsAuthority: String,
    val structureAuthority: String,
)

private fun DrawingSheetReferencePlacementFact.toPayload() = DrawingSheetReferencePlacementPayload(
    placementId = placementId,
    referenceId = referenceId,
    occurrenceId = occurrenceId,
    subjectId = subjectId,
    role = role.name,
    representationIdentity = representationIdentity.value,
    bounds = DrawingSheetBoundsPayload(bounds.x, bounds.y, bounds.width, bounds.height),
    anchorId = anchorId.value,
    anchorPoint = DrawingSheetStructurePointPayload(anchorPoint.x, anchorPoint.y),
    labelSlotId = labelSlotId.value,
    sheetReferenceSlotId = sheetReferenceSlotId.value,
    zoneReferenceSlotId = zoneReferenceSlotId?.value,
    availableReferenceSlotIds = availableReferenceSlotIds.map { it.value },
    zoneId = zoneId,
    compactNotation = compactNotation,
)
