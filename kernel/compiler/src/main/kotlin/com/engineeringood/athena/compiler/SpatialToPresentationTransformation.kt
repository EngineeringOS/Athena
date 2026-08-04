package com.engineeringood.athena.compiler

import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.presentation.PresentationAnchorAlias
import com.engineeringood.athena.presentation.PresentationAnchorDefinition
import com.engineeringood.athena.presentation.PresentationBounds
import com.engineeringood.athena.presentation.PresentationConnector
import com.engineeringood.athena.presentation.PresentationConnectorEndpoint
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationDrawingAuthorities
import com.engineeringood.athena.presentation.PresentationDrawingBounds
import com.engineeringood.athena.presentation.PresentationDrawingComposition
import com.engineeringood.athena.presentation.PresentationDrawingStructureFact
import com.engineeringood.athena.presentation.PresentationDrawingTitle
import com.engineeringood.athena.presentation.PresentationLayer
import com.engineeringood.athena.presentation.PresentationOccurrence
import com.engineeringood.athena.presentation.PresentationOccurrenceId
import com.engineeringood.athena.presentation.PresentationPoint
import com.engineeringood.athena.presentation.PresentationPrimitiveDefinition
import com.engineeringood.athena.presentation.PresentationPrimitiveId
import com.engineeringood.athena.presentation.PresentationPrimitiveOccurrenceReference
import com.engineeringood.athena.presentation.PresentationPrimitivePack
import com.engineeringood.athena.presentation.PresentationReality
import com.engineeringood.athena.presentation.PresentationStrokeRectangle
import com.engineeringood.athena.representation.RepresentationAnchorId
import com.engineeringood.athena.representation.RepresentationOccurrenceId
import com.engineeringood.athena.representation.RepresentationPortAnchorBindingId
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialAnchorId
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialReality
import com.engineeringood.athena.spatial.SpatialRoute
import com.engineeringood.athena.spatial.SpatialSheet
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SpatialToPresentationTransformation(
    private val view: ViewDefinition = ViewDefinition(id = "spatial-presentation", displayName = "Spatial Presentation"),
    private val connectionPaintCompiler: ConnectionPaintCompiler = ConnectionPaintCompiler(),
    private val presentationPaintCompiler: PresentationPaintCompiler = PresentationPaintCompiler(),
) : RealityTransformation<SpatialSheet, PresentationDocument> {
    override fun transform(input: SpatialSheet): RealityTransformationResult<PresentationDocument> {
        val spatialValidation = SpatialReality.validate(SpatialDocument(listOf(input)))
        val spatialDiagnostics = (spatialValidation.diagnostics + exactSpatialQualityDiagnostics(input))
            .distinct()
            .sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem, SpatialDiagnostic::correction))
        if (spatialDiagnostics.isNotEmpty()) {
            return spatialDiagnostics.toSpatialTransformationFailure()
        }
        val compositionDiagnostics = presentationCompositionDiagnostics(input)
        if (compositionDiagnostics.isNotEmpty()) {
            return compositionDiagnostics.toSpatialTransformationFailure()
        }

        val occurrences = input.occurrences.map { occurrence -> occurrence.toPresentationOccurrence() }
        val connectorPaints = input.routes.map { route ->
            val routePoints = route.points.map { point -> point.toPresentationPoint() }
            route to connectionPaintCompiler.compile(
                route = route,
                routePoints = routePoints,
                connectorId = PresentationOccurrenceId("paint:${route.routeId.value}"),
            )
        }
        val laneById = input.lanes.associateBy { lane -> lane.laneId }
        val connectors = connectorPaints.map { (route, paint) ->
            route.toPresentationConnector(
                paint = paint,
                laneRouteIds = laneById.getValue(route.laneId).routeIds.map { routeId -> routeId.value },
            )
        }
        val documentWithoutPaintPlan = PresentationDocument(
            view = view,
            canvasWidth = input.extent.width,
            canvasHeight = input.extent.height,
            primitivePacks = listOf(deviceShapePack()),
            compositePacks = emptyList(),
            occurrences = occurrences,
            connectors = connectors,
            connectionMarkers = connectorPaints.flatMap { (_, paint) -> paint.markers },
            drawingComposition = drawingComposition(
                sheet = input,
            ),
        )
        val document = documentWithoutPaintPlan.copy(
            paintPlan = presentationPaintCompiler.compile(documentWithoutPaintPlan),
        )
        val presentationValidation = PresentationReality.validate(document)
        if (!presentationValidation.isValid) {
            return presentationValidation.issues.toTransformationFailure()
        }
        return RealityTransformationResult.Success(document)
    }

    private fun SpatialOccurrenceGeometry.toPresentationOccurrence(): PresentationOccurrence =
        PresentationOccurrence(
            occurrenceId = PresentationOccurrenceId(
                "paint:occurrence:sheet=${occurrenceId.sheetId.encodedIdentityPart()}:" +
                    "projection=${occurrenceId.projectionId.encodedIdentityPart()}",
            ),
            semanticId = subjectId,
            reference = PresentationPrimitiveOccurrenceReference(DEVICE_BOX_ID),
            bounds = PresentationBounds(
                x = rectangle.x,
                y = rectangle.y,
                width = rectangle.width,
                height = rectangle.height,
            ),
            layer = PresentationLayer.DEVICE,
            displayLabel = subjectId.value.substringAfterLast(':'),
            sourceProjectionIds = sourceTrace.projectionIds,
        )

    private fun SpatialRoute.toPresentationConnector(
        paint: ConnectionPaint,
        laneRouteIds: List<String>,
    ): PresentationConnector {
        val routePoints = points.map { point -> point.toPresentationPoint() }
        return PresentationConnector(
            occurrenceId = PresentationOccurrenceId("paint:${routeId.value}"),
            semanticId = connectionId,
            primitiveId = CONDUCTOR_ID,
            routePoints = routePoints,
            line = paint.line,
            routeId = routeId.value,
            bundleId = laneId.value,
            laneId = laneId.value,
            laneRouteIds = laneRouteIds,
            selectedChannelIds = emptyList(),
            labels = paint.labels,
            quality = "spatial-owned",
            sourceEndpoint = routeEndpoint(
                anchorId = sourceAnchorId,
                point = routePoints.first(),
            ),
            targetEndpoint = routeEndpoint(
                anchorId = targetAnchorId,
                point = routePoints.last(),
            ),
            markerIds = paint.markerIds,
            sourceProjectionIds = sourceTrace.projectionIds,
            sourceSpan = GENERATED_SPAN,
        )
    }

    private fun routeEndpoint(
        anchorId: SpatialAnchorId,
        point: PresentationPoint,
    ): PresentationConnectorEndpoint =
        PresentationConnectorEndpoint(
            portSemanticId = anchorId.portId,
            bindingId = RepresentationPortAnchorBindingId("binding:${anchorId.value}"),
            occurrenceId = RepresentationOccurrenceId(anchorId.occurrenceId.projectionId),
            anchorId = RepresentationAnchorId(anchorId.value),
            point = point,
            sourceProvenance = listOf(
                anchorId.sheetId,
                anchorId.occurrenceId.projectionId,
                anchorId.portId.value,
            ),
        )

    private fun drawingComposition(
        sheet: SpatialSheet,
    ): PresentationDrawingComposition {
        val sheetBounds = sheet.extent.toPresentationDrawingBounds()
        val drawingAreaBounds = sheet.drawingArea.toPresentationDrawingBounds()
        val titleBlockY = ProjectionSpatialLayout.TITLE_BLOCK_START_Y
        return PresentationDrawingComposition(
            sheetId = sheet.sheetId,
            policyId = "presentation:default",
            contentBounds = drawingAreaBounds,
            frameBounds = sheetBounds,
            drawingAreaBounds = drawingAreaBounds,
            titleBlockBounds = PresentationDrawingBounds(
                sheet.extent.x,
                titleBlockY,
                sheet.extent.width,
                sheet.extent.bottom - titleBlockY,
            ),
            sheetBounds = sheetBounds,
            frameId = "frame:${view.id}:${sheet.sheetId.encodedIdentityPart()}",
            frameStyle = "plain",
            title = PresentationDrawingTitle(
                sheetTitle = view.displayName,
                sheetFamily = "Spatial",
                sheetNumber = sheet.sheetId.substringAfter("/sheet/", "01").substringBefore('-'),
                revisionCode = "A",
                revisionNote = "Generated",
                pageFormat = "A3",
                orientation = "landscape",
            ),
            coordinateZones = emptyList(),
            structureSubjects = emptyList(),
            structureFacts = emptyList(),
            referencePlacements = emptyList(),
            authorities = PresentationDrawingAuthorities(
                contentBounds = "presentation compiler",
                bounds = "presentation compiler",
                projection = "spatial compiler",
                representation = "presentation compiler",
                structure = "presentation compiler",
                policy = "presentation compiler",
            ),
        )
    }

    private fun SpatialPoint.toPresentationPoint(): PresentationPoint =
        PresentationPoint(x = x, y = y)

    private fun com.engineeringood.athena.spatial.SpatialRect.toPresentationDrawingBounds(): PresentationDrawingBounds =
        PresentationDrawingBounds(x = x, y = y, width = width, height = height)

    private fun deviceShapePack(): PresentationPrimitivePack =
        PresentationPrimitivePack(
            packId = com.engineeringood.athena.presentation.PresentationPackId("pack:spatial-presentation"),
            displayName = "Spatial Presentation",
            primitives = listOf(
                PresentationPrimitiveDefinition(
                    primitiveId = DEVICE_BOX_ID,
                    displayName = "Device Box",
                    viewBoxWidth = 80,
                    viewBoxHeight = 40,
                    commands = listOf(
                        PresentationStrokeRectangle(
                            bounds = PresentationBounds(0, 0, 80, 40),
                            strokeTokenKey = "device-stroke",
                            strokeWidthTokenKey = "device-stroke-width",
                        ),
                    ),
                    anchors = listOf(
                        PresentationAnchorDefinition(PresentationAnchorAlias("left"), PresentationPoint(0, 20)),
                        PresentationAnchorDefinition(PresentationAnchorAlias("right"), PresentationPoint(80, 20)),
                    ),
                    tokenDefaults = mapOf(
                        "device-stroke" to "device",
                        "device-stroke-width" to "1",
                    ),
                ),
            ),
        )

    companion object {
        private val DEVICE_BOX_ID = PresentationPrimitiveId("spatial-shape:device-box")
        private val CONDUCTOR_ID = PresentationPrimitiveId("spatial-shape:connector")
        private val GENERATED_SPAN = LayoutSourceSpan(
            sourceUnitId = "spatial-reality",
            startLine = 1,
            startColumn = 1,
            endLine = 1,
            endColumn = 1,
        )
    }
}

