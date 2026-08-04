package com.engineeringood.athena.compiler

import java.io.StringReader
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Properties
import java.util.TreeMap

internal object M41SpatialQualityBaselineCodec {
    const val SCHEMA_VERSION: Int = 1

    fun fields(baseline: M41SpatialQualityBaseline): Map<String, String> {
        val properties = TreeMap<String, String>()
        properties["comparison.m40.comparable"] = baseline.m40Comparison.comparable.toString()
        properties["comparison.m40.reason"] = baseline.m40Comparison.reason
        properties["fixture.source.path"] = baseline.sourcePath
        properties["fixture.source.sha256"] = baseline.sourceSha256
        properties["generation.command"] = baseline.generationCommand
        properties["generation.timestamp"] = baseline.generatedAt.toString()
        properties["schema.version"] = baseline.schemaVersion.toString()
        properties["sheet.count"] = baseline.sheets.size.toString()
        baseline.sheets.forEachIndexed { index, sheet -> properties.putSheet(index, sheet) }
        return properties
    }

    fun encode(baseline: M41SpatialQualityBaseline): ByteArray =
        fields(baseline).entries.joinToString(separator = "\n", postfix = "\n") { (key, value) ->
            "${escapeProperty(key)}=${escapeProperty(value)}"
        }.toByteArray(StandardCharsets.UTF_8)

    fun decode(bytes: ByteArray): M41SpatialQualityBaseline {
        require(bytes.take(3) != UTF8_BOM) { "Baseline must be UTF-8 without BOM." }
        val content = decodeUtf8(bytes)
        val loaded = DuplicateRejectingProperties()
        try {
            loaded.load(StringReader(content))
        } catch (error: DuplicatePropertyException) {
            throw IllegalArgumentException(error.message, error)
        } catch (error: IllegalArgumentException) {
            throw IllegalArgumentException("Malformed baseline property: ${error.message}", error)
        }
        val properties = loaded.stringPropertyNames().associateWith { key -> loaded.getProperty(key) }
        val schemaVersion = properties.requiredInt("schema.version")
        require(schemaVersion == SCHEMA_VERSION) { "schema.version must be $SCHEMA_VERSION." }
        val sheetCount = properties.requiredInt("sheet.count")
        require(sheetCount > 0) { "sheet.count must be positive." }
        require(sheetCount <= MAX_SHEET_COUNT) { "sheet.count must be at most $MAX_SHEET_COUNT." }
        val expectedKeys = expectedKeys(sheetCount)
        val missing = expectedKeys - properties.keys
        require(missing.isEmpty()) { "Missing required property: ${missing.sorted().first()}." }
        val unknown = properties.keys - expectedKeys
        require(unknown.isEmpty()) { "Found unknown property: ${unknown.sorted().first()}." }

        val generatedAt = properties.requiredInstant("generation.timestamp")
        val baseline = M41SpatialQualityBaseline(
            schemaVersion = schemaVersion,
            sourcePath = properties.getValue("fixture.source.path"),
            sourceSha256 = properties.getValue("fixture.source.sha256"),
            generationCommand = properties.getValue("generation.command"),
            generatedAt = generatedAt,
            sheets = (0 until sheetCount).map { index -> properties.readSheet(index) },
            m40Comparison = M41M40Comparison(
                comparable = properties.requiredBoolean("comparison.m40.comparable"),
                reason = properties.getValue("comparison.m40.reason"),
            ),
        )
        require(bytes.contentEquals(encode(baseline))) {
            "Baseline properties must use canonical key order, escaping, and final newline."
        }
        return baseline
    }

    private fun expectedKeys(sheetCount: Int): Set<String> = buildSet {
        addAll(
            listOf(
                "comparison.m40.comparable",
                "comparison.m40.reason",
                "fixture.source.path",
                "fixture.source.sha256",
                "generation.command",
                "generation.timestamp",
                "schema.version",
                "sheet.count",
            ),
        )
        repeat(sheetCount) { index ->
            val prefix = "sheet.$index"
            add("$prefix.id")
            RECT_FIELDS.forEach { field ->
                add("$prefix.extent.$field")
                add("$prefix.drawing-area.$field")
            }
            COUNT_FIELDS.forEach { field -> add("$prefix.count.$field") }
            M41SpatialQualityMetric.entries.forEach { metric ->
                add("$prefix.metric.${metric.propertyName}.definition")
                if (metric.ratio) {
                    add("$prefix.metric.${metric.propertyName}.numerator")
                    add("$prefix.metric.${metric.propertyName}.denominator")
                } else {
                    add("$prefix.metric.${metric.propertyName}.value")
                }
            }
        }
    }

