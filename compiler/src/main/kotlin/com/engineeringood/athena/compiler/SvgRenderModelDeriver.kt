package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringIrDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.renderer.svg.SvgRenderBox
import com.engineeringood.athena.renderer.svg.SvgRenderConnection
import com.engineeringood.athena.renderer.svg.SvgRenderModel

/** Derives a thin deterministic render-facing model from canonical engineering IR. */
class SvgRenderModelDeriver {
    /** Derives a render model from semantically valid [document] without reinterpreting authored syntax. */
    fun derive(document: EngineeringIrDocument): SvgRenderModel {
        val boxes = document.components.mapIndexed { index, component ->
            SvgRenderBox(
                semanticId = component.id,
                label = component.name,
                x = COMPONENT_MARGIN_X + index * (COMPONENT_WIDTH + COMPONENT_GAP_X),
                y = COMPONENT_Y,
                width = COMPONENT_WIDTH,
                height = COMPONENT_HEIGHT,
            )
        }
        val boxesByComponent = boxes.associateBy { it.semanticId }
        val portsByOwner = document.ports
            .groupBy { port ->
                requireNotNull(port.ownerReference.resolvedIdentity) {
                    "Render derivation requires resolved owner identity for port ${port.id.value}"
                }
            }
        val anchors = mutableMapOf<StableSemanticIdentity, Pair<Int, Int>>()
        portsByOwner.forEach { (ownerId, ports) ->
            val box = requireNotNull(boxesByComponent[ownerId]) {
                "Render derivation requires a component box for owner ${ownerId.value}"
            }
            assignAnchors(box, ports).forEach { (portId, anchor) ->
                anchors[portId] = anchor
            }
        }
        val connections = document.connections.map { connection ->
            val fromId = requireNotNull(connection.from.resolvedIdentity) {
                "Render derivation requires resolved source endpoint for ${connection.id.value}"
            }
            val toId = requireNotNull(connection.to.resolvedIdentity) {
                "Render derivation requires resolved target endpoint for ${connection.id.value}"
            }
            val (x1, y1) = requireNotNull(anchors[fromId]) {
                "Render derivation requires source anchor for ${fromId.value}"
            }
            val (x2, y2) = requireNotNull(anchors[toId]) {
                "Render derivation requires target anchor for ${toId.value}"
            }
            SvgRenderConnection(
                semanticId = connection.id,
                x1 = x1,
                y1 = y1,
                x2 = x2,
                y2 = y2,
            )
        }
        val canvasWidth = (boxes.maxOfOrNull { it.x + it.width } ?: 0) + COMPONENT_MARGIN_X
        val canvasHeight = COMPONENT_Y + COMPONENT_HEIGHT + COMPONENT_MARGIN_BOTTOM
        return SvgRenderModel(
            systemName = document.system.name,
            canvasWidth = canvasWidth.coerceAtLeast(120),
            canvasHeight = canvasHeight,
            boxes = boxes,
            connections = connections,
        )
    }

    private fun assignAnchors(
        box: SvgRenderBox,
        ports: List<EngineeringPort>,
    ): Map<StableSemanticIdentity, Pair<Int, Int>> {
        val inbound = ports.filter { it.directionSymbol() == "in" }.sortedBy { it.id.value }
        val outbound = ports.filter { it.directionSymbol() == "out" }.sortedBy { it.id.value }
        return buildMap {
            inbound.forEachIndexed { index, port ->
                put(port.id, box.x to anchorY(box, index))
            }
            outbound.forEachIndexed { index, port ->
                put(port.id, (box.x + box.width) to anchorY(box, index))
            }
        }
    }

    private fun anchorY(box: SvgRenderBox, index: Int): Int = box.y + 36 + index * 20

    private fun EngineeringPort.directionSymbol(): String {
        val directionProperty = properties.firstOrNull { it.name == "direction" }
        return requireNotNull((directionProperty?.value as? EngineeringPropertyValue.Symbol)?.text) {
            "Render derivation requires symbolic direction for port ${id.value}"
        }
    }
}

private const val COMPONENT_MARGIN_X = 40
private const val COMPONENT_MARGIN_BOTTOM = 40
private const val COMPONENT_Y = 60
private const val COMPONENT_WIDTH = 140
private const val COMPONENT_HEIGHT = 72
private const val COMPONENT_GAP_X = 120
