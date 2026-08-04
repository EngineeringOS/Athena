package com.engineeringood.athena.spatial

import com.engineeringood.athena.geometry.GeometryElementId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SpatialGridModelsTest {
    @Test
    fun `grid references keep typed subjects and stable identity separate from cell`() {
        val trace = SpatialSourceTrace(
            projectionIds = listOf("sheet:main", "occurrence:Q1"),
            geometryElementIds = listOf(GeometryElementId("geometry:Q1")),
        )
        val grid = SpatialGridDefinition(
            sheetId = "sheet:main",
            gridId = "grid:main",
            drawingArea = SpatialRect(40, 60, 1120, 640),
            rows = 8,
            columns = 12,
            sourceTrace = trace,
        )
        val occurrenceSubject = SpatialGridReferenceSubject.Occurrence(
            SpatialOccurrenceId("sheet:main", "occurrence:Q1"),
        )
        val occurrenceIdentity = SpatialGridReferenceId("sheet:main", occurrenceSubject)
        val occurrenceReference = reference(occurrenceIdentity, occurrenceSubject, "B3", 1, 2, trace)
        val movedReference = reference(occurrenceIdentity, occurrenceSubject, "C4", 2, 3, trace)
        val constructSubject = SpatialGridReferenceSubject.Construct(
            SpatialConstructId("sheet:main", "construct:control"),
        )
        val constructReference = reference(
            SpatialGridReferenceId("sheet:main", constructSubject),
            constructSubject,
            "A1",
            0,
            0,
            trace,
        )
        val sheet = SpatialSheet(
            sheetId = "sheet:main",
            extent = SpatialRect(0, 0, 1200, 800),
            drawingArea = grid.drawingArea,
            grid = grid,
            occurrences = emptyList(),
            regions = emptyList(),
            constructs = emptyList(),
            alignments = emptyList(),
            anchors = emptyList(),
            lanes = emptyList(),
            routes = emptyList(),
            gridReferences = listOf(occurrenceReference, constructReference),
            quality = SpatialQualitySnapshot(
                SpatialQualitySnapshotId("sheet:main"),
                "sheet:main",
                zeroSpatialQualityMetrics(),
                trace,
            ),
            sourceTrace = trace,
        )
        val document = SpatialDocument(listOf(sheet))

        assertEquals(occurrenceReference.gridReferenceId, movedReference.gridReferenceId)
        assertEquals("B3", occurrenceReference.cellReference)
        assertEquals("C4", movedReference.cellReference)
        assertIs<SpatialGridReferenceSubject.Occurrence>(document.sheets.single().gridReferences[0].subject)
        assertIs<SpatialGridReferenceSubject.Construct>(document.sheets.single().gridReferences[1].subject)
        assertEquals(List::class.java, SpatialSheet::class.java.getDeclaredField("gridReferences").type)
        assertTrue("grid definition" in SpatialReality.ownedFacts)
        assertTrue("Grid Reference" in SpatialReality.ownedFacts)
    }

    @Test
    fun `grid contracts reject invalid ownership dimensions and cell language`() {
        val trace = SpatialSourceTrace(
            projectionIds = listOf("sheet:main"),
            geometryElementIds = listOf(GeometryElementId("geometry:sheet:main")),
        )
        assertFailsWith<IllegalArgumentException> {
            SpatialGridDefinition("sheet:main", "grid:main", SpatialRect(0, 0, 100, 100), 0, 2, trace)
        }
        assertFailsWith<IllegalArgumentException> {
            SpatialGridDefinition(
                "sheet:main",
                "grid:main",
                SpatialRect(0, 0, 100, 100),
                SpatialGridDefinition.MAX_SUPPORTED_ROWS + 1,
                2,
                trace,
            )
        }
        val subject = SpatialGridReferenceSubject.Occurrence(
            SpatialOccurrenceId("sheet:other", "occurrence:Q1"),
        )
        assertFailsWith<IllegalArgumentException> {
            SpatialGridReferenceId("sheet:main", subject)
        }
        val ownedSubject = SpatialGridReferenceSubject.Occurrence(
            SpatialOccurrenceId("sheet:main", "occurrence:Q1"),
        )
        val identity = SpatialGridReferenceId("sheet:main", ownedSubject)
        assertFailsWith<IllegalArgumentException> {
            SpatialGridReference(
                gridReferenceId = identity,
                sheetId = "sheet:main",
                gridId = "grid:main",
                subject = ownedSubject,
                rowIndex = 1,
                rowLabel = "B",
                columnIndex = 2,
                columnNumber = 3,
                cellReference = "3B",
                sourceTrace = trace,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            SpatialGridReference(
                gridReferenceId = identity,
                sheetId = "sheet:main",
                gridId = "grid:main",
                subject = ownedSubject,
                rowIndex = 0,
                rowLabel = "ZZZ",
                columnIndex = 0,
                columnNumber = 1,
                cellReference = "ZZZ1",
                sourceTrace = trace,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            SpatialGridReference(
                gridReferenceId = identity,
                sheetId = "sheet:main",
                gridId = "grid:main",
                subject = ownedSubject,
                rowIndex = SpatialGridDefinition.MAX_SUPPORTED_ROWS,
                rowLabel = "ZZZZ",
                columnIndex = 0,
                columnNumber = 1,
                cellReference = "ZZZZ1",
                sourceTrace = trace,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            SpatialGridReference(
                gridReferenceId = identity,
                sheetId = "sheet:main",
                gridId = "grid:main",
                subject = ownedSubject,
                rowIndex = 0,
                rowLabel = "A",
                columnIndex = Int.MAX_VALUE,
                columnNumber = Int.MIN_VALUE,
                cellReference = "A${Int.MIN_VALUE}",
                sourceTrace = trace,
            )
        }
    }

    private fun reference(
        identity: SpatialGridReferenceId,
        subject: SpatialGridReferenceSubject,
        cell: String,
        rowIndex: Int,
        columnIndex: Int,
        trace: SpatialSourceTrace,
    ): SpatialGridReference {
        val rowLabel = cell.takeWhile(Char::isLetter)
        return SpatialGridReference(
            gridReferenceId = identity,
            sheetId = identity.sheetId,
            gridId = "grid:main",
            subject = subject,
            rowIndex = rowIndex,
            rowLabel = rowLabel,
            columnIndex = columnIndex,
            columnNumber = columnIndex + 1,
            cellReference = cell,
            sourceTrace = trace,
        )
    }
}
