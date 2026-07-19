package com.engineeringood.athena.routing

import com.engineeringood.athena.layout.LayoutSnapshotId

/** Constraint family used by governed schematic routing. */
enum class RouteConstraintKind {
    ORTHOGONAL_ONLY,
    GRID_SNAP,
    AVOID_COMPONENT_BODY,
    AVOID_NODE,
    PREFERRED_EXIT_SIDE,
    PREFERRED_ENTRY_SIDE,
    ROUTE_LANE,
    ROUTE_BUNDLE,
    TERMINAL_ORDER,
    CROSSING_POLICY,
    LABEL_CLEARANCE,
}

/** Strength of one route constraint. */
enum class RouteConstraintPriority {
    REQUIRED,
    PREFERRED,
    OPTIONAL,
}

/** One governed route constraint used before deterministic route facts are produced. */
data class RouteConstraint(
    val constraintId: RouteConstraintId,
    val kind: RouteConstraintKind,
    val connectionId: ElectricalConnectionId,
    val priority: RouteConstraintPriority = RouteConstraintPriority.PREFERRED,
    val description: String? = null,
) {
    init {
        require(description == null || description.isNotBlank()) {
            "Route constraint description must be null or non-blank."
        }
    }
}

/** Quality state for a solved route fact. */
enum class RouteQualityState {
    SATISFIED,
    DEGRADED,
    FALLBACK,
}

/** Route-quality explanation carried with route facts and diagnostics. */
data class RouteQuality(
    val state: RouteQualityState,
    val failedConstraintIds: List<RouteConstraintId> = emptyList(),
    val message: String? = null,
) {
    val isSatisfied: Boolean
        get() = state == RouteQualityState.SATISFIED

    val isDegraded: Boolean
        get() = state == RouteQualityState.DEGRADED

    init {
        require(message == null || message.isNotBlank()) { "Route quality message must be null or non-blank." }
        require(state != RouteQualityState.SATISFIED || failedConstraintIds.isEmpty()) {
            "Satisfied route quality must not carry failed constraints."
        }
    }

    companion object {
        fun satisfied(): RouteQuality = RouteQuality(RouteQualityState.SATISFIED)

        fun degraded(
            failedConstraintIds: List<RouteConstraintId>,
            message: String,
        ): RouteQuality = RouteQuality(RouteQualityState.DEGRADED, failedConstraintIds, message)

        fun fallback(
            failedConstraintIds: List<RouteConstraintId>,
            message: String,
        ): RouteQuality = RouteQuality(RouteQualityState.FALLBACK, failedConstraintIds, message)
    }
}

/** Label attached to a governed route fact. */
data class RouteLabelFact(
    val labelId: SchematicLabelId,
    val text: String,
    val anchorRouteId: SchematicRouteId,
    val placement: SchematicLabelPlacement,
) {
    init {
        require(text.isNotBlank()) { "Route label text must not be blank." }
    }
}

/** M24 governed route fact attached to terminal anchors and source connection identity. */
data class RouteFact(
    val routeId: SchematicRouteId,
    val snapshotId: LayoutSnapshotId,
    val connectionId: ElectricalConnectionId,
    val source: TerminalAnchorFact,
    val target: TerminalAnchorFact,
    val segments: List<SchematicRouteSegment>,
    val lane: SchematicRouteLane = SchematicRouteLane(0),
    val constraints: List<RouteConstraint> = emptyList(),
    val labels: List<RouteLabelFact> = emptyList(),
    val quality: RouteQuality = RouteQuality.satisfied(),
) {
    init {
        require(segments.isNotEmpty()) { "Route facts require at least one schematic segment." }
        require(constraints.all { constraint -> constraint.connectionId == connectionId }) {
            "Route fact constraints must belong to the same connection id."
        }
    }

    fun hasCanvasTruth(): Boolean = false

    internal fun stableKey(): String = listOf(
        routeId.value,
        connectionId.value,
        source.anchorId.value,
        target.anchorId.value,
    ).joinToString(separator = "|")
}

/** Ordered route bundle used to keep semantically related schematic routes readable. */
data class RouteBundleFact(
    val bundleId: RouteBundleId,
    val orderedRouteIds: List<SchematicRouteId>,
) {
    init {
        require(orderedRouteIds.isNotEmpty()) { "Route bundles require at least one route id." }
        require(orderedRouteIds.distinct().size == orderedRouteIds.size) {
            "Route bundles must not contain duplicate route ids."
        }
    }

    companion object {
        fun canonical(
            bundleId: RouteBundleId,
            routeFacts: List<RouteFact>,
        ): RouteBundleFact = RouteBundleFact(
            bundleId = bundleId,
            orderedRouteIds = routeFacts.sortedBy(RouteFact::stableKey).map(RouteFact::routeId),
        )
    }
}

/** Immutable route fact snapshot with deterministic fact ordering. */
data class RouteFactSnapshot(
    val snapshotId: LayoutSnapshotId,
    val family: String,
    val routeFacts: List<RouteFact>,
) {
    init {
        require(family.isNotBlank()) { "Route fact snapshot family must not be blank." }
        require(routeFacts.all { fact -> fact.snapshotId == snapshotId }) {
            "Route fact snapshot cannot mix route facts from another snapshot id."
        }
    }

    companion object {
        fun canonical(
            snapshotId: LayoutSnapshotId,
            family: String,
            routeFacts: List<RouteFact>,
        ): RouteFactSnapshot = RouteFactSnapshot(
            snapshotId = snapshotId,
            family = family,
            routeFacts = routeFacts.sortedBy(RouteFact::stableKey),
        )
    }
}