    private fun MutableMap<String, String>.putSheet(index: Int, sheet: M41SpatialSheetBaseline) {
        val prefix = "sheet.$index"
        this["$prefix.id"] = sheet.sheetId
        putRect("$prefix.extent", sheet.extent)
        putRect("$prefix.drawing-area", sheet.drawingArea)
        this["$prefix.count.occurrences"] = sheet.counts.occurrences.toString()
        this["$prefix.count.regions"] = sheet.counts.regions.toString()
        this["$prefix.count.constructs"] = sheet.counts.constructs.toString()
        this["$prefix.count.alignments"] = sheet.counts.alignments.toString()
        this["$prefix.count.anchors"] = sheet.counts.anchors.toString()
        this["$prefix.count.routes"] = sheet.counts.routes.toString()
        this["$prefix.count.used-lanes"] = sheet.counts.usedLanes.toString()
        this["$prefix.count.grid-references"] = sheet.counts.gridReferences.toString()
        this["$prefix.count.quality-snapshots"] = sheet.counts.qualitySnapshots.toString()
        M41SpatialQualityMetric.entries.forEach { metric ->
            this["$prefix.metric.${metric.propertyName}.definition"] = metric.definition
            val metricPrefix = "$prefix.metric.${metric.propertyName}"
            when (metric) {
                M41SpatialQualityMetric.OCCURRENCE_OVERLAP ->
                    this["$metricPrefix.value"] = sheet.metrics.occurrenceOverlapCount.toString()
                M41SpatialQualityMetric.CONSTRUCT_CONTAINMENT_FAILURE ->
                    this["$metricPrefix.value"] = sheet.metrics.constructContainmentFailureCount.toString()
                M41SpatialQualityMetric.ROUTE_BODY_INTERSECTION ->
                    this["$metricPrefix.value"] = sheet.metrics.routeBodyIntersectionCount.toString()
                M41SpatialQualityMetric.ROUTE_CROSSING ->
                    this["$metricPrefix.value"] = sheet.metrics.routeCrossingCount.toString()
                M41SpatialQualityMetric.TWIST -> this["$metricPrefix.value"] = sheet.metrics.twistCount.toString()
                M41SpatialQualityMetric.USED_LANES ->
                    this["$metricPrefix.value"] = sheet.metrics.usedLaneCount.toString()
                M41SpatialQualityMetric.PEAK_ROUTES_PER_LANE ->
                    this["$metricPrefix.value"] = sheet.metrics.peakRoutesPerLane.toString()
                M41SpatialQualityMetric.DENSITY -> putRatio(metricPrefix, sheet.metrics.density)
                M41SpatialQualityMetric.OCCUPANCY -> putRatio(metricPrefix, sheet.metrics.occupancy)
            }
        }
    }

    private fun MutableMap<String, String>.putRect(prefix: String, rectangle: M41BaselineRect) {
        this["$prefix.x"] = rectangle.x.toString()
        this["$prefix.y"] = rectangle.y.toString()
        this["$prefix.width"] = rectangle.width.toString()
        this["$prefix.height"] = rectangle.height.toString()
    }

    private fun MutableMap<String, String>.putRatio(prefix: String, ratio: M41ExactRatio) {
        this["$prefix.numerator"] = ratio.numerator.toString()
        this["$prefix.denominator"] = ratio.denominator.toString()
    }

    private fun Map<String, String>.readSheet(index: Int): M41SpatialSheetBaseline {
        val prefix = "sheet.$index"
        M41SpatialQualityMetric.entries.forEach { metric ->
            require(getValue("$prefix.metric.${metric.propertyName}.definition") == metric.definition) {
                "$prefix.metric.${metric.propertyName}.definition does not match schema version $SCHEMA_VERSION."
            }
        }
        return M41SpatialSheetBaseline(
            sheetId = getValue("$prefix.id"),
            extent = requiredRect("$prefix.extent"),
            drawingArea = requiredRect("$prefix.drawing-area"),
            counts = M41SpatialFactCounts(
                occurrences = requiredNonnegativeInt("$prefix.count.occurrences"),
                regions = requiredNonnegativeInt("$prefix.count.regions"),
                constructs = requiredNonnegativeInt("$prefix.count.constructs"),
                alignments = requiredNonnegativeInt("$prefix.count.alignments"),
                anchors = requiredNonnegativeInt("$prefix.count.anchors"),
                routes = requiredNonnegativeInt("$prefix.count.routes"),
                usedLanes = requiredNonnegativeInt("$prefix.count.used-lanes"),
                gridReferences = requiredNonnegativeInt("$prefix.count.grid-references"),
                qualitySnapshots = requiredInt("$prefix.count.quality-snapshots"),
            ),
            metrics = M41SpatialMetricValues(
                occurrenceOverlapCount = requiredMetricInt(prefix, M41SpatialQualityMetric.OCCURRENCE_OVERLAP),
                constructContainmentFailureCount =
                    requiredMetricInt(prefix, M41SpatialQualityMetric.CONSTRUCT_CONTAINMENT_FAILURE),
                routeBodyIntersectionCount =
                    requiredMetricInt(prefix, M41SpatialQualityMetric.ROUTE_BODY_INTERSECTION),
                routeCrossingCount = requiredMetricInt(prefix, M41SpatialQualityMetric.ROUTE_CROSSING),
                twistCount = requiredMetricInt(prefix, M41SpatialQualityMetric.TWIST),
                usedLaneCount = requiredMetricInt(prefix, M41SpatialQualityMetric.USED_LANES),
                peakRoutesPerLane = requiredMetricInt(prefix, M41SpatialQualityMetric.PEAK_ROUTES_PER_LANE),
                density = requiredRatio(prefix, M41SpatialQualityMetric.DENSITY),
                occupancy = requiredRatio(prefix, M41SpatialQualityMetric.OCCUPANCY),
            ),
        )
    }

