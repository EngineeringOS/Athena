package com.engineeringood.athena.knowledge

import com.engineeringood.athena.knowledge.protocol.CanonicalJsonProtocol
import com.engineeringood.athena.ir.EngineeringDefinitionId
import com.engineeringood.athena.ir.EngineeringPackageName
import com.engineeringood.athena.ir.SourceProvenance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class CanonicalJsonProtocolTest {
    private val provenance = SourceProvenance("packages/core.athena", 1, 1, 1, 4)

    @Test
    fun `knowledge bytes and digest stay deterministic`() {
        val id = EngineeringDefinitionId(EngineeringPackageName("core"), "power.capability")
        val document = EngineeringKnowledgeDocument(
            packages = listOf(KnowledgePackageIdentity("core", "1.0.0")),
            definitions = listOf(KnowledgeDefinition.Capability(id, provenance = provenance)),
            provenance = listOf(provenance),
        )
        val first = CanonicalJsonProtocol.knowledge(document)
        val second = CanonicalJsonProtocol.knowledge(document)
        assertEquals(first.bytes.toList(), second.bytes.toList())
        assertEquals(64, first.digest.length)
        assertFalse(first.bytes.firstOrNull()?.toInt() == 0xEF)
        assertEquals(first.digest, second.digest)
    }

    @Test
    fun `unpaired surrogate is rejected`() {
        val id = EngineeringDefinitionId(EngineeringPackageName("core"), "power.capability")
        assertFailsWith<IllegalArgumentException> {
            CanonicalJsonProtocol.knowledge(EngineeringKnowledgeDocument(
                listOf(KnowledgePackageIdentity("core", "1")),
                listOf(KnowledgeDefinition.Capability(id, provenance = provenance.copy(file = "bad\uD800.athena"))),
                listOf(provenance.copy(file = "bad\uD800.athena")),
            ))
        }
    }
}
