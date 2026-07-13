package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ElectricalProjectionDescriptor
import com.engineeringood.athena.presentation.PresentationAnchorAlias
import com.engineeringood.athena.presentation.PresentationAnchorBinding
import com.engineeringood.athena.presentation.PresentationCompositeOccurrenceReference
import com.engineeringood.athena.presentation.PresentationCompositePack
import com.engineeringood.athena.presentation.PresentationCompositeId
import com.engineeringood.athena.presentation.PresentationConnector
import com.engineeringood.athena.presentation.PresentationDocument
import com.engineeringood.athena.presentation.PresentationLayer
import com.engineeringood.athena.presentation.PresentationOccurrence
import com.engineeringood.athena.presentation.PresentationOccurrenceId
import com.engineeringood.athena.presentation.PresentationOrientation
import com.engineeringood.athena.presentation.PresentationPackId
import com.engineeringood.athena.presentation.PresentationPoint
import com.engineeringood.athena.presentation.PresentationPrimitiveOccurrenceReference
import com.engineeringood.athena.presentation.PresentationPrimitivePack
import com.engineeringood.athena.presentation.PresentationPrimitiveId
import com.engineeringood.athena.presentation.PresentationTextSlotId
import com.engineeringood.athena.projection.ElectricalAnchor
import com.engineeringood.athena.projection.ElectricalConnectionEndpoint
import com.engineeringood.athena.projection.ElectricalConnectionEndpointRole
import com.engineeringood.athena.projection.ElectricalRoutingCorridor
import com.engineeringood.athena.projection.ProjectionBounds
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionLabel
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNotationSubject

/**
 * Derives the first rebuildable `Presentation IR` from a renderer-neutral projection document.
 *
 * The deriver reuses projection-owned sheet, notation, anchor, endpoint, and routing guidance. It
 * does not invent a second semantic core and it does not let frontend widgets become the first real
 * presentation contract.
 */
class PresentationModelDeriver {
    /**
     * Materializes one rebuildable presentation document for one supported view.
     */
    fun derive(
        document: EngineeringDocument,
        projection: ProjectionDocument,
        primitivePacks: List<PresentationPrimitivePack>,
        compositePacks: List<PresentationCompositePack>,
    ): PresentationDocument {
        val familyId = projection.view.familyContract.toPresentationFamilyId()
        val activePrimitivePacks = primitivePacks.filter { pack -> pack.familyIds.isEmpty() || familyId in pack.familyIds }
        val activeCompositePacks = compositePacks.filter { pack -> pack.familyIds.isEmpty() || familyId in pack.familyIds }
        val notationBySemanticId = projection.notationPack?.subjects
            .orEmpty()
            .associateBy(ProjectionNotationSubject::semanticId)
        val anchorsByNodeId = projection.electricalAnchors.groupBy(ElectricalAnchor::nodeId)
        val anchorByLabelId = projection.electricalAnchors
            .mapNotNull { anchor -> anchor.labelId?.let { labelId -> labelId to anchor } }
            .toMap()
        val componentTypeBySemanticId = document.components.associate { component ->
            component.id to component.presentationComponentType()
        }
        val occurrences = buildList {
            addAll(
                projection.nodes.map { node ->
                    node.toPresentationOccurrence(
                        familyId = familyId,
                        notation = notationBySemanticId[node.semanticId],
                        componentType = componentTypeBySemanticId[node.semanticId],
                        nodeAnchors = anchorsByNodeId[node.projectionId].orEmpty(),
                        compositePacks = activeCompositePacks,
                    )
                },
            )
            addAll(
                projection.labels.map { label ->
                    label.toPresentationOccurrence(
                        notation = notationBySemanticId[label.semanticId],
                        anchor = anchorByLabelId[label.projectionId],
                    )
                },
            )
        }.sortedBy { occurrence -> occurrence.occurrenceId.value }
        val endpointsByConnectionId = projection.electricalConnectionEndpoints.groupBy(ElectricalConnectionEndpoint::projectionConnectionId)
        val corridorByConnectionId = projection.electricalRoutingCorridors.associateBy(ElectricalRoutingCorridor::projectionConnectionId)
        val connectors = projection.connections.map { connection ->
            connection.toPresentationConnector(
                notation = notationBySemanticId[connection.semanticId],
                endpoints = endpointsByConnectionId[connection.projectionId].orEmpty(),
                corridor = corridorByConnectionId[connection.projectionId],
            )
        }.sortedBy { connector -> connector.occurrenceId.value }

        return PresentationDocument(
            view = projection.view,
            canvasWidth = projection.canvasWidth,
            canvasHeight = projection.canvasHeight,
            primitivePacks = activePrimitivePacks.sortedBy { pack -> pack.packId.value },
            compositePacks = activeCompositePacks.sortedBy { pack -> pack.packId.value },
            occurrences = occurrences,
            connectors = connectors,
        )
    }
}

private fun ProjectionNode.toPresentationOccurrence(
    familyId: String?,
    notation: ProjectionNotationSubject?,
    componentType: String?,
    nodeAnchors: List<ElectricalAnchor>,
    compositePacks: List<PresentationCompositePack>,
): PresentationOccurrence {
    val compositeId = selectCompositeId(
        familyId = familyId,
        componentType = componentType,
        compositePacks = compositePacks,
    )
    return PresentationOccurrence(
        occurrenceId = PresentationOccurrenceId(projectionId.value.replace("/projection/node/", "/presentation/occurrence/")),
        semanticId = semanticId,
        reference = PresentationCompositeOccurrenceReference(compositeId),
        bounds = bounds.toPresentationBounds(),
        layer = PresentationLayer.DEVICE,
        displayLabel = label,
        orientation = PresentationOrientation.HORIZONTAL,
        markerKeys = notation?.markerKeys.orEmpty(),
        textValues = mapOf(PresentationTextSlotId("subject-label") to label),
        anchorBindings = nodeAnchors.map(ElectricalAnchor::toPresentationAnchorBinding),
        sourceProjectionIds = listOf(projectionId.value),
    )
}

