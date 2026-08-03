package com.engineeringood.athena.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PresentationPaintPlanTest {
    @Test
    fun `paint item requires target visibility and order`() {
        val item = PresentationPaintItem(
            itemId = "paint:item:shape:one",
            targetId = "paint:occurrence:supply",
            kind = "shape",
            visible = true,
            order = 10,
        )

        assertEquals("paint:occurrence:supply", item.targetId)
        assertEquals(true, item.visible)
        assertEquals(10, item.order)
    }

    @Test
    fun `paint plan order is deterministic`() {
        val plan = PresentationPaintPlan(
            items = listOf(
                PresentationPaintItem("paint:item:label:one", "label:one", "label", visible = true, order = 40),
                PresentationPaintItem("paint:item:shape:one", "shape:one", "shape", visible = true, order = 10),
            ),
        )

        assertEquals(listOf(10, 40), plan.items.map { item -> item.order })
    }

    @Test
    fun `paint item fails closed on missing target or invalid order`() {
        assertFailsWith<IllegalArgumentException> {
            PresentationPaintItem("paint:item:bad", " ", "shape", visible = true, order = 1)
        }
        assertFailsWith<IllegalArgumentException> {
            PresentationPaintItem("paint:item:bad", "shape:one", "shape", visible = true, order = -1)
        }
    }
}
