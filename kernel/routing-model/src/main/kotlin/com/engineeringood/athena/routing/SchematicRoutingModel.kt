package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import com.engineeringood.athena.layout.SchematicRouteLanePreference

/** Stable identifier for one schematic endpoint in a route request or fact. */
@JvmInline
value class SchematicEndpointId(val value: String) {
    init {
        require(value.isNotBlank()) { "Schematic endpoint id must not be blank." }
    }

    override fun toString(): String = value
}

/** Stable identifier for one schematic route fact, scoped by snapshot id. */
@JvmInline
value class SchematicRouteId(val value: String) {
    init {
        require(value.isNotBlank()) { "Schematic route id must not be blank." }
    }

    override fun toString(): String = value
}

/** Deterministic routing lane assigned inside one schematic route snapshot. */
@JvmInline
value class SchematicRouteLane(val value: Int) {
    init {
        require(value >= 0) { "Schematic route lane must be non-negative." }
    }

    override fun toString(): String = value.toString()
}

/** Sheet point used by schematic route facts. */
data class SchematicRoutePoint(
    val x: Int,
    val y: Int,
) {
    init {
        require(x >= 0 && y >= 0) { "Schematic route points must use non-negative sheet coordinates." }
    }
}

/** Orientation of one orthogonal schematic route segment. */
enum class SchematicRouteSegmentOrientation {
    HORIZONTAL,
    VERTICAL,
}

/** One ordered orthogonal segment inside a schematic route fact. */
data class SchematicRouteSegment(
    val start: SchematicRoutePoint,
    val end: SchematicRoutePoint,
    val orientation: SchematicRouteSegmentOrientation,
) {
    init {
        require(
            when (orientation) {
                SchematicRouteSegmentOrientation.HORIZONTAL -> start.y == end.y && start.x != end.x
                SchematicRouteSegmentOrientation.VERTICAL -> start.x == end.x && start.y != end.y
            },
        ) { "Schematic route segment orientation must match non-zero segment geometry." }
    }
}

/** Endpoint reference carrying governed identity into route derivation and facts. */
data class SchematicRouteEndpointRef(
    val endpointId: SchematicEndpointId,
    val subjectId: StableSemanticIdentity,
    val occurrenceId: LayoutOccurrenceId,
    val anchor: SchematicRoutePoint,
)

/** One route derivation request between two governed schematic endpoints. */
data class SchematicRouteRequest(
    val routeId: SchematicRouteId,
    val source: SchematicRouteEndpointRef,
    val target: SchematicRouteEndpointRef,
    val lanePreference: SchematicRouteLanePreference? = null,
)

/** Immutable schematic route derivation input for one layout snapshot. */
data class SchematicRouteSnapshot(
    val snapshotId: LayoutSnapshotId,
    val family: ElectricalProjectionFamily,
    val requests: List<SchematicRouteRequest>,
)

/** Deterministic route fact consumed by downstream sheet surfaces. */
data class SchematicRouteFact(
    val routeId: SchematicRouteId,
    val snapshotId: LayoutSnapshotId,
    val source: SchematicRouteEndpointRef,
    val target: SchematicRouteEndpointRef,
    val lane: SchematicRouteLane,
    val segments: List<SchematicRouteSegment>,
)

/** Deterministic route result for one schematic route snapshot. */
data class SchematicRouteStrategyResult(
    val snapshotId: LayoutSnapshotId,
    val family: ElectricalProjectionFamily,
    val routeFacts: List<SchematicRouteFact>,
)

/** Strategy boundary that turns governed schematic endpoints into route facts. */
interface SchematicRouteStrategy {
    fun solve(snapshot: SchematicRouteSnapshot): SchematicRouteStrategyResult
}

