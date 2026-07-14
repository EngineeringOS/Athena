package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.reuse.ExpansionMembership
import com.engineeringood.athena.reuse.SemanticMacroId
import com.engineeringood.athena.reuse.SemanticMacroAcceptedExpansion
import com.engineeringood.athena.reuse.SemanticMacroInstantiationId
import com.engineeringood.athena.reuse.SemanticMacroParameterDefinition
import com.engineeringood.athena.reuse.SemanticMacroParameterName
import com.engineeringood.athena.reuse.SemanticMacroParameterValidationRules
import com.engineeringood.athena.reuse.SemanticMacroParameterValue
import com.engineeringood.athena.reuse.SemanticMacroPreview
import com.engineeringood.athena.reuse.SemanticMacroPreviewChange
import com.engineeringood.athena.reuse.SemanticMacroPreviewComponent
import com.engineeringood.athena.reuse.SemanticMacroPreviewConnection
import com.engineeringood.athena.reuse.SemanticMacroPreviewId
import com.engineeringood.athena.reuse.SemanticMacroPreviewOriginAnchor
import com.engineeringood.athena.reuse.SemanticMacroPreviewPort
import com.engineeringood.athena.reuse.SemanticMacroPreviewPresentationConsequence
import com.engineeringood.athena.runtime.AthenaCommandKind
import com.engineeringood.athena.runtime.AthenaMutationOutcome
import com.engineeringood.athena.runtime.AthenaSemanticMacroAcceptanceCommitted
import com.engineeringood.athena.runtime.AthenaSemanticMacroAcceptanceRequest
import com.engineeringood.athena.runtime.AthenaSemanticMacroAcceptanceRejected
import com.engineeringood.athena.runtime.AthenaSemanticMacroAcceptanceResult
import com.engineeringood.athena.runtime.AthenaSemanticMacroAcceptanceUnavailable
import com.engineeringood.athena.runtime.AthenaSemanticMacroOriginInspectionReady
import com.engineeringood.athena.runtime.AthenaSemanticMacroCatalogDiagnostic
import com.engineeringood.athena.runtime.AthenaSemanticMacroCatalogEntry
import com.engineeringood.athena.runtime.AthenaSemanticMacroCatalogRequest
import com.engineeringood.athena.runtime.AthenaSemanticMacroCatalogReady
import com.engineeringood.athena.runtime.AthenaSemanticMacroCatalogResult
import com.engineeringood.athena.runtime.AthenaSemanticMacroCatalogUnavailable
import com.engineeringood.athena.runtime.AthenaSemanticMacroOriginInspectionRequest
import com.engineeringood.athena.runtime.AthenaSemanticMacroOriginInspectionResult
import com.engineeringood.athena.runtime.AthenaSemanticMacroOriginInspectionUnavailable
import com.engineeringood.athena.runtime.AthenaSemanticMacroPreviewReady
import com.engineeringood.athena.runtime.AthenaSemanticMacroPreviewRequest
import com.engineeringood.athena.runtime.AthenaSemanticMacroPreviewResult
import com.engineeringood.athena.runtime.AthenaSemanticMacroPreviewUnavailable
import com.engineeringood.athena.runtime.AthenaSemanticMacroCreateComponentOperation
import com.engineeringood.athena.runtime.AthenaSemanticMacroCreateConnectionOperation
import com.engineeringood.athena.runtime.AthenaSemanticMacroCreatePortOperation
import com.engineeringood.athena.runtime.AthenaSemanticMacroMutationOperation
import com.engineeringood.athena.runtime.AthenaSemanticMacroRegisterExpansionTraceabilityOperation
import com.engineeringood.athena.runtime.AthenaSemanticMacroValidationDiagnostic
import com.engineeringood.athena.runtime.AthenaSemanticMacroValidationInvalid
import com.engineeringood.athena.runtime.AthenaSemanticMacroValidationRequest
import com.engineeringood.athena.runtime.AthenaSemanticMacroValidationResult
import com.engineeringood.athena.runtime.AthenaSemanticMacroValidationValid
import com.engineeringood.athena.runtime.AthenaSemanticMacroValidationUnavailable

