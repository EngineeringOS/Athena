package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationLayer
import com.engineeringood.athena.presentation.PresentationReality
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialQualityMeasurement
import com.engineeringood.athena.spatial.SpatialRoute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SpatialToPresentationTransformationTest {
    @Test
    fun `spatial to presentation emits presentation document through typed transformation`() {
        val transformation: RealityTransformation<SpatialDocument, PresentationDocument> =
            SpatialToPresentationTransformation()

        val result = transformation.transform(spatialDocument())

        val output = assertIs<RealityTransformationResult.Success<PresentationDocument>>(result).output
        assertEquals("spatial-presentation", output.view.id)
        assertEquals(2, output.occurrences.size)
        assertEquals(1, output.connectors.size)
        assertEquals(1, output.primitivePacks.size)
        assertTrue(PresentationReality.validate(output).isValid)
    }

    @Test
    fun `spatial placements become presentation occurrences and shape paint facts`() {
        val output = successOutput()

        assertContains(output.occurrences.map { occurrence -> occurrence.occurrenceId.value }, "paint:occurrence:supply")
        assertContains(output.occurrences.map { occurrence -> occurrence.occurrenceId.value }, "paint:occurrence:breaker")
        assertEquals(listOf(PresentationLayer.DEVICE, PresentationLayer.DEVICE), output.occurrences.map { occurrence ->
            occurrence.layer
        })
        assertEquals(80, output.occurrences.first().bounds.width)
        assertEquals(40, output.occurrences.first().bounds.height)
        assertEquals("spatial-shape:device-box", output.primitivePacks.single().primitives.single().primitiveId.value)
    }

    @Test
    fun `spatial routes become presentation connectors without endpoint repair`() {
        val output = successOutput()
        val connector = output.connectors.single()

        assertEquals("paint:route:main-feed", connector.occurrenceId.value)
        assertEquals("connection:Supply.L1-to-Q1.1", connector.semanticId.value)
        assertEquals("lane:main", connector.laneId)
        assertEquals(listOf("route:main-feed"), connector.laneRouteIds)
        assertEquals(output.connectors.single().routePoints.first(), connector.sourceEndpoint.point)
        assertEquals(output.connectors.single().routePoints.last(), connector.targetEndpoint.point)
        assertEquals(listOf(80, 160), connector.routePoints.map { point -> point.x })
        assertEquals(listOf(20, 20), connector.routePoints.map { point -> point.y })
    }

    @Test
    fun `presentation output carries style labels visibility and paint order`() {
        val output = successOutput()
        val connector = output.connectors.single()

        assertEquals("spatial-route", connector.line.classId)
        assertEquals("solid", connector.line.style)
        assertEquals("connection", connector.line.colorKey)
        assertEquals(1.5, connector.line.weight)
        assertEquals("route:main-feed", connector.labels.single().text)
        assertEquals("label:route", connector.labels.single().labelClassId)
        assertEquals("presentation compiler", output.drawingComposition?.authorities?.policy)
        assertEquals(
            listOf("shape", "shape", "connector", "label"),
            output.paintPlan?.items.orEmpty().map { item -> item.kind },
        )
        assertTrue(output.paintPlan?.items.orEmpty().all { item -> item.visible })
    }

    @Test
    fun `spatial to presentation delegates connector appearance to presentation paint authority`() {
        val transformation = SpatialToPresentationTransformation(
            connectionPaintCompiler = ConnectionPaintCompiler(
                overrides = mapOf(
                    "route:main-feed" to ConnectionPaintOverride(
                        style = "dot",
                        label = "main feed",
                        position = "right top",
                    ),
                ),
            ),
        )

        val output = assertIs<RealityTransformationResult.Success<PresentationDocument>>(
            transformation.transform(spatialDocument()),
        ).output
        val connector = output.connectors.single()

        assertEquals("dot", connector.line.style)
        assertEquals("line:dot", connector.line.lineStyleId)
        assertEquals("main feed", connector.labels.single().text)
        assertEquals(168, connector.labels.single().point.x)
        assertEquals(8, connector.labels.single().point.y)
        assertEquals(connector.routePoints.first(), connector.sourceEndpoint.point)
        assertEquals(connector.routePoints.last(), connector.targetEndpoint.point)
    }

    @Test
    fun `spatial to presentation reports plain diagnostics before publication`() {
        val result = SpatialToPresentationTransformation().transform(SpatialDocument())

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertFalse(failure.diagnostics.isEmpty())
        assertTrue(failure.diagnostics.any { diagnostic ->
            diagnostic.reality == "Spatial Reality" && diagnostic.message == "missing occurrence geometry facts"
        })
        assertTrue(failure.diagnostics.any { diagnostic ->
            diagnostic.reality == "Spatial Reality" && diagnostic.message == "missing anchor position facts"
        })
    }

    @Test
    fun `new presentation transformation names avoid stale architecture terms`() {
        val names = listOf(
            SpatialToPresentationTransformation::class.simpleName.orEmpty(),
            ConnectionPaintCompiler::class.simpleName.orEmpty(),
        )
        val banned = listOf("M39", "V0", "V1", "Evidence", "ProfessionalControlDrawing", "Compatibility")

        banned.forEach { token ->
            assertFalse(
                names.any { name -> name.contains(token) },
                "Transformation names must not contain `$token`: $names",
            )
        }
    }

    private fun successOutput(): PresentationDocument {
        val result = SpatialToPresentationTransformation().transform(spatialDocument())
        return assertIs<RealityTransformationResult.Success<PresentationDocument>>(result).output
    }

    private fun spatialDocument(): SpatialDocument {
        val occurrences = listOf(
            testSpatialOccurrence("supply", "component:Supply", 0, 0),
            testSpatialOccurrence("breaker", "component:Q1", 140, 0),
        )
        return SpatialDocument(
            occurrences = occurrences,
            regions = listOf(testSpatialRegion(occurrences)),
            anchorPositions = listOf(
                SpatialAnchorPosition(anchorId = "anchor:supply:right", occurrenceId = "supply", x = 80.0, y = 20.0),
                SpatialAnchorPosition(anchorId = "anchor:breaker:left", occurrenceId = "breaker", x = 140.0, y = 20.0),
            ),
            lanes = listOf(SpatialLane(laneId = "lane:main", direction = "horizontal")),
            routes = listOf(
                SpatialRoute(
                    routeId = "route:main-feed",
                    connectionId = StableSemanticIdentity("connection:Supply.L1-to-Q1.1"),
                    sourceOccurrenceId = "supply",
                    targetOccurrenceId = "breaker",
                    sourceAnchorId = "anchor:supply:right",
                    targetAnchorId = "anchor:breaker:left",
                    laneId = "lane:main",
                    points = listOf(SpatialPoint(80.0, 20.0), SpatialPoint(160.0, 20.0)),
                ),
            ),
            qualityMeasurements = listOf(SpatialQualityMeasurement("route-count", 1.0)),
        )
    }
}
