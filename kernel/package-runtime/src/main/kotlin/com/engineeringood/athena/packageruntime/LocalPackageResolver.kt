package com.engineeringood.athena.packageruntime

class LocalPackageResolver {
    fun resolve(request: PackageResolutionRequest): PackageResolutionResult {
        val roots = request.roots
            .map(LocalPackageRegistryRoot::normalized)
            .distinctBy { stablePathKey(it.path) }
            .sortedWith(compareBy({ it.kind.priority }, { stablePathKey(it.path) }))

        val candidates = request.candidates
            .mapNotNull { candidate -> candidate.withOwningRoot(roots) }
            .groupBy { it.kind to it.packageId }

        val diagnostics = mutableListOf<PackageResolutionDiagnostic>()
        val resolved = mutableListOf<ResolvedPackageFact>()

        request.requirements
            .distinctBy { it.kind to it.packageId }
            .forEach { requirement ->
                val matches = candidates[requirement.kind to requirement.packageId].orEmpty()
                    .sortedWith(compareBy({ it.selectedRoot.kind.priority }, { stablePathKey(it.descriptorPath) }))

                when {
                    matches.isEmpty() -> diagnostics += diagnostic(
                        code = "package.resolution.missing",
                        subject = requirement.packageId,
                        message = "Required package was not found in governed registry roots.",
                    )

                    matches.size > 1 -> diagnostics += diagnostic(
                        code = "package.resolution.ambiguous",
                        subject = requirement.packageId,
                        message = "Required package has multiple matching candidates.",
                    )

                    !matches.single().candidate.compatible -> diagnostics += diagnostic(
                        code = "package.resolution.incompatible",
                        subject = requirement.packageId,
                        message = "Required package is not compatible with the active resolver policy.",
                    )

                    matches.single().candidate.validationDiagnostics.any {
                        it.severity == PackageResolutionDiagnosticSeverity.ERROR
                    } -> diagnostics += diagnostic(
                        code = "package.resolution.invalid",
                        subject = requirement.packageId,
                        message = "Required package has validation diagnostics.",
                    )

                    else -> resolved += matches.single().resolvedPackage.toFact()
                }
            }

        val finalResolved = if (diagnostics.any { it.severity == PackageResolutionDiagnosticSeverity.ERROR }) {
            emptyList()
        } else {
            resolved.sortedWith(compareBy({ it.kind.ordinal }, { it.packageId }, { stablePathKey(it.descriptorPath) }))
        }

        return PackageResolutionResult(
            resolvedPackages = finalResolved,
            diagnostics = diagnostics,
            rendererFallbackUsed = false,
        )
    }

    private fun PackageDescriptorCandidate.withOwningRoot(
        roots: List<LocalPackageRegistryRoot>,
    ): PackageResolutionCandidate? {
        val normalizedDescriptorPath = normalizePath(descriptorPath)
        val root = roots.firstOrNull { normalizedDescriptorPath.startsWith(it.path) } ?: return null
        return PackageResolutionCandidate(
            candidate = this,
            resolvedPackage = ResolvedPackageFact(
                packageId = packageId,
                kind = kind,
                version = version,
                descriptorPath = normalizedDescriptorPath,
                dependencies = dependencies.sortedWith(compareBy({ it.kind.ordinal }, { it.packageId }, { it.version })),
                validationStatus = if (validationDiagnostics.any {
                        it.severity == PackageResolutionDiagnosticSeverity.ERROR
                    }
                ) {
                    PackageValidationStatus.INVALID
                } else {
                    PackageValidationStatus.VALID
                },
                diagnostics = validationDiagnostics,
                selectedRoot = root,
            ),
        )
    }

    private fun ResolvedPackageFact.toFact(): ResolvedPackageFact = copy(
        diagnostics = diagnostics.sortedWith(compareBy({ it.code.wireValue }, { it.subject })),
    )

    private fun diagnostic(
        code: String,
        subject: String,
        message: String,
    ): PackageResolutionDiagnostic = PackageResolutionDiagnostic(
        code = PackageResolutionDiagnosticCode(code),
        severity = PackageResolutionDiagnosticSeverity.ERROR,
        subject = subject,
        message = message,
    )

    private data class PackageResolutionCandidate(
        val candidate: PackageDescriptorCandidate,
        val resolvedPackage: ResolvedPackageFact,
    ) {
        val packageId: String
            get() = candidate.packageId
        val kind: PackageResolutionPackageKind
            get() = candidate.kind
        val descriptorPath: java.nio.file.Path
            get() = resolvedPackage.descriptorPath
        val selectedRoot: LocalPackageRegistryRoot
            get() = resolvedPackage.selectedRoot
    }
}
