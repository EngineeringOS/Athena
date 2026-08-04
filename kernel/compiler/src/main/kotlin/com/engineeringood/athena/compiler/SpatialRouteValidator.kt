package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialBoundarySide
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialRoute
import com.engineeringood.athena.spatial.SpatialRouteId
import com.engineeringood.athena.spatial.SpatialRouteSegment

class SpatialRouteValidator {
    fun validate(
        projection: ProjectionDocument,
        sheets: List<SpatialRoutingSheetInput>,
        occurrences: List<SpatialOccurrenceGeometry>,
        anchors: List<SpatialAnchorPosition>,
        routes: List<SpatialRoute>,
        lanes: List<SpatialLane>,
    ): List<SpatialDiagnostic> {
        val sheetById = sheets.groupBy(SpatialRoutingSheetInput::sheetId)
        val anchorById = anchors.groupBy(SpatialAnchorPosition::anchorId)
        val occurrenceById = occurrences.groupBy(SpatialOccurrenceGeometry::occurrenceId)
        val routeById = routes.groupBy(SpatialRoute::routeId)
        val laneById = lanes.groupBy(SpatialLane::laneId)
        val connectionByProjectionId = projection.connections.groupBy { connection -> connection.projectionId.value }
        val expectedOccurrenceIds = projection.sheets.flatMap { sheet ->
            sheet.subjects.flatMap { subject ->
                subject.nodeIds.map { nodeId -> SpatialOccurrenceId(sheet.sheetId.value, nodeId.value) }
            }
        }.distinct().sortedWith(compareBy({ it.sheetId }, { it.projectionId }))
        val expectedRouteByProjectionId = projection.connections.mapNotNull { connection ->
            projection.connectionSheetIds(connection).singleOrNull()?.let { sheetId ->
                connection.projectionId.value to (SpatialRouteId(sheetId, connection.projectionId.value) to connection.semanticId)
            }
        }.toMap()
        val diagnostics = buildList {
            expectedOccurrenceIds.forEach { occurrenceId ->
                val count = occurrenceById[occurrenceId].orEmpty().size
                if (count != 1) {
                    add(
                        issue(
                            subject = "Occurrence ${occurrenceId.projectionId} on Sheet ${occurrenceId.sheetId}",
                            problem = if (count == 0) {
                                "has missing Occurrence geometry"
                            } else {
                                "has duplicate Occurrence geometry facts ($count)"
                            },
                            correction = "Publish exactly one placed Occurrence rectangle for every visible Projection node.",
                        ),
                    )
                }
            }
            if (routes.size != projection.connections.size) {
                add(
                    issue(
                        subject = "Spatial Routes",
                        problem = "cover ${routes.size} Routes for ${projection.connections.size} visible Connections",
                        correction = "Publish exactly one Route for every visible Projection Connection.",
                    ),
                )
            }
            routeById.filterValues { matches -> matches.size > 1 }.forEach { (routeId, matches) ->
                add(
                    issue(
                        subject = "Route ${routeId.value}",
                        problem = "has ${matches.size} facts with the same identity",
                        correction = "Publish one canonical Route fact for this Connection.",
                    ),
                )
            }
            laneById.filterValues { matches -> matches.size > 1 }.forEach { (laneId, matches) ->
                add(
                    issue(
                        subject = "Lane ${laneId.value}",
                        problem = "has ${matches.size} facts with the same identity",
                        correction = "Publish one canonical Lane fact for this routing channel.",
                    ),
                )
            }
            projection.connections.forEach { connection ->
                val expected = expectedRouteByProjectionId[connection.projectionId.value]
                val matches = routes.filter { route ->
                    route.routeId.projectionConnectionId == connection.projectionId.value
                }
                if (expected == null || matches.size != 1 ||
                    matches.singleOrNull()?.routeId != expected.first ||
                    matches.singleOrNull()?.connectionId != expected.second
                ) {
                    add(
                        issue(
                            subject = "Connection ${connection.projectionId.value}",
                            problem = "does not match exact Projection Connection identity in published Route facts",
                            correction = "Publish one Sheet-owned Route with this Projection and semantic Connection identity.",
                        ),
                    )
                }
            }
            routes.filter { route -> route.routeId.projectionConnectionId !in expectedRouteByProjectionId }.forEach { route ->
                add(
                    route.issue(
                        "does not match exact Projection Connection identity in the visible Connection set",
                        "Remove the extra Route or restore its exact Projection Connection identity.",
                        anchors,
                    ),
                )
            }
            routes.forEach { route ->
                val source = anchorById[route.sourceAnchorId].orEmpty().singleOrNull()
                val target = anchorById[route.targetAnchorId].orEmpty().singleOrNull()
                val sheet = sheetById[route.sheetId].orEmpty().singleOrNull()
                val connection = connectionByProjectionId[route.routeId.projectionConnectionId].orEmpty().singleOrNull()
                if (source == null || target == null) {
                    add(
                        route.issue(
                            "does not resolve both endpoint Anchors exactly once",
                            "Publish both exact typed endpoint Anchors before routing.",
                            anchors,
                        ),
                    )
                } else {
                    if (connection != null && !route.matchesProjectionEndpoints(connection)) {
                        add(
                            route.issue(
                                "typed endpoint Anchors do not match the Projection Connection source and target occurrence-ports",
                                "Use the exact source and target occurrence-port Anchors named by the Projection Connection.",
                                listOf(source, target),
                            ),
                        )
                    }
                    if (route.points.first() != source.point || route.points.last() != target.point) {
                        add(
                            route.issue(
                                "endpoint points do not equal the source and target Anchor points",
                                "Preserve exact Anchor points as the first and final Route points.",
                                listOf(source, target),
                            ),
                        )
                    }
                    if (!route.leaves(source) || !route.approaches(target)) {
                        add(
                            route.issue(
                                "does not leave and approach endpoint bodies through their declared Anchor sides",
                                "Route the first and final segment through the outside of each Anchor side.",
                                listOf(source, target),
                            ),
                        )
                    }
                    if (connection != null &&
                        route.sourceTrace != canonicalRouteSourceTrace(route.sheetId, connection, source, target)
                    ) {
                        add(
                            route.issue(
                                "Source Trace does not match its exact Connection and endpoint Anchor derivation",
                                "Publish the canonical Route Source Trace for this Connection, owning Sheet, endpoints, and source geometry.",
                                listOf(source, target),
                            ),
                        )
                    }
                    route.segments.forEachIndexed { index, segment ->
                        if (!segment.isPositiveOrthogonal) {
                            add(
                                route.issue(
                                    "contains nonpositive or non-orthogonal segment $index",
                                    "Publish only positive horizontal or vertical Route segments.",
                                    listOf(source, target),
                                ),
                            )
                        }
                    }
                    val endpointOwnerIds = setOf(source.subject.occurrenceId, target.subject.occurrenceId)
                    val obstacles = occurrences.filter { occurrence -> occurrence.sheetId == route.sheetId }
                    route.segments.forEachIndexed { index, segment ->
                        obstacles.filter { obstacle -> segment.entersInterior(obstacle.rectangle) }.forEach { obstacle ->
                            val ownerKind = if (obstacle.occurrenceId in endpointOwnerIds) {
                                "endpoint-owner"
                            } else {
                                "non-endpoint"
                            }
                            add(
                                route.issue(
                                    "segment $index enters $ownerKind Occurrence ${obstacle.occurrenceId.projectionId} interior",
                                    "Keep the Route outside every Occurrence interior after leaving its exact Anchor.",
                                    listOf(source, target),
                                    obstacle.sourceTrace.geometryElementIds,
                                ),
                            )
                        }
                    }
                }
                if (sheet == null || route.points.any { point -> !sheet.drawingArea.contains(point) }) {
                    add(
                        route.issue(
                            "contains a point outside its one owning-Sheet Drawing Area",
                            "Keep every Route point inside the Route's owning Drawing Area.",
                            listOfNotNull(source, target),
                        ),
                    )
                }
                val lane = laneById[route.laneId].orEmpty().singleOrNull()
                if (lane == null || route.routeId !in lane.routeIds) {
                    add(
                        route.issue(
                            "does not resolve one reciprocal Lane membership",
                            "Publish one used Lane that lists this Route exactly once.",
                            listOfNotNull(source, target),
                        ),
                    )
                }
                if (route.segments.all { segment -> segment.isPositiveOrthogonal } &&
                    route.laneId != basicSpatialLaneId(route.sheetId, route.points)
                ) {
                    add(
                        route.issue(
                            "Lane ${route.laneId.value} does not describe the Route's canonical basic channel",
                            "Assign the Lane derived from the longest canonical Route segment.",
                            listOfNotNull(source, target),
                        ),
                    )
                }
                if (occurrenceById[route.sourceAnchorId.occurrenceId].orEmpty().size != 1 ||
                    occurrenceById[route.targetAnchorId.occurrenceId].orEmpty().size != 1
                ) {
                    add(
                        route.issue(
                            "does not resolve both endpoint-owner Occurrences exactly once",
                            "Publish one same-Sheet Occurrence geometry fact for each endpoint Anchor.",
                            listOfNotNull(source, target),
                        ),
                    )
                }
            }
            val membership = lanes.flatMap { lane -> lane.routeIds.map { routeId -> routeId to lane.laneId } }
                .groupBy({ it.first }, { it.second })
            lanes.forEach { lane ->
                lane.routeIds.filter { routeId -> routeById[routeId].orEmpty().size != 1 }.forEach { routeId ->
                    add(
                        issue(
                            subject = "Lane ${lane.laneId.value}",
                            problem = "references missing or duplicate Route ${routeId.value}",
                            correction = "List each existing Route exactly once in its owning Lane.",
                        ),
                    )
                }
            }
            routes.forEach { route ->
                if (membership[route.routeId].orEmpty().size != 1) {
                    add(
                        route.issue(
                            "appears in ${membership[route.routeId].orEmpty().size} Lane membership lists",
                            "List the Route in exactly one owning-Sheet Lane.",
                            anchors,
                        ),
                    )
                }
            }
        }
        return diagnostics.canonicalDiagnostics()
    }
}

