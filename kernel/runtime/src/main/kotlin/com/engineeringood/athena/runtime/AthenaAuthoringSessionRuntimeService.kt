package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AcceptAuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringIntent
import com.engineeringood.athena.authoring.AuthoringPreview
import com.engineeringood.athena.authoring.AuthoringPreviewChange
import com.engineeringood.athena.authoring.AuthoringPreviewChangeKind
import com.engineeringood.athena.authoring.AuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.AuthoringPreviewStatus
import com.engineeringood.athena.authoring.ConnectPortsIntent
import com.engineeringood.athena.authoring.CreateComponentIntent
import com.engineeringood.athena.authoring.RejectAuthoringPreviewDecision
import com.engineeringood.athena.authoring.RevealSubjectIntent
import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.authoring.UpdateComponentPropertiesIntent
import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Runtime-owned record linking one guided authoring intent to its current inspectable preview.
 */
data class AthenaAuthoringSessionRecord(
    val intent: AuthoringIntent,
    val preview: AuthoringPreview,
)

/**
 * Serializable snapshot of runtime-owned guided authoring preview state for one active project.
 */
data class AthenaAuthoringSessionSnapshot(
    val records: List<AthenaAuthoringSessionRecord>,
    val nextPreviewOrdinal: Int,
)

/**
 * Inspectable runtime-owned view of the current guided authoring preview session.
 */
data class AthenaAuthoringSessionView(
    val records: List<AthenaAuthoringSessionRecord>,
    val pendingPreviewCount: Int,
)

/**
 * Outcome of submitting one guided authoring intent into runtime-owned preview state.
 */
sealed interface AthenaAuthoringPreviewSubmissionResult

/**
 * Runtime successfully recorded one inspectable preview for the submitted authoring intent.
 */
data class AthenaAuthoringPreviewSubmitted(
    val record: AthenaAuthoringSessionRecord,
) : AthenaAuthoringPreviewSubmissionResult

/**
 * Outcome of applying one explicit decision to a stored guided authoring preview.
 */
sealed interface AthenaAuthoringPreviewDecisionResult

/**
 * Runtime successfully updated one stored preview decision without mutating canonical engineering truth.
 */
data class AthenaAuthoringPreviewDecisionUpdated(
    val record: AthenaAuthoringSessionRecord,
) : AthenaAuthoringPreviewDecisionResult

/**
 * Runtime could not update one preview decision because no matching stored preview exists.
 */
data class AthenaAuthoringPreviewDecisionUnavailable(
    val previewId: AuthoringPreviewId,
    val reason: String,
) : AthenaAuthoringPreviewDecisionResult

/**
 * Runtime-owned authoring preview orchestrator above future M8 mutation handoff.
 *
 * This service accepts guided authoring intents, expands them into deterministic inspectable
 * previews, and records explicit review decisions. It does not execute canonical mutation commits.
 */
class AthenaAuthoringSessionRuntimeService internal constructor() {
    /**
     * Records one runtime-owned preview for the supplied guided authoring intent.
     */
    fun submit(
        context: AthenaExecutionContext,
        intent: AuthoringIntent,
    ): AthenaAuthoringPreviewSubmissionResult {
        val state = context.authoringSessionState()
        val preview = intent.toPreview(
            previewId = AuthoringPreviewId(
                "authoring-preview-${state.nextPreviewOrdinal.toString().padStart(4, '0')}",
            ),
        )
        val record = AthenaAuthoringSessionRecord(
            intent = intent,
            preview = preview,
        )
        context.replaceAuthoringSessionState(
            state.copy(
                records = state.records + record,
                nextPreviewOrdinal = state.nextPreviewOrdinal + 1,
            ),
        )
        return AthenaAuthoringPreviewSubmitted(record)
    }

    /**
     * Returns the current inspectable preview state for the active project.
     */
    fun state(context: AthenaExecutionContext): AthenaAuthoringSessionView {
        val records = context.authoringSessionState().records
        return AthenaAuthoringSessionView(
            records = records,
            pendingPreviewCount = records.count { record ->
                record.preview.status == AuthoringPreviewStatus.PENDING_REVIEW
            },
        )
    }

    /**
     * Returns a deterministic snapshot of stored guided authoring preview state.
     */
    fun snapshot(context: AthenaExecutionContext): AthenaAuthoringSessionSnapshot {
        val state = context.authoringSessionState()
        return AthenaAuthoringSessionSnapshot(
            records = state.records,
            nextPreviewOrdinal = state.nextPreviewOrdinal,
        )
    }

    /**
     * Restores stored guided authoring preview state from a runtime-owned snapshot.
     */
    fun restoreSession(
        context: AthenaExecutionContext,
        snapshot: AthenaAuthoringSessionSnapshot,
    ) {
        context.replaceAuthoringSessionState(
            AthenaAuthoringSessionState(
                records = snapshot.records,
                nextPreviewOrdinal = snapshot.nextPreviewOrdinal,
            ),
        )
    }

