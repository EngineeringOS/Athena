package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionReality
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialGridReferenceSubject
import com.engineeringood.athena.spatial.SpatialQualitySnapshot
import com.engineeringood.athena.spatial.SpatialQualitySnapshotId
import com.engineeringood.athena.spatial.SpatialReality
import com.engineeringood.athena.spatial.SpatialSheet
import com.engineeringood.athena.spatial.SpatialSourceTrace

class ProjectionSpatialCompiler(
    private val layout: ProjectionSpatialLayout = ProjectionSpatialLayout(),
    private val geometryCompiler: SpatialGeometryCompiler = SpatialGeometryCompiler(),
    private val anchorCompiler: SpatialAnchorCompiler = SpatialAnchorCompiler(),
    private val routeCompiler: SpatialRouteCompiler = SpatialRouteCompiler(),
    private val qualityCompiler: SpatialQualityCompiler = SpatialQualityCompiler(),
) : RealityTransformation<ProjectionDocument, SpatialDocument> {
    private val gridCompiler = SpatialGridCompiler()

    override fun transform(input: ProjectionDocument): RealityTransformationResult<SpatialDocument> {
        val projectionValidation = ProjectionReality.validate(input)
        if (!projectionValidation.isValid) {
            return projectionValidation.issues.toTransformationFailure()
        }

        val layoutResult = layout.place(input)
        if (layoutResult.diagnostics.isNotEmpty()) {
            return layoutResult.diagnostics.toSpatialTransformationFailure()
        }
        val geometryResult = geometryCompiler.compile(input, layoutResult.occurrences)
        if (geometryResult.diagnostics.isNotEmpty()) {
            return geometryResult.diagnostics.toSpatialTransformationFailure()
        }
        val gridResult = gridCompiler.compile(
            sheets = input.sheets.map { sheet ->
                SpatialGridSheetInput(
                    sheetId = sheet.sheetId.value,
                    order = sheet.order,
                    drawingArea = ProjectionSpatialLayout.DRAWING_AREA,
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
        val routeResult = routeCompiler.compile(
            projection = input,
            sheets = input.sheets.map { sheet ->
                SpatialRoutingSheetInput(sheet.sheetId.value, ProjectionSpatialLayout.DRAWING_AREA)
            },
            occurrences = layoutResult.occurrences,
            anchors = anchorResult.anchorPositions,
        )
        if (routeResult.diagnostics.isNotEmpty()) {
            return routeResult.diagnostics.toSpatialTransformationFailure()
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
                    val lanes = routeResult.lanes.filter { fact -> fact.sheetId == sheetId }
                    val routes = routeResult.routes.filter { fact -> fact.sheetId == sheetId }
                    val gridReferences = gridResult.references.filter { fact -> fact.sheetId == sheetId }
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
                            gridReferences.map { fact -> fact.sourceTrace },
                    )
                    canonicalSpatialSheet(SpatialSheet(
                        sheetId = sheetId,
                        extent = ProjectionSpatialLayout.SHEET_EXTENT,
                        drawingArea = ProjectionSpatialLayout.DRAWING_AREA,
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
                                drawingArea = ProjectionSpatialLayout.DRAWING_AREA,
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
