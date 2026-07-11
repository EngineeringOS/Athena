package com.engineeringood.athena.ir

import kotlin.test.Test
import kotlin.test.assertEquals

class EngineeringKnowledgeStateContractTest {
    @Test
    fun `engineering knowledge state defaults to empty canonical snapshots`() {
        val state = EngineeringKnowledgeState()

        assertEquals(
            DerivedEngineeringContext.canonical(emptyList()),
            state.derivedContext,
        )
        assertEquals(
            EngineeringCapabilityFacts.canonical(emptyList()),
            state.capabilityFacts,
        )
        assertEquals(
            EngineeringConstraintEvaluations.canonical(emptyList()),
            state.constraintEvaluations,
        )
    }
}
