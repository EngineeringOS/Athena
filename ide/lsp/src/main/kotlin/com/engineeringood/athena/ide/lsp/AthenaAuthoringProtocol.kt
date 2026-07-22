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
import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.authoring.AuthoringRevealTarget
import com.engineeringood.athena.authoring.AuthoringSurface
import com.engineeringood.athena.authoring.AuthoringValue
import com.engineeringood.athena.authoring.CancelAuthoringPreviewDecision
import com.engineeringood.athena.authoring.CreateSemanticEntityIntent
import com.engineeringood.athena.authoring.ElectricalConnectionRelationship
import com.engineeringood.athena.authoring.RejectAuthoringPreviewDecision
import com.engineeringood.athena.authoring.RevealSubjectIntent
import com.engineeringood.athena.authoring.RemoveSemanticEntityIntent
import com.engineeringood.athena.authoring.RemoveSemanticRelationshipIntent
import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.authoring.SemanticRelationshipPersistenceTarget
import com.engineeringood.athena.authoring.SemanticRelationshipProjectionContext
import com.engineeringood.athena.authoring.SemanticRelationshipType
import com.engineeringood.athena.authoring.UpdateSemanticEntityPropertiesIntent
import com.engineeringood.athena.authoring.AuthoringTransactionProvenance
import com.engineeringood.athena.authoring.SemanticEntityCreationContext
import com.engineeringood.athena.authoring.GovernedEntityCreationPreviewEvidence
import com.engineeringood.athena.authoring.GovernedRelationshipPreviewEvidence
import com.engineeringood.athena.component.EngineeringConceptTemplateId
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
    val parentSubjectId: String? = null,
    val conceptTemplateId: String? = null,
    val conceptId: String? = null,
    val preferredImplementationId: String? = null,
    val suggestedName: String? = null,
    val entitySubjectId: String? = null,
    val properties: Map<String, AthenaAuthoringValuePayload> = emptyMap(),
    val relationshipType: String? = null,
    val sourceSubjectId: String? = null,
    val targetSubjectId: String? = null,
    val projectionViewId: String? = null,
    val projectionOccurrenceId: String? = null,
    val persistenceSourceUri: String? = null,
    val provenance: String? = null,
    val actor: String? = null,
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

data class AthenaAuthoringNestedPortEvidencePayload(
    val name: String,
    val direction: String,
    val signalOrMedium: String,
    val semanticId: String,
)

data class AthenaAuthoringEntityCreationEvidencePayload(
    val canonicalTag: String,
    val semanticType: String,
    val model: String?,
    val nestedPorts: List<AthenaAuthoringNestedPortEvidencePayload>,
    val affectedSemanticIds: List<String>,
    val sourceEdit: AthenaAuthoringSourceEditEvidencePayload,
    val representationId: String,
    val compositionTargetId: String,
    val projectionOccurrenceIds: List<String>,
)

data class AthenaAuthoringRelationshipRoutePreviewPayload(
    val routeId: String,
    val quality: String,
    val sourceAnchorId: String?,
    val targetAnchorId: String?,
    val pointCount: Int,
)

data class AthenaAuthoringRelationshipEvidencePayload(
    val sourceSubjectId: String,
    val targetSubjectId: String,
    val relationshipType: String,
    val compatibility: String,
    val affectedSemanticIds: List<String>,
    val sourceEdit: AthenaAuthoringSourceEditEvidencePayload?,
    val routePreview: AthenaAuthoringRelationshipRoutePreviewPayload?,
)

data class AthenaAuthoringSourceEditEvidencePayload(
    val uri: String,
    val startOffset: Int,
    val endOffset: Int,
    val admittedText: String,
    val selectionStartOffset: Int?,
    val selectionEndOffset: Int?,
    val affectedSemanticIds: List<String>,
    val revisionGuard: AthenaAuthoringRevisionGuardPayload,
)

data class AthenaAuthoringDiagnosticPayload(
    val code: String,
    val message: String,
    val authority: String,
    val lifecycleStage: String,
    val recoveryAction: String? = null,
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
    val acceptanceEligible: Boolean = true,
    val diagnostics: List<AthenaAuthoringDiagnosticPayload> = emptyList(),
    val entityCreationEvidence: AthenaAuthoringEntityCreationEvidencePayload? = null,
    val relationshipEvidence: AthenaAuthoringRelationshipEvidencePayload? = null,
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
    val transactionResult: AthenaAuthoringTransactionResultPayload? = null,
    val reason: String? = null,
)

