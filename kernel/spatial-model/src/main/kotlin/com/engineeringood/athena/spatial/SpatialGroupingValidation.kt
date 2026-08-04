package com.engineeringood.athena.spatial

internal fun groupingDiagnostics(sheet: SpatialSheet): List<SpatialDiagnostic> {
    val occurrencesById = sheet.occurrences.groupBy(SpatialOccurrenceGeometry::occurrenceId)
    val regionsById = sheet.regions.groupBy(SpatialRegionGeometry::regionId)
    val constructsById = sheet.constructs.groupBy(SpatialConstructGeometry::constructId)
    return buildList {
        sheet.regions.forEach { region ->
            addAll(memberDiagnostics(regionSubject(region), region.memberOccurrenceIds, occurrencesById, region.sourceTrace))
            region.memberOccurrenceIds.distinct().forEach { memberId ->
                occurrencesById[memberId].orEmpty().singleOrNull()?.let { occurrence ->
                    if (occurrence.regionId != region.regionId.projectionId) {
                        add(
                            spatialIssue(
                                subject = regionSubject(region),
                                problem = "lists Occurrence ${memberId.projectionId} declared for Region ${occurrence.regionId}",
                                correction = "List each Occurrence only in its declared same-Sheet Region.",
                                traces = listOf(region.sourceTrace, occurrence.sourceTrace),
                            ),
                        )
                    }
                    if (!occurrence.rectangle.isInside(region.bounds)) {
                        add(
                            SpatialDiagnostic(
                                subject = regionSubject(region),
                                problem = "does not contain member Occurrence ${memberId.projectionId}",
                                correction = "Derive the Region bounds from the complete member rectangle union.",
                                sourceTrace = region.sourceTrace,
                            ),
                        )
                    }
                }
            }
        }
        sheet.constructs.forEach { construct ->
            addAll(
                memberDiagnostics(
                    constructSubject(construct),
                    construct.memberOccurrenceIds,
                    occurrencesById,
                    construct.sourceTrace,
                ),
            )
            construct.memberOccurrenceIds.distinct().forEach { memberId ->
                occurrencesById[memberId].orEmpty().singleOrNull()?.let { occurrence ->
                    if (!occurrence.rectangle.isInside(construct.envelope)) {
                        add(
                            SpatialDiagnostic(
                                subject = constructSubject(construct),
                                problem = "does not contain member Occurrence ${memberId.projectionId}",
                                correction = "Derive the Construct envelope from the complete member rectangle union.",
                                sourceTrace = construct.sourceTrace,
                            ),
                        )
                    }
                }
            }
        }
        sheet.occurrences.forEach { occurrence ->
            val regionId = SpatialRegionId(sheet.sheetId, occurrence.regionId)
            val regions = regionsById[regionId].orEmpty()
            if (regions.size != 1 || regions.singleOrNull()?.memberOccurrenceIds?.count { it == occurrence.occurrenceId } != 1) {
                add(
                    SpatialDiagnostic(
                        subject = occurrenceSubject(occurrence),
                        problem = "does not resolve one reciprocal Region membership ${occurrence.regionId}",
                        correction = "List the Occurrence exactly once in its declared same-Sheet Region.",
                        sourceTrace = occurrence.sourceTrace,
                    ),
                )
            }
        }
        val alignmentsBySource = sheet.alignments.groupBy(SpatialAlignment::constraintSource)
        val expectedSources = regionsById.keys.map(SpatialAlignmentSource::Region) +
            constructsById.keys.map(SpatialAlignmentSource::Construct)
        expectedSources.forEach { source ->
            val matches = alignmentsBySource[source].orEmpty()
            if (matches.size != 1) {
                add(
                    SpatialDiagnostic(
                        subject = alignmentSubject(source),
                        problem = "has ${matches.size} alignment facts",
                        correction = "Publish exactly one alignment for each Region and Construct source.",
                        sourceTrace = groupingTrace(source, regionsById, constructsById, sheet.sourceTrace),
                    ),
                )
            }
        }
        sheet.alignments.forEach { alignment ->
            val expectedMembers = when (val source = alignment.constraintSource) {
                is SpatialAlignmentSource.Region -> regionsById[source.regionId].orEmpty().singleOrNull()?.memberOccurrenceIds
                is SpatialAlignmentSource.Construct ->
                    constructsById[source.constructId].orEmpty().singleOrNull()?.memberOccurrenceIds
            }
            if (expectedMembers == null) {
                add(
                    SpatialDiagnostic(
                        subject = alignmentSubject(alignment.constraintSource),
                        problem = "does not resolve one same-Sheet grouping source",
                        correction = "Reference one existing Region or Construct from the alignment.",
                        sourceTrace = alignment.sourceTrace,
                    ),
                )
            } else if (
                alignment.occurrenceIds.groupingBy { occurrenceId -> occurrenceId }.eachCount() !=
                expectedMembers.groupingBy { occurrenceId -> occurrenceId }.eachCount()
            ) {
                add(
                    SpatialDiagnostic(
                        subject = alignmentSubject(alignment.constraintSource),
                        problem = "members do not equal its grouping source members",
                        correction = "Preserve the exact grouping membership in the alignment fact.",
                        sourceTrace = alignment.sourceTrace,
                    ),
                )
            }
            addAll(
                memberDiagnostics(
                    alignmentSubject(alignment.constraintSource),
                    alignment.occurrenceIds,
                    occurrencesById,
                    alignment.sourceTrace,
                ),
            )
        }
    }
}

private fun memberDiagnostics(
    subject: String,
    memberIds: List<SpatialOccurrenceId>,
    occurrencesById: Map<SpatialOccurrenceId, List<SpatialOccurrenceGeometry>>,
    trace: SpatialSourceTrace,
): List<SpatialDiagnostic> = buildList {
    memberIds.groupBy { it }.filterValues { matches -> matches.size > 1 }.keys.forEach { memberId ->
        add(
            SpatialDiagnostic(
                subject = subject,
                problem = "repeats member Occurrence ${memberId.projectionId}",
                correction = "List each same-Sheet Occurrence member exactly once.",
                sourceTrace = trace,
            ),
        )
    }
    memberIds.distinct().forEach { memberId ->
        val count = occurrencesById[memberId].orEmpty().size
        if (count != 1) {
            add(
                SpatialDiagnostic(
                    subject = subject,
                    problem = "member Occurrence ${memberId.projectionId} resolves to $count geometry facts",
                    correction = "Reference one existing same-Sheet Occurrence geometry fact.",
                    sourceTrace = trace,
                ),
            )
        }
    }
}

private fun groupingTrace(
    source: SpatialAlignmentSource,
    regionsById: Map<SpatialRegionId, List<SpatialRegionGeometry>>,
    constructsById: Map<SpatialConstructId, List<SpatialConstructGeometry>>,
    fallback: SpatialSourceTrace,
): SpatialSourceTrace {
    val traces = when (source) {
        is SpatialAlignmentSource.Region -> regionsById[source.regionId].orEmpty().map(SpatialRegionGeometry::sourceTrace)
        is SpatialAlignmentSource.Construct ->
            constructsById[source.constructId].orEmpty().map(SpatialConstructGeometry::sourceTrace)
    }
    return if (traces.isEmpty()) fallback else combinedTrace(traces)
}
