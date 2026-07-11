package com.engineeringood.athena.ir

import kotlin.test.Test
import kotlin.test.assertEquals

class EngineeringImpactConsequenceContractTest {
    @Test
    fun `publishes the first impact reason vocabulary`() {
        assertEquals(
            listOf(
                EngineeringImpactReasonKind.GOVERNED_INPUT_CHANGED,
                EngineeringImpactReasonKind.DERIVED_CONTEXT_CHANGED,
                EngineeringImpactReasonKind.CAPABILITY_FACT_CHANGED,
                EngineeringImpactReasonKind.CONSTRAINT_EVALUATION_CHANGED,
            ),
            EngineeringImpactReasonKind.entries,
        )
    }

    @Test
    fun `canonical impact consequences sort affected subjects and nested categories deterministically`() {
        val consequences = EngineeringImpactConsequences.canonical(
            listOf(
                EngineeringImpactConsequence(
                    affectedSubjectIdentity = StableSemanticIdentity("component:M2"),
                    triggerSubjectIdentities = listOf(StableSemanticIdentity("component:M2")),
                    reasonKinds = listOf(EngineeringImpactReasonKind.CONSTRAINT_EVALUATION_CHANGED),
                    affectedConstraintRuleKinds = listOf(EngineeringConstraintRuleKind.CABLE_SUFFICIENCY),
                ),
                EngineeringImpactConsequence(
                    affectedSubjectIdentity = StableSemanticIdentity("component:M1"),
                    triggerSubjectIdentities = listOf(
                        StableSemanticIdentity("component:M1"),
                        StableSemanticIdentity("component:M1"),
                    ),
                    reasonKinds = listOf(
                        EngineeringImpactReasonKind.CAPABILITY_FACT_CHANGED,
                        EngineeringImpactReasonKind.GOVERNED_INPUT_CHANGED,
                        EngineeringImpactReasonKind.GOVERNED_INPUT_CHANGED,
                    ),
                    affectedInputKinds = listOf(
                        DerivedEngineeringInputKind.BREAKER_RATED_CURRENT,
                        DerivedEngineeringInputKind.MOTOR_POWER,
                        DerivedEngineeringInputKind.MOTOR_POWER,
                    ),
                    affectedDerivedValueKinds = listOf(
                        DerivedEngineeringValueKind.THERMAL_LOAD,
                        DerivedEngineeringValueKind.FULL_LOAD_CURRENT,
                    ),
                    affectedCapabilityFactKinds = listOf(
                        EngineeringCapabilityFactKind.REQUIRED_CABLE_CURRENT,
                        EngineeringCapabilityFactKind.REQUIRED_PROTECTION_CURRENT,
                    ),
                    affectedConstraintRuleKinds = listOf(
                        EngineeringConstraintRuleKind.RELAY_SUFFICIENCY,
                        EngineeringConstraintRuleKind.PROTECTION_SUFFICIENCY,
                    ),
                ),
            ),
        )

        assertEquals(
            listOf("component:M1", "component:M2"),
            consequences.consequences.map { consequence -> consequence.affectedSubjectIdentity.value },
        )
        assertEquals(
            listOf(EngineeringImpactReasonKind.GOVERNED_INPUT_CHANGED, EngineeringImpactReasonKind.CAPABILITY_FACT_CHANGED),
            consequences.consequences.first().reasonKinds,
        )
        assertEquals(
            listOf(DerivedEngineeringInputKind.MOTOR_POWER, DerivedEngineeringInputKind.BREAKER_RATED_CURRENT),
            consequences.consequences.first().affectedInputKinds,
        )
        assertEquals(
            listOf(DerivedEngineeringValueKind.FULL_LOAD_CURRENT, DerivedEngineeringValueKind.THERMAL_LOAD),
            consequences.consequences.first().affectedDerivedValueKinds,
        )
    }
}
