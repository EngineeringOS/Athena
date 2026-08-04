package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialGridReferenceSubject
import com.engineeringood.athena.spatial.SpatialSourceTrace

internal data class CoverageExpectation<K>(
    val key: K,
    val subject: String,
    val sourceTrace: SpatialSourceTrace,
    val canonicalSourceTrace: SpatialSourceTrace = sourceTrace,
)

internal fun <K, T> exactCoverage(
    expectations: List<CoverageExpectation<K>>,
    actual: List<T>,
    actualKey: (T) -> K,
    actualSubject: (T) -> String,
    actualTrace: (T) -> SpatialSourceTrace,
    factName: String,
    expectedCorrection: String,
): List<SpatialDiagnostic> {
    val expectedByKey = expectations.associateBy(CoverageExpectation<K>::key)
    val actualByKey = actual.groupBy(actualKey)
    return buildList {
        expectations.forEach { expectation ->
            val matches = actualByKey[expectation.key].orEmpty()
            if (matches.size != 1) {
                add(
                    SpatialDiagnostic(
                        subject = expectation.subject,
                        problem = "has ${matches.size} final Spatial $factName facts",
                        correction = expectedCorrection,
                        sourceTrace = if (matches.isEmpty()) {
                            expectation.sourceTrace
                        } else {
                            combinedCoverageTrace(matches.map(actualTrace))
                        },
                    ),
                )
            } else if (actualTrace(matches.single()) != expectation.canonicalSourceTrace) {
                add(
                    SpatialDiagnostic(
                        subject = expectation.subject,
                        problem = "final Spatial $factName Source Trace does not equal canonical Projection provenance",
                        correction = "Rebuild the final Spatial $factName Source Trace from canonical Projection facts.",
                        sourceTrace = actualTrace(matches.single()),
                    ),
                )
            }
        }
        actualByKey.filterKeys { key -> key !in expectedByKey }.forEach { (_, matches) ->
            add(
                SpatialDiagnostic(
                    subject = actualSubject(matches.first()),
                    problem = "has ${matches.size} final Spatial $factName facts but no matching Projection fact",
                    correction = "Remove final Spatial $factName facts not required by canonical Projection coverage.",
                    sourceTrace = combinedCoverageTrace(matches.map(actualTrace)),
                ),
            )
        }
    }
}

internal fun sheetTrace(sheet: ProjectionSheet): SpatialSourceTrace = SpatialSourceTrace(
    projectionIds = listOf(sheet.sheetId.value),
    geometryElementIds = listOf(sheet.originGeometryElementId),
)

internal fun occurrenceTrace(sheet: ProjectionSheet, node: ProjectionNode): SpatialSourceTrace = SpatialSourceTrace(
    projectionIds = listOf(sheet.sheetId.value, node.projectionId.value),
    geometryElementIds = listOf(sheet.originGeometryElementId, node.originGeometryElementId),
)

internal fun gridReferenceSubject(subject: SpatialGridReferenceSubject): String = when (subject) {
    is SpatialGridReferenceSubject.Occurrence ->
        "Grid Reference Occurrence ${subject.occurrenceId.projectionId} on Sheet ${subject.sheetId}"
    is SpatialGridReferenceSubject.Construct ->
        "Grid Reference Construct ${subject.constructId.projectionId} on Sheet ${subject.sheetId}"
}

internal fun List<SpatialDiagnostic>.canonicalCoverageDiagnostics(): List<SpatialDiagnostic> =
    groupBy { diagnostic -> Triple(diagnostic.subject, diagnostic.problem, diagnostic.correction) }
        .map { (_, matches) ->
            matches.singleOrNull() ?: matches.first().copy(
                sourceTrace = combinedCoverageTrace(matches.map(SpatialDiagnostic::sourceTrace)),
            )
        }
        .sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem, SpatialDiagnostic::correction))

internal fun combinedCoverageTrace(traces: List<SpatialSourceTrace>): SpatialSourceTrace {
    require(traces.isNotEmpty()) { "Coverage diagnostic trace requires at least one source trace." }
    if (traces.all { trace -> trace == traces.first() }) return traces.first()
    val ordered = traces.sortedWith(
        compareBy(
            { trace -> trace.projectionIds.joinToString("\u0000") },
            { trace -> trace.geometryElementIds.joinToString("\u0000") { geometryId -> geometryId.value } },
        ),
    )
    return SpatialSourceTrace(
        projectionIds = ordered.flatMap(SpatialSourceTrace::projectionIds),
        geometryElementIds = ordered.flatMap(SpatialSourceTrace::geometryElementIds),
    )
}
