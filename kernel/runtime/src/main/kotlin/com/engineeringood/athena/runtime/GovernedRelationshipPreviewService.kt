package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AuthoringAcceptanceEligibility
import com.engineeringood.athena.authoring.AuthoringDiagnostic
import com.engineeringood.athena.authoring.AuthoringDiagnosticAuthority
import com.engineeringood.athena.authoring.AuthoringDiagnosticCode
import com.engineeringood.athena.authoring.AuthoringLifecycleState
import com.engineeringood.athena.authoring.AuthoringPreview
import com.engineeringood.athena.authoring.AuthoringPreviewChange
import com.engineeringood.athena.authoring.AuthoringPreviewChangeKind
import com.engineeringood.athena.authoring.AuthoringRecoveryAction
import com.engineeringood.athena.authoring.AuthoringRelationshipCompatibility
import com.engineeringood.athena.authoring.AuthoringSourceEditEvidence
import com.engineeringood.athena.authoring.AuthoringValidationStage
import com.engineeringood.athena.authoring.ElectricalSemanticRelationshipCompatibilityValidator
import com.engineeringood.athena.authoring.GovernedRelationshipPreviewEvidence
import com.engineeringood.athena.authoring.SemanticAuthoringTransaction
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionCreated
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionFactory
import com.engineeringood.athena.authoring.SemanticRelationshipValidationRequest
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlan
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlanned
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlanner
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditRejected
import com.engineeringood.athena.compiler.BackendRelationshipPlanningRequest
import com.engineeringood.athena.interaction.AuthoringIntentKind
import com.engineeringood.athena.interaction.AuthoringCapabilityRequirementKind
import com.engineeringood.athena.ir.StableSemanticIdentity