private fun SpatialRoute.matchesProjectionEndpoints(connection: ProjectionConnection): Boolean {
    val expectedSource = connection.source?.occurrencePortId ?: return false
    val expectedTarget = connection.target?.occurrencePortId ?: return false
    return sourceAnchorId.occurrenceId.projectionId == expectedSource.occurrenceId.value &&
        sourceAnchorId.portId == expectedSource.portId &&
        targetAnchorId.occurrenceId.projectionId == expectedTarget.occurrenceId.value &&
        targetAnchorId.portId == expectedTarget.portId
}

private fun SpatialRoute.leaves(anchor: SpatialAnchorPosition): Boolean {
    val next = points.getOrNull(1) ?: return false
    return when (anchor.side) {
        SpatialBoundarySide.LEFT -> next.x < anchor.point.x && next.y == anchor.point.y
        SpatialBoundarySide.RIGHT -> next.x > anchor.point.x && next.y == anchor.point.y
        SpatialBoundarySide.TOP -> next.y < anchor.point.y && next.x == anchor.point.x
        SpatialBoundarySide.BOTTOM -> next.y > anchor.point.y && next.x == anchor.point.x
    }
}

private fun SpatialRoute.approaches(anchor: SpatialAnchorPosition): Boolean {
    val previous = points.getOrNull(points.lastIndex - 1) ?: return false
    return when (anchor.side) {
        SpatialBoundarySide.LEFT -> previous.x < anchor.point.x && previous.y == anchor.point.y
        SpatialBoundarySide.RIGHT -> previous.x > anchor.point.x && previous.y == anchor.point.y
        SpatialBoundarySide.TOP -> previous.y < anchor.point.y && previous.x == anchor.point.x
        SpatialBoundarySide.BOTTOM -> previous.y > anchor.point.y && previous.x == anchor.point.x
    }
}

