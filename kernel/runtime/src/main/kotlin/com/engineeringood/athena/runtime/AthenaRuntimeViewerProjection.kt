package com.engineeringood.athena.runtime

import com.engineeringood.athena.compiler.CompilerCompilationParseFailure
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.compiler.CompilerRenderingBlocked
import com.engineeringood.athena.compiler.CompilerRenderingSuccess

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
    return when (val compilation = compileActiveProject()) {
        is CompilerCompilationParseFailure -> AthenaRuntimeViewerUnavailableProjection(
            projectName = project.name,
            reason = compilation.diagnostics.joinToString(separator = "; ") { diagnostic -> diagnostic.message },
        )

        is CompilerCompilationSuccess -> when (val rendering = compilation.rendering) {
            is CompilerRenderingBlocked -> AthenaRuntimeViewerUnavailableProjection(
                projectName = project.name,
                reason = rendering.reason,
            )

            is CompilerRenderingSuccess -> AthenaRuntimeViewerReadyProjection(
                projectName = project.name,
                scene = AthenaRuntimeViewerScene(
                    systemName = rendering.model.systemName,
                    canvasWidth = rendering.model.canvasWidth,
                    canvasHeight = rendering.model.canvasHeight,
                    components = rendering.model.boxes.map { box ->
                        AthenaRuntimeViewerComponentBox(
                            semanticId = box.semanticId.value,
                            label = box.label,
                            x = box.x,
                            y = box.y,
                            width = box.width,
                            height = box.height,
                        )
                    },
                    connections = rendering.model.connections.map { connection ->
                        AthenaRuntimeViewerConnectionLine(
                            semanticId = connection.semanticId.value,
                            x1 = connection.x1,
                            y1 = connection.y1,
                            x2 = connection.x2,
                            y2 = connection.y2,
                        )
                    },
                ),
            )
        }
    }
}
