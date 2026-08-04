package com.engineeringood.athena.spatial

import com.engineeringood.athena.geometry.GeometryElementId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SpatialQualityModelsTest {
    @Test
    fun `quality snapshot owns one typed finite per Sheet metric value`() {
        val metrics = SpatialQualityMetrics(
            occurrenceOverlapCount = 1,
            constructContainmentFailureCount = 2,
            routeBodyIntersectionCount = 3,
            routeCrossingCount = 4,
            twistCount = 5,
            usedLaneCount = 6,
            peakRoutesPerLane = 7,
            density = 0.25,
            occupancy = 0.5,
        )
        val snapshot = SpatialQualitySnapshot(
            qualitySnapshotId = SpatialQualitySnapshotId("sheet:main"),
            sheetId = "sheet:main",
            metrics = metrics,
            sourceTrace = qualityTrace(),
        )

        assertEquals(metrics, snapshot.metrics)
        assertEquals("sheet:main", snapshot.qualitySnapshotId.sheetId)
        assertEquals("sheet:main", snapshot.sheetId)
        assertEquals(
            setOf(
                "occurrenceOverlapCount",
                "constructContainmentFailureCount",
                "routeBodyIntersectionCount",
                "routeCrossingCount",
                "twistCount",
                "usedLaneCount",
                "peakRoutesPerLane",
                "density",
                "occupancy",
            ),
            SpatialQualityMetrics::class.java.declaredFields.map { field -> field.name }.toSet(),
        )
    }

    @Test
    fun `quality metrics reject negative counts`() {
        countFields().forEach { field ->
            assertFailsWith<IllegalArgumentException>(field) {
                metricsWith(countOverrides = mapOf(field to -1))
            }
        }
    }

    private fun metricsWith(
        countOverrides: Map<String, Int> = emptyMap(),
        density: Double = 0.0,
        occupancy: Double = 0.0,
    ): SpatialQualityMetrics = SpatialQualityMetrics(
        occurrenceOverlapCount = countOverrides["occurrenceOverlapCount"] ?: 0,
        constructContainmentFailureCount = countOverrides["constructContainmentFailureCount"] ?: 0,
        routeBodyIntersectionCount = countOverrides["routeBodyIntersectionCount"] ?: 0,
        routeCrossingCount = countOverrides["routeCrossingCount"] ?: 0,
        twistCount = countOverrides["twistCount"] ?: 0,
        usedLaneCount = countOverrides["usedLaneCount"] ?: 0,
        peakRoutesPerLane = countOverrides["peakRoutesPerLane"] ?: 0,
        density = density,
        occupancy = occupancy,
    )

    private fun countFields(): List<String> = listOf(
        "occurrenceOverlapCount",
        "constructContainmentFailureCount",
        "routeBodyIntersectionCount",
        "routeCrossingCount",
        "twistCount",
        "usedLaneCount",
        "peakRoutesPerLane",
    )

    private fun qualityTrace(): SpatialSourceTrace = SpatialSourceTrace(
        projectionIds = listOf("sheet:main"),
        geometryElementIds = listOf(GeometryElementId("geometry:sheet:main")),
    )
}
