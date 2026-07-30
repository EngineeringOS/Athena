package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DrawingAcceptanceEvidenceContractTest {
    @Test
    fun `drawing diagnostics classify every M33 acceptance authority`() {
        assertEquals(
            listOf(
                "symbol-anatomy",
                "graphic-primitive-ir",
                "sheet-composition",
                "route-anchor",
                "renderer-adapter",
                "workbench-chrome",
                "package-binding",
            ),
            DrawingEvidenceAuthority.entries.map { it.wireValue },
        )
    }

    @Test
    fun `evidence payload maps to deterministic product safe transport DTOs`() {
        val payload = DrawingAcceptanceEvidence(
            evidenceId = DrawingEvidenceId("m33.contract.evidence"),
            schemaVersion = DrawingEvidenceSchemaVersion("1.0"),
            acceptanceAuthority = DrawingAcceptanceAuthority.STRUCTURED_EVIDENCE,
            facts = DrawingEvidenceAuthority.entries.reversed().map { authority ->
                DrawingEvidenceFact(
                    factId = DrawingEvidenceFactId("fact.${authority.wireValue}"),
                    authority = authority,
                    status = DrawingEvidenceStatus.PASS,
                    subject = "subject:${authority.wireValue}",
                    evidence = mapOf("z" to "last", "a" to "first"),
                )
            },
            diagnostics = emptyList(),
        )

        val first = payload.toTransportPayload()
        val second = payload.toTransportPayload()

        assertTrue(first.accepted)
        assertEquals(first, second)
        assertEquals(
            DrawingEvidenceAuthority.entries.map { it.wireValue }.sorted(),
            first.facts.map { it.authority },
        )
        assertEquals(listOf("a", "z"), first.facts.first().evidence.keys.toList())
        assertEquals("structured-evidence", first.acceptanceAuthority)
    }

    @Test
    fun `evidence fails closed for failed facts or error diagnostics`() {
        val payload = DrawingAcceptanceEvidence(
            evidenceId = DrawingEvidenceId("m33.failed.evidence"),
            schemaVersion = DrawingEvidenceSchemaVersion("1.0"),
            acceptanceAuthority = DrawingAcceptanceAuthority.STRUCTURED_EVIDENCE,
            facts = listOf(
                DrawingEvidenceFact(
                    factId = DrawingEvidenceFactId("fact.route"),
                    authority = DrawingEvidenceAuthority.ROUTE_ANCHOR,
                    status = DrawingEvidenceStatus.FAIL,
                    subject = "connection:A-B",
                    evidence = emptyMap(),
                ),
            ),
            diagnostics = listOf(
                DrawingDiagnostic(
                    code = DrawingDiagnosticCode("drawing.route.anchor-missing"),
                    severity = DrawingDiagnosticSeverity.ERROR,
                    authority = DrawingEvidenceAuthority.ROUTE_ANCHOR,
                    subject = "connection:A-B",
                    message = "Route anchor is missing.",
                ),
            ),
        )

        assertFalse(payload.isAccepted)
        assertFalse(payload.toTransportPayload().accepted)
    }

    @Test
    fun `evidence rejects incomplete authority coverage empty evidence duplicate facts and unknown schema`() {
        val fact = DrawingEvidenceFact(
            factId = DrawingEvidenceFactId("fact.only"),
            authority = DrawingEvidenceAuthority.SYMBOL_ANATOMY,
            status = DrawingEvidenceStatus.PASS,
            subject = "symbol:one",
            evidence = emptyMap(),
        )
        val payload = DrawingAcceptanceEvidence(
            evidenceId = DrawingEvidenceId("m33.incomplete.evidence"),
            schemaVersion = DrawingEvidenceSchemaVersion("99.0"),
            acceptanceAuthority = DrawingAcceptanceAuthority.STRUCTURED_EVIDENCE,
            facts = listOf(fact, fact),
            diagnostics = emptyList(),
        )

        assertFalse(payload.isAccepted)
        assertFalse(payload.toTransportPayload().accepted)
        assertEquals(DrawingEvidenceAuthority.entries.map { it.wireValue }.sorted(), payload.toTransportPayload().requiredAuthorities)
    }

    @Test
    fun `required evidence authorities cannot be reduced by a producer`() {
        val payload = DrawingAcceptanceEvidence(
            evidenceId = DrawingEvidenceId("m33.fixed-authority.evidence"),
            schemaVersion = DrawingEvidenceSchemaVersion("1.0"),
            acceptanceAuthority = DrawingAcceptanceAuthority.STRUCTURED_EVIDENCE,
            facts = listOf(
                DrawingEvidenceFact(
                    factId = DrawingEvidenceFactId("fact.symbol"),
                    authority = DrawingEvidenceAuthority.SYMBOL_ANATOMY,
                    status = DrawingEvidenceStatus.PASS,
                    subject = "symbol:one",
                    evidence = mapOf("source" to "test"),
                ),
            ),
            diagnostics = emptyList(),
        )

        assertEquals(DrawingEvidenceAuthority.entries.toSet(), payload.requiredAuthorities)
        assertFalse(payload.isAccepted)
    }

    @Test
    fun `drawing diagnostic mapper preserves symbol and primitive authority`() {
        val symbolDiagnostic = DrawingDiagnosticMapper.from(
            DrawingSymbolDiagnostic(
                code = DrawingSymbolDiagnosticCode("drawing.symbol.anchor.missing"),
                severity = DrawingSymbolDiagnosticSeverity.ERROR,
                subject = "anchors",
                message = "Missing anchor.",
            ),
        )
        val primitiveDiagnostic = DrawingDiagnosticMapper.from(
            GraphicPrimitiveDiagnostic(
                code = GraphicPrimitiveDiagnosticCode("graphic.ir.geometry.invalid"),
                severity = GraphicPrimitiveDiagnosticSeverity.ERROR,
                subject = "primitive:line",
                message = "Invalid line.",
            ),
        )

        assertEquals(DrawingEvidenceAuthority.SYMBOL_ANATOMY, symbolDiagnostic.authority)
        assertEquals(DrawingEvidenceAuthority.GRAPHIC_PRIMITIVE_IR, primitiveDiagnostic.authority)
        assertEquals("drawing.symbol.anchor.missing", symbolDiagnostic.toTransportPayload().code)
        assertEquals("graphic.ir.geometry.invalid", primitiveDiagnostic.toTransportPayload().code)
    }
}
