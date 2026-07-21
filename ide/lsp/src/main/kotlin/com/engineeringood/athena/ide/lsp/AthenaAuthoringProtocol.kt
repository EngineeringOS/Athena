package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.authoring.AcceptAuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringIntent
import com.engineeringood.athena.authoring.AuthoringIntentId
import com.engineeringood.athena.authoring.AuthoringOrigin
import com.engineeringood.athena.authoring.AuthoringPreviewChange
import com.engineeringood.athena.authoring.AuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringPreviewDecisionKind
import com.engineeringood.athena.authoring.AuthoringPreviewStatus
import com.engineeringood.athena.authoring.AuthoringPropertyName
import com.engineeringood.athena.authoring.AuthoringRevealTarget
import com.engineeringood.athena.authoring.AuthoringSurface
import com.engineeringood.athena.authoring.AuthoringValue
import com.engineeringood.athena.authoring.ConnectPortsIntent
import com.engineeringood.athena.authoring.CreateComponentIntent
import com.engineeringood.athena.authoring.ElectricalConnectionRelationship
import com.engineeringood.athena.authoring.RejectAuthoringPreviewDecision
import com.engineeringood.athena.authoring.RevealSubjectIntent
import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.authoring.SemanticRelationshipPersistenceTarget
import com.engineeringood.athena.authoring.SemanticRelationshipProjectionContext
import com.engineeringood.athena.authoring.SemanticRelationshipType
import com.engineeringood.athena.authoring.UpdateComponentPropertiesIntent
import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.part.PartImplementationId
import com.engineeringood.athena.runtime.AthenaAuthoringPreviewDecisionResult
import com.engineeringood.athena.runtime.AthenaAuthoringPreviewDecisionUnavailable
import com.engineeringood.athena.runtime.AthenaAuthoringPreviewDecisionUpdated
import com.engineeringood.athena.runtime.AthenaAuthoringPreviewSubmitted
import com.engineeringood.athena.runtime.AthenaAuthoringSessionRecord
import com.engineeringood.athena.runtime.AthenaAuthoringSessionView

/**
 * Empty params object for runtime-owned guided authoring state inspection requests.
 */
data class AthenaAuthoringStateParams(
    val marker: String = "m15",
)

/**
 * Transport-safe typed value used by guided authoring requests across the Athena LSP boundary.
 */
data class AthenaAuthoringValuePayload(
    val kind: String,
    val text: String? = null,
    val booleanValue: Boolean? = null,
    val integerValue: Int? = null,
)

/**
 * Transport DTO for submitting one guided authoring preview request through Athena LSP.
 */
data class AthenaAuthoringPreviewParams(
    val intentId: String,
    val intentKind: String,
    val originSurface: String,
    val originDetail: String? = null,
    val parentIdentity: String? = null,
    val conceptId: String? = null,
    val preferredImplementationId: String? = null,
    val suggestedName: String? = null,
    val componentId: String? = null,
    val properties: Map<String, AthenaAuthoringValuePayload> = emptyMap(),
    val sourcePortId: String? = null,
    val targetPortId: String? = null,
    val relationshipType: String? = null,
    val sourceSubjectId: String? = null,
    val targetSubjectId: String? = null,
    val projectionViewId: String? = null,
    val projectionOccurrenceId: String? = null,
    val persistenceSourceUri: String? = null,
    val provenance: String? = null,
    val subjectId: String? = null,
    val revealTargets: List<String> = emptyList(),
)

/**
 * Transport DTO for one inspectable preview change.
 */
data class AthenaAuthoringPreviewChangePayload(
    val kind: String,
    val title: String,
    val summary: String? = null,
    val affectedSubjectIdentities: List<String>,
)

/**
 * Transport DTO for one runtime-owned guided authoring preview.
 */
data class AthenaAuthoringPreviewPayload(
    val previewId: String,
    val intentId: String,
    val intentKind: String,
    val originSurface: String,
    val originDetail: String? = null,
    val status: String,
    val title: String,
    val changes: List<AthenaAuthoringPreviewChangePayload>,
    val warnings: List<String>,
    val sourceImpact: AthenaAuthoringSourceEditPayload? = null,
)

/**
 * Transport DTO returned after one guided authoring preview submission.
 */
data class AthenaAuthoringPreviewSubmissionPayload(
    val projectName: String,
    val semanticPath: String,
    val status: String,
    val preview: AthenaAuthoringPreviewPayload,
)

/**
 * Transport DTO for runtime-owned guided authoring state inspection.
 */
data class AthenaAuthoringStatePayload(
    val projectName: String,
    val semanticPath: String,
    val status: String,
    val pendingPreviewCount: Int,
    val previews: List<AthenaAuthoringPreviewPayload>,
)

/**
 * Transport DTO for one explicit guided authoring preview decision.
 */
data class AthenaAuthoringDecisionParams(
    val previewId: String,
    val intentId: String,
    val decision: String,
    val note: String? = null,
)

/**
 * Transport DTO returned after one explicit guided authoring preview decision.
 */
