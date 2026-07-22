package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.runtime.AthenaAdjustLayoutPlacementIntent
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
import com.engineeringood.athena.layout.AuthoredLayoutAxis
import com.engineeringood.athena.layout.AuthoredLayoutIntent
import com.engineeringood.athena.layout.AuthoredLayoutIntentPriority
import com.engineeringood.athena.layout.AuthoredLayoutIntentRelation
import com.engineeringood.athena.layout.AuthoredLayoutIntentStatement
import com.engineeringood.athena.layout.LayoutSourceSpan

/**
 * Parameters for one graph-originated Athena command-intent submission.
 */
data class AthenaGraphCommandIntentParams(
    val intentId: String,
    val viewId: String,
    val source: AthenaGraphCommandTargetPayload? = null,
    val target: AthenaGraphCommandTargetPayload,
    val requestedPlacement: AthenaGraphPlacementPayload? = null,
    val authoredLayoutIntent: AthenaAuthoredLayoutIntentPayload? = null,
)

data class AthenaAuthoredLayoutIntentPayload(
    val viewFamily: String,
    val statements: List<AthenaAuthoredLayoutIntentStatementPayload>,
)

data class AthenaAuthoredLayoutIntentStatementPayload(
    val subject: String,
    val relation: String,
    val target: String,
    val axis: String? = null,
    val priority: String = "preference",
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
    val sourceEdit: AthenaAuthoringSourceEditPayload? = null,
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


        null -> null
    }
}

internal fun AthenaAuthoredLayoutIntentPayload.toBackendIntent(sourceUnitId: String): AuthoredLayoutIntent? {
    if (viewFamily.isBlank() || statements.isEmpty()) return null
    val span = LayoutSourceSpan(
        sourceUnitId = sourceUnitId.ifBlank { "active.athena" },
        startLine = 1,
        startColumn = 1,
        endLine = 1,
        endColumn = 1,
    )
    val mappedStatements = statements.map { statement ->
        val relation = when (statement.relation) {
            "near" -> AuthoredLayoutIntentRelation.NEAR
            "below" -> AuthoredLayoutIntentRelation.BELOW
            "aligned-with" -> AuthoredLayoutIntentRelation.ALIGNED_WITH
            "grouped-with" -> AuthoredLayoutIntentRelation.GROUPED_WITH
            else -> return null
        }
        val axis = when (statement.axis) {
            null -> null
            "horizontal" -> AuthoredLayoutAxis.HORIZONTAL
            "vertical" -> AuthoredLayoutAxis.VERTICAL
            else -> return null
        }
        if (statement.priority != "preference") return null
        AuthoredLayoutIntentStatement(
            subject = statement.subject,
            relation = relation,
            target = statement.target,
            axis = axis,
            priority = AuthoredLayoutIntentPriority.PREFERENCE,
            sourceSpan = span,
        )
    }
    return AuthoredLayoutIntent(
        viewFamily = viewFamily,
        statements = mappedStatements,
        sourceSpan = span,
    )
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
        AthenaGraphCommandIntentId.ADJUST_LAYOUT_PLACEMENT, null -> AthenaMutationCategory.PROJECTION_MUTATION
    }
}

private fun Enum<*>.toTransportValue(): String {
    return name.lowercase().replace('_', '-')
}
