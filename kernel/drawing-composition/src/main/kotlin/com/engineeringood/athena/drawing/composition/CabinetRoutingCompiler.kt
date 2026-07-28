package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.physical.ExactMillimeters
import com.engineeringood.athena.physical.InstallationOccurrenceKey
import com.engineeringood.athena.physical.PhysicalDuctV0
import com.engineeringood.athena.physical.PhysicalInstallationIRV0
import com.engineeringood.athena.physical.PhysicalInfrastructureOrientation
import com.engineeringood.athena.physical.PhysicalObjectId
import com.engineeringood.athena.physical.PhysicalRouteChannelV0
import com.engineeringood.athena.physical.PhysicalRouteIntentV0
import com.engineeringood.athena.physical.PhysicalSourceSpan
import kotlin.math.max
import kotlin.math.min

data class CabinetRouteEndpointRef(
    val key: InstallationOccurrenceKey,
    val anchorId: String,
)

data class CabinetConnectionEndpointBinding(
    val connectionAlias: String,
    val from: CabinetRouteEndpointRef,
    val to: CabinetRouteEndpointRef,
)

data class CabinetRouteEndpointPoint(
    val key: InstallationOccurrenceKey,
    val anchorId: String,
    val point: CabinetPointD,
)

data class CabinetRouteSegment(
    val from: CabinetPointD,
    val to: CabinetPointD,
)

data class CabinetRouteFact(
    val connectionAlias: String,
    val orderedChannelIds: List<PhysicalObjectId>,
    val from: CabinetRouteEndpointPoint,
    val to: CabinetRouteEndpointPoint,
    val laneCentersByChannel: Map<PhysicalObjectId, ExactMillimeters>,
    val segments: List<CabinetRouteSegment>,
)

data class CabinetRouteProof(
    val routeCount: Int,
    val channelUse: Map<PhysicalObjectId, Int>,
    val endpointBindingCount: Int,
    val segmentCount: Int,
    val bodyIntersectionCount: Int,
    val offChannelSegmentCount: Int,
    val unboundEndpointCount: Int,
)

data class CabinetRoutingRequest(
    val ir: PhysicalInstallationIRV0,
    val topology: com.engineeringood.athena.physical.RouteChannelTopology,
    val joins: List<CabinetOccurrenceVisualJoin>,
    val endpoints: List<CabinetConnectionEndpointBinding>,
)

data class CabinetRoutingDiagnostic(
    val code: String,
    val subject: String,
    val measured: String?,
    val expected: String,
    val span: PhysicalSourceSpan?,
)

sealed interface CabinetRoutingCompilation {
    data class Success(
        val routes: List<CabinetRouteFact>,
        val proof: CabinetRouteProof,
    ) : CabinetRoutingCompilation

    data class Failure(val diagnostics: List<CabinetRoutingDiagnostic>) : CabinetRoutingCompilation
}

