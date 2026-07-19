package com.engineeringood.athena.representation

@JvmInline
value class GridUnit(val value: Int)

data class PresentationPoint(
    val x: GridUnit,
    val y: GridUnit,
)

data class PresentationSize(
    val width: GridUnit,
    val height: GridUnit,
) {
    init {
        require(width.value > 0) { "Presentation width must be positive." }
        require(height.value > 0) { "Presentation height must be positive." }
    }
}

data class PresentationBounds(
    val width: GridUnit,
    val height: GridUnit,
) {
    init {
        require(width.value > 0) { "Presentation bounds width must be positive." }
        require(height.value > 0) { "Presentation bounds height must be positive." }
    }
}

data class PresentationHotspot(
    val point: PresentationPoint,
)
