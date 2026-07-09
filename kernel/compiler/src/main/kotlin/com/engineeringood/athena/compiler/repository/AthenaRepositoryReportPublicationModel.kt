package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.repository.EngineeringRepository
import com.engineeringood.athena.repository.RepositoryDiagnostic
import com.engineeringood.athena.repository.RepositoryDiagnosticSeverity
import com.engineeringood.athena.repository.RepositoryGraphReport
import com.engineeringood.athena.repository.RepositoryLock
import com.engineeringood.athena.repository.RepositoryResolutionInput
import com.engineeringood.athena.repository.ResolvedPackageGraph
import java.nio.file.Path

/**
 * Lifecycle state of the current on-disk `athena.lock` compared with compiler-owned canonical output.
 */
enum class AthenaRepositoryReportLockState {
    /** The current on-disk lock matches canonical compiler-owned resolver authority. */
    CURRENT,

    /** The current on-disk lock is present but needs re-materialization to match canonical output. */
    STALE,

    /** The current on-disk lock is present but malformed or otherwise unreadable as a valid lock contract. */
    INVALID,

    /** The current on-disk lock is missing, even though canonical compiler-owned output is available. */
    MISSING,
}

/**
 * Inspectable compiler-owned publication result for one governed repository graph report.
 *
 * The result keeps manifest intent, lock validation, canonical graph state, and diagnostics together
 * so runtime and future IDE layers can consume one stable authority instead of reconstructing it.
 */
data class AthenaRepositoryReportPublicationResult(
    val repositoryRoot: Path,
    val manifestPath: Path,
    val lockPath: Path,
    val manifestPresent: Boolean,
    val lockPresent: Boolean,
    val repository: EngineeringRepository? = null,
    val resolutionInput: RepositoryResolutionInput? = null,
    val graph: ResolvedPackageGraph? = null,
    val expectedLock: RepositoryLock? = null,
    val actualLock: RepositoryLock? = null,
    val lockState: AthenaRepositoryReportLockState,
    val diagnostics: List<RepositoryDiagnostic> = emptyList(),
) {
    /** Canonical repository graph report projected from compiler-owned authority for downstream consumers. */
    val report: RepositoryGraphReport?
        get() = repository?.let { repository ->
            RepositoryGraphReport(
                repository = expectedLock?.let { canonicalLock -> repository.copy(lock = canonicalLock) } ?: repository,
                graph = graph,
                diagnostics = diagnostics,
            )
        }

    /** Indicates whether the publication is canonical and ready for downstream runtime or IDE consumption. */
    val isValid: Boolean
        get() = repository != null &&
            resolutionInput != null &&
            graph != null &&
            expectedLock != null &&
            lockState == AthenaRepositoryReportLockState.CURRENT &&
            diagnostics.none { diagnostic ->
                diagnostic.severity == RepositoryDiagnosticSeverity.ERROR
            }
}
