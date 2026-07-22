package com.engineeringood.athena.authoring

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthoringLifecycleContractTest {
    @Test
    fun `diagnostic carries stable authority lifecycle and recovery evidence`() {
        val diagnostic = AuthoringDiagnostic(
            code = AuthoringDiagnosticCode.STOP_DOWNSTREAM,
            authority = AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
            lifecycleStage = AuthoringLifecycleState.BLOCKED,
            severity = AuthoringDiagnosticSeverity.ERROR,
            message = "Proposed source requested STOP_DOWNSTREAM.",
            subjectId = "system:RollingShutter",
            relatedIds = listOf("transaction:m31"),
            recoveryAction = AuthoringRecoveryAction.FIX_SOURCE,
        )

        assertEquals("authoring.validation.stop-downstream", diagnostic.code.value)
        assertEquals(AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION, diagnostic.authority)
        assertEquals(AuthoringLifecycleState.BLOCKED, diagnostic.lifecycleStage)
        assertEquals(AuthoringDiagnosticSeverity.ERROR, diagnostic.severity)
        assertEquals(AuthoringRecoveryAction.FIX_SOURCE, diagnostic.recoveryAction)
    }

    @Test
    fun `projection failed result requires committed mutation evidence`() {
        val guard = AuthoringRevisionGuard.from(
            semanticSnapshotId = "snapshot:committed",
            sourceUri = "file:///workspace/main.athena",
            documentVersion = 8,
            sourceText = "system RollingShutter {}",
        )
        val diagnostic = AuthoringDiagnostic(
            code = AuthoringDiagnosticCode.PROJECTION_FAILED_AFTER_COMMIT,
            authority = AuthoringDiagnosticAuthority.PROJECTION,
            lifecycleStage = AuthoringLifecycleState.PROJECTION_FAILED,
            severity = AuthoringDiagnosticSeverity.ERROR,
            message = "Projection failed after canonical mutation committed.",
            recoveryAction = AuthoringRecoveryAction.RETRY_PROJECTION,
        )

        val result = SemanticAuthoringResult(
            lifecycleState = AuthoringLifecycleState.PROJECTION_FAILED,
            committedRevision = guard,
            mutationId = "mutation:m31",
            affectedSemanticIds = listOf("component:ShutterMotorM31"),
            diagnostics = listOf(diagnostic),
        )

        assertEquals("mutation:m31", result.mutationId)
        assertEquals(guard, result.committedRevision)
        assertFailsWith<IllegalArgumentException> {
            SemanticAuthoringResult(
                lifecycleState = AuthoringLifecycleState.PROJECTION_FAILED,
                diagnostics = listOf(diagnostic),
            )
        }
    }
}