/** Empty params object for runtime-owned Semantic Macro catalog inspection requests. */
data class AthenaSemanticMacroCatalogParams(
    val marker: String = "m16",
)

/** Transport-safe parameter value used by Semantic Macro validation and preview requests. */
data class AthenaSemanticMacroParameterValuePayload(
    val kind: String,
    val text: String? = null,
    val booleanValue: Boolean? = null,
    val integerValue: Int? = null,
)

/** Transport DTO for runtime-owned Semantic Macro parameter validation. */
data class AthenaSemanticMacroValidationParams(
    val macroId: String,
    val instantiationId: String,
    val parameterValues: Map<String, AthenaSemanticMacroParameterValuePayload> = emptyMap(),
)

/** Transport DTO for runtime-owned Semantic Macro preview generation. */
data class AthenaSemanticMacroPreviewParams(
    val macroId: String,
    val instantiationId: String,
    val parameterValues: Map<String, AthenaSemanticMacroParameterValuePayload> = emptyMap(),
)

/** Transport DTO for runtime-owned Semantic Macro preview acceptance. */
data class AthenaSemanticMacroAcceptanceParams(
    val previewId: String,
    val macroId: String,
    val instantiationId: String,
)

/** Transport DTO for runtime-owned accepted-expansion origin inspection. */
data class AthenaSemanticMacroOriginInspectionParams(
    val subjectId: String? = null,
    val instantiationId: String? = null,
)

/** Transport payload returned after Semantic Macro catalog lookup. */
data class AthenaSemanticMacroCatalogPayload(
    val projectName: String,
    val semanticPath: String,
    val status: String,
    val entries: List<AthenaSemanticMacroCatalogEntryPayload> = emptyList(),
    val diagnostics: List<AthenaSemanticMacroCatalogDiagnosticPayload> = emptyList(),
    val reason: String? = null,
)

/** Transport payload for one governed Semantic Macro catalog entry. */
data class AthenaSemanticMacroCatalogEntryPayload(
    val macroId: String,
    val displayName: String,
    val summary: String,
    val packageName: String,
    val packageVersion: String? = null,
    val definitionPath: String,
    val classificationKeys: List<String>,
)

/** Transport payload for one Semantic Macro catalog diagnostic. */
data class AthenaSemanticMacroCatalogDiagnosticPayload(
    val code: String,
    val subject: String,
    val message: String,
)

/** Transport payload returned after Semantic Macro parameter validation. */
data class AthenaSemanticMacroValidationPayload(
    val projectName: String,
    val semanticPath: String,
    val status: String,
    val macroId: String,
    val instantiationId: String,
    val parameters: List<AthenaSemanticMacroParameterDefinitionPayload> = emptyList(),
    val normalizedValues: Map<String, AthenaSemanticMacroParameterValuePayload> = emptyMap(),
    val diagnostics: List<AthenaSemanticMacroValidationDiagnosticPayload> = emptyList(),
    val reason: String? = null,
)

/** Transport payload for one Semantic Macro parameter schema definition. */
data class AthenaSemanticMacroParameterDefinitionPayload(
    val name: String,
    val valueKind: String,
    val label: String,
    val description: String? = null,
    val required: Boolean = false,
    val defaultValue: AthenaSemanticMacroParameterValuePayload? = null,
    val validationRules: AthenaSemanticMacroParameterValidationRulesPayload = AthenaSemanticMacroParameterValidationRulesPayload(),
)

/** Transport payload for inspectable Semantic Macro parameter validation rules. */
data class AthenaSemanticMacroParameterValidationRulesPayload(
    val allowedValues: List<String> = emptyList(),
    val pattern: String? = null,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val minInteger: Int? = null,
    val maxInteger: Int? = null,
)

