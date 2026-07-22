package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.knowledge.AthenaComponentKnowledgeDiagnostic
import com.engineeringood.athena.compiler.knowledge.AthenaComponentKnowledgeSeverity
import com.engineeringood.athena.runtime.AthenaAvailableAuthoringComponent
import com.engineeringood.athena.runtime.AthenaComponentKnowledgeReady
import com.engineeringood.athena.runtime.AthenaComponentKnowledgeRuntimeResult
import com.engineeringood.athena.runtime.AthenaComponentKnowledgeUnavailable
import com.engineeringood.athena.runtime.AthenaResolvedComponentKnowledgeEntry
import com.engineeringood.athena.domain.electricalruntime.electricalEngineeringConceptTemplates

/** Empty params object for runtime-owned component-knowledge session requests. */
data class AthenaComponentKnowledgeSessionParams(
    val marker: String = "m14",
)

/** One typed component-knowledge diagnostic transported through the Athena LSP boundary. */
data class AthenaComponentKnowledgeDiagnosticPayload(
    val severity: String,
    val ruleId: String,
    val subject: String,
    val message: String,
)

/** One resolved component-knowledge entry transported through the Athena LSP boundary. */
data class AthenaResolvedComponentKnowledgePayload(
    val semanticSubjectId: String,
    val authoredComponentReference: String,
    val conceptId: String,
    val conceptDisplayName: String,
    val implementationId: String? = null,
    val vendorId: String? = null,
    val vendorPartNumber: String? = null,
)

/** One resolved semantic-port payload transported through the Athena LSP boundary. */
data class AthenaResolvedSemanticPortPayload(
    val portSemanticId: String,
    val ownerSemanticId: String,
    val portTypeId: String,
    val roleId: String,
    val direction: String,
    val signalFamilyId: String,
    val protocolIds: List<String>,
)

/** One resolved physical-trait payload transported through the Athena LSP boundary. */
data class AthenaResolvedPhysicalTraitPayload(
    val semanticSubjectId: String,
    val displayName: String,
    val widthMillimeters: Int,
    val heightMillimeters: Int,
    val depthMillimeters: Int,
    val mountingTypeId: String,
    val installationMarkerIds: List<String>,
)

/** One available vendor implementation for guided authoring transported through Athena LSP. */
data class AthenaAvailableComponentImplementationPayload(
    val implementationId: String,
    val vendorId: String,
    val vendorPartNumber: String,
    val displayName: String,
    val summary: String? = null,
)

/** One available authoring component concept transported through Athena LSP. */
data class AthenaAvailableComponentPayload(
    val conceptId: String,
    val authoringTemplateIds: List<String>,
    val displayName: String,
    val classificationKeys: List<String>,
    val summary: String? = null,
    val implementations: List<AthenaAvailableComponentImplementationPayload>,
)

/** Runtime-owned component-knowledge session payload transported through Athena LSP. */
data class AthenaComponentKnowledgeSessionPayload(
    val projectName: String,
    val systemSemanticId: String,
    val semanticPath: String,
    val status: String,
    val contributingPluginIds: List<String>,
    val activeConceptCount: Int,
    val activeImplementationCount: Int,
    val availableComponents: List<AthenaAvailableComponentPayload>,
    val components: List<AthenaResolvedComponentKnowledgePayload>,
    val semanticPorts: List<AthenaResolvedSemanticPortPayload>,
    val physicalTraits: List<AthenaResolvedPhysicalTraitPayload>,
    val diagnostics: List<AthenaComponentKnowledgeDiagnosticPayload>,
    val unavailableReason: String? = null,
)

