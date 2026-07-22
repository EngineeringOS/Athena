package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AuthoringAcceptanceEligibility
import com.engineeringood.athena.authoring.AuthoringDiagnostic
import com.engineeringood.athena.authoring.AuthoringDiagnosticAuthority
import com.engineeringood.athena.authoring.AuthoringDiagnosticCode
import com.engineeringood.athena.authoring.AuthoringLifecycleState
import com.engineeringood.athena.authoring.AuthoringNestedPortCreationEvidence
import com.engineeringood.athena.authoring.AuthoringPreview
import com.engineeringood.athena.authoring.AuthoringPreviewChange
import com.engineeringood.athena.authoring.AuthoringPreviewChangeKind
import com.engineeringood.athena.authoring.AuthoringRecoveryAction
import com.engineeringood.athena.authoring.AuthoringSourceEditEvidence
import com.engineeringood.athena.authoring.AuthoringValidationStage
import com.engineeringood.athena.authoring.AuthoringValue
import com.engineeringood.athena.authoring.CreateSemanticEntityIntent
import com.engineeringood.athena.authoring.GovernedEntityCreationPreviewEvidence
import com.engineeringood.athena.authoring.SemanticAuthoringTransaction
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionCreated
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionFactory
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlan
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlanned
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlanner
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditRejected
import com.engineeringood.athena.compiler.BackendEntityCreationPlanningRequest
import com.engineeringood.athena.component.EngineeringConceptTemplate
import com.engineeringood.athena.interaction.AuthoringIntentKind
import com.engineeringood.athena.interaction.AuthoringCapabilityRequirementKind
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.DeviceDeclaration

