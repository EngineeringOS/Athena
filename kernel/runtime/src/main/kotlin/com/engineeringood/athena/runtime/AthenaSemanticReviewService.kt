package com.engineeringood.athena.runtime

import com.engineeringood.athena.scm.SemanticBaselineSnapshot
import com.engineeringood.athena.scm.SemanticDiff
import com.engineeringood.athena.scm.SemanticReviewSummary
import com.engineeringood.athena.scm.SemanticReviewSummaryGenerator

/**
 * Runtime-owned facade that publishes semantic review summaries over the existing diff path.
 *
 * The runtime reuses the governed JVM semantic comparison flow instead of inventing a second
 * review model in the frontend or product shell.
 */
class AthenaSemanticReviewService(
    private val diffService: AthenaSemanticDiffService = AthenaSemanticDiffService(),
    private val generator: SemanticReviewSummaryGenerator = SemanticReviewSummaryGenerator(),
    private val pluginRuntimeServices: AthenaPluginRuntimeServices? = null,
) {
    /** Produces one semantic review summary directly from an existing semantic diff. */
    fun summarizeDiff(diff: SemanticDiff): SemanticReviewSummary = enrich(generator.summarize(diff))

    /**
     * Produces one semantic review summary by comparing the active repository session against one
     * already-resolved semantic baseline snapshot.
     */
    fun summarizeAgainstBaseline(
        session: RepositoryGraphSession,
        baseline: SemanticBaselineSnapshot,
    ): SemanticReviewSummary {
        return summarizeDiff(
            diffService.compareAgainstBaseline(
                session = session,
                baseline = baseline,
            ),
        )
    }

    private fun enrich(summary: SemanticReviewSummary): SemanticReviewSummary {
        val enrichments = pluginRuntimeServices?.enrichReview(summary).orEmpty()
        return if (enrichments.isEmpty()) {
            summary
        } else {
            summary.copy(enrichments = enrichments)
        }
    }
}