/** Transport payload for one Semantic Macro validation diagnostic. */
data class AthenaSemanticMacroValidationDiagnosticPayload(
    val code: String,
    val parameterName: String? = null,
    val message: String,
)

/** Transport payload returned after Semantic Macro preview generation. */
data class AthenaSemanticMacroPreviewPayload(
    val projectName: String,
    val semanticPath: String,
    val status: String,
    val previewId: String? = null,
    val title: String? = null,
    val macroId: String,
    val instantiationId: String,
    val changes: List<AthenaSemanticMacroPreviewChangePayload> = emptyList(),
    val components: List<AthenaSemanticMacroPreviewComponentPayload> = emptyList(),
    val ports: List<AthenaSemanticMacroPreviewPortPayload> = emptyList(),
    val connections: List<AthenaSemanticMacroPreviewConnectionPayload> = emptyList(),
    val originAnchors: List<AthenaSemanticMacroPreviewOriginAnchorPayload> = emptyList(),
    val presentationConsequences: List<AthenaSemanticMacroPreviewPresentationConsequencePayload> = emptyList(),
    val warnings: List<String> = emptyList(),
    val reason: String? = null,
)

/** Transport payload for one inspectable preview change summary. */
data class AthenaSemanticMacroPreviewChangePayload(
    val kind: String,
    val title: String,
    val summary: String? = null,
    val affectedSubjectIds: List<String> = emptyList(),
)

/** Transport payload for one previewed component consequence. */
data class AthenaSemanticMacroPreviewComponentPayload(
    val templateId: String,
    val conceptId: String,
    val implementationId: String? = null,
    val title: String,
    val summary: String? = null,
    val originAnchorId: String,
    val properties: Map<String, AthenaSemanticMacroParameterValuePayload> = emptyMap(),
    val tags: List<String> = emptyList(),
)

/** Transport payload for one previewed semantic-port consequence. */
data class AthenaSemanticMacroPreviewPortPayload(
    val componentTemplateId: String,
    val portRoleId: String,
    val title: String,
    val originAnchorId: String,
)

/** Transport payload for one previewed connection consequence. */
data class AthenaSemanticMacroPreviewConnectionPayload(
    val templateId: String,
    val fromComponentTemplateId: String,
    val fromPortRoleId: String,
    val toComponentTemplateId: String,
    val toPortRoleId: String,
    val title: String,
    val summary: String? = null,
    val originAnchorId: String,
)

/** Transport payload for one previewed origin anchor. */
data class AthenaSemanticMacroPreviewOriginAnchorPayload(
    val anchorId: String,
    val subjectKind: String,
    val templateId: String,
    val derivedSubjectId: String? = null,
)

/** Transport payload for one previewed presentation consequence. */
data class AthenaSemanticMacroPreviewPresentationConsequencePayload(
    val scope: String,
    val templateId: String? = null,
    val hintType: String,
    val attributes: Map<String, String> = emptyMap(),
    val originAnchorId: String,
)

/** Transport payload returned after Semantic Macro preview acceptance. */
data class AthenaSemanticMacroAcceptedExpansionPayload(
    val expansionId: String,
    val previewId: String,
    val macroId: String,
    val instantiationId: String,
    val packageName: String,
    val packageVersion: String? = null,
    val parameterValues: Map<String, AthenaSemanticMacroParameterValuePayload> = emptyMap(),
    val memberships: List<AthenaSemanticMacroExpansionMembershipPayload> = emptyList(),
)

/** Transport payload for one accepted expansion membership edge. */
data class AthenaSemanticMacroExpansionMembershipPayload(
    val subjectId: String,
    val role: String? = null,
)

