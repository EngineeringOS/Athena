package com.engineeringood.athena.interaction

object InteractionLifecycle {
    private val terminalStates = setOf(
        InteractionLifecycleState.BLOCKED,
        InteractionLifecycleState.CANCELLED,
        InteractionLifecycleState.REJECTED,
        InteractionLifecycleState.STALE,
    )

    private val legalTransitions = mapOf(
        InteractionLifecycleState.REQUESTED to setOf(InteractionLifecycleState.DISCOVERED),
        InteractionLifecycleState.DISCOVERED to setOf(InteractionLifecycleState.VALIDATED),
        InteractionLifecycleState.VALIDATED to setOf(InteractionLifecycleState.PREVIEWING),
        InteractionLifecycleState.PREVIEWING to setOf(InteractionLifecycleState.ACCEPTED),
        InteractionLifecycleState.ACCEPTED to setOf(InteractionLifecycleState.MUTATION_PENDING),
        InteractionLifecycleState.MUTATION_PENDING to setOf(InteractionLifecycleState.COMMITTED),
        InteractionLifecycleState.COMMITTED to setOf(InteractionLifecycleState.REPROJECTED),
    )

    fun transition(command: InteractionCommand, nextState: InteractionLifecycleState): InteractionCommand {
        if (nextState in terminalStates || nextState in legalTransitions[command.lifecycleState].orEmpty()) {
            return command.copy(
                lifecycleState = nextState,
                diagnostics = emptyList(),
            )
        }

        return command.copy(
            diagnostics = command.diagnostics + InteractionDiagnostic(
                code = InteractionDiagnosticCode.COMMAND_INVALID_STATE,
                severity = InteractionDiagnosticSeverity.ERROR,
                message = "Interaction command cannot transition from ${command.lifecycleState} to $nextState.",
                subject = command.actionIntent.subject,
                commandId = command.commandId,
                retryable = false,
            ),
        )
    }
}
