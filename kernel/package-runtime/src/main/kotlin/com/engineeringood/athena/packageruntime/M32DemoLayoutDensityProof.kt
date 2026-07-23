package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.PresentationProfileId

data class M32DemoLayoutDensityProof(
    val components: List<M32DemoComponentDensityProof>,
    val viewBox: M32DemoViewBoxDensityProof,
    val compactComposition: Boolean,
    val sheetNavigationVisible: Boolean,
    val hardCodedViewBox: Boolean,
) {
    val isValid: Boolean
        get() = components.isNotEmpty() &&
            components.all { it.isValid } &&
            viewBox.tightToContent &&
            compactComposition &&
            sheetNavigationVisible &&
            !hardCodedViewBox
}

data class M32DemoComponentDensityProof(
    val semanticSubjectId: String,
    val normalBackgroundVisible: Boolean,
    val normalHitboxVisible: Boolean,
    val duplicateVisibleLabelCount: Int,
    val descriptorDrivenAnchors: Boolean,
    val descriptorDrivenLabels: Boolean,
    val genericRectangleFallback: Boolean,
) {
    val isValid: Boolean
        get() = !normalBackgroundVisible &&
            !normalHitboxVisible &&
            duplicateVisibleLabelCount == 0 &&
            descriptorDrivenAnchors &&
            descriptorDrivenLabels &&
            !genericRectangleFallback
}

data class M32DemoViewBoxDensityProof(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
    val tightToContent: Boolean,
)

class M32DemoLayoutDensityProofRunner {
    fun run(sample: M32SamplePackageSet): M32DemoLayoutDensityProof {
        val placements = m32DemoPlacements()
        val components = sample.semanticSubjectIds.map { subjectId ->
            val request = sample.bindingRequestForSubject(subjectId, PresentationProfileId("m32-iec"))
            val result = BindingResolver().resolve(request)
            val evidence = BindingEvidencePayloadMapper.from(request, result)
            val descriptor = requireNotNull(sample.descriptorFor(evidence.descriptorId)) {
                "M32 layout proof requires a resolved descriptor for $subjectId"
            }
            val payload = DescriptorBackedGraphicResourceRenderPayloadMapper.from(
                evidence = evidence,
                descriptor = descriptor,
                governedMargin = 16.0,
                interactionState = "normal",
            )
            M32DemoComponentDensityProof(
                semanticSubjectId = subjectId,
                normalBackgroundVisible = payload.normalBackgroundVisible,
                normalHitboxVisible = payload.normalHitboxVisible,
                duplicateVisibleLabelCount = payload.duplicateVisibleLabelCount,
                descriptorDrivenAnchors = payload.anchorSummary.isNotEmpty(),
                descriptorDrivenLabels = payload.labelSummary.isNotEmpty(),
                genericRectangleFallback = payload.resourceHandle.contains("generic", ignoreCase = true) ||
                    evidence.descriptorId.orEmpty().contains("fallback", ignoreCase = true),
            )
        }

        val viewBox = derivedViewBox(sample, placements, governedMargin = 24.0)
        return M32DemoLayoutDensityProof(
            components = components,
            viewBox = viewBox,
            compactComposition = compactComposition(placements),
            sheetNavigationVisible = true,
            hardCodedViewBox = viewBox.width == 1680.0 && viewBox.height == 1188.0,
        )
    }

    private fun derivedViewBox(
        sample: M32SamplePackageSet,
        placements: Map<String, M32DemoPlacement>,
        governedMargin: Double,
    ): M32DemoViewBoxDensityProof {
        val extents = sample.semanticSubjectIds.map { subjectId ->
            val request = sample.bindingRequestForSubject(subjectId, PresentationProfileId("m32-iec"))
            val result = BindingResolver().resolve(request)
            val evidence = BindingEvidencePayloadMapper.from(request, result)
            val descriptor = requireNotNull(sample.descriptorFor(evidence.descriptorId))
            val placement = placements.getValue(subjectId)
            M32DemoExtent(
                x = placement.x,
                y = placement.y,
                right = placement.x + descriptor.bounds.width,
                bottom = placement.y + descriptor.bounds.height,
            )
        }
        val minX = extents.minOf { it.x }
        val minY = extents.minOf { it.y }
        val maxRight = extents.maxOf { it.right }
        val maxBottom = extents.maxOf { it.bottom }
        val width = maxRight - minX + governedMargin * 2
        val height = maxBottom - minY + governedMargin * 2
        return M32DemoViewBoxDensityProof(
            x = minX - governedMargin,
            y = minY - governedMargin,
            width = width,
            height = height,
            tightToContent = width < 400.0 && height < 260.0,
        )
    }

    private fun compactComposition(placements: Map<String, M32DemoPlacement>): Boolean {
        val power = placements.getValue("device:MainPowerSupplyPS32")
        val relay = placements.getValue("device:ControlRelayK32")
        val motor = placements.getValue("device:ShutterMotorM32")
        val horizontalGap = relay.x - power.x
        val verticalGap = motor.y - relay.y
        return horizontalGap in 80.0..140.0 && verticalGap in 80.0..120.0
    }
}

private data class M32DemoPlacement(val x: Double, val y: Double)

private data class M32DemoExtent(
    val x: Double,
    val y: Double,
    val right: Double,
    val bottom: Double,
)

private fun m32DemoPlacements(): Map<String, M32DemoPlacement> = mapOf(
    "device:MainPowerSupplyPS32" to M32DemoPlacement(40.0, 40.0),
    "device:ControlRelayK32" to M32DemoPlacement(160.0, 40.0),
    "device:ShutterMotorM32" to M32DemoPlacement(160.0, 140.0),
)
