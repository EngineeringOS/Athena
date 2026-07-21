package com.engineeringood.athena.interaction

data class InteractionActionDiscoveryResult(
    val actions: List<SemanticActionIntent>,
    val diagnostics: List<InteractionDiagnostic>,
)

object InteractionActionDiscovery {
    fun discover(
        registry: SemanticCapabilityRegistry,
        subjectKey: InteractionSubjectKey,
        requestedBy: InteractionProvenance,
    ): InteractionActionDiscoveryResult {
        val subject = runCatching { registry.requireSubject(subjectKey) }.getOrElse {
            return InteractionActionDiscoveryResult(
                actions = emptyList(),
                diagnostics = listOf(
                    InteractionDiagnostic(
                        code = InteractionDiagnosticCode.SUBJECT_UNRESOLVED,
                        severity = InteractionDiagnosticSeverity.ERROR,
                        message = "Interaction subject ${subjectKey.canonicalSubjectId} is not registered.",
                        subject = subjectKey,
                        retryable = false,
                    ),
                ),
            )
        }

        val enabledCapabilities = subject.capabilities.filter(SemanticCapability::enabled)
        val disabledDiagnostics = subject.capabilities
            .filterNot(SemanticCapability::enabled)
            .map { capability ->
                capability.disabledReason ?: InteractionDiagnostic(
                    code = InteractionDiagnosticCode.ACTION_UNSUPPORTED,
                    severity = InteractionDiagnosticSeverity.WARNING,
                    message = "Interaction capability ${capability.capabilityId} is not enabled for ${subjectKey.canonicalSubjectId}.",
                    subject = subjectKey,
                    retryable = false,
                )
            }

        return InteractionActionDiscoveryResult(
            actions = enabledCapabilities.map { capability ->
                SemanticActionIntent(
                    actionIntentId = "action:${capability.capabilityId}:${subjectKey.canonicalSubjectId.value}",
                    actionFamily = capability.actionFamily,
                    subject = subjectKey,
                    requestedBy = requestedBy,
                    parameters = capability.parameters + mapOf("capabilityId" to capability.capabilityId),
                )
            },
            diagnostics = disabledDiagnostics,
        )
    }
}
