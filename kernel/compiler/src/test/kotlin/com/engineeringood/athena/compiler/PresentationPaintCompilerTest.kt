package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.presentation.PresentationBounds
import com.engineeringood.athena.presentation.PresentationConnectionMarker
import com.engineeringood.athena.presentation.PresentationConnectionMarkerId
import com.engineeringood.athena.presentation.PresentationConnectionMarkerKind
import com.engineeringood.athena.presentation.PresentationConnector
import com.engineeringood.athena.presentation.PresentationConnectorEndpoint
import com.engineeringood.athena.presentation.PresentationConnectorLabel
import com.engineeringood.athena.presentation.PresentationConnectorLabelDisplay
import com.engineeringood.athena.presentation.PresentationConnectorLine
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationDrawingBounds
import com.engineeringood.athena.presentation.PresentationLayer
import com.engineeringood.athena.presentation.PresentationOccurrence
import com.engineeringood.athena.presentation.PresentationOccurrenceId
import com.engineeringood.athena.presentation.PresentationPaintItem
import com.engineeringood.athena.presentation.PresentationPoint
import com.engineeringood.athena.presentation.PresentationPrimitiveId
import com.engineeringood.athena.presentation.PresentationPrimitiveOccurrenceReference
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationPortAnchorBindingId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class PresentationPaintCompilerTest {
    @Test
    fun `presentation paint compiler creates one paint item for each visible target`() {
        val plan = PresentationPaintCompiler().compile(document())

        assertEquals(
            listOf(
                "paint:occurrence:supply",
                "paint:route:main",
                "marker:main",
                "label:route:main",
            ),
            plan.items.map { item -> item.targetId },
        )
        assertEquals(listOf("shape", "connector", "marker", "label"), plan.items.map { item -> item.kind })
        assertEquals(listOf(10, 20, 30, 40), plan.items.map { item -> item.order })
        assertEquals(listOf(true, true, true, true), plan.items.map { item -> item.visible })
    }

    @Test
    fun `connector label carries explicit target and position`() {
        val label = document().connectors.single().labels.single()

        assertEquals("paint:route:main", label.targetId)
        assertEquals(PresentationPoint(40, 10), label.point)
    }

    @Test
    fun `invalid paint item fails before publication`() {
        assertFailsWith<IllegalArgumentException> {
            PresentationPaintItem(
                itemId = "paint:item:bad",
                targetId = "paint:route:main",
                kind = "connector",
                visible = true,
                order = -1,
            )
        }
    }

    @Test
    fun `new paint names stay direct and clean`() {
        val names = listOf(
            PresentationPaintCompiler::class.simpleName.orEmpty(),
            PresentationPaintItem::class.simpleName.orEmpty(),
        )
        val banned = listOf("M39", "V0", "V1", "Evidence", "ProfessionalControlDrawing", "Compatibility")

        banned.forEach { token ->
            assertFalse(
                names.any { name -> name.contains(token) },
                "Presentation paint names must not contain `$token`: $names",
            )
        }
    }

    private fun document(): PresentationDocument {
        val connectorId = PresentationOccurrenceId("paint:route:main")
        return PresentationDocument(
            view = ViewDefinition("paint-test", "Paint Test"),
            canvasWidth = 200,
            canvasHeight = 100,
            primitivePacks = emptyList(),
            compositePacks = emptyList(),
            occurrences = listOf(
                PresentationOccurrence(
                    occurrenceId = PresentationOccurrenceId("paint:occurrence:supply"),
                    semanticId = StableSemanticIdentity("component:Supply"),
                    reference = PresentationPrimitiveOccurrenceReference(PresentationPrimitiveId("shape:device")),
                    bounds = PresentationBounds(0, 0, 80, 40),
                    layer = PresentationLayer.DEVICE,
                ),
            ),
            connectors = listOf(connector(connectorId)),
            connectionMarkers = listOf(marker(connectorId)),
        )
    }

    private fun connector(connectorId: PresentationOccurrenceId): PresentationConnector =
        PresentationConnector(
            occurrenceId = connectorId,
            semanticId = StableSemanticIdentity("connection:main"),
            primitiveId = PresentationPrimitiveId("electrical.conductor.orthogonal"),
            routePoints = listOf(PresentationPoint(10, 10), PresentationPoint(70, 10)),
            line = PresentationConnectorLine(
                classId = "line:control",
                lineKind = "CONTROL",
                lineStyleId = "stroke:control",
                weight = 1.0,
                style = "solid",
                colorKey = "drawing.control",
                endpointBehavior = "attached",
                labelPolicy = "terminal-pair",
                crossingBehavior = "recorded-by-spatial",
                policyId = "policy:test",
                compilerSnapshotId = "compiler:test",
            ),
            routeId = "route:main",
            bundleId = "bundle:main",
            laneId = "lane:main",
            laneRouteIds = listOf("route:main"),
            selectedChannelIds = emptyList(),
            labels = listOf(
                PresentationConnectorLabel(
                    labelId = "label:route:main",
                    targetId = connectorId.value,
                    text = "main",
                    point = PresentationPoint(40, 10),
                    bounds = PresentationDrawingBounds(30, 0, 28, 14),
                    labelClassId = "label:route",
                    display = PresentationConnectorLabelDisplay.ALWAYS,
                    sourceProvenance = listOf("source.athena:1:1"),
                    compilerSnapshotId = "compiler:test",
                ),
            ),
            quality = "spatial-owned",
            sourceEndpoint = endpoint("source", PresentationPoint(10, 10)),
            targetEndpoint = endpoint("target", PresentationPoint(70, 10)),
            markerIds = listOf(PresentationConnectionMarkerId("marker:main")),
            sourceProjectionIds = listOf("route:main"),
            sourceSpan = LayoutSourceSpan("source.athena", 1, 1, 1, 20),
        )

    private fun marker(connectorId: PresentationOccurrenceId): PresentationConnectionMarker =
        PresentationConnectionMarker(
            markerId = PresentationConnectionMarkerId("marker:main"),
            kind = PresentationConnectionMarkerKind.JUNCTION,
            point = PresentationPoint(40, 10),
            routeIds = listOf("route:main"),
            connectorIds = listOf(connectorId),
            semanticId = StableSemanticIdentity("port:joined"),
            joined = true,
            appearanceClassId = "marker:junction-dot",
            sourceProjectionIds = listOf("marker:main", "route:main"),
            sourceProvenance = listOf("source.athena:1:1"),
            compilerSnapshotId = "compiler:test",
        )

    private fun endpoint(id: String, point: PresentationPoint): PresentationConnectorEndpoint =
        PresentationConnectorEndpoint(
            portSemanticId = StableSemanticIdentity("port:$id"),
            bindingId = RepresentationPortAnchorBindingId("binding:$id"),
            occurrenceId = RepresentationOccurrenceId("occurrence:$id"),
            anchorId = RepresentationAnchorId("anchor:$id"),
            point = point,
            sourceProvenance = listOf("source.athena:1:1"),
        )
}
