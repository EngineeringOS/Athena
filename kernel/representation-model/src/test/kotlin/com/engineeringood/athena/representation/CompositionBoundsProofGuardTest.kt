package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CompositionBoundsValidatorTest {
    @Test
    fun `bounds evidence derives viewbox from content plus margin`() {
        val evidence = CompositionBoundsValidator().verify(
            CompositionBoundsValidationInput(
                contentBounds = listOf(
                    CompositionContentBounds("motor", minX = 10, minY = 20, maxX = 54, maxY = 64),
                    CompositionContentBounds("lamp", minX = 70, minY = 18, maxX = 110, maxY = 58),
                ),
                declaredViewBox = CompositionViewBox(minX = 2, minY = 10, width = 116, height = 62),
                governedMargin = 8,
                labels = listOf("M1", "H1"),
                visibleWrapperBorders = false,
            ),
        )

        assertEquals(emptyList(), evidence.diagnostics)
        assertEquals(CompositionViewBox(minX = 2, minY = 10, width = 116, height = 62), evidence.derivedViewBox)
    }

    @Test
    fun `bounds evidence rejects known visual regressions with explicit diagnostics`() {
        val evidence = CompositionBoundsValidator().verify(
            CompositionBoundsValidationInput(
                contentBounds = listOf(
                    CompositionContentBounds("motor", minX = 0, minY = 0, maxX = 44, maxY = 44),
                    CompositionContentBounds("motor-copy", minX = 1940, minY = 0, maxX = 1984, maxY = 44),
                ),
                declaredViewBox = CompositionViewBox(minX = 0, minY = 0, width = 1680, height = 1188),
                governedMargin = 8,
                labels = listOf("POWER", "POWER"),
                visibleWrapperBorders = true,
            ),
        )

        assertFalse(evidence.accepted)
        assertEquals(
            listOf(
                "composition.bounds.hard-coded-viewbox",
                "composition.bounds.offscreen-duplicate",
                "composition.bounds.repeated-label",
                "composition.bounds.wrapper-border-visible",
            ),
            evidence.diagnostics.map { diagnostic -> diagnostic.code },
        )
    }
}
