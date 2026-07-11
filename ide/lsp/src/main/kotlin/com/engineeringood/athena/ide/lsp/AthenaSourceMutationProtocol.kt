package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.runtime.AthenaMutationCategory
import com.engineeringood.athena.runtime.AthenaMutationOutcome
import com.engineeringood.athena.runtime.AthenaMutationValidationFeedback
import com.engineeringood.athena.runtime.AthenaProjectionRefreshConsequence
import com.engineeringood.athena.runtime.AthenaSemanticDiffEntry
import com.engineeringood.athena.runtime.AthenaSemanticDiffInspection
import com.engineeringood.athena.runtime.AthenaSemanticHistoryConsequence
import com.engineeringood.athena.runtime.AthenaSourceMutationAccepted
import com.engineeringood.athena.runtime.AthenaSourceMutationRejected
import com.engineeringood.athena.runtime.AthenaSourceMutationResult
import com.engineeringood.athena.runtime.AthenaSourceMutationUnavailable
import com.engineeringood.athena.runtime.AthenaSourceMutationValidationFeedbackResult

/**
 * Parameters for one explicit Athena-owned source mutation evaluation request.
 */
data class AthenaSourceMutationParams(
    val textDocument: AthenaSourceMutationTextDocument,
)

/**
 * Minimal text-document handle used by the source mutation request.
 */
data class AthenaSourceMutationTextDocument(
    val uri: String,
)

/**
 * One validation feedback entry transported through the Athena LSP boundary.
 */
data class AthenaMutationValidationFeedbackPayload(
    val code: String,
    val message: String,
    val severity: String,
    val relatedSemanticIds: List<String>,
)

/**
 * One semantic diff entry transported through the Athena LSP boundary.
 */
data class AthenaSemanticDiffEntryPayload(
    val semanticId: String,
    val semanticKind: String,
    val changeKind: String,
    val beforeSummary: String?,
    val afterSummary: String?,
)

/**
 * One command-history consequence transported through the Athena LSP boundary.
 */
data class AthenaSemanticHistoryConsequencePayload(
    val commandId: String,
    val commandKind: String,
    val status: String,
    val changedSemanticIds: List<String>,
)

/**
 * One projection refresh consequence transported through the Athena LSP boundary.
 */
data class AthenaProjectionRefreshConsequencePayload(
    val layer: String,
    val mode: String?,
    val affectedViewIds: List<String>,
    val affectedSemanticIds: List<String>,
)

/**
 * Runtime-owned semantic diff inspection transported through the Athena LSP boundary.
 */
data class AthenaSemanticDiffInspectionPayload(
    val projectName: String,
    val source: String,
    val affectedCommandIds: List<String>,
    val affectedSemanticIds: List<String>,
    val entries: List<AthenaSemanticDiffEntryPayload>,
    val historyConsequences: List<AthenaSemanticHistoryConsequencePayload>,
    val projectionConsequences: List<AthenaProjectionRefreshConsequencePayload>,
)

/**
 * Runtime-owned source mutation payload transported through the Athena LSP boundary.
 */
data class AthenaSourceMutationPayload(
    val uri: String,
    val version: Int,
    val projectName: String,
    val semanticPath: String,
    val mutationCategory: String,
    val outcome: String,
    val changedSemanticIds: List<String> = emptyList(),
    val validationFeedback: List<AthenaMutationValidationFeedbackPayload> = emptyList(),
    val reason: String? = null,
    val inspection: AthenaSemanticDiffInspectionPayload? = null,
    val semanticReview: AthenaSemanticMutationReviewPayload? = null,
)

