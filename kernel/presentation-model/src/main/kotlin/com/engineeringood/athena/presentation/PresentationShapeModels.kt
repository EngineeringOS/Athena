package com.engineeringood.athena.presentation

/**
 * Backend-neutral shape command vocabulary used inside primitive presentation atoms.
 *
 * Backend-specific draw trees remain backend-owned. These commands only define the governed
 * presentation language that multiple backends may interpret.
 */
sealed interface PresentationShapeCommand

/**
 * Outlined rectangle command.
 */
data class PresentationStrokeRectangle(
    val bounds: PresentationBounds,
    val strokeTokenKey: String = "stroke",
    val strokeWidthTokenKey: String = "strokeWidth",
    val radius: Int = 0,
) : PresentationShapeCommand

/**
 * Straight line command.
 */
data class PresentationStrokeLine(
    val start: PresentationPoint,
    val end: PresentationPoint,
    val strokeTokenKey: String = "stroke",
    val strokeWidthTokenKey: String = "strokeWidth",
) : PresentationShapeCommand

/**
 * Circle command that may render stroke-only or filled output depending on token usage.
 */
data class PresentationCircle(
    val center: PresentationPoint,
    val radius: Int,
    val strokeTokenKey: String = "stroke",
    val strokeWidthTokenKey: String = "strokeWidth",
    val fillTokenKey: String? = null,
) : PresentationShapeCommand

/**
 * Backend-neutral SVG path command for primitives that need richer IEC-like contours.
 *
 * The command carries only the governed path data and token references. Backend-specific symbol
 * libraries, macro expansion, and draw trees remain outside the Presentation IR boundary.
 */
data class PresentationSvgPath(
    val pathData: String,
    val strokeTokenKey: String = "stroke",
    val strokeWidthTokenKey: String = "strokeWidth",
    val fillTokenKey: String? = null,
) : PresentationShapeCommand
