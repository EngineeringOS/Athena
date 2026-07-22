package com.engineeringood.athena.authoring

import com.engineeringood.athena.component.EngineeringConceptTemplateId
import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.part.PartImplementationId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AuthoringIntentContractTest {
    @Test
    fun `authoring intents stay platform owned and surface agnostic`() {
        val guard = revisionGuard()
        val createOrigin = AuthoringOrigin(AuthoringSurface.PALETTE)
        val createIntent = CreateSemanticEntityIntent(
            intentId = AuthoringIntentId("intent:create-plc"),
            origin = createOrigin,
            creationContext = SemanticEntityCreationContext(
                parentSubjectId = StableSemanticIdentity("system:factory-line"),
            ),
            conceptTemplateId = EngineeringConceptTemplateId("electrical.plc.cpu.default"),
            conceptId = EngineeringConceptId("electrical.plc.cpu"),
            preferredImplementationId = PartImplementationId("siemens.cpu.313c"),
            suggestedName = "PLC1",
            revisionGuard = guard,
            provenance = AuthoringTransactionProvenance("user:test", createOrigin),
        )
        val updateOrigin = AuthoringOrigin(AuthoringSurface.INSPECTOR)
        val updateIntent = UpdateSemanticEntityPropertiesIntent(
            intentId = AuthoringIntentId("intent:update-plc"),
            origin = updateOrigin,
            subjectId = StableSemanticIdentity("component:PLC1"),
            properties = mapOf(
                AuthoringPropertyName("tag") to AuthoringValue.Text("PLC1"),
                AuthoringPropertyName("description") to AuthoringValue.Text("Main PLC CPU"),
            ),
            revisionGuard = guard,
            provenance = AuthoringTransactionProvenance("user:test", updateOrigin),
        )
        val relationshipIntent = SemanticRelationshipIntent(
            intentId = AuthoringIntentId("intent:relate-mpi"),
            origin = AuthoringOrigin(AuthoringSurface.GRAPH),
            relationshipType = ElectricalConnectionRelationship,
            sourceSubjectId = StableSemanticIdentity("port:PLC1.MPI"),
            targetSubjectId = StableSemanticIdentity("port:HMI1.MPI"),
        )
        val revealIntent = RevealSubjectIntent(
            intentId = AuthoringIntentId("intent:reveal-plc"),
            origin = AuthoringOrigin(AuthoringSurface.FORM),
            subjectId = StableSemanticIdentity("component:PLC1"),
            targets = setOf(AuthoringRevealTarget.GRAPH, AuthoringRevealTarget.INSPECTOR, AuthoringRevealTarget.SOURCE),
        )

        assertIs<AuthoringIntent>(createIntent)
        assertIs<AuthoringIntent>(updateIntent)
        assertIs<AuthoringIntent>(relationshipIntent)
        assertIs<AuthoringIntent>(revealIntent)
        assertEquals(AuthoringSurface.PALETTE, createIntent.origin.surface)
        assertEquals(AuthoringSurface.INSPECTOR, updateIntent.origin.surface)
        assertEquals(AuthoringSurface.GRAPH, relationshipIntent.origin.surface)
        assertEquals(AuthoringSurface.FORM, revealIntent.origin.surface)
    }

    @Test
    fun `authoring intent identities stay separate from canonical subject identities`() {
        val componentId = StableSemanticIdentity("component:PLC1")
        val origin = AuthoringOrigin(AuthoringSurface.INSPECTOR)
        val intent = UpdateSemanticEntityPropertiesIntent(
            intentId = AuthoringIntentId("intent:update-tag"),
            origin = origin,
            subjectId = componentId,
            properties = mapOf(AuthoringPropertyName("tag") to AuthoringValue.Text("PLC_MAIN")),
            revisionGuard = revisionGuard(),
            provenance = AuthoringTransactionProvenance("user:test", origin),
        )

        assertEquals("intent:update-tag", intent.intentId.value)
        assertEquals("component:PLC1", intent.subjectId.value)
        assertNotEquals(intent.intentId.value, intent.subjectId.value)
    }

    @Test
    fun `semantic relationship intent is generic while electrical connection is a specialization`() {
        val intent = SemanticRelationshipIntent(
            intentId = AuthoringIntentId("intent:relate-plc-hmi"),
            origin = AuthoringOrigin(AuthoringSurface.GRAPH),
            relationshipType = ElectricalConnectionRelationship,
            sourceSubjectId = StableSemanticIdentity("port:PLC1.MPI"),
            targetSubjectId = StableSemanticIdentity("port:HMI1.MPI"),
            projectionContext = SemanticRelationshipProjectionContext(viewId = "schematic"),
            persistenceTarget = SemanticRelationshipPersistenceTarget(sourceUri = "main.athena"),
            provenance = "graphical relationship preview",
        )

        assertIs<AuthoringIntent>(intent)
        assertEquals("ElectricalConnectionRelationship", intent.relationshipType.value)
        assertEquals("port:PLC1.MPI", intent.sourceSubjectId.value)
        assertEquals("port:HMI1.MPI", intent.targetSubjectId.value)
        assertEquals("schematic", intent.projectionContext.viewId)
        assertEquals("main.athena", intent.persistenceTarget.sourceUri)
    }

    @Test
    fun `relationship removal remains typed validation readiness without endpoint deletion`() {
        val removalIntent = RemoveSemanticRelationshipIntent(
            intentId = AuthoringIntentId("intent:remove-mpi"),
            origin = AuthoringOrigin(AuthoringSurface.GRAPH),
            relationshipType = ElectricalConnectionRelationship,
            sourceSubjectId = StableSemanticIdentity("port:PLC1.MPI"),
            targetSubjectId = StableSemanticIdentity("port:HMI1.MPI"),
            projectionContext = SemanticRelationshipProjectionContext(viewId = "schematic"),
            persistenceTarget = SemanticRelationshipPersistenceTarget(sourceUri = "main.athena"),
            provenance = "relationship impact inspection",
        )

        assertIs<AuthoringIntent>(removalIntent)
        assertEquals(ElectricalConnectionRelationship, removalIntent.relationshipType)
        assertEquals("port:PLC1.MPI", removalIntent.sourceSubjectId.value)
        assertEquals("port:HMI1.MPI", removalIntent.targetSubjectId.value)
        assertEquals("schematic", removalIntent.projectionContext.viewId)
        assertEquals("main.athena", removalIntent.persistenceTarget.sourceUri)
        assertEquals("relationship impact inspection", removalIntent.provenance)
    }

    @Test
    fun `authoring values stay reusable across future authoring surfaces`() {
        val values = listOf(
            AuthoringValue.Text("PLC1"),
            AuthoringValue.Symbol("CPU313C"),
            AuthoringValue.BooleanValue(true),
            AuthoringValue.IntegerValue(2),
        )

        assertTrue(values.any { value -> value is AuthoringValue.Text })
        assertTrue(values.any { value -> value is AuthoringValue.Symbol })
        assertTrue(values.any { value -> value is AuthoringValue.BooleanValue })
        assertTrue(values.any { value -> value is AuthoringValue.IntegerValue })
    }

    private fun revisionGuard(): AuthoringRevisionGuard = AuthoringRevisionGuard.from(
        semanticSnapshotId = "snapshot:test",
        sourceUri = "file:///workspace/main.athena",
        documentVersion = 1,
        sourceText = "system Test {}",
    )
}
