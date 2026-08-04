package com.engineeringood.athena.spatial

import java.math.BigInteger

internal fun gridDiagnostics(sheet: SpatialSheet): List<SpatialDiagnostic> {
    val occurrencesById = sheet.occurrences.groupBy(SpatialOccurrenceGeometry::occurrenceId)
    val constructsById = sheet.constructs.groupBy(SpatialConstructGeometry::constructId)
    val referencesBySubject = sheet.gridReferences.groupBy(SpatialGridReference::subject)
    val expectedSubjects = occurrencesById.keys.map(SpatialGridReferenceSubject::Occurrence) +
        constructsById.keys.map(SpatialGridReferenceSubject::Construct)
    return buildList {
        if (sheet.grid.drawingArea != sheet.drawingArea) {
            add(
                SpatialDiagnostic(
                    subject = "Grid ${sheet.grid.gridId} on Sheet ${sheet.sheetId}",
                    problem = "Drawing Area ${sheet.grid.drawingArea.text()} does not equal Sheet Drawing Area " +
                        sheet.drawingArea.text(),
                    correction = "Derive the grid from its owning Sheet Drawing Area.",
                    sourceTrace = sheet.grid.sourceTrace,
                ),
            )
        }
        expectedSubjects.forEach { subject ->
            val matches = referencesBySubject[subject].orEmpty()
            if (matches.size != 1) {
                add(
                    SpatialDiagnostic(
                        subject = gridReferenceSubject(subject),
                        problem = "has ${matches.size} Grid Reference facts",
                        correction = "Publish exactly one owning-Sheet Grid Reference for this subject.",
                        sourceTrace = subjectTrace(subject, occurrencesById, constructsById, sheet.sourceTrace),
                    ),
                )
            }
        }
        sheet.gridReferences.forEach { reference ->
            val rectangle = when (val subject = reference.subject) {
                is SpatialGridReferenceSubject.Occurrence -> occurrencesById[subject.occurrenceId]
                    .orEmpty().singleOrNull()?.rectangle
                is SpatialGridReferenceSubject.Construct -> constructsById[subject.constructId]
                    .orEmpty().singleOrNull()?.envelope
            }
            if (rectangle == null) {
                add(
                    SpatialDiagnostic(
                        subject = gridReferenceSubject(reference.subject),
                        problem = "does not resolve one same-Sheet geometry subject",
                        correction = "Reference one existing Occurrence or Construct on the owning Sheet.",
                        sourceTrace = reference.sourceTrace,
                    ),
                )
            }
            if (reference.gridId != sheet.grid.gridId) {
                add(
                    SpatialDiagnostic(
                        subject = gridReferenceSubject(reference.subject),
                        problem = "names grid ${reference.gridId} instead of owning grid ${sheet.grid.gridId}",
                        correction = "Map the subject with its owning Sheet grid ${sheet.grid.gridId}.",
                        sourceTrace = reference.sourceTrace,
                    ),
                )
            }
            if (reference.rowIndex !in 0 until sheet.grid.rows || reference.columnIndex !in 0 until sheet.grid.columns) {
                add(
                    SpatialDiagnostic(
                        subject = gridReferenceSubject(reference.subject),
                        problem = "cell ${reference.cellReference} is outside the owning " +
                            "${sheet.grid.rows}x${sheet.grid.columns} grid",
                        correction = "Publish a row and column inside the owning Sheet grid.",
                        sourceTrace = reference.sourceTrace,
                    ),
                )
            }
            if (rectangle != null && rectangle.centerIsInside(sheet.grid.drawingArea)) {
                val expected = spatialGridCell(rectangle, sheet.grid)
                if (reference.rowIndex != expected.rowIndex || reference.columnIndex != expected.columnIndex) {
                    add(
                        SpatialDiagnostic(
                            subject = gridReferenceSubject(reference.subject),
                            problem = "publishes cell ${reference.cellReference} but its subject center maps to " +
                                expected.cellReference,
                            correction = "Map the subject center with the owning Sheet grid and publish cell " +
                                "${expected.cellReference}.",
                            sourceTrace = reference.sourceTrace,
                        ),
                    )
                }
            }
        }
    }
}

data class SpatialGridCell(
    val rowIndex: Int,
    val rowLabel: String,
    val columnIndex: Int,
    val columnNumber: Int,
) {
    val cellReference: String = "$rowLabel$columnNumber"
}

fun spatialGridCell(rectangle: SpatialRect, grid: SpatialGridDefinition): SpatialGridCell {
    val columnIndex = spatialGridCellIndex(
        center2 = doubledCenter(rectangle.x, rectangle.width),
        start2 = doubledCoordinate(grid.drawingArea.x),
        end2 = doubledCoordinate(grid.drawingArea.right),
        cells = grid.columns,
    )
    val rowIndex = spatialGridCellIndex(
        center2 = doubledCenter(rectangle.y, rectangle.height),
        start2 = doubledCoordinate(grid.drawingArea.y),
        end2 = doubledCoordinate(grid.drawingArea.bottom),
        cells = grid.rows,
    )
    return SpatialGridCell(rowIndex, spatialGridRowLabel(rowIndex), columnIndex, columnIndex + 1)
}

private fun spatialGridCellIndex(center2: Long, start2: Long, end2: Long, cells: Int): Int {
    if (center2 == end2) return cells - 1
    val relative = BigInteger.valueOf(center2 - start2)
    return relative.multiply(BigInteger.valueOf(cells.toLong()))
        .divide(BigInteger.valueOf(end2 - start2))
        .intValueExact()
}

private fun doubledCoordinate(value: Int): Long = value.toLong() * 2L
private fun doubledCenter(origin: Int, size: Int): Long = doubledCoordinate(origin) + size.toLong()

private fun SpatialRect.centerIsInside(container: SpatialRect): Boolean =
    doubledCenter(x, width) in doubledCoordinate(container.x)..doubledCoordinate(container.right) &&
        doubledCenter(y, height) in doubledCoordinate(container.y)..doubledCoordinate(container.bottom)

private fun subjectTrace(
    subject: SpatialGridReferenceSubject,
    occurrencesById: Map<SpatialOccurrenceId, List<SpatialOccurrenceGeometry>>,
    constructsById: Map<SpatialConstructId, List<SpatialConstructGeometry>>,
    fallback: SpatialSourceTrace,
): SpatialSourceTrace {
    val traces = when (subject) {
        is SpatialGridReferenceSubject.Occurrence ->
            occurrencesById[subject.occurrenceId].orEmpty().map(SpatialOccurrenceGeometry::sourceTrace)
        is SpatialGridReferenceSubject.Construct ->
            constructsById[subject.constructId].orEmpty().map(SpatialConstructGeometry::sourceTrace)
    }
    return if (traces.isEmpty()) fallback else combinedTrace(traces)
}
