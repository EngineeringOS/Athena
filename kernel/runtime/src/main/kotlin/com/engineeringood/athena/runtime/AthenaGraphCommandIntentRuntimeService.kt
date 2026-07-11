package com.engineeringood.athena.runtime

import com.engineeringood.athena.layout.ProjectionOwnershipContract
import com.engineeringood.athena.runtime.AthenaGraphCommandIntentId.ADJUST_LAYOUT_PLACEMENT
import com.engineeringood.athena.runtime.AthenaGraphCommandIntentId.CONNECT_PORTS
import com.engineeringood.athena.runtime.AthenaGraphCommandSubjectKind.COMPONENT
import com.engineeringood.athena.runtime.AthenaGraphCommandSubjectKind.PORT

/**
 * Stable Athena-owned graph command intent identifiers.
 *
 * These identifiers name what a graph-originated action means inside Athena before any mutation
 * execution occurs. They are intentionally renderer-neutral.
 */
enum class AthenaGraphCommandIntentId {
    ADJUST_LAYOUT_PLACEMENT,
    CONNECT_PORTS,
}

/**
 * Stable subject-kind vocabulary carried by graph command intents.
 */
enum class AthenaGraphCommandSubjectKind {
    COMPONENT,
    LABEL,
    CONNECTION,
    PORT,
}

/**
 * Inspectable graph-command target carried through the Athena runtime boundary.
 */
data class AthenaGraphCommandTarget(
    val semanticId: String,
    val subjectKind: AthenaGraphCommandSubjectKind,
)

/**
 * Inspectable graph-command placement payload carried through the Athena runtime boundary.
 */
data class AthenaGraphPlacement(
    val x: Int,
    val y: Int,
)

/**
 * Runtime-owned graph command intent published before any real semantic or projection mutation path executes.
 */
sealed interface AthenaGraphCommandIntent {
    /**
     * Stable Athena-owned intent identifier.
     */
    val intentId: AthenaGraphCommandIntentId

    /**
     * Explicit mutation category associated with the intent meaning.
     */
    val mutationCategory: AthenaMutationCategory

    /**
     * Optional source subject referenced by the graph-originated action.
     */
    val source: AthenaGraphCommandTarget?
        get() = null

    /**
     * Projection view context under which the intent was emitted.
     */
    val viewId: String

    /**
     * Target subject referenced by the graph-originated action.
     */
    val target: AthenaGraphCommandTarget

    /**
     * Optional placement payload attached to the intent.
     */
    val requestedPlacement: AthenaGraphPlacement?
}

/**
 * Graph-originated request to adjust persisted projection placement for one target subject.
 */
data class AthenaAdjustLayoutPlacementIntent(
    override val viewId: String,
    override val target: AthenaGraphCommandTarget,
    override val requestedPlacement: AthenaGraphPlacement,
) : AthenaGraphCommandIntent {
    override val intentId: AthenaGraphCommandIntentId = ADJUST_LAYOUT_PLACEMENT
    override val mutationCategory: AthenaMutationCategory = AthenaMutationCategory.PROJECTION_MUTATION
}

/**
 * Graph-originated request to connect two existing ports through the runtime-owned semantic command path.
 */
data class AthenaConnectPortsIntent(
    override val viewId: String,
    override val source: AthenaGraphCommandTarget,
    override val target: AthenaGraphCommandTarget,
) : AthenaGraphCommandIntent {
    override val intentId: AthenaGraphCommandIntentId = CONNECT_PORTS
    override val mutationCategory: AthenaMutationCategory = AthenaMutationCategory.SEMANTIC_MUTATION
    override val requestedPlacement: AthenaGraphPlacement? = null
}

/**
 * Inspectable runtime-owned command execution details attached to a graph-originated semantic mutation.
 */
data class AthenaGraphCommandExecution(
    val commandKind: AthenaCommandKind,
    val outcome: AthenaMutationOutcome,
    val commandId: String? = null,
    val changedSemanticIds: List<String> = emptyList(),
    val validationFeedback: List<AthenaMutationValidationFeedback> = emptyList(),
)

/**
 * Inspectable runtime-owned result of one graph command intent submission.
 *
 * This result always reports the Athena-owned intent boundary and may also carry downstream
 * semantic command execution details when the supported intent is allowed to execute immediately.
 */
sealed interface AthenaGraphCommandIntentResult {
    /**
     * Runtime project name associated with the intent submission.
     */
    val projectName: String

    /**
     * Stable Athena-owned intent identifier that was submitted.
     */
    val intentId: AthenaGraphCommandIntentId

