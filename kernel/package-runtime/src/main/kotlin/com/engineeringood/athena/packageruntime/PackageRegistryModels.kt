package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.EngineeringPackageId
import java.nio.file.Path

enum class PackageRegistryRootKind(val priority: Int) {
    PROJECT_LOCAL(0),
    ATHENA_OWNED(100),
}

data class LocalPackageRegistryRoot(
    val path: Path,
    val kind: PackageRegistryRootKind,
) {
    fun normalized(): LocalPackageRegistryRoot = copy(path = normalizePath(path))
}

enum class DuplicatePackageIdPolicy {
    DIAGNOSE_AMBIGUITY,
    PREFER_HIGHEST_PRIORITY_ROOT,
}

data class LocalPackageRegistryPolicy(
    val duplicatePackageIdPolicy: DuplicatePackageIdPolicy,
)

data class LocalPackageCandidate(
    val packageId: EngineeringPackageId,
    val descriptorPath: Path,
)

data class SelectedLocalPackage(
    val packageId: EngineeringPackageId,
    val descriptorPath: Path,
    val root: LocalPackageRegistryRoot,
)

enum class PackageRegistryDiagnosticSeverity {
    ERROR,
    INFO,
}

@JvmInline
value class PackageRegistryDiagnosticCode(val wireValue: String) {
    override fun toString(): String = wireValue
}

data class PackageRegistryDiagnostic(
    val code: PackageRegistryDiagnosticCode,
    val severity: PackageRegistryDiagnosticSeverity,
    val subject: String,
    val message: String,
)

data class LocalPackageRegistryDiscoveryResult(
    val roots: List<LocalPackageRegistryRoot>,
    val ignoredWorkspaceFolders: List<Path>,
    val selectedPackages: List<SelectedLocalPackage>,
    val diagnostics: List<PackageRegistryDiagnostic>,
) {
    val isValid: Boolean
        get() = diagnostics.none { it.severity == PackageRegistryDiagnosticSeverity.ERROR }
}

internal fun normalizePath(path: Path): Path = path.toAbsolutePath().normalize()

internal fun stablePathKey(path: Path): String = path.toString().replace('\\', '/')