/** Transport payload for one prepared mutation operation within the approved bundle. */
data class AthenaSemanticMacroMutationOperationPayload(
    val operationId: String,
    val kind: String,
    val subjectId: String? = null,
    val relatedSubjectIds: List<String> = emptyList(),
    val templateId: String? = null,
    val conceptId: String? = null,
    val implementationId: String? = null,
    val componentTemplateId: String? = null,
    val portRoleId: String? = null,
    val membershipCount: Int? = null,
    val summary: String? = null,
)

/** Transport payload for the command execution that committed one accepted Semantic Macro bundle. */
data class AthenaSemanticMacroAcceptanceExecutionPayload(
    val commandKind: String,
    val outcome: String,
    val commandId: String,
    val changedSemanticIds: List<String> = emptyList(),
)

/** Transport payload returned after Semantic Macro preview acceptance. */
data class AthenaSemanticMacroAcceptancePayload(
    val projectName: String,
    val semanticPath: String,
    val status: String,
    val previewId: String,
    val macroId: String,
    val instantiationId: String,
    val bundleId: String? = null,
    val acceptedExpansion: AthenaSemanticMacroAcceptedExpansionPayload? = null,
    val operations: List<AthenaSemanticMacroMutationOperationPayload> = emptyList(),
    val affectedSemanticIds: List<String> = emptyList(),
    val presentationConsequences: List<AthenaSemanticMacroPreviewPresentationConsequencePayload> = emptyList(),
    val execution: AthenaSemanticMacroAcceptanceExecutionPayload? = null,
    val inspection: AthenaSemanticDiffInspectionPayload? = null,
    val semanticReview: AthenaSemanticMutationReviewPayload? = null,
    val reason: String? = null,
)

/** Transport payload returned after Semantic Macro origin inspection. */
data class AthenaSemanticMacroOriginInspectionPayload(
    val projectName: String,
    val semanticPath: String,
    val status: String,
    val subjectId: String? = null,
    val instantiationId: String? = null,
    val commandId: String? = null,
    val bundleId: String? = null,
    val acceptedExpansion: AthenaSemanticMacroAcceptedExpansionPayload? = null,
    val matchedMembership: AthenaSemanticMacroExpansionMembershipPayload? = null,
    val reason: String? = null,
)

internal fun AthenaSemanticMacroCatalogParams.toRuntimeRequest(): AthenaSemanticMacroCatalogRequest {
    return AthenaSemanticMacroCatalogRequest(marker = marker)
}

internal fun AthenaSemanticMacroValidationParams.toRuntimeRequest(): AthenaSemanticMacroValidationRequest {
    return AthenaSemanticMacroValidationRequest(
        macroId = SemanticMacroId(macroId),
        instantiationId = SemanticMacroInstantiationId(instantiationId),
        parameterValues = parameterValues.mapKeys { (parameterName, _) ->
            SemanticMacroParameterName(parameterName)
        }.mapValues { (_, value) ->
            value.toRuntimeValue()
        },
    )
}

internal fun AthenaSemanticMacroPreviewParams.toRuntimeRequest(): AthenaSemanticMacroPreviewRequest {
    return AthenaSemanticMacroPreviewRequest(
        macroId = SemanticMacroId(macroId),
        instantiationId = SemanticMacroInstantiationId(instantiationId),
        parameterValues = parameterValues.mapKeys { (parameterName, _) ->
            SemanticMacroParameterName(parameterName)
        }.mapValues { (_, value) ->
            value.toRuntimeValue()
        },
    )
}

internal fun AthenaSemanticMacroAcceptanceParams.toRuntimeRequest(): AthenaSemanticMacroAcceptanceRequest {
    return AthenaSemanticMacroAcceptanceRequest(
        previewId = SemanticMacroPreviewId(previewId),
        macroId = SemanticMacroId(macroId),
        instantiationId = SemanticMacroInstantiationId(instantiationId),
    )
}

