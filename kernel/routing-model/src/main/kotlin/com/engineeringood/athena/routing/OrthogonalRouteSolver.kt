package com.engineeringood.athena.routing

import java.util.PriorityQueue

data class OrthogonalRoutePoint(
    val x: Int,
    val y: Int,
)

enum class OrthogonalRouteSide {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
}

data class OrthogonalRouteRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) {
    init {
        require(width > 0 && height > 0) { "Orthogonal route rectangle size must be positive." }
        require(x.toLong() + width.toLong() <= Int.MAX_VALUE) { "Orthogonal route rectangle right edge exceeds Int bounds." }
        require(y.toLong() + height.toLong() <= Int.MAX_VALUE) { "Orthogonal route rectangle bottom edge exceeds Int bounds." }
    }

    val right: Int = x + width
    val bottom: Int = y + height

    fun contains(point: OrthogonalRoutePoint): Boolean =
        point.x in x..right && point.y in y..bottom

    fun containsInInterior(point: OrthogonalRoutePoint): Boolean =
        point.x > x && point.x < right && point.y > y && point.y < bottom
}

data class OrthogonalRouteObstacle(
    val obstacleId: String,
    val bounds: OrthogonalRouteRect,
) {
    init {
        require(obstacleId.isNotBlank()) { "Orthogonal route obstacle identity must not be blank." }
    }
}

data class OrthogonalRouteRequest(
    val requestId: String,
    val source: OrthogonalRoutePoint,
    val target: OrthogonalRoutePoint,
    val sourceSide: OrthogonalRouteSide,
    val targetSide: OrthogonalRouteSide,
    val drawingArea: OrthogonalRouteRect,
    val obstacles: List<OrthogonalRouteObstacle>,
    val stubLength: Int = 1,
    val obstacleClearance: Int = 0,
) {
    init {
        require(requestId.isNotBlank()) { "Orthogonal route request identity must not be blank." }
        require(stubLength >= 0) { "Orthogonal route stub length must not be negative." }
        require(obstacleClearance >= 0) { "Orthogonal route obstacle clearance must not be negative." }
    }
}

sealed interface OrthogonalRouteSolveResult {
    data class Success(val points: List<OrthogonalRoutePoint>) : OrthogonalRouteSolveResult

    data object NoPath : OrthogonalRouteSolveResult
}

