package com.engineeringood.athena.routing

/** Severity used for route-quality diagnostics exposed to IDE surfaces. */
enum class RouteQualityDiagnosticSeverity {
    WARNING,
    ERROR,
}

/** Diagnostic emitted when a route fact is degraded or fallback. */
data class RouteQualityDiagnostic(
    val routeId: SchematicRouteId,
    val connectionId: ElectricalConnectionId,
    val qualityState: RouteQualityState,
    val failedConstraintIds: List<RouteConstraintId>,
    val failedConstraintFamilies: List<RouteConstraintKind>,
    val message: String,
    val severity: RouteQualityDiagnosticSeverity,
) {
    init {
        require(qualityState != RouteQualityState.SATISFIED) {
            "Satisfied route facts must not publish route-quality diagnostics."
        }
        require(message.isNotBlank()) { "Route-quality diagnostic message must not be blank." }
    }
}

/** Inspection payload for one rendered or renderable route. */
data class RouteInspectionPayload(
    val routeId: SchematicRouteId,
    val connectionId: ElectricalConnectionId,
    val sourceAnchorId: TerminalAnchorId,
    val targetAnchorId: TerminalAnchorId,
    val sourcePortId: ElectricalPortId,
    val targetPortId: ElectricalPortId,
    val sourcePortSemanticId: com.engineeringood.athena.ir.StableSemanticIdentity? = null,
    val targetPortSemanticId: com.engineeringood.athena.ir.StableSemanticIdentity? = null,
    val qualityState: RouteQualityState,
    val failedConstraintIds: List<RouteConstraintId>,
    val failedConstraintFamilies: List<RouteConstraintKind>,
    val policySummary: String,
    val message: String? = null,
)

/** Snapshot-level inspection payload for route-quality UI and reveal paths. */
data class RouteQualityInspectionPayload(
    val snapshotId: com.engineeringood.athena.layout.LayoutSnapshotId,
    val family: String,
    val routes: List<RouteInspectionPayload>,
)

/** Publishes route-quality diagnostics and inspection payloads from governed route facts. */
class RouteQualityDiagnosticPublisher {
    fun diagnosticsFor(snapshot: RouteFactSnapshot): List<RouteQualityDiagnostic> {
        return snapshot.routeFacts
            .filterNot { route -> route.quality.state == RouteQualityState.SATISFIED }
            .map { route -> route.toDiagnostic() }
    }

    fun inspectionPayloadFor(snapshot: RouteFactSnapshot): RouteQualityInspectionPayload {
        return RouteQualityInspectionPayload(
            snapshotId = snapshot.snapshotId,
            family = snapshot.family,
            routes = snapshot.routeFacts.map { route -> route.toInspectionPayload() },
        )
    }

    private fun RouteFact.toDiagnostic(): RouteQualityDiagnostic {
        return RouteQualityDiagnostic(
            routeId = routeId,
            connectionId = connectionId,
            qualityState = quality.state,
            failedConstraintIds = quality.failedConstraintIds,
            failedConstraintFamilies = failedConstraintFamilies(),
            message = quality.message ?: "Route `${routeId.value}` is ${quality.state.name.lowercase()}.",
            severity = when (quality.state) {
                RouteQualityState.DEGRADED -> RouteQualityDiagnosticSeverity.WARNING
                RouteQualityState.FALLBACK -> RouteQualityDiagnosticSeverity.WARNING
                RouteQualityState.SATISFIED -> error("Satisfied route facts do not publish diagnostics.")
            },
        )
    }

    private fun RouteFact.toInspectionPayload(): RouteInspectionPayload {
        return RouteInspectionPayload(
            routeId = routeId,
            connectionId = connectionId,
            sourceAnchorId = source.anchorId,
            targetAnchorId = target.anchorId,
            sourcePortId = source.portId,
            targetPortId = target.portId,
            sourcePortSemanticId = source.portSemanticId,
            targetPortSemanticId = target.portSemanticId,
            qualityState = quality.state,
            failedConstraintIds = quality.failedConstraintIds,
            failedConstraintFamilies = failedConstraintFamilies(),
            policySummary = "m24:route-fact:${quality.state.name}:${segments.size}-segment",
            message = quality.message,
        )
    }

    private fun RouteFact.failedConstraintFamilies(): List<RouteConstraintKind> {
        val constraintsById = constraints.associateBy(RouteConstraint::constraintId)
        return quality.failedConstraintIds
            .mapNotNull { constraintId -> constraintsById[constraintId]?.kind }
            .distinct()
    }
}
