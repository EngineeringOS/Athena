package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.layout.SheetPlacementConstraint
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionSheetGrid
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialRect
import com.engineeringood.athena.spatial.SpatialSourceTrace

/** Derives bounded Spatial Reality from deterministic Projection placement and Sheet anchors. */
class GridAlignedSpatialLayoutCompiler(
    private val baseline: ProjectionSpatialLayout = ProjectionSpatialLayout(),
) {
    internal fun compile(
        projection: ProjectionDocument,
        constraints: List<SheetPlacementConstraint> = emptyList(),
        pageGeometries: Map<String, SpatialPageGeometryProfile> = emptyMap(),
    ): SpatialLayoutResult {
        val baselineResult = baseline.place(projection)
        if (baselineResult.diagnostics.isNotEmpty()) return baselineResult

        val orderedConstraints = constraints.sortedWith(
            compareBy<SheetPlacementConstraint>({ it.sheetId }, { it.occurrenceId.value }, { it.sourceSpan.startLine }),
        )
        val constraintDiagnostics = validateConstraints(projection, orderedConstraints)
        if (constraintDiagnostics.isNotEmpty()) {
            return SpatialLayoutResult(occurrences = emptyList(), diagnostics = constraintDiagnostics)
        }

        val derived = if (pageGeometries.isEmpty()) {
            deriveLegacyGeometry(baselineResult.occurrences, orderedConstraints)
        } else {
            deriveLogicalGridGeometry(projection, baselineResult.occurrences, orderedConstraints, pageGeometries)
        }
        val diagnostics = validateGeometry(projection, derived, orderedConstraints, pageGeometries)
        return if (diagnostics.isEmpty()) {
            SpatialLayoutResult(occurrences = derived.sortedWith(compareBy({ it.sheetId }, { it.occurrenceId.projectionId })))
        } else {
            SpatialLayoutResult(occurrences = emptyList(), diagnostics = diagnostics)
        }
    }

    private fun deriveLegacyGeometry(
        occurrences: List<SpatialOccurrenceGeometry>,
        constraints: List<SheetPlacementConstraint>,
    ): List<SpatialOccurrenceGeometry> {
        val anchored = constraints.associateBy { it.sheetId to it.occurrenceId.value }
        return occurrences.map { occurrence ->
            val constraint = anchored[occurrence.sheetId to occurrence.occurrenceId.projectionId]
            if (constraint == null) {
                occurrence.copy(rectangle = snap(occurrence.rectangle, 4))
            } else {
                occurrence.copy(
                    rectangle = anchoredRectangle(occurrence.rectangle, constraint, ProjectionSpatialLayout.DRAWING_AREA, centered = false),
                    placementReason = occurrence.placementReason.withAnchor(constraint),
                    sourceTrace = occurrence.sourceTrace.withConstraint(constraint),
                )
            }
        }
    }

    private fun deriveLogicalGridGeometry(
        projection: ProjectionDocument,
        occurrences: List<SpatialOccurrenceGeometry>,
        constraints: List<SheetPlacementConstraint>,
        pageGeometries: Map<String, SpatialPageGeometryProfile>,
    ): List<SpatialOccurrenceGeometry> {
        val constraintsByOccurrence = constraints.associateBy { it.sheetId to it.occurrenceId.value }
        val grids = projection.sheets.associate { sheet -> sheet.sheetId.value to requireNotNull(sheet.grid) }
        return occurrences
            .groupBy(SpatialOccurrenceGeometry::sheetId)
            .toSortedMap()
            .flatMap { (sheetId, sheetOccurrences) ->
                val page = requireNotNull(pageGeometries[sheetId]) { "Missing logical page geometry for Sheet $sheetId." }
                val grid = requireNotNull(grids[sheetId]) { "Missing grid for logical Sheet $sheetId." }
                val occupied = mutableListOf<SpatialRect>()
                sheetOccurrences.sortedBy { occurrence -> occurrence.occurrenceId.projectionId }.map { occurrence ->
                    val constraint = constraintsByOccurrence[sheetId to occurrence.occurrenceId.projectionId]
                    val rectangle = constraint?.let {
                        anchoredRectangle(occurrence.rectangle, it, page.drawingArea, grid, centered = true)
                    } ?: firstAvailableRectangle(occurrence.rectangle, grid, page.drawingArea, occupied)
                    occupied += rectangle
                    occurrence.copy(
                        rectangle = rectangle,
                        placementReason = constraint?.let { occurrence.placementReason.withAnchor(it) } ?: occurrence.placementReason,
                        sourceTrace = occurrence.sourceTrace.withConstraint(constraint),
                    )
                }
            }
    }

    private fun firstAvailableRectangle(
        original: SpatialRect,
        grid: ProjectionSheetGrid,
        drawingArea: SpatialRect,
        occupied: List<SpatialRect>,
    ): SpatialRect {
        val width = minOf(original.width, grid.subdivisions)
        val height = minOf(original.height, grid.subdivisions)
        for (row in 1..grid.rows) {
            for (column in 1..grid.columns) {
                val candidate = centeredRectangle(
                    anchorX = drawingArea.x + (column - 1) * grid.subdivisions + grid.subdivisions / 2,
                    anchorY = drawingArea.y + (row - 1) * grid.subdivisions + grid.subdivisions / 2,
                    width = width,
                    height = height,
                )
                if (candidate.isInside(drawingArea) && occupied.none { occupiedRectangle -> overlaps(candidate, occupiedRectangle) }) {
                    return candidate
                }
            }
        }
        return SpatialRect(drawingArea.right, drawingArea.bottom, width, height)
    }

    private fun validateConstraints(
        projection: ProjectionDocument,
        constraints: List<SheetPlacementConstraint>,
    ): List<SpatialDiagnostic> {
        val nodes = projection.nodes.associateBy { it.projectionId.value }
        val sheets = projection.sheets.associateBy { it.sheetId.value }
        val diagnostics = mutableListOf<SpatialDiagnostic>()
        val seen = mutableSetOf<Pair<String, String>>()
        constraints.forEach { constraint ->
            val key = constraint.sheetId to constraint.occurrenceId.value
            val node = nodes[constraint.occurrenceId.value]
            val sheet = sheets[constraint.sheetId]
            val trace = constraintTrace(constraint, node)
            when {
                sheet == null -> diagnostics += diagnostic(
                    constraint,
                    "Sheet does not exist in Projection Reality.",
                    "Place the occurrence on an existing Projection Sheet.",
                    trace,
                )
                node == null -> diagnostics += diagnostic(
                    constraint,
                    "Occurrence does not exist in Projection Reality.",
                    "Use the exact authored occurrence name from the project source.",
                    trace,
                )
                !ProjectionPlacementPlanner().sheetOwns(sheet, node) -> diagnostics += diagnostic(
                    constraint,
                    "Occurrence is not owned by the referenced Sheet.",
                    "Place the occurrence on its owning Sheet or update the Sheet Companion.",
                    trace,
                )
                !seen.add(key) -> diagnostics += diagnostic(
                    constraint,
                    "Occurrence has more than one authored anchor on this Sheet.",
                    "Keep one placement statement for this occurrence.",
                    trace,
                )
            }
        }
        return diagnostics.sortedWith(compareBy({ it.subject }, { it.problem }))
    }

    private fun validateGeometry(
        projection: ProjectionDocument,
        occurrences: List<SpatialOccurrenceGeometry>,
        constraints: List<SheetPlacementConstraint>,
        pageGeometries: Map<String, SpatialPageGeometryProfile>,
    ): List<SpatialDiagnostic> {
        val constraintByKey = constraints.associateBy { it.sheetId to it.occurrenceId.value }
        val diagnostics = mutableListOf<SpatialDiagnostic>()
        occurrences.groupBy(SpatialOccurrenceGeometry::sheetId).toSortedMap().forEach { (sheetId, sheetOccurrences) ->
            val drawingArea = pageGeometries[sheetId]?.drawingArea ?: ProjectionSpatialLayout.DRAWING_AREA
            sheetOccurrences.sortedBy { it.occurrenceId.projectionId }.forEach { occurrence ->
                if (!occurrence.rectangle.isInside(drawingArea)) {
                    val constraint = constraintByKey[sheetId to occurrence.occurrenceId.projectionId]
                    diagnostics += SpatialDiagnostic(
                        subject = "Occurrence ${occurrence.occurrenceId.projectionId} on Sheet $sheetId",
                        problem = "derived rectangle is outside the Sheet drawing area.",
                        correction = "Move the anchor to a cell whose complete occurrence fits inside the Sheet.",
                        sourceTrace = occurrence.sourceTrace.withConstraint(constraint),
                    )
                }
            }
            for (leftIndex in sheetOccurrences.indices) {
                for (rightIndex in leftIndex + 1 until sheetOccurrences.size) {
                    val left = sheetOccurrences[leftIndex]
                    val right = sheetOccurrences[rightIndex]
                    if (overlaps(left.rectangle, right.rectangle)) {
                        diagnostics += SpatialDiagnostic(
                            subject = "Sheet $sheetId",
                            problem = "occurrences ${left.occurrenceId.projectionId} and ${right.occurrenceId.projectionId} overlap.",
                            correction = "Move one occurrence to a different Sheet cell or remove the conflicting anchor.",
                            sourceTrace = SpatialSourceTrace(
                                projectionIds = listOf(sheetId, left.occurrenceId.projectionId, right.occurrenceId.projectionId),
                                geometryElementIds = (left.sourceTrace.geometryElementIds + right.sourceTrace.geometryElementIds)
                                    .distinctBy(GeometryElementId::value)
                                    .sortedBy(GeometryElementId::value),
                            ),
                        )
                    }
                }
            }
        }
        return diagnostics.sortedWith(compareBy({ it.subject }, { it.problem }))
    }

    private fun anchoredRectangle(
        original: SpatialRect,
        constraint: SheetPlacementConstraint,
        drawingArea: SpatialRect,
        grid: ProjectionSheetGrid? = null,
        centered: Boolean,
    ): SpatialRect {
        val anchorX = drawingArea.x + constraint.point.x - 1
        val anchorY = drawingArea.y + constraint.point.y - 1
        val width = grid?.let { minOf(original.width, it.subdivisions) } ?: original.width
        val height = grid?.let { minOf(original.height, it.subdivisions) } ?: original.height
        return if (centered) {
            centeredRectangle(anchorX, anchorY, width, height)
        } else {
            original.copy(x = anchorX, y = anchorY, width = width, height = height)
        }
    }

    private fun centeredRectangle(anchorX: Int, anchorY: Int, width: Int, height: Int): SpatialRect =
        SpatialRect(anchorX - width / 2, anchorY - height / 2, width, height)

    private fun snap(rectangle: SpatialRect, microPitch: Int): SpatialRect = rectangle.copy(
        x = ProjectionSpatialLayout.DRAWING_AREA.x +
            ((rectangle.x - ProjectionSpatialLayout.DRAWING_AREA.x) / microPitch) * microPitch,
        y = ProjectionSpatialLayout.DRAWING_AREA.y +
            ((rectangle.y - ProjectionSpatialLayout.DRAWING_AREA.y) / microPitch) * microPitch,
    )

    private fun overlaps(left: SpatialRect, right: SpatialRect): Boolean =
        left.x < right.right && left.right > right.x && left.y < right.bottom && left.bottom > right.y

    private fun diagnostic(
        constraint: SheetPlacementConstraint,
        problem: String,
        correction: String,
        trace: SpatialSourceTrace,
    ): SpatialDiagnostic = SpatialDiagnostic(
        subject = "Occurrence ${constraint.occurrenceName} on Sheet ${constraint.sheetId}",
        problem = problem,
        correction = correction,
        sourceTrace = trace,
    )

    private fun constraintTrace(constraint: SheetPlacementConstraint, node: ProjectionNode?): SpatialSourceTrace = SpatialSourceTrace(
        projectionIds = listOf(constraint.sheetId, constraint.occurrenceId.value, "${constraint.sourceSpan.sourceUnitId}:${constraint.sourceSpan.startLine}"),
        geometryElementIds = listOfNotNull(node?.originGeometryElementId ?: GeometryElementId("sheet:${constraint.sheetId}")),
    )

    private fun SpatialSourceTrace.withConstraint(constraint: SheetPlacementConstraint?): SpatialSourceTrace {
        if (constraint == null) return this
        return SpatialSourceTrace(
            projectionIds = (projectionIds + "${constraint.sourceSpan.sourceUnitId}:${constraint.sourceSpan.startLine}").distinct(),
            geometryElementIds = geometryElementIds,
        )
    }

    private fun com.engineeringood.athena.spatial.SpatialPlacementReason.withAnchor(
        constraint: SheetPlacementConstraint,
    ): com.engineeringood.athena.spatial.SpatialPlacementReason =
        com.engineeringood.athena.spatial.SpatialPlacementReason(
            buildList {
                addAll(constraints)
                add(
                    "authored Sheet point ${constraint.point.x},${constraint.point.y}" +
                        if (constraint.locked) " locked" else "",
                )
            },
        )
}