    /**
     * Explicit mutation category attached to the intent.
     */
    val mutationCategory: AthenaMutationCategory

    /**
     * Optional source subject carried by the intent submission.
     */
    val source: AthenaGraphCommandTarget?

    /**
     * Projection view context under which the intent was submitted.
     */
    val viewId: String

    /**
     * Target subject carried by the intent submission.
     */
    val target: AthenaGraphCommandTarget

    /**
     * Optional requested placement carried by the intent submission.
     */
    val requestedPlacement: AthenaGraphPlacement?

    /**
     * Optional command execution details when the graph intent reached the semantic runtime path.
     */
    val execution: AthenaGraphCommandExecution?

    /**
     * Optional semantic diff inspection when the intent produced accepted semantic consequences.
     */
    val inspection: AthenaSemanticDiffInspection?
        get() = null

    /**
     * Optional governed semantic review artifact for accepted semantic mutations.
     */
    val semanticReview: AthenaSemanticMutationReview?
        get() = null
}

/**
 * Runtime accepted the graph command intent contract and preserved it for downstream mutation stages.
 */
data class AthenaGraphCommandIntentAccepted(
    override val projectName: String,
    override val intentId: AthenaGraphCommandIntentId,
    override val mutationCategory: AthenaMutationCategory,
    override val source: AthenaGraphCommandTarget? = null,
    override val viewId: String,
    override val target: AthenaGraphCommandTarget,
    override val requestedPlacement: AthenaGraphPlacement? = null,
    override val execution: AthenaGraphCommandExecution? = null,
    override val inspection: AthenaSemanticDiffInspection? = null,
    override val semanticReview: AthenaSemanticMutationReview? = null,
) : AthenaGraphCommandIntentResult

/**
 * Runtime rejected the graph command intent contract before any mutation execution occurred.
 */
data class AthenaGraphCommandIntentRejected(
    override val projectName: String,
    override val intentId: AthenaGraphCommandIntentId,
    override val mutationCategory: AthenaMutationCategory,
    override val source: AthenaGraphCommandTarget? = null,
    override val viewId: String,
    override val target: AthenaGraphCommandTarget,
    override val requestedPlacement: AthenaGraphPlacement? = null,
    override val execution: AthenaGraphCommandExecution? = null,
    val reason: String,
) : AthenaGraphCommandIntentResult

/**
 * Runtime accepted the graph command-intent contract but returned caller-visible validation feedback.
 */
data class AthenaGraphCommandIntentValidationFeedback(
    override val projectName: String,
    override val intentId: AthenaGraphCommandIntentId,
    override val mutationCategory: AthenaMutationCategory,
    override val source: AthenaGraphCommandTarget? = null,
    override val viewId: String,
    override val target: AthenaGraphCommandTarget,
    override val requestedPlacement: AthenaGraphPlacement? = null,
    override val execution: AthenaGraphCommandExecution? = null,
    val validationFeedback: List<AthenaMutationValidationFeedback>,
) : AthenaGraphCommandIntentResult {
    init {
        require(validationFeedback.isNotEmpty()) {
            "Graph command intent validation feedback must include at least one feedback item."
        }
    }
}

/**
 * Runtime could not evaluate the graph command intent contract because projection state was unavailable.
 */
data class AthenaGraphCommandIntentUnavailable(
    override val projectName: String,
    override val intentId: AthenaGraphCommandIntentId,
    override val mutationCategory: AthenaMutationCategory,
    override val source: AthenaGraphCommandTarget? = null,
    override val viewId: String,
    override val target: AthenaGraphCommandTarget,
    override val requestedPlacement: AthenaGraphPlacement? = null,
    override val execution: AthenaGraphCommandExecution? = null,
    val reason: String,
) : AthenaGraphCommandIntentResult

/**
 * Runtime-owned evaluator for graph command intents.
 *
 * Projection intents may stop at contract validation. Supported semantic intents may continue into
 * the existing runtime command path and return inspectable execution details.
 */
