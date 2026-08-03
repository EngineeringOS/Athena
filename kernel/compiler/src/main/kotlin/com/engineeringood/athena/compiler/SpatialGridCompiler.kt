package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionSheetGrid
import com.engineeringood.athena.spatial.SpatialConstructGeometry
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialGridDefinition
import com.engineeringood.athena.spatial.SpatialGridReference
import com.engineeringood.athena.spatial.SpatialGridReferenceId
import com.engineeringood.athena.spatial.SpatialGridReferenceSubject
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialSourceTrace
import java.math.BigInteger
import java.util.Collections

internal data class SpatialGridSheetInput(
    val sheetId: String,
    val order: Int,
    val drawingArea: SpatialRect,
    val grid: ProjectionSheetGrid?,
    val sourceTrace: SpatialSourceTrace,
)

internal class SpatialGridCompilationResult(
    grids: List<SpatialGridDefinition> = emptyList(),
    references: List<SpatialGridReference> = emptyList(),
    diagnostics: List<SpatialDiagnostic> = emptyList(),
) {
    val grids: List<SpatialGridDefinition> = grids.immutableGridCopy()
    val references: List<SpatialGridReference> = references.immutableGridCopy()
    val diagnostics: List<SpatialDiagnostic> = diagnostics.immutableGridCopy()

    override fun equals(other: Any?): Boolean =
        this === other || other is SpatialGridCompilationResult &&
            grids == other.grids && references == other.references && diagnostics == other.diagnostics

    override fun hashCode(): Int = listOf(grids, references, diagnostics).hashCode()

    override fun toString(): String =
        "SpatialGridCompilationResult(grids=$grids, references=$references, diagnostics=$diagnostics)"
}

internal class SpatialGridCompiler {
    private val validator = SpatialGridValidator()

    fun compile(
        sheets: List<SpatialGridSheetInput>,
        occurrences: List<SpatialOccurrenceGeometry>,
        constructs: List<SpatialConstructGeometry>,
    ): SpatialGridCompilationResult {
        val diagnostics = validator.validate(sheets, occurrences, constructs)
        if (diagnostics.isNotEmpty()) {
            return SpatialGridCompilationResult(diagnostics = diagnostics)
        }
        val orderedSheets = sheets.sortedWith(compareBy(SpatialGridSheetInput::order, SpatialGridSheetInput::sheetId))
        val grids = orderedSheets.map { sheet ->
            val grid = requireNotNull(sheet.grid)
            SpatialGridDefinition(
                sheetId = sheet.sheetId,
                gridId = grid.gridId,
                drawingArea = sheet.drawingArea,
                rows = grid.rows,
                columns = grid.columns,
                sourceTrace = sheet.sourceTrace,
            )
        }
        val references = orderedSheets.flatMap { sheet ->
            val grid = grids.single { definition -> definition.sheetId == sheet.sheetId }
            val occurrenceReferences = occurrences
                .filter { occurrence -> occurrence.sheetId == sheet.sheetId }
                .sortedBy { occurrence -> occurrence.occurrenceId.projectionId }
                .map { occurrence ->
                    reference(
                        grid = grid,
                        subject = SpatialGridReferenceSubject.Occurrence(occurrence.occurrenceId),
                        rectangle = occurrence.rectangle,
                        sourceTrace = occurrence.sourceTrace,
                    )
                }
            val constructReferences = constructs
                .filter { construct -> construct.sheetId == sheet.sheetId }
                .sortedBy { construct -> construct.constructId.projectionId }
                .map { construct ->
                    reference(
                        grid = grid,
                        subject = SpatialGridReferenceSubject.Construct(construct.constructId),
                        rectangle = construct.envelope,
                        sourceTrace = construct.sourceTrace,
                    )
                }
            occurrenceReferences + constructReferences
        }
        return SpatialGridCompilationResult(grids = grids, references = references)
    }

    private fun reference(
        grid: SpatialGridDefinition,
        subject: SpatialGridReferenceSubject,
        rectangle: SpatialRect,
        sourceTrace: SpatialSourceTrace,
    ): SpatialGridReference {
        val columnIndex = cellIndex(
            center2 = doubledCenter(rectangle.x, rectangle.width),
            start2 = doubledCoordinate(grid.drawingArea.x),
            end2 = doubledCoordinate(grid.drawingArea.right),
            cells = grid.columns,
        )
        val rowIndex = cellIndex(
            center2 = doubledCenter(rectangle.y, rectangle.height),
            start2 = doubledCoordinate(grid.drawingArea.y),
            end2 = doubledCoordinate(grid.drawingArea.bottom),
            cells = grid.rows,
        )
        val rowLabel = spatialGridRowLabel(rowIndex)
        val columnNumber = columnIndex + 1
        return SpatialGridReference(
            gridReferenceId = SpatialGridReferenceId(grid.sheetId, subject),
            sheetId = grid.sheetId,
            gridId = grid.gridId,
            subject = subject,
            rowIndex = rowIndex,
            rowLabel = rowLabel,
            columnIndex = columnIndex,
            columnNumber = columnNumber,
            cellReference = "$rowLabel$columnNumber",
            sourceTrace = sourceTrace,
        )
    }

    private fun cellIndex(
        center2: Long,
        start2: Long,
        end2: Long,
        cells: Int,
    ): Int {
        if (center2 == end2) return cells - 1
        val relative = BigInteger.valueOf(center2 - start2)
        val scaled = relative.multiply(BigInteger.valueOf(cells.toLong()))
        return scaled.divide(BigInteger.valueOf(end2 - start2)).intValueExact()
    }
}

internal fun spatialGridRowLabel(rowIndex: Int): String {
    require(rowIndex >= 0) { "Spatial grid row index must not be negative." }
    var remaining = rowIndex.toLong() + 1L
    return buildString {
        while (remaining > 0L) {
            val digit = ((remaining - 1L) % 26L).toInt()
            append(('A'.code + digit).toChar())
            remaining = (remaining - 1L) / 26L
        }
    }.reversed()
}

internal fun doubledCoordinate(value: Int): Long = value.toLong() * 2L

internal fun doubledCenter(origin: Int, size: Int): Long = doubledCoordinate(origin) + size.toLong()

private fun <T> List<T>.immutableGridCopy(): List<T> = Collections.unmodifiableList(toList())
