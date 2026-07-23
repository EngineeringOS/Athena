package com.engineeringood.athena.packageplatform

enum class RepresentationDescriptorDiagnosticSeverity {
    ERROR,
}

@JvmInline
value class RepresentationDescriptorDiagnosticCode(val wireValue: String) {
    override fun toString(): String = wireValue
}

data class RepresentationDescriptorDiagnostic(
    val code: RepresentationDescriptorDiagnosticCode,
    val severity: RepresentationDescriptorDiagnosticSeverity,
    val subject: String,
    val message: String,
)

data class RepresentationDescriptorValidationResult(
    val diagnostics: List<RepresentationDescriptorDiagnostic>,
) {
    val isValid: Boolean
        get() = diagnostics.none { it.severity == RepresentationDescriptorDiagnosticSeverity.ERROR }
}

object RepresentationDescriptorValidator {
    fun validate(
        descriptor: RepresentationDescriptor,
        context: RepresentationDescriptorValidationContext,
    ): RepresentationDescriptorValidationResult {
        val diagnostics = mutableListOf<RepresentationDescriptorDiagnostic>()

        validateResource(descriptor, context, diagnostics)
        validateBounds(descriptor.bounds, "bounds", diagnostics)
        validateAnchors(descriptor, diagnostics)
        validateRequiredLabels(descriptor, context, diagnostics)
        validateHotspots(descriptor, diagnostics)
        validateVariants(descriptor, context, diagnostics)
        validateSemanticAuthority(descriptor, diagnostics)

        return RepresentationDescriptorValidationResult(diagnostics)
    }

    private fun validateResource(
        descriptor: RepresentationDescriptor,
        context: RepresentationDescriptorValidationContext,
        diagnostics: MutableList<RepresentationDescriptorDiagnostic>,
    ) {
        val resource = context.resourceReferences.find { it.resourceId == descriptor.resource.resourceId }
        if (resource == null || resource.kind != descriptor.resource.kind) {
            diagnostics += diagnostic(
                code = "package.representation.descriptor.resource-missing",
                subject = "resource",
                message = "Representation Descriptor must reference a declared Graphic Resource id and kind.",
            )
        }
    }

    private fun validateBounds(
        bounds: RepresentationDescriptorBounds,
        subject: String,
        diagnostics: MutableList<RepresentationDescriptorDiagnostic>,
    ) {
        if (bounds.width <= 0.0 || bounds.height <= 0.0) {
            diagnostics += diagnostic(
                code = "package.representation.descriptor.bounds-invalid",
                subject = subject,
                message = "Representation Descriptor bounds must have positive width and height.",
            )
        }
    }

    private fun validateAnchors(
        descriptor: RepresentationDescriptor,
        diagnostics: MutableList<RepresentationDescriptorDiagnostic>,
    ) {
        descriptor.anchors
            .groupBy { it.anchorId }
            .filterValues { it.size > 1 }
            .keys
            .forEach { anchorId ->
                diagnostics += diagnostic(
                    code = "package.representation.descriptor.anchor-duplicate",
                    subject = "anchors.${anchorId.value}",
                    message = "Representation Descriptor anchor ids must be unique.",
                )
            }
    }

    private fun validateRequiredLabels(
        descriptor: RepresentationDescriptor,
        context: RepresentationDescriptorValidationContext,
        diagnostics: MutableList<RepresentationDescriptorDiagnostic>,
    ) {
        val declaredSlots = descriptor.labelSlots.map { it.slotId }.toSet()
        context.requiredLabelSlots
            .filterNot { it in declaredSlots }
            .forEach { slotId ->
                diagnostics += diagnostic(
                    code = "package.representation.descriptor.label-slot-missing",
                    subject = "labelSlots.${slotId.value}",
                    message = "Representation Descriptor is missing a required label slot.",
                )
            }
    }

    private fun validateHotspots(
        descriptor: RepresentationDescriptor,
        diagnostics: MutableList<RepresentationDescriptorDiagnostic>,
    ) {
        descriptor.hotspots.forEachIndexed { index, hotspot ->
            validateBounds(hotspot.bounds, "hotspots[$index].bounds", diagnostics)
        }
    }

    private fun validateVariants(
        descriptor: RepresentationDescriptor,
        context: RepresentationDescriptorValidationContext,
        diagnostics: MutableList<RepresentationDescriptorDiagnostic>,
    ) {
        descriptor.variants
            .filterNot { it in context.supportedVariants }
            .forEach { variantId ->
                diagnostics += diagnostic(
                    code = "package.representation.descriptor.variant-unsupported",
                    subject = "variants.${variantId.value}",
                    message = "Representation Descriptor variant is not supported by the validation context.",
                )
            }
    }

    private fun validateSemanticAuthority(
        descriptor: RepresentationDescriptor,
        diagnostics: MutableList<RepresentationDescriptorDiagnostic>,
    ) {
        descriptor.forbiddenSemanticAuthorityClaims.forEach { claim ->
            diagnostics += diagnostic(
                code = "package.representation.descriptor.semantic-authority-forbidden",
                subject = claim.source.name.lowercase(),
                message = "Representation Descriptor must not treat ${claim.source.name.lowercase()} as engineering truth.",
            )
        }
    }

    private fun diagnostic(
        code: String,
        subject: String,
        message: String,
    ): RepresentationDescriptorDiagnostic = RepresentationDescriptorDiagnostic(
        code = RepresentationDescriptorDiagnosticCode(code),
        severity = RepresentationDescriptorDiagnosticSeverity.ERROR,
        subject = subject,
        message = message,
    )
}
