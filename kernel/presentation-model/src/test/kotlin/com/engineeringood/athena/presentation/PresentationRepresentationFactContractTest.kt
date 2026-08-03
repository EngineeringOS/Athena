package com.engineeringood.athena.presentation

import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicFill
import com.engineeringood.athena.representation.GraphicLineCap
import com.engineeringood.athena.representation.GraphicLineJoin
import com.engineeringood.athena.representation.GraphicPaintToken
import com.engineeringood.athena.representation.GraphicPoint
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicStyleToken
import com.engineeringood.athena.representation.GraphicStyleTokenId
import com.engineeringood.athena.representation.RepresentationAnchorContract
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationAnchorRole
import com.engineeringood.athena.representation.RepresentationDefinition
import com.engineeringood.athena.representation.RepresentationLabelSlotId
import com.engineeringood.athena.representation.RepresentationLibraryId
import com.engineeringood.athena.representation.RepresentationLifecycle
import com.engineeringood.athena.representation.RepresentationLifecycleState
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationProvenance
import com.engineeringood.athena.representation.RepresentationSubjectId
import com.engineeringood.athena.representation.RepresentationSymbolId
import com.engineeringood.athena.representation.RepresentationSymbolKind
import com.engineeringood.athena.representation.RepresentationVersion
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

        val facts = document.representationFactsForRendering()

        assertEquals(listOf("PLC1", "XT1"), facts.map { it.subjectId.value })
        assertTrue(facts.all { it.definition.graphicBody.primitives.isNotEmpty() })
        assertTrue(facts.all { it.definition.anchors.single().role == RepresentationAnchorRole.TERMINAL })
    }

    private fun representationFact(
        subject: String,
        family: String,
    ): PresentationRepresentationFact = PresentationRepresentationFact(
        subjectId = RepresentationSubjectId(subject),
        occurrenceId = RepresentationOccurrenceId("$subject@schematic-sheet"),
        definition = definition(family),
        terminals = emptyList(),
        labels = emptyList(),
        sourceProjectionIds = listOf("projection:$subject"),
    )

    private fun definition(family: String): RepresentationDefinition = RepresentationDefinition(
        symbolId = RepresentationSymbolId("athena-industrial-control:$family"),
        libraryId = RepresentationLibraryId("athena.native.iec"),
        version = RepresentationVersion("1.0.0"),
        lifecycle = RepresentationLifecycle(
            state = RepresentationLifecycleState.ACTIVE,
            provenance = RepresentationProvenance("test/$family.athena"),
        ),
        kind = RepresentationSymbolKind.GENERIC,
        labelSlots = emptyList(),
        graphicBody = GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId(family),
            bounds = GraphicBounds(0.0, 0.0, 80.0, 48.0),
            primitives = listOf(
                GraphicPrimitive.Rectangle(
                    primitiveId = GraphicPrimitiveId("$family-body"),
                    bounds = GraphicBounds(0.0, 0.0, 80.0, 48.0),
                    cornerRadius = 0.0,
                    styleTokenId = GraphicStyleTokenId("stroke"),
                ),
            ),
            styleTokens = listOf(
                GraphicStyleToken(
                    styleTokenId = GraphicStyleTokenId("stroke"),
                    stroke = GraphicPaintToken("foreground"),
                    strokeWidth = 1.0,
                    fill = GraphicFill.TRANSPARENT,
                    lineCap = GraphicLineCap.BUTT,
                    lineJoin = GraphicLineJoin.MITER,
                ),
            ),
        ),
        anchors = listOf(
            RepresentationAnchorContract(
                anchorId = RepresentationAnchorId("$family-t1"),
                geometryRef = "$family-body",
                primitiveId = GraphicPrimitiveId("$family-body"),
                point = GraphicPoint(80.0, 24.0),
                role = RepresentationAnchorRole.TERMINAL,
                required = true,
            ),
        ),
    )
}