data class AthenaAuthoringTransactionResultPayload(
    val lifecycleState: String,
    val committedRevision: AthenaAuthoringRevisionGuardPayload? = null,
    val mutationId: String? = null,
    val affectedSemanticIds: List<String> = emptyList(),
    val projectionOccurrenceIds: List<String> = emptyList(),
    val diagnostics: List<AthenaAuthoringDiagnosticPayload> = emptyList(),
)

internal fun AthenaAuthoringPreviewParams.toInvalidSubmissionPayload(
    projectName: String,
    semanticPath: String,
    reason: String,
): AthenaAuthoringPreviewSubmissionPayload = AthenaAuthoringPreviewSubmissionPayload(
    projectName = projectName,
    semanticPath = semanticPath,
    status = "blocked",
    preview = AthenaAuthoringPreviewPayload(
        previewId = "authoring-preview-invalid:${intentId.ifBlank { "request" }}",
        intentId = intentId,
        intentKind = intentKind,
        originSurface = originSurface,
        originDetail = originDetail,
        status = "blocked",
        title = "Invalid authoring request",
        changes = emptyList(),
        warnings = emptyList(),
        acceptanceEligible = false,
        diagnostics = listOf(
            AthenaAuthoringDiagnosticPayload(
                code = com.engineeringood.athena.authoring.AuthoringDiagnosticCode.SOURCE_INVALID.value,
                message = reason,
                authority = "transaction-runtime",
                lifecycleStage = "blocked",
                recoveryAction = "fix-source",
            ),
        ),
        relationshipEvidence = if (intentKind.trim().equals("semantic-relationship", ignoreCase = true)) {
            AthenaAuthoringRelationshipEvidencePayload(
                sourceSubjectId = sourceSubjectId.orEmpty(),
                targetSubjectId = targetSubjectId.orEmpty(),
                relationshipType = relationshipType.orEmpty(),
                compatibility = "not-evaluated",
                affectedSemanticIds = emptyList(),
                sourceEdit = null,
                routePreview = null,
            )
        } else {
            null
        },
    ),
)

