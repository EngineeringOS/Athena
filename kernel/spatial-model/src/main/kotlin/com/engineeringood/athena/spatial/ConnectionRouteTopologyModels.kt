package com.engineeringood.athena.spatial

import com.engineeringood.athena.ir.StableSemanticIdentity
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class ConnectionJunctionId(
    val sheetId: String,
    val netId: String,
    val point: SpatialPoint,
) {
    init {
        require(sheetId.isNotBlank() && netId.isNotBlank()) { "Connection Junction identity parts must not be blank." }
    }

    val value: String
        get() = "junction:sheet=${sheetId.topologyPart()}:net=${netId.topologyPart()}:at=${point.x},${point.y}"
}

class ConnectionJunction(
    val id: ConnectionJunctionId,
    val netId: StableSemanticIdentity,
    routeIds: List<ConnectionRoutePlanId>,
    val point: SpatialPoint,
    val sourceTrace: SpatialSourceTrace,
) {
    val routeIds = routeIds.distinct().sortedBy(ConnectionRoutePlanId::value)

    init {
        require(id.netId == netId.value && id.point == point) { "Connection Junction identity must name its Net and point." }
        require(this.routeIds.size >= 2) { "Connection Junction requires at least two distinct route plans." }
        require(this.routeIds.all { it.sheetId == id.sheetId }) { "Connection Junction route plans must belong to its Sheet." }
    }

    override fun equals(other: Any?): Boolean = this === other || other is ConnectionJunction &&
        id == other.id && netId == other.netId && routeIds == other.routeIds && point == other.point && sourceTrace == other.sourceTrace
    override fun hashCode(): Int = listOf(id, netId, routeIds, point, sourceTrace).hashCode()
}

data class ConnectionCrossingId(
    val sheetId: String,
    val firstRouteId: ConnectionRoutePlanId,
    val secondRouteId: ConnectionRoutePlanId,
    val point: SpatialPoint,
) {
    init {
        require(sheetId.isNotBlank()) { "Connection Crossing Sheet identity must not be blank." }
        require(firstRouteId != secondRouteId) { "Connection Crossing requires two distinct route plans." }
        require(firstRouteId.sheetId == sheetId && secondRouteId.sheetId == sheetId) {
            "Connection Crossing route plans must belong to its Sheet."
        }
    }

    val value: String
        get() = listOf(firstRouteId, secondRouteId).sortedBy(ConnectionRoutePlanId::value).let { routes ->
            "crossing:sheet=${sheetId.topologyPart()}:routes=${routes[0].value.topologyPart()},${routes[1].value.topologyPart()}:at=${point.x},${point.y}"
        }
}

class ConnectionCrossing(
    val id: ConnectionCrossingId,
    routeIds: List<ConnectionRoutePlanId>,
    val point: SpatialPoint,
    val bridgeOwnerRouteId: ConnectionRoutePlanId,
    val sourceTrace: SpatialSourceTrace,
) {
    val routeIds = routeIds.distinct().sortedBy(ConnectionRoutePlanId::value)

    init {
        require(this.routeIds.size == 2) { "Connection Crossing requires exactly two distinct route plans." }
        require(this.routeIds.toSet() == setOf(id.firstRouteId, id.secondRouteId)) {
            "Connection Crossing identity must name its route plans."
        }
        require(id.point == point) { "Connection Crossing identity must name its point." }
        require(bridgeOwnerRouteId in this.routeIds) { "Connection Crossing bridge owner must be one crossing route plan." }
    }

    override fun equals(other: Any?): Boolean = this === other || other is ConnectionCrossing &&
        id == other.id && routeIds == other.routeIds && point == other.point &&
        bridgeOwnerRouteId == other.bridgeOwnerRouteId && sourceTrace == other.sourceTrace
    override fun hashCode(): Int = listOf(id, routeIds, point, bridgeOwnerRouteId, sourceTrace).hashCode()
}

data class ConnectionSharedSegmentId(
    val sheetId: String,
    val netId: String,
    val segment: ConnectionRouteSegment,
) {
    init {
        require(sheetId.isNotBlank() && netId.isNotBlank()) { "Shared Connection Segment identity parts must not be blank." }
        require(segment.isPositiveOrthogonal) { "Shared Connection Segment identity requires positive orthogonal geometry." }
    }

    val value: String
        get() = "shared-segment:sheet=${sheetId.topologyPart()}:net=${netId.topologyPart()}:from=${segment.start.x},${segment.start.y}:to=${segment.end.x},${segment.end.y}"
}

