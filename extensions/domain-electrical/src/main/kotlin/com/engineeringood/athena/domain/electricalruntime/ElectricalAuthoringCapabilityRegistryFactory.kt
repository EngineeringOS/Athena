package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.interaction.AuthoringCapability
import com.engineeringood.athena.interaction.AuthoringCapabilityRequirement
import com.engineeringood.athena.interaction.AuthoringCapabilityRequirementKind
import com.engineeringood.athena.interaction.AuthoringIntentKind
import com.engineeringood.athena.interaction.InteractionActionFamily
import com.engineeringood.athena.interaction.InteractionOriginSurface
import com.engineeringood.athena.interaction.InteractionRegistryInput
import com.engineeringood.athena.interaction.InteractionRegistrySubjectFact
import com.engineeringood.athena.interaction.InteractionSubjectKind
import com.engineeringood.athena.interaction.SemanticCapability
import com.engineeringood.athena.interaction.SemanticCapabilityRegistry
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.representation.NativeRepresentationLibraryLoader

/** Builds electrical authoring capabilities from canonical semantic and active projection facts. */
class ElectricalAuthoringCapabilityRegistryFactory {
    fun build(
        document: EngineeringDocument,
        sourceContextId: String,
        sourceRevision: String?,
        activeProjectionViewIds: Set<String>,
    ): SemanticCapabilityRegistry {
        val templates = electricalEngineeringConceptTemplates()
        val representationIds = NativeRepresentationLibraryLoader()
            .loadBundled()
            .libraryOrNull
            ?.definitions
            .orEmpty()
            .mapTo(mutableSetOf()) { definition -> definition.symbolId.value }
        val projectionAvailable = activeProjectionViewIds.isNotEmpty()
        val entityCapability = SemanticCapability(
            capabilityId = CREATE_ENTITY_CAPABILITY_ID,
            actionFamily = InteractionActionFamily.MUTATE,
            enabled = true,
            authoring = AuthoringCapability(
                intentKind = AuthoringIntentKind.CREATE_ENTITY,
                allowedOrigins = AUTHORING_ORIGINS,
                requirements = listOf(
                    satisfiedRequirement(AuthoringCapabilityRequirementKind.DOMAIN, "electrical", true),
                    satisfiedRequirement(
                        AuthoringCapabilityRequirementKind.CONCEPT_TEMPLATE,
                        MOTOR_TEMPLATE_ID,
                        templates.any { template -> template.templateId.value == MOTOR_TEMPLATE_ID },
                    ),
                    satisfiedRequirement(
                        AuthoringCapabilityRequirementKind.PROJECTION,
                        activeProjectionViewIds.sorted().joinToString(",").ifBlank { "active-projection" },
                        projectionAvailable,
                    ),
                    satisfiedRequirement(
                        AuthoringCapabilityRequirementKind.REPRESENTATION,
                        MOTOR_REPRESENTATION_ID,
                        MOTOR_REPRESENTATION_ID in representationIds,
                    ),
                ),
            ),
        )
        val relationshipCapability = SemanticCapability(
            capabilityId = CREATE_RELATIONSHIP_CAPABILITY_ID,
            actionFamily = InteractionActionFamily.MUTATE,
            enabled = true,
            authoring = AuthoringCapability(
                intentKind = AuthoringIntentKind.CREATE_RELATIONSHIP,
                allowedOrigins = AUTHORING_ORIGINS,
                requirements = listOf(
                    satisfiedRequirement(AuthoringCapabilityRequirementKind.DOMAIN, "electrical", true),
                    satisfiedRequirement(
                        AuthoringCapabilityRequirementKind.PROJECTION,
                        activeProjectionViewIds.sorted().joinToString(",").ifBlank { "active-projection" },
                        projectionAvailable,
                    ),
                ),
            ),
        )
        return SemanticCapabilityRegistry.build(
            InteractionRegistryInput(
                sourceContextId = sourceContextId,
                sourceRevision = sourceRevision,
                subjects = buildList {
                    add(
                        InteractionRegistrySubjectFact(
                            canonicalSubjectId = document.system.id,
                            subjectKind = InteractionSubjectKind.WORKSPACE,
                            capabilities = listOf(entityCapability),
                        ),
                    )
                    document.ports.forEach { port ->
                        add(
                            InteractionRegistrySubjectFact(
                                canonicalSubjectId = port.id,
                                subjectKind = InteractionSubjectKind.PORT,
                                capabilities = listOf(relationshipCapability),
                            ),
                        )
                    }
                },
            ),
        )
    }
}

private fun satisfiedRequirement(
    kind: AuthoringCapabilityRequirementKind,
    identifier: String,
    satisfied: Boolean,
): AuthoringCapabilityRequirement = AuthoringCapabilityRequirement(
    kind = kind,
    identifier = identifier,
    satisfied = satisfied,
    reason = if (satisfied) null else "Required electrical authoring fact `$identifier` is unavailable.",
)

private const val CREATE_ENTITY_CAPABILITY_ID = "create-semantic-entity"
private const val CREATE_RELATIONSHIP_CAPABILITY_ID = "create-semantic-relationship"
private const val MOTOR_TEMPLATE_ID = "electrical.motor.ac.default"
private const val MOTOR_REPRESENTATION_ID = "iec.motor.compact"

private val AUTHORING_ORIGINS = setOf(
    InteractionOriginSurface.GRAPH,
    InteractionOriginSurface.SOURCE,
    InteractionOriginSurface.INSPECTOR,
    InteractionOriginSurface.PALETTE,
    InteractionOriginSurface.COMMAND_PALETTE,
    InteractionOriginSurface.AI,
    InteractionOriginSurface.API,
)