internal fun AthenaComponentKnowledgeRuntimeResult.toPayload(semanticPath: String): AthenaComponentKnowledgeSessionPayload {
    return when (this) {
        is AthenaComponentKnowledgeReady -> AthenaComponentKnowledgeSessionPayload(
            projectName = projectName,
            systemSemanticId = systemSemanticId,
            semanticPath = semanticPath,
            status = "ready",
            contributingPluginIds = contributingPluginIds,
            activeConceptCount = activeConceptCount,
            activeImplementationCount = activeImplementationCount,
            availableComponents = availableComponents.map(AthenaAvailableAuthoringComponent::toPayload),
            components = components.map(AthenaResolvedComponentKnowledgeEntry::toPayload),
            semanticPorts = semanticPorts.map { resolvedPort ->
                AthenaResolvedSemanticPortPayload(
                    portSemanticId = resolvedPort.portSemanticId.value,
                    ownerSemanticId = resolvedPort.ownerSemanticId.value,
                    portTypeId = resolvedPort.definition.portTypeId.value,
                    roleId = resolvedPort.definition.roleId.value,
                    direction = resolvedPort.definition.direction.name.lowercase(),
                    signalFamilyId = resolvedPort.definition.signalFamilyId.value,
                    protocolIds = resolvedPort.definition.protocolIds.map { protocolId -> protocolId.value }.sorted(),
                )
            },
            physicalTraits = physicalTraits.map { physicalTrait ->
                AthenaResolvedPhysicalTraitPayload(
                    semanticSubjectId = physicalTrait.semanticSubjectId.value,
                    displayName = physicalTrait.definition.displayName,
                    widthMillimeters = physicalTrait.definition.size.widthMillimeters,
                    heightMillimeters = physicalTrait.definition.size.heightMillimeters,
                    depthMillimeters = physicalTrait.definition.size.depthMillimeters,
                    mountingTypeId = physicalTrait.definition.mountingTypeId.value,
                    installationMarkerIds = physicalTrait.definition.installationMarkerIds.map { marker -> marker.value }.sorted(),
                )
            },
            diagnostics = diagnostics.map(AthenaComponentKnowledgeDiagnostic::toPayload),
        )

        is AthenaComponentKnowledgeUnavailable -> AthenaComponentKnowledgeSessionPayload(
            projectName = projectName,
            systemSemanticId = "",
            semanticPath = semanticPath,
            status = "unavailable",
            contributingPluginIds = emptyList(),
            activeConceptCount = 0,
            activeImplementationCount = 0,
            availableComponents = emptyList(),
            components = emptyList(),
            semanticPorts = emptyList(),
            physicalTraits = emptyList(),
            diagnostics = emptyList(),
            unavailableReason = reason,
        )
    }
}

private fun AthenaAvailableAuthoringComponent.toPayload(): AthenaAvailableComponentPayload {
    return AthenaAvailableComponentPayload(
        conceptId = concept.conceptId.value,
        authoringTemplateIds = electricalEngineeringConceptTemplates()
            .filter { template -> template.conceptId == concept.conceptId }
            .map { template -> template.templateId.value }
            .distinct()
            .sorted(),
        displayName = concept.displayName,
        classificationKeys = concept.classificationKeys.toList().sorted(),
        summary = concept.summary,
        implementations = implementations.map { implementation ->
            AthenaAvailableComponentImplementationPayload(
                implementationId = implementation.implementationId.value,
                vendorId = implementation.vendorId.value,
                vendorPartNumber = implementation.vendorPartNumber.value,
                displayName = implementation.displayName,
                summary = implementation.summary,
            )
        },
    )
}

private fun AthenaResolvedComponentKnowledgeEntry.toPayload(): AthenaResolvedComponentKnowledgePayload {
    return AthenaResolvedComponentKnowledgePayload(
        semanticSubjectId = resolvedComponent.semanticSubjectId.value,
        authoredComponentReference = resolvedComponent.authoredComponentReference,
        conceptId = resolvedComponent.concept.conceptId.value,
        conceptDisplayName = resolvedComponent.concept.displayName,
        implementationId = resolvedImplementation?.implementation?.implementationId?.value,
        vendorId = resolvedImplementation?.implementation?.vendorId?.value,
        vendorPartNumber = resolvedImplementation?.implementation?.vendorPartNumber?.value,
    )
}

private fun AthenaComponentKnowledgeDiagnostic.toPayload(): AthenaComponentKnowledgeDiagnosticPayload {
    return AthenaComponentKnowledgeDiagnosticPayload(
        severity = when (severity) {
            AthenaComponentKnowledgeSeverity.ERROR -> "error"
        },
        ruleId = ruleId.value,
        subject = subject,
        message = message,
    )
}
