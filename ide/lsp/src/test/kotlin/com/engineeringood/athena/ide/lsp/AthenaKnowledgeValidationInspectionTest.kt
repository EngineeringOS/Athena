package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.knowledge.EngineeringKnowledgeDocument
import com.engineeringood.athena.knowledge.KnowledgePackageIdentity
import com.engineeringood.athena.runtime.AthenaCompilationRevision
import com.engineeringood.athena.semantics.core.EngineeringValidationDocument
import com.engineeringood.athena.semantics.core.EngineeringValidationState
import kotlin.test.Test
import kotlin.test.assertIs

class AthenaKnowledgeValidationInspectionTest {
    private val digest = "0".repeat(64)

    @Test
    fun `stale revision rejected and current revision queried`() {
        val revision = AthenaCompilationRevision(
            "r1", "engineering", digest,
            EngineeringKnowledgeDocument(listOf(KnowledgePackageIdentity("core", "1")), emptyList(), emptyList()), digest,
            EngineeringValidationDocument(EngineeringValidationState.READY, emptyList(), emptyList(), emptyList(), emptyList()), digest,
        )
        assertIs<AthenaValidationInspectionResult.Rejected>(AthenaKnowledgeValidationInspection.inspect(revision, "M1", "r0"))
        assertIs<AthenaValidationInspectionResult.Success>(AthenaKnowledgeValidationInspection.inspect(revision, "M1", "r1"))
    }
}
