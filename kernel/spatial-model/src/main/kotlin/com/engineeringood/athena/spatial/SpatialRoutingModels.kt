package com.engineeringood.athena.spatial

import com.engineeringood.athena.ir.StableSemanticIdentity
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Collections

data class SpatialPoint(
    val x: Int,
    val y: Int,
)

enum class SpatialBoundarySide {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
}

data class SpatialOccurrencePortSubject(
    val occurrenceId: SpatialOccurrenceId,
    val portId: StableSemanticIdentity,
) {
    init {
        require(portId.value.isNotBlank()) { "Spatial occurrence-port semantic identity must not be blank." }
    }
}

data class SpatialAnchorId(
    val sheetId: String,
    val occurrenceId: SpatialOccurrenceId,
    val portId: StableSemanticIdentity,
) {
    init {
        require(sheetId.isNotBlank()) { "Spatial Anchor Sheet identity must not be blank." }
        require(occurrenceId.sheetId == sheetId) { "Spatial Anchor identity must name its owning Sheet." }
        require(portId.value.isNotBlank()) { "Spatial Anchor port identity must not be blank." }
    }

    val value: String
        get() = "anchor:sheet=${sheetId.encodedIdentityPart()}:" +
            "occurrence=${occurrenceId.projectionId.encodedIdentityPart()}:" +
            "port=${portId.value.encodedIdentityPart()}"
}

private fun String.encodedIdentityPart(): String = URLEncoder.encode(this, StandardCharsets.UTF_8)

data class SpatialAnchorPosition(
    val anchorId: SpatialAnchorId,
    val sheetId: String,
    val subject: SpatialOccurrencePortSubject,
    val side: SpatialBoundarySide,
    val point: SpatialPoint,
    val sourceTrace: SpatialSourceTrace,
) {
    init {
        require(anchorId.sheetId == sheetId) { "Spatial Anchor position identity must name its owning Sheet." }
        require(subject.occurrenceId.sheetId == sheetId) { "Spatial Anchor subject must belong to its owning Sheet." }
        require(anchorId.occurrenceId == subject.occurrenceId && anchorId.portId == subject.portId) {
            "Spatial Anchor identity must name its occurrence-port subject."
        }
    }
}

enum class SpatialLaneOrientation {
    HORIZONTAL,
    VERTICAL,
}

data class SpatialLaneId(
    val sheetId: String,
    val orientation: SpatialLaneOrientation,
    val coordinate: Int,
) {
    init {
        require(sheetId.isNotBlank()) { "Spatial Lane Sheet identity must not be blank." }
    }

    val value: String
        get() = "lane:sheet=${sheetId.encodedIdentityPart()}:" +
            "orientation=${orientation.name.lowercase()}:coordinate=$coordinate"
}

class SpatialLane(
    val laneId: SpatialLaneId,
    val sheetId: String,
    val orientation: SpatialLaneOrientation,
    val coordinate: Int,
    routeIds: List<ConnectionRoutePlanId>,
) {
    val routeIds: List<ConnectionRoutePlanId> = Collections.unmodifiableList(routeIds.toList())

    init {
        require(laneId.sheetId == sheetId) { "Spatial Lane identity must name its owning Sheet." }
        require(laneId.orientation == orientation && laneId.coordinate == coordinate) {
            "Spatial Lane identity must name its routing channel."
        }
        require(this.routeIds.isNotEmpty()) { "Spatial Lane must contain at least one Route." }
        require(this.routeIds.distinct().size == this.routeIds.size) { "Spatial Lane must not repeat Route identities." }
        require(this.routeIds.all { routeId -> routeId.sheetId == sheetId }) {
            "Spatial Lane Routes must belong to its owning Sheet."
        }
    }

    fun copy(
        laneId: SpatialLaneId = this.laneId,
        sheetId: String = this.sheetId,
        orientation: SpatialLaneOrientation = this.orientation,
        coordinate: Int = this.coordinate,
        routeIds: List<ConnectionRoutePlanId> = this.routeIds,
    ): SpatialLane = SpatialLane(laneId, sheetId, orientation, coordinate, routeIds)

    override fun equals(other: Any?): Boolean =
        this === other || other is SpatialLane &&
            laneId == other.laneId &&
            sheetId == other.sheetId &&
            orientation == other.orientation &&
            coordinate == other.coordinate &&
            routeIds == other.routeIds

    override fun hashCode(): Int = listOf(laneId, sheetId, orientation, coordinate, routeIds).hashCode()

    override fun toString(): String =
        "SpatialLane(laneId=$laneId, sheetId=$sheetId, orientation=$orientation, coordinate=$coordinate, " +
            "routeIds=$routeIds)"
}