internal fun AthenaSemanticMacroOriginInspectionParams.toRuntimeRequest(): AthenaSemanticMacroOriginInspectionRequest {
    return AthenaSemanticMacroOriginInspectionRequest(
        subjectId = subjectId?.takeIf(String::isNotBlank)?.let(::StableSemanticIdentity),
        instantiationId = instantiationId?.takeIf(String::isNotBlank)?.let(::SemanticMacroInstantiationId),
    )
}

internal fun AthenaSemanticMacroCatalogResult.toPayload(
    projectName: String,
    semanticPath: String,
): AthenaSemanticMacroCatalogPayload {
    return when (this) {
        is AthenaSemanticMacroCatalogReady -> AthenaSemanticMacroCatalogPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "ready",
            entries = entries.map(AthenaSemanticMacroCatalogEntry::toPayload),
            diagnostics = diagnostics.map(AthenaSemanticMacroCatalogDiagnostic::toPayload),
        )

        is AthenaSemanticMacroCatalogUnavailable -> AthenaSemanticMacroCatalogPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "unavailable",
            diagnostics = diagnostics.map(AthenaSemanticMacroCatalogDiagnostic::toPayload),
            reason = reason,
        )
    }
}

internal fun AthenaSemanticMacroValidationResult.toPayload(
    projectName: String,
    semanticPath: String,
): AthenaSemanticMacroValidationPayload {
    return when (this) {
        is AthenaSemanticMacroValidationValid -> AthenaSemanticMacroValidationPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "valid",
            macroId = macroId.value,
            instantiationId = instantiationId.value,
            parameters = parameters.map(SemanticMacroParameterDefinition::toPayload),
            normalizedValues = normalizedValues.mapKeys { (parameterName, _) -> parameterName.value }
                .mapValues { (_, value) -> value.toPayload() },
        )

        is AthenaSemanticMacroValidationInvalid -> AthenaSemanticMacroValidationPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "invalid",
            macroId = macroId.value,
            instantiationId = instantiationId.value,
            parameters = parameters.map(SemanticMacroParameterDefinition::toPayload),
            normalizedValues = normalizedValues.mapKeys { (parameterName, _) -> parameterName.value }
                .mapValues { (_, value) -> value.toPayload() },
            diagnostics = diagnostics.map(AthenaSemanticMacroValidationDiagnostic::toPayload),
        )

        is AthenaSemanticMacroValidationUnavailable -> AthenaSemanticMacroValidationPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "unavailable",
            macroId = macroId.value,
            instantiationId = instantiationId.value,
            reason = reason,
        )
    }
}

internal fun AthenaSemanticMacroPreviewResult.toPayload(
    projectName: String,
    semanticPath: String,
): AthenaSemanticMacroPreviewPayload {
    return when (this) {
        is AthenaSemanticMacroPreviewReady -> AthenaSemanticMacroPreviewPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "ready",
            previewId = preview.previewId.value,
            title = preview.title,
            macroId = preview.macroId.value,
            instantiationId = preview.instantiationId.value,
            changes = preview.changes.map(SemanticMacroPreviewChange::toPayload),
            components = preview.components.map(SemanticMacroPreviewComponent::toPayload),
            ports = preview.ports.map(SemanticMacroPreviewPort::toPayload),
            connections = preview.connections.map(SemanticMacroPreviewConnection::toPayload),
            originAnchors = preview.originAnchors.map(SemanticMacroPreviewOriginAnchor::toPayload),
            presentationConsequences = preview.presentationConsequences.map(SemanticMacroPreviewPresentationConsequence::toPayload),
            warnings = preview.warnings,
        )

        is AthenaSemanticMacroPreviewUnavailable -> AthenaSemanticMacroPreviewPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "unavailable",
            macroId = macroId.value,
            instantiationId = instantiationId.value,
            reason = reason,
        )
    }
}

