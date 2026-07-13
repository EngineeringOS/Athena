package com.engineeringood.athena.presentation

/**
 * Domain-neutral primitive pack hosted by Athena-owned extension seams.
 *
 * A primitive pack contributes reusable downstream atoms. It may target one or more projection
 * families, but it never becomes engineering truth.
 */
data class PresentationPrimitivePack(
    val packId: PresentationPackId,
    val displayName: String,
    val familyIds: Set<String> = emptySet(),
    val primitives: List<PresentationPrimitiveDefinition>,
)

/**
 * Domain-neutral composite pack hosted by Athena-owned extension seams.
 *
 * Composites assemble downstream presentation only. They are not semantic macro or engineering
 * assembly.
 */
data class PresentationCompositePack(
    val packId: PresentationPackId,
    val displayName: String,
    val familyIds: Set<String> = emptySet(),
    val composites: List<PresentationCompositeDefinition>,
)

/**
 * Reusable primitive atom expressed through backend-neutral shape commands and slots.
 */
data class PresentationPrimitiveDefinition(
    val primitiveId: PresentationPrimitiveId,
    val displayName: String,
    val viewBoxWidth: Int,
    val viewBoxHeight: Int,
    val commands: List<PresentationShapeCommand>,
    val textSlots: List<PresentationTextSlot> = emptyList(),
    val anchors: List<PresentationAnchorDefinition> = emptyList(),
    val tokenDefaults: Map<String, String> = emptyMap(),
    val supportedOrientations: Set<PresentationOrientation> = setOf(PresentationOrientation.HORIZONTAL),
)

/**
 * Reusable composite definition assembled from primitive atoms.
 */
data class PresentationCompositeDefinition(
    val compositeId: PresentationCompositeId,
    val displayName: String,
    val viewBoxWidth: Int,
    val viewBoxHeight: Int,
    val parts: List<PresentationCompositePart>,
    val textSlots: List<PresentationTextSlot> = emptyList(),
    val tokenDefaults: Map<String, String> = emptyMap(),
    val supportedOrientations: Set<PresentationOrientation> = setOf(PresentationOrientation.HORIZONTAL),
)

/**
 * Local placement of one primitive part inside one composite definition.
 */
data class PresentationCompositePart(
    val partId: String,
    val primitiveId: PresentationPrimitiveId,
    val bounds: PresentationBounds,
    val tokenOverrides: Map<String, String> = emptyMap(),
    val orientation: PresentationOrientation = PresentationOrientation.HORIZONTAL,
)

/**
 * Named text slot that one primitive or composite may expose for downstream label injection.
 */
data class PresentationTextSlot(
    val slotId: PresentationTextSlotId,
    val origin: PresentationPoint,
    val tokenKey: String = "label",
)

/**
 * Named local anchor definition exposed by one primitive.
 */
data class PresentationAnchorDefinition(
    val alias: PresentationAnchorAlias,
    val point: PresentationPoint,
)
