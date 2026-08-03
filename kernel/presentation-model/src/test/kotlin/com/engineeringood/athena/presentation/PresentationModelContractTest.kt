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
import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationPortAnchorBindingId
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
                    packId = PresentationPackId("electrical-primitives/default"),
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
                    semanticId = StableSemanticIdentity("connection:test:plc1_out_to_m1_in"),
                    primitiveId = PresentationPrimitiveId("electrical.conductor.orthogonal"),
                    routePoints = listOf(
                        PresentationPoint(x = 104, y = 86),
                        PresentationPoint(x = 210, y = 86),
                        PresentationPoint(x = 316, y = 86),
                    ),
                    line = connectorLine(),
                    routeId = "route:connection:test:plc1_out_to_m1_in",
                    bundleId = "bundle:connection:test:plc1_out_to_m1_in",
                    laneId = "lane:control",
                    laneRouteIds = listOf("cabinet/presentation/connection_PLC1_out_M1_in"),
                    selectedChannelIds = listOf("channel:control"),
                    quality = "SATISFIED",
                    sourceEndpoint = connectorEndpoint(
                        portSemanticId = "port:PLC1.out",
                        bindingId = "binding:PLC1:out",
                        occurrenceId = "drawing:component:PLC1",
                        anchorId = "anchor:PLC1:out",
                        point = PresentationPoint(x = 104, y = 86),
                    ),
                    targetEndpoint = connectorEndpoint(
                        portSemanticId = "port:M1.in",
                        bindingId = "binding:M1:in",
                        occurrenceId = "drawing:component:M1",
                        anchorId = "anchor:M1:in",
                        point = PresentationPoint(x = 316, y = 86),
                    ),
                    sourceProjectionIds = listOf("cabinet/projection/connection/connection_PLC1_out_M1_in"),
                    sourceSpan = LayoutSourceSpan("source:test.athena", 1, 1, 1, 20),
                ),
            ),
        )

        assertEquals("cabinet", document.view.id)
        assertEquals("electrical-primitives/default", document.primitivePacks.single().packId.value)
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
    fun `presentation rendering uses strict connectors instead of route fact conversion`() {
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
                    semanticId = StableSemanticIdentity("connection:test:plc1_do1_to_xt1_1"),
                    primitiveId = PresentationPrimitiveId("electrical.conductor.generic"),
                    routePoints = listOf(
                        PresentationPoint(x = 320, y = 180),
                        PresentationPoint(x = 520, y = 180),
                    ),
                    line = connectorLine(),
                    routeId = "route:connection:test:plc1_do1_to_xt1_1",
                    bundleId = "bundle:connection:test:plc1_do1_to_xt1_1",
                    laneId = "lane:control",
                    laneRouteIds = listOf("schematic/presentation/old-edge"),
                    selectedChannelIds = listOf("channel:main"),
                    quality = "SATISFIED",
                    sourceEndpoint = connectorEndpoint(
                        portSemanticId = "port:legacy.source",
                        bindingId = "binding:legacy:source",
                        occurrenceId = "drawing:component:legacy-source",
                        anchorId = "anchor:legacy:source",
                        point = PresentationPoint(x = 320, y = 180),
                    ),
                    targetEndpoint = connectorEndpoint(
                        portSemanticId = "port:legacy.target",
                        bindingId = "binding:legacy:target",
                        occurrenceId = "drawing:component:legacy-target",
                        anchorId = "anchor:legacy:target",
                        point = PresentationPoint(x = 520, y = 180),
                    ),
                    sourceSpan = LayoutSourceSpan("source:test.athena", 1, 1, 1, 20),
                ),
            ),
        )

        val connector = document.connectorsForRendering().single()

        assertEquals("schematic/presentation/old-edge", connector.occurrenceId.value)
        assertEquals(StableSemanticIdentity("connection:test:plc1_do1_to_xt1_1"), connector.semanticId)
        assertEquals(
            listOf(
                PresentationPoint(x = 320, y = 180),
                PresentationPoint(x = 520, y = 180),
            ),
            connector.routePoints,
        )
        assertEquals("anchor:legacy:source", connector.sourceEndpoint.anchorId.value)
        assertEquals("anchor:legacy:target", connector.targetEndpoint.anchorId.value)
        assertEquals(StableSemanticIdentity("port:legacy.source"), connector.sourceEndpoint.portSemanticId)
        assertEquals(StableSemanticIdentity("port:legacy.target"), connector.targetEndpoint.portSemanticId)
        assertTrue(connector.tokenOverrides.isEmpty())
    }

    @Test
    fun `presentation reference markers keep compact notation with canonical payload`() {
        val routeIdentity = StableSemanticIdentity("connection:test:plc1_q0_0_to_xt1_1")
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

    private fun connectorEndpoint(
        portSemanticId: String,
        bindingId: String,
        occurrenceId: String,
        anchorId: String,
        point: PresentationPoint,
    ): PresentationConnectorEndpoint = PresentationConnectorEndpoint(
        portSemanticId = StableSemanticIdentity(portSemanticId),
        bindingId = RepresentationPortAnchorBindingId(bindingId),
        occurrenceId = RepresentationOccurrenceId(occurrenceId),
        anchorId = RepresentationAnchorId(anchorId),
        point = point,
        sourceProvenance = listOf("source:test.athena:1:1"),
    )

    private fun connectorLine(): PresentationConnectorLine =
        PresentationConnectorLine(
            classId = "line:control",
            lineKind = "CONTROL",
            lineStyleId = "stroke:control",
            weight = 1.0,
            style = "SOLID",
            colorKey = "drawing.control",
            endpointBehavior = "ATTACH_TO_ANCHOR",
            labelPolicy = "TERMINAL_PAIR",
            crossingBehavior = "DISCONNECTED_CROSSING",
            policyId = "policy:test",
            compilerSnapshotId = "compiler:test",
        )

}