/** First M21 route strategy: stable orthogonal segments with deterministic lane assignment. */
class RuleBasedSchematicRouteStrategy : SchematicRouteStrategy {
    override fun solve(snapshot: SchematicRouteSnapshot): SchematicRouteStrategyResult {
        require(snapshot.family == ElectricalProjectionFamily.SCHEMATIC) {
            "Rule-based schematic route strategy only accepts schematic route snapshots."
        }
        val orderedRequests = snapshot.requests.sortedWith(
            compareBy<SchematicRouteRequest>(
                { request -> request.routeId.value },
                { request -> request.source.endpointId.value },
                { request -> request.target.endpointId.value },
            ),
        )
        val facts = orderedRequests.mapIndexed { index, request ->
            SchematicRouteFact(
                routeId = request.routeId,
                snapshotId = snapshot.snapshotId,
                source = request.source,
                target = request.target,
                lane = SchematicRouteLane(index),
                segments = orthogonalSegments(request.source.anchor, request.target.anchor, request.lanePreference),
            )
        }
        return SchematicRouteStrategyResult(
            snapshotId = snapshot.snapshotId,
            family = snapshot.family,
            routeFacts = facts,
        )
    }
}

private fun orthogonalSegments(
    source: SchematicRoutePoint,
    target: SchematicRoutePoint,
    lanePreference: SchematicRouteLanePreference? = null,
): List<SchematicRouteSegment> {
    require(source != target) { "Schematic route endpoints must not share the same sheet point." }
    if (source.y == target.y) {
        return listOf(
            SchematicRouteSegment(
                start = source,
                end = target,
                orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
            ),
        )
    }
    if (source.x == target.x) {
        return listOf(
            SchematicRouteSegment(
                start = source,
                end = target,
                orientation = SchematicRouteSegmentOrientation.VERTICAL,
            ),
        )
    }
    if (lanePreference == SchematicRouteLanePreference.VERTICAL_FIRST) {
        val turn = SchematicRoutePoint(x = source.x, y = target.y)
        return listOf(
            SchematicRouteSegment(
                start = source,
                end = turn,
                orientation = SchematicRouteSegmentOrientation.VERTICAL,
            ),
            SchematicRouteSegment(
                start = turn,
                end = target,
                orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
            ),
        )
    }
    if (lanePreference == SchematicRouteLanePreference.HORIZONTAL_FIRST) {
        val turn = SchematicRoutePoint(x = target.x, y = source.y)
        return listOf(
            SchematicRouteSegment(
                start = source,
                end = turn,
                orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
            ),
            SchematicRouteSegment(
                start = turn,
                end = target,
                orientation = SchematicRouteSegmentOrientation.VERTICAL,
            ),
        )
    }

    val middleX = midpoint(source.x, target.x)
    if (middleX == source.x || middleX == target.x) {
        val turn = SchematicRoutePoint(x = target.x, y = source.y)
        return listOf(
            SchematicRouteSegment(
                start = source,
                end = turn,
                orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
            ),
            SchematicRouteSegment(
                start = turn,
                end = target,
                orientation = SchematicRouteSegmentOrientation.VERTICAL,
            ),
        )
    }
    val firstTurn = SchematicRoutePoint(x = middleX, y = source.y)
    val secondTurn = SchematicRoutePoint(x = middleX, y = target.y)
    return listOf(
        SchematicRouteSegment(
            start = source,
            end = firstTurn,
            orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
        ),
        SchematicRouteSegment(
            start = firstTurn,
            end = secondTurn,
            orientation = SchematicRouteSegmentOrientation.VERTICAL,
        ),
        SchematicRouteSegment(
            start = secondTurn,
            end = target,
            orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
        ),
    )
}

private fun midpoint(
    first: Int,
    second: Int,
): Int = ((first.toLong() + second.toLong()) / 2).toIntExact()

private fun Long.toIntExact(): Int {
    require(this in Int.MIN_VALUE..Int.MAX_VALUE) {
        "Schematic route coordinates must stay within Int sheet coordinate bounds."
    }
    return toInt()
}
