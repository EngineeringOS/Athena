package com.engineeringood.athena.layout.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class RuleBasedLayoutEngineTest {
    @Test
    fun `engine canonicalizes groups and items before applying geometry policy`() {
        val items = listOf(
            RuleBasedLayoutItem(
                stableId = "load",
                groupId = "right",
                groupRank = 1,
                itemRank = 0,
                size = RuleBasedLayoutSize(width = 80, height = 40),
                payload = "Load",
            ),
            RuleBasedLayoutItem(
                stableId = "breaker",
                groupId = "left",
                groupRank = 0,
                itemRank = 1,
                size = RuleBasedLayoutSize(width = 80, height = 40),
                payload = "Breaker",
            ),
            RuleBasedLayoutItem(
                stableId = "supply",
                groupId = "left",
                groupRank = 0,
                itemRank = 0,
                size = RuleBasedLayoutSize(width = 80, height = 40),
                payload = "Supply",
            ),
        )

        val placements = RuleBasedLayoutEngine().place(items.reversed()) { context ->
            RuleBasedLayoutPoint(
                x = context.groupIndex * 200,
                y = context.itemIndex * 100,
            )
        }

        assertEquals(listOf("Supply", "Breaker", "Load"), placements.map { placement -> placement.payload })
        assertEquals(
            listOf(
                RuleBasedLayoutPoint(x = 0, y = 0),
                RuleBasedLayoutPoint(x = 0, y = 100),
                RuleBasedLayoutPoint(x = 200, y = 0),
            ),
            placements.map { placement -> placement.position },
        )
        assertEquals(listOf(2, 2, 1), placements.map { placement -> placement.context.itemCount })
        assertEquals(listOf(2, 2, 2), placements.map { placement -> placement.context.groupCount })
        assertEquals(items.first().size, placements.last().size)
    }
}
