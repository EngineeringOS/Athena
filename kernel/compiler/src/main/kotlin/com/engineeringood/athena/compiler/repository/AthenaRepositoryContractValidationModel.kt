package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.repository.EngineeringRepository
import com.engineeringood.athena.repository.RepositoryDiagnostic
import com.engineeringood.athena.repository.RepositoryDiagnosticSeverity
import java.nio.file.Path

/**
 * Inspectable result of loading and validating the governed M5 repository-root contract.
 *
 * The result stays JVM-owned so later runtime and LSP stories can consume the same validation
 * output without inventing parallel repository/package models.
 */
data class AthenaRepositoryContractValidationResult(
    val repositoryRoot: Path,
    val manifestPath: Path,
    val lockPath: Path,
    val manifestPresent: Boolean,
    val lockPresent: Boolean,
    val repository: EngineeringRepository? = null,
    val diagnostics: List<RepositoryDiagnostic> = emptyList(),
) {
    /** Indicates whether the repository contract passed validation without error diagnostics. */
    val isValid: Boolean
        get() = repository != null && diagnostics.none { diagnostic ->
            diagnostic.severity == RepositoryDiagnosticSeverity.ERROR
        }
}

