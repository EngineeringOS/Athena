package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.AuthoringPreviewStatus
import com.engineeringood.athena.authoring.AuthoringRelationshipRoutePreviewEvidence
import com.engineeringood.athena.authoring.AuthoringTransactionProvenance
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionId
import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.interaction.AuthoringCapabilityEvidence
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.runtime.AthenaAuthoringSessionRecord
import com.engineeringood.athena.runtime.GovernedRelationshipPreviewRequest
import com.engineeringood.athena.runtime.GovernedRelationshipPreviewResult
import com.engineeringood.athena.runtime.GovernedRelationshipPreviewService
import com.engineeringood.athena.runtime.GovernedRelationshipRoutePreviewAuthority

internal fun acceptedSemanticRelationshipSourceEdit(
    trackedDocument: AthenaTrackedDocument,
    record: AthenaAuthoringSessionRecord,
): AthenaAuthoringSourceEditPayload? {
    if (record.preview.status != AuthoringPreviewStatus.ACCEPTED) return null
    val intent = record.intent as? SemanticRelationshipIntent ?: return null
    val evidence = record.preview.relationshipEvidence ?: return null
    if (evidence.sourceSubjectId != intent.sourceSubjectId.value || evidence.targetSubjectId != intent.targetSubjectId.value) {
        return null
    }
    return evidence.sourceEdit?.toPayload(trackedDocument.text)
}

internal fun governedSemanticRelationshipPreview(
    trackedDocument: AthenaTrackedDocument,
    intent: SemanticRelationshipIntent,
    previewId: AuthoringPreviewId,
    provenance: AuthoringTransactionProvenance,
    capabilityEvidence: AuthoringCapabilityEvidence,
): GovernedRelationshipPreviewResult? {
    val compilation = trackedDocument.compilation as? CompilerCompilationSuccess ?: return null
    val sourceDocument = trackedDocument.toBackendSourceDocument(trackedDocument.toAuthoringRevisionGuard()) ?: return null
    return GovernedRelationshipPreviewService().preview(
        GovernedRelationshipPreviewRequest(
            transactionId = SemanticAuthoringTransactionId("transaction:${intent.intentId.value}"),
            previewId = previewId,
            intent = intent,
            capabilityEvidence = capabilityEvidence,
            provenance = provenance,
            sourceDocument = sourceDocument,
            semanticDocument = compilation.document,
        ),
    )
}
