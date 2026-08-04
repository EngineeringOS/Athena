package com.engineeringood.athena.runtime

/** Immutable, renderer-neutral Spatial facts retained from one compiler session. */
data class AthenaRuntimeSpatialFacts(
    val viewId: String,
    val activeSheetId: String?,
    val sheets: List<AthenaRuntimeSpatialSheetFacts>,
)

data class AthenaRuntimeSpatialSheetFacts(
    val sheetId: String,
    val extent: AthenaRuntimeRect,
    val drawingArea: AthenaRuntimeRect,
    val occurrences: List<AthenaRuntimeSpatialOccurrenceFacts>,
    val regions: List<AthenaRuntimeSpatialRegionFacts>,
    val constructs: List<AthenaRuntimeSpatialConstructFacts>,
    val anchors: List<AthenaRuntimeSpatialAnchorFacts>,
    val routes: List<AthenaRuntimeSpatialRouteFacts>,
    val lanes: List<AthenaRuntimeSpatialLaneFacts>,
    val gridReferences: List<AthenaRuntimeSpatialGridReferenceFacts>,
    val quality: AthenaRuntimeSpatialQualityFacts,
)

data class AthenaRuntimeRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

data class AthenaRuntimePoint(
    val x: Int,
    val y: Int,
)

data class AthenaRuntimeSpatialOccurrenceFacts(
    val occurrenceId: String,
    val semanticId: String,
    val regionId: String,
    val bounds: AthenaRuntimeRect,
)

data class AthenaRuntimeSpatialRegionFacts(
    val regionId: String,
    val bounds: AthenaRuntimeRect,
    val memberOccurrenceIds: List<String>,
)

data class AthenaRuntimeSpatialConstructFacts(
    val constructId: String,
    val kind: String,
    val name: String?,
    val bounds: AthenaRuntimeRect,
    val memberOccurrenceIds: List<String>,
)

data class AthenaRuntimeSpatialAnchorFacts(
    val anchorId: String,
    val occurrenceId: String,
    val portSemanticId: String,
    val side: String,
    val point: AthenaRuntimePoint,
)

data class AthenaRuntimeSpatialRouteFacts(
    val routeId: String,
    val projectionConnectionId: String,
    val connectionId: String,
    val sourceAnchorId: String,
    val targetAnchorId: String,
    val laneId: String,
    val points: List<AthenaRuntimePoint>,
)

data class AthenaRuntimeSpatialLaneFacts(
    val laneId: String,
    val orientation: String,
    val coordinate: Int,
    val routeIds: List<String>,
)

data class AthenaRuntimeSpatialGridReferenceFacts(
    val gridReferenceId: String,
    val subjectId: String,
    val cellReference: String,
    val rowLabel: String,
    val columnNumber: Int,
)

data class AthenaRuntimeSpatialQualityFacts(
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
