package com.engineeringood.athena.repository

/**
 * Identifies whether a repository contract artifact is authored intent or derived state.
 *
 * M5 keeps this distinction explicit so downstream tooling does not confuse `athena.yaml`
 * with the derived lock and resolution surfaces that Athena materializes later.
 */
enum class RepositoryArtifactRole {
    /** Authored repository and package intent. */
    AUTHORED_INTENT,

    /** Derived state emitted from Athena-owned resolution flows. */
    DERIVED_STATE,
}

/** Common marker for typed repository contract artifacts. */
sealed interface RepositoryArtifact {
    /** States whether the artifact represents authored intent or derived state. */
    val artifactRole: RepositoryArtifactRole
}

/**
 * Stable package identity used by repository manifest, lock, dependency, and graph contracts.
 *
 * The type remains vendor-neutral and deliberately avoids source-control semantics.
 */
data class PackageIdentifier(
    val name: String,
    val version: String? = null,
)

/**
 * Declares the one primary package owned by an M5 Engineering Repository.
 *
 * M5 keeps one primary package per repository and one governed authored source root under `src/`.
 */
data class PrimaryPackage(
    val id: PackageIdentifier,
    val sourceRoot: String = "src",
)

/**
 * Enumerates supported dependency declaration kinds for the first local-first M5 contract cut.
 *
 * Remote registry and Git-backed dependency sources are intentionally deferred.
 */
enum class PackageDependencySource {
    /** Dependency declared against another locally reachable package path or checked-in package root. */
    LOCAL_PATH,

    /** Dependency declared against a local package identity already known in the repository context. */
    LOCAL_PACKAGE,
}

/**
 * Typed dependency declaration owned by the repository manifest.
 *
 * This shape records authored dependency intent only. It is not a resolved package edge.
 */
data class PackageDependency(
    val packageId: PackageIdentifier,
    val source: PackageDependencySource = PackageDependencySource.LOCAL_PATH,
    val locator: String? = null,
)

/**
 * Authored repository-root manifest contract represented by `athena.yaml`.
 *
 * The manifest declares the repository's primary package and authored dependency intent.
 */
data class RepositoryManifest(
    val primaryPackage: PrimaryPackage,
    val dependencies: List<PackageDependency> = emptyList(),
) : RepositoryArtifact {
    override val artifactRole: RepositoryArtifactRole = RepositoryArtifactRole.AUTHORED_INTENT
}

/**
 * Derived lock contract represented by `athena.lock`.
 *
 * The lock records the reproducibility-critical package resolution result in a stable typed form.
 */
data class RepositoryLock(
    val version: Int = 1,
    val primaryPackage: PackageIdentifier,
    val packages: List<ResolvedPackage> = emptyList(),
) : RepositoryArtifact {
    override val artifactRole: RepositoryArtifactRole = RepositoryArtifactRole.DERIVED_STATE
}

/**
 * Typed repository aggregate used by compiler, runtime, and IDE adapters when they need one
 * stable repository/package contract boundary.
 */
data class EngineeringRepository(
    val manifest: RepositoryManifest,
    val lock: RepositoryLock? = null,
)

/**
 * One resolved package node in the canonical package graph.
 *
 * This shape records resolved dependency meaning rather than authored dependency declarations.
 */
data class ResolvedPackage(
    val packageId: PackageIdentifier,
    val sourceRoot: String,
    val directDependencies: List<PackageIdentifier> = emptyList(),
)

/**
 * One normalized dependency request carried into the package-resolution pipeline.
 *
 * This remains pre-resolution state derived from authored manifest intent, not a resolved graph edge.
 */
data class RepositoryResolutionDependency(
    val packageId: PackageIdentifier,
    val source: PackageDependencySource,
    val locator: String? = null,
)

/**
 * Deterministic package-resolution input derived from one governed repository contract.
 *
 * Later M5 stories consume this input to build the canonical package graph and lock state.
 */
data class RepositoryResolutionInput(
    val rootPackage: PackageIdentifier,
    val rootSourcePath: String,
    val dependencies: List<RepositoryResolutionDependency> = emptyList(),
)

/**
 * Canonical resolved package graph owned by Athena semantic flows.
 *
 * The graph remains independent from UI state, storage vendor mechanics, or classpath coincidence.
 */
data class ResolvedPackageGraph(
    val rootPackage: PackageIdentifier,
    val packages: List<ResolvedPackage> = emptyList(),
)

/** Severity used by repository and package diagnostics. */
enum class RepositoryDiagnosticSeverity {
    ERROR,
    WARNING,
    INFO,
}

/** Inspectable repository/package diagnostic published by semantic flows. */
data class RepositoryDiagnostic(
    val code: String,
    val message: String,
    val severity: RepositoryDiagnosticSeverity = RepositoryDiagnosticSeverity.ERROR,
)

/**
 * Inspectable report shape that packages the current repository contract, resolved graph, and diagnostics together.
 *
 * Later M5 stories can populate this report from validation and resolver execution without redefining the contract types.
 */
data class RepositoryGraphReport(
    val repository: EngineeringRepository,
    val graph: ResolvedPackageGraph? = null,
    val diagnostics: List<RepositoryDiagnostic> = emptyList(),
)
