package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.runtime.AthenaCommandRuntimeService
import com.engineeringood.athena.runtime.AthenaConnectPortsCommand
import com.engineeringood.athena.runtime.AthenaEngineeringGraphNodeKind
import com.engineeringood.athena.runtime.AthenaEngineeringGraphReadyProjection
import com.engineeringood.athena.runtime.AthenaEngineeringGraphReferenceKind
import com.engineeringood.athena.runtime.AthenaExecutionContext
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandContribution
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandFactory
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandReady
import com.engineeringood.athena.runtime.AthenaRuntimePluginCommandRejected
import com.engineeringood.athena.runtime.AthenaRuntimePluginInspectorField
import com.engineeringood.athena.runtime.AthenaRuntimePluginInspectorGroup
import com.engineeringood.athena.runtime.AthenaRuntimePluginViewContribution

internal fun electricalRuntimeCommandContributions(): List<AthenaRuntimePluginCommandContribution> {
    return listOf(
        AthenaRuntimePluginCommandContribution(
            contributionId = "electrical-runtime.connect-first-compatible",
            displayName = "Connect first compatible electrical ports",
            description = "Finds the first unconnected out->in electrical pair with the same signal and routes it through the command runtime.",
            factory = AthenaRuntimePluginCommandFactory { context ->
                val pair = context.firstCompatibleElectricalPair()
                    ?: return@AthenaRuntimePluginCommandFactory AthenaRuntimePluginCommandRejected(
                        "No compatible electrical port pair is available for plugin command execution.",
                    )
                AthenaRuntimePluginCommandReady(
                    command = AthenaConnectPortsCommand(
                        sourcePortSemanticId = pair.sourceSemanticId,
                        targetPortSemanticId = pair.targetSemanticId,
                    ),
                )
            },
        ),
    )
}

internal fun electricalRuntimeViewContributions(context: AthenaExecutionContext): List<AthenaRuntimePluginViewContribution> {
    val summary = context.electricalRuntimeSummary()
    if (summary.ownedPortCount == 0 && summary.ownedComponentCount == 0) {
        return emptyList()
    }
    return listOf(
        AthenaRuntimePluginViewContribution(
            inspectorGroups = listOf(
                AthenaRuntimePluginInspectorGroup(
                    title = "Electrical runtime",
                    fields = listOf(
                        AthenaRuntimePluginInspectorField("Domain", ELECTRICAL_DOMAIN_ID),
                        AthenaRuntimePluginInspectorField("Components", summary.ownedComponentCount.toString()),
                        AthenaRuntimePluginInspectorField("Ports", summary.ownedPortCount.toString()),
                        AthenaRuntimePluginInspectorField("Signals", summary.signalCount.toString()),
                        AthenaRuntimePluginInspectorField("Compatible pairs", summary.compatiblePairCount.toString()),
                        AthenaRuntimePluginInspectorField("Connected pairs", summary.connectedPairCount.toString()),
                    ),
                ),
            ),
            diagnosticsEntries = listOf(
                "Electrical runtime plugin active: ${summary.compatiblePairCount} compatible pair(s) available.",
            ),
        ),
    )
}

private data class ElectricalRuntimeSummary(
    val ownedComponentCount: Int,
    val ownedPortCount: Int,
    val signalCount: Int,
    val compatiblePairCount: Int,
    val connectedPairCount: Int,
)

private fun AthenaExecutionContext.firstCompatibleElectricalPair(): ElectricalCompatiblePair? {
    return electricalCompatiblePairs().firstOrNull()
}

private fun AthenaExecutionContext.electricalRuntimeSummary(): ElectricalRuntimeSummary {
    val graphProjection = projectEngineeringGraphProjection() as? AthenaEngineeringGraphReadyProjection
        ?: return ElectricalRuntimeSummary(
            ownedComponentCount = 0,
            ownedPortCount = 0,
            signalCount = 0,
            compatiblePairCount = 0,
            connectedPairCount = 0,
        )
    val electricalComponentIds = graphProjection.electricalComponentSemanticIds()
    val portCandidates = graphProjection.electricalPortCandidates(electricalComponentIds)
    val connectedPairs = graphProjection.connectedPairs()
    return ElectricalRuntimeSummary(
        ownedComponentCount = electricalComponentIds.size,
        ownedPortCount = portCandidates.size,
        signalCount = portCandidates.mapNotNull { candidate -> candidate.signal }.distinct().size,
        compatiblePairCount = electricalCompatiblePairs(graphProjection).size,
        connectedPairCount = connectedPairs.size,
    )
}

