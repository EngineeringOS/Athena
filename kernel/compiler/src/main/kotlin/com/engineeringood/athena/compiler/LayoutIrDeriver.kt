package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.LayoutAxis
import com.engineeringood.athena.layout.LayoutIntent
import com.engineeringood.athena.layout.LayoutDocument
import com.engineeringood.athena.layout.LayoutGroup
import com.engineeringood.athena.layout.LayoutGroupId
import com.engineeringood.athena.layout.LayoutNode
import com.engineeringood.athena.layout.LayoutNodeId
import com.engineeringood.athena.layout.LayoutRelationship
import com.engineeringood.athena.layout.LayoutRelationshipId
import com.engineeringood.athena.layout.LayoutPlacementRelation
import com.engineeringood.athena.layout.LayoutRelationshipKind
import com.engineeringood.athena.layout.LayoutRelativePlacement
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.layout.ViewEmphasis

/**
 * Derives deterministic `Layout IR` documents from canonical `Engineering IR` and one supported [ViewDefinition].
 */
class LayoutIrDeriver {
    /**
     * Derives one explicit layout document for the supplied semantic [document] and supported [view].
     */
    fun derive(
        document: EngineeringDocument,
        view: ViewDefinition,
    ): LayoutDocument {
        return when (view.layoutIntent) {
            LayoutIntent.STRUCTURAL -> deriveStructuralLayout(document, view)
            LayoutIntent.CONNECTIVITY -> deriveConnectivityLayout(document, view)
        }
    }

    /**
     * Reuses stable layout-owned objects from [previousLayout] when one scoped semantic mutation leaves them unchanged.
     */
    fun deriveIncremental(
        document: EngineeringDocument,
        view: ViewDefinition,
        previousLayout: LayoutDocument,
        affectedScope: CompilerAffectedScope,
    ): LayoutDocument? {
        if (previousLayout.view.id != view.id) {
            return null
        }

        val nextLayout = derive(document, view)
        if (!nextLayout.referencesAffectedScope(affectedScope)) {
            return previousLayout
        }

        val previousGroupsById = previousLayout.groups.associateBy { group -> group.groupId }
        val previousNodesById = previousLayout.nodes.associateBy { node -> node.layoutId }
        val previousRelationshipsById = previousLayout.relationships.associateBy { relationship -> relationship.relationshipId }

        return nextLayout.copy(
            groups = nextLayout.groups.map { group ->
                previousGroupsById[group.groupId]?.takeIf { previous -> previous == group } ?: group
            },
            nodes = nextLayout.nodes.map { node ->
                previousNodesById[node.layoutId]?.takeIf { previous -> previous == node } ?: node
            },
            relationships = nextLayout.relationships.map { relationship ->
                previousRelationshipsById[relationship.relationshipId]
                    ?.takeIf { previous -> previous == relationship }
                    ?: relationship
            },
        )
    }

