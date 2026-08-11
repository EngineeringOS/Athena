package com.engineeringood.athena.spatial

import com.engineeringood.athena.ir.StableSemanticIdentity
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class ConnectionRoutePlanId(
    val sheetId: String,
    val projectionRouteId: String,
) {
    init {
        require(sheetId.isNotBlank()) { "Connection route plan Sheet identity must not be blank." }
        require(projectionRouteId.isNotBlank()) { "Connection route plan projection route identity must not be blank." }
    }

    val value: String
        get() = "route-plan:sheet=${sheetId.encodedRoutePart()}:route=${projectionRouteId.encodedRoutePart()}"
}

data class ConnectionRouteSegment(
    val start: SpatialPoint,
    val end: SpatialPoint,
) {
    val isPositiveOrthogonal: Boolean = start != end && (start.x == end.x || start.y == end.y)

    val orientation: SpatialLaneOrientation?
        get() = when {
            start == end -> null
            start.y == end.y -> SpatialLaneOrientation.HORIZONTAL
            start.x == end.x -> SpatialLaneOrientation.VERTICAL
            else -> null
        }

    val manhattanLength: Long
        get() = kotlin.math.abs(start.x.toLong() - end.x.toLong()) + kotlin.math.abs(start.y.toLong() - end.y.toLong())
}

data class ConnectionRouteQuality(
    val semanticValidity: Int,
    val hardObstacleViolations: Int,
    val ambiguousOverlapCount: Int,
    val crossingCount: Int,
    val bendCount: Int,
    val manhattanLength: Long,
    val geometryIdentity: String,
) {
    init {
        require(semanticValidity >= 0) { "Route semantic validity score must not be negative." }
        require(hardObstacleViolations >= 0) { "Route obstacle violation count must not be negative." }
        require(ambiguousOverlapCount >= 0) { "Route overlap count must not be negative." }
        require(crossingCount >= 0) { "Route crossing count must not be negative." }
        require(bendCount >= 0) { "Route bend count must not be negative." }
        require(manhattanLength >= 0) { "Route Manhattan length must not be negative." }
        require(geometryIdentity.isNotBlank()) { "Route geometry identity must not be blank." }
    }

    val lexicographicCost: List<Comparable<*>>
        get() = listOf(
            semanticValidity,
            hardObstacleViolations,
            ambiguousOverlapCount,
            crossingCount,
            bendCount,
            manhattanLength,
            geometryIdentity,
        )

    companion object {
        fun valid(segments: List<ConnectionRouteSegment>): ConnectionRouteQuality {
            require(segments.isNotEmpty()) { "A valid route must contain at least one segment." }
            return ConnectionRouteQuality(
                semanticValidity = 0,
                hardObstacleViolations = 0,
                ambiguousOverlapCount = 0,
                crossingCount = 0,
                bendCount = (segments.size - 1).coerceAtLeast(0),
                manhattanLength = segments.sumOf(ConnectionRouteSegment::manhattanLength),
                geometryIdentity = segments.geometryIdentity(),
            )
        }
    }
}