data class AthenaAuthoringPreviewDecisionPayload(
    val projectName: String,
    val semanticPath: String,
    val status: String,
    val preview: AthenaAuthoringPreviewPayload? = null,
    val sourceEdit: AthenaAuthoringSourceEditPayload? = null,
    val reason: String? = null,
)

internal fun AthenaAuthoringPreviewParams.toRuntimeIntent(): AuthoringIntent {
    val origin = AuthoringOrigin(
        surface = originSurface.toAuthoringSurface(),
        detail = originDetail?.takeIf { detail -> detail.isNotBlank() },
    )
    return when (intentKind.trim().lowercase()) {
        "create-component" -> CreateComponentIntent(
            intentId = AuthoringIntentId(intentId),
            origin = origin,
            parentIdentity = StableSemanticIdentity(
                requireString(parentIdentity, "parentIdentity"),
            ),
            conceptId = EngineeringConceptId(requireString(conceptId, "conceptId")),
            preferredImplementationId = preferredImplementationId
                ?.takeIf { implementationId -> implementationId.isNotBlank() }
                ?.let(::PartImplementationId),
            suggestedName = suggestedName?.takeIf { name -> name.isNotBlank() },
        )

        "update-component-properties" -> UpdateComponentPropertiesIntent(
            intentId = AuthoringIntentId(intentId),
            origin = origin,
            componentId = StableSemanticIdentity(requireString(componentId, "componentId")),
            properties = properties.mapKeys { (propertyName, _) ->
                AuthoringPropertyName(propertyName)
            }.mapValues { (_, value) ->
                value.toRuntimeValue()
            },
        )

        "connect-ports" -> ConnectPortsIntent(
            intentId = AuthoringIntentId(intentId),
            origin = origin,
            sourcePortId = StableSemanticIdentity(requireString(sourcePortId, "sourcePortId")),
            targetPortId = StableSemanticIdentity(requireString(targetPortId, "targetPortId")),
        )

        "semantic-relationship" -> SemanticRelationshipIntent(
            intentId = AuthoringIntentId(intentId),
            origin = origin,
            relationshipType = relationshipType.toSemanticRelationshipType(),
            sourceSubjectId = StableSemanticIdentity(requireString(sourceSubjectId, "sourceSubjectId")),
            targetSubjectId = StableSemanticIdentity(requireString(targetSubjectId, "targetSubjectId")),
            projectionContext = SemanticRelationshipProjectionContext(
                viewId = projectionViewId?.takeIf { value -> value.isNotBlank() },
                occurrenceId = projectionOccurrenceId?.takeIf { value -> value.isNotBlank() },
            ),
            persistenceTarget = SemanticRelationshipPersistenceTarget(
                sourceUri = persistenceSourceUri?.takeIf { value -> value.isNotBlank() },
            ),
            provenance = provenance?.takeIf { value -> value.isNotBlank() },
        )

        "reveal-subject" -> RevealSubjectIntent(
            intentId = AuthoringIntentId(intentId),
            origin = origin,
            subjectId = StableSemanticIdentity(requireString(subjectId, "subjectId")),
            targets = revealTargets.map(String::toRevealTarget).toSet(),
        )

        else -> error(
            "Athena authoring intentKind must be one of create-component, update-component-properties, connect-ports, semantic-relationship, or reveal-subject.",
        )
    }
}

internal fun AthenaAuthoringDecisionParams.toRuntimeDecision(): AuthoringPreviewDecision {
    return when (decision.trim().lowercase()) {
        "accept", "accepted" -> AcceptAuthoringPreviewDecision(
            previewId = com.engineeringood.athena.authoring.AuthoringPreviewId(previewId),
            intentId = AuthoringIntentId(intentId),
            note = note?.takeIf { detail -> detail.isNotBlank() },
        )

        "reject", "rejected" -> RejectAuthoringPreviewDecision(
            previewId = com.engineeringood.athena.authoring.AuthoringPreviewId(previewId),
            intentId = AuthoringIntentId(intentId),
            reason = note?.takeIf { detail -> detail.isNotBlank() },
        )

        else -> error("Athena authoring decision must be one of accept, accepted, reject, or rejected.")
    }
}

internal fun AthenaLspSessionHostReady.authoringStatePayload(
    semanticPath: String,
): AthenaAuthoringStatePayload {
    return context.authoringSessions()
        .state(context)
        .toPayload(
            projectName = context.project.name,
            semanticPath = semanticPath,
        )
}

internal fun AthenaAuthoringPreviewSubmitted.toPayload(
    projectName: String,
    semanticPath: String,
    sourceImpact: AthenaAuthoringSourceEditPayload? = null,
): AthenaAuthoringPreviewSubmissionPayload {
    return AthenaAuthoringPreviewSubmissionPayload(
        projectName = projectName,
        semanticPath = semanticPath,
        status = "submitted",
        preview = record.toPayload(sourceImpact = sourceImpact),
    )
}

