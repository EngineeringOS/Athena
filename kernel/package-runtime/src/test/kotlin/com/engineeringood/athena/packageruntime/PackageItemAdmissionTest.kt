package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.*
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.*

class PackageItemAdmissionTest {
    @Test
    fun `all closed kinds admit through one authored and admitted contract`() {
        val admitted = PackageItemKind.entries.map { kind -> PackageItemAdmission.admit(request(kind = kind)).item }
        assertTrue(admitted.all { it?.admissionState == PackageItemAdmissionState.PACKAGE_READY })
        assertEquals(PackageItemKind.entries.toSet(), admitted.mapNotNull { it?.metadata?.kind }.toSet())
    }

    @Test
    fun `authored derived fields fail closed`() {
        val result = PackageItemAdmission.admit(request(authored = setOf("kind", "digest", "admissionState")))
        assertFalse(result.isValid)
        assertNull(result.item)
        assertEquals(listOf("admissionState", "digest"), result.diagnostics.map { it.problem.substringAfter('`').substringBefore('`') })
    }

    @Test
    fun `canonical digest normalizes maps sets numbers and unicode while preserving list order`() {
        val composed = "é"
        val decomposed = "e\u0301"
        val first = request(payload = payload(composed, listOf("a", "b"), linkedMapOf("z" to "last", "a" to "first")))
        val second = request(payload = payload(decomposed, listOf("a", "b"), linkedMapOf("a" to "first", "z" to "last")))
        val changedOrder = request(payload = payload(composed, listOf("b", "a"), linkedMapOf("a" to "first", "z" to "last")))

        val one = PackageItemAdmission.admit(first).item!!
        val two = PackageItemAdmission.admit(second).item!!
        val three = PackageItemAdmission.admit(changedOrder).item!!

        assertEquals(one.canonicalDigest, two.canonicalDigest)
        assertNotEquals(one.canonicalDigest, three.canonicalDigest)
        assertEquals(64, one.canonicalDigest.length)
    }

    @Test
    fun `incomplete item is inspectable but cannot publish package index`() {
        val result = PackageItemAdmission.admit(
            request(requiredReferences = setOf("base-symbol"), resolvedReferences = emptyMap()),
        )
        assertEquals(PackageItemAdmissionState.PACKAGE_INCOMPLETE, result.admissionState)
        assertNull(result.item)

        val publication = PackageItemPublicationGate.publish(
            PackageIdentifier("com.athena.iec", "1.0.0"),
            setOf("contactor.main"),
            listOf(result),
        )
        assertFalse(publication.isPublished)
        assertEquals(PackageItemAdmissionState.PACKAGE_INCOMPLETE, publication.report.state)
        assertNull(publication.index)
        assertTrue(publication.report.draftRevision.startsWith("draft:sha256:"))
        assertEquals(publication.report.canonicalBytes().toList(), publication.report.canonicalBytes().toList())
    }

    @Test
    fun `duplicate or foreign item identity blocks publication`() {
        val first = PackageItemAdmission.admit(request()).let { it }
        val duplicate = PackageItemAdmission.admit(request(payload = payload("other", listOf("a"), emptyMap())))
        val foreign = PackageItemAdmission.admit(
            request(metadata = PackageItemMetadata(
                PackageItemIdentity(PackageIdentifier("com.athena.vendor", "1.0.0"), "vendor.part", "1.0.0"),
                PackageItemKind.PART,
                PackageItemProvenance("catalog/vendor", "b".repeat(64), "MIT"),
                "parts/vendor.yaml",
            )),
        )

        val publication = PackageItemPublicationGate.publish(
            PackageIdentifier("com.athena.iec", "1.0.0"),
            setOf("contactor.main"),
            listOf(first, duplicate, foreign),
        )
        assertFalse(publication.isPublished)
        assertTrue(publication.report.diagnostics.any { it.code == "package.item.identity.duplicate" })
        assertTrue(publication.report.diagnostics.any { it.code == "package.item.owner.mismatch" })
    }

    @Test
    fun `invalid aggregate outranks incomplete and report ordering is deterministic`() {
        val incomplete = PackageItemAdmission.admit(
            request(requiredReferences = setOf("base-symbol"), resolvedReferences = emptyMap()),
        )
        val invalid = PackageItemAdmission.admit(request(authored = setOf("digest")))
        val first = PackageItemPublicationGate.publish(
            PackageIdentifier("com.athena.iec", "1.0.0"),
            setOf("contactor.main", "invalid.item"),
            listOf(incomplete, invalid),
        )
        val second = PackageItemPublicationGate.publish(
            PackageIdentifier("com.athena.iec", "1.0.0"),
            setOf("invalid.item", "contactor.main"),
            listOf(invalid, incomplete),
        )

        assertEquals(PackageItemAdmissionState.PACKAGE_INVALID, first.report.state)
        assertNull(first.index)
        assertEquals(first.report.canonicalBytes().toList(), second.report.canonicalBytes().toList())
        assertTrue(first.report.diagnostics.any { it.code == "package.item.authored-derived-field" })
    }

    @Test
    fun `canonical digest remains fixed for authored vector`() {
        val admitted = PackageItemAdmission.admit(request(
            payload = PackageItemValue.ObjectValue(
                mapOf(
                    "label" to PackageItemValue.TextValue("e\u0301"),
                    "order" to PackageItemValue.ListValue(listOf("a", "b").map(PackageItemValue::TextValue)),
                    "tags" to PackageItemValue.DeclaredSetValue(setOf("iec", "electrical")),
                    "rating" to PackageItemValue.NumberValue("24.00"),
                    "fields" to PackageItemValue.ObjectValue(mapOf("z" to PackageItemValue.TextValue("last"), "a" to PackageItemValue.TextValue("first"))),
                ),
            ),
        )).item!!
        assertEquals("eac24195c778132b91a273370f8d15dc11671b08d3f3e271bca125da11dc9e79", admitted.canonicalDigest)
    }

    private fun request(
        metadata: PackageItemMetadata = PackageItemMetadata(
            PackageItemIdentity(PackageIdentifier("com.athena.iec", "1.0.0"), "contactor.main", "1.0.0"),
            PackageItemKind.SYMBOL,
            PackageItemProvenance("catalog/iec", "a".repeat(64), "CC-BY-4.0"),
            "symbols/contactor.main.yaml",
        ),
        kind: PackageItemKind = PackageItemKind.SYMBOL,
        authored: Set<String> = setOf("identity", "kind", "provenance", "payloadReference"),
        payload: PackageItemValue.ObjectValue = payload("contactor", listOf("power", "control"), mapOf("center" to "0,0")),
        requiredReferences: Set<String> = emptySet(),
        resolvedReferences: Map<String, PackageItemIdentity> = emptyMap(),
    ) = PackageItemAdmissionRequest(
        metadata = metadata.copy(kind = kind),
        payload = payload,
        authoredFieldNames = authored,
        requiredReferenceNames = requiredReferences,
        resolvedReferences = resolvedReferences,
    )

    private fun payload(label: String, order: List<String>, fields: Map<String, String>) = PackageItemValue.ObjectValue(
        mapOf(
            "label" to PackageItemValue.TextValue(label),
            "order" to PackageItemValue.ListValue(order.map(PackageItemValue::TextValue)),
            "tags" to PackageItemValue.DeclaredSetValue(setOf("iec", "electrical")),
            "rating" to PackageItemValue.NumberValue("24.00"),
            "fields" to PackageItemValue.ObjectValue(fields.mapValues { PackageItemValue.TextValue(it.value) }),
        ),
    )
}
