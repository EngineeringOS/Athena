package com.engineeringood.athena.runtime

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.reuse.ExpansionMembership
import com.engineeringood.athena.reuse.SemanticMacroContract
import com.engineeringood.athena.reuse.SemanticMacroAcceptedExpansion
import com.engineeringood.athena.reuse.SemanticMacroId
import com.engineeringood.athena.reuse.SemanticMacroInstantiationId
import com.engineeringood.athena.reuse.SemanticMacroParameterDefinition
import com.engineeringood.athena.reuse.SemanticMacroParameterName
import com.engineeringood.athena.reuse.SemanticMacroParameterValue
import com.engineeringood.athena.reuse.SemanticMacroParameterValueKind
import com.engineeringood.athena.reuse.SemanticMacroPreview
import com.engineeringood.athena.reuse.SemanticMacroPreviewChange
import com.engineeringood.athena.reuse.SemanticMacroPreviewChangeKind
import com.engineeringood.athena.reuse.SemanticMacroPreviewComponent
import com.engineeringood.athena.reuse.SemanticMacroPreviewConnection
import com.engineeringood.athena.reuse.SemanticMacroPreviewId
import com.engineeringood.athena.reuse.SemanticMacroPreviewOriginAnchor
import com.engineeringood.athena.reuse.SemanticMacroPreviewPort
import com.engineeringood.athena.reuse.SemanticMacroPreviewPresentationConsequence
import com.engineeringood.athena.reuse.SemanticMacroPreviewStatus
import com.engineeringood.athena.template.TemplateValue
import java.nio.file.Path

/** Transport-neutral request for runtime-owned Semantic Macro catalog lookup. */
data class AthenaSemanticMacroCatalogRequest(
    val marker: String = "m16",
)

/** Result of requesting runtime-owned Semantic Macro catalog lookup. */
sealed interface AthenaSemanticMacroCatalogResult

/** One governed Semantic Macro catalog entry resolved from the locked package graph. */
data class AthenaSemanticMacroCatalogEntry(
    val macroId: SemanticMacroId,
    val displayName: String,
    val summary: String,
    val packageId: PackageIdentifier,
    val definitionPath: String,
    val classificationKeys: Set<String> = emptySet(),
)

/** One inspectable diagnostic emitted while resolving governed Semantic Macro catalog entries. */
data class AthenaSemanticMacroCatalogDiagnostic(
    val code: String,
    val subject: String,
    val message: String,
)

/** Indicates that runtime resolved the active Semantic Macro catalog from governed package context. */
data class AthenaSemanticMacroCatalogReady(
    val entries: List<AthenaSemanticMacroCatalogEntry>,
    val diagnostics: List<AthenaSemanticMacroCatalogDiagnostic> = emptyList(),
) : AthenaSemanticMacroCatalogResult

/** Indicates that Semantic Macro catalog lookup is not implemented yet, while the seam exists. */
data class AthenaSemanticMacroCatalogUnavailable(
    val reason: String,
    val diagnostics: List<AthenaSemanticMacroCatalogDiagnostic> = emptyList(),
) : AthenaSemanticMacroCatalogResult

/** Transport-neutral request for runtime-owned Semantic Macro parameter validation. */
data class AthenaSemanticMacroValidationRequest(
    val macroId: SemanticMacroId,
    val instantiationId: SemanticMacroInstantiationId,
    val parameterValues: Map<SemanticMacroParameterName, SemanticMacroParameterValue> = emptyMap(),
)

/** Result of requesting runtime-owned Semantic Macro parameter validation. */
sealed interface AthenaSemanticMacroValidationResult

/** One inspectable validation diagnostic emitted while normalizing one Semantic Macro instantiation. */
data class AthenaSemanticMacroValidationDiagnostic(
    val code: String,
    val parameterName: SemanticMacroParameterName? = null,
    val message: String,
)

/** Indicates that Semantic Macro validation succeeded and preview may proceed to later runtime stages. */
data class AthenaSemanticMacroValidationValid(
    val macroId: SemanticMacroId,
    val instantiationId: SemanticMacroInstantiationId,
    val parameters: List<SemanticMacroParameterDefinition>,
    val normalizedValues: Map<SemanticMacroParameterName, SemanticMacroParameterValue>,
    val diagnostics: List<AthenaSemanticMacroValidationDiagnostic> = emptyList(),
) : AthenaSemanticMacroValidationResult

/** Indicates that Semantic Macro validation blocked preview because values are incomplete or invalid. */
data class AthenaSemanticMacroValidationInvalid(
    val macroId: SemanticMacroId,
    val instantiationId: SemanticMacroInstantiationId,
    val parameters: List<SemanticMacroParameterDefinition>,
    val normalizedValues: Map<SemanticMacroParameterName, SemanticMacroParameterValue>,
    val diagnostics: List<AthenaSemanticMacroValidationDiagnostic>,
) : AthenaSemanticMacroValidationResult {
    init {
        require(diagnostics.isNotEmpty()) {
            "Invalid Semantic Macro validation results must include at least one diagnostic."
        }
    }
}

