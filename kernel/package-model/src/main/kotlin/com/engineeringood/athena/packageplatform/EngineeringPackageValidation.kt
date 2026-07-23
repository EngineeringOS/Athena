package com.engineeringood.athena.packageplatform

enum class EngineeringPackageDiagnosticSeverity {
    ERROR,
}

@JvmInline
value class EngineeringPackageDiagnosticCode(val wireValue: String) {
    override fun toString(): String = wireValue
}

data class EngineeringPackageDiagnostic(
    val code: EngineeringPackageDiagnosticCode,
    val severity: EngineeringPackageDiagnosticSeverity,
    val subject: String,
    val message: String,
)

data class EngineeringPackageValidationResult(
    val diagnostics: List<EngineeringPackageDiagnostic>,
) {
    val isValid: Boolean
        get() = diagnostics.none { it.severity == EngineeringPackageDiagnosticSeverity.ERROR }
}

object EngineeringPackageDescriptorValidator {
    private val packageIdPattern = Regex("[a-z][a-z0-9]*(\\.[a-z][a-z0-9-]*)+")
    private val groupIdPattern = Regex("[a-z][a-z0-9]*(\\.[a-z][a-z0-9-]*)+")
    private val artifactIdPattern = Regex("[a-z][a-z0-9]*(?:-[a-z0-9]+)*")
    private val versionPattern = Regex("\\d+\\.\\d+\\.\\d+(?:[-+][A-Za-z0-9.-]+)?")

    fun validate(descriptor: EngineeringPackageDescriptor): EngineeringPackageValidationResult {
        val diagnostics = mutableListOf<EngineeringPackageDiagnostic>()

        if (!packageIdPattern.matches(descriptor.packageId.value)) {
            diagnostics += diagnostic(
                code = "package.engineering.identity.invalid",
                subject = "packageId",
                message = "Engineering package id must be a stable reverse-domain identifier.",
            )
        }

        if (!groupIdPattern.matches(descriptor.coordinates.groupId.value)) {
            diagnostics += diagnostic(
                code = "package.engineering.group.invalid",
                subject = "coordinates.groupId",
                message = "Engineering package group id must be a stable reverse-domain identifier.",
            )
        }

        if (!artifactIdPattern.matches(descriptor.coordinates.artifactId.value)) {
            diagnostics += diagnostic(
                code = "package.engineering.artifact.invalid",
                subject = "coordinates.artifactId",
                message = "Engineering package artifact id must be a non-blank kebab-case identifier.",
            )
        }

        if (!versionPattern.matches(descriptor.coordinates.version.value)) {
            diagnostics += diagnostic(
                code = "package.engineering.version.invalid",
                subject = "coordinates.version",
                message = "Engineering package version must be semver-compatible.",
            )
        }

        if (descriptor.kind == EngineeringPackageKind.UNSPECIFIED) {
            diagnostics += diagnostic(
                code = "package.engineering.kind.missing",
                subject = "kind",
                message = "Engineering package kind must be specified.",
            )
        }

        descriptor.concepts.forEachIndexed { index, concept ->
            if (concept.conceptId.value.isBlank()) {
                diagnostics += diagnostic(
                    code = "package.engineering.concept.unsupported",
                    subject = "concepts[$index].conceptId",
                    message = "Engineering concept id must not be blank.",
                )
            }

            concept.parameters.forEachIndexed { parameterIndex, parameter ->
                if (parameter.parameterId.value.isBlank()) {
                    diagnostics += diagnostic(
                        code = "package.engineering.parameter.invalid",
                        subject = "concepts[$index].parameters[$parameterIndex].parameterId",
                        message = "Engineering parameter id must not be blank.",
                    )
                }
            }
        }

        if (descriptor.provenance.sources.isEmpty() || descriptor.provenance.reviewedBy.isBlank()) {
            diagnostics += diagnostic(
                code = "package.engineering.provenance.missing",
                subject = "provenance",
                message = "Engineering package provenance must declare at least one source and reviewer.",
            )
        }

        descriptor.forbiddenAuthorityFields.forEach { field ->
            diagnostics += diagnostic(
                code = forbiddenAuthorityCode(field.authority),
                subject = field.field,
                message = "Engineering package descriptors must not contain ${field.authority.name.lowercase()} authority fields.",
            )
        }

        return EngineeringPackageValidationResult(diagnostics)
    }

    private fun forbiddenAuthorityCode(authority: EngineeringPackageAuthority): String = when (authority) {
        EngineeringPackageAuthority.REPRESENTATION,
        EngineeringPackageAuthority.GRAPHIC_RESOURCE,
        EngineeringPackageAuthority.RENDERER -> "package.engineering.representation-field-forbidden"
        EngineeringPackageAuthority.PRESENTATION -> "package.engineering.presentation-field-forbidden"
        EngineeringPackageAuthority.SOURCE_MUTATION -> "package.engineering.source-mutation-field-forbidden"
    }

    private fun diagnostic(
        code: String,
        subject: String,
        message: String,
    ): EngineeringPackageDiagnostic = EngineeringPackageDiagnostic(
        code = EngineeringPackageDiagnosticCode(code),
        severity = EngineeringPackageDiagnosticSeverity.ERROR,
        subject = subject,
        message = message,
    )
}
