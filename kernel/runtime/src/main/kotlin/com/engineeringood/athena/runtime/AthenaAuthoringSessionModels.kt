package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AuthoringIntent
import com.engineeringood.athena.authoring.AuthoringPreview
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.SemanticAuthoringTransaction
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlan

/** Stored backend-owned inputs required to execute one governed preview decision. */
data class AthenaGovernedAuthoringPreviewContext(
    val transaction: SemanticAuthoringTransaction,
    val validationAuthority: AuthoringTransactionValidationAuthority,
    val sourceEditPlan: BackendAuthoringSourceEditPlan? = null,
)

/** Active authorities supplied by the host when a governed decision is executed. */
data class AthenaGovernedAuthoringDecisionAuthorities(
    val revisionAuthority: ActiveAuthoringRevisionAuthority,
    val mutationAuthority: SemanticAuthoringMutationAuthority,
    val reprojectionAuthority: SemanticAuthoringReprojectionAuthority,
    val acceptanceValidationAuthority: AuthoringTransactionValidationAuthority? = null,
)

data class AthenaAuthoringSessionRecord(
    val intent: AuthoringIntent,
    val preview: AuthoringPreview,
    val governedContext: AthenaGovernedAuthoringPreviewContext? = null,
)

data class AthenaAuthoringSessionSnapshot(
    val records: List<AthenaAuthoringSessionRecord>,
    val nextPreviewOrdinal: Int,
)

data class AthenaAuthoringSessionView(
    val records: List<AthenaAuthoringSessionRecord>,
    val pendingPreviewCount: Int,
)

sealed interface AthenaAuthoringPreviewSubmissionResult

data class AthenaAuthoringPreviewSubmitted(
    val record: AthenaAuthoringSessionRecord,
) : AthenaAuthoringPreviewSubmissionResult

sealed interface AthenaAuthoringPreviewDecisionResult

data class AthenaAuthoringPreviewDecisionUpdated(
    val record: AthenaAuthoringSessionRecord,
    val transaction: SemanticAuthoringTransaction? = record.governedContext?.transaction,
) : AthenaAuthoringPreviewDecisionResult

data class AthenaAuthoringPreviewDecisionUnavailable(
    val previewId: AuthoringPreviewId,
    val reason: String,
) : AthenaAuthoringPreviewDecisionResult