class AthenaGraphCommandIntentRuntimeService internal constructor() {
    /**
     * Validates [intent] against the active projection session and returns one inspectable Athena-owned result.
     */
    fun submit(
        context: AthenaExecutionContext,
        intent: AthenaGraphCommandIntent,
    ): AthenaGraphCommandIntentResult {
        val session = context.projectProjectionSession()
        val projectName = context.project.name
        val supportedView = session.supportedViews.firstOrNull { view -> view.viewId == intent.viewId }
            ?: return AthenaGraphCommandIntentRejected(
                projectName = projectName,
                intentId = intent.intentId,
                mutationCategory = intent.mutationCategory,
                source = intent.source,
                viewId = intent.viewId,
                target = intent.target,
                requestedPlacement = intent.requestedPlacement,
                reason = "Projection view `${intent.viewId}` is not supported for project `${context.project.name}`.",
            )

        if (session.activeViewId != intent.viewId) {
            return AthenaGraphCommandIntentRejected(
                projectName = projectName,
                intentId = intent.intentId,
                mutationCategory = intent.mutationCategory,
                source = intent.source,
                viewId = intent.viewId,
                target = intent.target,
                requestedPlacement = intent.requestedPlacement,
                reason = "Projection view `${intent.viewId}` is not the active view for project `${context.project.name}`.",
            )
        }

        val activeProjection = session.activeProjection
        if (activeProjection is AthenaRuntimeProjectionUnavailableSnapshot) {
            return AthenaGraphCommandIntentUnavailable(
                projectName = projectName,
                intentId = intent.intentId,
                mutationCategory = intent.mutationCategory,
                source = intent.source,
                viewId = intent.viewId,
                target = intent.target,
                requestedPlacement = intent.requestedPlacement,
                reason = activeProjection.reason,
            )
        }
        check(activeProjection is AthenaRuntimeProjectionReadySnapshot) {
            "Graph command intent evaluation requires either a ready or unavailable projection snapshot."
        }

        return when (intent) {
            is AthenaAdjustLayoutPlacementIntent -> executeAdjustLayoutPlacement(
                context = context,
                projectName = projectName,
                supportedView = supportedView,
                activeProjection = activeProjection,
                intent = intent,
            )

            is AthenaConnectPortsIntent -> executeConnectPorts(
                context = context,
                projectName = projectName,
                supportedView = supportedView,
                intent = intent,
            )
        }
    }

    private fun executeAdjustLayoutPlacement(
        context: AthenaExecutionContext,
        projectName: String,
        supportedView: AthenaRuntimeProjectionView,
        activeProjection: AthenaRuntimeProjectionReadySnapshot,
        intent: AthenaAdjustLayoutPlacementIntent,
    ): AthenaGraphCommandIntentResult {
        if (!supportedView.ownershipContract.isInteractive) {
            return AthenaGraphCommandIntentRejected(
                projectName = projectName,
                intentId = intent.intentId,
                mutationCategory = intent.mutationCategory,
                source = intent.source,
                viewId = intent.viewId,
                target = intent.target,
                requestedPlacement = intent.requestedPlacement,
                reason = "Projection view `${intent.viewId}` is inspect-only and cannot emit `adjust-layout-placement`.",
            )
        }

        if (!supportedView.ownershipContract.mayEmitProjectionCommand("adjust-layout-placement")) {
            return AthenaGraphCommandIntentRejected(
                projectName = projectName,
                intentId = intent.intentId,
                mutationCategory = intent.mutationCategory,
                source = intent.source,
                viewId = intent.viewId,
                target = intent.target,
                requestedPlacement = intent.requestedPlacement,
                reason = "Projection view `${intent.viewId}` does not own the `adjust-layout-placement` command intent.",
            )
        }

        if (intent.target.subjectKind != COMPONENT) {
            return AthenaGraphCommandIntentRejected(
                projectName = projectName,
                intentId = intent.intentId,
                mutationCategory = intent.mutationCategory,
                source = intent.source,
                viewId = intent.viewId,
                target = intent.target,
                requestedPlacement = intent.requestedPlacement,
                reason = "Projection placement intent currently supports only `${COMPONENT.name.lowercase()}` subjects.",
            )
        }

        if (activeProjection.scene.components.none { component -> component.semanticId == intent.target.semanticId }) {
            return AthenaGraphCommandIntentRejected(
                projectName = projectName,
                intentId = intent.intentId,
                mutationCategory = intent.mutationCategory,
                source = intent.source,
                viewId = intent.viewId,
                target = intent.target,
                requestedPlacement = intent.requestedPlacement,
                reason = "Projection placement target `${intent.target.semanticId}` is not present in active view `${intent.viewId}`.",
            )
        }

        context.replaceProjectionPlacementOverride(
            viewId = intent.viewId,
            semanticId = intent.target.semanticId,
            placement = intent.requestedPlacement,
        )

        return AthenaGraphCommandIntentAccepted(
            projectName = projectName,
            intentId = intent.intentId,
            mutationCategory = intent.mutationCategory,
            source = intent.source,
            viewId = intent.viewId,
            target = intent.target,
            requestedPlacement = intent.requestedPlacement,
        )
    }

