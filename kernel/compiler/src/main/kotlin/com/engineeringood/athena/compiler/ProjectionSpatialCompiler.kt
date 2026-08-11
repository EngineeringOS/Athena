package com.engineeringood.athena.compiler

import com.engineeringood.athena.connection.ConnectionDocument
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionReality
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialGridReferenceSubject
import com.engineeringood.athena.spatial.SpatialQualitySnapshot
import com.engineeringood.athena.spatial.SpatialQualitySnapshotId
import com.engineeringood.athena.spatial.SpatialReality
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.ConnectionRoutePlan
import com.engineeringood.athena.spatial.SpatialSheet
import com.engineeringood.athena.spatial.SpatialSourceTrace
import com.engineeringood.athena.spatial.ConnectionAnnotationSelection
import com.engineeringood.athena.spatial.ConnectionAnnotationPlanning
import com.engineeringood.athena.spatial.ConnectionRouteTopologyPlan
import com.engineeringood.athena.layout.SheetPlacementConstraint

class ProjectionSpatialCompiler(
    private val layout: ProjectionSpatialLayout = ProjectionSpatialLayout(),
    private val geometryCompiler: SpatialGeometryCompiler = SpatialGeometryCompiler(),
    private val anchorCompiler: SpatialAnchorCompiler = SpatialAnchorCompiler(),
    private val qualityCompiler: SpatialQualityCompiler = SpatialQualityCompiler(),
) : RealityTransformation<ProjectionDocument, SpatialDocument> {
    private val gridCompiler = SpatialGridCompiler()

    override fun transform(input: ProjectionDocument): RealityTransformationResult<SpatialDocument> =
        transform(input, emptyList(), emptyMap(), null, emptyList())

    internal fun transform(
        input: ProjectionDocument,
        connectionIr: ConnectionDocument?,
    ): RealityTransformationResult<SpatialDocument> = transform(input, emptyList(), emptyMap(), connectionIr, emptyList())

    /**
     * Compiles Spatial Reality with authored Sheet placement constraints. Constraints must enter
     * before geometry, anchors, grid references, and routes are derived; patching only occurrence
     * rectangles would leave stale downstream facts.
     */
    internal fun transform(
        input: ProjectionDocument,
        placementConstraints: List<SheetPlacementConstraint>,
        pageGeometries: Map<String, SpatialPageGeometryProfile> = emptyMap(),
        connectionIr: ConnectionDocument? = null,
        annotationSelections: List<ConnectionAnnotationSelection> = emptyList(),
    ): RealityTransformationResult<SpatialDocument> {
        val projectionValidation = ProjectionReality.validate(input)
        if (!projectionValidation.isValid) {
            return projectionValidation.issues.toTransformationFailure()
        }

        val layoutResult = if (placementConstraints.isEmpty()) {
            layout.place(input)
        } else {
            GridAlignedSpatialLayoutCompiler(layout).compile(input, placementConstraints, pageGeometries)
        }
        if (layoutResult.diagnostics.isNotEmpty()) {
            return layoutResult.diagnostics.toSpatialTransformationFailure()
        }
        val geometryResult = geometryCompiler.compile(input, layoutResult.occurrences, pageGeometries)
        if (geometryResult.diagnostics.isNotEmpty()) {
            return geometryResult.diagnostics.toSpatialTransformationFailure()
        }
        val gridResult = gridCompiler.compile(
            sheets = input.sheets.map { sheet ->
                SpatialGridSheetInput(
                    sheetId = sheet.sheetId.value,
                    order = sheet.order,
                    drawingArea = pageGeometries[sheet.sheetId.value]?.drawingArea
                        ?: ProjectionSpatialLayout.DRAWING_AREA,
                    grid = sheet.grid,
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf(sheet.sheetId.value) +
                            listOfNotNull(sheet.grid?.gridId?.takeIf(String::isNotBlank)),
                        geometryElementIds = listOf(sheet.originGeometryElementId),
                    ),
                )
            },
            occurrences = layoutResult.occurrences,
            constructs = geometryResult.constructs,
        )
        if (gridResult.diagnostics.isNotEmpty()) {
            return gridResult.diagnostics.toSpatialTransformationFailure()
        }
        val anchorResult = anchorCompiler.compile(input, layoutResult.occurrences)
        if (anchorResult.diagnostics.isNotEmpty()) {
            return anchorResult.diagnostics.toSpatialTransformationFailure()
        }
        val routeResult = ConnectionRoutePlanner().compile(
            projection = input,
            drawingAreas = pageGeometries.mapValues { (_, profile) -> profile.drawingArea },
            occurrences = layoutResult.occurrences,
            anchors = anchorResult.anchorPositions,
            connectionIr = connectionIr,
        )
        if (routeResult.diagnostics.isNotEmpty()) {
            return routeResult.diagnostics.toSpatialTransformationFailure()
        }
        val annotationPlans = input.sheets.associate { sheet ->
            val sheetId = sheet.sheetId.value
            val result = ConnectionAnnotationPlanner().plan(
                sheetId = sheetId,
                drawingArea = pageGeometries[sheetId]?.drawingArea ?: ProjectionSpatialLayout.DRAWING_AREA,
                connectionIr = connectionIr,
                selections = annotationSelections.filter { it.sheetId == sheetId },
                routes = routeResult.routes.filter { it.sheetId == sheetId },
                occurrences = layoutResult.occurrences.filter { it.sheetId == sheetId },
                topology = routeResult.topology.forSheet(sheetId),
            )
            sheetId to result
        }
        val annotationDiagnostics = annotationPlans.values.flatMap { result ->
            when (result) {
                is ConnectionAnnotationPlanning.Success -> emptyList()
                is ConnectionAnnotationPlanning.Failure -> result.diagnostics
            }
        }
        if (annotationDiagnostics.isNotEmpty()) {
            return annotationDiagnostics.toSpatialTransformationFailure()
        }
        val output = SpatialDocument(
            input.sheets
                .sortedWith(compareBy({ sheet -> sheet.order }, { sheet -> sheet.sheetId.value }))
                .map { sheet ->
                    val sheetId = sheet.sheetId.value
                    val grid = gridResult.grids.single { candidate -> candidate.sheetId == sheetId }
                    val occurrences = layoutResult.occurrences.filter { fact -> fact.sheetId == sheetId }
                    val regions = geometryResult.regions.filter { fact -> fact.sheetId == sheetId }
                    val constructs = geometryResult.constructs.filter { fact -> fact.sheetId == sheetId }
                    val alignments = geometryResult.alignments.filter { fact -> fact.sheetId == sheetId }
                    val anchors = anchorResult.anchorPositions.filter { fact -> fact.sheetId == sheetId }
                    val lanes = routeResult.lanes.filter { lane -> lane.sheetId == sheetId }
                    val routes = routeResult.routes.filter { route -> route.sheetId == sheetId }
                    val gridReferences = gridResult.references.filter { fact -> fact.sheetId == sheetId }
                    val pageGeometry = pageGeometries[sheetId]
                    val extent = pageGeometry?.extent ?: ProjectionSpatialLayout.SHEET_EXTENT
                    val drawingArea = pageGeometry?.drawingArea ?: ProjectionSpatialLayout.DRAWING_AREA
                    val qualityTrace = qualityTrace(
                        sheetId = sheetId,
                        sheetTrace = SpatialSourceTrace(
                            projectionIds = listOf(sheetId),
                            geometryElementIds = listOf(sheet.originGeometryElementId),
                        ),
                        traces = listOf(grid.sourceTrace) +
                            occurrences.map { fact -> fact.sourceTrace } +
                            regions.map { fact -> fact.sourceTrace } +
                            constructs.map { fact -> fact.sourceTrace } +
                            alignments.map { fact -> fact.sourceTrace } +
                            anchors.map { fact -> fact.sourceTrace } +
                            routes.map { fact -> fact.sourceTrace } +
                            gridReferences.map { fact -> fact.sourceTrace } +
                            (annotationPlans.getValue(sheetId) as? ConnectionAnnotationPlanning.Success)
                                ?.plan?.annotations?.map { fact -> fact.sourceTrace }.orEmpty(),
                    )
                    canonicalSpatialSheet(SpatialSheet(
                        sheetId = sheetId,
                        extent = extent,
                        drawingArea = drawingArea,
                        grid = grid,
                        occurrences = occurrences,
                        regions = regions,
                        constructs = constructs,
                        alignments = alignments,
                        anchors = anchors,
                        lanes = lanes,
                        routes = routes,
                        gridReferences = gridReferences,
                        quality = SpatialQualitySnapshot(
                            qualitySnapshotId = SpatialQualitySnapshotId(sheetId),
                            sheetId = sheetId,
                            metrics = qualityCompiler.measure(
                                drawingArea = drawingArea,
                                occurrences = occurrences,
                                constructs = constructs,
                                lanes = lanes,
                                routes = routes,
                            ),
                            sourceTrace = qualityTrace,
                        ),
                        sourceTrace = SpatialSourceTrace(
                            projectionIds = listOf(sheetId),
                            geometryElementIds = listOf(sheet.originGeometryElementId),
                        ),
                        connectionTopology = routeResult.topology.forSheet(sheetId),
                        annotations = (annotationPlans.getValue(sheetId) as ConnectionAnnotationPlanning.Success).plan.annotations,
                    ))
                },
        )
        return validateFinalSpatialDocument(input, output)
    }
}

