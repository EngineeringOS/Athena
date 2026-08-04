package com.engineeringood.athena.compiler

import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialConstructGeometry
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialQualityMetrics
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialRoute
import com.engineeringood.athena.spatial.SpatialSheet

class SpatialQualityCompiler {
    fun measure(
        drawingArea: SpatialRect,
        occurrences: List<SpatialOccurrenceGeometry>,
        constructs: List<SpatialConstructGeometry>,
        lanes: List<SpatialLane>,
        routes: List<SpatialRoute>,
    ): SpatialQualityMetrics {
        val laneUse = laneUse(routes, lanes)
        val drawingAreaArea = drawingArea.width.toLong() * drawingArea.height.toLong()
        return SpatialQualityMetrics(
            occurrenceOverlapCount = overlapCount(occurrences),
            constructContainmentFailureCount = containmentFailureCount(occurrences, constructs),
            routeBodyIntersectionCount = bodyIntersectionCount(occurrences, routes),
            routeCrossingCount = spatialRouteCrossingCount(routes),
            twistCount = twistCount(routes),
            usedLaneCount = laneUse.first,
            peakRoutesPerLane = laneUse.second,
            density = occurrences.size.toDouble() / drawingAreaArea.toDouble(),
            occupancy = spatialOccurrenceUnionArea(occurrences).toDouble() / drawingAreaArea.toDouble(),
        )
    }

    private fun overlapCount(occurrences: List<SpatialOccurrenceGeometry>): Int {
        val boxes = boxesFor(occurrences)
        return boxes.indices.sumOf { index ->
            ((index + 1) until boxes.size).count { other -> boxes[index].overlaps(boxes[other]) }
        }
    }

    private fun containmentFailureCount(
        occurrences: List<SpatialOccurrenceGeometry>,
        constructs: List<SpatialConstructGeometry>,
    ): Int {
        val occurrencesById = occurrences.groupBy(SpatialOccurrenceGeometry::occurrenceId)
        return constructs.sumOf { construct ->
            construct.memberOccurrenceIds.count { occurrenceId ->
                occurrencesById[occurrenceId]
                    ?.singleOrNull()
                    ?.rectangle
                    ?.isInside(construct.envelope) == false
            }
        }
    }

    private fun bodyIntersectionCount(
        occurrences: List<SpatialOccurrenceGeometry>,
        routes: List<SpatialRoute>,
    ): Int = routes.sumOf { route ->
        val endpointOwners = setOf(route.sourceAnchorId.occurrenceId, route.targetAnchorId.occurrenceId)
        val bodies = occurrences.filterNot { occurrence -> occurrence.occurrenceId in endpointOwners }
        route.segments.count { segment ->
            bodies.any { occurrence -> segment.intersectsOpenInterior(occurrence.rectangle) }
        }
    }

    private fun twistCount(routes: List<SpatialRoute>): Int = routes.sumOf { route ->
        route.segments.count { segment ->
            segment.start.x != segment.end.x && segment.start.y != segment.end.y
        }
    }

    private fun laneUse(routes: List<SpatialRoute>, lanes: List<SpatialLane>): Pair<Int, Int> {
        val existingLaneIds = lanes.map(SpatialLane::laneId).toSet()
        val routeCounts = routes
            .filter { route -> route.laneId in existingLaneIds }
            .groupingBy(SpatialRoute::laneId)
            .eachCount()
        return routeCounts.size to (routeCounts.values.maxOrNull() ?: 0)
    }

    private fun boxesFor(occurrences: List<SpatialOccurrenceGeometry>): List<SpatialBox> =
        occurrences.map { occurrence ->
            val rectangle = occurrence.rectangle
            SpatialBox(
                left = rectangle.x,
                top = rectangle.y,
                right = rectangle.right,
                bottom = rectangle.bottom,
            )
        }
}

internal fun exactSpatialQualityDiagnostics(sheet: SpatialSheet): List<SpatialDiagnostic> {
    val published = sheet.quality.metrics
    if (!published.density.isFinite() || published.density < 0.0 ||
        !published.occupancy.isFinite() || published.occupancy < 0.0
    ) {
        return emptyList()
    }
    val expected = try {
        SpatialQualityCompiler().measure(
            drawingArea = sheet.drawingArea,
            occurrences = sheet.occurrences,
            constructs = sheet.constructs,
            lanes = sheet.lanes,
            routes = sheet.routes,
        )
    } catch (_: ArithmeticException) {
        return listOf(
            SpatialDiagnostic(
                subject = "Quality snapshot on Sheet ${sheet.sheetId}",
                problem = "metrics cannot be recomputed because final Spatial geometry exceeds the supported area range",
                correction = "Keep every Occurrence rectangle inside its Sheet Drawing Area before measuring quality.",
                sourceTrace = sheet.quality.sourceTrace,
            ),
        )
    }
    return if (published == expected) {
        emptyList()
    } else {
        listOf(
            SpatialDiagnostic(
                subject = "Quality snapshot on Sheet ${sheet.sheetId}",
                problem = "metrics do not equal exact values recomputed from final Spatial facts",
                correction = "Recompute all quality metrics from this Sheet's Drawing Area, Occurrences, " +
                    "Constructs, Lanes, and Routes.",
                sourceTrace = sheet.quality.sourceTrace,
            ),
        )
    }
}

private data class SpatialBox(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    fun overlaps(other: SpatialBox): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top

}
