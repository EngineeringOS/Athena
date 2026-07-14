package com.engineeringood.athena.authoring

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthoringPreviewContractTest {
    @Test
    fun `preview stays tied to one authoring intent`() {
        val preview = AuthoringPreview(
            previewId = AuthoringPreviewId("preview:create-plc"),
            intentId = AuthoringIntentId("intent:create-plc"),
            title = "Add PLC1",
            changes = listOf(
                AuthoringPreviewChange(
                    kind = AuthoringPreviewChangeKind.CREATE,
                    title = "Create component PLC1",
                    affectedSubjectIdentities = setOf(StableSemanticIdentity("component:PLC1")),
                ),
            ),
        )

        assertEquals("preview:create-plc", preview.previewId.value)
        assertEquals("intent:create-plc", preview.intentId.value)
        assertEquals(AuthoringPreviewStatus.PENDING_REVIEW, preview.status)
        assertEquals(1, preview.changes.size)
    }

    @Test
    fun `preview consequences stay inspectable and canonical identity aware`() {
        val change = AuthoringPreviewChange(
            kind = AuthoringPreviewChangeKind.CONNECT,
            title = "Connect PLC1.MPI to HMI1.MPI",
            summary = "One compatible MPI connection will be added.",
            affectedSubjectIdentities = setOf(
                StableSemanticIdentity("port:PLC1.MPI"),
                StableSemanticIdentity("port:HMI1.MPI"),
                StableSemanticIdentity("connection:PLC1-MPI-HMI1-MPI"),
            ),
        )

        assertEquals(AuthoringPreviewChangeKind.CONNECT, change.kind)
        assertTrue(change.summary!!.contains("compatible MPI connection"))
        assertEquals(
            setOf("port:PLC1.MPI", "port:HMI1.MPI", "connection:PLC1-MPI-HMI1-MPI"),
            change.affectedSubjectIdentities.map { identity -> identity.value }.toSet(),
        )
    }

    @Test
    fun `acceptance and rejection decisions stay explicit and deterministic`() {
        val accept = AcceptAuthoringPreviewDecision(
            previewId = AuthoringPreviewId("preview:update-tag"),
            intentId = AuthoringIntentId("intent:update-tag"),
        )
        val reject = RejectAuthoringPreviewDecision(
            previewId = AuthoringPreviewId("preview:update-tag"),
            intentId = AuthoringIntentId("intent:update-tag"),
            reason = "Operator cancelled change.",
        )

        assertIs<AuthoringPreviewDecision>(accept)
        assertIs<AuthoringPreviewDecision>(reject)
        assertEquals(AuthoringPreviewDecisionKind.ACCEPT, accept.kind)
        assertEquals(AuthoringPreviewDecisionKind.REJECT, reject.kind)
        assertEquals("preview:update-tag", accept.previewId.value)
        assertEquals("preview:update-tag", reject.previewId.value)
        assertEquals("Operator cancelled change.", reject.reason)
    }
}
