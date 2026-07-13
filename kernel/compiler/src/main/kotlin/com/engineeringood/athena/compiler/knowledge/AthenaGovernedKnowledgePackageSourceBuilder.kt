package com.engineeringood.athena.compiler.knowledge

import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.ResolvedPackageGraph

/**
 * Builds a governed knowledge-package source from the existing repository package graph.
 *
 * This builder keeps `athena.lock` and the resolved package graph as the reproducibility anchor. It
 * does not introduce a second canonical lockfile or ad hoc filesystem discovery path for active
 * knowledge packs.
 */
class AthenaGovernedKnowledgePackageSourceBuilder {
    /**
     * Selects only the registry entries whose package identities are present in the resolved graph.
     */
    fun build(
        graph: ResolvedPackageGraph,
        registry: AthenaKnowledgePackRegistry,
    ): AthenaKnowledgePackageSource {
        if (registry.repositoryRootPackage != graph.rootPackage) {
            return AthenaKnowledgePackageSource.empty()
        }

        val allowedPackageIds = graph.allowedPackageIds()
        val activePackSet = AthenaActiveKnowledgePackSet.canonical(
            repositoryRootPackage = graph.rootPackage,
            entries = registry.entries.filter { entry -> entry.packageId in allowedPackageIds },
        )
        return AthenaKnowledgePackageSource.fromActivePackSet(activePackSet)
    }
}

private fun ResolvedPackageGraph.allowedPackageIds(): Set<PackageIdentifier> {
    return buildSet {
        add(rootPackage)
        packages.forEach { resolvedPackage -> add(resolvedPackage.packageId) }
    }
}
