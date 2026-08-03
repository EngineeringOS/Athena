package com.engineeringood.athena.compiler

import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialQualityMeasurement
import com.engineeringood.athena.spatial.SpatialRoute

class SpatialQualityCompiler {
    fun measure(
        occurrences: List<SpatialOccurrenceGeometry>,
        lanes: List<SpatialLane>,
        routes: List<SpatialRoute>,
    ): List<SpatialQualityMeasurement> =
        listOf(
            SpatialQualityMeasurement("overlap-count", overlapCount(occurrences).toDouble()),
            SpatialQualityMeasurement("body-intersection-count", bodyIntersectionCount(occurrences, routes).toDouble()),
            SpatialQualityMeasurement("crossing-count", crossingCount(routes).toDouble()),
            SpatialQualityMeasurement("twist-count", twistCount(routes).toDouble()),
            SpatialQualityMeasurement("lane-use-count", routes.map { route -> route.laneId }.distinct().size.toDouble()),
            SpatialQualityMeasurement("label-pressure", routes.size.toDouble()),
            SpatialQualityMeasurement("route-count", routes.size.toDouble()),
            SpatialQualityMeasurement("lane-count", lanes.size.toDouble()),
        )

    private fun overlapCount(occurrences: List<SpatialOccurrenceGeometry>): Int {
        val boxes = boxesFor(occurrences)
        return boxes.indices.sumOf { index ->
            ((index + 1) until boxes.size).count { other -> boxes[index].overlaps(boxes[other]) }
        }
    }

    private fun bodyIntersectionCount(
        occurrences: List<SpatialOccurrenceGeometry>,
        routes: List<SpatialRoute>,
    ): Int {
        val boxes = boxesFor(occurrences)
        return routes.sumOf { route ->
            route.points.count { point -> boxes.any { box -> box.contains(point) } }
        }
    }

    private fun crossingCount(routes: List<SpatialRoute>): Int {
        val segments = routes.flatMap { route -> route.points.zipWithNext() }
        return segments.indices.sumOf { index ->
            ((index + 1) until segments.size).count { other -> segments[index].crosses(segments[other]) }
        }
    }

    private fun twistCount(routes: List<SpatialRoute>): Int =
        routes.sumOf { route ->
            route.points.zipWithNext().count { (start, end) -> start.x != end.x && start.y != end.y }
        }

    private fun boxesFor(occurrences: List<SpatialOccurrenceGeometry>): List<SpatialBox> =
        occurrences.map { occurrence ->
            val rectangle = occurrence.rectangle
            SpatialBox(
                left = rectangle.x.toDouble(),
                top = rectangle.y.toDouble(),
                right = rectangle.right.toDouble(),
                bottom = rectangle.bottom.toDouble(),
            )
        }
}

private data class SpatialBox(
    val left: Double,
    val top: Double,
    val right: Double,
    val bottom: Double,
) {
    fun overlaps(other: SpatialBox): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top

    fun contains(point: SpatialPoint): Boolean =
        point.x in left..right && point.y in top..bottom
}

private fun Pair<SpatialPoint, SpatialPoint>.crosses(other: Pair<SpatialPoint, SpatialPoint>): Boolean {
    val (a, b) = this
    val (c, d) = other
    val horizontal = a.y == b.y
    val vertical = a.x == b.x
    val otherHorizontal = c.y == d.y
    val otherVertical = c.x == d.x
    if (horizontal && otherVertical) {
        return c.x.between(a.x, b.x) && a.y.between(c.y, d.y)
    }
    if (vertical && otherHorizontal) {
        return a.x.between(c.x, d.x) && c.y.between(a.y, b.y)
    }
    return false
}

private fun Double.between(a: Double, b: Double): Boolean =
    this >= minOf(a, b) && this <= maxOf(a, b)
