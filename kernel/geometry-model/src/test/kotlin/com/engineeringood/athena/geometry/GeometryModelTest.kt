package com.engineeringood.athena.geometry

import com.engineeringood.athena.ir.StableSemanticIdentity
import kotlin.test.Test
import kotlin.test.assertEquals

class GeometryModelTest {
    @Test
    fun `keeps geometry elements anchored to semantic identity with renderer-facing points`() {
        val semanticId = StableSemanticIdentity("connection:power-feed")
        val document = GeometryDocument(
            viewId = "wiring",
            canvasWidth = 320,
            canvasHeight = 180,
            elements = listOf(
                GeometryElement(
                    elementId = GeometryElementId("geometry-element-1"),
                    semanticId = semanticId,
                    kind = GeometryElementKind.PATH,
                    bounds = GeometryBounds(
                        x = 10,
                        y = 20,
                        width = 120,
                        height = 8,
                    ),
                    points = listOf(
                        GeometryPoint(x = 10, y = 24),
                        GeometryPoint(x = 130, y = 24),
                    ),
                ),
            ),
        )

        assertEquals(semanticId, document.elements.single().semanticId)
        assertEquals(GeometryElementKind.PATH, document.elements.single().kind)
        assertEquals(320, document.canvasWidth)
        assertEquals(listOf(10, 130), document.elements.single().points.map { point -> point.x })
    }
}
