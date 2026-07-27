package com.engineeringood.athena.presentation

import com.engineeringood.athena.representation.PresentationAnatomy

/** Renderer-neutral professional drawing composition attached to one presentation document. */
data class PresentationDrawingComposition(
    val sheetId: String,
    val policyId: String,
    val contentBounds: PresentationDrawingBounds,
    val frameBounds: PresentationDrawingBounds,
    val drawingAreaBounds: PresentationDrawingBounds,
    val titleBlockBounds: PresentationDrawingBounds,
    val sheetBounds: PresentationDrawingBounds,
    val frameId: String,
    val frameStyle: String,
    val title: PresentationDrawingTitle,
    val coordinateZones: List<PresentationDrawingCoordinateZone>,
    val structureSubjects: List<PresentationDrawingStructureSubject>,
    val structureFacts: List<PresentationDrawingStructureFact>,
    val referencePlacements: List<PresentationDrawingReferencePlacement>,
    val authorities: PresentationDrawingAuthorities,
)

data class PresentationDrawingBounds(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

data class PresentationDrawingPoint(val x: Int, val y: Int)

data class PresentationDrawingTitle(
    val sheetTitle: String,
    val sheetFamily: String,
    val sheetNumber: String,
    val revisionCode: String,
    val revisionNote: String,
    val pageFormat: String,
    val orientation: String,
    val fields: List<PresentationDrawingTitleField> = emptyList(),
)

data class PresentationDrawingTitleField(
    val fieldId: String,
    val label: String,
    val value: String,
    val bounds: PresentationDrawingBounds,
)

data class PresentationDrawingCoordinateZone(
    val zoneId: String,
    val axis: String,
    val label: String,
    val order: Int,
    val bounds: PresentationDrawingBounds,
)

data class PresentationDrawingStructureSubject(
    val subjectId: String,
    val representationIdentity: String,
    val bounds: PresentationDrawingBounds,
    val representationAuthority: String,
    val boundsAuthority: String,
)

data class PresentationDrawingStructureFact(
    val factId: String,
    val kind: String,
    val axis: String?,
    val bounds: PresentationDrawingBounds?,
    val start: PresentationDrawingPoint?,
    val end: PresentationDrawingPoint?,
    val memberIds: List<String>,
    val authority: String,
    val boundsAuthority: String?,
)

data class PresentationDrawingReferencePlacement(
    val placementId: String,
    val referenceId: String,
    val subjectId: String,
    val role: String,
    val representationIdentity: String,
    val bounds: PresentationDrawingBounds,
    val anchor: PresentationDrawingPoint,
    val compactNotation: String,
    val anatomy: PresentationAnatomy,
)

data class PresentationDrawingAuthorities(
    val contentBounds: String,
    val bounds: String,
    val projection: String,
    val representation: String,
    val structureIntent: String,
    val policy: String,
)
