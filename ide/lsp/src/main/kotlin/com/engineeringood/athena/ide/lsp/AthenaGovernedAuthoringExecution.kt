package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.authoring.AuthoringAcceptanceEligibility
import com.engineeringood.athena.authoring.AuthoringDiagnostic
import com.engineeringood.athena.authoring.AuthoringDiagnosticAuthority
import com.engineeringood.athena.authoring.AuthoringDiagnosticCode
import com.engineeringood.athena.authoring.AuthoringIntent
import com.engineeringood.athena.authoring.AuthoringLifecycleState
import com.engineeringood.athena.authoring.AuthoringPreview
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.AuthoringRecoveryAction
import com.engineeringood.athena.authoring.AuthoringRelationshipCompatibility
import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.authoring.AuthoringValidationStage
import com.engineeringood.athena.authoring.CreateSemanticEntityIntent
import com.engineeringood.athena.authoring.GovernedRelationshipPreviewEvidence
import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.authoring.ElectricalSemanticRelationshipCompatibilityValidator
import com.engineeringood.athena.authoring.SemanticRelationshipValidationRequest
import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlan
import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.domain.electricalruntime.ElectricalAuthoringCapabilityRegistryFactory
import com.engineeringood.athena.interaction.AuthoringCapabilityDiscoveryResult
import com.engineeringood.athena.interaction.AuthoringIntentKind
import com.engineeringood.athena.interaction.InteractionDiagnosticCode
import com.engineeringood.athena.interaction.InteractionDiagnostic
import com.engineeringood.athena.interaction.InteractionDiagnosticSeverity
import com.engineeringood.athena.interaction.InteractionProvenance
import com.engineeringood.athena.interaction.InteractionSubjectKey
import com.engineeringood.athena.interaction.InteractionSubjectKind
import com.engineeringood.athena.runtime.ActiveAuthoringRevisionAuthority
import com.engineeringood.athena.runtime.AthenaGovernedAuthoringDecisionAuthorities
import com.engineeringood.athena.runtime.AthenaGovernedAuthoringPreviewContext
import com.engineeringood.athena.runtime.AuthoringMutationCommitted
import com.engineeringood.athena.runtime.AuthoringMutationBlocked
import com.engineeringood.athena.runtime.AuthoringReprojectionFailed
import com.engineeringood.athena.runtime.AuthoringReprojectionSucceeded
import com.engineeringood.athena.runtime.AuthoringStageValidationBlocked
import com.engineeringood.athena.runtime.AuthoringStageValidationPassed
import com.engineeringood.athena.runtime.AuthoringTransactionValidationAuthority
import com.engineeringood.athena.runtime.GovernedEntityCreationPreviewBlocked
import com.engineeringood.athena.runtime.GovernedEntityCreationPreviewReady
import com.engineeringood.athena.runtime.GovernedEntityCreationPreviewResult
import com.engineeringood.athena.runtime.GovernedRelationshipPreviewBlocked
import com.engineeringood.athena.runtime.GovernedRelationshipPreviewReady
import com.engineeringood.athena.runtime.GovernedRelationshipPreviewResult
import com.engineeringood.athena.runtime.SemanticAuthoringMutationAuthority
import com.engineeringood.athena.runtime.SemanticAuthoringReprojectionAuthority

internal fun discoverGovernedAuthoringCapability(
    trackedDocument: AthenaTrackedDocument,
    intent: AuthoringIntent,
): AuthoringCapabilityDiscoveryResult? {
    val compilation = trackedDocument.compilation as? CompilerCompilationSuccess ?: return null
    val (subjectId, subjectKind, intentKind) = when (intent) {
        is CreateSemanticEntityIntent -> Triple(
            intent.creationContext.parentSubjectId,
            InteractionSubjectKind.WORKSPACE,
            AuthoringIntentKind.CREATE_ENTITY,
        )
        is SemanticRelationshipIntent -> Triple(
            intent.sourceSubjectId,
            InteractionSubjectKind.PORT,
            AuthoringIntentKind.CREATE_RELATIONSHIP,
        )
        else -> return null
    }
    val registry = ElectricalAuthoringCapabilityRegistryFactory().build(
        document = compilation.document,
        sourceContextId = trackedDocument.uri,
        sourceRevision = trackedDocument.toAuthoringRevisionGuard().contentSha256,
        activeProjectionViewIds = compilation.projections.mapTo(mutableSetOf()) { projection -> projection.view.id },
    )
    val discovery = registry.discoverAuthoringCapabilities(
        subjectKey = InteractionSubjectKey(
            canonicalSubjectId = subjectId,
            subjectKind = subjectKind,
            sourceContextId = trackedDocument.uri,
        ),
        requestedBy = InteractionProvenance(originSurface = intent.origin.surface.toInteractionOriginSurface()),
        intentKind = intentKind,
    )
    if (intent !is SemanticRelationshipIntent || discovery.evidence.isEmpty()) {
        return discovery
    }
    val targetKey = InteractionSubjectKey(
        canonicalSubjectId = intent.targetSubjectId,
        subjectKind = InteractionSubjectKind.PORT,
        sourceContextId = trackedDocument.uri,
    )
    val targetRegistered = runCatching { registry.requireSubject(targetKey) }.isSuccess
    val compatibility = ElectricalSemanticRelationshipCompatibilityValidator().validate(
        SemanticRelationshipValidationRequest(intent, compilation.document),
    )
    if (!targetRegistered) {
        return AuthoringCapabilityDiscoveryResult(
            evidence = emptyList(),
            diagnostics = listOf(
                InteractionDiagnostic(
                    code = InteractionDiagnosticCode.SUBJECT_UNRESOLVED,
                    severity = InteractionDiagnosticSeverity.ERROR,
                    message = compatibility.diagnostics
                        .joinToString(separator = "; ") { diagnostic -> diagnostic.message }
                        .ifBlank { "The requested relationship target is not a registered compatible semantic subject." },
                    subject = targetKey,
                    retryable = false,
                ),
            ),
        )
    }
    return discovery.copy(
        evidence = discovery.evidence.map { evidence ->
            evidence.copy(relatedSubjects = setOf(targetKey))
        },
    )
}

