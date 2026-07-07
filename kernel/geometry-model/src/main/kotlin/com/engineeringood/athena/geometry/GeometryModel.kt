package com.engineeringood.athena.geometry

import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Projection-local identifier for one geometry element.
 *
 * It exists for renderer structure only and remains secondary to canonical semantic identity.
 */
@JvmInline
value class GeometryElementId(val value: String) {
    override fun toString(): String = value
}

/**
 * Small renderer-facing geometry kinds for the first explicit geometry contract slice.
 */
enum class GeometryElementKind {
    BOX,
    PATH,
    LABEL,
}

/**
 * One absolute geometry point in the renderer-facing projection plane.
 */
data class GeometryPoint(
    val x: Int,
    val y: Int,
)

/**
 * Axis-aligned bounds used by the initial geometry contract surface.
 */
data class GeometryBounds(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/**
 * Explicit geometry intermediate representation document derived downstream from layout intent for one supported view.
 */
data class GeometryDocument(
    val viewId: String,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val elements: List<GeometryElement>,
)

/**
 * One renderer-facing geometry item anchored to canonical semantic identity.
 */
data class GeometryElement(
    val elementId: GeometryElementId,
    val semanticId: StableSemanticIdentity,
    val kind: GeometryElementKind,
    val bounds: GeometryBounds,
    val label: String? = null,
    val points: List<GeometryPoint> = emptyList(),
)
