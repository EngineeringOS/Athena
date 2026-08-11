package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.ConnectionDocument
import com.engineeringood.athena.projection.ConnectionProjection
import com.engineeringood.athena.projection.LogicalRouteConstraint
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionOccurrencePortId
import com.engineeringood.athena.spatial.ConnectionRoutePlan
import com.engineeringood.athena.spatial.ConnectionRoutePlanId
import com.engineeringood.athena.spatial.ConnectionRouteQuality
import com.engineeringood.athena.spatial.ConnectionRouteSegment
import com.engineeringood.athena.spatial.ConnectionRouteTopologyPlan
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialLaneId
import com.engineeringood.athena.spatial.SpatialLaneOrientation
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialSourceTrace

internal data class ConnectionRoutePlanningResult(
    val lanes: List<SpatialLane>,
    val routes: List<ConnectionRoutePlan>,
    val topology: ConnectionRouteTopologyPlan = ConnectionRouteTopologyPlan.EMPTY,
    val diagnostics: List<SpatialDiagnostic> = emptyList(),
)

internal data class ConnectionRouteGeometry(
    val points: List<SpatialPoint>,
    val quality: ConnectionRouteQuality,
)

internal class ConnectionRoutePlanner {
    fun compile(
        projection: ProjectionDocument,
        drawingArea: SpatialRect,
        occurrences: List<SpatialOccurrenceGeometry>,
        anchors: List<SpatialAnchorPosition>,
        keepOuts: List<SpatialRect> = emptyList(),
        connectionIr: ConnectionDocument? = null,
    ): ConnectionRoutePlanningResult = compileInternal(
        projection = projection,
        drawingAreaFor = { drawingArea },
        keepOutsFor = { keepOuts },
        connectionIr = connectionIr,
        occurrences = occurrences,
        anchors = anchors,
    )

    fun compile(
        projection: ProjectionDocument,
        drawingAreas: Map<String, SpatialRect>,
        occurrences: List<SpatialOccurrenceGeometry>,
        anchors: List<SpatialAnchorPosition>,
        keepOuts: Map<String, List<SpatialRect>> = emptyMap(),
        connectionIr: ConnectionDocument? = null,
    ): ConnectionRoutePlanningResult = compileInternal(
        projection = projection,
        drawingAreaFor = { sheetId -> drawingAreas[sheetId] ?: ProjectionSpatialLayout.DRAWING_AREA },
        keepOutsFor = { sheetId -> keepOuts[sheetId].orEmpty() },
        connectionIr = connectionIr,
        occurrences = occurrences,
        anchors = anchors,
    )

