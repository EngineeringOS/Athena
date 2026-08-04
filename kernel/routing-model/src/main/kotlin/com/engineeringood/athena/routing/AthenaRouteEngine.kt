package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId

/** Minimal schematic layout context consumed by the Athena route engine. */
data class SchematicRoutingLayoutContext(
    val gridSize: Int,
    val routeLaneCapacity: Int = 1,
    val routeLaneCount: Int = Int.MAX_VALUE,
    val routeLaneSpacing: Int = gridSize,
    val routeLaneOrientation: RouteLaneOrientation = RouteLaneOrientation.MIXED,
) {
    init {
        require(gridSize > 0) { "Schematic routing grid size must be positive." }
        require(routeLaneCapacity > 0) { "Route lane capacity must be positive." }
        require(routeLaneCount > 0) { "Route lane count must be positive." }
        require(routeLaneSpacing > 0) { "Route lane spacing must be positive." }
    }
}

/** Component bounds supplied to route solving so route decisions are not center-to-center edges. */
data class SchematicComponentBounds(
    val subjectId: StableSemanticIdentity,
    val occurrenceId: LayoutOccurrenceId,
    val topLeft: SchematicRoutePoint,
    val width: Int,
    val height: Int,
) {
    init {
        require(width > 0 && height > 0) { "Schematic component bounds must use positive size." }
    }

    val center: SchematicRoutePoint
        get() = SchematicRoutePoint(
            x = topLeft.x + width / 2,
            y = topLeft.y + height / 2,
        )

    fun intersects(segment: SchematicRouteSegment): Boolean {
        val minX = topLeft.x
        val maxX = topLeft.x + width
        val minY = topLeft.y
        val maxY = topLeft.y + height
        return when (segment.orientation) {
            SchematicRouteSegmentOrientation.HORIZONTAL -> {
                val y = segment.start.y
                val segmentMinX = minOf(segment.start.x, segment.end.x)
                val segmentMaxX = maxOf(segment.start.x, segment.end.x)
                y in minY..maxY && segmentMaxX >= minX && segmentMinX <= maxX
            }
            SchematicRouteSegmentOrientation.VERTICAL -> {
                val x = segment.start.x
                val segmentMinY = minOf(segment.start.y, segment.end.y)
                val segmentMaxY = maxOf(segment.start.y, segment.end.y)
                x in minX..maxX && segmentMaxY >= minY && segmentMinY <= maxY
            }
        }
    }
}

/** One governed route request consumed by the Athena route engine. */
data class AthenaRouteRequest(
    val routeId: SchematicRouteId,
    val connectionRoleFact: ElectricalConnectionRoleFact,
    val sourceAnchor: TerminalAnchorFact,
    val targetAnchor: TerminalAnchorFact,
    val bundleId: RouteBundleId? = null,
    val constraints: List<RouteConstraint> = emptyList(),
)

/** Route engine input for one schematic snapshot. */
data class AthenaRouteEngineInput(
    val snapshotId: LayoutSnapshotId,
    val layoutContext: SchematicRoutingLayoutContext,
    val componentBounds: List<SchematicComponentBounds> = emptyList(),
    val requests: List<AthenaRouteRequest>,
    val drawingArea: OrthogonalRouteRect = OrthogonalRouteRect(0, 0, Int.MAX_VALUE, Int.MAX_VALUE),
)

