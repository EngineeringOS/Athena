package com.engineeringood.athena.ide.lsp

/** Typed Spatial authority payload carried through LSP without renderer-owned recomputation. */
data class AthenaSpatialFactsPayload(
    val viewId: String,
    val activeSheetId: String?,
    val sheets: List<AthenaSpatialSheetFactsPayload>,
)

data class AthenaSpatialSheetFactsPayload(
    val sheetId: String,
    val extent: AthenaRectPayload,
    val drawingArea: AthenaRectPayload,
    val occurrences: List<AthenaSpatialOccurrenceFactsPayload>,
    val regions: List<AthenaSpatialRegionFactsPayload>,
    val constructs: List<AthenaSpatialConstructFactsPayload>,
    val anchors: List<AthenaSpatialAnchorFactsPayload>,
    val routes: List<AthenaSpatialRouteFactsPayload>,
    val lanes: List<AthenaSpatialLaneFactsPayload>,
    val gridReferences: List<AthenaSpatialGridReferenceFactsPayload>,
    val quality: AthenaSpatialQualityFactsPayload,
)

data class AthenaRectPayload(val x: Int, val y: Int, val width: Int, val height: Int)
data class AthenaSpatialPointPayload(val x: Int, val y: Int)

data class AthenaSpatialOccurrenceFactsPayload(
    val occurrenceId: String,
    val semanticId: String,
    val regionId: String,
    val bounds: AthenaRectPayload,
)

data class AthenaSpatialRegionFactsPayload(
    val regionId: String,
    val bounds: AthenaRectPayload,
    val memberOccurrenceIds: List<String>,
)

data class AthenaSpatialConstructFactsPayload(
    val constructId: String,
    val kind: String,
    val name: String?,
    val bounds: AthenaRectPayload,
    val memberOccurrenceIds: List<String>,
)

data class AthenaSpatialAnchorFactsPayload(
    val anchorId: String,
    val occurrenceId: String,
    val portSemanticId: String,
    val side: String,
    val point: AthenaSpatialPointPayload,
)

data class AthenaSpatialRouteFactsPayload(
    val routeId: String,
    val projectionConnectionId: String,
    val connectionId: String,
    val sourceAnchorId: String,
    val targetAnchorId: String,
    val laneId: String,
    val points: List<AthenaSpatialPointPayload>,
)

data class AthenaSpatialLaneFactsPayload(
    val laneId: String,
    val orientation: String,
    val coordinate: Int,
    val routeIds: List<String>,
)

data class AthenaSpatialGridReferenceFactsPayload(
    val gridReferenceId: String,
    val subjectId: String,
    val cellReference: String,
    val rowLabel: String,
    val columnNumber: Int,
)

data class AthenaSpatialQualityFactsPayload(
    val occurrenceOverlapCount: Int,
    val constructContainmentFailureCount: Int,
    val routeBodyIntersectionCount: Int,
    val routeCrossingCount: Int,
    val twistCount: Int,
    val usedLaneCount: Int,
    val peakRoutesPerLane: Int,
    val density: Double,
    val occupancy: Double,
)
