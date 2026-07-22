package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AcceptAuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringIntentId
import com.engineeringood.athena.authoring.AuthoringDiagnosticAuthority
import com.engineeringood.athena.authoring.AuthoringDiagnosticCode
import com.engineeringood.athena.authoring.AuthoringLifecycleState
import com.engineeringood.athena.authoring.AuthoringOrigin
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.authoring.AuthoringSurface
import com.engineeringood.athena.authoring.AuthoringTransactionProvenance
import com.engineeringood.athena.authoring.AuthoringValidationStage
import com.engineeringood.athena.authoring.CancelAuthoringPreviewDecision
import com.engineeringood.athena.authoring.CreateSemanticEntityIntent
import com.engineeringood.athena.authoring.RejectAuthoringPreviewDecision
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionId
import com.engineeringood.athena.authoring.SemanticEntityCreationContext
import com.engineeringood.athena.compiler.BackendAuthoringSourceDocument
import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.component.EngineeringConceptTemplate
import com.engineeringood.athena.domain.electricalruntime.electricalEngineeringConceptTemplates
import com.engineeringood.athena.interaction.AuthoringCapabilityEvidence
import com.engineeringood.athena.interaction.AuthoringCapabilityRequirement
import com.engineeringood.athena.interaction.AuthoringCapabilityRequirementKind
import com.engineeringood.athena.interaction.AuthoringIntentKind
import com.engineeringood.athena.interaction.InteractionOriginSurface
import com.engineeringood.athena.interaction.InteractionSubjectKey
import com.engineeringood.athena.interaction.InteractionSubjectKind
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.ParseSuccess
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GovernedEntityCreationPreviewServiceTest {
    @Test
    fun `motor preview carries exact semantic source representation and composition evidence`() {
        val fixture = Fixture()

        val result = assertIs<GovernedEntityCreationPreviewReady>(fixture.preview())
        val evidence = result.transaction.preview?.entityCreationEvidence

        requireNotNull(evidence)
        assertEquals("ShutterMotorM31", evidence.canonicalTag)
        assertEquals("Motor", evidence.semanticType)
        assertEquals("MOTOR-AC", evidence.model)
        assertEquals(
            listOf(
                Triple("up", "in", "Digital"),
                Triple("down", "in", "Digital"),
                Triple("status", "out", "Digital"),
            ),
            evidence.nestedPorts.map { port ->
                Triple(port.name, port.direction.name.lowercase(), port.signalOrMedium.value)
            },
        )
        assertEquals(
            listOf(
                "component:ShutterMotorM31",
                "port:ShutterMotorM31.down",
                "port:ShutterMotorM31.status",
                "port:ShutterMotorM31.up",
            ),
            evidence.affectedSemanticIds,
        )
        assertEquals("iec.motor.compact", evidence.representationId)
        assertEquals("composition:alignment_group", evidence.compositionTargetId)
        assertEquals(fixture.document.revisionGuard, evidence.sourceEdit.revisionGuard)
        assertEquals(result.sourceEditPlan.admittedText, evidence.sourceEdit.admittedText)
        assertEquals(result.sourceEditPlan.replacement.startOffset, evidence.sourceEdit.startOffset)
        assertTrue(result.transaction.preview?.acceptanceEligibility?.eligible == true)
        assertTrue(result.transaction.preview?.acceptanceEligibility?.diagnostics.orEmpty().isEmpty())
    }

    @Test
    fun `reject and cancel preserve source and call no transaction authorities`() {
        listOf("reject", "cancel").forEach { decisionKind ->
            val fixture = Fixture()
            val ready = assertIs<GovernedEntityCreationPreviewReady>(fixture.preview())
            val preview = checkNotNull(ready.transaction.preview)
            val runtime = fixture.runtime(ready)
            val decision = when (decisionKind) {
                "reject" -> RejectAuthoringPreviewDecision(preview.previewId, preview.intentId, "Not needed")
                else -> CancelAuthoringPreviewDecision(preview.previewId, preview.intentId, "Cancelled")
            }

            val result = runtime.decide(ready.transaction, decision)

            assertEquals(
                if (decisionKind == "reject") AuthoringLifecycleState.REJECTED else AuthoringLifecycleState.CANCELLED,
                result.lifecycleState,
            )
            assertEquals(SOURCE, fixture.currentSource)
            assertEquals(0, fixture.validationCalls)
            assertEquals(0, fixture.revisionCalls)
            assertEquals(0, fixture.mutationCalls)
            assertEquals(0, fixture.reprojectionCalls)
        }
    }

    @Test
    fun `accepted motor traverses fixed validation order and hands exact plan to mutation authority`() {
        val fixture = Fixture()
        val ready = assertIs<GovernedEntityCreationPreviewReady>(fixture.preview())
        val preview = checkNotNull(ready.transaction.preview)

        val result = fixture.runtime(ready).decide(
            ready.transaction,
            AcceptAuthoringPreviewDecision(preview.previewId, preview.intentId),
        )

        assertEquals(AuthoringValidationStage.entries.toList(), fixture.seenStages)
        assertEquals(1, fixture.mutationCalls)
        assertEquals(1, fixture.reprojectionCalls)
        assertEquals(AuthoringLifecycleState.REPROJECTED, result.lifecycleState)
        assertEquals("mutation:create:ShutterMotorM31", result.mutationId)
        assertEquals(ready.sourceEditPlan.applyTo(SOURCE), fixture.currentSource)
        assertEquals(
            listOf(
                "component:ShutterMotorM31",
                "port:ShutterMotorM31.down",
                "port:ShutterMotorM31.status",
                "port:ShutterMotorM31.up",
            ),
            result.result?.affectedSemanticIds,
        )
        assertEquals(listOf("sheet:control/component:ShutterMotorM31"), result.result?.projectionOccurrenceIds)
    }

    @Test
    fun `governed preview blocks invalid semantic representation and composition inputs`() {
        val motorTemplate = motorTemplate()
        val cases = listOf(
            BlockedCase(
                name = "duplicate-tag",
                source = """system RollingShutter {
  device ShutterMotorM31 {
    type Motor
  }
}
""",
                templates = listOf(motorTemplate),
                expectedCode = AuthoringDiagnosticCode.ENTITY_TAG_DUPLICATE,
                expectedAuthority = AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
            ),
            BlockedCase(
                name = "missing-template",
                templates = emptyList(),
                expectedCode = AuthoringDiagnosticCode.CONCEPT_TEMPLATE_MISSING,
                expectedAuthority = AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
            ),
            BlockedCase(
                name = "duplicate-template-id",
                templates = listOf(motorTemplate, motorTemplate),
                expectedCode = AuthoringDiagnosticCode.CONCEPT_TEMPLATE_IDENTITY_MISMATCH,
                expectedAuthority = AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
            ),
            BlockedCase(
                name = "mismatched-template",
                templates = listOf(motorTemplate),
                intentTransform = { intent -> intent.copy(conceptId = EngineeringConceptId("electrical.motor.dc")) },
                expectedCode = AuthoringDiagnosticCode.CONCEPT_TEMPLATE_IDENTITY_MISMATCH,
                expectedAuthority = AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
            ),
            BlockedCase(
                name = "invalid-anatomy",
                templates = listOf(
                    motorTemplate.copy(
                        nestedPorts = emptyList(),
                        relationshipCapabilities = emptyList(),
                    ),
                ),
                expectedCode = AuthoringDiagnosticCode.NESTED_PORT_ANATOMY_INVALID,
                expectedAuthority = AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
            ),
            BlockedCase(
                name = "unresolved-representation",
                templates = listOf(motorTemplate),
                projectionResult = GovernedEntityCreationRepresentationUnresolved("No motor policy is active."),
                expectedCode = AuthoringDiagnosticCode.REPRESENTATION_UNRESOLVED,
                expectedAuthority = AuthoringDiagnosticAuthority.REPRESENTATION,
            ),
            BlockedCase(
                name = "unsatisfied-composition",
                templates = listOf(motorTemplate),
                projectionResult = GovernedEntityCreationCompositionUnsatisfied(
                    representationId = "iec.motor.compact",
                    reason = "No power-load composition target is active.",
                ),
                expectedCode = AuthoringDiagnosticCode.COMPOSITION_UNSATISFIED,
                expectedAuthority = AuthoringDiagnosticAuthority.COMPOSITION,
            ),
        )

        cases.forEach { case ->
            val document = sourceDocument(case.source)
            val intent = case.intentTransform(intent(document.revisionGuard))
            val blocked = assertIs<GovernedEntityCreationPreviewBlocked>(
                GovernedEntityCreationPreviewService(
                    templates = case.templates,
                    projectionAuthority = GovernedEntityCreationProjectionAuthority { _, _, _ -> case.projectionResult },
                ).preview(
                    GovernedEntityCreationPreviewRequest(
                        transactionId = SemanticAuthoringTransactionId("transaction:${case.name}"),
                        previewId = AuthoringPreviewId("preview:${case.name}"),
                        intent = intent,
                        capabilityEvidence = capabilityEvidence(),
                        document = document,
                    ),
                ),
                case.name,
            )
            val diagnostic = blocked.diagnostics.single()
            assertEquals(case.expectedCode, diagnostic.code, case.name)
            assertEquals(case.expectedAuthority, diagnostic.authority, case.name)
            assertTrue(blocked.transaction.preview?.acceptanceEligibility?.eligible == false, case.name)

            var mutationCalls = 0
            var reprojectionCalls = 0
            val result = SemanticAuthoringTransactionRuntime(
                validationAuthority = blocked.validationAuthority,
                revisionAuthority = ActiveAuthoringRevisionAuthority { document.revisionGuard },
                mutationAuthority = SemanticAuthoringMutationAuthority {
                    mutationCalls += 1
                    error("Blocked ${case.name} must not reach mutation authority.")
                },
                reprojectionAuthority = SemanticAuthoringReprojectionAuthority { _, _ ->
                    reprojectionCalls += 1
                    error("Blocked ${case.name} must not reach reprojection authority.")
                },
            ).decide(
                blocked.transaction,
                AcceptAuthoringPreviewDecision(case.previewId(), intent.intentId),
            )

            assertEquals(AuthoringLifecycleState.BLOCKED, result.lifecycleState, case.name)
            assertEquals(0, mutationCalls, case.name)
            assertEquals(0, reprojectionCalls, case.name)
        }
    }

    @Test
    fun `stale governed acceptance performs no mutation or reprojection`() {
        val fixture = Fixture()
        val ready = assertIs<GovernedEntityCreationPreviewReady>(fixture.preview())
        val preview = checkNotNull(ready.transaction.preview)
        fixture.currentSource += "// concurrent change\n"

        val result = fixture.runtime(ready).decide(
            ready.transaction,
            AcceptAuthoringPreviewDecision(preview.previewId, preview.intentId),
        )

        assertEquals(AuthoringLifecycleState.STALE, result.lifecycleState)
        assertEquals(AuthoringDiagnosticCode.REVISION_GUARD_MISMATCH, result.diagnostics.single().code)
        assertEquals(0, fixture.mutationCalls)
        assertEquals(0, fixture.reprojectionCalls)
    }

    private class Fixture {
        val document = sourceDocument()
        var currentSource = SOURCE
        val seenStages = mutableListOf<AuthoringValidationStage>()
        var validationCalls = 0
        var revisionCalls = 0
        var mutationCalls = 0
        var reprojectionCalls = 0

        fun preview(): GovernedEntityCreationPreviewResult = GovernedEntityCreationPreviewService(
            templates = electricalEngineeringConceptTemplates(),
            projectionAuthority = GovernedEntityCreationProjectionAuthority { _, template, canonicalTag ->
                assertEquals("electrical.motor.ac.default", template.templateId.value)
                assertEquals("ShutterMotorM31", canonicalTag)
                GovernedEntityCreationProjectionResolved(
                    representationId = "iec.motor.compact",
                    compositionTargetId = "composition:alignment_group",
                    projectionOccurrenceIds = listOf("sheet:control/component:ShutterMotorM31"),
                )
            },
        ).preview(
            GovernedEntityCreationPreviewRequest(
                transactionId = SemanticAuthoringTransactionId("transaction:create:ShutterMotorM31"),
                previewId = AuthoringPreviewId("preview:create:ShutterMotorM31"),
                intent = intent(document.revisionGuard),
                capabilityEvidence = capabilityEvidence(),
                document = document,
            ),
        )

        fun runtime(ready: GovernedEntityCreationPreviewReady): SemanticAuthoringTransactionRuntime =
            SemanticAuthoringTransactionRuntime(
                validationAuthority = AuthoringTransactionValidationAuthority { stage, transaction ->
                    validationCalls += 1
                    seenStages += stage
                    ready.validationAuthority.validate(stage, transaction)
                },
                revisionAuthority = ActiveAuthoringRevisionAuthority {
                    revisionCalls += 1
                    AuthoringRevisionGuard.from(
                        semanticSnapshotId = document.semanticSnapshotId,
                        sourceUri = document.sourceUri,
                        documentVersion = document.documentVersion,
                        sourceText = currentSource,
                    )
                },
                mutationAuthority = SemanticAuthoringMutationAuthority {
                    mutationCalls += 1
                    currentSource = ready.sourceEditPlan.applyTo(currentSource)
                    AuthoringMutationCommitted(
                        mutationId = "mutation:create:ShutterMotorM31",
                        committedRevision = AuthoringRevisionGuard.from(
                            semanticSnapshotId = "snapshot:m31:committed",
                            sourceUri = document.sourceUri,
                            documentVersion = document.documentVersion + 1,
                            sourceText = currentSource,
                        ),
                        affectedSemanticIds = checkNotNull(ready.transaction.preview)
                            .entityCreationEvidence
                            ?.affectedSemanticIds
                            .orEmpty(),
                    )
                },
                reprojectionAuthority = SemanticAuthoringReprojectionAuthority { _, _ ->
                    reprojectionCalls += 1
                    AuthoringReprojectionSucceeded(listOf("sheet:control/component:ShutterMotorM31"))
                },
            )
    }

    private data class BlockedCase(
        val name: String,
        val source: String = SOURCE,
        val templates: List<EngineeringConceptTemplate>,
        val intentTransform: (CreateSemanticEntityIntent) -> CreateSemanticEntityIntent = { intent -> intent },
        val projectionResult: GovernedEntityCreationProjectionResult = GovernedEntityCreationProjectionResolved(
            representationId = "iec.motor.compact",
            compositionTargetId = "composition:alignment_group",
            projectionOccurrenceIds = listOf("sheet:control/component:ShutterMotorM31"),
        ),
        val expectedCode: AuthoringDiagnosticCode,
        val expectedAuthority: AuthoringDiagnosticAuthority,
    ) {
        fun previewId(): AuthoringPreviewId = AuthoringPreviewId("preview:$name")
    }

    companion object {
        private const val SOURCE = """system RollingShutter {
}
"""
        private val ORIGIN = AuthoringOrigin(AuthoringSurface.GRAPH)

        private fun sourceDocument(source: String = SOURCE): BackendAuthoringSourceDocument {
            val parse = assertIs<ParseSuccess>(AthenaLanguageParser().parse("sample.athena", source))
            return BackendAuthoringSourceDocument(
                sourceUri = "file:///workspace/sample.athena",
                documentVersion = 7,
                semanticSnapshotId = "snapshot:m31",
                sourceText = source,
                ast = parse.ast,
            )
        }

        private fun motorTemplate(): EngineeringConceptTemplate =
            electricalEngineeringConceptTemplates().single { template ->
                template.templateId.value == "electrical.motor.ac.default"
            }

        private fun intent(revisionGuard: AuthoringRevisionGuard): CreateSemanticEntityIntent =
            CreateSemanticEntityIntent(
                intentId = AuthoringIntentId("intent:create:ShutterMotorM31"),
                origin = ORIGIN,
                creationContext = SemanticEntityCreationContext(StableSemanticIdentity("system:RollingShutter")),
                conceptTemplateId = electricalEngineeringConceptTemplates().single { template ->
                    template.templateId.value == "electrical.motor.ac.default"
                }.templateId,
                conceptId = electricalEngineeringConceptTemplates().single { template ->
                    template.templateId.value == "electrical.motor.ac.default"
                }.conceptId,
                suggestedName = "ShutterMotorM31",
                revisionGuard = revisionGuard,
                provenance = AuthoringTransactionProvenance("user:Aaron", ORIGIN),
            )

        private fun capabilityEvidence(): AuthoringCapabilityEvidence = AuthoringCapabilityEvidence(
            capabilityId = "create-semantic-entity",
            intentKind = AuthoringIntentKind.CREATE_ENTITY,
            subject = InteractionSubjectKey(
                canonicalSubjectId = StableSemanticIdentity("system:RollingShutter"),
                subjectKind = InteractionSubjectKind.WORKSPACE,
                sourceContextId = "file:///workspace/sample.athena",
            ),
            actorOrigin = InteractionOriginSurface.GRAPH,
            satisfiedRequirements = AuthoringCapabilityRequirementKind.entries.map { kind ->
                AuthoringCapabilityRequirement(kind, "test:${kind.name.lowercase()}", true)
            },
        )
    }
}
