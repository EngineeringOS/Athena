package com.engineeringood.athena.drawing.composition

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.physical.ExactMillimeters
import com.engineeringood.athena.physical.InstallationOccurrenceKey
import com.engineeringood.athena.physical.PhysicalDuct
import com.engineeringood.athena.physical.PhysicalInstallationIR
import com.engineeringood.athena.physical.PhysicalInfrastructureOrientation
import com.engineeringood.athena.physical.PhysicalMountedOccurrence
import com.engineeringood.athena.physical.PhysicalObjectId
import com.engineeringood.athena.physical.PhysicalRouteChannel
import com.engineeringood.athena.physical.PhysicalRouteIntent
import com.engineeringood.athena.physical.PhysicalSourceSpan
import kotlin.math.max
import kotlin.math.min

data class CabinetRouteEndpointRef(
    val key: InstallationOccurrenceKey,
    val anchorId: String,
    val direction: CabinetAnchorDirection = CabinetAnchorDirection.PASSIVE,
)

enum class CabinetAnchorDirection { LEFT, RIGHT, UP, DOWN, PASSIVE }

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
    val kind: CabinetRouteSegmentKind = CabinetRouteSegmentKind.CHANNEL,
    val channelIds: List<PhysicalObjectId> = emptyList(),
)

enum class CabinetRouteSegmentKind { SOURCE_STUB, CHANNEL, TARGET_STUB }

data class CabinetRouteFact(
    val connectionAlias: String,
    val orderedChannelIds: List<PhysicalObjectId>,
    val from: CabinetRouteEndpointPoint,
    val to: CabinetRouteEndpointPoint,
    val laneCentersByChannel: Map<PhysicalObjectId, ExactMillimeters>,
    val segments: List<CabinetRouteSegment>,
    val sourceDirection: CabinetAnchorDirection = CabinetAnchorDirection.PASSIVE,
    val targetDirection: CabinetAnchorDirection = CabinetAnchorDirection.PASSIVE,
)

data class CabinetRouteEvidence(
    val routeCount: Int,
    val channelUse: Map<PhysicalObjectId, Int>,
    val endpointBindingCount: Int,
    val segmentCount: Int,
    val bodyIntersectionCount: Int,
    val offChannelSegmentCount: Int,
    val unboundEndpointCount: Int,
)

data class CabinetRoutingRequest(
    val ir: PhysicalInstallationIR,
    val topology: com.engineeringood.athena.physical.RouteChannelTopology,
    val joins: List<CabinetOccurrenceVisualJoin>,
    val endpoints: List<CabinetConnectionEndpointBinding>,
    val enclosureToDrawing: CabinetTargetFrame = CabinetTargetFrame(
        origin = CabinetPointD(0.0, 0.0),
        alongAxis = CabinetVectorD(1.0, 0.0),
        normalAxis = CabinetVectorD(0.0, 1.0),
    ),
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
        val evidence: CabinetRouteEvidence,
    ) : CabinetRoutingCompilation

    data class Failure(val diagnostics: List<CabinetRoutingDiagnostic>) : CabinetRoutingCompilation
}

object CabinetRoutingCompiler {
    fun compile(request: CabinetRoutingRequest): CabinetRoutingCompilation {
        val diagnostics = mutableListOf<CabinetRoutingDiagnostic>()
        val channelById = request.ir.space.channels.associateBy { channel -> channel.id }
        val ductById = request.ir.space.ducts.associateBy { duct -> duct.id }
        val mountedByKey = request.ir.space.mountedOccurrences.associateBy { occurrence -> occurrence.key }
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
        val evidenceChannelUse = mutableMapOf<PhysicalObjectId, Int>()
        var bodyIntersectionCount = 0
        var offChannelSegmentCount = 0
        var unboundEndpointCount = 0

        routeIntents.forEach routeLoop@ { route ->
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
                return@routeLoop
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
                return@routeLoop
            }

            val orderedChannels = route.channelIds
            val laneCentersByChannel = mutableMapOf<PhysicalObjectId, ExactMillimeters>()
            val segments = mutableListOf<CabinetRouteSegment>()
            val points = mutableListOf<CabinetPointD>()
            val segmentMetadata = mutableListOf<Pair<CabinetRouteSegmentKind, List<PhysicalObjectId>>>()
            fun appendPoint(
                point: CabinetPointD,
                kind: CabinetRouteSegmentKind,
                channelIds: List<PhysicalObjectId> = emptyList(),
            ) {
                if (points.lastOrNull() == point) return
                require(points.isNotEmpty()) { "Route must start at its source anchor." }
                points += point
                segmentMetadata += kind to channelIds
            }

            val firstChannel = channelById[orderedChannels.firstOrNull()]
            if (firstChannel == null) {
                diagnostics += missingLane(route, route.connectionAlias, route.provenance.span)
                return@routeLoop
            }

            val firstAssignment = laneAssignmentByAliasChannel["${route.connectionAlias}:${firstChannel.id.value}"]
            val firstLane = firstAssignment?.let { assignment ->
                topologyLaneByChannel[firstChannel.id].orEmpty().getOrNull(assignment.laneIndex)
            }
            if (firstAssignment == null || firstLane == null) {
                diagnostics += missingLane(route, route.connectionAlias, route.provenance.span)
                return@routeLoop
            }

            points += fromAnchor.point
            val firstEntry = projectEntry(firstChannel, firstLane.center, fromAnchor.point, ductById, request.enclosureToDrawing)
            sourceApproach(fromAnchor.point, firstEntry, binding.from.direction).forEach { point ->
                appendPoint(point, CabinetRouteSegmentKind.SOURCE_STUB)
            }
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
                    return@routeLoop
                }

