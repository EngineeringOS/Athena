package com.engineeringood.athena.packageplatform

import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.*

class VariantContractsTest {
    @Test
    fun `interface fingerprint ignores declaration ordering`() {
        val child = id("motor")
        val first = ElementInterfaceFingerprintFactory.create(ElementComposition(listOf(ElementChildReference(child, 0)), listOf(FunctionSlot("power", "main"), FunctionSlot("control", "coil")), listOf(ElementPort("out", child, "out", "power"), ElementPort("in", child, "in", "control"))))
        val second = ElementInterfaceFingerprintFactory.create(ElementComposition(listOf(ElementChildReference(child, 0)), listOf(FunctionSlot("control", "coil"), FunctionSlot("power", "main")), listOf(ElementPort("in", child, "in", "control"), ElementPort("out", child, "out", "power"))))
        assertEquals(first, second)
    }

    private fun id(item: String) = PackageItemIdentity(PackageIdentifier("com.athena.elements", "1"), item, "1")
}

