package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionSheet

internal class ProjectionPlacementPlanner {
    fun placementGroups(
        sheet: ProjectionSheet,
        projection: ProjectionDocument,
    ): List<ProjectionPlacementGroup> {
        val sheetNodes = projection.nodes.filter { node -> sheetOwns(sheet, node) }
        val nodesByLabel = sheetNodes.associateBy { node -> node.label }
        val authoredGroups = sheet.regions.mapNotNull { region ->
            val nodes = region.occurrenceNames.mapNotNull(nodesByLabel::get)
            nodes.takeIf(List<*>::isNotEmpty)?.let {
                val orderedNodes = orderNodes(
                    nodes = nodes,
                    authoredNames = region.occurrenceNames,
                    sheet = sheet,
                    projection = projection,
                )
                ProjectionPlacementGroup(
                    regionId = region.regionId,
                    regionName = region.name,
                    authored = true,
                    nodes = orderedNodes.map(OrderedProjectionNode::node),
                    orderingConstraints = orderedNodes.associate { ordered ->
                        ordered.node.projectionId to ordered.constraints
                    },
                )
            }
        }
        val assignedLabels = authoredGroups.flatMap { group -> group.nodes }.map { node -> node.label }.toSet()
        val unassignedNodes = sheetNodes
            .filterNot { node -> node.label in assignedLabels }
            .sortedBy { node -> node.projectionId.value }
        return authoredGroups + listOfNotNull(
            unassignedNodes.takeIf(List<*>::isNotEmpty)?.let { nodes ->
                val orderedNodes = orderNodes(
                    nodes = nodes,
                    authoredNames = emptyList(),
                    sheet = sheet,
                    projection = projection,
                )
                ProjectionPlacementGroup(
                    regionId = unassignedRegionId(sheet),
                    regionName = UNASSIGNED_REGION_NAME,
                    authored = false,
                    nodes = orderedNodes.map(OrderedProjectionNode::node),
                    orderingConstraints = orderedNodes.associate { ordered ->
                        ordered.node.projectionId to ordered.constraints
                    },
                )
            },
        )
    }

    fun sheetOwns(sheet: ProjectionSheet, node: ProjectionNode): Boolean =
        sheet.subjects.any { subject ->
            if (subject.nodeIds.isNotEmpty()) {
                node.projectionId in subject.nodeIds
            } else {
                node.semanticId == subject.semanticId
            }
        }

    fun unassignedRegionId(sheet: ProjectionSheet): String =
        "${sheet.sheetId.value}/region/unassigned"