internal fun AthenaSemanticMacroAcceptanceResult.toPayload(
    projectName: String,
    semanticPath: String,
): AthenaSemanticMacroAcceptancePayload {
    return when (this) {
        is AthenaSemanticMacroAcceptanceCommitted -> AthenaSemanticMacroAcceptancePayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "accepted",
            previewId = previewId.value,
            macroId = macroId.value,
            instantiationId = instantiationId.value,
            bundleId = bundle.bundleId,
            acceptedExpansion = bundle.acceptedExpansion.toPayload(),
            operations = bundle.operations.map(AthenaSemanticMacroMutationOperation::toPayload),
            affectedSemanticIds = bundle.affectedSemanticIds.map { identity -> identity.value },
            presentationConsequences = bundle.presentationConsequences.map(
                SemanticMacroPreviewPresentationConsequence::toPayload,
            ),
            execution = AthenaSemanticMacroAcceptanceExecutionPayload(
                commandKind = AthenaCommandKind.APPLY_SEMANTIC_MACRO_BUNDLE.name.lowercase().replace('_', '-'),
                outcome = AthenaMutationOutcome.ACCEPTED.name.lowercase().replace('_', '-'),
                commandId = commandId,
                changedSemanticIds = changedSemanticIds.sorted(),
            ),
            inspection = inspection?.toPayload(),
            semanticReview = semanticReview?.toPayload(),
            reason = reason,
        )

        is AthenaSemanticMacroAcceptanceRejected -> AthenaSemanticMacroAcceptancePayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "rejected",
            previewId = previewId.value,
            macroId = macroId.value,
            instantiationId = instantiationId.value,
            bundleId = bundle.bundleId,
            acceptedExpansion = bundle.acceptedExpansion.toPayload(),
            operations = bundle.operations.map(AthenaSemanticMacroMutationOperation::toPayload),
            affectedSemanticIds = bundle.affectedSemanticIds.map { identity -> identity.value },
            presentationConsequences = bundle.presentationConsequences.map(
                SemanticMacroPreviewPresentationConsequence::toPayload,
            ),
            reason = reason,
        )

        is AthenaSemanticMacroAcceptanceUnavailable -> AthenaSemanticMacroAcceptancePayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "unavailable",
            previewId = previewId.value,
            macroId = macroId.value,
            instantiationId = instantiationId.value,
            reason = reason,
        )
    }
}

internal fun AthenaSemanticMacroOriginInspectionResult.toPayload(
    projectName: String,
    semanticPath: String,
): AthenaSemanticMacroOriginInspectionPayload {
    return when (this) {
        is AthenaSemanticMacroOriginInspectionReady -> AthenaSemanticMacroOriginInspectionPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "ready",
            subjectId = subjectId?.value,
            instantiationId = instantiationId.value,
            commandId = commandId,
            bundleId = bundleId,
            acceptedExpansion = acceptedExpansion.toPayload(),
            matchedMembership = matchedMembership?.toPayload(),
        )

        is AthenaSemanticMacroOriginInspectionUnavailable -> AthenaSemanticMacroOriginInspectionPayload(
            projectName = projectName,
            semanticPath = semanticPath,
            status = "unavailable",
            subjectId = subjectId?.value,
            instantiationId = instantiationId?.value,
            reason = reason,
        )
    }
}

private fun SemanticMacroAcceptedExpansion.toPayload(): AthenaSemanticMacroAcceptedExpansionPayload {
    return AthenaSemanticMacroAcceptedExpansionPayload(
        expansionId = expansionId.value,
        previewId = previewId.value,
        macroId = origin.macroId.value,
        instantiationId = origin.instantiationId.value,
        packageName = origin.packageBinding.packageId.name,
        packageVersion = origin.packageBinding.packageId.version,
        parameterValues = origin.parameterValues.mapKeys { (parameterName, _) -> parameterName.value }
            .mapValues { (_, value) -> value.toPayload() },
        memberships = memberships.map(ExpansionMembership::toPayload),
    )
}

