package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.*
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FunctionRepresentationBindingAdmissionTest {
    @Test
    fun `only ready element and variant publish`() {
        val element = PackageItemIdentity(PackageIdentifier("com.vendor.symbols", "1"), "motor-element", "1")
        val binding = FunctionRepresentationBinding("binding-m1", FunctionRepresentationBindingKey("function:M1.drive", "electrical", "primary"), element)
        val rejected = FunctionRepresentationBindingAdmission.admit(binding, mapOf(element.key to PackageItemAdmissionState.PACKAGE_INCOMPLETE))
        assertEquals(null, rejected.binding)
        val accepted = FunctionRepresentationBindingAdmission.admit(binding, mapOf(element.key to PackageItemAdmissionState.PACKAGE_READY))
        assertEquals(binding, accepted.binding)
    }

    @Test
    fun `one Function admits independent electrical and simulation projection bindings`() {
        val element = PackageItemIdentity(PackageIdentifier("com.vendor.symbols", "1"), "motor-element", "1")
        val ready = mapOf(element.key to PackageItemAdmissionState.PACKAGE_READY)
        val electrical = FunctionRepresentationBinding(
            "binding-m1-electrical",
            FunctionRepresentationBindingKey("function:M1.drive", "electrical", "primary"),
            element,
        )
        val simulation = FunctionRepresentationBinding(
            "binding-m1-simulation",
            FunctionRepresentationBindingKey("function:M1.drive", "simulation", "primary"),
            element,
        )

        assertEquals(electrical, FunctionRepresentationBindingAdmission.admit(electrical, ready).binding)
        assertEquals(simulation, FunctionRepresentationBindingAdmission.admit(simulation, ready).binding)
        assertEquals(electrical.key.functionId, simulation.key.functionId)
        assertNotEquals(electrical.key.canonicalId, simulation.key.canonicalId)
    }
}
