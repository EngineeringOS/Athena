package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure

/**
 * Runtime-owned projection for one active project viewer request.
 */
sealed interface AthenaRuntimeViewerProjection {
    /**
     * Runtime project name associated with the projection request.
     */
    val projectName: String
}

/**
 * Successful runtime viewer projection that remains derived from canonical compiler output.
 */
data class AthenaRuntimeViewerReadyProjection(
    override val projectName: String,
    val scene: AthenaRuntimeViewerScene,
) : AthenaRuntimeViewerProjection

/**
 * Runtime viewer projection that could not derive a renderable scene.
 */
data class AthenaRuntimeViewerUnavailableProjection(
    override val projectName: String,
    val reason: String,
) : AthenaRuntimeViewerProjection

/**
 * Runtime-facing scene model exposed to desktop or web viewers without leaking compiler internals.
 */
data class AthenaRuntimeViewerScene(
    val systemName: String,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val components: List<AthenaRuntimeViewerComponentBox>,
    val connections: List<AthenaRuntimeViewerConnectionLine>,
)

/**
 * Runtime-facing component box derived from canonical render output.
 */
data class AthenaRuntimeViewerComponentBox(
    val semanticId: String,
    val label: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/**
 * Runtime-facing connection line derived from canonical render output.
 */
data class AthenaRuntimeViewerConnectionLine(
    val semanticId: String,
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int,
)

/**
 * Derives a viewer-safe runtime projection from the active project compilation result.
 */
fun AthenaExecutionContext.projectViewerProjection(): AthenaRuntimeViewerProjection {
    val session = projectProjectionSession()
    return session.activeProjection.toViewerProjection(projectName = session.projectName)
}
