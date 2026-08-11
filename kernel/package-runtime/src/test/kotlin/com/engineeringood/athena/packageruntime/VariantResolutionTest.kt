package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.*
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.*

class VariantResolutionTest {
    @Test
    fun `variant preserves interface and element identity`() {
        val element = id("motor")
        val child = id("symbol")
        val composition = ElementComposition(listOf(ElementChildReference(child, 0)), listOf(FunctionSlot("power", "main")), listOf(ElementPort("out", child, "out", "power")))
        val fingerprint = ElementInterfaceFingerprintFactory.create(composition)
        val variant = RepresentationVariant(id("motor-compact"), element, fingerprint, PackageItemValue.ObjectValue(emptyMap()))
        val result = VariantResolver.resolve(element, variant, fingerprint)
        assertTrue(result.isValid)
        assertEquals(element, result.variant!!.elementId)
    }

    @Test
    fun `variant interface mismatch fails closed`() {
        val element = id("motor")
        val child = id("symbol")
        val expected = ElementInterfaceFingerprintFactory.create(ElementComposition(listOf(ElementChildReference(child, 0)), listOf(FunctionSlot("power", "main")), listOf(ElementPort("out", child, "out", "power"))))
        val changed = ElementInterfaceFingerprintFactory.create(ElementComposition(listOf(ElementChildReference(child, 0)), listOf(FunctionSlot("power", "main"), FunctionSlot("feedback", "sensor")), listOf(ElementPort("out", child, "out", "power"))))
        val result = VariantResolver.resolve(element, RepresentationVariant(id("motor-wide"), element, changed, PackageItemValue.ObjectValue(emptyMap())), expected)
        assertFalse(result.isValid)
        assertEquals("variant.interface.mismatch", result.diagnostics.single().code)
    }

    private fun id(item: String) = PackageItemIdentity(PackageIdentifier("com.athena.elements", "1"), item, "1")
}

