package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.EngineeringPackageId
import java.nio.file.Path

class LocalPackageRegistry(
    roots: List<LocalPackageRegistryRoot>,
    private val workspaceFolders: List<Path> = emptyList(),
    private val packageCandidates: List<LocalPackageCandidate> = emptyList(),
    private val policy: LocalPackageRegistryPolicy = LocalPackageRegistryPolicy(
        DuplicatePackageIdPolicy.DIAGNOSE_AMBIGUITY,
    ),
) {
    private val configuredRoots: List<LocalPackageRegistryRoot> = roots
        .map(LocalPackageRegistryRoot::normalized)
        .distinctBy { stablePathKey(it.path) }
        .sortedWith(compareBy({ it.kind.priority }, { stablePathKey(it.path) }))

    fun discover(): LocalPackageRegistryDiscoveryResult {
        val diagnostics = mutableListOf<PackageRegistryDiagnostic>()
        val ignoredWorkspaceFolders = workspaceFolders
            .map(::normalizePath)
            .filterNot { workspaceFolder -> configuredRoots.any { it.path == workspaceFolder } }
            .sortedBy(::stablePathKey)

        val candidatesByPackage = packageCandidates
            .mapNotNull(::candidateWithRoot)
            .groupBy { it.packageId }

        val selectedPackages = candidatesByPackage.flatMap { (packageId, candidates) ->
            selectPackage(packageId, candidates.sortedByRootPriority(), diagnostics)
        }.sortedWith(compareBy({ it.packageId.value }, { stablePathKey(it.descriptorPath) }))

        return LocalPackageRegistryDiscoveryResult(
            roots = configuredRoots,
            ignoredWorkspaceFolders = ignoredWorkspaceFolders,
            selectedPackages = selectedPackages,
            diagnostics = diagnostics,
        )
    }

    private fun candidateWithRoot(candidate: LocalPackageCandidate): SelectedLocalPackage? {
        val descriptorPath = normalizePath(candidate.descriptorPath)
        val owningRoot = configuredRoots.firstOrNull { root -> descriptorPath.startsWith(root.path) }
            ?: return null

        return SelectedLocalPackage(
            packageId = candidate.packageId,
            descriptorPath = descriptorPath,
            root = owningRoot,
        )
    }

    private fun selectPackage(
        packageId: EngineeringPackageId,
        candidates: List<SelectedLocalPackage>,
        diagnostics: MutableList<PackageRegistryDiagnostic>,
    ): List<SelectedLocalPackage> {
        if (candidates.size <= 1) {
            return candidates
        }

        return when (policy.duplicatePackageIdPolicy) {
            DuplicatePackageIdPolicy.DIAGNOSE_AMBIGUITY -> {
                diagnostics += diagnostic(
                    code = "package.registry.package-id.ambiguous",
                    severity = PackageRegistryDiagnosticSeverity.ERROR,
                    subject = packageId.value,
                    message = "Package id is declared by multiple governed registry roots.",
                )
                emptyList()
            }

            DuplicatePackageIdPolicy.PREFER_HIGHEST_PRIORITY_ROOT -> {
                val selected = candidates.first()
                diagnostics += diagnostic(
                    code = "package.registry.package-id.precedence-applied",
                    severity = PackageRegistryDiagnosticSeverity.INFO,
                    subject = packageId.value,
                    message = "Package id is declared by multiple roots; highest-priority governed root was selected.",
                )
                listOf(selected)
            }
        }
    }

    private fun List<SelectedLocalPackage>.sortedByRootPriority(): List<SelectedLocalPackage> = sortedWith(
        compareBy(
            { it.root.kind.priority },
            { stablePathKey(it.root.path) },
            { stablePathKey(it.descriptorPath) },
        ),
    )

    private fun diagnostic(
        code: String,
        severity: PackageRegistryDiagnosticSeverity,
        subject: String,
        message: String,
    ): PackageRegistryDiagnostic = PackageRegistryDiagnostic(
        code = PackageRegistryDiagnosticCode(code),
        severity = severity,
        subject = subject,
        message = message,
    )
}
