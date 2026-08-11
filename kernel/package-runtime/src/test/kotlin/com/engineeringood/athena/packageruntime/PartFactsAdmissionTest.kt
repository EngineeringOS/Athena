package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PartFactsAdmissionTest {
    @Test
    fun `missing capability fails closed`() {
        val result = PartFactsAdmission.admit(
            PartFacts("Vendor", "A-1", listOf(PartTechnicalField("voltage", PackageItemValue.NumberValue("24")))),
            requiredCapabilities = setOf("motor-protection"),
        )
        assertFalse(result.isValid)
        assertTrue(result.diagnostics.single().code == "part.capability.incompatible")
    }
}
