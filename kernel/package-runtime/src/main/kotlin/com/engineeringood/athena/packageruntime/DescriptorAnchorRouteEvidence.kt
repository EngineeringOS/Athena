package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.RepresentationDescriptor

data class DescriptorAnchorRouteEvidenceRequest(
    val connectionSemanticId: String,
    val sourceSemanticTerminalId: String,
    val targetSemanticTerminalId: String,
    val sourceBindingEvidence: BindingEvidencePayload,
    val targetBindingEvidence: BindingEvidencePayload,
    val sourceDescriptor: RepresentationDescriptor?,
    val targetDescriptor: RepresentationDescriptor?,
)

data class DescriptorAnchorRoutePointPayload(
    val x: Double,
    val y: Double,
    val side: String,
)

data class DescriptorAnchorRoutePayload(
    val connectionSemanticId: String,
    val sourceSemanticTerminalId: String,
    val targetSemanticTerminalId: String,
    val sourceDescriptorAnchorEvidence: String,
    val targetDescriptorAnchorEvidence: String,
    val sourcePoint: DescriptorAnchorRoutePointPayload,
    val targetPoint: DescriptorAnchorRoutePointPayload,
)

data class DescriptorAnchorRouteDiagnosticPayload(
    val code: String,
    val subject: String,
    val message: String,
)

data class DescriptorAnchorRouteEvidenceResult(
    val route: DescriptorAnchorRoutePayload?,
    val diagnostics: List<DescriptorAnchorRouteDiagnosticPayload>,
    val centerFallbackAccepted: Boolean = false,
) {
    val isValid: Boolean
        get() = route != null && diagnostics.isEmpty() && !centerFallbackAccepted
}