/** Builds one governed creation preview while leaving acceptance to SemanticAuthoringTransactionRuntime. */
class GovernedEntityCreationPreviewService(
    templates: List<EngineeringConceptTemplate>,
    private val projectionAuthority: GovernedEntityCreationProjectionAuthority,
    private val sourcePlanner: BackendAuthoringSourceEditPlanner = BackendAuthoringSourceEditPlanner(),
) {
    private val templatesById = templates.groupBy { template -> template.templateId }

    fun preview(request: GovernedEntityCreationPreviewRequest): GovernedEntityCreationPreviewResult {
        val intent = request.intent
        val canonicalTag = intent.suggestedName?.trim().orEmpty()
        if (!ENTITY_TAG_PATTERN.matches(canonicalTag)) {
            return blocked(
                request,
                diagnostic(
                    AuthoringDiagnosticCode.SOURCE_INVALID,
                    "Requested semantic entity tag `$canonicalTag` is not a valid Athena identifier.",
                    AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
                ),
            )
        }
        if (request.capabilityEvidence.capabilityId != "create-semantic-entity" ||
            request.capabilityEvidence.intentKind != AuthoringIntentKind.CREATE_ENTITY ||
            request.capabilityEvidence.subject.canonicalSubjectId != intent.creationContext.parentSubjectId ||
            request.capabilityEvidence.subject.sourceContextId != request.document.sourceUri ||
            request.capabilityEvidence.actorOrigin.name != intent.origin.surface.name ||
            request.capabilityEvidence.satisfiedRequirements.any { requirement -> !requirement.satisfied } ||
            request.capabilityEvidence.satisfiedRequirements.map { requirement -> requirement.kind }.toSet() !=
            setOf(
                AuthoringCapabilityRequirementKind.DOMAIN,
                AuthoringCapabilityRequirementKind.CONCEPT_TEMPLATE,
                AuthoringCapabilityRequirementKind.PROJECTION,
                AuthoringCapabilityRequirementKind.REPRESENTATION,
            )
        ) {
            return blocked(
                request,
                diagnostic(
                    AuthoringDiagnosticCode.STOP_DOWNSTREAM,
                    "Create-entity capability evidence does not match the requested parent subject.",
                    AuthoringDiagnosticAuthority.CAPABILITY_REGISTRY,
                ),
            )
        }
        val matchingTemplates = templatesById[intent.conceptTemplateId].orEmpty()
        if (matchingTemplates.isEmpty()) {
            return blocked(
                request,
                diagnostic(
                    AuthoringDiagnosticCode.CONCEPT_TEMPLATE_MISSING,
                    "Engineering Concept Template `${intent.conceptTemplateId.value}` is unavailable.",
                    AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
                ),
            )
        }
        if (matchingTemplates.size != 1) {
            return blocked(
                request,
                diagnostic(
                    AuthoringDiagnosticCode.CONCEPT_TEMPLATE_IDENTITY_MISMATCH,
                    "Engineering Concept Template id `${intent.conceptTemplateId.value}` is ambiguous.",
                    AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
                ),
            )
        }
        val template = matchingTemplates.single()
        if (template.conceptId != intent.conceptId) {
            return blocked(
                request,
                diagnostic(
                    AuthoringDiagnosticCode.CONCEPT_TEMPLATE_IDENTITY_MISMATCH,
                    "Concept `${intent.conceptId.value}` does not match template `${template.templateId.value}`.",
                    AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
                ),
            )
        }
        if (!template.hasValidNestedPortAnatomy()) {
            return blocked(
                request,
                diagnostic(
                    AuthoringDiagnosticCode.NESTED_PORT_ANATOMY_INVALID,
                    "Engineering Concept Template `${template.templateId.value}` has no valid nested-port anatomy.",
                    AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
                ),
            )
        }
        if (request.document.ast.declarations
                .filterIsInstance<DeviceDeclaration>()
                .any { declaration -> declaration.name == canonicalTag }
        ) {
            return blocked(
                request,
                diagnostic(
                    AuthoringDiagnosticCode.ENTITY_TAG_DUPLICATE,
                    "Semantic entity tag `$canonicalTag` already exists in the active Athena source.",
                    AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
                ),
            )
        }

        val sourcePlan = when (
            val planning = sourcePlanner.plan(
                BackendEntityCreationPlanningRequest(
                    document = request.document,
                    revisionGuard = intent.revisionGuard,
                    intent = intent,
                    template = template,
                ),
            )
        ) {
            is BackendAuthoringSourceEditPlanned -> planning.plan
            is BackendAuthoringSourceEditRejected -> return blocked(request, planning.diagnostics)
        }
        val projection = when (val resolution = projectionAuthority.resolve(intent, template, canonicalTag)) {
            is GovernedEntityCreationProjectionResolved -> resolution
            is GovernedEntityCreationRepresentationUnresolved -> return blocked(
                request,
                diagnostic(
                    AuthoringDiagnosticCode.REPRESENTATION_UNRESOLVED,
                    resolution.reason,
                    AuthoringDiagnosticAuthority.REPRESENTATION,
                ),
            )
            is GovernedEntityCreationCompositionUnsatisfied -> return blocked(
                request,
                diagnostic(
                    AuthoringDiagnosticCode.COMPOSITION_UNSATISFIED,
                    resolution.reason,
                    AuthoringDiagnosticAuthority.COMPOSITION,
                ),
            )
        }
        val evidence = GovernedEntityCreationPreviewEvidence(
            canonicalTag = canonicalTag,
            semanticType = template.semanticType.value,
            model = intent.modelValue() ?: template.modelValue(),
            nestedPorts = template.nestedPorts.map { port ->
                AuthoringNestedPortCreationEvidence(
                    name = port.name,
                    direction = port.direction,
                    signalOrMedium = port.signalOrMedium,
                    semanticId = "port:$canonicalTag.${port.name}",
                )
            },
            affectedSemanticIds = sourcePlan.affectedSemanticIds,
            sourceEdit = sourcePlan.toEvidence(),
            representationId = projection.representationId,
            compositionTargetId = projection.compositionTargetId,
            projectionOccurrenceIds = projection.projectionOccurrenceIds.sorted(),
        )
        val preview = AuthoringPreview(
            previewId = request.previewId,
            intentId = intent.intentId,
            title = "Create $canonicalTag",
            changes = listOf(
                AuthoringPreviewChange(
                    kind = AuthoringPreviewChangeKind.CREATE,
                    title = canonicalTag,
                    summary = "Create `${template.conceptId.value}` with ${template.nestedPorts.size} nested ports.",
                    affectedSubjectIdentities = evidence.affectedSemanticIds
                        .mapTo(linkedSetOf(), ::StableSemanticIdentity),
                ),
            ),
            revisionGuard = request.document.revisionGuard,
            entityCreationEvidence = evidence,
        )
        val transaction = request.transaction(preview)
        return GovernedEntityCreationPreviewReady(
            transaction = transaction,
            sourceEditPlan = sourcePlan,
            validationAuthority = GovernedEntityCreationValidationAuthority(emptyList()),
        )
    }

    private fun blocked(
        request: GovernedEntityCreationPreviewRequest,
        diagnostic: AuthoringDiagnostic,
    ): GovernedEntityCreationPreviewBlocked = blocked(request, listOf(diagnostic))

    private fun blocked(
        request: GovernedEntityCreationPreviewRequest,
        diagnostics: List<AuthoringDiagnostic>,
    ): GovernedEntityCreationPreviewBlocked {
        val preview = AuthoringPreview(
            previewId = request.previewId,
            intentId = request.intent.intentId,
            title = "Create ${request.intent.suggestedName ?: request.intent.conceptTemplateId.value}",
            changes = emptyList(),
            revisionGuard = request.document.revisionGuard,
            acceptanceEligibility = AuthoringAcceptanceEligibility(
                eligible = false,
                diagnostics = diagnostics,
            ),
        )
        return GovernedEntityCreationPreviewBlocked(
            transaction = request.transaction(preview),
            diagnostics = diagnostics,
            validationAuthority = GovernedEntityCreationValidationAuthority(diagnostics),
        )
    }
}

