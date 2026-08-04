package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionEndpoint
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.routing.OrthogonalRouteObstacle
import com.engineeringood.athena.routing.OrthogonalRoutePoint
import com.engineeringood.athena.routing.OrthogonalRouteRect
import com.engineeringood.athena.routing.OrthogonalRouteRequest
import com.engineeringood.athena.routing.OrthogonalRouteSide
import com.engineeringood.athena.routing.OrthogonalRouteSolveResult
import com.engineeringood.athena.routing.OrthogonalRouteSolver
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialLaneId
import com.engineeringood.athena.spatial.SpatialLaneOrientation
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialRoute
import com.engineeringood.athena.spatial.SpatialRouteId

data class SpatialRoutingSheetInput(
    val sheetId: String,
    val drawingArea: SpatialRect,
) {
    init {
        require(sheetId.isNotBlank()) { "Spatial routing Sheet identity must not be blank." }
    }
}

data class SpatialRouteCompilationResult(
    val lanes: List<SpatialLane>,
    val routes: List<SpatialRoute>,
    val diagnostics: List<SpatialDiagnostic> = emptyList(),
)

class SpatialRouteCompiler(
    private val solver: OrthogonalRouteSolver = OrthogonalRouteSolver(),
    private val validator: SpatialRouteValidator = SpatialRouteValidator(),
) {
    fun compile(
        projection: ProjectionDocument,
        sheets: List<SpatialRoutingSheetInput>,
        occurrences: List<SpatialOccurrenceGeometry>,
        anchors: List<SpatialAnchorPosition>,
    ): SpatialRouteCompilationResult {
        if (projection.connections.isEmpty()) {
            return SpatialRouteCompilationResult(lanes = emptyList(), routes = emptyList())
        }
        val sheetInputs = sheets.groupBy(SpatialRoutingSheetInput::sheetId)
        val resolved = projection.connections
            .sortedBy { connection -> connection.projectionId.value }
            .map { connection ->
                val sheet = resolveConnectionSheet(connection, projection)
                ResolvedRoute(
                    connection = connection,
                    sheet = sheet,
                    source = resolve(projection, connection, connection.source, "source", sheet.sheetId, anchors),
                    target = resolve(projection, connection, connection.target, "target", sheet.sheetId, anchors),
                )
            }
        val resolutionDiagnostics = resolved.flatMap { route ->
            listOfNotNull(route.sheet.diagnostic, route.source.diagnostic, route.target.diagnostic)
        }
        if (resolutionDiagnostics.isNotEmpty()) {
            return SpatialRouteCompilationResult(emptyList(), emptyList(), resolutionDiagnostics.canonicalDiagnostics())
        }

        val solved = resolved.map { route ->
            solve(route, sheetInputs, occurrences)
        }
        val solveDiagnostics = solved.mapNotNull(SolvedRoute::diagnostic).canonicalDiagnostics()
        if (solveDiagnostics.isNotEmpty()) {
            return SpatialRouteCompilationResult(emptyList(), emptyList(), solveDiagnostics)
        }

        val routes = solved.map { solvedRoute -> requireNotNull(solvedRoute.route) }
            .sortedBy { route -> route.routeId.value }
        val lanes = routes.groupBy(SpatialRoute::laneId)
            .map { (laneId, laneRoutes) ->
                SpatialLane(
                    laneId = laneId,
                    sheetId = laneId.sheetId,
                    orientation = laneId.orientation,
                    coordinate = laneId.coordinate,
                    routeIds = laneRoutes.map(SpatialRoute::routeId).sortedBy(SpatialRouteId::value),
                )
            }
            .sortedBy { lane -> lane.laneId.value }
        val validationDiagnostics = validator.validate(
            projection = projection,
            sheets = sheets,
            occurrences = occurrences,
            anchors = anchors,
            routes = routes,
            lanes = lanes,
        )
        return if (validationDiagnostics.isEmpty()) {
            SpatialRouteCompilationResult(lanes, routes)
        } else {
            SpatialRouteCompilationResult(emptyList(), emptyList(), validationDiagnostics)
        }
    }

    private fun solve(
        resolved: ResolvedRoute,
        sheets: Map<String, List<SpatialRoutingSheetInput>>,
        occurrences: List<SpatialOccurrenceGeometry>,
    ): SolvedRoute {
        val source = requireNotNull(resolved.source.anchor)
        val target = requireNotNull(resolved.target.anchor)
        val owningSheetId = requireNotNull(resolved.sheet.sheetId)
        val connection = resolved.connection
        if (source.sheetId != target.sheetId) {
            return SolvedRoute(
                diagnostic = routeDiagnostic(
                    connection = connection,
                    source = source,
                    target = target,
                    problem = "connects Anchors on different Sheets '${source.sheetId}' and '${target.sheetId}'",
                    correction = "Keep both endpoints on one Sheet or defer explicit multi-Sheet continuation to M45.",
                ),
            )
        }
        if (source.sheetId != owningSheetId) {
            return SolvedRoute(
                diagnostic = routeDiagnostic(
                    connection = connection,
                    source = source,
                    target = target,
                    problem = "owns Sheet $owningSheetId but both Anchors resolve on Sheet ${source.sheetId}",
                    correction = "Place both endpoint Occurrences on the Connection's owning Sheet $owningSheetId.",
                ),
            )
        }
        val sheetMatches = sheets[source.sheetId].orEmpty()
        if (sheetMatches.size != 1) {
            val problem = if (sheetMatches.isEmpty()) {
                "has no routing Drawing Area for Sheet ${source.sheetId}"
            } else {
                "has ${sheetMatches.size} routing Drawing Areas for Sheet ${source.sheetId}"
            }
            return SolvedRoute(
                diagnostic = routeDiagnostic(
                    connection,
                    source,
                    target,
                    problem,
                    "Provide exactly one Drawing Area for the Route's owning Sheet.",
                ),
            )
        }
        val drawingArea = sheetMatches.single().drawingArea
        val sheetOccurrences = occurrences
            .filter { occurrence -> occurrence.sheetId == source.sheetId }
            .sortedWith(compareBy({ it.occurrenceId.projectionId }, { it.rectangle.x }, { it.rectangle.y }))
        val endpointOwnerIds = setOf(source.subject.occurrenceId, target.subject.occurrenceId)
        val nonEndpointObstacles = sheetOccurrences.filterNot { occurrence -> occurrence.occurrenceId in endpointOwnerIds }
        val request = OrthogonalRouteRequest(
            requestId = connection.projectionId.value,
            source = source.point.toRoutingPoint(),
            target = target.point.toRoutingPoint(),
            sourceSide = source.side.toRoutingSide(),
            targetSide = target.side.toRoutingSide(),
            drawingArea = drawingArea.toRoutingRect(),
            obstacles = sheetOccurrences.map { occurrence ->
                OrthogonalRouteObstacle(
                    obstacleId = "sheet=${occurrence.sheetId}:occurrence=${occurrence.occurrenceId.projectionId}",
                    bounds = occurrence.rectangle.toRoutingRect(),
                )
            },
        )
        return when (val result = solver.solve(request)) {
            is OrthogonalRouteSolveResult.Success -> {
                val points = result.points.map { point -> SpatialPoint(point.x, point.y) }
                val routeId = SpatialRouteId(source.sheetId, connection.projectionId.value)
                val laneId = basicSpatialLaneId(source.sheetId, points)
                SolvedRoute(
                    route = SpatialRoute(
                        routeId = routeId,
                        sheetId = source.sheetId,
                        connectionId = connection.semanticId,
                        sourceAnchorId = source.anchorId,
                        targetAnchorId = target.anchorId,
                        laneId = laneId,
                        sourceTrace = canonicalRouteSourceTrace(source.sheetId, connection, source, target),
                        points = points,
                    ),
                )
            }
            OrthogonalRouteSolveResult.NoPath -> SolvedRoute(
                diagnostic = routeDiagnostic(
                    connection = connection,
                    source = source,
                    target = target,
                    problem = noPathProblem(source.sheetId, nonEndpointObstacles),
                    correction = noPathCorrection(nonEndpointObstacles),
                    obstacles = nonEndpointObstacles,
                ),
            )
        }
    }

    private fun resolveConnectionSheet(
        connection: ProjectionConnection,
        projection: ProjectionDocument,
    ): ConnectionSheetResolution {
        val matches = projection.connectionSheetIds(connection)
        if (matches.size == 1) return ConnectionSheetResolution(sheetId = matches.single())
        val problem = if (matches.isEmpty()) {
            "has no owning Sheet"
        } else {
            "belongs to ${matches.size} Sheets: ${matches.joinToString()}"
        }
        return ConnectionSheetResolution(
            diagnostic = diagnostic(
                subject = "Connection ${connection.projectionId.value}",
                problem = problem,
                correction = "Publish the Connection on exactly one Projection Sheet before routing.",
                projectionIds = listOf(connection.projectionId.value) + matches,
                geometryIds = listOf(connection.originGeometryElementId),
            ),
        )
    }

    private fun resolve(
        projection: ProjectionDocument,
        connection: ProjectionConnection,
        endpoint: ProjectionConnectionEndpoint?,
        role: String,
        owningSheetId: String?,
        anchors: List<SpatialAnchorPosition>,
    ): AnchorResolution {
        if (endpoint == null) {
            return AnchorResolution(
                diagnostic = diagnostic(
                    subject = "Connection ${connection.projectionId.value}",
                    problem = "has no typed $role endpoint",
                    correction = "Resolve one occurrence-port before routing this Connection.",
                    projectionIds = listOfNotNull(owningSheetId, connection.projectionId.value),
                    geometryIds = listOf(connection.originGeometryElementId),
                ),
            )
        }
        val identity = endpoint.occurrencePortId
        val matches = anchors.filter { anchor ->
            anchor.subject.occurrenceId.projectionId == identity.occurrenceId.value &&
                anchor.subject.portId == identity.portId
        }
        val endpointSheetIds = projection.sheets.filter { sheet ->
            sheet.subjects.any { subject -> identity.occurrenceId in subject.nodeIds }
        }.map { sheet -> sheet.sheetId.value }.distinct().sorted()
        val requiredSheetId = endpointSheetIds.singleOrNull() ?: owningSheetId
        val requiredSheetMatches = matches.filter { anchor -> anchor.sheetId == requiredSheetId }
        if (requiredSheetId != null && requiredSheetMatches.size == 1) {
            return AnchorResolution(anchor = requiredSheetMatches.single())
        }
        if (requiredSheetId == null && matches.size == 1) return AnchorResolution(anchor = matches.single())
        val portOrigins = projection.occurrencePorts
            .filter { port -> port.occurrencePortId == identity }
            .map { port -> port.originGeometryElementId }
        val problem = when {
            matches.isEmpty() -> "has no resolved $role Anchor"
            requiredSheetId != null && requiredSheetMatches.isEmpty() -> {
                val foreignSheetIds = matches.map(SpatialAnchorPosition::sheetId).distinct().sorted()
                val resolution = if (matches.size == 1) {
                    "matching Anchor resolves on Sheet ${foreignSheetIds.single()}"
                } else {
                    "${matches.size} matching Anchors resolve across Sheets ${foreignSheetIds.joinToString()}"
                }
                val requiredSheetRole = if (requiredSheetId == owningSheetId) "owning" else "endpoint"
                "has no resolved $role Anchor on $requiredSheetRole Sheet $requiredSheetId; $resolution"
            }
            requiredSheetMatches.size > 1 -> {
                val requiredSheetRole = if (requiredSheetId == owningSheetId) "owning" else "endpoint"
                "has ${requiredSheetMatches.size} resolved $role Anchors on $requiredSheetRole Sheet $requiredSheetId"
            }
            else -> "has ${matches.size} resolved $role Anchors across Sheets"
        }
        return AnchorResolution(
            diagnostic = diagnostic(
                subject = "Connection ${connection.projectionId.value}",
                problem = problem,
                correction = "Resolve port ${identity.portId.value} on Occurrence ${identity.occurrenceId.value} before routing this Connection.",
                projectionIds = listOfNotNull(owningSheetId) +
                    listOf(connection.projectionId.value, identity.occurrenceId.value, identity.portId.value) +
                    matches.map(SpatialAnchorPosition::sheetId).distinct().sorted(),
                geometryIds = listOf(connection.originGeometryElementId) + portOrigins +
                    matches.flatMap { anchor -> anchor.sourceTrace.geometryElementIds },
            ),
        )
    }

    private fun routeDiagnostic(
        connection: ProjectionConnection,
        source: SpatialAnchorPosition,
        target: SpatialAnchorPosition,
        problem: String,
        correction: String,
        obstacles: List<SpatialOccurrenceGeometry> = emptyList(),
    ): SpatialDiagnostic = diagnostic(
        subject = "Connection ${connection.projectionId.value}",
        problem = problem,
        correction = correction,
        projectionIds = listOf(
            connection.projectionId.value,
            source.sheetId,
            source.subject.occurrenceId.projectionId,
            source.subject.portId.value,
            target.sheetId,
            target.subject.occurrenceId.projectionId,
            target.subject.portId.value,
        ) + obstacles.map { obstacle -> obstacle.occurrenceId.projectionId },
        geometryIds = listOf(connection.originGeometryElementId) +
            source.sourceTrace.geometryElementIds +
            target.sourceTrace.geometryElementIds +
            obstacles.flatMap { obstacle -> obstacle.sourceTrace.geometryElementIds },
    )

    private data class AnchorResolution(
        val anchor: SpatialAnchorPosition? = null,
        val diagnostic: SpatialDiagnostic? = null,
    )

    private data class ConnectionSheetResolution(
        val sheetId: String? = null,
        val diagnostic: SpatialDiagnostic? = null,
    )

    private data class ResolvedRoute(
        val connection: ProjectionConnection,
        val sheet: ConnectionSheetResolution,
        val source: AnchorResolution,
        val target: AnchorResolution,
    )

    private data class SolvedRoute(
        val route: SpatialRoute? = null,
        val diagnostic: SpatialDiagnostic? = null,
    )

}

