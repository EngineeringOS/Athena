package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AcceptAuthoringPreviewDecision
import com.engineeringood.athena.authoring.AuthoringDiagnosticCode
import com.engineeringood.athena.authoring.AuthoringIntentId
import com.engineeringood.athena.authoring.AuthoringLifecycleState
import com.engineeringood.athena.authoring.AuthoringOrigin
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.AuthoringRelationshipCompatibility
import com.engineeringood.athena.authoring.AuthoringRelationshipRoutePreviewEvidence
import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.authoring.AuthoringSurface
import com.engineeringood.athena.authoring.AuthoringTransactionProvenance
import com.engineeringood.athena.authoring.AuthoringValidationStage
import com.engineeringood.athena.authoring.CancelAuthoringPreviewDecision
import com.engineeringood.athena.authoring.ElectricalConnectionRelationship
import com.engineeringood.athena.authoring.RejectAuthoringPreviewDecision
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionId
import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.authoring.SemanticRelationshipPersistenceTarget
import com.engineeringood.athena.authoring.SemanticRelationshipType
import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.BackendAuthoringSourceDocument
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
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
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GovernedRelationshipPreviewServiceTest {
    @Test
    fun `relationship preview carries canonical compatibility source and route evidence`() {
        val fixture = Fixture()

        val ready = assertIs<GovernedRelationshipPreviewReady>(fixture.preview())
        val evidence = checkNotNull(ready.transaction.preview?.relationshipEvidence)

        assertEquals("port:Source.out", evidence.sourceSubjectId)
        assertEquals("port:Load.in", evidence.targetSubjectId)
        assertEquals(ElectricalConnectionRelationship, evidence.relationshipType)
        assertEquals(AuthoringRelationshipCompatibility.COMPATIBLE, evidence.compatibility)
        assertEquals(listOf("connection:Source.out->Load.in"), evidence.affectedSemanticIds)
        val sourceEdit = checkNotNull(evidence.sourceEdit)
        assertEquals(fixture.document.revisionGuard, sourceEdit.revisionGuard)
        assertEquals(ready.sourceEditPlan.admittedText, sourceEdit.admittedText)
        assertEquals(
            AuthoringRelationshipRoutePreviewEvidence(
                routeId = "route:connection:Source.out->Load.in",
                quality = "SATISFIED",
                sourceAnchorId = "anchor:port:Source.out",
                targetAnchorId = "anchor:port:Load.in",
                pointCount = 4,
            ),
            evidence.routePreview,
        )
        assertTrue(ready.transaction.preview?.acceptanceEligibility?.eligible == true)
    }

    @Test
    fun `route preview does not participate in semantic compatibility`() {
        val withRoute = assertIs<GovernedRelationshipPreviewReady>(Fixture().preview()).transaction.preview?.relationshipEvidence
        val withoutRoute = assertIs<GovernedRelationshipPreviewReady>(
            Fixture(routePreview = null).preview(),
        ).transaction.preview?.relationshipEvidence

        assertEquals(AuthoringRelationshipCompatibility.COMPATIBLE, withRoute?.compatibility)
        assertEquals(AuthoringRelationshipCompatibility.COMPATIBLE, withoutRoute?.compatibility)
        assertEquals(withRoute?.affectedSemanticIds, withoutRoute?.affectedSemanticIds)
        assertEquals(null, withoutRoute?.routePreview)
    }

    @Test
    fun `reject and cancel preserve source and call no transaction authorities`() {
        listOf("reject", "cancel").forEach { decisionKind ->
            val fixture = Fixture()
            val ready = assertIs<GovernedRelationshipPreviewReady>(fixture.preview())
            val preview = checkNotNull(ready.transaction.preview)
            val decision = if (decisionKind == "reject") {
                RejectAuthoringPreviewDecision(preview.previewId, preview.intentId)
            } else {
                CancelAuthoringPreviewDecision(preview.previewId, preview.intentId)
            }

            val result = fixture.runtime(ready).decide(ready.transaction, decision)

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
    fun `accepted relationship traverses validation and returns canonical relationship evidence`() {
        val fixture = Fixture()
        val ready = assertIs<GovernedRelationshipPreviewReady>(fixture.preview())
        val preview = checkNotNull(ready.transaction.preview)

        val result = fixture.runtime(ready).decide(
            ready.transaction,
            AcceptAuthoringPreviewDecision(preview.previewId, preview.intentId),
        )

        assertEquals(AuthoringValidationStage.entries.toList(), fixture.seenStages)
        assertEquals(1, fixture.mutationCalls)
        assertEquals(1, fixture.reprojectionCalls)
        assertEquals(AuthoringLifecycleState.REPROJECTED, result.lifecycleState)
        assertEquals("mutation:connection:Source.out->Load.in", result.mutationId)
        assertEquals(listOf("connection:Source.out->Load.in"), result.result?.affectedSemanticIds)
        assertEquals(listOf("route:connection:Source.out->Load.in"), result.result?.projectionOccurrenceIds)
        val compiled = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(Path.of("sample.athena"), fixture.currentSource),
        )
        assertEquals("connection:Source.out->Load.in", compiled.document.connections.single().id.value)
    }

    @Test
    fun `incompatible relationship is blocked with stable diagnostic and zero writes`() {
        val fixture = Fixture(intent = intent("port:Source.out", "port:Other.out"))
        val blocked = assertIs<GovernedRelationshipPreviewBlocked>(fixture.preview())
        val evidence = checkNotNull(blocked.transaction.preview?.relationshipEvidence)

        assertEquals(AuthoringRelationshipCompatibility.INCOMPATIBLE, evidence.compatibility)
        assertEquals(AuthoringDiagnosticCode.RELATIONSHIP_INCOMPATIBLE, blocked.diagnostics.single().code)
        assertTrue(blocked.transaction.preview?.acceptanceEligibility?.eligible == false)

        val preview = checkNotNull(blocked.transaction.preview)
        val result = fixture.runtime(blocked).decide(
            blocked.transaction,
            AcceptAuthoringPreviewDecision(preview.previewId, preview.intentId),
        )

        assertEquals(AuthoringLifecycleState.BLOCKED, result.lifecycleState)
        assertEquals(SOURCE, fixture.currentSource)
        assertEquals(0, fixture.mutationCalls)
        assertEquals(0, fixture.reprojectionCalls)
    }

    @Test
    fun `unresolved self and duplicate relationships fail with distinct structured diagnostics`() {
        val unresolved = assertIs<GovernedRelationshipPreviewBlocked>(
            Fixture(intent = intent(target = "port:Missing.in")).preview(),
        )
        assertEquals("authoring.relationship.subject-unresolved", unresolved.diagnostics.single().code.value)
        assertEquals(
            AuthoringRelationshipCompatibility.NOT_EVALUATED,
            unresolved.transaction.preview?.relationshipEvidence?.compatibility,
        )

        val self = assertIs<GovernedRelationshipPreviewBlocked>(
            Fixture(intent = intent(target = "port:Source.out")).preview(),
        )
        assertEquals("authoring.relationship.self", self.diagnostics.single().code.value)
        assertEquals(
            AuthoringRelationshipCompatibility.NOT_EVALUATED,
            self.transaction.preview?.relationshipEvidence?.compatibility,
        )

        val duplicate = assertIs<GovernedRelationshipPreviewBlocked>(
            Fixture(source = SOURCE_WITH_CONNECTION).preview(),
        )
        assertEquals("authoring.relationship.duplicate", duplicate.diagnostics.single().code.value)
        assertEquals(
            AuthoringRelationshipCompatibility.COMPATIBLE,
            duplicate.transaction.preview?.relationshipEvidence?.compatibility,
        )
    }

    @Test
    fun `unsupported relationship type is blocked before compatibility evaluation`() {
        val blocked = assertIs<GovernedRelationshipPreviewBlocked>(
            Fixture(intent = intent().copy(relationshipType = SemanticRelationshipType("FlowRelationship"))).preview(),
        )

        assertEquals("authoring.relationship.type-unsupported", blocked.diagnostics.single().code.value)
        assertEquals(
            AuthoringRelationshipCompatibility.NOT_EVALUATED,
            blocked.transaction.preview?.relationshipEvidence?.compatibility,
        )
    }

    @Test
    fun `persistence target mismatch blocks before planning without claiming incompatibility`() {
        val requested = intent().copy(
            persistenceTarget = SemanticRelationshipPersistenceTarget("file:///workspace/other.athena"),
        )

        val blocked = assertIs<GovernedRelationshipPreviewBlocked>(Fixture(intent = requested).preview())
        val evidence = checkNotNull(blocked.transaction.preview?.relationshipEvidence)

        assertEquals("authoring.source.conflict", blocked.diagnostics.single().code.value)
        assertEquals(AuthoringRelationshipCompatibility.NOT_EVALUATED, evidence.compatibility)
        assertEquals(null, evidence.sourceEdit)
    }

    @Test
    fun `relationship capability evidence is bound to target context origin and requirements`() {
        val valid = capabilityEvidence(
            StableSemanticIdentity("port:Source.out"),
            StableSemanticIdentity("port:Load.in"),
        )
        val invalidEvidence = listOf(
            valid.copy(relatedSubjects = emptySet()),
            valid.copy(actorOrigin = InteractionOriginSurface.PALETTE),
            valid.copy(
                subject = valid.subject.copy(sourceContextId = "file:///workspace/other.athena"),
            ),
            valid.copy(satisfiedRequirements = emptyList()),
        )

        invalidEvidence.forEach { evidence ->
            val blocked = assertIs<GovernedRelationshipPreviewBlocked>(Fixture(capability = evidence).preview())
            assertEquals(AuthoringDiagnosticCode.STOP_DOWNSTREAM, blocked.diagnostics.single().code)
            assertEquals(AuthoringRelationshipCompatibility.NOT_EVALUATED, blocked.transaction.preview?.relationshipEvidence?.compatibility)
        }
    }

    private class Fixture(
        private val intent: SemanticRelationshipIntent = intent(),
        private val routePreview: AuthoringRelationshipRoutePreviewEvidence? = ROUTE_PREVIEW,
        private val source: String = SOURCE,
        private val capability: AuthoringCapabilityEvidence = capabilityEvidence(
            intent.sourceSubjectId,
            intent.targetSubjectId,
        ),
    ) {
        val document = sourceDocument(source)
        private val semanticDocument = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(Path.of("sample.athena"), source),
        ).document
        var currentSource = source
        val seenStages = mutableListOf<AuthoringValidationStage>()
        var validationCalls = 0
        var revisionCalls = 0
        var mutationCalls = 0
        var reprojectionCalls = 0

        fun preview(): GovernedRelationshipPreviewResult = GovernedRelationshipPreviewService(
            routePreviewAuthority = GovernedRelationshipRoutePreviewAuthority { _, _ -> routePreview },
        ).preview(
            GovernedRelationshipPreviewRequest(
                transactionId = SemanticAuthoringTransactionId("transaction:relationship"),
                previewId = AuthoringPreviewId("preview:relationship"),
                intent = intent,
                capabilityEvidence = capability,
                provenance = AuthoringTransactionProvenance("user:Aaron", ORIGIN, "relationship authoring"),
                sourceDocument = document,
                semanticDocument = semanticDocument,
            ),
        )

        fun runtime(result: GovernedRelationshipPreviewResult): SemanticAuthoringTransactionRuntime =
            SemanticAuthoringTransactionRuntime(
                validationAuthority = AuthoringTransactionValidationAuthority { stage, transaction ->
                    validationCalls += 1
                    seenStages += stage
                    result.validationAuthority.validate(stage, transaction)
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
                    val ready = assertIs<GovernedRelationshipPreviewReady>(result)
                    currentSource = ready.sourceEditPlan.applyTo(currentSource)
                    AuthoringMutationCommitted(
                        mutationId = "mutation:connection:Source.out->Load.in",
                        committedRevision = AuthoringRevisionGuard.from(
                            semanticSnapshotId = "snapshot:m31:committed",
                            sourceUri = document.sourceUri,
                            documentVersion = document.documentVersion + 1,
                            sourceText = currentSource,
                        ),
                        affectedSemanticIds = ready.sourceEditPlan.affectedSemanticIds,
                    )
                },
                reprojectionAuthority = SemanticAuthoringReprojectionAuthority { _, _ ->
                    reprojectionCalls += 1
                    AuthoringReprojectionSucceeded(listOf("route:connection:Source.out->Load.in"))
                },
            )
    }

    private companion object {
        val ORIGIN = AuthoringOrigin(AuthoringSurface.GRAPH)
        val ROUTE_PREVIEW = AuthoringRelationshipRoutePreviewEvidence(
            routeId = "route:connection:Source.out->Load.in",
            quality = "SATISFIED",
            sourceAnchorId = "anchor:port:Source.out",
            targetAnchorId = "anchor:port:Load.in",
            pointCount = 4,
        )
        const val SOURCE = """system Demo {
  device Source {
    type Switch
    port out {
      direction out
      signal Digital
    }
  }

  device Load {
    type Lamp
    port in {
      direction in
      signal Digital
    }
  }

  device Other {
    type Switch
    port out {
      direction out
      signal Digital
    }
  }
}
"""

        fun intent(
            source: String = "port:Source.out",
            target: String = "port:Load.in",
        ): SemanticRelationshipIntent = SemanticRelationshipIntent(
            intentId = AuthoringIntentId("intent:relationship"),
            origin = ORIGIN,
            relationshipType = ElectricalConnectionRelationship,
            sourceSubjectId = StableSemanticIdentity(source),
            targetSubjectId = StableSemanticIdentity(target),
        )

        val SOURCE_WITH_CONNECTION = SOURCE.replace(
            "\n}",
            "\n\n  connect Source.out -> Load.in\n}",
        )

        fun sourceDocument(source: String): BackendAuthoringSourceDocument {
            val parse = assertIs<ParseSuccess>(AthenaLanguageParser().parse("sample.athena", source))
            return BackendAuthoringSourceDocument(
                sourceUri = "file:///workspace/sample.athena",
                documentVersion = 7,
                semanticSnapshotId = "snapshot:m31",
                sourceText = source,
                ast = parse.ast,
            )
        }

        fun capabilityEvidence(
            sourceSubjectId: StableSemanticIdentity,
            targetSubjectId: StableSemanticIdentity,
        ): AuthoringCapabilityEvidence =
            AuthoringCapabilityEvidence(
                capabilityId = "create-semantic-relationship",
                intentKind = AuthoringIntentKind.CREATE_RELATIONSHIP,
                subject = InteractionSubjectKey(
                    canonicalSubjectId = sourceSubjectId,
                    subjectKind = InteractionSubjectKind.PORT,
                    sourceContextId = "file:///workspace/sample.athena",
                ),
                actorOrigin = InteractionOriginSurface.GRAPH,
                satisfiedRequirements = listOf(
                    AuthoringCapabilityRequirement(AuthoringCapabilityRequirementKind.DOMAIN, "electrical", true),
                    AuthoringCapabilityRequirement(AuthoringCapabilityRequirementKind.PROJECTION, "schematic", true),
                ),
                relatedSubjects = setOf(
                    InteractionSubjectKey(
                        canonicalSubjectId = targetSubjectId,
                        subjectKind = InteractionSubjectKind.PORT,
                        sourceContextId = "file:///workspace/sample.athena",
                    ),
                ),
            )
    }
}
