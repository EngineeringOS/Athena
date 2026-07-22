package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.interaction.AuthoringCapabilityRequirementKind
import com.engineeringood.athena.interaction.AuthoringIntentKind
import com.engineeringood.athena.interaction.InteractionOriginSurface
import com.engineeringood.athena.interaction.InteractionProvenance
import com.engineeringood.athena.interaction.InteractionSubjectKey
import com.engineeringood.athena.interaction.InteractionSubjectKind
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringSystem
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ElectricalAuthoringCapabilityRegistryFactoryTest {
    @Test
    fun `domain registry discovers governed entity and relationship capabilities from semantic facts`() {
        val document = engineeringDocument()
        val sourceUri = "file:///workspace/main.athena"
        val registry = ElectricalAuthoringCapabilityRegistryFactory().build(
            document = document,
            sourceContextId = sourceUri,
            sourceRevision = "revision:m31",
            activeProjectionViewIds = setOf("cabinet"),
        )

        val entityEvidence = registry.discoverAuthoringCapabilities(
            subjectKey = InteractionSubjectKey(
                canonicalSubjectId = document.system.id,
                subjectKind = InteractionSubjectKind.WORKSPACE,
                sourceContextId = sourceUri,
            ),
            requestedBy = InteractionProvenance(originSurface = InteractionOriginSurface.PALETTE),
            intentKind = AuthoringIntentKind.CREATE_ENTITY,
        ).evidence.single()
        assertEquals("create-semantic-entity", entityEvidence.capabilityId)
        assertEquals(
            AuthoringCapabilityRequirementKind.entries.toSet(),
            entityEvidence.satisfiedRequirements.map { requirement -> requirement.kind }.toSet(),
        )
        assertTrue(entityEvidence.satisfiedRequirements.all { requirement -> requirement.satisfied })

        val relationshipEvidence = registry.discoverAuthoringCapabilities(
            subjectKey = InteractionSubjectKey(
                canonicalSubjectId = StableSemanticIdentity("port:PS1.out"),
                subjectKind = InteractionSubjectKind.PORT,
                sourceContextId = sourceUri,
            ),
            requestedBy = InteractionProvenance(originSurface = InteractionOriginSurface.GRAPH),
            intentKind = AuthoringIntentKind.CREATE_RELATIONSHIP,
        ).evidence.single()
        assertEquals("create-semantic-relationship", relationshipEvidence.capabilityId)
        assertTrue(relationshipEvidence.satisfiedRequirements.isNotEmpty())
        assertTrue(relationshipEvidence.satisfiedRequirements.all { requirement -> requirement.satisfied })
    }

    private fun engineeringDocument(): EngineeringDocument {
        val provenance = SourceProvenance("main.athena", 1, 1, 1, 1)
        val system = EngineeringSystem(
            id = StableSemanticIdentity("system:FactoryLine"),
            name = "FactoryLine",
            provenance = provenance,
        )
        return EngineeringDocument(
            system = system,
            components = emptyList(),
            ports = listOf(
                EngineeringPort(
                    id = StableSemanticIdentity("port:PS1.out"),
                    ownerReference = EngineeringReference(
                        authoredPath = listOf("PS1"),
                        resolvedIdentity = StableSemanticIdentity("component:PS1"),
                        provenance = provenance,
                    ),
                    name = "out",
                    properties = emptyList(),
                    provenance = provenance,
                ),
            ),
            connections = emptyList(),
        )
    }
}
