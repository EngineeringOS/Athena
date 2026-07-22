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
import kotlin.test.assertFailsWith

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
    fun `relationship action intent rejects non terminal semantic subjects`() {
        val actionIntent = SemanticActionIntent(
            actionIntentId = "action:connect-components",
            actionFamily = InteractionActionFamily.MUTATE,
            subject = InteractionSubjectKey(
                canonicalSubjectId = StableSemanticIdentity("component:PLC1"),
                subjectKind = InteractionSubjectKind.COMPONENT,
            ),
            targetSubjects = listOf(
                InteractionSubjectKey(
                    canonicalSubjectId = StableSemanticIdentity("port:HMI1.power"),
                    subjectKind = InteractionSubjectKind.PORT,
                ),
            ),
            requestedBy = InteractionProvenance(
                actor = "user:Aaron",
                originSurface = InteractionOriginSurface.GRAPH,
                reason = "relationship authoring",
            ),
        )

        val error = assertFailsWith<IllegalArgumentException> {
            actionIntent.toSemanticRelationshipIntent()
        }

        assertEquals(
            "Semantic relationship mutation requires canonical port or terminal subjects.",
            error.message,
        )
    }

    @Test
    fun `entity creation action intent maps to generic semantic entity intent`() {
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
                "conceptTemplateId" to "electrical.switch.default",
                "conceptId" to "electrical.switch",
                "preferredImplementationId" to "SPARE-XT",
                "suggestedName" to "SpareTerminalXT99",
            ),
        )

        val guard = AuthoringRevisionGuard.from(
            semanticSnapshotId = "snapshot:test",
            sourceUri = "file:///workspace/main.athena",
            documentVersion = 1,
            sourceText = "system MainPanel {}",
        )
        val createIntent = actionIntent.toCreateSemanticEntityIntent(
            revisionGuard = guard,
            provenance = AuthoringTransactionProvenance(
                actor = "user:Aaron",
                origin = AuthoringOrigin(AuthoringSurface.PALETTE),
                reason = "insert semantic entity",
            ),
        )

        assertEquals("action:create-spare-terminal", createIntent.intentId.value)
        assertEquals(AuthoringSurface.PALETTE, createIntent.origin.surface)
        assertEquals(parent.canonicalSubjectId, createIntent.creationContext.parentSubjectId)
        assertEquals("electrical.switch.default", createIntent.conceptTemplateId.value)
        assertEquals("electrical.switch", createIntent.conceptId.value)
        assertEquals("SPARE-XT", createIntent.preferredImplementationId?.value)
        assertEquals("SpareTerminalXT99", createIntent.suggestedName)
    }
}
