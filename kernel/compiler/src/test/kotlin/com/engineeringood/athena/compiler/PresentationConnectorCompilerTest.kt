package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutOrientation
import com.engineeringood.athena.layout.LayoutSnapshotId
import com.engineeringood.athena.presentation.PresentationDrawingBounds
import com.engineeringood.athena.presentation.PresentationGraphicOccurrence
import com.engineeringood.athena.presentation.PresentationGraphicTerminalBinding
import com.engineeringood.athena.presentation.PresentationPlacedAnchor
import com.engineeringood.athena.presentation.PresentationPoint
import com.engineeringood.athena.representation.GraphicBounds
import com.engineeringood.athena.representation.GraphicPrimitive
import com.engineeringood.athena.representation.GraphicPrimitiveDocument
import com.engineeringood.athena.representation.GraphicPrimitiveDocumentId
import com.engineeringood.athena.representation.GraphicPrimitiveId
import com.engineeringood.athena.representation.GraphicStyleTokenId
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationAnchorRole
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationPortAnchorBindingId
import com.engineeringood.athena.routing.ConnectionPresentationLineEvidence
import com.engineeringood.athena.routing.ConnectionLineClassId
import com.engineeringood.athena.routing.DrawingCrossingBehavior
import com.engineeringood.athena.routing.DrawingEndpointBehavior
import com.engineeringood.athena.routing.DrawingLabelPolicy
import com.engineeringood.athena.routing.DrawingProfileId
import com.engineeringood.athena.routing.DrawingStyle
import com.engineeringood.athena.routing.ElectricalConnectionId
import com.engineeringood.athena.routing.ElectricalPortId
import com.engineeringood.athena.routing.ElectricalPortRole
import com.engineeringood.athena.routing.RouteBundleId
import com.engineeringood.athena.routing.RouteFact
import com.engineeringood.athena.routing.RouteIntentId
import com.engineeringood.athena.routing.RouteQualityMetrics
import com.engineeringood.athena.routing.SchematicLabelAnchorRelation
import com.engineeringood.athena.routing.SchematicLabelId
import com.engineeringood.athena.routing.SchematicLabelPlacement
import com.engineeringood.athena.routing.RouteLabelFact
import com.engineeringood.athena.routing.SchematicRouteId
import com.engineeringood.athena.routing.SchematicRouteLane
import com.engineeringood.athena.routing.SchematicRoutePoint
import com.engineeringood.athena.routing.SchematicRouteSegment
import com.engineeringood.athena.routing.SchematicRouteSegmentOrientation
import com.engineeringood.athena.routing.LineStyleId
import com.engineeringood.athena.routing.TerminalAnchorFact
import com.engineeringood.athena.routing.TerminalAnchorId
import com.engineeringood.athena.routing.TerminalSide
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PresentationConnectorCompilerTest {
    @Test
    fun `lowering replaces route endpoints with exact placed Anchor points`() {
        val result = PresentationConnectorCompiler().compile(
            routeFacts = listOf(routeFact()),
            occurrences = listOf(occurrence("source", "port:source.out", "anchor:source", 10, 10), occurrence("target", "port:target.in", "anchor:target", 80, 40)),
            lineEvidence = listOf(lineEvidence()),
        )

        assertTrue(result.diagnostics.isEmpty())
        val connector = result.connectors.single()
        assertEquals(PresentationPoint(10, 10), connector.sourceEndpoint.point)
        assertEquals(PresentationPoint(80, 40), connector.targetEndpoint.point)
        assertEquals(connector.sourceEndpoint.point, connector.routePoints.first())
        assertEquals(connector.targetEndpoint.point, connector.routePoints.last())
        assertEquals("binding:source", connector.sourceEndpoint.bindingId.value)
        assertEquals("line:control", connector.line.classId)
        assertEquals("stroke:control", connector.line.lineStyleId)
        assertEquals("route:source-to-target", connector.routeId)
        assertTrue(connector.tokenOverrides.isEmpty())
    }

    @Test
    fun `lowering removes zero length and redundant orthogonal points only`() {
        val route = routeFact(
            segments = listOf(
                segment(0, 10, 10, 10),
                segment(10, 10, 40, 10),
                segment(40, 10, 60, 10),
                segment(60, 10, 90, 10),
            ),
        )
        val result = PresentationConnectorCompiler().compile(
            routeFacts = listOf(route),
            occurrences = listOf(occurrence("source", "port:source.out", "anchor:source", 10, 10), occurrence("target", "port:target.in", "anchor:target", 80, 10)),
            lineEvidence = listOf(lineEvidence()),
        )

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(
            listOf(PresentationPoint(10, 10), PresentationPoint(80, 10)),
            result.connectors.single().routePoints,
        )
    }

    @Test
    fun `lowering publishes line appearance and labels as typed facts`() {
        val result = PresentationConnectorCompiler().compile(
            routeFacts = listOf(
                routeFact(
                    labels = listOf(
                        RouteLabelFact(
                            labelId = SchematicLabelId("label:route:source-to-target"),
                            text = "CTRL",
                            anchorRouteId = SchematicRouteId("route:source-to-target"),
                            placement = SchematicLabelPlacement(
                                origin = SchematicRoutePoint(45, 10),
                                relation = SchematicLabelAnchorRelation.ABOVE,
                            ),
                        ),
                    ),
                ),
            ),
            occurrences = listOf(occurrence("source", "port:source.out", "anchor:source", 10, 10), occurrence("target", "port:target.in", "anchor:target", 80, 40)),
            lineEvidence = listOf(lineEvidence()),
        )

        assertTrue(result.diagnostics.isEmpty())
        val connector = result.connectors.single()
        assertEquals("stroke:control", connector.line.lineStyleId)
        assertEquals(1.0, connector.line.weight)
        assertEquals("CTRL", connector.labels.single().text)
        assertEquals(PresentationPoint(45, 10), connector.labels.single().point)
        assertTrue(connector.tokenOverrides.isEmpty())
    }

    @Test
    fun `lowering rejects non orthogonal route after endpoint attachment`() {
        val route = routeFact(
            segments = listOf(
                segment(0, 0, 40, 0),
                segment(40, 0, 80, 0),
            ),
        )
        val result = PresentationConnectorCompiler().compile(
            routeFacts = listOf(route),
            occurrences = listOf(occurrence("source", "port:source.out", "anchor:source", 10, 10), occurrence("target", "port:target.in", "anchor:target", 80, 10)),
            lineEvidence = listOf(lineEvidence()),
        )

        assertEquals(listOf("drawing.connector.route.non-orthogonal"), result.diagnostics.map { diagnostic -> diagnostic.code })
        assertTrue(result.connectors.isEmpty())
    }

    private fun occurrence(
        id: String,
        portId: String,
        anchorId: String,
        x: Int,
        y: Int,
    ): PresentationGraphicOccurrence = PresentationGraphicOccurrence(
        occurrenceId = RepresentationOccurrenceId("drawing:component:$id"),
        semanticSubjectId = "component:$id",
        physicalComponentId = "component:$id",
        functionId = null,
        bounds = PresentationDrawingBounds(x - 5, y - 5, 20, 20),
        orientation = LayoutOrientation.HORIZONTAL,
        deviceLabel = id,
        modelLabel = null,
        packageId = "athena.test",
        definitionId = "test.$id",
        bindingRuleId = "binding:test",
        graphic = GraphicPrimitiveDocument(
            documentId = GraphicPrimitiveDocumentId("test.$id"),
            bounds = GraphicBounds((x - 5).toDouble(), (y - 5).toDouble(), 20.0, 20.0),
            primitives = listOf(
                GraphicPrimitive.Rectangle(
                    primitiveId = GraphicPrimitiveId("body:$id"),
                    bounds = GraphicBounds((x - 5).toDouble(), (y - 5).toDouble(), 20.0, 20.0),
                    cornerRadius = 0.0,
                    styleTokenId = GraphicStyleTokenId("stroke"),
                ),
            ),
            styleTokens = emptyList(),
        ),
        placedAnchors = listOf(
            PresentationPlacedAnchor(
                anchorId = RepresentationAnchorId(anchorId),
                geometryRef = anchorId,
                primitiveId = GraphicPrimitiveId("body:$id"),
                point = SchematicRoutePoint(x, y),
                role = RepresentationAnchorRole.TERMINAL,
                required = true,
                sourceProvenance = listOf("source:$id.athena:1:1"),
            ),
        ),
        terminalBindings = listOf(
            PresentationGraphicTerminalBinding(
                portSemanticId = portId,
                bindingId = RepresentationPortAnchorBindingId("binding:$id"),
                anchorId = anchorId,
                terminalIdentity = id,
                point = SchematicRoutePoint(x, y),
                labelPoint = SchematicRoutePoint(x + 10, y - 8),
                side = TerminalSide.RIGHT,
            ),
        ),
        labels = emptyList(),
        sourceProvenance = listOf("source:$id.athena:1:1"),
    )

    private fun routeFact(
        segments: List<SchematicRouteSegment> = listOf(
            segment(0, 10, 50, 10),
            segment(50, 10, 50, 40),
            segment(50, 40, 90, 40),
        ),
        labels: List<RouteLabelFact> = emptyList(),
    ): RouteFact {
        val connectionId = ElectricalConnectionId("connection:source-to-target")
        return RouteFact(
            routeId = SchematicRouteId("route:source-to-target"),
            snapshotId = LayoutSnapshotId("snapshot:test"),
            connectionId = connectionId,
            routeIntentId = RouteIntentId("intent:source-to-target"),
            bundleId = RouteBundleId("bundle:source-to-target"),
            selectedChannelIds = listOf("channel:test"),
            plannerId = "test",
            compilerSnapshotId = "compiler:test",
            provenance = SourceProvenance("source:test.athena", 1, 1, 1, 1),
            qualityMetrics = RouteQualityMetrics(0, 0, 0, 0, 0, 0),
            source = terminalAnchor("source", "out", "anchor:source", 0, 0, ElectricalPortRole.OUTPUT),
            target = terminalAnchor("target", "in", "anchor:target", 80, 0, ElectricalPortRole.INPUT),
            segments = segments,
            lane = SchematicRouteLane(0),
            labels = labels,
        )
    }

    private fun terminalAnchor(
        subject: String,
        port: String,
        anchorId: String,
        x: Int,
        y: Int,
        role: ElectricalPortRole,
    ): TerminalAnchorFact = TerminalAnchorFact(
        anchorId = TerminalAnchorId(anchorId),
        subjectId = StableSemanticIdentity("component:$subject"),
        occurrenceId = LayoutOccurrenceId("occurrence:component:$subject"),
        portId = ElectricalPortId("$subject.$port"),
        portSemanticId = StableSemanticIdentity("port:$subject.$port"),
        portRole = role,
        side = TerminalSide.RIGHT,
        point = SchematicRoutePoint(x, y),
        gridPoint = SchematicRoutePoint(x, y),
    )

    private fun segment(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
    ): SchematicRouteSegment = SchematicRouteSegment(
        start = SchematicRoutePoint(startX, startY),
        end = SchematicRoutePoint(endX, endY),
        orientation = if (startY == endY) SchematicRouteSegmentOrientation.HORIZONTAL else SchematicRouteSegmentOrientation.VERTICAL,
    )

    private fun lineEvidence(): ConnectionPresentationLineEvidence = ConnectionPresentationLineEvidence(
        routeId = SchematicRouteId("route:source-to-target"),
        connectionId = ElectricalConnectionId("connection:source-to-target"),
        profileId = DrawingProfileId("profile:test"),
        lineClassId = ConnectionLineClassId("line:control"),
        lineStyleId = LineStyleId("stroke:control"),
        weight = 1.0,
        style = DrawingStyle.SOLID,
        colorKey = "drawing.control",
        endpointBehavior = DrawingEndpointBehavior.ATTACH_TO_ANCHOR,
        labelPolicy = DrawingLabelPolicy.TERMINAL_PAIR,
        crossingBehavior = DrawingCrossingBehavior.DISCONNECTED_CROSSING,
        selectedPolicyId = "policy:test",
        compilerSnapshotId = "compiler:test",
        provenance = SourceProvenance("source:test.athena", 1, 1, 1, 1),
    )
}