/** Indicates that Semantic Macro validation is unavailable because governed macro state could not be resolved. */
data class AthenaSemanticMacroValidationUnavailable(
    val macroId: SemanticMacroId,
    val instantiationId: SemanticMacroInstantiationId,
    val reason: String,
) : AthenaSemanticMacroValidationResult

/** Transport-neutral request for runtime-owned Semantic Macro preview generation. */
data class AthenaSemanticMacroPreviewRequest(
    val macroId: SemanticMacroId,
    val instantiationId: SemanticMacroInstantiationId,
    val parameterValues: Map<SemanticMacroParameterName, SemanticMacroParameterValue> = emptyMap(),
)

/** Result of requesting runtime-owned Semantic Macro preview generation. */
sealed interface AthenaSemanticMacroPreviewResult

/** Indicates that Semantic Macro preview generation is not implemented yet, while the seam exists. */
data class AthenaSemanticMacroPreviewUnavailable(
    val macroId: SemanticMacroId,
    val instantiationId: SemanticMacroInstantiationId,
    val reason: String,
) : AthenaSemanticMacroPreviewResult

/** Indicates that runtime assembled a deterministic preview for one governed Semantic Macro. */
data class AthenaSemanticMacroPreviewReady(
    val preview: SemanticMacroPreview,
) : AthenaSemanticMacroPreviewResult

/** Transport-neutral request for runtime-owned Semantic Macro preview acceptance. */
data class AthenaSemanticMacroAcceptanceRequest(
    val previewId: SemanticMacroPreviewId,
    val macroId: SemanticMacroId,
    val instantiationId: SemanticMacroInstantiationId,
)

/** Result of requesting runtime-owned Semantic Macro preview acceptance. */
sealed interface AthenaSemanticMacroAcceptanceResult

/** Indicates that Semantic Macro acceptance is not implemented yet, while the seam exists. */
data class AthenaSemanticMacroAcceptanceUnavailable(
    val previewId: SemanticMacroPreviewId,
    val macroId: SemanticMacroId,
    val instantiationId: SemanticMacroInstantiationId,
    val reason: String,
) : AthenaSemanticMacroAcceptanceResult

/** Indicates that runtime prepared one governed M8-facing mutation bundle for the approved preview. */
data class AthenaSemanticMacroAcceptanceCommitted(
    val previewId: SemanticMacroPreviewId,
    val macroId: SemanticMacroId,
    val instantiationId: SemanticMacroInstantiationId,
    val bundle: AthenaSemanticMacroMutationBundle,
    val commandId: String,
    val changedSemanticIds: List<String>,
    val inspection: AthenaSemanticDiffInspection? = null,
    val semanticReview: AthenaSemanticMutationReview? = null,
    val reason: String,
) : AthenaSemanticMacroAcceptanceResult

/** Indicates that runtime understood one approved preview but could not commit the governed mutation bundle. */
data class AthenaSemanticMacroAcceptanceRejected(
    val previewId: SemanticMacroPreviewId,
    val macroId: SemanticMacroId,
    val instantiationId: SemanticMacroInstantiationId,
    val bundle: AthenaSemanticMacroMutationBundle,
    val reason: String,
) : AthenaSemanticMacroAcceptanceResult

/** Transport-neutral request for runtime-owned accepted-expansion origin inspection. */
data class AthenaSemanticMacroOriginInspectionRequest(
    val subjectId: StableSemanticIdentity? = null,
    val instantiationId: SemanticMacroInstantiationId? = null,
)

/** Result of requesting runtime-owned Semantic Macro origin inspection. */
sealed interface AthenaSemanticMacroOriginInspectionResult

/** Indicates that Athena resolved one applied accepted expansion and its membership facts for inspection. */
data class AthenaSemanticMacroOriginInspectionReady(
    val subjectId: StableSemanticIdentity? = null,
    val instantiationId: SemanticMacroInstantiationId,
    val commandId: String,
    val bundleId: String,
    val acceptedExpansion: SemanticMacroAcceptedExpansion,
    val matchedMembership: ExpansionMembership? = null,
) : AthenaSemanticMacroOriginInspectionResult

/** Indicates that Semantic Macro origin inspection is not implemented yet, while the seam exists. */
data class AthenaSemanticMacroOriginInspectionUnavailable(
    val subjectId: StableSemanticIdentity? = null,
    val instantiationId: SemanticMacroInstantiationId? = null,
    val reason: String,
) : AthenaSemanticMacroOriginInspectionResult

/**
 * Runtime-owned seam for Semantic Macro catalog, validation, preview, acceptance, and origin inspection.
 *
 * Story 1.3 publishes the shared platform boundary only. Later M16 stories populate these methods
 * with governed implementations without making one Theia panel or graph widget the source of truth.
 */
