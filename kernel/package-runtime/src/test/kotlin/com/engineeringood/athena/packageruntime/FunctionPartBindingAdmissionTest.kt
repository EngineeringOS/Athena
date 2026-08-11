package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.FunctionPartBinding
import com.engineeringood.athena.packageplatform.PackageItemIdentity
import com.engineeringood.athena.packageplatform.PartFacts
import com.engineeringood.athena.packageplatform.PartItemReference
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FunctionPartBindingAdmissionTest {
    @Test
    fun `ready compatible part binding is admitted deterministically`() {
        val binding = binding()
        val facts = PartFacts(
            manufacturer = "Vendor",
            articleNumber = "M-400",
            technicalFields = emptyList(),
            functionTemplates = setOf("motor-drive"),
            capabilityContracts = setOf("motion", "power"),
            interfaceContracts = setOf("power-3ph"),
        )
        val requirements = FunctionPartBindingRequirements(
            functionTemplates = setOf("motor-drive"),
            capabilities = setOf("power", "motion"),
            interfaces = setOf("power-3ph"),
        )

        val first = FunctionPartBindingAdmission.admit(binding, facts, requirements)
        val second = FunctionPartBindingAdmission.admit(binding, facts, requirements)

        assertTrue(first.isValid)
        assertEquals(first, second)
        assertEquals("function:M1.drive#power", first.binding?.bindingId)
    }

    @Test
    fun `incompatible part binding fails closed with stable diagnostic order`() {
        val result = FunctionPartBindingAdmission.admit(
            binding(),
            PartFacts("Vendor", "M-400", emptyList(), capabilityContracts = setOf("motion")),
            FunctionPartBindingRequirements(
                functionTemplates = setOf("motor-drive"),
                capabilities = setOf("power", "motion"),
                interfaces = setOf("power-3ph"),
            ),
        )

        assertFalse(result.isValid)
        assertEquals(
            listOf(
                "part.binding.capability.incompatible",
                "part.binding.function-template.incompatible",
                "part.binding.interface.incompatible",
            ),
            result.diagnostics.map { it.code },
        )
    }

    private fun binding() = FunctionPartBinding(
        functionId = "function:M1.drive",
        implementationRole = "power",
        part = PartItemReference(
            PackageItemIdentity(PackageIdentifier("com.vendor.motion", "1"), "motor-400w", "1"),
        ),
    )
}
