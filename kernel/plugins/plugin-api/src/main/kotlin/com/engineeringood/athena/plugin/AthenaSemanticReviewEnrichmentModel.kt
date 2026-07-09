package com.engineeringood.athena.plugin

import com.engineeringood.athena.scm.SemanticReviewEnrichment
import com.engineeringood.athena.scm.SemanticReviewSummary

/**
 * Plugin-side contract for additive semantic review enrichment.
 *
 * Implementations may publish domain labels, hints, or short summaries after the core semantic
 * review is complete, but they may not rewrite or suppress core semantic SCM facts.
 */
interface AthenaSemanticReviewEnrichmentContributor : AthenaPlugin {
    /**
     * Returns additive semantic review enrichments derived from the already-generated core review.
     *
     * The returned enrichments must be deterministic for the same input review and plugin state.
     */
    fun enrichReview(review: SemanticReviewSummary): List<SemanticReviewEnrichment> = emptyList()
}