private class GovernedEntityCreationValidationAuthority(
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

private fun GovernedEntityCreationPreviewRequest.transaction(preview: AuthoringPreview): SemanticAuthoringTransaction =
    (SemanticAuthoringTransactionFactory.create(
        transactionId = transactionId,
        intents = listOf(intent),
        capabilityEvidence = capabilityEvidence,
        revisionGuard = document.revisionGuard,
        preview = preview,
        provenance = intent.provenance,
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

private fun EngineeringConceptTemplate.hasValidNestedPortAnatomy(): Boolean =
    nestedPorts.isNotEmpty() && nestedPorts.all { port -> ENTITY_TAG_PATTERN.matches(port.name) }

private fun CreateSemanticEntityIntent.modelValue(): String? = properties.entries
    .firstOrNull { (name, _) -> name.value == "model" }
    ?.value
    ?.let { value -> (value as? AuthoringValue.Text)?.text ?: (value as? AuthoringValue.Symbol)?.text }

private fun EngineeringConceptTemplate.modelValue(): String? =
    propertySchema.firstOrNull { property -> property.name == "model" }?.defaultValue ?: defaultModel

private fun AuthoringDiagnostic.validationStage(): AuthoringValidationStage = when (authority) {
    AuthoringDiagnosticAuthority.CAPABILITY_REGISTRY -> AuthoringValidationStage.CAPABILITY_EVIDENCE
    AuthoringDiagnosticAuthority.SOURCE_PLANNING -> AuthoringValidationStage.SOURCE_PLANNING
    AuthoringDiagnosticAuthority.PARSER -> AuthoringValidationStage.PARSER_VALIDATION
    AuthoringDiagnosticAuthority.REPRESENTATION,
    AuthoringDiagnosticAuthority.COMPOSITION,
    AuthoringDiagnosticAuthority.PROJECTION,
    -> AuthoringValidationStage.PREVIEW_ELIGIBILITY
    else -> AuthoringValidationStage.SEMANTIC_RULES
}

private fun diagnostic(
    code: AuthoringDiagnosticCode,
    message: String,
    authority: AuthoringDiagnosticAuthority,
): AuthoringDiagnostic = AuthoringDiagnostic(
    code = code,
    message = message,
    authority = authority,
    lifecycleStage = AuthoringLifecycleState.BLOCKED,
    recoveryAction = AuthoringRecoveryAction.FIX_SOURCE,
)

private val ENTITY_TAG_PATTERN = Regex("[A-Za-z][A-Za-z0-9_]*")