class ConnectionRoutePlan(
    val routeId: ConnectionRoutePlanId,
    val sheetId: String,
    val connectionId: StableSemanticIdentity,
    val projectionConnectionId: String,
    val sourceAnchorId: SpatialAnchorId,
    val targetAnchorId: SpatialAnchorId,
    points: List<SpatialPoint>,
    val quality: ConnectionRouteQuality = ConnectionRouteQuality.valid(points.zipWithNext(::ConnectionRouteSegment)),
    val sourceTrace: SpatialSourceTrace,
    val laneId: SpatialLaneId,
    drawingArea: SpatialRect? = null,
) {
    val points: List<SpatialPoint> = points.toList()
    val segments: List<ConnectionRouteSegment> = this.points.zipWithNext(::ConnectionRouteSegment)
    val geometryIdentity: String = this.segments.geometryIdentity()

    fun copy(
        routeId: ConnectionRoutePlanId = this.routeId,
        sheetId: String = this.sheetId,
        connectionId: StableSemanticIdentity = this.connectionId,
        projectionConnectionId: String = this.projectionConnectionId,
        sourceAnchorId: SpatialAnchorId = this.sourceAnchorId,
        targetAnchorId: SpatialAnchorId = this.targetAnchorId,
        points: List<SpatialPoint> = this.points,
        quality: ConnectionRouteQuality = ConnectionRouteQuality.valid(points.zipWithNext(::ConnectionRouteSegment)),
        sourceTrace: SpatialSourceTrace = this.sourceTrace,
        laneId: SpatialLaneId = this.laneId,
        drawingArea: SpatialRect? = null,
    ): ConnectionRoutePlan = ConnectionRoutePlan(
        routeId, sheetId, connectionId, projectionConnectionId, sourceAnchorId, targetAnchorId, points, quality,
        sourceTrace, laneId, drawingArea,
    )

    init {
        require(sheetId.isNotBlank()) { "Connection route plan Sheet identity must not be blank." }
        require(routeId.sheetId == sheetId) { "Connection route plan identity must name its owning Sheet." }
        require(laneId.sheetId == sheetId) { "Connection route plan lane must belong to its owning Sheet." }
        require(connectionId.value.isNotBlank()) { "Connection route plan connection identity must not be blank." }
        require(projectionConnectionId.isNotBlank()) {
            "Connection route plan projection Connection identity must not be blank."
        }
        require(sourceAnchorId.sheetId == sheetId && targetAnchorId.sheetId == sheetId) {
            "Connection route plan anchors must belong to its owning Sheet."
        }
        require(sourceAnchorId != targetAnchorId) { "Connection route plan endpoints must be distinct anchors." }
        require(this.segments.isNotEmpty()) { "Connection route plan must contain at least one segment." }
        require(this.segments.first().start == this.sourcePoint(sourceAnchorId)) {
            "Connection route plan must start at the source anchor point."
        }
        require(this.segments.last().end == this.targetPoint(targetAnchorId)) {
            "Connection route plan must end at the target anchor point."
        }
        require(this.segments.zipWithNext().all { (first, second) -> first.end == second.start }) {
            "Connection route plan segments must form one continuous path."
        }
        require(quality.geometryIdentity == geometryIdentity) {
            "Connection route quality must carry the plan's stable geometry identity."
        }
        drawingArea?.let { area ->
            require(points.all { point -> point.x in area.x..area.right && point.y in area.y..area.bottom }) {
                "Connection route plan geometry must stay inside its drawing area."
            }
        }
    }

    private fun sourcePoint(anchorId: SpatialAnchorId): SpatialPoint =
        if (anchorId == sourceAnchorId) points.firstOrNull() ?: error("Missing route points") else points.last()

    private fun targetPoint(anchorId: SpatialAnchorId): SpatialPoint =
        if (anchorId == targetAnchorId) points.lastOrNull() ?: error("Missing route points") else points.first()

    override fun equals(other: Any?): Boolean =
        this === other || other is ConnectionRoutePlan &&
            routeId == other.routeId && sheetId == other.sheetId && connectionId == other.connectionId &&
            projectionConnectionId == other.projectionConnectionId &&
            sourceAnchorId == other.sourceAnchorId && targetAnchorId == other.targetAnchorId &&
            laneId == other.laneId && quality == other.quality && sourceTrace == other.sourceTrace &&
            segments == other.segments

    override fun hashCode(): Int = listOf(
        routeId, sheetId, connectionId, projectionConnectionId, sourceAnchorId, targetAnchorId, laneId, quality,
        sourceTrace, points,
    ).hashCode()

    override fun toString(): String = "ConnectionRoutePlan(routeId=$routeId, sheetId=$sheetId, connectionId=$connectionId, points=$points)"
}

private fun String.encodedRoutePart(): String = URLEncoder.encode(this, StandardCharsets.UTF_8)

private fun List<ConnectionRouteSegment>.geometryIdentity(): String = flatMapIndexed { index, segment ->
    if (index == 0) listOf(segment.start, segment.end) else listOf(segment.end)
}.joinToString(";") { point -> "${point.x},${point.y}" }
