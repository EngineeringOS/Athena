package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryBounds
import com.engineeringood.athena.geometry.GeometryElementKind
import com.engineeringood.athena.geometry.GeometryDocument
import com.engineeringood.athena.geometry.GeometryElement
import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.geometry.GeometryPoint
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.LayoutDocument
import com.engineeringood.athena.layout.LayoutGroup
import com.engineeringood.athena.layout.LayoutNode
import com.engineeringood.athena.layout.LayoutNodeId
import com.engineeringood.athena.layout.LayoutRelationship
import com.engineeringood.athena.layout.LayoutRelationshipKind
import kotlin.math.max

/**
 * Derives deterministic renderer-facing `Geometry IR` from one explicit `Layout IR` document.
 */
class GeometryIrDeriver {
    /**
     * Derives one explicit geometry document from [layoutDocument].
     */
    fun derive(layoutDocument: LayoutDocument): GeometryDocument {
        return when (layoutDocument.view.layoutIntent) {
            LayoutIntent.STRUCTURAL -> deriveStructuralGeometry(layoutDocument)
            LayoutIntent.CONNECTIVITY -> deriveConnectivityGeometry(layoutDocument)
        }
    }

    /**
     * Reuses stable geometry elements from [previousGeometry] when the scoped mutation only changes downstream paths.
     */
    fun deriveIncremental(
        layoutDocument: LayoutDocument,
        previousGeometry: GeometryDocument,
        affectedScope: CompilerAffectedScope,
    ): GeometryDocument? {
        if (previousGeometry.viewId != layoutDocument.view.id) {
            return null
        }

        val nextGeometry = derive(layoutDocument)
        if (!layoutDocument.referencesAffectedScope(affectedScope)) {
            return previousGeometry
        }

        val previousElementsById = previousGeometry.elements.associateBy { element -> element.elementId }
        return nextGeometry.copy(
            elements = nextGeometry.elements.map { element ->
                previousElementsById[element.elementId]?.takeIf { previous -> previous == element } ?: element
            },
        )
    }

    private fun deriveStructuralGeometry(layoutDocument: LayoutDocument): GeometryDocument {
        val elements = mutableListOf<GeometryElement>()
        val portBoundsByLayoutId = mutableMapOf<LayoutNodeId, GeometryBounds>()
        val componentNodesByGroupId = layoutDocument.nodes
            .filter { node -> node.kind == "component" && node.groupId != null }
            .associateBy { node -> node.groupId!! }

        layoutDocument.groups
            .filter { group -> group.kind == "component-group" }
            .forEachIndexed { componentIndex, group ->
                val componentNode = requireNotNull(componentNodesByGroupId[group.groupId]) {
                    "Geometry derivation requires a component node for layout group ${group.groupId.value}."
                }
                val componentBounds = GeometryBounds(
                    x = COMPONENT_MARGIN_X + componentIndex * (COMPONENT_WIDTH + COMPONENT_GAP_X),
                    y = COMPONENT_Y,
                    width = COMPONENT_WIDTH,
                    height = COMPONENT_HEIGHT,
                )
                elements += GeometryElement(
                    elementId = GeometryElementId(
                        "${layoutDocument.view.id}/geometry/box/${sanitizeId(componentNode.semanticId.value)}",
                    ),
                    semanticId = componentNode.semanticId,
                    kind = GeometryElementKind.BOX,
                    bounds = componentBounds,
                    label = componentNode.label,
                )

                groupedPortNodes(layoutDocument, group).forEachIndexed { portIndex, portNode ->
                    val portBounds = GeometryBounds(
                        x = componentBounds.x + PORT_LABEL_OFFSET_X,
                        y = componentBounds.y + PORT_LABEL_OFFSET_Y + portIndex * PORT_LABEL_VERTICAL_GAP,
                        width = PORT_LABEL_WIDTH,
                        height = PORT_LABEL_HEIGHT,
                    )
                    portBoundsByLayoutId[portNode.layoutId] = portBounds
                    elements += GeometryElement(
                        elementId = GeometryElementId(
                            "${layoutDocument.view.id}/geometry/label/${sanitizeId(portNode.semanticId.value)}",
                        ),
                        semanticId = portNode.semanticId,
                        kind = GeometryElementKind.LABEL,
                        bounds = portBounds,
                        label = portNode.label,
                    )
                }
            }

        elements += buildConnectionPathElements(
            layoutDocument = layoutDocument,
            portBoundsByLayoutId = portBoundsByLayoutId,
        )

        return GeometryDocument(
            viewId = layoutDocument.view.id,
            canvasWidth = canvasWidth(elements),
            canvasHeight = canvasHeight(elements),
            elements = elements,
        )
    }