    private fun compileInternal(
        projection: ProjectionDocument,
        drawingAreaFor: (String) -> SpatialRect,
        keepOutsFor: (String) -> List<SpatialRect>,
        connectionIr: ConnectionDocument?,
        occurrences: List<SpatialOccurrenceGeometry>,
        anchors: List<SpatialAnchorPosition>,
    ): ConnectionRoutePlanningResult {
        val inventory = ProjectionSpatialCoverageInventory(ProjectionPlacementPlanner())
        val diagnostics = mutableListOf<SpatialDiagnostic>()
        val routes = mutableListOf<ConnectionRoutePlan>()

        projection.connections.sortedBy { it.projectionId.value }.forEach { connection ->
            val sheetId = projection.connectionSheetIds(connection).singleOrNull()
            if (sheetId == null) {
                diagnostics += connectionDiagnostic(
                    connection,
                    "does not resolve to exactly one Sheet",
                    "Place every selected occurrence Port on one Sheet before planning this connection.",
                )
                return@forEach
            }
            val sheet = projection.sheets.singleOrNull { it.sheetId.value == sheetId }
            if (sheet == null) {
                diagnostics += connectionDiagnostic(
                    connection,
                    "references missing Sheet `$sheetId`",
                    "Declare Sheet `$sheetId` or correct the connection projection.",
                )
                return@forEach
            }
            val routeLegs = connection.routeLegs()
            if (routeLegs.isEmpty()) {
                diagnostics += connectionDiagnostic(
                    connection,
                    "has no deterministic source-to-sink route legs for ${connection.participants.size} occurrence Ports",
                    "Declare one source or one sink so the Net can branch or merge deterministically.",
                )
                return@forEach
            }
            routeLegs.forEach { leg ->
                val sourceMatches = anchors.filter { anchor -> anchor.matches(sheetId, leg.source) }
                val targetMatches = anchors.filter { anchor -> anchor.matches(sheetId, leg.target) }
                if (sourceMatches.size != 1 || targetMatches.size != 1) {
                    diagnostics += connectionDiagnostic(
                        connection,
                        "cannot resolve one admitted anchor for each selected occurrence Port",
                        "Bind each selected Engineering Port to exactly one package-backed anchor on Sheet `$sheetId`.",
                    )
                    return@forEach
                }
                val source = sourceMatches.single()
                val target = targetMatches.single()
                if (source.anchorId == target.anchorId) {
                    diagnostics += connectionDiagnostic(
                        connection,
                        "resolves both endpoints to the same anchor `${source.anchorId.value}`",
                        "Select distinct occurrence Port anchors for this connection.",
                    )
                    return@forEach
                }

                val endpointOwners = setOf(source.subject.occurrenceId, target.subject.occurrenceId)
                val obstacles = occurrences
                    .filter { it.sheetId == sheetId && it.occurrenceId !in endpointOwners }
                    .sortedBy { it.occurrenceId.projectionId }
                val area = drawingAreaFor(sheetId)
                val hardObstacles = (obstacles.map(SpatialOccurrenceGeometry::rectangle) + keepOutsFor(sheetId))
                    .distinct()
                    .sortedWith(compareBy(SpatialRect::x, SpatialRect::y, SpatialRect::width, SpatialRect::height))
                val candidate = chooseCandidate(
                    source.point,
                    target.point,
                    area,
                    hardObstacles,
                    connection.logicalRouteConstraints,
                )
                if (candidate == null) {
                    diagnostics += connectionDiagnostic(
                        connection,
                        "has no legal orthogonal path inside Sheet `$sheetId` drawing bounds",
                        "Move an occurrence or keep-out so one in-bounds path remains clear between the admitted anchors.",
                    )
                    return@forEach
                }
                val routeProjectionId = connection.projectionId.value + leg.routeProjectionIdSuffix
                routes += ConnectionRoutePlan(
                    routeId = ConnectionRoutePlanId(sheetId, routeProjectionId),
                    sheetId = sheetId,
                    connectionId = connection.semanticId,
                    projectionConnectionId = connection.projectionId.value,
                    sourceAnchorId = source.anchorId,
                    targetAnchorId = target.anchorId,
                    points = candidate.points,
                    quality = candidate.quality,
                    sourceTrace = inventory.canonicalRouteTrace(
                        projection,
                        sheet,
                        connection,
                        leg.source,
                        leg.target,
                        routeProjectionId,
                    ),
                    laneId = laneId(sheetId, candidate.segments),
                    drawingArea = area,
                )
            }
        }

        if (diagnostics.isNotEmpty()) {
            return ConnectionRoutePlanningResult(
                lanes = emptyList(),
                routes = emptyList(),
                diagnostics = diagnostics.sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem)),
            )
        }
        val canonicalRoutes = routes.sortedBy { it.routeId.value }
        val lanes = canonicalRoutes.groupBy(ConnectionRoutePlan::laneId).map { (id, members) ->
            SpatialLane(id, id.sheetId, id.orientation, id.coordinate, members.map(ConnectionRoutePlan::routeId).sortedBy { it.value })
        }.sortedBy { it.laneId.value }
        val topology = ConnectionRouteTopologyPlanner().plan(connectionIr, projection.connections, canonicalRoutes)
        return ConnectionRoutePlanningResult(lanes, canonicalRoutes, topology)
    }

    private fun chooseCandidate(
        source: SpatialPoint,
        target: SpatialPoint,
        area: SpatialRect,
        obstacles: List<SpatialRect>,
        logicalConstraints: List<LogicalRouteConstraint> = emptyList(),
    ): Candidate? {
        val waypoints = logicalConstraints.map { constraint ->
            when (constraint) {
                is LogicalRouteConstraint.Via -> SpatialPoint(
                    area.x + constraint.point.column - 1,
                    area.y + constraint.point.row - 1,
                )
            }
        }
        if (source == target || !area.contains(source) || !area.contains(target) || waypoints.any { !area.contains(it) }) return null
        if (waypoints.isNotEmpty()) {
            val points = (listOf(source) + waypoints + target).zipWithNext().flatMapIndexed { index, (start, end) ->
                val leg = chooseCandidate(start, end, area, obstacles, emptyList()) ?: return null
                if (index == 0) leg.points else leg.points.drop(1)
            }
            return compact(points).toCandidate(area, obstacles)
        }
        return candidatePointSets(source, target, area, obstacles)
            .map(::compact)
            .distinct()
            .mapNotNull { points -> points.toCandidate(area, obstacles) }
            .minWithOrNull(compareBy(Candidate::cost))
    }

    private fun candidatePointSets(
        source: SpatialPoint,
        target: SpatialPoint,
        area: SpatialRect,
        obstacles: List<SpatialRect>,
    ): List<List<SpatialPoint>> {
        val direct = listOf(
            listOf(source, SpatialPoint(target.x, source.y), target),
            listOf(source, SpatialPoint(source.x, target.y), target),
        )
        val xLines = (listOf(source.x, target.x) + obstacles.flatMap { listOf(it.x - 1, it.right + 1) })
            .filter { it in area.x..area.right }
            .distinct()
            .sorted()
        val yLines = (listOf(source.y, target.y) + obstacles.flatMap { listOf(it.y - 1, it.bottom + 1) })
            .filter { it in area.y..area.bottom }
            .distinct()
            .sorted()
        return direct + xLines.map { x ->
            listOf(source, SpatialPoint(x, source.y), SpatialPoint(x, target.y), target)
        } + yLines.map { y ->
            listOf(source, SpatialPoint(source.x, y), SpatialPoint(target.x, y), target)
        }
    }

    private fun List<SpatialPoint>.toCandidate(
        area: SpatialRect,
        obstacles: List<SpatialRect>,
    ): Candidate? {
        if (size < 2 || any { !area.contains(it) }) return null
        val segments = runCatching { zipWithNext(::ConnectionRouteSegment) }.getOrNull() ?: return null
        val violations = segments.sumOf { segment ->
            obstacles.count { rectangle -> segment.entersInterior(rectangle) }
        }
        if (violations != 0) return null
        val quality = ConnectionRouteQuality(
            semanticValidity = 0,
            hardObstacleViolations = violations,
            ambiguousOverlapCount = 0,
            crossingCount = 0,
            bendCount = (segments.size - 1).coerceAtLeast(0),
            manhattanLength = segments.sumOf(ConnectionRouteSegment::manhattanLength),
            geometryIdentity = geometryIdentity(),
        )
        return Candidate(toList(), segments, quality, RouteCost(quality))
    }

    private fun compact(points: List<SpatialPoint>): List<SpatialPoint> = points.fold(mutableListOf()) { result, point ->
        if (result.lastOrNull() != point) result += point
        result
    }

    internal fun planGeometry(
        source: SpatialPoint,
        target: SpatialPoint,
        drawingArea: SpatialRect,
        keepOuts: List<SpatialRect>,
        logicalConstraints: List<LogicalRouteConstraint> = emptyList(),
    ): ConnectionRouteGeometry? = chooseCandidate(
        source = source,
        target = target,
        area = drawingArea,
        obstacles = keepOuts.sortedWith(compareBy(SpatialRect::x, SpatialRect::y, SpatialRect::width, SpatialRect::height)),
        logicalConstraints = logicalConstraints,
    )?.let { candidate -> ConnectionRouteGeometry(candidate.points, candidate.quality) }

    private fun ConnectionRouteSegment.entersInterior(rectangle: SpatialRect): Boolean = when (orientation) {
        SpatialLaneOrientation.HORIZONTAL -> start.y > rectangle.y && start.y < rectangle.bottom &&
            maxOf(start.x, end.x) > rectangle.x && minOf(start.x, end.x) < rectangle.right
        SpatialLaneOrientation.VERTICAL -> start.x > rectangle.x && start.x < rectangle.right &&
            maxOf(start.y, end.y) > rectangle.y && minOf(start.y, end.y) < rectangle.bottom
        null -> true
    }

    private fun SpatialRect.contains(point: SpatialPoint): Boolean = point.x in x..right && point.y in y..bottom

    private fun SpatialAnchorPosition.matches(sheetId: String, endpoint: ProjectionOccurrencePortId): Boolean =
        this.sheetId == sheetId &&
            subject.occurrenceId.projectionId == endpoint.occurrenceId.value &&
            subject.portId == endpoint.portId


    private fun laneId(sheetId: String, segments: List<ConnectionRouteSegment>): SpatialLaneId {
        val segment = segments.filter(ConnectionRouteSegment::isPositiveOrthogonal)
            .maxWith(compareBy<ConnectionRouteSegment>(ConnectionRouteSegment::manhattanLength, { it.start.x }, { it.start.y }))
        val orientation = requireNotNull(segment.orientation)
        return SpatialLaneId(
            sheetId = sheetId,
            orientation = orientation,
            coordinate = if (orientation == SpatialLaneOrientation.HORIZONTAL) segment.start.y else segment.start.x,
        )
    }

    private fun connectionDiagnostic(
        connection: ConnectionProjection,
        problem: String,
        correction: String,
    ): SpatialDiagnostic = SpatialDiagnostic(
        subject = "Connection `${connection.semanticId.value}` projection `${connection.projectionId.value}`",
        problem = problem,
        correction = correction,
        sourceTrace = SpatialSourceTrace(
            projectionIds = connection.sourceTrace.projectionIds,
            geometryElementIds = connection.sourceTrace.geometryElementIds,
        ),
    )

    private data class Candidate(
        val points: List<SpatialPoint>,
        val segments: List<ConnectionRouteSegment>,
        val quality: ConnectionRouteQuality,
        val cost: RouteCost,
    )

    private data class RouteCost(
        val semanticValidity: Int,
        val hardObstacleViolations: Int,
        val ambiguousOverlapCount: Int,
        val crossingCount: Int,
        val bendCount: Int,
        val manhattanLength: Long,
        val geometryIdentity: String,
    ) : Comparable<RouteCost> {
        constructor(quality: ConnectionRouteQuality) : this(
            quality.semanticValidity,
            quality.hardObstacleViolations,
            quality.ambiguousOverlapCount,
            quality.crossingCount,
            quality.bendCount,
            quality.manhattanLength,
            quality.geometryIdentity,
        )

        override fun compareTo(other: RouteCost): Int =
            compareValuesBy(
                this,
                other,
                RouteCost::semanticValidity,
                RouteCost::hardObstacleViolations,
                RouteCost::ambiguousOverlapCount,
                RouteCost::crossingCount,
                RouteCost::bendCount,
                RouteCost::manhattanLength,
                RouteCost::geometryIdentity,
            )
    }
}

private fun List<SpatialPoint>.geometryIdentity(): String =
    joinToString(";") { point -> "${point.x},${point.y}" }
