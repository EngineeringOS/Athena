package com.engineeringood.athena.compiler.knowledge

import com.engineeringood.athena.component.ResolvedComponentDefinition
import com.engineeringood.athena.connection.ResolvedSemanticPortDefinition
import com.engineeringood.athena.part.ResolvedPartImplementation
import com.engineeringood.athena.physical.ResolvedPhysicalTraitDefinition
import com.engineeringood.athena.repository.PackageIdentifier
import java.nio.file.Path

/**
 * One package-governed knowledge-pack registry entry.
 *
 * The entry is keyed by repository-package identity plus knowledge-artifact identity rather than
 * frontend state or filesystem discovery order.
 */
data class AthenaKnowledgePackRegistryEntry(
    val packageId: PackageIdentifier,
    val packageRoot: Path,
    val artifactId: String,
    val artifactKind: AthenaKnowledgeArtifactKind,
    val artifactVersion: String,
)

/**
 * Deterministic registry of knowledge packs admitted by one governed repository graph.
 */
data class AthenaKnowledgePackRegistry(
    val repositoryRootPackage: PackageIdentifier,
    val entries: List<AthenaKnowledgePackRegistryEntry>,
) {
    companion object {
        /**
         * Builds a deterministic registry by sorting entries in stable package/artifact order.
         */
        fun canonical(
            repositoryRootPackage: PackageIdentifier,
            entries: List<AthenaKnowledgePackRegistryEntry>,
        ): AthenaKnowledgePackRegistry {
            return AthenaKnowledgePackRegistry(
                repositoryRootPackage = repositoryRootPackage,
                entries = entries.sortedWith(knowledgePackEntryComparator()),
            )
        }
    }
}

/**
 * Deterministic active knowledge-pack set selected from the governed registry for one compilation run.
 */
data class AthenaActiveKnowledgePackSet(
    val repositoryRootPackage: PackageIdentifier,
    val entries: List<AthenaKnowledgePackRegistryEntry>,
) {
    companion object {
        /**
         * Builds a deterministic active set by sorting entries in the same stable order as the
         * governed registry.
         */
        fun canonical(
            repositoryRootPackage: PackageIdentifier,
            entries: List<AthenaKnowledgePackRegistryEntry>,
        ): AthenaActiveKnowledgePackSet {
            return AthenaActiveKnowledgePackSet(
                repositoryRootPackage = repositoryRootPackage,
                entries = entries.sortedWith(knowledgePackEntryComparator()),
            )
        }
    }
}

/** Compiler-owned source model that lists the local governed knowledge package roots to evaluate for one compilation run. */
data class AthenaKnowledgePackageSource(
    val packageRoots: List<Path>,
    val activePackSet: AthenaActiveKnowledgePackSet? = null,
) {
    companion object {
        /** Returns an empty governed knowledge source when no reviewed packages are configured for the compilation run. */
        fun empty(): AthenaKnowledgePackageSource = AthenaKnowledgePackageSource(emptyList())

        /**
         * Builds a governed package source from an explicit package-governed active pack set rather
         * than filesystem discovery order.
         */
        fun fromActivePackSet(activePackSet: AthenaActiveKnowledgePackSet): AthenaKnowledgePackageSource {
            return AthenaKnowledgePackageSource(
                packageRoots = activePackSet.entries.map(AthenaKnowledgePackRegistryEntry::packageRoot),
                activePackSet = activePackSet,
            )
        }
    }
}

/** Valid governed knowledge package candidate loaded successfully and eligible for compatibility evaluation. */
data class AthenaKnowledgeCandidatePackage(
    val packageRoot: Path,
    val artifactPackage: AthenaKnowledgeArtifactPackage,
)

/** Inspectable active governed knowledge artifact admitted into the effective compilation context. */
data class AthenaActiveKnowledgeArtifact(
    val packageRoot: Path,
    val artifactId: String,
    val artifactKind: AthenaKnowledgeArtifactKind,
    val artifactVersion: String,
    val provenance: AthenaKnowledgeProvenance,
)

