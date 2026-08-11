package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.interaction.AlignOccurrences
import com.engineeringood.athena.interaction.AlignmentAxis
import com.engineeringood.athena.interaction.DistributeOccurrences
import com.engineeringood.athena.interaction.DistributionAxis
import com.engineeringood.athena.interaction.LockAction
import com.engineeringood.athena.interaction.MoveOccurrence
import com.engineeringood.athena.interaction.SheetPoint
import com.engineeringood.athena.interaction.SnapOccurrenceToGrid
import com.engineeringood.athena.presentation.AthenaDiagramScene
import com.engineeringood.athena.presentation.InputRevision
import com.engineeringood.athena.presentation.ResolvedStyle
import com.engineeringood.athena.presentation.SceneBounds
import com.engineeringood.athena.presentation.SceneDigest
import com.engineeringood.athena.presentation.SceneElementId
import com.engineeringood.athena.presentation.SceneId
import com.engineeringood.athena.presentation.SceneOccurrence
import com.engineeringood.athena.presentation.ScenePage
import com.engineeringood.athena.presentation.ScenePoint
import com.engineeringood.athena.presentation.SceneSnapGrid
import com.engineeringood.athena.presentation.StyleId
import com.engineeringood.athena.presentation.TraceId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlacementOperationPlannerTest {
    private val planner = PlacementOperationPlanner()

    @Test
    fun `move and snap validate accepted Sheet points and retain requested lock intent`() {
        val scene = scene(occurrence("Q1", 4, 8, 2, 2))

        val move = planner.plan(
            scene,
            MoveOccurrence(SHEET, "Q1", SheetPoint(10, 12), LockAction.LOCK),
        ).single()
        assertEquals("Q1", move.occurrenceId)
        assertEquals(SheetPoint(10, 12), move.point)
        assertEquals(LockAction.LOCK, move.lockAction)

        val snap = planner.plan(
            scene,
            SnapOccurrenceToGrid(SHEET, "Q1", SheetPoint(1, 1)),
        ).single()
        assertEquals(SheetPoint(1, 1), snap.point)
        assertEquals(LockAction.PRESERVE, snap.lockAction)

        val outside = assertFailsWith<PlacementOperationPlanningFailure> {
            planner.plan(scene, MoveOccurrence(SHEET, "Q1", SheetPoint(80, 1), LockAction.PRESERVE))
        }
        assertEquals("edit.operation.bounds-outside-grid", outside.code)
    }

    @Test
    fun `align calculates all six axes from accepted bounds and keeps lock state`() {
        val scene = scene(
            occurrence("alpha", 4, 8, 2, 2),
            occurrence("bravo", 12, 16, 4, 4),
            occurrence("charlie", 20, 24, 2, 6),
        )
        val ids = listOf("alpha", "bravo", "charlie")

        assertEquals(
            mapOf("alpha" to SheetPoint(5, 9), "bravo" to SheetPoint(5, 17), "charlie" to SheetPoint(5, 25)),
            points(planner.plan(scene, AlignOccurrences(SHEET, ids, AlignmentAxis.LEFT))),
        )
        assertEquals(
            mapOf("alpha" to SheetPoint(21, 9), "bravo" to SheetPoint(19, 17), "charlie" to SheetPoint(21, 25)),
            points(planner.plan(scene, AlignOccurrences(SHEET, ids, AlignmentAxis.RIGHT))),
        )
        assertEquals(
            mapOf("alpha" to SheetPoint(13, 9), "bravo" to SheetPoint(12, 17), "charlie" to SheetPoint(13, 25)),
            points(planner.plan(scene, AlignOccurrences(SHEET, ids, AlignmentAxis.CENTER_X))),
        )
        assertEquals(
            mapOf("alpha" to SheetPoint(5, 9), "bravo" to SheetPoint(13, 9), "charlie" to SheetPoint(21, 9)),
            points(planner.plan(scene, AlignOccurrences(SHEET, ids, AlignmentAxis.TOP))),
        )
        assertEquals(
            mapOf("alpha" to SheetPoint(5, 29), "bravo" to SheetPoint(13, 27), "charlie" to SheetPoint(21, 25)),
            points(planner.plan(scene, AlignOccurrences(SHEET, ids, AlignmentAxis.BOTTOM))),
        )
        assertEquals(
            mapOf("alpha" to SheetPoint(5, 19), "bravo" to SheetPoint(13, 18), "charlie" to SheetPoint(21, 17)),
            points(planner.plan(scene, AlignOccurrences(SHEET, ids, AlignmentAxis.CENTER_Y))),
        )
        assertEquals(
            setOf(LockAction.PRESERVE),
            planner.plan(scene, AlignOccurrences(SHEET, ids, AlignmentAxis.LEFT)).map { it.lockAction }.toSet(),
        )
    }

    @Test
    fun `distribute is input-order-independent quantized and breaks center ties by occurrence identity`() {
        val scene = scene(
            occurrence("alpha", 0, 0, 2, 2),
            occurrence("bravo", 6, 0, 2, 2),
            occurrence("charlie", 6, 8, 2, 2),
            occurrence("delta", 20, 12, 2, 2),
        )
        val unordered = listOf("delta", "charlie", "alpha", "bravo").sorted()

        val horizontal = planner.plan(scene, DistributeOccurrences(SHEET, unordered, DistributionAxis.HORIZONTAL))
        assertEquals(
            mapOf("alpha" to SheetPoint(1, 1), "bravo" to SheetPoint(8, 1), "charlie" to SheetPoint(14, 9), "delta" to SheetPoint(21, 13)),
            points(horizontal),
        )
        assertEquals(
            horizontal,
            planner.plan(scene, DistributeOccurrences(SHEET, unordered.reversed().sorted(), DistributionAxis.HORIZONTAL)),
        )

        val vertical = planner.plan(scene, DistributeOccurrences(SHEET, unordered, DistributionAxis.VERTICAL))
        assertEquals(
            mapOf("alpha" to SheetPoint(1, 1), "bravo" to SheetPoint(7, 5), "charlie" to SheetPoint(7, 9), "delta" to SheetPoint(21, 13)),
            points(vertical),
        )
    }

    @Test
    fun `rejects missing occurrences wrong sheets and moved bounds outside drawing area`() {
        val scene = scene(
            occurrence("alpha", 0, 0, 2, 2),
            occurrence("bravo", 60, 0, 4, 2),
        )

        assertEquals(
            "edit.operation.occurrence-missing",
            assertFailsWith<PlacementOperationPlanningFailure> {
                planner.plan(scene, AlignOccurrences(SHEET, listOf("alpha", "missing"), AlignmentAxis.LEFT))
            }.code,
        )
        assertEquals(
            "edit.operation.sheet-unknown",
            assertFailsWith<PlacementOperationPlanningFailure> {
                planner.plan(scene, MoveOccurrence("other-sheet", "alpha", SheetPoint(1, 1), LockAction.PRESERVE))
            }.code,
        )
        assertEquals(
            "edit.operation.bounds-outside-grid",
            assertFailsWith<PlacementOperationPlanningFailure> {
                planner.plan(scene, MoveOccurrence(SHEET, "bravo", SheetPoint(80, 1), LockAction.PRESERVE))
            }.code,
        )
    }

    private fun points(plan: List<PlannedPlacement>): Map<String, SheetPoint> =
        plan.associate { it.occurrenceId to it.point }

    private fun scene(vararg occurrences: SceneOccurrence) = AthenaDiagramScene(
        sceneId = SceneId("scene:sha256:${"1".repeat(64)}"),
        inputRevision = InputRevision("input:sha256:${"2".repeat(64)}"),
        sceneDigest = SceneDigest.uncomputed(),
        page = ScenePage(SceneBounds(0, 0, 64, 64), SceneBounds(0, 0, 64, 64)),
        snapGrid = SceneSnapGrid(SHEET, step = 1, drawingOrigin = ScenePoint(0, 0)),
        styles = listOf(ResolvedStyle(STYLE, "#000000ff", "#ffffffff", 1)),
        assets = emptyList(),
        occurrences = occurrences.toList(),
        connections = emptyList(),
        decorations = emptyList(),
        traces = emptyList(),
    )

    private fun occurrence(id: String, x: Int, y: Int, width: Int, height: Int) = SceneOccurrence(
        elementId = SceneElementId("occurrence:sha256:${digest(id)}"),
        occurrenceId = id,
        subjectId = "function:$id",
        bounds = SceneBounds(x, y, width, height),
        placementAnchor = ScenePoint(x, y),
        zIndex = 100,
        styleId = STYLE,
        traceId = TraceId("trace:sha256:${digest(id)}"),
    )

    private fun digest(id: String): String = when (id) {
        "alpha" -> "a"
        "bravo" -> "b"
        "charlie" -> "c"
        "delta" -> "d"
        "Q1" -> "e"
        else -> "f"
    }.repeat(64)

    private companion object {
        const val SHEET = "sheet-main"
        val STYLE = StyleId("style:sha256:${"3".repeat(64)}")
    }
}
