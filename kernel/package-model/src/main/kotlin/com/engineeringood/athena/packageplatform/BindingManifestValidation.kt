package com.engineeringood.athena.packageplatform

enum class BindingManifestDiagnosticSeverity {
    ERROR,
}

@JvmInline
value class BindingManifestDiagnosticCode(val wireValue: String) {
    override fun toString(): String = wireValue
}

data class BindingManifestDiagnostic(
    val code: BindingManifestDiagnosticCode,
    val severity: BindingManifestDiagnosticSeverity,
    val subject: String,
    val message: String,
)

data class BindingManifestValidationResult(
    val diagnostics: List<BindingManifestDiagnostic>,
) {
    val isValid: Boolean
        get() = diagnostics.none { it.severity == BindingManifestDiagnosticSeverity.ERROR }
}

object BindingManifestValidator {
    fun validate(manifest: BindingManifest): BindingManifestValidationResult {
        val diagnostics = mutableListOf<BindingManifestDiagnostic>()

        if (manifest.manifestId.value.isBlank()) {
            diagnostics += diagnostic("package.binding-manifest.identity.invalid", "manifestId", "Binding Manifest id must not be blank.")
        }
        if (manifest.engineeringPackageId.isBlank() || manifest.engineeringPackageVersionRange.isBlank()) {
            diagnostics += diagnostic(
                "package.binding-manifest.engineering-package.invalid",
                "engineeringPackage",
                "Binding Manifest must name an engineering package id and version range.",
            )
        }
        if (manifest.conceptId.value.isBlank()) {
            diagnostics += diagnostic("package.binding-manifest.concept.invalid", "conceptId", "Binding Manifest concept id must not be blank.")
        }
        if (manifest.defaultRepresentationPackageId.isBlank()) {
            diagnostics += diagnostic(
                "package.binding-manifest.representation-package.invalid",
                "defaultRepresentationPackageId",
                "Binding Manifest must name a default representation package.",
            )
        }
        if (manifest.provenance.sources.isEmpty() || manifest.provenance.reviewedBy.isBlank()) {
            diagnostics += diagnostic("package.binding-manifest.provenance.missing", "provenance", "Binding Manifest provenance must declare source and reviewer.")
        }
        manifest.forbiddenAuthorityFields.forEach { field ->
            diagnostics += diagnostic(
                "package.binding-manifest.authority-forbidden",
                field.field,
                "Binding Manifest must not contain ${field.authority.name.lowercase()} authority fields.",
            )
        }

        return BindingManifestValidationResult(diagnostics)
    }

    private fun diagnostic(
        code: String,
        subject: String,
        message: String,
    ): BindingManifestDiagnostic = BindingManifestDiagnostic(
        code = BindingManifestDiagnosticCode(code),
        severity = BindingManifestDiagnosticSeverity.ERROR,
        subject = subject,
        message = message,
    )
}