/** Inspectable rejected governed knowledge package with the responsible package identity hint and rejection diagnostics. */
data class AthenaRejectedKnowledgePackage(
    val packageRoot: Path,
    val artifactId: String?,
    val artifactVersion: String?,
    val artifactKind: AthenaKnowledgeArtifactKind?,
    val diagnostics: List<AthenaKnowledgePackageDiagnostic>,
)

/** Effective governed knowledge context attached to compiler-facing results for one compilation run. */
data class AthenaCompilationKnowledgeContext(
    val source: AthenaKnowledgePackageSource,
    val candidates: List<AthenaKnowledgeCandidatePackage>,
    val activeArtifacts: List<AthenaActiveKnowledgeArtifact>,
    val rejectedPackages: List<AthenaRejectedKnowledgePackage>,
    val componentKnowledgeContributors: List<String> = emptyList(),
    val activeComponentConceptCount: Int = 0,
    val activeComponentImplementationCount: Int = 0,
    val resolvedComponents: List<ResolvedComponentDefinition> = emptyList(),
    val resolvedImplementations: List<ResolvedPartImplementation> = emptyList(),
    val resolvedSemanticPorts: List<ResolvedSemanticPortDefinition> = emptyList(),
    val resolvedPhysicalTraits: List<ResolvedPhysicalTraitDefinition> = emptyList(),
    val componentKnowledgeDiagnostics: List<AthenaComponentKnowledgeDiagnostic> = emptyList(),
) {
    /** Returns one resolved component definition for [semanticId] when available. */
    fun resolvedComponent(semanticId: String): ResolvedComponentDefinition? {
        return resolvedComponents.firstOrNull { resolved -> resolved.semanticSubjectId.value == semanticId }
    }

    /** Returns resolved semantic-port definitions owned by [ownerSemanticId] in deterministic order. */
    fun resolvedSemanticPortsForOwner(ownerSemanticId: String): List<ResolvedSemanticPortDefinition> {
        return resolvedSemanticPorts.filter { resolved -> resolved.ownerSemanticId.value == ownerSemanticId }
    }

    /** Returns one resolved physical-trait definition for [semanticId] when available. */
    fun resolvedPhysicalTrait(semanticId: String): ResolvedPhysicalTraitDefinition? {
        return resolvedPhysicalTraits.firstOrNull { resolved -> resolved.semanticSubjectId.value == semanticId }
    }

    /** Returns a copy enriched with compiler-owned resolved component knowledge. */
    fun withResolvedComponentKnowledge(snapshot: AthenaResolvedComponentKnowledgeSnapshot): AthenaCompilationKnowledgeContext {
        return copy(
            componentKnowledgeContributors = snapshot.contributingArtifactIds,
            activeComponentConceptCount = snapshot.activeConceptCount,
            activeComponentImplementationCount = snapshot.activeImplementationCount,
            resolvedComponents = snapshot.resolvedComponents,
            resolvedImplementations = snapshot.resolvedImplementations,
            resolvedSemanticPorts = snapshot.resolvedSemanticPorts,
            resolvedPhysicalTraits = snapshot.resolvedPhysicalTraits,
            componentKnowledgeDiagnostics = snapshot.diagnostics,
        )
    }

    companion object {
        /** Returns an empty governed knowledge context for compiler runs that do not evaluate any reviewed packages. */
        fun empty(
            source: AthenaKnowledgePackageSource = AthenaKnowledgePackageSource.empty(),
        ): AthenaCompilationKnowledgeContext {
            return AthenaCompilationKnowledgeContext(
                source = source,
                candidates = emptyList(),
                activeArtifacts = emptyList(),
                rejectedPackages = emptyList(),
            )
        }
    }
}

private fun knowledgePackEntryComparator(): Comparator<AthenaKnowledgePackRegistryEntry> {
    return compareBy<AthenaKnowledgePackRegistryEntry>(
        { entry -> entry.packageId.name },
        { entry -> entry.packageId.version.orEmpty() },
        { entry -> entry.artifactId },
        { entry -> entry.artifactVersion },
        { entry -> entry.packageRoot.toAbsolutePath().normalize().toString() },
    )
}
