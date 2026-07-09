package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.repository.EngineeringRepository
import com.engineeringood.athena.repository.RepositoryDiagnostic
import com.engineeringood.athena.repository.RepositoryDiagnosticSeverity
import com.engineeringood.athena.repository.RepositoryGraphReport
import com.engineeringood.athena.repository.RepositoryResolutionInput
import com.engineeringood.athena.repository.ResolvedPackageGraph
import java.nio.file.Path

/**
 * Inspectable result of resolving one governed repository into the first canonical local package graph.
 *
 * The result stays compiler-owned so later lock, runtime, and IDE stories can consume the same
 * graph and diagnostics without redefining package-resolution authority elsewhere.
 */
data class AthenaRepositoryGraphResolutionResult(
    val repositoryRoot: Path,
    val manifestPath: Path,
    val lockPath: Path,
    val manifestPresent: Boolean,
    val lockPresent: Boolean,
    val repository: EngineeringRepository? = null,
    val resolutionInput: RepositoryResolutionInput? = null,
    val graph: ResolvedPackageGraph? = null,
    val diagnostics: List<RepositoryDiagnostic> = emptyList(),
) {
    /** Combined inspectable repository report for downstream runtime and IDE consumers. */
    val report: RepositoryGraphReport?
        get() = repository?.let { repository ->
            RepositoryGraphReport(
                repository = repository,
                graph = graph,
                diagnostics = diagnostics,
            )
        }

    /** Indicates whether package-graph resolution completed without error diagnostics. */
    val isValid: Boolean
        get() = repository != null &&
            resolutionInput != null &&
            graph != null &&
            diagnostics.none { diagnostic ->
                diagnostic.severity == RepositoryDiagnosticSeverity.ERROR
            }
}
