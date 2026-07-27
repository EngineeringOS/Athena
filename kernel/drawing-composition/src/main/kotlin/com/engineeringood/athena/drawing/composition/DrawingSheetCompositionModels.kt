package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.representation.GraphicBounds

data class DrawingSheetCompositionPolicy(
    val policyId: String,
    val contentToFrame: Double,
    val frameToSheet: Double,
    val titleBlockHeight: Double,
    val maximumSheetWidth: Double,
    val maximumSheetHeight: Double,
    val columnLabels: List<String>,
    val rowLabels: List<String>,
    val fixedSheetBounds: GraphicBounds? = null,
    val coordinateBandSize: Double = 0.0,
)

data class DrawingSheetCompositionRequest(
    val sheetId: ProjectionSheetId,
    val publication: ProjectionSheetPublication,
    val contentBounds: GraphicBounds?,
    val policy: DrawingSheetCompositionPolicy,
    val titleFields: List<DrawingSheetTitleFieldInput> = emptyList(),
)

data class DrawingSheetTitleFieldInput(
    val fieldId: String,
    val label: String,
    val value: String,
)

data class DrawingSheetMarginFact(
    val contentToFrame: Double,
    val frameToSheet: Double,
    val authority: String,
)

data class DrawingSheetFrameFact(
    val frameId: String,
    val style: String,
    val bounds: GraphicBounds,
    val metadataAuthority: String,
    val boundsAuthority: String,
)

data class DrawingSheetTitleBlockFact(
    val bounds: GraphicBounds,
    val sheetTitle: String,
    val sheetFamily: String,
    val sheetNumber: String,
    val revisionCode: String,
    val revisionNote: String,
    val pageFormat: String,
    val orientation: String,
    val metadataAuthority: String,
    val boundsAuthority: String,
    val fields: List<DrawingSheetTitleFieldFact> = emptyList(),
)

data class DrawingSheetTitleFieldFact(
    val fieldId: String,
    val label: String,
    val value: String,
    val bounds: GraphicBounds,
    val metadataAuthority: String,
    val boundsAuthority: String,
)

data class DrawingSheetNamedZoneFact(
    val zoneId: String,
    val label: String,
    val order: Int,
    val authority: String,
)

enum class DrawingSheetZoneAxis {
    COLUMN,
    ROW,
}

data class DrawingSheetCoordinateZoneFact(
    val zoneId: String,
    val axis: DrawingSheetZoneAxis,
    val label: String,
    val order: Int,
    val bounds: GraphicBounds,
    val labelAuthority: String,
    val boundsAuthority: String,
)

data class DrawingSheetCompositionPlan(
    val sheetId: String,
    val sheetBounds: GraphicBounds,
    val frame: DrawingSheetFrameFact,
    val drawingAreaBounds: GraphicBounds,
    val titleBlock: DrawingSheetTitleBlockFact,
    val namedZones: List<DrawingSheetNamedZoneFact>,
    val coordinateZones: List<DrawingSheetCoordinateZoneFact>,
    val margins: DrawingSheetMarginFact,
)

data class DrawingSheetCompositionProof(
    val policyId: String,
    val contentBounds: GraphicBounds,
    val frameBounds: GraphicBounds,
    val drawingAreaBounds: GraphicBounds,
    val titleBlockBounds: GraphicBounds,
    val sheetBounds: GraphicBounds,
    val namedZoneIds: List<String>,
    val coordinateZoneIds: List<String>,
    val contentBoundsAuthority: String,
    val boundsAuthority: String,
    val projectionAuthority: String,
    val policyAuthority: String,
)

data class DrawingSheetCompositionDiagnostic(
    val code: String,
    val authority: String,
    val subject: String,
    val message: String,
)

