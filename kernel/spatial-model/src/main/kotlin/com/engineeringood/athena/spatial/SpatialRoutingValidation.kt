package com.engineeringood.athena.spatial

internal fun spatialRoutingDiagnostics(sheet: SpatialSheet): List<SpatialDiagnostic> {
    val occurrencesById = sheet.occurrences.groupBy(SpatialOccurrenceGeometry::occurrenceId)
    val anchorsById = sheet.anchors.groupBy(SpatialAnchorPosition::anchorId)
    val routesById = sheet.routes.groupBy(SpatialRoute::routeId)
    val lanesById = sheet.lanes.groupBy(SpatialLane::laneId)
    return buildList {
        anchorsById.filterValues { matches -> matches.size > 1 }.forEach { (anchorId, matches) ->
            add(
                spatialIssue(
                    subject = "Anchor ${anchorId.value}",
                    problem = "has ${matches.size} facts with the same identity",
                    correction = "Publish exactly one Anchor for each referenced same-Sheet occurrence-port.",
                    traces = matches.map(SpatialAnchorPosition::sourceTrace),
                ),
            )
        }
        routesById.filterValues { matches -> matches.size > 1 }.forEach { (routeId, matches) ->
            add(
                spatialIssue(
                    subject = "Route ${routeId.value}",
                    problem = "has ${matches.size} facts with the same identity",
                    correction = "Publish exactly one Route for each visible Projection Connection.",
                    traces = matches.map(SpatialRoute::sourceTrace),
                ),
            )
        }
        lanesById.filterValues { matches -> matches.size > 1 }.forEach { (laneId, _) ->
            add(
                SpatialDiagnostic(
                    subject = "Lane ${laneId.value}",
                    problem = "has ${lanesById.getValue(laneId).size} facts with the same identity",
                    correction = "Publish exactly one Lane for each used routing channel.",
                    sourceTrace = sheet.sourceTrace,
                ),
            )
        }
        sheet.anchors.forEach { anchor ->
            val occurrences = occurrencesById[anchor.subject.occurrenceId].orEmpty()
            if (occurrences.size != 1) {
                add(
                    SpatialDiagnostic(
                        subject = "Anchor ${anchor.anchorId.value}",
                        problem = "subject Occurrence ${anchor.subject.occurrenceId.projectionId} resolves to ${occurrences.size} geometry facts",
                        correction = "Reference one existing same-Sheet Occurrence from the Anchor.",
                        sourceTrace = anchor.sourceTrace,
                    ),
                )
            } else if (!anchor.matchesBoundary(occurrences.single().rectangle)) {
                add(
                    SpatialDiagnostic(
                        subject = "Anchor ${anchor.anchorId.value}",
                        problem = "point ${anchor.point.text()} is not strictly on declared ${anchor.side.name.lowercase()} boundary",
                        correction = "Place the Anchor on the declared Occurrence boundary side away from its corners.",
                        sourceTrace = anchor.sourceTrace,
                    ),
                )
            }
        }
        sheet.routes.forEach { route ->
            val source = anchorsById[route.sourceAnchorId].orEmpty().singleOrNull()
            val target = anchorsById[route.targetAnchorId].orEmpty().singleOrNull()
            if (source == null || target == null) {
                add(
                    SpatialDiagnostic(
                        subject = "Route ${route.routeId.value}",
                        problem = "does not resolve both endpoint Anchors exactly once",
                        correction = "Publish both exact typed endpoint Anchors before routing.",
                        sourceTrace = route.sourceTrace,
                    ),
                )
            } else {
                if (route.points.first() != source.point || route.points.last() != target.point) {
                    add(
                        SpatialDiagnostic(
                            subject = "Route ${route.routeId.value}",
                            problem = "endpoint points do not equal source and target Anchor points",
                            correction = "Preserve exact Anchor points as the first and final ordered Route points.",
                            sourceTrace = route.sourceTrace,
                        ),
                    )
                }
            }
            val requiredTrace = listOf(
                route.sheetId,
                route.routeId.projectionConnectionId,
                route.sourceAnchorId.occurrenceId.projectionId,
                route.sourceAnchorId.portId.value,
                route.targetAnchorId.occurrenceId.projectionId,
                route.targetAnchorId.portId.value,
            )
            if (route.sourceTrace.projectionIds.take(requiredTrace.size) != requiredTrace) {
                add(
                    SpatialDiagnostic(
                        subject = "Route ${route.routeId.value}",
                        problem = "Source Trace does not retain Sheet, Connection, and endpoint occurrence-port order",
                        correction = "Publish the six required Route trace positions in source-to-target order, including repeats.",
                        sourceTrace = route.sourceTrace,
                    ),
                )
            }
            route.segments.forEachIndexed { index, segment ->
                if (!segment.isPositiveOrthogonal) {
                    add(
                        SpatialDiagnostic(
                            subject = "Route ${route.routeId.value}",
                            problem = "contains nonpositive or non-orthogonal segment $index",
                            correction = "Publish only positive horizontal or vertical Route segments.",
                            sourceTrace = route.sourceTrace,
                        ),
                    )
                }
            }
            if (route.points.any { point -> !sheet.drawingArea.contains(point) }) {
                add(
                    SpatialDiagnostic(
                        subject = "Route ${route.routeId.value}",
                        problem = "contains a point outside Drawing Area ${sheet.drawingArea.text()}",
                        correction = "Keep every Route point inside the Route's owning Sheet Drawing Area.",
                        sourceTrace = route.sourceTrace,
                    ),
                )
            }
            val endpointOwners = setOf(route.sourceAnchorId.occurrenceId, route.targetAnchorId.occurrenceId)
            route.segments.forEachIndexed { index, segment ->
                if (segment.isPositiveOrthogonal) {
                    sheet.occurrences.filter { it.occurrenceId !in endpointOwners }
                        .filter { occurrence -> segment.entersInterior(occurrence.rectangle) }
                        .forEach { occurrence ->
                            add(
                                spatialIssue(
                                    subject = "Route ${route.routeId.value}",
                                    problem = "segment $index enters non-endpoint Occurrence ${occurrence.occurrenceId.projectionId} interior",
                                    correction = "Keep every Route segment outside non-endpoint Occurrence interiors.",
                                    traces = listOf(route.sourceTrace, occurrence.sourceTrace),
                                ),
                            )
                        }
                }
            }
            val lane = lanesById[route.laneId].orEmpty().singleOrNull()
            if (lane == null) {
                add(
                    SpatialDiagnostic(
                        subject = "Route ${route.routeId.value}",
                        problem = "Lane ${route.laneId.value} does not resolve exactly once",
                        correction = "Publish one same-Sheet used Lane for the Route's lane identity.",
                        sourceTrace = route.sourceTrace,
                    ),
                )
            }
        }
        val laneMembership = sheet.lanes.flatMap { lane -> lane.routeIds.map { routeId -> routeId to lane.laneId } }
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })
        sheet.lanes.forEach { lane ->
            if (!lane.isInside(sheet.drawingArea)) {
                add(
                    SpatialDiagnostic(
                        subject = "Lane ${lane.laneId.value}",
                        problem = "coordinate ${lane.coordinate} is outside Drawing Area ${sheet.drawingArea.text()}",
                        correction = "Keep every Lane coordinate inside its owning Sheet Drawing Area.",
                        sourceTrace = sheet.sourceTrace,
                    ),
                )
            }
            lane.routeIds.forEach { routeId ->
                val matches = routesById[routeId].orEmpty()
                if (matches.isEmpty()) {
                    add(
                        SpatialDiagnostic(
                            subject = "Lane ${lane.laneId.value}",
                            problem = "references missing Route ${routeId.value}",
                            correction = "List each existing same-Sheet Route exactly once in its owning Lane.",
                            sourceTrace = sheet.sourceTrace,
                        ),
                    )
                } else if (matches.size > 1) {
                    add(
                        SpatialDiagnostic(
                            subject = "Lane ${lane.laneId.value}",
                            problem = "references duplicate Route ${routeId.value}",
                            correction = "List one canonical same-Sheet Route identity in its owning Lane.",
                            sourceTrace = sheet.sourceTrace,
                        ),
                    )
                } else if (matches.single().laneId != lane.laneId) {
                    add(
                        SpatialDiagnostic(
                            subject = "Lane ${lane.laneId.value}",
                            problem = "lists Route ${routeId.value} owned by Lane ${matches.single().laneId.value}",
                            correction = "Keep Route and Lane reciprocal identities equal.",
                            sourceTrace = matches.single().sourceTrace,
                        ),
                    )
                } else if (!matches.single().uses(lane)) {
                    add(
                        SpatialDiagnostic(
                            subject = "Lane ${lane.laneId.value}",
                            problem = "member Route ${routeId.value} has no segment on this " +
                                "${lane.orientation.name.lowercase()} channel",
                            correction =
                                "Derive Lane membership from a Route segment using the same orientation and coordinate.",
                            sourceTrace = matches.single().sourceTrace,
                        ),
                    )
                }
            }
        }
        sheet.routes.forEach { route ->
            val memberships = laneMembership[route.routeId].orEmpty()
            if (memberships.size != 1) {
                add(
                    SpatialDiagnostic(
                        subject = "Route ${route.routeId.value}",
                        problem = "appears in ${memberships.size} Lane membership lists",
                        correction = "List the Route exactly once in the one Lane named by its lane identity.",
                        sourceTrace = route.sourceTrace,
                    ),
                )
            } else if (memberships.single() != route.laneId) {
                add(
                    SpatialDiagnostic(
                        subject = "Route ${route.routeId.value}",
                        problem = "is listed by Lane ${memberships.single().value} instead of ${route.laneId.value}",
                        correction = "List the Route only in the Lane named by its lane identity.",
                        sourceTrace = route.sourceTrace,
                    ),
                )
            }
        }
    }.distinct().sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem, SpatialDiagnostic::correction))
}