object CabinetRoutingCompiler {
    fun compile(request: CabinetRoutingRequest): CabinetRoutingCompilation {
        val diagnostics = mutableListOf<CabinetRoutingDiagnostic>()
        val channelById = request.ir.space.channels.associateBy { channel -> channel.id }
        val ductById = request.ir.space.ducts.associateBy { duct -> duct.id }
        val topologyById = request.topology.channels.associateBy { channel -> channel.channelId }
        val topologyLaneByChannel = request.topology.lanes.groupBy { lane -> lane.channelId }
        val laneAssignmentByAliasChannel = request.topology.laneAssignments.associateBy { assignment ->
            "${assignment.connectionAlias}:${assignment.channelId.value}"
        }
        val topologyAdjacency = request.topology.adjacencies.associateBy { adjacencyKey(it.fromChannelId, it.toChannelId) }
        val joinByKey = request.joins.associateBy { join -> join.key }
        val endpointBindings = request.endpoints.associateBy { endpoint -> endpoint.connectionAlias }
        val routeIntents = request.ir.routes.sortedBy { route -> route.connectionAlias }

        val routeFacts = mutableListOf<CabinetRouteFact>()
        val proofChannelUse = mutableMapOf<PhysicalObjectId, Int>()
        var bodyIntersectionCount = 0
        var offChannelSegmentCount = 0
        var unboundEndpointCount = 0

        routeIntents.forEach { route ->
            val binding = endpointBindings[route.connectionAlias]
            if (binding == null) {
                diagnostics += diagnostic(
                    code = "cabinet.route.endpoint.unbound_anchor",
                    subject = route.connectionAlias,
                    measured = null,
                    expected = "one endpoint binding for alias",
                    span = route.provenance.span,
                )
                unboundEndpointCount++
                return@forEach
            }

            val fromJoin = joinByKey[binding.from.key]
            val toJoin = joinByKey[binding.to.key]
            val fromAnchor = fromJoin?.anchors?.firstOrNull { anchor -> anchor.id == binding.from.anchorId }
            val toAnchor = toJoin?.anchors?.firstOrNull { anchor -> anchor.id == binding.to.anchorId }
            if (fromJoin == null || fromAnchor == null || toJoin == null || toAnchor == null) {
                diagnostics += diagnostic(
                    code = "cabinet.route.endpoint.unbound_anchor",
                    subject = route.connectionAlias,
                    measured = listOfNotNull(
                        fromJoin?.key?.canonicalSemanticSubjectId?.value,
                        fromAnchor?.id,
                        toJoin?.key?.canonicalSemanticSubjectId?.value,
                        toAnchor?.id,
                    ).joinToString("|").takeIf { it.isNotBlank() },
                    expected = "explicit transformed anchors for both endpoints",
                    span = route.provenance.span,
                )
                unboundEndpointCount++
                return@forEach
            }

            val orderedChannels = route.channelIds
            val laneCentersByChannel = mutableMapOf<PhysicalObjectId, ExactMillimeters>()
            val segments = mutableListOf<CabinetRouteSegment>()
            val points = mutableListOf<CabinetPointD>()

            val firstChannel = channelById[orderedChannels.firstOrNull()]
            if (firstChannel == null) {
                diagnostics += missingLane(route, route.connectionAlias, route.provenance.span)
                return@forEach
            }

            val firstAssignment = laneAssignmentByAliasChannel["${route.connectionAlias}:${firstChannel.id.value}"]
            val firstLane = firstAssignment?.let { assignment ->
                topologyLaneByChannel[firstChannel.id].orEmpty().getOrNull(assignment.laneIndex)
            }
            if (firstAssignment == null || firstLane == null) {
                diagnostics += missingLane(route, route.connectionAlias, route.provenance.span)
                return@forEach
            }

            points += fromAnchor.point
            points += projectEntry(firstChannel, firstLane.center, fromAnchor.point, ductById)
            laneCentersByChannel[firstChannel.id] = firstLane.center

            orderedChannels.zipWithNext().forEach { (fromId, toId) ->
                val fromChannel = channelById[fromId]
                val toChannel = channelById[toId]
                val adjacency = topologyAdjacency[adjacencyKey(fromId, toId)]
                val fromAssignment = laneAssignmentByAliasChannel["${route.connectionAlias}:${fromId.value}"]
                val toAssignment = laneAssignmentByAliasChannel["${route.connectionAlias}:${toId.value}"]
                val fromLane = fromAssignment?.let { assignment ->
                    topologyLaneByChannel[fromId].orEmpty().getOrNull(assignment.laneIndex)
                }
                val toLane = toAssignment?.let { assignment ->
                    topologyLaneByChannel[toId].orEmpty().getOrNull(assignment.laneIndex)
                }
                if (fromChannel == null || toChannel == null || adjacency == null || fromLane == null || toLane == null) {
                    diagnostics += diagnostic(
                        code = if (fromAssignment == null || toAssignment == null) {
                            "cabinet.route.lane_assignment.missing"
                        } else if (adjacency == null) {
                            "cabinet.route.adjacency.missing"
                        } else {
                            "cabinet.route.capacity.missing"
                        },
                        subject = "$fromId->$toId",
                        measured = null,
                        expected = "same-duct passable adjacency and lane assignment",
                        span = route.provenance.span,
                    )
                    return@forEach
                }

                laneCentersByChannel[fromId] = fromLane.center
                laneCentersByChannel[toId] = toLane.center

                val midpoint = adjacency.passableMidpoint
                val midpointPoint = ductLocalPoint(fromChannel, CabinetPointD(midpoint.x.toDouble(), midpoint.y.toDouble()), ductById)
                points += exitPoint(fromChannel, fromLane.center, midpointPoint, ductById)
                points += entryPoint(toChannel, toLane.center, midpointPoint, ductById)
            }

            val lastChannel = channelById[orderedChannels.last()]
            val lastAssignment = lastChannel?.let { channel ->
                laneAssignmentByAliasChannel["${route.connectionAlias}:${channel.id.value}"]
            }
            val lastLane = lastAssignment?.let { assignment ->
                topologyLaneByChannel[lastChannel.id].orEmpty().getOrNull(assignment.laneIndex)
            }
            if (lastChannel == null || lastAssignment == null || lastLane == null) {
                diagnostics += missingLane(route, route.connectionAlias, route.provenance.span)
                return@forEach
            }
            points += exitPoint(lastChannel, lastLane.center, toAnchor.point, ductById)
            points += toAnchor.point

            points
                .zipWithNext()
                .filterNot { (from, to) -> from == to }
                .forEach { (from, to) ->
                    val segment = CabinetRouteSegment(from, to)
                    segments += segment
                    if (intersectsAnyObstacle(segment, joinByKey, binding)) {
                        diagnostics += diagnostic(
                            code = "cabinet.route.body_intersection",
                            subject = route.connectionAlias,
                            measured = "${segment.from} -> ${segment.to}",
                            expected = "zero non-endpoint body intersections",
                            span = route.provenance.span,
                        )
                        bodyIntersectionCount++
                    }
                }

            if (orderedChannels.any { channelId -> channelId !in topologyById }) {
                diagnostics += diagnostic(
                    code = "cabinet.route.off_channel_segment",
                    subject = route.connectionAlias,
                    measured = orderedChannels.joinToString(",") { it.value },
                    expected = "all channels admitted by topology",
                    span = route.provenance.span,
                )
                offChannelSegmentCount++
            }

            orderedChannels.distinct().forEach { channelId ->
                proofChannelUse[channelId] = (proofChannelUse[channelId] ?: 0) + 1
            }

            routeFacts += CabinetRouteFact(
                connectionAlias = route.connectionAlias,
                orderedChannelIds = orderedChannels,
                from = CabinetRouteEndpointPoint(binding.from.key, binding.from.anchorId, fromAnchor.point),
                to = CabinetRouteEndpointPoint(binding.to.key, binding.to.anchorId, toAnchor.point),
                laneCentersByChannel = laneCentersByChannel.toSortedMap(compareBy { it.value }),
                segments = segments,
            )
        }

        if (diagnostics.isNotEmpty()) {
            return CabinetRoutingCompilation.Failure(
                diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.measured.orEmpty() })),
            )
        }

        return CabinetRoutingCompilation.Success(
            routes = routeFacts,
            proof = CabinetRouteProof(
                routeCount = routeFacts.size,
                channelUse = proofChannelUse.toSortedMap(compareBy { it.value }),
                endpointBindingCount = routeFacts.size * 2,
                segmentCount = routeFacts.sumOf { route -> route.segments.size },
                bodyIntersectionCount = bodyIntersectionCount,
                offChannelSegmentCount = offChannelSegmentCount,
                unboundEndpointCount = unboundEndpointCount,
            ),
        )
    }
}

