package com.engineeringood.athena.compiler

import com.engineeringood.athena.drawing.composition.CabinetConnectionEndpointBinding
import com.engineeringood.athena.drawing.composition.CabinetOccurrenceVisualJoin
import com.engineeringood.athena.drawing.composition.CabinetRouteEndpointRef
import com.engineeringood.athena.drawing.composition.CabinetRoutingCompilation
import com.engineeringood.athena.drawing.composition.CabinetRoutingCompiler
import com.engineeringood.athena.drawing.composition.CabinetRoutingRequest
import com.engineeringood.athena.drawing.composition.CabinetTargetFrame
import com.engineeringood.athena.drawing.composition.CabinetRectD
import com.engineeringood.athena.drawing.composition.CabinetTransformedAnchor
import com.engineeringood.athena.physical.PhysicalDuct
import com.engineeringood.athena.physical.PhysicalInstallationIR
import com.engineeringood.athena.physical.PhysicalObjectId
import com.engineeringood.athena.physical.PhysicalRouteIntent
import com.engineeringood.athena.physical.PhysicalRouteChannel
import com.engineeringood.athena.physical.PhysicalSourceProvenance
import com.engineeringood.athena.physical.PhysicalSourceSpan
import com.engineeringood.athena.physical.PhysicalSourceUnitId
import com.engineeringood.athena.physical.RouteChannelTopologyCompiler
import com.engineeringood.athena.routing.RouteIntent
import com.engineeringood.athena.routing.RouteIntentCompilation
import com.engineeringood.athena.routing.RouteIntentConstraintKind
import com.engineeringood.athena.routing.RouteIntentConstraintStrength
import com.engineeringood.athena.routing.RouteIntentConstraintTarget
import kotlin.math.abs

data class CabinetRouteRealizationRequest(
    val physicalIr: PhysicalInstallationIR,
    val routeIntents: RouteIntentCompilation,
    val joins: List<CabinetOccurrenceVisualJoin>,
    val endpoints: List<CabinetConnectionEndpointBinding>,
    val enclosureToDrawing: CabinetTargetFrame,
)

data class CabinetRouteRealizationEvidence(
    val routeCount: Int,
    val realizedRouteCount: Int,
    val selectedChannelCount: Int,
)

data class CabinetRouteRealizationDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
    val span: PhysicalSourceSpan?,
)

sealed interface CabinetRouteRealizationCompilation {
    data class Success(
        val ir: PhysicalInstallationIR,
        val evidence: CabinetRouteRealizationEvidence,
    ) : CabinetRouteRealizationCompilation

    data class Failure(val diagnostics: List<CabinetRouteRealizationDiagnostic>) : CabinetRouteRealizationCompilation
}

