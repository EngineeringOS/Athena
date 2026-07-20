package com.engineeringood.athena.routing

/**
 * Stable identifier for one route backend adapter.
 *
 * Backend ids classify an implementation boundary only. They never replace Athena route,
 * connection, terminal, or document identities.
 */
@JvmInline
value class RoutingBackendId(val value: String) {
    init {
        require(value.isNotBlank()) { "Routing backend id must not be blank." }
    }

    override fun toString(): String = value
}

/**
 * Explicit authority claims a backend result must not make.
 */
data class RoutingBackendAuthorityClaims(
    val ownsSemanticConnectionMeaning: Boolean = false,
    val ownsSourceMutation: Boolean = false,
    val ownsTerminalIdentity: Boolean = false,
    val ownsDocumentOccurrenceIdentity: Boolean = false,
    val ownsPersistedLayoutTruth: Boolean = false,
) {
    val isAthenaSafe: Boolean
        get() = !ownsSemanticConnectionMeaning &&
            !ownsSourceMutation &&
            !ownsTerminalIdentity &&
            !ownsDocumentOccurrenceIdentity &&
            !ownsPersistedLayoutTruth
}

/**
 * Raw backend result before Athena normalizes it into route facts.
 */
data class RoutingBackendResult(
    val backendId: RoutingBackendId,
    val routeFacts: List<RouteFact>,
    val authorityClaims: RoutingBackendAuthorityClaims = RoutingBackendAuthorityClaims(),
) {
    init {
        require(routeFacts.isNotEmpty()) { "Routing backend result must contain at least one route fact." }
    }
}

/**
 * Adapter seam for built-in or future external route solvers.
 *
 * Implementations may search paths. They do not own semantic meaning, terminal identity, source
 * mutation, document occurrence identity, or persisted layout truth.
 */
interface RoutingBackendAdapter {
    val backendId: RoutingBackendId

    fun solve(input: AthenaRouteEngineInput): RoutingBackendResult
}

/**
 * Accepted M27 backend adapter that delegates to Athena's deterministic v0 route engine.
 */
class AthenaV0RoutingBackendAdapter(
    private val engine: AthenaRouteEngineV0 = AthenaRouteEngineV0(),
) : RoutingBackendAdapter {
    override val backendId: RoutingBackendId = RoutingBackendId("athena-route-engine-v0")

    override fun solve(input: AthenaRouteEngineInput): RoutingBackendResult {
        val snapshot = engine.solve(input)
        return RoutingBackendResult(
            backendId = backendId,
            routeFacts = snapshot.routeFacts,
        )
    }
}

/**
 * Boundary that turns backend output into normalized Athena-owned route facts.
 */
class RoutingBackendBoundary(
    private val adapter: RoutingBackendAdapter = AthenaV0RoutingBackendAdapter(),
) {
    fun solve(input: AthenaRouteEngineInput): RouteFactSnapshot {
        val result = adapter.solve(input)
        require(result.authorityClaims.isAthenaSafe) {
            "Routing backend `${result.backendId}` attempted to claim Athena-owned authority."
        }
        require(result.routeFacts.none(RouteFact::hasCanvasTruth)) {
            "Routing backend `${result.backendId}` returned canvas-owned route truth."
        }
        require(result.routeFacts.all { fact -> fact.snapshotId == input.snapshotId }) {
            "Routing backend `${result.backendId}` returned route facts for another snapshot."
        }
        val requestedRouteIds = input.requests.map { request -> request.routeId }.toSet()
        require(result.routeFacts.all { fact -> fact.routeId in requestedRouteIds }) {
            "Routing backend `${result.backendId}` returned route facts for unrequested routes."
        }
        return RouteFactSnapshot.canonical(
            snapshotId = input.snapshotId,
            family = "schematic",
            routeFacts = result.routeFacts,
        )
    }
}
