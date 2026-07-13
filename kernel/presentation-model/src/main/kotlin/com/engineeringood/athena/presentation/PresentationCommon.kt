package com.engineeringood.athena.presentation

/**
 * Stable pack identifier for one governed presentation vocabulary contribution.
 */
@JvmInline
value class PresentationPackId(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable identifier for one primitive presentation atom.
 */
@JvmInline
value class PresentationPrimitiveId(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable identifier for one composite presentation assembly.
 */
@JvmInline
value class PresentationCompositeId(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable occurrence identifier published by one derived presentation document.
 */
@JvmInline
value class PresentationOccurrenceId(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable identifier for one named text slot.
 */
@JvmInline
value class PresentationTextSlotId(val value: String) {
    override fun toString(): String = value
}

/**
 * Stable identifier for one named presentation anchor alias.
 */
@JvmInline
value class PresentationAnchorAlias(val value: String) {
    override fun toString(): String = value
}

/**
 * Directional orientation supported by one occurrence or definition.
 */
enum class PresentationOrientation {
    HORIZONTAL,
    VERTICAL,
}

/**
 * Render-layer hint for one downstream presentation occurrence.
 */
enum class PresentationLayer {
    DEVICE,
    LABEL,
    CONNECTION,
    REFERENCE,
}

/**
 * Simple integer point used across presentation definitions and occurrences.
 */
data class PresentationPoint(
    val x: Int,
    val y: Int,
)

/**
 * Integer bounds used for placed presentation occurrences and local part boxes.
 */
data class PresentationBounds(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)
