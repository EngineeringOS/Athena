package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.layout.ElectricalProjectionDescriptor
import com.engineeringood.athena.layout.LayoutOccurrenceId
import com.engineeringood.athena.layout.LayoutSnapshotId
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
import com.engineeringood.athena.presentation.PresentationPhysicalSize
import com.engineeringood.athena.presentation.PresentationPrimitiveOccurrenceReference
import com.engineeringood.athena.presentation.PresentationPrimitivePack
import com.engineeringood.athena.presentation.PresentationPrimitiveId
import com.engineeringood.athena.presentation.PresentationRepresentationFact
import com.engineeringood.athena.presentation.PresentationResolvedSubject
import com.engineeringood.athena.presentation.PresentationTextSlotId
import com.engineeringood.athena.policy.AthenaIndustrialControlV0Profile
import com.engineeringood.athena.policy.ComponentFamilyKey
import com.engineeringood.athena.policy.ComponentRepresentationComposer
import com.engineeringood.athena.policy.ComponentRepresentationFact
import com.engineeringood.athena.policy.ComponentRepresentationRequest
import com.engineeringood.athena.policy.ComponentSubjectKey
import com.engineeringood.athena.projection.ElectricalAnchor
import com.engineeringood.athena.projection.ElectricalConnectionEndpoint
import com.engineeringood.athena.projection.ElectricalConnectionEndpointRole
import com.engineeringood.athena.projection.ElectricalRoutingCorridor
import com.engineeringood.athena.projection.ElectricalAnchorSide
import com.engineeringood.athena.projection.ProjectionBounds
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionLabel
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNotationSubject
import com.engineeringood.athena.routing.AthenaRouteEngineInput
import com.engineeringood.athena.routing.AthenaRouteEngineV0
import com.engineeringood.athena.routing.AthenaRouteRequest
import com.engineeringood.athena.routing.ElectricalConnectionId
import com.engineeringood.athena.routing.ElectricalConnectionIntent
import com.engineeringood.athena.routing.ElectricalConnectionRole
import com.engineeringood.athena.routing.ElectricalPortId
import com.engineeringood.athena.routing.ElectricalPortRole
import com.engineeringood.athena.routing.ElectricalSignalClass
import com.engineeringood.athena.routing.SchematicComponentBounds
import com.engineeringood.athena.routing.SchematicRouteId
import com.engineeringood.athena.routing.SchematicRoutePoint
import com.engineeringood.athena.routing.SchematicRoutingLayoutContext
import com.engineeringood.athena.routing.TerminalAnchorFact
import com.engineeringood.athena.routing.TerminalAnchorId
import com.engineeringood.athena.routing.TerminalSide

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
        val routeFactSnapshot = projection.toRouteFactSnapshot(
            endpointsByConnectionId = endpointsByConnectionId,
        )
        val representationFacts = document.toPresentationRepresentationFacts(projection)

        return PresentationDocument(
            view = projection.view,
            canvasWidth = projection.canvasWidth,
            canvasHeight = projection.canvasHeight,
            primitivePacks = activePrimitivePacks.sortedBy { pack -> pack.packId.value },
            compositePacks = activeCompositePacks.sortedBy { pack -> pack.packId.value },
            resolvedSubjects = projection.resolvedSubjects.map { resolved -> resolved.toPresentationResolvedSubject() },
            occurrences = occurrences,
            connectors = connectors,
            routeFactSnapshot = routeFactSnapshot,
            representationFacts = representationFacts,
        )
    }
}

private fun EngineeringDocument.toPresentationRepresentationFacts(
    projection: ProjectionDocument,
): List<PresentationRepresentationFact> {
    val projectedSubjectIds = projection.nodes.map { node -> node.semanticId }.toSet()
    val sourceProjectionIdsBySemanticId = projection.nodes.associate { node ->
        node.semanticId to listOf(node.projectionId.value)
    }
    val requests = components
        .filter { component -> component.id in projectedSubjectIds }
        .mapNotNull { component ->
            component.m25PresentationFamilyKey()?.let { family ->
                ComponentRepresentationRequest(
                    subject = ComponentSubjectKey(component.id.value),
                    family = family,
                )
            }
        }
    if (requests.isEmpty()) {
        return emptyList()
    }
    return ComponentRepresentationComposer(AthenaIndustrialControlV0Profile.profile())
        .compose(requests)
        .facts
        .map { fact -> fact.toPresentationRepresentationFact(sourceProjectionIdsBySemanticId) }
        .sortedWith(
            compareBy<PresentationRepresentationFact> { fact -> fact.subjectId.value }
                .thenBy { fact -> fact.occurrenceId.value },
        )
}

