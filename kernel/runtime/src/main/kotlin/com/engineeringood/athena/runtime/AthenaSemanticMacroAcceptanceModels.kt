package com.engineeringood.athena.runtime

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.reuse.ExpansionMembership
import com.engineeringood.athena.reuse.ExpansionOrigin
import com.engineeringood.athena.reuse.SemanticMacroAcceptedExpansion
import com.engineeringood.athena.reuse.SemanticMacroExpansionId
import com.engineeringood.athena.reuse.SemanticMacroInstantiationId
import com.engineeringood.athena.reuse.SemanticMacroPackageBinding
import com.engineeringood.athena.reuse.SemanticMacroParameterName
import com.engineeringood.athena.reuse.SemanticMacroParameterValue
import com.engineeringood.athena.reuse.SemanticMacroPreview
import com.engineeringood.athena.reuse.SemanticMacroPreviewId
import com.engineeringood.athena.reuse.SemanticMacroPreviewPresentationConsequence

/**
 * Runtime-owned preview record retained so approved reuse can hand one deterministic bundle to M8
 * without asking frontend code to resubmit hidden expansion details.
 */
data class AthenaSemanticMacroPreviewRecord(
    val preview: SemanticMacroPreview,
    val packageBinding: SemanticMacroPackageBinding,
    val normalizedValues: Map<SemanticMacroParameterName, SemanticMacroParameterValue>,
)

/**
 * Internal reusable-preview state retained for one active project.
 */
internal data class AthenaSemanticMacroPreviewSessionState(
    val records: List<AthenaSemanticMacroPreviewRecord> = emptyList(),
)

internal fun AthenaSemanticMacroPreviewSessionState.recordPreview(
    record: AthenaSemanticMacroPreviewRecord,
): AthenaSemanticMacroPreviewSessionState {
    return copy(
        records = (records.filterNot { existing -> existing.preview.previewId == record.preview.previewId } + record)
            .sortedBy { existing -> existing.preview.previewId.value },
    )
}

/**
 * Stable runtime-owned mutation bundle prepared after preview approval and destined for later M8 execution.
 */
data class AthenaSemanticMacroMutationBundle(
    val bundleId: String,
    val previewId: SemanticMacroPreviewId,
    val acceptedExpansion: SemanticMacroAcceptedExpansion,
    val operations: List<AthenaSemanticMacroMutationOperation>,
    val affectedSemanticIds: List<StableSemanticIdentity>,
    val presentationConsequences: List<SemanticMacroPreviewPresentationConsequence> = emptyList(),
)

/**
 * Stable runtime-owned mutation operation kinds emitted for one approved Semantic Macro preview.
 */
enum class AthenaSemanticMacroMutationOperationKind {
    CREATE_COMPONENT,
    CREATE_PORT,
    CREATE_CONNECTION,
    REGISTER_EXPANSION_TRACEABILITY,
}

/**
 * One low-level mutation operation within the atomic Semantic Macro handoff bundle.
 */
sealed interface AthenaSemanticMacroMutationOperation {
    val operationId: String
    val kind: AthenaSemanticMacroMutationOperationKind
    val subjectId: StableSemanticIdentity?
    val summary: String?
}

data class AthenaSemanticMacroCreateComponentOperation(
    override val operationId: String,
    override val subjectId: StableSemanticIdentity,
    val templateId: String,
    val conceptId: String,
    val implementationId: String? = null,
    val properties: Map<String, SemanticMacroParameterValue> = emptyMap(),
    val tags: Set<String> = emptySet(),
    override val summary: String? = null,
) : AthenaSemanticMacroMutationOperation {
    override val kind: AthenaSemanticMacroMutationOperationKind = AthenaSemanticMacroMutationOperationKind.CREATE_COMPONENT
}

data class AthenaSemanticMacroCreatePortOperation(
    override val operationId: String,
    override val subjectId: StableSemanticIdentity,
    val componentSubjectId: StableSemanticIdentity,
    val componentTemplateId: String,
    val portRoleId: String,
    override val summary: String? = null,
) : AthenaSemanticMacroMutationOperation {
    override val kind: AthenaSemanticMacroMutationOperationKind = AthenaSemanticMacroMutationOperationKind.CREATE_PORT
}

data class AthenaSemanticMacroCreateConnectionOperation(
    override val operationId: String,
    override val subjectId: StableSemanticIdentity,
    val templateId: String,
    val fromPortSubjectId: StableSemanticIdentity,
    val toPortSubjectId: StableSemanticIdentity,
    override val summary: String? = null,
) : AthenaSemanticMacroMutationOperation {
    override val kind: AthenaSemanticMacroMutationOperationKind = AthenaSemanticMacroMutationOperationKind.CREATE_CONNECTION
}

data class AthenaSemanticMacroRegisterExpansionTraceabilityOperation(
    override val operationId: String,
    val expansionId: SemanticMacroExpansionId,
    val membershipCount: Int,
    override val summary: String? = null,
) : AthenaSemanticMacroMutationOperation {
    override val kind: AthenaSemanticMacroMutationOperationKind =
        AthenaSemanticMacroMutationOperationKind.REGISTER_EXPANSION_TRACEABILITY
    override val subjectId: StableSemanticIdentity? = null
}

