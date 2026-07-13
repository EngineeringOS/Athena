package com.engineeringood.athena.component

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ComponentModelContractTest {
    @Test
    fun `resolved component definition keeps canonical subject identity separate from resolved concept identity`() {
        val concept = EngineeringConceptDefinition(
            conceptId = EngineeringConceptId("electrical.plc.cpu"),
            displayName = "PLC CPU",
            classificationKeys = setOf("electrical", "control", "cpu"),
            summary = "Programmable controller central processing unit.",
        )
        val resolved = ResolvedComponentDefinition(
            semanticSubjectId = StableSemanticIdentity("component:PLC1"),
            authoredComponentReference = "Siemens.S7300.CPU313C",
            concept = concept,
        )

        assertEquals("component:PLC1", resolved.semanticSubjectId.value)
        assertEquals("Siemens.S7300.CPU313C", resolved.authoredComponentReference)
        assertEquals("electrical.plc.cpu", resolved.concept.conceptId.value)
        assertEquals("PLC CPU", resolved.concept.displayName)
        assertNotEquals(resolved.semanticSubjectId.value, resolved.concept.conceptId.value)
    }

    @Test
    fun `engineering concept definition stays vendor neutral and reusable across future vendor packs`() {
        val concept = EngineeringConceptDefinition(
            conceptId = EngineeringConceptId("electrical.motor.starter"),
            displayName = "Motor starter",
            classificationKeys = setOf("electrical", "power-control", "starter"),
            summary = "Vendor-neutral concept for one motor-starting assembly.",
        )
        val futureVendorMappings = mapOf(
            "Siemens.3RT2015" to concept.conceptId,
            "ABB.AF16" to concept.conceptId,
        )

        assertTrue(concept.classificationKeys.none { key -> key.contains("siemens", ignoreCase = true) })
        assertTrue(concept.classificationKeys.none { key -> key.contains("abb", ignoreCase = true) })
        assertEquals(1, futureVendorMappings.values.toSet().size)
        assertEquals("electrical.motor.starter", futureVendorMappings.getValue("Siemens.3RT2015").value)
        assertEquals("electrical.motor.starter", futureVendorMappings.getValue("ABB.AF16").value)
    }
}
