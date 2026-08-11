package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.spatial.SpatialSourceTrace
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectionSpatialCoverageSupportTest {
    @Test
    fun `canonical coverage retains appended authored placement evidence`() {
        val canonical = SpatialSourceTrace(
            projectionIds = listOf("sheet", "region", "occurrence"),
            geometryElementIds = listOf(GeometryElementId("projection:occurrence")),
        )
        val actual = SpatialSourceTrace(
            projectionIds = canonical.projectionIds + "sheet-companion:7",
            geometryElementIds = canonical.geometryElementIds,
        )

        val diagnostics = exactCoverage(
            expectations = listOf(CoverageExpectation("occurrence", "Occurrence occurrence", canonical)),
            actual = listOf(actual),
            actualKey = { "occurrence" },
            actualSubject = { "Occurrence occurrence" },
            actualTrace = { it },
            factName = "geometry",
            expectedCorrection = "Restore canonical coverage.",
        )

        assertEquals(emptyList(), diagnostics)
    }

    @Test
    fun `canonical coverage rejects replacement or untyped provenance`() {
        val canonical = SpatialSourceTrace(
            projectionIds = listOf("sheet", "region", "occurrence"),
            geometryElementIds = listOf(GeometryElementId("projection:occurrence")),
        )
        val replaced = SpatialSourceTrace(
            projectionIds = listOf("sheet", "other-region", "occurrence", "sheet-companion:7"),
            geometryElementIds = canonical.geometryElementIds,
        )
        val untyped = SpatialSourceTrace(
            projectionIds = canonical.projectionIds + "not-authored-evidence",
            geometryElementIds = canonical.geometryElementIds,
        )
        val foreignSourceUnit = SpatialSourceTrace(
            projectionIds = canonical.projectionIds + "other-source:7",
            geometryElementIds = canonical.geometryElementIds,
        )

        listOf(replaced, untyped, foreignSourceUnit).forEach { actual ->
            val diagnostics = exactCoverage(
                expectations = listOf(CoverageExpectation("occurrence", "Occurrence occurrence", canonical)),
                actual = listOf(actual),
                actualKey = { "occurrence" },
                actualSubject = { "Occurrence occurrence" },
                actualTrace = { it },
                factName = "geometry",
                expectedCorrection = "Restore canonical coverage.",
            )

            assertEquals(1, diagnostics.size)
        }
    }
}
