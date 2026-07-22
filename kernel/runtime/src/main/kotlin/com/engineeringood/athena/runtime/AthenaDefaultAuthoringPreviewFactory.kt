package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AcceptAuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringAcceptanceEligibility
import com.engineeringood.athena.authoring.AuthoringDiagnostic
import com.engineeringood.athena.authoring.AuthoringDiagnosticAuthority
import com.engineeringood.athena.authoring.AuthoringDiagnosticCode
import com.engineeringood.athena.authoring.AuthoringIntent
import com.engineeringood.athena.authoring.AuthoringLifecycleState
import com.engineeringood.athena.authoring.AuthoringPreview
import com.engineeringood.athena.authoring.AuthoringPreviewChange
import com.engineeringood.athena.authoring.AuthoringPreviewChangeKind
import com.engineeringood.athena.authoring.AuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.AuthoringPreviewStatus
import com.engineeringood.athena.authoring.CancelAuthoringPreviewDecision
import com.engineeringood.athena.authoring.CreateSemanticEntityIntent
import com.engineeringood.athena.authoring.RejectAuthoringPreviewDecision
import com.engineeringood.athena.authoring.RemoveSemanticEntityIntent
import com.engineeringood.athena.authoring.RemoveSemanticRelationshipIntent
import com.engineeringood.athena.authoring.RevealSubjectIntent
import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.authoring.UpdateSemanticEntityPropertiesIntent
import com.engineeringood.athena.ir.StableSemanticIdentity

internal fun AuthoringIntent.toPreview(previewId: AuthoringPreviewId): AuthoringPreview = when (this) {
    is CreateSemanticEntityIntent -> AuthoringPreview(
        previewId = previewId,
        intentId = intentId,
        title = "Create component preview",
        changes = listOf(
            AuthoringPreviewChange(
                kind = AuthoringPreviewChangeKind.CREATE,
                title = suggestedName ?: conceptTemplateId.value,
                summary = buildString {
                    append("Create semantic entity from concept template `")
                    append(conceptTemplateId.value)
                    append("` under `")
                    append(creationContext.parentSubjectId.value)
                    append("`.")
                    preferredImplementationId?.let { implementationId ->
                        append(" Preferred implementation: `")
                        append(implementationId.value)
                        append("`.")
                    }
                },
                affectedSubjectIdentities = buildSet {
                    add(creationContext.parentSubjectId)
                    suggestedName?.takeIf(String::isNotBlank)?.let { name ->
                        add(StableSemanticIdentity("component:$name"))
                    }
                },
            ),
        ),
    )
    is UpdateSemanticEntityPropertiesIntent -> AuthoringPreview(
        previewId = previewId,
        intentId = intentId,
        title = "Update component properties preview",
        changes = listOf(
            AuthoringPreviewChange(
                kind = AuthoringPreviewChangeKind.UPDATE,
                title = subjectId.value,
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
                affectedSubjectIdentities = setOf(subjectId),
            ),
        ),
    )
    is RemoveSemanticEntityIntent -> {
        val diagnostic = AuthoringDiagnostic(
            code = AuthoringDiagnosticCode.REMOVAL_DEPENDENCIES,
            message = "Entity removal requires dependency-impact validation before acceptance.",
            authority = AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
            lifecycleStage = AuthoringLifecycleState.BLOCKED,
            subjectId = subjectId.value,
        )
        AuthoringPreview(
            previewId = previewId,
            intentId = intentId,
            title = "Remove semantic entity preview",
            changes = listOf(
                AuthoringPreviewChange(
                    kind = AuthoringPreviewChangeKind.UPDATE,
                    title = subjectId.value,
                    summary = "Inspect dependency impact before semantic entity removal.",
                    affectedSubjectIdentities = setOf(subjectId),
                ),
            ),
            revisionGuard = revisionGuard,
            acceptanceEligibility = AuthoringAcceptanceEligibility(false, listOf(diagnostic)),
        )
    }
    is RemoveSemanticRelationshipIntent -> {
        val diagnostic = AuthoringDiagnostic(
            code = AuthoringDiagnosticCode.STOP_DOWNSTREAM,
            message = "Relationship removal is available for preview and validation only in M31.",
            authority = AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
            lifecycleStage = AuthoringLifecycleState.BLOCKED,
            relatedIds = listOf(sourceSubjectId.value, targetSubjectId.value),
        )
        AuthoringPreview(
            previewId = previewId,
            intentId = intentId,
            title = "Remove semantic relationship preview",
            changes = listOf(
                AuthoringPreviewChange(
                    kind = AuthoringPreviewChangeKind.REMOVE,
                    title = "${sourceSubjectId.value} -> ${targetSubjectId.value}",
                    summary = "Inspect relationship impact without removing either endpoint entity.",
                    affectedSubjectIdentities = setOf(sourceSubjectId, targetSubjectId),
                ),
            ),
            acceptanceEligibility = AuthoringAcceptanceEligibility(false, listOf(diagnostic)),
        )
    }
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

internal fun AuthoringPreviewDecision.toPreviewStatus(): AuthoringPreviewStatus = when (this) {
    is AcceptAuthoringPreviewDecision -> AuthoringPreviewStatus.ACCEPTED
    is RejectAuthoringPreviewDecision -> AuthoringPreviewStatus.REJECTED
    is CancelAuthoringPreviewDecision -> AuthoringPreviewStatus.CANCELLED
}

internal fun AuthoringLifecycleState.toPreviewStatus(): AuthoringPreviewStatus = when (this) {
    AuthoringLifecycleState.REJECTED -> AuthoringPreviewStatus.REJECTED
    AuthoringLifecycleState.CANCELLED -> AuthoringPreviewStatus.CANCELLED
    AuthoringLifecycleState.BLOCKED -> AuthoringPreviewStatus.BLOCKED
    AuthoringLifecycleState.STALE -> AuthoringPreviewStatus.STALE
    AuthoringLifecycleState.COMMITTED,
    AuthoringLifecycleState.RECOMPILED,
    AuthoringLifecycleState.REPROJECTED,
    AuthoringLifecycleState.PROJECTION_FAILED,
    AuthoringLifecycleState.ACCEPTED,
    -> AuthoringPreviewStatus.ACCEPTED
    AuthoringLifecycleState.REQUESTED,
    AuthoringLifecycleState.DISCOVERED,
    AuthoringLifecycleState.VALIDATED,
    AuthoringLifecycleState.PREVIEWING,
    AuthoringLifecycleState.MUTATION_PENDING,
    -> AuthoringPreviewStatus.PENDING_REVIEW
}
