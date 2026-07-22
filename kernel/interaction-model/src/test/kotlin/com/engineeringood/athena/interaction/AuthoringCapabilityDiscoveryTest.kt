package com.engineeringood.athena.interaction

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthoringCapabilityDiscoveryTest {
    @Test
    fun `registry exposes typed authoring capability evidence`() {
        val subjectKey = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("system:RollingShutter"),
            subjectKind = InteractionSubjectKind.WORKSPACE,
            sourceContextId = "file:///workspace/main.athena",
        )
        val registry = SemanticCapabilityRegistry.build(
            InteractionRegistryInput(
                sourceContextId = requireNotNull(subjectKey.sourceContextId),
                subjects = listOf(
                    InteractionRegistrySubjectFact(
                        canonicalSubjectId = subjectKey.canonicalSubjectId,
                        subjectKind = subjectKey.subjectKind,
                        capabilities = listOf(createEntityCapability()),
                    ),
                ),
                occurrences = listOf(
                    InteractionRegistryOccurrenceFact(
                        canonicalSubjectId = subjectKey.canonicalSubjectId,
                        subjectKind = subjectKey.subjectKind,
                        adapterMetadata = mapOf("screenX" to "640", "widgetId" to "graph-1"),
                    ),
                ),
            ),
        )

        val result = registry.discoverAuthoringCapabilities(
            subjectKey = subjectKey,
            requestedBy = InteractionProvenance(originSurface = InteractionOriginSurface.GRAPH),
        )
        val evidence = result.evidence.single()

        assertEquals(emptyList(), result.diagnostics)
        assertEquals("create-semantic-entity", evidence.capabilityId)
        assertEquals(AuthoringIntentKind.CREATE_ENTITY, evidence.intentKind)
        assertEquals(subjectKey, evidence.subject)
        assertEquals(InteractionOriginSurface.GRAPH, evidence.actorOrigin)
        assertEquals(
            setOf(
                AuthoringCapabilityRequirementKind.DOMAIN,
                AuthoringCapabilityRequirementKind.CONCEPT_TEMPLATE,
                AuthoringCapabilityRequirementKind.PROJECTION,
                AuthoringCapabilityRequirementKind.REPRESENTATION,
            ),
            evidence.satisfiedRequirements.map { requirement -> requirement.kind }.toSet(),
        )
        assertFalse(evidence.toString().contains("screenX"))
        assertFalse(evidence.toString().contains("widgetId"))
    }

    @Test
    fun `registry rejects authoring capability with unsatisfied requirement`() {
        val subjectKey = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("system:RollingShutter"),
            subjectKind = InteractionSubjectKind.WORKSPACE,
            sourceContextId = "file:///workspace/main.athena",
        )
        val capability = createEntityCapability()
        val authoring = requireNotNull(capability.authoring)
        val unavailable = capability.copy(
            authoring = authoring.copy(
                requirements = authoring.requirements.map { requirement ->
                    if (requirement.kind == AuthoringCapabilityRequirementKind.REPRESENTATION) {
                        requirement.copy(satisfied = false, reason = "M31 motor symbol is unavailable.")
                    } else {
                        requirement
                    }
                },
            ),
        )
        val registry = registryFor(subjectKey, unavailable)

        val result = registry.discoverAuthoringCapabilities(
            subjectKey = subjectKey,
            requestedBy = InteractionProvenance(originSurface = InteractionOriginSurface.GRAPH),
        )

        assertTrue(result.evidence.isEmpty())
        assertEquals(InteractionDiagnosticCode.AUTHORING_CAPABILITY_UNAVAILABLE, result.diagnostics.single().code)
        assertTrue(result.diagnostics.single().message.contains("representation"))
    }

    @Test
    fun `registry enforces authoring actor policy`() {
        val subjectKey = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("system:RollingShutter"),
            subjectKind = InteractionSubjectKind.WORKSPACE,
            sourceContextId = "file:///workspace/main.athena",
        )
        val registry = registryFor(subjectKey, createEntityCapability())

        val result = registry.discoverAuthoringCapabilities(
            subjectKey = subjectKey,
            requestedBy = InteractionProvenance(originSurface = InteractionOriginSurface.API),
        )

        assertTrue(result.evidence.isEmpty())
        assertEquals(InteractionDiagnosticCode.AUTHORING_CAPABILITY_UNAVAILABLE, result.diagnostics.single().code)
        assertTrue(result.diagnostics.single().message.contains("actor policy"))
    }

    private fun registryFor(
        subjectKey: InteractionSubjectKey,
        capability: SemanticCapability,
    ): SemanticCapabilityRegistry = SemanticCapabilityRegistry.build(
        InteractionRegistryInput(
            sourceContextId = requireNotNull(subjectKey.sourceContextId),
            subjects = listOf(
                InteractionRegistrySubjectFact(
                    canonicalSubjectId = subjectKey.canonicalSubjectId,
                    subjectKind = subjectKey.subjectKind,
                    capabilities = listOf(capability),
                ),
            ),
        ),
    )

    private fun createEntityCapability(): SemanticCapability = SemanticCapability(
        capabilityId = "create-semantic-entity",
        actionFamily = InteractionActionFamily.MUTATE,
        enabled = true,
        authoring = AuthoringCapability(
            intentKind = AuthoringIntentKind.CREATE_ENTITY,
            allowedOrigins = setOf(InteractionOriginSurface.GRAPH),
            requirements = listOf(
                AuthoringCapabilityRequirement(AuthoringCapabilityRequirementKind.DOMAIN, "electrical", true),
                AuthoringCapabilityRequirement(AuthoringCapabilityRequirementKind.CONCEPT_TEMPLATE, "electrical.motor.ac", true),
                AuthoringCapabilityRequirement(AuthoringCapabilityRequirementKind.PROJECTION, "cabinet", true),
                AuthoringCapabilityRequirement(AuthoringCapabilityRequirementKind.REPRESENTATION, "iec.motor.compact", true),
            ),
        ),
    )
}
