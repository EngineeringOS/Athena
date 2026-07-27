package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DrawingProofPayloadContractTest {
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
            DrawingProofAuthority.entries.map { it.wireValue },
        )
    }

    @Test
    fun `proof payload maps to deterministic product safe transport DTOs`() {
        val payload = DrawingProofPayload(
            proofId = DrawingProofId("m33.contract.proof"),
            schemaVersion = DrawingProofSchemaVersion("1.0"),
            acceptanceAuthority = DrawingAcceptanceAuthority.STRUCTURED_PROOF,
            facts = DrawingProofAuthority.entries.reversed().map { authority ->
                DrawingProofFact(
                    factId = DrawingProofFactId("fact.${authority.wireValue}"),
                    authority = authority,
                    status = DrawingProofStatus.PASS,
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
            DrawingProofAuthority.entries.map { it.wireValue }.sorted(),
            first.facts.map { it.authority },
        )
        assertEquals(listOf("a", "z"), first.facts.first().evidence.keys.toList())
        assertEquals("structured-proof", first.acceptanceAuthority)
    }

    @Test
    fun `proof fails closed for failed facts or error diagnostics`() {
        val payload = DrawingProofPayload(
            proofId = DrawingProofId("m33.failed.proof"),
            schemaVersion = DrawingProofSchemaVersion("1.0"),
            acceptanceAuthority = DrawingAcceptanceAuthority.STRUCTURED_PROOF,
            facts = listOf(
                DrawingProofFact(
                    factId = DrawingProofFactId("fact.route"),
                    authority = DrawingProofAuthority.ROUTE_ANCHOR,
                    status = DrawingProofStatus.FAIL,
                    subject = "connection:A-B",
                    evidence = emptyMap(),
                ),
            ),
            diagnostics = listOf(
                DrawingDiagnostic(
                    code = DrawingDiagnosticCode("drawing.route.anchor-missing"),
                    severity = DrawingDiagnosticSeverity.ERROR,
                    authority = DrawingProofAuthority.ROUTE_ANCHOR,
                    subject = "connection:A-B",
                    message = "Route anchor is missing.",
                ),
            ),
        )

        assertFalse(payload.isAccepted)
        assertFalse(payload.toTransportPayload().accepted)
    }

    @Test
    fun `proof rejects incomplete authority coverage empty evidence duplicate facts and unknown schema`() {
        val fact = DrawingProofFact(
            factId = DrawingProofFactId("fact.only"),
            authority = DrawingProofAuthority.SYMBOL_ANATOMY,
            status = DrawingProofStatus.PASS,
            subject = "symbol:one",
            evidence = emptyMap(),
        )
        val payload = DrawingProofPayload(
            proofId = DrawingProofId("m33.incomplete.proof"),
            schemaVersion = DrawingProofSchemaVersion("99.0"),
            acceptanceAuthority = DrawingAcceptanceAuthority.STRUCTURED_PROOF,
            facts = listOf(fact, fact),
            diagnostics = emptyList(),
        )

        assertFalse(payload.isAccepted)
        assertFalse(payload.toTransportPayload().accepted)
        assertEquals(DrawingProofAuthority.entries.map { it.wireValue }.sorted(), payload.toTransportPayload().requiredAuthorities)
    }

    @Test
    fun `required proof authorities cannot be reduced by a producer`() {
        val payload = DrawingProofPayload(
            proofId = DrawingProofId("m33.fixed-authority.proof"),
            schemaVersion = DrawingProofSchemaVersion("1.0"),
            acceptanceAuthority = DrawingAcceptanceAuthority.STRUCTURED_PROOF,
            facts = listOf(
                DrawingProofFact(
                    factId = DrawingProofFactId("fact.symbol"),
                    authority = DrawingProofAuthority.SYMBOL_ANATOMY,
                    status = DrawingProofStatus.PASS,
                    subject = "symbol:one",
                    evidence = mapOf("source" to "test"),
                ),
            ),
            diagnostics = emptyList(),
        )

        assertEquals(DrawingProofAuthority.entries.toSet(), payload.requiredAuthorities)
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

        assertEquals(DrawingProofAuthority.SYMBOL_ANATOMY, symbolDiagnostic.authority)
        assertEquals(DrawingProofAuthority.GRAPHIC_PRIMITIVE_IR, primitiveDiagnostic.authority)
        assertEquals("drawing.symbol.anchor.missing", symbolDiagnostic.toTransportPayload().code)
        assertEquals("graphic.ir.geometry.invalid", primitiveDiagnostic.toTransportPayload().code)
    }
}
