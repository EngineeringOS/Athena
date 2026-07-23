package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.RepresentationAnchorDefinition
import com.engineeringood.athena.packageplatform.RepresentationAnchorId
import com.engineeringood.athena.packageplatform.RepresentationDescriptor

object DescriptorAnchorRouteEvidenceMapper {
    fun map(request: DescriptorAnchorRouteEvidenceRequest): DescriptorAnchorRouteEvidenceResult {
        val sourceAnchorId = request.sourceBindingEvidence.anchorFor(request.sourceSemanticTerminalId)
        val targetAnchorId = request.targetBindingEvidence.anchorFor(request.targetSemanticTerminalId)
        val diagnostics = mutableListOf<DescriptorAnchorRouteDiagnosticPayload>()

        if (request.sourceDescriptor == null || request.targetDescriptor == null) {
            diagnostics += diagnostic(
                code = "descriptor-route.descriptor.missing",
                subject = request.connectionSemanticId,
                message = "Descriptor-anchor routing requires source and target descriptors.",
            )
        }

        val sourceAnchor = request.sourceDescriptor.anchorOrNull(sourceAnchorId)
        val targetAnchor = request.targetDescriptor.anchorOrNull(targetAnchorId)
        if (sourceAnchorId == null || sourceAnchor == null || targetAnchorId == null || targetAnchor == null) {
            diagnostics += diagnostic(
                code = "descriptor-route.anchor.missing",
                subject = request.connectionSemanticId,
                message = "Descriptor-anchor routing requires mapped source and target anchors.",
            )
        }

        val sortedDiagnostics = diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.message }))
        if (sortedDiagnostics.isNotEmpty()) {
            return DescriptorAnchorRouteEvidenceResult(
                route = null,
                diagnostics = sortedDiagnostics,
                centerFallbackAccepted = false,
            )
        }

        val sourceDescriptor = requireNotNull(request.sourceDescriptor)
        val targetDescriptor = requireNotNull(request.targetDescriptor)
        val source = requireNotNull(sourceAnchor)
        val target = requireNotNull(targetAnchor)
        return DescriptorAnchorRouteEvidenceResult(
            route = DescriptorAnchorRoutePayload(
                connectionSemanticId = request.connectionSemanticId,
                sourceSemanticTerminalId = request.sourceSemanticTerminalId,
                targetSemanticTerminalId = request.targetSemanticTerminalId,
                sourceDescriptorAnchorEvidence = "${sourceDescriptor.descriptorId.value}#${source.anchorId.value}",
                targetDescriptorAnchorEvidence = "${targetDescriptor.descriptorId.value}#${target.anchorId.value}",
                sourcePoint = source.toRoutePoint(),
                targetPoint = target.toRoutePoint(),
            ),
            diagnostics = emptyList(),
            centerFallbackAccepted = false,
        )
    }

    private fun BindingEvidencePayload.anchorFor(semanticTerminalId: String): RepresentationAnchorId? =
        anchorMapSummary.parsePairs()[semanticTerminalId]?.let(::RepresentationAnchorId)

    private fun RepresentationDescriptor?.anchorOrNull(anchorId: RepresentationAnchorId?): RepresentationAnchorDefinition? =
        this?.anchors?.firstOrNull { it.anchorId == anchorId }

    private fun RepresentationAnchorDefinition.toRoutePoint(): DescriptorAnchorRoutePointPayload =
        DescriptorAnchorRoutePointPayload(x = x, y = y, side = side.name)

    private fun List<String>.parsePairs(): Map<String, String> =
        mapNotNull { summary ->
            val key = summary.substringBefore("=", missingDelimiterValue = "")
            val value = summary.substringAfter("=", missingDelimiterValue = "")
            if (key.isBlank() || value.isBlank()) null else key to value
        }.toMap()

    private fun diagnostic(
        code: String,
        subject: String,
        message: String,
    ): DescriptorAnchorRouteDiagnosticPayload = DescriptorAnchorRouteDiagnosticPayload(
        code = code,
        subject = subject,
        message = message,
    )
}
