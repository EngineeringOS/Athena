package com.engineeringood.athena.presentation

import com.engineeringood.athena.document.CrossReferenceFact
import com.engineeringood.athena.document.CrossReferenceRelationType
import com.engineeringood.athena.document.DocumentLocation
import com.engineeringood.athena.document.DocumentOccurrenceId
import com.engineeringood.athena.document.DocumentProjectionSnapshot
import com.engineeringood.athena.document.SheetViewId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.representation.LabelFact
import com.engineeringood.athena.representation.PresentationTerminalFact
import com.engineeringood.athena.representation.RepresentationDefinition
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationSubjectId

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
    val graphicOccurrences: List<PresentationGraphicOccurrence> = emptyList(),
    val connectors: List<PresentationConnector> = emptyList(),
    val connectionMarkers: List<PresentationConnectionMarker> = emptyList(),
    val paintPlan: PresentationPaintPlan? = null,
    val representationFacts: List<PresentationRepresentationFact> = emptyList(),
    val referenceMarkers: List<PresentationReferenceMarkerFact> = emptyList(),
    val drawingComposition: PresentationDrawingComposition? = null,
)

/** Renderer-facing representation fact carried by Presentation IR. */
data class PresentationRepresentationFact(
    val subjectId: RepresentationSubjectId,
    val occurrenceId: RepresentationOccurrenceId,
    val definition: RepresentationDefinition,
    val terminals: List<PresentationTerminalFact>,
    val labels: List<LabelFact>,
    val sourceProjectionIds: List<String> = emptyList(),
    val packageTrace: PresentationPackageTrace? = null,
)

data class PresentationPackageTrace(
    val engineeringPackageId: String,
    val engineeringPackageVersion: String,
    val presentationProfileId: String,
    val bindingManifestId: String,
    val representationPackageId: String,
    val representationPackageVersion: String,
    val descriptorId: String,
    val graphicResourceId: String,
    val variant: String,
    val anchorMapSummary: List<String>,
    val labelBindingSummary: List<String>,
    val resolverStage: String,
) {
    init {
        require(engineeringPackageId.isNotBlank()) { "Engineering package trace id must not be blank." }
        require(presentationProfileId.isNotBlank()) { "Presentation profile trace id must not be blank." }
        require(bindingManifestId.isNotBlank()) { "Binding descriptor trace id must not be blank." }
        require(representationPackageId.isNotBlank()) { "Representation package trace id must not be blank." }
        require(descriptorId.isNotBlank()) { "Representation descriptor trace id must not be blank." }
        require(graphicResourceId.isNotBlank()) { "Graphic resource trace id must not be blank." }
    }
}

fun PresentationDocument.representationFactsForRendering(): List<PresentationRepresentationFact> =
    representationFacts.sortedWith(
        compareBy<PresentationRepresentationFact> { fact -> fact.subjectId.value }
            .thenBy { fact -> fact.occurrenceId.value },
    )

/**
 * Returns the part of this Presentation IR belonging to one selected projection sheet.
 *
 * The caller supplies projection-owned membership. Presentation IR remains downstream and does
 * not decide which engineering subjects belong to a sheet.
 */
fun PresentationDocument.scopedToProjectionMembership(
    sourceProjectionIds: Set<String>,
    connectionSemanticIds: Set<String>,
    occurrenceSemanticIds: Set<String>,
): PresentationDocument {
    return copy(
        occurrences = occurrences.filter { occurrence ->
            occurrence.semanticId.value in occurrenceSemanticIds ||
                occurrence.sourceProjectionIds.any { projectionId -> projectionId in sourceProjectionIds }
        },
        graphicOccurrences = graphicOccurrences.filter { occurrence ->
            occurrence.semanticSubjectId in occurrenceSemanticIds
        },
        connectors = connectors.filter { connector ->
            connector.semanticId.value in connectionSemanticIds ||
                connector.sourceProjectionIds.any { projectionId -> projectionId in sourceProjectionIds }
        },
        connectionMarkers = connectionMarkers.filter { marker ->
            marker.sourceProjectionIds.any { projectionId -> projectionId in sourceProjectionIds } ||
                marker.connectorIds.any { connectorId -> connectorId.value in connectionSemanticIds }
        },
        representationFacts = representationFacts.filter { fact ->
            fact.subjectId.value in occurrenceSemanticIds ||
                fact.sourceProjectionIds.any { projectionId -> projectionId in sourceProjectionIds }
        },
        referenceMarkers = referenceMarkers.filter { marker ->
            marker.sourceProjectionIds.any { projectionId -> projectionId in sourceProjectionIds }
        },
    )
}

fun PresentationDocument.connectorsForRendering(): List<PresentationConnector> = connectors

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
