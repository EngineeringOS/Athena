package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.PartFacts

data class PartFactsAdmissionResult(
    val facts: PartFacts?,
    val diagnostics: List<SymbolGeometryDiagnostic>,
) {
    val isValid: Boolean get() = facts != null && diagnostics.isEmpty()
}

object PartFactsAdmission {
    fun admit(
        facts: PartFacts,
        requiredCapabilities: Set<String> = emptySet(),
    ): PartFactsAdmissionResult {
        val missing = requiredCapabilities - facts.capabilityContracts
        if (missing.isNotEmpty()) {
            return PartFactsAdmissionResult(
                null,
                missing.sorted().map { capability ->
                    SymbolGeometryDiagnostic(
                        facts.articleNumber,
                        "Part does not declare required capability `$capability`.",
                        "Add capability `$capability` or choose a compatible Part.",
                        "part.capability.incompatible",
                    )
                },
            )
        }
        return PartFactsAdmissionResult(facts, emptyList())
    }
}
