package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId

/** Stable identifier for one schematic label fact, scoped by snapshot id. */
@JvmInline
value class SchematicLabelId(val value: String) {
    init {
        require(value.isNotBlank()) { "Schematic label id must not be blank." }
    }

    override fun toString(): String = value
}

/** Label purpose vocabulary for M21 schematic readability. */
enum class SchematicLabelKind {
    DEVICE_NAME,
    TERMINAL_NAME,
    ROUTE_NAME,
    CROSS_REFERENCE,
}

/** Relationship between a label placement and its governed anchor. */
enum class SchematicLabelAnchorRelation {
    LOWER_RIGHT,
    ABOVE,
    RIGHT,
}

/** Placement of one schematic label in sheet coordinates. */
data class SchematicLabelPlacement(
    val origin: SchematicRoutePoint,
    val relation: SchematicLabelAnchorRelation,
)

/** Bounds for the subject body that owns a label anchor. */
data class SchematicLabelSubjectBounds(
    val origin: SchematicRoutePoint,
    val width: Int,
    val height: Int,
) {
    init {
        require(width > 0 && height > 0) { "Schematic label subject bounds must use positive size." }
    }

    fun contains(point: SchematicRoutePoint): Boolean {
        return point.x >= origin.x &&
            point.y >= origin.y &&
            point.x < origin.x + width &&
            point.y < origin.y + height
    }
}

/** Governed anchor used to derive one schematic label fact. */
data class SchematicLabelAnchor(
    val subjectId: StableSemanticIdentity? = null,
    val occurrenceId: LayoutOccurrenceId? = null,
    val endpointId: SchematicEndpointId? = null,
    val routeId: SchematicRouteId? = null,
    val routeSegment: SchematicRouteSegment? = null,
    val subjectBounds: SchematicLabelSubjectBounds? = null,
    val point: SchematicRoutePoint,
) {
    init {
        require(subjectId != null || endpointId != null || routeId != null) {
            "Schematic label anchors must preserve at least one governed identity."
        }
    }
}

/** One requested schematic label before deterministic placement. */
data class SchematicLabelRequest(
    val labelId: SchematicLabelId,
    val kind: SchematicLabelKind,
    val text: String,
    val anchor: SchematicLabelAnchor,
) {
    init {
        require(text.isNotBlank()) { "Schematic label text must not be blank." }
    }
}

/** Immutable label input for one schematic snapshot. */
data class SchematicLabelSnapshot(
    val snapshotId: LayoutSnapshotId,
    val family: ElectricalProjectionFamily,
    val requests: List<SchematicLabelRequest>,
)

/** Deterministic label fact consumed by downstream sheet surfaces. */
data class SchematicLabelFact(
    val labelId: SchematicLabelId,
    val snapshotId: LayoutSnapshotId,
    val kind: SchematicLabelKind,
    val text: String,
    val anchor: SchematicLabelAnchor,
    val placement: SchematicLabelPlacement,
)

/** Deterministic label result for one schematic label snapshot. */
data class SchematicLabelStrategyResult(
    val snapshotId: LayoutSnapshotId,
    val family: ElectricalProjectionFamily,
    val labelFacts: List<SchematicLabelFact>,
)

/** Strategy boundary that turns governed label requests into label facts. */
interface SchematicLabelStrategy {
    fun solve(snapshot: SchematicLabelSnapshot): SchematicLabelStrategyResult
}

/** First M21 label strategy: stable offsets from governed anchors and route segments. */
class RuleBasedSchematicLabelStrategy : SchematicLabelStrategy {
    override fun solve(snapshot: SchematicLabelSnapshot): SchematicLabelStrategyResult {
        require(snapshot.family == ElectricalProjectionFamily.SCHEMATIC) {
            "Rule-based schematic label strategy only accepts schematic label snapshots."
        }
        val labelIds = snapshot.requests.map(SchematicLabelRequest::labelId)
        require(labelIds.size == labelIds.toSet().size) {
            "Schematic label snapshots must reference each label id at most once."
        }
        val facts = snapshot.requests
            .sortedWith(
                compareBy<SchematicLabelRequest>(
                    { request -> request.labelId.value },
                    { request -> request.kind.name },
                    { request -> request.text },
                ),
            )
            .map { request ->
                SchematicLabelFact(
                    labelId = request.labelId,
                    snapshotId = snapshot.snapshotId,
                    kind = request.kind,
                    text = request.text,
                    anchor = request.anchor,
                    placement = placeLabel(request.anchor),
                )
            }
        return SchematicLabelStrategyResult(
            snapshotId = snapshot.snapshotId,
            family = snapshot.family,
            labelFacts = facts,
        )
    }
}

private fun placeLabel(anchor: SchematicLabelAnchor): SchematicLabelPlacement {
    val segment = anchor.routeSegment
    if (segment != null) {
        val midpoint = segment.midpoint()
        return when (segment.orientation) {
            SchematicRouteSegmentOrientation.HORIZONTAL -> SchematicLabelPlacement(
                origin = midpoint.moveBy(dx = 0, dy = -16, fallbackDy = 16),
                relation = SchematicLabelAnchorRelation.ABOVE,
            )
            SchematicRouteSegmentOrientation.VERTICAL -> SchematicLabelPlacement(
                origin = midpoint.moveBy(dx = 16, dy = 0, fallbackDx = -16),
                relation = SchematicLabelAnchorRelation.RIGHT,
            )
        }
    }
    val lowerRight = anchor.point.moveBy(dx = 12, dy = 12)
    val bounds = anchor.subjectBounds
    if (bounds != null && bounds.contains(lowerRight)) {
        return SchematicLabelPlacement(
            origin = SchematicRoutePoint(x = bounds.origin.x + bounds.width + 12, y = anchor.point.y),
            relation = SchematicLabelAnchorRelation.RIGHT,
        )
    }
    return SchematicLabelPlacement(origin = lowerRight, relation = SchematicLabelAnchorRelation.LOWER_RIGHT)
}

private fun SchematicRouteSegment.midpoint(): SchematicRoutePoint {
    return SchematicRoutePoint(
        x = ((start.x.toLong() + end.x.toLong()) / 2).toIntExact(),
        y = ((start.y.toLong() + end.y.toLong()) / 2).toIntExact(),
    )
}

private fun SchematicRoutePoint.moveBy(
    dx: Int,
    dy: Int,
    fallbackDx: Int = dx,
    fallbackDy: Int = dy,
): SchematicRoutePoint {
    val movedX = x.toLong() + dx.toLong()
    val movedY = y.toLong() + dy.toLong()
    if (movedX >= 0 && movedY >= 0) {
        return SchematicRoutePoint(x = movedX.toIntExact(), y = movedY.toIntExact())
    }
    return SchematicRoutePoint(
        x = (x.toLong() + fallbackDx.toLong()).toIntExact(),
        y = (y.toLong() + fallbackDy.toLong()).toIntExact(),
    )
}

private fun Long.toIntExact(): Int {
    require(this in Int.MIN_VALUE..Int.MAX_VALUE) {
        "Schematic label coordinates must stay within Int sheet coordinate bounds."
    }
    return toInt()
}
