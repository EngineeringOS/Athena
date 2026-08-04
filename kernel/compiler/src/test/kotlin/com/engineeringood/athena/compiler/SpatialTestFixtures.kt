package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialAnchorId
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialBoundarySide
import com.engineeringood.athena.spatial.SpatialOccurrencePortSubject
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialPlacementReason
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialRegionGeometry
import com.engineeringood.athena.spatial.SpatialRegionId
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialLaneId
import com.engineeringood.athena.spatial.SpatialLaneOrientation
import com.engineeringood.athena.spatial.SpatialRoute
import com.engineeringood.athena.spatial.SpatialRouteId
import com.engineeringood.athena.spatial.SpatialSourceTrace
import com.engineeringood.athena.spatial.SpatialAlignment
import com.engineeringood.athena.spatial.SpatialConstructGeometry
import com.engineeringood.athena.spatial.SpatialGridDefinition
import com.engineeringood.athena.spatial.SpatialGridReference
import com.engineeringood.athena.spatial.SpatialQualityMetrics
import com.engineeringood.athena.spatial.SpatialQualitySnapshot
import com.engineeringood.athena.spatial.SpatialQualitySnapshotId
import com.engineeringood.athena.spatial.SpatialSheet

internal const val TEST_SPATIAL_SHEET_ID = "sheet:test"
internal const val TEST_SPATIAL_REGION_ID = "region:test"

internal fun testSpatialOccurrence(
    projectionId: String,
    subjectId: String,
    x: Int,
    y: Int,
    width: Int = 80,
    height: Int = 40,
): SpatialOccurrenceGeometry =
    SpatialOccurrenceGeometry(
        occurrenceId = SpatialOccurrenceId(TEST_SPATIAL_SHEET_ID, projectionId),
        subjectId = StableSemanticIdentity(subjectId),
        sheetId = TEST_SPATIAL_SHEET_ID,
        regionId = TEST_SPATIAL_REGION_ID,
        rectangle = SpatialRect(x, y, width, height),
        placementReason = SpatialPlacementReason(listOf("test geometry")),
        sourceTrace = SpatialSourceTrace(
            projectionIds = listOf(TEST_SPATIAL_SHEET_ID, TEST_SPATIAL_REGION_ID, projectionId),
            geometryElementIds = listOf(GeometryElementId("geometry:$projectionId")),
        ),
    )

internal fun testSpatialAnchorId(
    projectionId: String,
    portId: String = "port:$projectionId",
    sheetId: String = TEST_SPATIAL_SHEET_ID,
): SpatialAnchorId = SpatialAnchorId(
    sheetId = sheetId,
    occurrenceId = SpatialOccurrenceId(sheetId, projectionId),
    portId = StableSemanticIdentity(portId),
)

internal fun testSpatialAnchor(
    projectionId: String,
    portId: String,
    point: SpatialPoint,
    side: SpatialBoundarySide,
    sheetId: String = TEST_SPATIAL_SHEET_ID,
): SpatialAnchorPosition {
    val anchorId = testSpatialAnchorId(projectionId, portId, sheetId)
    return SpatialAnchorPosition(
        anchorId = anchorId,
        sheetId = sheetId,
        subject = SpatialOccurrencePortSubject(anchorId.occurrenceId, anchorId.portId),
        side = side,
        point = point,
        sourceTrace = SpatialSourceTrace(
            projectionIds = listOf(sheetId, projectionId, portId),
            geometryElementIds = listOf(GeometryElementId("geometry:$projectionId:$portId")),
        ),
    )
}

internal fun testSpatialRouteId(
    projectionConnectionId: String,
    sheetId: String = TEST_SPATIAL_SHEET_ID,
): SpatialRouteId = SpatialRouteId(sheetId, projectionConnectionId)

internal fun testSpatialLaneId(
    orientation: SpatialLaneOrientation = SpatialLaneOrientation.HORIZONTAL,
    coordinate: Int = 0,
    sheetId: String = TEST_SPATIAL_SHEET_ID,
): SpatialLaneId = SpatialLaneId(sheetId, orientation, coordinate)

internal fun testSpatialLane(
    vararg routeIds: String,
    orientation: SpatialLaneOrientation = SpatialLaneOrientation.HORIZONTAL,
    coordinate: Int = 0,
    sheetId: String = TEST_SPATIAL_SHEET_ID,
): SpatialLane {
    val laneId = testSpatialLaneId(orientation, coordinate, sheetId)
    return SpatialLane(
        laneId = laneId,
        sheetId = sheetId,
        orientation = orientation,
        coordinate = coordinate,
        routeIds = routeIds.map { routeId -> testSpatialRouteId(routeId, sheetId) },
    )
}

internal fun testSpatialRoute(
    routeId: String,
    connectionId: String,
    sourceAnchorId: SpatialAnchorId,
    targetAnchorId: SpatialAnchorId,
    points: List<SpatialPoint>,
    laneId: SpatialLaneId = testSpatialLaneId(),
    sheetId: String = TEST_SPATIAL_SHEET_ID,
    sourceTrace: SpatialSourceTrace = SpatialSourceTrace(
        projectionIds = listOf(
            sheetId,
            routeId,
            sourceAnchorId.occurrenceId.projectionId,
            sourceAnchorId.portId.value,
            targetAnchorId.occurrenceId.projectionId,
            targetAnchorId.portId.value,
        ),
        geometryElementIds = listOf(GeometryElementId("geometry:$routeId")),
    ),
): SpatialRoute = SpatialRoute(
    routeId = testSpatialRouteId(routeId, sheetId),
    sheetId = sheetId,
    connectionId = StableSemanticIdentity(connectionId),
    sourceAnchorId = sourceAnchorId,
    targetAnchorId = targetAnchorId,
    laneId = laneId,
    sourceTrace = sourceTrace,
    points = points,
)

