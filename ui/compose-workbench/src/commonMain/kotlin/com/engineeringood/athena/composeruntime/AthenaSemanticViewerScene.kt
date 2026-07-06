package com.engineeringood.athena.composeruntime

/**
 * Read-only semantic viewer scene derived from runtime-owned semantic and render outputs.
 */
data class AthenaSemanticViewerScene(
    val systemName: String,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val components: List<AthenaSemanticViewerComponentBox>,
    val connections: List<AthenaSemanticViewerConnectionLine>,
) {
    /**
     * Total component boxes visible in this scene.
     */
    val componentCount: Int
        get() = components.size

    /**
     * Total connections visible in this scene.
     */
    val connectionCount: Int
        get() = connections.size
}

/**
 * One derived component box displayed by the semantic viewer.
 */
data class AthenaSemanticViewerComponentBox(
    val semanticId: String,
    val label: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/**
 * One derived connection line displayed by the semantic viewer.
 */
data class AthenaSemanticViewerConnectionLine(
    val semanticId: String,
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int,
)