object CabinetRouteRealizationCompiler {
    fun compile(request: CabinetRouteRealizationRequest): CabinetRouteRealizationCompilation {
        val endpointBindings = request.endpoints.associateBy { binding -> binding.connectionAlias }
        val joinsByKey = request.joins.associateBy { join -> join.key }
        val channelsById = request.physicalIr.space.channels.associateBy { channel -> channel.id }
        val adjacencyGraph = buildAdjacencyGraph(request.physicalIr.space.channels)
        val realizedRoutes = mutableListOf<PhysicalRouteIntent>()
        val diagnostics = mutableListOf<CabinetRouteRealizationDiagnostic>()
        var selectedChannelCount = 0

        request.routeIntents.routeIntents.sortedBy { intent -> intent.intentId.value }.forEach routeLoop@ { routeIntent ->
            val connectionAlias = routeIntent.connectionId.value.substringAfterLast(':')
            val invalidRequiredTarget = routeIntent.constraints.firstOrNull { constraint ->
                constraint.strength == RouteIntentConstraintStrength.REQUIRED &&
                    constraint.kind in setOf(RouteIntentConstraintKind.THROUGH, RouteIntentConstraintKind.AVOID) &&
                    constraint.channelIdOrNull() !in channelsById
            }
            if (invalidRequiredTarget != null) {
                diagnostics += diagnostic(
                    code = "cabinet.route.realization.constraint.unresolved",
                    subject = connectionAlias,
                    message = "Required route constraint '${invalidRequiredTarget.constraintId.value}' does not resolve to a physical channel.",
                    span = routeIntent.provenance.toPhysicalSpan(),
                )
                return@routeLoop
            }
            val route = PhysicalRouteIntent(
                connectionAlias = connectionAlias,
                channelIds = routeIntent.throughChannelIds(RouteIntentConstraintStrength.REQUIRED),
                provenance = routeIntent.provenance.toPhysicalProvenance(connectionAlias),
            )
            val avoidedChannels = routeIntent.avoidedChannelIds()
            val binding = endpointBindings[connectionAlias]
            if (binding == null) {
                diagnostics += diagnostic(
                    code = "cabinet.route.realization.endpoint.unbound",
                    subject = connectionAlias,
                    message = "Route alias has no endpoint binding.",
                    span = route.provenance.span,
                )
                return@routeLoop
            }

            val fromJoin = joinsByKey[binding.from.key]
            val toJoin = joinsByKey[binding.to.key]
            val fromAnchor = fromJoin?.anchors?.firstOrNull { anchor -> anchor.id == binding.from.anchorId }
            val toAnchor = toJoin?.anchors?.firstOrNull { anchor -> anchor.id == binding.to.anchorId }
            if (fromJoin == null || fromAnchor == null || toJoin == null || toAnchor == null) {
                diagnostics += diagnostic(
                    code = "cabinet.route.realization.endpoint.unbound",
                    subject = route.connectionAlias,
                    message = "Route endpoints must resolve to transformed anchors.",
                    span = route.provenance.span,
                )
                return@routeLoop
            }

            val requiredSequences = if (route.channelIds.isNotEmpty()) listOf(route.channelIds) else emptyList()
            val defaultSequences = if (requiredSequences.isEmpty()) {
                realizeDefaultSequences(
                    source = fromAnchor.point,
                    target = toAnchor.point,
                    channels = request.physicalIr.space.channels,
                    adjacencyGraph = adjacencyGraph,
                )
            } else {
                emptyList()
            }
            val candidateSequences = (requiredSequences + defaultSequences)
                .distinct()
                .filter { sequence -> sequence.none { channelId -> channelId in avoidedChannels } }

            val candidateFailures = mutableListOf<String>()
            val realizedRoute = candidateSequences.firstNotNullOfOrNull { candidate ->
                val trialRoute = route.copy(channelIds = candidate)
                val trialIr = request.physicalIr.copy(
                    routes = listOf(trialRoute),
                )
                val topology = when (val topologyCompilation = RouteChannelTopologyCompiler.compile(trialIr.space.channels, trialIr.routes)) {
                    is com.engineeringood.athena.physical.RouteChannelTopologyCompilation.Success -> topologyCompilation.topology
                    is com.engineeringood.athena.physical.RouteChannelTopologyCompilation.Failure -> {
                        candidateFailures += topologyCompilation.diagnostics.firstOrNull()?.let { diagnostic ->
                            "${diagnostic.code}:${diagnostic.subject}"
                        } ?: "route.topology.failure"
                        return@firstNotNullOfOrNull null
                    }
                }
                when (
                    val routing = CabinetRoutingCompiler.compile(
                        CabinetRoutingRequest(
                            ir = trialIr,
                            topology = topology,
                            joins = request.joins,
                            endpoints = request.endpoints,
                            enclosureToDrawing = request.enclosureToDrawing,
                        ),
                    )
                ) {
                    is CabinetRoutingCompilation.Success -> trialRoute
	                    is CabinetRoutingCompilation.Failure -> {
	                        candidateFailures += routing.diagnostics.take(3).joinToString("|") { diagnostic ->
	                            listOfNotNull(
	                                "${diagnostic.code}:${diagnostic.subject}",
	                                diagnostic.measured,
	                            ).joinToString("=")
	                        }.ifBlank { "cabinet.routing.failure" }
	                        null
	                    }
                }
            }

            if (realizedRoute == null) {
                val reason = candidateFailures.distinct().take(3).joinToString("; ").ifBlank { "no candidate sequence" }
                diagnostics += diagnostic(
                    code = "cabinet.route.realization.no_valid_path",
                    subject = route.connectionAlias,
                    message = "No valid physical route-channel sequence could be realized: $reason.",
                    span = route.provenance.span,
                )
                return@routeLoop
            }

            selectedChannelCount += realizedRoute.channelIds.size
            realizedRoutes += realizedRoute
        }

        if (diagnostics.isNotEmpty()) {
            return CabinetRouteRealizationCompilation.Failure(
                diagnostics = diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.message })),
            )
        }

        return CabinetRouteRealizationCompilation.Success(
            ir = request.physicalIr.copy(routes = realizedRoutes),
            evidence = CabinetRouteRealizationEvidence(
                routeCount = realizedRoutes.size,
                realizedRouteCount = realizedRoutes.size,
                selectedChannelCount = selectedChannelCount,
            ),
        )
    }
}

private fun RouteIntent.throughChannelIds(strength: RouteIntentConstraintStrength): List<PhysicalObjectId> =
    constraints
        .filter { constraint ->
            constraint.kind == RouteIntentConstraintKind.THROUGH && constraint.strength == strength
        }
        .mapNotNull { constraint -> constraint.channelIdOrNull() }

private fun RouteIntent.avoidedChannelIds(): Set<PhysicalObjectId> = constraints
    .filter { constraint -> constraint.kind == RouteIntentConstraintKind.AVOID }
    .mapNotNull { constraint -> constraint.channelIdOrNull() }
    .toSet()

private fun com.engineeringood.athena.routing.RouteIntentConstraint.channelIdOrNull(): PhysicalObjectId? {
    val reference = (target as? RouteIntentConstraintTarget.Reference)?.reference ?: return null
    val resolved = reference.resolvedIdentity?.value ?: return null
    return resolved.removePrefix("physical-channel:")
        .takeIf { channelId -> channelId != resolved && channelId.isNotBlank() }
        ?.let(::PhysicalObjectId)
}

