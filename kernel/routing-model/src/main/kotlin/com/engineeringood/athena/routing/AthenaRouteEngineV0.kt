package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId

/** Minimal schematic layout context consumed by Athena route engine v0. */
data class SchematicRoutingLayoutContext(
    val gridSize: Int,
) {
    init {
        require(gridSize > 0) { "Schematic routing grid size must be positive." }
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

/** One governed route request consumed by Athena route engine v0. */
data class AthenaRouteRequest(
    val routeId: SchematicRouteId,
    val connectionIntent: ElectricalConnectionIntent,
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
)

/** First deterministic, Athena-owned schematic route engine. */
class AthenaRouteEngineV0 {
    fun solve(input: AthenaRouteEngineInput): RouteFactSnapshot {
        val facts = input.requests
            .sortedWith(
                compareBy<AthenaRouteRequest>(
                    { request -> request.routeId.value },
                    { request -> request.connectionIntent.connectionId.value },
                    { request -> request.sourceAnchor.anchorId.value },
                    { request -> request.targetAnchor.anchorId.value },
                ),
            )
            .map { request -> request.toRouteFact(input) }
        return RouteFactSnapshot.canonical(
            snapshotId = input.snapshotId,
            family = "schematic",
            routeFacts = facts,
        )
    }

    private fun AthenaRouteRequest.toRouteFact(input: AthenaRouteEngineInput): RouteFact {
        require(sourceAnchor.gridPoint.isGridAligned(input.layoutContext.gridSize)) {
            "Source terminal anchor `${sourceAnchor.anchorId}` is not aligned to the schematic routing grid."
        }
        require(targetAnchor.gridPoint.isGridAligned(input.layoutContext.gridSize)) {
            "Target terminal anchor `${targetAnchor.anchorId}` is not aligned to the schematic routing grid."
        }
        val segments = routeSegments(sourceAnchor, targetAnchor, input)
        return RouteFact(
            routeId = routeId,
            snapshotId = input.snapshotId,
            connectionId = connectionIntent.connectionId,
            source = sourceAnchor,
            target = targetAnchor,
            segments = segments,
            lane = SchematicRouteLane(input.requests.sortedBy { request -> request.routeId.value }.indexOf(this)),
            constraints = constraints,
            labels = routeLabels(segments, input),
            quality = routeQuality(sourceAnchor, targetAnchor, input.layoutContext),
        )
    }

    private fun AthenaRouteRequest.routeLabels(
        segments: List<SchematicRouteSegment>,
        input: AthenaRouteEngineInput,
    ): List<RouteLabelFact> {
        val labelSegment = segments
            .sortedByDescending { segment -> segment.manhattanLength() }
            .firstOrNull { segment -> input.componentBounds.none { bounds -> bounds.intersects(segment) } }
            ?: segments.maxByOrNull { segment -> segment.manhattanLength() }
            ?: return emptyList()
        val text = when (connectionIntent.role) {
            ElectricalConnectionRole.TERMINAL_TRANSITION -> targetAnchor.portId.value
            else -> connectionIntent.connectionId.value.removePrefix("connection:")
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
        val source = sourceAnchor.gridPoint
        val target = targetAnchor.gridPoint
        require(source != target) { "Route engine endpoints must not share the same terminal anchor point." }
        val sourceStub = sideStubPoint(sourceAnchor, input.layoutContext) ?: source
        val targetStub = sideStubPoint(targetAnchor, input.layoutContext) ?: target
        val segments = mutableListOf<SchematicRouteSegment>()
        segments.addSegment(source, sourceStub)
        segments += middleSegments(sourceStub, targetStub, input.componentBounds, input.layoutContext)
        segments.addSegment(targetStub, target)
        return segments

    }

    private fun middleSegments(
        source: SchematicRoutePoint,
        target: SchematicRoutePoint,
        componentBounds: List<SchematicComponentBounds>,
        layoutContext: SchematicRoutingLayoutContext,
    ): List<SchematicRouteSegment> {
        if (source == target) {
            return emptyList()
        }
        if (source.y == target.y || source.x == target.x) {
            val direct = buildList { addSegment(source, target) }
            return if (direct.any { segment -> componentBounds.any { bounds -> bounds.intersects(segment) } }) {
                laneAround(source, target, componentBounds, layoutContext) ?: direct
            } else {
                direct
            }
        }
        val middleX = midpoint(source.x, target.x)
        val routedMiddleX = if (componentBounds.any { bounds -> bounds.center.x == middleX }) {
            middleX + 20
        } else {
            middleX
        }
        val firstTurn = SchematicRoutePoint(x = routedMiddleX, y = source.y)
        val secondTurn = SchematicRoutePoint(x = routedMiddleX, y = target.y)
        val default = listOf(
            SchematicRouteSegment(source, firstTurn, SchematicRouteSegmentOrientation.HORIZONTAL),
            SchematicRouteSegment(firstTurn, secondTurn, SchematicRouteSegmentOrientation.VERTICAL),
            SchematicRouteSegment(secondTurn, target, SchematicRouteSegmentOrientation.HORIZONTAL),
        )
        return if (default.any { segment -> componentBounds.any { bounds -> bounds.intersects(segment) } }) {
            laneAround(source, target, componentBounds, layoutContext) ?: default
        } else {
            default
        }
    }

    private fun laneAround(
        source: SchematicRoutePoint,
        target: SchematicRoutePoint,
        componentBounds: List<SchematicComponentBounds>,
        layoutContext: SchematicRoutingLayoutContext,
    ): List<SchematicRouteSegment>? {
        val laneY = componentBounds.minOfOrNull { bounds -> bounds.topLeft.y }?.minus(layoutContext.gridSize)
            ?.takeIf { candidate -> candidate >= 0 }
            ?: return null
        val firstTurn = SchematicRoutePoint(x = source.x, y = laneY)
        val secondTurn = SchematicRoutePoint(x = target.x, y = laneY)
        val candidate = buildList {
            addSegment(source, firstTurn)
            addSegment(firstTurn, secondTurn)
            addSegment(secondTurn, target)
        }
        return candidate.takeIf { segments -> segments.none { segment -> componentBounds.any { bounds -> bounds.intersects(segment) } } }
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

    private fun routeQuality(
        sourceAnchor: TerminalAnchorFact,
        targetAnchor: TerminalAnchorFact,
        layoutContext: SchematicRoutingLayoutContext,
    ): RouteQuality {
        val failed = buildList {
            if (sideStubPoint(sourceAnchor, layoutContext) == null) {
                add(RouteConstraintId("constraint:${sourceAnchor.anchorId.value}:preferred-side-stub"))
            }
            if (sideStubPoint(targetAnchor, layoutContext) == null) {
                add(RouteConstraintId("constraint:${targetAnchor.anchorId.value}:preferred-side-stub"))
            }
        }
        return if (failed.isEmpty()) {
            RouteQuality.satisfied()
        } else {
            RouteQuality.degraded(failed, "Preferred terminal side stub could not be produced inside sheet bounds.")
        }
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
}
