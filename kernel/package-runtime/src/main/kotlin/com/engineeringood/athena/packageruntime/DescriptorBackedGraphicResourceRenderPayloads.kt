package com.engineeringood.athena.packageruntime

data class DescriptorBackedBoundsPayload(
    val width: Double,
    val height: Double,
)

data class DescriptorBackedViewBoxPayload(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
)

data class DescriptorBackedGraphicResourceRenderPayload(
    val semanticSubjectId: String,
    val resourceHandle: String,
    val resourceKind: String,
    val bounds: DescriptorBackedBoundsPayload,
    val anchorSummary: List<String>,
    val labelSummary: List<String>,
    val interactionState: String,
    val normalBackgroundVisible: Boolean,
    val normalHitboxVisible: Boolean,
    val interactionChrome: String,
    val viewBox: DescriptorBackedViewBoxPayload,
    val duplicateVisibleLabelCount: Int,
    val resourceSemanticInferenceForbidden: Boolean,
)
