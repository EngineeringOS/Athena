package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.runtime.AthenaAdjustLayoutPlacementIntent
import com.engineeringood.athena.runtime.AthenaConnectPortsIntent
import com.engineeringood.athena.runtime.AthenaGraphCommandIntent
import com.engineeringood.athena.runtime.AthenaGraphCommandIntentAccepted
import com.engineeringood.athena.runtime.AthenaGraphCommandIntentId
import com.engineeringood.athena.runtime.AthenaGraphCommandIntentRejected
import com.engineeringood.athena.runtime.AthenaGraphCommandIntentResult
import com.engineeringood.athena.runtime.AthenaGraphCommandIntentUnavailable
import com.engineeringood.athena.runtime.AthenaGraphCommandIntentValidationFeedback
import com.engineeringood.athena.runtime.AthenaGraphCommandExecution
import com.engineeringood.athena.runtime.AthenaGraphCommandSubjectKind
import com.engineeringood.athena.runtime.AthenaGraphCommandTarget
import com.engineeringood.athena.runtime.AthenaGraphPlacement
import com.engineeringood.athena.runtime.AthenaMutationCategory
import com.engineeringood.athena.runtime.AthenaMutationValidationFeedback

/**
 * Parameters for one graph-originated Athena command-intent submission.
 */
data class AthenaGraphCommandIntentParams(
    val intentId: String,
    val viewId: String,
    val source: AthenaGraphCommandTargetPayload? = null,
    val target: AthenaGraphCommandTargetPayload,
    val requestedPlacement: AthenaGraphPlacementPayload? = null,
)

/**
 * Inspectable graph-command target transported through the Athena LSP boundary.
 */
data class AthenaGraphCommandTargetPayload(
    val semanticId: String,
    val subjectKind: String,
)

/**
 * Inspectable graph-command placement payload transported through the Athena LSP boundary.
 */
data class AthenaGraphPlacementPayload(
    val x: Int,
    val y: Int,
)

/**
 * Runtime-owned graph command-intent payload transported through the Athena LSP boundary.
 */
data class AthenaGraphCommandIntentPayload(
    val projectName: String,
    val semanticPath: String,
    val status: String,
    val intentId: String,
    val mutationCategory: String,
    val viewId: String,
    val source: AthenaGraphCommandTargetPayload? = null,
    val target: AthenaGraphCommandTargetPayload,
    val requestedPlacement: AthenaGraphPlacementPayload? = null,
    val execution: AthenaGraphCommandExecutionPayload? = null,
    val inspection: AthenaSemanticDiffInspectionPayload? = null,
    val semanticReview: AthenaSemanticMutationReviewPayload? = null,
    val validationFeedback: List<AthenaMutationValidationFeedbackPayload> = emptyList(),
    val reason: String? = null,
)

/**
 * Runtime-owned command execution details transported through the Athena LSP boundary for semantic graph edits.
 */
data class AthenaGraphCommandExecutionPayload(
    val commandKind: String,
    val outcome: String,
    val commandId: String? = null,
    val changedSemanticIds: List<String> = emptyList(),
    val validationFeedback: List<AthenaMutationValidationFeedbackPayload> = emptyList(),
)

internal fun AthenaGraphCommandIntentResult.toPayload(semanticPath: String): AthenaGraphCommandIntentPayload {
    return when (this) {
        is AthenaGraphCommandIntentAccepted -> AthenaGraphCommandIntentPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "accepted",
            intentId = intentId.toTransportValue(),
            mutationCategory = mutationCategory.toTransportValue(),
            viewId = viewId,
            source = source?.toPayload(),
            target = target.toPayload(),
            requestedPlacement = requestedPlacement?.toPayload(),
            execution = execution?.toPayload(),
            inspection = inspection?.toPayload(),
            semanticReview = semanticReview?.toPayload(),
        )

        is AthenaGraphCommandIntentRejected -> AthenaGraphCommandIntentPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "rejected",
            intentId = intentId.toTransportValue(),
            mutationCategory = mutationCategory.toTransportValue(),
            viewId = viewId,
            source = source?.toPayload(),
            target = target.toPayload(),
            requestedPlacement = requestedPlacement?.toPayload(),
            execution = execution?.toPayload(),
            inspection = inspection?.toPayload(),
            semanticReview = semanticReview?.toPayload(),
            reason = reason,
        )

        is AthenaGraphCommandIntentValidationFeedback -> AthenaGraphCommandIntentPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "validation-feedback",
            intentId = intentId.toTransportValue(),
            mutationCategory = mutationCategory.toTransportValue(),
            viewId = viewId,
            source = source?.toPayload(),
            target = target.toPayload(),
            requestedPlacement = requestedPlacement?.toPayload(),
            execution = execution?.toPayload(),
            inspection = inspection?.toPayload(),
            semanticReview = semanticReview?.toPayload(),
            validationFeedback = validationFeedback.map(AthenaMutationValidationFeedback::toPayload),
        )

        is AthenaGraphCommandIntentUnavailable -> AthenaGraphCommandIntentPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "unavailable",
            intentId = intentId.toTransportValue(),
            mutationCategory = mutationCategory.toTransportValue(),
            viewId = viewId,
            source = source?.toPayload(),
            target = target.toPayload(),
            requestedPlacement = requestedPlacement?.toPayload(),
            execution = execution?.toPayload(),
            inspection = inspection?.toPayload(),
            semanticReview = semanticReview?.toPayload(),
            reason = reason,
        )
    }
}

