package com.engineeringood.athena.presentation

class PresentationPaintPlan(
    items: List<PresentationPaintItem>,
) {
    val items: List<PresentationPaintItem> = items.sortedWith(compareBy({ item -> item.order }, { item -> item.targetId }))

    init {
        require(this.items.map { item -> item.targetId }.distinct().size == this.items.size) {
            "Presentation paint plan target ids must be unique."
        }
    }
}

data class PresentationPaintItem(
    val itemId: String,
    val targetId: String,
    val kind: String,
    val visible: Boolean,
    val order: Int,
) {
    init {
        require(itemId.isNotBlank()) { "Presentation paint item id must not be blank." }
        require(targetId.isNotBlank()) { "Presentation paint target id must not be blank." }
        require(kind.isNotBlank()) { "Presentation paint item kind must not be blank." }
        require(order >= 0) { "Presentation paint order must not be negative." }
    }
}
