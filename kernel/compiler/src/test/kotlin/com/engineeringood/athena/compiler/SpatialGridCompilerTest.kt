package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.projection.ProjectionSheetGrid
import com.engineeringood.athena.spatial.SpatialConstructGeometry
import com.engineeringood.athena.spatial.SpatialConstructId
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialGridDefinition
import com.engineeringood.athena.spatial.SpatialGridReference
import com.engineeringood.athena.spatial.SpatialGridReferenceId
import com.engineeringood.athena.spatial.SpatialGridReferenceSubject
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialPlacementReason
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialSourceTrace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpatialGridCompilerTest {
    @Test
    fun `owning sheet maps typed occurrence and construct references independently`() {
        val sheetA = sheetInput(
            sheetId = SHEET_A,
            order = 0,
            drawingArea = SpatialRect(0, 0, 100, 100),
            rows = 2,
            columns = 4,
        )
        val sheetB = sheetInput(
            sheetId = SHEET_B,
            order = 1,
            drawingArea = SpatialRect(100, 200, 60, 90),
            rows = 3,
            columns = 3,
        )
        val occurrenceA = occurrence(SHEET_A, "occurrence:a", SpatialRect(0, 0, 25, 100))
        val occurrenceB = occurrence(SHEET_B, "occurrence:b", SpatialRect(140, 230, 20, 30))
        val constructA = construct(SHEET_A, "construct:a", occurrenceA, SpatialRect(40, 10, 20, 30))

        val result = SpatialGridCompiler().compile(
            sheets = listOf(sheetB, sheetA),
            occurrences = listOf(occurrenceB, occurrenceA),
            constructs = listOf(constructA),
        )

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(
            listOf(
                SpatialGridDefinition(
                    sheetId = SHEET_A,
                    gridId = "grid:$SHEET_A",
                    drawingArea = SpatialRect(0, 0, 100, 100),
                    rows = 2,
                    columns = 4,
                    sourceTrace = sheetA.sourceTrace,
                ),
                SpatialGridDefinition(
                    sheetId = SHEET_B,
                    gridId = "grid:$SHEET_B",
                    drawingArea = SpatialRect(100, 200, 60, 90),
                    rows = 3,
                    columns = 3,
                    sourceTrace = sheetB.sourceTrace,
                ),
            ),
            result.grids,
        )
        assertEquals(
            listOf(
                expectedReference(occurrenceA, rowIndex = 1, rowLabel = "B", columnIndex = 0),
                expectedReference(constructA, rowIndex = 0, rowLabel = "A", columnIndex = 2),
                expectedReference(occurrenceB, rowIndex = 1, rowLabel = "B", columnIndex = 2),
            ),
            result.references,
        )
    }

    @Test
    fun `row labels pass Z and exact centers own deterministic boundary cells`() {
        val rowsSheet = sheetInput(
            sheetId = SHEET_A,
            order = 0,
            drawingArea = SpatialRect(0, 0, 10, 280),
            rows = 28,
            columns = 1,
        )
        val rowOccurrences = listOf(
            occurrence(SHEET_A, "row-a", SpatialRect(0, 0, 10, 10)),
            occurrence(SHEET_A, "row-z", SpatialRect(0, 250, 10, 10)),
            occurrence(SHEET_A, "row-aa", SpatialRect(0, 260, 10, 10)),
            occurrence(SHEET_A, "row-ab", SpatialRect(0, 270, 10, 10)),
        )

        val rowResult = SpatialGridCompiler().compile(
            sheets = listOf(rowsSheet),
            occurrences = rowOccurrences,
            constructs = emptyList(),
        )

        assertEquals(
            listOf("A1", "AA1", "AB1", "Z1"),
            rowResult.references.map(SpatialGridReference::cellReference).sorted(),
        )

        val boundarySheet = sheetInput(
            sheetId = SHEET_B,
            order = 0,
            drawingArea = SpatialRect(0, 0, 100, 100),
            rows = 2,
            columns = 4,
        )
        val boundaryOccurrences = listOf(
            occurrence(SHEET_B, "origin", SpatialRect(-5, -5, 10, 10)),
            occurrence(SHEET_B, "internal", SpatialRect(20, 45, 10, 10)),
            occurrence(SHEET_B, "outer", SpatialRect(95, 95, 10, 10)),
        )
        val boundaryResult = SpatialGridCompiler().compile(
            sheets = listOf(boundarySheet),
            occurrences = boundaryOccurrences,
            constructs = emptyList(),
        )

        assertEquals(
            mapOf("origin" to "A1", "internal" to "B2", "outer" to "B4"),
            boundaryResult.references.associate { reference ->
                reference.subject.projectionId to reference.cellReference
            },
        )

        val oddCenterResult = SpatialGridCompiler().compile(
            sheets = listOf(
                sheetInput(
                    sheetId = SHEET_A,
                    order = 0,
                    drawingArea = SpatialRect(0, 0, 5, 5),
                    rows = 2,
                    columns = 2,
                ),
            ),
            occurrences = listOf(occurrence(SHEET_A, "odd-center", SpatialRect(2, 2, 1, 1))),
            constructs = emptyList(),
        )
        assertEquals("B2", oddCenterResult.references.single().cellReference)

        val maximumRowResult = SpatialGridCompiler().compile(
            sheets = listOf(
                sheetInput(
                    sheetId = SHEET_A,
                    order = 0,
                    drawingArea = SpatialRect(0, 0, 1, SpatialGridDefinition.MAX_SUPPORTED_ROWS),
                    rows = SpatialGridDefinition.MAX_SUPPORTED_ROWS,
                    columns = 1,
                ),
            ),
            occurrences = listOf(
                occurrence(
                    SHEET_A,
                    "row-zzz",
                    SpatialRect(0, SpatialGridDefinition.MAX_SUPPORTED_ROWS - 1, 1, 1),
                ),
            ),
            constructs = emptyList(),
        )
        assertEquals("ZZZ1", maximumRowResult.references.single().cellReference)
    }

    @Test
    fun `grid results stay canonical under permutations and other sheet changes`() {
        val sheetA = sheetInput(
            sheetId = SHEET_A,
            order = 0,
            drawingArea = SpatialRect(0, 0, 100, 100),
            rows = 2,
            columns = 2,
        )
        val sheetB = sheetInput(
            sheetId = SHEET_B,
            order = 1,
            drawingArea = SpatialRect(100, 100, 200, 100),
            rows = 4,
            columns = 5,
        )
        val occurrences = listOf(
            occurrence(SHEET_A, "occurrence:z", SpatialRect(50, 50, 20, 20)),
            occurrence(SHEET_A, "occurrence:a", SpatialRect(10, 10, 20, 20)),
            occurrence(SHEET_B, "occurrence:b", SpatialRect(110, 110, 20, 20)),
        )
        val constructs = listOf(
            construct(SHEET_A, "construct:z", occurrences[0], SpatialRect(40, 40, 20, 20)),
            construct(SHEET_B, "construct:b", occurrences[2], SpatialRect(250, 170, 20, 20)),
        )

        val expected = SpatialGridCompiler().compile(
            sheets = listOf(sheetA, sheetB),
            occurrences = occurrences,
            constructs = constructs,
        )
        val permuted = SpatialGridCompiler().compile(
            sheets = listOf(sheetB, sheetA),
            occurrences = occurrences.reversed(),
            constructs = constructs.reversed(),
        )

        assertEquals(expected, permuted)
        assertEquals(expected, SpatialGridCompiler().compile(listOf(sheetA, sheetB), occurrences, constructs))

        val changedSheetB = SpatialGridCompiler().compile(
            sheets = listOf(sheetA, sheetB.copy(grid = sheetB.grid?.copy(rows = 8, columns = 10))),
            occurrences = occurrences,
            constructs = constructs,
        )
        assertEquals(
            expected.references.filter { reference -> reference.sheetId == SHEET_A },
            changedSheetB.references.filter { reference -> reference.sheetId == SHEET_A },
        )

        val movedSheetB = SpatialGridCompiler().compile(
            sheets = listOf(
                sheetA,
                sheetB.copy(drawingArea = SpatialRect(80, 80, 240, 120)),
            ),
            occurrences = occurrences,
            constructs = constructs,
        )
        assertTrue(movedSheetB.diagnostics.isEmpty())
        assertEquals(
            expected.references.filter { reference -> reference.sheetId == SHEET_A },
            movedSheetB.references.filter { reference -> reference.sheetId == SHEET_A },
        )
    }

    @Test
    fun `invalid grids aggregate exact diagnostics and publish no partial facts`() {
        val valid = sheetInput(
            sheetId = "sheet:valid",
            order = 0,
            drawingArea = SpatialRect(0, 0, 100, 100),
            rows = 2,
            columns = 2,
        )
        val missing = sheetInput(
            sheetId = "sheet:missing",
            order = 1,
            drawingArea = SpatialRect(0, 0, 100, 100),
            rows = 2,
            columns = 2,
        ).copy(grid = null)
        val blank = sheetInput(
            sheetId = "sheet:blank",
            order = 2,
            drawingArea = SpatialRect(0, 0, 100, 100),
            rows = 2,
            columns = 2,
        ).copy(grid = ProjectionSheetGrid("", 2, 2))
        val noRows = sheetInput(
            sheetId = "sheet:no-rows",
            order = 3,
            drawingArea = SpatialRect(0, 0, 100, 100),
            rows = 0,
            columns = 2,
        )
        val negativeRows = sheetInput(
            sheetId = "sheet:negative-rows",
            order = 4,
            drawingArea = SpatialRect(0, 0, 100, 100),
            rows = -1,
            columns = 2,
        )
        val zeroColumns = sheetInput(
            sheetId = "sheet:zero-columns",
            order = 5,
            drawingArea = SpatialRect(0, 0, 100, 100),
            rows = 2,
            columns = 0,
        )
        val noColumns = sheetInput(
            sheetId = "sheet:no-columns",
            order = 6,
            drawingArea = SpatialRect(0, 0, 100, 100),
            rows = 2,
            columns = -1,
        )
        val tooManyRows = sheetInput(
            sheetId = "sheet:too-many-rows",
            order = 7,
            drawingArea = SpatialRect(0, 0, 100, 100),
            rows = SpatialGridDefinition.MAX_SUPPORTED_ROWS + 1,
            columns = 2,
        )

        val result = SpatialGridCompiler().compile(
            sheets = listOf(noColumns, valid, zeroColumns, tooManyRows, blank, negativeRows, missing, noRows),
            occurrences = listOf(occurrence("sheet:valid", "valid", SpatialRect(10, 10, 20, 20))),
            constructs = emptyList(),
        )

        assertNoGridFacts(result)
        assertEquals(
            listOf(
                GridDiagnosticContract(
                    subject = "Sheet sheet:blank grid",
                    problem = "has a blank identity",
                    correction = "Give the grid on Sheet sheet:blank a non-blank identity.",
                    projectionIds = blank.sourceTrace.projectionIds,
                ),
                GridDiagnosticContract(
                    subject = "Sheet sheet:missing grid",
                    problem = "is missing",
                    correction = "Define a grid for Sheet sheet:missing before compiling Grid References.",
                    projectionIds = missing.sourceTrace.projectionIds,
                ),
                GridDiagnosticContract(
                    subject = "Sheet sheet:negative-rows grid",
                    problem = "has -1 rows",
                    correction = "Set grid rows on Sheet sheet:negative-rows to a count from 1 through 18278.",
                    projectionIds = negativeRows.sourceTrace.projectionIds,
                ),
                GridDiagnosticContract(
                    subject = "Sheet sheet:no-columns grid",
                    problem = "has -1 columns",
                    correction = "Set grid columns on Sheet sheet:no-columns to a positive count.",
                    projectionIds = noColumns.sourceTrace.projectionIds,
                ),
                GridDiagnosticContract(
                    subject = "Sheet sheet:no-rows grid",
                    problem = "has 0 rows",
                    correction = "Set grid rows on Sheet sheet:no-rows to a count from 1 through 18278.",
                    projectionIds = noRows.sourceTrace.projectionIds,
                ),
                GridDiagnosticContract(
                    subject = "Sheet sheet:too-many-rows grid",
                    problem = "has 18279 rows, above the supported maximum of 18278",
                    correction = "Set grid rows on Sheet sheet:too-many-rows to a count from 1 through 18278.",
                    projectionIds = tooManyRows.sourceTrace.projectionIds,
                ),
                GridDiagnosticContract(
                    subject = "Sheet sheet:zero-columns grid",
                    problem = "has 0 columns",
                    correction = "Set grid columns on Sheet sheet:zero-columns to a positive count.",
                    projectionIds = zeroColumns.sourceTrace.projectionIds,
                ),
            ),
            result.diagnostics.map { diagnostic -> diagnostic.gridContract() },
        )
    }

    @Test
    fun `duplicate ownership and subject geometry fail canonically`() {
        val sheetA = sheetInput(
            sheetId = SHEET_A,
            order = 0,
            drawingArea = SpatialRect(0, 0, 100, 100),
            rows = 2,
            columns = 2,
        )
        val duplicateSheetA = sheetA.copy(order = 1)
        val duplicateOccurrence = occurrence(SHEET_A, "duplicate-occurrence", SpatialRect(10, 10, 20, 20))
        val alternateDuplicateOccurrence = duplicateOccurrence.copy(
            sourceTrace = SpatialSourceTrace(
                projectionIds = duplicateOccurrence.sourceTrace.projectionIds + "alternate-source",
                geometryElementIds = listOf(GeometryElementId("geometry:duplicate-occurrence:alternate")),
            ),
        )
        val duplicateConstruct = construct(
            SHEET_A,
            "duplicate-construct",
            duplicateOccurrence,
            SpatialRect(10, 10, 20, 20),
        )
        val unknownOccurrence = occurrence("sheet:unknown", "unknown", SpatialRect(10, 10, 20, 20))
        val unknownConstruct = construct(
            "sheet:unknown",
            "unknown-construct",
            unknownOccurrence,
            SpatialRect(10, 10, 20, 20),
        )

        val result = SpatialGridCompiler().compile(
            sheets = listOf(duplicateSheetA, sheetA),
            occurrences = listOf(unknownOccurrence, alternateDuplicateOccurrence, duplicateOccurrence),
            constructs = listOf(unknownConstruct, duplicateConstruct, duplicateConstruct),
        )

        assertNoGridFacts(result)
        assertEquals(
            listOf(
                "Construct duplicate-construct on Sheet sheet:a" to "has 2 geometry facts",
                "Construct duplicate-construct on Sheet sheet:a" to "has 2 owning Spatial grid definitions",
                "Construct unknown-construct on Sheet sheet:unknown" to "has no owning Spatial grid definition",
                "Occurrence duplicate-occurrence on Sheet sheet:a" to "has 2 geometry facts",
                "Occurrence duplicate-occurrence on Sheet sheet:a" to "has 2 owning Spatial grid definitions",
                "Occurrence unknown on Sheet sheet:unknown" to "has no owning Spatial grid definition",
                "Sheet sheet:a" to "has 2 Spatial grid definitions",
            ),
            result.diagnostics.map { diagnostic -> diagnostic.subject to diagnostic.problem },
        )
        assertTrue(result.diagnostics.all { diagnostic -> diagnostic.correction.isNotBlank() })
        assertEquals(
            listOf("sheet:a", "duplicate-construct", "duplicate-occurrence"),
            result.diagnostics.first { diagnostic -> diagnostic.problem == "has 2 geometry facts" }
                .sourceTrace.projectionIds,
        )
        val occurrenceDiagnostic = result.diagnostics.single { diagnostic ->
            diagnostic.subject == "Occurrence duplicate-occurrence on Sheet sheet:a" &&
                diagnostic.problem == "has 2 geometry facts"
        }
        assertEquals(
            listOf("sheet:a", "duplicate-occurrence", "alternate-source"),
            occurrenceDiagnostic.sourceTrace.projectionIds,
        )
        assertEquals(
            listOf("geometry:duplicate-occurrence", "geometry:duplicate-occurrence:alternate"),
            occurrenceDiagnostic.sourceTrace.geometryElementIds.map(GeometryElementId::value),
        )
    }

    @Test
    fun `duplicate facts retain secondary defects and canonical source traces`() {
        val firstSheet = sheetInput(
            sheetId = SHEET_A,
            order = 0,
            drawingArea = SpatialRect(0, 0, 100, 100),
            rows = 2,
            columns = 2,
        ).copy(grid = null)
        val secondSheet = firstSheet.copy(
            order = 1,
            sourceTrace = SpatialSourceTrace(
                projectionIds = listOf(SHEET_A, "alternate-sheet-source"),
                geometryElementIds = listOf(GeometryElementId("geometry:alternate-sheet")),
            ),
        )
        val repeated = occurrence(SHEET_A, SHEET_A, SpatialRect(100, 40, 2, 20))

        val forward = SpatialGridCompiler().compile(
            sheets = listOf(firstSheet, secondSheet),
            occurrences = listOf(repeated, repeated),
            constructs = emptyList(),
        )
        val reversed = SpatialGridCompiler().compile(
            sheets = listOf(secondSheet, firstSheet),
            occurrences = listOf(repeated, repeated),
            constructs = emptyList(),
        )

        assertNoGridFacts(forward)
        assertEquals(forward, reversed)
        assertEquals(
            listOf(
                "Occurrence sheet:a on Sheet sheet:a" to "has 2 geometry facts",
                "Occurrence sheet:a on Sheet sheet:a" to "has 2 owning Spatial grid definitions",
                "Sheet sheet:a" to "has 2 Spatial grid definitions",
                "Sheet sheet:a grid" to "is missing",
            ),
            forward.diagnostics.map { diagnostic -> diagnostic.subject to diagnostic.problem },
        )
        assertEquals(
            listOf(SHEET_A),
            forward.diagnostics.first().sourceTrace.projectionIds,
        )
        assertEquals(
            listOf(SHEET_A, "alternate-sheet-source", "grid:$SHEET_A"),
            forward.diagnostics.last().sourceTrace.projectionIds,
        )
    }

    @Test
    fun `blank sheet identity fails closed before grid model construction`() {
        val blankSheet = SpatialGridSheetInput(
            sheetId = "",
            order = 0,
            drawingArea = SpatialRect(0, 0, 100, 100),
            grid = ProjectionSheetGrid("grid:blank-sheet", 2, 2),
            sourceTrace = SpatialSourceTrace(
                projectionIds = listOf("projection:blank-sheet-input"),
                geometryElementIds = listOf(GeometryElementId("geometry:blank-sheet-input")),
            ),
        )

        val result = SpatialGridCompiler().compile(listOf(blankSheet), emptyList(), emptyList())

        assertNoGridFacts(result)
        assertEquals(
            listOf("Sheet identity" to "is blank"),
            result.diagnostics.map { diagnostic -> diagnostic.subject to diagnostic.problem },
        )
    }

    @Test
    fun `centers outside every drawing area edge fail without clamping`() {
        val sheet = sheetInput(
            sheetId = SHEET_A,
            order = 0,
            drawingArea = SpatialRect(0, 0, 100, 100),
            rows = 2,
            columns = 2,
        )
        val occurrences = listOf(
            occurrence(SHEET_A, "left", SpatialRect(-2, 40, 2, 20)),
            occurrence(SHEET_A, "right", SpatialRect(100, 40, 2, 20)),
            occurrence(SHEET_A, "top", SpatialRect(40, -2, 20, 2)),
            occurrence(SHEET_A, "bottom", SpatialRect(40, 100, 20, 2)),
        )
        val member = occurrence(SHEET_A, "member", SpatialRect(10, 10, 10, 10))
        val outsideConstruct = construct(
            SHEET_A,
            "outside-construct",
            member,
            SpatialRect(101, 40, 2, 20),
        )

        val result = SpatialGridCompiler().compile(
            sheets = listOf(sheet),
            occurrences = occurrences,
            constructs = listOf(outsideConstruct),
        )

        assertNoGridFacts(result)
        assertEquals(
            listOf(
                "Construct outside-construct on Sheet sheet:a",
                "Occurrence bottom on Sheet sheet:a",
                "Occurrence left on Sheet sheet:a",
                "Occurrence right on Sheet sheet:a",
                "Occurrence top on Sheet sheet:a",
            ),
            result.diagnostics.map(SpatialDiagnostic::subject),
        )
        assertEquals(
            listOf(
                "has an envelope center outside Drawing Area (0,0,100,100)",
                "has a rectangle center outside Drawing Area (0,0,100,100)",
                "has a rectangle center outside Drawing Area (0,0,100,100)",
                "has a rectangle center outside Drawing Area (0,0,100,100)",
                "has a rectangle center outside Drawing Area (0,0,100,100)",
            ),
            result.diagnostics.map(SpatialDiagnostic::problem),
        )
        assertTrue(result.diagnostics.all { diagnostic ->
            diagnostic.correction.endsWith("inside its owning Sheet Drawing Area.")
        })
    }

    @Test
    fun `extreme coordinates and column count map without overflow`() {
        val sheet = sheetInput(
            sheetId = SHEET_A,
            order = 0,
            drawingArea = SpatialRect(0, 0, Int.MAX_VALUE, 1),
            rows = 1,
            columns = Int.MAX_VALUE,
        )
        val occurrence = occurrence(
            sheetId = SHEET_A,
            projectionId = "extreme",
            rectangle = SpatialRect(Int.MAX_VALUE - 2, 0, 2, 1),
        )

        val result = SpatialGridCompiler().compile(listOf(sheet), listOf(occurrence), emptyList())

        assertTrue(result.diagnostics.isEmpty())
        assertEquals(Int.MAX_VALUE - 1, result.references.single().columnIndex)
        assertEquals(Int.MAX_VALUE, result.references.single().columnNumber)
        assertEquals("A2147483647", result.references.single().cellReference)
    }

    private fun sheetInput(
        sheetId: String,
        order: Int,
        drawingArea: SpatialRect,
        rows: Int,
        columns: Int,
    ): SpatialGridSheetInput = SpatialGridSheetInput(
        sheetId = sheetId,
        order = order,
        drawingArea = drawingArea,
        grid = ProjectionSheetGrid(gridId = "grid:$sheetId", rows = rows, columns = columns),
        sourceTrace = SpatialSourceTrace(
            projectionIds = listOf(sheetId, "grid:$sheetId"),
            geometryElementIds = listOf(GeometryElementId("geometry:$sheetId")),
        ),
    )

    private fun occurrence(
        sheetId: String,
        projectionId: String,
        rectangle: SpatialRect,
    ): SpatialOccurrenceGeometry = SpatialOccurrenceGeometry(
        occurrenceId = SpatialOccurrenceId(sheetId, projectionId),
        subjectId = StableSemanticIdentity("subject:$projectionId"),
        sheetId = sheetId,
        regionId = "region:$sheetId",
        rectangle = rectangle,
        placementReason = SpatialPlacementReason(listOf("test placement")),
        sourceTrace = SpatialSourceTrace(
            projectionIds = listOf(sheetId, projectionId),
            geometryElementIds = listOf(GeometryElementId("geometry:$projectionId")),
        ),
    )

    private fun construct(
        sheetId: String,
        projectionId: String,
        member: SpatialOccurrenceGeometry,
        envelope: SpatialRect,
    ): SpatialConstructGeometry = SpatialConstructGeometry(
        constructId = SpatialConstructId(sheetId, projectionId),
        sheetId = sheetId,
        kind = "sequence",
        name = projectionId,
        memberOccurrenceIds = listOf(member.occurrenceId),
        envelope = envelope,
        sourceTrace = SpatialSourceTrace(
            projectionIds = listOf(sheetId, projectionId, member.occurrenceId.projectionId),
            geometryElementIds = listOf(GeometryElementId("geometry:$projectionId")),
        ),
    )

    private fun expectedReference(
        occurrence: SpatialOccurrenceGeometry,
        rowIndex: Int,
        rowLabel: String,
        columnIndex: Int,
    ): SpatialGridReference {
        val subject = SpatialGridReferenceSubject.Occurrence(occurrence.occurrenceId)
        return expectedReference(
            sheetId = occurrence.sheetId,
            subject = subject,
            rowIndex = rowIndex,
            rowLabel = rowLabel,
            columnIndex = columnIndex,
            sourceTrace = occurrence.sourceTrace,
        )
    }

    private fun expectedReference(
        construct: SpatialConstructGeometry,
        rowIndex: Int,
        rowLabel: String,
        columnIndex: Int,
    ): SpatialGridReference {
        val subject = SpatialGridReferenceSubject.Construct(construct.constructId)
        return expectedReference(
            sheetId = construct.sheetId,
            subject = subject,
            rowIndex = rowIndex,
            rowLabel = rowLabel,
            columnIndex = columnIndex,
            sourceTrace = construct.sourceTrace,
        )
    }

    private fun expectedReference(
        sheetId: String,
        subject: SpatialGridReferenceSubject,
        rowIndex: Int,
        rowLabel: String,
        columnIndex: Int,
        sourceTrace: SpatialSourceTrace,
    ): SpatialGridReference {
        val columnNumber = columnIndex + 1
        return SpatialGridReference(
            gridReferenceId = SpatialGridReferenceId(sheetId, subject),
            sheetId = sheetId,
            gridId = "grid:$sheetId",
            subject = subject,
            rowIndex = rowIndex,
            rowLabel = rowLabel,
            columnIndex = columnIndex,
            columnNumber = columnNumber,
            cellReference = "$rowLabel$columnNumber",
            sourceTrace = sourceTrace,
        )
    }

    private companion object {
        const val SHEET_A = "sheet:a"
        const val SHEET_B = "sheet:b"
    }

    private data class GridDiagnosticContract(
        val subject: String,
        val problem: String,
        val correction: String,
        val projectionIds: List<String>,
    )

    private fun SpatialDiagnostic.gridContract(): GridDiagnosticContract = GridDiagnosticContract(
        subject = subject,
        problem = problem,
        correction = correction,
        projectionIds = sourceTrace.projectionIds,
    )

    private fun assertNoGridFacts(result: SpatialGridCompilationResult) {
        assertTrue(result.grids.isEmpty())
        assertTrue(result.references.isEmpty())
    }
}
