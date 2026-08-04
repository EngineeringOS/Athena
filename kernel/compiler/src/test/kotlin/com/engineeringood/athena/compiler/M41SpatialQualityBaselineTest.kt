package com.engineeringood.athena.compiler

import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialQualityMetrics
import com.engineeringood.athena.spatial.SpatialRect
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class M41SpatialQualityBaselineTest {

    @Test
    fun `dedicated fixture support compiles exact source bytes through active Spatial authority`() {
        val source = loadDedicatedM41ExampleSource()
        val compiled = compileDedicatedM41Example(source)

        assertEquals(
            "examples/m41/rolling-shutter/src/com/engineeringood/m41/rollingshutter/" +
                "01-rolling-shutter-spatial.athena",
            source.repositoryRelativePath,
        )
        assertEquals(
            "sha256:3af530db2f1390c5b873ed1f5134766293bf390301535adfcb94dfd3cb1a773e",
            sha256Digest(source.bytes),
        )
        assertEquals(1, compiled.spatialDocument.sheets.size)
        assertDedicatedM41SpatialGolden(compiled.spatialDocument.sheets.single())
    }

    @Test
    fun `dedicated fixture compilation consumes captured bytes without rereading source path`() {
        val loaded = loadDedicatedM41ExampleSource()
        val detachedPath = Files.createTempDirectory("m41-captured-source").resolve("captured.athena")
        try {
            val compiled = compileDedicatedM41Example(loaded.copy(sourcePath = detachedPath))

            assertEquals(sha256Digest(loaded.bytes), sha256Digest(compiled.source.bytes))
            assertEquals(1, compiled.spatialDocument.sheets.size)
        } finally {
            detachedPath.parent.toFile().deleteRecursively()
        }
    }

    @Test
    fun `source digest changes for one byte mutation`() {
        val source = loadDedicatedM41ExampleSource()
        val mutated = source.bytes.copyOf().also { bytes -> bytes[bytes.lastIndex] = (bytes.last() + 1).toByte() }

        assertTrue(sha256Digest(source.bytes) != sha256Digest(mutated))
    }

    @Test
    fun `baseline projector publishes exact Golden facts without oracle inputs`() {
        val compiled = compileDedicatedM41Example()
        val timestamp = Instant.parse("2026-08-04T00:00:00Z")
        val command = generationCommand(timestamp)

        val baseline = M41SpatialQualityBaselineProjector.project(compiled, timestamp, command)
        val sheet = baseline.sheets.single()

        assertEquals(compiled.source.repositoryRelativePath, baseline.sourcePath)
        assertEquals(sha256Digest(compiled.source.bytes), baseline.sourceSha256)
        assertEquals("schematic/sheet/S1", sheet.sheetId)
        assertEquals(M41BaselineRect(0, 0, 1200, 800), sheet.extent)
        assertEquals(M41BaselineRect(40, 60, 1120, 640), sheet.drawingArea)
        assertEquals(M41SpatialFactCounts(8, 3, 7, 10, 16, 9, 7, 15, 1), sheet.counts)
        assertEquals(
            M41SpatialMetricValues(0, 0, 0, 3, 0, 7, 2, M41ExactRatio(8, 716_800), M41ExactRatio(25_600, 716_800)),
            sheet.metrics,
        )
    }

    @Test
    fun `baseline projector keeps Sheet order denominators and union area independent`() {
        val compiled = compileDedicatedM41Example()
        val first = testSpatialOccurrence("occurrence:a", "component:a", 40, 60, 100, 100)
        val second = testSpatialOccurrence("occurrence:b", "component:b", 90, 60, 100, 100)
        val syntheticBase = testSpatialSheet(
            occurrences = listOf(first, second),
            qualityMetrics = SpatialQualityMetrics(
                occurrenceOverlapCount = 1,
                constructContainmentFailureCount = 0,
                routeBodyIntersectionCount = 0,
                routeCrossingCount = 0,
                twistCount = 0,
                usedLaneCount = 0,
                peakRoutesPerLane = 0,
                density = 2.0 / 179_200.0,
                occupancy = 15_000.0 / 179_200.0,
            ),
        )
        val synthetic = syntheticBase.copy(
            extent = SpatialRect(0, 0, 640, 440),
            drawingArea = SpatialRect(40, 60, 560, 320),
        )
        val timestamp = Instant.parse("2026-08-04T00:00:00Z")
        val baseline = M41SpatialQualityBaselineProjector.project(
            source = compiled.source,
            document = SpatialDocument(listOf(synthetic, compiled.spatialDocument.sheets.single())),
            generatedAt = timestamp,
            generationCommand = generationCommand(timestamp),
        )

        assertEquals(listOf("schematic/sheet/S1", TEST_SPATIAL_SHEET_ID), baseline.sheets.map { sheet -> sheet.sheetId })
        assertEquals(M41ExactRatio(8, 716_800), baseline.sheets[0].metrics.density)
        assertEquals(M41ExactRatio(25_600, 716_800), baseline.sheets[0].metrics.occupancy)
        assertEquals(M41ExactRatio(2, 179_200), baseline.sheets[1].metrics.density)
        assertEquals(M41ExactRatio(15_000, 179_200), baseline.sheets[1].metrics.occupancy)
    }

    @Test
    fun `baseline projector counts only Lanes assigned by Routes`() {
        val compiled = compileDedicatedM41Example()
        val usedLane = testSpatialLane("route:a", coordinate = 100)
        val unusedLane = testSpatialLane("route:phantom", coordinate = 200)
        val route = testSpatialRoute(
            routeId = "route:a",
            connectionId = "connection:a",
            sourceAnchorId = testSpatialAnchorId("occurrence:a", "port:a"),
            targetAnchorId = testSpatialAnchorId("occurrence:b", "port:b"),
            points = listOf(
                com.engineeringood.athena.spatial.SpatialPoint(100, 100),
                com.engineeringood.athena.spatial.SpatialPoint(200, 100),
            ),
            laneId = usedLane.laneId,
        )
        val sheet = testSpatialSheet(
            lanes = listOf(usedLane, unusedLane),
            routes = listOf(route),
            qualityMetrics = zeroSpatialQualityMetrics().copy(usedLaneCount = 1, peakRoutesPerLane = 1),
        )
        val timestamp = Instant.parse("2026-08-04T00:00:00Z")

        val projected = M41SpatialQualityBaselineProjector.project(
            compiled.source,
            SpatialDocument(listOf(sheet)),
            timestamp,
            generationCommand(timestamp),
        ).sheets.single()

        assertEquals(1, projected.counts.usedLanes)
        assertEquals(1, projected.metrics.usedLaneCount)
    }

    @Test
    fun `baseline projector rejects Occurrence geometry outside Drawing Area`() {
        val compiled = compileDedicatedM41Example()
        val outside = testSpatialOccurrence("occurrence:outside", "component:outside", 0, 0, 10, 10)
        val sheet = testSpatialSheet(
            occurrences = listOf(outside),
            qualityMetrics = zeroSpatialQualityMetrics().copy(
                density = 1.0 / 716_800.0,
                occupancy = 100.0 / 716_800.0,
            ),
        )
        val timestamp = Instant.parse("2026-08-04T00:00:00Z")

        val error = assertFailsWith<IllegalArgumentException> {
            M41SpatialQualityBaselineProjector.project(
                compiled.source,
                SpatialDocument(listOf(sheet)),
                timestamp,
                generationCommand(timestamp),
            )
        }

        assertTrue("Drawing Area" in requireNotNull(error.message), error.message)
    }

    @Test
    fun `baseline projector rejects compiler ratios that disagree with exact geometry`() {
        val compiled = compileDedicatedM41Example()
        val sheet = compiled.spatialDocument.sheets.single()
        val forged = sheet.copy(
            quality = sheet.quality.copy(
                metrics = sheet.quality.metrics.copy(occupancy = 0.5),
            ),
        )
        val error = assertFailsWith<IllegalArgumentException> {
            M41SpatialQualityBaselineProjector.project(
                source = compiled.source,
                document = SpatialDocument(listOf(forged)),
                generatedAt = Instant.parse("2026-08-04T00:00:00Z"),
                generationCommand = generationCommand(Instant.parse("2026-08-04T00:00:00Z")),
            )
        }

        assertTrue("Occupancy" in requireNotNull(error.message))
    }

    @Test
    fun `generator produces canonical bytes from compiled facts and injected timestamp`() {
        val timestamp = Instant.parse("2026-08-04T00:00:00Z")
        val command = generationCommand(timestamp)

        val bytes = M41SpatialQualityBaselineGenerator.generate(
            compiled = compileDedicatedM41Example(),
            generatedAt = timestamp,
            generationCommand = command,
        )
        val parsed = M41SpatialQualityBaselineCodec.decode(bytes)

        assertEquals(timestamp, parsed.generatedAt)
        assertEquals(command, parsed.generationCommand)
        assertEquals(false, parsed.m40Comparison.comparable)
        assertContentEquals(bytes, M41SpatialQualityBaselineCodec.encode(parsed))
    }

    @Test
    fun `generator replaces artifact atomically without leaving temporary files`() {
        val repositoryRoot = Files.createTempDirectory("m41-baseline-write")
        try {
            M41SpatialQualityBaselineGenerator.writeArtifact(repositoryRoot, "first".encodeToByteArray())
            M41SpatialQualityBaselineGenerator.writeArtifact(repositoryRoot, "second".encodeToByteArray())

            val output = repositoryRoot.resolve(
                "_bmad-output/implementation-artifacts/m41/m41-spatial-quality-baseline.properties",
            )
            assertContentEquals("second".encodeToByteArray(), Files.readAllBytes(output))
            Files.list(requireNotNull(output.parent)).use { entries ->
                assertEquals(listOf(output), entries.toList())
            }
        } finally {
            repositoryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `generator rejects absent offset or mismatched timestamp arguments`() {
        listOf("", "2026-08-04T00:00:00+08:00", "not-an-instant").forEach { value ->
            val error = assertFailsWith<IllegalArgumentException> {
                M41SpatialQualityBaselineGenerator.parseTimestamp(value)
            }
            assertTrue("m41BaselineTimestamp" in requireNotNull(error.message))
        }

        val timestamp = Instant.parse("2026-08-04T00:00:00Z")
        val error = assertFailsWith<IllegalArgumentException> {
            M41SpatialQualityBaselineGenerator.generate(
                compiled = compileDedicatedM41Example(),
                generatedAt = timestamp,
                generationCommand = generationCommand(timestamp.plusSeconds(1)),
            )
        }
        assertTrue("generation.command" in requireNotNull(error.message))
    }

    @Test
    fun `committed baseline is canonical and matches a fresh compiler run field by field`() {
        val source = loadDedicatedM41ExampleSource()
        val artifact = source.repositoryRoot.resolve(
            "_bmad-output/implementation-artifacts/m41/m41-spatial-quality-baseline.properties",
        )
        assertTrue(Files.isRegularFile(artifact), "Generate missing M41 baseline with the recorded Gradle task.")
        val bytesBefore = Files.readAllBytes(artifact)

        val verified = M41SpatialQualityBaselineVerifier.verify(
            artifactBytes = bytesBefore,
            compiled = compileDedicatedM41Example(source),
            expectedGeneratedAt = COMMITTED_BASELINE_TIMESTAMP,
        )

        assertEquals("schematic/sheet/S1", verified.sheets.single().sheetId)
        assertContentEquals(bytesBefore, Files.readAllBytes(artifact), "Verification must not rewrite committed evidence.")
    }

    @Test
    fun `verifier rejects every structured tamper category with exact field names`() {
        val timestamp = Instant.parse("2026-08-04T00:00:00Z")
        val compiled = compileDedicatedM41Example()
        val canonical = M41SpatialQualityBaselineProjector.project(compiled, timestamp, generationCommand(timestamp))

        val mutatedBytes = compiled.source.bytes.copyOf().also { bytes -> bytes[0] = (bytes[0] + 1).toByte() }
        assertVerificationFailure(
            "fixture.source.sha256",
            canonical,
            compiled.copy(source = compiled.source.copy(bytes = mutatedBytes)),
            timestamp,
        )

        val wrongCount = canonical.copy(
            sheets = listOf(
                canonical.sheets.single().copy(
                    counts = canonical.sheets.single().counts.copy(occurrences = 9),
                ),
            ),
        )
        assertVerificationFailure("sheet.0.count.occurrences", wrongCount, compiled, timestamp)

        val wrongExtent = canonical.withSheet { sheet ->
            sheet.copy(extent = sheet.extent.copy(width = sheet.extent.width + 1))
        }
        assertVerificationFailure("sheet.0.extent.width", wrongExtent, compiled, timestamp)

        val wrongDrawingArea = canonical.withSheet { sheet ->
            sheet.copy(drawingArea = sheet.drawingArea.copy(height = sheet.drawingArea.height + 1))
        }
        assertVerificationFailure("sheet.0.drawing-area.height", wrongDrawingArea, compiled, timestamp)

        val wrongMetric = canonical.copy(
            sheets = listOf(
                canonical.sheets.single().copy(
                    metrics = canonical.sheets.single().metrics.copy(routeCrossingCount = 4),
                ),
            ),
        )
        assertVerificationFailure("sheet.0.metric.route-crossing-count.value", wrongMetric, compiled, timestamp)

        val wrongDensityNumerator = canonical.withSheet { sheet ->
            sheet.copy(metrics = sheet.metrics.copy(density = sheet.metrics.density.copy(numerator = 9)))
        }
        assertVerificationFailure(
            "sheet.0.metric.density.numerator",
            wrongDensityNumerator,
            compiled,
            timestamp,
        )

        val wrongDensityDenominator = canonical.withSheet { sheet ->
            sheet.copy(metrics = sheet.metrics.copy(density = sheet.metrics.density.copy(denominator = 716_801)))
        }
        assertVerificationFailure(
            "sheet.0.metric.density.denominator",
            wrongDensityDenominator,
            compiled,
            timestamp,
        )

        val wrongOccupancyNumerator = canonical.withSheet { sheet ->
            sheet.copy(metrics = sheet.metrics.copy(occupancy = sheet.metrics.occupancy.copy(numerator = 25_601)))
        }
        assertVerificationFailure(
            "sheet.0.metric.occupancy.numerator",
            wrongOccupancyNumerator,
            compiled,
            timestamp,
        )

        val wrongOccupancyDenominator = canonical.withSheet { sheet ->
            sheet.copy(metrics = sheet.metrics.copy(occupancy = sheet.metrics.occupancy.copy(denominator = 716_801)))
        }
        assertVerificationFailure(
            "sheet.0.metric.occupancy.denominator",
            wrongOccupancyDenominator,
            compiled,
            timestamp,
        )

        val wrongCommand = canonical.copy(
            generationCommand = ".\\gradlew.bat test -Pm41BaselineTimestamp=$timestamp",
        )
        assertVerificationFailure("generation.command", wrongCommand, compiled, timestamp)

        val tamperedTimestamp = timestamp.plusSeconds(1)
        val coordinatedTimestampTamper = canonical.copy(
            generatedAt = tamperedTimestamp,
            generationCommand = generationCommand(tamperedTimestamp),
        )
        assertVerificationFailure("generation.timestamp", coordinatedTimestampTamper, compiled, timestamp)

        val wrongDefinition = replaceProperty(
            M41SpatialQualityBaselineCodec.encode(canonical).decodeToString(),
            "sheet.0.metric.occupancy.definition",
            "Rounded occupied pixels.",
        ).encodeToByteArray()
        assertVerificationFailure("sheet.0.metric.occupancy.definition", wrongDefinition, compiled, timestamp)
    }

    @Test
    fun `baseline codec is canonical deterministic and round trips exact facts`() {
        val baseline = sampleBaseline()

        val first = M41SpatialQualityBaselineCodec.encode(baseline)
        val second = M41SpatialQualityBaselineCodec.encode(baseline)

        assertContentEquals(first, second)
        assertEquals(baseline, M41SpatialQualityBaselineCodec.decode(first))
        assertContentEquals(first, M41SpatialQualityBaselineCodec.encode(M41SpatialQualityBaselineCodec.decode(first)))
        val keys = first.decodeToString().lineSequence().filter(String::isNotBlank).map { line -> line.substringBefore('=') }.toList()
        assertEquals(keys.sorted(), keys)
    }

    @Test
    fun `baseline codec rejects duplicate unknown out of order and unsupported schema properties`() {
        val canonical = M41SpatialQualityBaselineCodec.encode(sampleBaseline()).decodeToString()
        val firstLine = canonical.lineSequence().first()

        assertBaselineFailure("duplicate property") { "$canonical$firstLine\n" }
        assertBaselineFailure("unknown property") { canonical + "sheet.0.metric.label-pressure.value=1\n" }
        assertBaselineFailure("canonical key order") {
            val lines = canonical.lines().filter(String::isNotEmpty).toMutableList()
            lines[0] = lines[1].also { lines[1] = lines[0] }
            lines.joinToString("\n", postfix = "\n")
        }
        assertBaselineFailure("schema.version") { replaceProperty(canonical, "schema.version", "2") }
    }

    @Test
    fun `baseline codec rejects missing malformed and non finite evidence`() {
        val canonical = M41SpatialQualityBaselineCodec.encode(sampleBaseline()).decodeToString()
        val cases = listOf(
            "required property" to removeProperty(canonical, "fixture.source.sha256"),
            "schema.version" to removeProperty(canonical, "schema.version"),
            "sheet.count" to removeProperty(canonical, "sheet.count"),
            "fixture.source.sha256" to replaceProperty(canonical, "fixture.source.sha256", "sha256:not-a-digest"),
            "generation.timestamp" to replaceProperty(canonical, "generation.timestamp", "2026-08-04T00:00:00+08:00"),
            "sheet.0.count.occurrences" to replaceProperty(canonical, "sheet.0.count.occurrences", "-1"),
            "sheet.0.metric.density.denominator" to replaceProperty(
                canonical,
                "sheet.0.metric.density.denominator",
                "0",
            ),
            "sheet.0.metric.occupancy.numerator" to replaceProperty(
                canonical,
                "sheet.0.metric.occupancy.numerator",
                "NaN",
            ),
            "sheet.0.extent.width" to replaceProperty(canonical, "sheet.0.extent.width", "wide"),
        )

        cases.forEach { (message, content) -> assertBaselineFailure(message) { content } }
    }

    @Test
    fun `baseline codec bounds declared Sheet count before expanding schema keys`() {
        val canonical = M41SpatialQualityBaselineCodec.encode(sampleBaseline()).decodeToString()

        assertBaselineFailure("sheet.count must be at most 64") {
            replaceProperty(canonical, "sheet.count", "65")
        }
    }

    @Test
    fun `baseline model rejects noncanonical or escaping fixture provenance paths`() {
        listOf(
            "/absolute/example.athena",
            "C:/absolute/example.athena",
            "../outside/example.athena",
            "examples/../outside/example.athena",
            " examples/m41/example.athena",
            "examples/m41/example.athena ",
            "examples//m41/example.athena",
        ).forEach { path ->
            val error = assertFailsWith<IllegalArgumentException> {
                sampleBaseline().copy(sourcePath = path)
            }
            assertTrue("fixture.source.path" in requireNotNull(error.message), error.message)
        }
    }

    @Test
    fun `baseline schema records metric definitions and excludes numeric M40 and label evidence`() {
        val encoded = M41SpatialQualityBaselineCodec.encode(sampleBaseline()).decodeToString()

        M41SpatialQualityMetric.entries.forEach { metric ->
            assertTrue("sheet.0.metric.${metric.propertyName}.definition=" in encoded)
        }
        assertTrue("comparison.m40.comparable=false" in encoded)
        assertTrue("comparison.m40.reason=" in encoded)
        assertTrue("comparison.m40.metric" !in encoded)
        assertTrue("label" !in encoded.lowercase())
    }

    private fun assertBaselineFailure(expectedMessage: String, content: () -> String) {
        val error = assertFailsWith<IllegalArgumentException> {
            M41SpatialQualityBaselineCodec.decode(content().encodeToByteArray())
        }
        assertTrue(expectedMessage in requireNotNull(error.message), error.message)
    }

    private fun assertVerificationFailure(
        expectedField: String,
        baseline: M41SpatialQualityBaseline,
        compiled: DedicatedM41CompiledExample,
        expectedGeneratedAt: Instant,
    ) = assertVerificationFailure(
        expectedField,
        M41SpatialQualityBaselineCodec.encode(baseline),
        compiled,
        expectedGeneratedAt,
    )

    private fun assertVerificationFailure(
        expectedField: String,
        artifactBytes: ByteArray,
        compiled: DedicatedM41CompiledExample,
        expectedGeneratedAt: Instant,
    ) {
        val error = assertFailsWith<IllegalArgumentException> {
            M41SpatialQualityBaselineVerifier.verify(artifactBytes, compiled, expectedGeneratedAt)
        }
        assertTrue(expectedField in requireNotNull(error.message), error.message)
    }

    private fun M41SpatialQualityBaseline.withSheet(
        transform: (M41SpatialSheetBaseline) -> M41SpatialSheetBaseline,
    ): M41SpatialQualityBaseline = copy(sheets = listOf(transform(sheets.single())))

    private fun sampleBaseline(): M41SpatialQualityBaseline = M41SpatialQualityBaseline(
        schemaVersion = 1,
        sourcePath = "examples/m41/rolling-shutter/src/example.athena",
        sourceSha256 = "sha256:" + "a".repeat(64),
        generationCommand =
            ".\\gradlew.bat :kernel:compiler:generateM41SpatialQualityBaseline " +
                "-Pm41BaselineTimestamp=2026-08-04T00:00:00Z",
        generatedAt = Instant.parse("2026-08-04T00:00:00Z"),
        sheets = listOf(
            M41SpatialSheetBaseline(
                sheetId = "schematic/sheet/S1",
                extent = M41BaselineRect(0, 0, 1200, 800),
                drawingArea = M41BaselineRect(40, 60, 1120, 640),
                counts = M41SpatialFactCounts(
                    occurrences = 8,
                    regions = 3,
                    constructs = 7,
                    alignments = 10,
                    anchors = 16,
                    routes = 9,
                    usedLanes = 7,
                    gridReferences = 15,
                    qualitySnapshots = 1,
                ),
                metrics = M41SpatialMetricValues(
                    occurrenceOverlapCount = 0,
                    constructContainmentFailureCount = 0,
                    routeBodyIntersectionCount = 0,
                    routeCrossingCount = 3,
                    twistCount = 0,
                    usedLaneCount = 7,
                    peakRoutesPerLane = 2,
                    density = M41ExactRatio(8, 716_800),
                    occupancy = M41ExactRatio(25_600, 716_800),
                ),
            ),
        ),
        m40Comparison = M41M40Comparison(
            comparable = false,
            reason = "Fixture, viewport, units, and method differ.",
        ),
    )

    private fun replaceProperty(content: String, key: String, value: String): String = content.lineSequence()
        .map { line -> if (line.startsWith("$key=")) "$key=$value" else line }
        .joinToString("\n")

    private fun removeProperty(content: String, key: String): String = content.lineSequence()
        .filterNot { line -> line.startsWith("$key=") }
        .joinToString("\n")

    private fun generationCommand(timestamp: Instant): String =
        ".\\gradlew.bat :kernel:compiler:generateM41SpatialQualityBaseline " +
            "-Pm41BaselineTimestamp=$timestamp"

    private companion object {
        val COMMITTED_BASELINE_TIMESTAMP: Instant = Instant.parse("2026-08-04T01:20:00Z")
    }
}
