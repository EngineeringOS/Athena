package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext
import com.engineeringood.athena.geometry.GeometryDocument
import com.engineeringood.athena.geometry.GeometryElement
import com.engineeringood.athena.geometry.GeometryElementKind
import com.engineeringood.athena.geometry.GeometryPoint
import com.engineeringood.athena.layout.ElectricalProjectionDescriptor
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionBounds
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ProjectionCrossReference
import com.engineeringood.athena.projection.ProjectionCrossReferenceKind
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionLabel
import com.engineeringood.athena.projection.ProjectionLabelId
import com.engineeringood.athena.projection.ProjectionLabelPolicy
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionNotationPack
import com.engineeringood.athena.projection.ProjectionNotationPackId
import com.engineeringood.athena.projection.ProjectionNotationSubject
import com.engineeringood.athena.projection.ProjectionPoint
import com.engineeringood.athena.projection.ProjectionPhysicalSize
import com.engineeringood.athena.projection.ProjectionResolvedSubject
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetSubject
import com.engineeringood.athena.projection.ProjectionSymbolKey
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.StableSemanticIdentity

/**
 * Derives a renderer-neutral projection document from one geometry-backed supported view.
 */
class ProjectionModelDeriver {
    /**
     * Materializes one inspectable projection document from one view definition plus its geometry.
     */
    fun derive(
        view: ViewDefinition,
        document: EngineeringDocument,
        geometry: GeometryDocument,
        knowledgeContext: AthenaCompilationKnowledgeContext,
    ): ProjectionDocument {
        require(view.id == geometry.viewId) {
            "Projection derivation requires matching view ids but received `${view.id}` and `${geometry.viewId}`."
        }
        val baseNodes = geometry.elements
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
            }
        val nodes = documentationProjectionNodes(
            view = view,
            nodes = baseNodes,
            canvasWidth = geometry.canvasWidth,
        )
        val connections = geometry.elements
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
            }
        val labels = geometry.elements
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
            }
        val sheets = deriveSheets(
            view = view,
            nodes = nodes,
            connections = connections,
            labels = labels,
        )
        val resolvedSubjects = deriveResolvedSubjects(
            document = document,
            knowledgeContext = knowledgeContext,
        )
        val electricalContracts = deriveProjectionElectricalContracts(
            view = view,
            document = document,
            nodes = nodes,
            connections = connections,
            labels = labels,
        )
        return ProjectionDocument(
            view = view,
            canvasWidth = documentationCanvasWidth(
                view = view,
                baseCanvasWidth = geometry.canvasWidth,
                nodes = nodes,
            ),
            canvasHeight = documentationCanvasHeight(
                view = view,
                baseCanvasHeight = geometry.canvasHeight,
                nodes = nodes,
            ),
            nodes = nodes,
            connections = connections,
            labels = labels,
            resolvedSubjects = resolvedSubjects,
            sheets = sheets,
            notationPack = deriveNotationPack(
                view = view,
                nodes = nodes,
                connections = connections,
                labels = labels,
            ),
            crossReferences = deriveCrossReferences(
                view = view,
                sheets = sheets,
            ),
            electricalAnchors = electricalContracts.anchors,
            electricalConnectionEndpoints = electricalContracts.connectionEndpoints,
            electricalRoutingCorridors = electricalContracts.routingCorridors,
        )
    }
}

private fun deriveResolvedSubjects(
    document: EngineeringDocument,
    knowledgeContext: AthenaCompilationKnowledgeContext,
): List<ProjectionResolvedSubject> {
    val implementationBySubjectId = knowledgeContext.resolvedImplementations.associateBy { resolved ->
        resolved.semanticSubjectId.value
    }
    val physicalTraitBySubjectId = knowledgeContext.resolvedPhysicalTraits.associateBy { resolved ->
        resolved.semanticSubjectId.value
    }
    val componentIds = document.components.map { component -> component.id.value }.toSet()
    return knowledgeContext.resolvedComponents
        .filter { resolved -> resolved.semanticSubjectId.value in componentIds }
        .map { resolved ->
            val implementation = implementationBySubjectId[resolved.semanticSubjectId.value]
            val physicalTrait = physicalTraitBySubjectId[resolved.semanticSubjectId.value]
            ProjectionResolvedSubject(
                semanticId = resolved.semanticSubjectId,
                conceptId = resolved.concept.conceptId.value,
                classificationKeys = resolved.concept.classificationKeys.toSortedSet(),
                implementationId = implementation?.implementation?.implementationId?.value,
                vendorPartNumber = implementation?.implementation?.vendorPartNumber?.value,
                physicalSize = physicalTrait?.definition?.size?.let { size ->
                    ProjectionPhysicalSize(
                        widthMillimeters = size.widthMillimeters,
                        heightMillimeters = size.heightMillimeters,
                        depthMillimeters = size.depthMillimeters,
                    )
                },
                mountingTypeId = physicalTrait?.definition?.mountingTypeId?.value,
                installationMarkerIds = physicalTrait?.definition?.installationMarkerIds
                    ?.map { marker -> marker.value }
                    ?.toSortedSet()
                    .orEmpty(),
            )
        }
        .sortedBy { resolved -> resolved.semanticId.value }
}

