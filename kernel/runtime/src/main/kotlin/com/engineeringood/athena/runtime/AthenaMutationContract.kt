package com.engineeringood.athena.runtime

/**
 * Runtime-owned category vocabulary for governed change and interaction requests.
 *
 * The vocabulary is intentionally renderer-neutral so source, graph, and future IDE surfaces
 * converge on one Athena-owned classification model instead of inventing frontend-specific terms.
 */
enum class AthenaMutationCategory {
    SEMANTIC_MUTATION,
    PROJECTION_MUTATION,
    TRANSIENT_INTERACTION,
}

/**
 * Runtime-owned outcome vocabulary for one inspectable mutation attempt.
 */
enum class AthenaMutationOutcome {
    ACCEPTED,
    REJECTED,
    VALIDATION_FEEDBACK,
    UNAVAILABLE,
}

/**
 * Runtime-owned severity for validation feedback emitted during a governed mutation path.
 */
enum class AthenaMutationValidationFeedbackSeverity {
    INFO,
    WARNING,
    ERROR,
}

/**
 * One inspectable validation feedback item attached to a runtime-owned mutation result.
 */
data class AthenaMutationValidationFeedback(
    val code: String,
    val message: String,
    val severity: AthenaMutationValidationFeedbackSeverity,
    val relatedSemanticIds: List<String> = emptyList(),
)

/**
 * Shared Athena-owned mutation result contract used by command execution and history-backed mutation surfaces.
 */
sealed interface AthenaMutationResult {
    /**
     * Runtime project name associated with the mutation attempt.
     */
    val projectName: String

    /**
     * Explicit Athena-owned category for the governed change or interaction.
     */
    val mutationCategory: AthenaMutationCategory

    /**
     * Stable outcome classification for the mutation attempt.
     */
    val outcome: AthenaMutationOutcome

    /**
     * Inspectable validation feedback attached to the mutation attempt.
     */
    val validationFeedback: List<AthenaMutationValidationFeedback>
}

/**
 * Resolves the default mutation category for the current command kind.
 */
internal fun AthenaCommandKind.defaultMutationCategory(): AthenaMutationCategory {
    return when (this) {
        AthenaCommandKind.CONNECT_PORTS -> AthenaMutationCategory.SEMANTIC_MUTATION
        AthenaCommandKind.APPLY_SEMANTIC_MACRO_BUNDLE -> AthenaMutationCategory.SEMANTIC_MUTATION
    }
}

/**
 * Resolves the default mutation category for the current history mutation operation.
 */
internal fun AthenaCommandHistoryOperation.defaultMutationCategory(): AthenaMutationCategory {
    return AthenaMutationCategory.SEMANTIC_MUTATION
}