/** First deterministic, Athena-owned schematic route engine. */
class AthenaRouteEngine(
    private val orthogonalRouteSolver: OrthogonalRouteSolver = OrthogonalRouteSolver(),
) {
    fun solve(input: AthenaRouteEngineInput): RouteFactSnapshot {
        val lanePlan = allocateLanes(input)
        val facts = lanePlan.orderedRequests.map { request ->
            request.toRouteFact(input, lanePlan.assignmentByRouteId.getValue(request.routeId))
        }
        val topology = deriveRouteTopology(facts)
        return RouteFactSnapshot.canonical(
            snapshotId = input.snapshotId,
            family = "schematic",
            routeFacts = facts,
            junctionFacts = topology.junctions,
            crossingFacts = topology.crossings,
            laneDiagnostics = lanePlan.diagnostics,
        )
    }

    private fun AthenaRouteRequest.toRouteFact(
        input: AthenaRouteEngineInput,
        laneAssignment: RouteLaneAssignment,
    ): RouteFact {
        require(sourceAnchor.gridPoint.isGridAligned(input.layoutContext.gridSize)) {
            "Source terminal anchor `${sourceAnchor.anchorId}` is not aligned to the schematic routing grid."
        }
        require(targetAnchor.gridPoint.isGridAligned(input.layoutContext.gridSize)) {
            "Target terminal anchor `${targetAnchor.anchorId}` is not aligned to the schematic routing grid."
        }
        val segments = routeSegments(sourceAnchor, targetAnchor, input)
        val provenance = requireNotNull(connectionRoleFact.sourceSpan) {
            "Route '${routeId.value}' requires authored connection provenance."
        }.let { span ->
            SourceProvenance(
                file = span.sourceUnitId,
                startLine = span.startLine,
                startColumn = span.startColumn,
                endLine = span.endLine,
                endColumn = span.endColumn,
            )
        }
        return RouteFact(
            routeId = routeId,
            snapshotId = input.snapshotId,
            connectionId = connectionRoleFact.connectionId,
            routeIntentId = RouteIntentId("route:${connectionRoleFact.connectionId.value}"),
            bundleId = bundleId ?: RouteBundleId("bundle:${connectionRoleFact.connectionId.value}"),
            selectedChannelIds = emptyList(),
            plannerId = "athena-route-engine",
            compilerSnapshotId = input.snapshotId.value,
            provenance = provenance,
            qualityMetrics = RouteQualityMetrics(
                crossingCount = 0,
                bendCount = (segments.size - 1).coerceAtLeast(0),
                length = segments.sumOf { segment -> segment.manhattanLength() },
                channelChangeCount = 0,
                bundleContinuityPenalty = 0,
                labelClearanceViolationCount = 0,
            ),
            source = sourceAnchor,
            target = targetAnchor,
            connectionRole = connectionRoleFact.role,
            segments = segments,
            lane = laneAssignment.lane,
            laneAssignment = laneAssignment,
            constraints = constraints,
            labels = connectionLabels(segments, input),
            quality = routeQuality(sourceAnchor, targetAnchor, segments, input),
        )
    }

    private fun AthenaRouteRequest.connectionLabels(
        segments: List<SchematicRouteSegment>,
        input: AthenaRouteEngineInput,
    ): List<RouteLabelFact> {
        val labelSegment = segments
            .sortedByDescending { segment -> segment.manhattanLength() }
            .firstOrNull { segment -> input.componentBounds.none { bounds -> bounds.intersects(segment) } }
            ?: segments.maxByOrNull { segment -> segment.manhattanLength() }
            ?: return emptyList()
        val text = when (connectionRoleFact.role) {
            ElectricalConnectionRole.TERMINAL_TRANSITION -> targetAnchor.portId.value
            else -> "${sourceAnchor.portId.value}-${targetAnchor.portId.value}"
        }
        val labelFact = RuleBasedSchematicLabelStrategy().solve(
            SchematicLabelSnapshot(
                snapshotId = input.snapshotId,
                family = ElectricalProjectionFamily.SCHEMATIC,
                requests = listOf(
                    SchematicLabelRequest(
                        labelId = SchematicLabelId("label:${routeId.value}"),
                        kind = SchematicLabelKind.ROUTE_NAME,
                        text = text,
                        anchor = SchematicLabelAnchor(
                            routeId = routeId,
                            routeSegment = labelSegment,
                            point = labelSegment.midpoint(),
                        ),
                    ),
                ),
            ),
        ).labelFacts.single()
        return listOf(
            RouteLabelFact(
                labelId = labelFact.labelId,
                text = labelFact.text,
                anchorRouteId = routeId,
                placement = labelFact.placement,
            ),
        )
    }

    private fun routeSegments(
        sourceAnchor: TerminalAnchorFact,
        targetAnchor: TerminalAnchorFact,
        input: AthenaRouteEngineInput,
    ): List<SchematicRouteSegment> {
        val obstacles = input.componentBounds.excludingEndpointOwners(sourceAnchor, targetAnchor)
        val request = OrthogonalRouteRequest(
            requestId = "schematic:${sourceAnchor.anchorId.value}:${targetAnchor.anchorId.value}",
            source = sourceAnchor.gridPoint.toOrthogonalPoint(),
            target = targetAnchor.gridPoint.toOrthogonalPoint(),
            sourceSide = sourceAnchor.side.toOrthogonalSide(),
            targetSide = targetAnchor.side.toOrthogonalSide(),
            drawingArea = input.drawingArea,
            obstacles = obstacles.map { bounds ->
                OrthogonalRouteObstacle(
                    obstacleId = "${bounds.subjectId.value}:${bounds.occurrenceId.value}",
                    bounds = OrthogonalRouteRect(
                        bounds.topLeft.x,
                        bounds.topLeft.y,
                        bounds.width,
                        bounds.height,
                    ),
                )
            },
            stubLength = input.layoutContext.gridSize,
            obstacleClearance = input.layoutContext.gridSize,
        )
        val result = orthogonalRouteSolver.solve(request)
        require(result is OrthogonalRouteSolveResult.Success) {
            "Route engine could not find an obstacle-safe orthogonal path inside the Drawing Area."
        }
        return buildList {
            result.points.zipWithNext().forEach { (start, end) ->
                addSegment(start.toSchematicPoint(), end.toSchematicPoint())
            }
        }

    }

    private fun sideStubPoint(
        anchor: TerminalAnchorFact,
        layoutContext: SchematicRoutingLayoutContext,
    ): SchematicRoutePoint? {
        val distance = layoutContext.gridSize
        val point = anchor.gridPoint
        val (nextX, nextY) = when (anchor.side) {
            TerminalSide.LEFT -> point.x - distance to point.y
            TerminalSide.RIGHT -> point.x + distance to point.y
            TerminalSide.TOP -> point.x to point.y - distance
            TerminalSide.BOTTOM -> point.x to point.y + distance
        }
        return if (nextX >= 0 && nextY >= 0) {
            SchematicRoutePoint(x = nextX, y = nextY)
        } else {
            null
        }
    }

    private fun AthenaRouteRequest.routeQuality(
        sourceAnchor: TerminalAnchorFact,
        targetAnchor: TerminalAnchorFact,
        segments: List<SchematicRouteSegment>,
        input: AthenaRouteEngineInput,
    ): RouteQuality {
        val failed = buildList {
            if (sideStubPoint(sourceAnchor, input.layoutContext) == null) {
                add(RouteConstraintId("constraint:${sourceAnchor.anchorId.value}:preferred-side-stub"))
            }
            if (sideStubPoint(targetAnchor, input.layoutContext) == null) {
                add(RouteConstraintId("constraint:${targetAnchor.anchorId.value}:preferred-side-stub"))
            }
            val obstacles = input.componentBounds.excludingEndpointOwners(sourceAnchor, targetAnchor)
            if (segments.any { segment -> obstacles.any { bounds -> bounds.intersects(segment) } }) {
                val avoidanceConstraintIds = constraints
                    .filter { constraint ->
                        constraint.kind == RouteConstraintKind.AVOID_COMPONENT_BODY ||
                            constraint.kind == RouteConstraintKind.AVOID_NODE
                    }
                    .map(RouteConstraint::constraintId)
                if (avoidanceConstraintIds.isEmpty()) {
                    add(RouteConstraintId("constraint:${connectionRoleFact.connectionId.value}:avoid-component-body"))
                } else {
                    addAll(avoidanceConstraintIds)
                }
            }
        }
        return if (failed.isEmpty()) {
            RouteQuality.satisfied()
        } else {
            RouteQuality.degraded(failed.distinct(), "Route could not satisfy all preferred side and component avoidance constraints.")
        }
    }

    private fun List<SchematicComponentBounds>.excludingEndpointOwners(
        sourceAnchor: TerminalAnchorFact,
        targetAnchor: TerminalAnchorFact,
    ): List<SchematicComponentBounds> = filterNot { bounds ->
        (bounds.subjectId == sourceAnchor.subjectId && bounds.occurrenceId == sourceAnchor.occurrenceId) ||
            (bounds.subjectId == targetAnchor.subjectId && bounds.occurrenceId == targetAnchor.occurrenceId)
    }

    private fun MutableList<SchematicRouteSegment>.addSegment(
        start: SchematicRoutePoint,
        end: SchematicRoutePoint,
    ) {
        if (start == end) {
            return
        }
        val orientation = when {
            start.y == end.y -> SchematicRouteSegmentOrientation.HORIZONTAL
            start.x == end.x -> SchematicRouteSegmentOrientation.VERTICAL
            else -> error("Route engine can only append orthogonal segments.")
        }
        add(SchematicRouteSegment(start = start, end = end, orientation = orientation))
    }

    private fun SchematicRoutePoint.isGridAligned(gridSize: Int): Boolean {
        return x % gridSize == 0 && y % gridSize == 0
    }

    private fun midpoint(
        first: Int,
        second: Int,
    ): Int = ((first.toLong() + second.toLong()) / 2).toIntExact()

    private fun SchematicRouteSegment.manhattanLength(): Int {
        return kotlin.math.abs(start.x - end.x) + kotlin.math.abs(start.y - end.y)
    }

    private fun SchematicRouteSegment.midpoint(): SchematicRoutePoint {
        return SchematicRoutePoint(
            x = midpoint(start.x, end.x),
            y = midpoint(start.y, end.y),
        )
    }

    private fun Long.toIntExact(): Int {
        require(this in Int.MIN_VALUE..Int.MAX_VALUE) {
            "Schematic route coordinates must stay within Int sheet coordinate bounds."
        }
        return toInt()
    }

    private fun allocateLanes(input: AthenaRouteEngineInput): RouteLanePlan {
        val orderedRequests = input.requests.sortedWith(
            compareBy<AthenaRouteRequest>(
                { request -> request.connectionRoleFact.role.name },
                { request -> request.connectionRoleFact.connectionId.value },
                { request -> request.routeId.value },
                { request -> request.sourceAnchor.anchorId.value },
                { request -> request.targetAnchor.anchorId.value },
            ),
        )
        val diagnostics = laneDiagnostics(input, orderedRequests.size)
        val assignmentByLane = orderedRequests
            .mapIndexed { index, request -> routeLane(index, input.layoutContext) to request.routeId }
            .groupBy({ it.first }, { it.second })
        val assignmentByRouteId = orderedRequests.mapIndexed { index, request ->
            val lane = routeLane(index, input.layoutContext)
            val conflicts = diagnostics
                .filter { diagnostic -> diagnostic.affectedRouteIds.isEmpty() || request.routeId in diagnostic.affectedRouteIds }
                .map { diagnostic -> RouteLaneConflict(diagnostic.code, diagnostic.message) }
            val laneAssignment = RouteLaneAssignment(
                laneId = RouteLaneId("lane:${lane.value}"),
                lane = lane,
                orientation = input.layoutContext.routeLaneOrientation,
                capacity = RouteLaneCapacity(input.layoutContext.routeLaneCapacity),
                occupancy = RouteLaneOccupancy(
                    usedRoutes = assignmentByLane.getValue(lane).size,
                    routeIds = assignmentByLane.getValue(lane).sortedBy(SchematicRouteId::value),
                ),
                conflicts = conflicts,
            )
            request.routeId to laneAssignment
        }.toMap()
        return RouteLanePlan(
            orderedRequests = orderedRequests,
            assignmentByRouteId = assignmentByRouteId,
            diagnostics = diagnostics,
        )
    }

    private fun laneDiagnostics(
        input: AthenaRouteEngineInput,
        requestCount: Int,
    ): List<RouteLaneDiagnostic> = buildList {
        val capacity = input.layoutContext.routeLaneCapacity.toLong() * input.layoutContext.routeLaneCount.toLong()
        if (requestCount.toLong() > capacity) {
            add(
                RouteLaneDiagnostic(
                    code = "route.lane.capacity.exceeded",
                    message = "Route requests exceed configured lane capacity.",
                    affectedRouteIds = input.requests.map { request -> request.routeId }.sortedBy(SchematicRouteId::value),
                ),
            )
        }
        if (input.layoutContext.routeLaneSpacing < input.layoutContext.gridSize) {
            add(
                RouteLaneDiagnostic(
                    code = "route.lane.spacing.conflict",
                    message = "Route lane spacing is smaller than the routing grid.",
                    affectedRouteIds = input.requests.map { request -> request.routeId }.sortedBy(SchematicRouteId::value),
                ),
            )
        }
    }

    private fun routeLane(
        index: Int,
        layoutContext: SchematicRoutingLayoutContext,
    ): SchematicRouteLane = SchematicRouteLane((index / layoutContext.routeLaneCapacity).coerceAtMost(layoutContext.routeLaneCount - 1))

}