    private fun executeConnectPorts(
        context: AthenaExecutionContext,
        projectName: String,
        supportedView: AthenaRuntimeProjectionView,
        intent: AthenaConnectPortsIntent,
    ): AthenaGraphCommandIntentResult {
        if (!supportedView.ownershipContract.isInteractive) {
            return AthenaGraphCommandIntentRejected(
                projectName = projectName,
                intentId = intent.intentId,
                mutationCategory = intent.mutationCategory,
                source = intent.source,
                viewId = intent.viewId,
                target = intent.target,
                reason = "Projection view `${intent.viewId}` is inspect-only and cannot emit `connect-ports`.",
            )
        }

        if (!supportedView.ownershipContract.mayEmitSemanticCommand("connect-ports")) {
            return AthenaGraphCommandIntentRejected(
                projectName = projectName,
                intentId = intent.intentId,
                mutationCategory = intent.mutationCategory,
                source = intent.source,
                viewId = intent.viewId,
                target = intent.target,
                reason = "Projection view `${intent.viewId}` does not own the `connect-ports` command intent.",
            )
        }

        if (intent.source.subjectKind != PORT || intent.target.subjectKind != PORT) {
            return AthenaGraphCommandIntentRejected(
                projectName = projectName,
                intentId = intent.intentId,
                mutationCategory = intent.mutationCategory,
                source = intent.source,
                viewId = intent.viewId,
                target = intent.target,
                reason = "Semantic connect intent currently supports only `${PORT.name.lowercase()}` subjects.",
            )
        }

        return when (
            val execution = context.commandRuntime().execute(
                context = context,
                command = AthenaConnectPortsCommand(
                    sourcePortSemanticId = intent.source.semanticId,
                    targetPortSemanticId = intent.target.semanticId,
                ),
            )
        ) {
            is AthenaCommandExecutionSuccess -> AthenaGraphCommandIntentAccepted(
                projectName = projectName,
                intentId = intent.intentId,
                mutationCategory = intent.mutationCategory,
                source = intent.source,
                viewId = intent.viewId,
                target = intent.target,
                execution = AthenaGraphCommandExecution(
                    commandKind = execution.commandKind,
                    outcome = execution.outcome,
                    commandId = execution.commandId,
                    changedSemanticIds = execution.changedSemanticIds.sorted(),
                ),
                inspection = context.latestSemanticDiffInspection(),
                semanticReview = context.semanticMutationReviews().summarizeAcceptedMutation(
                    context = context,
                    beforeDocument = execution.beforeDocument,
                    afterDocument = execution.afterDocument,
                ),
            )

            is AthenaCommandExecutionRejected -> AthenaGraphCommandIntentRejected(
                projectName = projectName,
                intentId = intent.intentId,
                mutationCategory = intent.mutationCategory,
                source = intent.source,
                viewId = intent.viewId,
                target = intent.target,
                execution = AthenaGraphCommandExecution(
                    commandKind = execution.commandKind,
                    outcome = execution.outcome,
                    changedSemanticIds = execution.changedSemanticIds.sorted(),
                ),
                reason = execution.reason,
            )

            is AthenaCommandExecutionValidationFeedback -> AthenaGraphCommandIntentValidationFeedback(
                projectName = projectName,
                intentId = intent.intentId,
                mutationCategory = intent.mutationCategory,
                source = intent.source,
                viewId = intent.viewId,
                target = intent.target,
                execution = AthenaGraphCommandExecution(
                    commandKind = execution.commandKind,
                    outcome = execution.outcome,
                    changedSemanticIds = execution.changedSemanticIds.sorted(),
                    validationFeedback = execution.validationFeedback,
                ),
                validationFeedback = execution.validationFeedback,
            )

            is AthenaCommandExecutionUnavailable -> AthenaGraphCommandIntentUnavailable(
                projectName = projectName,
                intentId = intent.intentId,
                mutationCategory = intent.mutationCategory,
                source = intent.source,
                viewId = intent.viewId,
                target = intent.target,
                execution = AthenaGraphCommandExecution(
                    commandKind = execution.commandKind,
                    outcome = execution.outcome,
                ),
                reason = execution.reason,
            )
        }
    }
}

private fun ProjectionOwnershipContract.mayEmitProjectionCommand(commandId: String): Boolean {
    return isInteractive && projectionCommandIds.contains(commandId)
}

private fun ProjectionOwnershipContract.mayEmitSemanticCommand(commandId: String): Boolean {
    return isInteractive && semanticCommandIds.contains(commandId)
}
