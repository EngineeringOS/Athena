package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.FunctionPartBinding
import com.engineeringood.athena.packageplatform.PartFacts

data class FunctionPartBindingRequirements(
    val functionTemplates: Set<String> = emptySet(),
    val capabilities: Set<String> = emptySet(),
    val interfaces: Set<String> = emptySet(),
)

data class PartBindingDiagnostic(
    val subject: String,
    val problem: String,
    val correction: String,
    val code: String,
)

data class FunctionPartBindingAdmissionResult(
    val binding: FunctionPartBinding?,
    val diagnostics: List<PartBindingDiagnostic>,
) {
    val isValid: Boolean get() = binding != null && diagnostics.isEmpty()
}

/** Validates source-owned Part selection against admitted reusable Part facts. */
object FunctionPartBindingAdmission {
    fun admit(
        binding: FunctionPartBinding,
        part: PartFacts,
        requirements: FunctionPartBindingRequirements,
    ): FunctionPartBindingAdmissionResult {
        val diagnostics = buildList {
            missingDiagnostics(
                binding,
                requirements.capabilities,
                part.capabilityContracts,
                "capability",
                "part.binding.capability.incompatible",
            ).forEach(::add)
            missingDiagnostics(
                binding,
                requirements.functionTemplates,
                part.functionTemplates,
                "function template",
                "part.binding.function-template.incompatible",
            ).forEach(::add)
            missingDiagnostics(
                binding,
                requirements.interfaces,
                part.interfaceContracts,
                "interface",
                "part.binding.interface.incompatible",
            ).forEach(::add)
        }.sortedBy(PartBindingDiagnostic::code)
        return FunctionPartBindingAdmissionResult(binding.takeIf { diagnostics.isEmpty() }, diagnostics)
    }

    private fun missingDiagnostics(
        binding: FunctionPartBinding,
        required: Set<String>,
        declared: Set<String>,
        subjectKind: String,
        code: String,
    ): List<PartBindingDiagnostic> = (required - declared).sorted().map { value ->
        PartBindingDiagnostic(
            subject = binding.bindingId,
            problem = "Selected Part does not declare required $subjectKind `$value`.",
            correction = "Choose a compatible Part or declare $subjectKind `$value` on the Part.",
            code = code,
        )
    }
}