    private fun deriveStructuralLayout(
        document: EngineeringDocument,
        view: ViewDefinition,
    ): LayoutDocument {
        val componentNodeIds = linkedMapOf<StableSemanticIdentity, LayoutNodeId>()
        val portNodeIds = linkedMapOf<StableSemanticIdentity, LayoutNodeId>()
        val groups = mutableListOf<LayoutGroup>()
        val nodes = mutableListOf<LayoutNode>()
        val relationships = mutableListOf<LayoutRelationship>()
        val portsByOwner = document.ports.groupBy { port ->
            requireNotNull(port.ownerReference.resolvedIdentity) {
                "Layout derivation requires resolved owner identity for port ${port.id.value}."
            }
        }

        var previousComponentLayoutId: LayoutNodeId? = null
        document.components.forEachIndexed { componentIndex, component ->
            val componentGroupId = LayoutGroupId("${view.id}/group/${sanitizeSemanticId(component.id.value)}")
            val componentLayoutId = LayoutNodeId("${view.id}/node/${sanitizeSemanticId(component.id.value)}")
            componentNodeIds[component.id] = componentLayoutId
            nodes += LayoutNode(
                layoutId = componentLayoutId,
                semanticId = component.id,
                label = component.name,
                kind = "component",
                groupId = componentGroupId,
                order = componentIndex,
                relativePlacement = previousComponentLayoutId?.let { referenceLayoutId ->
                    LayoutRelativePlacement(
                        axis = LayoutAxis.HORIZONTAL,
                        relation = LayoutPlacementRelation.AFTER,
                        referenceLayoutId = referenceLayoutId,
                    )
                },
                emphasis = structuralNodeEmphasis(view),
            )

            val componentPorts = orderedPorts(portsByOwner[component.id].orEmpty())
            val memberLayoutIds = mutableListOf(componentLayoutId)
            val groupSemanticIds = mutableListOf(component.id)
            var previousPortLayoutId: LayoutNodeId? = null
            componentPorts.forEachIndexed { portIndex, port ->
                val portLayoutId = LayoutNodeId("${view.id}/node/${sanitizeSemanticId(port.id.value)}")
                portNodeIds[port.id] = portLayoutId
                memberLayoutIds += portLayoutId
                groupSemanticIds += port.id
                nodes += LayoutNode(
                    layoutId = portLayoutId,
                    semanticId = port.id,
                    label = port.name,
                    kind = "port",
                    groupId = componentGroupId,
                    order = portIndex,
                    relativePlacement = LayoutRelativePlacement(
                        axis = LayoutAxis.VERTICAL,
                        relation = if (previousPortLayoutId == null) {
                            LayoutPlacementRelation.WITHIN
                        } else {
                            LayoutPlacementRelation.AFTER
                        },
                        referenceLayoutId = previousPortLayoutId ?: componentLayoutId,
                    ),
                    emphasis = listOf(ViewEmphasis.PLACEMENT),
                )
                relationships += ownershipRelationship(
                    view = view,
                    componentLayoutId = componentLayoutId,
                    portLayoutId = portLayoutId,
                    portSemanticId = port.id,
                )
                previousPortLayoutId = portLayoutId
            }

            groups += LayoutGroup(
                groupId = componentGroupId,
                label = component.name,
                kind = "component-group",
                semanticIds = groupSemanticIds,
                memberLayoutIds = memberLayoutIds,
            )
            previousComponentLayoutId = componentLayoutId
        }

        relationships += connectivityRelationships(
            view = view,
            connections = document.connections,
            portNodeIds = portNodeIds,
            emphasized = false,
        )

        return LayoutDocument(
            view = view,
            groups = groups,
            nodes = nodes,
            relationships = relationships,
        )
    }

