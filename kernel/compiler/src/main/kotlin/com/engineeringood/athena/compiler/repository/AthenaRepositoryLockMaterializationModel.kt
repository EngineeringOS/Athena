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
 * Inspectable result of materializing the canonical `athena.lock` contract from resolver authority.
 */
data class AthenaRepositoryLockMaterializationResult(
    val repositoryRoot: Path,
    val manifestPath: Path,
    val lockPath: Path,
    val manifestPresent: Boolean,
    val lockPresent: Boolean,
    val repository: EngineeringRepository? = null,
    val resolutionInput: RepositoryResolutionInput? = null,
    val graph: ResolvedPackageGraph? = null,
    val lock: RepositoryLock? = null,
    val renderedLock: String? = null,
    val diagnostics: List<RepositoryDiagnostic> = emptyList(),
) {
    /** Combined inspectable repository report for later runtime and IDE consumers. */
    val report: RepositoryGraphReport?
        get() = repository?.let { repository ->
            RepositoryGraphReport(
                repository = lock?.let { materializedLock -> repository.copy(lock = materializedLock) } ?: repository,
                graph = graph,
                diagnostics = diagnostics,
            )
        }

    /** Indicates whether lock materialization completed without error diagnostics. */
    val isValid: Boolean
        get() = repository != null &&
            resolutionInput != null &&
            graph != null &&
            lock != null &&
            renderedLock != null &&
            diagnostics.none { diagnostic ->
                diagnostic.severity == RepositoryDiagnosticSeverity.ERROR
            }
}

/**
 * Inspectable result of validating the on-disk `athena.lock` against canonical resolver authority.
 */
data class AthenaRepositoryLockValidationResult(
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
    val renderedExpectedLock: String? = null,
    val diagnostics: List<RepositoryDiagnostic> = emptyList(),
) {
    /** Combined inspectable repository report for later runtime and IDE consumers. */
    val report: RepositoryGraphReport?
        get() = repository?.let { repository ->
            RepositoryGraphReport(
                repository = expectedLock?.let { lock -> repository.copy(lock = lock) } ?: repository,
                graph = graph,
                diagnostics = diagnostics,
            )
        }

    /** Indicates whether the current on-disk lock matches canonical resolver authority. */
    val isValid: Boolean
        get() = repository != null &&
            resolutionInput != null &&
            graph != null &&
            expectedLock != null &&
            actualLock != null &&
            diagnostics.none { diagnostic ->
                diagnostic.severity == RepositoryDiagnosticSeverity.ERROR
            }
}
