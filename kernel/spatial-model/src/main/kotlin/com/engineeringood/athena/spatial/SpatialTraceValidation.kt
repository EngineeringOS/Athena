package com.engineeringood.athena.spatial

internal fun spatialTraceDiagnostics(sheet: SpatialSheet): List<SpatialDiagnostic> {
    val regionsById = sheet.regions.groupBy(SpatialRegionGeometry::regionId)
    val constructsById = sheet.constructs.groupBy(SpatialConstructGeometry::constructId)
    val occurrencesById = sheet.occurrences.groupBy(SpatialOccurrenceGeometry::occurrenceId)
    return buildList {
        if (sheet.sourceTrace.projectionIds.first() != sheet.sheetId) {
            add(
                traceIssue(
                    subject = "Sheet ${sheet.sheetId}",
                    problem = "Source Trace does not start with its owning Sheet identity",
                    correction = "Retain Sheet ${sheet.sheetId} as the first Source Trace Projection identity.",
                    trace = sheet.sourceTrace,
                ),
            )
        }
        val gridPrefix = listOf(sheet.sheetId, sheet.grid.gridId)
        if (sheet.grid.sourceTrace.projectionIds.take(gridPrefix.size) != gridPrefix) {
            add(
                traceIssue(
                    subject = "Grid ${sheet.grid.gridId} on Sheet ${sheet.sheetId}",
                    problem = "Source Trace does not retain Sheet and grid identity",
                    correction = "Retain Sheet ${sheet.sheetId} followed by grid ${sheet.grid.gridId}.",
                    trace = sheet.grid.sourceTrace,
                ),
            )
        }
        sheet.occurrences.forEach { occurrence ->
            val required = listOf(sheet.sheetId, occurrence.regionId, occurrence.occurrenceId.projectionId)
            if (occurrence.sourceTrace.projectionIds.take(required.size) != required) {
                add(
                    traceIssue(
                        subject = occurrenceSubject(occurrence),
                        problem = "Source Trace does not retain Sheet, Region, and Occurrence identity",
                        correction = "Retain Sheet ${sheet.sheetId}, Region ${occurrence.regionId}, and " +
                            "Occurrence ${occurrence.occurrenceId.projectionId} in that order.",
                        trace = occurrence.sourceTrace,
                    ),
                )
            }
        }
        sheet.regions.forEach { region ->
            val required = listOf(sheet.sheetId, region.regionId.projectionId) +
                region.memberOccurrenceIds.map(SpatialOccurrenceId::projectionId)
            if (region.sourceTrace.projectionIds.take(required.size) != required) {
                add(
                    traceIssue(
                        subject = regionSubject(region),
                        problem = "Source Trace does not retain Sheet, Region, and exact member Occurrences",
                        correction = "Retain Sheet ${sheet.sheetId}, Region ${region.regionId.projectionId}, and " +
                            "every member Occurrence in source order.",
                        trace = region.sourceTrace,
                    ),
                )
            }
        }
        sheet.constructs.forEach { construct ->
            val required = listOf(sheet.sheetId, construct.constructId.projectionId) +
                construct.memberOccurrenceIds.map(SpatialOccurrenceId::projectionId)
            if (construct.sourceTrace.projectionIds.take(required.size) != required) {
                add(
                    traceIssue(
                        subject = constructSubject(construct),
                        problem = "Source Trace does not retain Sheet, Construct, and exact member Occurrences",
                        correction = "Retain Sheet ${sheet.sheetId}, Construct ${construct.constructId.projectionId}, and " +
                            "every member Occurrence in source order.",
                        trace = construct.sourceTrace,
                    ),
                )
            }
        }
        sheet.alignments.forEach { alignment ->
            val expected = when (val source = alignment.constraintSource) {
                is SpatialAlignmentSource.Region -> regionsById[source.regionId].orEmpty().singleOrNull()?.sourceTrace
                is SpatialAlignmentSource.Construct ->
                    constructsById[source.constructId].orEmpty().singleOrNull()?.sourceTrace
            }
            if (expected != null && alignment.sourceTrace != expected) {
                add(
                    traceIssue(
                        subject = alignmentSubject(alignment.constraintSource),
                        problem = "Source Trace does not equal its grouping source trace",
                        correction = "Retain the exact Region or Construct Source Trace in its alignment fact.",
                        trace = alignment.sourceTrace,
                    ),
                )
            }
        }
        sheet.anchors.forEach { anchor ->
            val required = listOf(
                sheet.sheetId,
                anchor.subject.occurrenceId.projectionId,
                anchor.subject.portId.value,
            )
            if (anchor.sourceTrace.projectionIds.take(required.size) != required) {
                add(
                    traceIssue(
                        subject = "Anchor ${anchor.anchorId.value}",
                        problem = "Source Trace does not retain Sheet, Occurrence, and port identity",
                        correction = "Retain Sheet ${sheet.sheetId}, Occurrence " +
                            "${anchor.subject.occurrenceId.projectionId}, and port ${anchor.subject.portId.value} in that order.",
                        trace = anchor.sourceTrace,
                    ),
                )
            }
        }
        sheet.gridReferences.forEach { reference ->
            val expected = when (val subject = reference.subject) {
                is SpatialGridReferenceSubject.Occurrence ->
                    occurrencesById[subject.occurrenceId].orEmpty().singleOrNull()?.sourceTrace
                is SpatialGridReferenceSubject.Construct ->
                    constructsById[subject.constructId].orEmpty().singleOrNull()?.sourceTrace
            }
            if (expected != null && reference.sourceTrace != expected) {
                add(
                    traceIssue(
                        subject = gridReferenceSubject(reference.subject),
                        problem = "Source Trace does not equal its geometry subject trace",
                        correction = "Retain the exact Occurrence or Construct Source Trace in its Grid Reference.",
                        trace = reference.sourceTrace,
                    ),
                )
            }
        }
        val expectedQualityTrace = qualityTrace(sheet)
        if (sheet.quality.sourceTrace != expectedQualityTrace) {
            add(
                traceIssue(
                    subject = "Quality snapshot on Sheet ${sheet.sheetId}",
                    problem = "Source Trace does not retain every contributing Spatial fact identity",
                    correction = "Rebuild the quality trace from the complete validated Sheet fact set.",
                    trace = sheet.quality.sourceTrace,
                ),
            )
        }
    }
}

