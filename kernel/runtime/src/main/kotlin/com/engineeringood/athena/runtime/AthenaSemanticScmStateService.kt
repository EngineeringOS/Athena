package com.engineeringood.athena.runtime

import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticBaselineLocator
import com.engineeringood.athena.scm.SemanticCommitIntent
import com.engineeringood.athena.scm.SemanticReviewSummary
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity

/**
 * Runtime-owned semantic SCM state status published above baseline resolution and below IDE seams.
 */
enum class AthenaSemanticScmStateStatus {
    READY,
    BASELINE_UNRESOLVED,
}

/**
 * Typed runtime-owned semantic SCM projection for one baseline request against one active session.
 *
 * The projection keeps baseline diagnostics, review output, and commit-intent output together so
 * downstream LSP and IDE adapters can project one semantic SCM snapshot without rebuilding meaning.
 */
data class AthenaSemanticScmState(
    val descriptor: SemanticBaselineDescriptor,
    val locator: SemanticBaselineLocator,
    val status: AthenaSemanticScmStateStatus,
    val diagnostics: List<SemanticDiagnostic> = emptyList(),
    val reviewSummary: SemanticReviewSummary? = null,
    val commitIntent: SemanticCommitIntent? = null,
) {
    /** True when the baseline resolved without error-level diagnostics and review/commit state is available. */
    val isReady: Boolean
        get() = status == AthenaSemanticScmStateStatus.READY &&
            diagnostics.none { diagnostic -> diagnostic.severity == SemanticDiagnosticSeverity.ERROR } &&
            reviewSummary != null &&
            commitIntent != null
}

/**
 * Runtime-owned facade that resolves one baseline and projects review plus commit-preparation state.
 *
 * The service reuses the existing baseline, review, and commit services so review/commit meaning
 * stays anchored in the JVM semantic path rather than being rebuilt in LSP or Theia.
 */
class AthenaSemanticScmStateService(
    private val baselineService: AthenaSemanticBaselineService = AthenaSemanticBaselineService(),
    private val reviewService: AthenaSemanticReviewService = AthenaSemanticReviewService(),
    private val commitService: AthenaSemanticCommitService = AthenaSemanticCommitService(),
) {
    /**
     * Resolves one semantic SCM state projection for [session] and the supplied baseline request.
     */
    fun inspect(
        session: RepositoryGraphSession,
        descriptor: SemanticBaselineDescriptor,
        locator: SemanticBaselineLocator,
    ): AthenaSemanticScmState {
        val resolution = baselineService.resolveBaseline(
            session = session,
            descriptor = descriptor,
            locator = locator,
        )
        val snapshot = resolution.snapshot
        if (!resolution.isResolved || snapshot == null) {
            return AthenaSemanticScmState(
                descriptor = descriptor,
                locator = locator,
                status = AthenaSemanticScmStateStatus.BASELINE_UNRESOLVED,
                diagnostics = resolution.diagnostics,
            )
        }

        val review = reviewService.summarizeAgainstBaseline(
            session = session,
            baseline = snapshot,
        )
        return AthenaSemanticScmState(
            descriptor = descriptor,
            locator = locator,
            status = AthenaSemanticScmStateStatus.READY,
            diagnostics = resolution.diagnostics,
            reviewSummary = review,
            commitIntent = commitService.prepareReview(review),
        )
    }
}