private fun ExpansionMembership.toPayload(): AthenaSemanticMacroExpansionMembershipPayload {
    return AthenaSemanticMacroExpansionMembershipPayload(
        subjectId = subjectId.value,
        role = role,
    )
}

private fun AthenaSemanticMacroMutationOperation.toPayload(): AthenaSemanticMacroMutationOperationPayload {
    return when (this) {
        is AthenaSemanticMacroCreateComponentOperation -> AthenaSemanticMacroMutationOperationPayload(
            operationId = operationId,
            kind = kind.name.lowercase(),
            subjectId = subjectId.value,
            templateId = templateId,
            conceptId = conceptId,
            implementationId = implementationId,
            summary = summary,
        )

        is AthenaSemanticMacroCreatePortOperation -> AthenaSemanticMacroMutationOperationPayload(
            operationId = operationId,
            kind = kind.name.lowercase(),
            subjectId = subjectId.value,
            relatedSubjectIds = listOf(componentSubjectId.value),
            componentTemplateId = componentTemplateId,
            portRoleId = portRoleId,
            summary = summary,
        )

        is AthenaSemanticMacroCreateConnectionOperation -> AthenaSemanticMacroMutationOperationPayload(
            operationId = operationId,
            kind = kind.name.lowercase(),
            subjectId = subjectId.value,
            relatedSubjectIds = listOf(fromPortSubjectId.value, toPortSubjectId.value).sorted(),
            templateId = templateId,
            summary = summary,
        )

        is AthenaSemanticMacroRegisterExpansionTraceabilityOperation -> AthenaSemanticMacroMutationOperationPayload(
            operationId = operationId,
            kind = kind.name.lowercase(),
            membershipCount = membershipCount,
            summary = summary,
        )
    }
}

private fun AthenaSemanticMacroCatalogEntry.toPayload(): AthenaSemanticMacroCatalogEntryPayload {
    return AthenaSemanticMacroCatalogEntryPayload(
        macroId = macroId.value,
        displayName = displayName,
        summary = summary,
        packageName = packageId.name,
        packageVersion = packageId.version,
        definitionPath = definitionPath,
        classificationKeys = classificationKeys.toList().sorted(),
    )
}

private fun AthenaSemanticMacroCatalogDiagnostic.toPayload(): AthenaSemanticMacroCatalogDiagnosticPayload {
    return AthenaSemanticMacroCatalogDiagnosticPayload(
        code = code,
        subject = subject,
        message = message,
    )
}

private fun SemanticMacroPreviewChange.toPayload(): AthenaSemanticMacroPreviewChangePayload {
    return AthenaSemanticMacroPreviewChangePayload(
        kind = kind.name.lowercase(),
        title = title,
        summary = summary,
        affectedSubjectIds = affectedSubjectIdentities.map { identity -> identity.value }.sorted(),
    )
}

private fun SemanticMacroPreviewComponent.toPayload(): AthenaSemanticMacroPreviewComponentPayload {
    return AthenaSemanticMacroPreviewComponentPayload(
        templateId = templateId,
        conceptId = conceptId,
        implementationId = implementationId,
        title = title,
        summary = summary,
        originAnchorId = originAnchorId,
        properties = properties.toSortedMap().mapValues { (_, value) -> value.toPayload() },
        tags = tags.toList().sorted(),
    )
}

private fun SemanticMacroPreviewPort.toPayload(): AthenaSemanticMacroPreviewPortPayload {
    return AthenaSemanticMacroPreviewPortPayload(
        componentTemplateId = componentTemplateId,
        portRoleId = portRoleId,
        title = title,
        originAnchorId = originAnchorId,
    )
}

