package com.engineeringood.athena.presentation

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationPortAnchorBindingId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PresentationConnectorContractTest {
    @Test
    fun `connector route starts and ends at exact endpoint points`() {
        val connector = connector()

        assertEquals(connector.sourceEndpoint.point, connector.routePoints.first())
        assertEquals(connector.targetEndpoint.point, connector.routePoints.last())
        assertEquals("line:power", connector.line.classId)
        assertEquals("stroke:power", connector.line.lineStyleId)
        assertEquals("route:source-to-target", connector.routeId)
        assertEquals("binding:source", connector.sourceEndpoint.bindingId.value)
        assertEquals("anchor:target", connector.targetEndpoint.anchorId.value)
    }

    @Test
    fun `connector rejects detached start point`() {
        assertFailsWith<IllegalArgumentException> {
            connector(
                routePoints = listOf(
                    PresentationPoint(5, 10),
                    PresentationPoint(80, 10),
                ),
            )
        }
    }

    @Test
    fun `connector rejects detached target point`() {
        assertFailsWith<IllegalArgumentException> {
            connector(
                routePoints = listOf(
                    PresentationPoint(10, 10),
                    PresentationPoint(80, 12),
                ),
            )
        }
    }

    @Test
    fun `connector rejects non orthogonal visible geometry`() {
        assertFailsWith<IllegalArgumentException> {
            connector(
                routePoints = listOf(
                    PresentationPoint(10, 10),
                    PresentationPoint(50, 25),
                    PresentationPoint(80, 10),
                ),
            )
        }
    }

    @Test
    fun `connector rejects required route and stroke facts hidden in token overrides`() {
        assertFailsWith<IllegalArgumentException> {
            connector(tokenOverrides = mapOf("routeLabels" to "L1", "strokeWeight" to "2"))
        }
    }

    private fun connector(
        routePoints: List<PresentationPoint> = listOf(
            PresentationPoint(10, 10),
            PresentationPoint(50, 10),
            PresentationPoint(80, 10),
        ),
        tokenOverrides: Map<String, String> = emptyMap(),
    ): PresentationConnector = PresentationConnector(
        occurrenceId = PresentationOccurrenceId("route:source-to-target"),
        semanticId = StableSemanticIdentity("connection:source-to-target"),
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
            crossingBehavior = "JUNCTION_REQUIRED",
            policyId = "policy:test",
            compilerSnapshotId = "compiler:test",
        ),
        routeId = "route:source-to-target",
        bundleId = "bundle:source-to-target",
        laneId = "lane:test",
        laneRouteIds = listOf("route:source-to-target"),
        selectedChannelIds = listOf("channel:power"),
        quality = "SATISFIED",
        sourceEndpoint = endpoint("source", PresentationPoint(10, 10)),
        targetEndpoint = endpoint("target", PresentationPoint(80, 10)),
        tokenOverrides = tokenOverrides,
        sourceSpan = LayoutSourceSpan("source:test.athena", 1, 1, 1, 20),
    )

    private fun endpoint(
        id: String,
        point: PresentationPoint,
    ): PresentationConnectorEndpoint = PresentationConnectorEndpoint(
        portSemanticId = StableSemanticIdentity("port:$id"),
        bindingId = RepresentationPortAnchorBindingId("binding:$id"),
        occurrenceId = RepresentationOccurrenceId("drawing:component:$id"),
        anchorId = RepresentationAnchorId("anchor:$id"),
        point = point,
        sourceProvenance = listOf("source:test.athena:1:1"),
    )
}