private fun deriveSheets(
    view: ViewDefinition,
    nodes: List<ProjectionNode>,
    connections: List<ProjectionConnection>,
    labels: List<ProjectionLabel>,
): List<ProjectionSheet> {
    val allSubjects = subjectAnchors(
        nodes = nodes,
        connections = connections,
        labels = labels,
    )
    return when ((view.familyContract as? ElectricalProjectionDescriptor)?.family) {
        ElectricalProjectionFamily.DOCUMENTATION -> documentationSheets(
            view = view,
            allSubjects = allSubjects,
            nodes = nodes,
            connections = connections,
            labels = labels,
        )

        else -> listOf(
            ProjectionSheet(
                sheetId = ProjectionSheetId("${view.id}/sheet/01-main"),
                displayName = "${view.displayName} Main",
                order = 0,
                subjects = allSubjects,
            ),
        )
    }
}

private fun documentationSheets(
    view: ViewDefinition,
    allSubjects: List<ProjectionSheetSubject>,
    nodes: List<ProjectionNode>,
    connections: List<ProjectionConnection>,
    labels: List<ProjectionLabel>,
): List<ProjectionSheet> {
    val overviewId = ProjectionSheetId("${view.id}/sheet/01-overview")
    val referenceId = ProjectionSheetId("${view.id}/sheet/02-reference")
    val overviewNodeIds = nodes.filterNot(ProjectionNode::isDocumentationReferenceNode).map(ProjectionNode::projectionId).toSet()
    val referenceNodeIds = nodes.filter(ProjectionNode::isDocumentationReferenceNode).map(ProjectionNode::projectionId).toSet()
    val overviewConnectionIds = connections.map(ProjectionConnection::projectionId).toSet()
    val overviewLabelIds = labels.map(ProjectionLabel::projectionId).toSet()
    val overviewSubjects = allSubjects.mapNotNull { subject ->
        subject.filtered(
            nodeIds = overviewNodeIds,
            connectionIds = overviewConnectionIds,
            labelIds = overviewLabelIds,
        )
    }
    val referenceSubjects = allSubjects.mapNotNull { subject ->
        subject.filtered(
            nodeIds = referenceNodeIds,
        )
    }
    return listOf(
        ProjectionSheet(
            sheetId = overviewId,
            displayName = "Overview",
            order = 0,
            nextSheetId = referenceId,
            subjects = overviewSubjects,
        ),
        ProjectionSheet(
            sheetId = referenceId,
            displayName = "Reference",
            order = 1,
            previousSheetId = overviewId,
            subjects = referenceSubjects,
        ),
    )
}

private fun documentationProjectionNodes(
    view: ViewDefinition,
    nodes: List<ProjectionNode>,
    canvasWidth: Int,
): List<ProjectionNode> {
    val family = (view.familyContract as? ElectricalProjectionDescriptor)?.family
    if (family != ElectricalProjectionFamily.DOCUMENTATION) {
        return nodes
    }
    val referenceCopies = nodes.map { node ->
        node.copy(
            projectionId = ProjectionNodeId("${node.projectionId.value}_reference"),
            bounds = node.bounds.copy(
                x = node.bounds.x + canvasWidth + DOCUMENTATION_REFERENCE_COLUMN_GAP,
            ),
        )
    }
    return nodes + referenceCopies
}

private fun documentationCanvasWidth(
    view: ViewDefinition,
    baseCanvasWidth: Int,
    nodes: List<ProjectionNode>,
): Int {
    val family = (view.familyContract as? ElectricalProjectionDescriptor)?.family
    if (family != ElectricalProjectionFamily.DOCUMENTATION) {
        return baseCanvasWidth
    }
    val maxNodeEdge = nodes.maxOfOrNull { node -> node.bounds.x + node.bounds.width } ?: baseCanvasWidth
    return maxOf(baseCanvasWidth, maxNodeEdge + DOCUMENTATION_CANVAS_MARGIN)
}