    private fun deriveConnectivityGeometry(layoutDocument: LayoutDocument): GeometryDocument {
        val elements = mutableListOf<GeometryElement>()
        val portBoundsByLayoutId = mutableMapOf<LayoutNodeId, GeometryBounds>()

        layoutDocument.nodes
            .filter { node -> node.kind == "component" }
            .sortedBy { node -> node.order }
            .forEachIndexed { componentIndex, componentNode ->
                val componentBounds = GeometryBounds(
                    x = SIGNAL_COMPONENT_X,
                    y = SIGNAL_COMPONENT_MARGIN_Y + componentIndex * SIGNAL_COMPONENT_VERTICAL_GAP,
                    width = SIGNAL_COMPONENT_WIDTH,
                    height = SIGNAL_COMPONENT_HEIGHT,
                )
                elements += GeometryElement(
                    elementId = GeometryElementId(
                        "${layoutDocument.view.id}/geometry/box/${sanitizeId(componentNode.semanticId.value)}",
                    ),
                    semanticId = componentNode.semanticId,
                    kind = GeometryElementKind.BOX,
                    bounds = componentBounds,
                    label = componentNode.label,
                )
            }

        layoutDocument.groups
            .filter { group -> group.kind == "signal-group" }
            .sortedBy { group -> group.label }
            .forEachIndexed { signalIndex, group ->
                val baseY = SIGNAL_GROUP_MARGIN_Y + signalIndex * SIGNAL_GROUP_VERTICAL_GAP
                groupedPortNodes(layoutDocument, group).forEachIndexed { portIndex, portNode ->
                    val portBounds = GeometryBounds(
                        x = SIGNAL_PORT_MARGIN_X + portIndex * SIGNAL_PORT_HORIZONTAL_GAP,
                        y = baseY,
                        width = SIGNAL_PORT_WIDTH,
                        height = SIGNAL_PORT_HEIGHT,
                    )
                    portBoundsByLayoutId[portNode.layoutId] = portBounds
                    elements += GeometryElement(
                        elementId = GeometryElementId(
                            "${layoutDocument.view.id}/geometry/label/${sanitizeId(portNode.semanticId.value)}",
                        ),
                        semanticId = portNode.semanticId,
                        kind = GeometryElementKind.LABEL,
                        bounds = portBounds,
                        label = portNode.label,
                    )
                }
            }

        elements += buildConnectionPathElements(
            layoutDocument = layoutDocument,
            portBoundsByLayoutId = portBoundsByLayoutId,
        )

        return GeometryDocument(
            viewId = layoutDocument.view.id,
            canvasWidth = canvasWidth(elements),
            canvasHeight = canvasHeight(elements),
            elements = elements,
        )
    }

    private fun groupedPortNodes(
        layoutDocument: LayoutDocument,
        group: LayoutGroup,
    ): List<LayoutNode> {
        return layoutDocument.nodes
            .filter { node -> node.groupId == group.groupId && node.kind == "port" }
            .sortedBy { node -> node.order }
    }

    private fun buildConnectionPathElements(
        layoutDocument: LayoutDocument,
        portBoundsByLayoutId: Map<LayoutNodeId, GeometryBounds>,
    ): List<GeometryElement> {
        return layoutDocument.relationships
            .filter { relationship -> relationship.kind == LayoutRelationshipKind.CONNECTIVITY }
            .sortedBy { relationship -> relationship.semanticId.value }
            .map { relationship ->
                connectionPathElement(
                    viewId = layoutDocument.view.id,
                    relationship = relationship,
                    sourceBounds = requireNotNull(portBoundsByLayoutId[relationship.sourceLayoutId]) {
                        "Geometry derivation requires source geometry for ${relationship.sourceLayoutId.value}."
                    },
                    targetBounds = requireNotNull(portBoundsByLayoutId[relationship.targetLayoutId]) {
                        "Geometry derivation requires target geometry for ${relationship.targetLayoutId.value}."
                    },
                )
            }
    }