private fun ProjectionLabel.toPresentationOccurrence(
    notation: ProjectionNotationSubject?,
    anchor: ElectricalAnchor?,
): PresentationOccurrence {
    return PresentationOccurrence(
        occurrenceId = PresentationOccurrenceId(projectionId.value.replace("/projection/label/", "/presentation/occurrence/")),
        semanticId = semanticId,
        reference = PresentationPrimitiveOccurrenceReference(
            primitiveId = PresentationPrimitiveId("electrical.label.terminal"),
        ),
        bounds = bounds.toPresentationBounds(),
        layer = PresentationLayer.LABEL,
        displayLabel = label,
        orientation = PresentationOrientation.HORIZONTAL,
        markerKeys = notation?.markerKeys.orEmpty(),
        textValues = mapOf(PresentationTextSlotId("terminal-label") to label.uppercase()),
        anchorBindings = listOfNotNull(anchor?.toPresentationAnchorBinding()),
        sourceProjectionIds = listOf(projectionId.value),
    )
}

private fun ProjectionConnection.toPresentationConnector(
    notation: ProjectionNotationSubject?,
    endpoints: List<ElectricalConnectionEndpoint>,
    corridor: ElectricalRoutingCorridor?,
): PresentationConnector {
    val sourceEndpoint = endpoints.firstOrNull { endpoint -> endpoint.endpointRole == ElectricalConnectionEndpointRole.SOURCE }
    val targetEndpoint = endpoints.firstOrNull { endpoint -> endpoint.endpointRole == ElectricalConnectionEndpointRole.TARGET }
    return PresentationConnector(
        occurrenceId = PresentationOccurrenceId(
            projectionId.value.replace("/projection/connection/", "/presentation/connector/"),
        ),
        semanticId = semanticId,
        primitiveId = PresentationPrimitiveId("electrical.conductor.orthogonal"),
        routePoints = listOf(start.toPresentationPoint()) +
            corridor?.preferredBendPoints.orEmpty().map { point -> point.toPresentationPoint() } +
            listOf(end.toPresentationPoint()),
        sourceAnchorId = corridor?.sourceAnchorId?.value ?: sourceEndpoint?.anchorId?.value,
        targetAnchorId = corridor?.targetAnchorId?.value ?: targetEndpoint?.anchorId?.value,
        sourcePortSemanticId = sourceEndpoint?.portSemanticId,
        targetPortSemanticId = targetEndpoint?.portSemanticId,
        markerKeys = notation?.markerKeys.orEmpty(),
        sourceProjectionIds = listOf(projectionId.value),
    )
}

private fun ElectricalAnchor.toPresentationAnchorBinding(): PresentationAnchorBinding {
    return PresentationAnchorBinding(
        alias = PresentationAnchorAlias(
            when (side.name.lowercase()) {
                "left" -> "left-terminal"
                "right" -> "right-terminal"
                "top" -> "top-terminal"
                "bottom" -> "bottom-terminal"
                else -> "terminal"
            },
        ),
        anchorId = anchorId.value,
        portSemanticId = portSemanticId,
        ownerSemanticId = ownerSemanticId,
        sourceLabelId = labelId?.value,
    )
}

private fun ProjectionBounds.toPresentationBounds(): com.engineeringood.athena.presentation.PresentationBounds {
    return com.engineeringood.athena.presentation.PresentationBounds(
        x = x,
        y = y,
        width = width,
        height = height,
    )
}

private fun com.engineeringood.athena.projection.ProjectionPoint.toPresentationPoint(): PresentationPoint {
    return PresentationPoint(x = x, y = y)
}

private fun selectCompositeId(
    familyId: String?,
    componentType: String?,
    compositePacks: List<PresentationCompositePack>,
): PresentationCompositeId {
    val preferredId = when (componentType?.lowercase()) {
        "motor" -> PresentationCompositeId("electrical.device.motor-panel")
        "switch" -> PresentationCompositeId("electrical.device.switch-panel")
        else -> PresentationCompositeId("electrical.device.generic-panel")
    }
    if (compositePacks.any { pack -> pack.composites.any { definition -> definition.compositeId == preferredId } }) {
        return preferredId
    }
    val fallbackComposite = compositePacks.firstNotNullOfOrNull { pack -> pack.composites.firstOrNull()?.compositeId }
    if (fallbackComposite != null) {
        return fallbackComposite
    }
    return when {
        familyId?.startsWith("electrical/") == true -> PresentationCompositeId("electrical.device.generic-panel")
        else -> PresentationCompositeId("generic.device.default")
    }
}

private fun EngineeringComponent.presentationComponentType(): String? {
    return properties.firstOrNull { property -> property.name == "type" }?.value?.let { value ->
        when (value) {
            is EngineeringPropertyValue.Symbol -> value.text
            is EngineeringPropertyValue.Text -> value.text
        }
    } ?: kind.takeIf(String::isNotBlank)
}

private fun com.engineeringood.athena.layout.ProjectionFamilyContract?.toPresentationFamilyId(): String? {
    return when (this) {
        is ElectricalProjectionDescriptor -> "electrical/${family.name.lowercase()}"
        null -> null
    }
}