private fun ComponentRepresentationFact.toPresentationRepresentationFact(
    sourceProjectionIdsBySemanticId: Map<StableSemanticIdentity, List<String>>,
): PresentationRepresentationFact {
    val subject = StableSemanticIdentity(subject.value)
    return PresentationRepresentationFact(
        subjectId = terminals.first().subjectId,
        occurrenceId = terminals.first().occurrenceId,
        symbol = symbol,
        anatomy = anatomy,
        terminals = terminals,
        labels = labels,
        sourceProjectionIds = sourceProjectionIdsBySemanticId[subject].orEmpty(),
    )
}

private fun com.engineeringood.athena.projection.ProjectionResolvedSubject.toPresentationResolvedSubject(): PresentationResolvedSubject {
    return PresentationResolvedSubject(
        semanticId = semanticId,
        conceptId = conceptId,
        classificationKeys = classificationKeys,
        implementationId = implementationId,
        vendorPartNumber = vendorPartNumber,
        physicalSize = physicalSize?.let { size ->
            PresentationPhysicalSize(
                widthMillimeters = size.widthMillimeters,
                heightMillimeters = size.heightMillimeters,
                depthMillimeters = size.depthMillimeters,
            )
        },
        mountingTypeId = mountingTypeId,
        installationMarkerIds = installationMarkerIds,
    )
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

private fun ProjectionDocument.toRouteFactSnapshot(
    endpointsByConnectionId: Map<com.engineeringood.athena.projection.ProjectionConnectionId, List<ElectricalConnectionEndpoint>>,
): com.engineeringood.athena.routing.RouteFactSnapshot? {
    val anchorById = electricalAnchors.associateBy(ElectricalAnchor::anchorId)
    val requests = connections.mapNotNull { connection ->
        val endpoints = endpointsByConnectionId[connection.projectionId].orEmpty()
        val sourceEndpoint = endpoints.firstOrNull { endpoint -> endpoint.endpointRole == ElectricalConnectionEndpointRole.SOURCE }
        val targetEndpoint = endpoints.firstOrNull { endpoint -> endpoint.endpointRole == ElectricalConnectionEndpointRole.TARGET }
        val sourceAnchor = sourceEndpoint?.anchorId?.let(anchorById::get)
        val targetAnchor = targetEndpoint?.anchorId?.let(anchorById::get)
        if (sourceEndpoint == null || targetEndpoint == null || sourceAnchor == null || targetAnchor == null) {
            null
        } else {
            connection.toRouteRequest(
                sourceEndpoint = sourceEndpoint,
                targetEndpoint = targetEndpoint,
                sourceAnchor = sourceAnchor,
                targetAnchor = targetAnchor,
            )
        }
    }
    if (requests.isEmpty()) {
        return null
    }
    return AthenaRouteEngineV0().solve(
        AthenaRouteEngineInput(
            snapshotId = LayoutSnapshotId("snapshot:${view.id}:${sheets.firstOrNull()?.sheetId?.value ?: "sheet"}"),
            layoutContext = SchematicRoutingLayoutContext(gridSize = 20),
            componentBounds = nodes.map(ProjectionNode::toSchematicComponentBounds),
            requests = requests,
        ),
    )
}

private fun ProjectionConnection.toRouteRequest(
    sourceEndpoint: ElectricalConnectionEndpoint,
    targetEndpoint: ElectricalConnectionEndpoint,
    sourceAnchor: ElectricalAnchor,
    targetAnchor: ElectricalAnchor,
): AthenaRouteRequest {
    val connectionId = ElectricalConnectionId(semanticId.value)
    val sourceTerminal = sourceAnchor.toTerminalAnchorFact(sourceEndpoint, ElectricalPortRole.OUTPUT)
    val targetTerminal = targetAnchor.toTerminalAnchorFact(targetEndpoint, ElectricalPortRole.INPUT)
    return AthenaRouteRequest(
        routeId = SchematicRouteId("route:${semanticId.value}"),
        connectionIntent = ElectricalConnectionIntent(
            connectionId = connectionId,
            sourceSubjectId = sourceTerminal.subjectId,
            sourcePortId = sourceTerminal.portId,
            sourcePortSemanticId = sourceEndpoint.portSemanticId,
            targetSubjectId = targetTerminal.subjectId,
            targetPortId = targetTerminal.portId,
            targetPortSemanticId = targetEndpoint.portSemanticId,
            role = ElectricalConnectionRole.CONTROL_SIGNAL,
            signalClass = ElectricalSignalClass.UNKNOWN,
        ),
        sourceAnchor = sourceTerminal,
        targetAnchor = targetTerminal,
    )
}

private fun ElectricalAnchor.toTerminalAnchorFact(
    endpoint: ElectricalConnectionEndpoint,
    portRole: ElectricalPortRole,
): TerminalAnchorFact {
    return TerminalAnchorFact(
        anchorId = TerminalAnchorId(anchorId.value),
        subjectId = ownerSemanticId,
        occurrenceId = LayoutOccurrenceId("occurrence:${ownerSemanticId.value}"),
        portId = ElectricalPortId(endpoint.portSemanticId.value.removePrefix("port:")),
        portSemanticId = endpoint.portSemanticId,
        portRole = portRole,
        side = side.toTerminalSide(),
        point = SchematicRoutePoint(x = position.x, y = position.y),
        gridPoint = SchematicRoutePoint(x = position.x.snapToGrid(20), y = position.y.snapToGrid(20)),
        policySource = "m24:projection-electrical-anchor",
    )
}

private fun ProjectionNode.toSchematicComponentBounds(): SchematicComponentBounds {
    return SchematicComponentBounds(
        subjectId = semanticId,
        occurrenceId = LayoutOccurrenceId("occurrence:${semanticId.value}"),
        topLeft = SchematicRoutePoint(x = bounds.x, y = bounds.y),
        width = bounds.width,
        height = bounds.height,
    )
}

private fun ElectricalAnchorSide.toTerminalSide(): TerminalSide {
    return when (this) {
        ElectricalAnchorSide.LEFT -> TerminalSide.LEFT
        ElectricalAnchorSide.RIGHT -> TerminalSide.RIGHT
        ElectricalAnchorSide.TOP -> TerminalSide.TOP
        ElectricalAnchorSide.BOTTOM -> TerminalSide.BOTTOM
    }
}

private fun Int.snapToGrid(gridSize: Int): Int {
    return (((this + gridSize / 2) / gridSize) * gridSize).coerceAtLeast(0)
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

private fun EngineeringComponent.m25PresentationFamilyKey(): ComponentFamilyKey? {
    val model = properties.firstOrNull { property -> property.name == "model" }?.value?.let { value ->
        when (value) {
            is EngineeringPropertyValue.Symbol -> value.text
            is EngineeringPropertyValue.Text -> value.text
        }
    }.orEmpty()
    val key = listOf(id.value, kind, presentationComponentType().orEmpty(), model)
        .joinToString(separator = " ")
        .lowercase()
    return when {
        "plc" in key || "controller" in key -> ComponentFamilyKey("plc-controller")
        "hmi" in key || "operator" in key -> ComponentFamilyKey("hmi-operator")
        "terminal" in key || "xt" in key -> ComponentFamilyKey("terminal-block")
        "power" in key || "psu" in key || "24vdc" in key -> ComponentFamilyKey("power-supply")
        "breaker" in key || "qf" in key || "protection" in key -> ComponentFamilyKey("protection-device")
        "motor" in key || "actuator" in key || "valve" in key -> ComponentFamilyKey("load-actuator")
        else -> null
    }
}

private fun com.engineeringood.athena.layout.ProjectionFamilyContract?.toPresentationFamilyId(): String? {
    return when (this) {
        is ElectricalProjectionDescriptor -> "electrical/${family.name.lowercase()}"
        null -> null
    }
}
