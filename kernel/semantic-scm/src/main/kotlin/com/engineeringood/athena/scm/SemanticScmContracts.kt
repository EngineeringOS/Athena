package com.engineeringood.athena.scm

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.RepositoryGraphReport
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticContinuationDecision
import com.engineeringood.athena.semantics.core.SemanticValidationResult

/**
 * Stable descriptor for one semantic comparison baseline.
 *
 * The baseline stays VCS-neutral and identifies a meaningful semantic checkpoint rather than
 * a provider-specific branch, commit, or workspace head.
 */
data class SemanticBaselineDescriptor(
    val baselineId: String,
    val label: String,
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * Repository-scoped semantic snapshot captured for later comparison and review flows.
 *
 * M6 keeps the snapshot contract narrow: repository/package meaning is mandatory, while canonical
 * engineering documents and validation state remain optional additive context.
 */
data class SemanticBaselineSnapshot(
    val descriptor: SemanticBaselineDescriptor,
    val repositoryReport: RepositoryGraphReport,
    val engineeringDocuments: List<EngineeringDocument> = emptyList(),
    val validationResult: SemanticValidationResult? = null,
    val diagnostics: List<SemanticDiagnostic> = emptyList(),
)

/**
 * Broad authored semantic change families that later diff and review flows can inspect.
 *
 * The taxonomy intentionally speaks in Athena semantic terms instead of vendor SCM concepts.
 */
enum class SemanticChangeCategory {
    REPOSITORY_CONTRACT_CHANGED,
    PACKAGE_DEPENDENCY_CHANGED,
    ENGINEERING_STRUCTURE_CHANGED,
    ENGINEERING_PROPERTY_CHANGED,
    CONNECTION_TOPOLOGY_CHANGED,
    VALIDATION_STATE_CHANGED,
    EXTENSION_SEMANTICS_CHANGED,
}

/**
 * Canonical semantic layer where one authored change originates.
 *
 * Keeping the layer explicit prevents later review surfaces from flattening repository,
 * engineering, projection, and extension meaning into one undifferentiated change list.
 */
enum class SemanticChangeLayer {
    REPOSITORY,
    PACKAGE,
    ENGINEERING,
    LAYOUT,
    GEOMETRY,
    VALIDATION,
    EXTENSION,
}

/**
 * One inspectable authored semantic change derived from baseline comparison.
 *
 * The record captures what the author intended to change. Downstream derived consequences are
 * modeled separately so review and commit preparation do not confuse intent with fallout.
 */
data class SemanticChangeRecord(
    val category: SemanticChangeCategory,
    val layer: SemanticChangeLayer,
    val message: String,
    val affectedPackage: PackageIdentifier? = null,
    val subjectIdentity: StableSemanticIdentity? = null,
    val provenance: SourceProvenance? = null,
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * Broad consequence families derived from authored semantic changes.
 *
 * These categories help later tooling explain why a semantic review or commit includes extra
 * actions that were not authored directly by the operator.
 */
enum class SemanticDerivedConsequenceType {
    REPOSITORY_CONTRACT_REVIEW_REQUIRED,
    LOCK_UPDATED,
    PACKAGE_GRAPH_RECOMPUTED,
    VALIDATION_DELTA_DETECTED,
    COMPARISON_INPUT_INCOMPLETE,
    VALIDATION_RECHECK_REQUIRED,
    PROJECTION_REFRESH_REQUIRED,
    RUNTIME_REPLAY_REQUIRED,
}

/**
 * One inspectable derived semantic consequence emitted downstream of authored changes.
 *
 * Consequences are modeled independently from authored changes so Athena can preserve the user
 * mental model of "what I changed" versus "what the platform must now update".
 */
data class SemanticDerivedConsequence(
    val type: SemanticDerivedConsequenceType,
    val message: String,
    val affectedPackage: PackageIdentifier? = null,
    val subjectIdentity: StableSemanticIdentity? = null,
    val diagnostic: SemanticDiagnostic? = null,
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * Canonical semantic diff contract published above repository-model and below runtime/IDE flows.
 *
 * The diff groups one baseline, one current snapshot, authored semantic changes, and derived
 * consequences into a stable inspectable shape.
 */
data class SemanticDiff(
    val baseline: SemanticBaselineDescriptor,
    val snapshot: SemanticBaselineSnapshot,
    val authoredChanges: List<SemanticChangeRecord> = emptyList(),
    val derivedConsequences: List<SemanticDerivedConsequence> = emptyList(),
) {
    /** Distinct package identities touched either directly by authored change or derived fallout. */
    val affectedPackages: List<PackageIdentifier>
        get() = (
            authoredChanges.mapNotNull { it.affectedPackage } +
                derivedConsequences.mapNotNull { it.affectedPackage }
            ).distinct()
            .sortedWith(
                compareBy<PackageIdentifier>(
                    { packageId -> if (packageId == snapshot.repositoryReport.repository.manifest.primaryPackage.id) 0 else 1 },
                    { packageId -> packageId.name },
                    { packageId -> packageId.version.orEmpty() },
                ),
            )
}

/** High-level semantic review item families published for reviewers and later commit tooling. */
enum class SemanticReviewEntryKind {
    AFFECTED_PACKAGE,
    REPOSITORY_CONTRACT,
    PACKAGE_DEPENDENCY,
    ENGINEERING_CHANGE,
    DERIVED_CONSEQUENCE,
    VALIDATION_IMPACT,
    INPUT_WARNING,
}

/** Stable semantic fact categories that one review entry may trace back to. */
enum class SemanticReviewFactKind {
    AUTHORED_CHANGE,
    DERIVED_CONSEQUENCE,
    DIAGNOSTIC,
}

/**
 * Inspectable semantic fact reference attached to one review entry.
 *
 * Review entries stay readable for people while preserving the exact semantic fact family that
 * justified the published review statement.
 */
data class SemanticReviewFactReference(
    val factKind: SemanticReviewFactKind,
    val identifier: String,
    val affectedPackage: PackageIdentifier? = null,
    val subjectIdentity: StableSemanticIdentity? = null,
)

/**
 * One typed semantic review entry derived from stable diff and consequence facts.
 *
 * Review output stays more explicit than a raw diff mirror while remaining traceable back to
 * authored changes, derived consequences, or diagnostics.
 */
data class SemanticReviewEntry(
    val kind: SemanticReviewEntryKind,
    val message: String,
    val affectedPackage: PackageIdentifier? = null,
    val subjectIdentity: StableSemanticIdentity? = null,
    val factReferences: List<SemanticReviewFactReference> = emptyList(),
)

/** Additive semantic review enrichment families published by governed hosted plugins. */
enum class SemanticReviewEnrichmentKind {
    DOMAIN_LABEL,
    REVIEW_HINT,
    DOMAIN_SUMMARY,
    PLUGIN_WARNING,
}

/**
 * Additive semantic review enrichment published after core review generation.
 *
 * Enrichments remain separate from core review entries so plugins can add interpretation without
 * becoming alternative semantic authorities.
 */
data class SemanticReviewEnrichment(
    val pluginId: String,
    val kind: SemanticReviewEnrichmentKind,
    val message: String,
    val affectedPackage: PackageIdentifier? = null,
    val subjectIdentity: StableSemanticIdentity? = null,
    val factReferences: List<SemanticReviewFactReference> = emptyList(),
)

/**
 * Review-oriented summary derived from one semantic diff.
 *
 * Review surfaces can present this contract directly without needing Git-style hunks or UI-owned
 * reconstruction of what changed semantically.
 */
data class SemanticReviewSummary(
    val baseline: SemanticBaselineDescriptor,
    val affectedPackages: List<PackageIdentifier> = emptyList(),
    val authoredChanges: List<SemanticChangeRecord> = emptyList(),
    val derivedConsequences: List<SemanticDerivedConsequence> = emptyList(),
    val diagnostics: List<SemanticDiagnostic> = emptyList(),
    val entries: List<SemanticReviewEntry> = emptyList(),
    val enrichments: List<SemanticReviewEnrichment> = emptyList(),
)

/** High-level semantic commit-intent item families published for downstream execution handoff. */
enum class SemanticCommitEntryKind {
    AFFECTED_PACKAGE,
    REPOSITORY_CONTRACT,
    PACKAGE_DEPENDENCY,
    ENGINEERING_CHANGE,
    DERIVED_CONSEQUENCE,
    VALIDATION_CONSEQUENCE,
    INPUT_WARNING,
}

/** Stable semantic fact categories that one commit-intent entry may trace back to. */
enum class SemanticCommitFactKind {
    REVIEW_ENTRY,
    AUTHORED_CHANGE,
    DERIVED_CONSEQUENCE,
    DIAGNOSTIC,
}

/**
 * Inspectable semantic fact reference attached to one commit-intent entry.
 *
 * Commit entries stay adapter-ready while preserving the reviewed semantic facts that justify the
 * proposed commit grouping.
 */
data class SemanticCommitFactReference(
    val factKind: SemanticCommitFactKind,
    val identifier: String,
    val affectedPackage: PackageIdentifier? = null,
    val subjectIdentity: StableSemanticIdentity? = null,
)

/**
 * One typed semantic commit-intent entry derived from reviewed semantic change.
 *
 * Commit preparation remains semantic-first and inspectable instead of collapsing back to file
 * staging vocabulary or provider-specific execution terms.
 */
data class SemanticCommitEntry(
    val kind: SemanticCommitEntryKind,
    val message: String,
    val affectedPackage: PackageIdentifier? = null,
    val subjectIdentity: StableSemanticIdentity? = null,
    val factReferences: List<SemanticCommitFactReference> = emptyList(),
)

/**
 * Intent contract for preparing a semantic commit or publication step.
 *
 * This remains semantic and transport-light: it describes what would be committed semantically,
 * not how a provider-specific SCM backend stores or transmits that action.
 */
data class SemanticCommitIntent(
    val baseline: SemanticBaselineDescriptor,
    val affectedPackages: List<PackageIdentifier> = emptyList(),
    val authoredChanges: List<SemanticChangeRecord> = emptyList(),
    val derivedConsequences: List<SemanticDerivedConsequence> = emptyList(),
    val diagnostics: List<SemanticDiagnostic> = emptyList(),
    val entries: List<SemanticCommitEntry> = emptyList(),
    val summary: String? = null,
)

/** Stable version-meaning changes that one package may express across a semantic history window. */
enum class SemanticPackageVersionChangeKind {
    VERSION_UNSPECIFIED,
    INITIAL_VERSION_DECLARED,
    VERSION_UPDATED,
    VERSION_REMOVED,
    VERSION_UNCHANGED,
}

/**
 * Package-version meaning anchored to one stable package identity.
 *
 * The history model keeps version meaning explicit instead of hiding it in free-form messages or
 * provider-native revision strings.
 */
data class SemanticPackageVersionMeaning(
    val packageId: PackageIdentifier,
    val baselineVersion: String? = null,
    val currentVersion: String? = packageId.version,
    val changeKind: SemanticPackageVersionChangeKind,
)

/** Stable dependency-movement families published for package-aware semantic history. */
enum class SemanticDependencyMovementKind {
    ADDED,
    REMOVED,
    VERSION_CHANGED,
    RETARGETED,
}

/**
 * One dependency movement tied to stable package identity and version meaning.
 *
 * This keeps package-evolution review semantic-first without widening M6 into registry transport
 * or vendor-native dependency graph vocabulary.
 */
data class SemanticDependencyMovement(
    val packageId: PackageIdentifier,
    val kind: SemanticDependencyMovementKind,
    val baselineVersion: String? = null,
    val currentVersion: String? = packageId.version,
    val message: String,
    val metadata: Map<String, String> = emptyMap(),
)

/** Narrow M6 release-relevance signals derived from semantic package evolution. */
enum class SemanticReleaseRelevance {
    NONE,
    PATCH_CANDIDATE,
    MINOR_CANDIDATE,
    MAJOR_CANDIDATE,
    REVIEW_REQUIRED,
}

/** Narrow M6 contract-break risk levels derived from semantic package evolution. */
enum class SemanticContractBreakRisk {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    UNDETERMINED,
}

/**
 * Inspectable validation movement between one baseline and the current semantic state.
 *
 * M6 keeps validation movement semantic and transport-light by publishing typed counts and
 * continuation decisions rather than provider-native workflow language.
 */
data class SemanticValidationMovement(
    val baselineErrorCount: Int,
    val baselineWarningCount: Int,
    val currentErrorCount: Int,
    val currentWarningCount: Int,
    val baselineContinuationDecision: SemanticContinuationDecision? = null,
    val currentContinuationDecision: SemanticContinuationDecision? = null,
    val message: String,
)

/**
 * Request contract for package-aware semantic history over one baseline sequence.
 *
 * The request remains transport-light: it names the package of interest and the semantic
 * baselines to compare, without pulling in registry, remote, or provider-native log mechanics.
 */
data class SemanticHistoryRequest(
    val packageId: PackageIdentifier,
    val baselineSequence: List<SemanticBaselineDescriptor> = emptyList(),
)

/**
 * One baseline-to-current comparison consumed by the publish-oriented history summarizer.
 *
 * The comparison keeps the current snapshot attached to the semantic diff while preserving the
 * exact baseline snapshot that produced it.
 */
data class SemanticHistoryComparison(
    val baseline: SemanticBaselineSnapshot,
    val diff: SemanticDiff,
)

/**
 * One inspectable semantic history entry associated with a package-aware baseline sequence.
 *
 * Later provider adapters may enrich how history is sourced, but the published meaning stays
 * package-centered, version-aware, dependency-aware, and release-oriented inside this neutral
 * kernel contract.
 */
enum class SemanticHistoryEntryKind {
    AUTHORED_EVOLUTION,
    VALIDATION_MOVEMENT,
    DERIVED_CHURN,
}

/**
 * One inspectable semantic history entry associated with a package-aware baseline sequence.
 *
 * Later provider adapters may enrich how history is sourced, but the published meaning stays
 * package-centered, version-aware, dependency-aware, and release-oriented inside this neutral
 * kernel contract.
 */
data class SemanticHistoryEntry(
    val kind: SemanticHistoryEntryKind = SemanticHistoryEntryKind.AUTHORED_EVOLUTION,
    val baseline: SemanticBaselineDescriptor,
    val packageVersion: SemanticPackageVersionMeaning,
    val changeCategory: SemanticChangeCategory? = null,
    val releaseRelevance: SemanticReleaseRelevance = SemanticReleaseRelevance.NONE,
    val contractBreakRisk: SemanticContractBreakRisk = SemanticContractBreakRisk.NONE,
    val message: String,
    val dependencyMovements: List<SemanticDependencyMovement> = emptyList(),
    val validationMovement: SemanticValidationMovement? = null,
    val authoredChanges: List<SemanticChangeRecord> = emptyList(),
    val derivedConsequences: List<SemanticDerivedConsequence> = emptyList(),
)

/**
 * Package-aware semantic history summary published for later IDE and workflow consumption.
 *
 * The history remains semantic and baseline-oriented instead of exposing provider-native log
 * mechanics or transport details. M6 keeps the summary centered on one package identity, one
 * baseline sequence, typed package lineage, and narrow release relevance.
 */
data class SemanticHistorySummary(
    val packageId: PackageIdentifier,
    val baselineSequence: List<SemanticBaselineDescriptor> = emptyList(),
    val packageLineage: List<SemanticPackageVersionMeaning> = emptyList(),
    val validationMovements: List<SemanticValidationMovement> = emptyList(),
    val entries: List<SemanticHistoryEntry> = emptyList(),
    val releaseRelevance: SemanticReleaseRelevance = SemanticReleaseRelevance.NONE,
    val contractBreakRisk: SemanticContractBreakRisk = SemanticContractBreakRisk.NONE,
    val summary: String? = null,
)

/**
 * VCS-neutral seam for baseline capture, diff, review, commit-intent, and history publication.
 *
 * Story 1.1 only freezes the contract boundary. Later M6 stories can implement adapter-backed
 * behavior behind this seam without pulling Git, Theia SCM, or runtime mechanics into the model.
 */
interface SemanticScmAdapter {
    /** Stable identifier for one semantic SCM adapter implementation. */
    val adapterId: String

    /** Captures a typed semantic baseline snapshot from canonical repository and engineering state. */
    fun captureBaseline(
        descriptor: SemanticBaselineDescriptor,
        repositoryReport: RepositoryGraphReport,
        engineeringDocuments: List<EngineeringDocument> = emptyList(),
        validationResult: SemanticValidationResult? = null,
        diagnostics: List<SemanticDiagnostic> = emptyList(),
    ): SemanticBaselineSnapshot

    /** Produces one typed semantic diff between an earlier baseline and the current snapshot. */
    fun summarizeDiff(
        baseline: SemanticBaselineSnapshot,
        current: SemanticBaselineSnapshot,
    ): SemanticDiff

    /** Produces a review summary from one already-computed semantic diff. */
    fun summarizeReview(diff: SemanticDiff): SemanticReviewSummary

    /** Produces a commit-intent summary from one already-reviewed semantic diff. */
    fun prepareCommitIntent(review: SemanticReviewSummary): SemanticCommitIntent

    /** Publishes a package-aware semantic history summary without exposing vendor-native log types. */
    fun summarizeHistory(request: SemanticHistoryRequest): SemanticHistorySummary
}