class ConnectionSharedSegment(
    val id: ConnectionSharedSegmentId,
    val netId: StableSemanticIdentity,
    routeIds: List<ConnectionRoutePlanId>,
    val segment: ConnectionRouteSegment,
    val sourceTrace: SpatialSourceTrace,
) {
    val routeIds = routeIds.distinct().sortedBy(ConnectionRoutePlanId::value)

    init {
        require(id.netId == netId.value && id.segment == segment) { "Shared Connection Segment identity must name its Net and geometry." }
        require(this.routeIds.size >= 2) { "Shared Connection Segment requires at least two distinct route plans." }
        require(this.routeIds.all { it.sheetId == id.sheetId }) { "Shared Connection Segment route plans must belong to its Sheet." }
    }

    override fun equals(other: Any?): Boolean = this === other || other is ConnectionSharedSegment &&
        id == other.id && netId == other.netId && routeIds == other.routeIds && segment == other.segment && sourceTrace == other.sourceTrace
    override fun hashCode(): Int = listOf(id, netId, routeIds, segment, sourceTrace).hashCode()
}

data class ConnectionInterruptionId(
    val sheetId: String,
    val semanticId: String,
    val firstContinuationId: String,
    val secondContinuationId: String,
) {
    init {
        require(listOf(sheetId, semanticId, firstContinuationId, secondContinuationId).all(String::isNotBlank)) {
            "Connection Interruption identity parts must not be blank."
        }
        require(firstContinuationId != secondContinuationId) { "Connection Interruption continuations must be distinct." }
    }

    val value: String
        get() = "interruption:sheet=${sheetId.topologyPart()}:semantic=${semanticId.topologyPart()}:continuations=${firstContinuationId.topologyPart()},${secondContinuationId.topologyPart()}"
}

class ConnectionInterruptionAnchors(
    val id: ConnectionInterruptionId,
    val semanticId: StableSemanticIdentity,
    continuationIds: List<String>,
    points: List<SpatialPoint>,
    val sourceTrace: SpatialSourceTrace,
) {
    val continuationIds = continuationIds.toList()
    val points = points.toList()

    init {
        require(id.semanticId == semanticId.value) { "Connection Interruption identity must name its semantic connection or Net." }
        require(this.continuationIds.size == 2 && this.continuationIds.distinct().size == 2) {
            "Connection Interruption requires exactly two distinct ordered continuation identities."
        }
        require(this.continuationIds == listOf(id.firstContinuationId, id.secondContinuationId)) {
            "Connection Interruption identity must retain continuation order."
        }
        require(this.points.size == 2 && this.points.distinct().size == 2) {
            "Connection Interruption requires exactly two distinct anchor points."
        }
    }

    override fun equals(other: Any?): Boolean = this === other || other is ConnectionInterruptionAnchors &&
        id == other.id && semanticId == other.semanticId && continuationIds == other.continuationIds &&
        points == other.points && sourceTrace == other.sourceTrace
    override fun hashCode(): Int = listOf(id, semanticId, continuationIds, points, sourceTrace).hashCode()
}

class ConnectionRouteTopologyPlan(
    junctions: List<ConnectionJunction> = emptyList(),
    crossings: List<ConnectionCrossing> = emptyList(),
    sharedSegments: List<ConnectionSharedSegment> = emptyList(),
    interruptions: List<ConnectionInterruptionAnchors> = emptyList(),
) {
    val junctions = junctions.sortedBy { it.id.value }
    val crossings = crossings.sortedBy { it.id.value }
    val sharedSegments = sharedSegments.sortedBy { it.id.value }
    val interruptions = interruptions.sortedBy { it.id.value }

    init {
        require(this.junctions.map { it.id }.distinct().size == this.junctions.size) { "Connection Junction identities must be unique." }
        require(this.crossings.map { it.id }.distinct().size == this.crossings.size) { "Connection Crossing identities must be unique." }
        require(this.sharedSegments.map { it.id }.distinct().size == this.sharedSegments.size) { "Shared Connection Segment identities must be unique." }
        require(this.interruptions.map { it.id }.distinct().size == this.interruptions.size) { "Connection Interruption identities must be unique." }
        require(this.junctions.map { it.point }.toSet().intersect(this.crossings.map { it.point }.toSet()).isEmpty()) {
            "One spatial point cannot be both a Connection Junction and an unconnected Crossing."
        }
    }

    override fun equals(other: Any?): Boolean = this === other || other is ConnectionRouteTopologyPlan &&
        junctions == other.junctions && crossings == other.crossings &&
        sharedSegments == other.sharedSegments && interruptions == other.interruptions
    override fun hashCode(): Int = listOf(junctions, crossings, sharedSegments, interruptions).hashCode()

    companion object {
        val EMPTY = ConnectionRouteTopologyPlan()
    }
}

private fun String.topologyPart(): String = URLEncoder.encode(this, StandardCharsets.UTF_8)