    private fun deriveConnectivityLayout(
        document: EngineeringDocument,
        view: ViewDefinition,
    ): LayoutDocument {
        val nodes = mutableListOf<LayoutNode>()
        val groups = mutableListOf<LayoutGroup>()
        val relationships = mutableListOf<LayoutRelationship>()
        val componentNodeIds = linkedMapOf<StableSemanticIdentity, LayoutNodeId>()
        val portNodeIds = linkedMapOf<StableSemanticIdentity, LayoutNodeId>()
        val portsById = document.ports.associateBy { it.id }

        var previousComponentLayoutId: LayoutNodeId? = null
        document.components.forEachIndexed { componentIndex, component ->
            val componentLayoutId = LayoutNodeId("${view.id}/node/${sanitizeSemanticId(component.id.value)}")
            componentNodeIds[component.id] = componentLayoutId
            nodes += LayoutNode(
                layoutId = componentLayoutId,
                semanticId = component.id,
                label = component.name,
                kind = "component",
                order = componentIndex,
                relativePlacement = previousComponentLayoutId?.let { referenceLayoutId ->
                    LayoutRelativePlacement(
                        axis = LayoutAxis.VERTICAL,
                        relation = LayoutPlacementRelation.AFTER,
                        referenceLayoutId = referenceLayoutId,
                    )
                },
            )
            previousComponentLayoutId = componentLayoutId
        }

        val portsBySignal = document.ports.withIndex().groupBy(
            keySelector = { indexedPort -> indexedPort.value.signalKey() },
            valueTransform = { indexedPort -> indexedPort },
        )
        val connectionIdsBySignal = document.connections.groupBy { connection ->
            connection.signalKey(portsById)
        }

        var globalOrder = 0
        portsBySignal.forEach { (signalKey, indexedPorts) ->
            val signalGroupId = LayoutGroupId("${view.id}/group/signal/${sanitizeKey(signalKey)}")
            val signalPorts = indexedPorts.sortedWith(
                compareBy<IndexedValue<EngineeringPort>>(
                    { directionRank(it.value) },
                    { it.index },
                ),
            ).map { indexedPort -> indexedPort.value }

            val memberLayoutIds = mutableListOf<LayoutNodeId>()
            val groupSemanticIds = mutableListOf<StableSemanticIdentity>()
            var previousSignalPortLayoutId: LayoutNodeId? = null
            signalPorts.forEach { port ->
                val portLayoutId = LayoutNodeId("${view.id}/node/${sanitizeSemanticId(port.id.value)}")
                portNodeIds[port.id] = portLayoutId
                memberLayoutIds += portLayoutId
                groupSemanticIds += port.id
                nodes += LayoutNode(
                    layoutId = portLayoutId,
                    semanticId = port.id,
                    label = port.name,
                    kind = "port",
                    groupId = signalGroupId,
                    order = globalOrder++,
                    relativePlacement = LayoutRelativePlacement(
                        axis = LayoutAxis.HORIZONTAL,
                        relation = if (previousSignalPortLayoutId == null) {
                            LayoutPlacementRelation.WITHIN
                        } else {
                            LayoutPlacementRelation.AFTER
                        },
                        referenceLayoutId = previousSignalPortLayoutId,
                    ),
                    emphasis = view.viewEmphasis,
                )
                val ownerSemanticId = requireNotNull(port.ownerReference.resolvedIdentity) {
                    "Layout derivation requires resolved owner identity for port ${port.id.value}."
                }
                val componentLayoutId = requireNotNull(componentNodeIds[ownerSemanticId]) {
                    "Layout derivation requires a component node for owner ${ownerSemanticId.value}."
                }
                relationships += ownershipRelationship(
                    view = view,
                    componentLayoutId = componentLayoutId,
                    portLayoutId = portLayoutId,
                    portSemanticId = port.id,
                )
                previousSignalPortLayoutId = portLayoutId
            }

            val signalConnectionIds = connectionIdsBySignal[signalKey].orEmpty().map { connection -> connection.id }
            groups += LayoutGroup(
                groupId = signalGroupId,
                label = signalKey,
                kind = "signal-group",
                semanticIds = (groupSemanticIds + signalConnectionIds).distinct(),
                memberLayoutIds = memberLayoutIds,
            )
        }

        relationships += connectivityRelationships(
            view = view,
            connections = document.connections,
            portNodeIds = portNodeIds,
            emphasized = true,
        )

        return LayoutDocument(
            view = view,
            groups = groups,
            nodes = nodes,
            relationships = relationships,
        )
    }

    private fun ownershipRelationship(
        view: ViewDefinition,
        componentLayoutId: LayoutNodeId,
        portLayoutId: LayoutNodeId,
        portSemanticId: StableSemanticIdentity,
    ): LayoutRelationship {
        return LayoutRelationship(
            relationshipId = LayoutRelationshipId(
                "${view.id}/relationship/ownership/${sanitizeSemanticId(portSemanticId.value)}",
            ),
            semanticId = portSemanticId,
            kind = LayoutRelationshipKind.OWNERSHIP,
            sourceLayoutId = componentLayoutId,
            targetLayoutId = portLayoutId,
            emphasis = if (ViewEmphasis.OWNERSHIP in view.viewEmphasis) {
                listOf(ViewEmphasis.OWNERSHIP)
            } else {
                emptyList()
            },
        )
    }

