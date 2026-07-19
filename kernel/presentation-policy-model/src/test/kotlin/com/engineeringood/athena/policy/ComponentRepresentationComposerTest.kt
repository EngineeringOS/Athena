package com.engineeringood.athena.policy

import com.engineeringood.athena.representation.RepresentationContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComponentRepresentationComposerTest {
    @Test
    fun `mandatory M25 families compose into supported presentation anatomy facts`() {
        val composer = ComponentRepresentationComposer(AthenaIndustrialControlV0Profile.profile())
        val components = listOf(
            ComponentRepresentationRequest(ComponentSubjectKey("PLC1"), ComponentFamilyKey("plc-controller")),
            ComponentRepresentationRequest(ComponentSubjectKey("XT1"), ComponentFamilyKey("terminal-block")),
            ComponentRepresentationRequest(ComponentSubjectKey("PS1"), ComponentFamilyKey("power-supply")),
            ComponentRepresentationRequest(ComponentSubjectKey("M1"), ComponentFamilyKey("load-actuator")),
        )

        val snapshot = composer.compose(components)

        assertTrue(snapshot.hasZeroFallbackSymbols())
        assertEquals(4, snapshot.facts.size)
        assertTrue(snapshot.facts.all { it.anatomy.context == RepresentationContext.ELECTRICAL_SCHEMATIC })
        assertTrue(snapshot.facts.all { it.terminals.isNotEmpty() })
        assertTrue(snapshot.facts.all { it.labels.isNotEmpty() })
    }

    @Test
    fun `optional M25 families compose when present`() {
        val composer = ComponentRepresentationComposer(AthenaIndustrialControlV0Profile.profile())
        val snapshot = composer.compose(
            listOf(
                ComponentRepresentationRequest(ComponentSubjectKey("HMI1"), ComponentFamilyKey("hmi-operator")),
                ComponentRepresentationRequest(ComponentSubjectKey("QF1"), ComponentFamilyKey("protection-device")),
            ),
        )

        assertTrue(snapshot.hasZeroFallbackSymbols())
        assertEquals(listOf("HMI1", "QF1"), snapshot.facts.map { it.subject.value })
    }
}