class AthenaSemanticMacroRuntimeService internal constructor(
    private val catalogResolver: AthenaSemanticMacroCatalogResolver = AthenaSemanticMacroCatalogResolver(),
    private val definitionLoader: AthenaSemanticMacroDefinitionLoader = AthenaSemanticMacroDefinitionLoader(),
) {
    /** Exposes runtime-owned catalog lookup without binding the boundary to one specific UI surface. */
    fun catalog(
        context: AthenaExecutionContext,
        request: AthenaSemanticMacroCatalogRequest,
    ): AthenaSemanticMacroCatalogResult {
        @Suppress("UnusedParameter")
        val ignored = request
        val publication = context.services.repositoryReports().publishRepositoryGraphReport(context.project.workspaceRoot)
        if (!publication.isValid) {
            return AthenaSemanticMacroCatalogUnavailable(
                reason = buildCatalogUnavailableReason(publication),
                diagnostics = publication.diagnostics.map { diagnostic ->
                    AthenaSemanticMacroCatalogDiagnostic(
                        code = diagnostic.code,
                        subject = "repository",
                        message = diagnostic.message,
                    )
                },
            )
        }

        return catalogResolver.resolve(
            repositoryRoot = context.project.workspaceRoot,
            publication = publication,
        )
    }

    /** Exposes runtime-owned parameter validation without moving rule ownership into the workbench. */
    fun validate(
        context: AthenaExecutionContext,
        request: AthenaSemanticMacroValidationRequest,
    ): AthenaSemanticMacroValidationResult {
        val publication = context.services.repositoryReports().publishRepositoryGraphReport(context.project.workspaceRoot)
        if (!publication.isValid) {
            return AthenaSemanticMacroValidationUnavailable(
                macroId = request.macroId,
                instantiationId = request.instantiationId,
                reason = buildCatalogUnavailableReason(publication),
            )
        }

        val resolution = catalogResolver.resolveContracts(
            repositoryRoot = context.project.workspaceRoot,
            publication = publication,
        )
        val contract = resolution.contracts.firstOrNull { candidate -> candidate.macroId == request.macroId }
            ?: return AthenaSemanticMacroValidationInvalid(
                macroId = request.macroId,
                instantiationId = request.instantiationId,
                parameters = emptyList(),
                normalizedValues = emptyMap(),
                diagnostics = buildList {
                    addAll(resolution.diagnostics.map { diagnostic ->
                        AthenaSemanticMacroValidationDiagnostic(
                            code = diagnostic.code,
                            message = diagnostic.message,
                        )
                    })
                    add(
                        AthenaSemanticMacroValidationDiagnostic(
                            code = "semantic.macro.validation.macro.unresolved",
                            message = "Semantic Macro `${request.macroId.value}` is not available through the active governed repository graph.",
                        ),
                    )
                },
            )

        return validateAgainstContract(
            request = request,
            contract = contract,
        )
    }

    /** Exposes runtime-owned preview generation without allowing direct frontend expansion logic. */
    fun preview(
        context: AthenaExecutionContext,
        request: AthenaSemanticMacroPreviewRequest,
    ): AthenaSemanticMacroPreviewResult {
        val validation = when (
            val result = validate(
                context = context,
                request = AthenaSemanticMacroValidationRequest(
                    macroId = request.macroId,
                    instantiationId = request.instantiationId,
                    parameterValues = request.parameterValues,
                ),
            )
        ) {
            is AthenaSemanticMacroValidationValid -> result
            is AthenaSemanticMacroValidationInvalid -> {
                return AthenaSemanticMacroPreviewUnavailable(
                    macroId = request.macroId,
                    instantiationId = request.instantiationId,
                    reason = "Semantic Macro preview is blocked until validation succeeds: " +
                        result.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
                )
            }
            is AthenaSemanticMacroValidationUnavailable -> {
                return AthenaSemanticMacroPreviewUnavailable(
                    macroId = request.macroId,
                    instantiationId = request.instantiationId,
                    reason = result.reason,
                )
            }
        }

        val publication = context.services.repositoryReports().publishRepositoryGraphReport(context.project.workspaceRoot)
        if (!publication.isValid) {
            return AthenaSemanticMacroPreviewUnavailable(
                macroId = request.macroId,
                instantiationId = request.instantiationId,
                reason = buildCatalogUnavailableReason(publication),
            )
        }

        val resolution = catalogResolver.resolveContracts(
            repositoryRoot = context.project.workspaceRoot,
            publication = publication,
        )
        val contract = resolution.contracts.firstOrNull { candidate -> candidate.macroId == request.macroId }
            ?: return AthenaSemanticMacroPreviewUnavailable(
                macroId = request.macroId,
                instantiationId = request.instantiationId,
                reason = "Semantic Macro `${request.macroId.value}` is not available through the active governed repository graph.",
            )
        val packageRoot = resolvePackageRoot(
            repositoryRoot = context.project.workspaceRoot,
            publication = publication,
            packageId = contract.packageBinding.packageId,
        ) ?: return AthenaSemanticMacroPreviewUnavailable(
            macroId = request.macroId,
            instantiationId = request.instantiationId,
            reason = "Semantic Macro package `${contract.packageBinding.packageId.name}` could not be resolved from the active repository graph.",
        )
        val definition = definitionLoader.load(
            packageRoot = packageRoot,
            contract = contract,
        )
        val template = definition.template ?: return AthenaSemanticMacroPreviewUnavailable(
            macroId = request.macroId,
            instantiationId = request.instantiationId,
            reason = "Semantic Macro preview is unavailable because definition loading failed: " +
                definition.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
        )

        val preview = buildDeterministicPreview(
            contract = contract,
            instantiationId = request.instantiationId,
            normalizedValues = validation.normalizedValues,
            template = template,
        )
        context.replaceSemanticMacroPreviewSessionState(
            context.semanticMacroPreviewSessionState().recordPreview(
                AthenaSemanticMacroPreviewRecord(
                    preview = preview,
                    packageBinding = contract.packageBinding,
                    normalizedValues = validation.normalizedValues,
                ),
            ),
        )
        return AthenaSemanticMacroPreviewReady(preview = preview)
    }

    /** Exposes runtime-owned acceptance without creating a second mutation path outside later M8 handoff. */
    fun accept(
        context: AthenaExecutionContext,
        request: AthenaSemanticMacroAcceptanceRequest,
    ): AthenaSemanticMacroAcceptanceResult {
        val previewState = context.semanticMacroPreviewSessionState()
        val recordIndex = previewState.records.indexOfFirst { record ->
            record.preview.previewId == request.previewId
        }
        if (recordIndex < 0) {
            return AthenaSemanticMacroAcceptanceUnavailable(
                previewId = request.previewId,
                macroId = request.macroId,
                instantiationId = request.instantiationId,
                reason = "Semantic Macro preview `${request.previewId.value}` is not present in the active runtime session.",
            )
        }

        val record = previewState.records[recordIndex]
        if (record.preview.macroId != request.macroId) {
            return AthenaSemanticMacroAcceptanceUnavailable(
                previewId = request.previewId,
                macroId = request.macroId,
                instantiationId = request.instantiationId,
                reason = "Semantic Macro preview `${request.previewId.value}` does not match macro `${request.macroId.value}`.",
            )
        }
        if (record.preview.instantiationId != request.instantiationId) {
            return AthenaSemanticMacroAcceptanceUnavailable(
                previewId = request.previewId,
                macroId = request.macroId,
                instantiationId = request.instantiationId,
                reason = "Semantic Macro preview `${request.previewId.value}` does not match instantiation `${request.instantiationId.value}`.",
            )
        }

        val acceptedRecord = record.copy(
            preview = record.preview.copy(status = SemanticMacroPreviewStatus.ACCEPTED),
        )
        context.replaceSemanticMacroPreviewSessionState(
            previewState.copy(
                records = previewState.records.toMutableList().apply {
                    set(recordIndex, acceptedRecord)
                }.toList(),
            ),
        )

        val bundle = acceptedRecord.toMutationBundle()
        return when (
            val execution = context.commandRuntime().execute(
                context = context,
                command = AthenaApplySemanticMacroBundleCommand(bundle),
                origin = AthenaCommandOrigin.SEMANTIC_MACRO_ACCEPTED,
            )
        ) {
            is AthenaCommandExecutionSuccess -> AthenaSemanticMacroAcceptanceCommitted(
                previewId = request.previewId,
                macroId = request.macroId,
                instantiationId = request.instantiationId,
                bundle = bundle,
                commandId = execution.commandId,
                changedSemanticIds = execution.changedSemanticIds.sorted(),
                inspection = context.latestSemanticDiffInspection(),
                semanticReview = context.semanticMutationReviews().summarizeAcceptedMutation(
                    context = context,
                    beforeDocument = execution.beforeDocument,
                    afterDocument = execution.afterDocument,
                ),
                reason = "Committed approved Semantic Macro bundle through the sole M8 mutation authority. Canonical, graph, inspection, and review state were refreshed.",
            )

            is AthenaCommandExecutionRejected -> AthenaSemanticMacroAcceptanceRejected(
                previewId = request.previewId,
                macroId = request.macroId,
                instantiationId = request.instantiationId,
                bundle = bundle,
                reason = execution.reason,
            )

            is AthenaCommandExecutionValidationFeedback -> AthenaSemanticMacroAcceptanceRejected(
                previewId = request.previewId,
                macroId = request.macroId,
                instantiationId = request.instantiationId,
                bundle = bundle,
                reason = execution.validationFeedback.joinToString(separator = "; ") { feedback -> feedback.message },
            )

            is AthenaCommandExecutionUnavailable -> AthenaSemanticMacroAcceptanceUnavailable(
                previewId = request.previewId,
                macroId = request.macroId,
                instantiationId = request.instantiationId,
                reason = execution.reason,
            )
        }
    }

    /** Exposes runtime-owned origin inspection without moving traceability truth into frontend code. */
    fun inspectOrigin(
        context: AthenaExecutionContext,
        request: AthenaSemanticMacroOriginInspectionRequest,
    ): AthenaSemanticMacroOriginInspectionResult {
        if (request.subjectId == null && request.instantiationId == null) {
            return AthenaSemanticMacroOriginInspectionUnavailable(
                reason = "Semantic Macro origin inspection requires either a subjectId or an instantiationId.",
            )
        }

        val match = context.commandRuntime()
            .history(context)
            .appliedSemanticMacroBundleRecords()
            .firstOrNull { candidate ->
                val expansion = candidate.bundle.acceptedExpansion
                val matchesSubject = request.subjectId == null || expansion.memberships.any { membership ->
                    membership.subjectId == request.subjectId
                }
                val matchesInstantiation = request.instantiationId == null ||
                    expansion.origin.instantiationId == request.instantiationId
                matchesSubject && matchesInstantiation
            }

        if (match == null) {
            return AthenaSemanticMacroOriginInspectionUnavailable(
                subjectId = request.subjectId,
                instantiationId = request.instantiationId,
                reason = when {
                    request.subjectId != null ->
                        "No applied accepted Semantic Macro expansion includes subject `${request.subjectId.value}`."
                    request.instantiationId != null ->
                        "No applied accepted Semantic Macro expansion exists for instantiation `${request.instantiationId.value}`."
                    else -> "No applied accepted Semantic Macro expansion matches the requested traceability query."
                },
            )
        }

        val matchedMembership = request.subjectId?.let { subjectId ->
            match.bundle.acceptedExpansion.memberships.firstOrNull { membership -> membership.subjectId == subjectId }
        }
        return AthenaSemanticMacroOriginInspectionReady(
            subjectId = request.subjectId,
            instantiationId = match.bundle.acceptedExpansion.origin.instantiationId,
            commandId = match.commandId,
            bundleId = match.bundle.bundleId,
            acceptedExpansion = match.bundle.acceptedExpansion,
            matchedMembership = matchedMembership,
        )
    }
}

