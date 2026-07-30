package com.engineeringood.athena.ide.lsp

/**
 * Shared compiler evidence transported through the Athena LSP boundary.
 */
data class AthenaPresentationTracePayload(
    val sourceProvenance: List<String> = emptyList(),
    val sourceProjectionIds: List<String> = emptyList(),
    val compilerStage: String,
    val packageEvidence: AthenaPresentationPackageEvidencePayload? = null,
)

/**
 * Route-snapshot payload transported through the Athena LSP boundary.
 */
data class AthenaPresentationRouteFactSnapshotPayload(
    val snapshotId: String,
    val family: String,
    val routeFacts: List<AthenaPresentationRouteFactPayload>,
    val junctionFacts: List<AthenaPresentationRouteJunctionFactPayload> = emptyList(),
    val crossingFacts: List<AthenaPresentationRouteCrossingFactPayload> = emptyList(),
)

data class AthenaPresentationRouteFactPayload(
    val routeId: String,
    val snapshotId: String,
    val connectionId: String,
    val source: AthenaPresentationTerminalFactPayload,
    val target: AthenaPresentationTerminalFactPayload,
    val segments: List<AthenaPresentationRouteSegmentPayload>,
    val lane: Int,
    val quality: AthenaPresentationRouteQualityPayload,
    val trace: AthenaPresentationTracePayload,
)

data class AthenaPresentationRouteSegmentPayload(
    val start: AthenaProjectionPointPayload,
    val end: AthenaProjectionPointPayload,
)

data class AthenaPresentationRouteQualityPayload(
    val state: String,
    val failedConstraintIds: List<String> = emptyList(),
    val message: String? = null,
)

data class AthenaPresentationRouteJunctionFactPayload(
    val junctionId: String,
    val point: AthenaProjectionPointPayload,
    val routeIds: List<String>,
    val semanticPortId: String,
)

data class AthenaPresentationRouteCrossingFactPayload(
    val crossingId: String,
    val point: AthenaProjectionPointPayload,
    val routeIds: List<String>,
    val joined: Boolean,
)