internal fun testSpatialRegion(
    occurrences: List<SpatialOccurrenceGeometry>,
): SpatialRegionGeometry =
    SpatialRegionGeometry(
        regionId = SpatialRegionId(TEST_SPATIAL_SHEET_ID, TEST_SPATIAL_REGION_ID),
        sheetId = TEST_SPATIAL_SHEET_ID,
        memberOccurrenceIds = occurrences.map(SpatialOccurrenceGeometry::occurrenceId),
        bounds = paddedGroupingUnion(occurrences.map(SpatialOccurrenceGeometry::rectangle)),
        sourceTrace = SpatialSourceTrace(
            projectionIds = listOf(TEST_SPATIAL_SHEET_ID, TEST_SPATIAL_REGION_ID) +
                occurrences.map { occurrence -> occurrence.occurrenceId.projectionId },
            geometryElementIds = listOf(GeometryElementId("geometry:$TEST_SPATIAL_REGION_ID")),
        ),
    )

internal fun testSpatialSheet(
    occurrences: List<SpatialOccurrenceGeometry> = emptyList(),
    regions: List<SpatialRegionGeometry> = if (occurrences.isEmpty()) emptyList() else listOf(testSpatialRegion(occurrences)),
    constructs: List<SpatialConstructGeometry> = emptyList(),
    alignments: List<SpatialAlignment> = emptyList(),
    anchors: List<SpatialAnchorPosition> = emptyList(),
    lanes: List<SpatialLane> = emptyList(),
    routes: List<SpatialRoute> = emptyList(),
    gridReferences: List<SpatialGridReference> = emptyList(),
    qualityMetrics: SpatialQualityMetrics = zeroSpatialQualityMetrics(),
): SpatialSheet {
    val drawingArea = SpatialRect(40, 60, 1120, 640)
    val sheetTrace = SpatialSourceTrace(
        projectionIds = listOf(TEST_SPATIAL_SHEET_ID),
        geometryElementIds = listOf(GeometryElementId("geometry:$TEST_SPATIAL_SHEET_ID")),
    )
    val gridTrace = SpatialSourceTrace(
        projectionIds = listOf(TEST_SPATIAL_SHEET_ID, "grid:test"),
        geometryElementIds = sheetTrace.geometryElementIds,
    )
    val grid = SpatialGridDefinition(TEST_SPATIAL_SHEET_ID, "grid:test", drawingArea, 3, 4, gridTrace)
    return SpatialSheet(
        sheetId = TEST_SPATIAL_SHEET_ID,
        extent = SpatialRect(0, 0, 1200, 800),
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
            SpatialQualitySnapshotId(TEST_SPATIAL_SHEET_ID),
            TEST_SPATIAL_SHEET_ID,
            qualityMetrics,
            testSpatialQualityTrace(
                sheetTrace,
                grid.sourceTrace,
                occurrences.map(SpatialOccurrenceGeometry::sourceTrace) +
                    regions.map(SpatialRegionGeometry::sourceTrace) +
                    constructs.map(SpatialConstructGeometry::sourceTrace) +
                    alignments.map(SpatialAlignment::sourceTrace) +
                    anchors.map(SpatialAnchorPosition::sourceTrace) +
                    routes.map(SpatialRoute::sourceTrace) +
                    gridReferences.map(SpatialGridReference::sourceTrace),
            ),
        ),
        sourceTrace = sheetTrace,
    )
}

internal fun zeroSpatialQualityMetrics(): SpatialQualityMetrics = SpatialQualityMetrics(
    occurrenceOverlapCount = 0,
    constructContainmentFailureCount = 0,
    routeBodyIntersectionCount = 0,
    routeCrossingCount = 0,
    twistCount = 0,
    usedLaneCount = 0,
    peakRoutesPerLane = 0,
    density = 0.0,
    occupancy = 0.0,
)

internal fun testSpatialQualityTrace(
    sheetTrace: SpatialSourceTrace,
    gridTrace: SpatialSourceTrace,
    factTraces: List<SpatialSourceTrace>,
): SpatialSourceTrace = SpatialSourceTrace(
    projectionIds = listOf(TEST_SPATIAL_SHEET_ID) +
        (sheetTrace.projectionIds + gridTrace.projectionIds + factTraces.flatMap(SpatialSourceTrace::projectionIds))
            .filterNot { projectionId -> projectionId == TEST_SPATIAL_SHEET_ID }
            .distinct()
            .sorted(),
    geometryElementIds = (sheetTrace.geometryElementIds + gridTrace.geometryElementIds +
        factTraces.flatMap(SpatialSourceTrace::geometryElementIds))
        .distinctBy(GeometryElementId::value)
        .sortedBy(GeometryElementId::value),
)
