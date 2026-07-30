package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.representation.DrawingAcceptanceEvidence

data class AthenaDrawingEvidencePayload(
    val evidenceId: String,
    val schemaVersion: String,
    val acceptanceAuthority: String,
    val accepted: Boolean,
    val requiredAuthorities: List<String>,
    val facts: List<AthenaDrawingEvidenceFactPayload>,
    val diagnostics: List<AthenaDrawingDiagnosticPayload>,
)

data class AthenaDrawingEvidenceFactPayload(
    val factId: String,
    val authority: String,
    val status: String,
    val subject: String,
    val evidence: Map<String, String>,
)

data class AthenaDrawingDiagnosticPayload(
    val code: String,
    val severity: String,
    val authority: String,
    val subject: String,
    val message: String,
)

object AthenaDrawingEvidencePayloadMapper {
    fun from(evidence: DrawingAcceptanceEvidence): AthenaDrawingEvidencePayload {
        val source = evidence.toTransportPayload()
        return AthenaDrawingEvidencePayload(
            evidenceId = source.evidenceId,
            schemaVersion = source.schemaVersion,
            acceptanceAuthority = source.acceptanceAuthority,
            accepted = source.accepted,
            requiredAuthorities = source.requiredAuthorities,
            facts = source.facts.map { fact ->
                AthenaDrawingEvidenceFactPayload(
                    factId = fact.factId,
                    authority = fact.authority,
                    status = fact.status,
                    subject = fact.subject,
                    evidence = fact.evidence,
                )
            },
            diagnostics = source.diagnostics.map { diagnostic ->
                AthenaDrawingDiagnosticPayload(
                    code = diagnostic.code,
                    severity = diagnostic.severity,
                    authority = diagnostic.authority,
                    subject = diagnostic.subject,
                    message = diagnostic.message,
                )
            },
        )
    }
}
