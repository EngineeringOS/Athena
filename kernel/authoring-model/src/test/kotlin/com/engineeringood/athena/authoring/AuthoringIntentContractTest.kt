package com.engineeringood.athena.authoring

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
        val createIntent = CreateComponentIntent(
            intentId = AuthoringIntentId("intent:create-plc"),
            origin = AuthoringOrigin(AuthoringSurface.PALETTE),
            parentIdentity = StableSemanticIdentity("system:factory-line"),
            conceptId = EngineeringConceptId("electrical.plc.cpu"),
            preferredImplementationId = PartImplementationId("siemens.cpu.313c"),
            suggestedName = "PLC1",
        )
        val updateIntent = UpdateComponentPropertiesIntent(
            intentId = AuthoringIntentId("intent:update-plc"),
            origin = AuthoringOrigin(AuthoringSurface.INSPECTOR),
            componentId = StableSemanticIdentity("component:PLC1"),
            properties = mapOf(
                AuthoringPropertyName("tag") to AuthoringValue.Text("PLC1"),
                AuthoringPropertyName("description") to AuthoringValue.Text("Main PLC CPU"),
            ),
        )
        val connectIntent = ConnectPortsIntent(
            intentId = AuthoringIntentId("intent:connect-mpi"),
            origin = AuthoringOrigin(AuthoringSurface.GRAPH),
            sourcePortId = StableSemanticIdentity("port:PLC1.MPI"),
            targetPortId = StableSemanticIdentity("port:HMI1.MPI"),
        )
        val revealIntent = RevealSubjectIntent(
            intentId = AuthoringIntentId("intent:reveal-plc"),
            origin = AuthoringOrigin(AuthoringSurface.FORM),
            subjectId = StableSemanticIdentity("component:PLC1"),
            targets = setOf(AuthoringRevealTarget.GRAPH, AuthoringRevealTarget.INSPECTOR, AuthoringRevealTarget.SOURCE),
        )

        assertIs<AuthoringIntent>(createIntent)
        assertIs<AuthoringIntent>(updateIntent)
        assertIs<AuthoringIntent>(connectIntent)
        assertIs<AuthoringIntent>(revealIntent)
        assertEquals(AuthoringSurface.PALETTE, createIntent.origin.surface)
        assertEquals(AuthoringSurface.INSPECTOR, updateIntent.origin.surface)
        assertEquals(AuthoringSurface.GRAPH, connectIntent.origin.surface)
        assertEquals(AuthoringSurface.FORM, revealIntent.origin.surface)
    }

    @Test
    fun `authoring intent identities stay separate from canonical subject identities`() {
        val componentId = StableSemanticIdentity("component:PLC1")
        val intent = UpdateComponentPropertiesIntent(
            intentId = AuthoringIntentId("intent:update-tag"),
            origin = AuthoringOrigin(AuthoringSurface.INSPECTOR),
            componentId = componentId,
            properties = mapOf(AuthoringPropertyName("tag") to AuthoringValue.Text("PLC_MAIN")),
        )

        assertEquals("intent:update-tag", intent.intentId.value)
        assertEquals("component:PLC1", intent.componentId.value)
        assertNotEquals(intent.intentId.value, intent.componentId.value)
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
    fun `legacy connect ports intent lifts into electrical semantic relationship intent`() {
        val legacyIntent = ConnectPortsIntent(
            intentId = AuthoringIntentId("intent:connect-mpi"),
            origin = AuthoringOrigin(AuthoringSurface.GRAPH),
            sourcePortId = StableSemanticIdentity("port:PLC1.MPI"),
            targetPortId = StableSemanticIdentity("port:HMI1.MPI"),
        )

        val relationshipIntent = legacyIntent.toSemanticRelationshipIntent(
            projectionContext = SemanticRelationshipProjectionContext(viewId = "schematic"),
            persistenceTarget = SemanticRelationshipPersistenceTarget(sourceUri = "main.athena"),
            provenance = "legacy graph connection action",
        )

        assertEquals(legacyIntent.intentId, relationshipIntent.intentId)
        assertEquals(legacyIntent.origin, relationshipIntent.origin)
        assertEquals(ElectricalConnectionRelationship, relationshipIntent.relationshipType)
        assertEquals(legacyIntent.sourcePortId, relationshipIntent.sourceSubjectId)
        assertEquals(legacyIntent.targetPortId, relationshipIntent.targetSubjectId)
        assertEquals("schematic", relationshipIntent.projectionContext.viewId)
        assertEquals("main.athena", relationshipIntent.persistenceTarget.sourceUri)
        assertEquals("legacy graph connection action", relationshipIntent.provenance)
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
}
