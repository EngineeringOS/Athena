package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionEndpoint
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ProjectionConstructId
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionOccurrencePort
import com.engineeringood.athena.projection.ProjectionOccurrencePortId
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetGrid
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.projection.ProjectionRegion
import com.engineeringood.athena.projection.ProjectionSheetConstruct
import com.engineeringood.athena.projection.ProjectionSheetSubject
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialSheet
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialSourceTrace
import com.engineeringood.athena.spatial.SpatialAlignmentId
import com.engineeringood.athena.spatial.SpatialAlignmentSource
import com.engineeringood.athena.spatial.SpatialAnchorId
import com.engineeringood.athena.spatial.SpatialConstructId
import com.engineeringood.athena.spatial.SpatialRegionId
import com.engineeringood.athena.spatial.SpatialGridReferenceSubject
import com.engineeringood.athena.spatial.SpatialGridReferenceId
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialOccurrencePortSubject
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialRegionGeometry
import com.engineeringood.athena.spatial.SpatialConstructGeometry
import com.engineeringood.athena.spatial.SpatialRouteId
import com.engineeringood.athena.spatial.SpatialGridDefinition
import com.engineeringood.athena.spatial.SpatialQualitySnapshot
import com.engineeringood.athena.spatial.SpatialQualitySnapshotId
import com.engineeringood.athena.spatial.SpatialQualityMetrics
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ProjectionSpatialCompilerTest {
    @Test
    fun `projection to spatial emits spatial document through typed transformation`() {
        val transformation: RealityTransformation<ProjectionDocument, SpatialDocument> =
            ProjectionSpatialCompiler()

        val result = transformation.transform(projectionDocument())

        val output = assertIs<RealityTransformationResult.Success<SpatialDocument>>(result).output
        val sheet = output.sheets.single()
        assertEquals(listOf("projection/node/component:Supply", "projection/node/component:Q1"), sheet.occurrences.map {
            occurrence -> occurrence.occurrenceId.projectionId
        })
        assertEquals(2, sheet.occurrences.size)
        assertEquals(1, sheet.regions.size)
        assertEquals(2, sheet.anchors.size)
        val sheetId = "engineering-projection/sheet/01-main"
        assertEquals(
            listOf(
                SpatialAlignmentId(
                    sheetId = sheetId,
                    source = SpatialAlignmentSource.Region(
                        SpatialRegionId(sheetId, "$sheetId/region/unassigned"),
                    ),
                ),
            ),
            sheet.alignments.map { alignment -> alignment.alignmentId },
        )
        assertEquals(listOf(sheet.routes.single().laneId), sheet.lanes.map { lane -> lane.laneId })
        assertEquals("grid:main", sheet.grid.gridId)
        assertEquals(
            listOf(
                SpatialGridReferenceSubject.Occurrence(
                    SpatialOccurrenceId(sheetId, "projection/node/component:Q1"),
                ),
                SpatialGridReferenceSubject.Occurrence(
                    SpatialOccurrenceId(sheetId, "projection/node/component:Supply"),
                ),
            ),
            sheet.gridReferences.map { reference -> reference.subject },
        )
        assertEquals(listOf("C3", "A3"), sheet.gridReferences.map { reference -> reference.cellReference })
        assertEquals(SpatialRect(0, 0, 1200, 800), sheet.extent)
        assertEquals(SpatialRect(40, 60, 1120, 640), sheet.drawingArea)
        assertEquals(listOf(sheetId), sheet.sourceTrace.projectionIds)
        assertEquals(
            listOf(GeometryElementId("projection-sheet:$sheetId")),
            sheet.sourceTrace.geometryElementIds,
        )
        assertEquals(sheetId, sheet.quality.qualitySnapshotId.sheetId)
        assertEquals(sheetId, sheet.quality.sheetId)
    }

    @Test
    fun `one repeated Projection identity stays Sheet qualified through Spatial and Presentation`() {
        val base = projectionDocument()
        val repeatedNode = base.nodes.first().copy(
            projectionId = ProjectionNodeId("projection/node/repeated"),
            semanticId = StableSemanticIdentity("component:Repeated"),
            label = "Repeated",
            originGeometryElementId = GeometryElementId("origin:repeated"),
        )
        fun sheet(id: String, order: Int, node: ProjectionNode): ProjectionSheet {
            val sheetId = ProjectionSheetId(id)
            val subject = ProjectionSheetSubject(node.semanticId, nodeIds = listOf(node.projectionId))
            return ProjectionSheet(
                sheetId = sheetId,
                displayName = id.substringAfterLast('/'),
                order = order,
                subjects = listOf(subject),
                grid = ProjectionSheetGrid("grid:$order", rows = 3, columns = 4),
                publication = ProjectionSheetPublication.fromProjectionState(
                    sheetId,
                    id.substringAfterLast('/'),
                    order,
                    listOf(subject),
                ),
            )
        }
        val first = sheet("engineering-projection/sheet/01-first", 0, repeatedNode)
        val second = sheet("engineering-projection/sheet/02-second", 1, repeatedNode)
        val projection = base.copy(
            nodes = listOf(repeatedNode),
            connections = emptyList(),
            occurrencePorts = emptyList(),
            sheets = listOf(second, first),
        )

        val result = ProjectionSpatialCompiler().transform(projection)
        val document = assertIs<RealityTransformationResult.Success<SpatialDocument>>(
            result,
            (result as? RealityTransformationResult.Failure)?.diagnostics?.joinToString("\n") { diagnostic ->
                "${diagnostic.subject}: ${diagnostic.problem} ${diagnostic.correction}"
            },
        ).output
        val permuted = assertIs<RealityTransformationResult.Success<SpatialDocument>>(
            ProjectionSpatialCompiler().transform(
                projection.copy(nodes = projection.nodes.reversed(), sheets = projection.sheets.reversed()),
            ),
        ).output

        assertEquals(listOf(first.sheetId.value, second.sheetId.value), document.sheets.map { it.sheetId })
        assertEquals(listOf(repeatedNode.projectionId.value, repeatedNode.projectionId.value), document.sheets.map { sheet ->
            sheet.occurrences.single().occurrenceId.projectionId
        })
        assertEquals(2, document.sheets.map { sheet -> sheet.occurrences.single().occurrenceId }.distinct().size)
        assertEquals(listOf("Repeated", "Repeated"), document.sheets.map { sheet ->
            sheet.occurrences.single().subjectId.value.substringAfterLast(':')
        })
        assertTrue(document.sheets.all { sheet -> sheet.gridReferences.all { it.sheetId == sheet.sheetId } })
        assertTrue(document.sheets.all { sheet -> sheet.quality.sheetId == sheet.sheetId })
        assertEquals(document, permuted)
        val presentations = assertIs<RealityTransformationResult.Success<List<PresentationDocument>>>(
            transformSpatialSheetsToPresentation(document, projection.view),
        ).output
        assertEquals(listOf(first.sheetId.value, second.sheetId.value), presentations.map { presentation ->
            presentation.drawingComposition?.sheetId
        })
        assertEquals(2, presentations.flatMap { presentation -> presentation.occurrences }.map { occurrence ->
            occurrence.occurrenceId
        }.distinct().size)
    }

    @Test
    fun `projection to spatial derives routes from projection connection identity`() {
        val result = ProjectionSpatialCompiler().transform(projectionDocument())
        val output = assertIs<RealityTransformationResult.Success<SpatialDocument>>(result).output
        val sheet = output.sheets.single()

        assertEquals(1, sheet.routes.size)
        val route = sheet.routes.single()
        assertEquals(
            "route:sheet=engineering-projection%2Fsheet%2F01-main:" +
                "connection=projection%2Fconnection%2Fconnection%3ASupply.L1-to-Q1.1",
            route.routeId.value,
        )
        assertEquals("connection:Supply.L1-to-Q1.1", route.connectionId.value)
        assertEquals("engineering-projection/sheet/01-main", route.laneId.sheetId)
        assertEquals("projection/node/component:Supply", route.sourceAnchorId.occurrenceId.projectionId)
        assertEquals("port:Supply.L1", route.sourceAnchorId.portId.value)
        assertEquals("projection/node/component:Q1", route.targetAnchorId.occurrenceId.projectionId)
        assertEquals("port:Q1.1", route.targetAnchorId.portId.value)
        val anchorById = sheet.anchors.associateBy { anchor -> anchor.anchorId }
        val sourceAnchor = requireNotNull(anchorById[route.sourceAnchorId])
        val targetAnchor = requireNotNull(anchorById[route.targetAnchorId])
        assertEquals(sourceAnchor.point, route.points.first())
        assertEquals(targetAnchor.point, route.points.last())
    }

    @Test
    fun `projection to spatial publishes typed per Sheet quality metrics`() {
        val result = ProjectionSpatialCompiler().transform(projectionDocument())
        val output = assertIs<RealityTransformationResult.Success<SpatialDocument>>(result).output
        val metrics = output.sheets.single().quality.metrics

        assertEquals(
            SpatialQualityMetrics(
                occurrenceOverlapCount = 0,
                constructContainmentFailureCount = 0,
                routeBodyIntersectionCount = 0,
                routeCrossingCount = 0,
                twistCount = 0,
                usedLaneCount = 1,
                peakRoutesPerLane = 1,
                density = 2.0 / 716_800.0,
                occupancy = 6_400.0 / 716_800.0,
            ),
            metrics,
        )
    }

    @Test
    fun `quality numerators remain isolated across ordered Sheets`() {
        val projection = projectionDocument()
        val third = ProjectionNode(
            projectionId = ProjectionNodeId("projection/node/component:Q2"),
            semanticId = StableSemanticIdentity("component:Q2"),
            label = "Q2",
            originGeometryElementId = GeometryElementId("origin:Q2"),
        )
        val firstSheetId = ProjectionSheetId("engineering-projection/sheet/01-first")
        val secondSheetId = ProjectionSheetId("engineering-projection/sheet/02-second")
        val firstSubjects = projection.nodes.map { node ->
            ProjectionSheetSubject(node.semanticId, nodeIds = listOf(node.projectionId))
        }
        val secondSubjects = listOf(
            ProjectionSheetSubject(third.semanticId, nodeIds = listOf(third.projectionId)),
        )
        val input = projection.copy(
            nodes = projection.nodes + third,
            connections = emptyList(),
            occurrencePorts = emptyList(),
            sheets = listOf(
                ProjectionSheet(
                    sheetId = firstSheetId,
                    displayName = "First",
                    order = 0,
                    subjects = firstSubjects,
                    grid = ProjectionSheetGrid("grid:first", rows = 3, columns = 4),
                    publication = ProjectionSheetPublication.fromProjectionState(
                        firstSheetId,
                        "First",
                        0,
                        firstSubjects,
                    ),
                ),
                ProjectionSheet(
                    sheetId = secondSheetId,
                    displayName = "Second",
                    order = 1,
                    subjects = secondSubjects,
                    grid = ProjectionSheetGrid("grid:second", rows = 3, columns = 4),
                    publication = ProjectionSheetPublication.fromProjectionState(
                        secondSheetId,
                        "Second",
                        1,
                        secondSubjects,
                    ),
                ),
            ),
        )

        val output = assertIs<RealityTransformationResult.Success<SpatialDocument>>(
            ProjectionSpatialCompiler().transform(input),
        ).output

        assertEquals(listOf(firstSheetId.value, secondSheetId.value), output.sheets.map(SpatialSheet::sheetId))
        assertEquals(2.0 / 716_800.0, output.sheets[0].quality.metrics.density)
        assertEquals(6_400.0 / 716_800.0, output.sheets[0].quality.metrics.occupancy)
        assertEquals(1.0 / 716_800.0, output.sheets[1].quality.metrics.density)
        assertEquals(3_200.0 / 716_800.0, output.sheets[1].quality.metrics.occupancy)
    }

    @Test
    fun `projection to spatial reports plain diagnostics before presentation`() {
        val result = ProjectionSpatialCompiler().transform(
            ProjectionDocument(
                view = ViewDefinition(id = "", displayName = "Broken"),
                nodes = emptyList(),
                connections = emptyList(),
                sheets = emptyList(),
            ),
        )

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertFalse(failure.diagnostics.isEmpty())
        assertTrue(failure.diagnostics.any { diagnostic ->
            diagnostic.reality == "Projection Reality" &&
                diagnostic.message == "missing view identity"
        })
        assertTrue(failure.diagnostics.any { diagnostic ->
            diagnostic.reality == "Projection Reality" &&
                diagnostic.message == "missing sheet facts"
        })
    }

    @Test
    fun `projection to spatial fails closed when owning sheet grid is missing`() {
        val projection = projectionDocument()
        val result = ProjectionSpatialCompiler().transform(
            projection.copy(sheets = projection.sheets.map { sheet -> sheet.copy(grid = null) }),
        )

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertEquals(1, failure.diagnostics.size)
        assertEquals("Sheet engineering-projection/sheet/01-main grid", failure.diagnostics.single().subject)
        assertEquals("is missing", failure.diagnostics.single().problem)
        assertEquals(
            "Define a grid for Sheet engineering-projection/sheet/01-main before compiling Grid References.",
            failure.diagnostics.single().correction,
        )
        assertEquals(
            listOf("engineering-projection/sheet/01-main"),
            failure.diagnostics.single().sourceTrace?.projectionIds,
        )
    }

    @Test
    fun `projection to spatial diagnoses blank grid identity without throwing`() {
        val projection = projectionDocument()
        val result = ProjectionSpatialCompiler().transform(
            projection.copy(
                sheets = projection.sheets.map { sheet ->
                    sheet.copy(grid = sheet.grid?.copy(gridId = ""))
                },
            ),
        )

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertEquals("Sheet engineering-projection/sheet/01-main grid", failure.diagnostics.single().subject)
        assertEquals("has a blank identity", failure.diagnostics.single().problem)
        assertEquals(
            listOf("engineering-projection/sheet/01-main"),
            failure.diagnostics.single().sourceTrace?.projectionIds,
        )
    }

    @Test
    fun `projection to spatial fails closed for a cross sheet connection`() {
        val projection = projectionDocument()
        val source = projection.nodes.first { node -> node.semanticId.value == "component:Supply" }
        val target = projection.nodes.first { node -> node.semanticId.value == "component:Q1" }
        val connection = projection.connections.single()
        val sourceSubjects = listOf(
            ProjectionSheetSubject(source.semanticId, nodeIds = listOf(source.projectionId)),
            ProjectionSheetSubject(connection.semanticId, connectionIds = listOf(connection.projectionId)),
        )
        val targetSubjects = listOf(
            ProjectionSheetSubject(target.semanticId, nodeIds = listOf(target.projectionId)),
        )
        val sourceSheetId = ProjectionSheetId("engineering-projection/sheet/01-source")
        val targetSheetId = ProjectionSheetId("engineering-projection/sheet/02-target")
        val result = ProjectionSpatialCompiler().transform(
            projection.copy(
                sheets = listOf(
                    ProjectionSheet(
                        sheetId = sourceSheetId,
                        displayName = "Source",
                        order = 0,
                        subjects = sourceSubjects,
                        grid = ProjectionSheetGrid("grid:source", rows = 3, columns = 4),
                        publication = ProjectionSheetPublication.fromProjectionState(
                            sourceSheetId,
                            "Source",
                            0,
                            sourceSubjects,
                        ),
                    ),
                    ProjectionSheet(
                        sheetId = targetSheetId,
                        displayName = "Target",
                        order = 1,
                        subjects = targetSubjects,
                        grid = ProjectionSheetGrid("grid:target", rows = 3, columns = 4),
                        publication = ProjectionSheetPublication.fromProjectionState(
                            targetSheetId,
                            "Target",
                            1,
                            targetSubjects,
                        ),
                    ),
                ),
            ),
        )

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertEquals(1, failure.diagnostics.size)
        assertEquals("Spatial Reality", failure.diagnostics.single().reality)
        assertEquals(
            "connects Anchors on different Sheets '${sourceSheetId.value}' and '${targetSheetId.value}'",
            failure.diagnostics.single().problem,
        )
        assertEquals(
            "Keep both endpoints on one Sheet or defer explicit multi-Sheet continuation to M45.",
            failure.diagnostics.single().correction,
        )
    }

    @Test
    fun `one invalid Route blocks valid sibling and all downstream Spatial facts`() {
        val projection = projectionDocument()
        val mainSheet = projection.sheets.single()
        val sourcePort = projection.occurrencePorts.first { port ->
            port.occurrencePortId.portId.value == "port:Supply.L1"
        }
        val foreignNode = ProjectionNode(
            projectionId = ProjectionNodeId("projection/node/component:Q2"),
            semanticId = StableSemanticIdentity("component:Q2"),
            label = "Q2",
            originGeometryElementId = GeometryElementId("origin:Q2"),
        )
        val foreignPort = ProjectionOccurrencePort(
            ProjectionOccurrencePortId(foreignNode.projectionId, StableSemanticIdentity("port:Q2.1")),
            GeometryElementId("origin:Q2.1"),
        )
        val invalidConnection = ProjectionConnection(
            projectionId = ProjectionConnectionId("projection/connection/connection:Supply.L1-to-Q2.1"),
            semanticId = StableSemanticIdentity("connection:Supply.L1-to-Q2.1"),
            originGeometryElementId = GeometryElementId("origin:connection:Supply-Q2"),
            source = ProjectionConnectionEndpoint(sourcePort.occurrencePortId),
            target = ProjectionConnectionEndpoint(foreignPort.occurrencePortId),
        )
        val mainSubjects = mainSheet.subjects + ProjectionSheetSubject(
            invalidConnection.semanticId,
            connectionIds = listOf(invalidConnection.projectionId),
        )
        val foreignSheetId = ProjectionSheetId("engineering-projection/sheet/02-foreign")
        val foreignSubjects = listOf(
            ProjectionSheetSubject(foreignNode.semanticId, nodeIds = listOf(foreignNode.projectionId)),
        )
        val input = projection.copy(
            nodes = projection.nodes + foreignNode,
            occurrencePorts = projection.occurrencePorts + foreignPort,
            connections = projection.connections + invalidConnection,
            sheets = listOf(
                mainSheet.copy(
                    subjects = mainSubjects,
                    publication = ProjectionSheetPublication.fromProjectionState(
                        mainSheet.sheetId,
                        mainSheet.displayName,
                        mainSheet.order,
                        mainSubjects,
                    ),
                ),
                ProjectionSheet(
                    sheetId = foreignSheetId,
                    displayName = "Foreign",
                    order = 1,
                    subjects = foreignSubjects,
                    grid = ProjectionSheetGrid("grid:foreign", rows = 3, columns = 4),
                    publication = ProjectionSheetPublication.fromProjectionState(
                        foreignSheetId,
                        "Foreign",
                        1,
                        foreignSubjects,
                    ),
                ),
            ),
        )

        val result: RealityTransformationResult<SpatialDocument> = ProjectionSpatialCompiler().transform(input)
        val failure = assertIs<RealityTransformationResult.Failure>(result)

        assertEquals(1, failure.diagnostics.size)
        assertEquals("Connection ${invalidConnection.projectionId.value}", failure.diagnostics.single().subject)
        assertEquals(
            "connects Anchors on different Sheets '${mainSheet.sheetId.value}' and '${foreignSheetId.value}'",
            failure.diagnostics.single().problem,
        )
    }

    @Test
    fun `final coverage validator reports a projected Occurrence missing from assembled Spatial facts`() {
        val projection = projectionDocument()
        val compiled = assertIs<RealityTransformationResult.Success<SpatialDocument>>(
            ProjectionSpatialCompiler().transform(projection),
        ).output
        val sheet = compiled.sheets.single()
        val missingNode = projection.nodes.last()
        val invalid = SpatialDocument(
            listOf(sheet.copy(occurrences = sheet.occurrences.dropLast(1))),
        )

        val diagnostics = ProjectionSpatialCoverageValidator().validate(projection, invalid)

        assertEquals(
            listOf(
                SpatialDiagnostic(
                    subject = "Occurrence ${missingNode.projectionId.value} on Sheet ${sheet.sheetId}",
                    problem = "has 0 final Spatial geometry facts",
                    correction = "Publish exactly one Sheet-qualified Occurrence for every projected Sheet subject.",
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf(sheet.sheetId, missingNode.projectionId.value),
                        geometryElementIds = listOf(
                            projection.sheets.single().originGeometryElementId,
                            missingNode.originGeometryElementId,
                        ),
                    ),
                ),
            ),
            diagnostics,
        )
    }

    @Test
    fun `final coverage validator reports every missing Projection owned final fact`() {
        val projection = projectionDocumentWithGrouping()
        val compiled = assertIs<RealityTransformationResult.Success<SpatialDocument>>(
            ProjectionSpatialCompiler().transform(projection),
        ).output
        val sheet = compiled.sheets.single()
        val projectionSheet = projection.sheets.single()
        val supply = projection.nodes.first { node -> node.label == "Supply" }
        val breaker = projection.nodes.first { node -> node.label == "Q1" }
        val connection = projection.connections.single()
        val sourcePort = requireNotNull(connection.source).occurrencePortId
        val targetPort = requireNotNull(connection.target).occurrencePortId
        val region = projectionSheet.regions.single()
        val construct = projectionSheet.constructs.single()
        val invalid = SpatialDocument(
            listOf(
                sheet.copy(
                    regions = emptyList(),
                    constructs = emptyList(),
                    alignments = emptyList(),
                    anchors = emptyList(),
                    lanes = emptyList(),
                    routes = emptyList(),
                    gridReferences = emptyList(),
                ),
            ),
        )

        val diagnostics = ProjectionSpatialCoverageValidator().validate(projection, invalid)
        val sheetId = projectionSheet.sheetId.value
        val expectedRegionTrace = SpatialSourceTrace(
            projectionIds = listOf(sheetId, region.regionId, supply.projectionId.value, breaker.projectionId.value),
            geometryElementIds = listOf(
                region.originGeometryElementId,
                supply.originGeometryElementId,
                breaker.originGeometryElementId,
            ),
        )
        val expectedConstructTrace = SpatialSourceTrace(
            projectionIds = listOf(sheetId, construct.constructId.value, supply.projectionId.value, breaker.projectionId.value),
            geometryElementIds = listOf(
                construct.originGeometryElementId,
                supply.originGeometryElementId,
                breaker.originGeometryElementId,
            ),
        )
        val expected = listOf(
            SpatialDiagnostic(
                subject = "Anchor ${SpatialAnchorId(sheetId, com.engineeringood.athena.spatial.SpatialOccurrenceId(sheetId, breaker.projectionId.value), targetPort.portId).value}",
                problem = "has 0 final Spatial Anchor facts",
                correction = "Publish exactly one Sheet-qualified Anchor for every referenced projected occurrence-port.",
                sourceTrace = endpointCoverageTrace(projection, projectionSheet, breaker, targetPort, connection),
            ),
            SpatialDiagnostic(
                subject = "Anchor ${SpatialAnchorId(sheetId, com.engineeringood.athena.spatial.SpatialOccurrenceId(sheetId, supply.projectionId.value), sourcePort.portId).value}",
                problem = "has 0 final Spatial Anchor facts",
                correction = "Publish exactly one Sheet-qualified Anchor for every referenced projected occurrence-port.",
                sourceTrace = endpointCoverageTrace(projection, projectionSheet, supply, sourcePort, connection),
            ),
            SpatialDiagnostic(
                subject = "Construct ${construct.constructId.value} on Sheet $sheetId",
                problem = "has 0 final Spatial geometry facts",
                correction = "Publish exactly one Sheet-qualified Construct for every projected Construct.",
                sourceTrace = expectedConstructTrace,
            ),
            SpatialDiagnostic(
                subject = "Grid Reference Construct ${construct.constructId.value} on Sheet $sheetId",
                problem = "has 0 final Spatial Grid Reference facts",
                correction = "Publish exactly one Sheet-qualified Grid Reference for every projected Occurrence and Construct.",
                sourceTrace = expectedConstructTrace,
            ),
            SpatialDiagnostic(
                subject = "Grid Reference Occurrence ${breaker.projectionId.value} on Sheet $sheetId",
                problem = "has 0 final Spatial Grid Reference facts",
                correction = "Publish exactly one Sheet-qualified Grid Reference for every projected Occurrence and Construct.",
                sourceTrace = occurrenceCoverageTrace(projectionSheet, breaker),
            ),
            SpatialDiagnostic(
                subject = "Grid Reference Occurrence ${supply.projectionId.value} on Sheet $sheetId",
                problem = "has 0 final Spatial Grid Reference facts",
                correction = "Publish exactly one Sheet-qualified Grid Reference for every projected Occurrence and Construct.",
                sourceTrace = occurrenceCoverageTrace(projectionSheet, supply),
            ),
            SpatialDiagnostic(
                subject = "Region ${region.regionId} on Sheet $sheetId",
                problem = "has 0 final Spatial geometry facts",
                correction = "Publish exactly one Sheet-qualified Region for every Projection placement group.",
                sourceTrace = expectedRegionTrace,
            ),
            SpatialDiagnostic(
                subject = "Route ${com.engineeringood.athena.spatial.SpatialRouteId(sheetId, connection.projectionId.value).value}",
                problem = "has 0 final Spatial Route facts",
                correction = "Publish exactly one Sheet-qualified Route for every visible Projection Connection.",
                sourceTrace = routeCoverageTrace(projection, projectionSheet, supply, breaker, connection),
            ),
        ).sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem, SpatialDiagnostic::correction))

        assertEquals(expected, diagnostics)
    }

    @Test
    fun `final coverage validator reports duplicate and extra Projection owned facts`() {
        val projection = projectionDocumentWithGrouping()
        val compiled = assertIs<RealityTransformationResult.Success<SpatialDocument>>(
            ProjectionSpatialCompiler().transform(projection),
        ).output
        val sheet = compiled.sheets.single()
        val occurrence = sheet.occurrences.first()
        val region = sheet.regions.first()
        val construct = sheet.constructs.single()
        val anchor = sheet.anchors.first()
        val route = sheet.routes.single()
        val gridReference = sheet.gridReferences.first()
        val extraOccurrenceId = SpatialOccurrenceId(sheet.sheetId, "projection/node/extra")
        val extraOccurrenceTrace = SpatialSourceTrace(
            listOf(sheet.sheetId, region.regionId.projectionId, extraOccurrenceId.projectionId),
            listOf(GeometryElementId("origin:extra")),
        )
        val extraOccurrence = occurrence.copy(
            occurrenceId = extraOccurrenceId,
            subjectId = StableSemanticIdentity("component:extra"),
            sourceTrace = extraOccurrenceTrace,
        )
        val extraRegionTrace = SpatialSourceTrace(
            listOf(sheet.sheetId, "region:extra", occurrence.occurrenceId.projectionId),
            listOf(GeometryElementId("region:extra")),
        )
        val extraRegion = SpatialRegionGeometry(
            SpatialRegionId(sheet.sheetId, "region:extra"),
            sheet.sheetId,
            listOf(occurrence.occurrenceId),
            region.bounds,
            extraRegionTrace,
        )
        val extraConstructTrace = SpatialSourceTrace(
            listOf(sheet.sheetId, "construct:extra", occurrence.occurrenceId.projectionId),
            listOf(GeometryElementId("construct:extra")),
        )
        val extraConstruct = SpatialConstructGeometry(
            SpatialConstructId(sheet.sheetId, "construct:extra"),
            sheet.sheetId,
            "extra",
            "Extra",
            listOf(occurrence.occurrenceId),
            construct.envelope,
            extraConstructTrace,
        )
        val extraPort = StableSemanticIdentity("port:extra")
        val extraAnchorTrace = SpatialSourceTrace(
            listOf(sheet.sheetId, occurrence.occurrenceId.projectionId, extraPort.value),
            listOf(GeometryElementId("anchor:extra")),
        )
        val extraAnchor = anchor.copy(
            anchorId = SpatialAnchorId(sheet.sheetId, occurrence.occurrenceId, extraPort),
            subject = SpatialOccurrencePortSubject(occurrence.occurrenceId, extraPort),
            sourceTrace = extraAnchorTrace,
        )
        val extraRouteTrace = SpatialSourceTrace(
            listOf(
                sheet.sheetId,
                "connection:extra",
                route.sourceAnchorId.occurrenceId.projectionId,
                route.sourceAnchorId.portId.value,
                route.targetAnchorId.occurrenceId.projectionId,
                route.targetAnchorId.portId.value,
            ),
            listOf(GeometryElementId("route:extra")),
        )
        val extraRoute = route.copy(
            routeId = SpatialRouteId(sheet.sheetId, "connection:extra"),
            connectionId = StableSemanticIdentity("connection:extra"),
            sourceTrace = extraRouteTrace,
        )
        val extraGridSubject = SpatialGridReferenceSubject.Occurrence(extraOccurrenceId)
        val extraGridReference = gridReference.copy(
            gridReferenceId = SpatialGridReferenceId(sheet.sheetId, extraGridSubject),
            subject = extraGridSubject,
            sourceTrace = extraOccurrenceTrace,
        )
        val extraSheetTrace = SpatialSourceTrace(
            listOf("sheet:extra"),
            listOf(GeometryElementId("sheet:extra")),
        )
        val extraSheetGridTrace = SpatialSourceTrace(
            listOf("sheet:extra", "grid:extra"),
            extraSheetTrace.geometryElementIds,
        )
        val extraSheet = SpatialSheet(
            sheetId = "sheet:extra",
            extent = sheet.extent,
            drawingArea = sheet.drawingArea,
            grid = SpatialGridDefinition("sheet:extra", "grid:extra", sheet.drawingArea, 1, 1, extraSheetGridTrace),
            occurrences = emptyList(),
            regions = emptyList(),
            constructs = emptyList(),
            alignments = emptyList(),
            anchors = emptyList(),
            lanes = emptyList(),
            routes = emptyList(),
            gridReferences = emptyList(),
            quality = SpatialQualitySnapshot(
                SpatialQualitySnapshotId("sheet:extra"),
                "sheet:extra",
                sheet.quality.metrics,
                extraSheetGridTrace,
            ),
            sourceTrace = extraSheetTrace,
        )
        val invalid = SpatialDocument(
            listOf(
                sheet.copy(
                    occurrences = sheet.occurrences + occurrence + extraOccurrence,
                    regions = sheet.regions + region + extraRegion,
                    constructs = sheet.constructs + construct + extraConstruct,
                    anchors = sheet.anchors + anchor + extraAnchor,
                    routes = sheet.routes + route + extraRoute,
                    gridReferences = sheet.gridReferences + gridReference + extraGridReference,
                ),
                extraSheet,
            ),
        )

        val diagnostics = ProjectionSpatialCoverageValidator().validate(projection, invalid)

        val expected = listOf(
            SpatialDiagnostic(
                "Anchor ${anchor.anchorId.value}",
                "has 2 final Spatial Anchor facts",
                "Publish exactly one Sheet-qualified Anchor for every referenced projected occurrence-port.",
                anchor.sourceTrace,
            ),
            SpatialDiagnostic(
                "Anchor ${extraAnchor.anchorId.value}",
                "has 1 final Spatial Anchor facts but no matching Projection fact",
                "Remove final Spatial Anchor facts not required by canonical Projection coverage.",
                extraAnchorTrace,
            ),
            SpatialDiagnostic(
                "Construct ${construct.constructId.projectionId} on Sheet ${sheet.sheetId}",
                "has 2 final Spatial geometry facts",
                "Publish exactly one Sheet-qualified Construct for every projected Construct.",
                construct.sourceTrace,
            ),
            SpatialDiagnostic(
                "Construct construct:extra on Sheet ${sheet.sheetId}",
                "has 1 final Spatial geometry facts but no matching Projection fact",
                "Remove final Spatial geometry facts not required by canonical Projection coverage.",
                extraConstructTrace,
            ),
            SpatialDiagnostic(
                "Grid Reference Occurrence ${extraOccurrenceId.projectionId} on Sheet ${sheet.sheetId}",
                "has 1 final Spatial Grid Reference facts but no matching Projection fact",
                "Remove final Spatial Grid Reference facts not required by canonical Projection coverage.",
                extraOccurrenceTrace,
            ),
            SpatialDiagnostic(
                "Grid Reference Occurrence ${gridReference.subject.projectionId} on Sheet ${sheet.sheetId}",
                "has 2 final Spatial Grid Reference facts",
                "Publish exactly one Sheet-qualified Grid Reference for every projected Occurrence and Construct.",
                gridReference.sourceTrace,
            ),
            SpatialDiagnostic(
                "Occurrence ${extraOccurrenceId.projectionId} on Sheet ${sheet.sheetId}",
                "has 1 final Spatial geometry facts but no matching Projection fact",
                "Remove final Spatial geometry facts not required by canonical Projection coverage.",
                extraOccurrenceTrace,
            ),
            SpatialDiagnostic(
                "Occurrence ${occurrence.occurrenceId.projectionId} on Sheet ${sheet.sheetId}",
                "has 2 final Spatial geometry facts",
                "Publish exactly one Sheet-qualified Occurrence for every projected Sheet subject.",
                occurrence.sourceTrace,
            ),
            SpatialDiagnostic(
                "Region ${region.regionId.projectionId} on Sheet ${sheet.sheetId}",
                "has 2 final Spatial geometry facts",
                "Publish exactly one Sheet-qualified Region for every Projection placement group.",
                region.sourceTrace,
            ),
            SpatialDiagnostic(
                "Region region:extra on Sheet ${sheet.sheetId}",
                "has 1 final Spatial geometry facts but no matching Projection fact",
                "Remove final Spatial geometry facts not required by canonical Projection coverage.",
                extraRegionTrace,
            ),
            SpatialDiagnostic(
                "Route ${extraRoute.routeId.value}",
                "has 1 final Spatial Route facts but no matching Projection fact",
                "Remove final Spatial Route facts not required by canonical Projection coverage.",
                extraRouteTrace,
            ),
            SpatialDiagnostic(
                "Route ${route.routeId.value}",
                "has 2 final Spatial Route facts",
                "Publish exactly one Sheet-qualified Route for every visible Projection Connection.",
                route.sourceTrace,
            ),
            SpatialDiagnostic(
                "Sheet sheet:extra",
                "has 1 final Spatial Sheet roots but no matching Projection Sheet",
                "Remove Spatial Sheet roots not required by canonical Projection coverage.",
                extraSheetTrace,
            ),
        ).sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem, SpatialDiagnostic::correction))
        assertEquals(expected, diagnostics)
    }

    @Test
    fun `final coverage rejects Projection semantic payload grid and provenance drift`() {
        val projection = projectionDocumentWithGrouping()
        val compiled = assertIs<RealityTransformationResult.Success<SpatialDocument>>(
            ProjectionSpatialCompiler().transform(projection),
        ).output
        val sheet = compiled.sheets.single()
        val projectionSheet = projection.sheets.single()
        val projectionGrid = requireNotNull(projectionSheet.grid)
        val occurrence = sheet.occurrences.first()
        val projectionNode = projection.nodes.single { node ->
            node.projectionId.value == occurrence.occurrenceId.projectionId
        }
        val route = sheet.routes.single()
        val connection = projection.connections.single()
        val forgedOccurrenceTrace = SpatialSourceTrace(
            projectionIds = occurrence.sourceTrace.projectionIds,
            geometryElementIds = listOf(GeometryElementId("geometry:forged")),
        )
        val invalidOccurrence = occurrence.copy(
            subjectId = StableSemanticIdentity("component:forged"),
            sourceTrace = forgedOccurrenceTrace,
        )
        val invalidRoute = route.copy(
            connectionId = StableSemanticIdentity("connection:forged"),
            sourceAnchorId = route.targetAnchorId,
            targetAnchorId = route.sourceAnchorId,
        )
        val invalidGrid = sheet.grid.copy(
            gridId = "grid:forged",
            rows = sheet.grid.rows + 1,
        )
        val invalid = SpatialDocument(
            listOf(
                sheet.copy(
                    grid = invalidGrid,
                    occurrences = listOf(invalidOccurrence) + sheet.occurrences.drop(1),
                    routes = listOf(invalidRoute),
                ),
            ),
        )

        val diagnostics = ProjectionSpatialCoverageValidator().validate(projection, invalid)
        val expectedSourceAnchor = SpatialAnchorId(
            sheet.sheetId,
            SpatialOccurrenceId(sheet.sheetId, requireNotNull(connection.source).occurrencePortId.occurrenceId.value),
            requireNotNull(connection.source).occurrencePortId.portId,
        )
        val expectedTargetAnchor = SpatialAnchorId(
            sheet.sheetId,
            SpatialOccurrenceId(sheet.sheetId, requireNotNull(connection.target).occurrencePortId.occurrenceId.value),
            requireNotNull(connection.target).occurrencePortId.portId,
        )

        assertEquals(
            listOf(
                SpatialDiagnostic(
                    subject = "Grid ${projectionGrid.gridId} on Sheet ${sheet.sheetId}",
                    problem = "publishes grid grid:forged with ${invalidGrid.rows} rows and ${invalidGrid.columns} columns " +
                        "instead of Projection grid ${projectionGrid.gridId} with ${projectionGrid.rows} rows " +
                        "and ${projectionGrid.columns} columns",
                    correction = "Publish the exact Projection-owned grid identity and dimensions on this Spatial Sheet.",
                    sourceTrace = invalidGrid.sourceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Occurrence ${occurrence.occurrenceId.projectionId} on Sheet ${sheet.sheetId}",
                    problem = "final Spatial geometry Source Trace does not equal canonical Projection provenance",
                    correction = "Rebuild the final Spatial geometry Source Trace from canonical Projection facts.",
                    sourceTrace = forgedOccurrenceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Occurrence ${occurrence.occurrenceId.projectionId} on Sheet ${sheet.sheetId}",
                    problem = "semantic subject ${invalidOccurrence.subjectId.value} does not equal Projection subject " +
                        projectionNode.semanticId.value,
                    correction = "Preserve the canonical Projection semantic subject on the Spatial Occurrence.",
                    sourceTrace = forgedOccurrenceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Route ${route.routeId.value}",
                    problem = "ordered endpoint Anchors do not equal Projection source ${expectedSourceAnchor.value} and " +
                        "target ${expectedTargetAnchor.value}",
                    correction = "Preserve Projection source and target occurrence-port order in the Spatial Route.",
                    sourceTrace = route.sourceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Route ${route.routeId.value}",
                    problem = "semantic Connection ${invalidRoute.connectionId.value} does not equal Projection subject " +
                        connection.semanticId.value,
                    correction = "Preserve the canonical Projection semantic Connection on the Spatial Route.",
                    sourceTrace = route.sourceTrace,
                ),
            ).sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem, SpatialDiagnostic::correction)),
            diagnostics,
        )
    }

    @Test
    fun `final compiler gate merges Projection coverage and independent Spatial diagnostics`() {
        val projection = projectionDocument()
        val compiled = assertIs<RealityTransformationResult.Success<SpatialDocument>>(
            ProjectionSpatialCompiler().transform(projection),
        ).output
        val sheet = compiled.sheets.single()
        val missing = sheet.occurrences.last()
        val invalid = SpatialDocument(
            listOf(sheet.copy(occurrences = sheet.occurrences.dropLast(1))),
        )

        val result = validateFinalSpatialDocument(projection, invalid)

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertContains(
            failure.diagnostics.map { diagnostic -> diagnostic.subject },
            "Occurrence ${missing.occurrenceId.projectionId} on Sheet ${sheet.sheetId}",
        )
        assertContains(
            failure.diagnostics.map { diagnostic -> diagnostic.problem },
            "has 0 final Spatial geometry facts",
        )
        assertTrue(failure.diagnostics.all { diagnostic ->
            diagnostic.subject != null && diagnostic.problem != null &&
                diagnostic.correction != null && diagnostic.sourceTrace != null
        })
    }

    @Test
    fun `final compiler gate rejects finite quality metrics that do not match final Spatial facts`() {
        val projection = projectionDocument()
        val compiled = assertIs<RealityTransformationResult.Success<SpatialDocument>>(
            ProjectionSpatialCompiler().transform(projection),
        ).output
        val sheet = compiled.sheets.single()
        val invalid = SpatialDocument(
            listOf(
                sheet.copy(
                    quality = sheet.quality.copy(
                        metrics = sheet.quality.metrics.copy(
                            occurrenceOverlapCount = sheet.quality.metrics.occurrenceOverlapCount + 1,
                        ),
                    ),
                ),
            ),
        )

        val result = validateFinalSpatialDocument(projection, invalid)

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        val diagnostic = failure.diagnostics.single()
        assertEquals("Quality snapshot on Sheet ${sheet.sheetId}", diagnostic.subject)
        assertEquals(
            "metrics do not equal exact values recomputed from final Spatial facts",
            diagnostic.problem,
        )
        assertEquals(
            "Recompute all quality metrics from this Sheet's Drawing Area, Occurrences, Constructs, Lanes, and Routes.",
            diagnostic.correction,
        )
        assertEquals(sheet.quality.sourceTrace, diagnostic.sourceTrace)
    }

    @Test
    fun `final compiler gate reports extreme union area without throwing`() {
        val projection = projectionDocument()
        val compiled = assertIs<RealityTransformationResult.Success<SpatialDocument>>(
            ProjectionSpatialCompiler().transform(projection),
        ).output
        val sheet = compiled.sheets.single()
        val template = sheet.occurrences.first()
        val origins = listOf(
            Int.MIN_VALUE to Int.MIN_VALUE,
            0 to Int.MIN_VALUE,
            Int.MIN_VALUE to 0,
            0 to 0,
        )
        val extremeOccurrences = origins.mapIndexed { index, (x, y) ->
            val projectionId = "projection/node/extreme:$index"
            template.copy(
                occurrenceId = SpatialOccurrenceId(sheet.sheetId, projectionId),
                subjectId = StableSemanticIdentity("component:extreme:$index"),
                rectangle = SpatialRect(x, y, Int.MAX_VALUE, Int.MAX_VALUE),
                sourceTrace = SpatialSourceTrace(
                    projectionIds = listOf(sheet.sheetId, projectionId),
                    geometryElementIds = listOf(GeometryElementId("origin:extreme:$index")),
                ),
            )
        }
        val invalid = SpatialDocument(listOf(sheet.copy(occurrences = extremeOccurrences)))

        val result = validateFinalSpatialDocument(projection, invalid)

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertContains(
            failure.diagnostics.mapNotNull { diagnostic -> diagnostic.problem },
            "metrics cannot be recomputed because final Spatial geometry exceeds the supported area range",
        )
        assertTrue(failure.diagnostics.all { diagnostic ->
            diagnostic.subject != null && diagnostic.problem != null &&
                diagnostic.correction != null && diagnostic.sourceTrace != null
        })
    }

    @Test
    fun `new spatial transformation names avoid stale architecture terms`() {
        val names = listOf(ProjectionSpatialCompiler::class.simpleName.orEmpty())
        val banned = listOf("M39", "V0", "V1", "Evidence", "ProfessionalControlDrawing", "Compatibility")

        banned.forEach { token ->
            assertFalse(
                names.any { name -> name.contains(token) },
                "Transformation names must not contain `$token`: $names",
            )
        }
    }

    private fun projectionDocument(): ProjectionDocument {
        val view = ViewDefinition(id = "engineering-projection", displayName = "Engineering Projection")
        val supplyNode = ProjectionNode(
            projectionId = ProjectionNodeId("projection/node/component:Supply"),
            semanticId = StableSemanticIdentity("component:Supply"),
            label = "Supply",
            originGeometryElementId = GeometryElementId("origin:Supply"),
        )
        val breakerNode = ProjectionNode(
            projectionId = ProjectionNodeId("projection/node/component:Q1"),
            semanticId = StableSemanticIdentity("component:Q1"),
            label = "Q1",
            originGeometryElementId = GeometryElementId("origin:Q1"),
        )
        val sourcePort = ProjectionOccurrencePort(
            ProjectionOccurrencePortId(supplyNode.projectionId, StableSemanticIdentity("port:Supply.L1")),
            GeometryElementId("origin:Supply.L1"),
        )
        val targetPort = ProjectionOccurrencePort(
            ProjectionOccurrencePortId(breakerNode.projectionId, StableSemanticIdentity("port:Q1.1")),
            GeometryElementId("origin:Q1.1"),
        )
        val connection = ProjectionConnection(
            projectionId = ProjectionConnectionId("projection/connection/connection:Supply.L1-to-Q1.1"),
            semanticId = StableSemanticIdentity("connection:Supply.L1-to-Q1.1"),
            originGeometryElementId = GeometryElementId("origin:connection"),
            source = ProjectionConnectionEndpoint(sourcePort.occurrencePortId),
            target = ProjectionConnectionEndpoint(targetPort.occurrencePortId),
        )
        val subjects = listOf(
            ProjectionSheetSubject(supplyNode.semanticId, nodeIds = listOf(supplyNode.projectionId)),
            ProjectionSheetSubject(breakerNode.semanticId, nodeIds = listOf(breakerNode.projectionId)),
            ProjectionSheetSubject(connection.semanticId, connectionIds = listOf(connection.projectionId)),
        )
        val sheetId = ProjectionSheetId("engineering-projection/sheet/01-main")
        return ProjectionDocument(
            view = view,
            nodes = listOf(supplyNode, breakerNode),
            connections = listOf(connection),
            occurrencePorts = listOf(sourcePort, targetPort),
            sheets = listOf(
                ProjectionSheet(
                    sheetId = sheetId,
                    displayName = "Engineering Projection Main",
                    order = 0,
                    subjects = subjects,
                    grid = ProjectionSheetGrid(gridId = "grid:main", rows = 3, columns = 4),
                    publication = ProjectionSheetPublication.fromProjectionState(
                        sheetId = sheetId,
                        displayName = "Engineering Projection Main",
                        order = 0,
                        subjects = subjects,
                    ),
                ),
            ),
        )
    }

    private fun projectionDocumentWithGrouping(): ProjectionDocument {
        val projection = projectionDocument()
        val sheet = projection.sheets.single()
        return projection.copy(
            sheets = listOf(
                sheet.copy(
                    regions = listOf(
                        ProjectionRegion(
                            regionId = "region:main",
                            name = "Main",
                            occurrenceNames = listOf("Supply", "Q1"),
                            originGeometryElementId = GeometryElementId("origin:region:main"),
                        ),
                    ),
                    constructs = listOf(
                        ProjectionSheetConstruct(
                            constructId = ProjectionConstructId("construct:main"),
                            kind = "control-chain",
                            name = "Main",
                            memberNames = listOf("Supply", "Q1"),
                            originGeometryElementId = GeometryElementId("origin:construct:main"),
                        ),
                    ),
                ),
            ),
        )
    }

    private fun occurrenceCoverageTrace(
        sheet: ProjectionSheet,
        node: ProjectionNode,
    ): SpatialSourceTrace = SpatialSourceTrace(
        projectionIds = listOf(sheet.sheetId.value, node.projectionId.value),
        geometryElementIds = listOf(sheet.originGeometryElementId, node.originGeometryElementId),
    )

    private fun endpointCoverageTrace(
        projection: ProjectionDocument,
        sheet: ProjectionSheet,
        node: ProjectionNode,
        endpoint: ProjectionOccurrencePortId,
        connection: ProjectionConnection,
    ): SpatialSourceTrace = SpatialSourceTrace(
        projectionIds = listOf(
            sheet.sheetId.value,
            node.projectionId.value,
            endpoint.portId.value,
            connection.projectionId.value,
        ),
        geometryElementIds = listOf(
            sheet.originGeometryElementId,
            node.originGeometryElementId,
            projection.occurrencePorts.single { port -> port.occurrencePortId == endpoint }.originGeometryElementId,
            connection.originGeometryElementId,
        ).distinctBy { geometryId -> geometryId.value }.sortedBy { geometryId -> geometryId.value },
    )

    private fun routeCoverageTrace(
        projection: ProjectionDocument,
        sheet: ProjectionSheet,
        source: ProjectionNode,
        target: ProjectionNode,
        connection: ProjectionConnection,
    ): SpatialSourceTrace {
        val sourcePort = requireNotNull(connection.source).occurrencePortId
        val targetPort = requireNotNull(connection.target).occurrencePortId
        return SpatialSourceTrace(
            projectionIds = listOf(
                sheet.sheetId.value,
                connection.projectionId.value,
                source.projectionId.value,
                sourcePort.portId.value,
                target.projectionId.value,
                targetPort.portId.value,
            ),
            geometryElementIds = listOf(
                sheet.originGeometryElementId,
                connection.originGeometryElementId,
                source.originGeometryElementId,
                projection.occurrencePorts.single { port -> port.occurrencePortId == sourcePort }.originGeometryElementId,
                target.originGeometryElementId,
                projection.occurrencePorts.single { port -> port.occurrencePortId == targetPort }.originGeometryElementId,
            ).distinctBy { geometryId -> geometryId.value }.sortedBy { geometryId -> geometryId.value },
        )
    }
}