private fun SpatialAnchorPosition.matchesBoundary(rectangle: SpatialRect): Boolean = when (side) {
    SpatialBoundarySide.LEFT -> point.x == rectangle.x && point.y > rectangle.y && point.y < rectangle.bottom
    SpatialBoundarySide.RIGHT -> point.x == rectangle.right && point.y > rectangle.y && point.y < rectangle.bottom
    SpatialBoundarySide.TOP -> point.y == rectangle.y && point.x > rectangle.x && point.x < rectangle.right
    SpatialBoundarySide.BOTTOM -> point.y == rectangle.bottom && point.x > rectangle.x && point.x < rectangle.right
}

private fun SpatialRect.contains(point: SpatialPoint): Boolean = point.x in x..right && point.y in y..bottom

private fun SpatialRouteSegment.entersInterior(rectangle: SpatialRect): Boolean = when {
    start.y == end.y -> start.y > rectangle.y && start.y < rectangle.bottom &&
        maxOf(start.x, end.x) > rectangle.x && minOf(start.x, end.x) < rectangle.right
    start.x == end.x -> start.x > rectangle.x && start.x < rectangle.right &&
        maxOf(start.y, end.y) > rectangle.y && minOf(start.y, end.y) < rectangle.bottom
    else -> false
}

private fun SpatialLane.isInside(drawingArea: SpatialRect): Boolean = when (orientation) {
    SpatialLaneOrientation.HORIZONTAL -> coordinate in drawingArea.y..drawingArea.bottom
    SpatialLaneOrientation.VERTICAL -> coordinate in drawingArea.x..drawingArea.right
}

private fun SpatialRoute.uses(lane: SpatialLane): Boolean = segments.any { segment ->
    segment.isPositiveOrthogonal && when (lane.orientation) {
        SpatialLaneOrientation.HORIZONTAL -> segment.start.y == segment.end.y && segment.start.y == lane.coordinate
        SpatialLaneOrientation.VERTICAL -> segment.start.x == segment.end.x && segment.start.x == lane.coordinate
    }
}

private fun SpatialPoint.text(): String = "($x,$y)"
