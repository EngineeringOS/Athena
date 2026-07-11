package com.engineeringood.athena.ir

import kotlin.test.Test
import kotlin.test.assertEquals

class EngineeringCapabilityFactContractTest {
    @Test
    fun `publishes the first narrow electrical capability fact vocabulary`() {
        assertEquals(
            listOf(
                EngineeringCapabilityFactKind.REQUIRED_PROTECTION_CURRENT,
                EngineeringCapabilityFactKind.REQUIRED_CABLE_CURRENT,
                EngineeringCapabilityFactKind.REQUIRED_RELAY_SIZING_CURRENT,
            ),
            EngineeringCapabilityFactKind.entries,
        )
    }

    @Test
    fun `canonical capability facts sort subjects and fact judgements deterministically`() {
        val facts = EngineeringCapabilityFacts.canonical(
            listOf(
                EngineeringCapabilitySubjectFacts(
                    subjectIdentity = StableSemanticIdentity("component:M2"),
                    facts = emptyList(),
                ),
                EngineeringCapabilitySubjectFacts(
                    subjectIdentity = StableSemanticIdentity("component:M1"),
                    facts = listOf(
                        EngineeringCapabilityFact(
                            kind = EngineeringCapabilityFactKind.REQUIRED_RELAY_SIZING_CURRENT,
                            subjectIdentity = StableSemanticIdentity("component:M1"),
                            comparison = EngineeringCapabilityComparison.MINIMUM_INCLUSIVE,
                            quantity = DerivedEngineeringQuantity.Decimal("14", "A"),
                            trace = EngineeringCapabilityFactTrace(
                                knowledgeArtifactId = "com.engineeringood.athena.knowledge.pack.electrical-basic",
                                knowledgeArtifactVersion = "0.1.0",
                                knowledgeEntryId = "capability",
                                sourceDerivedValues = listOf(
                                    EngineeringCapabilityDerivedValueReference(
                                        subjectIdentity = StableSemanticIdentity("component:M1"),
                                        valueKind = DerivedEngineeringValueKind.FULL_LOAD_CURRENT,
                                    ),
                                ),
                            ),
                        ),
                        EngineeringCapabilityFact(
                            kind = EngineeringCapabilityFactKind.REQUIRED_PROTECTION_CURRENT,
                            subjectIdentity = StableSemanticIdentity("component:M1"),
                            comparison = EngineeringCapabilityComparison.MINIMUM_INCLUSIVE,
                            quantity = DerivedEngineeringQuantity.Decimal("18", "A"),
                            trace = EngineeringCapabilityFactTrace(
                                knowledgeArtifactId = "com.engineeringood.athena.knowledge.pack.electrical-basic",
                                knowledgeArtifactVersion = "0.1.0",
                                knowledgeEntryId = "capability",
                                sourceDerivedValues = listOf(
                                    EngineeringCapabilityDerivedValueReference(
                                        subjectIdentity = StableSemanticIdentity("component:M1"),
                                        valueKind = DerivedEngineeringValueKind.FULL_LOAD_CURRENT,
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        assertEquals(
            listOf("component:M1", "component:M2"),
            facts.subjects.map { subject -> subject.subjectIdentity.value },
        )
        assertEquals(
            listOf(
                EngineeringCapabilityFactKind.REQUIRED_PROTECTION_CURRENT,
                EngineeringCapabilityFactKind.REQUIRED_RELAY_SIZING_CURRENT,
            ),
            facts.subjects.first().facts.map { fact -> fact.kind },
        )
    }
}