data class DrawingSheetCompositionResult(
    val plan: DrawingSheetCompositionPlan?,
    val proof: DrawingSheetCompositionProof?,
    val diagnostics: List<DrawingSheetCompositionDiagnostic>,
) {
    val isValid: Boolean
        get() = plan != null && proof != null && diagnostics.isEmpty()

    fun toTransportPayload(): DrawingSheetCompositionTransportPayload? {
        val resolvedPlan = plan ?: return null
        val resolvedProof = proof ?: return null
        if (diagnostics.isNotEmpty()) return null
        return DrawingSheetCompositionTransportPayload(
            sheetId = resolvedPlan.sheetId,
            policyId = resolvedProof.policyId,
            contentBounds = resolvedProof.contentBounds.toPayload(),
            frameBounds = resolvedProof.frameBounds.toPayload(),
            drawingAreaBounds = resolvedProof.drawingAreaBounds.toPayload(),
            titleBlockBounds = resolvedProof.titleBlockBounds.toPayload(),
            sheetBounds = resolvedProof.sheetBounds.toPayload(),
            frameId = resolvedPlan.frame.frameId,
            frameStyle = resolvedPlan.frame.style,
            title = resolvedPlan.titleBlock.toPayload(),
            namedZones = resolvedPlan.namedZones.map { it.toPayload() },
            coordinateZones = resolvedPlan.coordinateZones.map { it.toPayload() },
            namedZoneIds = resolvedProof.namedZoneIds,
            contentToFrameMargin = resolvedPlan.margins.contentToFrame,
            frameToSheetMargin = resolvedPlan.margins.frameToSheet,
            contentBoundsAuthority = resolvedProof.contentBoundsAuthority,
            boundsAuthority = resolvedProof.boundsAuthority,
            projectionAuthority = resolvedProof.projectionAuthority,
            policyAuthority = resolvedProof.policyAuthority,
        )
    }
}

data class DrawingSheetBoundsPayload(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
)

data class DrawingSheetTitleBlockPayload(
    val sheetTitle: String,
    val sheetFamily: String,
    val sheetNumber: String,
    val revisionCode: String,
    val revisionNote: String,
    val pageFormat: String,
    val orientation: String,
    val metadataAuthority: String,
    val boundsAuthority: String,
    val fields: List<DrawingSheetTitleFieldPayload> = emptyList(),
)

data class DrawingSheetTitleFieldPayload(
    val fieldId: String,
    val label: String,
    val value: String,
    val bounds: DrawingSheetBoundsPayload,
    val metadataAuthority: String,
    val boundsAuthority: String,
)

data class DrawingSheetNamedZonePayload(
    val zoneId: String,
    val label: String,
    val order: Int,
    val authority: String,
)

data class DrawingSheetCoordinateZonePayload(
    val zoneId: String,
    val axis: String,
    val label: String,
    val order: Int,
    val bounds: DrawingSheetBoundsPayload,
    val labelAuthority: String,
    val boundsAuthority: String,
)

data class DrawingSheetCompositionTransportPayload(
    val sheetId: String,
    val policyId: String,
    val contentBounds: DrawingSheetBoundsPayload,
    val frameBounds: DrawingSheetBoundsPayload,
    val drawingAreaBounds: DrawingSheetBoundsPayload,
    val titleBlockBounds: DrawingSheetBoundsPayload,
    val sheetBounds: DrawingSheetBoundsPayload,
    val frameId: String,
    val frameStyle: String,
    val title: DrawingSheetTitleBlockPayload,
    val namedZones: List<DrawingSheetNamedZonePayload>,
    val coordinateZones: List<DrawingSheetCoordinateZonePayload>,
    val namedZoneIds: List<String>,
    val contentToFrameMargin: Double,
    val frameToSheetMargin: Double,
    val contentBoundsAuthority: String,
    val boundsAuthority: String,
    val projectionAuthority: String,
    val policyAuthority: String,
)

private fun GraphicBounds.toPayload() = DrawingSheetBoundsPayload(x, y, width, height)

private fun DrawingSheetTitleBlockFact.toPayload() = DrawingSheetTitleBlockPayload(
    sheetTitle,
    sheetFamily,
    sheetNumber,
    revisionCode,
    revisionNote,
    pageFormat,
    orientation,
    metadataAuthority,
    boundsAuthority,
    fields.map { field ->
        DrawingSheetTitleFieldPayload(
            field.fieldId,
            field.label,
            field.value,
            field.bounds.toPayload(),
            field.metadataAuthority,
            field.boundsAuthority,
        )
    },
)

private fun DrawingSheetNamedZoneFact.toPayload() = DrawingSheetNamedZonePayload(zoneId, label, order, authority)

private fun DrawingSheetCoordinateZoneFact.toPayload() = DrawingSheetCoordinateZonePayload(
    zoneId,
    axis.name,
    label,
    order,
    bounds.toPayload(),
    labelAuthority,
    boundsAuthority,
)
