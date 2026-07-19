package com.engineeringood.athena.presentation

import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.representation.GridUnit
import com.engineeringood.athena.representation.PresentationAnatomy
import com.engineeringood.athena.representation.PresentationBounds
import com.engineeringood.athena.representation.PresentationHotspot
import com.engineeringood.athena.representation.PresentationPoint
import com.engineeringood.athena.representation.PresentationPrimitive
import com.engineeringood.athena.representation.PresentationPrimitiveId
import com.engineeringood.athena.representation.PresentationSize
import com.engineeringood.athena.representation.RepresentationContext
import com.engineeringood.athena.representation.RepresentationId
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationSubjectId
import com.engineeringood.athena.representation.SymbolAnatomy
import com.engineeringood.athena.representation.SymbolFamilyId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PresentationRepresentationFactContractTest {
    @Test
    fun `presentation document carries representation facts in deterministic order`() {
        val first = representationFact("PLC1", "plc-controller")
        val second = representationFact("XT1", "terminal-block")
        val document = PresentationDocument(
            view = ViewDefinition(id = "schematic-sheet", displayName = "Schematic Sheet"),
            canvasWidth = 1200,
            canvasHeight = 800,
            primitivePacks = emptyList(),
            compositePacks = emptyList(),
            occurrences = emptyList(),
            representationFacts = listOf(second, first),
        )

        assertEquals(listOf("PLC1", "XT1"), document.representationFactsForRendering().map { it.subjectId.value })
        assertTrue(document.representationFactsForRendering().all { it.symbol.anatomy.context == RepresentationContext.ELECTRICAL_SCHEMATIC })
    }

    private fun representationFact(
        subject: String,
        family: String,
    ): PresentationRepresentationFact {
        val anatomy = PresentationAnatomy(
            representationId = RepresentationId("athena-industrial-control-v0:$family"),
            context = RepresentationContext.ELECTRICAL_SCHEMATIC,
            bounds = PresentationBounds(GridUnit(80), GridUnit(48)),
            hotspot = PresentationHotspot(PresentationPoint(GridUnit(0), GridUnit(0))),
            primitives = listOf(
                PresentationPrimitive.Rectangle(
                    primitiveId = PresentationPrimitiveId("$family-body"),
                    origin = PresentationPoint(GridUnit(0), GridUnit(0)),
                    size = PresentationSize(GridUnit(80), GridUnit(48)),
                ),
            ),
            terminals = emptyList(),
            labelAnchors = emptyList(),
        )
        return PresentationRepresentationFact(
            subjectId = RepresentationSubjectId(subject),
            occurrenceId = RepresentationOccurrenceId("$subject@schematic-sheet"),
            symbol = SymbolAnatomy(SymbolFamilyId(family), anatomy),
            anatomy = anatomy,
            terminals = emptyList(),
            labels = emptyList(),
            sourceProjectionIds = listOf("projection:$subject"),
        )
    }
}
