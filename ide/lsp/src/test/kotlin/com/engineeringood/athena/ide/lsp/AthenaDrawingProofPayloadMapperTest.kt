package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.representation.DrawingAcceptanceAuthority
import com.engineeringood.athena.representation.DrawingEvidenceAuthority
import com.engineeringood.athena.representation.DrawingEvidenceFact
import com.engineeringood.athena.representation.DrawingEvidenceFactId
import com.engineeringood.athena.representation.DrawingEvidenceId
import com.engineeringood.athena.representation.DrawingAcceptanceEvidence
import com.engineeringood.athena.representation.DrawingEvidenceSchemaVersion
import com.engineeringood.athena.representation.DrawingEvidenceStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AthenaDrawingEvidencePayloadMapperTest {
    @Test
    fun `maps core drawing evidence through LSP owned transport payload`() {
        val evidence = DrawingAcceptanceEvidence(
            evidenceId = DrawingEvidenceId("m33.lsp.evidence"),
            schemaVersion = DrawingEvidenceSchemaVersion("1.0"),
            acceptanceAuthority = DrawingAcceptanceAuthority.STRUCTURED_EVIDENCE,
            facts = DrawingEvidenceAuthority.entries.map { authority ->
                DrawingEvidenceFact(
                    factId = DrawingEvidenceFactId("fact.${authority.wireValue}"),
                    authority = authority,
                    status = DrawingEvidenceStatus.PASS,
                    subject = "subject:${authority.wireValue}",
                    evidence = mapOf("source" to "structured-test"),
                )
            },
            diagnostics = emptyList(),
        )

        val payload = AthenaDrawingEvidencePayloadMapper.from(evidence)

        assertTrue(payload.accepted)
        assertEquals("m33.lsp.evidence", payload.evidenceId)
        assertEquals("structured-evidence", payload.acceptanceAuthority)
        assertEquals(DrawingEvidenceAuthority.entries.size, payload.facts.size)
        assertEquals("workbench-chrome", payload.facts.last().authority)
    }
}