internal fun AthenaSemanticMacroPreviewRecord.toMutationBundle(): AthenaSemanticMacroMutationBundle {
    val preview = preview
    val memberships = buildMemberships(preview.instantiationId)
    val acceptedExpansion = SemanticMacroAcceptedExpansion(
        expansionId = buildExpansionId(preview.previewId),
        previewId = preview.previewId,
        origin = ExpansionOrigin(
            macroId = preview.macroId,
            instantiationId = preview.instantiationId,
            packageBinding = packageBinding,
            parameterValues = normalizedValues.toSortedMap(compareBy(SemanticMacroParameterName::value)),
        ),
        memberships = memberships,
    )
    val componentOperations = preview.components.map { component ->
        val subjectId = derivedComponentSubjectId(preview.instantiationId, component.templateId)
        AthenaSemanticMacroCreateComponentOperation(
            operationId = "operation:${preview.previewId.value}:component:${component.templateId}",
            subjectId = subjectId,
            templateId = component.templateId,
            conceptId = component.conceptId,
            implementationId = component.implementationId,
            properties = component.properties.toSortedMap(),
            tags = component.tags.toSortedSet(),
            summary = component.summary ?: component.title,
        )
    }
    val portOperations = preview.ports.map { port ->
        val subjectId = derivedPortSubjectId(
            instantiationId = preview.instantiationId,
            componentTemplateId = port.componentTemplateId,
            portRoleId = port.portRoleId,
        )
        AthenaSemanticMacroCreatePortOperation(
            operationId = "operation:${preview.previewId.value}:port:${port.componentTemplateId}:${port.portRoleId}",
            subjectId = subjectId,
            componentSubjectId = derivedComponentSubjectId(preview.instantiationId, port.componentTemplateId),
            componentTemplateId = port.componentTemplateId,
            portRoleId = port.portRoleId,
            summary = port.title,
        )
    }
    val connectionOperations = preview.connections.map { connection ->
        val subjectId = derivedConnectionSubjectId(preview.instantiationId, connection.templateId)
        AthenaSemanticMacroCreateConnectionOperation(
            operationId = "operation:${preview.previewId.value}:connection:${connection.templateId}",
            subjectId = subjectId,
            templateId = connection.templateId,
            fromPortSubjectId = derivedPortSubjectId(
                instantiationId = preview.instantiationId,
                componentTemplateId = connection.fromComponentTemplateId,
                portRoleId = connection.fromPortRoleId,
            ),
            toPortSubjectId = derivedPortSubjectId(
                instantiationId = preview.instantiationId,
                componentTemplateId = connection.toComponentTemplateId,
                portRoleId = connection.toPortRoleId,
            ),
            summary = connection.summary ?: connection.title,
        )
    }
    val traceabilityOperation = AthenaSemanticMacroRegisterExpansionTraceabilityOperation(
        operationId = "operation:${preview.previewId.value}:traceability",
        expansionId = acceptedExpansion.expansionId,
        membershipCount = memberships.size,
        summary = "Register accepted expansion origin and membership facts for `${preview.instantiationId.value}`.",
    )
    val operations = (componentOperations + portOperations + connectionOperations + traceabilityOperation)
        .sortedBy(AthenaSemanticMacroMutationOperation::operationId)
    val affectedSemanticIds = memberships.map(ExpansionMembership::subjectId)
        .distinctBy(StableSemanticIdentity::value)
        .sortedBy(StableSemanticIdentity::value)

    return AthenaSemanticMacroMutationBundle(
        bundleId = buildMutationBundleId(preview.previewId),
        previewId = preview.previewId,
        acceptedExpansion = acceptedExpansion,
        operations = operations,
        affectedSemanticIds = affectedSemanticIds,
        presentationConsequences = preview.presentationConsequences,
    )
}

internal fun derivedComponentSubjectId(
    instantiationId: SemanticMacroInstantiationId,
    templateId: String,
): StableSemanticIdentity = StableSemanticIdentity("component:${instantiationId.value}:$templateId")

internal fun derivedPortSubjectId(
    instantiationId: SemanticMacroInstantiationId,
    componentTemplateId: String,
    portRoleId: String,
): StableSemanticIdentity = StableSemanticIdentity("port:${instantiationId.value}:$componentTemplateId:$portRoleId")

internal fun derivedConnectionSubjectId(
    instantiationId: SemanticMacroInstantiationId,
    templateId: String,
): StableSemanticIdentity = StableSemanticIdentity("connection:${instantiationId.value}:$templateId")

private fun buildExpansionId(previewId: SemanticMacroPreviewId): SemanticMacroExpansionId {
    return SemanticMacroExpansionId("expansion:${previewId.value.substringAfter("preview:")}")
}

private fun buildMutationBundleId(previewId: SemanticMacroPreviewId): String {
    return "bundle:${previewId.value.substringAfter("preview:")}"
}

private fun AthenaSemanticMacroPreviewRecord.buildMemberships(
    instantiationId: SemanticMacroInstantiationId,
): List<ExpansionMembership> {
    return buildList {
        preview.components.forEach { component ->
            add(
                ExpansionMembership(
                    instantiationId = instantiationId,
                    subjectId = derivedComponentSubjectId(instantiationId, component.templateId),
                    role = "component:${component.templateId}",
                ),
            )
        }
        preview.ports.forEach { port ->
            add(
                ExpansionMembership(
                    instantiationId = instantiationId,
                    subjectId = derivedPortSubjectId(instantiationId, port.componentTemplateId, port.portRoleId),
                    role = "port:${port.componentTemplateId}:${port.portRoleId}",
                ),
            )
        }
        preview.connections.forEach { connection ->
            add(
                ExpansionMembership(
                    instantiationId = instantiationId,
                    subjectId = derivedConnectionSubjectId(instantiationId, connection.templateId),
                    role = "connection:${connection.templateId}",
                ),
            )
        }
    }.sortedWith(
        compareBy<ExpansionMembership>(
            { membership -> membership.role.orEmpty() },
            { membership -> membership.subjectId.value },
        ),
    )
}
