package com.engineeringood.athena.compiler

import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialRoute
import com.engineeringood.athena.spatial.SpatialRouteSegment
import java.math.BigInteger

internal fun SpatialRouteSegment.intersectsOpenInterior(rectangle: SpatialRect): Boolean {
    if (start == end) return false
    val horizontal = openParameterInterval(start.x, end.x, rectangle.x, rectangle.right) ?: return false
    val vertical = openParameterInterval(start.y, end.y, rectangle.y, rectangle.bottom) ?: return false
    val lower = maxOf(Rational.ZERO, horizontal.lower, vertical.lower)
    val upper = minOf(Rational.ONE, horizontal.upper, vertical.upper)
    return lower < upper
}

internal fun spatialRouteCrossingCount(routes: List<SpatialRoute>): Int {
    val crossings = mutableSetOf<SpatialRouteCrossing>()
    for (firstIndex in routes.indices) {
        for (secondIndex in (firstIndex + 1) until routes.size) {
            val first = routes[firstIndex]
            val second = routes[secondIndex]
            for (firstSegment in first.segments) {
                for (secondSegment in second.segments) {
                    val point = firstSegment.perpendicularIntersection(secondSegment) ?: continue
                    if (point in first.points && point in second.points) continue
                    if (first.sharesEndpointAnchorAt(second, point)) continue
                    crossings += SpatialRouteCrossing.of(first, second, point)
                }
            }
        }
    }
    return crossings.size
}

internal fun spatialOccurrenceUnionArea(occurrences: List<SpatialOccurrenceGeometry>): Long {
    val rectangles = occurrences.map(SpatialOccurrenceGeometry::rectangle)
    val xCoordinates = rectangles
        .flatMap { rectangle -> listOf(rectangle.x, rectangle.right) }
        .distinct()
        .sorted()
    return xCoordinates.zipWithNext().fold(0L) { area, (left, right) ->
        val width = right.toLong() - left.toLong()
        val coveredHeight = coveredHeight(rectangles, left, right)
        Math.addExact(area, Math.multiplyExact(width, coveredHeight))
    }
}

private fun coveredHeight(rectangles: List<SpatialRect>, left: Int, right: Int): Long {
    val intervals = rectangles
        .filter { rectangle -> rectangle.x < right && rectangle.right > left }
        .map { rectangle -> rectangle.y to rectangle.bottom }
        .sortedWith(compareBy<Pair<Int, Int>>({ interval -> interval.first }, { interval -> interval.second }))
    if (intervals.isEmpty()) return 0L
    var currentStart = intervals.first().first
    var currentEnd = intervals.first().second
    var covered = 0L
    intervals.drop(1).forEach { (start, end) ->
        if (start > currentEnd) {
            covered = Math.addExact(covered, currentEnd.toLong() - currentStart.toLong())
            currentStart = start
            currentEnd = end
        } else {
            currentEnd = maxOf(currentEnd, end)
        }
    }
    return Math.addExact(covered, currentEnd.toLong() - currentStart.toLong())
}

private data class SpatialRouteCrossing(
    val firstRouteId: String,
    val secondRouteId: String,
    val point: SpatialPoint,
) {
    companion object {
        fun of(first: SpatialRoute, second: SpatialRoute, point: SpatialPoint): SpatialRouteCrossing {
            val routeIds = listOf(first.routeId.value, second.routeId.value).sorted()
            return SpatialRouteCrossing(routeIds[0], routeIds[1], point)
        }
    }
}

private fun SpatialRoute.sharesEndpointAnchorAt(other: SpatialRoute, point: SpatialPoint): Boolean {
    val endpoints = listOf(sourceAnchorId to points.first(), targetAnchorId to points.last())
    val otherEndpoints = listOf(other.sourceAnchorId to other.points.first(), other.targetAnchorId to other.points.last())
    return endpoints.any { endpoint ->
        endpoint.second == point && otherEndpoints.any { otherEndpoint -> otherEndpoint == endpoint }
    }
}

private fun SpatialRouteSegment.perpendicularIntersection(other: SpatialRouteSegment): SpatialPoint? {
    if (start == end || other.start == other.end) return null
    val horizontal = start.y == end.y
    val vertical = start.x == end.x
    val otherHorizontal = other.start.y == other.end.y
    val otherVertical = other.start.x == other.end.x
    return when {
        horizontal && otherVertical && other.start.x.between(start.x, end.x) && start.y.between(other.start.y, other.end.y) ->
            SpatialPoint(other.start.x, start.y)
        vertical && otherHorizontal && start.x.between(other.start.x, other.end.x) && other.start.y.between(start.y, end.y) ->
            SpatialPoint(start.x, other.start.y)
        else -> null
    }
}

private fun Int.between(first: Int, second: Int): Boolean = this in minOf(first, second)..maxOf(first, second)

private data class OpenParameterInterval(
    val lower: Rational,
    val upper: Rational,
)

private fun openParameterInterval(
    start: Int,
    end: Int,
    minimum: Int,
    maximum: Int,
): OpenParameterInterval? {
    val delta = end.toLong() - start.toLong()
    if (delta == 0L) {
        return if (start > minimum && start < maximum) {
            OpenParameterInterval(Rational.NEGATIVE_INFINITY, Rational.POSITIVE_INFINITY)
        } else {
            null
        }
    }
    return if (delta > 0L) {
        OpenParameterInterval(
            Rational(minimum.toLong() - start.toLong(), delta),
            Rational(maximum.toLong() - start.toLong(), delta),
        )
    } else {
        val positiveDelta = -delta
        OpenParameterInterval(
            Rational(start.toLong() - maximum.toLong(), positiveDelta),
            Rational(start.toLong() - minimum.toLong(), positiveDelta),
        )
    }
}

private data class Rational(
    val numerator: Long,
    val denominator: Long,
) : Comparable<Rational> {
    init {
        require(denominator > 0L) { "Rational denominator must be positive." }
    }

    override fun compareTo(other: Rational): Int =
        BigInteger.valueOf(numerator).multiply(BigInteger.valueOf(other.denominator)).compareTo(
            BigInteger.valueOf(other.numerator).multiply(BigInteger.valueOf(denominator)),
        )

    companion object {
        val NEGATIVE_INFINITY = Rational(Long.MIN_VALUE, 1L)
        val ZERO = Rational(0L, 1L)
        val ONE = Rational(1L, 1L)
        val POSITIVE_INFINITY = Rational(Long.MAX_VALUE, 1L)
    }
}
