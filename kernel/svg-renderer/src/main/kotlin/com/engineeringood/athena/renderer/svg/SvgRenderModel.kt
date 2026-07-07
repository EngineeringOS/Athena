package com.engineeringood.athena.renderer.svg

import com.engineeringood.athena.ir.StableSemanticIdentity

/** Thin viewer-facing document derived from explicit geometry for runtime-safe scene projection. */
data class SvgRenderModel(
    val systemName: String,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val boxes: List<SvgRenderBox>,
    val connections: List<SvgRenderConnection>,
)

/** Simple render-facing rectangle representing one semantic component box. */
data class SvgRenderBox(
    val semanticId: StableSemanticIdentity,
    val label: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/** Simple render-facing connection line representing one semantic connection edge. */
data class SvgRenderConnection(
    val semanticId: StableSemanticIdentity,
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int,
)
