package com.engineeringood.athena.presentation

import com.engineeringood.athena.authoring.AuthoringIntentId
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.AuthoringSourceEditEvidence
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionId
import com.engineeringood.athena.interaction.InteractionOccurrenceKey
import com.engineeringood.athena.interaction.InteractionRevealSurface
import com.engineeringood.athena.interaction.InteractionRevealTarget
import com.engineeringood.athena.interaction.InteractionSubject
import com.engineeringood.athena.interaction.SemanticActionIntent
import com.engineeringood.athena.interaction.SemanticCapabilityRegistry
import com.engineeringood.athena.interaction.SourceRangeRef

data class CabinetGraphicSelectionRequest(
    val selectedOccurrenceId: GraphicOccurrenceId?,
    val traceTable: GraphicOccurrenceTraceTable,
    val registry: SemanticCapabilityRegistry,
    val requestedBy: com.engineeringood.athena.interaction.InteractionProvenance,
    val attemptedFallbacks: Set<CabinetForbiddenSelectionFallback> = emptySet(),
)

sealed interface CabinetGraphicSelectionResolution {
    data class Success(
        val subject: InteractionSubject,
        val revealTarget: InteractionRevealTarget,
        val trace: CabinetSelectionTrace,
    ) : CabinetGraphicSelectionResolution

    data class Failure(val diagnostics: List<CabinetSelectionDiagnostic>) : CabinetGraphicSelectionResolution
}

data class CabinetSelectionTrace(
    val occurrenceId: GraphicOccurrenceId,
    val subjectAuthority: String,
    val capabilityAuthority: String,
    val revealAuthority: String,
    val fallbackUsed: Boolean,
)

data class CabinetSelectionDiagnostic(
    val code: String,
    val message: String,
    val occurrenceId: GraphicOccurrenceId? = null,
    val attemptedFallbacks: Set<CabinetForbiddenSelectionFallback> = emptySet(),
)

enum class CabinetForbiddenSelectionFallback {
    SEMANTIC_ID_PREFIX,
    LABEL_TEXT,
    DOM_ID,
    SVG_NODE_ID,
    PRIMITIVE_ID,
}

object CabinetGraphicSelectionResolver {
    fun resolve(request: CabinetGraphicSelectionRequest): CabinetGraphicSelectionResolution {
        if (request.attemptedFallbacks.isNotEmpty()) {
            return CabinetGraphicSelectionResolution.Failure(
                listOf(
                    CabinetSelectionDiagnostic(
                        code = "cabinet.selection.fallback_forbidden",
                        message = "Cabinet selection must resolve from GraphicOccurrenceTraceTable only.",
                        occurrenceId = request.selectedOccurrenceId,
                        attemptedFallbacks = request.attemptedFallbacks,
                    ),
                ),
            )
        }

        val occurrenceId = request.selectedOccurrenceId
            ?: return CabinetGraphicSelectionResolution.Failure(
                listOf(
                    CabinetSelectionDiagnostic(
                        code = "cabinet.selection.occurrence_required",
                        message = "Cabinet selection requires a GraphicOccurrenceId.",
                    ),
                ),
            )

        val trace = request.traceTable.entries.singleOrNull { entry -> entry.occurrenceId == occurrenceId }
            ?: return CabinetGraphicSelectionResolution.Failure(
                listOf(
                    CabinetSelectionDiagnostic(
                        code = "cabinet.selection.trace_missing",
                        message = "No normalized trace entry exists for selected cabinet occurrence.",
                        occurrenceId = occurrenceId,
                    ),
                ),
            )

        val subjectKey = request.registry.subjects
            .singleOrNull { subject -> subject.key.canonicalSubjectId == trace.semanticSubjectId }
            ?.key
            ?: return CabinetGraphicSelectionResolution.Failure(
                listOf(
                    CabinetSelectionDiagnostic(
                        code = "cabinet.selection.capability_subject_missing",
                        message = "Trace subject is not admitted by the semantic capability registry.",
                        occurrenceId = occurrenceId,
                    ),
                ),
            )
        val subject = request.registry.requireSubject(subjectKey)
        val sourceRange = trace.sourceChain.mountedOccurrence.span.toSourceRangeRef()
        val occurrence = InteractionOccurrenceKey(
            subjectKey = subject.key,
            occurrenceId = occurrenceId.value,
            sourceRevision = subject.occurrences.firstOrNull()?.sourceRevision,
        )

        return CabinetGraphicSelectionResolution.Success(
            subject = subject.copy(sourceRange = sourceRange),
            revealTarget = InteractionRevealTarget(
                target = InteractionRevealSurface.SOURCE,
                sourceRange = sourceRange,
                occurrence = occurrence,
            ),
            trace = CabinetSelectionTrace(
                occurrenceId = occurrenceId,
                subjectAuthority = "graphic-occurrence-trace-table",
                capabilityAuthority = "semantic-capability-registry",
                revealAuthority = "trace-source-span",
                fallbackUsed = false,
            ),
        )
    }
}

