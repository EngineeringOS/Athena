package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.layout.ElectricalProjectionDescriptor
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ElectricalAnchor
import com.engineeringood.athena.projection.ElectricalAnchorId
import com.engineeringood.athena.projection.ElectricalAnchorSide
import com.engineeringood.athena.projection.ElectricalConnectionEndpoint
import com.engineeringood.athena.projection.ElectricalConnectionEndpointId
import com.engineeringood.athena.projection.ElectricalConnectionEndpointRole
import com.engineeringood.athena.projection.ElectricalRoutingCorridor
import com.engineeringood.athena.projection.ElectricalRoutingCorridorId
import com.engineeringood.athena.projection.ProjectionBounds
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionLabel
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionPoint
import kotlin.math.abs

internal data class ProjectionElectricalContracts(
    val anchors: List<ElectricalAnchor> = emptyList(),
    val connectionEndpoints: List<ElectricalConnectionEndpoint> = emptyList(),
    val routingCorridors: List<ElectricalRoutingCorridor> = emptyList(),
)

internal fun deriveProjectionElectricalContracts(
    view: ViewDefinition,
    document: EngineeringDocument,
    nodes: List<ProjectionNode>,
    connections: List<ProjectionConnection>,
    labels: List<ProjectionLabel>,
): ProjectionElectricalContracts {
    val family = (view.familyContract as? ElectricalProjectionDescriptor)?.family
        ?: return ProjectionElectricalContracts()
    val portsBySemanticId = document.ports.associateBy { port -> port.id }
    val nodesBySemanticId = nodes.groupBy(ProjectionNode::semanticId)
    val anchors = labels.mapNotNull { label ->
        deriveElectricalAnchor(
            label = label,
            portsBySemanticId = portsBySemanticId,
            nodesBySemanticId = nodesBySemanticId,
        )
    }
    if (anchors.isEmpty() || connections.isEmpty()) {
        return ProjectionElectricalContracts(anchors = anchors)
    }
    val anchorsByPortSemanticId = anchors.groupBy(ElectricalAnchor::portSemanticId)
    val canonicalConnectionsBySemanticId = document.connections.associateBy { connection -> connection.id }
    val endpoints = mutableListOf<ElectricalConnectionEndpoint>()
    val corridors = mutableListOf<ElectricalRoutingCorridor>()
    connections.forEach { projectionConnection ->
        val canonicalConnection = canonicalConnectionsBySemanticId[projectionConnection.semanticId] ?: return@forEach
        val sourcePortSemanticId = canonicalConnection.from.resolvedIdentity ?: return@forEach
        val targetPortSemanticId = canonicalConnection.to.resolvedIdentity ?: return@forEach
        val sourceAnchor = anchorsByPortSemanticId[sourcePortSemanticId]
            .orEmpty()
            .closestTo(projectionConnection.start)
            ?: return@forEach
        val targetAnchor = anchorsByPortSemanticId[targetPortSemanticId]
            .orEmpty()
            .closestTo(projectionConnection.end)
            ?: return@forEach
        endpoints += ElectricalConnectionEndpoint(
            endpointId = ElectricalConnectionEndpointId("${projectionConnection.projectionId.value}/endpoint/source"),
            projectionConnectionId = projectionConnection.projectionId,
            connectionSemanticId = projectionConnection.semanticId,
            endpointRole = ElectricalConnectionEndpointRole.SOURCE,
            portSemanticId = sourcePortSemanticId,
            anchorId = sourceAnchor.anchorId,
        )
        endpoints += ElectricalConnectionEndpoint(
            endpointId = ElectricalConnectionEndpointId("${projectionConnection.projectionId.value}/endpoint/target"),
            projectionConnectionId = projectionConnection.projectionId,
            connectionSemanticId = projectionConnection.semanticId,
            endpointRole = ElectricalConnectionEndpointRole.TARGET,
            portSemanticId = targetPortSemanticId,
            anchorId = targetAnchor.anchorId,
        )
        corridors += ElectricalRoutingCorridor(
            corridorId = ElectricalRoutingCorridorId("${projectionConnection.projectionId.value}/corridor"),
            projectionConnectionId = projectionConnection.projectionId,
            connectionSemanticId = projectionConnection.semanticId,
            sourceAnchorId = sourceAnchor.anchorId,
            targetAnchorId = targetAnchor.anchorId,
            preferredBendPoints = derivePreferredBendPoints(
                family = family,
                sourceAnchor = sourceAnchor,
                targetAnchor = targetAnchor,
            ),
        )
    }
    return ProjectionElectricalContracts(
        anchors = anchors,
        connectionEndpoints = endpoints,
        routingCorridors = corridors,
    )
}