private fun SpatialRouteSegment.entersInterior(rectangle: SpatialRect): Boolean = when {
    start.y == end.y -> {
        start.y > rectangle.y && start.y < rectangle.bottom &&
            maxOf(start.x, end.x) > rectangle.x && minOf(start.x, end.x) < rectangle.right
    }
    start.x == end.x -> {
        start.x > rectangle.x && start.x < rectangle.right &&
            maxOf(start.y, end.y) > rectangle.y && minOf(start.y, end.y) < rectangle.bottom
    }
    else -> true
}

private fun SpatialRect.contains(point: SpatialPoint): Boolean =
    point.x in x..right && point.y in y..bottom

private fun SpatialRoute.issue(
    problem: String,
    correction: String,
    anchors: List<SpatialAnchorPosition>,
    additionalGeometryIds: List<GeometryElementId> = emptyList(),
): SpatialDiagnostic = diagnostic(
    subject = "Route ${routeId.value}",
    problem = problem,
    correction = correction,
    projectionIds = listOf(routeId.projectionConnectionId, sheetId) +
        anchors.flatMap { anchor -> anchor.sourceTrace.projectionIds },
    geometryIds = anchors.flatMap { anchor -> anchor.sourceTrace.geometryElementIds } + additionalGeometryIds,
)

private fun issue(
    subject: String,
    problem: String,
    correction: String,
): SpatialDiagnostic = diagnostic(subject, problem, correction, emptyList(), emptyList())