class GovernedRelationshipPreviewService(
    private val compatibilityValidator: ElectricalSemanticRelationshipCompatibilityValidator =
        ElectricalSemanticRelationshipCompatibilityValidator(),
    private val sourcePlanner: BackendAuthoringSourceEditPlanner = BackendAuthoringSourceEditPlanner(),
    private val routePreviewAuthority: GovernedRelationshipRoutePreviewAuthority =
        GovernedRelationshipRoutePreviewAuthority { _, _ -> null },
) {
    fun preview(request: GovernedRelationshipPreviewRequest): GovernedRelationshipPreviewResult {
        val intent = request.intent
        val targetEvidence = request.capabilityEvidence.relatedSubjects.singleOrNull()
        if (request.capabilityEvidence.capabilityId != "create-semantic-relationship" ||
            request.capabilityEvidence.intentKind != AuthoringIntentKind.CREATE_RELATIONSHIP ||
            request.capabilityEvidence.subject.canonicalSubjectId != intent.sourceSubjectId ||
            request.capabilityEvidence.subject.sourceContextId != request.sourceDocument.sourceUri ||
            request.capabilityEvidence.actorOrigin.name != intent.origin.surface.name ||
            request.capabilityEvidence.satisfiedRequirements.any { requirement -> !requirement.satisfied } ||
            request.capabilityEvidence.satisfiedRequirements.map { requirement -> requirement.kind }.toSet() !=
            setOf(AuthoringCapabilityRequirementKind.DOMAIN, AuthoringCapabilityRequirementKind.PROJECTION) ||
            targetEvidence == null ||
            targetEvidence.canonicalSubjectId != intent.targetSubjectId ||
            targetEvidence.subjectKind != com.engineeringood.athena.interaction.InteractionSubjectKind.PORT ||
            targetEvidence.sourceContextId != request.sourceDocument.sourceUri
        ) {
            return blocked(
                request,
                diagnostic(
                    AuthoringDiagnosticCode.STOP_DOWNSTREAM,
                    "Create-relationship capability evidence does not match the requested source subject.",
                    AuthoringDiagnosticAuthority.CAPABILITY_REGISTRY,
                    intent,
                ),
                AuthoringRelationshipCompatibility.NOT_EVALUATED,
            )
        }

        val persistenceSourceUri = intent.persistenceTarget.sourceUri
        if (persistenceSourceUri != null && persistenceSourceUri != request.sourceDocument.sourceUri) {
            return blocked(
                request,
                diagnostic(
                    AuthoringDiagnosticCode.SOURCE_CONFLICT,
                    "Relationship persistence target `$persistenceSourceUri` does not match the active source `${request.sourceDocument.sourceUri}`.",
                    AuthoringDiagnosticAuthority.SOURCE_PLANNING,
                    intent,
                ),
                AuthoringRelationshipCompatibility.NOT_EVALUATED,
            )
        }

        val compatibility = compatibilityValidator.validate(
            SemanticRelationshipValidationRequest(intent, request.semanticDocument),
        )
        if (!compatibility.previewEligible || !compatibility.persistenceEligible) {
            val detail = compatibility.diagnostics.joinToString(separator = "; ") { candidate ->
                "${candidate.code}: ${candidate.message}"
            }
            return blocked(
                request,
                diagnostic(
                    compatibility.diagnostics.toAuthoringCode(),
                    detail.ifBlank { "Semantic relationship endpoints are incompatible." },
                    AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
                    intent,
                ),
                compatibility.diagnostics.toCompatibilityEvidence(),
            )
        }

        val sourceAuthoredPath = request.semanticDocument.authoredPath(intent.sourceSubjectId)
        val targetAuthoredPath = request.semanticDocument.authoredPath(intent.targetSubjectId)
        if (sourceAuthoredPath == null || targetAuthoredPath == null) {
            return blocked(
                request,
                diagnostic(
                    AuthoringDiagnosticCode.RELATIONSHIP_SUBJECT_UNRESOLVED,
                    "Validated relationship subjects do not resolve to deterministic authored endpoint paths.",
                    AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
                    intent,
                ),
                AuthoringRelationshipCompatibility.INCOMPATIBLE,
            )
        }

        val sourcePlan = when (
            val planning = sourcePlanner.plan(
                BackendRelationshipPlanningRequest(
                    document = request.sourceDocument,
                    revisionGuard = request.sourceDocument.revisionGuard,
                    intent = intent,
                    sourceAuthoredPath = sourceAuthoredPath,
                    targetAuthoredPath = targetAuthoredPath,
                ),
            )
        ) {
            is BackendAuthoringSourceEditPlanned -> planning.plan
            is BackendAuthoringSourceEditRejected -> return blocked(
                request,
                planning.diagnostics,
                AuthoringRelationshipCompatibility.COMPATIBLE,
            )
        }
        val evidence = GovernedRelationshipPreviewEvidence(
            sourceSubjectId = intent.sourceSubjectId.value,
            targetSubjectId = intent.targetSubjectId.value,
            relationshipType = intent.relationshipType,
            compatibility = AuthoringRelationshipCompatibility.COMPATIBLE,
            affectedSemanticIds = sourcePlan.affectedSemanticIds,
            sourceEdit = sourcePlan.toEvidence(),
            routePreview = routePreviewAuthority.preview(intent, request.semanticDocument),
        )
        val preview = AuthoringPreview(
            previewId = request.previewId,
            intentId = intent.intentId,
            title = "Create semantic relationship",
            changes = listOf(
                AuthoringPreviewChange(
                    kind = AuthoringPreviewChangeKind.CONNECT,
                    title = "${intent.sourceSubjectId.value} -> ${intent.targetSubjectId.value}",
                    summary = "Create one governed `${intent.relationshipType.value}` relationship.",
                    affectedSubjectIdentities = setOf(intent.sourceSubjectId, intent.targetSubjectId),
                ),
            ),
            revisionGuard = request.sourceDocument.revisionGuard,
            relationshipEvidence = evidence,
        )
        return GovernedRelationshipPreviewReady(
            transaction = request.transaction(preview),
            sourceEditPlan = sourcePlan,
            validationAuthority = GovernedRelationshipValidationAuthority(emptyList()),
        )
    }

    private fun blocked(
        request: GovernedRelationshipPreviewRequest,
        diagnostic: AuthoringDiagnostic,
        compatibility: AuthoringRelationshipCompatibility,
    ): GovernedRelationshipPreviewBlocked = blocked(request, listOf(diagnostic), compatibility)

    private fun blocked(
        request: GovernedRelationshipPreviewRequest,
        diagnostics: List<AuthoringDiagnostic>,
        compatibility: AuthoringRelationshipCompatibility,
    ): GovernedRelationshipPreviewBlocked {
        val intent = request.intent
        val preview = AuthoringPreview(
            previewId = request.previewId,
            intentId = intent.intentId,
            title = "Create semantic relationship",
            changes = emptyList(),
            revisionGuard = request.sourceDocument.revisionGuard,
            acceptanceEligibility = AuthoringAcceptanceEligibility(false, diagnostics),
            relationshipEvidence = GovernedRelationshipPreviewEvidence(
                sourceSubjectId = intent.sourceSubjectId.value,
                targetSubjectId = intent.targetSubjectId.value,
                relationshipType = intent.relationshipType,
                compatibility = compatibility,
                affectedSemanticIds = emptyList(),
            ),
        )
        return GovernedRelationshipPreviewBlocked(
            transaction = request.transaction(preview),
            diagnostics = diagnostics,
            validationAuthority = GovernedRelationshipValidationAuthority(diagnostics),
        )
    }
}

