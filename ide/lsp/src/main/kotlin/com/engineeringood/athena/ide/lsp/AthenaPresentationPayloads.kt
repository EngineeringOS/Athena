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
    val representationFacts: List<AthenaPresentationRepresentationFactPayload> = emptyList(),
    val referenceMarkers: List<AthenaPresentationReferenceMarkerPayload> = emptyList(),
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
 * M26 compact document reference marker exposed through the Athena LSP boundary.
 */
data class AthenaPresentationReferenceMarkerPayload(
    val markerId: String,
    val markerKind: String,
    val relationType: String,
    val selectedSheetViewId: String,
    val sourceOccurrenceId: String,
    val targetOccurrenceId: String,
    val sourceIdentity: String,
    val targetIdentity: String,
    val sourceDocumentLocation: AthenaDocumentLocationPayload,
    val targetDocumentLocation: AthenaDocumentLocationPayload,
    val compactNotation: String,
    val sourceProjectionIds: List<String>,
)

data class AthenaDocumentLocationPayload(
    val sheetViewId: String,
    val zoneId: String,
    val displayNotation: String,
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

/**
 * M25 governed representation fact exposed through the Athena LSP boundary.
 */
data class AthenaPresentationRepresentationFactPayload(
    val subjectId: String,
    val occurrenceId: String,
    val sourceProjectionIds: List<String>,
    val symbol: AthenaPresentationSymbolAnatomyPayload,
    val anatomy: AthenaPresentationAnatomyPayload,
    val terminals: List<AthenaPresentationTerminalFactPayload>,
    val labels: List<AthenaPresentationLabelFactPayload>,
)

data class AthenaPresentationSymbolAnatomyPayload(
    val familyId: String,
)

data class AthenaPresentationAnatomyPayload(
    val representationId: String,
    val context: String,
    val bounds: AthenaPresentationSizePayload,
    val hotspot: AthenaProjectionPointPayload,
    val primitives: List<AthenaPresentationAnatomyPrimitivePayload>,
    val terminals: List<AthenaPresentationTerminalPointPayload>,
    val labelAnchors: List<AthenaPresentationLabelAnchorPayload>,
)

data class AthenaPresentationAnatomyPrimitivePayload(
    val kind: String,
    val primitiveId: String,
    val origin: AthenaProjectionPointPayload? = null,
    val size: AthenaPresentationSizePayload? = null,
    val start: AthenaProjectionPointPayload? = null,
    val end: AthenaProjectionPointPayload? = null,
    val points: List<AthenaProjectionPointPayload> = emptyList(),
    val center: AthenaProjectionPointPayload? = null,
    val radius: Int? = null,
)

data class AthenaPresentationSizePayload(
    val width: Int,
    val height: Int,
)

data class AthenaPresentationTerminalPointPayload(
    val terminalId: String,
    val role: String,
    val localPoint: AthenaProjectionPointPayload,
    val side: String,
    val notation: AthenaPresentationTerminalNotationPayload,
)

data class AthenaPresentationTerminalFactPayload(
    val presentationTerminalId: String,
    val subjectId: String,
    val occurrenceId: String,
    val portId: String,
    val physicalTerminalId: String,
    val side: String,
    val routeAnchor: AthenaPresentationRouteAnchorPayload,
    val notation: AthenaPresentationTerminalNotationPayload,
)

data class AthenaPresentationRouteAnchorPayload(
    val anchorId: String,
    val point: AthenaProjectionPointPayload,
)

data class AthenaPresentationTerminalNotationPayload(
    val marker: String,
    val number: String,
)

data class AthenaPresentationLabelAnchorPayload(
    val anchorId: String,
    val role: String,
    val point: AthenaProjectionPointPayload,
)

data class AthenaPresentationLabelFactPayload(
    val labelId: String,
    val subjectId: String,
    val occurrenceId: String,
    val role: String,
    val value: String,
    val anchor: AthenaPresentationLabelAnchorPayload,
)
