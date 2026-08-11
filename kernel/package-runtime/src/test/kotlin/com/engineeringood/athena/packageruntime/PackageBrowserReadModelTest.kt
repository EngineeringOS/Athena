package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.*
import com.engineeringood.athena.repository.PackageIdentifier
import kotlin.test.*

class PackageBrowserReadModelTest {
    @Test
    fun `browser exposes only ready item with immutable inspection data`() {
        val identity = PackageItemIdentity(PackageIdentifier("com.athena.iec", "1"), "motor", "1")
        val metadata = PackageItemMetadata(identity, PackageItemKind.ELEMENT, PackageItemProvenance("catalog/motor.svg", "a".repeat(64), "MIT"), "elements/motor.yaml")
        val result = PackageItemAdmission.admit(PackageItemAdmissionRequest(metadata, PackageItemValue.ObjectValue(mapOf("label" to PackageItemValue.TextValue("M1"))), setOf("identity", "kind", "provenance", "payloadReference")))
        val admitted = result.item!!
        val report = PackageItemPublicationGate.publish(metadata.identity.packageId, setOf(identity.itemId), listOf(result)).report
        val item = PackageBrowserItem(identity, PackageItemKind.ELEMENT, admitted.canonicalDigest, "0,0", listOf(PackageBrowserPort("power", "OUT", "electrical", "power")), emptyList(), emptyList(), PackageProvenanceView(identity, metadata.provenance), "MIT", "resources/motor.svg")
        val snapshot = PackageBrowserReadModel.build(PackageItemPublicationGate.publish(metadata.identity.packageId, setOf(identity.itemId), listOf(result)).index!!, report, mapOf(identity.key to item))
        assertEquals(listOf(identity.key), snapshot.items.map { it.identity.key })
        assertEquals("catalog/motor.svg", snapshot.items.single().provenance.source)
    }

    @Test
    fun `browser rejects missing inspection details`() {
        val identity = PackageItemIdentity(PackageIdentifier("com.athena.iec", "1"), "motor", "1")
        val metadata = PackageItemMetadata(identity, PackageItemKind.ELEMENT, PackageItemProvenance("catalog/motor.svg", "a".repeat(64), "MIT"), "elements/motor.yaml")
        val result = PackageItemAdmission.admit(PackageItemAdmissionRequest(metadata, PackageItemValue.ObjectValue(emptyMap()), setOf("identity", "kind", "provenance", "payloadReference")))
        val publication = PackageItemPublicationGate.publish(identity.packageId, setOf(identity.itemId), listOf(result))
        assertFailsWith<IllegalArgumentException> { PackageBrowserReadModel.build(publication.index!!, publication.report, emptyMap()) }
    }
}