private fun List<com.engineeringood.athena.authoring.SemanticRelationshipValidationDiagnostic>.toAuthoringCode(): AuthoringDiagnosticCode =
    when {
        any { diagnostic -> diagnostic.code == "semantic.relationship.subject.missing" } ->
            AuthoringDiagnosticCode.RELATIONSHIP_SUBJECT_UNRESOLVED
        any { diagnostic -> diagnostic.code == "semantic.relationship.self" } ->
            AuthoringDiagnosticCode.RELATIONSHIP_SELF
        any { diagnostic -> diagnostic.code == "semantic.relationship.duplicate" } ->
            AuthoringDiagnosticCode.RELATIONSHIP_DUPLICATE
        any { diagnostic -> diagnostic.code == "semantic.relationship.subject.malformed" } ->
            AuthoringDiagnosticCode.SOURCE_INVALID
        any { diagnostic -> diagnostic.code == "semantic.relationship.type.unsupported" } ->
            AuthoringDiagnosticCode.RELATIONSHIP_TYPE_UNSUPPORTED
        else -> AuthoringDiagnosticCode.RELATIONSHIP_INCOMPATIBLE
    }

private fun List<com.engineeringood.athena.authoring.SemanticRelationshipValidationDiagnostic>.toCompatibilityEvidence():
    AuthoringRelationshipCompatibility = when {
    any { diagnostic -> diagnostic.code == "semantic.relationship.duplicate" } ->
        AuthoringRelationshipCompatibility.COMPATIBLE
    any { diagnostic ->
        diagnostic.code == "semantic.relationship.electrical.direction" ||
            diagnostic.code == "semantic.relationship.electrical.signal"
    } -> AuthoringRelationshipCompatibility.INCOMPATIBLE
    else -> AuthoringRelationshipCompatibility.NOT_EVALUATED
}

private fun com.engineeringood.athena.ir.EngineeringDocument.authoredPath(subjectId: StableSemanticIdentity): String? =
    ports.singleOrNull { port -> port.id == subjectId }
        ?.let { port -> (port.ownerReference.authoredPath + port.name).joinToString(".") }

private class GovernedRelationshipValidationAuthority(
    private val diagnostics: List<AuthoringDiagnostic>,
) : AuthoringTransactionValidationAuthority {
    override fun validate(
        stage: AuthoringValidationStage,
        transaction: SemanticAuthoringTransaction,
    ): AuthoringStageValidationResult {
        val diagnostic = diagnostics.firstOrNull { candidate -> candidate.validationStage() == stage }
        return if (diagnostic == null) AuthoringStageValidationPassed else AuthoringStageValidationBlocked(diagnostic)
    }
}

private fun GovernedRelationshipPreviewRequest.transaction(preview: AuthoringPreview): SemanticAuthoringTransaction =
    (SemanticAuthoringTransactionFactory.create(
        transactionId = transactionId,
        intents = listOf(intent),
        capabilityEvidence = capabilityEvidence,
        revisionGuard = sourceDocument.revisionGuard,
        preview = preview,
        provenance = provenance,
    ) as SemanticAuthoringTransactionCreated).transaction

private fun BackendAuthoringSourceEditPlan.toEvidence(): AuthoringSourceEditEvidence = AuthoringSourceEditEvidence(
    revisionGuard = revisionGuard,
    sourceUri = sourceUri,
    startOffset = replacement.startOffset,
    endOffset = replacement.endOffset,
    admittedText = admittedText,
    selectionStartOffset = selection?.startOffset,
    selectionEndOffset = selection?.endOffset,
    affectedSemanticIds = affectedSemanticIds,
)

private fun AuthoringDiagnostic.validationStage(): AuthoringValidationStage = when (authority) {
    AuthoringDiagnosticAuthority.CAPABILITY_REGISTRY -> AuthoringValidationStage.CAPABILITY_EVIDENCE
    AuthoringDiagnosticAuthority.SOURCE_PLANNING -> AuthoringValidationStage.SOURCE_PLANNING
    AuthoringDiagnosticAuthority.PARSER -> AuthoringValidationStage.PARSER_VALIDATION
    else -> AuthoringValidationStage.SEMANTIC_RULES
}

private fun diagnostic(
    code: AuthoringDiagnosticCode,
    message: String,
    authority: AuthoringDiagnosticAuthority,
    intent: com.engineeringood.athena.authoring.SemanticRelationshipIntent,
): AuthoringDiagnostic = AuthoringDiagnostic(
    code = code,
    message = message,
    authority = authority,
    lifecycleStage = AuthoringLifecycleState.BLOCKED,
    relatedIds = listOf(intent.sourceSubjectId.value, intent.targetSubjectId.value),
    recoveryAction = AuthoringRecoveryAction.FIX_SOURCE,
)
