package com.engineeringood.athena.spatial

data class SpatialQualitySnapshotId(
    val sheetId: String,
) {
    init {
        require(sheetId.isNotBlank()) { "Spatial quality snapshot Sheet identity must not be blank." }
    }
}

data class SpatialQualityMetrics(
    val occurrenceOverlapCount: Int,
    val constructContainmentFailureCount: Int,
    val routeBodyIntersectionCount: Int,
    val routeCrossingCount: Int,
    val twistCount: Int,
    val usedLaneCount: Int,
    val peakRoutesPerLane: Int,
    val density: Double,
    val occupancy: Double,
) {
    init {
        require(occurrenceOverlapCount >= 0) { "Occurrence overlap count must not be negative." }
        require(constructContainmentFailureCount >= 0) {
            "Construct containment failure count must not be negative."
        }
        require(routeBodyIntersectionCount >= 0) { "Route/body intersection count must not be negative." }
        require(routeCrossingCount >= 0) { "Route crossing count must not be negative." }
        require(twistCount >= 0) { "Twist count must not be negative." }
        require(usedLaneCount >= 0) { "Used Lane count must not be negative." }
        require(peakRoutesPerLane >= 0) { "Peak Routes per Lane must not be negative." }
    }
}

data class SpatialQualitySnapshot(
    val qualitySnapshotId: SpatialQualitySnapshotId,
    val sheetId: String,
    val metrics: SpatialQualityMetrics,
    val sourceTrace: SpatialSourceTrace,
) {
    init {
        require(qualitySnapshotId.sheetId == sheetId) {
            "Spatial quality snapshot identity must name its owning Sheet."
        }
    }
}
