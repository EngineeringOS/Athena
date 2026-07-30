package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.compiler.BackendAuthoringSourceDocument
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlanned
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlanner
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditRejected
import com.engineeringood.athena.compiler.BackendCabinetPlacementPlanningRequest

const val ATHENA_GOVERNED_GRAPHIC_EDIT_PREVIEW_METHOD: String = "athena/graphicEdit/preview"

data class AthenaGovernedGraphicEditIntentRequest(
    val revisionGuard: AthenaAuthoringRevisionGuardPayload,
    val edit: AthenaGovernedGraphicEditPayload,
)

sealed interface AthenaGovernedGraphicEditPayload {
    data class LayoutMove(
        val occurrenceId: String,
        val dx: Double,
        val dy: Double,
        val surface: AthenaGovernedGraphicEditSurface,
    ) : AthenaGovernedGraphicEditPayload

    data class DirectMutation(
        val target: AthenaGovernedGraphicMutationTarget,
        val subject: String,
    ) : AthenaGovernedGraphicEditPayload
}

enum class AthenaGovernedGraphicEditSurface {
    CabinetProjection,
}

enum class AthenaGovernedGraphicMutationTarget {
    SvgResource,
    DomNode,
    GraphicPrimitiveIr,
    PlacementFact,
    RouteFact,
}

enum class AthenaGovernedGraphicEditDecision {
    PreviewReady,
    Rejected,
}

enum class AthenaGovernedGraphicEditAuthority {
    SourceMutation,
}

data class AthenaGovernedGraphicEditPreviewPayload(
    val decision: AthenaGovernedGraphicEditDecision,
    val authority: AthenaGovernedGraphicEditAuthority,
    val sourceEdit: AthenaAuthoringSourceEditPayload?,
    val validation: AthenaGovernedGraphicEditValidationPayload,
    val recompileRequired: Boolean,
    val rerenderRequired: Boolean,
    val evidence: List<String>,
)

data class AthenaGovernedGraphicEditValidationPayload(
    val diagnostics: List<AthenaGovernedGraphicEditDiagnosticPayload>,
)

data class AthenaGovernedGraphicEditDiagnosticPayload(
    val code: String,
    val message: String,
    val subject: String,
)

object AthenaGovernedGraphicEditIntentCompiler {
    fun preview(
        request: AthenaGovernedGraphicEditIntentRequest,
        document: BackendAuthoringSourceDocument,
    ): AthenaGovernedGraphicEditPreviewPayload =
        when (val edit = request.edit) {
            is AthenaGovernedGraphicEditPayload.LayoutMove -> previewLayoutMove(request.revisionGuard, edit, document)
            is AthenaGovernedGraphicEditPayload.DirectMutation -> rejectDirectMutation(edit)
        }

    private fun previewLayoutMove(
        revisionGuard: AthenaAuthoringRevisionGuardPayload,
        edit: AthenaGovernedGraphicEditPayload.LayoutMove,
        document: BackendAuthoringSourceDocument,
    ): AthenaGovernedGraphicEditPreviewPayload {
        val guard = revisionGuard.toAuthoringRevisionGuardOrNull()
            ?: return rejectLayoutMove(
                edit = edit,
                code = "authoring.source.conflict",
                message = "Graphic edit Revision Guard is malformed.",
            )
        return when (
            val result = BackendAuthoringSourceEditPlanner().plan(
                BackendCabinetPlacementPlanningRequest(
                    document = document,
                    revisionGuard = guard,
                    occurrenceId = edit.occurrenceId,
                    deltaXMillimeters = edit.dx,
                    deltaYMillimeters = edit.dy,
                ),
            )
        ) {
            is BackendAuthoringSourceEditPlanned -> AthenaGovernedGraphicEditPreviewPayload(
                decision = AthenaGovernedGraphicEditDecision.PreviewReady,
                authority = AthenaGovernedGraphicEditAuthority.SourceMutation,
                sourceEdit = result.plan.toPayload(document.sourceText),
                validation = AthenaGovernedGraphicEditValidationPayload(diagnostics = emptyList()),
                recompileRequired = true,
                rerenderRequired = true,
                evidence = listOf("compiler.validation", "source.mutation.preview", "projection.rerender"),
            )
            is BackendAuthoringSourceEditRejected -> AthenaGovernedGraphicEditPreviewPayload(
                decision = AthenaGovernedGraphicEditDecision.Rejected,
                authority = AthenaGovernedGraphicEditAuthority.SourceMutation,
                sourceEdit = null,
                validation = AthenaGovernedGraphicEditValidationPayload(
                    diagnostics = result.diagnostics.map { diagnostic ->
                        AthenaGovernedGraphicEditDiagnosticPayload(
                            code = diagnostic.code.value,
                            message = diagnostic.message,
                            subject = edit.occurrenceId,
                        )
                    },
                ),
                recompileRequired = false,
                rerenderRequired = false,
                evidence = listOf("compiler.validation", "source.ssot.preserved"),
            )
        }
    }

    private fun rejectLayoutMove(
        edit: AthenaGovernedGraphicEditPayload.LayoutMove,
        code: String,
        message: String,
    ): AthenaGovernedGraphicEditPreviewPayload = AthenaGovernedGraphicEditPreviewPayload(
        decision = AthenaGovernedGraphicEditDecision.Rejected,
        authority = AthenaGovernedGraphicEditAuthority.SourceMutation,
        sourceEdit = null,
        validation = AthenaGovernedGraphicEditValidationPayload(
            diagnostics = listOf(AthenaGovernedGraphicEditDiagnosticPayload(code, message, edit.occurrenceId)),
        ),
        recompileRequired = false,
        rerenderRequired = false,
        evidence = listOf("source.ssot.preserved"),
    )

    private fun rejectDirectMutation(
        edit: AthenaGovernedGraphicEditPayload.DirectMutation,
    ): AthenaGovernedGraphicEditPreviewPayload =
        AthenaGovernedGraphicEditPreviewPayload(
            decision = AthenaGovernedGraphicEditDecision.Rejected,
            authority = AthenaGovernedGraphicEditAuthority.SourceMutation,
            sourceEdit = null,
            validation = AthenaGovernedGraphicEditValidationPayload(
                diagnostics = listOf(
                    AthenaGovernedGraphicEditDiagnosticPayload(
                        code = "athena.graphicEdit.directMutation.forbidden",
                        message = "Direct mutation of ${edit.target.name} is forbidden; request a source mutation intent instead.",
                        subject = edit.subject,
                    ),
                ),
            ),
            recompileRequired = false,
            rerenderRequired = false,
            evidence = listOf("source.ssot.preserved", "direct.graphic.mutation.rejected"),
        )
}

private fun AthenaAuthoringRevisionGuardPayload.toAuthoringRevisionGuardOrNull(): AuthoringRevisionGuard? =
    runCatching {
        AuthoringRevisionGuard(
            semanticSnapshotId = semanticSnapshotId,
            sourceUri = sourceUri,
            documentVersion = documentVersion,
            contentSha256 = contentSha256,
        )
    }.getOrNull()
