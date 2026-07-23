package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.RepresentationDescriptor

object DescriptorBackedGraphicResourceRenderPayloadMapper {
    fun from(
        evidence: BindingEvidencePayload,
        descriptor: RepresentationDescriptor,
        governedMargin: Double,
        interactionState: String,
    ): DescriptorBackedGraphicResourceRenderPayload {
        val dedupedLabels = evidence.labelBindingSummary.distinct()
        return DescriptorBackedGraphicResourceRenderPayload(
            semanticSubjectId = evidence.semanticSubjectId,
            resourceHandle = descriptor.resource.resourceId.value,
            resourceKind = descriptor.resource.kind.name,
            bounds = DescriptorBackedBoundsPayload(
                width = descriptor.bounds.width,
                height = descriptor.bounds.height,
            ),
            anchorSummary = descriptor.anchors
                .sortedBy { it.anchorId.value }
                .map { anchor -> "${anchor.anchorId.value}=${anchor.x},${anchor.y},${anchor.side.name}" },
            labelSummary = dedupedLabels,
            interactionState = interactionState,
            normalBackgroundVisible = false,
            normalHitboxVisible = false,
            interactionChrome = "transient",
            viewBox = DescriptorBackedViewBoxPayload(
                x = 0.0,
                y = 0.0,
                width = descriptor.bounds.width + governedMargin * 2,
                height = descriptor.bounds.height + governedMargin * 2,
            ),
            duplicateVisibleLabelCount = dedupedLabels.size - dedupedLabels.distinct().size,
            resourceSemanticInferenceForbidden = true,
        )
    }
}
