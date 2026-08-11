package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.*

data class VariantResolutionResult(val variant: RepresentationVariant?, val diagnostics: List<VariantDiagnostic>) {
    val isValid get() = variant != null && diagnostics.isEmpty()
}

data class VariantDiagnostic(val subject: String, val problem: String, val correction: String, val code: String)

object VariantResolver {
    fun resolve(elementId: PackageItemIdentity, variant: RepresentationVariant, expected: ElementInterfaceFingerprint): VariantResolutionResult {
        if (variant.elementId != elementId) return reject(variant.variantId.key, "Variant targets a different Element.", "Select a Variant declared for `${elementId.key}`.", "variant.element.mismatch")
        if (variant.interfaceFingerprint != expected) return reject(variant.variantId.key, "Variant changes the Element public interface.", "Create a new Element item/version for the changed Function slots or public ports.", "variant.interface.mismatch")
        return VariantResolutionResult(variant, emptyList())
    }

    private fun reject(subject: String, problem: String, correction: String, code: String) = VariantResolutionResult(null, listOf(VariantDiagnostic(subject, problem, correction, code)))
}

