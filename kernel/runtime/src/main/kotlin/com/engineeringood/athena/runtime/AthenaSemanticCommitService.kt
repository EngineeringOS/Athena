package com.engineeringood.athena.runtime

import com.engineeringood.athena.scm.SemanticBaselineSnapshot
import com.engineeringood.athena.scm.SemanticCommitIntent
import com.engineeringood.athena.scm.SemanticCommitIntentGenerator
import com.engineeringood.athena.scm.SemanticReviewSummary

/**
 * Runtime-owned facade that publishes semantic commit intent over the existing review path.
 *
 * The runtime reuses the governed JVM semantic comparison and review flow instead of inventing a
 * second commit-preparation model in the frontend or adapter shell.
 */
class AthenaSemanticCommitService(
    private val reviewService: AthenaSemanticReviewService = AthenaSemanticReviewService(),
    private val generator: SemanticCommitIntentGenerator = SemanticCommitIntentGenerator(),
) {
    /** Produces one semantic commit intent directly from an existing semantic review summary. */
    fun prepareReview(review: SemanticReviewSummary): SemanticCommitIntent = generator.prepare(review)

    /**
     * Produces one semantic commit intent by comparing the active repository session against one
     * already-resolved semantic baseline snapshot and then reusing the runtime review path.
     */
    fun prepareAgainstBaseline(
        session: RepositoryGraphSession,
        baseline: SemanticBaselineSnapshot,
    ): SemanticCommitIntent {
        return prepareReview(
            reviewService.summarizeAgainstBaseline(
                session = session,
                baseline = baseline,
            ),
        )
    }
}
