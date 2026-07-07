package com.engineeringood.athena.renderer.svg

import com.engineeringood.athena.geometry.GeometryBounds
import com.engineeringood.athena.geometry.GeometryElementKind
import com.engineeringood.athena.geometry.GeometryDocument
import com.engineeringood.athena.geometry.GeometryElement
import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.geometry.GeometryPoint
import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals

class SvgRendererModuleMarkerTest {
    @Test
    fun `reports svg renderer module marker`() {
        assertEquals("kernel:svg-renderer", SvgRendererModuleMarker().moduleName)
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

    @Test
    fun `renders deterministic svg directly from geometry ir`() {
        val geometry = GeometryDocument(
            viewId = "cabinet",
            canvasWidth = 480,
            canvasHeight = 172,
            elements = listOf(
                GeometryElement(
                    elementId = GeometryElementId("cabinet/geometry/box/component_PLC1"),
                    semanticId = StableSemanticIdentity("component:PLC1"),
                    kind = GeometryElementKind.BOX,
                    bounds = GeometryBounds(x = 40, y = 60, width = 140, height = 72),
                    label = "PLC1",
                ),
                GeometryElement(
                    elementId = GeometryElementId("cabinet/geometry/label/port_PLC1_out"),
                    semanticId = StableSemanticIdentity("port:PLC1.out"),
                    kind = GeometryElementKind.LABEL,
                    bounds = GeometryBounds(x = 56, y = 78, width = 48, height = 16),
                    label = "out",
                ),
                GeometryElement(
                    elementId = GeometryElementId("cabinet/geometry/box/component_M1"),
                    semanticId = StableSemanticIdentity("component:M1"),
                    kind = GeometryElementKind.BOX,
                    bounds = GeometryBounds(x = 300, y = 60, width = 140, height = 72),
                    label = "M1",
                ),
                GeometryElement(
                    elementId = GeometryElementId("cabinet/geometry/label/port_M1_in"),
                    semanticId = StableSemanticIdentity("port:M1.in"),
                    kind = GeometryElementKind.LABEL,
                    bounds = GeometryBounds(x = 316, y = 78, width = 48, height = 16),
                    label = "in",
                ),
                GeometryElement(
                    elementId = GeometryElementId("cabinet/geometry/path/connection_PLC1_out_M1_in"),
                    semanticId = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                    kind = GeometryElementKind.PATH,
                    bounds = GeometryBounds(x = 104, y = 86, width = 212, height = 1),
                    points = listOf(
                        GeometryPoint(x = 104, y = 86),
                        GeometryPoint(x = 210, y = 86),
                        GeometryPoint(x = 210, y = 86),
                        GeometryPoint(x = 316, y = 86),
                    ),
                ),
            ),
        )

        assertEquals(
            """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 480 172" width="480" height="172">
                  <title>DemoCabinet</title>
                  <text x="40" y="28" class="system-label">DemoCabinet</text>
                  <line x1="104" y1="86" x2="316" y2="86" class="connection" />
                  <rect x="40" y="60" width="140" height="72" rx="8" ry="8" class="component" />
                  <text x="52" y="88" class="label">PLC1</text>
                  <rect x="300" y="60" width="140" height="72" rx="8" ry="8" class="component" />
                  <text x="312" y="88" class="label">M1</text>
                </svg>
            """.trimIndent(),
            SvgRenderer().render("DemoCabinet", geometry),
        )
    }
}