private fun com.engineeringood.athena.ir.SourceProvenance.toPhysicalProvenance(
    declarationId: String,
): PhysicalSourceProvenance = PhysicalSourceProvenance(
    sourceUnitId = PhysicalSourceUnitId(file),
    declarationId = declarationId,
    span = toPhysicalSpan(),
)

private fun com.engineeringood.athena.ir.SourceProvenance.toPhysicalSpan(): PhysicalSourceSpan = PhysicalSourceSpan(
    file = file,
    line = startLine,
    column = startColumn,
)

private fun realizeDefaultSequences(
    source: com.engineeringood.athena.drawing.composition.CabinetPointD,
    target: com.engineeringood.athena.drawing.composition.CabinetPointD,
    channels: List<PhysicalRouteChannel>,
    adjacencyGraph: Map<PhysicalObjectId, Set<PhysicalObjectId>>,
): List<List<PhysicalObjectId>> {
    val startCandidates = channels
        .sortedWith(compareBy({ distance(source, it) }, { it.id.value }))
    val endCandidates = channels
        .sortedWith(compareBy({ distance(target, it) }, { it.id.value }))
    val candidatePairs = buildList {
        startCandidates.forEach { start ->
            endCandidates.forEach { end ->
                add(Triple(start.id, end.id, distance(source, start) + distance(target, end)))
            }
        }
    }.sortedWith(compareBy({ it.third }, { it.first.value }, { it.second.value }))

    for ((start, end, _) in candidatePairs) {
        val path = shortestPath(start, end, adjacencyGraph)
        if (path.isNotEmpty()) {
            return listOf(path)
        }
    }
    return emptyList()
}

private fun shortestPath(
    start: PhysicalObjectId,
    end: PhysicalObjectId,
    adjacencyGraph: Map<PhysicalObjectId, Set<PhysicalObjectId>>,
): List<PhysicalObjectId> {
    if (start == end) return listOf(start)
    val queue = ArrayDeque<List<PhysicalObjectId>>()
    val seen = mutableSetOf<PhysicalObjectId>()
    queue += listOf(start)
    seen += start
    while (queue.isNotEmpty()) {
        val path = queue.removeFirst()
        val tail = path.last()
        adjacencyGraph[tail].orEmpty()
            .sortedBy { channel -> channel.value }
            .forEach { next ->
                if (next in path) return@forEach
                val nextPath = path + next
                if (next == end) {
                    return nextPath
                }
                if (next !in seen) {
                    seen += next
                    queue += nextPath
                }
            }
    }
    return emptyList()
}

private fun buildAdjacencyGraph(channels: List<PhysicalRouteChannel>): Map<PhysicalObjectId, Set<PhysicalObjectId>> {
    val neighbors = channels.associate { channel -> channel.id to mutableSetOf<PhysicalObjectId>() }.toMutableMap()
    channels.forEachIndexed { index, left ->
        channels.drop(index + 1).forEach { right ->
            if (passableAdjacency(left, right)) {
                neighbors.getValue(left.id) += right.id
                neighbors.getValue(right.id) += left.id
            }
        }
    }
    return neighbors.mapValues { (_, value) -> value.toSortedSet(compareBy { it.value }) }
}

private fun passableAdjacency(
    left: PhysicalRouteChannel,
    right: PhysicalRouteChannel,
): Boolean {
    if (left.ductId != right.ductId) return false
    val a = left.bounds()
    val b = right.bounds()
    val verticalBoundary = when {
        a.right == b.x -> a.right
        b.right == a.x -> a.x
        else -> null
    }
    if (verticalBoundary != null && !a.overlapsPositiveArea(b)) {
        val overlapStart = maxOf(a.y, b.y)
        val overlapEnd = minOf(a.bottom, b.bottom)
        val trim = maxOf(left.margin.value, right.margin.value)
        if (overlapEnd - overlapStart - (2 * trim) > 0) {
            return true
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
        val trim = maxOf(left.margin.value, right.margin.value)
        if (overlapEnd - overlapStart - (2 * trim) > 0) {
            return true
        }
    }
    return false
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

private fun PhysicalRouteChannel.bounds(): ChannelRect = ChannelRect(at.x, at.y, size.width, size.height)

private fun distance(
    point: com.engineeringood.athena.drawing.composition.CabinetPointD,
    channel: PhysicalRouteChannel,
): Int {
    val rect = channel.bounds()
    val dx = when {
        point.x < rect.x -> rect.x - point.x
        point.x > rect.right -> point.x - rect.right
        else -> 0.0
    }
    val dy = when {
        point.y < rect.y -> rect.y - point.y
        point.y > rect.bottom -> point.y - rect.bottom
        else -> 0.0
    }
    return abs(dx + dy).toInt()
}

private fun diagnostic(
    code: String,
    subject: String,
    message: String,
    span: PhysicalSourceSpan?,
): CabinetRouteRealizationDiagnostic = CabinetRouteRealizationDiagnostic(
    code = code,
    subject = subject,
    message = message,
    span = span,
)