private fun AthenaExecutionContext.electricalCompatiblePairs(): List<ElectricalCompatiblePair> {
    val graphProjection = projectEngineeringGraphProjection() as? AthenaEngineeringGraphReadyProjection ?: return emptyList()
    return electricalCompatiblePairs(graphProjection)
}

private fun electricalCompatiblePairs(
    graphProjection: AthenaEngineeringGraphReadyProjection,
): List<ElectricalCompatiblePair> {
    val electricalComponentIds = graphProjection.electricalComponentSemanticIds()
    val candidates = graphProjection.electricalPortCandidates(electricalComponentIds)
    val connectedPairs = graphProjection.connectedPairs()
    val outputs = candidates.filter { candidate -> candidate.direction == "out" && candidate.signal != null }
    val inputs = candidates.filter { candidate -> candidate.direction == "in" && candidate.signal != null }
    return outputs.flatMap { output ->
        inputs.mapNotNull { input ->
            if (
                output.signal == input.signal &&
                output.semanticId != input.semanticId &&
                connectedPairs.none { pair ->
                    pair.first == output.semanticId && pair.second == input.semanticId
                }
            ) {
                ElectricalCompatiblePair(
                    sourceSemanticId = output.semanticId,
                    targetSemanticId = input.semanticId,
                    sortKey = "${output.label}->${input.label}",
                )
            } else {
                null
            }
        }
    }.sortedBy { pair -> pair.sortKey }
}

private fun AthenaEngineeringGraphReadyProjection.electricalComponentSemanticIds(): Set<String> {
    return graph.nodesOfKind(AthenaEngineeringGraphNodeKind.COMPONENT)
        .filter { node ->
            val domainValue = node.properties.firstOrNull { property -> property.name == "domain" }?.value
            domainValue == null || domainValue == ELECTRICAL_DOMAIN_ID
        }
        .map { node -> node.semanticId }
        .toSet()
}

private fun AthenaEngineeringGraphReadyProjection.electricalPortCandidates(
    electricalComponentIds: Set<String>,
): List<ElectricalPortCandidate> {
    val graph = graph
    return graph.nodesOfKind(AthenaEngineeringGraphNodeKind.PORT).mapNotNull { portNode ->
        val ownerSemanticId = portNode.references
            .firstOrNull { reference -> reference.kind == AthenaEngineeringGraphReferenceKind.OWNER }
            ?.resolvedSemanticId
            ?: return@mapNotNull null
        if (ownerSemanticId !in electricalComponentIds) {
            return@mapNotNull null
        }
        val ownerName = graph.node(ownerSemanticId)?.displayName
            ?: portNode.references.firstOrNull()?.authoredPath?.joinToString(".")
            ?: "Unknown"
        ElectricalPortCandidate(
            semanticId = portNode.semanticId,
            label = "$ownerName.${portNode.displayName}",
            direction = portNode.properties.firstOrNull { property -> property.name == "direction" }?.value,
            signal = portNode.properties.firstOrNull { property -> property.name == "signal" }?.value,
        )
    }
}

private fun AthenaEngineeringGraphReadyProjection.connectedPairs(): Set<Pair<String, String>> {
    return graph.nodesOfKind(AthenaEngineeringGraphNodeKind.CONNECTION).mapNotNull { connectionNode ->
        val sourceSemanticId = connectionNode.references
            .firstOrNull { reference -> reference.kind == AthenaEngineeringGraphReferenceKind.CONNECTION_SOURCE }
            ?.resolvedSemanticId
        val targetSemanticId = connectionNode.references
            .firstOrNull { reference -> reference.kind == AthenaEngineeringGraphReferenceKind.CONNECTION_TARGET }
            ?.resolvedSemanticId
        if (sourceSemanticId == null || targetSemanticId == null) {
            null
        } else {
            sourceSemanticId to targetSemanticId
        }
    }.toSet()
}

private data class ElectricalPortCandidate(
    val semanticId: String,
    val label: String,
    val direction: String?,
    val signal: String?,
)

private data class ElectricalCompatiblePair(
    val sourceSemanticId: String,
    val targetSemanticId: String,
    val sortKey: String,
)