internal fun capabilityBlockedPreview(
    previewId: AuthoringPreviewId,
    intent: AuthoringIntent,
    revisionGuard: AuthoringRevisionGuard,
    discovery: AuthoringCapabilityDiscoveryResult?,
): AuthoringPreview {
    val unresolvedRelationship = intent is SemanticRelationshipIntent &&
        discovery?.diagnostics?.any { diagnostic -> diagnostic.code == InteractionDiagnosticCode.SUBJECT_UNRESOLVED } == true
    val diagnostic = AuthoringDiagnostic(
        code = if (unresolvedRelationship) {
            AuthoringDiagnosticCode.RELATIONSHIP_SUBJECT_UNRESOLVED
        } else {
            AuthoringDiagnosticCode.STOP_DOWNSTREAM
        },
        message = discovery?.diagnostics
            ?.joinToString(separator = "; ") { candidate -> candidate.message }
            ?.ifBlank { null }
            ?: "No registry-discovered authoring capability admits this request.",
        authority = AuthoringDiagnosticAuthority.CAPABILITY_REGISTRY,
        lifecycleStage = AuthoringLifecycleState.BLOCKED,
        recoveryAction = AuthoringRecoveryAction.FIX_SOURCE,
    )
    return AuthoringPreview(
        previewId = previewId,
        intentId = intent.intentId,
        title = when (intent) {
            is SemanticRelationshipIntent -> "Create semantic relationship"
            is CreateSemanticEntityIntent -> "Create ${intent.suggestedName ?: intent.conceptTemplateId.value}"
            else -> "Governed authoring preview"
        },
        changes = emptyList(),
        revisionGuard = revisionGuard,
        acceptanceEligibility = AuthoringAcceptanceEligibility(false, listOf(diagnostic)),
        relationshipEvidence = (intent as? SemanticRelationshipIntent)?.let { relationship ->
            GovernedRelationshipPreviewEvidence(
                sourceSubjectId = relationship.sourceSubjectId.value,
                targetSubjectId = relationship.targetSubjectId.value,
                relationshipType = relationship.relationshipType,
                compatibility = AuthoringRelationshipCompatibility.NOT_EVALUATED,
                affectedSemanticIds = emptyList(),
            )
        },
    )
}

internal fun GovernedEntityCreationPreviewResult.toSessionContext(): AthenaGovernedAuthoringPreviewContext =
    when (this) {
        is GovernedEntityCreationPreviewReady -> AthenaGovernedAuthoringPreviewContext(
            transaction = transaction,
            validationAuthority = validationAuthority,
            sourceEditPlan = sourceEditPlan,
        )
        is GovernedEntityCreationPreviewBlocked -> AthenaGovernedAuthoringPreviewContext(
            transaction = transaction,
            validationAuthority = validationAuthority,
        )
    }

internal fun GovernedRelationshipPreviewResult.toSessionContext(): AthenaGovernedAuthoringPreviewContext =
    when (this) {
        is GovernedRelationshipPreviewReady -> AthenaGovernedAuthoringPreviewContext(
            transaction = transaction,
            validationAuthority = validationAuthority,
            sourceEditPlan = sourceEditPlan,
        )
        is GovernedRelationshipPreviewBlocked -> AthenaGovernedAuthoringPreviewContext(
            transaction = transaction,
            validationAuthority = validationAuthority,
        )
    }