private fun projectEntry(
    channel: PhysicalRouteChannelV0,
    laneCenter: ExactMillimeters,
    anchor: CabinetPointD,
    ducts: Map<PhysicalObjectId, PhysicalDuctV0>,
): CabinetPointD = when (channel.orientation) {
    PhysicalInfrastructureOrientation.Horizontal -> CabinetPointD(
        x = clamp(anchor.x, channelGlobalLeft(channel, ducts), channelGlobalRight(channel, ducts)),
        y = channelGlobalTop(channel, ducts) + laneCenter.toDouble(),
    )
    PhysicalInfrastructureOrientation.Vertical -> CabinetPointD(
        x = channelGlobalLeft(channel, ducts) + laneCenter.toDouble(),
        y = clamp(anchor.y, channelGlobalTop(channel, ducts), channelGlobalBottom(channel, ducts)),
    )
}

private fun exitPoint(
    channel: PhysicalRouteChannelV0,
    laneCenter: ExactMillimeters,
    boundary: CabinetPointD,
    ducts: Map<PhysicalObjectId, PhysicalDuctV0>,
): CabinetPointD = when (channel.orientation) {
    PhysicalInfrastructureOrientation.Horizontal -> CabinetPointD(boundary.x, channelGlobalTop(channel, ducts) + laneCenter.toDouble())
    PhysicalInfrastructureOrientation.Vertical -> CabinetPointD(channelGlobalLeft(channel, ducts) + laneCenter.toDouble(), boundary.y)
}

