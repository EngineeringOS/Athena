package com.engineeringood.athena.authoring

import com.engineeringood.athena.component.EngineeringConceptTemplateId
import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.interaction.AuthoringCapabilityEvidence
import com.engineeringood.athena.interaction.AuthoringCapabilityRequirement
import com.engineeringood.athena.interaction.AuthoringCapabilityRequirementKind
import com.engineeringood.athena.interaction.AuthoringIntentKind
import com.engineeringood.athena.interaction.InteractionOriginSurface
import com.engineeringood.athena.interaction.InteractionSubjectKey
import com.engineeringood.athena.interaction.InteractionSubjectKind
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class SemanticAuthoringTransactionTest {
    @Test
    fun `revision guard hashes exact utf8 source deterministically`() {
        val first = AuthoringRevisionGuard.from(
            semanticSnapshotId = "snapshot:m31",
            sourceUri = "file:///workspace/main.athena",
            documentVersion = 7,
            sourceText = "system RollingShutter {\n}\n",
        )
        val second = AuthoringRevisionGuard.from(
            semanticSnapshotId = "snapshot:m31",
            sourceUri = "file:///workspace/main.athena",
            documentVersion = 7,
            sourceText = "system RollingShutter {\n}\n",
        )
        val changed = AuthoringRevisionGuard.from(
            semanticSnapshotId = "snapshot:m31",
            sourceUri = "file:///workspace/main.athena",
            documentVersion = 7,
            sourceText = "system RollingShutter {\n}\n ",
        )

        assertEquals(first, second)
        assertEquals("c325c9baf948575ecf93aea00d3da5771d0fc911d7d3af2330624142b376be56", first.contentSha256)
        assertNotEquals(first.contentSha256, changed.contentSha256)
    }

    @Test
    fun `factory creates one complete single intent transaction`() {
        val guard = revisionGuard()
        val intent = createIntent("intent:create:m31", guard)
        val preview = AuthoringPreview(
            previewId = AuthoringPreviewId("preview:create:m31"),
            intentId = intent.intentId,
            title = "Create motor",
            changes = emptyList(),
            revisionGuard = guard,
        )

        val result = SemanticAuthoringTransactionFactory.create(
            transactionId = SemanticAuthoringTransactionId("transaction:create:m31"),
            intents = listOf(intent),
            capabilityEvidence = capabilityEvidence(),
            revisionGuard = guard,
            preview = preview,
            provenance = AuthoringTransactionProvenance(
                actor = "user:Aaron",
                origin = intent.origin,
                reason = "customer authoring proof",
            ),
        )
        val transaction = assertIs<SemanticAuthoringTransactionCreated>(result).transaction

        assertEquals(intent, transaction.intent)
        assertEquals(guard, transaction.revisionGuard)
        assertEquals(preview, transaction.preview)
        assertEquals(AuthoringLifecycleState.PREVIEWING, transaction.lifecycleState)
        assertEquals("create-semantic-entity", transaction.capabilityEvidence.capabilityId)
        assertNull(transaction.mutationId)
        assertNull(transaction.result)
    }

    @Test
    fun `factory rejects empty and multi intent transactions`() {
        val empty = SemanticAuthoringTransactionFactory.create(
            transactionId = SemanticAuthoringTransactionId("transaction:empty"),
            intents = emptyList(),
            capabilityEvidence = capabilityEvidence(),
            revisionGuard = revisionGuard(),
            preview = null,
            provenance = provenance(),
        )
        val multiple = SemanticAuthoringTransactionFactory.create(
            transactionId = SemanticAuthoringTransactionId("transaction:multiple"),
            intents = listOf(createIntent("intent:1", revisionGuard()), createIntent("intent:2", revisionGuard())),
            capabilityEvidence = capabilityEvidence(),
            revisionGuard = revisionGuard(),
            preview = null,
            provenance = provenance(),
        )

        assertEquals(AuthoringDiagnosticCode.TRANSACTION_INTENT_COUNT_UNSUPPORTED, assertIs<SemanticAuthoringTransactionRejected>(empty).diagnostic.code)
        assertEquals(AuthoringDiagnosticCode.TRANSACTION_INTENT_COUNT_UNSUPPORTED, assertIs<SemanticAuthoringTransactionRejected>(multiple).diagnostic.code)
    }

    private fun createIntent(id: String, guard: AuthoringRevisionGuard): CreateSemanticEntityIntent {
        val origin = AuthoringOrigin(AuthoringSurface.GRAPH)
        return CreateSemanticEntityIntent(
            intentId = AuthoringIntentId(id),
            origin = origin,
            creationContext = SemanticEntityCreationContext(
                parentSubjectId = StableSemanticIdentity("system:RollingShutter"),
            ),
            conceptTemplateId = EngineeringConceptTemplateId("electrical.motor.ac.default"),
            conceptId = EngineeringConceptId("electrical.motor.ac"),
            suggestedName = "ShutterMotorM31",
            revisionGuard = guard,
            provenance = AuthoringTransactionProvenance("user:Aaron", origin),
        )
    }

    private fun revisionGuard(): AuthoringRevisionGuard = AuthoringRevisionGuard.from(
        semanticSnapshotId = "snapshot:m31",
        sourceUri = "file:///workspace/main.athena",
        documentVersion = 7,
        sourceText = "system RollingShutter {\n}\n",
    )

    private fun capabilityEvidence(): AuthoringCapabilityEvidence = AuthoringCapabilityEvidence(
        capabilityId = "create-semantic-entity",
        intentKind = AuthoringIntentKind.CREATE_ENTITY,
        subject = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("system:RollingShutter"),
            subjectKind = InteractionSubjectKind.WORKSPACE,
            sourceContextId = "file:///workspace/main.athena",
        ),
        actorOrigin = InteractionOriginSurface.GRAPH,
        satisfiedRequirements = listOf(
            AuthoringCapabilityRequirement(
                kind = AuthoringCapabilityRequirementKind.CONCEPT_TEMPLATE,
                identifier = "electrical.motor.ac",
                satisfied = true,
            ),
        ),
    )

    private fun provenance(): AuthoringTransactionProvenance = AuthoringTransactionProvenance(
        actor = "user:Aaron",
        origin = AuthoringOrigin(AuthoringSurface.GRAPH),
    )
}
