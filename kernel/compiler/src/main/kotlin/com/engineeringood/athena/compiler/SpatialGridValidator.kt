package com.engineeringood.athena.compiler

import com.engineeringood.athena.spatial.SpatialConstructGeometry
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialGridDefinition
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialSourceTrace

internal class SpatialGridValidator {
    fun validate(
        sheets: List<SpatialGridSheetInput>,
        occurrences: List<SpatialOccurrenceGeometry>,
        constructs: List<SpatialConstructGeometry>,
    ): List<SpatialDiagnostic> {
        val sheetsById = sheets.groupBy(SpatialGridSheetInput::sheetId)
        return buildList {
            addAll(sheetDiagnostics(sheetsById))
            addAll(occurrenceDiagnostics(sheetsById, occurrences))
            addAll(constructDiagnostics(sheetsById, constructs))
        }.sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem, SpatialDiagnostic::correction))
    }

    private fun sheetDiagnostics(
        sheetsById: Map<String, List<SpatialGridSheetInput>>,
    ): List<SpatialDiagnostic> = buildList {
        sheetsById.forEach { (sheetId, definitions) ->
            val requiredProjectionIds = listOf(sheetId).filter(String::isNotBlank)
            if (sheetId.isBlank()) {
                add(
                    SpatialDiagnostic(
                        subject = "Sheet identity",
                        problem = "is blank",
                        correction = "Give every Spatial grid definition a non-blank owning Sheet identity.",
                        sourceTrace = combinedTrace(emptyList(), definitions.map(SpatialGridSheetInput::sourceTrace)),
                    ),
                )
            }
            if (definitions.size > 1) {
                add(
                    SpatialDiagnostic(
                        subject = if (sheetId.isBlank()) "Sheet with blank identity" else "Sheet $sheetId",
                        problem = "has ${definitions.size} Spatial grid definitions",
                        correction = if (sheetId.isBlank()) {
                            "Provide exactly one Spatial grid definition for each non-blank Sheet identity."
                        } else {
                            "Provide exactly one Spatial grid definition for Sheet $sheetId."
                        },
                        sourceTrace = combinedTrace(
                            requiredProjectionIds = requiredProjectionIds,
                            traces = definitions.map(SpatialGridSheetInput::sourceTrace),
                        ),
                    ),
                )
            }
            definitions
                .flatMap { sheet -> sheetDefects(sheet).map { defect -> defect to sheet.sourceTrace } }
                .groupBy(keySelector = Pair<GridDefect, SpatialSourceTrace>::first, valueTransform = Pair<GridDefect, SpatialSourceTrace>::second)
                .forEach { (defect, traces) ->
                    add(
                        SpatialDiagnostic(
                            subject = if (sheetId.isBlank()) "Sheet with blank identity grid" else "Sheet $sheetId grid",
                            problem = defect.problem,
                            correction = defect.correction,
                            sourceTrace = combinedTrace(requiredProjectionIds, traces),
                        ),
                    )
                }
        }
    }

    private fun sheetDefects(sheet: SpatialGridSheetInput): List<GridDefect> = buildList {
        val grid = sheet.grid
        when {
            grid == null -> add(
                GridDefect(
                    problem = "is missing",
                    correction = "Define a grid for Sheet ${sheet.sheetId} before compiling Grid References.",
                ),
            )
            grid.gridId.isBlank() -> add(
                GridDefect(
                    problem = "has a blank identity",
                    correction = "Give the grid on Sheet ${sheet.sheetId} a non-blank identity.",
                ),
            )
        }
        if (grid != null && grid.rows <= 0) {
            add(GridDefect("has ${grid.rows} rows", rowCorrection(sheet.sheetId)))
        } else if (grid != null && grid.rows > SpatialGridDefinition.MAX_SUPPORTED_ROWS) {
            add(
                GridDefect(
                    problem = "has ${grid.rows} rows, above the supported maximum of " +
                        SpatialGridDefinition.MAX_SUPPORTED_ROWS,
                    correction = rowCorrection(sheet.sheetId),
                ),
            )
        }
        if (grid != null && grid.columns <= 0) {
            add(
                GridDefect(
                    problem = "has ${grid.columns} columns",
                    correction = "Set grid columns on Sheet ${sheet.sheetId} to a positive count.",
                ),
            )
        }
    }

    private fun occurrenceDiagnostics(
        sheetsById: Map<String, List<SpatialGridSheetInput>>,
        occurrences: List<SpatialOccurrenceGeometry>,
    ): List<SpatialDiagnostic> = buildList {
        occurrences.groupBy(SpatialOccurrenceGeometry::occurrenceId).forEach { (_, facts) ->
            val occurrence = facts.first()
            val requiredProjectionIds = listOf(occurrence.sheetId, occurrence.occurrenceId.projectionId)
            if (facts.size > 1) {
                add(
                    SpatialDiagnostic(
                        subject = occurrenceSubject(occurrence),
                        problem = "has ${facts.size} geometry facts",
                        correction = "Publish exactly one geometry fact for Occurrence " +
                            "${occurrence.occurrenceId.projectionId} on Sheet ${occurrence.sheetId}.",
                        sourceTrace = combinedTrace(requiredProjectionIds, facts.map(SpatialOccurrenceGeometry::sourceTrace)),
                    ),
                )
            }
            val definitions = sheetsById[occurrence.sheetId].orEmpty()
            when {
                definitions.isEmpty() -> add(
                    SpatialDiagnostic(
                        subject = occurrenceSubject(occurrence),
                        problem = "has no owning Spatial grid definition",
                        correction = "Provide one grid definition for Sheet ${occurrence.sheetId} before mapping this Occurrence.",
                        sourceTrace = combinedTrace(requiredProjectionIds, facts.map(SpatialOccurrenceGeometry::sourceTrace)),
                    ),
                )
                definitions.size > 1 -> add(
                    SpatialDiagnostic(
                        subject = occurrenceSubject(occurrence),
                        problem = "has ${definitions.size} owning Spatial grid definitions",
                        correction = "Provide exactly one grid definition for Sheet ${occurrence.sheetId} before mapping this Occurrence.",
                        sourceTrace = combinedTrace(requiredProjectionIds, facts.map(SpatialOccurrenceGeometry::sourceTrace)),
                    ),
                )
                else -> {
                    val drawingArea = definitions.single().drawingArea
                    val outside = facts.filterNot { fact -> centerIsInside(fact.rectangle, drawingArea) }
                    if (outside.isNotEmpty()) {
                        add(
                            SpatialDiagnostic(
                                subject = occurrenceSubject(occurrence),
                                problem = "has a rectangle center outside ${drawingAreaText(drawingArea)}",
                                correction = "Place Occurrence ${occurrence.occurrenceId.projectionId} so its center is inside its " +
                                    "owning Sheet Drawing Area.",
                                sourceTrace = combinedTrace(
                                    requiredProjectionIds,
                                    outside.map(SpatialOccurrenceGeometry::sourceTrace),
                                ),
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun constructDiagnostics(
        sheetsById: Map<String, List<SpatialGridSheetInput>>,
        constructs: List<SpatialConstructGeometry>,
    ): List<SpatialDiagnostic> = buildList {
        constructs.groupBy(SpatialConstructGeometry::constructId).forEach { (_, facts) ->
            val construct = facts.first()
            val requiredProjectionIds = listOf(construct.sheetId, construct.constructId.projectionId)
            if (facts.size > 1) {
                add(
                    SpatialDiagnostic(
                        subject = constructSubject(construct),
                        problem = "has ${facts.size} geometry facts",
                        correction = "Publish exactly one geometry fact for Construct " +
                            "${construct.constructId.projectionId} on Sheet ${construct.sheetId}.",
                        sourceTrace = combinedTrace(requiredProjectionIds, facts.map(SpatialConstructGeometry::sourceTrace)),
                    ),
                )
            }
            val definitions = sheetsById[construct.sheetId].orEmpty()
            when {
                definitions.isEmpty() -> add(
                    SpatialDiagnostic(
                        subject = constructSubject(construct),
                        problem = "has no owning Spatial grid definition",
                        correction = "Provide one grid definition for Sheet ${construct.sheetId} before mapping this Construct.",
                        sourceTrace = combinedTrace(requiredProjectionIds, facts.map(SpatialConstructGeometry::sourceTrace)),
                    ),
                )
                definitions.size > 1 -> add(
                    SpatialDiagnostic(
                        subject = constructSubject(construct),
                        problem = "has ${definitions.size} owning Spatial grid definitions",
                        correction = "Provide exactly one grid definition for Sheet ${construct.sheetId} before mapping this Construct.",
                        sourceTrace = combinedTrace(requiredProjectionIds, facts.map(SpatialConstructGeometry::sourceTrace)),
                    ),
                )
                else -> {
                    val drawingArea = definitions.single().drawingArea
                    val outside = facts.filterNot { fact -> centerIsInside(fact.envelope, drawingArea) }
                    if (outside.isNotEmpty()) {
                        add(
                            SpatialDiagnostic(
                                subject = constructSubject(construct),
                                problem = "has an envelope center outside ${drawingAreaText(drawingArea)}",
                                correction = "Place Construct ${construct.constructId.projectionId} so its center is inside its " +
                                    "owning Sheet Drawing Area.",
                                sourceTrace = combinedTrace(
                                    requiredProjectionIds,
                                    outside.map(SpatialConstructGeometry::sourceTrace),
                                ),
                            ),
                        )
                    }
                }
            }
        }
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

    private fun combinedTrace(
        requiredProjectionIds: List<String>,
        traces: List<SpatialSourceTrace>,
    ): SpatialSourceTrace {
        val canonicalRequired = requiredProjectionIds.distinct()
        val required = canonicalRequired.toSet()
        return SpatialSourceTrace(
            projectionIds = canonicalRequired + traces
                .flatMap(SpatialSourceTrace::projectionIds)
                .filterNot(required::contains)
                .distinct()
                .sorted(),
            geometryElementIds = traces
                .flatMap(SpatialSourceTrace::geometryElementIds)
                .distinctBy { geometryId -> geometryId.value }
                .sortedBy { geometryId -> geometryId.value },
        )
    }

    private data class GridDefect(
        val problem: String,
        val correction: String,
    )
}
