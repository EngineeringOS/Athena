package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.ConnectionDocument
import com.engineeringood.athena.connection.ConnectionFact
import com.engineeringood.athena.connection.NetFact
import com.engineeringood.athena.ir.EngineeringValue
import com.engineeringood.athena.spatial.ConnectionAnnotation
import com.engineeringood.athena.spatial.ConnectionAnnotationDisplayRole
import com.engineeringood.athena.spatial.ConnectionAnnotationId
import com.engineeringood.athena.spatial.ConnectionAnnotationPlan
import com.engineeringood.athena.spatial.ConnectionAnnotationPlanning
import com.engineeringood.athena.spatial.ConnectionAnnotationSelection
import com.engineeringood.athena.spatial.ConnectionRoutePlan
import com.engineeringood.athena.spatial.ConnectionRouteSegment
import com.engineeringood.athena.spatial.ConnectionRouteTopologyPlan
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialSourceTrace

/** Plans only explicitly selected, compact engineering annotations in logical Sheet coordinates. */
class ConnectionAnnotationPlanner {
    fun plan(
        sheetId: String,
        drawingArea: SpatialRect,
        connectionIr: ConnectionDocument?,
        selections: List<ConnectionAnnotationSelection>,
        routes: List<ConnectionRoutePlan>,
        occurrences: List<SpatialOccurrenceGeometry>,
        topology: ConnectionRouteTopologyPlan = ConnectionRouteTopologyPlan.EMPTY,
    ): ConnectionAnnotationPlanning {
        if (selections.isEmpty()) return ConnectionAnnotationPlanning.Success(ConnectionAnnotationPlan())
        val facts = (connectionIr?.connections.orEmpty().map { it.id.value to it } + connectionIr?.nets.orEmpty().map { it.id.value to it })
            .toMap()
        val orderedSelections = selections.sortedWith(compareBy({ it.semanticId.value }, { it.displayRole.name }))
        val planned = mutableListOf<ConnectionAnnotation>()
        val diagnostics = mutableListOf<SpatialDiagnostic>()

        orderedSelections.forEach { selection ->
            if (selection.sheetId != sheetId) {
                diagnostics += diagnostic(selection, "selection belongs to Sheet ${selection.sheetId}, not Sheet $sheetId", "Select an annotation on Sheet $sheetId.")
                return@forEach
            }
            val fact = facts[selection.semanticId.value]
            if (fact == null) {
                diagnostics += diagnostic(selection, "semantic subject is not present in accepted Connection IR", "Select a published Connection or Net before annotating it.")
                return@forEach
            }
            val value = displayValue(fact, selection.displayRole)
            if (value == null) {
                diagnostics += diagnostic(selection, "selected display role has no resolved engineering value", "Select a resolved value or remove this annotation selection.")
                return@forEach
            }
            val matchingRoutes = routes.filter { it.sheetId == sheetId && it.connectionId == selection.semanticId }.sortedBy { it.routeId.value }
            if (matchingRoutes.isEmpty()) {
                diagnostics += diagnostic(selection, "semantic subject has no placed route on this Sheet", "Place a projection route before annotating it.")
                return@forEach
            }
            val candidates = matchingRoutes.flatMap { route ->
                route.segments.flatMapIndexed { index, segment -> candidateBounds(segment, value).map { Candidate(route, index, it) } }
            }.sortedWith(compareBy({ it.route.routeId.value }, { it.segmentIndex }, { it.bounds.x }, { it.bounds.y }))
            val accepted = candidates.firstOrNull { candidate ->
                candidate.bounds.isInside(drawingArea) &&
                    !candidate.bounds.overlapsAny(occurrences.map(SpatialOccurrenceGeometry::rectangle)) &&
                    !candidate.bounds.overlapsAny(planned.map(ConnectionAnnotation::bounds)) &&
                    !candidate.bounds.intersectsAny(routes.flatMap { it.segments }) &&
                    !candidate.bounds.containsAny(topologyPoints(topology))
            }
            if (accepted == null) {
                diagnostics += diagnostic(selection, "has no legal in-bounds collision-free placement", "Move the selection or enlarge the Drawing Area.")
            } else {
                val id = ConnectionAnnotationId(sheetId, selection.semanticId.value, selection.displayRole)
                planned += ConnectionAnnotation(
                    id = id,
                    sheetId = sheetId,
                    semanticId = selection.semanticId,
                    displayRole = selection.displayRole,
                    value = value,
                    anchor = SpatialPoint(accepted.bounds.x + accepted.bounds.width / 2, accepted.bounds.y + accepted.bounds.height / 2),
                    bounds = accepted.bounds,
                    sourceTrace = annotationTrace(selection, accepted.route),
                )
            }
        }
        return if (diagnostics.isNotEmpty()) {
            ConnectionAnnotationPlanning.Failure(diagnostics.sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem)))
        } else {
            ConnectionAnnotationPlanning.Success(ConnectionAnnotationPlan(planned))
        }
    }

    private fun displayValue(fact: Any, role: ConnectionAnnotationDisplayRole): String? = when (role) {
        ConnectionAnnotationDisplayRole.KIND -> when (fact) {
            is ConnectionFact -> fact.kind.name
            is NetFact -> fact.kind.name
            else -> null
        }
        ConnectionAnnotationDisplayRole.POTENTIAL_OR_SIGNAL -> (fact as? NetFact)?.potentialOrSignal?.joinToString(".")
        ConnectionAnnotationDisplayRole.SPECIFICATION -> when (fact) {
            is ConnectionFact -> propertiesText(fact.specification.properties)
            is NetFact -> propertiesText(fact.specification.properties)
            else -> null
        }
    }?.takeIf(String::isNotBlank)

    private fun propertiesText(properties: List<com.engineeringood.athena.ir.EngineeringProperty>): String =
        properties.sortedBy { it.name }.joinToString(", ") { property -> "${property.name}=${displayEngineeringValue(property.value)}" }

    private fun displayEngineeringValue(value: EngineeringValue): String = when (value) {
        is EngineeringValue.Quantity -> "${value.value} ${value.unit.authoredName.joinToString(".")}"
        is EngineeringValue.Integer -> value.value.toString()
        is EngineeringValue.Boolean -> value.value.toString()
        is EngineeringValue.Text -> value.text
        is EngineeringValue.Symbol -> value.text
        is EngineeringValue.Reference -> value.reference.authoredPath.joinToString(".")
    }

    private fun candidateBounds(segment: ConnectionRouteSegment, value: String): List<SpatialRect> {
        // Annotation bounds live in logical Sheet coordinates. Renderer scales text later;
        // fixed pixel-sized bounds make compact authored Sheets impossible to publish.
        val width = (value.length + 2).coerceAtLeast(4)
        val height = 3
        return when (segment.orientation) {
            com.engineeringood.athena.spatial.SpatialLaneOrientation.HORIZONTAL -> {
                routeOffsets(segment.start.x, segment.end.x).flatMap { center ->
                    listOf(
                        SpatialRect(center - width / 2, segment.start.y - height - 1, width, height),
                        SpatialRect(center - width / 2, segment.start.y + 1, width, height),
                    )
                }
            }
            com.engineeringood.athena.spatial.SpatialLaneOrientation.VERTICAL -> {
                routeOffsets(segment.start.y, segment.end.y).flatMap { center ->
                    listOf(
                        SpatialRect(segment.start.x - width - 1, center - height / 2, width, height),
                        SpatialRect(segment.start.x + 1, center - height / 2, width, height),
                    )
                }
            }
            null -> emptyList()
        }
    }

    private fun routeOffsets(start: Int, end: Int): List<Int> {
        val low = minOf(start, end)
        val span = kotlin.math.abs(end - start)
        return listOf(
            low + span / 4,
            low + span / 2,
            low + (span * 3) / 4,
        ).distinct()
    }

    private fun topologyPoints(topology: ConnectionRouteTopologyPlan): List<SpatialPoint> =
        topology.junctions.map { it.point } + topology.crossings.map { it.point } + topology.interruptions.flatMap { it.points }

    private fun annotationTrace(
        selection: ConnectionAnnotationSelection,
        route: ConnectionRoutePlan,
    ): SpatialSourceTrace {
        val required = listOf(selection.sheetId, selection.semanticId.value)
        return SpatialSourceTrace(
            projectionIds = required +
                (selection.sourceTrace.projectionIds + route.sourceTrace.projectionIds)
                    .filterNot(required::contains)
                    .distinct(),
            geometryElementIds = (selection.sourceTrace.geometryElementIds + route.sourceTrace.geometryElementIds)
                .distinct()
                .sortedBy { it.value },
        )
    }

    private fun diagnostic(
        selection: ConnectionAnnotationSelection,
        problem: String,
        correction: String,
    ): SpatialDiagnostic = SpatialDiagnostic(
        subject = "Annotation selection `${selection.semanticId.value}`",
        problem = problem,
        correction = correction,
        sourceTrace = selection.sourceTrace,
    )

    private data class Candidate(val route: ConnectionRoutePlan, val segmentIndex: Int, val bounds: SpatialRect)
}

private fun SpatialRect.overlapsAny(rectangles: List<SpatialRect>): Boolean = rectangles.any(::overlaps)

private fun SpatialRect.overlaps(other: SpatialRect): Boolean =
    x < other.right && right > other.x && y < other.bottom && bottom > other.y

private fun SpatialRect.intersectsAny(segments: List<ConnectionRouteSegment>): Boolean = segments.any { segment ->
    when (segment.orientation) {
        com.engineeringood.athena.spatial.SpatialLaneOrientation.HORIZONTAL ->
            segment.start.y > y && segment.start.y < bottom &&
                maxOf(minOf(segment.start.x, segment.end.x), x) < minOf(maxOf(segment.start.x, segment.end.x), right)
        com.engineeringood.athena.spatial.SpatialLaneOrientation.VERTICAL ->
            segment.start.x > x && segment.start.x < right &&
                maxOf(minOf(segment.start.y, segment.end.y), y) < minOf(maxOf(segment.start.y, segment.end.y), bottom)
        null -> false
    }
}

private fun SpatialRect.containsAny(points: List<SpatialPoint>): Boolean = points.any { point ->
    point.x in x until right && point.y in y until bottom
}