private data class AppliedSemanticMacroBundleRecord(
    val commandId: String,
    val bundle: AthenaSemanticMacroMutationBundle,
)

private fun AthenaCommandHistory.appliedSemanticMacroBundleRecords(): List<AppliedSemanticMacroBundleRecord> {
    return records.asReversed().mapNotNull { record ->
        if (record.status != AthenaCommandHistoryRecordStatus.APPLIED) {
            return@mapNotNull null
        }
        val command = record.command as? AthenaApplySemanticMacroBundleCommand ?: return@mapNotNull null
        AppliedSemanticMacroBundleRecord(
            commandId = record.commandId,
            bundle = command.bundle,
        )
    }
}

private fun validateAgainstContract(
    request: AthenaSemanticMacroValidationRequest,
    contract: SemanticMacroContract,
): AthenaSemanticMacroValidationResult {
    val diagnostics = mutableListOf<AthenaSemanticMacroValidationDiagnostic>()
    val normalizedValues = linkedMapOf<SemanticMacroParameterName, SemanticMacroParameterValue>()
    val knownParameters = contract.parameters.associateBy(SemanticMacroParameterDefinition::name)

    request.parameterValues.keys
        .filter { parameterName -> parameterName !in knownParameters.keys }
        .sortedBy(SemanticMacroParameterName::value)
        .forEach { parameterName ->
            diagnostics += AthenaSemanticMacroValidationDiagnostic(
                code = "semantic.macro.validation.parameter.unknown",
                parameterName = parameterName,
                message = "Semantic Macro `${request.macroId.value}` does not declare parameter `${parameterName.value}`.",
            )
        }

    contract.parameters.forEach { parameter ->
        val suppliedValue = request.parameterValues[parameter.name]
        val normalizedValue = suppliedValue ?: parameter.defaultValue
        if (normalizedValue == null) {
            if (parameter.required) {
                diagnostics += AthenaSemanticMacroValidationDiagnostic(
                    code = "semantic.macro.validation.parameter.required",
                    parameterName = parameter.name,
                    message = "Semantic Macro parameter `${parameter.name.value}` is required.",
                )
            }
            return@forEach
        }

        if (!normalizedValue.matches(parameter.valueKind)) {
            diagnostics += AthenaSemanticMacroValidationDiagnostic(
                code = "semantic.macro.validation.parameter.kind-mismatch",
                parameterName = parameter.name,
                message = "Semantic Macro parameter `${parameter.name.value}` requires `${parameter.valueKind.name.lowercase()}` values.",
            )
            return@forEach
        }

        diagnostics += validateValueRules(parameter, normalizedValue)
        normalizedValues[parameter.name] = normalizedValue
    }

    return if (diagnostics.isEmpty()) {
        AthenaSemanticMacroValidationValid(
            macroId = request.macroId,
            instantiationId = request.instantiationId,
            parameters = contract.parameters,
            normalizedValues = normalizedValues,
        )
    } else {
        AthenaSemanticMacroValidationInvalid(
            macroId = request.macroId,
            instantiationId = request.instantiationId,
            parameters = contract.parameters,
            normalizedValues = normalizedValues,
            diagnostics = diagnostics.sortedWith(
                compareBy<AthenaSemanticMacroValidationDiagnostic>(
                    { diagnostic -> diagnostic.parameterName?.value.orEmpty() },
                    { diagnostic -> diagnostic.code },
                    { diagnostic -> diagnostic.message },
                ),
            ),
        )
    }
}