internal fun governedDecisionAuthorities(
    trackedDocument: AthenaTrackedDocument,
    compiler: AthenaCompiler,
    governedContext: AthenaGovernedAuthoringPreviewContext,
    sourceMutationAuthority: (AthenaAuthoringSourceEditPayload, String) -> AthenaAuthoringWorkspaceMutationResult,
    onSourceMutated: (String) -> Unit,
): AthenaGovernedAuthoringDecisionAuthorities {
    val sourcePlan = governedContext.sourceEditPlan
    var proposedSource: String? = null
    var successfulCompilation: CompilerCompilationSuccess? = null
    return AthenaGovernedAuthoringDecisionAuthorities(
        revisionAuthority = ActiveAuthoringRevisionAuthority { trackedDocument.toAuthoringRevisionGuard() },
        acceptanceValidationAuthority = AuthoringTransactionValidationAuthority { stage, _ ->
            if (stage != AuthoringValidationStage.SEMANTIC_VALIDATION || sourcePlan == null) {
                AuthoringStageValidationPassed
            } else {
                val admittedSource = runCatching { sourcePlan.applyTo(trackedDocument.text) }.getOrElse { failure ->
                    return@AuthoringTransactionValidationAuthority AuthoringStageValidationBlocked(
                        compilationDiagnostic(failure.message ?: "The revision-bound source plan is no longer applicable."),
                    )
                }
                proposedSource = admittedSource
                when (val compilation = compiler.compile(trackedDocument.path, admittedSource)) {
                    is CompilerCompilationParseFailure -> AuthoringStageValidationBlocked(
                        compilationDiagnostic(
                            message = compilation.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
                        ),
                    )
                    is CompilerCompilationSuccess -> {
                        if (compilation.semanticResult.diagnostics.isEmpty()) {
                            successfulCompilation = compilation
                            AuthoringStageValidationPassed
                        } else {
                            AuthoringStageValidationBlocked(
                                compilationDiagnostic(
                                    message = compilation.semanticResult.diagnostics
                                        .joinToString(separator = "; ") { diagnostic -> diagnostic.message },
                                ),
                            )
                        }
                    }
                }
            }
        },
        mutationAuthority = SemanticAuthoringMutationAuthority { transaction ->
            val admittedSource = proposedSource
            check(sourcePlan != null && admittedSource != null && successfulCompilation != null) {
                "Mutation Authority requires one parser- and compiler-admitted backend source plan."
            }
            val sourceEdit = sourcePlan.toPayload(trackedDocument.text).copy(appliedByAuthority = true)
            when (val mutation = sourceMutationAuthority(sourceEdit, admittedSource)) {
                AthenaAuthoringWorkspaceMutationApplied -> {
                    onSourceMutated(admittedSource)
                    AuthoringMutationCommitted(
                        mutationId = "authoring-mutation:${transaction.transactionId.value}",
                        committedRevision = AuthoringRevisionGuard.from(
                            semanticSnapshotId = "semantic-snapshot:${transaction.transactionId.value}",
                            sourceUri = sourcePlan.sourceUri,
                            documentVersion = trackedDocument.version + 1,
                            sourceText = admittedSource,
                        ),
                        affectedSemanticIds = sourcePlan.affectedSemanticIds,
                    )
                }
                is AthenaAuthoringWorkspaceMutationRejected -> AuthoringMutationBlocked(
                    AuthoringDiagnostic(
                        code = AuthoringDiagnosticCode.SOURCE_CONFLICT,
                        message = mutation.reason,
                        authority = AuthoringDiagnosticAuthority.MUTATION_AUTHORITY,
                        lifecycleStage = AuthoringLifecycleState.BLOCKED,
                        recoveryAction = AuthoringRecoveryAction.REFRESH_PREVIEW,
                    ),
                )
            }
        },
        reprojectionAuthority = SemanticAuthoringReprojectionAuthority { _, commit ->
            val compilation = checkNotNull(successfulCompilation) {
                "Reprojection requires the compiler-admitted authoring result."
            }
            val affectedIds = commit.affectedSemanticIds.toSet()
            val occurrenceIds = compilation.presentations.flatMap { presentation ->
                presentation.occurrences
                    .filter { occurrence -> occurrence.semanticId.value in affectedIds }
                    .map { occurrence -> occurrence.occurrenceId.value } +
                    presentation.connectors
                        .filter { connector -> connector.semanticId.value in affectedIds }
                        .map { connector -> connector.occurrenceId.value }
            }.distinct().sorted()
            if (occurrenceIds.isEmpty()) {
                AuthoringReprojectionFailed(
                    listOf(
                        AuthoringDiagnostic(
                            code = AuthoringDiagnosticCode.PROJECTION_FAILED_AFTER_COMMIT,
                            message = "Accepted semantic mutation produced no projection occurrence for the affected identities.",
                            authority = AuthoringDiagnosticAuthority.PROJECTION,
                            lifecycleStage = AuthoringLifecycleState.PROJECTION_FAILED,
                            relatedIds = commit.affectedSemanticIds,
                            recoveryAction = AuthoringRecoveryAction.RETRY_PROJECTION,
                        ),
                    ),
                )
            } else {
                AuthoringReprojectionSucceeded(occurrenceIds)
            }
        },
    )
}

private fun compilationDiagnostic(message: String): AuthoringDiagnostic = AuthoringDiagnostic(
    code = AuthoringDiagnosticCode.SOURCE_INVALID,
    message = message.ifBlank { "The admitted authoring source did not compile successfully." },
    authority = AuthoringDiagnosticAuthority.COMPILER,
    lifecycleStage = AuthoringLifecycleState.BLOCKED,
    recoveryAction = AuthoringRecoveryAction.FIX_SOURCE,
)
