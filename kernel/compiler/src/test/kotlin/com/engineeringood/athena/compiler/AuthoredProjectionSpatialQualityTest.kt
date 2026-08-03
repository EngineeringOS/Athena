package com.engineeringood.athena.compiler

import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialLane
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialPoint
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
        assertEquals(2, first.output.occurrences.size)
        assertTrue(projection.sheets.all { sheet -> sheet.publication.coordinateZones.none { zone -> zone.zoneId.startsWith("layout-") } })
    }

    @Test
    fun `quality metrics are deterministic compiler facts with full label count`() {
        val occurrences = listOf(
            testSpatialOccurrence("o1", "component:A", 0, 0),
            testSpatialOccurrence("o2", "component:B", 140, 0),
        )
        val lanes = listOf(SpatialLane(laneId = "lane:main", direction = "horizontal"))
        val routes = listOf(
            SpatialRoute(
                routeId = "r1",
                connectionId = com.engineeringood.athena.ir.StableSemanticIdentity("connection:c1"),
                sourceOccurrenceId = "o1",
                targetOccurrenceId = "o2",
                sourceAnchorId = "anchor:o1",
                targetAnchorId = "anchor:o2",
                laneId = "lane:main",
                points = listOf(SpatialPoint(90.0, 20.0), SpatialPoint(100.0, 20.0)),
            ),
        )
        val measurements = SpatialQualityCompiler().measure(occurrences, lanes, routes)
        val spatial = SpatialDocument(
            occurrences = occurrences,
            regions = listOf(testSpatialRegion(occurrences)),
            lanes = lanes,
            routes = routes,
            qualityMeasurements = measurements,
        )

        assertEquals(measurements, spatial.qualityMeasurements)
        val values = spatial.qualityMeasurements.associate { measurement -> measurement.kind to measurement.value }
        assertEquals(1.0, values["route-count"])
        assertEquals(0.0, values["body-intersection-count"])
    }

    private fun compile(source: String): CompilerCompilationSuccess {
        val path = Files.createTempFile("athena-m40-spatial", ".athena")
        return assertIs<CompilerCompilationSuccess>(AthenaCompiler().compile(path, source))
    }
}
