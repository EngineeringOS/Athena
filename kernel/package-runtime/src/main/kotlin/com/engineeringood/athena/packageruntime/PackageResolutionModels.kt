package com.engineeringood.athena.packageruntime

import java.nio.file.Path

enum class PackageResolutionPackageKind {
    ENGINEERING,
    REPRESENTATION,
}

data class PackageRequirement(
    val packageId: String,
    val kind: PackageResolutionPackageKind,
)

data class PackageDependency(
    val packageId: String,
    val kind: PackageResolutionPackageKind,
    val version: String,
)

data class PackageDescriptorCandidate(
    val packageId: String,
    val kind: PackageResolutionPackageKind,
    val version: String,
    val descriptorPath: Path,
    val dependencies: List<PackageDependency> = emptyList(),
    val compatible: Boolean = true,
    val validationDiagnostics: List<PackageResolutionDiagnostic> = emptyList(),
)

data class PackageResolutionRequest(
    val roots: List<LocalPackageRegistryRoot>,
    val requirements: List<PackageRequirement>,
    val candidates: List<PackageDescriptorCandidate>,
)

enum class PackageResolutionDiagnosticSeverity {
    ERROR,
}

@JvmInline
value class PackageResolutionDiagnosticCode(val wireValue: String) {
    override fun toString(): String = wireValue
}

data class PackageResolutionDiagnostic(
    val code: PackageResolutionDiagnosticCode,
    val severity: PackageResolutionDiagnosticSeverity,
    val subject: String,
    val message: String,
)

data class ResolvedPackageFact(
    val packageId: String,
    val kind: PackageResolutionPackageKind,
    val version: String,
    val descriptorPath: Path,
    val dependencies: List<PackageDependency>,
    val validationStatus: PackageValidationStatus,
    val diagnostics: List<PackageResolutionDiagnostic>,
    val selectedRoot: LocalPackageRegistryRoot,
)

enum class PackageValidationStatus {
    VALID,
    INVALID,
}

data class PackageResolutionResult(
    val resolvedPackages: List<ResolvedPackageFact>,
    val diagnostics: List<PackageResolutionDiagnostic>,
    val rendererFallbackUsed: Boolean = false,
) {
    val isValid: Boolean
        get() = diagnostics.none { it.severity == PackageResolutionDiagnosticSeverity.ERROR } && !rendererFallbackUsed
}
