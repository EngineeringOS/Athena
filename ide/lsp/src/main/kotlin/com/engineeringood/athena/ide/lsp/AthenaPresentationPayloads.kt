package com.engineeringood.athena.ide.lsp

data class AthenaPointPayload(
    val x: Int,
    val y: Int,
)

/**
 * Rebuildable Presentation IR payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationDocumentPayload(
    val canvasWidth: Int,
    val canvasHeight: Int,
    val primitivePacks: List<AthenaPresentationPrimitivePackPayload>,
    val compositePacks: List<AthenaPresentationCompositePackPayload>,
    val occurrences: List<AthenaPresentationOccurrencePayload>,
    val graphicOccurrences: List<AthenaPresentationGraphicOccurrencePayload> = emptyList(),
    val connectors: List<AthenaPresentationConnectorPayload>,
    val connectionMarkers: List<AthenaPresentationConnectionMarkerPayload> = emptyList(),
    val paintPlan: AthenaPresentationPaintPlanPayload,
    val representationFacts: List<AthenaPresentationRepresentationFactPayload> = emptyList(),
    val referenceMarkers: List<AthenaPresentationReferenceMarkerPayload> = emptyList(),
    val drawingComposition: AthenaDrawingCompositionPayload? = null,
)

data class AthenaPresentationPaintPlanPayload(
    val items: List<AthenaPresentationPaintItemPayload>,
)

data class AthenaPresentationPaintItemPayload(
    val itemId: String,
    val targetId: String,
    val kind: String,
    val visible: Boolean,
    val order: Int,
)

data class AthenaPresentationGraphicOccurrencePayload(
    val occurrenceId: String,
    val semanticSubjectId: String,
    val physicalComponentId: String,
    val functionId: String?,
    val bounds: AthenaPresentationBoundsPayload,
    val orientation: String,
    val deviceLabel: String,
    val modelLabel: String?,
    val packageId: String,
    val definitionId: String,
    val bindingRuleId: String,
    val graphic: AthenaGraphicPrimitiveDocumentPayload,
    val placedAnchors: List<AthenaPresentationPlacedAnchorPayload> = emptyList(),
    val terminalBindings: List<AthenaPresentationGraphicTerminalBindingPayload>,
    val labels: List<AthenaPresentationGraphicLabelPayload>,
    val sourceProvenance: List<String>,
    val authorities: AthenaPresentationGraphicOccurrenceAuthoritiesPayload,
    val trace: AthenaPresentationTracePayload? = null,
)

data class AthenaPresentationPlacedAnchorPayload(
    val anchorId: String,
    val geometryRef: String,
    val primitiveId: String,
    val point: AthenaPointPayload,
    val role: String,
    val required: Boolean,
    val sourceProvenance: List<String>,
    val trace: AthenaPresentationTracePayload? = null,
)

data class AthenaPresentationGraphicOccurrenceAuthoritiesPayload(
    val graphic: String,
    val placement: String,
    val material: String,
)

data class AthenaPresentationGraphicTerminalBindingPayload(
    val portSemanticId: String,
    val bindingId: String,
    val anchorId: String,
    val terminalIdentity: String,
    val point: AthenaPointPayload,
    val labelPoint: AthenaPointPayload,
    val side: String,
    val trace: AthenaPresentationTracePayload? = null,
)

data class AthenaPresentationGraphicLabelPayload(
    val labelId: String,
    val role: String,
    val value: String,
    val bounds: AthenaPresentationBoundsPayload,
)

data class AthenaGraphicPrimitiveDocumentPayload(
    val documentId: String?,
    val bounds: AthenaPresentationBoundsPayload?,
    val primitives: List<AthenaGraphicPrimitivePayload>,
    val provenanceSources: List<String>,
    val forbiddenAuthorityClaims: List<String>,
)

data class AthenaGraphicPrimitivePayload(
    val primitiveId: String,
    val kind: String,
    val bounds: AthenaPresentationBoundsPayload,
    val styleTokenId: String?,
    val start: AthenaPointPayload? = null,
    val end: AthenaPointPayload? = null,
    val points: List<AthenaPointPayload> = emptyList(),
    val center: AthenaPointPayload? = null,
    val origin: AthenaPointPayload? = null,
    val radius: Int? = null,
    val startAngleDegrees: Double? = null,
    val sweepAngleDegrees: Double? = null,
    val text: String? = null,
    val cornerRadius: Int? = null,
    val markerKind: String? = null,
    val headSize: Int? = null,
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
    val start: AthenaPointPayload? = null,
    val end: AthenaPointPayload? = null,
    val center: AthenaPointPayload? = null,
    val origin: AthenaPointPayload? = null,
    val radius: Int? = null,
    val text: String? = null,
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
    val trace: AthenaPresentationTracePayload? = null,
)

/**
 * Connector payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationConnectorPayload(
    val occurrenceId: String,
    val semanticId: String,
    val primitiveId: String,
    val routePoints: List<AthenaPointPayload>,
    val lineClassId: String,
    val line: AthenaPresentationConnectorLinePayload,
    val routeId: String,
    val bundleId: String,
    val laneId: String,
    val laneRouteIds: List<String>,
    val selectedChannelIds: List<String>,
    val labels: List<AthenaPresentationConnectorLabelPayload>,
    val quality: String,
    val sourceEndpoint: AthenaPresentationConnectorEndpointPayload,
    val targetEndpoint: AthenaPresentationConnectorEndpointPayload,
    val layer: String,
    val markerIds: List<String>,
    val tokenOverrides: Map<String, String>,
    val sourceProjectionIds: List<String>,
    val trace: AthenaPresentationTracePayload? = null,
    val sourceSpan: AthenaPresentationSourceSpanPayload? = null,
)

data class AthenaPresentationConnectorLinePayload(
    val classId: String,
    val lineKind: String,
    val lineStyleId: String,
    val weight: Double,
    val style: String,
    val colorKey: String,
    val endpointBehavior: String,
    val labelPolicy: String,
    val crossingBehavior: String,
    val policyId: String,
    val compilerSnapshotId: String,
)

data class AthenaPresentationConnectorLabelPayload(
    val labelId: String,
    val text: String,
    val point: AthenaPointPayload,
    val bounds: AthenaPresentationBoundsPayload,
    val labelClassId: String,
    val display: String,
    val sourceProvenance: List<String>,
    val compilerSnapshotId: String,
    val trace: AthenaPresentationTracePayload? = null,
)

data class AthenaPresentationConnectorEndpointPayload(
    val portSemanticId: String,
    val bindingId: String,
    val occurrenceId: String,
    val anchorId: String,
    val point: AthenaPointPayload,
    val sourceProvenance: List<String>,
    val trace: AthenaPresentationTracePayload? = null,
)

data class AthenaPresentationConnectionMarkerPayload(
    val markerId: String,
    val kind: String,
    val point: AthenaPointPayload,
    val routeIds: List<String>,
    val connectorIds: List<String>,
    val semanticId: String?,
    val joined: Boolean,
    val appearanceClassId: String,
    val sourceProjectionIds: List<String>,
    val sourceProvenance: List<String>,
    val compilerSnapshotId: String,
    val trace: AthenaPresentationTracePayload? = null,
)

/**
 * Compact document reference marker exposed through the Athena LSP boundary.
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

data class AthenaPresentationSourceSpanPayload(
    val file: String,
    val startLine: Int,
    val startColumn: Int,
    val endLine: Int,
    val endColumn: Int,
)

/**
 * Text-slot payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationTextSlotPayload(
    val slotId: String,
    val origin: AthenaPointPayload,
    val tokenKey: String,
)

/**
 * Anchor-definition payload exposed through the Athena LSP boundary.
 */
