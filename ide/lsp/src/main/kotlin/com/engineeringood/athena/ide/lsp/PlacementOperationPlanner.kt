package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.interaction.AlignOccurrences
import com.engineeringood.athena.interaction.AlignmentAxis
import com.engineeringood.athena.interaction.DistributeOccurrences
import com.engineeringood.athena.interaction.DistributionAxis
import com.engineeringood.athena.interaction.EditOperationBody
import com.engineeringood.athena.interaction.LockAction
import com.engineeringood.athena.interaction.MoveOccurrence
import com.engineeringood.athena.interaction.SheetPoint
import com.engineeringood.athena.interaction.SnapOccurrenceToGrid
import com.engineeringood.athena.presentation.AthenaDiagramScene
import com.engineeringood.athena.presentation.SceneBounds
import com.engineeringood.athena.presentation.SceneOccurrence
import com.engineeringood.athena.presentation.ScenePoint
import kotlin.math.floor

internal data class PlannedPlacement(
    val occurrenceId: String,
    val point: SheetPoint,
    val lockAction: LockAction,
)

internal class PlacementOperationPlanningFailure(
    val subject: String,
    val problem: String,
    val correction: String,
    val code: String,
) : IllegalArgumentException(problem)

/** Calculates canonical Sheet point intent from one accepted Canonical Scene. */
internal class PlacementOperationPlanner {
    fun plan(scene: AthenaDiagramScene, operation: EditOperationBody): List<PlannedPlacement> = when (operation) {
        is MoveOccurrence -> listOf(
            PlannedPlacement(
                operation.occurrenceId,
                validatePoint(scene, operation.sheetId, operation.occurrenceId, operation.point),
                operation.lockAction,
            ),
        )
        is SnapOccurrenceToGrid -> listOf(
            PlannedPlacement(
                operation.occurrenceId,
                validatePoint(scene, operation.sheetId, operation.occurrenceId, snap(operation.point, scene.snapGrid.step)),
                LockAction.PRESERVE,
            ),
        )
        is AlignOccurrences -> align(scene, operation)
        is DistributeOccurrences -> distribute(scene, operation)
        else -> throw failure(
            operation.kind.name,
            "Operation is not a Presentation placement edit.",
            "Submit Move, Align, Distribute, or Snap for the active Sheet.",
            "edit.operation.kind-invalid",
        )
    }

    private fun validatePoint(
        scene: AthenaDiagramScene,
        sheetId: String,
        occurrenceId: String,
        point: SheetPoint,
    ): SheetPoint {
        requireSheet(scene, sheetId)
        validateBounds(scene, requireOccurrence(scene, occurrenceId), scenePoint(scene, point))
        return point
    }

    private fun align(scene: AthenaDiagramScene, operation: AlignOccurrences): List<PlannedPlacement> {
        requireSheet(scene, operation.sheetId)
        val occurrences = occurrences(scene, operation.occurrenceIds)
        val target = alignmentTarget(occurrences, operation.axis)
        return planned(scene, occurrences) { occurrence ->
            val delta = target - axisValue(occurrence.bounds, operation.axis)
            when (operation.axis) {
                AlignmentAxis.LEFT,
                AlignmentAxis.CENTER_X,
                AlignmentAxis.RIGHT -> ScenePoint(quantize(occurrence.placementAnchor.x + delta), occurrence.placementAnchor.y)
                AlignmentAxis.TOP,
                AlignmentAxis.CENTER_Y,
                AlignmentAxis.BOTTOM -> ScenePoint(occurrence.placementAnchor.x, quantize(occurrence.placementAnchor.y + delta))
            }
        }
    }

    private fun distribute(scene: AthenaDiagramScene, operation: DistributeOccurrences): List<PlannedPlacement> {
        requireSheet(scene, operation.sheetId)
        val ordered = occurrences(scene, operation.occurrenceIds).sortedWith(
            compareBy<SceneOccurrence> {
                when (operation.axis) {
                    DistributionAxis.HORIZONTAL -> centerX(it.bounds)
                    DistributionAxis.VERTICAL -> centerY(it.bounds)
                }
            }.thenBy(SceneOccurrence::occurrenceId),
        )
        val first = axisCenter(ordered.first().bounds, operation.axis)
        val last = axisCenter(ordered.last().bounds, operation.axis)
        val interval = (last - first) / (ordered.size - 1)
        val planned = planned(scene, ordered) { occurrence ->
            val index = ordered.indexOf(occurrence)
            val delta = first + interval * index - axisCenter(occurrence.bounds, operation.axis)
            when (operation.axis) {
                DistributionAxis.HORIZONTAL -> ScenePoint(quantize(occurrence.placementAnchor.x + delta), occurrence.placementAnchor.y)
                DistributionAxis.VERTICAL -> ScenePoint(occurrence.placementAnchor.x, quantize(occurrence.placementAnchor.y + delta))
            }
        }
        val coordinates = planned.map {
            when (operation.axis) {
                DistributionAxis.HORIZONTAL -> it.point.x
                DistributionAxis.VERTICAL -> it.point.y
            }
        }
        if (coordinates.distinct().size != coordinates.size) {
            throw failure(
                operation.sheetId,
                "Snap quantization collapses distributed occurrence positions.",
                "Use a wider interval or fewer occurrences before distributing.",
                "edit.operation.distribution-collapsed",
            )
        }
        return planned
    }

