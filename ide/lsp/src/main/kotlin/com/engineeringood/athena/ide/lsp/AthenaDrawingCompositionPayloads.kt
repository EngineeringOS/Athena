package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.presentation.PresentationDrawingComposition

data class AthenaDrawingCompositionPayload(
    val sheetId: String,
    val policyId: String,
    val contentBounds: AthenaPresentationBoundsPayload,
    val frameBounds: AthenaPresentationBoundsPayload,
    val drawingAreaBounds: AthenaPresentationBoundsPayload,
    val titleBlockBounds: AthenaPresentationBoundsPayload,
    val sheetBounds: AthenaPresentationBoundsPayload,
    val frameId: String,
    val frameStyle: String,
    val title: AthenaDrawingTitlePayload,
    val coordinateZones: List<AthenaDrawingCoordinateZonePayload>,
    val structureSubjects: List<AthenaDrawingStructureSubjectPayload>,
    val structureFacts: List<AthenaDrawingStructureFactPayload>,
    val referencePlacements: List<AthenaDrawingReferencePlacementPayload>,
    val authorities: AthenaDrawingAuthoritiesPayload,
)

data class AthenaDrawingTitlePayload(
    val sheetTitle: String,
    val sheetFamily: String,
    val sheetNumber: String,
    val revisionCode: String,
    val revisionNote: String,
    val pageFormat: String,
    val orientation: String,
    val fields: List<AthenaDrawingTitleFieldPayload> = emptyList(),
)

data class AthenaDrawingTitleFieldPayload(
    val fieldId: String,
    val label: String,
    val value: String,
    val bounds: AthenaPresentationBoundsPayload,
)

data class AthenaDrawingCoordinateZonePayload(
    val zoneId: String,
    val axis: String,
    val label: String,
    val order: Int,
    val bounds: AthenaPresentationBoundsPayload,
)

data class AthenaDrawingStructureSubjectPayload(
    val subjectId: String,
    val representationIdentity: String,
    val bounds: AthenaPresentationBoundsPayload,
    val representationAuthority: String,
    val boundsAuthority: String,
)

data class AthenaDrawingStructureFactPayload(
    val factId: String,
    val kind: String,
    val axis: String?,
    val bounds: AthenaPresentationBoundsPayload?,
    val start: AthenaProjectionPointPayload?,
    val end: AthenaProjectionPointPayload?,
    val memberIds: List<String>,
    val authority: String,
    val boundsAuthority: String?,
)

data class AthenaDrawingReferencePlacementPayload(
    val placementId: String,
    val referenceId: String,
    val subjectId: String,
    val role: String,
    val representationIdentity: String,
    val bounds: AthenaPresentationBoundsPayload,
    val anchor: AthenaProjectionPointPayload,
    val compactNotation: String,
    val anatomy: AthenaPresentationAnatomyPayload,
)

data class AthenaDrawingAuthoritiesPayload(
    val contentBounds: String,
    val bounds: String,
    val projection: String,
    val representation: String,
    val structureIntent: String,
    val policy: String,
)

internal fun PresentationDrawingComposition.toPayload(): AthenaDrawingCompositionPayload = AthenaDrawingCompositionPayload(
    sheetId = sheetId,
    policyId = policyId,
    contentBounds = contentBounds.toPayload(),
    frameBounds = frameBounds.toPayload(),
    drawingAreaBounds = drawingAreaBounds.toPayload(),
    titleBlockBounds = titleBlockBounds.toPayload(),
    sheetBounds = sheetBounds.toPayload(),
    frameId = frameId,
    frameStyle = frameStyle,
    title = AthenaDrawingTitlePayload(
        title.sheetTitle,
        title.sheetFamily,
        title.sheetNumber,
        title.revisionCode,
        title.revisionNote,
        title.pageFormat,
        title.orientation,
        title.fields.map { field ->
            AthenaDrawingTitleFieldPayload(
                field.fieldId,
                field.label,
                field.value,
                field.bounds.toPayload(),
            )
        },
    ),
    coordinateZones = coordinateZones.map { zone ->
        AthenaDrawingCoordinateZonePayload(zone.zoneId, zone.axis, zone.label, zone.order, zone.bounds.toPayload())
    },
    structureSubjects = structureSubjects.map { subject ->
        AthenaDrawingStructureSubjectPayload(
            subject.subjectId,
            subject.representationIdentity,
            subject.bounds.toPayload(),
            subject.representationAuthority,
            subject.boundsAuthority,
        )
    },
    structureFacts = structureFacts.map { fact ->
        AthenaDrawingStructureFactPayload(
            fact.factId,
            fact.kind,
            fact.axis,
            fact.bounds?.toPayload(),
            fact.start?.let { AthenaProjectionPointPayload(it.x, it.y) },
            fact.end?.let { AthenaProjectionPointPayload(it.x, it.y) },
            fact.memberIds,
            fact.authority,
            fact.boundsAuthority,
        )
    },
    referencePlacements = referencePlacements.map { placement ->
        AthenaDrawingReferencePlacementPayload(
            placement.placementId,
            placement.referenceId,
            placement.subjectId,
            placement.role,
            placement.representationIdentity,
            placement.bounds.toPayload(),
            AthenaProjectionPointPayload(placement.anchor.x, placement.anchor.y),
            placement.compactNotation,
            placement.anatomy.toPayload(),
        )
    },
    authorities = AthenaDrawingAuthoritiesPayload(
        authorities.contentBounds,
        authorities.bounds,
        authorities.projection,
        authorities.representation,
        authorities.structureIntent,
        authorities.policy,
    ),
)

private fun com.engineeringood.athena.presentation.PresentationDrawingBounds.toPayload() =
    AthenaPresentationBoundsPayload(x, y, width, height)
