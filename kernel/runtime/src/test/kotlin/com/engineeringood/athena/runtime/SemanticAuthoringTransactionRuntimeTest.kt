package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AcceptAuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringDiagnostic
import com.engineeringood.athena.authoring.AuthoringDiagnosticAuthority
import com.engineeringood.athena.authoring.AuthoringDiagnosticCode
import com.engineeringood.athena.authoring.AuthoringDiagnosticSeverity
import com.engineeringood.athena.authoring.AuthoringLifecycleState
import com.engineeringood.athena.authoring.AuthoringOrigin
import com.engineeringood.athena.authoring.AuthoringPreview
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.AuthoringRecoveryAction
import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.authoring.AuthoringSurface
import com.engineeringood.athena.authoring.AuthoringTransactionProvenance
import com.engineeringood.athena.authoring.AuthoringTransactionValidationStatus
import com.engineeringood.athena.authoring.AuthoringValidationStage
import com.engineeringood.athena.authoring.CancelAuthoringPreviewDecision
import com.engineeringood.athena.authoring.CreateSemanticEntityIntent
import com.engineeringood.athena.authoring.SemanticEntityCreationContext
import com.engineeringood.athena.authoring.RejectAuthoringPreviewDecision
import com.engineeringood.athena.authoring.SemanticAuthoringTransaction
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionCreated
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionFactory
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionId
import com.engineeringood.athena.authoring.AuthoringIntentId
import com.engineeringood.athena.component.EngineeringConceptTemplateId
import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.interaction.AuthoringCapabilityEvidence
import com.engineeringood.athena.interaction.AuthoringIntentKind
import com.engineeringood.athena.interaction.InteractionOriginSurface
import com.engineeringood.athena.interaction.InteractionSubjectKey
import com.engineeringood.athena.interaction.InteractionSubjectKind
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SemanticAuthoringTransactionRuntimeTest {
    @Test
    fun `accept validates every stage in fixed order then commits and reprojects`() {
        val fixture = Fixture()

        val result = fixture.runtime().decide(fixture.transaction(), fixture.acceptDecision())

        assertEquals(AuthoringValidationStage.entries.toList(), fixture.seenStages)
        assertEquals(AuthoringTransactionValidationStatus.VALID, result.validation.status)
        assertEquals(AuthoringValidationStage.entries.toList(), result.validation.completedStages)
        assertEquals(AuthoringLifecycleState.REPROJECTED, result.lifecycleState)
        assertEquals("mutation:m31", result.mutationId)
        assertEquals(1, fixture.mutationCalls)
        assertEquals(1, fixture.reprojectionCalls)
    }

    @Test
    fun `stale revision stops before mutation and reprojection`() {
        val fixture = Fixture(
            activeRevision = revisionGuard(snapshot = "snapshot:changed", version = 8),
        )

        val result = fixture.runtime().decide(fixture.transaction(), fixture.acceptDecision())

        assertEquals(
            listOf(
                AuthoringValidationStage.INTENT_SHAPE,
                AuthoringValidationStage.CAPABILITY_EVIDENCE,
                AuthoringValidationStage.ACTOR_SUBJECT_ELIGIBILITY,
            ),
            fixture.seenStages,
        )
        assertEquals(AuthoringLifecycleState.STALE, result.lifecycleState)
        assertEquals(AuthoringDiagnosticCode.REVISION_GUARD_MISMATCH, result.diagnostics.single().code)
        assertEquals(0, fixture.mutationCalls)
        assertEquals(0, fixture.reprojectionCalls)
    }

    @Test
    fun `stop downstream remains a blocked semantic validation outcome`() {
        val diagnostic = AuthoringDiagnostic(
            code = AuthoringDiagnosticCode.STOP_DOWNSTREAM,
            authority = AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
            lifecycleStage = AuthoringLifecycleState.BLOCKED,
            severity = AuthoringDiagnosticSeverity.ERROR,
            message = "Proposed source requested STOP_DOWNSTREAM.",
            recoveryAction = AuthoringRecoveryAction.FIX_SOURCE,
        )
        val fixture = Fixture(
            blockedStage = AuthoringValidationStage.SEMANTIC_VALIDATION,
            blockedDiagnostic = diagnostic,
        )

        val result = fixture.runtime().decide(fixture.transaction(), fixture.acceptDecision())

        assertEquals(AuthoringLifecycleState.BLOCKED, result.lifecycleState)
        assertEquals(AuthoringDiagnosticCode.STOP_DOWNSTREAM, result.diagnostics.single().code)
        assertEquals(0, fixture.mutationCalls)
        assertEquals(0, fixture.reprojectionCalls)
    }

    @Test
    fun `projection failure preserves committed mutation evidence`() {
        val diagnostic = AuthoringDiagnostic(
            code = AuthoringDiagnosticCode.PROJECTION_FAILED_AFTER_COMMIT,
            authority = AuthoringDiagnosticAuthority.PROJECTION,
            lifecycleStage = AuthoringLifecycleState.PROJECTION_FAILED,
            severity = AuthoringDiagnosticSeverity.ERROR,
            message = "Projection failed after commit.",
            recoveryAction = AuthoringRecoveryAction.RETRY_PROJECTION,
        )
        val fixture = Fixture(projectionDiagnostic = diagnostic)

        val result = fixture.runtime().decide(fixture.transaction(), fixture.acceptDecision())

        assertEquals(AuthoringLifecycleState.PROJECTION_FAILED, result.lifecycleState)
        assertEquals("mutation:m31", result.mutationId)
        assertEquals(fixture.committedRevision, result.result?.committedRevision)
        assertEquals(AuthoringDiagnosticCode.PROJECTION_FAILED_AFTER_COMMIT, result.diagnostics.single().code)
        assertEquals(1, fixture.mutationCalls)
        assertEquals(1, fixture.reprojectionCalls)
    }

    @Test
    fun `reject and cancel never invoke validation mutation or reprojection`() {
        val rejectedFixture = Fixture()
        val cancelledFixture = Fixture()
        val rejectedTransaction = rejectedFixture.transaction()
        val cancelledTransaction = cancelledFixture.transaction()

        val rejected = rejectedFixture.runtime().decide(
            rejectedTransaction,
            RejectAuthoringPreviewDecision(
                previewId = checkNotNull(rejectedTransaction.preview).previewId,
                intentId = rejectedTransaction.intent.intentId,
                reason = "Not required.",
            ),
        )
        val cancelled = cancelledFixture.runtime().decide(
            cancelledTransaction,
            CancelAuthoringPreviewDecision(
                previewId = checkNotNull(cancelledTransaction.preview).previewId,
                intentId = cancelledTransaction.intent.intentId,
                reason = "User cancelled.",
            ),
        )

        assertEquals(AuthoringLifecycleState.REJECTED, rejected.lifecycleState)
        assertEquals(AuthoringLifecycleState.CANCELLED, cancelled.lifecycleState)
        assertEquals(0, rejectedFixture.totalAuthorityCalls())
        assertEquals(0, cancelledFixture.totalAuthorityCalls())
    }

    private class Fixture(
        private val activeRevision: AuthoringRevisionGuard = revisionGuard(),
        private val blockedStage: AuthoringValidationStage? = null,
        private val blockedDiagnostic: AuthoringDiagnostic? = null,
        private val projectionDiagnostic: AuthoringDiagnostic? = null,
    ) {
        val seenStages = mutableListOf<AuthoringValidationStage>()
        var revisionCalls = 0
        var mutationCalls = 0
        var reprojectionCalls = 0
        val committedRevision = revisionGuard(snapshot = "snapshot:committed", version = 8)

        fun runtime(): SemanticAuthoringTransactionRuntime = SemanticAuthoringTransactionRuntime(
            validationAuthority = AuthoringTransactionValidationAuthority { stage, _ ->
                seenStages += stage
                if (stage == blockedStage) {
                    AuthoringStageValidationBlocked(checkNotNull(blockedDiagnostic))
                } else {
                    AuthoringStageValidationPassed
                }
            },
            revisionAuthority = ActiveAuthoringRevisionAuthority {
                revisionCalls += 1
                activeRevision
            },
            mutationAuthority = SemanticAuthoringMutationAuthority {
                mutationCalls += 1
                AuthoringMutationCommitted(
                    mutationId = "mutation:m31",
                    committedRevision = committedRevision,
                    affectedSemanticIds = listOf("component:ShutterMotorM31"),
                )
            },
            reprojectionAuthority = SemanticAuthoringReprojectionAuthority { _, _ ->
                reprojectionCalls += 1
                projectionDiagnostic?.let { AuthoringReprojectionFailed(listOf(it)) }
                    ?: AuthoringReprojectionSucceeded(listOf("occurrence:ShutterMotorM31"))
            },
        )

        fun transaction(): SemanticAuthoringTransaction {
            val origin = AuthoringOrigin(AuthoringSurface.GRAPH)
            val guard = revisionGuard()
            val intent = CreateSemanticEntityIntent(
                intentId = AuthoringIntentId("intent:create:m31"),
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
            val preview = AuthoringPreview(
                previewId = AuthoringPreviewId("preview:create:m31"),
                intentId = intent.intentId,
                title = "Create motor",
                changes = emptyList(),
                revisionGuard = guard,
            )
            return assertIs<SemanticAuthoringTransactionCreated>(
                SemanticAuthoringTransactionFactory.create(
                    transactionId = SemanticAuthoringTransactionId("transaction:create:m31"),
                    intents = listOf(intent),
                    capabilityEvidence = AuthoringCapabilityEvidence(
                        capabilityId = "create-semantic-entity",
                        intentKind = AuthoringIntentKind.CREATE_ENTITY,
                        subject = InteractionSubjectKey(
                            canonicalSubjectId = StableSemanticIdentity("system:RollingShutter"),
                            subjectKind = InteractionSubjectKind.WORKSPACE,
                            sourceContextId = "file:///workspace/main.athena",
                        ),
                        actorOrigin = InteractionOriginSurface.GRAPH,
                        satisfiedRequirements = emptyList(),
                    ),
                    revisionGuard = guard,
                    preview = preview,
                    provenance = AuthoringTransactionProvenance(
                        actor = "user:Aaron",
                        origin = intent.origin,
                    ),
                ),
            ).transaction
        }

        fun acceptDecision(): AcceptAuthoringPreviewDecision {
            val transaction = transaction()
            return AcceptAuthoringPreviewDecision(
                previewId = checkNotNull(transaction.preview).previewId,
                intentId = transaction.intent.intentId,
            )
        }

        fun totalAuthorityCalls(): Int = seenStages.size + revisionCalls + mutationCalls + reprojectionCalls
    }

    companion object {
        private fun revisionGuard(
            snapshot: String = "snapshot:m31",
            version: Int = 7,
        ): AuthoringRevisionGuard = AuthoringRevisionGuard.from(
            semanticSnapshotId = snapshot,
            sourceUri = "file:///workspace/main.athena",
            documentVersion = version,
            sourceText = "system RollingShutter {}",
        )
    }
}