                laneCentersByChannel[fromId] = fromLane.center
                laneCentersByChannel[toId] = toLane.center

                val midpoint = adjacency.passableMidpoint
                val midpointPoint = request.enclosureToDrawing.apply(
                    ductLocalPoint(fromChannel, CabinetPointD(midpoint.x.toDouble(), midpoint.y.toDouble()), ductById),
                )
                appendPoint(
                    exitPoint(fromChannel, fromLane.center, midpointPoint, ductById, request.enclosureToDrawing),
                    CabinetRouteSegmentKind.CHANNEL,
                    listOf(fromId),
                )
                appendPoint(
                    entryPoint(toChannel, toLane.center, midpointPoint, ductById, request.enclosureToDrawing),
                    CabinetRouteSegmentKind.CHANNEL,
                    listOf(fromId, toId),
                )
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
                return@routeLoop
            }
            val lastExit = exitPoint(lastChannel, lastLane.center, toAnchor.point, ductById, request.enclosureToDrawing)
            appendPoint(lastExit, CabinetRouteSegmentKind.CHANNEL, listOf(lastChannel.id))
            targetApproach(lastExit, toAnchor.point, binding.to.direction).forEach { point ->
                appendPoint(point, CabinetRouteSegmentKind.TARGET_STUB)
            }

            if (!movesOutward(fromAnchor.point, points[1], binding.from.direction) ||
                !movesInto(points[points.lastIndex - 1], toAnchor.point, binding.to.direction)
            ) {
                diagnostics += diagnostic(
                    code = "cabinet.route.anchor_direction.invalid",
                    subject = route.connectionAlias,
                    measured = "${binding.from.direction}:${fromAnchor.point}->${points[1]} | " +
                        "${binding.to.direction}:${points[points.lastIndex - 1]}->${toAnchor.point}",
                    expected = "route stubs aligned with authored anchor directions",
                    span = route.provenance.span,
                )
                return@routeLoop
            }

            points
                .zipWithNext()
                .forEachIndexed { index, (from, to) ->
                    if (from == to) return@forEachIndexed
                    val (kind, channelIds) = segmentMetadata[index]
                    val routedSegments = avoidObstacles(
                        segment = CabinetRouteSegment(from, to, kind, channelIds),
                        joinsByKey = joinByKey,
                        endpointBinding = binding,
                        mountedByKey = mountedByKey,
                    )
                    segments += routedSegments
                    if (kind == CabinetRouteSegmentKind.CHANNEL && routedSegments.any { routedSegment ->
                            !routedSegment.isContainedByChannels(channelIds, channelById, ductById, request.enclosureToDrawing)
                        }
                    ) {
                        diagnostics += diagnostic(
                            code = "cabinet.route.off_channel_segment",
                            subject = route.connectionAlias,
                            measured = "$from -> $to",
                            expected = "channel route geometry contained by selected physical channels",
                            span = route.provenance.span,
                        )
                        offChannelSegmentCount++
                    }
                    val intersectedBody = routedSegments.firstNotNullOfOrNull { routedSegment ->
                        intersectingObstacleSubject(routedSegment, joinByKey, binding, mountedByKey)
                    }
                    if (intersectedBody != null) {
                        diagnostics += diagnostic(
                            code = "cabinet.route.body_intersection",
                            subject = route.connectionAlias,
                            measured = "${from} -> ${to} intersects $intersectedBody",
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
                evidenceChannelUse[channelId] = (evidenceChannelUse[channelId] ?: 0) + 1
            }

            routeFacts += CabinetRouteFact(
                connectionAlias = route.connectionAlias,
                orderedChannelIds = orderedChannels,
                from = CabinetRouteEndpointPoint(binding.from.key, binding.from.anchorId, fromAnchor.point),
                to = CabinetRouteEndpointPoint(binding.to.key, binding.to.anchorId, toAnchor.point),
                laneCentersByChannel = laneCentersByChannel.toSortedMap(compareBy { it.value }),
                segments = segments,
                sourceDirection = binding.from.direction,
                targetDirection = binding.to.direction,
            )
        }

        if (diagnostics.isNotEmpty()) {
            return CabinetRoutingCompilation.Failure(
                diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.measured.orEmpty() })),
            )
        }

        return CabinetRoutingCompilation.Success(
            routes = routeFacts,
            evidence = CabinetRouteEvidence(
                routeCount = routeFacts.size,
                channelUse = evidenceChannelUse.toSortedMap(compareBy { it.value }),
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
    channel: PhysicalRouteChannel,
    laneCenter: ExactMillimeters,
    anchor: CabinetPointD,
    ducts: Map<PhysicalObjectId, PhysicalDuct>,
    frame: CabinetTargetFrame,
): CabinetPointD {
    val localAnchor = frame.inverse(anchor)
    return frame.apply(when (channel.orientation) {
    PhysicalInfrastructureOrientation.Horizontal -> CabinetPointD(
        x = clamp(localAnchor.x, channelGlobalLeft(channel, ducts), channelGlobalRight(channel, ducts)),
        y = channelGlobalTop(channel, ducts) + laneCenter.toDouble(),
    )
    PhysicalInfrastructureOrientation.Vertical -> CabinetPointD(
        x = channelGlobalLeft(channel, ducts) + laneCenter.toDouble(),
        y = clamp(localAnchor.y, channelGlobalTop(channel, ducts), channelGlobalBottom(channel, ducts)),
    )
    })
}

private fun exitPoint(
    channel: PhysicalRouteChannel,
    laneCenter: ExactMillimeters,
    boundary: CabinetPointD,
    ducts: Map<PhysicalObjectId, PhysicalDuct>,
    frame: CabinetTargetFrame,
): CabinetPointD {
    val localBoundary = frame.inverse(boundary)
    return frame.apply(when (channel.orientation) {
        PhysicalInfrastructureOrientation.Horizontal -> CabinetPointD(
            clamp(localBoundary.x, channelGlobalLeft(channel, ducts), channelGlobalRight(channel, ducts)),
            channelGlobalTop(channel, ducts) + laneCenter.toDouble(),
        )
        PhysicalInfrastructureOrientation.Vertical -> CabinetPointD(
            channelGlobalLeft(channel, ducts) + laneCenter.toDouble(),
            clamp(localBoundary.y, channelGlobalTop(channel, ducts), channelGlobalBottom(channel, ducts)),
        )
    })
}

private fun entryPoint(
    channel: PhysicalRouteChannel,
    laneCenter: ExactMillimeters,
    boundary: CabinetPointD,
    ducts: Map<PhysicalObjectId, PhysicalDuct>,
    frame: CabinetTargetFrame,
): CabinetPointD {
    val localBoundary = frame.inverse(boundary)
    return frame.apply(when (channel.orientation) {
        PhysicalInfrastructureOrientation.Horizontal -> CabinetPointD(
            clamp(localBoundary.x, channelGlobalLeft(channel, ducts), channelGlobalRight(channel, ducts)),
            channelGlobalTop(channel, ducts) + laneCenter.toDouble(),
        )
        PhysicalInfrastructureOrientation.Vertical -> CabinetPointD(
            channelGlobalLeft(channel, ducts) + laneCenter.toDouble(),
            clamp(localBoundary.y, channelGlobalTop(channel, ducts), channelGlobalBottom(channel, ducts)),
        )
    })
}

private fun intersectingObstacleSubject(
    segment: CabinetRouteSegment,
    joinsByKey: Map<InstallationOccurrenceKey, CabinetOccurrenceVisualJoin>,
    endpointBinding: CabinetConnectionEndpointBinding,
    mountedByKey: Map<InstallationOccurrenceKey, PhysicalMountedOccurrence>,
): String? {
    return intersectingObstacle(segment, joinsByKey, endpointBinding, mountedByKey)?.subject
}

private data class RouteObstacle(
    val subject: String,
    val bounds: CabinetRectD,
)

private fun intersectingObstacle(
    segment: CabinetRouteSegment,
    joinsByKey: Map<InstallationOccurrenceKey, CabinetOccurrenceVisualJoin>,
    endpointBinding: CabinetConnectionEndpointBinding,
    mountedByKey: Map<InstallationOccurrenceKey, PhysicalMountedOccurrence>,
): RouteObstacle? = intersectingObstacles(segment, joinsByKey, endpointBinding, mountedByKey).firstOrNull()

private fun intersectingObstacles(
    segment: CabinetRouteSegment,
    joinsByKey: Map<InstallationOccurrenceKey, CabinetOccurrenceVisualJoin>,
    endpointBinding: CabinetConnectionEndpointBinding,
    mountedByKey: Map<InstallationOccurrenceKey, PhysicalMountedOccurrence>,
): List<RouteObstacle> {
    val ignored = setOf(endpointBinding.from.key, endpointBinding.to.key)
    return joinsByKey.values
        .asSequence()
	        .filterNot { join -> join.key in ignored }
        .filterNot { join ->
            join.anchors.any { anchor -> anchor.point == segment.from || anchor.point == segment.to }
        }
        .filter { join ->
            val clearance = mountedByKey[join.key]?.contract?.clearance
            val bounds = clearance?.let { join.body.bounds.inflate(it) } ?: join.body.bounds
            segmentIntersectsRect(segment, bounds)
        }
        .map { join ->
            val clearance = mountedByKey[join.key]?.contract?.clearance
            val bounds = clearance?.let { join.body.bounds.inflate(it) } ?: join.body.bounds
            RouteObstacle(join.key.canonicalSemanticSubjectId.value, bounds)
        }
        .sortedBy { obstacle -> obstacle.subject }
        .toList()
}

private fun avoidObstacles(
    segment: CabinetRouteSegment,
    joinsByKey: Map<InstallationOccurrenceKey, CabinetOccurrenceVisualJoin>,
    endpointBinding: CabinetConnectionEndpointBinding,
    mountedByKey: Map<InstallationOccurrenceKey, PhysicalMountedOccurrence>,
): List<CabinetRouteSegment> {
    if (intersectingObstacle(segment, joinsByKey, endpointBinding, mountedByKey) == null) return listOf(segment)
    val candidates = detourCandidates(segment, joinsByKey, endpointBinding, mountedByKey)
    return candidates.firstOrNull { candidate ->
        candidate.all { routed ->
            intersectingObstacle(routed, joinsByKey, endpointBinding, mountedByKey) == null
        }
    } ?: listOf(segment)
}

private fun detourCandidates(
    segment: CabinetRouteSegment,
    joinsByKey: Map<InstallationOccurrenceKey, CabinetOccurrenceVisualJoin>,
    endpointBinding: CabinetConnectionEndpointBinding,
    mountedByKey: Map<InstallationOccurrenceKey, PhysicalMountedOccurrence>,
): List<List<CabinetRouteSegment>> {
    val obstacles = intersectingObstacles(segment, joinsByKey, endpointBinding, mountedByKey)
    if (obstacles.isEmpty()) return emptyList()
    val clearance = 6.0
    return when {
        segment.from.y == segment.to.y -> {
            val yAbove = obstacles.minOf { obstacle -> obstacle.bounds.y } - clearance
            val yBelow = obstacles.maxOf { obstacle -> obstacle.bounds.bottom } + clearance
            listOf(yAbove, yBelow).map { y ->
                orthogonalSegments(segment, segment.from, CabinetPointD(segment.from.x, y), CabinetPointD(segment.to.x, y), segment.to)
            }
        }

        segment.from.x == segment.to.x -> {
            val xLeft = obstacles.minOf { obstacle -> obstacle.bounds.x } - clearance
            val xRight = obstacles.maxOf { obstacle -> obstacle.bounds.right } + clearance
            listOf(xLeft, xRight).map { x ->
                orthogonalSegments(segment, segment.from, CabinetPointD(x, segment.from.y), CabinetPointD(x, segment.to.y), segment.to)
            }
        }

        else -> emptyList()
    }
}

private fun orthogonalSegments(
    template: CabinetRouteSegment,
    first: CabinetPointD,
    second: CabinetPointD,
    third: CabinetPointD,
    fourth: CabinetPointD,
): List<CabinetRouteSegment> = listOf(first, second, third, fourth)
    .zipWithNext()
    .filterNot { (from, to) -> from == to }
    .map { (from, to) -> template.copy(from = from, to = to) }

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
    channel: PhysicalRouteChannel,
    ducts: Map<PhysicalObjectId, PhysicalDuct>,
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

private fun CabinetRouteSegment.isContainedByChannels(
    channelIds: List<PhysicalObjectId>,
    channels: Map<PhysicalObjectId, PhysicalRouteChannel>,
    ducts: Map<PhysicalObjectId, PhysicalDuct>,
    frame: CabinetTargetFrame,
): Boolean {
    if (from.x != to.x && from.y != to.y) return false
    val bounds = channelIds.mapNotNull { id -> channels[id]?.let { channel -> frame.apply(channelBounds(channel, ducts)) } }
    if (bounds.isEmpty()) return false
    val midpoint = CabinetPointD((from.x + to.x) / 2.0, (from.y + to.y) / 2.0)
    return listOf(from, midpoint, to).all { point -> bounds.any { rect -> rect.contains(point) } }
}

private fun CabinetTargetFrame.apply(rect: CabinetRectD): CabinetRectD {
    val corners = listOf(
        apply(CabinetPointD(rect.x, rect.y)),
        apply(CabinetPointD(rect.right, rect.y)),
        apply(CabinetPointD(rect.right, rect.bottom)),
        apply(CabinetPointD(rect.x, rect.bottom)),
    )
    val minX = corners.minOf { it.x }
    val minY = corners.minOf { it.y }
    return CabinetRectD(minX, minY, corners.maxOf { it.x } - minX, corners.maxOf { it.y } - minY)
}

private fun CabinetRectD.contains(point: CabinetPointD): Boolean {
    val epsilon = 1e-6
    return point.x >= x - epsilon && point.x <= right + epsilon &&
        point.y >= y - epsilon && point.y <= bottom + epsilon
}

private fun movesOutward(from: CabinetPointD, to: CabinetPointD, direction: CabinetAnchorDirection): Boolean = when (direction) {
    CabinetAnchorDirection.LEFT -> to.x < from.x && to.y == from.y
    CabinetAnchorDirection.RIGHT -> to.x > from.x && to.y == from.y
    CabinetAnchorDirection.UP -> to.y < from.y && to.x == from.x
    CabinetAnchorDirection.DOWN -> to.y > from.y && to.x == from.x
    CabinetAnchorDirection.PASSIVE -> true
}

private fun sourceApproach(
    anchor: CabinetPointD,
    channelEntry: CabinetPointD,
    direction: CabinetAnchorDirection,
): List<CabinetPointD> {
    if (movesOutward(anchor, channelEntry, direction)) return listOf(channelEntry)
    if (direction == CabinetAnchorDirection.PASSIVE) return manhattanApproach(anchor, channelEntry)

    val escape = anchor.offset(direction, ENDPOINT_ESCAPE_MILLIMETERS)
    val bend = when (direction) {
        CabinetAnchorDirection.LEFT,
        CabinetAnchorDirection.RIGHT,
        -> CabinetPointD(escape.x, channelEntry.y)
        CabinetAnchorDirection.UP,
        CabinetAnchorDirection.DOWN,
        -> CabinetPointD(channelEntry.x, escape.y)
        CabinetAnchorDirection.PASSIVE -> error("Passive direction handled above.")
    }
    return listOf(escape, bend, channelEntry).withoutConsecutiveDuplicates()
}

private fun targetApproach(
    channelExit: CabinetPointD,
    anchor: CabinetPointD,
    direction: CabinetAnchorDirection,
): List<CabinetPointD> {
    if (movesInto(channelExit, anchor, direction)) return listOf(anchor)
    if (direction == CabinetAnchorDirection.PASSIVE) return manhattanApproach(channelExit, anchor)

    val approach = anchor.offset(direction, ENDPOINT_ESCAPE_MILLIMETERS)
    val bend = when (direction) {
        CabinetAnchorDirection.LEFT,
        CabinetAnchorDirection.RIGHT,
        -> CabinetPointD(channelExit.x, approach.y)
        CabinetAnchorDirection.UP,
        CabinetAnchorDirection.DOWN,
        -> CabinetPointD(approach.x, channelExit.y)
        CabinetAnchorDirection.PASSIVE -> error("Passive direction handled above.")
    }
    return listOf(bend, approach, anchor).withoutConsecutiveDuplicates()
}

private fun manhattanApproach(from: CabinetPointD, to: CabinetPointD): List<CabinetPointD> =
    listOf(CabinetPointD(from.x, to.y), to).withoutConsecutiveDuplicates()

private fun CabinetPointD.offset(direction: CabinetAnchorDirection, distance: Double): CabinetPointD = when (direction) {
    CabinetAnchorDirection.LEFT -> copy(x = x - distance)
    CabinetAnchorDirection.RIGHT -> copy(x = x + distance)
    CabinetAnchorDirection.UP -> copy(y = y - distance)
    CabinetAnchorDirection.DOWN -> copy(y = y + distance)
    CabinetAnchorDirection.PASSIVE -> this
}

private fun List<CabinetPointD>.withoutConsecutiveDuplicates(): List<CabinetPointD> =
    fold(emptyList()) { points, point -> if (points.lastOrNull() == point) points else points + point }

private fun movesInto(from: CabinetPointD, to: CabinetPointD, direction: CabinetAnchorDirection): Boolean = when (direction) {
    CabinetAnchorDirection.LEFT -> from.x < to.x && from.y == to.y
    CabinetAnchorDirection.RIGHT -> from.x > to.x && from.y == to.y
    CabinetAnchorDirection.UP -> from.y < to.y && from.x == to.x
    CabinetAnchorDirection.DOWN -> from.y > to.y && from.x == to.x
    CabinetAnchorDirection.PASSIVE -> true
}

private const val ENDPOINT_ESCAPE_MILLIMETERS = 4.0

private fun CabinetTargetFrame.apply(point: CabinetPointD): CabinetPointD = CabinetPointD(
    x = origin.x + alongAxis.x * point.x + normalAxis.x * point.y,
    y = origin.y + alongAxis.y * point.x + normalAxis.y * point.y,
)

private fun CabinetTargetFrame.inverse(point: CabinetPointD): CabinetPointD {
    require(kotlin.math.abs(determinant) > 1e-9) { "Cabinet route frame must be invertible." }
    val dx = point.x - origin.x
    val dy = point.y - origin.y
    return CabinetPointD(
        x = (dx * normalAxis.y - dy * normalAxis.x) / determinant,
        y = (alongAxis.x * dy - alongAxis.y * dx) / determinant,
    )
}

private fun ductLocalPoint(
    channel: PhysicalRouteChannel,
    point: CabinetPointD,
    ducts: Map<PhysicalObjectId, PhysicalDuct>,
): CabinetPointD {
    val duct = ducts.getValue(channel.ductId)
    val inset = duct.wall.value
    return CabinetPointD(
        x = duct.at.x + inset + point.x,
        y = duct.at.y + inset + point.y,
    )
}

private fun channelGlobalLeft(channel: PhysicalRouteChannel, ducts: Map<PhysicalObjectId, PhysicalDuct>): Double =
    channelBounds(channel, ducts).x

private fun channelGlobalRight(channel: PhysicalRouteChannel, ducts: Map<PhysicalObjectId, PhysicalDuct>): Double =
    channelBounds(channel, ducts).right

private fun channelGlobalTop(channel: PhysicalRouteChannel, ducts: Map<PhysicalObjectId, PhysicalDuct>): Double =
    channelBounds(channel, ducts).y

private fun channelGlobalBottom(channel: PhysicalRouteChannel, ducts: Map<PhysicalObjectId, PhysicalDuct>): Double =
    channelBounds(channel, ducts).bottom

private fun adjacencyKey(from: PhysicalObjectId, to: PhysicalObjectId): String = "${from.value}->${to.value}"

private fun missingLane(
    route: PhysicalRouteIntent,
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

private fun CabinetRectD.inflate(
    clearance: com.engineeringood.athena.physical.PhysicalInstallationClearance,
): CabinetRectD = CabinetRectD(
    x = x - clearance.left.value.toDouble(),
    y = y - clearance.top.value.toDouble(),
    width = width + clearance.left.value + clearance.right.value,
    height = height + clearance.top.value + clearance.bottom.value,
)
