package com.engineeringood.athena.projection

/**
 * One absolute point in the projection plane.
 */
data class ProjectionPoint(
    val x: Int,
    val y: Int,
)

/**
 * Axis-aligned bounds for one projection node.
 */
data class ProjectionBounds(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)
