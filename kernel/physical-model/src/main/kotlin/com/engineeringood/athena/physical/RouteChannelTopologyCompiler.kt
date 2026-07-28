package com.engineeringood.athena.physical

import kotlin.math.abs

data class ExactMillimeters(
    val numerator: Int,
    val denominator: Int,
) : Comparable<ExactMillimeters> {
    init {
        require(denominator > 0) { "denominator must be positive" }
    }

    override fun compareTo(other: ExactMillimeters): Int =
        (numerator.toLong() * other.denominator).compareTo(other.numerator.toLong() * denominator)
}

data class RouteChannelBoundaryPoint(
    val x: ExactMillimeters,
    val y: ExactMillimeters,
)

data class RouteChannelLaneFact(
    val channelId: PhysicalObjectId,
    val laneIndex: Int,
    val center: ExactMillimeters,
)

data class RouteChannelLaneAssignment(
    val connectionAlias: String,
    val channelId: PhysicalObjectId,
    val laneIndex: Int,
)

data class RouteChannelAdjacencyFact(
    val fromChannelId: PhysicalObjectId,
    val toChannelId: PhysicalObjectId,
    val passableMidpoint: RouteChannelBoundaryPoint,
)

data class RouteChannelTopologyChannel(
    val channelId: PhysicalObjectId,
    val laneCenters: List<ExactMillimeters>,
)

data class RouteChannelTopologyProof(
    val channelCount: Int,
    val routeCount: Int,
    val allocatedLaneCount: Int,
    val adjacencyCount: Int,
)

data class RouteChannelTopology(
    val channels: List<RouteChannelTopologyChannel>,
    val lanes: List<RouteChannelLaneFact>,
    val laneAssignments: List<RouteChannelLaneAssignment>,
    val adjacencies: List<RouteChannelAdjacencyFact>,
    val proof: RouteChannelTopologyProof,
)

data class RouteChannelTopologyDiagnostic(
    val code: String,
    val subject: String,
    val measured: String?,
    val expected: String,
    val span: PhysicalSourceSpan?,
)

sealed interface RouteChannelTopologyCompilation {
    data class Success(val topology: RouteChannelTopology) : RouteChannelTopologyCompilation

    data class Failure(val diagnostics: List<RouteChannelTopologyDiagnostic>) : RouteChannelTopologyCompilation
}

object RouteChannelTopologyCompiler {
    fun compile(
        channels: List<PhysicalRouteChannelV0>,
        routes: List<PhysicalRouteIntentV0>,
    ): RouteChannelTopologyCompilation {
        val diagnostics = mutableListOf<RouteChannelTopologyDiagnostic>()
        val channelsById = channels.associateBy { channel -> channel.id }
        val topologyChannels = channels.sortedBy { channel -> channel.id.value }.mapNotNull { channel ->
            val laneCenters = laneCenters(channel, diagnostics) ?: return@mapNotNull null
            RouteChannelTopologyChannel(channel.id, laneCenters)
        }

        val laneAssignments = mutableListOf<RouteChannelLaneAssignment>()
        channels.sortedBy { channel -> channel.id.value }.forEach { channel ->
            val aliases = routes
                .filter { route -> channel.id in route.channelIds }
                .map { route -> route.connectionAlias }
                .distinct()
                .sorted()
            if (aliases.size > channel.lanes) {
                diagnostics += diagnostic(
                    code = "physical.route.channel.capacity.overflow",
                    subject = channel.id.value,
                    measured = aliases.size.toString(),
                    expected = "at most ${channel.lanes} routed aliases",
                    span = channel.provenance.span,
                )
            } else {
                aliases.forEachIndexed { index, alias ->
                    laneAssignments += RouteChannelLaneAssignment(alias, channel.id, index)
                }
            }
        }

        val adjacencies = mutableListOf<RouteChannelAdjacencyFact>()
        routes.sortedBy { route -> route.connectionAlias }.forEach { route ->
            route.channelIds.zipWithNext().forEach { (fromId, toId) ->
                val from = channelsById[fromId] ?: return@forEach
                val to = channelsById[toId] ?: return@forEach
                val adjacency = adjacency(from, to)
                if (adjacency == null) {
                    diagnostics += diagnostic(
                        code = if (from.ductId != to.ductId) {
                            "physical.route.channel.adjacency.cross_duct"
                        } else {
                            "physical.route.channel.adjacency.not_passable"
                        },
                        subject = "${from.id.value}->${to.id.value}",
                        measured = "${from.bounds()} to ${to.bounds()}",
                        expected = "same-duct positive shared boundary after margin trimming",
                        span = route.provenance.span,
                    )
                } else {
                    adjacencies += adjacency
                }
            }
        }

        if (diagnostics.isNotEmpty()) {
            return RouteChannelTopologyCompilation.Failure(
                diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.measured.orEmpty() })),
            )
        }

        val lanes = topologyChannels.flatMap { channel ->
            channel.laneCenters.mapIndexed { index, center ->
                RouteChannelLaneFact(channel.channelId, index, center)
            }
        }
        return RouteChannelTopologyCompilation.Success(
            RouteChannelTopology(
                channels = topologyChannels,
                lanes = lanes,
                laneAssignments = laneAssignments.sortedWith(compareBy({ it.channelId.value }, { it.connectionAlias })),
                adjacencies = adjacencies.distinctBy { adjacency ->
                    "${adjacency.fromChannelId.value}->${adjacency.toChannelId.value}"
                },
                proof = RouteChannelTopologyProof(
                    channelCount = channels.size,
                    routeCount = routes.size,
                    allocatedLaneCount = laneAssignments.size,
                    adjacencyCount = adjacencies.size,
                ),
            ),
        )
    }
}