private fun qualityTrace(sheet: SpatialSheet): SpatialSourceTrace {
    val traces = listOf(sheet.grid.sourceTrace) +
        sheet.occurrences.map(SpatialOccurrenceGeometry::sourceTrace) +
        sheet.regions.map(SpatialRegionGeometry::sourceTrace) +
        sheet.constructs.map(SpatialConstructGeometry::sourceTrace) +
        sheet.alignments.map(SpatialAlignment::sourceTrace) +
        sheet.anchors.map(SpatialAnchorPosition::sourceTrace) +
        sheet.routes.map(SpatialRoute::sourceTrace) +
        sheet.gridReferences.map(SpatialGridReference::sourceTrace)
    return SpatialSourceTrace(
        projectionIds = listOf(sheet.sheetId) +
            (sheet.sourceTrace.projectionIds + traces.flatMap(SpatialSourceTrace::projectionIds))
                .filterNot { projectionId -> projectionId == sheet.sheetId }
                .distinct()
                .sorted(),
        geometryElementIds = (sheet.sourceTrace.geometryElementIds + traces.flatMap(SpatialSourceTrace::geometryElementIds))
            .distinctBy { geometryId -> geometryId.value }
            .sortedBy { geometryId -> geometryId.value },
    )
}

private fun traceIssue(
    subject: String,
    problem: String,
    correction: String,
    trace: SpatialSourceTrace,
): SpatialDiagnostic = SpatialDiagnostic(subject, problem, correction, trace)
