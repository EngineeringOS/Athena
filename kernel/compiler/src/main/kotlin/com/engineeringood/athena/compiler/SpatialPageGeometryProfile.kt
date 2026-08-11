package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionSheetGrid
import com.engineeringood.athena.projection.ProjectionSheetPageSize
import com.engineeringood.athena.spatial.SpatialRect
import kotlin.math.max

/** Renderer-neutral page geometry derived from one authored Sheet grid. */
internal data class SpatialPageGeometryProfile(
    val extent: SpatialRect,
    val drawingArea: SpatialRect,
    val groupingPadding: Int,
)

/**
 * Owns logical page margins and standard page aspect for the Sheet Companion coordinate system.
 * Page pixels remain an adapter concern; these values only make the logical frame and drawing
 * origin explicit.
 */
internal object SpatialPageGeometryProfiles {
    private const val LEFT_MARGIN = 4
    private const val TOP_MARGIN = 4
    private const val RIGHT_MARGIN = 8
    private const val BOTTOM_MARGIN = 2

    fun logical(
        grid: ProjectionSheetGrid,
        pageSize: ProjectionSheetPageSize,
    ): SpatialPageGeometryProfile {
        val drawingWidth = grid.columns.toLong() * grid.subdivisions.toLong()
        val drawingHeight = grid.rows.toLong() * grid.subdivisions.toLong()
        require(drawingWidth in 1L..Int.MAX_VALUE && drawingHeight in 1L..Int.MAX_VALUE) {
            "Sheet grid logical extent exceeds supported SceneUnit range."
        }
        val drawing = SpatialRect(
            x = LEFT_MARGIN,
            y = TOP_MARGIN,
            width = drawingWidth.toInt(),
            height = drawingHeight.toInt(),
        )
        val baseWidth = drawing.right + RIGHT_MARGIN
        val baseHeight = drawing.bottom + BOTTOM_MARGIN
        val ratio = pageRatio(pageSize)
        val pageWidth: Int
        val pageHeight: Int
        if (ratio == null) {
            pageWidth = baseWidth
            pageHeight = baseHeight
        } else if (ratio.first >= ratio.second) {
            pageWidth = max(baseWidth, ceilMultiply(baseHeight, ratio.first, ratio.second))
            pageHeight = baseHeight
        } else {
            pageWidth = baseWidth
            pageHeight = max(baseHeight, ceilMultiply(baseWidth, ratio.second, ratio.first))
        }
        return SpatialPageGeometryProfile(
            extent = SpatialRect(
                x = 0,
                y = 0,
                width = pageWidth,
                height = pageHeight,
            ),
            drawingArea = drawing,
            groupingPadding = 0,
        )
    }

    private fun pageRatio(pageSize: ProjectionSheetPageSize): Pair<Int, Int>? {
        val base = when (pageSize.format.uppercase()) {
            "A0" -> 1189 to 841
            "A1" -> 841 to 594
            "A2" -> 594 to 420
            "A3" -> 420 to 297
            "A4" -> 297 to 210
            "A5" -> 210 to 148
            else -> return null
        }
        return if (pageSize.orientation.equals("portrait", ignoreCase = true)) {
            base.second to base.first
        } else {
            base
        }
    }

    private fun ceilMultiply(value: Int, numerator: Int, denominator: Int): Int =
        ((value.toLong() * numerator.toLong()) + denominator - 1L)
            .div(denominator.toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
}
