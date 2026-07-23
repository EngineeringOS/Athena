package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AcceptAuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringIntentId
import com.engineeringood.athena.authoring.AuthoringDiagnostic
import com.engineeringood.athena.authoring.AuthoringDiagnosticAuthority
import com.engineeringood.athena.authoring.AuthoringDiagnosticCode
import com.engineeringood.athena.authoring.AuthoringLifecycleState
import com.engineeringood.athena.authoring.AuthoringRecoveryAction
import com.engineeringood.athena.authoring.AuthoringOrigin
import com.engineeringood.athena.authoring.AuthoringPreviewStatus
import com.engineeringood.athena.authoring.AuthoringPreview
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.authoring.AuthoringSurface
import com.engineeringood.athena.authoring.AuthoringValidationStage
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionCreated
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionFactory
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionId
import com.engineeringood.athena.authoring.AuthoringTransactionProvenance
import com.engineeringood.athena.authoring.ElectricalConnectionRelationship
import com.engineeringood.athena.authoring.CreateSemanticEntityIntent
import com.engineeringood.athena.authoring.CancelAuthoringPreviewDecision
import com.engineeringood.athena.authoring.RejectAuthoringPreviewDecision
import com.engineeringood.athena.authoring.SemanticEntityCreationContext
import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.component.EngineeringConceptTemplateId
import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.compiler.BackendAuthoringSourceEditPlan
import com.engineeringood.athena.compiler.BackendAuthoringSourceOffsetRange
import com.engineeringood.athena.interaction.AuthoringCapabilityEvidence
import com.engineeringood.athena.interaction.AuthoringIntentKind
import com.engineeringood.athena.interaction.InteractionOriginSurface
import com.engineeringood.athena.interaction.InteractionSubjectKey
import com.engineeringood.athena.interaction.InteractionSubjectKind
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.part.PartImplementationId
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AthenaAuthoringSessionRuntimeServiceTest {
    @Test
    fun `submitting a guided component creation records preview state without semantic mutation`() {
        val sourcePath = writeProject(authoringFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "authoring-runtime",
                sourcePath = sourcePath,
            )
            val canonicalCompilation = context.compileActiveProject()

            val submitted = assertIs<AthenaAuthoringPreviewSubmitted>(
                context.authoringSessions().submit(
                    context = context,
                    intent = createEntityIntent(
                        intentId = AuthoringIntentId("intent-0001"),
                        templateId = "electrical.plc.cpu.default",
                        conceptId = "electrical.plc.cpu",
                        preferredImplementationId = PartImplementationId("electrical.plc.cpu.siemens.cpu313c"),
                        suggestedName = "PLC2",
                    ),
                ),
            )

            assertEquals("authoring-preview-0001", submitted.record.preview.previewId.value)
            assertEquals("intent-0001", submitted.record.preview.intentId.value)
            assertEquals(AuthoringPreviewStatus.PENDING_REVIEW, submitted.record.preview.status)
            assertEquals(
                listOf("component:PLC2", "system:AuthoringRuntime"),
                submitted.record.preview.changes.single().affectedSubjectIdentities.map { it.value }.sorted(),
            )
            assertSame(canonicalCompilation, context.compileActiveProject())
            assertTrue(context.commandRuntime().history(context).records.isEmpty())
            assertEquals(1, context.authoringSessions().state(context).pendingPreviewCount)
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `accepted guided preview updates runtime owned decision state without mutating canonical state`() {
        val sourcePath = writeProject(authoringFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "authoring-runtime",
                sourcePath = sourcePath,
            )
            val canonicalCompilation = context.compileActiveProject()
            val submitted = assertIs<AthenaAuthoringPreviewSubmitted>(
                context.authoringSessions().submit(
                    context = context,
                    intent = SemanticRelationshipIntent(
                        intentId = AuthoringIntentId("intent-0002"),
                        origin = AuthoringOrigin(AuthoringSurface.GRAPH),
                        relationshipType = ElectricalConnectionRelationship,
                        sourceSubjectId = StableSemanticIdentity("port:PLC1.out"),
                        targetSubjectId = StableSemanticIdentity("port:M1.in"),
                    ),
                ),
            )

            val updated = assertIs<AthenaAuthoringPreviewDecisionUpdated>(
                context.authoringSessions().applyDecision(
                    context = context,
                    decision = AcceptAuthoringPreviewDecision(
                        previewId = submitted.record.preview.previewId,
                        intentId = submitted.record.preview.intentId,
                        note = "Ready for later M8 handoff.",
                    ),
                ),
            )

            assertEquals(AuthoringPreviewStatus.ACCEPTED, updated.record.preview.status)
            assertSame(canonicalCompilation, context.compileActiveProject())
            assertTrue(context.commandRuntime().history(context).records.isEmpty())
            assertEquals(0, context.authoringSessions().state(context).pendingPreviewCount)

            val repeated = context.authoringSessions().applyDecision(
                context = context,
                decision = AcceptAuthoringPreviewDecision(
                    previewId = submitted.record.preview.previewId,
                    intentId = submitted.record.preview.intentId,
                ),
            )
            assertIs<AthenaAuthoringPreviewDecisionUnavailable>(repeated)
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `guided authoring preview state can be snapshotted and restored through runtime session state`() {
        val sourcePath = writeProject(authoringFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "authoring-runtime",
                sourcePath = sourcePath,
            )

            val submitted = assertIs<AthenaAuthoringPreviewSubmitted>(
                context.authoringSessions().submit(
                    context = context,
                    intent = createEntityIntent(
                        intentId = AuthoringIntentId("intent-0003"),
                        templateId = "electrical.power-supply.dc24.default",
                        conceptId = "electrical.power-supply.dc24",
                        suggestedName = "PS1",
                    ),
                ),
            )

            val snapshot = context.authoringSessions().snapshot(context)
            val restoredContext = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "authoring-runtime-restored",
                sourcePath = sourcePath,
            )
            val restoredService = AthenaAuthoringSessionRuntimeService()
            restoredService.restoreSession(restoredContext, snapshot)

            val restoredState = restoredService.state(restoredContext)
            assertEquals(1, restoredState.records.size)
            assertEquals(submitted.record.preview.previewId, restoredState.records.single().preview.previewId)
            assertEquals(1, restoredState.pendingPreviewCount)
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `preview session compatibility is explicitly versioned as read only legacy API`() {
        val contract = AthenaAuthoringSessionRuntimeService().compatibilityContract()

        assertEquals("legacy-preview-readonly-v1", contract.apiVersion)
        assertEquals(false, contract.mutableSourceAuthority)
        assertEquals(true, contract.acceptanceRequiresGovernedAuthorities)
        assertEquals(
            listOf("submit", "state", "snapshot", "restoreSession", "applyDecision"),
            contract.retainedMethods,
        )
    }

    @Test
    fun `governed acceptance executes the stored transaction exactly once`() {
        val sourcePath = writeProject(authoringFixture())

        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "authoring-runtime",
                sourcePath = sourcePath,
            )
            val intent = createEntityIntent(
                intentId = AuthoringIntentId("intent-governed"),
                templateId = "electrical.motor.ac.default",
                conceptId = "electrical.motor.ac",
                suggestedName = "Motor_31",
            )
            val submitted = assertIs<AthenaAuthoringPreviewSubmitted>(
                context.authoringSessions().submit(
                    context = context,
                    intent = intent,
                    governedPreviewFactory = { previewId -> governedContext(intent, previewId) },
                ),
            )
            val mutationCalls = AtomicInteger()
            var reprojectionCalls = 0
            val authorities = AthenaGovernedAuthoringDecisionAuthorities(
                revisionAuthority = ActiveAuthoringRevisionAuthority { intent.revisionGuard },
                mutationAuthority = SemanticAuthoringMutationAuthority {
                    mutationCalls.incrementAndGet()
                    AuthoringMutationCommitted(
                        mutationId = "mutation:governed",
                        committedRevision = intent.revisionGuard.copy(documentVersion = 2),
                        affectedSemanticIds = listOf("component:Motor_31"),
                    )
                },
                reprojectionAuthority = SemanticAuthoringReprojectionAuthority { _, _ ->
                    reprojectionCalls += 1
                    AuthoringReprojectionSucceeded(listOf("occurrence:Motor_31"))
                },
            )
            val decision = AcceptAuthoringPreviewDecision(
                previewId = submitted.record.preview.previewId,
                intentId = intent.intentId,
            )

            val accepted = assertIs<AthenaAuthoringPreviewDecisionUpdated>(
                context.authoringSessions().applyDecision(context, decision, authorities),
            )

            assertEquals(AuthoringPreviewStatus.ACCEPTED, accepted.record.preview.status)
            assertEquals("mutation:governed", accepted.transaction?.mutationId)
            assertEquals(listOf("occurrence:Motor_31"), accepted.transaction?.result?.projectionOccurrenceIds)
            assertEquals(1, mutationCalls.get())
            assertEquals(1, reprojectionCalls)
            assertIs<AthenaAuthoringPreviewDecisionUnavailable>(
                context.authoringSessions().applyDecision(context, decision, authorities),
            )
            assertEquals(1, mutationCalls.get())
            assertEquals(1, reprojectionCalls)
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `governed mutation rejection remains blocked and skips reprojection`() {
        val sourcePath = writeProject(authoringFixture())
        try {
            val context = AthenaRuntime().openWorkspace(sourcePath.parent).activateProject(
                projectName = "authoring-runtime",
                sourcePath = sourcePath,
            )
            val intent = createEntityIntent(
                intentId = AuthoringIntentId("intent-mutation-blocked"),
                templateId = "electrical.motor.ac.default",
                conceptId = "electrical.motor.ac",
                suggestedName = "MotorBlocked",
            )
            val submitted = assertIs<AthenaAuthoringPreviewSubmitted>(
                context.authoringSessions().submit(
                    context = context,
                    intent = intent,
                    governedPreviewFactory = { previewId -> governedContext(intent, previewId) },
                ),
            )
            var reprojectionCalls = 0
            val authorities = AthenaGovernedAuthoringDecisionAuthorities(
                revisionAuthority = ActiveAuthoringRevisionAuthority { intent.revisionGuard },
                mutationAuthority = SemanticAuthoringMutationAuthority {
                    AuthoringMutationBlocked(
                        AuthoringDiagnostic(
                            code = AuthoringDiagnosticCode.SOURCE_CONFLICT,
                            message = "The authoritative source mutation was rejected.",
                            authority = AuthoringDiagnosticAuthority.MUTATION_AUTHORITY,
                            lifecycleStage = AuthoringLifecycleState.BLOCKED,
                            recoveryAction = AuthoringRecoveryAction.REFRESH_PREVIEW,
                        ),
                    )
                },
                reprojectionAuthority = SemanticAuthoringReprojectionAuthority { _, _ ->
                    reprojectionCalls += 1
                    AuthoringReprojectionSucceeded()
                },
            )

            val result = assertIs<AthenaAuthoringPreviewDecisionUpdated>(
                context.authoringSessions().applyDecision(
                    context,
                    AcceptAuthoringPreviewDecision(submitted.record.preview.previewId, intent.intentId),
                    authorities,
                ),
            )

            assertEquals(AuthoringPreviewStatus.BLOCKED, result.record.preview.status)
            assertEquals(AuthoringLifecycleState.BLOCKED, result.transaction?.lifecycleState)
            assertEquals(0, reprojectionCalls)
            assertEquals(AuthoringDiagnosticCode.SOURCE_CONFLICT, result.transaction?.diagnostics?.single()?.code)
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `stale governed acceptance preserves stale preview status`() {
        val sourcePath = writeProject(authoringFixture())
        try {
            val context = AthenaRuntime().openWorkspace(sourcePath.parent).activateProject(
                projectName = "authoring-runtime",
                sourcePath = sourcePath,
            )
            val intent = createEntityIntent(
                intentId = AuthoringIntentId("intent-stale-status"),
                templateId = "electrical.motor.ac.default",
                conceptId = "electrical.motor.ac",
                suggestedName = "MotorStale",
            )
            val submitted = assertIs<AthenaAuthoringPreviewSubmitted>(
                context.authoringSessions().submit(
                    context = context,
                    intent = intent,
                    governedPreviewFactory = { previewId -> governedContext(intent, previewId) },
                ),
            )
            val authorities = AthenaGovernedAuthoringDecisionAuthorities(
                revisionAuthority = ActiveAuthoringRevisionAuthority {
                    intent.revisionGuard.copy(documentVersion = intent.revisionGuard.documentVersion + 1)
                },
                mutationAuthority = SemanticAuthoringMutationAuthority {
                    error("stale acceptance must not reach mutation authority")
                },
                reprojectionAuthority = SemanticAuthoringReprojectionAuthority { _, _ ->
                    error("stale acceptance must not reach reprojection authority")
                },
            )

            val result = assertIs<AthenaAuthoringPreviewDecisionUpdated>(
                context.authoringSessions().applyDecision(
                    context,
                    AcceptAuthoringPreviewDecision(submitted.record.preview.previewId, intent.intentId),
                    authorities,
                ),
            )

            assertEquals(AuthoringPreviewStatus.STALE, result.record.preview.status)
            assertEquals(AuthoringLifecycleState.STALE, result.transaction?.lifecycleState)
        } finally {
            Files.deleteIfExists(sourcePath)
        }
    }

    @Test
    fun `governed reject and cancel do not require mutation authorities`() {
        listOf("reject", "cancel").forEach { decisionKind ->
            val sourcePath = writeProject(authoringFixture())
            try {
                val context = AthenaRuntime().openWorkspace(sourcePath.parent).activateProject(
                    projectName = "authoring-runtime-$decisionKind",
                    sourcePath = sourcePath,
                )
                val intent = createEntityIntent(
                    intentId = AuthoringIntentId("intent-$decisionKind-no-authority"),
                    templateId = "electrical.motor.ac.default",
                    conceptId = "electrical.motor.ac",
                    suggestedName = "Motor${decisionKind.replaceFirstChar(Char::uppercase)}",
                )
                val submitted = assertIs<AthenaAuthoringPreviewSubmitted>(
                    context.authoringSessions().submit(
                        context = context,
                        intent = intent,
                        governedPreviewFactory = { previewId -> governedContext(intent, previewId) },
                    ),
                )
                val decision = if (decisionKind == "reject") {
                    RejectAuthoringPreviewDecision(submitted.record.preview.previewId, intent.intentId)
                } else {
                    CancelAuthoringPreviewDecision(submitted.record.preview.previewId, intent.intentId)
                }

                val result = assertIs<AthenaAuthoringPreviewDecisionUpdated>(
                    context.authoringSessions().applyDecision(context, decision),
                )

                assertEquals(
                    if (decisionKind == "reject") AuthoringPreviewStatus.REJECTED else AuthoringPreviewStatus.CANCELLED,
                    result.record.preview.status,
                )
            } finally {
                Files.deleteIfExists(sourcePath)
            }
        }
    }

    @Test
    fun `concurrent governed accepts commit only once`() {
        val sourcePath = writeProject(authoringFixture())
        val executor = Executors.newFixedThreadPool(2)
        try {
            val runtime = AthenaRuntime()
            val context = runtime.openWorkspace(sourcePath.parent).activateProject(
                projectName = "authoring-runtime",
                sourcePath = sourcePath,
            )
            val intent = createEntityIntent(
                intentId = AuthoringIntentId("intent-concurrent"),
                templateId = "electrical.motor.ac.default",
                conceptId = "electrical.motor.ac",
                suggestedName = "MotorConcurrent",
            )
            val submitted = assertIs<AthenaAuthoringPreviewSubmitted>(
                context.authoringSessions().submit(
                    context = context,
                    intent = intent,
                    governedPreviewFactory = { previewId -> governedContext(intent, previewId) },
                ),
            )
            val startGate = CountDownLatch(1)
            val mutationBarrier = CountDownLatch(2)
            val mutationCalls = AtomicInteger()
            val authorities = AthenaGovernedAuthoringDecisionAuthorities(
                revisionAuthority = ActiveAuthoringRevisionAuthority { intent.revisionGuard },
                mutationAuthority = SemanticAuthoringMutationAuthority {
                    mutationCalls.incrementAndGet()
                    mutationBarrier.countDown()
                    mutationBarrier.await(1, TimeUnit.SECONDS)
                    AuthoringMutationCommitted("mutation:concurrent", intent.revisionGuard, listOf("component:MotorConcurrent"))
                },
                reprojectionAuthority = SemanticAuthoringReprojectionAuthority { _, _ -> AuthoringReprojectionSucceeded() },
            )
            val decision = AcceptAuthoringPreviewDecision(
                previewId = submitted.record.preview.previewId,
                intentId = intent.intentId,
            )

            val first = executor.submit<AthenaAuthoringPreviewDecisionResult> {
                startGate.await(5, TimeUnit.SECONDS)
                context.authoringSessions().applyDecision(context, decision, authorities)
            }
            val second = executor.submit<AthenaAuthoringPreviewDecisionResult> {
                startGate.await(5, TimeUnit.SECONDS)
                context.authoringSessions().applyDecision(context, decision, authorities)
            }
            startGate.countDown()
            val results = listOf(first.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS))

            assertEquals(1, mutationCalls.get())
            assertEquals(1, results.count { result -> result is AthenaAuthoringPreviewDecisionUpdated })
            assertEquals(1, results.count { result -> result is AthenaAuthoringPreviewDecisionUnavailable })
        } finally {
            executor.shutdownNow()
            Files.deleteIfExists(sourcePath)
        }
    }

    private fun writeProject(source: String): Path {
        val path = Files.createTempFile("athena-authoring-session-", ".athena")
        Files.writeString(path, source)
        return path
    }

    private fun createEntityIntent(
        intentId: AuthoringIntentId,
        templateId: String,
        conceptId: String,
        preferredImplementationId: PartImplementationId? = null,
        suggestedName: String? = null,
    ): CreateSemanticEntityIntent {
        val origin = AuthoringOrigin(AuthoringSurface.PALETTE, detail = "components")
        val guard = AuthoringRevisionGuard.from(
            semanticSnapshotId = "snapshot:authoring-runtime",
            sourceUri = "file:///authoring-runtime.athena",
            documentVersion = 1,
            sourceText = authoringFixture(),
        )
        return CreateSemanticEntityIntent(
            intentId = intentId,
            origin = origin,
            creationContext = SemanticEntityCreationContext(
                parentSubjectId = StableSemanticIdentity("system:AuthoringRuntime"),
            ),
            conceptTemplateId = EngineeringConceptTemplateId(templateId),
            conceptId = EngineeringConceptId(conceptId),
            preferredImplementationId = preferredImplementationId,
            suggestedName = suggestedName,
            revisionGuard = guard,
            provenance = AuthoringTransactionProvenance("user:test", origin),
        )
    }

    private fun governedContext(
        intent: CreateSemanticEntityIntent,
        previewId: AuthoringPreviewId,
    ): AthenaGovernedAuthoringPreviewContext {
        val preview = AuthoringPreview(
            previewId = previewId,
            intentId = intent.intentId,
            title = "Create Motor_31",
            changes = emptyList(),
            revisionGuard = intent.revisionGuard,
        )
        val transaction = assertIs<SemanticAuthoringTransactionCreated>(
            SemanticAuthoringTransactionFactory.create(
                transactionId = SemanticAuthoringTransactionId("transaction:${intent.intentId.value}"),
                intents = listOf(intent),
                capabilityEvidence = AuthoringCapabilityEvidence(
                    capabilityId = "create-semantic-entity",
                    intentKind = AuthoringIntentKind.CREATE_ENTITY,
                    subject = InteractionSubjectKey(
                        canonicalSubjectId = intent.creationContext.parentSubjectId,
                        subjectKind = InteractionSubjectKind.WORKSPACE,
                        sourceContextId = intent.revisionGuard.sourceUri,
                    ),
                    actorOrigin = InteractionOriginSurface.GRAPH,
                    satisfiedRequirements = emptyList(),
                ),
                revisionGuard = intent.revisionGuard,
                preview = preview,
                provenance = intent.provenance,
            ),
        ).transaction
        return AthenaGovernedAuthoringPreviewContext(
            transaction = transaction,
            validationAuthority = AuthoringTransactionValidationAuthority { _: AuthoringValidationStage, _ ->
                AuthoringStageValidationPassed
            },
            sourceEditPlan = BackendAuthoringSourceEditPlan(
                revisionGuard = intent.revisionGuard,
                sourceUri = intent.revisionGuard.sourceUri,
                replacement = BackendAuthoringSourceOffsetRange(0, 0),
                admittedText = "",
                affectedSemanticIds = listOf("component:Motor_31"),
            ),
        )
    }

    private fun authoringFixture(): String {
        return """
            system AuthoringRuntime {
              device PLC1 {
                type Switch
              }

              device M1 {
                type Motor
              }

              port PLC1.out {
                direction out
                signal Digital
              }

              port M1.in {
                direction in
                signal Digital
              }

              connect PLC1.out -> M1.in
            }
        """.trimIndent()
    }
}
