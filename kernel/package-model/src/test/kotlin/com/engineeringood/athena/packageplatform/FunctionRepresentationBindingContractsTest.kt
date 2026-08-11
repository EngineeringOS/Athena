package com.engineeringood.athena.packageplatform

import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FunctionRepresentationBindingContractsTest {
    @Test
    fun `binding identity is stable and payload is canonical`() {
        val element = PackageItemIdentity(PackageIdentifier("com.vendor.symbols", "1"), "motor-element", "1")
        val first = binding(element, mapOf("label" to PackageItemValue.TextValue("M1"), "size" to PackageItemValue.NumberValue("2")))
        val second = binding(element, mapOf("size" to PackageItemValue.NumberValue("2"), "label" to PackageItemValue.TextValue("M1")))
        assertEquals("function:M1.drive#electrical#primary", first.key.canonicalId)
        assertEquals(first.canonicalPayload, second.canonicalPayload)
    }

    @Test
    fun `binding rejects invalid binding identity`() {
        assertFailsWith<IllegalArgumentException> {
            FunctionRepresentationBinding(
                bindingId = "",
                key = FunctionRepresentationBindingKey("function:M1.drive", "electrical", "primary"),
                element = PackageItemIdentity(PackageIdentifier("com.vendor.symbols", "1"), "motor", "1"),
            )
        }
    }

    @Test
    fun `provenance view is portable and usage view is canonical`() {
        val item = PackageItemIdentity(PackageIdentifier("com.vendor.symbols", "1"), "motor-element", "1")
        val provenance = PackageItemProvenance("catalog/vendor/motor.svg", "a".repeat(64), "MIT")
        val view = PackageProvenanceView(item, provenance)
        assertEquals("catalog/vendor/motor.svg", view.source)
        assertTrue(view.canonicalText().contains("com.vendor.symbols@1/motor-element@1"))
        val usage = FunctionRepresentationUsageTrace(
            bindingId = "binding-m1",
            functionId = "function:M1.drive",
            projectionId = "electrical",
            bindingRole = "primary",
            item = item,
            itemDigest = "b".repeat(64),
            provenance = view,
            occurrenceIds = listOf("occ-2", "occ-1"),
        )
        val canonical = PackageUsageTraceView(listOf(usage)).canonicalBytes()
        assertTrue(canonical.contentEquals(PackageUsageTraceView(listOf(usage.copy(occurrenceIds = listOf("occ-1", "occ-2")))).canonicalBytes()))
        assertTrue(canonical.isNotEmpty())
    }

    @Test
    fun `provenance rejects absolute locator`() {
        assertFailsWith<IllegalArgumentException> {
            PackageProvenanceView(
                PackageItemIdentity(PackageIdentifier("com.vendor.symbols", "1"), "motor-element", "1"),
                PackageItemProvenance("C:/machine/catalog.svg", "a".repeat(64), "MIT"),
            )
        }
    }

    private fun binding(element: PackageItemIdentity, values: Map<String, PackageItemValue> = emptyMap()) =
        FunctionRepresentationBinding(
            bindingId = "binding-m1",
            key = FunctionRepresentationBindingKey("function:M1.drive", "electrical", "primary"),
            element = element,
            placeholderValues = values,
        )
}
