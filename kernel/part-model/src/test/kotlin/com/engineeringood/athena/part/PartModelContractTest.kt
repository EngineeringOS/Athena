package com.engineeringood.athena.part

import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PartModelContractTest {
    @Test
    fun `part implementation definition separates vendor identity from engineering concept identity`() {
        val implementation = PartImplementationDefinition(
            implementationId = PartImplementationId("impl/electrical/plc-cpu/siemens-cpu313c"),
            conceptId = EngineeringConceptId("electrical.plc.cpu"),
            vendorId = VendorId("siemens"),
            vendorPartNumber = VendorPartNumber("S7300.CPU313C"),
            displayName = "SIMATIC S7-300 CPU 313C",
            summary = "One Siemens implementation of the PLC CPU concept.",
        )
        val resolved = ResolvedPartImplementation(
            semanticSubjectId = StableSemanticIdentity("component:PLC1"),
            implementation = implementation,
        )

        assertEquals("electrical.plc.cpu", resolved.implementation.conceptId.value)
        assertEquals("siemens", resolved.implementation.vendorId.value)
        assertEquals("S7300.CPU313C", resolved.implementation.vendorPartNumber.value)
        assertEquals("component:PLC1", resolved.semanticSubjectId.value)
        assertNotEquals(resolved.implementation.conceptId.value, resolved.implementation.vendorPartNumber.value)
    }

    @Test
    fun `one engineering concept can own more than one vendor implementation`() {
        val conceptId = EngineeringConceptId("electrical.contactor.power")
        val implementations = listOf(
            PartImplementationDefinition(
                implementationId = PartImplementationId("impl/electrical/contactor/siemens-3rt2015"),
                conceptId = conceptId,
                vendorId = VendorId("siemens"),
                vendorPartNumber = VendorPartNumber("3RT2015"),
                displayName = "Siemens 3RT2015",
            ),
            PartImplementationDefinition(
                implementationId = PartImplementationId("impl/electrical/contactor/abb-af16"),
                conceptId = conceptId,
                vendorId = VendorId("abb"),
                vendorPartNumber = VendorPartNumber("AF16"),
                displayName = "ABB AF16",
            ),
        )

        assertEquals(2, implementations.size)
        assertEquals(1, implementations.map(PartImplementationDefinition::conceptId).toSet().size)
        assertEquals(setOf("siemens", "abb"), implementations.map { definition -> definition.vendorId.value }.toSet())
        assertTrue(implementations.none { definition -> definition.vendorPartNumber.value == conceptId.value })
    }
}