    private fun orderNodes(
        nodes: List<ProjectionNode>,
        authoredNames: List<String>,
        sheet: ProjectionSheet,
        projection: ProjectionDocument,
    ): List<OrderedProjectionNode> {
        val authoredIndex = authoredNames.withIndex().associate { (index, name) -> name to index }
        val constructMembership = buildMap {
            sheet.constructs.forEachIndexed { constructIndex, construct ->
                construct.memberNames.forEachIndexed { memberIndex, name ->
                    putIfAbsent(
                        name,
                        ConstructOrder(
                            constructIndex = constructIndex,
                            memberIndex = memberIndex,
                            constructName = construct.name ?: construct.kind,
                        ),
                    )
                }
            }
        }
        val nodesByEndpoint = nodes.associateBy(ProjectionNode::projectionId)
        val topologyEdges = projection.connections.mapNotNull { connection ->
            val source = connection.source?.occurrencePortId?.occurrenceId?.let(nodesByEndpoint::get)
            val target = connection.target?.occurrencePortId?.occurrenceId?.let(nodesByEndpoint::get)
            if (source == null || target == null || source == target) null else source to target
        }.distinct()
        val topologyOrder = topologyOrder(topologyEdges, authoredIndex, constructMembership)
        val topologyParticipants = topologyEdges.flatMap { (source, target) -> listOf(source, target) }.toSet()

        return nodes.map { node ->
            val construct = constructMembership[node.label]
            val authoredPosition = authoredIndex[node.label]
            val topologyPosition = topologyOrder[node]
            val key = OccurrenceOrderKey(
                constructClass = if (construct == null) 1 else 0,
                constructIndex = construct?.constructIndex ?: Int.MAX_VALUE,
                constructMemberIndex = construct?.memberIndex ?: Int.MAX_VALUE,
                topologyClass = if (node in topologyParticipants) 0 else 1,
                topologyIndex = topologyPosition ?: Int.MAX_VALUE,
                authoredIndex = authoredPosition ?: Int.MAX_VALUE,
                stableId = node.projectionId.value,
            )
            val constraints = buildList {
                if (construct != null) {
                    add("Construct ${construct.constructName} member order ${construct.memberIndex + 1}")
                }
                if (node in topologyParticipants && topologyPosition != null) {
                    add("Connection topology order ${topologyPosition + 1}")
                }
                if (authoredPosition != null) {
                    add("authored Region member order ${authoredPosition + 1}")
                }
                add("stable Projection identity fallback ${node.projectionId.value}")
            }
            OrderedProjectionNode(node = node, key = key, constraints = constraints)
        }.sortedWith(
            compareBy<OrderedProjectionNode>(
                { ordered -> ordered.key.constructClass },
                { ordered -> ordered.key.constructIndex },
                { ordered -> ordered.key.constructMemberIndex },
                { ordered -> ordered.key.topologyClass },
                { ordered -> ordered.key.topologyIndex },
                { ordered -> ordered.key.authoredIndex },
                { ordered -> ordered.key.stableId },
            ),
        )
    }

    private fun topologyOrder(
        edges: List<Pair<ProjectionNode, ProjectionNode>>,
        authoredIndex: Map<String, Int>,
        constructMembership: Map<String, ConstructOrder>,
    ): Map<ProjectionNode, Int> {
        val participants = edges.flatMap { (source, target) -> listOf(source, target) }.distinct()
        val indegree = participants.associateWith { node -> edges.count { (_, target) -> target == node } }.toMutableMap()
        val ordered = mutableListOf<ProjectionNode>()
        val baseline = compareBy<ProjectionNode>(
            { node -> constructMembership[node.label]?.constructIndex ?: Int.MAX_VALUE },
            { node -> constructMembership[node.label]?.memberIndex ?: Int.MAX_VALUE },
            { node -> authoredIndex[node.label] ?: Int.MAX_VALUE },
            { node -> node.projectionId.value },
        )
        val ready = participants.filter { node -> indegree.getValue(node) == 0 }.sortedWith(baseline).toMutableList()
        while (ready.isNotEmpty()) {
            val node = ready.removeAt(0)
            ordered += node
            edges.filter { (source, _) -> source == node }.forEach { (_, target) ->
                val remaining = indegree.getValue(target) - 1
                indegree[target] = remaining
                if (remaining == 0) {
                    ready += target
                    ready.sortWith(baseline)
                }
            }
        }
        ordered += participants.filterNot(ordered::contains).sortedWith(baseline)
        return ordered.withIndex().associate { (index, node) -> node to index }
    }

    companion object {
        private const val UNASSIGNED_REGION_NAME = "Unassigned"
    }
}

internal data class ProjectionPlacementGroup(
    val regionId: String,
    val regionName: String,
    val authored: Boolean,
    val nodes: List<ProjectionNode>,
    val orderingConstraints: Map<ProjectionNodeId, List<String>>,
)

private data class OrderedProjectionNode(
    val node: ProjectionNode,
    val key: OccurrenceOrderKey,
    val constraints: List<String>,
)

private data class OccurrenceOrderKey(
    val constructClass: Int,
    val constructIndex: Int,
    val constructMemberIndex: Int,
    val topologyClass: Int,
    val topologyIndex: Int,
    val authoredIndex: Int,
    val stableId: String,
)

private data class ConstructOrder(
    val constructIndex: Int,
    val memberIndex: Int,
    val constructName: String,
)