private fun validateValueRules(
    parameter: SemanticMacroParameterDefinition,
    value: SemanticMacroParameterValue,
): List<AthenaSemanticMacroValidationDiagnostic> {
    val diagnostics = mutableListOf<AthenaSemanticMacroValidationDiagnostic>()
    val rules = parameter.validationRules
    when (value) {
        is SemanticMacroParameterValue.Text -> {
            diagnostics += validateTextLikeRules(parameter, value.text, rules)
        }
        is SemanticMacroParameterValue.Symbol -> {
            diagnostics += validateTextLikeRules(parameter, value.text, rules)
        }
        is SemanticMacroParameterValue.IntegerValue -> {
            val minInteger = rules.minInteger
            val maxInteger = rules.maxInteger
            if (minInteger != null && value.value < minInteger) {
                diagnostics += AthenaSemanticMacroValidationDiagnostic(
                    code = "semantic.macro.validation.parameter.min-integer",
                    parameterName = parameter.name,
                    message = "Semantic Macro parameter `${parameter.name.value}` must be >= $minInteger.",
                )
            }
            if (maxInteger != null && value.value > maxInteger) {
                diagnostics += AthenaSemanticMacroValidationDiagnostic(
                    code = "semantic.macro.validation.parameter.max-integer",
                    parameterName = parameter.name,
                    message = "Semantic Macro parameter `${parameter.name.value}` must be <= $maxInteger.",
                )
            }
        }
        is SemanticMacroParameterValue.BooleanValue -> Unit
    }
    return diagnostics
}