private fun deriveElectricalAnchor(
    label: ProjectionLabel,
    portsBySemanticId: Map<com.engineeringood.athena.ir.StableSemanticIdentity, EngineeringPort>,
    nodesBySemanticId: Map<com.engineeringood.athena.ir.StableSemanticIdentity, List<ProjectionNode>>,
): ElectricalAnchor? {
    val port = portsBySemanticId[label.semanticId] ?: return null
    val ownerSemanticId = port.ownerReference.resolvedIdentity ?: return null
    val ownerNode = nodesBySemanticId[ownerSemanticId].orEmpty().closestTo(label.bounds.centerPoint()) ?: return null
    val labelCenter = label.bounds.centerPoint()
    val side = deriveAnchorSide(
        ownerBounds = ownerNode.bounds,
        referencePoint = labelCenter,
    )
    return ElectricalAnchor(
        anchorId = ElectricalAnchorId("${label.projectionId.value}/anchor"),
        portSemanticId = port.id,
        ownerSemanticId = ownerSemanticId,
        nodeId = ownerNode.projectionId,
        labelId = label.projectionId,
        position = ownerNode.bounds.edgePoint(side = side, referencePoint = labelCenter),
        side = side,
    )
}

private fun deriveAnchorSide(
    ownerBounds: ProjectionBounds,
    referencePoint: ProjectionPoint,
): ElectricalAnchorSide {
    val left = ownerBounds.x
    val right = ownerBounds.x + ownerBounds.width
    val top = ownerBounds.y
    val bottom = ownerBounds.y + ownerBounds.height
    return when {
        referencePoint.x <= left -> ElectricalAnchorSide.LEFT
        referencePoint.x >= right -> ElectricalAnchorSide.RIGHT
        referencePoint.y <= top -> ElectricalAnchorSide.TOP
        referencePoint.y >= bottom -> ElectricalAnchorSide.BOTTOM
        else -> listOf(
            ElectricalAnchorSide.LEFT to abs(referencePoint.x - left),
            ElectricalAnchorSide.RIGHT to abs(right - referencePoint.x),
            ElectricalAnchorSide.TOP to abs(referencePoint.y - top),
            ElectricalAnchorSide.BOTTOM to abs(bottom - referencePoint.y),
        ).minBy { (_, distance) -> distance }.first
    }
}

private fun ProjectionBounds.centerPoint(): ProjectionPoint {
    return ProjectionPoint(
        x = x + (width / 2),
        y = y + (height / 2),
    )
}

private fun ProjectionBounds.edgePoint(
    side: ElectricalAnchorSide,
    referencePoint: ProjectionPoint,
): ProjectionPoint {
    val rightEdge = x + width
    val bottomEdge = y + height
    return when (side) {
        ElectricalAnchorSide.LEFT -> ProjectionPoint(
            x = x,
            y = referencePoint.y.coerceIn(y, bottomEdge),
        )

        ElectricalAnchorSide.RIGHT -> ProjectionPoint(
            x = rightEdge,
            y = referencePoint.y.coerceIn(y, bottomEdge),
        )

        ElectricalAnchorSide.TOP -> ProjectionPoint(
            x = referencePoint.x.coerceIn(x, rightEdge),
            y = y,
        )

        ElectricalAnchorSide.BOTTOM -> ProjectionPoint(
            x = referencePoint.x.coerceIn(x, rightEdge),
            y = bottomEdge,
        )
    }
}

private fun List<ProjectionNode>.closestTo(point: ProjectionPoint): ProjectionNode? {
    return minByOrNull { node -> squaredDistance(point, node.bounds.centerPoint()) }
}

private fun List<ElectricalAnchor>.closestTo(point: ProjectionPoint): ElectricalAnchor? {
    return minByOrNull { anchor -> squaredDistance(point, anchor.position) }
}

private fun squaredDistance(
    first: ProjectionPoint,
    second: ProjectionPoint,
): Int {
    val deltaX = first.x - second.x
    val deltaY = first.y - second.y
    return (deltaX * deltaX) + (deltaY * deltaY)
}

private fun derivePreferredBendPoints(
    family: ElectricalProjectionFamily,
    sourceAnchor: ElectricalAnchor,
    targetAnchor: ElectricalAnchor,
): List<ProjectionPoint> {
    if (sourceAnchor.position.x == targetAnchor.position.x || sourceAnchor.position.y == targetAnchor.position.y) {
        return emptyList()
    }
    val horizontalFirst = when (family) {
        ElectricalProjectionFamily.CABINET,
        ElectricalProjectionFamily.DOCUMENTATION -> true

        ElectricalProjectionFamily.SCHEMATIC,
        ElectricalProjectionFamily.WIRING -> {
            abs(targetAnchor.position.x - sourceAnchor.position.x) >= abs(targetAnchor.position.y - sourceAnchor.position.y)
        }
    }
    return if (horizontalFirst) {
        val midX = (sourceAnchor.position.x + targetAnchor.position.x) / 2
        listOf(
            ProjectionPoint(midX, sourceAnchor.position.y),
            ProjectionPoint(midX, targetAnchor.position.y),
        ).deduplicateSequentialPoints()
    } else {
        val midY = (sourceAnchor.position.y + targetAnchor.position.y) / 2
        listOf(
            ProjectionPoint(sourceAnchor.position.x, midY),
            ProjectionPoint(targetAnchor.position.x, midY),
        ).deduplicateSequentialPoints()
    }
}

private fun List<ProjectionPoint>.deduplicateSequentialPoints(): List<ProjectionPoint> {
    return fold(mutableListOf()) { points, point ->
        if (points.lastOrNull() != point) {
            points += point
        }
        points
    }
}
