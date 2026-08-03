package com.engineeringood.athena.renderer.svg

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.layout.ViewDefinition
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
import com.engineeringood.athena.presentation.PresentationOccurrenceId
import com.engineeringood.athena.presentation.PresentationPaintItem
import com.engineeringood.athena.presentation.PresentationPaintPlan
import com.engineeringood.athena.presentation.PresentationPoint
import com.engineeringood.athena.presentation.PresentationPrimitiveId
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationPortAnchorBindingId
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class PresentationDocumentSvgExporterTest {
    @Test
    fun `exports connectors labels and markers from one presentation document`() {
        val document = PresentationDocument(
            view = ViewDefinition("schematic", "Control Drawing", layoutIntent = LayoutIntent.CONNECTIVITY),
            canvasWidth = 640,
            canvasHeight = 360,
            primitivePacks = emptyList(),
            compositePacks = emptyList(),
            occurrences = emptyList(),
            connectors = listOf(connector("connection:main", listOf(PresentationPoint(10, 10), PresentationPoint(70, 10)))),
            connectionMarkers = listOf(
                PresentationConnectionMarker(
                    markerId = PresentationConnectionMarkerId("marker:no-connect:main"),
                    kind = PresentationConnectionMarkerKind.NO_CONNECT_CROSSING,
                    point = PresentationPoint(40, 10),
                    routeIds = listOf("route:connection:main"),
                    connectorIds = listOf(PresentationOccurrenceId("route:connection:main")),
                    semanticId = StableSemanticIdentity("connection:main"),
                    joined = false,
                    appearanceClassId = "marker:no-connect",
                    sourceProjectionIds = listOf("connection:main"),
                    sourceProvenance = listOf("source.athena:1:1"),
                    compilerSnapshotId = "snapshot:main",
                ),
            ),
            paintPlan = PresentationPaintPlan(
                listOf(
                    paint("paint:item:connector:main", "route:connection:main", "connector", visible = true, order = 1),
                    paint("paint:item:label:main", "label:connection:main", "label", visible = true, order = 2),
                    paint("paint:item:marker:main", "marker:no-connect:main", "marker", visible = true, order = 3),
                ),
            ),
        )

        val svg = PresentationDocumentSvgExporter().export(document)

        assertContains(svg, """data-connection-id="connection:main"""")
        assertContains(svg, """points="10,10 70,10"""")
        assertContains(svg, """data-source-anchor="anchor:source"""")
        assertContains(svg, """data-target-anchor="anchor:target"""")
        assertContains(svg, """data-label-id="label:connection:main"""")
        assertContains(svg, """data-athena-marker-kind="no_connect_crossing"""")
        assertFalse(svg.contains("inferred", ignoreCase = true))
    }

    @Test
    fun `does not infer crossing marker from intersecting connectors`() {
        val document = PresentationDocument(
            view = ViewDefinition("schematic", "Control Drawing", layoutIntent = LayoutIntent.CONNECTIVITY),
            canvasWidth = 640,
            canvasHeight = 360,
            primitivePacks = emptyList(),
            compositePacks = emptyList(),
            occurrences = emptyList(),
            connectors = listOf(
                connector("connection:a", listOf(PresentationPoint(10, 40), PresentationPoint(70, 40))),
                connector("connection:b", listOf(PresentationPoint(40, 10), PresentationPoint(40, 70))),
            ),
            paintPlan = PresentationPaintPlan(
                listOf(
                    paint("paint:item:connector:a", "route:connection:a", "connector", visible = true, order = 1),
                    paint("paint:item:connector:b", "route:connection:b", "connector", visible = true, order = 2),
                ),
            ),
        )

        val svg = PresentationDocumentSvgExporter().export(document)

        assertFalse(svg.contains("""data-athena-marker-kind=""""))
    }

    @Test
    fun `honors presentation paint plan order and visibility`() {
        val hidden = connector("connection:hidden", listOf(PresentationPoint(10, 40), PresentationPoint(70, 40)))
        val visible = connector("connection:visible", listOf(PresentationPoint(10, 20), PresentationPoint(70, 20)))
        val marker = PresentationConnectionMarker(
            markerId = PresentationConnectionMarkerId("marker:visible"),
            kind = PresentationConnectionMarkerKind.JUNCTION,
            point = PresentationPoint(40, 20),
            routeIds = listOf("route:connection:visible"),
            connectorIds = listOf(PresentationOccurrenceId("route:connection:visible")),
            semanticId = StableSemanticIdentity("connection:visible"),
            joined = true,
            appearanceClassId = "marker:junction",
            sourceProjectionIds = listOf("connection:visible"),
            sourceProvenance = listOf("source.athena:1:1"),
            compilerSnapshotId = "snapshot:main",
        )
        val document = PresentationDocument(
            view = ViewDefinition("schematic", "Control Drawing", layoutIntent = LayoutIntent.CONNECTIVITY),
            canvasWidth = 640,
            canvasHeight = 360,
            primitivePacks = emptyList(),
            compositePacks = emptyList(),
            occurrences = emptyList(),
            connectors = listOf(hidden, visible),
            connectionMarkers = listOf(marker),
            paintPlan = PresentationPaintPlan(
                listOf(
                    paint("paint:item:connector:hidden", hidden.occurrenceId.value, "connector", visible = false, order = 1),
                    paint("paint:item:label:hidden", hidden.labels.single().labelId, "label", visible = false, order = 2),
                    paint("paint:item:marker:visible", marker.markerId.value, "marker", visible = true, order = 3),
                    paint("paint:item:connector:visible", visible.occurrenceId.value, "connector", visible = true, order = 4),
                    paint("paint:item:label:visible", visible.labels.single().labelId, "label", visible = true, order = 5),
                ),
            ),
        )

        val svg = PresentationDocumentSvgExporter().export(document)

        assertFalse(svg.contains("connection:hidden"))
        assertFalse(svg.contains("label:connection:hidden"))
        assertBefore(svg, """data-marker-id="marker:visible"""", """data-connection-id="connection:visible"""")
        assertBefore(svg, """data-connection-id="connection:visible"""", """data-label-id="label:connection:visible"""")
        assertContains(svg, """data-paint-order="4"""")
    }

    private fun connector(id: String, routePoints: List<PresentationPoint>): PresentationConnector =
        PresentationConnector(
            occurrenceId = PresentationOccurrenceId("route:$id"),
            semanticId = StableSemanticIdentity(id),
            primitiveId = PresentationPrimitiveId("electrical.conductor.orthogonal"),
            routePoints = routePoints,
            line = PresentationConnectorLine(
                classId = "line:power",
                lineKind = "POWER",
                lineStyleId = "stroke:power",
                weight = 1.4,
                style = "SOLID",
                colorKey = "drawing.power",
                endpointBehavior = "ATTACH_TO_ANCHOR",
                labelPolicy = "TERMINAL_PAIR",
                crossingBehavior = "EXPLICIT_MARKER_ONLY",
                policyId = "control-drawing-iec",
                compilerSnapshotId = "snapshot:main",
            ),
            routeId = "route:$id",
            bundleId = "bundle:$id",
            laneId = "lane:main",
            laneRouteIds = listOf("route:$id"),
            selectedChannelIds = listOf("channel:power"),
            labels = listOf(
                PresentationConnectorLabel(
                    labelId = "label:$id",
                    targetId = "route:$id",
                    text = id.substringAfter(':'),
                    point = routePoints.first(),
                    bounds = PresentationDrawingBounds(routePoints.first().x, routePoints.first().y, 60, 16),
                    labelClassId = "label:route",
                    display = PresentationConnectorLabelDisplay.ALWAYS,
                    sourceProvenance = listOf("source.athena:1:1"),
                    compilerSnapshotId = "snapshot:main",
                ),
            ),
            quality = "SATISFIED",
            sourceEndpoint = endpoint("source", routePoints.first()),
            targetEndpoint = endpoint("target", routePoints.last()),
            sourceProjectionIds = listOf(id, "snapshot:main"),
            sourceSpan = LayoutSourceSpan("source.athena", 1, 1, 1, 20),
        )

    private fun endpoint(name: String, point: PresentationPoint): PresentationConnectorEndpoint =
        PresentationConnectorEndpoint(
            portSemanticId = StableSemanticIdentity("port:$name"),
            bindingId = RepresentationPortAnchorBindingId("binding:$name"),
            occurrenceId = RepresentationOccurrenceId("occurrence:$name"),
            anchorId = RepresentationAnchorId("anchor:$name"),
            point = point,
            sourceProvenance = listOf("source.athena:1:1"),
        )

    private fun paint(
        id: String,
        target: String,
        kind: String,
        visible: Boolean,
        order: Int,
    ): PresentationPaintItem =
        PresentationPaintItem(
            itemId = id,
            targetId = target,
            kind = kind,
            visible = visible,
            order = order,
        )

    private fun assertBefore(text: String, first: String, second: String) {
        val firstIndex = text.indexOf(first)
        val secondIndex = text.indexOf(second)
        kotlin.test.assertTrue(firstIndex >= 0, "Missing `$first`.")
        kotlin.test.assertTrue(secondIndex >= 0, "Missing `$second`.")
        kotlin.test.assertTrue(firstIndex < secondIndex, "`$first` must appear before `$second`.")
    }
}
