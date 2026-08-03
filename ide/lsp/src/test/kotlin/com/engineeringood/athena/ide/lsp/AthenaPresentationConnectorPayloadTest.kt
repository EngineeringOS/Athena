package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.presentation.PresentationConnector
import com.engineeringood.athena.presentation.PresentationConnectorEndpoint
import com.engineeringood.athena.presentation.PresentationConnectorLabel
import com.engineeringood.athena.presentation.PresentationConnectorLabelDisplay
import com.engineeringood.athena.presentation.PresentationConnectorLine
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationDrawingBounds
import com.engineeringood.athena.presentation.PresentationOccurrenceId
import com.engineeringood.athena.presentation.PresentationPaintItem
import com.engineeringood.athena.presentation.PresentationPaintPlan
import com.engineeringood.athena.presentation.PresentationPoint
import com.engineeringood.athena.presentation.PresentationPrimitiveId
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationPortAnchorBindingId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AthenaPresentationConnectorPayloadTest {
    @Test
    fun `connector payload is the only public visible route evidence`() {
        val payload = presentationDocument()
            .toPayload()
            .connectors
            .single()

        assertEquals("connection:main", payload.semanticId)
        assertEquals(listOf(AthenaPointPayload(10, 10), AthenaPointPayload(70, 10)), payload.routePoints)
        assertEquals("line:power", payload.line.classId)
        assertEquals("stroke:power", payload.line.lineStyleId)
        assertEquals("route:main", payload.routeId)
        assertEquals("lane:main", payload.laneId)
        assertEquals(listOf("route:connection:main"), payload.laneRouteIds)
        assertEquals(listOf("channel:power"), payload.selectedChannelIds)
        assertEquals("main", payload.labels.single().text)
        assertEquals("always", payload.labels.single().display)
        assertEquals("SATISFIED", payload.quality)
        assertEquals(AthenaPresentationSourceSpanPayload("routes.athena", 4, 1, 4, 40), payload.sourceSpan)
        assertEquals(payload.sourceSpan, payload.trace?.sourceSpan)
        assertEquals("anchor:source", payload.sourceEndpoint.anchorId)
        assertEquals("anchor:target", payload.targetEndpoint.anchorId)
        assertEquals(
            listOf("anchor:source", "binding:source", "occurrence:source", "port:source"),
            payload.sourceEndpoint.trace?.sourceProjectionIds,
        )
        assertEquals(listOf("routes.athena:4:1"), payload.sourceEndpoint.trace?.sourceProvenance)
        assertFalse(payload.tokenOverrides.keys.any { key -> key.contains("route", ignoreCase = true) || key.contains("stroke", ignoreCase = true) })

        val wireText = payload.toString()
        listOf("<svg", "<xml", "<definition", ".elmt", "qelectrotech", "org.eclipse.elk", "DOM").forEach { forbidden ->
            assertFalse(wireText.contains(forbidden, ignoreCase = true), "Connector payload leaked `$forbidden`.")
        }
    }

    @Test
    fun `presentation payload carries paint plan as renderer input`() {
        val payload = presentationDocument()
            .copy(
                paintPlan = PresentationPaintPlan(
                    listOf(
                        PresentationPaintItem(
                            itemId = "paint:item:connector:main",
                            targetId = "route:connection:main",
                            kind = "connector",
                            visible = true,
                            order = 20,
                        ),
                        PresentationPaintItem(
                            itemId = "paint:item:label:main",
                            targetId = "label:route:connection:main",
                            kind = "label",
                            visible = false,
                            order = 40,
                        ),
                    ),
                ),
            )
            .toPayload()

        assertEquals(2, payload.paintPlan.items.size)
        assertEquals("route:connection:main", payload.paintPlan.items[0].targetId)
        assertEquals(true, payload.paintPlan.items[0].visible)
        assertEquals(20, payload.paintPlan.items[0].order)
        assertEquals("label:route:connection:main", payload.paintPlan.items[1].targetId)
        assertEquals(false, payload.paintPlan.items[1].visible)
    }

    private fun presentationDocument(): PresentationDocument =
        PresentationDocument(
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
            connectors = listOf(connector()),
            paintPlan = PresentationPaintPlan(
                listOf(
                    PresentationPaintItem(
                        itemId = "paint:item:connector:main",
                        targetId = "route:connection:main",
                        kind = "connector",
                        visible = true,
                        order = 20,
                    ),
                    PresentationPaintItem(
                        itemId = "paint:item:label:main",
                        targetId = "label:route:connection:main",
                        kind = "label",
                        visible = true,
                        order = 40,
                    ),
                ),
            ),
        )

    private fun connector(): PresentationConnector =
        PresentationConnector(
            occurrenceId = PresentationOccurrenceId("route:connection:main"),
            semanticId = StableSemanticIdentity("connection:main"),
            primitiveId = PresentationPrimitiveId("electrical.conductor.orthogonal"),
            routePoints = listOf(PresentationPoint(10, 10), PresentationPoint(70, 10)),
            line = PresentationConnectorLine(
                classId = "line:power",
                lineKind = "POWER",
                lineStyleId = "stroke:power",
                weight = 1.4,
                style = "SOLID",
                colorKey = "drawing.power",
                endpointBehavior = "ATTACH_TO_ANCHOR",
                labelPolicy = "TERMINAL_PAIR",
                crossingBehavior = "JUNCTION_REQUIRED",
                policyId = "control-drawing-iec",
                compilerSnapshotId = "snapshot:route-payload",
            ),
            routeId = "route:main",
            bundleId = "bundle:main",
            laneId = "lane:main",
            laneRouteIds = listOf("route:connection:main"),
            selectedChannelIds = listOf("channel:power"),
            labels = listOf(
                PresentationConnectorLabel(
                    labelId = "label:route:connection:main",
                    targetId = "route:connection:main",
                    text = "main",
                    point = PresentationPoint(40, 10),
                    bounds = PresentationDrawingBounds(28, 5, 24, 10),
                    labelClassId = "label:route",
                    display = PresentationConnectorLabelDisplay.ALWAYS,
                    sourceProvenance = listOf("routes.athena:4:1"),
                    compilerSnapshotId = "snapshot:route-payload",
                ),
            ),
            quality = "SATISFIED",
            sourceEndpoint = connectorEndpoint("source", PresentationPoint(10, 10)),
            targetEndpoint = connectorEndpoint("target", PresentationPoint(70, 10)),
            sourceProjectionIds = listOf("route:connection:main"),
            sourceSpan = LayoutSourceSpan("routes.athena", 4, 1, 4, 40),
        )

    private fun connectorEndpoint(name: String, point: PresentationPoint): PresentationConnectorEndpoint =
        PresentationConnectorEndpoint(
            portSemanticId = StableSemanticIdentity("port:$name"),
            bindingId = RepresentationPortAnchorBindingId("binding:$name"),
            occurrenceId = RepresentationOccurrenceId("occurrence:$name"),
            anchorId = RepresentationAnchorId("anchor:$name"),
            point = point,
            sourceProvenance = listOf("routes.athena:4:1"),
        )
}
