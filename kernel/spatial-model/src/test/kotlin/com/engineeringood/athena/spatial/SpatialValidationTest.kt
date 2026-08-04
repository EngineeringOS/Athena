package com.engineeringood.athena.spatial

import com.engineeringood.athena.spatial.SpatialValidationTestFixtures.alignment
import com.engineeringood.athena.spatial.SpatialValidationTestFixtures.gridReference
import com.engineeringood.athena.spatial.SpatialValidationTestFixtures.occurrence
import com.engineeringood.athena.spatial.SpatialValidationTestFixtures.trace
import com.engineeringood.athena.spatial.SpatialValidationTestFixtures.validDocument
import com.engineeringood.athena.spatial.SpatialValidationTestFixtures.validSheet
import com.engineeringood.athena.spatial.SpatialValidationTestFixtures.withCurrentQualityTrace
import com.engineeringood.athena.spatial.SpatialValidationTestFixtures.withObstacle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpatialValidationTest {
    @Test
    fun `complete valid Sheet passes final spatial validation`() {
        val document = validDocument()

        val result = SpatialValidation.validate(document)

        assertTrue(result.isValid)
        assertEquals(emptyList(), result.diagnostics)
    }

    @Test
    fun `negative and non finite quality ratios accumulate exact metric integrity diagnostics`() {
        val valid = validSheet()
        val invalidMetrics = valid.quality.metrics.copy(
            density = -0.1,
            occupancy = Double.NaN,
        )
        val invalid = valid.copy(
            quality = valid.quality.copy(metrics = invalidMetrics),
        )

        val result = SpatialValidation.validate(SpatialDocument(listOf(invalid)))

        assertEquals(
            listOf(
                SpatialDiagnostic(
                    subject = "Quality snapshot on Sheet sheet:main",
                    problem = "Density value -0.1 is negative",
                    correction = "Compute Density from the owning Sheet Drawing Area without clamping or coercion.",
                    sourceTrace = valid.quality.sourceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Quality snapshot on Sheet sheet:main",
                    problem = "Occupancy value NaN is not finite",
                    correction = "Compute Occupancy from the owning Sheet Drawing Area without clamping or coercion.",
                    sourceTrace = valid.quality.sourceTrace,
                ),
            ),
            result.diagnostics,
        )
    }

    @Test
    fun `every non finite quality ratio receives deterministic validation`() {
        val valid = validSheet()
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            val densityResult = SpatialValidation.validate(
                SpatialDocument(
                    listOf(valid.copy(quality = valid.quality.copy(metrics = valid.quality.metrics.copy(density = value)))),
                ),
            )
            val occupancyResult = SpatialValidation.validate(
                SpatialDocument(
                    listOf(valid.copy(quality = valid.quality.copy(metrics = valid.quality.metrics.copy(occupancy = value)))),
                ),
            )

            assertEquals("Density value $value is not finite", densityResult.diagnostics.single().problem)
            assertEquals("Occupancy value $value is not finite", occupancyResult.diagnostics.single().problem)
        }
    }

    @Test
    fun `alignment order may differ while exact grouping membership remains valid`() {
        val valid = validSheet()
        val regionSource = SpatialAlignmentSource.Region(valid.regions.single().regionId)
        val reordered = valid.alignments.map { alignment ->
            if (alignment.constraintSource == regionSource) {
                SpatialAlignment(
                    alignmentId = alignment.alignmentId,
                    sheetId = alignment.sheetId,
                    constraintSource = alignment.constraintSource,
                    occurrenceIds = alignment.occurrenceIds.reversed(),
                    sourceTrace = alignment.sourceTrace,
                )
            } else {
                alignment
            }
        }

        val result = SpatialValidation.validate(SpatialDocument(listOf(valid.copy(alignments = reordered))))

        assertTrue(result.isValid)
        assertEquals(emptyList(), result.diagnostics)
    }

    @Test
    fun `incomplete fact specific Source Traces accumulate as typed diagnostics`() {
        val valid = validSheet()
        val sheetTrace = trace("sheet:foreign")
        val gridTrace = trace(valid.sheetId)
        val sourceOccurrenceTrace = trace(valid.sheetId, "region:main")
        val sourceOccurrence = valid.occurrences.first().copy(sourceTrace = sourceOccurrenceTrace)
        val occurrences = listOf(sourceOccurrence, valid.occurrences.last())
        val regionTrace = trace(valid.sheetId, valid.regions.single().regionId.projectionId)
        val region = valid.regions.single().let { original ->
            SpatialRegionGeometry(
                original.regionId,
                original.sheetId,
                original.memberOccurrenceIds,
                original.bounds,
                regionTrace,
            )
        }
        val constructTrace = trace(valid.sheetId, valid.constructs.single().constructId.projectionId)
        val construct = valid.constructs.single().let { original ->
            SpatialConstructGeometry(
                original.constructId,
                original.sheetId,
                original.kind,
                original.name,
                original.memberOccurrenceIds,
                original.envelope,
                constructTrace,
            )
        }
        val alignmentTrace = trace(valid.sheetId, "alignment:wrong")
        val alignments = valid.alignments.map { original ->
            val sourceTrace = when (original.constraintSource) {
                is SpatialAlignmentSource.Region -> alignmentTrace
                is SpatialAlignmentSource.Construct -> constructTrace
            }
            SpatialAlignment(
                original.alignmentId,
                original.sheetId,
                original.constraintSource,
                original.occurrenceIds,
                sourceTrace,
            )
        }
        val anchorTrace = trace(valid.sheetId, sourceOccurrence.occurrenceId.projectionId)
        val anchors = listOf(valid.anchors.first().copy(sourceTrace = anchorTrace), valid.anchors.last())
        val routeTrace = trace(valid.sheetId, valid.routes.single().routeId.projectionConnectionId)
        val route = valid.routes.single().copy(sourceTrace = routeTrace)
        val gridReferenceTrace = trace(valid.sheetId, "grid-reference:wrong")
        val gridReferences = valid.gridReferences.mapIndexed { index, reference ->
            when (index) {
                0 -> reference.copy(sourceTrace = sourceOccurrenceTrace)
                1 -> reference.copy(sourceTrace = gridReferenceTrace)
                else -> reference.copy(sourceTrace = constructTrace)
            }
        }
        val qualityTrace = trace(valid.sheetId)
        val quality = SpatialQualitySnapshot(
            valid.quality.qualitySnapshotId,
            valid.quality.sheetId,
            valid.quality.metrics,
            qualityTrace,
        )
        val invalid = valid.copy(
            grid = valid.grid.copy(sourceTrace = gridTrace),
            occurrences = occurrences,
            regions = listOf(region),
            constructs = listOf(construct),
            alignments = alignments,
            anchors = anchors,
            routes = listOf(route),
            gridReferences = gridReferences,
            quality = quality,
            sourceTrace = sheetTrace,
        )

        val result = SpatialValidation.validate(SpatialDocument(listOf(invalid)))

        assertEquals(
            listOf(
                SpatialDiagnostic(
                    "Alignment Region region:main on Sheet sheet:main",
                    "Source Trace does not equal its grouping source trace",
                    "Retain the exact Region or Construct Source Trace in its alignment fact.",
                    alignmentTrace,
                ),
                SpatialDiagnostic(
                    "Anchor ${anchors.first().anchorId.value}",
                    "Source Trace does not retain Sheet, Occurrence, and port identity",
                    "Retain Sheet sheet:main, Occurrence occurrence:source, and port port:source in that order.",
                    anchorTrace,
                ),
                SpatialDiagnostic(
                    "Construct construct:main on Sheet sheet:main",
                    "Source Trace does not retain Sheet, Construct, and exact member Occurrences",
                    "Retain Sheet sheet:main, Construct construct:main, and every member Occurrence in source order.",
                    constructTrace,
                ),
                SpatialDiagnostic(
                    "Grid Reference Occurrence occurrence:target on Sheet sheet:main",
                    "Source Trace does not equal its geometry subject trace",
                    "Retain the exact Occurrence or Construct Source Trace in its Grid Reference.",
                    gridReferenceTrace,
                ),
                SpatialDiagnostic(
                    "Grid grid:main on Sheet sheet:main",
                    "Source Trace does not retain Sheet and grid identity",
                    "Retain Sheet sheet:main followed by grid grid:main.",
                    gridTrace,
                ),
                SpatialDiagnostic(
                    "Occurrence occurrence:source on Sheet sheet:main",
                    "Source Trace does not retain Sheet, Region, and Occurrence identity",
                    "Retain Sheet sheet:main, Region region:main, and Occurrence occurrence:source in that order.",
                    sourceOccurrenceTrace,
                ),
                SpatialDiagnostic(
                    "Quality snapshot on Sheet sheet:main",
                    "Source Trace does not retain every contributing Spatial fact identity",
                    "Rebuild the quality trace from the complete validated Sheet fact set.",
                    qualityTrace,
                ),
                SpatialDiagnostic(
                    "Region region:main on Sheet sheet:main",
                    "Source Trace does not retain Sheet, Region, and exact member Occurrences",
                    "Retain Sheet sheet:main, Region region:main, and every member Occurrence in source order.",
                    regionTrace,
                ),
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "Source Trace does not retain Sheet, Connection, and endpoint occurrence-port order",
                    "Publish the six required Route trace positions in source-to-target order, including repeats.",
                    routeTrace,
                ),
                SpatialDiagnostic(
                    "Sheet sheet:main",
                    "Source Trace does not start with its owning Sheet identity",
                    "Retain Sheet sheet:main as the first Source Trace Projection identity.",
                    sheetTrace,
                ),
            ),
            result.diagnostics,
        )
    }

    @Test
    fun `required facts are validated independently per Sheet`() {
        val valid = validSheet()
        val empty = validSheet("sheet:empty").copy(
            occurrences = emptyList(),
            regions = emptyList(),
            constructs = emptyList(),
            alignments = emptyList(),
            anchors = emptyList(),
            lanes = emptyList(),
            routes = emptyList(),
            gridReferences = emptyList(),
        )

        val result = SpatialValidation.validate(SpatialDocument(listOf(valid, empty)))

        assertEquals(
            listOf(
                SpatialDiagnostic(
                    subject = "Quality snapshot on Sheet sheet:empty",
                    problem = "Source Trace does not retain every contributing Spatial fact identity",
                    correction = "Rebuild the quality trace from the complete validated Sheet fact set.",
                    sourceTrace = empty.quality.sourceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Sheet sheet:empty",
                    problem = "has no Occurrence geometry facts",
                    correction = "Publish every projected Occurrence on Sheet sheet:empty before Presentation.",
                    sourceTrace = empty.sourceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Sheet sheet:empty",
                    problem = "has no Region geometry facts",
                    correction = "Publish every projected Region on Sheet sheet:empty before Presentation.",
                    sourceTrace = empty.sourceTrace,
                ),
            ),
            result.diagnostics,
        )
    }

    @Test
    fun `cross fact root ownership and containment defects accumulate instead of throwing`() {
        val valid = validSheet()
        val foreign = validSheet("sheet:foreign")
        val invalid = valid.copy(
            extent = SpatialRect(0, 0, 100, 100),
            grid = foreign.grid,
            quality = foreign.quality,
        )

        val result = SpatialValidation.validate(SpatialDocument(listOf(invalid)))

        assertEquals(
            listOf(
                SpatialDiagnostic(
                    "Grid grid:main on Sheet sheet:main",
                    "Source Trace does not retain Sheet and grid identity",
                    "Retain Sheet sheet:main followed by grid grid:main.",
                    foreign.grid.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Quality snapshot on Sheet sheet:main",
                    "Source Trace does not retain every contributing Spatial fact identity",
                    "Rebuild the quality trace from the complete validated Sheet fact set.",
                    foreign.quality.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Sheet sheet:main",
                    "Drawing Area (40,60,1120,640) is outside extent (0,0,100,100)",
                    "Keep the complete Drawing Area inside the owning Sheet extent.",
                    valid.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Sheet sheet:main",
                    "contains grid grid:main owned by Sheet sheet:foreign",
                    "Keep every Spatial fact inside its exact owning Sheet root.",
                    SpatialSourceTrace(
                        projectionIds = foreign.grid.sourceTrace.projectionIds + valid.sourceTrace.projectionIds,
                        geometryElementIds = foreign.grid.sourceTrace.geometryElementIds + valid.sourceTrace.geometryElementIds,
                    ),
                ),
                SpatialDiagnostic(
                    "Sheet sheet:main",
                    "contains quality snapshot owned by Sheet sheet:foreign",
                    "Keep every Spatial fact inside its exact owning Sheet root.",
                    SpatialSourceTrace(
                        projectionIds = foreign.quality.sourceTrace.projectionIds + valid.sourceTrace.projectionIds,
                        geometryElementIds = foreign.quality.sourceTrace.geometryElementIds + valid.sourceTrace.geometryElementIds,
                    ),
                ),
            ),
            result.diagnostics,
        )
    }

    @Test
    fun `duplicate Sheet identity reports one exact diagnostic`() {
        val sheet = validSheet()

        val result = SpatialValidation.validate(SpatialDocument(listOf(sheet, sheet)))

        assertEquals(
            listOf(
                SpatialDiagnostic(
                    "Sheet sheet:main",
                    "has 2 facts with the same identity",
                    "Publish exactly one Spatial Sheet for each Projection Sheet.",
                    sheet.sourceTrace,
                ),
            ),
            result.diagnostics,
        )
    }

    @Test
    fun `duplicate geometry and alignment identities report complete dependent diagnostics`() {
        val valid = validSheet()
        val source = valid.occurrences.first()
        val target = valid.occurrences.last()
        val region = valid.regions.single()
        val construct = valid.constructs.single()
        val regionAlignment = valid.alignments.first()

        val cases = listOf(
            valid.copy(occurrences = listOf(source, source, target)) to listOf(
                SpatialDiagnostic(
                    "Alignment Construct construct:main on Sheet sheet:main",
                    "member Occurrence occurrence:source resolves to 2 geometry facts",
                    "Reference one existing same-Sheet Occurrence geometry fact.",
                    construct.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Alignment Region region:main on Sheet sheet:main",
                    "member Occurrence occurrence:source resolves to 2 geometry facts",
                    "Reference one existing same-Sheet Occurrence geometry fact.",
                    region.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Anchor ${valid.anchors.first().anchorId.value}",
                    "subject Occurrence occurrence:source resolves to 2 geometry facts",
                    "Reference one existing same-Sheet Occurrence from the Anchor.",
                    valid.anchors.first().sourceTrace,
                ),
                SpatialDiagnostic(
                    "Construct construct:main on Sheet sheet:main",
                    "member Occurrence occurrence:source resolves to 2 geometry facts",
                    "Reference one existing same-Sheet Occurrence geometry fact.",
                    construct.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Grid Reference Occurrence occurrence:source on Sheet sheet:main",
                    "does not resolve one same-Sheet geometry subject",
                    "Reference one existing Occurrence or Construct on the owning Sheet.",
                    source.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Occurrence occurrence:source on Sheet sheet:main",
                    "has 2 facts with the same identity",
                    "Publish exactly one Occurrence geometry fact for this Sheet-qualified identity.",
                    source.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Region region:main on Sheet sheet:main",
                    "member Occurrence occurrence:source resolves to 2 geometry facts",
                    "Reference one existing same-Sheet Occurrence geometry fact.",
                    region.sourceTrace,
                ),
            ),
            valid.copy(regions = listOf(region, region)) to listOf(
                SpatialDiagnostic(
                    "Alignment Region region:main on Sheet sheet:main",
                    "does not resolve one same-Sheet grouping source",
                    "Reference one existing Region or Construct from the alignment.",
                    regionAlignment.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Occurrence occurrence:source on Sheet sheet:main",
                    "does not resolve one reciprocal Region membership region:main",
                    "List the Occurrence exactly once in its declared same-Sheet Region.",
                    source.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Occurrence occurrence:target on Sheet sheet:main",
                    "does not resolve one reciprocal Region membership region:main",
                    "List the Occurrence exactly once in its declared same-Sheet Region.",
                    target.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Region region:main on Sheet sheet:main",
                    "has 2 facts with the same identity",
                    "Publish exactly one Region geometry fact for this Sheet-qualified identity.",
                    region.sourceTrace,
                ),
            ),
            valid.copy(constructs = listOf(construct, construct)) to listOf(
                SpatialDiagnostic(
                    "Alignment Construct construct:main on Sheet sheet:main",
                    "does not resolve one same-Sheet grouping source",
                    "Reference one existing Region or Construct from the alignment.",
                    valid.alignments.last().sourceTrace,
                ),
                SpatialDiagnostic(
                    "Construct construct:main on Sheet sheet:main",
                    "has 2 facts with the same identity",
                    "Publish exactly one Construct geometry fact for this Sheet-qualified identity.",
                    construct.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Grid Reference Construct construct:main on Sheet sheet:main",
                    "does not resolve one same-Sheet geometry subject",
                    "Reference one existing Occurrence or Construct on the owning Sheet.",
                    construct.sourceTrace,
                ),
            ),
            valid.copy(alignments = listOf(regionAlignment, regionAlignment, valid.alignments.last())) to listOf(
                SpatialDiagnostic(
                    "Alignment Region region:main on Sheet sheet:main",
                    "has 2 alignment facts",
                    "Publish exactly one alignment for each Region and Construct source.",
                    region.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Alignment Region region:main on Sheet sheet:main",
                    "has 2 facts with the same identity",
                    "Publish exactly one alignment fact for this Sheet-qualified grouping source.",
                    regionAlignment.sourceTrace,
                ),
            ),
        )

        cases.forEach { (sheet, expected) ->
            assertEquals(expected, SpatialValidation.validate(SpatialDocument(listOf(sheet))).diagnostics)
        }
    }

    @Test
    fun `duplicate Anchor Route and Lane identities report complete reciprocal diagnostics`() {
        val valid = validSheet()
        val sourceAnchor = valid.anchors.first()
        val route = valid.routes.single()
        val lane = valid.lanes.single()
        val cases = listOf(
            valid.copy(anchors = listOf(sourceAnchor, sourceAnchor, valid.anchors.last())) to listOf(
                SpatialDiagnostic(
                    "Anchor ${sourceAnchor.anchorId.value}",
                    "has 2 facts with the same identity",
                    "Publish exactly one Anchor for each referenced same-Sheet occurrence-port.",
                    sourceAnchor.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "does not resolve both endpoint Anchors exactly once",
                    "Publish both exact typed endpoint Anchors before routing.",
                    route.sourceTrace,
                ),
            ),
            valid.copy(routes = listOf(route, route)) to listOf(
                SpatialDiagnostic(
                    "Lane ${lane.laneId.value}",
                    "references duplicate Route ${route.routeId.value}",
                    "List one canonical same-Sheet Route identity in its owning Lane.",
                    valid.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "has 2 facts with the same identity",
                    "Publish exactly one Route for each visible Projection Connection.",
                    route.sourceTrace,
                ),
            ),
            valid.copy(lanes = listOf(lane, lane)) to listOf(
                SpatialDiagnostic(
                    "Lane ${lane.laneId.value}",
                    "has 2 facts with the same identity",
                    "Publish exactly one Lane for each used routing channel.",
                    valid.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "Lane ${lane.laneId.value} does not resolve exactly once",
                    "Publish one same-Sheet used Lane for the Route's lane identity.",
                    route.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "appears in 2 Lane membership lists",
                    "List the Route exactly once in the one Lane named by its lane identity.",
                    route.sourceTrace,
                ),
            ),
        )

        cases.forEach { (sheet, expected) ->
            assertEquals(expected, SpatialValidation.validate(SpatialDocument(listOf(sheet))).diagnostics)
        }
    }

    @Test
    fun `independent geometry route and grid defects accumulate canonically`() {
        val valid = validSheet()
        val first = valid.occurrences.first()
        val moved = first.copy(rectangle = SpatialRect(10, 10, 80, 40))
        val route = valid.routes.single().copy(
            points = listOf(
                valid.anchors.first().point,
                SpatialPoint(220, 160),
                valid.anchors.last().point,
            ),
        )
        val occurrenceReference = valid.gridReferences[1]
        val wrongReference = occurrenceReference.copy(
            rowIndex = 1,
            rowLabel = "B",
            columnIndex = 1,
            columnNumber = 2,
            cellReference = "B2",
        )
        val invalid = valid.copy(
            occurrences = listOf(moved, valid.occurrences.last()),
            routes = listOf(route),
            gridReferences = listOf(valid.gridReferences.first(), wrongReference) + valid.gridReferences.drop(2),
        )

        val result = SpatialValidation.validate(SpatialDocument(listOf(invalid)))

        assertFalse(result.isValid)
        assertEquals(
            listOf(
                SpatialDiagnostic(
                    subject = "Anchor ${valid.anchors.first().anchorId.value}",
                    problem = "point (160,120) is not strictly on declared right boundary",
                    correction = "Place the Anchor on the declared Occurrence boundary side away from its corners.",
                    sourceTrace = valid.anchors.first().sourceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Construct construct:main on Sheet sheet:main",
                    problem = "does not contain member Occurrence occurrence:source",
                    correction = "Derive the Construct envelope from the complete member rectangle union.",
                    sourceTrace = valid.constructs.single().sourceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Grid Reference Occurrence occurrence:target on Sheet sheet:main",
                    problem = "publishes cell B2 but its subject center maps to A2",
                    correction = "Map the subject center with the owning Sheet grid and publish cell A2.",
                    sourceTrace = occurrenceReference.sourceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Lane ${valid.lanes.single().laneId.value}",
                    problem = "member Route ${route.routeId.value} has no segment on this horizontal channel",
                    correction = "Derive Lane membership from a Route segment using the same orientation and coordinate.",
                    sourceTrace = route.sourceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Occurrence occurrence:source on Sheet sheet:main",
                    problem = "rectangle is outside Drawing Area (40,60,1120,640)",
                    correction = "Place the complete Occurrence rectangle inside its owning Sheet Drawing Area.",
                    sourceTrace = moved.sourceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Region region:main on Sheet sheet:main",
                    problem = "does not contain member Occurrence occurrence:source",
                    correction = "Derive the Region bounds from the complete member rectangle union.",
                    sourceTrace = valid.regions.single().sourceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Route ${route.routeId.value}",
                    problem = "contains nonpositive or non-orthogonal segment 0",
                    correction = "Publish only positive horizontal or vertical Route segments.",
                    sourceTrace = route.sourceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Route ${route.routeId.value}",
                    problem = "contains nonpositive or non-orthogonal segment 1",
                    correction = "Publish only positive horizontal or vertical Route segments.",
                    sourceTrace = route.sourceTrace,
                ),
            ),
            result.diagnostics,
        )
    }

    @Test
    fun `grouping membership and alignment defects accumulate exactly`() {
        val valid = validSheet()
        val sourceId = valid.occurrences.first().occurrenceId
        val targetId = valid.occurrences.last().occurrenceId
        val missingId = SpatialOccurrenceId(valid.sheetId, "occurrence:missing")
        val originalRegion = valid.regions.single()
        val region = SpatialRegionGeometry(
            originalRegion.regionId,
            originalRegion.sheetId,
            listOf(sourceId, sourceId),
            originalRegion.bounds,
            originalRegion.sourceTrace,
        )
        val originalConstruct = valid.constructs.single()
        val construct = SpatialConstructGeometry(
            originalConstruct.constructId,
            originalConstruct.sheetId,
            originalConstruct.kind,
            originalConstruct.name,
            listOf(sourceId, missingId),
            originalConstruct.envelope,
            originalConstruct.sourceTrace,
        )

        val result = SpatialValidation.validate(
            SpatialDocument(listOf(valid.copy(regions = listOf(region), constructs = listOf(construct)))),
        )

        assertEquals(
            listOf(
                SpatialDiagnostic(
                    "Alignment Construct construct:main on Sheet sheet:main",
                    "members do not equal its grouping source members",
                    "Preserve the exact grouping membership in the alignment fact.",
                    originalConstruct.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Alignment Region region:main on Sheet sheet:main",
                    "members do not equal its grouping source members",
                    "Preserve the exact grouping membership in the alignment fact.",
                    originalRegion.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Construct construct:main on Sheet sheet:main",
                    "Source Trace does not retain Sheet, Construct, and exact member Occurrences",
                    "Retain Sheet sheet:main, Construct construct:main, and every member Occurrence in source order.",
                    originalConstruct.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Construct construct:main on Sheet sheet:main",
                    "member Occurrence occurrence:missing resolves to 0 geometry facts",
                    "Reference one existing same-Sheet Occurrence geometry fact.",
                    originalConstruct.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Occurrence ${sourceId.projectionId} on Sheet sheet:main",
                    "does not resolve one reciprocal Region membership region:main",
                    "List the Occurrence exactly once in its declared same-Sheet Region.",
                    valid.occurrences.first().sourceTrace,
                ),
                SpatialDiagnostic(
                    "Occurrence ${targetId.projectionId} on Sheet sheet:main",
                    "does not resolve one reciprocal Region membership region:main",
                    "List the Occurrence exactly once in its declared same-Sheet Region.",
                    valid.occurrences.last().sourceTrace,
                ),
                SpatialDiagnostic(
                    "Region region:main on Sheet sheet:main",
                    "Source Trace does not retain Sheet, Region, and exact member Occurrences",
                    "Retain Sheet sheet:main, Region region:main, and every member Occurrence in source order.",
                    originalRegion.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Region region:main on Sheet sheet:main",
                    "repeats member Occurrence ${sourceId.projectionId}",
                    "List each same-Sheet Occurrence member exactly once.",
                    originalRegion.sourceTrace,
                ),
            ),
            result.diagnostics,
        )
    }

    @Test
    fun `Grid Reference ownership range target and cell defects accumulate exactly`() {
        val valid = validSheet()
        val sourceReference = valid.gridReferences[0].copy(
            gridId = "grid:wrong",
            rowIndex = 4,
            rowLabel = "E",
            cellReference = "E1",
        )
        val constructReference = valid.gridReferences[2]
        val missingSubject = SpatialGridReferenceSubject.Occurrence(
            SpatialOccurrenceId(valid.sheetId, "occurrence:missing"),
        )
        val extraReference = valid.gridReferences[1].copy(
            gridReferenceId = SpatialGridReferenceId(valid.sheetId, missingSubject),
            subject = missingSubject,
            sourceTrace = valid.occurrences.first().sourceTrace,
        )
        val invalid = valid.copy(
            gridReferences = listOf(sourceReference, constructReference, constructReference, extraReference),
        )

        val result = SpatialValidation.validate(SpatialDocument(listOf(invalid)))

        assertEquals(
            listOf(
                SpatialDiagnostic(
                    "Grid Reference Construct construct:main on Sheet sheet:main",
                    "has 2 Grid Reference facts",
                    "Publish exactly one owning-Sheet Grid Reference for this subject.",
                    valid.constructs.single().sourceTrace,
                ),
                SpatialDiagnostic(
                    "Grid Reference Construct construct:main on Sheet sheet:main",
                    "has 2 facts with the same identity",
                    "Publish exactly one Grid Reference for this Sheet-qualified subject.",
                    constructReference.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Grid Reference Occurrence occurrence:missing on Sheet sheet:main",
                    "does not resolve one same-Sheet geometry subject",
                    "Reference one existing Occurrence or Construct on the owning Sheet.",
                    extraReference.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Grid Reference Occurrence occurrence:source on Sheet sheet:main",
                    "cell E1 is outside the owning 3x4 grid",
                    "Publish a row and column inside the owning Sheet grid.",
                    sourceReference.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Grid Reference Occurrence occurrence:source on Sheet sheet:main",
                    "names grid grid:wrong instead of owning grid grid:main",
                    "Map the subject with its owning Sheet grid grid:main.",
                    sourceReference.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Grid Reference Occurrence occurrence:source on Sheet sheet:main",
                    "publishes cell E1 but its subject center maps to A1",
                    "Map the subject center with the owning Sheet grid and publish cell A1.",
                    sourceReference.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Grid Reference Occurrence occurrence:target on Sheet sheet:main",
                    "has 0 Grid Reference facts",
                    "Publish exactly one owning-Sheet Grid Reference for this subject.",
                    valid.occurrences.last().sourceTrace,
                ),
            ),
            result.diagnostics,
        )
    }

    @Test
    fun `Route and Lane membership is exact reciprocal and Sheet local`() {
        val valid = validSheet()
        val route = valid.routes.single()
        val phantomRouteId = SpatialRouteId(valid.sheetId, "connection:phantom")
        val lane = valid.lanes.single().copy(routeIds = listOf(phantomRouteId))

        val result = SpatialValidation.validate(
            SpatialDocument(listOf(valid.copy(lanes = listOf(lane)))),
        )

        assertEquals(
            listOf(
                SpatialDiagnostic(
                    subject = "Lane ${lane.laneId.value}",
                    problem = "references missing Route ${phantomRouteId.value}",
                    correction = "List each existing same-Sheet Route exactly once in its owning Lane.",
                    sourceTrace = valid.sourceTrace,
                ),
                SpatialDiagnostic(
                    subject = "Route ${route.routeId.value}",
                    problem = "appears in 0 Lane membership lists",
                    correction = "List the Route exactly once in the one Lane named by its lane identity.",
                    sourceTrace = route.sourceTrace,
                ),
            ),
            result.diagnostics,
        )
    }

    @Test
    fun `Anchor Route obstacle and Lane relationship defects report exactly`() {
        val valid = validSheet()
        val source = valid.anchors.first()
        val target = valid.anchors.last()
        val route = valid.routes.single()
        val lane = valid.lanes.single()
        val missingAnchorSheet = valid.copy(anchors = listOf(target)).withCurrentQualityTrace()
        val reversedRoute = route.copy(
            sourceAnchorId = target.anchorId,
            targetAnchorId = source.anchorId,
            points = listOf(target.point, source.point),
        )
        val endpointMismatch = route.copy(
            points = listOf(SpatialPoint(source.point.x, 140), SpatialPoint(target.point.x, 140)),
        )
        val zeroLength = route.copy(points = listOf(source.point, source.point, target.point))
        val outside = route.copy(points = listOf(source.point, SpatialPoint(20, source.point.y), target.point))
        val otherLaneId = SpatialLaneId(valid.sheetId, SpatialLaneOrientation.HORIZONTAL, 180)
        val wrongLaneRoute = route.copy(laneId = otherLaneId)
        val obstacle = occurrence(valid.sheetId, "occurrence:obstacle", 240, 100)
        val obstacleSheet = valid.withObstacle(obstacle)
        val obstacleTrace = SpatialSourceTrace(
            projectionIds = route.sourceTrace.projectionIds + obstacle.sourceTrace.projectionIds,
            geometryElementIds = route.sourceTrace.geometryElementIds + obstacle.sourceTrace.geometryElementIds,
        )
        val cases = listOf(
            missingAnchorSheet to listOf(
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "does not resolve both endpoint Anchors exactly once",
                    "Publish both exact typed endpoint Anchors before routing.",
                    route.sourceTrace,
                ),
            ),
            valid.copy(routes = listOf(reversedRoute)) to listOf(
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "Source Trace does not retain Sheet, Connection, and endpoint occurrence-port order",
                    "Publish the six required Route trace positions in source-to-target order, including repeats.",
                    route.sourceTrace,
                ),
            ),
            valid.copy(routes = listOf(endpointMismatch)) to listOf(
                SpatialDiagnostic(
                    "Lane ${lane.laneId.value}",
                    "member Route ${route.routeId.value} has no segment on this horizontal channel",
                    "Derive Lane membership from a Route segment using the same orientation and coordinate.",
                    route.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "endpoint points do not equal source and target Anchor points",
                    "Preserve exact Anchor points as the first and final ordered Route points.",
                    route.sourceTrace,
                ),
            ),
            valid.copy(routes = listOf(zeroLength)) to listOf(
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "contains nonpositive or non-orthogonal segment 0",
                    "Publish only positive horizontal or vertical Route segments.",
                    route.sourceTrace,
                ),
            ),
            valid.copy(routes = listOf(outside)) to listOf(
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "contains a point outside Drawing Area (40,60,1120,640)",
                    "Keep every Route point inside the Route's owning Sheet Drawing Area.",
                    route.sourceTrace,
                ),
            ),
            obstacleSheet to listOf(
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "segment 0 enters non-endpoint Occurrence occurrence:obstacle interior",
                    "Keep every Route segment outside non-endpoint Occurrence interiors.",
                    obstacleTrace,
                ),
            ),
            valid.copy(routes = listOf(wrongLaneRoute)) to listOf(
                SpatialDiagnostic(
                    "Lane ${lane.laneId.value}",
                    "lists Route ${route.routeId.value} owned by Lane ${otherLaneId.value}",
                    "Keep Route and Lane reciprocal identities equal.",
                    route.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "Lane ${otherLaneId.value} does not resolve exactly once",
                    "Publish one same-Sheet used Lane for the Route's lane identity.",
                    route.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "is listed by Lane ${lane.laneId.value} instead of ${otherLaneId.value}",
                    "List the Route only in the Lane named by its lane identity.",
                    route.sourceTrace,
                ),
            ),
        )

        cases.forEach { (sheet, expected) ->
            assertEquals(expected, SpatialValidation.validate(SpatialDocument(listOf(sheet))).diagnostics)
        }
    }

    @Test
    fun `invalid Route segment and unresolved Anchor do not suppress independent defects`() {
        val valid = validSheet()
        val source = valid.anchors.first()
        val target = valid.anchors.last()
        val obstacle = occurrence(valid.sheetId, "occurrence:obstacle", 240, 100)
        val obstacleSheet = valid.withObstacle(obstacle)
        val laneId = SpatialLaneId(valid.sheetId, SpatialLaneOrientation.HORIZONTAL, 130)
        val route = valid.routes.single().copy(
            laneId = laneId,
            points = listOf(
                source.point,
                SpatialPoint(source.point.x + 10, 130),
                SpatialPoint(target.point.x, 130),
                target.point,
            ),
            sourceTrace = SpatialSourceTrace(
                projectionIds = listOf(
                    valid.sheetId,
                    "connection:main",
                    "occurrence:wrong",
                    source.anchorId.portId.value,
                    target.anchorId.occurrenceId.projectionId,
                    target.anchorId.portId.value,
                ),
                geometryElementIds = valid.routes.single().sourceTrace.geometryElementIds,
            ),
        )
        val lane = valid.lanes.single().copy(
            laneId = laneId,
            orientation = SpatialLaneOrientation.HORIZONTAL,
            coordinate = 130,
        )
        val invalid = obstacleSheet.copy(routes = listOf(route), lanes = listOf(lane)).withCurrentQualityTrace()
        val obstacleTrace = SpatialSourceTrace(
            projectionIds = route.sourceTrace.projectionIds + obstacle.sourceTrace.projectionIds,
            geometryElementIds = route.sourceTrace.geometryElementIds + obstacle.sourceTrace.geometryElementIds,
        )

        assertEquals(
            listOf(
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "Source Trace does not retain Sheet, Connection, and endpoint occurrence-port order",
                    "Publish the six required Route trace positions in source-to-target order, including repeats.",
                    route.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "contains nonpositive or non-orthogonal segment 0",
                    "Publish only positive horizontal or vertical Route segments.",
                    route.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "segment 1 enters non-endpoint Occurrence occurrence:obstacle interior",
                    "Keep every Route segment outside non-endpoint Occurrence interiors.",
                    obstacleTrace,
                ),
            ),
            SpatialValidation.validate(SpatialDocument(listOf(invalid))).diagnostics,
        )

        val missingSourceAnchor = invalid.copy(anchors = listOf(target)).withCurrentQualityTrace()
        assertEquals(
            listOf(
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "Source Trace does not retain Sheet, Connection, and endpoint occurrence-port order",
                    "Publish the six required Route trace positions in source-to-target order, including repeats.",
                    route.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "contains nonpositive or non-orthogonal segment 0",
                    "Publish only positive horizontal or vertical Route segments.",
                    route.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "does not resolve both endpoint Anchors exactly once",
                    "Publish both exact typed endpoint Anchors before routing.",
                    route.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Route ${route.routeId.value}",
                    "segment 1 enters non-endpoint Occurrence occurrence:obstacle interior",
                    "Keep every Route segment outside non-endpoint Occurrence interiors.",
                    obstacleTrace,
                ),
            ),
            SpatialValidation.validate(SpatialDocument(listOf(missingSourceAnchor))).diagnostics,
        )
    }

    @Test
    fun `Region rejects an Occurrence additionally listed outside its declared Region`() {
        val valid = validSheet()
        val source = valid.occurrences.first()
        val foreignId = SpatialRegionId(valid.sheetId, "region:foreign")
        val foreignTrace = trace(valid.sheetId, foreignId.projectionId, source.occurrenceId.projectionId)
        val foreign = SpatialRegionGeometry(
            regionId = foreignId,
            sheetId = valid.sheetId,
            memberOccurrenceIds = listOf(source.occurrenceId),
            bounds = valid.regions.single().bounds,
            sourceTrace = foreignTrace,
        )
        val invalid = valid.copy(
            regions = valid.regions + foreign,
            alignments = valid.alignments + alignment(
                valid.sheetId,
                SpatialAlignmentSource.Region(foreignId),
                listOf(source),
                foreignTrace,
            ),
        ).withCurrentQualityTrace()

        assertEquals(
            listOf(
                SpatialDiagnostic(
                    "Region ${foreignId.projectionId} on Sheet ${valid.sheetId}",
                    "lists Occurrence ${source.occurrenceId.projectionId} declared for Region ${source.regionId}",
                    "List each Occurrence only in its declared same-Sheet Region.",
                    SpatialSourceTrace(
                        projectionIds = foreignTrace.projectionIds + source.sourceTrace.projectionIds,
                        geometryElementIds = foreignTrace.geometryElementIds + source.sourceTrace.geometryElementIds,
                    ),
                ),
            ),
            SpatialValidation.validate(SpatialDocument(listOf(invalid))).diagnostics,
        )
    }

    @Test
    fun `duplicate facts combine dependent and repeated diagnostic traces canonically`() {
        val valid = validSheet()
        val source = valid.occurrences.first()
        val foreignId = SpatialRegionId(valid.sheetId, "region:foreign")
        val firstTrace = trace(valid.sheetId, foreignId.projectionId, source.occurrenceId.projectionId, "first")
        val secondTrace = trace(valid.sheetId, foreignId.projectionId, source.occurrenceId.projectionId, "second")
        fun foreign(trace: SpatialSourceTrace) = SpatialRegionGeometry(
            foreignId,
            valid.sheetId,
            listOf(source.occurrenceId),
            valid.regions.single().bounds,
            trace,
        )
        val constructAlignment = valid.alignments.single { it.constraintSource is SpatialAlignmentSource.Construct }
        fun document(regions: List<SpatialRegionGeometry>) = SpatialDocument(
            listOf(
                valid.copy(
                    regions = valid.regions + regions,
                    alignments = listOf(constructAlignment),
                ).withCurrentQualityTrace(),
            ),
        )

        val forward = SpatialValidation.validate(document(listOf(foreign(firstTrace), foreign(secondTrace)))).diagnostics
        val reversed = SpatialValidation.validate(document(listOf(foreign(secondTrace), foreign(firstTrace)))).diagnostics
        val combinedForeignTrace = SpatialSourceTrace(
            projectionIds = firstTrace.projectionIds + secondTrace.projectionIds,
            geometryElementIds = firstTrace.geometryElementIds + secondTrace.geometryElementIds,
        )
        val alignmentDiagnostic = forward.single {
            it.subject == "Alignment Region ${foreignId.projectionId} on Sheet ${valid.sheetId}" &&
                it.problem == "has 0 alignment facts"
        }
        val membershipDiagnostics = forward.filter {
            it.subject == "Region ${foreignId.projectionId} on Sheet ${valid.sheetId}" &&
                it.problem.startsWith("lists Occurrence")
        }

        assertEquals(forward, reversed)
        assertEquals(combinedForeignTrace, alignmentDiagnostic.sourceTrace)
        assertEquals(1, membershipDiagnostics.size)
        assertEquals(
            SpatialSourceTrace(
                projectionIds = firstTrace.projectionIds + source.sourceTrace.projectionIds +
                    secondTrace.projectionIds + source.sourceTrace.projectionIds,
                geometryElementIds = firstTrace.geometryElementIds + source.sourceTrace.geometryElementIds +
                    secondTrace.geometryElementIds + source.sourceTrace.geometryElementIds,
            ),
            membershipDiagnostics.single().sourceTrace,
        )
    }

    @Test
    fun `missing Grid Reference combines every duplicate subject trace canonically`() {
        val valid = validSheet()
        val source = valid.occurrences.first()
        val alternateTrace = trace(valid.sheetId, source.regionId, source.occurrenceId.projectionId, "alternate")
        val duplicate = source.copy(sourceTrace = alternateTrace)
        val invalid = valid.copy(
            occurrences = listOf(source, duplicate, valid.occurrences.last()),
            gridReferences = valid.gridReferences.filterNot { reference ->
                reference.subject == SpatialGridReferenceSubject.Occurrence(source.occurrenceId)
            },
        ).withCurrentQualityTrace()
        val reversed = invalid.copy(occurrences = invalid.occurrences.reversed()).withCurrentQualityTrace()
        fun diagnostic(sheet: SpatialSheet) = SpatialValidation.validate(SpatialDocument(listOf(sheet))).diagnostics.single {
            it.subject == "Grid Reference Occurrence ${source.occurrenceId.projectionId} on Sheet ${valid.sheetId}" &&
                it.problem == "has 0 Grid Reference facts"
        }
        val expectedTrace = SpatialSourceTrace(
            projectionIds = source.sourceTrace.projectionIds + alternateTrace.projectionIds,
            geometryElementIds = source.sourceTrace.geometryElementIds + alternateTrace.geometryElementIds,
        )

        assertEquals(expectedTrace, diagnostic(invalid).sourceTrace)
        assertEquals(diagnostic(invalid), diagnostic(reversed))
    }

    @Test
    fun `Lane must be inside Drawing Area and used as a Route segment channel`() {
        val valid = validSheet()
        val laneId = SpatialLaneId(valid.sheetId, SpatialLaneOrientation.HORIZONTAL, 40)
        val route = valid.routes.single().copy(laneId = laneId)
        val lane = valid.lanes.single().copy(
            laneId = laneId,
            orientation = SpatialLaneOrientation.HORIZONTAL,
            coordinate = 40,
        )
        val invalid = valid.copy(routes = listOf(route), lanes = listOf(lane)).withCurrentQualityTrace()

        assertEquals(
            listOf(
                SpatialDiagnostic(
                    "Lane ${laneId.value}",
                    "coordinate 40 is outside Drawing Area (40,60,1120,640)",
                    "Keep every Lane coordinate inside its owning Sheet Drawing Area.",
                    valid.sourceTrace,
                ),
                SpatialDiagnostic(
                    "Lane ${laneId.value}",
                    "member Route ${route.routeId.value} has no segment on this horizontal channel",
                    "Derive Lane membership from a Route segment using the same orientation and coordinate.",
                    route.sourceTrace,
                ),
            ),
            SpatialValidation.validate(SpatialDocument(listOf(invalid))).diagnostics,
        )
    }

    @Test
    fun `diagnostic equality ignores unordered Sheet and fact input order`() {
        val first = validSheet("sheet:a")
        val second = validSheet("sheet:b").copy(
            occurrences = emptyList(),
            regions = emptyList(),
            constructs = emptyList(),
            alignments = emptyList(),
            anchors = emptyList(),
            lanes = emptyList(),
            routes = emptyList(),
            gridReferences = emptyList(),
        )
        val forward = SpatialDocument(listOf(first, second))
        val reversed = SpatialDocument(
            listOf(
                second.copy(
                    constructs = second.constructs.reversed(),
                    alignments = second.alignments.reversed(),
                    anchors = second.anchors.reversed(),
                    lanes = second.lanes.reversed(),
                    routes = second.routes.reversed(),
                    gridReferences = second.gridReferences.reversed(),
                ),
                first.copy(
                    occurrences = first.occurrences.reversed(),
                    regions = first.regions.reversed(),
                    constructs = first.constructs.reversed(),
                    alignments = first.alignments.reversed(),
                    anchors = first.anchors.reversed(),
                    lanes = first.lanes.reversed(),
                    routes = first.routes.reversed(),
                    gridReferences = first.gridReferences.reversed(),
                ),
            ),
        )
        val expected = listOf(
            SpatialDiagnostic(
                "Quality snapshot on Sheet sheet:b",
                "Source Trace does not retain every contributing Spatial fact identity",
                "Rebuild the quality trace from the complete validated Sheet fact set.",
                second.quality.sourceTrace,
            ),
            SpatialDiagnostic(
                "Sheet sheet:b",
                "has no Occurrence geometry facts",
                "Publish every projected Occurrence on Sheet sheet:b before Presentation.",
                second.sourceTrace,
            ),
            SpatialDiagnostic(
                "Sheet sheet:b",
                "has no Region geometry facts",
                "Publish every projected Region on Sheet sheet:b before Presentation.",
                second.sourceTrace,
            ),
        )

        assertEquals(expected, SpatialValidation.validate(forward).diagnostics)
        assertEquals(expected, SpatialValidation.validate(reversed).diagnostics)
    }

}
