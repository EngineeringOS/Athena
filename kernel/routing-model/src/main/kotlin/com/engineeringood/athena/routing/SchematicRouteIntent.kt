package com.engineeringood.athena.routing

import com.engineeringood.athena.layout.LayoutSnapshotId

/** Route intent for one semantic electrical connection inside a schematic sheet context. */
data class SchematicRouteIntent(
    val routeId: SchematicRouteId,
    val snapshotId: LayoutSnapshotId,
    val viewId: String,
    val sheetId: String,
    val layoutContext: SchematicRoutingLayoutContext,
    val connectionIntent: ElectricalConnectionIntent,
    val sourceAnchor: TerminalAnchorFact,
    val targetAnchor: TerminalAnchorFact,
    val constraints: List<RouteConstraint> = emptyList(),
) {
    init {
        require(viewId.isNotBlank()) { "Schematic route intent view id must not be blank." }
        require(sheetId.isNotBlank()) { "Schematic route intent sheet id must not be blank." }
        require(sourceAnchor.subjectId == connectionIntent.sourceSubjectId) {
            "Route intent source anchor must match the source semantic subject."
        }
        require(sourceAnchor.portId == connectionIntent.sourcePortId) {
            "Route intent source anchor must match the source semantic port."
        }
        require(targetAnchor.subjectId == connectionIntent.targetSubjectId) {
            "Route intent target anchor must match the target semantic subject."
        }
        require(targetAnchor.portId == connectionIntent.targetPortId) {
            "Route intent target anchor must match the target semantic port."
        }
        require(constraints.all { constraint -> constraint.connectionId == connectionIntent.connectionId }) {
            "Route intent constraints must belong to the same semantic connection."
        }
    }

    internal fun stableKey(): String = listOf(
        connectionIntent.connectionId.value,
        routeId.value,
        sourceAnchor.anchorId.value,
        targetAnchor.anchorId.value,
    ).joinToString(separator = "|")

    fun toRouteRequest(): AthenaRouteRequest = AthenaRouteRequest(
        routeId = routeId,
        connectionIntent = connectionIntent,
        sourceAnchor = sourceAnchor,
        targetAnchor = targetAnchor,
        constraints = constraints,
    )
}

/** Immutable schematic route-intent snapshot emitted before route solving. */
data class SchematicRouteIntentSnapshot(
    val snapshotId: LayoutSnapshotId,
    val viewId: String,
    val sheetId: String,
    val layoutContext: SchematicRoutingLayoutContext,
    val routeIntents: List<SchematicRouteIntent>,
) {
    init {
        require(viewId.isNotBlank()) { "Schematic route intent snapshot view id must not be blank." }
        require(sheetId.isNotBlank()) { "Schematic route intent snapshot sheet id must not be blank." }
        require(routeIntents.all { intent -> intent.snapshotId == snapshotId }) {
            "Schematic route intent snapshot cannot mix snapshot ids."
        }
        require(routeIntents.all { intent -> intent.viewId == viewId && intent.sheetId == sheetId }) {
            "Schematic route intent snapshot cannot mix view or sheet contexts."
        }
    }

    fun toEngineInput(
        componentBounds: List<SchematicComponentBounds> = emptyList(),
    ): AthenaRouteEngineInput = AthenaRouteEngineInput(
        snapshotId = snapshotId,
        layoutContext = layoutContext,
        componentBounds = componentBounds,
        requests = routeIntents.map(SchematicRouteIntent::toRouteRequest),
    )

    companion object {
        fun canonical(
            snapshotId: LayoutSnapshotId,
            viewId: String,
            sheetId: String,
            layoutContext: SchematicRoutingLayoutContext,
            routeIntents: List<SchematicRouteIntent>,
        ): SchematicRouteIntentSnapshot = SchematicRouteIntentSnapshot(
            snapshotId = snapshotId,
            viewId = viewId,
            sheetId = sheetId,
            layoutContext = layoutContext,
            routeIntents = routeIntents.sortedBy(SchematicRouteIntent::stableKey),
        )
    }
}

/** Projects semantic electrical connection intent into schematic route intent. */
class SchematicRouteIntentProjector {
    fun project(
        snapshotId: LayoutSnapshotId,
        viewId: String,
        sheetId: String,
        layoutContext: SchematicRoutingLayoutContext,
        connectionIntents: List<ElectricalConnectionIntent>,
        anchors: List<TerminalAnchorFact>,
    ): SchematicRouteIntentSnapshot {
        val routeIntents = connectionIntents.map { intent ->
            SchematicRouteIntent(
                routeId = SchematicRouteId("route:${intent.connectionId.value}"),
                snapshotId = snapshotId,
                viewId = viewId,
                sheetId = sheetId,
                layoutContext = layoutContext,
                connectionIntent = intent,
                sourceAnchor = anchors.anchorFor(intent.sourceSubjectId, intent.sourcePortId, intent.connectionId, "source"),
                targetAnchor = anchors.anchorFor(intent.targetSubjectId, intent.targetPortId, intent.connectionId, "target"),
                constraints = defaultConstraints(intent),
            )
        }
        return SchematicRouteIntentSnapshot.canonical(
            snapshotId = snapshotId,
            viewId = viewId,
            sheetId = sheetId,
            layoutContext = layoutContext,
            routeIntents = routeIntents,
        )
    }

    private fun List<TerminalAnchorFact>.anchorFor(
        subjectId: com.engineeringood.athena.ir.StableSemanticIdentity,
        portId: ElectricalPortId,
        connectionId: ElectricalConnectionId,
        endpoint: String,
    ): TerminalAnchorFact {
        return firstOrNull { anchor -> anchor.subjectId == subjectId && anchor.portId == portId }
            ?: error("No $endpoint terminal anchor found for `${connectionId.value}` and port `${portId.value}`.")
    }

    private fun defaultConstraints(intent: ElectricalConnectionIntent): List<RouteConstraint> {
        return listOf(
            RouteConstraint(
                constraintId = RouteConstraintId("constraint:${intent.connectionId.value}:orthogonal"),
                kind = RouteConstraintKind.ORTHOGONAL_ONLY,
                connectionId = intent.connectionId,
                priority = RouteConstraintPriority.REQUIRED,
            ),
            RouteConstraint(
                constraintId = RouteConstraintId("constraint:${intent.connectionId.value}:grid-snap"),
                kind = RouteConstraintKind.GRID_SNAP,
                connectionId = intent.connectionId,
                priority = RouteConstraintPriority.REQUIRED,
            ),
        )
    }
}