private fun SchematicRoutePoint.toOrthogonalPoint(): OrthogonalRoutePoint = OrthogonalRoutePoint(x, y)

private fun OrthogonalRoutePoint.toSchematicPoint(): SchematicRoutePoint = SchematicRoutePoint(x, y)

private fun TerminalSide.toOrthogonalSide(): OrthogonalRouteSide = when (this) {
    TerminalSide.LEFT -> OrthogonalRouteSide.LEFT
    TerminalSide.RIGHT -> OrthogonalRouteSide.RIGHT
    TerminalSide.TOP -> OrthogonalRouteSide.TOP
    TerminalSide.BOTTOM -> OrthogonalRouteSide.BOTTOM
}

private data class RouteLanePlan(
    val orderedRequests: List<AthenaRouteRequest>,
    val assignmentByRouteId: Map<SchematicRouteId, RouteLaneAssignment>,
    val diagnostics: List<RouteLaneDiagnostic>,
)

private data class DerivedRouteTopology(
    val junctions: List<RouteJunctionFact>,
    val crossings: List<RouteCrossingFact>,
)

private fun deriveRouteTopology(routeFacts: List<RouteFact>): DerivedRouteTopology {
    val endpointUsages = routeFacts.flatMap { route ->
        listOf(route.source, route.target).mapNotNull { endpoint ->
            endpoint.portSemanticId?.value?.let { semanticPortId ->
                Triple(semanticPortId, endpoint.point, route.routeId)
            }
        }
    }
    val junctions = endpointUsages
        .groupBy { (semanticPortId, point, _) -> semanticPortId to point }
        .mapNotNull { (key, usages) ->
            val routeIds = usages.map { it.third }.distinct().sortedBy(SchematicRouteId::value)
            if (routeIds.size < 2) {
                null
            } else {
                RouteJunctionFact(
                    junctionId = "junction:${key.first}:${key.second.x}:${key.second.y}",
                    point = key.second,
                    routeIds = routeIds,
                    semanticPortId = key.first,
                )
            }
        }
    val junctionKeys = junctions.flatMap { junction ->
        junction.routeIds.flatMapIndexed { index, routeId ->
            junction.routeIds.drop(index + 1).map { other ->
                Triple(junction.point, routeId.value, other.value)
            }
        }
    }.toSet()
    val crossings = buildList {
        routeFacts.sortedBy { it.routeId.value }.forEachIndexed { index, left ->
            routeFacts.sortedBy { it.routeId.value }.drop(index + 1).forEach { right ->
                left.segments.forEach { leftSegment ->
                    right.segments.forEach { rightSegment ->
                        val point = orthogonalIntersection(leftSegment, rightSegment) ?: return@forEach
                        val routeIds = listOf(left.routeId, right.routeId).sortedBy(SchematicRouteId::value)
                        if (Triple(point, routeIds[0].value, routeIds[1].value) !in junctionKeys) {
                            add(
                                RouteCrossingFact(
                                    crossingId = "crossing:${routeIds[0].value}:${routeIds[1].value}:${point.x}:${point.y}",
                                    point = point,
                                    routeIds = routeIds,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }.distinctBy { crossing -> crossing.crossingId }
    return DerivedRouteTopology(junctions = junctions, crossings = crossings)
}

private fun orthogonalIntersection(
    first: SchematicRouteSegment,
    second: SchematicRouteSegment,
): SchematicRoutePoint? {
    if (first.orientation == second.orientation) return null
    val horizontal = if (first.orientation == SchematicRouteSegmentOrientation.HORIZONTAL) first else second
    val vertical = if (first.orientation == SchematicRouteSegmentOrientation.VERTICAL) first else second
    val x = vertical.start.x
    val y = horizontal.start.y
    return SchematicRoutePoint(x, y).takeIf {
        x in minOf(horizontal.start.x, horizontal.end.x)..maxOf(horizontal.start.x, horizontal.end.x) &&
            y in minOf(vertical.start.y, vertical.end.y)..maxOf(vertical.start.y, vertical.end.y)
    }
}