private fun SemanticMacroPreviewConnection.toPayload(): AthenaSemanticMacroPreviewConnectionPayload {
    return AthenaSemanticMacroPreviewConnectionPayload(
        templateId = templateId,
        fromComponentTemplateId = fromComponentTemplateId,
        fromPortRoleId = fromPortRoleId,
        toComponentTemplateId = toComponentTemplateId,
        toPortRoleId = toPortRoleId,
        title = title,
        summary = summary,
        originAnchorId = originAnchorId,
    )
}

private fun SemanticMacroPreviewOriginAnchor.toPayload(): AthenaSemanticMacroPreviewOriginAnchorPayload {
    return AthenaSemanticMacroPreviewOriginAnchorPayload(
        anchorId = anchorId,
        subjectKind = subjectKind,
        templateId = templateId,
        derivedSubjectId = derivedSubjectIdentity?.value,
    )
}

private fun SemanticMacroPreviewPresentationConsequence.toPayload(): AthenaSemanticMacroPreviewPresentationConsequencePayload {
    return AthenaSemanticMacroPreviewPresentationConsequencePayload(
        scope = scope,
        templateId = templateId,
        hintType = hintType,
        attributes = attributes.toSortedMap(),
        originAnchorId = originAnchorId,
    )
}

private fun SemanticMacroParameterDefinition.toPayload(): AthenaSemanticMacroParameterDefinitionPayload {
    return AthenaSemanticMacroParameterDefinitionPayload(
        name = name.value,
        valueKind = valueKind.name.lowercase(),
        label = label,
        description = description,
        required = required,
        defaultValue = defaultValue?.toPayload(),
        validationRules = validationRules.toPayload(),
    )
}

private fun SemanticMacroParameterValidationRules.toPayload(): AthenaSemanticMacroParameterValidationRulesPayload {
    return AthenaSemanticMacroParameterValidationRulesPayload(
        allowedValues = allowedValues,
        pattern = pattern,
        minLength = minLength,
        maxLength = maxLength,
        minInteger = minInteger,
        maxInteger = maxInteger,
    )
}

private fun AthenaSemanticMacroValidationDiagnostic.toPayload(): AthenaSemanticMacroValidationDiagnosticPayload {
    return AthenaSemanticMacroValidationDiagnosticPayload(
        code = code,
        parameterName = parameterName?.value,
        message = message,
    )
}

private fun SemanticMacroParameterValue.toPayload(): AthenaSemanticMacroParameterValuePayload {
    return when (this) {
        is SemanticMacroParameterValue.Text -> AthenaSemanticMacroParameterValuePayload(
            kind = "text",
            text = text,
        )
        is SemanticMacroParameterValue.Symbol -> AthenaSemanticMacroParameterValuePayload(
            kind = "symbol",
            text = text,
        )
        is SemanticMacroParameterValue.BooleanValue -> AthenaSemanticMacroParameterValuePayload(
            kind = "boolean",
            booleanValue = value,
        )
        is SemanticMacroParameterValue.IntegerValue -> AthenaSemanticMacroParameterValuePayload(
            kind = "integer",
            integerValue = value,
        )
    }
}

private fun AthenaSemanticMacroParameterValuePayload.toRuntimeValue(): SemanticMacroParameterValue {
    return when (kind.trim().lowercase()) {
        "text" -> SemanticMacroParameterValue.Text(requireString(text, "text"))
        "symbol" -> SemanticMacroParameterValue.Symbol(requireString(text, "text"))
        "boolean" -> SemanticMacroParameterValue.BooleanValue(
            booleanValue ?: error("Athena Semantic Macro booleanValue is required."),
        )
        "integer" -> SemanticMacroParameterValue.IntegerValue(
            integerValue ?: error("Athena Semantic Macro integerValue is required."),
        )
        else -> error("Athena Semantic Macro value kind must be one of text, symbol, boolean, or integer.")
    }
}

private fun requireString(value: String?, fieldName: String): String {
    return value?.takeIf { text -> text.isNotBlank() }
        ?: error("Athena Semantic Macro field `$fieldName` is required for the selected request shape.")
}