data class CabinetEditRequest(
    val action: SemanticActionIntent,
    val authoringRequestId: AuthoringIntentId,
    val transactionId: SemanticAuthoringTransactionId,
    val previewId: AuthoringPreviewId,
    val sourceEditTrace: AuthoringSourceEditEvidence,
    val target: CabinetGovernedEditTarget,
    val compileLintRequired: Boolean,
    val rerenderRequired: Boolean,
)

enum class CabinetGovernedEditTarget {
    INSTALLATION_DECLARATION,
    BINDING_DECLARATION,
    PROFILE_DECLARATION,
    ENGINEERING_DECLARATION,
}

enum class CabinetForbiddenMutationTarget {
    SVG_DOM,
    SVG_FILE,
    GRAPHIC_PRIMITIVE_IR,
    PRESENTATION_IR,
    REPRESENTATION_OCCURRENCE,
    RENDERER_STATE,
}

sealed interface CabinetGovernedEditBoundaryResult {
    data class Accepted(val path: CabinetEditPath) : CabinetGovernedEditBoundaryResult

    data class Failure(val diagnostics: List<CabinetGovernedEditDiagnostic>) : CabinetGovernedEditBoundaryResult
}

data class CabinetEditPath(
    val orderedStages: List<String>,
    val target: CabinetGovernedEditTarget,
)

data class CabinetGovernedEditDiagnostic(
    val code: String,
    val message: String,
    val target: String,
)

object CabinetGovernedEditBoundary {
    private val governedStages = listOf(
        "SemanticAction",
        "AuthoringRequest",
        "SemanticAuthoringTransaction",
        "AuthoringPreview",
        "SourceEditTrace",
        "compile-lint",
        "rerender",
    )

    fun rejectDirectMutation(target: CabinetForbiddenMutationTarget): CabinetGovernedEditBoundaryResult =
        CabinetGovernedEditBoundaryResult.Failure(
            listOf(
                CabinetGovernedEditDiagnostic(
                    code = "cabinet.edit.direct_mutation_forbidden",
                    message = "Cabinet edits must enter the governed authoring path before source mutation.",
                    target = target.name,
                ),
            ),
        )

    fun acceptGovernedPath(request: CabinetEditRequest): CabinetGovernedEditBoundaryResult {
        val diagnostics = buildList {
            if (!request.compileLintRequired) {
                add(diagnostic("cabinet.edit.compile_lint_required", request.target))
            }
            if (!request.rerenderRequired) {
                add(diagnostic("cabinet.edit.rerender_required", request.target))
            }
            if (request.action.subject.canonicalSubjectId.value !in request.sourceEditTrace.affectedSemanticIds) {
                add(diagnostic("cabinet.edit.source_subject_mismatch", request.target))
            }
        }
        if (diagnostics.isNotEmpty()) {
            return CabinetGovernedEditBoundaryResult.Failure(diagnostics)
        }

        return CabinetGovernedEditBoundaryResult.Accepted(
            CabinetEditPath(
                orderedStages = governedStages,
                target = request.target,
            ),
        )
    }

    private fun diagnostic(code: String, target: CabinetGovernedEditTarget): CabinetGovernedEditDiagnostic =
        CabinetGovernedEditDiagnostic(
            code = code,
            message = "Governed cabinet edit request is incomplete.",
            target = target.name,
        )
}

private fun TraceSourceSpan.toSourceRangeRef(): SourceRangeRef =
    SourceRangeRef(
        sourceUri = file,
        startLine = line,
        startColumn = column,
        endLine = line,
        endColumn = column,
    )