private fun laneCenters(
    channel: PhysicalRouteChannelV0,
    diagnostics: MutableList<RouteChannelTopologyDiagnostic>,
): List<ExactMillimeters>? {
    if (channel.lanes <= 0) {
        diagnostics += diagnostic(
            code = "physical.route.channel.lanes.invalid",
            subject = channel.id.value,
            measured = channel.lanes.toString(),
            expected = "lane count > 0",
            span = channel.provenance.span,
        )
        return null
    }
    val crossSpan = when (channel.orientation) {
        PhysicalInfrastructureOrientation.Horizontal -> channel.size.height
        PhysicalInfrastructureOrientation.Vertical -> channel.size.width
    }
    val usableSpan = crossSpan - (2 * channel.margin.value)
    if (usableSpan <= 0) {
        diagnostics += diagnostic(
            code = "physical.route.channel.usable_span.invalid",
            subject = channel.id.value,
            measured = usableSpan.toString(),
            expected = "S - 2M > 0",
            span = channel.provenance.span,
        )
        return null
    }
    return (0 until channel.lanes).map { index ->
        exact((2 * channel.margin.value * channel.lanes) + ((2 * index + 1) * usableSpan), 2 * channel.lanes)
    }
}

private fun adjacency(
    from: PhysicalRouteChannelV0,
    to: PhysicalRouteChannelV0,
): RouteChannelAdjacencyFact? {
    if (from.ductId != to.ductId) return null
    val a = from.bounds()
    val b = to.bounds()
    val verticalBoundary = when {
        a.right == b.x -> a.right
        b.right == a.x -> a.x
        else -> null
    }
    if (verticalBoundary != null && !a.overlapsPositiveArea(b)) {
        val overlapStart = maxOf(a.y, b.y)
        val overlapEnd = minOf(a.bottom, b.bottom)
        val trim = maxOf(from.margin.value, to.margin.value)
        if (overlapEnd - overlapStart - (2 * trim) > 0) {
            return RouteChannelAdjacencyFact(
                fromChannelId = from.id,
                toChannelId = to.id,
                passableMidpoint = RouteChannelBoundaryPoint(
                    x = exact(verticalBoundary, 1),
                    y = exact(overlapStart + overlapEnd, 2),
                ),
            )
        }
    }
    val horizontalBoundary = when {
        a.bottom == b.y -> a.bottom
        b.bottom == a.y -> a.y
        else -> null
    }
    if (horizontalBoundary != null && !a.overlapsPositiveArea(b)) {
        val overlapStart = maxOf(a.x, b.x)
        val overlapEnd = minOf(a.right, b.right)
        val trim = maxOf(from.margin.value, to.margin.value)
        if (overlapEnd - overlapStart - (2 * trim) > 0) {
            return RouteChannelAdjacencyFact(
                fromChannelId = from.id,
                toChannelId = to.id,
                passableMidpoint = RouteChannelBoundaryPoint(
                    x = exact(overlapStart + overlapEnd, 2),
                    y = exact(horizontalBoundary, 1),
                ),
            )
        }
    }
    return null
}

private data class ChannelRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) {
    val right: Int = x + width
    val bottom: Int = y + height

    fun overlapsPositiveArea(other: ChannelRect): Boolean =
        x < other.right && right > other.x && y < other.bottom && bottom > other.y
}

private fun PhysicalRouteChannelV0.bounds(): ChannelRect = ChannelRect(at.x, at.y, size.width, size.height)

private fun exact(numerator: Int, denominator: Int): ExactMillimeters {
    val divisor = gcd(abs(numerator), abs(denominator))
    return ExactMillimeters(numerator / divisor, denominator / divisor)
}

private tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

private fun diagnostic(
    code: String,
    subject: String,
    measured: String?,
    expected: String,
    span: PhysicalSourceSpan?,
): RouteChannelTopologyDiagnostic = RouteChannelTopologyDiagnostic(code, subject, measured, expected, span)
