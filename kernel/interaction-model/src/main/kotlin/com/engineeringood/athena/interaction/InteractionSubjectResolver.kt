package com.engineeringood.athena.interaction

data class InteractionSelectionPayload(
    val subjectKey: InteractionSubjectKey?,
    val adapterMetadata: Map<String, String> = emptyMap(),
)

data class InteractionSubjectResolutionResult(
    val subject: InteractionSubject?,
    val diagnostics: List<InteractionDiagnostic>,
)

object InteractionSubjectResolver {
    fun resolve(
        registry: SemanticCapabilityRegistry,
        selection: InteractionSelectionPayload,
    ): InteractionSubjectResolutionResult {
        val key = selection.subjectKey ?: return unresolved(null)
        val subject = runCatching { registry.requireSubject(key) }.getOrNull()
            ?: return unresolved(key)

        return InteractionSubjectResolutionResult(
            subject = subject,
            diagnostics = emptyList(),
        )
    }

    private fun unresolved(key: InteractionSubjectKey?): InteractionSubjectResolutionResult {
        return InteractionSubjectResolutionResult(
            subject = null,
            diagnostics = listOf(
                InteractionDiagnostic(
                    code = InteractionDiagnosticCode.SUBJECT_UNRESOLVED,
                    severity = InteractionDiagnosticSeverity.ERROR,
                    message = "Selection does not contain a governed interaction subject.",
                    subject = key,
                    retryable = false,
                ),
            ),
        )
    }
}
