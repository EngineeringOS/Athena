package com.engineeringood.athena.renderer.svg

import kotlin.test.Test
import kotlin.test.assertEquals

class SvgRendererModuleMarkerTest {
    @Test
    fun `reports svg renderer module marker`() {
        assertEquals("renderer-svg", SvgRendererModuleMarker().moduleName)
    }

    @Test
    fun `renders deterministic svg from a thin render model`() {
        val model = SvgRenderModel(
            systemName = "DemoCabinet",
            canvasWidth = 480,
            canvasHeight = 172,
            boxes = listOf(
                SvgRenderBox(
                    semanticId = com.engineeringood.athena.ir.StableSemanticIdentity("component:PLC1"),
                    label = "PLC1",
                    x = 40,
                    y = 60,
                    width = 140,
                    height = 72,
                ),
                SvgRenderBox(
                    semanticId = com.engineeringood.athena.ir.StableSemanticIdentity("component:M1"),
                    label = "M1",
                    x = 300,
                    y = 60,
                    width = 140,
                    height = 72,
                ),
            ),
            connections = listOf(
                SvgRenderConnection(
                    semanticId = com.engineeringood.athena.ir.StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    x1 = 180,
                    y1 = 96,
                    x2 = 300,
                    y2 = 96,
                ),
            ),
        )

        assertEquals(
            """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 480 172" width="480" height="172">
                  <title>DemoCabinet</title>
                  <text x="40" y="28" class="system-label">DemoCabinet</text>
                  <line x1="180" y1="96" x2="300" y2="96" class="connection" />
                  <rect x="40" y="60" width="140" height="72" rx="8" ry="8" class="component" />
                  <text x="52" y="88" class="label">PLC1</text>
                  <rect x="300" y="60" width="140" height="72" rx="8" ry="8" class="component" />
                  <text x="312" y="88" class="label">M1</text>
                </svg>
            """.trimIndent(),
            SvgRenderer().render(model),
        )
    }
}
