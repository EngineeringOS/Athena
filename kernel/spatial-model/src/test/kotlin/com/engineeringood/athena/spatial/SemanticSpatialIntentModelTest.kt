package com.engineeringood.athena.spatial

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SemanticSpatialIntentModelTest {
    @Test
    fun `semantic spatial intent captures M27 required 2D schematic contract without coordinates`() {
        val route = subject("connection:ControllerPLC1.do1->FieldTerminalXT1.in1", SemanticSpatialSubjectKind.CONNECTION)
        val terminal = subject("port:FieldTerminalXT1.in1", SemanticSpatialSubjectKind.TERMINAL, terminalId = "terminal:XT1:1")

        val intent = SemanticSpatialIntent(
            intentId = SemanticSpatialIntentId("spatial:intent:route:do1"),
            scope = SemanticSpatialProjectionScope.ELECTRICAL_SCHEMATIC_2D,
            subject = route,
            relation = "route-occupies-schematic-space",
            priority = SemanticSpatialPriority.HARD,
            confidence = SemanticSpatialConfidence(0.94),
            source = SemanticSpatialConstraintSource.PRESENTATION_POLICY,
            preferredDirection = SemanticSpatialDirection.LEFT_TO_RIGHT,
            terminalSide = SemanticSpatialSide.RIGHT,
            lanePreference = SemanticSpatialLanePreference.HORIZONTAL_FIRST,
            ordering = SemanticSpatialOrdering.SIGNAL_FLOW,
            groupings = listOf(SemanticSpatialGrouping(groupId = "field-output", role = "signal-output")),
            separations = listOf(SemanticSpatialSeparation(from = route, avoid = terminal)),
            avoidances = listOf(SemanticSpatialAvoidance(target = terminal, reason = "component-body-avoidance")),
        )

        assertEquals(SemanticSpatialProjectionScope.ELECTRICAL_SCHEMATIC_2D, intent.scope)
        assertEquals(SemanticSpatialPriority.HARD, intent.priority)
        assertEquals(SemanticSpatialConstraintSource.PRESENTATION_POLICY, intent.source)
        assertEquals(SemanticSpatialConfidence(0.94), intent.confidence)

        val reflectedProperties = SemanticSpatialIntent::class.java.declaredFields.map { field -> field.name }.toSet()
        assertTrue("x" !in reflectedProperties)
        assertTrue("y" !in reflectedProperties)
        assertTrue("points" !in reflectedProperties)
        assertTrue("canvasId" !in reflectedProperties)
        assertTrue("svgElementId" !in reflectedProperties)
    }

    @Test
    fun `canonical snapshot is deterministic by priority source subject and intent id`() {
        val low = intent("spatial:intent:low", SemanticSpatialPriority.PREFERENCE, SemanticSpatialConstraintSource.AI_SUGGESTION)
        val hard = intent("spatial:intent:hard", SemanticSpatialPriority.HARD, SemanticSpatialConstraintSource.SEMANTIC_MODEL)
        val strong = intent("spatial:intent:strong", SemanticSpatialPriority.STRONG, SemanticSpatialConstraintSource.ROUTING_POLICY)

        val snapshot = SemanticSpatialIntentSnapshot.canonical(
            snapshotId = SemanticSpatialIntentSnapshotId("spatial:snapshot:m27"),
            scope = SemanticSpatialProjectionScope.ELECTRICAL_SCHEMATIC_2D,
            intents = listOf(low, strong, hard),
        )

        assertEquals(
            listOf("spatial:intent:hard", "spatial:intent:strong", "spatial:intent:low"),
            snapshot.intents.map { spatialIntent -> spatialIntent.intentId.value },
        )
    }

    @Test
    fun `confidence is bounded for explainable policy and future ai suggestions`() {
        assertFailsWith<IllegalArgumentException> { SemanticSpatialConfidence(-0.01) }
        assertFailsWith<IllegalArgumentException> { SemanticSpatialConfidence(1.01) }
    }

    @Test
    fun `subject references reject blank projection aliases`() {
        assertFailsWith<IllegalArgumentException> {
            subject("component:PLC1", SemanticSpatialSubjectKind.COMPONENT, terminalId = "")
        }
    }

    private fun intent(
        id: String,
        priority: SemanticSpatialPriority,
        source: SemanticSpatialConstraintSource,
    ): SemanticSpatialIntent = SemanticSpatialIntent(
        intentId = SemanticSpatialIntentId(id),
        scope = SemanticSpatialProjectionScope.ELECTRICAL_SCHEMATIC_2D,
        subject = subject("connection:$id", SemanticSpatialSubjectKind.CONNECTION),
        relation = "route-occupies-schematic-space",
        priority = priority,
        confidence = SemanticSpatialConfidence(0.75),
        source = source,
    )

    private fun subject(
        id: String,
        kind: SemanticSpatialSubjectKind,
        terminalId: String? = null,
    ): SemanticSpatialSubjectRef = SemanticSpatialSubjectRef(
        semanticId = StableSemanticIdentity(id),
        kind = kind,
        occurrenceId = "occurrence:$id",
        sheetId = "documentation/sheet/01",
        viewId = "documentation",
        anchorId = "anchor:$id",
        terminalId = terminalId,
    )
}