private fun noPathProblem(
    sheetId: String,
    obstacles: List<SpatialOccurrenceGeometry>,
): String = if (obstacles.isEmpty()) {
    "has no obstacle-safe orthogonal Route inside Sheet $sheetId Drawing Area because the Drawing Area boundary and endpoint side exits leave no candidate path"
} else {
    "has no obstacle-safe orthogonal Route inside Sheet $sheetId Drawing Area; blocking Occurrences: " +
        obstacles.joinToString { obstacle -> obstacle.occurrenceId.projectionId }
}

private fun noPathCorrection(obstacles: List<SpatialOccurrenceGeometry>): String = if (obstacles.isEmpty()) {
    "Move endpoint Occurrences inward or choose boundary sides that leave an in-area orthogonal path."
} else {
    "Move or regroup the named blocking Occurrences so one in-area orthogonal path remains."
}

internal fun ProjectionDocument.connectionSheetIds(connection: ProjectionConnection): List<String> =
    sheets.filter { sheet ->
        sheet.subjects.any { subject -> connection.projectionId in subject.connectionIds }
    }.map { sheet -> sheet.sheetId.value }.sorted()

internal fun basicSpatialLaneId(sheetId: String, points: List<SpatialPoint>): SpatialLaneId {
    val primary = points.zipWithNext()
        .map { (start, end) ->
            val orientation = if (start.y == end.y) {
                SpatialLaneOrientation.HORIZONTAL
            } else {
                SpatialLaneOrientation.VERTICAL
            }
            BasicChannelSegment(
                orientation = orientation,
                coordinate = if (orientation == SpatialLaneOrientation.HORIZONTAL) start.y else start.x,
                length = kotlin.math.abs(start.x.toLong() - end.x.toLong()) +
                    kotlin.math.abs(start.y.toLong() - end.y.toLong()),
                start = start,
                end = end,
            )
        }
        .sortedWith(
            compareByDescending<BasicChannelSegment> { segment -> segment.length }
                .thenBy { segment -> segment.orientation }
                .thenBy { segment -> segment.coordinate }
                .thenBy { segment -> segment.start.x }
                .thenBy { segment -> segment.start.y }
                .thenBy { segment -> segment.end.x }
                .thenBy { segment -> segment.end.y },
        )
        .first()
    return SpatialLaneId(sheetId, primary.orientation, primary.coordinate)
}

private data class BasicChannelSegment(
    val orientation: SpatialLaneOrientation,
    val coordinate: Int,
    val length: Long,
    val start: SpatialPoint,
    val end: SpatialPoint,
)

private fun SpatialPoint.toRoutingPoint(): OrthogonalRoutePoint = OrthogonalRoutePoint(x, y)

private fun SpatialRect.toRoutingRect(): OrthogonalRouteRect = OrthogonalRouteRect(x, y, width, height)

private fun com.engineeringood.athena.spatial.SpatialBoundarySide.toRoutingSide(): OrthogonalRouteSide = when (this) {
    com.engineeringood.athena.spatial.SpatialBoundarySide.LEFT -> OrthogonalRouteSide.LEFT
    com.engineeringood.athena.spatial.SpatialBoundarySide.RIGHT -> OrthogonalRouteSide.RIGHT
    com.engineeringood.athena.spatial.SpatialBoundarySide.TOP -> OrthogonalRouteSide.TOP
    com.engineeringood.athena.spatial.SpatialBoundarySide.BOTTOM -> OrthogonalRouteSide.BOTTOM
}
