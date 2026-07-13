package com.engineeringood.athena.domain.electricalruntime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ElectricalRuntimeComponentKnowledgeTest {
    @Test
    fun `publishes the first narrow electrical engineering concept slice`() {
        val concepts = electricalEngineeringConcepts()

        assertEquals(
            listOf(
                "electrical.plc.cpu",
                "electrical.contactor.power",
                "electrical.relay.overload",
                "electrical.motor.ac",
                "electrical.power-supply.dc24",
            ),
            concepts.map { concept -> concept.conceptId.value },
        )
        assertEquals(
            listOf(
                "PLC CPU",
                "Power contactor",
                "Overload relay",
                "AC motor",
                "24V DC power supply",
            ),
            concepts.map { concept -> concept.displayName },
        )
        assertTrue(concepts.all { concept -> concept.classificationKeys.contains("electrical") })
    }

    @Test
    fun `publishes one siemens first implementation for each targeted proof family`() {
        val concepts = electricalEngineeringConcepts()
        val implementations = siemensElectricalPartImplementations()

        assertEquals(5, implementations.size)
        assertEquals(setOf("siemens"), implementations.map { implementation -> implementation.vendorId.value }.toSet())
        assertEquals(
            concepts.map { concept -> concept.conceptId }.toSet(),
            implementations.map { implementation -> implementation.conceptId }.toSet(),
        )
        assertTrue(
            implementations.none { implementation ->
                implementation.vendorPartNumber.value == implementation.conceptId.value
            },
        )
    }
}
