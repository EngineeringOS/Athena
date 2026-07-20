package com.engineeringood.athena.presentation

import com.engineeringood.athena.document.CrossReferenceRelationType
import com.engineeringood.athena.document.DocumentOccurrenceDetailRole
import com.engineeringood.athena.document.DocumentOccurrenceRole
import com.engineeringood.athena.document.DocumentProjectionEntryPoint
import com.engineeringood.athena.document.DocumentProjectionSourceUnitSummary
import com.engineeringood.athena.document.DocumentProjectionSubjectSummary
import com.engineeringood.athena.document.DocumentProjectionWorkspaceSemanticSnapshot
import com.engineeringood.athena.document.SheetViewId
import com.engineeringood.athena.document.SheetViewRole
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.routing.ElectricalConnectionId
import com.engineeringood.athena.routing.ElectricalPortId
import com.engineeringood.athena.routing.ElectricalPortRole
import com.engineeringood.athena.routing.RouteFact
import com.engineeringood.athena.routing.RouteFactSnapshot
import com.engineeringood.athena.routing.RouteQualityState
import com.engineeringood.athena.routing.SchematicRouteId
import com.engineeringood.athena.routing.SchematicRouteLane
import com.engineeringood.athena.routing.SchematicRoutePoint
import com.engineeringood.athena.routing.SchematicRouteSegment
import com.engineeringood.athena.routing.SchematicRouteSegmentOrientation
import com.engineeringood.athena.routing.TerminalAnchorFact
import com.engineeringood.athena.routing.TerminalAnchorId
import com.engineeringood.athena.routing.TerminalSide
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PresentationModelContractTest {
    @Test
    fun `presentation document stays domain neutral and downstream of view contract`() {
        val document = PresentationDocument(
            view = ViewDefinition(
                id = "cabinet",
                displayName = "Cabinet",
                layoutIntent = LayoutIntent.STRUCTURAL,
            ),
            canvasWidth = 640,
            canvasHeight = 360,
            primitivePacks = listOf(
                PresentationPrimitivePack(
                    packId = PresentationPackId("electrical-primitives/default-v1"),
                    displayName = "Electrical primitives",
                    familyIds = setOf("electrical/cabinet"),
                    primitives = listOf(
                        PresentationPrimitiveDefinition(
                            primitiveId = PresentationPrimitiveId("electrical.mark.contact-open"),
                            displayName = "Open contact mark",
                            viewBoxWidth = 24,
                            viewBoxHeight = 24,
                            commands = listOf(
                                PresentationStrokeLine(
                                    start = PresentationPoint(4, 12),
                                    end = PresentationPoint(20, 12),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            compositePacks = listOf(
                PresentationCompositePack(
                    packId = PresentationPackId("electrical-composites/default-v1"),
                    displayName = "Electrical composites",
                    familyIds = setOf("electrical/cabinet"),
                    composites = listOf(
                        PresentationCompositeDefinition(
                            compositeId = PresentationCompositeId("electrical.device.switch-panel"),
                            displayName = "Switch panel",
                            viewBoxWidth = 140,
                            viewBoxHeight = 72,
                            parts = listOf(
                                PresentationCompositePart(
                                    partId = "contact",
                                    primitiveId = PresentationPrimitiveId("electrical.mark.contact-open"),
                                    bounds = PresentationBounds(x = 84, y = 24, width = 24, height = 24),
                                ),
                            ),
                            textSlots = listOf(
                                PresentationTextSlot(
                                    slotId = PresentationTextSlotId("subject-label"),
                                    origin = PresentationPoint(x = 8, y = 16),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            occurrences = listOf(
                PresentationOccurrence(
                    occurrenceId = PresentationOccurrenceId("cabinet/presentation/component_PLC1"),
                    semanticId = StableSemanticIdentity("component:PLC1"),
                    reference = PresentationCompositeOccurrenceReference(
                        compositeId = PresentationCompositeId("electrical.device.switch-panel"),
                    ),
                    bounds = PresentationBounds(x = 40, y = 60, width = 140, height = 72),
                    layer = PresentationLayer.DEVICE,
                    textValues = mapOf(PresentationTextSlotId("subject-label") to "PLC1"),
                    sourceProjectionIds = listOf("cabinet/projection/node/component_PLC1"),
                ),
            ),
            connectors = listOf(
                PresentationConnector(
                    occurrenceId = PresentationOccurrenceId("cabinet/presentation/connection_PLC1_out_M1_in"),
                    semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    primitiveId = PresentationPrimitiveId("electrical.conductor.orthogonal"),
                    routePoints = listOf(
                        PresentationPoint(x = 104, y = 86),
                        PresentationPoint(x = 210, y = 86),
                        PresentationPoint(x = 316, y = 86),
                    ),
                    sourceProjectionIds = listOf("cabinet/projection/connection/connection_PLC1_out_M1_in"),
                ),
            ),
        )

        assertEquals("cabinet", document.view.id)
        assertEquals("electrical-primitives/default-v1", document.primitivePacks.single().packId.value)
        assertEquals("electrical-composites/default-v1", document.compositePacks.single().packId.value)
        assertEquals("component:PLC1", document.occurrences.single().semanticId.value)
        assertEquals(
            "cabinet/projection/node/component_PLC1",
            document.occurrences.single().sourceProjectionIds.single(),
        )
        assertEquals(
            "cabinet/projection/connection/connection_PLC1_out_M1_in",
            document.connectors.single().sourceProjectionIds.single(),
        )
        assertIs<PresentationCompositeOccurrenceReference>(document.occurrences.single().reference)
    }

    @Test
    fun `presentation layer does not absorb semantic macro or backend draw trees`() {
        val primitive = PresentationPrimitiveDefinition(
            primitiveId = PresentationPrimitiveId("electrical.mark.motor"),
            displayName = "Motor mark",
            viewBoxWidth = 32,
            viewBoxHeight = 32,
            commands = listOf(
                PresentationCircle(
                    center = PresentationPoint(x = 16, y = 16),
                    radius = 10,
                ),
                PresentationSvgPath(
                    pathData = "M 10 10 L 22 22",
                ),
            ),
            tokenDefaults = mapOf(
                "stroke" to "#1f1f1f",
                "strokeWidth" to "1.6",
            ),
        )

        assertTrue(primitive.tokenDefaults.containsKey("stroke"))
        assertEquals(2, primitive.commands.size)
        assertIs<PresentationCircle>(primitive.commands.first())
        assertIs<PresentationSvgPath>(primitive.commands.last())
    }

    @Test
    fun `presentation snapshots expose route facts instead of accepting old edge route points`() {
        val snapshotId = LayoutSnapshotId("snapshot:m24:presentation-route-facts")
        val routeFact = routeFact(snapshotId)
        val document = PresentationDocument(
            view = ViewDefinition(
                id = "schematic",
                displayName = "Schematic",
                layoutIntent = LayoutIntent.CONNECTIVITY,
            ),
            canvasWidth = 640,
            canvasHeight = 360,
            primitivePacks = emptyList(),
            compositePacks = emptyList(),
            occurrences = emptyList(),
            connectors = listOf(
                PresentationConnector(
                    occurrenceId = PresentationOccurrenceId("schematic/presentation/old-edge"),
                    semanticId = StableSemanticIdentity("connection:PLC1.DO1->XT1.1"),
                    primitiveId = PresentationPrimitiveId("electrical.conductor.generic"),
                    routePoints = listOf(
                        PresentationPoint(x = 320, y = 180),
                        PresentationPoint(x = 520, y = 180),
                    ),
                ),
            ),
            routeFactSnapshot = RouteFactSnapshot.canonical(
                snapshotId = snapshotId,
                family = "schematic",
                routeFacts = listOf(routeFact),
            ),
        )

        val connector = document.connectorsForRendering().single()

        assertEquals("route:PLC1.DO1->XT1.1", connector.occurrenceId.value)
        assertEquals(StableSemanticIdentity("connection:PLC1.DO1->XT1.1"), connector.semanticId)
        assertEquals(
            listOf(
                PresentationPoint(x = 320, y = 180),
                PresentationPoint(x = 340, y = 180),
                PresentationPoint(x = 340, y = 220),
                PresentationPoint(x = 520, y = 220),
            ),
            connector.routePoints,
        )
        assertEquals("anchor:PLC1:DO1", connector.sourceAnchorId)
        assertEquals("anchor:XT1:1", connector.targetAnchorId)
        assertEquals(StableSemanticIdentity("port:PLC1.DO1"), connector.sourcePortSemanticId)
        assertEquals(StableSemanticIdentity("port:XT1.1"), connector.targetPortSemanticId)
        assertEquals("0", connector.tokenOverrides["routeLane"])
        assertEquals(RouteQualityState.SATISFIED.name, connector.tokenOverrides["routeQuality"])
    }

    @Test
    fun `presentation reference markers keep compact notation with canonical payload`() {
        val routeIdentity = StableSemanticIdentity("connection:PLC1.Q0.0->XT1.1")
        val sourceTerminal = StableSemanticIdentity("terminal:PLC1.Q0.0")
        val targetTerminal = StableSemanticIdentity("terminal:XT1.1")
        val documentProjection = DocumentProjectionEntryPoint.projectWorkspace(
            DocumentProjectionWorkspaceSemanticSnapshot(
                semanticGraphId = "graph:presentation-markers",
                sourceUnits = listOf(DocumentProjectionSourceUnitSummary("source:system", "src/system.athena")),
                subjects = listOf(
                    DocumentProjectionSubjectSummary(
                        canonicalSubjectId = routeIdentity,
                        occurrenceRole = DocumentOccurrenceRole.ROUTE,
                        detailRole = DocumentOccurrenceDetailRole.ROUTE,
                        sheetViewRoles = listOf(
                            SheetViewRole.CONTROL_AND_PLC_LOGIC,
                            SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION,
                        ),
                        sourceTerminalIdentity = sourceTerminal,
                        targetTerminalIdentity = targetTerminal,
                    ),
                    DocumentProjectionSubjectSummary(
                        canonicalSubjectId = sourceTerminal,
                        occurrenceRole = DocumentOccurrenceRole.TERMINAL,
                        detailRole = DocumentOccurrenceDetailRole.TERMINAL,
                        sheetViewRoles = listOf(SheetViewRole.CONTROL_AND_PLC_LOGIC),
                    ),
                    DocumentProjectionSubjectSummary(
                        canonicalSubjectId = targetTerminal,
                        occurrenceRole = DocumentOccurrenceRole.TERMINAL,
                        detailRole = DocumentOccurrenceDetailRole.TERMINAL,
                        sheetViewRoles = listOf(SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION),
                    ),
                ),
            ),
        )

        val markers = documentReferenceMarkersForSheetView(
            documentProjection = documentProjection,
            selectedSheetViewId = SheetViewId("sheet-view:control-and-plc-logic"),
        )
        val markerByRelation = markers.associateBy(PresentationReferenceMarkerFact::relationType)
        val routeMarker = markerByRelation.getValue(CrossReferenceRelationType.ROUTE_CONTINUATION)
        val terminalMarker = markerByRelation.getValue(CrossReferenceRelationType.TERMINAL_CONTINUATION)

        assertEquals(PresentationReferenceMarkerKind.CONTINUATION, routeMarker.markerKind)
        assertEquals(routeIdentity, routeMarker.sourceIdentity)
        assertEquals(routeIdentity, routeMarker.targetIdentity)
        assertEquals("sheet-view:field-wiring-and-terminal-transition A1", routeMarker.compactNotation)
        assertTrue(routeMarker.compactNotation.contains("field-wiring"))
        assertFalse(routeMarker.compactNotation.contains("connection:"))

        assertEquals(PresentationReferenceMarkerKind.CONTINUATION, terminalMarker.markerKind)
        assertEquals(sourceTerminal, terminalMarker.sourceIdentity)
        assertEquals(targetTerminal, terminalMarker.targetIdentity)
        assertEquals("sheet-view:field-wiring-and-terminal-transition B2", terminalMarker.compactNotation)
        assertFalse(terminalMarker.compactNotation.contains("terminal:"))

        val document = PresentationDocument(
            view = ViewDefinition(
                id = "schematic",
                displayName = "Schematic",
                layoutIntent = LayoutIntent.CONNECTIVITY,
            ),
            canvasWidth = 640,
            canvasHeight = 360,
            primitivePacks = emptyList(),
            compositePacks = emptyList(),
            occurrences = emptyList(),
            referenceMarkers = markers,
        )

        assertEquals(markers, document.referenceMarkers)
    }

    private fun routeFact(snapshotId: LayoutSnapshotId): RouteFact {
        val connectionId = ElectricalConnectionId("connection:PLC1.DO1->XT1.1")
        val source = terminalAnchor("PLC1", "DO1", ElectricalPortRole.OUTPUT, TerminalSide.RIGHT, 320, 180)
        val target = terminalAnchor("XT1", "1", ElectricalPortRole.TERMINAL, TerminalSide.LEFT, 520, 220)
        return RouteFact(
            routeId = SchematicRouteId("route:PLC1.DO1->XT1.1"),
            snapshotId = snapshotId,
            connectionId = connectionId,
            source = source,
            target = target,
            lane = SchematicRouteLane(0),
            segments = listOf(
                SchematicRouteSegment(
                    start = SchematicRoutePoint(x = 320, y = 180),
                    end = SchematicRoutePoint(x = 340, y = 180),
                    orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
                ),
                SchematicRouteSegment(
                    start = SchematicRoutePoint(x = 340, y = 180),
                    end = SchematicRoutePoint(x = 340, y = 220),
                    orientation = SchematicRouteSegmentOrientation.VERTICAL,
                ),
                SchematicRouteSegment(
                    start = SchematicRoutePoint(x = 340, y = 220),
                    end = SchematicRoutePoint(x = 520, y = 220),
                    orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
                ),
            ),
        )
    }

    private fun terminalAnchor(
        subject: String,
        port: String,
        role: ElectricalPortRole,
        side: TerminalSide,
        x: Int,
        y: Int,
    ): TerminalAnchorFact {
        return TerminalAnchorFact(
            anchorId = TerminalAnchorId("anchor:$subject:$port"),
            subjectId = StableSemanticIdentity("component:$subject"),
            occurrenceId = LayoutOccurrenceId("occurrence:component:$subject"),
            portId = ElectricalPortId("$subject.$port"),
            portSemanticId = StableSemanticIdentity("port:$subject.$port"),
            portRole = role,
            side = side,
            point = SchematicRoutePoint(x = x, y = y),
            gridPoint = SchematicRoutePoint(x = x, y = y),
        )
    }
}