private fun documentationCanvasHeight(
    view: ViewDefinition,
    baseCanvasHeight: Int,
    nodes: List<ProjectionNode>,
): Int {
    val family = (view.familyContract as? ElectricalProjectionDescriptor)?.family
    if (family != ElectricalProjectionFamily.DOCUMENTATION) {
        return baseCanvasHeight
    }
    val maxNodeEdge = nodes.maxOfOrNull { node -> node.bounds.y + node.bounds.height } ?: baseCanvasHeight
    return maxOf(baseCanvasHeight, maxNodeEdge + DOCUMENTATION_CANVAS_MARGIN)
}

private fun deriveNotationPack(
    view: ViewDefinition,
    nodes: List<ProjectionNode>,
    connections: List<ProjectionConnection>,
    labels: List<ProjectionLabel>,
): ProjectionNotationPack? {
    val family = (view.familyContract as? ElectricalProjectionDescriptor)?.family ?: return null
    val componentSemanticIds = nodes.map(ProjectionNode::semanticId)
    val connectionSemanticIds = connections.map(ProjectionConnection::semanticId)
    val portSemanticIds = labels.map(ProjectionLabel::semanticId)
    val subjects = buildList {
        when (family) {
            ElectricalProjectionFamily.CABINET -> {
                addNotationSubjects(
                    semanticIds = componentSemanticIds,
                    symbolKey = "device.cabinet.default",
                    labelPolicy = ProjectionLabelPolicy.SUBJECT_LABEL,
                    markerKeys = listOf("owned-device"),
                )
                addNotationSubjects(
                    semanticIds = connectionSemanticIds,
                    symbolKey = "connection.cabinet.default",
                    labelPolicy = ProjectionLabelPolicy.HIDDEN,
                )
                addNotationSubjects(
                    semanticIds = portSemanticIds,
                    symbolKey = "port.cabinet.default",
                    labelPolicy = ProjectionLabelPolicy.TERMINAL_LABEL,
                )
            }

            ElectricalProjectionFamily.WIRING -> {
                addNotationSubjects(
                    semanticIds = componentSemanticIds,
                    symbolKey = "device.wiring.default",
                    labelPolicy = ProjectionLabelPolicy.SUBJECT_LABEL,
                    markerKeys = listOf("connectivity-device"),
                )
                addNotationSubjects(
                    semanticIds = connectionSemanticIds,
                    symbolKey = "connection.wiring.default",
                    labelPolicy = ProjectionLabelPolicy.HIDDEN,
                    markerKeys = listOf("signal-flow"),
                )
                addNotationSubjects(
                    semanticIds = portSemanticIds,
                    symbolKey = "port.wiring.default",
                    labelPolicy = ProjectionLabelPolicy.TERMINAL_LABEL,
                )
            }

            ElectricalProjectionFamily.SCHEMATIC -> {
                addNotationSubjects(
                    semanticIds = componentSemanticIds,
                    symbolKey = "device.schematic.default",
                    labelPolicy = ProjectionLabelPolicy.SUBJECT_LABEL,
                    markerKeys = listOf("canonical-device"),
                )
                addNotationSubjects(
                    semanticIds = connectionSemanticIds,
                    symbolKey = "connection.schematic.default",
                    labelPolicy = ProjectionLabelPolicy.HIDDEN,
                )
                addNotationSubjects(
                    semanticIds = portSemanticIds,
                    symbolKey = "port.schematic.default",
                    labelPolicy = ProjectionLabelPolicy.TERMINAL_LABEL,
                )
            }

            ElectricalProjectionFamily.DOCUMENTATION -> {
                addNotationSubjects(
                    semanticIds = componentSemanticIds,
                    symbolKey = "device.documentation.default",
                    labelPolicy = ProjectionLabelPolicy.SUBJECT_LABEL,
                    markerKeys = listOf("reference-subject"),
                )
                addNotationSubjects(
                    semanticIds = connectionSemanticIds,
                    symbolKey = "connection.documentation.default",
                    labelPolicy = ProjectionLabelPolicy.HIDDEN,
                )
                addNotationSubjects(
                    semanticIds = portSemanticIds,
                    symbolKey = "port.documentation.default",
                    labelPolicy = ProjectionLabelPolicy.TERMINAL_LABEL,
                )
            }
        }
    }
    return ProjectionNotationPack(
        packId = ProjectionNotationPackId("electrical-notation/${family.name.lowercase()}/default-v1"),
        displayName = "Electrical ${family.name.lowercase().replaceFirstChar { character -> character.titlecase() }} Default",
        subjects = subjects.sortedBy { subject -> subject.semanticId.value },
    )
}

