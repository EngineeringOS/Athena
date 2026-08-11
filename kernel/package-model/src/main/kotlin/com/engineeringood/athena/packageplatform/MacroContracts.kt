package com.engineeringood.athena.packageplatform

/** Relative representation placement inside a reusable Macro. */
data class MacroChildPlacement(
    val childId: PackageItemIdentity,
    val x: Int,
    val y: Int,
    val rotationDegrees: Int = 0,
) {
    init { require(rotationDegrees in 0..359) }
}

data class MacroBindingReference(
    val childId: PackageItemIdentity,
    val functionSlotId: String,
    val bindingRole: String,
) {
    init { require(functionSlotId.isNotBlank() && bindingRole.isNotBlank()) }
}

data class RepresentationMacro(
    val macroId: PackageItemIdentity,
    val children: List<MacroChildPlacement>,
    val bindings: List<MacroBindingReference>,
) {
    init {
        require(macroId.itemId.isNotBlank() && children.isNotEmpty())
        require(children.map { it.childId.key }.distinct().size == children.size)
        require(bindings.map { "${it.childId.key}#${it.functionSlotId}#${it.bindingRole}" }.distinct().size == bindings.size)
        require(bindings.all { reference -> children.any { it.childId == reference.childId } })
    }
}

data class MacroTransform(
    val offsetX: Int,
    val offsetY: Int,
    val rotationDegrees: Int = 0,
) {
    init { require(rotationDegrees in 0..359) }
}

data class MacroInsertionRequest(
    val macroId: PackageItemIdentity,
    val sheetId: String,
    val functionIdsBySlot: Map<String, String>,
    val transform: MacroTransform,
    val operationId: String,
) {
    init {
        require(sheetId.isNotBlank() && operationId.isNotBlank())
        require(functionIdsBySlot.keys.all(String::isNotBlank) && functionIdsBySlot.values.all(String::isNotBlank))
    }
}

data class ResolvedMacroOccurrence(
    val occurrenceId: String,
    val childId: PackageItemIdentity,
    val functionId: String?,
    val x: Int,
    val y: Int,
    val rotationDegrees: Int,
)

