package com.engineeringood.athena.presentation

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.routing.RouteFact
import com.engineeringood.athena.routing.RouteFactSnapshot
import com.engineeringood.athena.routing.RouteLabelFact
import com.engineeringood.athena.routing.SchematicRoutePoint

/**
 * Rebuildable downstream presentation document for one supported projection view.
 *
 * `PresentationDocument` is never a second semantic authority. Canonical engineering meaning
 * remains in `Engineering IR`, view-family ownership remains in `Projection Model`, and this
 * document only describes how one governed downstream presentation language should appear.
 */
data class PresentationDocument(
    val view: ViewDefinition,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val primitivePacks: List<PresentationPrimitivePack>,
    val compositePacks: List<PresentationCompositePack>,
    val resolvedSubjects: List<PresentationResolvedSubject> = emptyList(),
    val occurrences: List<PresentationOccurrence>,
    val connectors: List<PresentationConnector> = emptyList(),
    val routeFactSnapshot: RouteFactSnapshot? = null,
)

/**
 * Renderer-facing connector list. M24 route facts take precedence over legacy edge route points
 * because route facts carry terminal anchors, segments, labels, and quality state.
 */
fun PresentationDocument.connectorsForRendering(): List<PresentationConnector> {
    val routeFacts = routeFactSnapshot?.routeFacts.orEmpty()
    return if (routeFacts.isEmpty()) {
        connectors
    } else {
        routeFacts.map(RouteFact::toPresentationConnector)
    }
}

private fun RouteFact.toPresentationConnector(): PresentationConnector {
    return PresentationConnector(
        occurrenceId = PresentationOccurrenceId(routeId.value),
        semanticId = StableSemanticIdentity(connectionId.value),
        primitiveId = PresentationPrimitiveId("electrical.conductor.orthogonal"),
        routePoints = routePoints(),
        sourceAnchorId = source.anchorId.value,
        targetAnchorId = target.anchorId.value,
        sourcePortSemanticId = source.portSemanticId ?: StableSemanticIdentity("port:${source.portId.value}"),
        targetPortSemanticId = target.portSemanticId ?: StableSemanticIdentity("port:${target.portId.value}"),
        tokenOverrides = buildMap {
            put("routeLane", lane.value.toString())
            put("routeQuality", quality.state.name)
            put("routeSegmentCount", segments.size.toString())
            if (labels.isNotEmpty()) {
                put("routeLabels", labels.joinToString(separator = "|", transform = RouteLabelFact::text))
            }
        },
    )
}

private fun RouteFact.routePoints(): List<PresentationPoint> {
    return buildList {
        add(segments.first().start.toPresentationPoint())
        segments.forEach { segment -> add(segment.end.toPresentationPoint()) }
    }
}

private fun SchematicRoutePoint.toPresentationPoint(): PresentationPoint = PresentationPoint(x = x, y = y)