internal fun AthenaAuthoringPreviewDecisionResult.toPayload(
    projectName: String,
    semanticPath: String,
    sourceEdit: AthenaAuthoringSourceEditPayload? = null,
): AthenaAuthoringPreviewDecisionPayload {
    return when (this) {
        is AthenaAuthoringPreviewDecisionUpdated -> AthenaAuthoringPreviewDecisionPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "updated",
            preview = record.toPayload(),
            sourceEdit = sourceEdit,
        )

        is AthenaAuthoringPreviewDecisionUnavailable -> AthenaAuthoringPreviewDecisionPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "unavailable",
            reason = reason,
        )
    }
}

private fun AthenaAuthoringSessionView.toPayload(
    projectName: String,
    semanticPath: String,
): AthenaAuthoringStatePayload {
    return AthenaAuthoringStatePayload(
        projectName = projectName,
        semanticPath = semanticPath,
        status = "ready",
        pendingPreviewCount = pendingPreviewCount,
        previews = records.map(AthenaAuthoringSessionRecord::toPayload),
    )
}

private fun AthenaAuthoringSessionRecord.toPayload(
    sourceImpact: AthenaAuthoringSourceEditPayload? = null,
): AthenaAuthoringPreviewPayload {
    return AthenaAuthoringPreviewPayload(
        previewId = preview.previewId.value,
        intentId = preview.intentId.value,
        intentKind = intent.toProtocolIntentKind(),
        originSurface = intent.origin.surface.toProtocolValue(),
        originDetail = intent.origin.detail,
        status = preview.status.toProtocolValue(),
        title = preview.title,
        changes = preview.changes.map(AuthoringPreviewChange::toPayload),
        warnings = preview.warnings,
        sourceImpact = sourceImpact,
    )
}

private fun AuthoringPreviewChange.toPayload(): AthenaAuthoringPreviewChangePayload {
    return AthenaAuthoringPreviewChangePayload(
        kind = kind.toProtocolValue(),
        title = title,
        summary = summary,
        affectedSubjectIdentities = affectedSubjectIdentities.map { identity -> identity.value }.sorted(),
    )
}

private fun AthenaAuthoringValuePayload.toRuntimeValue(): AuthoringValue {
    return when (kind.trim().lowercase()) {
        "text" -> AuthoringValue.Text(requireString(text, "text"))
        "symbol" -> AuthoringValue.Symbol(requireString(text, "text"))
        "boolean" -> AuthoringValue.BooleanValue(booleanValue ?: error("Athena authoring booleanValue is required."))
        "integer" -> AuthoringValue.IntegerValue(integerValue ?: error("Athena authoring integerValue is required."))
        else -> error("Athena authoring value kind must be one of text, symbol, boolean, or integer.")
    }
}

private fun AuthoringIntent.toProtocolIntentKind(): String {
    return when (this) {
        is CreateComponentIntent -> "create-component"
        is UpdateComponentPropertiesIntent -> "update-component-properties"
        is ConnectPortsIntent -> "connect-ports"
        is SemanticRelationshipIntent -> "semantic-relationship"
        is RevealSubjectIntent -> "reveal-subject"
    }
}

private fun String?.toSemanticRelationshipType(): SemanticRelationshipType {
    val normalized = requireString(this, "relationshipType")
    return when (normalized) {
        ElectricalConnectionRelationship.value -> ElectricalConnectionRelationship
        else -> SemanticRelationshipType(normalized)
    }
}

private fun AuthoringSurface.toProtocolValue(): String {
    return name.lowercase().replace('_', '-')
}

private fun AuthoringPreviewStatus.toProtocolValue(): String {
    return name.lowercase().replace('_', '-')
}

private fun AuthoringPreviewDecisionKind.toProtocolValue(): String {
    return name.lowercase().replace('_', '-')
}

private fun Enum<*>.toProtocolValue(): String {
    return name.lowercase().replace('_', '-')
}

private fun String.toAuthoringSurface(): AuthoringSurface {
    return when (trim().lowercase()) {
        "palette" -> AuthoringSurface.PALETTE
        "inspector" -> AuthoringSurface.INSPECTOR
        "graph" -> AuthoringSurface.GRAPH
        "form" -> AuthoringSurface.FORM
        "template" -> AuthoringSurface.TEMPLATE
        "ai" -> AuthoringSurface.AI
        "api" -> AuthoringSurface.API
        "dsl" -> AuthoringSurface.DSL
        else -> error("Athena authoring originSurface is not recognized: `$this`.")
    }
}

private fun String.toRevealTarget(): AuthoringRevealTarget {
    return when (trim().lowercase()) {
        "source" -> AuthoringRevealTarget.SOURCE
        "graph" -> AuthoringRevealTarget.GRAPH
        "inspector" -> AuthoringRevealTarget.INSPECTOR
        "semantic-scm" -> AuthoringRevealTarget.SEMANTIC_SCM
        else -> error("Athena authoring revealTarget is not recognized: `$this`.")
    }
}

private fun requireString(value: String?, fieldName: String): String {
    return value?.takeIf { text -> text.isNotBlank() }
        ?: error("Athena authoring field `$fieldName` is required for the selected request shape.")
}