private fun entryPoint(
    channel: PhysicalRouteChannelV0,
    laneCenter: ExactMillimeters,
    boundary: CabinetPointD,
    ducts: Map<PhysicalObjectId, PhysicalDuctV0>,
): CabinetPointD = when (channel.orientation) {
    PhysicalInfrastructureOrientation.Horizontal -> CabinetPointD(boundary.x, channelGlobalTop(channel, ducts) + laneCenter.toDouble())
    PhysicalInfrastructureOrientation.Vertical -> CabinetPointD(channelGlobalLeft(channel, ducts) + laneCenter.toDouble(), boundary.y)
}

private fun intersectsAnyObstacle(
    segment: CabinetRouteSegment,
    joinsByKey: Map<InstallationOccurrenceKey, CabinetOccurrenceVisualJoin>,
    endpointBinding: CabinetConnectionEndpointBinding,
): Boolean {
    val ignored = setOf(endpointBinding.from.key, endpointBinding.to.key)
    return joinsByKey.values
        .asSequence()
        .filterNot { join -> join.key in ignored }
        .any { join -> segmentIntersectsRect(segment, join.body.bounds) }
}

private fun segmentIntersectsRect(segment: CabinetRouteSegment, rect: CabinetRectD): Boolean {
    if (segment.from.x == segment.to.x) {
        val x = segment.from.x
        val y1 = min(segment.from.y, segment.to.y)
        val y2 = max(segment.from.y, segment.to.y)
        return x > rect.x && x < rect.right && y2 > rect.y && y1 < rect.bottom
    }
    if (segment.from.y == segment.to.y) {
        val y = segment.from.y
        val x1 = min(segment.from.x, segment.to.x)
        val x2 = max(segment.from.x, segment.to.x)
        return y > rect.y && y < rect.bottom && x2 > rect.x && x1 < rect.right
    }
    return false
}

private fun channelBounds(
    channel: PhysicalRouteChannelV0,
    ducts: Map<PhysicalObjectId, PhysicalDuctV0>,
): CabinetRectD {
    val duct = ducts.getValue(channel.ductId)
    val origin = duct.at
    val inset = duct.wall.value
    return CabinetRectD(
        x = (origin.x + inset + channel.at.x).toDouble(),
        y = (origin.y + inset + channel.at.y).toDouble(),
        width = channel.size.width.toDouble(),
        height = channel.size.height.toDouble(),
    )
}

private fun ductLocalPoint(
    channel: PhysicalRouteChannelV0,
    point: CabinetPointD,
    ducts: Map<PhysicalObjectId, PhysicalDuctV0>,
): CabinetPointD {
    val duct = ducts.getValue(channel.ductId)
    val inset = duct.wall.value
    return CabinetPointD(
        x = duct.at.x + inset + point.x,
        y = duct.at.y + inset + point.y,
    )
}

private fun channelGlobalLeft(channel: PhysicalRouteChannelV0, ducts: Map<PhysicalObjectId, PhysicalDuctV0>): Double =
    channelBounds(channel, ducts).x

private fun channelGlobalRight(channel: PhysicalRouteChannelV0, ducts: Map<PhysicalObjectId, PhysicalDuctV0>): Double =
    channelBounds(channel, ducts).right

private fun channelGlobalTop(channel: PhysicalRouteChannelV0, ducts: Map<PhysicalObjectId, PhysicalDuctV0>): Double =
    channelBounds(channel, ducts).y

private fun channelGlobalBottom(channel: PhysicalRouteChannelV0, ducts: Map<PhysicalObjectId, PhysicalDuctV0>): Double =
    channelBounds(channel, ducts).bottom

private fun adjacencyKey(from: PhysicalObjectId, to: PhysicalObjectId): String = "${from.value}->${to.value}"

private fun missingLane(
    route: PhysicalRouteIntentV0,
    subject: String,
    span: PhysicalSourceSpan?,
): CabinetRoutingDiagnostic = diagnostic(
    code = "cabinet.route.lane_assignment.missing",
    subject = subject,
    measured = route.channelIds.joinToString(",") { it.value },
    expected = "one lane assignment per authored channel",
    span = span,
)

private fun diagnostic(
    code: String,
    subject: String,
    measured: String?,
    expected: String,
    span: PhysicalSourceSpan?,
): CabinetRoutingDiagnostic = CabinetRoutingDiagnostic(code, subject, measured, expected, span)

private fun clamp(value: Double, minValue: Double, maxValue: Double): Double =
    when {
        value < minValue -> minValue
        value > maxValue -> maxValue
        else -> value
    }

private fun ExactMillimeters.toDouble(): Double = numerator.toDouble() / denominator.toDouble()
