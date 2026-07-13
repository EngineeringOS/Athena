package com.engineeringood.athena.ide.lsp

/**
 * Rebuildable Presentation IR payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationDocumentPayload(
    val canvasWidth: Int,
    val canvasHeight: Int,
    val primitivePacks: List<AthenaPresentationPrimitivePackPayload>,
    val compositePacks: List<AthenaPresentationCompositePackPayload>,
    val occurrences: List<AthenaPresentationOccurrencePayload>,
    val connectors: List<AthenaPresentationConnectorPayload>,
)

/**
 * Primitive-pack payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationPrimitivePackPayload(
    val packId: String,
    val displayName: String,
    val familyIds: List<String>,
    val primitives: List<AthenaPresentationPrimitiveDefinitionPayload>,
)

/**
 * Composite-pack payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationCompositePackPayload(
    val packId: String,
    val displayName: String,
    val familyIds: List<String>,
    val composites: List<AthenaPresentationCompositeDefinitionPayload>,
)

/**
 * Primitive-definition payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationPrimitiveDefinitionPayload(
    val primitiveId: String,
    val displayName: String,
    val viewBoxWidth: Int,
    val viewBoxHeight: Int,
    val commands: List<AthenaPresentationShapeCommandPayload>,
    val textSlots: List<AthenaPresentationTextSlotPayload>,
    val anchors: List<AthenaPresentationAnchorDefinitionPayload>,
    val tokenDefaults: Map<String, String>,
    val supportedOrientations: List<String>,
)

/**
 * Composite-definition payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationCompositeDefinitionPayload(
    val compositeId: String,
    val displayName: String,
    val viewBoxWidth: Int,
    val viewBoxHeight: Int,
    val parts: List<AthenaPresentationCompositePartPayload>,
    val textSlots: List<AthenaPresentationTextSlotPayload>,
    val tokenDefaults: Map<String, String>,
    val supportedOrientations: List<String>,
)

/**
 * Local primitive-part payload for one composite definition.
 */
data class AthenaPresentationCompositePartPayload(
    val partId: String,
    val primitiveId: String,
    val bounds: AthenaPresentationBoundsPayload,
    val tokenOverrides: Map<String, String>,
    val orientation: String,
)

/**
 * Flattened shape command payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationShapeCommandPayload(
    val kind: String,
    val bounds: AthenaPresentationBoundsPayload? = null,
    val start: AthenaProjectionPointPayload? = null,
    val end: AthenaProjectionPointPayload? = null,
    val center: AthenaProjectionPointPayload? = null,
    val radius: Int? = null,
    val pathData: String? = null,
    val strokeTokenKey: String? = null,
    val strokeWidthTokenKey: String? = null,
    val fillTokenKey: String? = null,
)

/**
 * Placed occurrence payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationOccurrencePayload(
    val occurrenceId: String,
    val semanticId: String,
    val referenceKind: String,
    val primitiveId: String? = null,
    val compositeId: String? = null,
    val bounds: AthenaPresentationBoundsPayload,
    val layer: String,
    val displayLabel: String? = null,
    val orientation: String,
    val markerKeys: List<String>,
    val textValues: Map<String, String>,
    val anchorBindings: List<AthenaPresentationAnchorBindingPayload>,
    val tokenOverrides: Map<String, String>,
    val sourceProjectionIds: List<String>,
)

/**
 * Connector payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationConnectorPayload(
    val occurrenceId: String,
    val semanticId: String,
    val primitiveId: String,
    val routePoints: List<AthenaProjectionPointPayload>,
    val layer: String,
    val sourceAnchorId: String? = null,
    val targetAnchorId: String? = null,
    val sourcePortSemanticId: String? = null,
    val targetPortSemanticId: String? = null,
    val markerKeys: List<String>,
    val tokenOverrides: Map<String, String>,
    val sourceProjectionIds: List<String>,
)

/**
 * Simple presentation bounds payload.
 */
data class AthenaPresentationBoundsPayload(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/**
 * Text-slot payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationTextSlotPayload(
    val slotId: String,
    val origin: AthenaProjectionPointPayload,
    val tokenKey: String,
)

/**
 * Anchor-definition payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationAnchorDefinitionPayload(
    val alias: String,
    val point: AthenaProjectionPointPayload,
)

/**
 * Occurrence-anchor binding payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationAnchorBindingPayload(
    val alias: String,
    val anchorId: String,
    val portSemanticId: String? = null,
    val ownerSemanticId: String? = null,
    val sourceLabelId: String? = null,
)
