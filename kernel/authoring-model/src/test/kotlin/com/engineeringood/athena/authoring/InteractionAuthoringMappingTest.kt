package com.engineeringood.athena.authoring

import com.engineeringood.athena.interaction.InteractionActionFamily
import com.engineeringood.athena.interaction.InteractionOriginSurface
import com.engineeringood.athena.interaction.InteractionProvenance
import com.engineeringood.athena.interaction.InteractionSubjectKey
import com.engineeringood.athena.interaction.InteractionSubjectKind
import com.engineeringood.athena.interaction.SemanticActionIntent
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals

class InteractionAuthoringMappingTest {
    @Test
    fun `relationship action intent maps to semantic relationship intent`() {
        val source = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("port:PLC1.power"),
            subjectKind = InteractionSubjectKind.PORT,
        )
        val target = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("port:PS1.lplus"),
            subjectKind = InteractionSubjectKind.PORT,
        )
        val actionIntent = SemanticActionIntent(
            actionIntentId = "action:connect-power",
            actionFamily = InteractionActionFamily.MUTATE,
            subject = source,
            targetSubjects = listOf(target),
            requestedBy = InteractionProvenance(
                actor = "user:Aaron",
                originSurface = InteractionOriginSurface.GRAPH,
                reason = "relationship authoring",
            ),
            parameters = mapOf(
                "relationshipType" to "ElectricalConnectionRelationship",
                "projectionViewId" to "schematic",
                "projectionOccurrenceId" to "occurrence:PLC1.power",
                "persistenceSourceUri" to "file:///workspace/main.athena",
            ),
        )

        val relationshipIntent = actionIntent.toSemanticRelationshipIntent()

        assertEquals("action:connect-power", relationshipIntent.intentId.value)
        assertEquals(AuthoringSurface.GRAPH, relationshipIntent.origin.surface)
        assertEquals(ElectricalConnectionRelationship, relationshipIntent.relationshipType)
        assertEquals(source.canonicalSubjectId, relationshipIntent.sourceSubjectId)
        assertEquals(target.canonicalSubjectId, relationshipIntent.targetSubjectId)
        assertEquals("schematic", relationshipIntent.projectionContext.viewId)
        assertEquals("occurrence:PLC1.power", relationshipIntent.projectionContext.occurrenceId)
        assertEquals("file:///workspace/main.athena", relationshipIntent.persistenceTarget.sourceUri)
        assertEquals("relationship authoring", relationshipIntent.provenance)
    }

    @Test
    fun `component creation action intent maps to create component intent`() {
        val parent = InteractionSubjectKey(
            canonicalSubjectId = StableSemanticIdentity("system:MainPanel"),
            subjectKind = InteractionSubjectKind.WORKSPACE,
        )
        val actionIntent = SemanticActionIntent(
            actionIntentId = "action:create-spare-terminal",
            actionFamily = InteractionActionFamily.MUTATE,
            subject = parent,
            requestedBy = InteractionProvenance(
                actor = "user:Aaron",
                originSurface = InteractionOriginSurface.COMMAND_PALETTE,
                reason = "insert semantic entity",
            ),
            parameters = mapOf(
                "componentConceptId" to "electrical.switch",
                "preferredImplementationId" to "SPARE-XT",
                "suggestedName" to "SpareTerminalXT99",
            ),
        )

        val createIntent = actionIntent.toCreateComponentIntent()

        assertEquals("action:create-spare-terminal", createIntent.intentId.value)
        assertEquals(AuthoringSurface.PALETTE, createIntent.origin.surface)
        assertEquals(parent.canonicalSubjectId, createIntent.parentIdentity)
        assertEquals("electrical.switch", createIntent.conceptId.value)
        assertEquals("SPARE-XT", createIntent.preferredImplementationId?.value)
        assertEquals("SpareTerminalXT99", createIntent.suggestedName)
    }
}