class OrthogonalRouteSolver {
    fun solve(request: OrthogonalRouteRequest): OrthogonalRouteSolveResult {
        if (request.source == request.target ||
            !request.drawingArea.contains(request.source) ||
            !request.drawingArea.contains(request.target)
        ) {
            return OrthogonalRouteSolveResult.NoPath
        }
        val sourceStub = request.source.outward(request.sourceSide, request.stubLength) ?: return OrthogonalRouteSolveResult.NoPath
        val targetStub = request.target.outward(request.targetSide, request.stubLength) ?: return OrthogonalRouteSolveResult.NoPath
        val obstacles = request.obstacles
            .distinctBy { obstacle -> obstacle.obstacleId to obstacle.bounds }
            .sortedWith(compareBy({ it.obstacleId }, { it.bounds.x }, { it.bounds.y }, { it.bounds.width }, { it.bounds.height }))
            .map { obstacle ->
                val expanded = obstacle.bounds.expandedOrNull(request.obstacleClearance)
                    ?: return OrthogonalRouteSolveResult.NoPath
                obstacle.copy(bounds = expanded)
            }
        if (listOf(sourceStub, targetStub).any { point ->
                !request.drawingArea.contains(point) || obstacles.any { obstacle -> obstacle.bounds.containsInInterior(point) }
            }
        ) {
            return OrthogonalRouteSolveResult.NoPath
        }

        val xCoordinates = buildSet {
            add(request.drawingArea.x)
            add(request.drawingArea.right)
            add(request.source.x)
            add(request.target.x)
            add(sourceStub.x)
            add(targetStub.x)
            obstacles.forEach { obstacle ->
                add(obstacle.bounds.x)
                add(obstacle.bounds.right)
            }
        }.sorted()
        val yCoordinates = buildSet {
            add(request.drawingArea.y)
            add(request.drawingArea.bottom)
            add(request.source.y)
            add(request.target.y)
            add(sourceStub.y)
            add(targetStub.y)
            obstacles.forEach { obstacle ->
                add(obstacle.bounds.y)
                add(obstacle.bounds.bottom)
            }
        }.sorted()
        val nodes = xCoordinates.flatMap { x -> yCoordinates.map { y -> OrthogonalRoutePoint(x, y) } }
            .filter { point ->
                request.drawingArea.contains(point) &&
                    obstacles.none { obstacle -> obstacle.bounds.containsInInterior(point) }
            }
            .toSet()
        if (sourceStub !in nodes || targetStub !in nodes) return OrthogonalRouteSolveResult.NoPath

        val adjacent = nodes.associateWith { mutableSetOf<OrthogonalRoutePoint>() }
        nodes.groupBy(OrthogonalRoutePoint::y).toSortedMap().values.forEach { row ->
            row.sortedBy(OrthogonalRoutePoint::x).zipWithNext().forEach { (left, right) ->
                if (clear(left, right, obstacles)) {
                    adjacent.getValue(left) += right
                    adjacent.getValue(right) += left
                }
            }
        }
        nodes.groupBy(OrthogonalRoutePoint::x).toSortedMap().values.forEach { column ->
            column.sortedBy(OrthogonalRoutePoint::y).zipWithNext().forEach { (top, bottom) ->
                if (clear(top, bottom, obstacles)) {
                    adjacent.getValue(top) += bottom
                    adjacent.getValue(bottom) += top
                }
            }
        }
        val neighbors = nodes.associateWith { point ->
            adjacent.getValue(point).asSequence()
                .filter { candidate ->
                    (request.stubLength == 0 || point != sourceStub ||
                        remainsOutside(sourceStub, candidate, request.sourceSide)) &&
                        (request.stubLength == 0 || candidate != targetStub ||
                            remainsOutside(targetStub, point, request.targetSide))
                }
                .sortedWith(pointComparator)
                .toList()
        }
        val middle = shortestPath(sourceStub, targetStub, neighbors) ?: return OrthogonalRouteSolveResult.NoPath
        val compactMiddle = middle.compactCollinear()
        val points = (listOf(request.source) + compactMiddle + request.target).distinctConsecutive()
        if (points.zipWithNext().any { (start, end) ->
                start == end || (start.x != end.x && start.y != end.y) ||
                    obstacles.any { obstacle -> segmentEntersInterior(start, end, obstacle.bounds) }
            }
        ) {
            return OrthogonalRouteSolveResult.NoPath
        }
        return OrthogonalRouteSolveResult.Success(points)
    }

    private fun shortestPath(
        source: OrthogonalRoutePoint,
        target: OrthogonalRoutePoint,
        neighbors: Map<OrthogonalRoutePoint, List<OrthogonalRoutePoint>>,
    ): List<OrthogonalRoutePoint>? {
        val queue = PriorityQueue(pathComparator)
        queue += RoutePath(listOf(source), 0L)
        val best = mutableMapOf<OrthogonalRoutePoint, RoutePath>()
        while (queue.isNotEmpty()) {
            val current = queue.remove()
            val point = current.points.last()
            val known = best[point]
            if (known != null && pathComparator.compare(known, current) <= 0) continue
            best[point] = current
            if (point == target) return current.points
            neighbors[point].orEmpty().forEach { neighbor ->
                queue += RoutePath(
                    points = current.points + neighbor,
                    length = current.length + manhattan(point, neighbor),
                )
            }
        }
        return null
    }

    private fun clear(
        start: OrthogonalRoutePoint,
        end: OrthogonalRoutePoint,
        obstacles: List<OrthogonalRouteObstacle>,
    ): Boolean = obstacles.none { obstacle -> segmentEntersInterior(start, end, obstacle.bounds) }

    private data class RoutePath(
        val points: List<OrthogonalRoutePoint>,
        val length: Long,
    )

