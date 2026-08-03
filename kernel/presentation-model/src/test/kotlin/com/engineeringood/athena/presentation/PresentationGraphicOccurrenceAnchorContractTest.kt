package com.engineeringood.athena.presentation

import com.engineeringood.athena.layout.LayoutOrientation
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
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationAnchorRole
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationPortAnchorBindingId
import com.engineeringood.athena.routing.SchematicRoutePoint
import com.engineeringood.athena.routing.TerminalSide
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PresentationGraphicOccurrenceAnchorContractTest {
    @Test
    fun `terminal bindings reuse placed Anchor point exactly`() {
        val occurrence = occurrence()

        assertEquals(
            occurrence.placedAnchors.single().point,
            occurrence.terminalBindings.single().point,
        )
    }

    @Test
    fun `duplicate placed Anchor ids fail before publication`() {
        assertFailsWith<IllegalArgumentException> {
            occurrence(
                placedAnchors = listOf(placedAnchor("A1"), placedAnchor("A1")),
                terminalBindings = emptyList(),
            )
        }
    }

    @Test
    fun `terminal binding cannot reference unplaced Anchor`() {
        assertFailsWith<IllegalArgumentException> {
            occurrence(
                placedAnchors = listOf(placedAnchor("A1")),
                terminalBindings = listOf(terminalBinding("A2")),
            )
        }
    }

    private fun occurrence(
        placedAnchors: List<PresentationPlacedAnchor> = listOf(placedAnchor("A1")),
        terminalBindings: List<PresentationGraphicTerminalBinding> = listOf(terminalBinding("A1")),
    ): PresentationGraphicOccurrence = PresentationGraphicOccurrence(
        occurrenceId = RepresentationOccurrenceId("drawing:component:K1"),
        semanticSubjectId = "component:K1",
        physicalComponentId = "component:K1",
        functionId = null,
        bounds = PresentationDrawingBounds(10, 20, 80, 40),
        orientation = LayoutOrientation.VERTICAL,
        deviceLabel = "K1",
        modelLabel = null,
        packageId = "athena.test",
        definitionId = "test.contactor",
        bindingRuleId = "binding:test",
        graphic = graphic(),
        placedAnchors = placedAnchors,
        terminalBindings = terminalBindings,
        labels = emptyList(),
        sourceProvenance = listOf("source:test.athena:1:1"),
    )

    private fun placedAnchor(anchorId: String): PresentationPlacedAnchor = PresentationPlacedAnchor(
        anchorId = RepresentationAnchorId(anchorId),
        geometryRef = "anchor:$anchorId",
        primitiveId = GraphicPrimitiveId("line"),
        point = SchematicRoutePoint(10, 30),
        role = RepresentationAnchorRole.TERMINAL,
        required = true,
        sourceProvenance = listOf("source:test.athena:1:1"),
    )

    private fun terminalBinding(anchorId: String): PresentationGraphicTerminalBinding =
        PresentationGraphicTerminalBinding(
            portSemanticId = "port:K1.$anchorId",
            bindingId = RepresentationPortAnchorBindingId("binding:K1:$anchorId"),
            anchorId = anchorId,
            terminalIdentity = anchorId,
            point = SchematicRoutePoint(10, 30),
            labelPoint = SchematicRoutePoint(0, 22),
            side = TerminalSide.LEFT,
        )

    private fun graphic(): GraphicPrimitiveDocument = GraphicPrimitiveDocument(
        documentId = GraphicPrimitiveDocumentId("test.contactor"),
        bounds = GraphicBounds(10.0, 20.0, 80.0, 40.0),
        primitives = listOf(
            GraphicPrimitive.Line(
                primitiveId = GraphicPrimitiveId("line"),
                bounds = GraphicBounds(10.0, 30.0, 20.0, 1.0),
                start = GraphicPoint(10.0, 30.0),
                end = GraphicPoint(30.0, 30.0),
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
    )
}
