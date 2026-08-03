package com.engineeringood.athena.layout.engine

data class RuleBasedLayoutPoint(
    val x: Int,
    val y: Int,
)

data class RuleBasedLayoutSize(
    val width: Int,
    val height: Int,
) {
    init {
        require(width > 0 && height > 0) {
            "Rule-based layout items must have positive size."
        }
    }
}

data class RuleBasedLayoutItem<T>(
    val stableId: String,
    val groupId: String,
    val groupRank: Int,
    val itemRank: Int,
    val size: RuleBasedLayoutSize,
    val payload: T,
) {
    init {
        require(stableId.isNotBlank()) { "Rule-based layout items require a stable identity." }
        require(groupId.isNotBlank()) { "Rule-based layout items require a stable group identity." }
    }
}

data class RuleBasedLayoutContext<T>(
    val item: RuleBasedLayoutItem<T>,
    val groupIndex: Int,
    val groupCount: Int,
    val itemIndex: Int,
    val itemCount: Int,
)

data class RuleBasedLayoutPlacement<T>(
    val stableId: String,
    val groupId: String,
    val position: RuleBasedLayoutPoint,
    val size: RuleBasedLayoutSize,
    val payload: T,
    val context: RuleBasedLayoutContext<T>,
)

class RuleBasedLayoutEngine {
    fun <T> place(
        items: List<RuleBasedLayoutItem<T>>,
        geometryPolicy: (RuleBasedLayoutContext<T>) -> RuleBasedLayoutPoint,
    ): List<RuleBasedLayoutPlacement<T>> {
        require(items.map { item -> item.stableId }.distinct().size == items.size) {
            "Rule-based layout item identities must be unique."
        }
        val groups = items.groupBy { item -> item.groupId }
            .map { (groupId, groupItems) ->
                require(groupItems.map { item -> item.groupRank }.distinct().size == 1) {
                    "Rule-based layout group $groupId must use one group rank."
                }
                groupItems
            }
            .sortedWith(
                compareBy<List<RuleBasedLayoutItem<T>>>(
                    { groupItems -> groupItems.first().groupRank },
                    { groupItems -> groupItems.first().groupId },
                ),
            )
        return groups.flatMapIndexed { groupIndex, groupItems ->
            val orderedItems = groupItems.sortedWith(
                compareBy<RuleBasedLayoutItem<T>>(
                    { item -> item.itemRank },
                    { item -> item.stableId },
                ),
            )
            orderedItems.mapIndexed { itemIndex, item ->
                val context = RuleBasedLayoutContext(
                    item = item,
                    groupIndex = groupIndex,
                    groupCount = groups.size,
                    itemIndex = itemIndex,
                    itemCount = orderedItems.size,
                )
                RuleBasedLayoutPlacement(
                    stableId = item.stableId,
                    groupId = item.groupId,
                    position = geometryPolicy(context),
                    size = item.size,
                    payload = item.payload,
                    context = context,
                )
            }
        }
    }
}
