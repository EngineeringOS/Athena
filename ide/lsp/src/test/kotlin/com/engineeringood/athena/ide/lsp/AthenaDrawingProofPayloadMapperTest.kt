package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.representation.DrawingAcceptanceAuthority
import com.engineeringood.athena.representation.DrawingProofAuthority
import com.engineeringood.athena.representation.DrawingProofFact
import com.engineeringood.athena.representation.DrawingProofFactId
import com.engineeringood.athena.representation.DrawingProofId
import com.engineeringood.athena.representation.DrawingProofPayload
import com.engineeringood.athena.representation.DrawingProofSchemaVersion
import com.engineeringood.athena.representation.DrawingProofStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AthenaDrawingProofPayloadMapperTest {
    @Test
    fun `maps core drawing proof through LSP owned transport payload`() {
        val proof = DrawingProofPayload(
            proofId = DrawingProofId("m33.lsp.proof"),
            schemaVersion = DrawingProofSchemaVersion("1.0"),
            acceptanceAuthority = DrawingAcceptanceAuthority.STRUCTURED_PROOF,
            facts = DrawingProofAuthority.entries.map { authority ->
                DrawingProofFact(
                    factId = DrawingProofFactId("fact.${authority.wireValue}"),
                    authority = authority,
                    status = DrawingProofStatus.PASS,
                    subject = "subject:${authority.wireValue}",
                    evidence = mapOf("source" to "structured-test"),
                )
            },
            diagnostics = emptyList(),
        )

        val payload = AthenaDrawingProofPayloadMapper.from(proof)

        assertTrue(payload.accepted)
        assertEquals("m33.lsp.proof", payload.proofId)
        assertEquals("structured-proof", payload.acceptanceAuthority)
        assertEquals(DrawingProofAuthority.entries.size, payload.facts.size)
        assertEquals("workbench-chrome", payload.facts.last().authority)
    }
}