    /**
     * Applies one explicit preview decision without mutating canonical engineering truth.
     */
    fun applyDecision(
        context: AthenaExecutionContext,
        decision: AuthoringPreviewDecision,
    ): AthenaAuthoringPreviewDecisionResult {
        val state = context.authoringSessionState()
        val recordIndex = state.records.indexOfFirst { record ->
            record.preview.previewId == decision.previewId
        }
        if (recordIndex < 0) {
            return AthenaAuthoringPreviewDecisionUnavailable(
                previewId = decision.previewId,
                reason = "Authoring preview `${decision.previewId.value}` is not present in the active runtime session.",
            )
        }

        val record = state.records[recordIndex]
        if (record.preview.intentId != decision.intentId) {
            return AthenaAuthoringPreviewDecisionUnavailable(
                previewId = decision.previewId,
                reason = "Authoring preview `${decision.previewId.value}` does not match intent `${decision.intentId.value}`.",
            )
        }

        val updatedRecord = record.copy(
            preview = record.preview.copy(status = decision.toPreviewStatus()),
        )
        val updatedRecords = state.records.toMutableList().apply {
            set(recordIndex, updatedRecord)
        }.toList()
        context.replaceAuthoringSessionState(
            state.copy(records = updatedRecords),
        )
        return AthenaAuthoringPreviewDecisionUpdated(updatedRecord)
    }
}

/**
 * Internal runtime-owned guided authoring preview state for one active project.
 */
internal data class AthenaAuthoringSessionState(
    val records: List<AthenaAuthoringSessionRecord> = emptyList(),
    val nextPreviewOrdinal: Int = 1,
)

private fun AuthoringIntent.toPreview(previewId: AuthoringPreviewId): AuthoringPreview {
    return when (this) {
        is CreateComponentIntent -> AuthoringPreview(
            previewId = previewId,
            intentId = intentId,
            title = "Create component preview",
            changes = listOf(
                AuthoringPreviewChange(
                    kind = AuthoringPreviewChangeKind.CREATE,
                    title = suggestedName ?: conceptId.value,
                    summary = buildString {
                        append("Create component from concept `")
                        append(conceptId.value)
                        append("` under `")
                        append(parentIdentity.value)
                        append("`.")
                        preferredImplementationId?.let { implementationId ->
                            append(" Preferred implementation: `")
                            append(implementationId.value)
                            append("`.")
                        }
                    },
                    affectedSubjectIdentities = buildSet {
                        add(parentIdentity)
                        suggestedName?.takeIf(String::isNotBlank)?.let { name ->
                            add(StableSemanticIdentity("component:$name"))
                        }
                    },
                ),
            ),
        )

        is UpdateComponentPropertiesIntent -> AuthoringPreview(
            previewId = previewId,
            intentId = intentId,
            title = "Update component properties preview",
            changes = listOf(
                AuthoringPreviewChange(
                    kind = AuthoringPreviewChangeKind.UPDATE,
                    title = componentId.value,
                    summary = buildString {
                        append("Update ")
                        append(
                            properties.keys
                                .map { propertyName -> propertyName.value }
                                .sorted()
                                .joinToString(separator = ", ")
                                .ifBlank { "${properties.size} guided authoring propert${if (properties.size == 1) "y" else "ies"}" },
                        )
                        append(".")
                    },
                    affectedSubjectIdentities = setOf(componentId),
                ),
            ),
        )

        is ConnectPortsIntent -> AuthoringPreview(
            previewId = previewId,
            intentId = intentId,
            title = "Connect ports preview",
            changes = listOf(
                AuthoringPreviewChange(
                    kind = AuthoringPreviewChangeKind.CONNECT,
                    title = "${sourcePortId.value} -> ${targetPortId.value}",
                    summary = "Create one guided semantic connection between the selected ports.",
                    affectedSubjectIdentities = setOf(sourcePortId, targetPortId),
                ),
            ),
        )

        is SemanticRelationshipIntent -> AuthoringPreview(
            previewId = previewId,
            intentId = intentId,
            title = "Semantic relationship preview",
            changes = listOf(
                AuthoringPreviewChange(
                    kind = AuthoringPreviewChangeKind.CONNECT,
                    title = "${sourceSubjectId.value} -> ${targetSubjectId.value}",
                    summary = "Create one governed `${relationshipType.value}` semantic relationship between the selected subjects.",
                    affectedSubjectIdentities = setOf(sourceSubjectId, targetSubjectId),
                ),
            ),
        )

        is RevealSubjectIntent -> AuthoringPreview(
            previewId = previewId,
            intentId = intentId,
            title = "Reveal subject preview",
            changes = listOf(
                AuthoringPreviewChange(
                    kind = AuthoringPreviewChangeKind.REVEAL,
                    title = subjectId.value,
                    summary = "Reveal the canonical subject across ${targets.size} workbench target${if (targets.size == 1) "" else "s"}.",
                    affectedSubjectIdentities = setOf(subjectId),
                ),
            ),
        )
    }
}

private fun AuthoringPreviewDecision.toPreviewStatus(): AuthoringPreviewStatus {
    return when (this) {
        is AcceptAuthoringPreviewDecision -> AuthoringPreviewStatus.ACCEPTED
        is RejectAuthoringPreviewDecision -> AuthoringPreviewStatus.REJECTED
    }
}
