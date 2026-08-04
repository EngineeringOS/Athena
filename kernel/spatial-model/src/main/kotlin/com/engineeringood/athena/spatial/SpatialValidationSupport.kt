package com.engineeringood.athena.spatial

internal fun SpatialSheet.foreignIssue(
    fact: String,
    actualSheetId: String,
    trace: SpatialSourceTrace,
): SpatialDiagnostic = SpatialDiagnostic(
    subject = "Sheet $sheetId",
    problem = "contains $fact owned by Sheet $actualSheetId",
    correction = "Keep every Spatial fact inside its exact owning Sheet root.",
    sourceTrace = combinedTrace(listOf(sourceTrace, trace)),
)

internal fun <T, K> duplicateFacts(
    facts: List<T>,
    identity: (T) -> K,
    describe: (T) -> Triple<String, SpatialSourceTrace, String>,
): List<SpatialDiagnostic> = facts.groupBy(identity)
    .filterValues { matches -> matches.size > 1 }
    .map { (_, matches) ->
        val (subject, _, correction) = describe(matches.first())
        spatialIssue(
            subject,
            "has ${matches.size} facts with the same identity",
            correction,
            matches.map { fact -> describe(fact).second },
        )
    }

internal fun spatialIssue(
    subject: String,
    problem: String,
    correction: String,
    traces: List<SpatialSourceTrace>,
): SpatialDiagnostic = SpatialDiagnostic(subject, problem, correction, combinedTrace(traces))

internal fun List<SpatialDiagnostic>.canonicalSpatialDiagnostics(): List<SpatialDiagnostic> =
    groupBy { diagnostic -> Triple(diagnostic.subject, diagnostic.problem, diagnostic.correction) }
        .map { (identity, matches) ->
            SpatialDiagnostic(
                subject = identity.first,
                problem = identity.second,
                correction = identity.third,
                sourceTrace = combinedTrace(matches.map(SpatialDiagnostic::sourceTrace)),
            )
        }
        .sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem, SpatialDiagnostic::correction))

internal fun combinedTrace(traces: List<SpatialSourceTrace>): SpatialSourceTrace {
    require(traces.isNotEmpty()) { "Combined Spatial diagnostic trace requires at least one source trace." }
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

internal fun SpatialRect.text(): String = "($x,$y,$width,$height)"
internal fun occurrenceSubject(fact: SpatialOccurrenceGeometry): String =
    "Occurrence ${fact.occurrenceId.projectionId} on Sheet ${fact.sheetId}"
internal fun regionSubject(fact: SpatialRegionGeometry): String =
    "Region ${fact.regionId.projectionId} on Sheet ${fact.sheetId}"
internal fun constructSubject(fact: SpatialConstructGeometry): String =
    "Construct ${fact.constructId.projectionId} on Sheet ${fact.sheetId}"
internal fun alignmentSubject(source: SpatialAlignmentSource): String = when (source) {
    is SpatialAlignmentSource.Region ->
        "Alignment Region ${source.regionId.projectionId} on Sheet ${source.regionId.sheetId}"
    is SpatialAlignmentSource.Construct ->
        "Alignment Construct ${source.constructId.projectionId} on Sheet ${source.constructId.sheetId}"
}
internal fun gridReferenceSubject(subject: SpatialGridReferenceSubject): String = when (subject) {
    is SpatialGridReferenceSubject.Occurrence ->
        "Grid Reference Occurrence ${subject.occurrenceId.projectionId} on Sheet ${subject.sheetId}"
    is SpatialGridReferenceSubject.Construct ->
        "Grid Reference Construct ${subject.constructId.projectionId} on Sheet ${subject.sheetId}"
}