private fun deriveCrossReferences(
    view: ViewDefinition,
    sheets: List<ProjectionSheet>,
): List<ProjectionCrossReference> {
    val family = (view.familyContract as? ElectricalProjectionDescriptor)?.family
    if (family != ElectricalProjectionFamily.DOCUMENTATION) {
        return emptyList()
    }
    return sheets
        .flatMap { sheet -> sheet.subjects.map { subject -> sheet to subject } }
        .groupBy(
            keySelector = { (_, subject) -> subject.semanticId },
            valueTransform = { (sheet, subject) -> sheet to subject },
        )
        .mapNotNull { (semanticId, references) ->
            val sheetIds = references
                .map { (sheet, _) -> sheet.sheetId }
                .distinct()
                .sortedBy(ProjectionSheetId::value)
            val occurrenceIds = references
                .flatMap { (_, subject) -> subject.occurrenceIds() }
                .distinct()
                .sorted()
            if (sheetIds.size <= 1 && occurrenceIds.size <= 1) {
                return@mapNotNull null
            }
            ProjectionCrossReference(
                semanticId = semanticId,
                kind = ProjectionCrossReferenceKind.REPEATED_REFERENCE,
                sheetIds = sheetIds,
                occurrenceIds = occurrenceIds,
            )
        }
        .sortedBy { crossReference -> crossReference.semanticId.value }
}

private fun subjectAnchors(
    nodes: List<ProjectionNode>,
    connections: List<ProjectionConnection>,
    labels: List<ProjectionLabel>,
): List<ProjectionSheetSubject> {
    val nodeIdsBySemanticId = nodes.groupBy(ProjectionNode::semanticId)
        .mapValues { (_, groupedNodes) -> groupedNodes.map(ProjectionNode::projectionId).sortedBy(ProjectionNodeId::value) }
    val connectionIdsBySemanticId = connections.groupBy(ProjectionConnection::semanticId)
        .mapValues { (_, groupedConnections) ->
            groupedConnections.map(ProjectionConnection::projectionId).sortedBy(ProjectionConnectionId::value)
        }
    val labelIdsBySemanticId = labels.groupBy(ProjectionLabel::semanticId)
        .mapValues { (_, groupedLabels) -> groupedLabels.map(ProjectionLabel::projectionId).sortedBy(ProjectionLabelId::value) }
    return (nodeIdsBySemanticId.keys + connectionIdsBySemanticId.keys + labelIdsBySemanticId.keys)
        .distinct()
        .sortedBy(StableSemanticIdentity::value)
        .map { semanticId ->
            ProjectionSheetSubject(
                semanticId = semanticId,
                nodeIds = nodeIdsBySemanticId[semanticId].orEmpty(),
                connectionIds = connectionIdsBySemanticId[semanticId].orEmpty(),
                labelIds = labelIdsBySemanticId[semanticId].orEmpty(),
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

private fun ProjectionNode.isDocumentationReferenceNode(): Boolean = projectionId.value.endsWith("_reference")

private fun ProjectionSheetSubject.filtered(
    nodeIds: Set<ProjectionNodeId> = emptySet(),
    connectionIds: Set<ProjectionConnectionId> = emptySet(),
    labelIds: Set<ProjectionLabelId> = emptySet(),
): ProjectionSheetSubject? {
    val filteredNodeIds = if (nodeIds.isEmpty()) emptyList() else this.nodeIds.filter(nodeIds::contains)
    val filteredConnectionIds = if (connectionIds.isEmpty()) emptyList() else this.connectionIds.filter(connectionIds::contains)
    val filteredLabelIds = if (labelIds.isEmpty()) emptyList() else this.labelIds.filter(labelIds::contains)
    if (filteredNodeIds.isEmpty() && filteredConnectionIds.isEmpty() && filteredLabelIds.isEmpty()) {
        return null
    }
    return copy(
        nodeIds = filteredNodeIds,
        connectionIds = filteredConnectionIds,
        labelIds = filteredLabelIds,
    )
}

private fun ProjectionSheetSubject.occurrenceIds(): List<String> {
    return buildList {
        addAll(nodeIds.map(ProjectionNodeId::value))
        addAll(connectionIds.map(ProjectionConnectionId::value))
        addAll(labelIds.map(ProjectionLabelId::value))
    }
}

private fun MutableList<ProjectionNotationSubject>.addNotationSubjects(
    semanticIds: List<StableSemanticIdentity>,
    symbolKey: String,
    labelPolicy: ProjectionLabelPolicy,
    markerKeys: List<String> = emptyList(),
) {
    addAll(
        semanticIds
            .distinct()
            .sortedBy(StableSemanticIdentity::value)
            .map { semanticId ->
                ProjectionNotationSubject(
                    semanticId = semanticId,
                    symbolKey = ProjectionSymbolKey(symbolKey),
                    labelPolicy = labelPolicy,
                    markerKeys = markerKeys,
                )
            },
    )
}

private const val DOCUMENTATION_REFERENCE_COLUMN_GAP = 120
private const val DOCUMENTATION_CANVAS_MARGIN = 40