private fun validateTextLikeRules(
    parameter: SemanticMacroParameterDefinition,
    text: String,
    rules: com.engineeringood.athena.reuse.SemanticMacroParameterValidationRules,
): List<AthenaSemanticMacroValidationDiagnostic> {
    val diagnostics = mutableListOf<AthenaSemanticMacroValidationDiagnostic>()
    val pattern = rules.pattern
    val minLength = rules.minLength
    val maxLength = rules.maxLength
    if (rules.allowedValues.isNotEmpty() && text !in rules.allowedValues) {
        diagnostics += AthenaSemanticMacroValidationDiagnostic(
            code = "semantic.macro.validation.parameter.allowed-values",
            parameterName = parameter.name,
            message = "Semantic Macro parameter `${parameter.name.value}` must be one of ${rules.allowedValues.joinToString()}.",
        )
    }
    if (pattern != null && !Regex(pattern).matches(text)) {
        diagnostics += AthenaSemanticMacroValidationDiagnostic(
            code = "semantic.macro.validation.parameter.pattern",
            parameterName = parameter.name,
            message = "Semantic Macro parameter `${parameter.name.value}` does not match `$pattern`.",
        )
    }
    if (minLength != null && text.length < minLength) {
        diagnostics += AthenaSemanticMacroValidationDiagnostic(
            code = "semantic.macro.validation.parameter.min-length",
            parameterName = parameter.name,
            message = "Semantic Macro parameter `${parameter.name.value}` must be at least $minLength characters.",
        )
    }
    if (maxLength != null && text.length > maxLength) {
        diagnostics += AthenaSemanticMacroValidationDiagnostic(
            code = "semantic.macro.validation.parameter.max-length",
            parameterName = parameter.name,
            message = "Semantic Macro parameter `${parameter.name.value}` must be at most $maxLength characters.",
        )
    }
    return diagnostics
}

