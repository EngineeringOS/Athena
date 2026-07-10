package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryDocument
import com.engineeringood.athena.geometry.GeometryElement
import com.engineeringood.athena.geometry.GeometryElementKind
import com.engineeringood.athena.geometry.GeometryPoint
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionBounds
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionLabel
import com.engineeringood.athena.projection.ProjectionLabelId
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionPoint

/**
 * Derives a renderer-neutral projection document from one geometry-backed supported view.
 */
class ProjectionModelDeriver {
    /**
     * Materializes one inspectable projection document from one view definition plus its geometry.
     */
    fun derive(
        view: ViewDefinition,
        geometry: GeometryDocument,
    ): ProjectionDocument {
        require(view.id == geometry.viewId) {
            "Projection derivation requires matching view ids but received `${view.id}` and `${geometry.viewId}`."
        }
        return ProjectionDocument(
            view = view,
            canvasWidth = geometry.canvasWidth,
            canvasHeight = geometry.canvasHeight,
            nodes = geometry.elements
                .filter { element -> element.kind == GeometryElementKind.BOX }
                .map { element ->
                    ProjectionNode(
                        projectionId = ProjectionNodeId(element.elementId.value.replace("/geometry/box/", "/projection/node/")),
                        semanticId = element.semanticId,
                        label = element.label.orEmpty(),
                        bounds = ProjectionBounds(
                            x = element.bounds.x,
                            y = element.bounds.y,
                            width = element.bounds.width,
                            height = element.bounds.height,
                        ),
                        originGeometryElementId = element.elementId,
                    )
                },
            connections = geometry.elements
                .filter { element -> element.kind == GeometryElementKind.PATH }
                .map { element ->
                    ProjectionConnection(
                        projectionId = ProjectionConnectionId(
                            element.elementId.value.replace("/geometry/path/", "/projection/connection/"),
                        ),
                        semanticId = element.semanticId,
                        start = element.connectionStart().toProjectionPoint(),
                        end = element.connectionEnd().toProjectionPoint(),
                        originGeometryElementId = element.elementId,
                    )
                },
            labels = geometry.elements
                .filter { element -> element.kind == GeometryElementKind.LABEL }
                .map { element ->
                    ProjectionLabel(
                        projectionId = ProjectionLabelId(
                            element.elementId.value.replace("/geometry/label/", "/projection/label/"),
                        ),
                        semanticId = element.semanticId,
                        label = element.label.orEmpty(),
                        bounds = ProjectionBounds(
                            x = element.bounds.x,
                            y = element.bounds.y,
                            width = element.bounds.width,
                            height = element.bounds.height,
                        ),
                        originGeometryElementId = element.elementId,
                    )
                },
        )
    }
}

private fun GeometryElement.connectionStart(): GeometryPoint {
    return points.firstOrNull()
        ?: GeometryPoint(
            x = bounds.x,
            y = bounds.y + bounds.height / 2,
        )
}

private fun GeometryElement.connectionEnd(): GeometryPoint {
    return points.lastOrNull()
        ?: GeometryPoint(
            x = bounds.x + bounds.width,
            y = bounds.y + bounds.height / 2,
        )
}

private fun GeometryPoint.toProjectionPoint(): ProjectionPoint {
    return ProjectionPoint(
        x = x,
        y = y,
    )
}
