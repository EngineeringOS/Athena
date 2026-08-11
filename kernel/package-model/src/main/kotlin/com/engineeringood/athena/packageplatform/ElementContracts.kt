package com.engineeringood.athena.packageplatform

data class ElementChildReference(
    val childId: PackageItemIdentity,
    val order: Int,
) {
    init {
        require(order >= 0)
    }
}

data class FunctionSlot(
    val slotId: String,
    val role: String,
) {
    init {
        require(slotId.matches(Regex("^[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*$")))
        require(role.isNotBlank())
    }
}

data class ElementPort(
    val portKey: String,
    val childItemId: PackageItemIdentity,
    val anchorKey: String,
    val functionSlotId: String,
    val public: Boolean = true,
) {
    init {
        require(portKey.matches(Regex("^[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*$")))
        require(anchorKey.isNotBlank())
        require(functionSlotId.isNotBlank())
    }
}

data class ElementComposition(
    val children: List<ElementChildReference>,
    val functionSlots: List<FunctionSlot>,
    val ports: List<ElementPort>,
) {
    init {
        require(children.isNotEmpty())
        require(children.map { it.childId.key }.distinct().size == children.size)
        require(functionSlots.map(FunctionSlot::slotId).distinct().size == functionSlots.size)
        require(ports.map(ElementPort::portKey).distinct().size == ports.size)
        require(ports.all { port -> functionSlots.any { it.slotId == port.functionSlotId } })
    }
}
