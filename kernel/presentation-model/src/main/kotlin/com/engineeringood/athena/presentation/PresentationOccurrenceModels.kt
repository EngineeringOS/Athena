package com.engineeringood.athena.presentation

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Renderable occurrence reference for one placed primitive or composite.
 */
sealed interface PresentationOccurrenceReference

/**
 * Occurrence reference that points at one primitive definition.
 */
data class PresentationPrimitiveOccurrenceReference(
    val primitiveId: PresentationPrimitiveId,
) : PresentationOccurrenceReference

/**
 * Occurrence reference that points at one composite definition.
 */
data class PresentationCompositeOccurrenceReference(
    val compositeId: PresentationCompositeId,
) : PresentationOccurrenceReference

/**
 * Rebuildable downstream occurrence for one canonical subject or alias-like downstream label.
 */
data class PresentationOccurrence(
    val occurrenceId: PresentationOccurrenceId,
    val semanticId: StableSemanticIdentity,
    val reference: PresentationOccurrenceReference,
    val bounds: PresentationBounds,
    val layer: PresentationLayer,
    val displayLabel: String? = null,
    val orientation: PresentationOrientation = PresentationOrientation.HORIZONTAL,
    val markerKeys: List<String> = emptyList(),
    val textValues: Map<PresentationTextSlotId, String> = emptyMap(),
    val anchorBindings: List<PresentationAnchorBinding> = emptyList(),
    val tokenOverrides: Map<String, String> = emptyMap(),
    val sourceProjectionIds: List<String> = emptyList(),
)

/**
 * Stable connector occurrence derived from canonical connection identity plus projection guidance.
 */
data class PresentationConnector(
    val occurrenceId: PresentationOccurrenceId,
    val semanticId: StableSemanticIdentity,
    val primitiveId: PresentationPrimitiveId,
    val routePoints: List<PresentationPoint>,
    val layer: PresentationLayer = PresentationLayer.CONNECTION,
    val sourceAnchorId: String? = null,
    val targetAnchorId: String? = null,
    val sourcePortSemanticId: StableSemanticIdentity? = null,
    val targetPortSemanticId: StableSemanticIdentity? = null,
    val markerKeys: List<String> = emptyList(),
    val tokenOverrides: Map<String, String> = emptyMap(),
    val sourceProjectionIds: List<String> = emptyList(),
)

/**
 * Binding from one downstream occurrence to one canonical or projection-owned anchor occurrence.
 */
data class PresentationAnchorBinding(
    val alias: PresentationAnchorAlias,
    val anchorId: String,
    val portSemanticId: StableSemanticIdentity? = null,
    val ownerSemanticId: StableSemanticIdentity? = null,
    val sourceLabelId: String? = null,
)
