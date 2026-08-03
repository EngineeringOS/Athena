package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.representation.DrawingSymbolAnchorId
import com.engineeringood.athena.representation.DrawingSymbolIdentity
import com.engineeringood.athena.representation.DrawingSymbolSlotId
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPoint

enum class DrawingSheetAxis {
    HORIZONTAL,
    VERTICAL,
}

data class DrawingSheetStructurePolicy(
    val policyId: String,
    val terminalStripPadding: Double,
    val maximumSubjectGap: Double,
)

data class DrawingSheetStructureAnchorInput(
    val anchorId: DrawingSymbolAnchorId,
    val point: GraphicPoint,
)

data class DrawingSheetStructureLabelInput(
    val labelId: String,
    val slotId: DrawingSymbolSlotId,
    val bounds: GraphicBounds,
)

data class DrawingSheetStructureSubjectInput(
    val subjectId: String,
    val representationIdentity: DrawingSymbolIdentity,
    val bounds: GraphicBounds,
    val anchors: List<DrawingSheetStructureAnchorInput>,
    val requiredAnchorIds: Set<DrawingSymbolAnchorId>,
    val labels: List<DrawingSheetStructureLabelInput>,
    val requiredLabelSlotIds: Set<DrawingSymbolSlotId>,
)

data class DrawingSheetAnchorReference(
    val subjectId: String,
    val anchorId: DrawingSymbolAnchorId,
)

data class DrawingSheetRailIntent(
    val railId: String,
    val axis: DrawingSheetAxis,
    val start: GraphicPoint,
    val end: GraphicPoint,
    val subjectIds: List<String>,
)

data class DrawingSheetLaneIntent(
    val laneId: String,
    val axis: DrawingSheetAxis,
    val bounds: GraphicBounds,
    val subjectIds: List<String>,
)

data class DrawingSheetTerminalStripIntent(
    val stripId: String,
    val subjectIds: List<String>,
)

data class DrawingSheetLabelBandIntent(
    val bandId: String,
    val bounds: GraphicBounds,
    val labelIds: List<String>,
)

data class DrawingSheetRouteChannelIntent(
    val channelId: String,
    val axis: DrawingSheetAxis,
    val bounds: GraphicBounds,
    val anchorReferences: List<DrawingSheetAnchorReference>,
)

data class DrawingSheetStructureRequest(
    val sheetPlan: DrawingSheetCompositionPlan,
    val subjects: List<DrawingSheetStructureSubjectInput>,
    val rails: List<DrawingSheetRailIntent>,
    val lanes: List<DrawingSheetLaneIntent>,
    val terminalStrips: List<DrawingSheetTerminalStripIntent>,
    val labelBands: List<DrawingSheetLabelBandIntent>,
    val routeChannels: List<DrawingSheetRouteChannelIntent>,
    val policy: DrawingSheetStructurePolicy,
)

data class DrawingSheetStructureAnchorFact(
    val anchorId: DrawingSymbolAnchorId,
    val point: GraphicPoint,
)

data class DrawingSheetStructureLabelFact(
    val labelId: String,
    val slotId: DrawingSymbolSlotId,
    val bounds: GraphicBounds,
)

data class DrawingSheetStructureSubjectFact(
    val subjectId: String,
    val representationIdentity: DrawingSymbolIdentity,
    val bounds: GraphicBounds,
    val anchors: List<DrawingSheetStructureAnchorFact>,
    val labels: List<DrawingSheetStructureLabelFact>,
    val representationAuthority: String,
    val boundsAuthority: String,
)

data class DrawingSheetRailFact(
    val railId: String,
    val axis: DrawingSheetAxis,
    val start: GraphicPoint,
    val end: GraphicPoint,
    val subjectIds: List<String>,
    val authority: String,
)

data class DrawingSheetLaneFact(
    val laneId: String,
    val axis: DrawingSheetAxis,
    val bounds: GraphicBounds,
    val subjectIds: List<String>,
    val authority: String,
)

data class DrawingSheetTerminalStripFact(
    val stripId: String,
    val bounds: GraphicBounds,
    val subjectIds: List<String>,
    val membershipAuthority: String,
    val boundsAuthority: String,
)

data class DrawingSheetLabelBandFact(
    val bandId: String,
    val bounds: GraphicBounds,
    val labelIds: List<String>,
    val authority: String,
)

data class DrawingSheetRouteChannelFact(
    val channelId: String,
    val axis: DrawingSheetAxis,
    val bounds: GraphicBounds,
    val anchorReferences: List<DrawingSheetAnchorReference>,
    val authority: String,
)

data class DrawingSheetStructurePlan(
    val sheetId: String,
    val subjects: List<DrawingSheetStructureSubjectFact>,
    val rails: List<DrawingSheetRailFact>,
    val lanes: List<DrawingSheetLaneFact>,
    val terminalStrips: List<DrawingSheetTerminalStripFact>,
    val labelBands: List<DrawingSheetLabelBandFact>,
    val routeChannels: List<DrawingSheetRouteChannelFact>,
)

data class DrawingSheetStructureEvidence(
    val sheetId: String,
    val policyId: String,
    val drawingAreaBounds: GraphicBounds,
    val subjectIds: List<String>,
    val railIds: List<String>,
    val laneIds: List<String>,
    val terminalStripIds: List<String>,
    val labelBandIds: List<String>,
    val routeChannelIds: List<String>,
    val boundsAuthority: String,
    val representationAuthority: String,
    val structureIntentAuthority: String,
    val policyAuthority: String,
)