private fun buildDeterministicPreview(
    contract: SemanticMacroContract,
    instantiationId: SemanticMacroInstantiationId,
    normalizedValues: Map<SemanticMacroParameterName, SemanticMacroParameterValue>,
    template: AthenaSemanticMacroTemplateDefinition,
): SemanticMacroPreview {
    val warnings = mutableListOf<String>()
    val components = template.componentTemplates
        .sortedBy { component -> component.templateId.value }
        .map { component ->
            val resolvedProperties = linkedMapOf<String, SemanticMacroParameterValue>()
            component.properties.entries
                .sortedBy { (propertyName, _) -> propertyName.value }
                .forEach { (propertyName, value) ->
                    when (value) {
                        is TemplateValue.Literal -> {
                            resolvedProperties[propertyName.value] = value.value
                        }

                        is TemplateValue.ParameterReference -> {
                            val parameterValue = normalizedValues[value.parameterName]
                            if (parameterValue == null) {
                                warnings += "Component template `${component.templateId.value}` skipped property `${propertyName.value}` because parameter `${value.parameterName.value}` is not set."
                            } else {
                                resolvedProperties[propertyName.value] = parameterValue
                            }
                        }
                    }
                }

            SemanticMacroPreviewComponent(
                templateId = component.templateId.value,
                conceptId = component.conceptId.value,
                implementationId = component.implementationId?.value,
                title = component.defaultMetadata.displayName ?: component.templateId.value,
                summary = component.defaultMetadata.summary,
                originAnchorId = componentOriginAnchorId(instantiationId, component.templateId.value),
                properties = resolvedProperties,
                tags = component.defaultMetadata.tags,
            )
        }
    val ports = template.connectionTemplates
        .flatMap { connection -> listOf(connection.from, connection.to) }
        .distinctBy { reference -> reference.componentTemplateId.value to reference.portRoleId.value }
        .sortedWith(
            compareBy(
                { reference: com.engineeringood.athena.template.TemplatePortReference -> reference.componentTemplateId.value },
                { reference -> reference.portRoleId.value },
            ),
        )
        .map { reference ->
            SemanticMacroPreviewPort(
                componentTemplateId = reference.componentTemplateId.value,
                portRoleId = reference.portRoleId.value,
                title = "${reference.componentTemplateId.value} ${reference.portRoleId.value}",
                originAnchorId = portOriginAnchorId(
                    instantiationId = instantiationId,
                    componentTemplateId = reference.componentTemplateId.value,
                    portRoleId = reference.portRoleId.value,
                ),
            )
        }
    val connections = template.connectionTemplates
        .sortedBy { connection -> connection.templateId.value }
        .map { connection ->
            SemanticMacroPreviewConnection(
                templateId = connection.templateId.value,
                fromComponentTemplateId = connection.from.componentTemplateId.value,
                fromPortRoleId = connection.from.portRoleId.value,
                toComponentTemplateId = connection.to.componentTemplateId.value,
                toPortRoleId = connection.to.portRoleId.value,
                title = connection.defaultMetadata.displayName ?: connection.templateId.value,
                summary = connection.defaultMetadata.summary,
                originAnchorId = connectionOriginAnchorId(instantiationId, connection.templateId.value),
            )
        }
    val presentationConsequences = buildList {
        addAll(
            template.presentationHints.sortedBy { hint -> hint.hintType }.map { hint ->
                SemanticMacroPreviewPresentationConsequence(
                    scope = "macro",
                    hintType = hint.hintType,
                    attributes = hint.attributes.toSortedMap(),
                    originAnchorId = macroOriginAnchorId(instantiationId, hint.hintType),
                )
            },
        )
        addAll(
            template.componentTemplates
                .sortedBy { component -> component.templateId.value }
                .flatMap { component ->
                    component.presentationHints.sortedBy { hint -> hint.hintType }.map { hint ->
                        SemanticMacroPreviewPresentationConsequence(
                            scope = "component",
                            templateId = component.templateId.value,
                            hintType = hint.hintType,
                            attributes = hint.attributes.toSortedMap(),
                            originAnchorId = componentOriginAnchorId(instantiationId, component.templateId.value),
                        )
                    }
                },
        )
        addAll(
            template.connectionTemplates
                .sortedBy { connection -> connection.templateId.value }
                .flatMap { connection ->
                    connection.presentationHints.sortedBy { hint -> hint.hintType }.map { hint ->
                        SemanticMacroPreviewPresentationConsequence(
                            scope = "connection",
                            templateId = connection.templateId.value,
                            hintType = hint.hintType,
                            attributes = hint.attributes.toSortedMap(),
                            originAnchorId = connectionOriginAnchorId(instantiationId, connection.templateId.value),
                        )
                    }
                },
        )
    }
    val originAnchors = buildList {
        addAll(
            components.map { component ->
                SemanticMacroPreviewOriginAnchor(
                    anchorId = component.originAnchorId,
                    subjectKind = "component",
                    templateId = component.templateId,
                    derivedSubjectIdentity = derivedComponentSubjectId(instantiationId, component.templateId),
                )
            },
        )
        addAll(
            ports.map { port ->
                SemanticMacroPreviewOriginAnchor(
                    anchorId = port.originAnchorId,
                    subjectKind = "port",
                    templateId = "${port.componentTemplateId}:${port.portRoleId}",
                    derivedSubjectIdentity = derivedPortSubjectId(
                        instantiationId = instantiationId,
                        componentTemplateId = port.componentTemplateId,
                        portRoleId = port.portRoleId,
                    ),
                )
            },
        )
        addAll(
            connections.map { connection ->
                SemanticMacroPreviewOriginAnchor(
                    anchorId = connection.originAnchorId,
                    subjectKind = "connection",
                    templateId = connection.templateId,
                    derivedSubjectIdentity = derivedConnectionSubjectId(instantiationId, connection.templateId),
                )
            },
        )
        addAll(
            presentationConsequences
                .filter { consequence -> consequence.scope == "macro" }
                .map { consequence ->
                    SemanticMacroPreviewOriginAnchor(
                        anchorId = consequence.originAnchorId,
                        subjectKind = "presentation",
                        templateId = consequence.hintType,
                    )
                },
        )
    }.sortedBy { anchor -> anchor.anchorId }
    val changes = buildList {
        addAll(
            components.map { component ->
                SemanticMacroPreviewChange(
                    kind = SemanticMacroPreviewChangeKind.CREATE,
                    title = "Create ${component.title}",
                    summary = component.summary ?: component.conceptId,
                )
            },
        )
        addAll(
            ports.map { port ->
                SemanticMacroPreviewChange(
                    kind = SemanticMacroPreviewChangeKind.CREATE,
                    title = "Expose ${port.title}",
                    summary = "Semantic port ${port.portRoleId} on ${port.componentTemplateId}",
                )
            },
        )
        addAll(
            connections.map { connection ->
                SemanticMacroPreviewChange(
                    kind = SemanticMacroPreviewChangeKind.CONNECT,
                    title = "Connect ${connection.title}",
                    summary = "${connection.fromComponentTemplateId}.${connection.fromPortRoleId} -> ${connection.toComponentTemplateId}.${connection.toPortRoleId}",
                )
            },
        )
        addAll(
            presentationConsequences.map { consequence ->
                SemanticMacroPreviewChange(
                    kind = SemanticMacroPreviewChangeKind.REVEAL,
                    title = "Reveal ${consequence.hintType}",
                    summary = consequence.templateId ?: consequence.scope,
                )
            },
        )
    }

    return SemanticMacroPreview(
        previewId = buildPreviewId(contract.macroId, instantiationId),
        macroId = contract.macroId,
        instantiationId = instantiationId,
        title = "Preview ${contract.displayName} (${instantiationId.value})",
        status = SemanticMacroPreviewStatus.PENDING_REVIEW,
        changes = changes,
        components = components,
        ports = ports,
        connections = connections,
        originAnchors = originAnchors,
        presentationConsequences = presentationConsequences,
        warnings = warnings.sorted(),
    )
}