internal fun AthenaSourceMutationResult.toPayload(
    uri: String,
    version: Int,
    semanticPath: String,
): AthenaSourceMutationPayload {
    return when (this) {
        is AthenaSourceMutationAccepted -> AthenaSourceMutationPayload(
            uri = uri,
            version = version,
            projectName = projectName,
            semanticPath = semanticPath,
            mutationCategory = mutationCategory.toTransportValue(),
            outcome = outcome.toTransportValue(),
            changedSemanticIds = changedSemanticIds.sorted(),
            inspection = inspection.toPayload(),
            semanticReview = semanticReview?.toPayload(),
        )

        is AthenaSourceMutationRejected -> AthenaSourceMutationPayload(
            uri = uri,
            version = version,
            projectName = projectName,
            semanticPath = semanticPath,
            mutationCategory = mutationCategory.toTransportValue(),
            outcome = outcome.toTransportValue(),
            changedSemanticIds = changedSemanticIds.sorted(),
            reason = reason,
        )

        is AthenaSourceMutationValidationFeedbackResult -> AthenaSourceMutationPayload(
            uri = uri,
            version = version,
            projectName = projectName,
            semanticPath = semanticPath,
            mutationCategory = mutationCategory.toTransportValue(),
            outcome = outcome.toTransportValue(),
            changedSemanticIds = changedSemanticIds.sorted(),
            validationFeedback = validationFeedback.map(AthenaMutationValidationFeedback::toPayload),
        )

        is AthenaSourceMutationUnavailable -> AthenaSourceMutationPayload(
            uri = uri,
            version = version,
            projectName = projectName,
            semanticPath = semanticPath,
            mutationCategory = mutationCategory.toTransportValue(),
            outcome = outcome.toTransportValue(),
            reason = reason,
        )
    }
}

internal fun unavailableSourceMutationPayload(
    projectName: String,
    semanticPath: String,
    uri: String,
    version: Int,
    reason: String,
): AthenaSourceMutationPayload {
    return AthenaSourceMutationPayload(
        uri = uri,
        version = version,
        projectName = projectName,
        semanticPath = semanticPath,
        mutationCategory = AthenaMutationCategory.SEMANTIC_MUTATION.toTransportValue(),
        outcome = AthenaMutationOutcome.UNAVAILABLE.toTransportValue(),
        reason = reason,
    )
}

internal fun AthenaSemanticDiffInspection.toPayload(): AthenaSemanticDiffInspectionPayload {
    return AthenaSemanticDiffInspectionPayload(
        projectName = projectName,
        source = source.toTransportValue(),
        affectedCommandIds = affectedCommandIds.distinct(),
        affectedSemanticIds = affectedSemanticIds.sorted(),
        entries = entries.map(AthenaSemanticDiffEntry::toPayload),
        historyConsequences = historyConsequences.map(AthenaSemanticHistoryConsequence::toPayload),
        projectionConsequences = projectionConsequences.map(AthenaProjectionRefreshConsequence::toPayload),
    )
}

private fun AthenaSemanticDiffEntry.toPayload(): AthenaSemanticDiffEntryPayload {
    return AthenaSemanticDiffEntryPayload(
        semanticId = semanticId,
        semanticKind = semanticKind,
        changeKind = changeKind.toTransportValue(),
        beforeSummary = beforeSummary,
        afterSummary = afterSummary,
    )
}

private fun AthenaSemanticHistoryConsequence.toPayload(): AthenaSemanticHistoryConsequencePayload {
    return AthenaSemanticHistoryConsequencePayload(
        commandId = commandId,
        commandKind = commandKind.toTransportValue(),
        status = status.toTransportValue(),
        changedSemanticIds = changedSemanticIds.sorted(),
    )
}

private fun AthenaProjectionRefreshConsequence.toPayload(): AthenaProjectionRefreshConsequencePayload {
    return AthenaProjectionRefreshConsequencePayload(
        layer = layer.toTransportValue(),
        mode = mode,
        affectedViewIds = affectedViewIds.sorted(),
        affectedSemanticIds = affectedSemanticIds.sorted(),
    )
}

private fun AthenaMutationValidationFeedback.toPayload(): AthenaMutationValidationFeedbackPayload {
    return AthenaMutationValidationFeedbackPayload(
        code = code,
        message = message,
        severity = severity.toTransportValue(),
        relatedSemanticIds = relatedSemanticIds.sorted(),
    )
}

private fun Enum<*>.toTransportValue(): String {
    return name.lowercase().replace('_', '-')
}