    private fun Map<String, String>.requiredRect(prefix: String): M41BaselineRect = M41BaselineRect(
        x = requiredInt("$prefix.x"),
        y = requiredInt("$prefix.y"),
        width = requiredInt("$prefix.width"),
        height = requiredInt("$prefix.height"),
    )

    private fun Map<String, String>.requiredMetricInt(prefix: String, metric: M41SpatialQualityMetric): Int =
        requiredNonnegativeInt("$prefix.metric.${metric.propertyName}.value")

    private fun Map<String, String>.requiredRatio(
        prefix: String,
        metric: M41SpatialQualityMetric,
    ): M41ExactRatio = M41ExactRatio(
        numerator = requiredNonnegativeLong("$prefix.metric.${metric.propertyName}.numerator"),
        denominator = requiredPositiveLong("$prefix.metric.${metric.propertyName}.denominator"),
    )

    private fun Map<String, String>.requiredInt(key: String): Int =
        this[key]?.toIntOrNull() ?: if (key !in this) {
            throw IllegalArgumentException("Missing required property: $key.")
        } else {
            throw IllegalArgumentException("$key must be an integer.")
        }

    private fun Map<String, String>.requiredLong(key: String): Long =
        getValue(key).toLongOrNull() ?: throw IllegalArgumentException("$key must be an integer.")

    private fun Map<String, String>.requiredNonnegativeInt(key: String): Int = requiredInt(key).also { value ->
        require(value >= 0) { "$key must not be negative." }
    }

    private fun Map<String, String>.requiredNonnegativeLong(key: String): Long = requiredLong(key).also { value ->
        require(value >= 0L) { "$key must not be negative." }
    }

    private fun Map<String, String>.requiredPositiveLong(key: String): Long = requiredLong(key).also { value ->
        require(value > 0L) { "$key must be positive." }
    }

    private fun Map<String, String>.requiredBoolean(key: String): Boolean = when (val value = getValue(key)) {
        "true" -> true
        "false" -> false
        else -> throw IllegalArgumentException("$key must be true or false, not '$value'.")
    }

    private fun Map<String, String>.requiredInstant(key: String): Instant {
        val value = getValue(key)
        require(value.endsWith("Z")) { "$key must be a UTC ISO-8601 instant ending in Z." }
        return try {
            Instant.parse(value)
        } catch (error: DateTimeParseException) {
            throw IllegalArgumentException("$key must be a UTC ISO-8601 instant.", error)
        }
    }

    private fun decodeUtf8(bytes: ByteArray): String = try {
        StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString()
    } catch (error: Exception) {
        throw IllegalArgumentException("Baseline must contain valid UTF-8.", error)
    }

    private fun escapeProperty(value: String): String = buildString {
        value.forEachIndexed { index, character ->
            when (character) {
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                '=', ':' -> append('\\').append(character)
                ' ' -> if (index == 0) append("\\ ") else append(character)
                else -> append(character)
            }
        }
    }

    private val RECT_FIELDS = listOf("x", "y", "width", "height")
    private val COUNT_FIELDS = listOf(
        "occurrences",
        "regions",
        "constructs",
        "alignments",
        "anchors",
        "routes",
        "used-lanes",
        "grid-references",
        "quality-snapshots",
    )
    private val UTF8_BOM = listOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    private const val MAX_SHEET_COUNT = 64
}

private class DuplicateRejectingProperties : Properties() {
    override fun put(key: Any, value: Any): Any? {
        if (containsKey(key)) throw DuplicatePropertyException("Found duplicate property: $key.")
        return super.put(key, value)
    }
}

private class DuplicatePropertyException(message: String) : IllegalArgumentException(message)
