package com.engineeringood.athena.runtime

import com.engineeringood.athena.authoring.AuthoringIntentId
import com.engineeringood.athena.authoring.AuthoringOrigin
import com.engineeringood.athena.authoring.AuthoringPreviewId
import com.engineeringood.athena.authoring.AuthoringRelationshipCompatibility
import com.engineeringood.athena.authoring.AuthoringRelationshipRoutePreviewEvidence
import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.authoring.AuthoringSurface
import com.engineeringood.athena.authoring.AuthoringTransactionProvenance
import com.engineeringood.athena.authoring.CreateSemanticEntityIntent
import com.engineeringood.athena.authoring.ElectricalConnectionRelationship
import com.engineeringood.athena.authoring.SemanticAuthoringTransactionId
import com.engineeringood.athena.authoring.SemanticEntityCreationContext
import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.compiler.AthenaCompiler
import com.engineeringood.athena.compiler.BackendAuthoringSourceDocument
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
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
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AthenaM31SampleAuthoringProofTest {
    @Test
    fun `m31 customer sample supports governed entity creation preview`() {
        val source = Files.readString(sampleProjectSource())
        val document = sourceDocument(source)
        val template = motorTemplate()
        val result = GovernedEntityCreationPreviewService(
            templates = electricalEngineeringConceptTemplates(),
            projectionAuthority = GovernedEntityCreationProjectionAuthority { _, resolvedTemplate, canonicalTag ->
                assertEquals(template.templateId, resolvedTemplate.templateId)
                assertEquals("ServiceMotorM31", canonicalTag)
                GovernedEntityCreationProjectionResolved(
                    representationId = "iec.motor.compact",
                    compositionTargetId = "composition:m31-customer-control",
                    projectionOccurrenceIds = listOf("sheet:control/component:ServiceMotorM31"),
                )
            },
        ).preview(
            GovernedEntityCreationPreviewRequest(
                transactionId = SemanticAuthoringTransactionId("transaction:m31:create-service-motor"),
                previewId = AuthoringPreviewId("preview:m31:create-service-motor"),
                intent = CreateSemanticEntityIntent(
                    intentId = AuthoringIntentId("intent:m31:create-service-motor"),
                    origin = ORIGIN,
                    creationContext = SemanticEntityCreationContext(SYSTEM_IDENTITY),
                    conceptTemplateId = template.templateId,
                    conceptId = template.conceptId,
                    suggestedName = "ServiceMotorM31",
                    revisionGuard = document.revisionGuard,
                    provenance = AuthoringTransactionProvenance("user:Aaron", ORIGIN, "m31 sample proof"),
                ),
                capabilityEvidence = createCapabilityEvidence(document.sourceUri),
                document = document,
            ),
        )

        val ready = assertIs<GovernedEntityCreationPreviewReady>(result)
        val evidence = checkNotNull(ready.transaction.preview?.entityCreationEvidence)
        assertEquals("ServiceMotorM31", evidence.canonicalTag)
        assertEquals(
            listOf("down", "status", "up"),
            evidence.nestedPorts.map { port -> port.name }.sorted(),
        )
        assertTrue(ready.transaction.preview?.acceptanceEligibility?.eligible == true)
    }

    @Test
    fun `m31 customer sample supports compatible governed relationship preview`() {
        val source = Files.readString(sampleProjectSource())
        val document = sourceDocument(source)
        val semanticDocument = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(sampleProjectSource()),
        ).document
        val result = GovernedRelationshipPreviewService(
            routePreviewAuthority = GovernedRelationshipRoutePreviewAuthority { _, _ ->
                AuthoringRelationshipRoutePreviewEvidence(
                    routeId = "route:connection:ControlRelayK31.spareOut->SpareTerminalXT31.in1",
                    quality = "SATISFIED",
                    sourceAnchorId = "anchor:port:ControlRelayK31.spareOut",
                    targetAnchorId = "anchor:port:SpareTerminalXT31.in1",
                    pointCount = 4,
                )
            },
        ).preview(
            GovernedRelationshipPreviewRequest(
                transactionId = SemanticAuthoringTransactionId("transaction:m31:relationship"),
                previewId = AuthoringPreviewId("preview:m31:relationship"),
                intent = SemanticRelationshipIntent(
                    intentId = AuthoringIntentId("intent:m31:relationship"),
                    origin = ORIGIN,
                    relationshipType = ElectricalConnectionRelationship,
                    sourceSubjectId = StableSemanticIdentity("port:ControlRelayK31.spareOut"),
                    targetSubjectId = StableSemanticIdentity("port:SpareTerminalXT31.in1"),
                ),
                capabilityEvidence = relationshipCapabilityEvidence(document.sourceUri),
                provenance = AuthoringTransactionProvenance("user:Aaron", ORIGIN, "m31 sample proof"),
                sourceDocument = document,
                semanticDocument = semanticDocument,
            ),
        )

        val ready = assertIs<GovernedRelationshipPreviewReady>(result)
        val evidence = checkNotNull(ready.transaction.preview?.relationshipEvidence)
        assertEquals(AuthoringRelationshipCompatibility.COMPATIBLE, evidence.compatibility)
        assertEquals(
            listOf("connection:ControlRelayK31.spareOut->SpareTerminalXT31.in1"),
            evidence.affectedSemanticIds,
        )
        assertTrue(checkNotNull(evidence.sourceEdit).admittedText.contains("ControlRelayK31.spareOut -> SpareTerminalXT31.in1"))
        assertTrue(ready.transaction.preview?.acceptanceEligibility?.eligible == true)
    }

    private fun sourceDocument(source: String): BackendAuthoringSourceDocument {
        val parse = assertIs<ParseSuccess>(AthenaLanguageParser().parse(sampleProjectSource().toString(), source))
        return BackendAuthoringSourceDocument(
            sourceUri = sampleProjectSource().toUri().toString(),
            documentVersion = 31,
            semanticSnapshotId = "snapshot:m31:sample",
            sourceText = source,
            ast = parse.ast,
        )
    }

    private fun createCapabilityEvidence(sourceUri: String): AuthoringCapabilityEvidence =
        AuthoringCapabilityEvidence(
            capabilityId = "create-semantic-entity",
            intentKind = AuthoringIntentKind.CREATE_ENTITY,
            subject = InteractionSubjectKey(
                canonicalSubjectId = SYSTEM_IDENTITY,
                subjectKind = InteractionSubjectKind.WORKSPACE,
                sourceContextId = sourceUri,
            ),
            actorOrigin = InteractionOriginSurface.GRAPH,
            satisfiedRequirements = AuthoringCapabilityRequirementKind.entries.map { kind ->
                AuthoringCapabilityRequirement(kind, "m31:${kind.name.lowercase()}", true)
            },
        )

    private fun relationshipCapabilityEvidence(sourceUri: String): AuthoringCapabilityEvidence =
        AuthoringCapabilityEvidence(
            capabilityId = "create-semantic-relationship",
            intentKind = AuthoringIntentKind.CREATE_RELATIONSHIP,
            subject = InteractionSubjectKey(
                canonicalSubjectId = StableSemanticIdentity("port:ControlRelayK31.spareOut"),
                subjectKind = InteractionSubjectKind.PORT,
                sourceContextId = sourceUri,
            ),
            actorOrigin = InteractionOriginSurface.GRAPH,
            satisfiedRequirements = listOf(
                AuthoringCapabilityRequirement(AuthoringCapabilityRequirementKind.DOMAIN, "electrical", true),
                AuthoringCapabilityRequirement(AuthoringCapabilityRequirementKind.PROJECTION, "m31-customer", true),
            ),
            relatedSubjects = setOf(
                InteractionSubjectKey(
                    canonicalSubjectId = StableSemanticIdentity("port:SpareTerminalXT31.in1"),
                    subjectKind = InteractionSubjectKind.PORT,
                    sourceContextId = sourceUri,
                ),
            ),
        )

    private fun motorTemplate(): EngineeringConceptTemplate =
        electricalEngineeringConceptTemplates().single { template ->
            template.templateId.value == "electrical.motor.ac.default"
        }

    private fun sampleProjectSource(): Path =
        resolveRepoRoot().resolve("examples/m31/sample-project/src/01-governed-authoring-customer-source.athena")

    private fun resolveRepoRoot(): Path {
        var current = Path.of("").toAbsolutePath()
        while (current.parent != null && !Files.exists(current.resolve("settings.gradle.kts"))) current = current.parent
        check(Files.exists(current.resolve("settings.gradle.kts"))) { "Could not locate repository root" }
        return current
    }

    private companion object {
        val ORIGIN = AuthoringOrigin(AuthoringSurface.GRAPH)
        val SYSTEM_IDENTITY = StableSemanticIdentity("system:RollingShutterGovernedAuthoringProof")
    }
}
