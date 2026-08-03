package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialPlacementReason
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialRegionGeometry
import com.engineeringood.athena.spatial.SpatialRegionId
import com.engineeringood.athena.spatial.SpatialSourceTrace

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