internal fun AthenaAuthoringPreviewParams.toRuntimeIntent(revisionGuard: AuthoringRevisionGuard): AuthoringIntent {
    val origin = AuthoringOrigin(
        surface = originSurface.toAuthoringSurface(),
        detail = originDetail?.takeIf { detail -> detail.isNotBlank() },
    )
    return when (intentKind.trim().lowercase()) {
        "create-entity" -> CreateSemanticEntityIntent(
            intentId = AuthoringIntentId(intentId),
            origin = origin,
            creationContext = SemanticEntityCreationContext(
                parentSubjectId = StableSemanticIdentity(requireString(parentSubjectId, "parentSubjectId")),
                sourceUri = revisionGuard.sourceUri,
            ),
            conceptTemplateId = EngineeringConceptTemplateId(requireString(conceptTemplateId, "conceptTemplateId")),
            conceptId = EngineeringConceptId(requireString(conceptId, "conceptId")),
            preferredImplementationId = preferredImplementationId
                ?.takeIf { implementationId -> implementationId.isNotBlank() }
                ?.let(::PartImplementationId),
            suggestedName = suggestedName?.takeIf { name -> name.isNotBlank() },
            revisionGuard = revisionGuard,
            provenance = toEntityProvenance(origin),
        )

        "update-entity-properties" -> UpdateSemanticEntityPropertiesIntent(
            intentId = AuthoringIntentId(intentId),
            origin = origin,
            subjectId = StableSemanticIdentity(requireString(entitySubjectId, "entitySubjectId")),
            properties = properties.mapKeys { (propertyName, _) ->
                AuthoringPropertyName(propertyName)
            }.mapValues { (_, value) ->
                value.toRuntimeValue()
            },
            revisionGuard = revisionGuard,
            provenance = toEntityProvenance(origin),
        )

        "remove-entity" -> RemoveSemanticEntityIntent(
            intentId = AuthoringIntentId(intentId),
            origin = origin,
            subjectId = StableSemanticIdentity(requireString(entitySubjectId, "entitySubjectId")),
            revisionGuard = revisionGuard,
            provenance = toEntityProvenance(origin),
        )

        "semantic-relationship" -> SemanticRelationshipIntent(
            intentId = AuthoringIntentId(intentId),
            origin = origin,
            relationshipType = relationshipType.toSemanticRelationshipTypeOrMalformed(),
            sourceSubjectId = StableSemanticIdentity(sourceSubjectId.orEmpty().trim()),
            targetSubjectId = StableSemanticIdentity(targetSubjectId.orEmpty().trim()),
            projectionContext = SemanticRelationshipProjectionContext(
                viewId = projectionViewId?.takeIf { value -> value.isNotBlank() },
                occurrenceId = projectionOccurrenceId?.takeIf { value -> value.isNotBlank() },
            ),
            persistenceTarget = SemanticRelationshipPersistenceTarget(
                sourceUri = persistenceSourceUri?.takeIf { value -> value.isNotBlank() },
            ),
            provenance = provenance?.takeIf { value -> value.isNotBlank() },
        )

        "remove-semantic-relationship" -> RemoveSemanticRelationshipIntent(
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
            "Athena authoring intentKind must be one of create-entity, update-entity-properties, remove-entity, semantic-relationship, remove-semantic-relationship, or reveal-subject.",
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

        "cancel", "cancelled" -> CancelAuthoringPreviewDecision(
            previewId = com.engineeringood.athena.authoring.AuthoringPreviewId(previewId),
            intentId = AuthoringIntentId(intentId),
            reason = note?.takeIf { detail -> detail.isNotBlank() },
        )

        else -> error("Athena authoring decision must be one of accept, accepted, reject, rejected, cancel, or cancelled.")
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
        is AthenaAuthoringPreviewDecisionUpdated -> {
            val unavailable = transaction?.lifecycleState in setOf(
                com.engineeringood.athena.authoring.AuthoringLifecycleState.BLOCKED,
                com.engineeringood.athena.authoring.AuthoringLifecycleState.STALE,
            )
            AthenaAuthoringPreviewDecisionPayload(
                projectName = projectName,
                semanticPath = semanticPath,
                status = if (unavailable) "unavailable" else "updated",
                preview = record.toPayload(),
                sourceEdit = sourceEdit.takeUnless { unavailable },
                transactionResult = transaction?.result?.let { result ->
                    AthenaAuthoringTransactionResultPayload(
                        lifecycleState = result.lifecycleState.toProtocolValue(),
                        committedRevision = result.committedRevision?.toPayload(),
                        mutationId = result.mutationId,
                        affectedSemanticIds = result.affectedSemanticIds,
                        projectionOccurrenceIds = result.projectionOccurrenceIds,
                        diagnostics = result.diagnostics.map { diagnostic ->
                            AthenaAuthoringDiagnosticPayload(
                                code = diagnostic.code.value,
                                message = diagnostic.message,
                                authority = diagnostic.authority.toProtocolValue(),
                                lifecycleStage = diagnostic.lifecycleStage.toProtocolValue(),
                                recoveryAction = diagnostic.recoveryAction?.toProtocolValue(),
                            )
                        },
                    )
                },
                reason = transaction?.diagnostics
                    ?.takeIf { diagnostics -> diagnostics.isNotEmpty() }
                    ?.joinToString(separator = "; ") { diagnostic -> "${diagnostic.code.value}: ${diagnostic.message}" },
            )
        }

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
        acceptanceEligible = preview.acceptanceEligibility.eligible,
        diagnostics = preview.acceptanceEligibility.diagnostics.map { diagnostic ->
            AthenaAuthoringDiagnosticPayload(
                code = diagnostic.code.value,
                message = diagnostic.message,
                authority = diagnostic.authority.toProtocolValue(),
                lifecycleStage = diagnostic.lifecycleStage.toProtocolValue(),
                recoveryAction = diagnostic.recoveryAction?.toProtocolValue(),
            )
        },
        entityCreationEvidence = preview.entityCreationEvidence?.toPayload(),
        relationshipEvidence = preview.relationshipEvidence?.toPayload(),
    )
}

private fun GovernedEntityCreationPreviewEvidence.toPayload(): AthenaAuthoringEntityCreationEvidencePayload =
    AthenaAuthoringEntityCreationEvidencePayload(
        canonicalTag = canonicalTag,
        semanticType = semanticType,
        model = model,
        nestedPorts = nestedPorts.map { port ->
            AthenaAuthoringNestedPortEvidencePayload(
                name = port.name,
                direction = port.direction.name.lowercase(),
                signalOrMedium = port.signalOrMedium.value,
                semanticId = port.semanticId,
            )
        },
        affectedSemanticIds = affectedSemanticIds,
        sourceEdit = AthenaAuthoringSourceEditEvidencePayload(
            uri = sourceEdit.sourceUri,
            startOffset = sourceEdit.startOffset,
            endOffset = sourceEdit.endOffset,
            admittedText = sourceEdit.admittedText,
            selectionStartOffset = sourceEdit.selectionStartOffset,
            selectionEndOffset = sourceEdit.selectionEndOffset,
            affectedSemanticIds = sourceEdit.affectedSemanticIds,
            revisionGuard = sourceEdit.revisionGuard.toPayload(),
        ),
        representationId = representationId,
        compositionTargetId = compositionTargetId,
        projectionOccurrenceIds = projectionOccurrenceIds,
    )

private fun GovernedRelationshipPreviewEvidence.toPayload(): AthenaAuthoringRelationshipEvidencePayload =
    AthenaAuthoringRelationshipEvidencePayload(
        sourceSubjectId = sourceSubjectId,
        targetSubjectId = targetSubjectId,
        relationshipType = relationshipType.value,
        compatibility = compatibility.name.lowercase().replace('_', '-'),
        affectedSemanticIds = affectedSemanticIds,
        sourceEdit = sourceEdit?.toEvidencePayload(),
        routePreview = routePreview?.let { route ->
            AthenaAuthoringRelationshipRoutePreviewPayload(
                routeId = route.routeId,
                quality = route.quality.lowercase().replace('_', '-'),
                sourceAnchorId = route.sourceAnchorId,
                targetAnchorId = route.targetAnchorId,
                pointCount = route.pointCount,
            )
        },
    )

private fun com.engineeringood.athena.authoring.AuthoringSourceEditEvidence.toEvidencePayload():
    AthenaAuthoringSourceEditEvidencePayload = AthenaAuthoringSourceEditEvidencePayload(
        uri = sourceUri,
        startOffset = startOffset,
        endOffset = endOffset,
        admittedText = admittedText,
        selectionStartOffset = selectionStartOffset,
        selectionEndOffset = selectionEndOffset,
        affectedSemanticIds = affectedSemanticIds,
        revisionGuard = revisionGuard.toPayload(),
    )

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
        is CreateSemanticEntityIntent -> "create-entity"
        is UpdateSemanticEntityPropertiesIntent -> "update-entity-properties"
        is RemoveSemanticEntityIntent -> "remove-entity"
        is SemanticRelationshipIntent -> "semantic-relationship"
        is RemoveSemanticRelationshipIntent -> "remove-semantic-relationship"
        is RevealSubjectIntent -> "reveal-subject"
    }
}

