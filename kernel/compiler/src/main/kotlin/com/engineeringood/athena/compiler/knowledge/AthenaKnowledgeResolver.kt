package com.engineeringood.athena.compiler.knowledge

import com.engineeringood.athena.plugin.AthenaCoreRuntime
import com.engineeringood.athena.plugin.AthenaCoreVersion
import java.nio.file.Path

/** Resolves reviewed governed knowledge packages into the inspectable compilation context used by one compiler run. */
class AthenaKnowledgeResolver(
    private val packageLoader: AthenaKnowledgePackageLoader = AthenaKnowledgePackageLoader(),
    private val runtime: AthenaCoreRuntime = AthenaCoreRuntime.current(),
) {
    /** Resolves the packages from [source] into deterministic candidate, active, and rejected views. */
    fun resolve(source: AthenaKnowledgePackageSource): AthenaCompilationKnowledgeContext {
        if (source.packageRoots.isEmpty()) {
            return AthenaCompilationKnowledgeContext.empty(source)
        }

        val normalizedRoots = source.packageRoots.map(::normalizeRoot).distinctBy(::stablePathKey).sortedBy(::stablePathKey)
        val candidates = mutableListOf<AthenaKnowledgeCandidatePackage>()
        val activeArtifacts = mutableListOf<AthenaActiveKnowledgeArtifact>()
        val rejectedPackages = mutableListOf<AthenaRejectedKnowledgePackage>()

        normalizedRoots.forEach { packageRoot ->
            val loadResult = packageLoader.load(packageRoot)
            val loadedPackage = loadResult.loadedPackage

            if (!loadResult.isValid || loadedPackage == null) {
                rejectedPackages += AthenaRejectedKnowledgePackage(
                    packageRoot = packageRoot,
                    artifactId = loadedPackage?.manifest?.artifactId,
                    artifactVersion = loadedPackage?.manifest?.artifactVersion,
                    artifactKind = loadedPackage?.manifest?.artifactKind,
                    diagnostics = loadResult.diagnostics,
                )
                return@forEach
            }

            val candidate = AthenaKnowledgeCandidatePackage(
                packageRoot = packageRoot,
                artifactPackage = loadedPackage,
            )
            candidates += candidate

            if (isCompatible(loadedPackage.manifest.coreCompatibility)) {
                activeArtifacts += AthenaActiveKnowledgeArtifact(
                    packageRoot = packageRoot,
                    artifactId = loadedPackage.manifest.artifactId,
                    artifactKind = loadedPackage.manifest.artifactKind,
                    artifactVersion = loadedPackage.manifest.artifactVersion,
                    provenance = loadedPackage.manifest.provenance,
                )
            } else {
                rejectedPackages += AthenaRejectedKnowledgePackage(
                    packageRoot = packageRoot,
                    artifactId = loadedPackage.manifest.artifactId,
                    artifactVersion = loadedPackage.manifest.artifactVersion,
                    artifactKind = loadedPackage.manifest.artifactKind,
                    diagnostics = listOf(incompatibleRuntimeDiagnostic(loadedPackage.manifest.coreCompatibility)),
                )
            }
        }

        return AthenaCompilationKnowledgeContext(
            source = AthenaKnowledgePackageSource(normalizedRoots),
            candidates = candidates.sortedWith(compareBy(
                { it.artifactPackage.manifest.artifactId },
                { it.artifactPackage.manifest.artifactVersion },
                { stablePathKey(it.packageRoot) },
            )),
            activeArtifacts = activeArtifacts.sortedWith(compareBy(
                AthenaActiveKnowledgeArtifact::artifactId,
                AthenaActiveKnowledgeArtifact::artifactVersion,
                { stablePathKey(it.packageRoot) },
            )),
            rejectedPackages = rejectedPackages.sortedWith(compareBy(
                { it.artifactId == null },
                { it.artifactId ?: "" },
                { it.artifactVersion ?: "" },
                { stablePathKey(it.packageRoot) },
            )),
        )
    }

    private fun isCompatible(coreCompatibility: AthenaKnowledgeCoreCompatibilityRange): Boolean {
        val currentVersion = runtime.version
        val minimumVersion = AthenaCoreVersion.parse(coreCompatibility.minimumInclusive)
            ?: return false
        val maximumVersion = coreCompatibility.maximumInclusive?.let(AthenaCoreVersion::parse)

        if (currentVersion < minimumVersion) {
            return false
        }

        if (maximumVersion != null && currentVersion > maximumVersion) {
            return false
        }

        return true
    }

    private fun incompatibleRuntimeDiagnostic(coreCompatibility: AthenaKnowledgeCoreCompatibilityRange): AthenaKnowledgePackageDiagnostic {
        val declaredRange = if (coreCompatibility.maximumInclusive == null) {
            "${coreCompatibility.minimumInclusive}+"
        } else {
            "${coreCompatibility.minimumInclusive}..${coreCompatibility.maximumInclusive}"
        }

        return AthenaKnowledgePackageDiagnostic(
            severity = AthenaKnowledgePackageSeverity.ERROR,
            ruleId = AthenaKnowledgePackageRuleId("knowledge.package.compatibility.unsupported-core"),
            subject = "compatibility.core",
            message = "Governed knowledge package is not compatible with Athena core `${runtime.version}`; declared range is `$declaredRange`.",
        )
    }
}

private fun normalizeRoot(path: Path): Path = path.toAbsolutePath().normalize()

private fun stablePathKey(path: Path): String = path.toString().replace('\\', '/')
