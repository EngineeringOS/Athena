package com.engineeringood.athena.scm

import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.semantics.core.SemanticRuleId
import java.nio.file.Path

/**
 * VCS-neutral baseline locator handed from runtime or later product surfaces into Athena's
 * semantic SCM core.
 *
 * The `adapterId` selects one substrate adapter while `locator` remains an opaque adapter-owned
 * reference that the semantic core does not interpret as a Git branch, commit, or Theia object.
 */
data class SemanticBaselineLocator(
    val adapterId: String,
    val locator: String,
    val label: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * One baseline-resolution request derived from the active repository session plus a chosen
 * baseline locator.
 *
 * The request stays runtime-independent: it carries only the current repository root needed for
 * relative baseline resolution and one semantic baseline descriptor owned by Athena.
 */
data class SemanticBaselineResolutionRequest(
    val descriptor: SemanticBaselineDescriptor,
    val locator: SemanticBaselineLocator,
    val currentRepositoryRoot: Path,
)

/**
 * Inspectable result of one baseline-resolution attempt.
 *
 * Resolution succeeds only when Athena materializes a baseline snapshot and no error-level
 * semantic diagnostics were emitted.
 */
data class SemanticBaselineResolutionResult(
    val descriptor: SemanticBaselineDescriptor,
    val snapshot: SemanticBaselineSnapshot? = null,
    val diagnostics: List<SemanticDiagnostic> = emptyList(),
) {
    /** True when one baseline snapshot was loaded without any error-level semantic diagnostics. */
    val isResolved: Boolean
        get() = snapshot != null && diagnostics.none { it.severity == SemanticDiagnosticSeverity.ERROR }
}

/**
 * Vendor-backed seam that loads one semantic baseline snapshot for Athena's semantic SCM core.
 *
 * Implementations may use Git or another substrate internally, but they return only Athena-owned
 * semantic contracts and typed semantic diagnostics to the kernel boundary.
 */
interface SemanticBaselineAdapter {
    /** Stable identifier for one baseline-loading adapter implementation. */
    val adapterId: String

    /** Resolves the requested baseline into one typed semantic snapshot or deterministic diagnostics. */
    fun resolve(request: SemanticBaselineResolutionRequest): SemanticBaselineResolutionResult
}

/**
 * Small semantic baseline orchestrator that chooses the matching adapter and normalizes error
 * handling for unsupported or malformed requests.
 */
class SemanticBaselineResolver(
    private val adapters: List<SemanticBaselineAdapter> = emptyList(),
) {
    /** Resolves one semantic baseline through the adapter declared by the incoming locator. */
    fun resolve(request: SemanticBaselineResolutionRequest): SemanticBaselineResolutionResult {
        val adapter = adapters.firstOrNull { candidate -> candidate.adapterId == request.locator.adapterId }
            ?: return SemanticBaselineResolutionResult(
                descriptor = request.descriptor,
                diagnostics = listOf(
                    baselineResolutionDiagnostic(
                        ruleId = "semantic.baseline.adapter.unsupported",
                        message = "No semantic baseline adapter is registered for `${request.locator.adapterId}`.",
                        locator = request.locator,
                    ),
                ),
            )
        val result = adapter.resolve(request)
        return when {
            result.descriptor != request.descriptor -> result.copy(descriptor = request.descriptor)
            result.snapshot != null || result.diagnostics.isNotEmpty() -> result
            else -> SemanticBaselineResolutionResult(
                descriptor = request.descriptor,
                diagnostics = listOf(
                    baselineResolutionDiagnostic(
                        ruleId = "semantic.baseline.adapter.empty-result",
                        message = "Semantic baseline adapter `${adapter.adapterId}` returned neither a snapshot nor diagnostics.",
                        locator = request.locator,
                    ),
                ),
            )
        }
    }
}

/**
 * Builds one deterministic semantic diagnostic for baseline-resolution failures.
 *
 * M6 uses a synthetic provenance because baseline loading errors are adapter/boundary failures
 * rather than authored-source span failures.
 */
fun baselineResolutionDiagnostic(
    ruleId: String,
    message: String,
    locator: SemanticBaselineLocator,
    severity: SemanticDiagnosticSeverity = SemanticDiagnosticSeverity.ERROR,
): SemanticDiagnostic {
    return SemanticDiagnostic(
        severity = severity,
        ruleId = SemanticRuleId(ruleId),
        category = SemanticDiagnosticCategory.DOMAIN,
        subjectIdentity = null,
        provenance = SourceProvenance(
            file = locator.label ?: locator.locator.ifBlank { "<semantic-baseline>" },
            startLine = 1,
            startColumn = 1,
            endLine = 1,
            endColumn = 1,
        ),
        message = message,
    )
}
