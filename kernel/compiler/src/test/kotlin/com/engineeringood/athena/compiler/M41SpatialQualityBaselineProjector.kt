package com.engineeringood.athena.compiler

import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialSheet
import java.time.Instant

internal object M41SpatialQualityBaselineProjector {
    fun project(
        compiled: DedicatedM41CompiledExample,
        generatedAt: Instant,
        generationCommand: String,
    ): M41SpatialQualityBaseline = project(
        source = compiled.source,
        document = compiled.spatialDocument,
        generatedAt = generatedAt,
        generationCommand = generationCommand,
    )

    fun project(
        source: DedicatedM41ExampleSource,
        document: SpatialDocument,
        generatedAt: Instant,
        generationCommand: String,
    ): M41SpatialQualityBaseline = M41SpatialQualityBaseline(
        schemaVersion = M41SpatialQualityBaselineCodec.SCHEMA_VERSION,
        sourcePath = source.repositoryRelativePath,
        sourceSha256 = sha256Digest(source.bytes),
        generationCommand = generationCommand,
        generatedAt = generatedAt,
        sheets = document.sheets.sortedBy(SpatialSheet::sheetId).map(::projectSheet),
        m40Comparison = M41M40Comparison(
            comparable = false,
            reason = "M40 fixture, viewport, units, and measurement method differ from M41 Spatial evidence.",
        ),
    )

    private fun projectSheet(sheet: SpatialSheet): M41SpatialSheetBaseline {
        require(sheet.occurrences.all { occurrence -> occurrence.rectangle.isInside(sheet.drawingArea) }) {
            "Every Occurrence on Sheet ${sheet.sheetId} must stay inside its Drawing Area before baseline measurement."
        }
        val denominator = Math.multiplyExact(sheet.drawingArea.width.toLong(), sheet.drawingArea.height.toLong())
        val density = M41ExactRatio(sheet.occurrences.size.toLong(), denominator)
        val occupancy = M41ExactRatio(independentOccurrenceUnionArea(sheet.occurrences), denominator)
        require(sheet.quality.metrics.density == density.toDouble()) {
            "Density on Sheet ${sheet.sheetId} does not match exact Occurrence count / Drawing Area facts."
        }
        require(sheet.quality.metrics.occupancy == occupancy.toDouble()) {
            "Occupancy on Sheet ${sheet.sheetId} does not match exact Occurrence union / Drawing Area facts."
        }
        val existingLaneIds = sheet.lanes.map { lane -> lane.laneId }.toSet()
        val usedLaneIds = sheet.routes.map { route -> route.laneId }.filter { laneId -> laneId in existingLaneIds }.toSet()
        require(sheet.quality.metrics.usedLaneCount == usedLaneIds.size) {
            "Used Lane count on Sheet ${sheet.sheetId} does not match actual Route assignments."
        }
        val peakRoutesPerLane = sheet.routes
            .filter { route -> route.laneId in existingLaneIds }
            .groupingBy { route -> route.laneId }
            .eachCount()
            .values
            .maxOrNull() ?: 0
        require(sheet.quality.metrics.peakRoutesPerLane == peakRoutesPerLane) {
            "Peak Routes per Lane on Sheet ${sheet.sheetId} does not match published Route facts."
        }
        return M41SpatialSheetBaseline(
            sheetId = sheet.sheetId,
            extent = sheet.extent.toBaselineRect(),
            drawingArea = sheet.drawingArea.toBaselineRect(),
            counts = M41SpatialFactCounts(
                occurrences = sheet.occurrences.size,
                regions = sheet.regions.size,
                constructs = sheet.constructs.size,
                alignments = sheet.alignments.size,
                anchors = sheet.anchors.size,
                routes = sheet.routes.size,
                usedLanes = usedLaneIds.size,
                gridReferences = sheet.gridReferences.size,
                qualitySnapshots = 1,
            ),
            metrics = M41SpatialMetricValues(
                occurrenceOverlapCount = sheet.quality.metrics.occurrenceOverlapCount,
                constructContainmentFailureCount = sheet.quality.metrics.constructContainmentFailureCount,
                routeBodyIntersectionCount = sheet.quality.metrics.routeBodyIntersectionCount,
                routeCrossingCount = sheet.quality.metrics.routeCrossingCount,
                twistCount = sheet.quality.metrics.twistCount,
                usedLaneCount = sheet.quality.metrics.usedLaneCount,
                peakRoutesPerLane = sheet.quality.metrics.peakRoutesPerLane,
                density = density,
                occupancy = occupancy,
            ),
        )
    }

    private fun independentOccurrenceUnionArea(occurrences: List<SpatialOccurrenceGeometry>): Long {
        val rectangles = occurrences.map(SpatialOccurrenceGeometry::rectangle)
        val xCoordinates = rectangles.flatMap { rectangle -> listOf(rectangle.x, rectangle.right) }.distinct().sorted()
        val yCoordinates = rectangles.flatMap { rectangle -> listOf(rectangle.y, rectangle.bottom) }.distinct().sorted()
        var area = 0L
        xCoordinates.zipWithNext().forEach { (left, right) ->
            yCoordinates.zipWithNext().forEach { (top, bottom) ->
                val covered = rectangles.any { rectangle ->
                    rectangle.x <= left && rectangle.right >= right && rectangle.y <= top && rectangle.bottom >= bottom
                }
                if (covered) {
                    area = Math.addExact(
                        area,
                        Math.multiplyExact(right.toLong() - left.toLong(), bottom.toLong() - top.toLong()),
                    )
                }
            }
        }
        return area
    }

    private fun SpatialRect.toBaselineRect(): M41BaselineRect = M41BaselineRect(x, y, width, height)
}
