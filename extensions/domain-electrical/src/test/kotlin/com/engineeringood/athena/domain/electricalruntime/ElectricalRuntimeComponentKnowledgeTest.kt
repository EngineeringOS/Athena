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
    fun `publishes Siemens first implementations for each targeted proof family and preserves governed alternatives`() {
        val concepts = electricalEngineeringConcepts()
        val implementations = siemensElectricalPartImplementations()

        assertEquals(6, implementations.size)
        assertEquals(setOf("siemens"), implementations.map { implementation -> implementation.vendorId.value }.toSet())
        assertTrue(
            concepts.all { concept ->
                implementations.any { implementation -> implementation.conceptId == concept.conceptId }
            },
        )
        assertEquals(
            2,
            implementations.count { implementation -> implementation.conceptId.value == "electrical.plc.cpu" },
        )
        assertTrue(
            implementations.none { implementation ->
                implementation.vendorPartNumber.value == implementation.conceptId.value
            },
        )
    }
}
