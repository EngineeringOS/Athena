package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.runtime.AthenaCompilationQueries
import com.engineeringood.athena.runtime.AthenaCompilationRevision

data class AthenaValidationInspectionPayload(
    val revisionId: String,
    val validationDigest: String,
    val subject: String,
    val requirementIds: List<String>,
    val judgementIds: List<String>,
    val correctionIds: List<String>,
)

sealed interface AthenaValidationInspectionResult {
    data class Success(val payload: AthenaValidationInspectionPayload) : AthenaValidationInspectionResult
    data class Rejected(val code: String, val message: String) : AthenaValidationInspectionResult
}

/** LSP adapter: queries published revision, never resolves or evaluates engineering meaning. */
object AthenaKnowledgeValidationInspection {
    fun inspect(revision: AthenaCompilationRevision<*>, subject: String, expectedRevisionId: String): AthenaValidationInspectionResult {
        if (revision.revisionId != expectedRevisionId) {
            return AthenaValidationInspectionResult.Rejected("stale-revision", "Validation revision is stale. Refresh before inspecting `$subject`.")
        }
        if (subject.isBlank()) {
            return AthenaValidationInspectionResult.Rejected("invalid-subject", "Inspection subject must not be blank.")
        }
        val slice = AthenaCompilationQueries.validationSlice(revision, subject)
        return AthenaValidationInspectionResult.Success(
            AthenaValidationInspectionPayload(slice.revisionId, slice.validationDigest, subject, slice.requirements, slice.judgementIds, slice.correctionIds),
        )
    }
}
