package com.engineeringood.athena.compiler

import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationPoint
import com.engineeringood.athena.projection.ProjectionConnectionEndpoint
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.spatial.SpatialAnchorId
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialGridReferenceSubject
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialSheet
import com.engineeringood.athena.spatial.SpatialSourceTrace
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DedicatedM41ExampleTest {

    @Test
    fun `M41 example compiles through four realities with spatial milestone facts`() {
        val source = loadDedicatedM41ExampleSource()
        assertTrue(Files.isRegularFile(source.sampleRoot.resolve("athena.yaml")))
        assertTrue(Files.isRegularFile(source.sourcePath))

        val compiler = AthenaCompiler()
        val lock = compiler.materializeRepositoryLock(source.sampleRoot)
        assertTrue(
            lock.isValid,
            lock.diagnostics.joinToString("\n") { diagnostic -> "${diagnostic.code}: ${diagnostic.message}" },
        )
        assertTrue(compiler.validateRepositoryContract(source.sampleRoot).isValid)
        assertTrue(compiler.validateRepositoryLock(source.sampleRoot).isValid)

        val compiled = compileDedicatedM41Example(source, compiler)
        val spatialDocument = compiled.spatialDocument
        val spatial = spatialDocument.sheets.single()

        assertExactSheetFacts(spatialDocument, spatial)
        assertDedicatedM41SpatialGolden(spatial)
        assertGridCoverage(spatial)
        assertRouteAndLaneCoverage(compiled.projection, spatial)
        assertPresentationPreservesSpatial(compiled.presentationDocument, spatial)
        assertProjectionPermutations(compiled.projection, spatialDocument)
        assertRoutingPermutations(compiled.projection, spatial)
        assertGridPermutations(compiled.projection, spatial)
    }

    private fun assertExactSheetFacts(document: SpatialDocument, sheet: SpatialSheet) {
        assertEquals(1, document.sheets.size)
        assertEquals("schematic/sheet/S1", sheet.sheetId)
        assertEquals(SpatialRect(0, 0, 1200, 800), sheet.extent)
        assertEquals(SpatialRect(40, 60, 1120, 640), sheet.drawingArea)
        assertEquals(8, sheet.occurrences.size)
        assertEquals(3, sheet.regions.size)
        assertEquals(7, sheet.constructs.size)
        assertEquals(10, sheet.alignments.size)
        assertEquals(16, sheet.anchors.size)
        assertEquals(9, sheet.routes.size)
        assertEquals(7, sheet.lanes.size)
        assertEquals(15, sheet.gridReferences.size)
        assertTrue(sheet.occurrences.all { occurrence -> occurrence.placementReason.text.isNotBlank() })
    }

    private fun assertGridCoverage(sheet: SpatialSheet) {
        val expectedSubjects = (
            sheet.occurrences.map { occurrence -> occurrence.occurrenceId } +
                sheet.constructs.map { construct -> construct.constructId }
            ).toSet()
        val actualSubjects = sheet.gridReferences.map { reference ->
            when (val subject = reference.subject) {
                is SpatialGridReferenceSubject.Occurrence -> subject.occurrenceId
                is SpatialGridReferenceSubject.Construct -> subject.constructId
            }
        }.toSet()
        assertEquals(expectedSubjects, actualSubjects)
        sheet.gridReferences.forEach { reference ->
            assertEquals(sheet.sheetId, reference.sheetId)
            assertTrue(reference.rowLabel.matches(Regex("[A-Z]+")))
            assertTrue(reference.columnNumber > 0)
            assertEquals("${reference.rowLabel}${reference.columnNumber}", reference.cellReference)
            assertTracePresent(reference.sourceTrace)
        }
    }

    private fun assertRouteAndLaneCoverage(projection: ProjectionDocument, sheet: SpatialSheet) {
        val sheetId = sheet.sheetId
        fun endpointAnchorId(endpoint: ProjectionConnectionEndpoint): SpatialAnchorId = SpatialAnchorId(
            sheetId = sheetId,
            occurrenceId = SpatialOccurrenceId(sheetId, endpoint.occurrencePortId.occurrenceId.value),
            portId = endpoint.occurrencePortId.portId,
        )

        val expectedAnchorsByConnection = projection.connections.associate { connection ->
            connection.semanticId to (
                endpointAnchorId(requireNotNull(connection.source)) to
                    endpointAnchorId(requireNotNull(connection.target))
                )
        }
        val expectedAnchorIds = expectedAnchorsByConnection.values
            .flatMap { (source, target) -> listOf(source, target) }
            .toSet()
        assertEquals(expectedAnchorIds, sheet.anchors.map { anchor -> anchor.anchorId }.toSet())
        assertEquals(expectedAnchorIds.size, sheet.anchors.size)
        assertEquals(expectedAnchorsByConnection.keys, sheet.routes.map { route -> route.connectionId }.toSet())
        assertEquals(expectedAnchorsByConnection.size, sheet.routes.size)

        val anchorsById = sheet.anchors.associateBy { anchor -> anchor.anchorId }
        val lanesById = sheet.lanes.associateBy { lane -> lane.laneId }
        sheet.routes.forEach { route ->
            val (sourceAnchorId, targetAnchorId) = expectedAnchorsByConnection.getValue(route.connectionId)
            assertEquals(sheetId, route.sheetId)
            assertEquals(sourceAnchorId, route.sourceAnchorId)
            assertEquals(targetAnchorId, route.targetAnchorId)
            assertEquals(anchorsById.getValue(sourceAnchorId).point, route.points.first())
            assertEquals(anchorsById.getValue(targetAnchorId).point, route.points.last())
            assertTrue(route.points.size >= 2)
            assertTrue(route.points.zipWithNext().all { (start, end) ->
                start != end && (start.x == end.x || start.y == end.y)
            })
            assertTrue(route.routeId in lanesById.getValue(route.laneId).routeIds)
            assertTracePresent(route.sourceTrace)
        }

        val routesByLane = sheet.routes.groupBy { route -> route.laneId }
            .mapValues { (_, routes) -> routes.map { route -> route.routeId }.sortedBy { routeId -> routeId.value } }
        assertEquals(sheet.lanes.map { lane -> lane.laneId }.toSet(), routesByLane.keys)
        sheet.lanes.forEach { lane ->
            assertEquals(sheetId, lane.sheetId)
            assertEquals(routesByLane.getValue(lane.laneId), lane.routeIds)
        }
    }

    private fun assertPresentationPreservesSpatial(presentation: PresentationDocument, sheet: SpatialSheet) {
        assertEquals(sheet.extent.width, presentation.canvasWidth)
        assertEquals(sheet.extent.height, presentation.canvasHeight)
        assertEquals(sheet.sheetId, presentation.drawingComposition?.sheetId)
        val occurrencesBySubject = presentation.occurrences.associateBy { occurrence -> occurrence.semanticId }
        assertEquals(sheet.occurrences.size, presentation.occurrences.size)
        assertEquals(sheet.occurrences.size, occurrencesBySubject.size)
        sheet.occurrences.forEach { occurrence ->
            val presented = occurrencesBySubject.getValue(occurrence.subjectId)
            assertEquals(occurrence.rectangle.x, presented.bounds.x)
            assertEquals(occurrence.rectangle.y, presented.bounds.y)
            assertEquals(occurrence.rectangle.width, presented.bounds.width)
            assertEquals(occurrence.rectangle.height, presented.bounds.height)
            assertEquals(occurrence.sourceTrace.projectionIds, presented.sourceProjectionIds)
        }

        val connectorsByRouteId = presentation.connectors.associateBy { connector -> connector.routeId }
        assertEquals(sheet.routes.size, presentation.connectors.size)
        assertEquals(sheet.routes.size, connectorsByRouteId.size)
        sheet.routes.forEach { route ->
            val connector = connectorsByRouteId.getValue(route.routeId.value)
            assertEquals(route.points.map { point -> PresentationPoint(point.x, point.y) }, connector.routePoints)
            assertEquals(route.sourceAnchorId.portId, connector.sourceEndpoint.portSemanticId)
            assertEquals(route.targetAnchorId.portId, connector.targetEndpoint.portSemanticId)
            assertEquals(route.sourceAnchorId.value, connector.sourceEndpoint.anchorId.value)
            assertEquals(route.targetAnchorId.value, connector.targetEndpoint.anchorId.value)
        }
    }

    private fun assertProjectionPermutations(projection: ProjectionDocument, expected: SpatialDocument) {
        val permutations = linkedMapOf(
            "nodes" to projection.copy(nodes = projection.nodes.reversed()),
            "Connections" to projection.copy(connections = projection.connections.reversed()),
            "ports" to projection.copy(occurrencePorts = projection.occurrencePorts.reversed()),
            "resolved subjects" to projection.copy(resolvedSubjects = projection.resolvedSubjects.reversed()),
            "Sheets" to projection.copy(sheets = projection.sheets.reversed()),
            "Sheet subjects" to projection.copy(
                sheets = projection.sheets.map { sheet -> sheet.copy(subjects = sheet.subjects.reversed()) },
            ),
            "all unordered Projection inputs" to projection.copy(
                nodes = projection.nodes.reversed(),
                connections = projection.connections.reversed(),
                occurrencePorts = projection.occurrencePorts.reversed(),
                resolvedSubjects = projection.resolvedSubjects.reversed(),
                sheets = projection.sheets.reversed().map { sheet -> sheet.copy(subjects = sheet.subjects.reversed()) },
            ),
        )
        permutations.forEach { (name, input) ->
            val actual = assertIs<RealityTransformationResult.Success<SpatialDocument>>(
                ProjectionSpatialCompiler().transform(input),
                "Permutation '$name' must compile successfully.",
            ).output
            assertEquals(expected, actual, "Permutation '$name' changed SpatialDocument.")
            assertDedicatedM41SpatialGolden(actual.sheets.single())
        }
    }

    private fun assertRoutingPermutations(projection: ProjectionDocument, sheet: SpatialSheet) {
        val sheetInputs = projection.sheets.map { projectionSheet ->
            SpatialRoutingSheetInput(projectionSheet.sheetId.value, ProjectionSpatialLayout.DRAWING_AREA)
        }
        val permutations = linkedMapOf(
            "Projection Connections" to SpatialRouteCompiler().compile(
                projection.copy(connections = projection.connections.reversed()), sheetInputs,
                sheet.occurrences, sheet.anchors,
            ),
            "Sheets" to SpatialRouteCompiler().compile(
                projection, sheetInputs.reversed(), sheet.occurrences, sheet.anchors,
            ),
            "Occurrences" to SpatialRouteCompiler().compile(
                projection, sheetInputs, sheet.occurrences.reversed(), sheet.anchors,
            ),
            "Anchors" to SpatialRouteCompiler().compile(
                projection, sheetInputs, sheet.occurrences, sheet.anchors.reversed(),
            ),
            "trace contributors" to SpatialRouteCompiler().compile(
                projection,
                sheetInputs,
                sheet.occurrences,
                sheet.anchors.map { anchor ->
                    anchor.copy(
                        sourceTrace = SpatialSourceTrace(
                            anchor.sourceTrace.projectionIds.reversed(),
                            anchor.sourceTrace.geometryElementIds.reversed(),
                        ),
                    )
                },
            ),
        )
        permutations.forEach { (name, actual) ->
            assertTrue(actual.diagnostics.isEmpty(), "$name permutation must compile without diagnostics.")
            assertEquals(sheet.routes, actual.routes, "$name permutation changed Routes.")
            assertEquals(sheet.lanes, actual.lanes, "$name permutation changed Lanes.")
        }
    }

    private fun assertGridPermutations(projection: ProjectionDocument, sheet: SpatialSheet) {
        val input = SpatialGridSheetInput(
            sheetId = sheet.sheetId,
            order = projection.sheets.single().order,
            drawingArea = sheet.drawingArea,
            grid = projection.sheets.single().grid,
            sourceTrace = sheet.grid.sourceTrace,
        )
        val permutations = linkedMapOf(
            "Occurrences" to SpatialGridCompiler().compile(listOf(input), sheet.occurrences.reversed(), sheet.constructs),
            "Constructs" to SpatialGridCompiler().compile(listOf(input), sheet.occurrences, sheet.constructs.reversed()),
            "all inputs" to SpatialGridCompiler().compile(
                listOf(input), sheet.occurrences.reversed(), sheet.constructs.reversed(),
            ),
        )
        permutations.forEach { (name, actual) ->
            assertTrue(actual.diagnostics.isEmpty(), "$name grid permutation must compile without diagnostics.")
            assertEquals(sheet.grid, actual.grids.single(), "$name grid permutation changed Grid.")
            assertEquals(sheet.gridReferences, actual.references, "$name grid permutation changed references.")
        }
    }

    private fun assertTracePresent(trace: SpatialSourceTrace) {
        assertTrue(trace.projectionIds.isNotEmpty())
        assertTrue(trace.geometryElementIds.isNotEmpty())
    }
}
