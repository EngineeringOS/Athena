package com.engineeringood.athena.runtime

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionLabel
import com.engineeringood.athena.projection.ProjectionNode

internal fun ProjectionDocument.toViewerScene(
    systemName: String,
    document: EngineeringDocument,
    placementOverrides: Map<String, AthenaGraphPlacement> = emptyMap(),
): AthenaRuntimeViewerScene {
    return AthenaRuntimeViewerScene(
        systemName = systemName,
        canvasWidth = canvasWidth,
        canvasHeight = canvasHeight,
        components = nodes.map(ProjectionNode::toViewerComponent),
        connections = connections.map(ProjectionConnection::toViewerConnection),
        labels = labels.map(ProjectionLabel::toViewerLabel),
    ).withPlacementOverrides(
        document = document,
        placementOverrides = placementOverrides,
    )
}

private fun ProjectionNode.toViewerComponent(): AthenaRuntimeViewerComponentBox {
    return AthenaRuntimeViewerComponentBox(
        projectionId = projectionId.value,
        semanticId = semanticId.value,
        label = label,
        x = bounds.x,
        y = bounds.y,
        width = bounds.width,
        height = bounds.height,
    )
}

private fun ProjectionConnection.toViewerConnection(): AthenaRuntimeViewerConnectionLine {
    return AthenaRuntimeViewerConnectionLine(
        projectionId = projectionId.value,
        semanticId = semanticId.value,
        x1 = start.x,
        y1 = start.y,
        x2 = end.x,
        y2 = end.y,
    )
}

private fun ProjectionLabel.toViewerLabel(): AthenaRuntimeViewerLabel {
    return AthenaRuntimeViewerLabel(
        projectionId = projectionId.value,
        semanticId = semanticId.value,
        label = label,
        x = bounds.x,
        y = bounds.y,
        width = bounds.width,
        height = bounds.height,
    )
}

private fun AthenaRuntimeViewerScene.withPlacementOverrides(
    document: EngineeringDocument,
    placementOverrides: Map<String, AthenaGraphPlacement>,
): AthenaRuntimeViewerScene {
    if (placementOverrides.isEmpty()) {
        return this
    }

    val componentDeltas = components.mapNotNull { component ->
        val override = placementOverrides[component.semanticId] ?: return@mapNotNull null
        component.semanticId to ProjectionDelta(
            deltaX = override.x - component.x,
            deltaY = override.y - component.y,
        )
    }.toMap()
    if (componentDeltas.isEmpty()) {
        return this
    }

    val ownerByPortSemanticId = document.ports.associate { port ->
        port.id.value to port.ownerReference.resolvedIdentity?.value
    }
    val connectionsBySemanticId = document.connections.associateBy { connection -> connection.id.value }

    return copy(
        components = components.map { component ->
            placementOverrides[component.semanticId]?.let { override ->
                component.copy(
                    x = override.x,
                    y = override.y,
                )
            } ?: component
        },
        labels = labels.map { label ->
            val ownerSemanticId = ownerByPortSemanticId[label.semanticId]
            val delta = ownerSemanticId?.let(componentDeltas::get)
            if (delta == null) {
                label
            } else {
                label.copy(
                    x = label.x + delta.deltaX,
                    y = label.y + delta.deltaY,
                )
            }
        },
        connections = connections.map { connection ->
            val engineeringConnection = connectionsBySemanticId[connection.semanticId]
            val sourceOwnerSemanticId = engineeringConnection?.from?.resolvedIdentity?.value?.let(ownerByPortSemanticId::get)
            val targetOwnerSemanticId = engineeringConnection?.to?.resolvedIdentity?.value?.let(ownerByPortSemanticId::get)
            val sourceDelta = sourceOwnerSemanticId?.let(componentDeltas::get)
            val targetDelta = targetOwnerSemanticId?.let(componentDeltas::get)
            if (sourceDelta == null && targetDelta == null) {
                connection
            } else {
                connection.copy(
                    x1 = connection.x1 + (sourceDelta?.deltaX ?: 0),
                    y1 = connection.y1 + (sourceDelta?.deltaY ?: 0),
                    x2 = connection.x2 + (targetDelta?.deltaX ?: 0),
                    y2 = connection.y2 + (targetDelta?.deltaY ?: 0),
                )
            }
        },
    )
}

private data class ProjectionDelta(
    val deltaX: Int,
    val deltaY: Int,
)