data class DrawingSheetStructureDiagnostic(
    val code: String,
    val authority: String,
    val subject: String,
    val message: String,
)

data class DrawingSheetStructureResult(
    val plan: DrawingSheetStructurePlan?,
    val evidence: DrawingSheetStructureEvidence?,
    val diagnostics: List<DrawingSheetStructureDiagnostic>,
) {
    val isValid: Boolean
        get() = plan != null && evidence != null && diagnostics.isEmpty()

    fun toTransportPayload(): DrawingSheetStructureTransportPayload? {
        val resolvedPlan = plan ?: return null
        val resolvedEvidence = evidence ?: return null
        if (diagnostics.isNotEmpty()) return null
        return DrawingSheetStructureTransportPayload(
            sheetId = resolvedPlan.sheetId,
            policyId = resolvedEvidence.policyId,
            drawingAreaBounds = resolvedEvidence.drawingAreaBounds.toStructurePayload(),
            subjects = resolvedPlan.subjects.map { it.toPayload() },
            facts = buildList {
                resolvedPlan.rails.forEach { add(it.toPayload()) }
                resolvedPlan.lanes.forEach { add(it.toPayload()) }
                resolvedPlan.terminalStrips.forEach { add(it.toPayload()) }
                resolvedPlan.labelBands.forEach { add(it.toPayload()) }
                resolvedPlan.routeChannels.forEach { add(it.toPayload()) }
            },
            boundsAuthority = resolvedEvidence.boundsAuthority,
            representationAuthority = resolvedEvidence.representationAuthority,
            structureIntentAuthority = resolvedEvidence.structureIntentAuthority,
            policyAuthority = resolvedEvidence.policyAuthority,
        )
    }
}

data class DrawingSheetStructurePointPayload(val x: Double, val y: Double)

data class DrawingSheetStructureAnchorPayload(val anchorId: String, val point: DrawingSheetStructurePointPayload)

data class DrawingSheetStructureLabelPayload(val labelId: String, val slotId: String, val bounds: DrawingSheetBoundsPayload)

data class DrawingSheetStructureSubjectPayload(
    val subjectId: String,
    val representationIdentity: String,
    val bounds: DrawingSheetBoundsPayload,
    val anchors: List<DrawingSheetStructureAnchorPayload>,
    val labels: List<DrawingSheetStructureLabelPayload>,
    val representationAuthority: String,
    val boundsAuthority: String,
)

data class DrawingSheetStructureFactPayload(
    val factId: String,
    val kind: String,
    val axis: String?,
    val bounds: DrawingSheetBoundsPayload?,
    val start: DrawingSheetStructurePointPayload?,
    val end: DrawingSheetStructurePointPayload?,
    val memberIds: List<String>,
    val authority: String,
    val boundsAuthority: String?,
)

data class DrawingSheetStructureTransportPayload(
    val sheetId: String,
    val policyId: String,
    val drawingAreaBounds: DrawingSheetBoundsPayload,
    val subjects: List<DrawingSheetStructureSubjectPayload>,
    val facts: List<DrawingSheetStructureFactPayload>,
    val boundsAuthority: String,
    val representationAuthority: String,
    val structureIntentAuthority: String,
    val policyAuthority: String,
)

private fun GraphicBounds.toStructurePayload() = DrawingSheetBoundsPayload(x, y, width, height)
private fun GraphicPoint.toPayload() = DrawingSheetStructurePointPayload(x, y)

private fun DrawingSheetStructureSubjectFact.toPayload() = DrawingSheetStructureSubjectPayload(
    subjectId,
    representationIdentity.value,
    bounds.toStructurePayload(),
    anchors.map { DrawingSheetStructureAnchorPayload(it.anchorId.value, it.point.toPayload()) },
    labels.map { DrawingSheetStructureLabelPayload(it.labelId, it.slotId.value, it.bounds.toStructurePayload()) },
    representationAuthority,
    boundsAuthority,
)

private fun DrawingSheetRailFact.toPayload() = DrawingSheetStructureFactPayload(
    railId, "rail", axis.name, null, start.toPayload(), end.toPayload(), subjectIds, authority, null,
)

private fun DrawingSheetLaneFact.toPayload() = DrawingSheetStructureFactPayload(
    laneId, "lane", axis.name, bounds.toStructurePayload(), null, null, subjectIds, authority, null,
)

private fun DrawingSheetTerminalStripFact.toPayload() = DrawingSheetStructureFactPayload(
    stripId, "terminal-strip", null, bounds.toStructurePayload(), null, null, subjectIds, membershipAuthority, boundsAuthority,
)

private fun DrawingSheetLabelBandFact.toPayload() = DrawingSheetStructureFactPayload(
    bandId, "label-band", null, bounds.toStructurePayload(), null, null, labelIds, authority, null,
)

private fun DrawingSheetRouteChannelFact.toPayload() = DrawingSheetStructureFactPayload(
    channelId,
    "route-channel",
    axis.name,
    bounds.toStructurePayload(),
    null,
    null,
    anchorReferences.map { "${it.subjectId}:${it.anchorId.value}" },
    authority,
    null,
)