private fun presentationCompositionDiagnostics(sheet: SpatialSheet): List<SpatialDiagnostic> = buildList {
    if (sheet.extent != ProjectionSpatialLayout.SHEET_EXTENT) {
        add(
            SpatialDiagnostic(
                subject = "Sheet ${sheet.sheetId}",
                problem = "extent ${sheet.extent.compositionText()} does not equal fixed Sheet extent " +
                    ProjectionSpatialLayout.SHEET_EXTENT.compositionText(),
                correction = "Compile the M41 fixed Sheet extent before creating Presentation canvas facts.",
                sourceTrace = sheet.sourceTrace,
            ),
        )
    }
    if (sheet.drawingArea != ProjectionSpatialLayout.DRAWING_AREA) {
        add(
            SpatialDiagnostic(
                subject = "Sheet ${sheet.sheetId}",
                problem = "Drawing Area ${sheet.drawingArea.compositionText()} does not equal fixed Drawing Area " +
                    ProjectionSpatialLayout.DRAWING_AREA.compositionText(),
                correction = "Compile the M41 fixed Drawing Area before creating Presentation content bounds.",
                sourceTrace = sheet.sourceTrace,
            ),
        )
    }
    val titleBlockY = ProjectionSpatialLayout.TITLE_BLOCK_START_Y
    if (titleBlockY !in sheet.extent.y until sheet.extent.bottom) {
        add(
            SpatialDiagnostic(
                subject = "Sheet ${sheet.sheetId}",
                problem = "fixed title block start y=$titleBlockY is outside Sheet extent ${sheet.extent.compositionText()}",
                correction = "Keep the complete fixed title block inside the Presentation canvas.",
                sourceTrace = sheet.sourceTrace,
            ),
        )
    }
    if (sheet.drawingArea.bottom > titleBlockY) {
        add(
            SpatialDiagnostic(
                subject = "Sheet ${sheet.sheetId}",
                problem = "Drawing Area bottom ${sheet.drawingArea.bottom} crosses fixed title block start y=$titleBlockY",
                correction = "End the Drawing Area at or above the fixed title block boundary.",
                sourceTrace = sheet.sourceTrace,
            ),
        )
    }
}.sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem, SpatialDiagnostic::correction))