private fun buildPreviewId(
    macroId: SemanticMacroId,
    instantiationId: SemanticMacroInstantiationId,
): SemanticMacroPreviewId {
    val macroToken = macroId.value.substringAfter("macro:")
    val instantiationToken = instantiationId.value.substringAfter("instance:")
    return SemanticMacroPreviewId("preview:$macroToken:$instantiationToken")
}

private fun componentOriginAnchorId(
    instantiationId: SemanticMacroInstantiationId,
    templateId: String,
): String = "origin:${instantiationId.value}:component:$templateId"

private fun portOriginAnchorId(
    instantiationId: SemanticMacroInstantiationId,
    componentTemplateId: String,
    portRoleId: String,
): String = "origin:${instantiationId.value}:port:$componentTemplateId:$portRoleId"

private fun connectionOriginAnchorId(
    instantiationId: SemanticMacroInstantiationId,
    templateId: String,
): String = "origin:${instantiationId.value}:connection:$templateId"

private fun macroOriginAnchorId(
    instantiationId: SemanticMacroInstantiationId,
    hintType: String,
): String = "origin:${instantiationId.value}:presentation:$hintType"

private fun resolvePackageRoot(
    repositoryRoot: Path,
    publication: com.engineeringood.athena.compiler.repository.AthenaRepositoryReportPublicationResult,
    packageId: PackageIdentifier,
): Path? {
    val graph = publication.report?.graph ?: return null
    return graph.packages.firstOrNull { resolvedPackage -> resolvedPackage.packageId == packageId }
        ?.let { resolvedPackage ->
            repositoryRoot.resolve(resolvedPackage.sourceRoot).normalize().parent
        }
}

private fun SemanticMacroParameterValue.matches(kind: SemanticMacroParameterValueKind): Boolean {
    return when (kind) {
        SemanticMacroParameterValueKind.TEXT -> this is SemanticMacroParameterValue.Text
        SemanticMacroParameterValueKind.SYMBOL -> this is SemanticMacroParameterValue.Symbol
        SemanticMacroParameterValueKind.BOOLEAN -> this is SemanticMacroParameterValue.BooleanValue
        SemanticMacroParameterValueKind.INTEGER -> this is SemanticMacroParameterValue.IntegerValue
    }
}

private fun buildCatalogUnavailableReason(
    publication: com.engineeringood.athena.compiler.repository.AthenaRepositoryReportPublicationResult,
): String {
    val diagnostics = publication.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message }
    return when {
        diagnostics.isNotBlank() -> "Semantic Macro catalog requires a current governed repository graph and athena.lock. Current repository state is `${publication.lockState.name.lowercase()}`: $diagnostics"
        else -> "Semantic Macro catalog requires a current governed repository graph and athena.lock. Current repository state is `${publication.lockState.name.lowercase()}`."
    }
}
