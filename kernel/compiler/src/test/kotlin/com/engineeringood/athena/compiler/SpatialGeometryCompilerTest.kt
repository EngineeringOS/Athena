package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionConstructId
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionRegion
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetConstruct
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetGrid
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.projection.ProjectionSheetSubject
import com.engineeringood.athena.spatial.SpatialAlignmentId
import com.engineeringood.athena.spatial.SpatialAlignmentSource
import com.engineeringood.athena.spatial.SpatialConstructGeometry
import com.engineeringood.athena.spatial.SpatialConstructId
import com.engineeringood.athena.spatial.SpatialDocument
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialPlacementReason
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialRegionGeometry
import com.engineeringood.athena.spatial.SpatialRegionId
import com.engineeringood.athena.spatial.SpatialSourceTrace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SpatialGeometryCompilerTest {
    @Test
    fun `region and construct use exact padded unions of positioned member rectangles`() {
        val fixture = groupingFixture()

        val result = SpatialGeometryCompiler().compile(fixture.projection, fixture.occurrences)

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(
            listOf(
                SpatialRegionGeometry(
                    regionId = SpatialRegionId(SHEET_ID, REGION_ID),
                    sheetId = SHEET_ID,
                    memberOccurrenceIds = listOf(ALPHA_ID, BETA_ID),
                    bounds = SpatialRect(x = 76, y = 76, width = 308, height = 228),
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf(SHEET_ID, REGION_ID, ALPHA_PROJECTION_ID, BETA_PROJECTION_ID),
                        geometryElementIds = listOf(REGION_SOURCE, ALPHA_SOURCE, BETA_SOURCE),
                    ),
                ),
            ),
            result.regions,
        )
        assertEquals(
            listOf(
                SpatialConstructGeometry(
                    constructId = SpatialConstructId(SHEET_ID, CONSTRUCT_ID),
                    sheetId = SHEET_ID,
                    kind = "sequence",
                    name = "Reverse Order",
                    memberOccurrenceIds = listOf(BETA_ID, ALPHA_ID),
                    envelope = SpatialRect(x = 76, y = 76, width = 308, height = 228),
                    sourceTrace = SpatialSourceTrace(
                        projectionIds = listOf(SHEET_ID, CONSTRUCT_ID, BETA_PROJECTION_ID, ALPHA_PROJECTION_ID),
                        geometryElementIds = listOf(CONSTRUCT_SOURCE, BETA_SOURCE, ALPHA_SOURCE),
                    ),
                ),
            ),
            result.constructs,
        )
        assertTrue(result.constructs.single().memberOccurrenceIds.all { memberId ->
            val member = fixture.occurrences.single { occurrence -> occurrence.occurrenceId == memberId }
            member.rectangle.isInside(result.constructs.single().envelope)
        })
    }

    @Test
    fun `alignment facts have exact grouping cardinality source and member order`() {
        val fixture = groupingFixture()

        val alignments = SpatialGeometryCompiler().compile(fixture.projection, fixture.occurrences).alignments

        val regionId = SpatialRegionId(SHEET_ID, REGION_ID)
        val constructId = SpatialConstructId(SHEET_ID, CONSTRUCT_ID)
        assertEquals(2, alignments.size)
        assertEquals(
            SpatialAlignmentId(SHEET_ID, SpatialAlignmentSource.Region(regionId)),
            alignments[0].alignmentId,
        )
        assertEquals(SpatialAlignmentSource.Region(regionId), alignments[0].constraintSource)
        assertEquals(listOf(BETA_ID, ALPHA_ID), alignments[0].occurrenceIds)
        assertEquals(
            SpatialSourceTrace(
                projectionIds = listOf(SHEET_ID, REGION_ID, ALPHA_PROJECTION_ID, BETA_PROJECTION_ID),
                geometryElementIds = listOf(REGION_SOURCE, ALPHA_SOURCE, BETA_SOURCE),
            ),
            alignments[0].sourceTrace,
        )
        assertEquals(
            SpatialAlignmentId(SHEET_ID, SpatialAlignmentSource.Construct(constructId)),
            alignments[1].alignmentId,
        )
        assertEquals(SpatialAlignmentSource.Construct(constructId), alignments[1].constraintSource)
        assertEquals(listOf(BETA_ID, ALPHA_ID), alignments[1].occurrenceIds)
        assertEquals(
            SpatialSourceTrace(
                projectionIds = listOf(SHEET_ID, CONSTRUCT_ID, BETA_PROJECTION_ID, ALPHA_PROJECTION_ID),
                geometryElementIds = listOf(CONSTRUCT_SOURCE, BETA_SOURCE, ALPHA_SOURCE),
            ),
            alignments[1].sourceTrace,
        )
    }

    @Test
    fun `grouping geometry is equal across repeated compilation and unordered input permutations`() {
        val fixture = groupingFixture()
        val compiler = SpatialGeometryCompiler()

        val first = compiler.compile(fixture.projection, fixture.occurrences)
        val repeated = compiler.compile(fixture.projection, fixture.occurrences)
        val permuted = compiler.compile(
            fixture.projection.copy(
                nodes = fixture.projection.nodes.reversed(),
                sheets = fixture.projection.sheets.map { sheet ->
                    sheet.copy(subjects = sheet.subjects.reversed())
                }.reversed(),
            ),
            fixture.occurrences.reversed(),
        )

        assertEquals(first, repeated)
        assertEquals(first, permuted)
    }

    @Test
    fun `successful multi sheet grouping is canonical when unordered inputs are permuted`() {
        val fixture = groupingFixture()
        val gammaSource = GeometryElementId("projection:occurrence:Gamma")
        val gamma = node("Gamma", "engineering-projection/sheet/secondary/occurrence/Gamma", gammaSource)
        val secondarySheetId = ProjectionSheetId("engineering-projection/sheet/secondary")
        val secondaryRegion = ProjectionRegion(
            "engineering-projection/secondary/region",
            "Secondary",
            listOf("Gamma"),
            GeometryElementId("projection:region:secondary"),
        )
        val secondaryConstruct = ProjectionSheetConstruct(
            ProjectionConstructId("engineering-projection/secondary/construct"),
            "sequence",
            "Secondary",
            listOf("Gamma"),
            GeometryElementId("projection:construct:secondary"),
        )
        val secondarySubjects = listOf(ProjectionSheetSubject(gamma.semanticId, nodeIds = listOf(gamma.projectionId)))
        val secondarySheet = ProjectionSheet(
            sheetId = secondarySheetId,
            displayName = "Secondary",
            order = 1,
            subjects = secondarySubjects,
            regions = listOf(secondaryRegion),
            constructs = listOf(secondaryConstruct),
            originGeometryElementId = GeometryElementId("projection:sheet:secondary"),
            publication = ProjectionSheetPublication.fromProjectionState(
                secondarySheetId,
                "Secondary",
                1,
                secondarySubjects,
            ),
        )
        val gammaOccurrence = occurrence(
            gamma,
            secondaryRegion,
            SpatialRect(500, 300, 90, 50),
            secondarySheetId.value,
        ).copy(
            sourceTrace = SpatialSourceTrace(
                projectionIds = listOf(secondarySheetId.value, secondaryRegion.regionId, gamma.projectionId.value),
                geometryElementIds = listOf(gammaSource),
            ),
        )
        val projection = fixture.projection.copy(
            nodes = fixture.projection.nodes + gamma,
            sheets = fixture.projection.sheets + secondarySheet,
        )
        val occurrences = fixture.occurrences + gammaOccurrence

        val canonical = SpatialGeometryCompiler().compile(projection, occurrences)
        val permuted = SpatialGeometryCompiler().compile(
            projection.copy(nodes = projection.nodes.reversed(), sheets = projection.sheets.reversed()),
            occurrences.reversed(),
        )

        assertEquals(canonical, permuted)
        assertEquals(listOf(REGION_ID, secondaryRegion.regionId), canonical.regions.map { it.regionId.projectionId })
        assertEquals(
            listOf(CONSTRUCT_ID, secondaryConstruct.constructId.value),
            canonical.constructs.map { it.constructId.projectionId },
        )
        assertEquals(listOf(SHEET_ID, secondarySheetId.value), canonical.regions.map(SpatialRegionGeometry::sheetId))
        assertEquals(4, canonical.alignments.size)
    }

    @Test
    fun `empty duplicate and unknown grouping members aggregate in canonical diagnostic order`() {
        val fixture = groupingFixture()
        val constructs = listOf(
            ProjectionSheetConstruct(
                ProjectionConstructId("construct:empty"),
                "sequence",
                "Empty",
                emptyList(),
                GeometryElementId("projection:construct:empty"),
            ),
            ProjectionSheetConstruct(
                ProjectionConstructId("construct:duplicate"),
                "sequence",
                "Duplicate",
                listOf("Alpha", "Alpha"),
                GeometryElementId("projection:construct:duplicate"),
            ),
            ProjectionSheetConstruct(
                ProjectionConstructId("construct:unknown"),
                "sequence",
                "Unknown",
                listOf("Missing"),
                GeometryElementId("projection:construct:unknown"),
            ),
        )
        val emptyRegion = ProjectionRegion("region:empty", "Empty", emptyList(), GeometryElementId("projection:region:empty"))
        val sheet = fixture.projection.sheets.single().copy(
            regions = fixture.projection.sheets.single().regions + emptyRegion,
            constructs = constructs,
        )

        val result = SpatialGeometryCompiler().compile(
            fixture.projection.copy(sheets = listOf(sheet)),
            fixture.occurrences,
        )

        assertNoGeometryFacts(result)
        assertEquals(
            listOf(
                "Construct Duplicate member Alpha" to "is listed more than once",
                "Construct Empty" to "has no members",
                "Construct Unknown member Missing" to "does not resolve to a projected Occurrence",
                "Region Empty" to "has no members",
            ),
            result.diagnostics.map { diagnostic -> diagnostic.subject to diagnostic.problem },
        )
        assertEquals(
            listOf(
                DiagnosticContract(
                    "List Alpha once in Construct Duplicate.",
                    listOf(SHEET_ID, "construct:duplicate", ALPHA_PROJECTION_ID),
                    listOf("projection:construct:duplicate", ALPHA_SOURCE.value),
                ),
                DiagnosticContract(
                    "Add at least one Occurrence to Construct Empty.",
                    listOf(SHEET_ID, "construct:empty"),
                    listOf("projection:construct:empty"),
                ),
                DiagnosticContract(
                    "Name an Occurrence owned by Sheet $SHEET_ID in Construct Unknown.",
                    listOf(SHEET_ID, "construct:unknown"),
                    listOf("projection:construct:unknown"),
                ),
                DiagnosticContract(
                    "Add at least one Occurrence to Region Empty.",
                    listOf(SHEET_ID, "region:empty"),
                    listOf("projection:region:empty"),
                ),
            ),
            result.diagnostics.map { diagnostic -> diagnostic.contract() },
        )
    }

    @Test
    fun `ambiguous cross sheet and duplicate construct identities fail with exact trace`() {
        val fixture = groupingFixture()
        val firstShared = node("Shared", "projection/shared/first", GeometryElementId("origin:shared:first"))
        val secondShared = node("Shared", "projection/shared/second", GeometryElementId("origin:shared:second"))
        val foreign = node("Foreign", "projection/foreign", GeometryElementId("origin:foreign"))
        val primary = fixture.projection.sheets.single()
        val primarySubjects = primary.subjects + listOf(firstShared, secondShared).map { item ->
            ProjectionSheetSubject(item.semanticId, nodeIds = listOf(item.projectionId))
        }
        val duplicateConstructs = listOf(
            ProjectionSheetConstruct(
                ProjectionConstructId("construct:shared"),
                "sequence",
                "Ambiguous",
                listOf("Shared"),
                GeometryElementId("projection:construct:ambiguous"),
            ),
            ProjectionSheetConstruct(
                ProjectionConstructId("construct:shared"),
                "sequence",
                "Foreign",
                listOf("Foreign"),
                GeometryElementId("projection:construct:foreign"),
            ),
        )
        val primarySheet = primary.copy(subjects = primarySubjects, constructs = duplicateConstructs)
        val foreignSheetId = ProjectionSheetId("engineering-projection/sheet/foreign")
        val foreignSubjects = listOf(ProjectionSheetSubject(foreign.semanticId, nodeIds = listOf(foreign.projectionId)))
        val foreignSheet = ProjectionSheet(
            sheetId = foreignSheetId,
            displayName = "Foreign",
            order = 1,
            subjects = foreignSubjects,
            regions = listOf(ProjectionRegion("region:foreign", "Foreign", listOf("Foreign"))),
            publication = ProjectionSheetPublication.fromProjectionState(foreignSheetId, "Foreign", 1, foreignSubjects),
        )
        val projection = fixture.projection.copy(
            nodes = fixture.projection.nodes + firstShared + secondShared + foreign,
            sheets = listOf(primarySheet, foreignSheet),
        )
        val occurrences = fixture.occurrences + listOf(
            occurrence(firstShared, primarySheet.regions.single(), SpatialRect(500, 100, 80, 40)),
            occurrence(secondShared, primarySheet.regions.single(), SpatialRect(500, 200, 80, 40)),
            occurrence(foreign, foreignSheet.regions.single(), SpatialRect(100, 100, 80, 40), foreignSheetId.value),
        )

        val result = SpatialGeometryCompiler().compile(projection, occurrences)

        assertNoGeometryFacts(result)
        assertEquals(
            listOf(
                "Construct Ambiguous member Shared" to "matches 2 projected Occurrences",
                "Construct Foreign member Foreign" to "belongs to Sheet ${foreignSheetId.value}, not Sheet $SHEET_ID",
                "Construct identity construct:shared" to "is used by 2 Constructs on one Sheet",
            ),
            result.diagnostics.map { diagnostic -> diagnostic.subject to diagnostic.problem },
        )
        assertEquals(
            listOf(
                DiagnosticContract(
                    "Reference a unique projected Occurrence in Construct Ambiguous.",
                    listOf(SHEET_ID, "construct:shared", "projection/shared/first", "projection/shared/second"),
                    listOf("projection:construct:ambiguous", "origin:shared:first", "origin:shared:second"),
                ),
                DiagnosticContract(
                    "List Foreign only in a grouping on its owning Sheet.",
                    listOf(SHEET_ID, "construct:shared", "projection/foreign"),
                    listOf("projection:construct:foreign", "origin:foreign"),
                ),
                DiagnosticContract(
                    "Give every Construct on Sheet $SHEET_ID a unique identity.",
                    listOf(SHEET_ID, "construct:shared"),
                    listOf("projection:construct:ambiguous", "projection:construct:foreign"),
                ),
            ),
            result.diagnostics.map { diagnostic -> diagnostic.contract() },
        )
    }

    @Test
    fun `missing occurrence geometry and out of area padding fail without partial facts`() {
        val fixture = groupingFixture()
        val movedAlpha = fixture.occurrences.first().copy(rectangle = SpatialRect(45, 100, 80, 40))

        val result = SpatialGeometryCompiler().compile(
            fixture.projection,
            listOf(movedAlpha),
        )

        assertNoGeometryFacts(result)
        assertEquals(
            listOf(
                "Construct Reverse Order" to "has a 24-unit padded envelope outside Drawing Area (40,60,1120,640)",
                "Construct Reverse Order member Beta" to "has no placed Occurrence geometry",
                "Region Main" to "has a 24-unit padded bound outside Drawing Area (40,60,1120,640)",
                "Region Main member Beta" to "has no placed Occurrence geometry",
            ),
            result.diagnostics.map { diagnostic -> diagnostic.subject to diagnostic.problem },
        )
        assertEquals(
            listOf(
                DiagnosticContract(
                    "Keep every Construct Reverse Order member at least 24 units inside the Drawing Area.",
                    listOf(SHEET_ID, CONSTRUCT_ID, BETA_PROJECTION_ID, ALPHA_PROJECTION_ID),
                    listOf(CONSTRUCT_SOURCE.value, BETA_SOURCE.value, ALPHA_SOURCE.value),
                ),
                DiagnosticContract(
                    "Place Beta on Sheet $SHEET_ID before deriving Construct Reverse Order geometry.",
                    listOf(SHEET_ID, CONSTRUCT_ID, BETA_PROJECTION_ID),
                    listOf(CONSTRUCT_SOURCE.value, BETA_SOURCE.value),
                ),
                DiagnosticContract(
                    "Keep every Region Main member at least 24 units inside the Drawing Area.",
                    listOf(SHEET_ID, REGION_ID, ALPHA_PROJECTION_ID, BETA_PROJECTION_ID),
                    listOf(REGION_SOURCE.value, ALPHA_SOURCE.value, BETA_SOURCE.value),
                ),
                DiagnosticContract(
                    "Place Beta on Sheet $SHEET_ID before deriving Region Main geometry.",
                    listOf(SHEET_ID, REGION_ID, BETA_PROJECTION_ID),
                    listOf(REGION_SOURCE.value, BETA_SOURCE.value),
                ),
            ),
            result.diagnostics.map { diagnostic -> diagnostic.contract() },
        )
    }

    @Test
    fun `unassigned region validates missing and out of area member geometry`() {
        val fixture = groupingFixture()
        val alpha = fixture.projection.nodes.first { node -> node.label == "Alpha" }
        val sheet = fixture.projection.sheets.single().copy(
            subjects = listOf(ProjectionSheetSubject(alpha.semanticId, nodeIds = listOf(alpha.projectionId))),
            regions = emptyList(),
            constructs = emptyList(),
        )
        val projection = fixture.projection.copy(nodes = listOf(alpha), sheets = listOf(sheet))
        val unassignedId = "${sheet.sheetId.value}/region/unassigned"
        val unassignedRegion = ProjectionRegion(unassignedId, "Unassigned", listOf("Alpha"), SHEET_SOURCE)
        val outside = occurrence(alpha, unassignedRegion, SpatialRect(45, 100, 80, 40))

        val missingResult = SpatialGeometryCompiler().compile(projection, emptyList())
        val outsideResult = SpatialGeometryCompiler().compile(projection, listOf(outside))

        assertNoGeometryFacts(missingResult)
        assertEquals(
            "Region Unassigned member Alpha" to "has no placed Occurrence geometry",
            missingResult.diagnostics.single().let { it.subject to it.problem },
        )
        assertEquals(
            DiagnosticContract(
                "Place Alpha on Sheet $SHEET_ID before deriving Region Unassigned geometry.",
                listOf(SHEET_ID, unassignedId, ALPHA_PROJECTION_ID),
                listOf(SHEET_SOURCE.value, ALPHA_SOURCE.value),
            ),
            missingResult.diagnostics.single().contract(),
        )
        assertNoGeometryFacts(outsideResult)
        assertEquals(
            "Region Unassigned" to "has a 24-unit padded bound outside Drawing Area (40,60,1120,640)",
            outsideResult.diagnostics.single().let { it.subject to it.problem },
        )
        assertEquals(
            DiagnosticContract(
                "Keep every Region Unassigned member at least 24 units inside the Drawing Area.",
                listOf(SHEET_ID, unassignedId, ALPHA_PROJECTION_ID),
                listOf(SHEET_SOURCE.value, ALPHA_SOURCE.value),
            ),
            outsideResult.diagnostics.single().contract(),
        )
    }

    @Test
    fun `padded extent overflow returns a structured diagnostic instead of throwing`() {
        val fixture = groupingFixture()
        val alpha = fixture.projection.nodes.first { node -> node.label == "Alpha" }
        val region = ProjectionRegion("region:extreme", "Extreme", listOf("Alpha"), REGION_SOURCE)
        val sheet = fixture.projection.sheets.single().copy(
            subjects = listOf(ProjectionSheetSubject(alpha.semanticId, nodeIds = listOf(alpha.projectionId))),
            regions = listOf(region),
            constructs = emptyList(),
        )
        val projection = fixture.projection.copy(nodes = listOf(alpha), sheets = listOf(sheet))
        val occurrence = occurrence(
            alpha,
            region,
            SpatialRect(Int.MAX_VALUE - 79, 100, 79, 40),
        )

        val result = SpatialGeometryCompiler().compile(projection, listOf(occurrence))

        assertNoGeometryFacts(result)
        assertEquals(
            "Region Extreme" to "has a 24-unit padded bound outside Drawing Area (40,60,1120,640)",
            result.diagnostics.single().let { it.subject to it.problem },
        )
        assertEquals(
            DiagnosticContract(
                "Keep every Region Extreme member at least 24 units inside the Drawing Area.",
                listOf(SHEET_ID, "region:extreme", ALPHA_PROJECTION_ID),
                listOf(REGION_SOURCE.value, ALPHA_SOURCE.value),
            ),
            result.diagnostics.single().contract(),
        )
    }

    @Test
    fun `authored region cannot use compiler owned unassigned identity`() {
        val fixture = groupingFixture()
        val sheet = fixture.projection.sheets.single()
        val reservedId = "${sheet.sheetId.value}/region/unassigned"
        val reservedRegion = ProjectionRegion(
            regionId = reservedId,
            name = "Authored Unassigned",
            occurrenceNames = listOf("Alpha"),
            originGeometryElementId = REGION_SOURCE,
        )
        val projection = fixture.projection.copy(
            sheets = listOf(sheet.copy(regions = listOf(reservedRegion), constructs = emptyList())),
        )

        val result = SpatialGeometryCompiler().compile(projection, fixture.occurrences)

        assertNoGeometryFacts(result)
        val diagnostic = result.diagnostics.single()
        assertEquals("Region identity $reservedId", diagnostic.subject)
        assertEquals("is reserved for compiler-owned Unassigned Region", diagnostic.problem)
        assertEquals(
            "Give the authored Region on Sheet $SHEET_ID a different identity.",
            diagnostic.correction,
        )
        assertEquals(
            SpatialSourceTrace(
                projectionIds = listOf(SHEET_ID, reservedId),
                geometryElementIds = listOf(REGION_SOURCE),
            ),
            diagnostic.sourceTrace,
        )
    }

    @Test
    fun `duplicate occurrence geometry identity fails canonically`() {
        val fixture = groupingFixture()
        val duplicate = fixture.occurrences.first().copy(rectangle = SpatialRect(180, 140, 80, 40))

        val canonical = SpatialGeometryCompiler().compile(
            fixture.projection,
            fixture.occurrences + duplicate,
        )
        val permuted = SpatialGeometryCompiler().compile(
            fixture.projection,
            (fixture.occurrences + duplicate).reversed(),
        )

        assertNoGeometryFacts(canonical)
        assertEquals(canonical, permuted)
        val diagnostic = canonical.diagnostics.single()
        assertEquals("Occurrence Alpha on Sheet $SHEET_ID", diagnostic.subject)
        assertEquals("has 2 placed geometry facts", diagnostic.problem)
        assertEquals(
            "Publish exactly one placed Occurrence geometry for Alpha on Sheet $SHEET_ID.",
            diagnostic.correction,
        )
        assertEquals(
            SpatialSourceTrace(
                projectionIds = listOf(SHEET_ID, ALPHA_PROJECTION_ID),
                geometryElementIds = listOf(ALPHA_SOURCE),
            ),
            diagnostic.sourceTrace,
        )
    }

    @Test
    fun `blank grouping identity and kind return structured diagnostics`() {
        val fixture = groupingFixture()
        val sheet = fixture.projection.sheets.single().copy(
            regions = listOf(ProjectionRegion("", "Blank", listOf("Alpha"), REGION_SOURCE)),
            constructs = listOf(
                ProjectionSheetConstruct(
                    constructId = ProjectionConstructId(""),
                    kind = "",
                    name = "Blank",
                    memberNames = listOf("Alpha"),
                    originGeometryElementId = CONSTRUCT_SOURCE,
                ),
            ),
        )

        val result = SpatialGeometryCompiler().compile(
            fixture.projection.copy(sheets = listOf(sheet)),
            fixture.occurrences,
        )

        assertNoGeometryFacts(result)
        assertEquals(
            listOf(
                "Construct Blank" to "has no kind",
                "Construct identity" to "is blank",
                "Region identity" to "is blank",
            ),
            result.diagnostics.map { diagnostic -> diagnostic.subject to diagnostic.problem },
        )
        assertEquals(
            listOf(
                DiagnosticContract(
                    "Name the engineering grouping kind for Construct Blank.",
                    listOf(SHEET_ID),
                    listOf(CONSTRUCT_SOURCE.value),
                ),
                DiagnosticContract(
                    "Give every Construct on Sheet $SHEET_ID a non-blank identity.",
                    listOf(SHEET_ID),
                    listOf(CONSTRUCT_SOURCE.value),
                ),
                DiagnosticContract(
                    "Give every Region on Sheet $SHEET_ID a non-blank identity.",
                    listOf(SHEET_ID),
                    listOf(REGION_SOURCE.value),
                ),
            ),
            result.diagnostics.map { diagnostic -> diagnostic.contract() },
        )
    }

    @Test
    fun `overlapping membership across distinct constructs remains valid`() {
        val fixture = groupingFixture()
        val sheet = fixture.projection.sheets.single()
        val overlapping = ProjectionSheetConstruct(
            constructId = ProjectionConstructId("construct:overlapping"),
            kind = "inspection",
            name = "Overlapping",
            memberNames = listOf("Alpha"),
            originGeometryElementId = GeometryElementId("projection:construct:overlapping"),
        )

        val result = SpatialGeometryCompiler().compile(
            fixture.projection.copy(sheets = listOf(sheet.copy(constructs = sheet.constructs + overlapping))),
            fixture.occurrences,
        )

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(listOf(CONSTRUCT_ID, "construct:overlapping"), result.constructs.map { construct ->
            construct.constructId.projectionId
        })
        assertEquals(ALPHA_ID, result.constructs.last().memberOccurrenceIds.single())
    }

    @Test
    fun `authored Region and alignment preserve their distinct member orders`() {
        val fixture = groupingFixture()

        val result = SpatialGeometryCompiler().compile(fixture.projection, fixture.occurrences)

        assertTrue(result.diagnostics.isEmpty())
        val region = result.regions.single { candidate -> candidate.regionId.projectionId == REGION_ID }
        val alignment = result.alignments.single { candidate ->
            candidate.constraintSource == SpatialAlignmentSource.Region(region.regionId)
        }
        assertEquals(listOf(ALPHA_ID, BETA_ID), region.memberOccurrenceIds)
        assertEquals(listOf(BETA_ID, ALPHA_ID), alignment.occurrenceIds)
        assertEquals(region.memberOccurrenceIds.toSet(), alignment.occurrenceIds.toSet())
    }

    @Test
    fun `projection spatial compiler is sole active path and publishes typed grouping geometry`() {
        val fixture = groupingFixture()

        val result = ProjectionSpatialCompiler().transform(fixture.projection)

        val output = assertIs<RealityTransformationResult.Success<SpatialDocument>>(result).output
        val spatialSheet = output.sheets.single()
        assertEquals(2, spatialSheet.occurrences.size)
        assertEquals(listOf(REGION_ID), spatialSheet.regions.map { region -> region.regionId.projectionId })
        assertEquals(listOf(CONSTRUCT_ID), spatialSheet.constructs.map { construct -> construct.constructId.projectionId })
        assertEquals(2, spatialSheet.alignments.size)
        val documentFields = SpatialDocument::class.java.declaredFields.map { field -> field.name }
        assertEquals(listOf("sheets"), documentFields)
        assertFalse("placements" in documentFields)
        assertFalse("bounds" in documentFields)
        assertFalse(ProjectionSpatialCompiler::class.java.declaredFields.any { field ->
            field.type.name.contains("ProjectionToSpatialTransformation") ||
                field.type.name.contains("SpatialPlacementCompiler")
        })
    }

    @Test
    fun `projection spatial compiler blocks presentation on grouping diagnostics`() {
        val fixture = groupingFixture()
        val sheet = fixture.projection.sheets.single().copy(
            constructs = listOf(
                ProjectionSheetConstruct(
                    ProjectionConstructId("construct:empty"),
                    "sequence",
                    "Empty",
                    emptyList(),
                    GeometryElementId("projection:construct:empty"),
                ),
            ),
        )

        val result = ProjectionSpatialCompiler().transform(fixture.projection.copy(sheets = listOf(sheet)))

        val failure = assertIs<RealityTransformationResult.Failure>(result)
        assertEquals(1, failure.diagnostics.size)
        val diagnostic = failure.diagnostics.single()
        assertEquals("Construct Empty", diagnostic.subject)
        assertEquals("has no members", diagnostic.problem)
        assertEquals("Add at least one Occurrence to Construct Empty.", diagnostic.correction)
        assertEquals(
            SpatialSourceTrace(
                projectionIds = listOf(SHEET_ID, "construct:empty"),
                geometryElementIds = listOf(GeometryElementId("projection:construct:empty")),
            ),
            diagnostic.sourceTrace,
        )
    }

    private fun groupingFixture(): GroupingFixture {
        val alpha = node("Alpha", ALPHA_PROJECTION_ID, ALPHA_SOURCE)
        val beta = node("Beta", BETA_PROJECTION_ID, BETA_SOURCE)
        val region = ProjectionRegion(
            regionId = REGION_ID,
            name = "Main",
            occurrenceNames = listOf("Alpha", "Beta"),
            originGeometryElementId = REGION_SOURCE,
        )
        val construct = ProjectionSheetConstruct(
            constructId = ProjectionConstructId(CONSTRUCT_ID),
            kind = "sequence",
            name = "Reverse Order",
            memberNames = listOf("Beta", "Alpha"),
            originGeometryElementId = CONSTRUCT_SOURCE,
        )
        val sheetId = ProjectionSheetId(SHEET_ID)
        val subjects = listOf(alpha, beta).map { item ->
            ProjectionSheetSubject(item.semanticId, nodeIds = listOf(item.projectionId))
        }
        val sheet = ProjectionSheet(
            sheetId = sheetId,
            displayName = "Main",
            order = 0,
            subjects = subjects,
            regions = listOf(region),
            constructs = listOf(construct),
            grid = ProjectionSheetGrid("grid:main", rows = 8, columns = 12),
            originGeometryElementId = SHEET_SOURCE,
            publication = ProjectionSheetPublication.fromProjectionState(sheetId, "Main", 0, subjects),
        )
        val projection = ProjectionDocument(
            view = ViewDefinition(id = "engineering-projection", displayName = "Engineering Projection"),
            nodes = listOf(alpha, beta),
            connections = emptyList(),
            sheets = listOf(sheet),
        )
        return GroupingFixture(
            projection = projection,
            occurrences = listOf(
                occurrence(alpha, region, SpatialRect(x = 100, y = 100, width = 80, height = 40)),
                occurrence(beta, region, SpatialRect(x = 260, y = 220, width = 100, height = 60)),
            ),
        )
    }

    private fun node(name: String, projectionId: String, source: GeometryElementId): ProjectionNode = ProjectionNode(
        projectionId = ProjectionNodeId(projectionId),
        semanticId = StableSemanticIdentity("component:$name"),
        label = name,
        originGeometryElementId = source,
    )

    private fun occurrence(
        node: ProjectionNode,
        region: ProjectionRegion,
        rectangle: SpatialRect,
        sheetId: String = SHEET_ID,
    ): SpatialOccurrenceGeometry = SpatialOccurrenceGeometry(
        occurrenceId = SpatialOccurrenceId(sheetId, node.projectionId.value),
        subjectId = node.semanticId,
        sheetId = sheetId,
        regionId = region.regionId,
        rectangle = rectangle,
        placementReason = SpatialPlacementReason(listOf("test placement")),
        sourceTrace = SpatialSourceTrace(
            projectionIds = listOf(SHEET_ID, region.regionId, node.projectionId.value),
            geometryElementIds = listOf(node.originGeometryElementId),
        ),
    )

    private data class GroupingFixture(
        val projection: ProjectionDocument,
        val occurrences: List<SpatialOccurrenceGeometry>,
    )

    private data class DiagnosticContract(
        val correction: String,
        val projectionIds: List<String>,
        val geometryElementIds: List<String>,
    )

    private fun SpatialDiagnostic.contract(): DiagnosticContract = DiagnosticContract(
        correction = correction,
        projectionIds = sourceTrace.projectionIds,
        geometryElementIds = sourceTrace.geometryElementIds.map(GeometryElementId::value),
    )

    private fun assertNoGeometryFacts(result: SpatialGeometryCompilationResult) {
        assertTrue(result.regions.isEmpty())
        assertTrue(result.constructs.isEmpty())
        assertTrue(result.alignments.isEmpty())
    }

    private companion object {
        const val SHEET_ID = "engineering-projection/sheet/main"
        const val REGION_ID = "engineering-projection/main/main-region"
        const val CONSTRUCT_ID = "engineering-projection/main/sequence:reverse"
        const val ALPHA_PROJECTION_ID = "engineering-projection/sheet/main/occurrence/Alpha"
        const val BETA_PROJECTION_ID = "engineering-projection/sheet/main/occurrence/Beta"

        val ALPHA_ID = SpatialOccurrenceId(SHEET_ID, ALPHA_PROJECTION_ID)
        val BETA_ID = SpatialOccurrenceId(SHEET_ID, BETA_PROJECTION_ID)
        val SHEET_SOURCE = GeometryElementId("projection:sheet:main")
        val REGION_SOURCE = GeometryElementId("projection:region:main")
        val CONSTRUCT_SOURCE = GeometryElementId("projection:construct:reverse")
        val ALPHA_SOURCE = GeometryElementId("projection:occurrence:Alpha")
        val BETA_SOURCE = GeometryElementId("projection:occurrence:Beta")
    }
}
