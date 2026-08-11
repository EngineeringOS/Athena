package com.engineeringood.athena.compiler

import com.engineeringood.athena.packageplatform.*
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.packageruntime.PackageItemAdmission
import com.engineeringood.athena.packageruntime.PackageItemAdmissionRequest
import kotlin.test.*

class PackageUsageTraceCompilerTest {
    @Test
    fun `usage trace joins ready item and occurrence deterministically`() {
        val item = identity()
        val binding = FunctionRepresentationBinding("binding-m1", FunctionRepresentationBindingKey("function:M1.drive", "electrical", "primary"), item)
        val admitted = PackageItemAdmission.admit(PackageItemAdmissionRequest(
            metadata = PackageItemMetadata(item, PackageItemKind.ELEMENT, PackageItemProvenance("catalog/motor.svg", "a".repeat(64), "MIT"), "elements/motor.yaml"),
            payload = PackageItemValue.ObjectValue(mapOf("label" to PackageItemValue.TextValue("M1"))),
            authoredFieldNames = setOf("identity", "kind", "provenance", "payloadReference"),
        )).item!!
        val first = PackageUsageTraceCompiler.compile(listOf(binding), mapOf(item.key to admitted), mapOf("occ-2" to listOf("binding-m1"), "occ-1" to listOf("binding-m1")), setOf("occ-1", "occ-2"))
        val second = PackageUsageTraceCompiler.compile(listOf(binding), mapOf(item.key to admitted), mapOf("occ-1" to listOf("binding-m1"), "occ-2" to listOf("binding-m1")), setOf("occ-1", "occ-2"))
        assertTrue(first.isValid)
        assertEquals(first.view!!.digest(), second.view!!.digest())
        assertEquals(listOf("occ-1", "occ-2"), first.view.traces.single().occurrenceIds)
    }

    @Test
    fun `usage trace rejects missing item and duplicate links`() {
        val item = identity()
        val binding = FunctionRepresentationBinding("binding-m1", FunctionRepresentationBindingKey("function:M1.drive", "electrical", "primary"), item)
        val result = PackageUsageTraceCompiler.compile(listOf(binding), emptyMap(), mapOf("occ-1" to listOf("binding-m1", "binding-m1"), "occ-2" to listOf("binding-m1")), setOf("occ-1", "occ-2"))
        assertFalse(result.isValid)
        assertNull(result.view)
        assertTrue(result.diagnostics.any { it.code == "package.usage.item.not-ready" })
        assertTrue(result.diagnostics.any { it.code == "package.usage.binding.duplicate" })
    }

    @Test
    fun `usage trace rejects occurrence outside visible scene`() {
        val item = identity()
        val binding = FunctionRepresentationBinding("binding-m1", FunctionRepresentationBindingKey("function:M1.drive", "electrical", "primary"), item)
        val result = PackageUsageTraceCompiler.compile(listOf(binding), emptyMap(), mapOf("occ-unknown" to listOf("binding-m1")), setOf("occ-visible"))
        assertTrue(result.diagnostics.any { it.code == "package.usage.occurrence.unknown" })
    }

    private fun identity() = PackageItemIdentity(PackageIdentifier("com.vendor.symbols", "1"), "motor-element", "1")
}