private fun com.engineeringood.athena.spatial.SpatialRect.compositionText(): String = "($x,$y,$width,$height)"

internal fun transformSpatialSheetsToPresentation(
    document: SpatialDocument,
    view: ViewDefinition,
): RealityTransformationResult<List<PresentationDocument>> {
    val spatialValidation = SpatialReality.validate(document)
    if (!spatialValidation.isValid) {
        return spatialValidation.diagnostics.toSpatialTransformationFailure()
    }
    val compositionDiagnostics = document.sheets.flatMap(::presentationCompositionDiagnostics)
        .distinct()
        .sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem, SpatialDiagnostic::correction))
    if (compositionDiagnostics.isNotEmpty()) {
        return compositionDiagnostics.toSpatialTransformationFailure()
    }
    val results = document.sheets.map { sheet ->
        SpatialToPresentationTransformation(view = view).transform(sheet)
    }
    val diagnostics = results.filterIsInstance<RealityTransformationResult.Failure>()
        .flatMap(RealityTransformationResult.Failure::diagnostics)
    if (diagnostics.isNotEmpty()) {
        return RealityTransformationResult.Failure(diagnostics)
    }
    return RealityTransformationResult.Success(
        results.filterIsInstance<RealityTransformationResult.Success<PresentationDocument>>()
            .map(RealityTransformationResult.Success<PresentationDocument>::output),
    )
}

private fun String.encodedIdentityPart(): String = URLEncoder.encode(this, StandardCharsets.UTF_8)