    private fun planned(
        scene: AthenaDiagramScene,
        occurrences: List<SceneOccurrence>,
        pointFor: (SceneOccurrence) -> ScenePoint,
    ): List<PlannedPlacement> = occurrences.map { occurrence ->
        val point = snap(sheetPoint(scene, pointFor(occurrence)), scene.snapGrid.step)
        validateBounds(scene, occurrence, scenePoint(scene, point))
        PlannedPlacement(occurrence.occurrenceId, point, LockAction.PRESERVE)
    }.sortedBy(PlannedPlacement::occurrenceId)

    private fun requireSheet(scene: AthenaDiagramScene, sheetId: String) {
        if (sheetId != scene.snapGrid.sheetId) {
            throw failure(
                sheetId,
                "Presentation operation does not target current accepted Sheet.",
                "Select current Sheet and retry the presentation edit.",
                "edit.operation.sheet-unknown",
            )
        }
    }

    private fun occurrences(scene: AthenaDiagramScene, occurrenceIds: List<String>): List<SceneOccurrence> =
        occurrenceIds.map { occurrenceId -> requireOccurrence(scene, occurrenceId) }

    private fun requireOccurrence(scene: AthenaDiagramScene, occurrenceId: String): SceneOccurrence =
        scene.occurrences.singleOrNull { it.occurrenceId == occurrenceId }
            ?: throw failure(
                occurrenceId,
                "Occurrence is not present in current accepted Canonical Scene.",
                "Refresh the engineering document and select a visible occurrence.",
                "edit.operation.occurrence-missing",
            )

    private fun validateBounds(scene: AthenaDiagramScene, occurrence: SceneOccurrence, plannedAnchor: ScenePoint) {
        val moved = occurrence.bounds.translate(
            plannedAnchor.x - occurrence.placementAnchor.x,
            plannedAnchor.y - occurrence.placementAnchor.y,
        )
        val drawing = scene.page.drawingBounds
        if (moved.x < drawing.x || moved.y < drawing.y || moved.x + moved.width > drawing.x + drawing.width || moved.y + moved.height > drawing.y + drawing.height) {
            throw failure(
                occurrence.occurrenceId,
                "Placement would move occurrence bounds outside the accepted Sheet drawing area.",
                "Choose a Sheet point that keeps the complete occurrence inside the drawing area.",
                "edit.operation.bounds-outside-grid",
            )
        }
    }

    private fun scenePoint(scene: AthenaDiagramScene, point: SheetPoint): ScenePoint = ScenePoint(
        scene.snapGrid.drawingOrigin.x + point.x - 1,
        scene.snapGrid.drawingOrigin.y + point.y - 1,
    )

    private fun sheetPoint(scene: AthenaDiagramScene, point: ScenePoint): SheetPoint {
        val x = point.x - scene.snapGrid.drawingOrigin.x + 1
        val y = point.y - scene.snapGrid.drawingOrigin.y + 1
        if (x <= 0 || y <= 0) {
            throw failure(
                "${point.x},${point.y}",
                "Computed placement point is outside the Sheet drawing area.",
                "Choose an operation that keeps the occurrence inside the current Sheet.",
                "edit.operation.bounds-outside-grid",
            )
        }
        return SheetPoint(x, y)
    }

    private fun snap(point: SheetPoint, step: Int): SheetPoint =
        SheetPoint(nearestStep(point.x, step), nearestStep(point.y, step))

    private fun nearestStep(value: Int, step: Int): Int = maxOf(step, floor(value.toDouble() / step + 0.5).toInt() * step)

    private fun alignmentTarget(occurrences: List<SceneOccurrence>, axis: AlignmentAxis): Double = when (axis) {
        AlignmentAxis.LEFT -> occurrences.minOf { it.bounds.x }.toDouble()
        AlignmentAxis.CENTER_X -> (occurrences.minOf { it.bounds.x } + occurrences.maxOf { it.bounds.x + it.bounds.width }) / 2.0
        AlignmentAxis.RIGHT -> occurrences.maxOf { it.bounds.x + it.bounds.width }.toDouble()
        AlignmentAxis.TOP -> occurrences.minOf { it.bounds.y }.toDouble()
        AlignmentAxis.CENTER_Y -> (occurrences.minOf { it.bounds.y } + occurrences.maxOf { it.bounds.y + it.bounds.height }) / 2.0
        AlignmentAxis.BOTTOM -> occurrences.maxOf { it.bounds.y + it.bounds.height }.toDouble()
    }

    private fun axisValue(bounds: SceneBounds, axis: AlignmentAxis): Double = when (axis) {
        AlignmentAxis.LEFT -> bounds.x.toDouble()
        AlignmentAxis.CENTER_X -> centerX(bounds)
        AlignmentAxis.RIGHT -> (bounds.x + bounds.width).toDouble()
        AlignmentAxis.TOP -> bounds.y.toDouble()
        AlignmentAxis.CENTER_Y -> centerY(bounds)
        AlignmentAxis.BOTTOM -> (bounds.y + bounds.height).toDouble()
    }

    private fun axisCenter(bounds: SceneBounds, axis: DistributionAxis): Double = when (axis) {
        DistributionAxis.HORIZONTAL -> centerX(bounds)
        DistributionAxis.VERTICAL -> centerY(bounds)
    }

    private fun centerX(bounds: SceneBounds): Double = bounds.x + bounds.width / 2.0
    private fun centerY(bounds: SceneBounds): Double = bounds.y + bounds.height / 2.0
    private fun quantize(value: Double): Int = floor(value + 0.5).toInt()
    private fun SceneBounds.translate(dx: Int, dy: Int): SceneBounds = SceneBounds(x + dx, y + dy, width, height)
    private fun failure(subject: String, problem: String, correction: String, code: String) = PlacementOperationPlanningFailure(subject, problem, correction, code)
}