    private fun connectivityRelationships(
        view: ViewDefinition,
        connections: List<EngineeringConnection>,
        portNodeIds: Map<StableSemanticIdentity, LayoutNodeId>,
        emphasized: Boolean,
    ): List<LayoutRelationship> {
        return connections.map { connection ->
            val sourceSemanticId = requireNotNull(connection.from.resolvedIdentity) {
                "Layout derivation requires a resolved source endpoint for ${connection.id.value}."
            }
            val targetSemanticId = requireNotNull(connection.to.resolvedIdentity) {
                "Layout derivation requires a resolved target endpoint for ${connection.id.value}."
            }
            LayoutRelationship(
                relationshipId = LayoutRelationshipId(
                    "${view.id}/relationship/connectivity/${sanitizeSemanticId(connection.id.value)}",
                ),
                semanticId = connection.id,
                kind = LayoutRelationshipKind.CONNECTIVITY,
                sourceLayoutId = requireNotNull(portNodeIds[sourceSemanticId]) {
                    "Layout derivation requires a source port node for ${sourceSemanticId.value}."
                },
                targetLayoutId = requireNotNull(portNodeIds[targetSemanticId]) {
                    "Layout derivation requires a target port node for ${targetSemanticId.value}."
                },
                emphasis = if (emphasized) {
                    view.viewEmphasis.filter { emphasis ->
                        emphasis == ViewEmphasis.CONNECTIVITY || emphasis == ViewEmphasis.SIGNAL_FLOW
                    }
                } else {
                    emptyList()
                },
            )
        }
    }

    private fun structuralNodeEmphasis(view: ViewDefinition): List<ViewEmphasis> {
        return view.viewEmphasis.filter { emphasis ->
            emphasis == ViewEmphasis.OWNERSHIP || emphasis == ViewEmphasis.PLACEMENT
        }
    }

    private fun orderedPorts(ports: List<EngineeringPort>): List<EngineeringPort> {
        return ports.withIndex().sortedWith(
            compareBy<IndexedValue<EngineeringPort>>(
                { directionRank(it.value) },
                { it.index },
            ),
        ).map { indexedPort -> indexedPort.value }
    }

    private fun EngineeringPort.signalKey(): String {
        return properties.symbolValue("signal") ?: "unassigned"
    }

    private fun EngineeringConnection.signalKey(
        portsById: Map<StableSemanticIdentity, EngineeringPort>,
    ): String {
        val sourceSignal = from.resolvedIdentity?.let(portsById::get)?.signalKey()
        val targetSignal = to.resolvedIdentity?.let(portsById::get)?.signalKey()
        return sourceSignal ?: targetSignal ?: "unassigned"
    }

    private fun directionRank(port: EngineeringPort): Int {
        return when (port.properties.symbolValue("direction")) {
            "out" -> 0
            "in" -> 1
            else -> 2
        }
    }

    private fun List<EngineeringProperty>.symbolValue(name: String): String? {
        return (firstOrNull { property -> property.name == name }?.value as? EngineeringPropertyValue.Symbol)?.text
    }

    private fun sanitizeSemanticId(value: String): String = sanitizeKey(value)

    private fun sanitizeKey(value: String): String = value.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_')
}

private fun LayoutDocument.referencesAffectedScope(affectedScope: CompilerAffectedScope): Boolean {
    val affectedSemanticIds = affectedScope.changedSemanticIds.toSet()
    return groups.any { group -> group.semanticIds.any { semanticId -> semanticId.value in affectedSemanticIds } } ||
        nodes.any { node -> node.semanticId.value in affectedSemanticIds } ||
        relationships.any { relationship -> relationship.semanticId.value in affectedSemanticIds }
}
