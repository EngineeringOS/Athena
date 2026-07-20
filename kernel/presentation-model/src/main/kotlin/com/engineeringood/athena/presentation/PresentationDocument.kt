package com.engineeringood.athena.presentation

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.document.CrossReferenceFact
import com.engineeringood.athena.document.CrossReferenceRelationType
import com.engineeringood.athena.document.DocumentLocation
import com.engineeringood.athena.document.DocumentOccurrenceId
import com.engineeringood.athena.document.DocumentProjectionSnapshot
import com.engineeringood.athena.document.SheetViewId
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.representation.LabelFact
import com.engineeringood.athena.representation.PresentationAnatomy
import com.engineeringood.athena.representation.PresentationTerminalFact
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationSubjectId
import com.engineeringood.athena.representation.SymbolAnatomy
import com.engineeringood.athena.routing.ElectricalConnectionId
import com.engineeringood.athena.routing.RouteFact
import com.engineeringood.athena.routing.RouteFactSnapshot
import com.engineeringood.athena.routing.RouteQualityState
import com.engineeringood.athena.routing.RouteLabelFact
import com.engineeringood.athena.routing.SchematicRouteId
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
    val representationFacts: List<PresentationRepresentationFact> = emptyList(),
    val referenceMarkers: List<PresentationReferenceMarkerFact> = emptyList(),
)

/** Renderer-facing representation fact carried by Presentation IR. */
data class PresentationRepresentationFact(
    val subjectId: RepresentationSubjectId,
    val occurrenceId: RepresentationOccurrenceId,
    val symbol: SymbolAnatomy,
    val anatomy: PresentationAnatomy,
    val terminals: List<PresentationTerminalFact>,
    val labels: List<LabelFact>,
    val sourceProjectionIds: List<String> = emptyList(),
) {
    init {
        require(symbol.anatomy == anatomy) { "Presentation representation fact symbol must wrap the same anatomy." }
    }
}

fun PresentationDocument.representationFactsForRendering(): List<PresentationRepresentationFact> =
    representationFacts.sortedWith(
        compareBy<PresentationRepresentationFact> { fact -> fact.subjectId.value }
            .thenBy { fact -> fact.occurrenceId.value },
    )

data class PresentationRouteAttachmentFact(
    val routeId: SchematicRouteId,
    val connectionId: ElectricalConnectionId,
    val sourcePresentationTerminalId: com.engineeringood.athena.representation.PresentationTerminalId,
    val targetPresentationTerminalId: com.engineeringood.athena.representation.PresentationTerminalId,
    val routeQuality: RouteQualityState,
) {
    val usesCenterFallback: Boolean
        get() = false
}

fun attachRoutesToPresentationTerminals(
    routeFactSnapshot: RouteFactSnapshot,
    terminals: List<PresentationTerminalFact>,
): List<PresentationRouteAttachmentFact> {
    val terminalsByAnchor = terminals.associateBy { terminal -> terminal.routeAnchor.anchorId.value }
    return routeFactSnapshot.routeFacts.mapNotNull { route ->
        val sourceTerminal = terminalsByAnchor[route.source.anchorId.value]
        val targetTerminal = terminalsByAnchor[route.target.anchorId.value]
        if (sourceTerminal == null || targetTerminal == null) {
            null
        } else {
            PresentationRouteAttachmentFact(
                routeId = route.routeId,
                connectionId = route.connectionId,
                sourcePresentationTerminalId = sourceTerminal.presentationTerminalId,
                targetPresentationTerminalId = targetTerminal.presentationTerminalId,
                routeQuality = route.quality.state,
            )
        }
    }.sortedBy { attachment -> attachment.routeId.value }
}

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

@JvmInline
value class PresentationReferenceMarkerId(val value: String) {
    init {
        require(value.isNotBlank()) { "Presentation reference marker id must not be blank." }
    }

    override fun toString(): String = value
}

enum class PresentationReferenceMarkerKind {
    CONTINUATION,
    CROSS_REFERENCE,
}

data class PresentationReferenceMarkerFact(
    val markerId: PresentationReferenceMarkerId,
    val markerKind: PresentationReferenceMarkerKind,
    val relationType: CrossReferenceRelationType,
    val selectedSheetViewId: SheetViewId,
    val sourceOccurrenceId: DocumentOccurrenceId,
    val targetOccurrenceId: DocumentOccurrenceId,
    val sourceIdentity: StableSemanticIdentity,
    val targetIdentity: StableSemanticIdentity,
    val sourceDocumentLocation: DocumentLocation,
    val targetDocumentLocation: DocumentLocation,
    val compactNotation: String,
    val sourceProjectionIds: List<String> = emptyList(),
) {
    init {
        require(sourceDocumentLocation.sheetViewId == selectedSheetViewId) {
            "Presentation reference marker source location must belong to the selected sheet view."
        }
        require(compactNotation.isNotBlank()) { "Presentation reference marker notation must not be blank." }
        require(sourceProjectionIds.all(String::isNotBlank)) {
            "Presentation reference marker projection ids must not be blank."
        }
    }
}

fun documentReferenceMarkersForSheetView(
    documentProjection: DocumentProjectionSnapshot,
    selectedSheetViewId: SheetViewId,
): List<PresentationReferenceMarkerFact> =
    documentProjection.referenceFacts.crossReferenceFacts
        .filter { reference -> reference.sourceDocumentLocation.sheetViewId == selectedSheetViewId }
        .map { reference ->
            PresentationReferenceMarkerFact(
                markerId = PresentationReferenceMarkerId("reference-marker:${reference.crossReferenceFactId.value}"),
                markerKind = reference.markerKind(),
                relationType = reference.relationType,
                selectedSheetViewId = selectedSheetViewId,
                sourceOccurrenceId = reference.sourceOccurrenceId,
                targetOccurrenceId = reference.targetOccurrenceId,
                sourceIdentity = reference.sourceIdentity,
                targetIdentity = reference.targetIdentity,
                sourceDocumentLocation = reference.sourceDocumentLocation,
                targetDocumentLocation = reference.targetDocumentLocation,
                compactNotation = reference.displayNotation,
                sourceProjectionIds = listOf(reference.crossReferenceFactId.value),
            )
        }
        .sortedWith(
            compareBy<PresentationReferenceMarkerFact>(
                { marker -> marker.markerKind.name },
                { marker -> marker.relationType.name },
                { marker -> marker.sourceIdentity.value },
                { marker -> marker.targetIdentity.value },
                { marker -> marker.markerId.value },
            ),
        )

private fun CrossReferenceFact.markerKind(): PresentationReferenceMarkerKind =
    when (relationType) {
        CrossReferenceRelationType.ROUTE_CONTINUATION,
        CrossReferenceRelationType.TERMINAL_CONTINUATION,
        -> PresentationReferenceMarkerKind.CONTINUATION
        CrossReferenceRelationType.REPEATED_SUBJECT -> PresentationReferenceMarkerKind.CROSS_REFERENCE
    }