internal fun validateFinalSpatialDocument(
    projection: ProjectionDocument,
    spatial: SpatialDocument,
): RealityTransformationResult<SpatialDocument> {
    val diagnostics = (
        ProjectionSpatialCoverageValidator().validate(projection, spatial) +
            SpatialReality.validate(spatial).diagnostics +
            spatial.sheets.flatMap(::exactSpatialQualityDiagnostics)
        ).distinct().sortedWith(
        compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem, SpatialDiagnostic::correction),
    )
    return if (diagnostics.isEmpty()) {
        RealityTransformationResult.Success(spatial)
    } else {
        diagnostics.toSpatialTransformationFailure()
    }
}

internal fun canonicalSpatialSheet(sheet: SpatialSheet): SpatialSheet = sheet.copy(
    anchors = sheet.anchors.sortedWith(
        compareBy(
            { anchor -> anchor.sheetId },
            { anchor -> anchor.subject.occurrenceId.projectionId },
            { anchor -> anchor.subject.portId.value },
        ),
    ),
    lanes = sheet.lanes.sortedBy { lane -> lane.laneId.value },
    routes = sheet.routes.sortedBy { route -> route.routeId.value },
    gridReferences = sheet.gridReferences.sortedWith(
        compareBy(
            { reference ->
                when (reference.subject) {
                    is SpatialGridReferenceSubject.Occurrence -> 0
                    is SpatialGridReferenceSubject.Construct -> 1
                }
            },
            { reference -> reference.subject.projectionId },
        ),
    ),
    annotations = sheet.annotations.sortedBy { annotation -> annotation.id.value },
)

private fun qualityTrace(
    sheetId: String,
    sheetTrace: SpatialSourceTrace,
    traces: List<SpatialSourceTrace>,
): SpatialSourceTrace = SpatialSourceTrace(
    projectionIds = listOf(sheetId) +
        (sheetTrace.projectionIds + traces.flatMap(SpatialSourceTrace::projectionIds))
            .filterNot { projectionId -> projectionId == sheetId }
            .distinct()
            .sorted(),
    geometryElementIds = (sheetTrace.geometryElementIds + traces.flatMap(SpatialSourceTrace::geometryElementIds))
        .distinct()
        .sortedBy { geometryId -> geometryId.value },
)

private fun ConnectionRouteTopologyPlan.forSheet(sheetId: String): ConnectionRouteTopologyPlan = ConnectionRouteTopologyPlan(
    junctions = junctions.filter { it.id.sheetId == sheetId },
    crossings = crossings.filter { it.id.sheetId == sheetId },
    sharedSegments = sharedSegments.filter { it.id.sheetId == sheetId },
    interruptions = interruptions.filter { it.id.sheetId == sheetId },
)
