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
import com.engineeringood.athena.spatial.spatialGridCell
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
        val cell = spatialGridCell(rectangle, grid)
        return SpatialGridReference(
            gridReferenceId = SpatialGridReferenceId(grid.sheetId, subject),
            sheetId = grid.sheetId,
            gridId = grid.gridId,
            subject = subject,
            rowIndex = cell.rowIndex,
            rowLabel = cell.rowLabel,
            columnIndex = cell.columnIndex,
            columnNumber = cell.columnNumber,
            cellReference = cell.cellReference,
            sourceTrace = sourceTrace,
        )
    }

}

internal fun doubledCoordinate(value: Int): Long = value.toLong() * 2L

internal fun doubledCenter(origin: Int, size: Int): Long = doubledCoordinate(origin) + size.toLong()

private fun <T> List<T>.immutableGridCopy(): List<T> = Collections.unmodifiableList(toList())
