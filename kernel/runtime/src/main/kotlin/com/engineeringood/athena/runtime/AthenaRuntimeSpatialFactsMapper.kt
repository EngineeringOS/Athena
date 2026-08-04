package com.engineeringood.athena.runtime

import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialSheet

internal fun SpatialDocument.toRuntimeSpatialFacts(
    viewId: String,
    activeSheetId: String?,
): AthenaRuntimeSpatialFacts = AthenaRuntimeSpatialFacts(
    viewId = viewId,
    activeSheetId = activeSheetId,
    sheets = sheets.map(SpatialSheet::toRuntimeSpatialSheetFacts),
)

private fun SpatialSheet.toRuntimeSpatialSheetFacts(): AthenaRuntimeSpatialSheetFacts =
    AthenaRuntimeSpatialSheetFacts(
        sheetId = sheetId,
        extent = AthenaRuntimeRect(extent.x, extent.y, extent.width, extent.height),
        drawingArea = AthenaRuntimeRect(drawingArea.x, drawingArea.y, drawingArea.width, drawingArea.height),
        occurrences = occurrences.map { occurrence ->
            AthenaRuntimeSpatialOccurrenceFacts(
                occurrenceId = occurrence.occurrenceId.projectionId,
                semanticId = occurrence.subjectId.value,
                regionId = occurrence.regionId,
                bounds = AthenaRuntimeRect(
                    occurrence.rectangle.x,
                    occurrence.rectangle.y,
                    occurrence.rectangle.width,
                    occurrence.rectangle.height,
                ),
            )
        },
        regions = regions.map { region ->
            AthenaRuntimeSpatialRegionFacts(
                regionId = region.regionId.projectionId,
                bounds = AthenaRuntimeRect(region.bounds.x, region.bounds.y, region.bounds.width, region.bounds.height),
                memberOccurrenceIds = region.memberOccurrenceIds.map { occurrenceId -> occurrenceId.projectionId },
            )
        },
        constructs = constructs.map { construct ->
            AthenaRuntimeSpatialConstructFacts(
                constructId = construct.constructId.projectionId,
                kind = construct.kind,
                name = construct.name,
                bounds = AthenaRuntimeRect(
                    construct.envelope.x,
                    construct.envelope.y,
                    construct.envelope.width,
                    construct.envelope.height,
                ),
                memberOccurrenceIds = construct.memberOccurrenceIds.map { occurrenceId -> occurrenceId.projectionId },
            )
        },
        anchors = anchors.map { anchor ->
            AthenaRuntimeSpatialAnchorFacts(
                anchorId = anchor.anchorId.value,
                occurrenceId = anchor.subject.occurrenceId.projectionId,
                portSemanticId = anchor.subject.portId.value,
                side = anchor.side.name.lowercase(),
                point = AthenaRuntimePoint(anchor.point.x, anchor.point.y),
            )
        },
        routes = routes.map { route ->
            AthenaRuntimeSpatialRouteFacts(
                routeId = route.routeId.value,
                projectionConnectionId = route.routeId.projectionConnectionId,
                connectionId = route.connectionId.value,
                sourceAnchorId = route.sourceAnchorId.value,
                targetAnchorId = route.targetAnchorId.value,
                laneId = route.laneId.value,
                points = route.points.map { point -> AthenaRuntimePoint(point.x, point.y) },
            )
        },
        lanes = lanes.map { lane ->
            AthenaRuntimeSpatialLaneFacts(
                laneId = lane.laneId.value,
                orientation = lane.orientation.name.lowercase(),
                coordinate = lane.coordinate,
                routeIds = lane.routeIds.map { routeId -> routeId.value },
            )
        },
        gridReferences = gridReferences.map { reference ->
            AthenaRuntimeSpatialGridReferenceFacts(
                gridReferenceId = "grid-reference:${reference.sheetId}:${reference.cellReference}:${reference.subject.projectionId}",
                subjectId = reference.subject.projectionId,
                cellReference = reference.cellReference,
                rowLabel = reference.rowLabel,
                columnNumber = reference.columnNumber,
            )
        },
        quality = AthenaRuntimeSpatialQualityFacts(
            occurrenceOverlapCount = quality.metrics.occurrenceOverlapCount,
            constructContainmentFailureCount = quality.metrics.constructContainmentFailureCount,
            routeBodyIntersectionCount = quality.metrics.routeBodyIntersectionCount,
            routeCrossingCount = quality.metrics.routeCrossingCount,
            twistCount = quality.metrics.twistCount,
            usedLaneCount = quality.metrics.usedLaneCount,
            peakRoutesPerLane = quality.metrics.peakRoutesPerLane,
            density = quality.metrics.density,
            occupancy = quality.metrics.occupancy,
        ),
    )
