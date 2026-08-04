package com.engineeringood.athena.compiler

import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialRoute
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthoredProjectionSpatialQualityTest {

    @Test
    fun `canonical spatial transformation derives deterministic authored projection placements`() {
        val projection = compile(
            """
            system Demo {
              device Supply { port L1 { direction out signal Power role line } }
              device Breaker { port line { direction in signal Power role line } }
              view schematic {
                sheet S1
                grid G1 { rows 3 columns 4 }
                region "Power" { occurrences [Supply, Breaker] }
              }
            }
            """.trimIndent(),
        ).authoredProjectionViews.single()

        val transformation = ProjectionSpatialCompiler()
        val first = assertIs<RealityTransformationResult.Success<SpatialDocument>>(transformation.transform(projection))
        val second = assertIs<RealityTransformationResult.Success<SpatialDocument>>(transformation.transform(projection))

        assertEquals(first.output, second.output)
        assertEquals(2, first.output.sheets.single().occurrences.size)
        assertTrue(projection.sheets.all { sheet -> sheet.publication.coordinateZones.none { zone -> zone.zoneId.startsWith("layout-") } })
    }

    @Test
    fun `quality metrics are deterministic typed compiler facts`() {
        val occurrences = listOf(
            testSpatialOccurrence("o1", "component:A", 0, 0),
            testSpatialOccurrence("o2", "component:B", 140, 0),
        )
        val lanes = listOf(testSpatialLane("r1"))
        val routes = listOf(
            testSpatialRoute(
                routeId = "r1",
                connectionId = "connection:c1",
                sourceAnchorId = testSpatialAnchorId("o1"),
                targetAnchorId = testSpatialAnchorId("o2"),
                points = listOf(SpatialPoint(90, 20), SpatialPoint(100, 20)),
            ),
        )
        val metrics = SpatialQualityCompiler().measure(
            drawingArea = SpatialRect(0, 0, 400, 200),
            occurrences = occurrences,
            constructs = emptyList(),
            lanes = lanes,
            routes = routes,
        )
        val spatial = SpatialDocument(listOf(testSpatialSheet(
            occurrences = occurrences,
            regions = listOf(testSpatialRegion(occurrences)),
            lanes = lanes,
            routes = routes,
            qualityMetrics = metrics,
        )))

        assertEquals(metrics, spatial.sheets.single().quality.metrics)
        assertEquals(1, metrics.usedLaneCount)
        assertEquals(0, metrics.routeBodyIntersectionCount)
    }

    private fun compile(source: String): CompilerCompilationSuccess {
        val path = Files.createTempFile("athena-m40-spatial", ".athena")
        return assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path, source))
    }
}
