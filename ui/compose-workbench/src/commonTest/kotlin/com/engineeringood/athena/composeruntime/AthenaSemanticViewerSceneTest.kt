package com.engineeringood.athena.composeruntime

import kotlin.test.Test
import kotlin.test.assertEquals

class AthenaSemanticViewerSceneTest {
    @Test
    fun `reports stable component and connection counts`() {
        val scene = AthenaSemanticViewerScene(
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

        assertEquals(2, scene.componentCount)
        assertEquals(1, scene.connectionCount)
    }
}
