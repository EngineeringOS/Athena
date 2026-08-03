package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialReality
import com.engineeringood.athena.spatial.SpatialRoute

data class SpatialRouteCompilationResult(
    val anchorPositions: List<SpatialAnchorPosition>,
    val lanes: List<SpatialLane>,
    val routes: List<SpatialRoute>,
    val diagnostics: List<RealityTransformationDiagnostic> = emptyList(),
)

class SpatialRouteCompiler {
    fun compile(
        projection: ProjectionDocument,
        occurrences: List<SpatialOccurrenceGeometry>,
    ): SpatialRouteCompilationResult {
        if (occurrences.isEmpty()) {
            return failure("missing occurrence geometry facts")
        }

        val anchors = anchorsFor(
            nodes = projection.nodes,
            occurrences = occurrences,
        )
        if (projection.connections.isNotEmpty() && anchors.size < 2) {
            return failure("missing anchor position facts")
        }
        val lanes = if (projection.connections.isEmpty()) {
            emptyList()
        } else {
            listOf(SpatialLane(laneId = "lane:${projection.view.id}:main", direction = "horizontal"))
        }
        val routes = routesFor(
            projection = projection,
            anchors = anchors,
            lane = lanes.firstOrNull(),
        )
        return SpatialRouteCompilationResult(
            anchorPositions = anchors,
            lanes = lanes,
            routes = routes,
        )
    }

    private fun anchorsFor(
        nodes: List<com.engineeringood.athena.projection.ProjectionNode>,
        occurrences: List<SpatialOccurrenceGeometry>,
    ): List<SpatialAnchorPosition> {
        val geometryByProjectionId = occurrences.associateBy { occurrence -> occurrence.occurrenceId.projectionId }
        return nodes.flatMap { node ->
            val occurrenceId = node.projectionId.value
            val geometry = geometryByProjectionId[occurrenceId] ?: return@flatMap emptyList()
            val rectangle = geometry.rectangle
            listOf(
                SpatialAnchorPosition(
                    anchorId = "anchor:$occurrenceId:left",
                    occurrenceId = occurrenceId,
                    x = rectangle.x.toDouble(),
                    y = rectangle.y + rectangle.height / 2.0,
                ),
                SpatialAnchorPosition(
                    anchorId = "anchor:$occurrenceId:right",
                    occurrenceId = occurrenceId,
                    x = rectangle.right.toDouble(),
                    y = rectangle.y + rectangle.height / 2.0,
                ),
            )
        }
    }

    private fun routesFor(
        projection: ProjectionDocument,
        anchors: List<SpatialAnchorPosition>,
        lane: SpatialLane?,
    ): List<SpatialRoute> {
        val laneId = lane?.laneId ?: return emptyList()
        val firstOccurrenceId = projection.nodes.firstOrNull()?.projectionId?.value
        val lastOccurrenceId = projection.nodes.lastOrNull()?.projectionId?.value
        val anchorsByOccurrence = anchors.groupBy { anchor -> anchor.occurrenceId }
        return projection.connections.mapNotNull { connection ->
            val sourceOccurrenceId = connection.sourceOccurrenceId ?: firstOccurrenceId ?: return@mapNotNull null
            val targetOccurrenceId = connection.targetOccurrenceId ?: lastOccurrenceId ?: return@mapNotNull null
            val start = anchorsByOccurrence[sourceOccurrenceId]
                ?.firstOrNull { anchor -> anchor.anchorId.endsWith(":right") }
                ?: anchorsByOccurrence[sourceOccurrenceId]?.firstOrNull()
                ?: return@mapNotNull null
            val end = anchorsByOccurrence[targetOccurrenceId]
                ?.firstOrNull { anchor -> anchor.anchorId.endsWith(":left") }
                ?: anchorsByOccurrence[targetOccurrenceId]?.firstOrNull()
                ?: return@mapNotNull null
            SpatialRoute(
                routeId = "route:${connection.projectionId.value}",
                connectionId = connection.semanticId,
                sourceOccurrenceId = start.occurrenceId,
                targetOccurrenceId = end.occurrenceId,
                sourceAnchorId = start.anchorId,
                targetAnchorId = end.anchorId,
                sourcePortId = connection.sourcePortId,
                targetPortId = connection.targetPortId,
                laneId = laneId,
                points = orthogonalPoints(start, end),
            )
        }
    }

    private fun orthogonalPoints(
        start: SpatialAnchorPosition,
        end: SpatialAnchorPosition,
    ): List<SpatialPoint> {
        val midX = (start.x + end.x) / 2.0
        return listOf(
            SpatialPoint(start.x, start.y),
            SpatialPoint(midX, start.y),
            SpatialPoint(midX, end.y),
            SpatialPoint(end.x, end.y),
        ).withoutRedundantMiddlePoint()
    }

    private fun failure(message: String): SpatialRouteCompilationResult =
        SpatialRouteCompilationResult(
            anchorPositions = emptyList(),
            lanes = emptyList(),
            routes = emptyList(),
            diagnostics = listOf(
                RealityTransformationDiagnostic(
                    reality = SpatialReality.name,
                    message = message,
                ),
            ),
        )
}

private fun List<SpatialPoint>.withoutRedundantMiddlePoint(): List<SpatialPoint> =
    if (size == 3 && first().y == last().y) {
        listOf(first(), last())
    } else {
        this
    }
