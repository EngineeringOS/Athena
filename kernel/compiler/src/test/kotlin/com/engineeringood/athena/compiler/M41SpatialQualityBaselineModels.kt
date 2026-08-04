package com.engineeringood.athena.compiler

import java.time.Instant

internal data class M41SpatialQualityBaseline(
    val schemaVersion: Int,
    val sourcePath: String,
    val sourceSha256: String,
    val generationCommand: String,
    val generatedAt: Instant,
    val sheets: List<M41SpatialSheetBaseline>,
    val m40Comparison: M41M40Comparison,
) {
    init {
        require(schemaVersion == M41SpatialQualityBaselineCodec.SCHEMA_VERSION) {
            "schema.version must be ${M41SpatialQualityBaselineCodec.SCHEMA_VERSION}."
        }
        val sourceSegments = sourcePath.split('/')
        require(
            sourcePath.isNotBlank() &&
                sourcePath == sourcePath.trim() &&
                '\\' !in sourcePath &&
                !sourcePath.startsWith('/') &&
                !WINDOWS_ABSOLUTE_PATH.matches(sourcePath) &&
                sourceSegments.all { segment -> segment.isNotEmpty() && segment != "." && segment != ".." },
        ) {
            "fixture.source.path must be a nonblank repository-relative forward-slash path."
        }
        require(SHA256_PATTERN.matches(sourceSha256)) {
            "fixture.source.sha256 must use lowercase sha256:<64 hex>."
        }
        require(generationCommand.isNotBlank() && generatedAt.toString() in generationCommand) {
            "generation.command must contain generation.timestamp."
        }
        require(sheets.isNotEmpty()) { "sheet.count must be positive." }
        require(sheets.map(M41SpatialSheetBaseline::sheetId).distinct().size == sheets.size) {
            "Sheet identities must be unique."
        }
        require(sheets == sheets.sortedBy(M41SpatialSheetBaseline::sheetId)) {
            "Sheets must use canonical Sheet identity order."
        }
    }

    private companion object {
        val SHA256_PATTERN = Regex("sha256:[0-9a-f]{64}")
        val WINDOWS_ABSOLUTE_PATH = Regex("[A-Za-z]:/.*")
    }
}

internal data class M41SpatialSheetBaseline(
    val sheetId: String,
    val extent: M41BaselineRect,
    val drawingArea: M41BaselineRect,
    val counts: M41SpatialFactCounts,
    val metrics: M41SpatialMetricValues,
) {
    init {
        require(sheetId.isNotBlank()) { "Sheet identity must not be blank." }
    }
}

internal data class M41BaselineRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) {
    init {
        require(width > 0) { "Baseline rectangle width must be positive." }
        require(height > 0) { "Baseline rectangle height must be positive." }
    }
}

internal data class M41SpatialFactCounts(
    val occurrences: Int,
    val regions: Int,
    val constructs: Int,
    val alignments: Int,
    val anchors: Int,
    val routes: Int,
    val usedLanes: Int,
    val gridReferences: Int,
    val qualitySnapshots: Int,
) {
    init {
        require(
            listOf(
                occurrences,
                regions,
                constructs,
                alignments,
                anchors,
                routes,
                usedLanes,
                gridReferences,
            ).all { count -> count >= 0 },
        ) { "Spatial fact counts must not be negative." }
        require(qualitySnapshots == 1) { "Each Sheet must contain exactly one quality snapshot." }
    }
}

internal data class M41SpatialMetricValues(
    val occurrenceOverlapCount: Int,
    val constructContainmentFailureCount: Int,
    val routeBodyIntersectionCount: Int,
    val routeCrossingCount: Int,
    val twistCount: Int,
    val usedLaneCount: Int,
    val peakRoutesPerLane: Int,
    val density: M41ExactRatio,
    val occupancy: M41ExactRatio,
) {
    init {
        require(
            listOf(
                occurrenceOverlapCount,
                constructContainmentFailureCount,
                routeBodyIntersectionCount,
                routeCrossingCount,
                twistCount,
                usedLaneCount,
                peakRoutesPerLane,
            ).all { value -> value >= 0 },
        ) { "Spatial quality count values must not be negative." }
        require(occupancy.numerator <= occupancy.denominator) {
            "Occupancy numerator must not exceed its Drawing Area denominator."
        }
    }
}

internal data class M41ExactRatio(
    val numerator: Long,
    val denominator: Long,
) {
    init {
        require(numerator >= 0L) { "Exact ratio numerator must not be negative." }
        require(denominator > 0L) { "Exact ratio denominator must be positive." }
    }

    fun toDouble(): Double = numerator.toDouble() / denominator.toDouble()
}

internal data class M41M40Comparison(
    val comparable: Boolean,
    val reason: String,
) {
    init {
        require(!comparable) { "comparison.m40.comparable must remain false without equivalence evidence." }
        require(reason.isNotBlank()) { "comparison.m40.reason must explain non-comparability." }
    }
}

internal enum class M41SpatialQualityMetric(
    val propertyName: String,
    val definition: String,
    val ratio: Boolean = false,
) {
    OCCURRENCE_OVERLAP(
        "occurrence-overlap-count",
        "Unordered Occurrence rectangle pairs with positive-area intersection.",
    ),
    CONSTRUCT_CONTAINMENT_FAILURE(
        "construct-containment-failure-count",
        "Construct member relationships whose Occurrence rectangle is not fully contained.",
    ),
    ROUTE_BODY_INTERSECTION(
        "route-body-intersection-count",
        "Route segments intersecting any non-endpoint Occurrence open interior.",
    ),
    ROUTE_CROSSING(
        "route-crossing-count",
        "Distinct unordered Route-pair and perpendicular intersection-point tuples.",
    ),
    TWIST("twist-count", "Route segments changing both x and y."),
    USED_LANES("used-lane-count", "Existing Lanes assigned by at least one Route."),
    PEAK_ROUTES_PER_LANE("peak-routes-per-lane", "Largest actual Route count assigned to one Lane."),
    DENSITY("density", "Occurrence count divided by owning Sheet Drawing Area area.", ratio = true),
    OCCUPANCY(
        "occupancy",
        "Occurrence rectangle union area divided by owning Sheet Drawing Area area.",
        ratio = true,
    ),
}