data class AthenaPresentationAnchorDefinitionPayload(
    val alias: String,
    val point: AthenaPointPayload,
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
    val trace: AthenaPresentationTracePayload? = null,
)

/**
 * Governed representation fact exposed through the Athena LSP boundary.
 */
data class AthenaPresentationRepresentationFactPayload(
    val subjectId: String,
    val occurrenceId: String,
    val sourceProjectionIds: List<String>,
    val definition: AthenaRepresentationDefinitionPayload,
    val terminals: List<AthenaPresentationTerminalFactPayload>,
    val labels: List<AthenaPresentationLabelFactPayload>,
    val packageTrace: AthenaPresentationPackageTracePayload? = null,
    val trace: AthenaPresentationTracePayload? = null,
)

data class AthenaRepresentationDefinitionPayload(
    val symbolId: String,
    val libraryId: String,
    val version: String,
    val kind: String,
    val definitionKind: String,
    val graphicBody: AthenaGraphicPrimitiveDocumentPayload,
    val anchors: List<AthenaRepresentationAnchorPayload>,
    val labelSlots: List<AthenaRepresentationLabelSlotPayload>,
    val provenance: String,
)

data class AthenaRepresentationAnchorPayload(
    val anchorId: String,
    val geometryRef: String,
    val primitiveId: String,
    val point: AthenaPointPayload,
    val role: String,
    val required: Boolean,
)

data class AthenaRepresentationLabelSlotPayload(
    val slotId: String,
    val role: String,
    val origin: AthenaPointPayload? = null,
    val bounds: AthenaPresentationBoundsPayload? = null,
    val styleTokenId: String? = null,
)

data class AthenaPresentationPackageTracePayload(
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
)

data class AthenaPresentationSizePayload(
    val width: Int,
    val height: Int,
)

data class AthenaPresentationTerminalPointPayload(
    val terminalId: String,
    val role: String,
    val localPoint: AthenaPointPayload,
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
    val point: AthenaPointPayload,
)

data class AthenaPresentationTerminalNotationPayload(
    val marker: String,
    val number: String,
)

data class AthenaPresentationLabelAnchorPayload(
    val anchorId: String,
    val role: String,
    val point: AthenaPointPayload,
)

data class AthenaPresentationLabelFactPayload(
    val labelId: String,
    val subjectId: String,
    val occurrenceId: String,
    val role: String,
    val value: String,
    val anchor: AthenaPresentationLabelAnchorPayload,
)
