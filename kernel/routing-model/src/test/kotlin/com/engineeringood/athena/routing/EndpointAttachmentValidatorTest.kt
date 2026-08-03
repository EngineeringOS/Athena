package com.engineeringood.athena.routing

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EndpointAttachmentValidatorTest {
    private val validator = EndpointAttachmentValidator()

    @Test
    fun `valid engineering route endpoints attach exactly to anchor facts`() {
        val route = routeFact()
        val result = validator.validate(
            listOf(
                EndpointAttachmentRequest.engineeringRoute(
                    route,
                    sourceCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.source)),
                    targetCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.target)),
                ),
            ),
        )

        assertTrue(result.successful)
        val attachment = result.attachments.single()
        assertEquals(route.source.gridPoint, attachment.source.point)
        assertEquals(route.target.gridPoint, attachment.target.point)
        assertEquals(route.segments.first().start, attachment.source.renderedPoint)
        assertEquals(route.segments.last().end, attachment.target.renderedPoint)
    }

    @Test
    fun `projection transform preserves exact endpoint coordinates`() {
        val route = routeFact()
        val result = validator.validate(
            listOf(
                EndpointAttachmentRequest.engineeringRoute(
                    route,
                    sourceCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.source)),
                    targetCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.target)),
                    transform = EndpointProjectionTransform(offsetX = 100, offsetY = 50),
                ),
            ),
        )

        val attachment = result.attachments.single()
        assertEquals(SchematicRoutePoint(120, 70), attachment.source.renderedPoint)
        assertEquals(SchematicRoutePoint(180, 70), attachment.target.renderedPoint)
    }

    @Test
    fun `invalid engineering endpoints block accepted attachment`() {
        val route = routeFact()
        val cases = listOf(
            EndpointAttachmentRequest.engineeringRoute(route, sourceCandidates = emptyList(), targetCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.target))),
            EndpointAttachmentRequest.engineeringRoute(
                route,
                sourceCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.source), EndpointAttachmentFact.fromAnchor(route.source).copy(endpointId = "anchor:source:other")),
                targetCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.target)),
            ),
            EndpointAttachmentRequest.engineeringRoute(
                route,
                sourceCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.source).copy(kind = EndpointAttachmentKind.FALLBACK)),
                targetCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.target)),
            ),
            EndpointAttachmentRequest.engineeringRoute(
                route,
                sourceCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.source).copy(point = SchematicRoutePoint(30, 30))),
                targetCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.target)),
            ),
            EndpointAttachmentRequest.engineeringRoute(
                route,
                sourceCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.source)),
                targetCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.target)),
                componentBounds = listOf(
                    SchematicComponentBounds(
                        subjectId = StableSemanticIdentity("component:body"),
                        occurrenceId = LayoutOccurrenceId("occurrence:body"),
                        topLeft = SchematicRoutePoint(10, 10),
                        width = 20,
                        height = 20,
                    ),
                ),
            ),
        )

        val result = validator.validate(cases)

        assertFalse(result.successful)
        val codes = result.diagnostics.map { diagnostic -> diagnostic.code }
        assertContains(codes, "drawing.endpoint.unresolved")
        assertContains(codes, "drawing.endpoint.ambiguous")
        assertContains(codes, "drawing.endpoint.fallback")
        assertContains(codes, "drawing.endpoint.detached")
        assertContains(codes, "drawing.endpoint.body-interior")
        assertTrue(result.attachments.isEmpty())
    }

    @Test
    fun `loose line is accepted only as graphical annotation`() {
        val result = validator.validate(
            listOf(
                EndpointAttachmentRequest.annotationLine(
                    annotationId = "annotation:separator",
                    start = SchematicRoutePoint(10, 10),
                    end = SchematicRoutePoint(60, 10),
                    provenance = SourceProvenance("drawing.athena", 3, 1, 3, 20),
                ),
            ),
        )

        assertTrue(result.successful)
        assertEquals(listOf("annotation:separator"), result.annotations.map { annotation -> annotation.annotationId })
    }

    @Test
    fun `renderer payload contains attachment facts only`() {
        val route = routeFact()
        val result = validator.validate(
            listOf(
                EndpointAttachmentRequest.engineeringRoute(
                    route,
                    sourceCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.source)),
                    targetCandidates = listOf(EndpointAttachmentFact.fromAnchor(route.target)),
                ),
            ),
        )

        val payload = validator.normalize(result)

        assertEquals("athena", payload.authority)
        assertEquals(listOf("route:a"), payload.attachments.map { attachment -> attachment.routeId })
        assertTrue(payload.rendererRepairs.isEmpty())
        assertTrue(payload.rawGeometryFragments.isEmpty())
    }

    private fun routeFact(): RouteFact {
        val routeId = SchematicRouteId("route:a")
        val connectionId = ElectricalConnectionId("connection:a")
        return RouteFact(
            routeId = routeId,
            snapshotId = LayoutSnapshotId("snapshot:endpoint"),
            connectionId = connectionId,
            routeIntentId = RouteIntentId("route-intent:a"),
            bundleId = RouteBundleId("bundle:a"),
            selectedChannelIds = emptyList(),
            plannerId = "athena-route-engine",
            compilerSnapshotId = "snapshot:endpoint",
            provenance = SourceProvenance("routes.athena", 1, 1, 1, 20),
            qualityMetrics = RouteQualityMetrics(0, 0, 60, 0, 0, 0),
            source = anchor("source", 20, 20),
            target = anchor("target", 80, 20),
            segments = listOf(
                SchematicRouteSegment(
                    start = SchematicRoutePoint(20, 20),
                    end = SchematicRoutePoint(80, 20),
                    orientation = SchematicRouteSegmentOrientation.HORIZONTAL,
                ),
            ),
        )
    }

    private fun anchor(name: String, x: Int, y: Int): TerminalAnchorFact {
        return TerminalAnchorFact(
            anchorId = TerminalAnchorId("anchor:$name"),
            subjectId = StableSemanticIdentity("component:$name"),
            occurrenceId = LayoutOccurrenceId("occurrence:$name"),
            portId = ElectricalPortId("port:$name"),
            portRole = ElectricalPortRole.OUTPUT,
            side = TerminalSide.RIGHT,
            point = SchematicRoutePoint(x, y),
            gridPoint = SchematicRoutePoint(x, y),
            policySource = "endpoint-test",
        )
    }
}
