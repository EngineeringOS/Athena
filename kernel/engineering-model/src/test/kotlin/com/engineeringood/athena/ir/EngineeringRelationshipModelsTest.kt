package com.engineeringood.athena.ir

import kotlin.test.Test
import kotlin.test.assertEquals

class EngineeringRelationshipModelsTest {
    private val provenance = SourceProvenance("relationships.athena", 1, 1, 1, 20)

    @Test
    fun relationshipPreservesNamedRolesAndCrossEntityFunctionSubjects() {
        val relationship = EngineeringRelationship(
            id = StableSemanticIdentity("relationship:controls:PLC1:KM1"),
            definitionReference = EngineeringDefinitionReference(listOf("automation", "controls"), null, provenance),
            participants = listOf(
                EngineeringParticipant(
                    role = "controller",
                    subject = EngineeringSubjectReference.Function(
                        EngineeringFunctionReference(EngineeringReference(listOf("PLC1", "control"), StableSemanticIdentity("function:PLC1.control"), provenance)),
                    ),
                    provenance = provenance,
                ),
                EngineeringParticipant(
                    role = "controlled",
                    subject = EngineeringSubjectReference.Function(
                        EngineeringFunctionReference(EngineeringReference(listOf("KM1", "coil"), StableSemanticIdentity("function:KM1.coil"), provenance)),
                    ),
                    provenance = provenance,
                ),
            ),
            properties = emptyList(),
            provenance = provenance,
        )

        assertEquals("controller", relationship.participants[0].role)
        assertEquals("function:KM1.coil", relationship.participants[1].subject.reference.resolvedIdentity?.value)
    }

    @Test
    fun flowRemainsSeparateFactReferencingRelationshipRoles() {
        val flow = EngineeringFlow(
            id = StableSemanticIdentity("flow:switched-power:KM1:M1"),
            relationship = EngineeringReference(listOf("power"), StableSemanticIdentity("relationship:supplies:KM1:M1"), provenance),
            definitionReference = EngineeringDefinitionReference(listOf("electrical", "switched-power"), null, provenance),
            sourceRole = "provider",
            sinkRole = "consumer",
            medium = null,
            properties = emptyList(),
            provenance = provenance,
        )

        assertEquals("relationship:supplies:KM1:M1", flow.relationship.resolvedIdentity?.value)
        assertEquals("consumer", flow.sinkRole)
    }

    @Test
    fun unresolvedSubjectRemainsExplicitlyUnresolved() {
        val reference = EngineeringReference(listOf("Q1"), null, provenance)
        val subject = EngineeringSubjectReference.Unresolved(reference)

        assertEquals(listOf("Q1"), subject.reference.authoredPath)
        val relationship = EngineeringRelationship(
                id = StableSemanticIdentity("relationship:protects:Q1:M1"),
                definitionReference = EngineeringDefinitionReference(listOf("protects"), null, provenance),
                participants = listOf(
                    EngineeringParticipant("protector", subject, provenance),
                    EngineeringParticipant(
                        "protected",
                        EngineeringSubjectReference.Entity(
                            EngineeringEntityReference(EngineeringReference(listOf("M1"), StableSemanticIdentity("entity:M1"), provenance)),
                        ),
                        provenance,
                    ),
                ),
                properties = emptyList(),
                provenance = provenance,
            )
        assertEquals(true, relationship.participants.first().subject is EngineeringSubjectReference.Unresolved)
    }
}