    private companion object {
        val pointComparator: Comparator<OrthogonalRoutePoint> = compareBy({ it.x }, { it.y })

        val pathComparator: Comparator<RoutePath> = Comparator { left, right ->
            val lengthComparison = left.length.compareTo(right.length)
            if (lengthComparison != 0) {
                lengthComparison
            } else {
                comparePointLists(left.points, right.points)
            }
        }

        fun comparePointLists(left: List<OrthogonalRoutePoint>, right: List<OrthogonalRoutePoint>): Int {
            val size = minOf(left.size, right.size)
            for (index in 0 until size) {
                val comparison = pointComparator.compare(left[index], right[index])
                if (comparison != 0) return comparison
            }
            return left.size.compareTo(right.size)
        }

        fun manhattan(first: OrthogonalRoutePoint, second: OrthogonalRoutePoint): Long =
            kotlin.math.abs(first.x.toLong() - second.x.toLong()) +
                kotlin.math.abs(first.y.toLong() - second.y.toLong())

        fun segmentEntersInterior(
            start: OrthogonalRoutePoint,
            end: OrthogonalRoutePoint,
            rectangle: OrthogonalRouteRect,
        ): Boolean = when {
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

        fun remainsOutside(
            stub: OrthogonalRoutePoint,
            candidate: OrthogonalRoutePoint,
            side: OrthogonalRouteSide,
        ): Boolean = when (side) {
            OrthogonalRouteSide.LEFT -> candidate.y != stub.y || candidate.x <= stub.x
            OrthogonalRouteSide.RIGHT -> candidate.y != stub.y || candidate.x >= stub.x
            OrthogonalRouteSide.TOP -> candidate.x != stub.x || candidate.y <= stub.y
            OrthogonalRouteSide.BOTTOM -> candidate.x != stub.x || candidate.y >= stub.y
        }
    }
}

private fun OrthogonalRoutePoint.outward(side: OrthogonalRouteSide, distance: Int): OrthogonalRoutePoint? {
    val (nextX, nextY) = when (side) {
        OrthogonalRouteSide.LEFT -> x.toLong() - distance.toLong() to y.toLong()
        OrthogonalRouteSide.RIGHT -> x.toLong() + distance.toLong() to y.toLong()
        OrthogonalRouteSide.TOP -> x.toLong() to y.toLong() - distance.toLong()
        OrthogonalRouteSide.BOTTOM -> x.toLong() to y.toLong() + distance.toLong()
    }
    if (nextX !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() ||
        nextY !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()
    ) {
        return null
    }
    return OrthogonalRoutePoint(nextX.toInt(), nextY.toInt())
}

private fun OrthogonalRouteRect.expandedOrNull(clearance: Int): OrthogonalRouteRect? {
    if (clearance == 0) return this
    val expandedLeft = x.toLong() - clearance.toLong()
    val expandedTop = y.toLong() - clearance.toLong()
    val expandedRight = right.toLong() + clearance.toLong()
    val expandedBottom = bottom.toLong() + clearance.toLong()
    if (!(expandedLeft >= Int.MIN_VALUE && expandedTop >= Int.MIN_VALUE &&
        expandedRight <= Int.MAX_VALUE && expandedBottom <= Int.MAX_VALUE &&
        expandedRight - expandedLeft <= Int.MAX_VALUE && expandedBottom - expandedTop <= Int.MAX_VALUE
        )) return null
    return OrthogonalRouteRect(
        x = expandedLeft.toInt(),
        y = expandedTop.toInt(),
        width = (expandedRight - expandedLeft).toInt(),
        height = (expandedBottom - expandedTop).toInt(),
    )
}

private fun List<OrthogonalRoutePoint>.compactCollinear(): List<OrthogonalRoutePoint> =
    fold(emptyList()) { result, point ->
        when {
            result.lastOrNull() == point -> result
            result.size < 2 -> result + point
            result[result.lastIndex - 1].x == result.last().x && result.last().x == point.x ->
                result.dropLast(1) + point
            result[result.lastIndex - 1].y == result.last().y && result.last().y == point.y ->
                result.dropLast(1) + point
            else -> result + point
        }
    }

private fun List<OrthogonalRoutePoint>.distinctConsecutive(): List<OrthogonalRoutePoint> =
    fold(emptyList()) { result, point -> if (result.lastOrNull() == point) result else result + point }
