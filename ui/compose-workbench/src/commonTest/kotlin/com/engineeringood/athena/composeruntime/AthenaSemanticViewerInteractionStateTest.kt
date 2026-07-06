package com.engineeringood.athena.composeruntime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AthenaSemanticViewerInteractionStateTest {
    @Test
    fun `pan updates camera offset without mutating selection or viewport`() {
        val initial = AthenaSemanticViewerInteractionState(
            viewport = AthenaSemanticViewerViewport(width = 1280, height = 800),
            camera = AthenaSemanticViewerCamera(offsetX = 12f, offsetY = -4f, zoom = 1.25f),
            selection = AthenaSemanticViewerSelection(semanticId = "component:PLC1"),
        )

        val moved = initial.panBy(deltaX = 30f, deltaY = -18f)

        assertEquals(42f, moved.camera.offsetX)
        assertEquals(-22f, moved.camera.offsetY)
        assertEquals(1.25f, moved.camera.zoom)
        assertEquals("component:PLC1", moved.selection?.semanticId)
        assertEquals(1280, moved.viewport.width)
        assertEquals(800, moved.viewport.height)
    }

    @Test
    fun `zoom keeps the pointer anchored to the same world point`() {
        val initial = AthenaSemanticViewerInteractionState(
            camera = AthenaSemanticViewerCamera(offsetX = 18f, offsetY = 24f, zoom = 1.2f),
        )
        val worldBefore = initial.camera.screenToWorld(screenX = 240f, screenY = 180f)

        val zoomed = initial.zoomBy(
            focusX = 240f,
            focusY = 180f,
            zoomFactor = 1.5f,
        )
        val worldAfter = zoomed.camera.screenToWorld(screenX = 240f, screenY = 180f)

        assertEquals(1.8f, zoomed.camera.zoom, 0.001f)
        assertEquals(worldBefore.x, worldAfter.x, 0.001f)
        assertEquals(worldBefore.y, worldAfter.y, 0.001f)
    }

    @Test
    fun `selectAt chooses a component by screen-space hit-testing`() {
        val selected = AthenaSemanticViewerInteractionState()
            .selectAt(
                scene = demoScene(),
                screenX = 96f,
                screenY = 96f,
            )

        assertEquals("component:PLC1", selected.selection?.semanticId)
    }

    @Test
    fun `selectAt chooses a connection when no component is under the pointer`() {
        val selected = AthenaSemanticViewerInteractionState()
            .selectAt(
                scene = demoScene(),
                screenX = 240f,
                screenY = 96f,
            )

        assertEquals("connection:PLC1.out->M1.in", selected.selection?.semanticId)
    }

    @Test
    fun `selectAt clears selection when clicking empty space`() {
        val selected = AthenaSemanticViewerInteractionState(
            selection = AthenaSemanticViewerSelection(semanticId = "component:PLC1"),
        ).selectAt(
            scene = demoScene(),
            screenX = 24f,
            screenY = 24f,
        )

        assertNull(selected.selection)
    }

    private fun demoScene(): AthenaSemanticViewerScene {
        return AthenaSemanticViewerScene(
            systemName = "DemoCabinet",
            canvasWidth = 480,
            canvasHeight = 172,
            components = listOf(
                AthenaSemanticViewerComponentBox(
                    semanticId = "component:PLC1",
                    label = "PLC1",
                    x = 40,
                    y = 60,
                    width = 140,
                    height = 72,
                ),
                AthenaSemanticViewerComponentBox(
                    semanticId = "component:M1",
                    label = "M1",
                    x = 300,
                    y = 60,
                    width = 140,
                    height = 72,
                ),
            ),
            connections = listOf(
                AthenaSemanticViewerConnectionLine(
                    semanticId = "connection:PLC1.out->M1.in",
                    x1 = 180,
                    y1 = 96,
                    x2 = 300,
                    y2 = 96,
                ),
            ),
        )
    }
}