internal fun unavailableGraphCommandIntentPayload(
    projectName: String,
    semanticPath: String,
    params: AthenaGraphCommandIntentParams,
    reason: String,
): AthenaGraphCommandIntentPayload {
    return AthenaGraphCommandIntentPayload(
        projectName = projectName,
        semanticPath = semanticPath,
        status = "unavailable",
        intentId = params.intentId,
        mutationCategory = params.defaultMutationCategory().toTransportValue(),
        viewId = params.viewId,
        source = params.source,
        target = params.target,
        requestedPlacement = params.requestedPlacement,
        reason = reason,
    )
}

internal fun AthenaGraphCommandIntentParams.toRuntimeIntent(): AthenaGraphCommandIntent? {
    return when (intentId.toGraphIntentIdOrNull()) {
        AthenaGraphCommandIntentId.ADJUST_LAYOUT_PLACEMENT -> {
            val placement = requestedPlacement ?: return null
            AthenaAdjustLayoutPlacementIntent(
                viewId = viewId,
                target = target.toRuntimeTarget() ?: return null,
                requestedPlacement = placement.toRuntimePlacement(),
            )
        }

        AthenaGraphCommandIntentId.CONNECT_PORTS -> AthenaConnectPortsIntent(
            viewId = viewId,
            source = source?.toRuntimeTarget() ?: return null,
            target = target.toRuntimeTarget() ?: return null,
        )

        null -> null
    }
}

private fun AthenaGraphCommandTargetPayload.toRuntimeTarget(): AthenaGraphCommandTarget? {
    return AthenaGraphCommandSubjectKind.entries.firstOrNull { kind ->
        kind.name.lowercase() == subjectKind.replace('-', '_').lowercase()
    }?.let { kind ->
        AthenaGraphCommandTarget(
            semanticId = semanticId,
            subjectKind = kind,
        )
    }
}

private fun AthenaGraphPlacementPayload.toRuntimePlacement(): AthenaGraphPlacement {
    return AthenaGraphPlacement(
        x = x,
        y = y,
    )
}

private fun AthenaGraphCommandTarget.toPayload(): AthenaGraphCommandTargetPayload {
    return AthenaGraphCommandTargetPayload(
        semanticId = semanticId,
        subjectKind = subjectKind.toTransportValue(),
    )
}

private fun AthenaGraphPlacement.toPayload(): AthenaGraphPlacementPayload {
    return AthenaGraphPlacementPayload(
        x = x,
        y = y,
    )
}

private fun AthenaGraphCommandExecution.toPayload(): AthenaGraphCommandExecutionPayload {
    return AthenaGraphCommandExecutionPayload(
        commandKind = commandKind.toTransportValue(),
        outcome = outcome.toTransportValue(),
        commandId = commandId,
        changedSemanticIds = changedSemanticIds.sorted(),
        validationFeedback = validationFeedback.map(AthenaMutationValidationFeedback::toPayload),
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

private fun String.toGraphIntentIdOrNull(): AthenaGraphCommandIntentId? {
    return AthenaGraphCommandIntentId.entries.firstOrNull { intent ->
        intent.name.lowercase().replace('_', '-') == lowercase()
    }
}

internal fun AthenaGraphCommandIntentParams.defaultMutationCategory(): AthenaMutationCategory {
    return when (intentId.toGraphIntentIdOrNull()) {
        AthenaGraphCommandIntentId.CONNECT_PORTS -> AthenaMutationCategory.SEMANTIC_MUTATION
        AthenaGraphCommandIntentId.ADJUST_LAYOUT_PLACEMENT, null -> AthenaMutationCategory.PROJECTION_MUTATION
    }
}

private fun Enum<*>.toTransportValue(): String {
    return name.lowercase().replace('_', '-')
}
