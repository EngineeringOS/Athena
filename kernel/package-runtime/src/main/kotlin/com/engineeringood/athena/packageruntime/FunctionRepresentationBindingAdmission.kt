package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.FunctionRepresentationBinding
import com.engineeringood.athena.packageplatform.PackageItemAdmissionState

data class FunctionRepresentationBindingAdmissionResult(
    val binding: FunctionRepresentationBinding?,
    val diagnostics: List<String>,
)

object FunctionRepresentationBindingAdmission {
    fun admit(
        binding: FunctionRepresentationBinding,
        admittedItems: Map<String, PackageItemAdmissionState>,
        existingKeys: Set<String> = emptySet(),
    ): FunctionRepresentationBindingAdmissionResult {
        val diagnostics = buildList {
            if (admittedItems[binding.element.key] != PackageItemAdmissionState.PACKAGE_READY) {
                add("Element ${binding.element.key} is not PACKAGE_READY; choose an admitted Element.")
            }
            binding.variant?.let { variant ->
                if (admittedItems[variant.key] != PackageItemAdmissionState.PACKAGE_READY) {
                    add("Variant ${variant.key} is not PACKAGE_READY; choose an admitted Variant.")
                }
            }
            if (binding.key.canonicalId in existingKeys) {
                add("Function representation binding ${binding.key.canonicalId} is already active; update existing binding.")
            }
        }
        return FunctionRepresentationBindingAdmissionResult(binding.takeIf { diagnostics.isEmpty() }, diagnostics)
    }
}
