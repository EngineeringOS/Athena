package com.engineeringood.athena.runtime

import com.engineeringood.athena.knowledge.EngineeringKnowledgeDocument
import com.engineeringood.athena.knowledge.KnowledgePackageIdentity
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.semantics.core.EngineeringValidationDocument
import com.engineeringood.athena.semantics.core.EngineeringValidationState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class AthenaCompilationRevisionTest {
    private val digest = "0".repeat(64)
    private val knowledge = EngineeringKnowledgeDocument(listOf(KnowledgePackageIdentity("core", "1")), emptyList(), emptyList())
    private val validation = EngineeringValidationDocument(EngineeringValidationState.READY, emptyList(), emptyList(), emptyList(), emptyList())

    @Test
    fun `publisher exposes one atomic revision and digest keyed cache`() {
        val revision = AthenaCompilationRevision("r1", "engineering", digest, knowledge, digest, validation, digest)
        val publisher = AthenaCompilationRevisionPublisher<String>()
        publisher.publish(revision)
        assertSame(revision, publisher.current())
        assertSame(revision, publisher.find("r1"))
        assertEquals("r1", publisher.current()?.revisionId)
    }
}
