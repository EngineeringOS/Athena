package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementKind
import com.engineeringood.athena.geometry.GeometryDocument
import com.engineeringood.athena.geometry.GeometryElement
import com.engineeringood.athena.geometry.GeometryPoint
import com.engineeringood.athena.renderer.svg.SvgRenderBox
import com.engineeringood.athena.renderer.svg.SvgRenderConnection
import com.engineeringood.athena.renderer.svg.SvgRenderModel

/** Derives the thin runtime viewer model from explicit `Geometry IR`. */
class SvgRenderModelDeriver {
    /** Derives one runtime-facing render model from [geometry]. */
    fun derive(
        systemName: String,
        geometry: GeometryDocument,
    ): SvgRenderModel {
        return SvgRenderModel(
            systemName = systemName,
            canvasWidth = geometry.canvasWidth,
            canvasHeight = geometry.canvasHeight,
            boxes = geometry.elements
                .filter { element -> element.kind == GeometryElementKind.BOX }
                .map { element ->
                    SvgRenderBox(
                        semanticId = element.semanticId,
                        label = element.label.orEmpty(),
                        x = element.bounds.x,
                        y = element.bounds.y,
                        width = element.bounds.width,
                        height = element.bounds.height,
                    )
                },
            connections = geometry.elements
                .filter { element -> element.kind == GeometryElementKind.PATH }
                .map { element ->
                    val start = element.connectionStart()
                    val end = element.connectionEnd()
                    SvgRenderConnection(
                        semanticId = element.semanticId,
                        x1 = start.x,
                        y1 = start.y,
                        x2 = end.x,
                        y2 = end.y,
                    )
                },
        )
    }

    /**
     * Applies a scoped render refresh over [previousModel] using the already-derived [geometry] document.
     */
    fun deriveIncremental(
        systemName: String,
        geometry: GeometryDocument,
        previousModel: SvgRenderModel,
        affectedScope: CompilerAffectedScope,
    ): SvgRenderModel? {
        val nextModel = derive(
            systemName = systemName,
            geometry = geometry,
        )
        val previousBoxesById = previousModel.boxes.associateBy { box -> box.semanticId }
        if (nextModel.boxes.map { box -> box.semanticId } != previousModel.boxes.map { box -> box.semanticId }) {
            return null
        }
        val previousConnectionsById = previousModel.connections.associateBy { connection -> connection.semanticId }

        return nextModel.copy(
            boxes = nextModel.boxes.map { box ->
                if (box.semanticId.value in affectedScope.renderComponentSemanticIds) {
                    box
                } else {
                    previousBoxesById[box.semanticId] ?: box
                }
            },
            connections = nextModel.connections.map { connection ->
                if (connection.semanticId.value in affectedScope.renderConnectionSemanticIds) {
                    connection
                } else {
                    previousConnectionsById[connection.semanticId] ?: connection
                }
            },
        )
    }
}

/** Resolves the first path point used as the runtime line start. */
private fun GeometryElement.connectionStart(): GeometryPoint {
    return points.firstOrNull()
        ?: GeometryPoint(
            x = bounds.x,
            y = bounds.y + bounds.height / 2,
        )
}

/** Resolves the last path point used as the runtime line end. */
private fun GeometryElement.connectionEnd(): GeometryPoint {
    return points.lastOrNull()
        ?: GeometryPoint(
            x = bounds.x + bounds.width,
            y = bounds.y + bounds.height / 2,
        )
}
