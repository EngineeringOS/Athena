package com.engineeringood.athena.interaction

import com.engineeringood.athena.ir.StableSemanticIdentity

enum class InteractionSubjectKind {
    COMPONENT,
    PORT,
    CONNECTION,
    ROUTE,
    SHEET_OCCURRENCE,
    REFERENCE_MARKER,
    DIAGNOSTIC,
    SOURCE_RANGE,
    WORKSPACE,
}

enum class InteractionActionFamily {
    SELECT,
    HOVER,
    FOCUS,
    REVEAL,
    PREVIEW,
    ACCEPT,
    REJECT,
    MUTATE,
}

enum class InteractionOriginSurface {
    GRAPH,
    SOURCE,
    INSPECTOR,
    PROBLEMS,
    PALETTE,
    COMMAND_PALETTE,
    AI,
    API,
    RUNTIME,
}

enum class InteractionLifecycleState {
    REQUESTED,
    DISCOVERED,
    VALIDATED,
    PREVIEWING,
    ACCEPTED,
    REJECTED,
    MUTATION_PENDING,
    COMMITTED,
    REPROJECTED,
    BLOCKED,
    STALE,
    CANCELLED,
}

enum class InteractionPreviewStatus {
    READY,
    BLOCKED,
    STALE,
}

enum class InteractionDiagnosticSeverity {
    INFO,
    WARNING,
    ERROR,
}

enum class InteractionDiagnosticCode(val value: String) {
    SUBJECT_UNRESOLVED("interaction.subject.unresolved"),
    ACTION_UNSUPPORTED("interaction.action.unsupported"),
    COMMAND_INVALID_STATE("interaction.command.invalid-state"),
    COMMAND_STALE("interaction.command.stale"),
    MUTATION_INELIGIBLE("interaction.mutation.ineligible"),
    REVEAL_MISSING_TARGET("interaction.reveal.missing-target"),
    REGISTRY_STALE("interaction.registry.stale"),
    TRANSPORT_UNSUPPORTED_VERSION("interaction.transport.unsupported-version"),
    AUTHORING_CAPABILITY_UNAVAILABLE("authoring.capability.unavailable"),
}

enum class InteractionRevealSurface {
    SOURCE,
    GRAPH,
    INSPECTOR,
    PROBLEMS,
}

data class SourceRangeRef(
    val sourceUri: String,
    val startLine: Int,
    val startColumn: Int,
    val endLine: Int,
    val endColumn: Int,
)

data class InteractionSubjectKey(
    val canonicalSubjectId: StableSemanticIdentity,
    val subjectKind: InteractionSubjectKind,
    val sourceContextId: String? = null,
)

data class InteractionOccurrenceKey(
    val subjectKey: InteractionSubjectKey,
    val projectionViewId: String? = null,
    val sheetId: String? = null,
    val documentProjectionId: String? = null,
    val occurrenceId: String? = null,
    val sourceRevision: String? = null,
)

data class InteractionProvenance(
    val actor: String? = null,
    val originSurface: InteractionOriginSurface,
    val reason: String? = null,
    val timestamp: String? = null,
    val confidence: Double? = null,
)

data class SemanticCapability(
    val capabilityId: String,
    val actionFamily: InteractionActionFamily,
    val enabled: Boolean,
    val parameters: Map<String, String> = emptyMap(),
    val disabledReason: InteractionDiagnostic? = null,
    val authoring: AuthoringCapability? = null,
)

data class InteractionSubject(
    val key: InteractionSubjectKey,
    val occurrences: List<InteractionOccurrenceKey> = emptyList(),
    val sourceRange: SourceRangeRef? = null,
    val diagnosticId: String? = null,
    val capabilities: List<SemanticCapability> = emptyList(),
    val presentationMetadata: Map<String, String> = emptyMap(),
    val standardMetadata: Map<String, String> = emptyMap(),
    val adapterMetadata: Map<String, String> = emptyMap(),
    val provenance: InteractionProvenance,
)

data class SemanticActionIntent(
    val actionIntentId: String,
    val actionFamily: InteractionActionFamily,
    val subject: InteractionSubjectKey,
    val targetSubjects: List<InteractionSubjectKey> = emptyList(),
    val requestedBy: InteractionProvenance,
    val parameters: Map<String, String> = emptyMap(),
)

data class SourceImpact(
    val serializationTargetUri: String,
    val summary: String,
)

data class InteractionDiagnostic(
    val code: InteractionDiagnosticCode,
    val severity: InteractionDiagnosticSeverity,
    val message: String,
    val subject: InteractionSubjectKey? = null,
    val commandId: String? = null,
    val sourceRange: SourceRangeRef? = null,
    val retryable: Boolean,
)

data class InteractionPreview(
    val previewId: String,
    val commandId: String,
    val status: InteractionPreviewStatus,
    val sourceImpact: SourceImpact? = null,
    val affectedSubjects: List<InteractionSubjectKey> = emptyList(),
    val projectionPreview: Map<String, String> = emptyMap(),
    val diagnostics: List<InteractionDiagnostic> = emptyList(),
    val transient: Boolean = true,
    val persisted: Boolean = false,
)

data class InteractionCommand(
    val commandId: String,
    val actionIntent: SemanticActionIntent,
    val lifecycleState: InteractionLifecycleState,
    val preview: InteractionPreview? = null,
    val diagnostics: List<InteractionDiagnostic> = emptyList(),
    val undoable: Boolean,
    val mutationId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

data class InteractionRevealTarget(
    val target: InteractionRevealSurface,
    val sourceRange: SourceRangeRef? = null,
    val occurrence: InteractionOccurrenceKey? = null,
    val diagnosticId: String? = null,
)

data class InteractionRevealRequest(
    val subject: InteractionSubjectKey,
    val preferredTargets: Set<InteractionRevealSurface>,
    val occurrence: InteractionOccurrenceKey? = null,
)

data class InteractionRevealResult(
    val subject: InteractionSubjectKey,
    val targets: List<InteractionRevealTarget>,
    val diagnostics: List<InteractionDiagnostic>,
    val partial: Boolean,
)
