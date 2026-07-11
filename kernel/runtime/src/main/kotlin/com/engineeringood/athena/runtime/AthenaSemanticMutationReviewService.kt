package com.engineeringood.athena.runtime

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.repository.RepositoryGraphReport
import com.engineeringood.athena.scm.SemanticBaselineDescriptor
import com.engineeringood.athena.scm.SemanticBaselineSnapshot
import com.engineeringood.athena.scm.SemanticCommitIntent
import com.engineeringood.athena.scm.SemanticDiff
import com.engineeringood.athena.scm.SemanticDiffCalculator
import com.engineeringood.athena.scm.SemanticReviewSummary
import com.engineeringood.athena.semantics.core.SemanticValidationResult
import java.nio.file.Path

/**
 * Runtime-owned semantic review artifact for one accepted mutation.
 *
 * The artifact keeps the raw semantic diff plus downstream review and commit preparation output
 * together so source- and graph-originated mutations can publish one governed review model.
 */
data class AthenaSemanticMutationReview(
    val diff: SemanticDiff,
    val reviewSummary: SemanticReviewSummary,
    val commitIntent: SemanticCommitIntent,
)

/**
 * Runtime-owned adapter that feeds accepted semantic mutations back through the existing M6
 * semantic diff, review, and commit services.
 *
 * Review output is only published when the active project belongs to a governed repository source
 * root. Example-only or standalone source files keep their mutation inspection evidence, but they
 * do not fabricate semantic SCM state outside a valid repository contract.
 */
class AthenaSemanticMutationReviewService(
    private val calculator: SemanticDiffCalculator = SemanticDiffCalculator(),
    private val reviewService: AthenaSemanticReviewService = AthenaSemanticReviewService(),
    private val commitService: AthenaSemanticCommitService = AthenaSemanticCommitService(),
) {
    /**
     * Produces one governed semantic review artifact for an accepted mutation over canonical
     * engineering documents, or `null` when no governed repository context is available.
     */
    fun summarizeAcceptedMutation(
        context: AthenaExecutionContext,
        beforeDocument: EngineeringDocument,
        afterDocument: EngineeringDocument,
        beforeValidationResult: SemanticValidationResult? = null,
        afterValidationResult: SemanticValidationResult? = null,
    ): AthenaSemanticMutationReview? {
        val publication = context.services.repositoryReports().publishRepositoryGraphReport(context.project.workspaceRoot)
        val report = publication.report ?: return null
        if (!publication.isValid || !context.project.sourcePath.isWithinPrimarySourceRoot(report, context.project.workspaceRoot)) {
            return null
        }

        val baseline = SemanticBaselineSnapshot(
            descriptor = SemanticBaselineDescriptor(
                baselineId = "accepted-mutation-before:${context.project.name}",
                label = "Before accepted mutation",
            ),
            repositoryReport = report,
            engineeringDocuments = listOf(beforeDocument),
            validationResult = beforeValidationResult,
        )
        val current = SemanticBaselineSnapshot(
            descriptor = SemanticBaselineDescriptor(
                baselineId = "accepted-mutation-after:${context.project.name}",
                label = "After accepted mutation",
            ),
            repositoryReport = report,
            engineeringDocuments = listOf(afterDocument),
            validationResult = afterValidationResult,
        )
        val diff = calculator.calculate(
            baseline = baseline,
            current = current,
        )
        val review = reviewService.summarizeDiff(diff)
        return AthenaSemanticMutationReview(
            diff = diff,
            reviewSummary = review,
            commitIntent = commitService.prepareReview(review),
        )
    }
}

private fun Path.isWithinPrimarySourceRoot(
    report: RepositoryGraphReport,
    workspaceRoot: Path,
): Boolean {
    val sourceRoot = workspaceRoot.resolve(report.repository.manifest.primaryPackage.sourceRoot).normalize().toAbsolutePath()
    val candidate = normalize().toAbsolutePath()
    return candidate.startsWith(sourceRoot)
}
