package com.engineeringood.athena.compiler

import com.engineeringood.athena.spatial.SpatialConstructGeometry
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialGridDefinition
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialRect

internal class SpatialGridValidator {
    fun validate(
        sheets: List<SpatialGridSheetInput>,
        occurrences: List<SpatialOccurrenceGeometry>,
        constructs: List<SpatialConstructGeometry>,
    ): List<SpatialDiagnostic> {
        val diagnostics = mutableListOf<SpatialDiagnostic>()
        diagnostics += sheetDiagnostics(sheets)
        diagnostics += duplicateOccurrenceDiagnostics(occurrences)
        diagnostics += duplicateConstructDiagnostics(constructs)

        val uniqueSheets = sheets.groupBy(SpatialGridSheetInput::sheetId)
            .filterValues { definitions -> definitions.size == 1 }
            .mapValues { (_, definitions) -> definitions.single() }
        occurrences.groupBy { occurrence -> occurrence.occurrenceId }
            .filterValues { facts -> facts.size == 1 }
            .values
            .map(List<SpatialOccurrenceGeometry>::single)
            .forEach { occurrence ->
                val sheet = uniqueSheets[occurrence.sheetId]
                if (sheet == null) {
                    diagnostics += SpatialDiagnostic(
                        subject = occurrenceSubject(occurrence),
                        problem = "has no owning Spatial grid definition",
                        correction = "Provide one grid definition for Sheet ${occurrence.sheetId} before mapping this Occurrence.",
                        sourceTrace = occurrence.sourceTrace,
                    )
                } else if (!centerIsInside(occurrence.rectangle, sheet.drawingArea)) {
                    diagnostics += SpatialDiagnostic(
                        subject = occurrenceSubject(occurrence),
                        problem = "has a rectangle center outside ${drawingAreaText(sheet.drawingArea)}",
                        correction = "Place Occurrence ${occurrence.occurrenceId.projectionId} so its center is inside its " +
                            "owning Sheet Drawing Area.",
                        sourceTrace = occurrence.sourceTrace,
                    )
                }
            }
        constructs.groupBy { construct -> construct.constructId }
            .filterValues { facts -> facts.size == 1 }
            .values
            .map(List<SpatialConstructGeometry>::single)
            .forEach { construct ->
                val sheet = uniqueSheets[construct.sheetId]
                if (sheet == null) {
                    diagnostics += SpatialDiagnostic(
                        subject = constructSubject(construct),
                        problem = "has no owning Spatial grid definition",
                        correction = "Provide one grid definition for Sheet ${construct.sheetId} before mapping this Construct.",
                        sourceTrace = construct.sourceTrace,
                    )
                } else if (!centerIsInside(construct.envelope, sheet.drawingArea)) {
                    diagnostics += SpatialDiagnostic(
                        subject = constructSubject(construct),
                        problem = "has an envelope center outside ${drawingAreaText(sheet.drawingArea)}",
                        correction = "Place Construct ${construct.constructId.projectionId} so its center is inside its " +
                            "owning Sheet Drawing Area.",
                        sourceTrace = construct.sourceTrace,
                    )
                }
            }
        return diagnostics.sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem))
    }

    private fun sheetDiagnostics(sheets: List<SpatialGridSheetInput>): List<SpatialDiagnostic> = buildList {
        sheets.groupBy(SpatialGridSheetInput::sheetId)
            .filterValues { definitions -> definitions.size > 1 }
            .forEach { (sheetId, definitions) ->
                add(
                    SpatialDiagnostic(
                        subject = "Sheet $sheetId",
                        problem = "has ${definitions.size} Spatial grid definitions",
                        correction = "Provide exactly one Spatial grid definition for Sheet $sheetId.",
                        sourceTrace = definitions.first().sourceTrace,
                    ),
                )
            }
        sheets.forEach { sheet ->
            val grid = sheet.grid
            when {
                grid == null -> add(
                    SpatialDiagnostic(
                        subject = "Sheet ${sheet.sheetId} grid",
                        problem = "is missing",
                        correction = "Define a grid for Sheet ${sheet.sheetId} before compiling Grid References.",
                        sourceTrace = sheet.sourceTrace,
                    ),
                )
                grid.gridId.isBlank() -> add(
                    SpatialDiagnostic(
                        subject = "Sheet ${sheet.sheetId} grid",
                        problem = "has a blank identity",
                        correction = "Give the grid on Sheet ${sheet.sheetId} a non-blank identity.",
                        sourceTrace = sheet.sourceTrace,
                    ),
                )
            }
            if (grid != null && grid.rows <= 0) {
                add(
                    SpatialDiagnostic(
                        subject = "Sheet ${sheet.sheetId} grid",
                        problem = "has ${grid.rows} rows",
                        correction = rowCorrection(sheet.sheetId),
                        sourceTrace = sheet.sourceTrace,
                    ),
                )
            } else if (grid != null && grid.rows > SpatialGridDefinition.MAX_SUPPORTED_ROWS) {
                add(
                    SpatialDiagnostic(
                        subject = "Sheet ${sheet.sheetId} grid",
                        problem = "has ${grid.rows} rows, above the supported maximum of " +
                            SpatialGridDefinition.MAX_SUPPORTED_ROWS,
                        correction = rowCorrection(sheet.sheetId),
                        sourceTrace = sheet.sourceTrace,
                    ),
                )
            }
            if (grid != null && grid.columns <= 0) {
                add(
                    SpatialDiagnostic(
                        subject = "Sheet ${sheet.sheetId} grid",
                        problem = "has ${grid.columns} columns",
                        correction = "Set grid columns on Sheet ${sheet.sheetId} to a positive count.",
                        sourceTrace = sheet.sourceTrace,
                    ),
                )
            }
        }
    }

    private fun duplicateOccurrenceDiagnostics(
        occurrences: List<SpatialOccurrenceGeometry>,
    ): List<SpatialDiagnostic> = occurrences.groupBy(SpatialOccurrenceGeometry::occurrenceId)
        .filterValues { facts -> facts.size > 1 }
        .map { (_, facts) ->
            val occurrence = facts.first()
            SpatialDiagnostic(
                subject = occurrenceSubject(occurrence),
                problem = "has ${facts.size} geometry facts",
                correction = "Publish exactly one geometry fact for Occurrence " +
                    "${occurrence.occurrenceId.projectionId} on Sheet ${occurrence.sheetId}.",
                sourceTrace = occurrence.sourceTrace,
            )
        }

    private fun duplicateConstructDiagnostics(
        constructs: List<SpatialConstructGeometry>,
    ): List<SpatialDiagnostic> = constructs.groupBy(SpatialConstructGeometry::constructId)
        .filterValues { facts -> facts.size > 1 }
        .map { (_, facts) ->
            val construct = facts.first()
            SpatialDiagnostic(
                subject = constructSubject(construct),
                problem = "has ${facts.size} geometry facts",
                correction = "Publish exactly one geometry fact for Construct " +
                    "${construct.constructId.projectionId} on Sheet ${construct.sheetId}.",
                sourceTrace = construct.sourceTrace,
            )
        }

    private fun centerIsInside(rectangle: SpatialRect, drawingArea: SpatialRect): Boolean {
        val center2X = doubledCenter(rectangle.x, rectangle.width)
        val center2Y = doubledCenter(rectangle.y, rectangle.height)
        return center2X in doubledCoordinate(drawingArea.x)..doubledCoordinate(drawingArea.right) &&
            center2Y in doubledCoordinate(drawingArea.y)..doubledCoordinate(drawingArea.bottom)
    }

    private fun rowCorrection(sheetId: String): String =
        "Set grid rows on Sheet $sheetId to a count from 1 through ${SpatialGridDefinition.MAX_SUPPORTED_ROWS}."

    private fun occurrenceSubject(occurrence: SpatialOccurrenceGeometry): String =
        "Occurrence ${occurrence.occurrenceId.projectionId} on Sheet ${occurrence.sheetId}"

    private fun constructSubject(construct: SpatialConstructGeometry): String =
        "Construct ${construct.constructId.projectionId} on Sheet ${construct.sheetId}"

    private fun drawingAreaText(drawingArea: SpatialRect): String =
        "Drawing Area (${drawingArea.x},${drawingArea.y},${drawingArea.width},${drawingArea.height})"
}