private fun AthenaAuthoringPreviewParams.toEntityProvenance(origin: AuthoringOrigin): AuthoringTransactionProvenance =
    AuthoringTransactionProvenance(
        actor = requireString(actor, "actor"),
        origin = origin,
        reason = provenance?.takeIf(String::isNotBlank),
    )

internal fun AthenaTrackedDocument.toAuthoringRevisionGuard(): AuthoringRevisionGuard {
    val contentGuard = AuthoringRevisionGuard.from(
        semanticSnapshotId = "semantic-snapshot:pending",
        sourceUri = uri,
        documentVersion = version,
        sourceText = text,
    )
    val semanticIdentity = projectSemanticGraphId?.takeIf(String::isNotBlank) ?: "document"
    return contentGuard.copy(
        semanticSnapshotId = "$semanticIdentity:${contentGuard.contentSha256}",
    )
}

private fun String?.toSemanticRelationshipType(): SemanticRelationshipType {
    val normalized = requireString(this, "relationshipType")
    return when (normalized) {
        ElectricalConnectionRelationship.value -> ElectricalConnectionRelationship
        else -> SemanticRelationshipType(normalized)
    }
}

private fun String?.toSemanticRelationshipTypeOrMalformed(): SemanticRelationshipType {
    val normalized = this?.trim().orEmpty()
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
