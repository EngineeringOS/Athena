package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.component.EngineeringConceptPortDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ElectricalEngineeringConceptTemplatesTest {
    @Test
    fun `electrical extension owns rolling shutter motor concept template`() {
        val template = electricalEngineeringConceptTemplates()
            .single { candidate -> candidate.templateId.value == "electrical.motor.ac.default" }

        assertEquals("electrical.motor.ac", template.conceptId.value)
        assertEquals("Motor", template.semanticType.value)
        assertEquals("MOTOR-AC", template.defaultModel)
        assertEquals(
            listOf(
                Triple("up", EngineeringConceptPortDirection.IN, "Digital"),
                Triple("down", EngineeringConceptPortDirection.IN, "Digital"),
                Triple("status", EngineeringConceptPortDirection.OUT, "Digital"),
            ),
            template.nestedPorts.map { port -> Triple(port.name, port.direction, port.signalOrMedium.value) },
        )
        assertTrue(template.relationshipCapabilities.any { capability ->
            capability.relationshipType == "ElectricalConnectionRelationship"
        })
        assertEquals("electrical", template.provenance.domainId)
    }
}
