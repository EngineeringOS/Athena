package com.engineeringood.athena.packageplatform

enum class PresentationProfileDiagnosticSeverity {
    ERROR,
}

@JvmInline
value class PresentationProfileDiagnosticCode(val wireValue: String) {
    override fun toString(): String = wireValue
}

data class PresentationProfileDiagnostic(
    val code: PresentationProfileDiagnosticCode,
    val severity: PresentationProfileDiagnosticSeverity,
    val subject: String,
    val message: String,
)

data class PresentationProfileValidationResult(
    val diagnostics: List<PresentationProfileDiagnostic>,
) {
    val isValid: Boolean
        get() = diagnostics.none { it.severity == PresentationProfileDiagnosticSeverity.ERROR }
}

object PresentationProfileDescriptorValidator {
    private val profileIdPattern = Regex("[A-Za-z][A-Za-z0-9_.-]*")
    private val versionPattern = Regex("\\d+\\.\\d+\\.\\d+(?:[-+][A-Za-z0-9.-]+)?")

    fun validate(profile: PresentationProfileDescriptor): PresentationProfileValidationResult {
        val diagnostics = mutableListOf<PresentationProfileDiagnostic>()

        if (!profileIdPattern.matches(profile.profileId.value)) {
            diagnostics += diagnostic(
                code = "package.presentation-profile.identity.invalid",
                subject = "profileId",
                message = "Presentation Profile id must be stable and non-blank.",
            )
        }

        if (!versionPattern.matches(profile.version.value)) {
            diagnostics += diagnostic(
                code = "package.presentation-profile.version.invalid",
                subject = "version",
                message = "Presentation Profile version must be semver-compatible.",
            )
        }

        if (profile.projectionContexts.isEmpty() || profile.projectionContexts.any { it.value.isBlank() }) {
            diagnostics += diagnostic(
                code = "package.presentation-profile.context.invalid",
                subject = "projectionContexts",
                message = "Presentation Profile must declare at least one projection context.",
            )
        }

        if (profile.styleProfile.value.isBlank()) {
            diagnostics += diagnostic(
                code = "package.presentation-profile.style.invalid",
                subject = "styleProfile",
                message = "Presentation Profile style profile must not be blank.",
            )
        }

        profile.compatibilityConstraints.forEachIndexed { index, constraint ->
            if (constraint.packageId.isBlank() || constraint.versionRange.isBlank()) {
                diagnostics += diagnostic(
                    code = "package.presentation-profile.compatibility.invalid",
                    subject = "compatibilityConstraints[$index]",
                    message = "Presentation Profile compatibility constraint must include package id and version range.",
                )
            }
        }

        if (profile.provenance.sources.isEmpty() || profile.provenance.reviewedBy.isBlank()) {
            diagnostics += diagnostic(
                code = "package.presentation-profile.provenance.missing",
                subject = "provenance",
                message = "Presentation Profile provenance must declare at least one source and reviewer.",
            )
        }

        profile.policyFacts.forEachIndexed { index, fact ->
            if (fact.value.isBlank()) {
                diagnostics += diagnostic(
                    code = "package.presentation-profile.policy-fact.invalid",
                    subject = "policyFacts[$index]",
                    message = "Presentation Profile policy fact value must not be blank.",
                )
            }
        }

        profile.forbiddenAuthorityFields.forEach { field ->
            diagnostics += diagnostic(
                code = "package.presentation-profile.authority-forbidden",
                subject = field.field,
                message = "Presentation Profile must not contain ${field.authority.name.lowercase()} authority fields.",
            )
        }

        return PresentationProfileValidationResult(diagnostics)
    }

    private fun diagnostic(
        code: String,
        subject: String,
        message: String,
    ): PresentationProfileDiagnostic = PresentationProfileDiagnostic(
        code = PresentationProfileDiagnosticCode(code),
        severity = PresentationProfileDiagnosticSeverity.ERROR,
        subject = subject,
        message = message,
    )
}