    private fun connectionPathElement(
        viewId: String,
        relationship: LayoutRelationship,
        sourceBounds: GeometryBounds,
        targetBounds: GeometryBounds,
    ): GeometryElement {
        val start = GeometryPoint(
            x = sourceBounds.x + sourceBounds.width,
            y = sourceBounds.y + sourceBounds.height / 2,
        )
        val end = GeometryPoint(
            x = targetBounds.x,
            y = targetBounds.y + targetBounds.height / 2,
        )
        val middleX = (start.x + end.x) / 2
        val points = listOf(
            start,
            GeometryPoint(x = middleX, y = start.y),
            GeometryPoint(x = middleX, y = end.y),
            end,
        )
        return GeometryElement(
            elementId = GeometryElementId(
                "$viewId/geometry/path/${sanitizeId(relationship.semanticId.value)}",
            ),
            semanticId = relationship.semanticId,
            kind = GeometryElementKind.PATH,
            bounds = boundsFor(points),
            points = points,
        )
    }

    private fun boundsFor(points: List<GeometryPoint>): GeometryBounds {
        val minX = points.minOf { point -> point.x }
        val minY = points.minOf { point -> point.y }
        val maxX = points.maxOf { point -> point.x }
        val maxY = points.maxOf { point -> point.y }
        return GeometryBounds(
            x = minX,
            y = minY,
            width = max(1, maxX - minX),
            height = max(1, maxY - minY),
        )
    }

    private fun canvasWidth(elements: List<GeometryElement>): Int {
        return (elements.maxOfOrNull { element -> element.bounds.x + element.bounds.width } ?: 0) + CANVAS_MARGIN
    }

    private fun canvasHeight(elements: List<GeometryElement>): Int {
        return (elements.maxOfOrNull { element -> element.bounds.y + element.bounds.height } ?: 0) + CANVAS_MARGIN
    }

    private fun sanitizeId(value: String): String = value.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_')
}

private const val COMPONENT_MARGIN_X = 40
private const val COMPONENT_GAP_X = 120
private const val COMPONENT_Y = 60
private const val COMPONENT_WIDTH = 140
private const val COMPONENT_HEIGHT = 72
private const val PORT_LABEL_OFFSET_X = 16
private const val PORT_LABEL_OFFSET_Y = 18
private const val PORT_LABEL_VERTICAL_GAP = 20
private const val PORT_LABEL_WIDTH = 48
private const val PORT_LABEL_HEIGHT = 16
private const val SIGNAL_COMPONENT_X = 40
private const val SIGNAL_COMPONENT_MARGIN_Y = 40
private const val SIGNAL_COMPONENT_VERTICAL_GAP = 120
private const val SIGNAL_COMPONENT_WIDTH = 110
private const val SIGNAL_COMPONENT_HEIGHT = 44
private const val SIGNAL_GROUP_MARGIN_Y = 72
private const val SIGNAL_GROUP_VERTICAL_GAP = 140
private const val SIGNAL_PORT_MARGIN_X = 240
private const val SIGNAL_PORT_HORIZONTAL_GAP = 150
private const val SIGNAL_PORT_WIDTH = 60
private const val SIGNAL_PORT_HEIGHT = 18
private const val CANVAS_MARGIN = 40

private fun LayoutDocument.referencesAffectedScope(affectedScope: CompilerAffectedScope): Boolean {
    val affectedSemanticIds = affectedScope.changedSemanticIds.toSet()
    return groups.any { group -> group.semanticIds.any { semanticId -> semanticId.value in affectedSemanticIds } } ||
        nodes.any { node -> node.semanticId.value in affectedSemanticIds } ||
        relationships.any { relationship -> relationship.semanticId.value in affectedSemanticIds }
}
