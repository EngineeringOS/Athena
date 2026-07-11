package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.runtime.AthenaSemanticMutationReview

/**
 * Governed semantic review artifact transported for one accepted mutation.
 *
 * The payload keeps raw diff cardinality plus downstream M6 review and commit projections
 * together so source and graph surfaces can expose one semantic review model.
 */
data class AthenaSemanticMutationReviewPayload(
    val authoredChangeCount: Int,
    val derivedConsequenceCount: Int,
    val reviewSummary: AthenaSemanticReviewPayload,
    val commitIntent: AthenaSemanticCommitPayload,
)

internal fun AthenaSemanticMutationReview.toPayload(): AthenaSemanticMutationReviewPayload {
    return AthenaSemanticMutationReviewPayload(
        authoredChangeCount = diff.authoredChanges.size,
        derivedConsequenceCount = diff.derivedConsequences.size,
        reviewSummary = reviewSummary.toPayload(),
        commitIntent = commitIntent.toPayload(),
    )
}
