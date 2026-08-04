package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationBounds
import com.engineeringood.athena.presentation.PresentationLayer
import com.engineeringood.athena.presentation.PresentationReality
import com.engineeringood.athena.spatial.SpatialAnchorId
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialAlignment
import com.engineeringood.athena.spatial.SpatialAlignmentId
import com.engineeringood.athena.spatial.SpatialAlignmentSource
import com.engineeringood.athena.spatial.SpatialBoundarySide
import com.engineeringood.athena.spatial.SpatialGridDefinition
import com.engineeringood.athena.spatial.SpatialGridReference
import com.engineeringood.athena.spatial.SpatialGridReferenceId
import com.engineeringood.athena.spatial.SpatialGridReferenceSubject
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialOccurrencePortSubject
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialQualitySnapshot
import com.engineeringood.athena.spatial.SpatialQualitySnapshotId
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialRoute
import com.engineeringood.athena.spatial.SpatialSheet
import com.engineeringood.athena.spatial.SpatialSourceTrace
import com.engineeringood.athena.geometry.GeometryElementId
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SpatialToPresentationTransformationTest {
    @Test
    fun `finite forged quality metrics block Presentation with a structured diagnostic`() {
        val valid = spatialSheet()
        val invalid = valid.copy(
            quality = valid.quality.copy(
                metrics = valid.quality.metrics.copy(
                    occurrenceOverlapCount = valid.quality.metrics.occurrenceOverlapCount + 1,
                ),
            ),
        )

        val result = SpatialToPresentationTransformation().transform(invalid)

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertEquals(1, failure.diagnostics.size)
        assertEquals("Quality snapshot on Sheet $TEST_SPATIAL_SHEET_ID", failure.diagnostics.single().subject)
        assertEquals(
            "metrics do not equal exact values recomputed from final Spatial facts",
            failure.diagnostics.single().problem,
        )
        assertEquals(
            "Recompute all quality metrics from this Sheet's Drawing Area, Occurrences, Constructs, Lanes, and Routes.",
            failure.diagnostics.single().correction,
        )
        assertEquals(valid.quality.sourceTrace, failure.diagnostics.single().sourceTrace)
    }

    @Test
    fun `non finite quality blocks Presentation with a structured diagnostic`() {
        val valid = spatialSheet()
        val invalid = valid.copy(
            quality = valid.quality.copy(
                metrics = valid.quality.metrics.copy(density = Double.POSITIVE_INFINITY),
            ),
        )

        val result = SpatialToPresentationTransformation().transform(invalid)

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertEquals(1, failure.diagnostics.size)
        assertEquals("Quality snapshot on Sheet $TEST_SPATIAL_SHEET_ID", failure.diagnostics.single().subject)
        assertEquals("Density value Infinity is not finite", failure.diagnostics.single().problem)
        assertEquals(
            "Compute Density from the owning Sheet Drawing Area without clamping or coercion.",
            failure.diagnostics.single().correction,
        )
        assertEquals(valid.quality.sourceTrace, failure.diagnostics.single().sourceTrace)
    }

    @Test
    fun `spatial to presentation emits presentation document through typed transformation`() {
        val transformation: RealityTransformation<SpatialSheet, PresentationDocument> =
            SpatialToPresentationTransformation()

        val result = transformation.transform(spatialSheet())

        val output = assertIs<RealityTransformationResult.Success<PresentationDocument>>(result).output
        assertEquals("spatial-presentation", output.view.id)
        assertEquals(2, output.occurrences.size)
        assertEquals(1, output.connectors.size)
        assertEquals(1, output.primitivePacks.size)
        assertEquals(TEST_SPATIAL_SHEET_ID, output.drawingComposition?.sheetId)
        assertEquals(1200, output.canvasWidth)
        assertEquals(800, output.canvasHeight)
        assertTrue(PresentationReality.validate(output).isValid)
    }

    @Test
    fun `spatial placements become presentation occurrences and shape paint facts`() {
        val spatial = spatialSheet()
        val output = assertIs<RealityTransformationResult.Success<PresentationDocument>>(
            SpatialToPresentationTransformation().transform(spatial),
        ).output

        assertContains(
            output.occurrences.map { occurrence -> occurrence.occurrenceId.value },
            "paint:occurrence:sheet=sheet%3Atest:projection=supply",
        )
        assertContains(
            output.occurrences.map { occurrence -> occurrence.occurrenceId.value },
            "paint:occurrence:sheet=sheet%3Atest:projection=breaker",
        )
        assertEquals(listOf(PresentationLayer.DEVICE, PresentationLayer.DEVICE), output.occurrences.map { occurrence ->
            occurrence.layer
        })
        val expectedBounds = listOf(
            PresentationBounds(80, 100, 80, 40),
            PresentationBounds(400, 100, 80, 40),
        )
        assertEquals(expectedBounds, output.occurrences.map { occurrence -> occurrence.bounds })
        assertEquals(
            spatial.occurrences.map { occurrence ->
                PresentationBounds(
                    occurrence.rectangle.x,
                    occurrence.rectangle.y,
                    occurrence.rectangle.width,
                    occurrence.rectangle.height,
                )
            },
            output.occurrences.map { occurrence -> occurrence.bounds },
        )
        assertEquals("spatial-shape:device-box", output.primitivePacks.single().primitives.single().primitiveId.value)
    }

    @Test
    fun `spatial routes become presentation connectors with exact points and endpoint identities`() {
        val output = successOutput()
        val connector = output.connectors.single()

        assertEquals(
            "paint:route:sheet=sheet%3Atest:connection=route%3Amain-feed",
            connector.occurrenceId.value,
        )
        assertEquals("connection:Supply.L1-to-Q1.1", connector.semanticId.value)
        assertEquals("lane:sheet=sheet%3Atest:orientation=horizontal:coordinate=120", connector.laneId)
        assertEquals(
            listOf("route:sheet=sheet%3Atest:connection=route%3Amain-feed"),
            connector.laneRouteIds,
        )
        assertEquals(output.connectors.single().routePoints.first(), connector.sourceEndpoint.point)
        assertEquals(output.connectors.single().routePoints.last(), connector.targetEndpoint.point)
        assertEquals(listOf(160, 400), connector.routePoints.map { point -> point.x })
        assertEquals(listOf(120, 120), connector.routePoints.map { point -> point.y })
        assertEquals("port:Supply.L1", connector.sourceEndpoint.portSemanticId.value)
        assertEquals("port:Q1.1", connector.targetEndpoint.portSemanticId.value)
        assertEquals("supply", connector.sourceEndpoint.occurrenceId.value)
        assertEquals("breaker", connector.targetEndpoint.occurrenceId.value)
        assertEquals(
            "anchor:sheet=sheet%3Atest:occurrence=supply:port=port%3ASupply.L1",
            connector.sourceEndpoint.anchorId.value,
        )
        assertEquals(
            "anchor:sheet=sheet%3Atest:occurrence=breaker:port=port%3AQ1.1",
            connector.targetEndpoint.anchorId.value,
        )
        assertEquals(
            spatialSheet().routes.single().sourceTrace.projectionIds,
            connector.sourceProjectionIds,
        )
    }

    @Test
    fun `presentation output carries style labels visibility and paint order`() {
        val output = successOutput()
        val connector = output.connectors.single()

        assertEquals("spatial-route", connector.line.classId)
        assertEquals("solid", connector.line.style)
        assertEquals("connection", connector.line.colorKey)
        assertEquals(1.5, connector.line.weight)
        assertEquals("Supply.L1-to-Q1.1", connector.labels.single().text)
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
                    "route:sheet=sheet%3Atest:connection=route%3Amain-feed" to ConnectionPaintOverride(
                        style = "dot",
                        label = "main feed",
                        position = "right top",
                    ),
                ),
            ),
        )

        val output = assertIs<RealityTransformationResult.Success<PresentationDocument>>(
            transformation.transform(spatialSheet()),
        ).output
        val connector = output.connectors.single()

        assertEquals("dot", connector.line.style)
        assertEquals("line:dot", connector.line.lineStyleId)
        assertEquals("main feed", connector.labels.single().text)
        assertEquals(408, connector.labels.single().point.x)
        assertEquals(108, connector.labels.single().point.y)
        assertEquals(connector.routePoints.first(), connector.sourceEndpoint.point)
        assertEquals(connector.routePoints.last(), connector.targetEndpoint.point)
    }

    @Test
    fun `presentation connectors preserve complete shared Lane membership`() {
        val base = spatialSheet()
        val firstRoute = base.routes.single()
        val secondRoute = firstRoute.copy(
            routeId = testSpatialRouteId("route:backup"),
            connectionId = StableSemanticIdentity("connection:backup"),
            sourceTrace = SpatialSourceTrace(
                projectionIds = listOf(
                    TEST_SPATIAL_SHEET_ID,
                    "route:backup",
                    "supply",
                    "port:Supply.L1",
                    "breaker",
                    "port:Q1.1",
                ),
                geometryElementIds = listOf(GeometryElementId("geometry:route:backup")),
            ),
        )
        val lane = testSpatialLane("route:backup", "route:main-feed", coordinate = 120)
        val routes = listOf(firstRoute, secondRoute)
        val qualityTrace = testSpatialQualityTrace(
            sheetTrace = base.sourceTrace,
            gridTrace = base.grid.sourceTrace,
            factTraces = base.occurrences.map { occurrence -> occurrence.sourceTrace } +
                base.regions.map { region -> region.sourceTrace } +
                base.constructs.map { construct -> construct.sourceTrace } +
                base.alignments.map { alignment -> alignment.sourceTrace } +
                base.anchors.map { anchor -> anchor.sourceTrace } +
                routes.map { route -> route.sourceTrace } +
                base.gridReferences.map { reference -> reference.sourceTrace },
        )

        val result = SpatialToPresentationTransformation().transform(
            base.copy(
                lanes = listOf(lane),
                routes = routes,
                quality = SpatialQualitySnapshot(
                    base.quality.qualitySnapshotId,
                    base.quality.sheetId,
                    SpatialQualityCompiler().measure(
                        drawingArea = base.drawingArea,
                        occurrences = base.occurrences,
                        constructs = base.constructs,
                        lanes = listOf(lane),
                        routes = routes,
                    ),
                    qualityTrace,
                ),
            ),
        )

        val output = assertIs<RealityTransformationResult.Success<PresentationDocument>>(result).output
        val expectedMembership = lane.routeIds.map { routeId -> routeId.value }
        assertTrue(output.connectors.all { connector -> connector.laneRouteIds == expectedMembership })
    }

    @Test
    fun `spatial to presentation reports plain diagnostics before publication`() {
        val result = SpatialToPresentationTransformation().transform(
            spatialSheet().copy(
                occurrences = emptyList(),
                regions = emptyList(),
                anchors = emptyList(),
                lanes = emptyList(),
                routes = emptyList(),
                gridReferences = emptyList(),
            ),
        )

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertContains(
            failure.diagnostics,
            RealityTransformationDiagnostic(
                reality = "Spatial Reality",
                message = "Sheet $TEST_SPATIAL_SHEET_ID: has no Occurrence geometry facts " +
                    "Publish every projected Occurrence on Sheet $TEST_SPATIAL_SHEET_ID before Presentation.",
                subject = "Sheet $TEST_SPATIAL_SHEET_ID",
                problem = "has no Occurrence geometry facts",
                correction = "Publish every projected Occurrence on Sheet $TEST_SPATIAL_SHEET_ID before Presentation.",
                sourceTrace = spatialSheet().sourceTrace,
            ),
        )
    }

    @Test
    fun `multi Sheet presentation assembly fails closed when one Sheet is invalid`() {
        val valid = spatialSheet()
        val invalidSheetId = "sheet:invalid"
        val invalidTrace = SpatialSourceTrace(
            projectionIds = listOf(invalidSheetId),
            geometryElementIds = listOf(GeometryElementId("geometry:$invalidSheetId")),
        )
        val invalidGridTrace = SpatialSourceTrace(
            projectionIds = listOf(invalidSheetId, "grid:invalid"),
            geometryElementIds = invalidTrace.geometryElementIds,
        )
        val invalid = SpatialSheet(
            sheetId = invalidSheetId,
            extent = SpatialRect(0, 0, 1200, 800),
            drawingArea = SpatialRect(40, 60, 1120, 640),
            grid = SpatialGridDefinition(
                invalidSheetId,
                "grid:invalid",
                SpatialRect(40, 60, 1120, 640),
                3,
                4,
                invalidGridTrace,
            ),
            occurrences = emptyList(),
            regions = emptyList(),
            constructs = emptyList(),
            alignments = emptyList(),
            anchors = emptyList(),
            lanes = emptyList(),
            routes = emptyList(),
            gridReferences = emptyList(),
            quality = SpatialQualitySnapshot(
                SpatialQualitySnapshotId(invalidSheetId),
                invalidSheetId,
                zeroSpatialQualityMetrics(),
                invalidGridTrace,
            ),
            sourceTrace = invalidTrace,
        )

        val result = transformSpatialSheetsToPresentation(
            document = SpatialDocument(listOf(valid, invalid)),
            view = ViewDefinition("spatial-presentation", "Spatial Presentation"),
        )

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertEquals(
            listOf(
                "has no Occurrence geometry facts",
                "has no Region geometry facts",
            ),
            failure.diagnostics.map { diagnostic -> diagnostic.problem },
        )
        assertTrue(failure.diagnostics.all { diagnostic ->
            diagnostic.subject == "Sheet $invalidSheetId" && diagnostic.sourceTrace == invalidTrace
        })
    }

    @Test
    fun `complete presentation preflight rejects composition before batch paint assembly`() {
        val valid = spatialSheet()
        val invalid = valid.copy(extent = SpatialRect(0, 0, 1200, 740))

        val result = transformSpatialSheetsToPresentation(
            document = SpatialDocument(listOf(invalid)),
            view = ViewDefinition("spatial-presentation", "Spatial Presentation"),
        )

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertEquals(
            listOf(
                "extent (0,0,1200,740) does not equal fixed Sheet extent (0,0,1200,800)",
                "fixed title block start y=740 is outside Sheet extent (0,0,1200,740)",
            ),
            failure.diagnostics.map { diagnostic -> diagnostic.problem },
        )
        assertTrue(failure.diagnostics.all { diagnostic -> diagnostic.sourceTrace == valid.sourceTrace })
    }

    @Test
    fun `presentation preflight rejects noncanonical Sheet composition before paint`() {
        val valid = spatialSheet()
        val drawingArea = SpatialRect(40, 60, 1120, 700)
        val invalid = valid.copy(
            extent = SpatialRect(10, 20, 1200, 800),
            drawingArea = drawingArea,
            grid = SpatialGridDefinition(
                valid.sheetId,
                valid.grid.gridId,
                drawingArea,
                valid.grid.rows,
                valid.grid.columns,
                valid.grid.sourceTrace,
            ),
            quality = valid.quality.copy(
                metrics = SpatialQualityCompiler().measure(
                    drawingArea = drawingArea,
                    occurrences = valid.occurrences,
                    constructs = valid.constructs,
                    lanes = valid.lanes,
                    routes = valid.routes,
                ),
            ),
        )

        val result = SpatialToPresentationTransformation().transform(invalid)

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertEquals(
            listOf(
                "Drawing Area (40,60,1120,700) does not equal fixed Drawing Area (40,60,1120,640)",
                "Drawing Area bottom 760 crosses fixed title block start y=740",
                "extent (10,20,1200,800) does not equal fixed Sheet extent (0,0,1200,800)",
            ),
            failure.diagnostics.map { diagnostic -> diagnostic.problem },
        )
        assertTrue(failure.diagnostics.all { diagnostic ->
            diagnostic.subject == "Sheet $TEST_SPATIAL_SHEET_ID" && diagnostic.sourceTrace == valid.sourceTrace
        })
    }

    @Test
    fun `presentation preflight rejects a truncated title block before paint`() {
        val valid = spatialSheet()
        val invalid = valid.copy(extent = SpatialRect(0, 0, 1200, 740))

        val result = SpatialToPresentationTransformation().transform(invalid)

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertEquals(
            listOf(
                "extent (0,0,1200,740) does not equal fixed Sheet extent (0,0,1200,800)",
                "fixed title block start y=740 is outside Sheet extent (0,0,1200,740)",
            ),
            failure.diagnostics.map { diagnostic -> diagnostic.problem },
        )
        assertTrue(failure.diagnostics.all { diagnostic ->
            diagnostic.subject == "Sheet $TEST_SPATIAL_SHEET_ID" && diagnostic.sourceTrace == valid.sourceTrace
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
        val result = SpatialToPresentationTransformation().transform(spatialSheet())
        return assertIs<RealityTransformationResult.Success<PresentationDocument>>(result).output
    }

    private fun spatialSheet(): SpatialSheet {
        val occurrences = listOf(
            testSpatialOccurrence("supply", "component:Supply", 80, 100),
            testSpatialOccurrence("breaker", "component:Q1", 400, 100),
        )
        val sourceAnchor = anchor(
            occurrenceId = "supply",
            portId = "port:Supply.L1",
            side = SpatialBoundarySide.RIGHT,
            point = SpatialPoint(160, 120),
        )
        val targetAnchor = anchor(
            occurrenceId = "breaker",
            portId = "port:Q1.1",
            side = SpatialBoundarySide.LEFT,
            point = SpatialPoint(400, 120),
        )
        val trace = SpatialSourceTrace(
            projectionIds = listOf(TEST_SPATIAL_SHEET_ID),
            geometryElementIds = listOf(GeometryElementId("geometry:$TEST_SPATIAL_SHEET_ID")),
        )
        val drawingArea = SpatialRect(40, 60, 1120, 640)
        val gridTrace = SpatialSourceTrace(
            projectionIds = listOf(TEST_SPATIAL_SHEET_ID, "grid:test"),
            geometryElementIds = trace.geometryElementIds,
        )
        val grid = SpatialGridDefinition(TEST_SPATIAL_SHEET_ID, "grid:test", drawingArea, 3, 4, gridTrace)
        val region = testSpatialRegion(occurrences)
        val alignment = SpatialAlignment(
            alignmentId = SpatialAlignmentId(
                TEST_SPATIAL_SHEET_ID,
                SpatialAlignmentSource.Region(region.regionId),
            ),
            sheetId = TEST_SPATIAL_SHEET_ID,
            constraintSource = SpatialAlignmentSource.Region(region.regionId),
            occurrenceIds = occurrences.map { occurrence -> occurrence.occurrenceId },
            sourceTrace = region.sourceTrace,
        )
        val anchors = listOf(sourceAnchor, targetAnchor)
        val lane = testSpatialLane("route:main-feed", coordinate = 120)
        val route = testSpatialRoute(
            routeId = "route:main-feed",
            connectionId = "connection:Supply.L1-to-Q1.1",
            sourceAnchorId = sourceAnchor.anchorId,
            targetAnchorId = targetAnchor.anchorId,
            laneId = testSpatialLaneId(coordinate = 120),
            points = listOf(sourceAnchor.point, targetAnchor.point),
        )
        val gridReferences = listOf(
            gridReference(occurrences[0], rowIndex = 0, rowLabel = "A", columnIndex = 0, columnNumber = 1),
            gridReference(occurrences[1], rowIndex = 0, rowLabel = "A", columnIndex = 1, columnNumber = 2),
        )
        val qualityTrace = testSpatialQualityTrace(
            sheetTrace = trace,
            gridTrace = grid.sourceTrace,
            factTraces = occurrences.map { occurrence -> occurrence.sourceTrace } +
                listOf(region.sourceTrace, alignment.sourceTrace) +
                anchors.map { anchor -> anchor.sourceTrace } +
                listOf(route.sourceTrace) +
                gridReferences.map { reference -> reference.sourceTrace },
        )
        return SpatialSheet(
            sheetId = TEST_SPATIAL_SHEET_ID,
            extent = SpatialRect(0, 0, 1200, 800),
            drawingArea = drawingArea,
            grid = grid,
            occurrences = occurrences,
            regions = listOf(region),
            constructs = emptyList(),
            alignments = listOf(alignment),
            anchors = anchors,
            lanes = listOf(lane),
            routes = listOf(route),
            gridReferences = gridReferences,
            quality = SpatialQualitySnapshot(
                SpatialQualitySnapshotId(TEST_SPATIAL_SHEET_ID),
                TEST_SPATIAL_SHEET_ID,
                SpatialQualityCompiler().measure(
                    drawingArea = drawingArea,
                    occurrences = occurrences,
                    constructs = emptyList(),
                    lanes = listOf(lane),
                    routes = listOf(route),
                ),
                qualityTrace,
            ),
            sourceTrace = trace,
        )
    }

    private fun anchor(
        occurrenceId: String,
        portId: String,
        side: SpatialBoundarySide,
        point: SpatialPoint,
    ): SpatialAnchorPosition {
        val occurrence = SpatialOccurrenceId(TEST_SPATIAL_SHEET_ID, occurrenceId)
        val port = StableSemanticIdentity(portId)
        return SpatialAnchorPosition(
            anchorId = SpatialAnchorId(TEST_SPATIAL_SHEET_ID, occurrence, port),
            sheetId = TEST_SPATIAL_SHEET_ID,
            subject = SpatialOccurrencePortSubject(occurrence, port),
            side = side,
            point = point,
            sourceTrace = SpatialSourceTrace(
                projectionIds = listOf(TEST_SPATIAL_SHEET_ID, occurrenceId, portId),
                geometryElementIds = listOf(GeometryElementId("geometry:$occurrenceId:$portId")),
            ),
        )
    }

    private fun gridReference(
        occurrence: com.engineeringood.athena.spatial.SpatialOccurrenceGeometry,
        rowIndex: Int,
        rowLabel: String,
        columnIndex: Int,
        columnNumber: Int,
    ): SpatialGridReference {
        val subject = SpatialGridReferenceSubject.Occurrence(occurrence.occurrenceId)
        return SpatialGridReference(
            gridReferenceId = SpatialGridReferenceId(TEST_SPATIAL_SHEET_ID, subject),
            sheetId = TEST_SPATIAL_SHEET_ID,
            gridId = "grid:test",
            subject = subject,
            rowIndex = rowIndex,
            rowLabel = rowLabel,
            columnIndex = columnIndex,
            columnNumber = columnNumber,
            cellReference = "$rowLabel$columnNumber",
            sourceTrace = occurrence.sourceTrace,
        )
    }
}
