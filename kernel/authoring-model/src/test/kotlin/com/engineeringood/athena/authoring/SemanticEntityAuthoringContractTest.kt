package com.engineeringood.athena.authoring

import com.engineeringood.athena.component.EngineeringConceptTemplateId
import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class SemanticEntityAuthoringContractTest {
    @Test
    fun `generic entity intents carry revision context properties and provenance`() {
        val guard = revisionGuard()
        val provenance = AuthoringTransactionProvenance(
            actor = "user:Aaron",
            origin = AuthoringOrigin(AuthoringSurface.GRAPH),
            reason = "M31 customer authoring",
        )
        val create = CreateSemanticEntityIntent(
            intentId = AuthoringIntentId("intent:create:motor"),
            origin = provenance.origin,
            creationContext = SemanticEntityCreationContext(
                parentSubjectId = StableSemanticIdentity("system:RollingShutter"),
                sourceUri = guard.sourceUri,
            ),
            conceptTemplateId = EngineeringConceptTemplateId("electrical.motor.ac.default"),
            conceptId = EngineeringConceptId("electrical.motor.ac"),
            properties = mapOf(
                AuthoringPropertyName("tag") to AuthoringValue.Symbol("M31"),
                AuthoringPropertyName("model") to AuthoringValue.Text("MOTOR-AC"),
            ),
            suggestedName = "ShutterMotorM31",
            revisionGuard = guard,
            provenance = provenance,
        )
        val update = UpdateSemanticEntityPropertiesIntent(
            intentId = AuthoringIntentId("intent:update:motor"),
            origin = provenance.origin,
            subjectId = StableSemanticIdentity("component:ShutterMotorM31"),
            properties = mapOf(AuthoringPropertyName("model") to AuthoringValue.Text("MOTOR-AC-2")),
            revisionGuard = guard,
            provenance = provenance,
        )
        val remove = RemoveSemanticEntityIntent(
            intentId = AuthoringIntentId("intent:remove:motor"),
            origin = provenance.origin,
            subjectId = StableSemanticIdentity("component:ShutterMotorM31"),
            revisionGuard = guard,
            provenance = provenance,
        )

        assertIs<AuthoringIntent>(create)
        assertIs<AuthoringIntent>(update)
        assertIs<AuthoringIntent>(remove)
        assertEquals(guard, create.revisionGuard)
        assertEquals("electrical.motor.ac.default", create.conceptTemplateId.value)
        assertEquals("electrical.motor.ac", create.conceptId.value)
        assertEquals("component:ShutterMotorM31", update.subjectId.value)
        assertEquals("user:Aaron", remove.provenance.actor)
    }

    @Test
    fun `entity removal dependencies produce typed blocked preview eligibility`() {
        val intent = RemoveSemanticEntityIntent(
            intentId = AuthoringIntentId("intent:remove:motor"),
            origin = AuthoringOrigin(AuthoringSurface.INSPECTOR),
            subjectId = StableSemanticIdentity("component:ShutterMotorM31"),
            revisionGuard = revisionGuard(),
            provenance = AuthoringTransactionProvenance(
                actor = "user:Aaron",
                origin = AuthoringOrigin(AuthoringSurface.INSPECTOR),
            ),
        )
        val impact = AuthoringDependencyImpact(
            dependentRelationshipIds = listOf("connection:Relay.upOut->Motor.up"),
            projectionOccurrenceIds = listOf("occurrence:field:ShutterMotorM31"),
        )
        val diagnostic = AuthoringDiagnostic(
            code = AuthoringDiagnosticCode.REMOVAL_DEPENDENCIES,
            message = "Entity removal is blocked by dependencies.",
            authority = AuthoringDiagnosticAuthority.SEMANTIC_VALIDATION,
            lifecycleStage = AuthoringLifecycleState.BLOCKED,
            subjectId = intent.subjectId.value,
            relatedIds = impact.allDependencyIds,
        )
        val preview = AuthoringPreview(
            previewId = AuthoringPreviewId("preview:remove:motor"),
            intentId = intent.intentId,
            title = "Remove motor",
            changes = emptyList(),
            revisionGuard = intent.revisionGuard,
            dependencyImpact = impact,
            acceptanceEligibility = AuthoringAcceptanceEligibility(
                eligible = false,
                diagnostics = listOf(diagnostic),
            ),
        )

        assertFalse(preview.acceptanceEligibility.eligible)
        assertEquals(2, preview.dependencyImpact.allDependencyIds.size)
        assertEquals(AuthoringDiagnosticCode.REMOVAL_DEPENDENCIES, preview.acceptanceEligibility.diagnostics.single().code)
    }

    private fun revisionGuard(): AuthoringRevisionGuard = AuthoringRevisionGuard.from(
        semanticSnapshotId = "snapshot:m31",
        sourceUri = "file:///workspace/main.athena",
        documentVersion = 7,
        sourceText = "system RollingShutter {}",
    )
}
