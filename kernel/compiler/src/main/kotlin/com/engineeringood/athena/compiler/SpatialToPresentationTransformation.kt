package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.StableSemanticIdentity
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
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialReality
import com.engineeringood.athena.spatial.SpatialRoute
import kotlin.math.roundToInt

class SpatialToPresentationTransformation(
    private val view: ViewDefinition = ViewDefinition(id = "spatial-presentation", displayName = "Spatial Presentation"),
    private val connectionPaintCompiler: ConnectionPaintCompiler = ConnectionPaintCompiler(),
    private val presentationPaintCompiler: PresentationPaintCompiler = PresentationPaintCompiler(),
) : RealityTransformation<SpatialDocument, PresentationDocument> {
    override fun transform(input: SpatialDocument): RealityTransformationResult<PresentationDocument> {
        val spatialValidation = SpatialReality.validate(input)
        if (!spatialValidation.isValid) {
            return spatialValidation.issues.toTransformationFailure()
        }

        val occurrences = input.occurrences.map { occurrence -> occurrence.toPresentationOccurrence() }
        val connectorPaints = input.routes.map { route ->
            val routePoints = route.points.map { point -> point.toPresentationPoint() }
            route to connectionPaintCompiler.compile(
                route = route,
                routePoints = routePoints,
                connectorId = PresentationOccurrenceId("paint:${route.routeId}"),
            )
        }
        val connectors = connectorPaints.map { (route, paint) -> route.toPresentationConnector(paint) }
        val documentWithoutPaintPlan = PresentationDocument(
            view = view,
            canvasWidth = drawingWidth(input.occurrences),
            canvasHeight = drawingHeight(input.occurrences),
            primitivePacks = listOf(deviceShapePack()),
            compositePacks = emptyList(),
            occurrences = occurrences,
            connectors = connectors,
            connectionMarkers = connectorPaints.flatMap { (_, paint) -> paint.markers },
            drawingComposition = drawingComposition(
                occurrences = occurrences,
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
            occurrenceId = PresentationOccurrenceId("paint:occurrence:${occurrenceId.projectionId}"),
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

    private fun SpatialRoute.toPresentationConnector(paint: ConnectionPaint): PresentationConnector {
        val routePoints = points.map { point -> point.toPresentationPoint() }
        return PresentationConnector(
            occurrenceId = PresentationOccurrenceId("paint:$routeId"),
            semanticId = connectionId,
            primitiveId = CONDUCTOR_ID,
            routePoints = routePoints,
            line = paint.line,
            routeId = routeId,
            bundleId = laneId,
            laneId = laneId,
            laneRouteIds = listOf(routeId),
            selectedChannelIds = emptyList(),
            labels = paint.labels,
            quality = "spatial-owned",
            sourceEndpoint = routeEndpoint(
                routeId = routeId,
                connectionId = connectionId,
                suffix = "source",
                point = routePoints.first(),
            ),
            targetEndpoint = routeEndpoint(
                routeId = routeId,
                connectionId = connectionId,
                suffix = "target",
                point = routePoints.last(),
            ),
            markerIds = paint.markerIds,
            sourceProjectionIds = listOf(routeId, connectionId.value),
            sourceSpan = GENERATED_SPAN,
        )
    }

    private fun routeEndpoint(
        routeId: String,
        connectionId: StableSemanticIdentity,
        suffix: String,
        point: PresentationPoint,
    ): PresentationConnectorEndpoint =
        PresentationConnectorEndpoint(
            portSemanticId = StableSemanticIdentity("${connectionId.value}:$suffix"),
            bindingId = RepresentationPortAnchorBindingId("binding:$routeId:$suffix"),
            occurrenceId = RepresentationOccurrenceId("occurrence:$routeId:$suffix"),
            anchorId = RepresentationAnchorId("anchor:$routeId:$suffix"),
            point = point,
            sourceProvenance = listOf(routeId),
        )

    private fun drawingComposition(
        occurrences: List<PresentationOccurrence>,
    ): PresentationDrawingComposition {
        val sheet = PresentationDrawingBounds(0, 0, 1200, 800)
        return PresentationDrawingComposition(
            sheetId = "spatial-presentation/sheet/01",
            policyId = "presentation:default",
            contentBounds = sheet,
            frameBounds = sheet,
            drawingAreaBounds = sheet,
            titleBlockBounds = PresentationDrawingBounds(0, 740, 1200, 60),
            sheetBounds = sheet,
            frameId = "frame:${view.id}",
            frameStyle = "plain",
            title = PresentationDrawingTitle(
                sheetTitle = view.displayName,
                sheetFamily = "M39",
                sheetNumber = "01",
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
        PresentationPoint(x = x.roundToInt(), y = y.roundToInt())

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

    private fun drawingWidth(occurrences: List<SpatialOccurrenceGeometry>): Int =
        occurrences.maxOfOrNull { occurrence -> occurrence.rectangle.right }?.plus(80) ?: 1200

    private fun drawingHeight(occurrences: List<SpatialOccurrenceGeometry>): Int =
        occurrences.maxOfOrNull { occurrence -> occurrence.rectangle.bottom }?.plus(80) ?: 800

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
