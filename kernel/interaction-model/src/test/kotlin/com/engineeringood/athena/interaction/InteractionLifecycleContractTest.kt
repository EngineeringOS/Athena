package com.engineeringood.athena.interaction

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals

class InteractionLifecycleContractTest {
    @Test
    fun `runtime lifecycle accepts the legal command path`() {
        var command = commandAt(InteractionLifecycleState.REQUESTED)

        listOf(
            InteractionLifecycleState.DISCOVERED,
            InteractionLifecycleState.VALIDATED,
            InteractionLifecycleState.PREVIEWING,
            InteractionLifecycleState.ACCEPTED,
            InteractionLifecycleState.MUTATION_PENDING,
            InteractionLifecycleState.COMMITTED,
            InteractionLifecycleState.REPROJECTED,
        ).forEach { next ->
            command = InteractionLifecycle.transition(command, next)
            assertEquals(next, command.lifecycleState)
        }

        assertEquals(emptyList(), command.diagnostics)
    }

    @Test
    fun `runtime lifecycle rejects illegal transitions with stable diagnostic`() {
        val command = commandAt(InteractionLifecycleState.REQUESTED)

        val result = InteractionLifecycle.transition(command, InteractionLifecycleState.COMMITTED)

        assertEquals(InteractionLifecycleState.REQUESTED, result.lifecycleState)
        assertEquals(InteractionDiagnosticCode.COMMAND_INVALID_STATE, result.diagnostics.single().code)
        assertEquals(InteractionDiagnosticSeverity.ERROR, result.diagnostics.single().severity)
        assertEquals("command:lifecycle", result.diagnostics.single().commandId)
    }

    @Test
    fun `runtime lifecycle allows terminal stale cancelled rejected and blocked states`() {
        val command = commandAt(InteractionLifecycleState.PREVIEWING)

        listOf(
            InteractionLifecycleState.STALE,
            InteractionLifecycleState.CANCELLED,
            InteractionLifecycleState.REJECTED,
            InteractionLifecycleState.BLOCKED,
        ).forEach { terminal ->
            val result = InteractionLifecycle.transition(command, terminal)
            assertEquals(terminal, result.lifecycleState)
            assertEquals(emptyList(), result.diagnostics)
        }
    }

    private fun commandAt(state: InteractionLifecycleState): InteractionCommand {
        val subject = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("component:PLC1"),
            subjectKind = InteractionSubjectKind.COMPONENT,
        )
        return InteractionCommand(
            commandId = "command:lifecycle",
            actionIntent = SemanticActionIntent(
                actionIntentId = "action:lifecycle",
                actionFamily = InteractionActionFamily.REVEAL,
                subject = subject,
                requestedBy = InteractionProvenance(originSurface = InteractionOriginSurface.RUNTIME),
            ),
            lifecycleState = state,
            diagnostics = emptyList(),
            undoable = false,
        )
    }
}
