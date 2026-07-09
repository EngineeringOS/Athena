package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.repository.EngineeringRepository
import com.engineeringood.athena.repository.RepositoryDiagnostic
import com.engineeringood.athena.repository.RepositoryDiagnosticSeverity
import com.engineeringood.athena.repository.RepositoryResolutionInput
import java.nio.file.Path

/**
 * Inspectable result of turning one governed repository contract into deterministic resolver input.
 *
 * The result stays compiler-owned so later runtime and IDE stories can consume the same typed
 * package-input state without inventing parallel preparation logic.
 */
data class AthenaRepositoryResolutionInputResult(
    val repositoryRoot: Path,
    val manifestPath: Path,
    val lockPath: Path,
    val manifestPresent: Boolean,
    val lockPresent: Boolean,
    val repository: EngineeringRepository? = null,
    val resolutionInput: RepositoryResolutionInput? = null,
    val diagnostics: List<RepositoryDiagnostic> = emptyList(),
) {
    /** Indicates whether the repository can proceed into package resolution without error diagnostics. */
    val isValid: Boolean
        get() = repository != null &&
            resolutionInput != null &&
            diagnostics.none { diagnostic ->
                diagnostic.severity == RepositoryDiagnosticSeverity.ERROR
            }
}
