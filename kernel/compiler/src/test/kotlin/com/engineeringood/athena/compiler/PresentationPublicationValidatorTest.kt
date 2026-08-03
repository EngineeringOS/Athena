package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.presentation.PresentationConnectionMarker
import com.engineeringood.athena.presentation.PresentationConnectionMarkerId
import com.engineeringood.athena.presentation.PresentationConnectionMarkerKind
import com.engineeringood.athena.presentation.PresentationConnector
import com.engineeringood.athena.presentation.PresentationConnectorEndpoint
import com.engineeringood.athena.presentation.PresentationConnectorLine
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationOccurrenceId
import com.engineeringood.athena.presentation.PresentationPoint
import com.engineeringood.athena.presentation.PresentationPrimitiveId
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationPortAnchorBindingId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PresentationPublicationValidatorTest {
    @Test
    fun `valid document publishes connector and marker facts atomically`() {
        val marker = marker()
        val document = document(connector = connector(markerIds = listOf(marker.markerId)), marker = marker)

        assertTrue(PresentationPublicationValidator.validate(document).isEmpty())
    }

    @Test
    fun `document rejects one way marker references`() {
        val issues = PresentationPublicationValidator.validate(
            document(connector = connector(markerIds = emptyList()), marker = marker()),
        )

        assertEquals(listOf("presentation.publication.marker.connector.conflict"), issues.map { issue -> issue.code })
    }

    @Test
    fun `document rejects connector without source trace`() {
        val issues = PresentationPublicationValidator.validate(
            document(
                connector = connector(markerIds = listOf(PresentationConnectionMarkerId("marker:main"))).copy(sourceProjectionIds = emptyList()),
                marker = marker(),
            ),
        )

        assertTrue(issues.any { issue ->
            issue.code == "presentation.publication.connector.trace.missing" &&
                issue.subject == "route:main" &&
                issue.message.contains("source projection trace")
        })
    }

    private fun document(
        connector: PresentationConnector,
        marker: PresentationConnectionMarker,
    ): PresentationDocument = PresentationDocument(
        view = ViewDefinition("schematic", "Schematic", LayoutIntent.CONNECTIVITY),
        canvasWidth = 640,
        canvasHeight = 360,
        primitivePacks = emptyList(),
        compositePacks = emptyList(),
        occurrences = emptyList(),
        connectors = listOf(connector),
        connectionMarkers = listOf(marker),
    )

    private fun connector(
        markerIds: List<PresentationConnectionMarkerId>,
    ): PresentationConnector = PresentationConnector(
        occurrenceId = PresentationOccurrenceId("route:main"),
        semanticId = StableSemanticIdentity("connection:main"),
        primitiveId = PresentationPrimitiveId("electrical.conductor.orthogonal"),
        routePoints = listOf(PresentationPoint(10, 10), PresentationPoint(70, 10)),
        line = PresentationConnectorLine(
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
        ),
        routeId = "route:main",
        bundleId = "bundle:main",
        laneId = "lane:main",
        laneRouteIds = listOf("route:main"),
        selectedChannelIds = listOf("channel:main"),
        quality = "SATISFIED",
        sourceEndpoint = endpoint("source", PresentationPoint(10, 10)),
        targetEndpoint = endpoint("target", PresentationPoint(70, 10)),
        markerIds = markerIds,
        sourceProjectionIds = listOf("route:main"),
        sourceSpan = LayoutSourceSpan("routes.athena", 1, 1, 1, 20),
    )

    private fun marker(): PresentationConnectionMarker = PresentationConnectionMarker(
        markerId = PresentationConnectionMarkerId("marker:main"),
        kind = PresentationConnectionMarkerKind.JUNCTION,
        point = PresentationPoint(40, 10),
        routeIds = listOf("route:main"),
        connectorIds = listOf(PresentationOccurrenceId("route:main")),
        semanticId = StableSemanticIdentity("port:joined"),
        joined = true,
        appearanceClassId = "marker:junction-dot",
        sourceProjectionIds = listOf("marker:main", "route:main"),
        sourceProvenance = listOf("routes.athena:1:1"),
        compilerSnapshotId = "compiler:test",
    )

    private fun endpoint(id: String, point: PresentationPoint): PresentationConnectorEndpoint =
        PresentationConnectorEndpoint(
            portSemanticId = StableSemanticIdentity("port:$id"),
            bindingId = RepresentationPortAnchorBindingId("binding:$id"),
            occurrenceId = RepresentationOccurrenceId("occurrence:$id"),
            anchorId = RepresentationAnchorId("anchor:$id"),
            point = point,
            sourceProvenance = listOf("routes.athena:1:1"),
        )
}
