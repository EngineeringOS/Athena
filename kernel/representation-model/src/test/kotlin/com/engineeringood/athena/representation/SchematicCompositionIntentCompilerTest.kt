package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SchematicCompositionIntentCompilerTest {
    @Test
    fun `composition intent emits professional planning facts before geometry`() {
        val occurrence = RepresentationOccurrence(
            occurrenceId = RepresentationOccurrenceId("occurrence:MotorM1"),
            canonicalSemanticId = RepresentationSubjectId("component:MotorM1"),
            projectionOccurrenceId = RepresentationProjectionOccurrenceId("sheet:control/motor"),
            occurrenceRole = RepresentationOccurrenceRole.LOAD_SYMBOL,
            symbolId = RepresentationSymbolId("iec.motor.compact"),
            compositionIntentMembership = listOf(CompositionIntentMembershipId("lane:power-loads")),
        )

        val plan = SchematicCompositionIntentCompiler().plan(
            SchematicCompositionInput(
                occurrences = listOf(occurrence),
                boundsByOccurrence = mapOf(occurrence.occurrenceId to PresentationBounds(GridUnit(44), GridUnit(44))),
                terminalAnchorCountByOccurrence = mapOf(occurrence.occurrenceId to 1),
                spatialIntentReferences = listOf(SchematicSpatialIntentReference("m27:lane:power-loads")),
            ),
        )

        assertEquals(
            setOf(
                SchematicCompositionFactKind.RAIL,
                SchematicCompositionFactKind.COLUMN,
                SchematicCompositionFactKind.TERMINAL_GROUP,
                SchematicCompositionFactKind.ROUTE_LANE,
                SchematicCompositionFactKind.REFERENCE_ZONE,
                SchematicCompositionFactKind.LABEL_BAND,
                SchematicCompositionFactKind.ALIGNMENT_GROUP,
            ),
            plan.facts.map { fact -> fact.kind }.toSet(),
        )
        assertTrue(plan.facts.all { fact -> fact.occurrenceIds.contains(occurrence.occurrenceId) })
        assertTrue(plan.facts.any { fact -> fact.spatialIntentReferences.isNotEmpty() })
    }

    @Test
    fun `composition intent payload does not persist cad geometry truth`() {
        val plan = SchematicCompositionIntentCompiler().plan(SchematicCompositionInput.empty())

        val payloadText = plan.toTransportPayload().joinToString(separator = "\n") { payload ->
            payload.entries.joinToString(separator = ",") { (key, value) -> "$key=$value" }
        }

        listOf("x=", "y=", "width=", "height=", "viewBox", "svg", "cad").forEach { forbidden ->
            assertFalse(payloadText.contains(forbidden, ignoreCase = true), "Found forbidden geometry marker `$forbidden`.")
        }
    }
}
