package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.ConnectionDocument
import com.engineeringood.athena.connection.TopologyOperatorKind
import com.engineeringood.athena.projection.ConnectionProjection
import com.engineeringood.athena.spatial.ConnectionCrossing
import com.engineeringood.athena.spatial.ConnectionCrossingId
import com.engineeringood.athena.spatial.ConnectionInterruptionAnchors
import com.engineeringood.athena.spatial.ConnectionInterruptionId
import com.engineeringood.athena.spatial.ConnectionJunction
import com.engineeringood.athena.spatial.ConnectionJunctionId
import com.engineeringood.athena.spatial.ConnectionRoutePlan
import com.engineeringood.athena.spatial.ConnectionRoutePlanId
import com.engineeringood.athena.spatial.ConnectionRouteSegment
import com.engineeringood.athena.spatial.ConnectionRouteTopologyPlan
import com.engineeringood.athena.spatial.ConnectionSharedSegment
import com.engineeringood.athena.spatial.ConnectionSharedSegmentId
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialSourceTrace

/** Derives explicit spatial topology from accepted IR operators and already-planned route geometry. */
internal class ConnectionRouteTopologyPlanner {
    fun plan(
        connectionIr: ConnectionDocument?,
        projections: List<ConnectionProjection>,
        routes: List<ConnectionRoutePlan>,
    ): ConnectionRouteTopologyPlan {
        val operators = connectionIr?.topologyOperators.orEmpty()
        val sharedNetIds = operators.filter {
            it.kind == TopologyOperatorKind.BRANCH ||
                it.kind == TopologyOperatorKind.MERGE ||
                it.kind == TopologyOperatorKind.INTERRUPTION
        }.map { it.netId.value }.toSet()
        val junctionNetIds = operators.filter {
            it.kind == TopologyOperatorKind.BRANCH || it.kind == TopologyOperatorKind.MERGE
        }.map { it.netId.value }.toSet()
        val routesBySemantic = routes.groupBy { it.connectionId.value }
        val junctions = mutableListOf<ConnectionJunction>()
        val crossings = mutableListOf<ConnectionCrossing>()
        val sharedSegments = mutableListOf<ConnectionSharedSegment>()

        routes.sortedBy { it.routeId.value }.forEachIndexed { firstIndex, first ->
            routes.drop(firstIndex + 1).sortedBy { it.routeId.value }.forEach { second ->
                val sameSemantic = first.connectionId == second.connectionId
                val sharedRoute = sameSemantic && first.connectionId.value in sharedNetIds
                val junctionEligible = sharedRoute && first.connectionId.value in junctionNetIds
                val intersections = first.segments.flatMap { left ->
                    second.segments.mapNotNull { right -> perpendicularIntersection(left, right) }
                }.distinct().sortedWith(compareBy(SpatialPoint::x, SpatialPoint::y))
                intersections.forEach { point ->
                    if (junctionEligible) {
                        junctions += ConnectionJunction(
                            id = ConnectionJunctionId(first.routeId.sheetId, first.connectionId.value, point),
                            netId = first.connectionId,
                            routeIds = listOf(first.routeId, second.routeId),
                            point = point,
                            sourceTrace = mergedTrace(first.sourceTrace, second.sourceTrace),
                        )
                    } else if (!sameSemantic && point !in setOf(first.points.first(), first.points.last(), second.points.first(), second.points.last())) {
                        val routeIds = listOf(first.routeId, second.routeId).sortedBy(ConnectionRoutePlanId::value)
                        crossings += ConnectionCrossing(
                            id = ConnectionCrossingId(first.routeId.sheetId, routeIds[0], routeIds[1], point),
                            routeIds = routeIds,
                            point = point,
                            bridgeOwnerRouteId = routeIds.first(),
                            sourceTrace = mergedTrace(first.sourceTrace, second.sourceTrace),
                        )
                    }
                }
                if (sharedRoute) {
                    first.segments.intersect(second.segments.toSet()).sortedWith(compareBy(ConnectionRouteSegment::geometryKey)).forEach { segment ->
                        sharedSegments += ConnectionSharedSegment(
                            id = ConnectionSharedSegmentId(first.routeId.sheetId, first.connectionId.value, segment),
                            netId = first.connectionId,
                            routeIds = listOf(first.routeId, second.routeId),
                            segment = segment,
                            sourceTrace = mergedTrace(first.sourceTrace, second.sourceTrace),
                        )
                    }
                }
            }
        }

        val interruptions = operators.filter { it.kind == TopologyOperatorKind.INTERRUPTION }.mapNotNull { operator ->
            val continuationIds = operator.orderedEndpointIds.map { it.value }
            if (continuationIds.size != 2) return@mapNotNull null
            val matchingRoutes = routesBySemantic[operator.netId.value].orEmpty().sortedBy { it.routeId.value }
            val points = when (matchingRoutes.size) {
                0 -> emptyList()
                1 -> listOf(matchingRoutes.single().points.first(), matchingRoutes.single().points.last())
                else -> listOf(matchingRoutes[0].points.first(), matchingRoutes[1].points.last())
            }
            if (points.size != 2 || points[0] == points[1]) return@mapNotNull null
            val sheetId = matchingRoutes.first().routeId.sheetId
            val trace = matchingRoutes.map(ConnectionRoutePlan::sourceTrace).reduce(::mergedTrace)
            ConnectionInterruptionAnchors(
                id = ConnectionInterruptionId(sheetId, operator.netId.value, continuationIds[0], continuationIds[1]),
                semanticId = operator.netId,
                continuationIds = continuationIds,
                points = points,
                sourceTrace = trace,
            )
        }

        return ConnectionRouteTopologyPlan(
            junctions = junctions.distinctBy { it.id }.sortedBy { it.id.value },
            crossings = crossings.distinctBy { it.id }.sortedBy { it.id.value },
            sharedSegments = sharedSegments.distinctBy { it.id }.sortedBy { it.id.value },
            interruptions = interruptions.distinctBy { it.id }.sortedBy { it.id.value },
        )
    }

    private fun perpendicularIntersection(first: ConnectionRouteSegment, second: ConnectionRouteSegment): SpatialPoint? {
        if (!first.isPositiveOrthogonal || !second.isPositiveOrthogonal || first.orientation == second.orientation) return null
        val horizontal = if (first.orientation == com.engineeringood.athena.spatial.SpatialLaneOrientation.HORIZONTAL) first to second else second to first
        val h = horizontal.first
        val v = horizontal.second
        val point = SpatialPoint(v.start.x, h.start.y)
        return point.takeIf { point.x in minOf(h.start.x, h.end.x)..maxOf(h.start.x, h.end.x) && point.y in minOf(v.start.y, v.end.y)..maxOf(v.start.y, v.end.y) }
    }
}

private fun ConnectionRouteSegment.geometryKey(): String =
    listOf(start, end).sortedWith(compareBy(SpatialPoint::x, SpatialPoint::y)).joinToString("-") { "${it.x},${it.y}" }

private fun mergedTrace(first: SpatialSourceTrace, second: SpatialSourceTrace): SpatialSourceTrace = SpatialSourceTrace(
    projectionIds = (first.projectionIds + second.projectionIds).distinct().sorted(),
    geometryElementIds = (first.geometryElementIds + second.geometryElementIds).distinctBy { it.value }.sortedBy { it.value },
)
