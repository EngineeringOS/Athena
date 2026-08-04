package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext
import com.engineeringood.athena.document.BuiltInDocumentProjectionPolicies
import com.engineeringood.athena.document.DocumentProjectionPolicy
import com.engineeringood.athena.document.SheetViewRole
import com.engineeringood.athena.geometry.GeometryDocument
import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.geometry.GeometryElementKind
import com.engineeringood.athena.layout.ElectricalProjectionDescriptor
import com.engineeringood.athena.layout.ElectricalProjectionFamily
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionEndpoint
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ProjectionCrossReference
import com.engineeringood.athena.projection.ProjectionCrossReferenceId
import com.engineeringood.athena.projection.ProjectionCrossReferenceKind
import com.engineeringood.athena.projection.ProjectionCrossReferenceLink
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionLabelPolicy
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionOccurrencePort
import com.engineeringood.athena.projection.ProjectionOccurrencePortId
import com.engineeringood.athena.projection.ProjectionNotationPack
import com.engineeringood.athena.projection.ProjectionNotationPackId
import com.engineeringood.athena.projection.ProjectionNotationSubject
import com.engineeringood.athena.projection.ProjectionPhysicalSize
import com.engineeringood.athena.projection.ProjectionResolvedSubject
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetPolicyEvidence
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
                    originGeometryElementId = element.elementId,
                )
            }
        val nodes = baseNodes
        val nodesBySemanticId = nodes.groupBy(ProjectionNode::semanticId)
        val engineeringPortsById = document.ports.associateBy { port -> port.id }
        val occurrencePorts = document.ports.flatMap { port ->
            val ownerId = port.ownerReference.resolvedIdentity ?: return@flatMap emptyList()
            nodesBySemanticId[ownerId].orEmpty().map { owner ->
                ProjectionOccurrencePort(
                    occurrencePortId = ProjectionOccurrencePortId(owner.projectionId, port.id),
                    originGeometryElementId = geometry.elements
                        .firstOrNull { element -> element.semanticId == port.id }
                        ?.elementId
                        ?: GeometryElementId("${view.id}/projection/origin/${port.id.value}"),
                )
            }
        }
        val connections = geometry.elements
            .filter { element -> element.kind == GeometryElementKind.PATH }
            .map { element ->
                val engineeringConnection = document.connections.singleOrNull { connection ->
                    connection.id == element.semanticId
                }
                ProjectionConnection(
                    projectionId = ProjectionConnectionId(
                        element.elementId.value.replace("/geometry/path/", "/projection/connection/"),
                    ),
                    semanticId = element.semanticId,
                    originGeometryElementId = element.elementId,
                    source = engineeringConnection?.from?.resolvedIdentity?.let { portId ->
                        val ownerId = engineeringPortsById[portId]?.ownerReference?.resolvedIdentity
                        val owner = ownerId?.let(nodesBySemanticId::get)?.singleOrNull()
                        owner?.let { ProjectionConnectionEndpoint(ProjectionOccurrencePortId(it.projectionId, portId)) }
                    },
                    target = engineeringConnection?.to?.resolvedIdentity?.let { portId ->
                        val ownerId = engineeringPortsById[portId]?.ownerReference?.resolvedIdentity
                        val owner = ownerId?.let(nodesBySemanticId::get)?.singleOrNull()
                        owner?.let { ProjectionConnectionEndpoint(ProjectionOccurrencePortId(it.projectionId, portId)) }
                    },
                )
            }
        val sheets = deriveSheets(
            view = view,
            nodes = nodes,
            connections = connections,
        )
        val resolvedSubjects = deriveResolvedSubjects(
            document = document,
            knowledgeContext = knowledgeContext,
        )
        return ProjectionDocument(
            view = view,
            nodes = nodes,
            connections = connections,
            occurrencePorts = occurrencePorts,
            resolvedSubjects = resolvedSubjects,
            sheets = sheets,
            notationPack = deriveNotationPack(
                view = view,
                nodes = nodes,
                connections = connections,
            ),
            crossReferences = deriveCrossReferences(
                view = view,
                sheets = sheets,
            ),
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
): List<ProjectionSheet> {
    val allSubjects = subjectAnchors(
        nodes = nodes,
        connections = connections,
    )
    return when ((view.familyContract as? ElectricalProjectionDescriptor)?.family) {
        ElectricalProjectionFamily.DOCUMENTATION -> documentationSheets(
            view = view,
            allSubjects = allSubjects,
            nodes = nodes,
            connections = connections,
        )

        else -> listOf(
            ProjectionSheet(
                sheetId = ProjectionSheetId("${view.id}/sheet/01-main"),
                displayName = "${view.displayName} Main",
                order = 0,
                subjects = allSubjects,
                publication = ProjectionSheetPublication.fromProjectionState(
                    sheetId = ProjectionSheetId("${view.id}/sheet/01-main"),
                    displayName = "${view.displayName} Main",
                    order = 0,
                    subjects = allSubjects,
                ),
            ),
        )
    }
}

private fun documentationSheets(
    view: ViewDefinition,
    allSubjects: List<ProjectionSheetSubject>,
    nodes: List<ProjectionNode>,
    connections: List<ProjectionConnection>,
): List<ProjectionSheet> {
    val policy = BuiltInDocumentProjectionPolicies.athenaCustomerDocumentProjection()
    val controlRole = policy.supportedSheetViewRoles.single { role ->
        role.role == SheetViewRole.CONTROL_AND_PLC_LOGIC
    }
    val fieldRole = policy.supportedSheetViewRoles.single { role ->
        role.role == SheetViewRole.FIELD_WIRING_AND_TERMINAL_TRANSITION
    }
    val controlSheetId = ProjectionSheetId("${view.id}/sheet/01-control")
    val fieldSheetId = ProjectionSheetId("${view.id}/sheet/02-field-device")
    val overviewNodeIds = nodes.map(ProjectionNode::projectionId).toSet()
    val overviewConnectionIds = connections.map(ProjectionConnection::projectionId).toSet()
    val powerSubjects = documentationSheetSubjects(
        allSubjects = allSubjects,
        selectedSubjects = allSubjects.filter { subject ->
            subject.isPowerDistributionSubject() || !subject.hasRecognizedDocumentationRole()
        },
        nodeIds = overviewNodeIds,
        connectionIds = overviewConnectionIds,
    ).ifEmpty {
        documentationSheetSubjects(
            allSubjects = allSubjects,
            selectedSubjects = allSubjects,
            nodeIds = overviewNodeIds,
            connectionIds = overviewConnectionIds,
        )
    }
    val controlSubjects = documentationSheetSubjects(
        allSubjects = allSubjects,
        selectedSubjects = allSubjects.filter { subject ->
            subject.isControlLogicSubject() || subject.isPowerDistributionSubject() || !subject.hasRecognizedDocumentationRole()
        },
        nodeIds = overviewNodeIds,
        connectionIds = overviewConnectionIds,
    ).ifEmpty { powerSubjects }
    val fieldSubjects = documentationSheetSubjects(
        allSubjects = allSubjects,
        selectedSubjects = allSubjects.filter { subject ->
            subject.isFieldWiringSubject() || !subject.hasRecognizedDocumentationRole()
        },
        nodeIds = overviewNodeIds,
        connectionIds = overviewConnectionIds,
    )
    return listOf(
        ProjectionSheet(
            sheetId = controlSheetId,
            displayName = controlRole.displayTitle,
            order = controlRole.order,
            nextSheetId = fieldSheetId,
            subjects = controlSubjects,
            policyEvidence = policy.toProjectionSheetPolicyEvidence(
                role = controlRole.role,
                roleOrder = controlRole.order,
            ),
            publication = ProjectionSheetPublication.fromProjectionState(
                sheetId = controlSheetId,
                displayName = controlRole.displayTitle,
                order = controlRole.order,
                subjects = controlSubjects,
            ),
        ),
        ProjectionSheet(
            sheetId = fieldSheetId,
            displayName = fieldRole.displayTitle,
            order = fieldRole.order,
            previousSheetId = controlSheetId,
            subjects = fieldSubjects,
            policyEvidence = policy.toProjectionSheetPolicyEvidence(
                role = fieldRole.role,
                roleOrder = fieldRole.order,
            ),
            publication = ProjectionSheetPublication.fromProjectionState(
                sheetId = fieldSheetId,
                displayName = fieldRole.displayTitle,
                order = fieldRole.order,
                subjects = fieldSubjects,
            ),
        ),
    )
}

private fun DocumentProjectionPolicy.toProjectionSheetPolicyEvidence(
    role: SheetViewRole,
    roleOrder: Int,
): ProjectionSheetPolicyEvidence {
    return ProjectionSheetPolicyEvidence(
        policyId = policyId.value,
        policyVersion = policyVersion.value,
        policyDeterministicIdentity = deterministicIdentity.value,
        sheetViewRole = role.name.lowercase().replace('_', '-'),
        sheetViewRoleOrder = roleOrder,
    )
}

private fun documentationSheetSubjects(
    allSubjects: List<ProjectionSheetSubject>,
    selectedSubjects: List<ProjectionSheetSubject>,
    nodeIds: Set<ProjectionNodeId>,
    connectionIds: Set<ProjectionConnectionId>,
): List<ProjectionSheetSubject> {
    val subjectBySemanticId = allSubjects.associateBy(ProjectionSheetSubject::semanticId)
    val selectedSemanticIds = selectedSubjects.map(ProjectionSheetSubject::semanticId).toSet()
    val owningComponents = selectedSubjects
        .flatMap(ProjectionSheetSubject::owningComponentSemanticIds)
        .filterNot(selectedSemanticIds::contains)
        .mapNotNull(subjectBySemanticId::get)
    return (selectedSubjects + owningComponents)
        .distinctBy(ProjectionSheetSubject::semanticId)
        .mapNotNull { subject ->
            subject.filtered(
                nodeIds = nodeIds,
                connectionIds = connectionIds,
            )
        }
        .sortedBy { subject -> subject.semanticId.value }
}

private fun deriveNotationPack(
    view: ViewDefinition,
    nodes: List<ProjectionNode>,
    connections: List<ProjectionConnection>,
): ProjectionNotationPack? {
    val family = (view.familyContract as? ElectricalProjectionDescriptor)?.family ?: return null
    val componentSemanticIds = nodes.map(ProjectionNode::semanticId)
    val connectionSemanticIds = connections.map(ProjectionConnection::semanticId)
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
            }
        }
    }
    return ProjectionNotationPack(
        packId = ProjectionNotationPackId("electrical-notation/${family.name.lowercase()}/default"),
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
            val referencesBySheet = references
                .mapNotNull { (sheet, subject) ->
                    val occurrenceId = subject.occurrenceIds().firstOrNull() ?: return@mapNotNull null
                    CrossReferenceEndpoint(
                        sheet = sheet,
                        occurrenceId = occurrenceId,
                    )
                }
                .distinctBy { endpoint -> endpoint.sheet.sheetId }
                .sortedWith(compareBy<CrossReferenceEndpoint> { endpoint -> endpoint.sheet.order }.thenBy { endpoint ->
                    endpoint.sheet.sheetId.value
                })
            val sheetIds = referencesBySheet
                .map { endpoint -> endpoint.sheet.sheetId }
            val occurrenceIds = references
                .flatMap { (_, subject) -> subject.occurrenceIds() }
                .distinct()
                .sorted()
            val links = referencesBySheet.zipWithNext { source, target ->
                ProjectionCrossReferenceLink(
                    semanticId = semanticId,
                    sourceSheetId = source.sheet.sheetId,
                    targetSheetId = target.sheet.sheetId,
                    sourceOccurrenceId = source.occurrenceId,
                    targetOccurrenceId = target.occurrenceId,
                    compactNotation = "${source.sheet.sheetId.compactSheetNotation()} -> ${target.sheet.sheetId.compactSheetNotation()}",
                )
            }
            if (sheetIds.size <= 1 || links.isEmpty()) {
                return@mapNotNull null
            }
            ProjectionCrossReference(
                semanticId = semanticId,
                kind = ProjectionCrossReferenceKind.REPEATED_REFERENCE,
                crossReferenceId = ProjectionCrossReferenceId("cross-reference:${semanticId.value}"),
                sheetIds = sheetIds,
                occurrenceIds = occurrenceIds,
                links = links,
            )
        }
        .sortedBy { crossReference -> crossReference.semanticId.value }
}

private data class CrossReferenceEndpoint(
    val sheet: ProjectionSheet,
    val occurrenceId: String,
)

private fun ProjectionSheetId.compactSheetNotation(): String {
    return value.substringAfterLast("/sheet/", value)
}

private fun subjectAnchors(
    nodes: List<ProjectionNode>,
    connections: List<ProjectionConnection>,
): List<ProjectionSheetSubject> {
    val nodeIdsBySemanticId = nodes.groupBy(ProjectionNode::semanticId)
        .mapValues { (_, groupedNodes) -> groupedNodes.map(ProjectionNode::projectionId).sortedBy(ProjectionNodeId::value) }
    val connectionIdsBySemanticId = connections.groupBy(ProjectionConnection::semanticId)
        .mapValues { (_, groupedConnections) ->
            groupedConnections.map(ProjectionConnection::projectionId).sortedBy(ProjectionConnectionId::value)
        }
    return (nodeIdsBySemanticId.keys + connectionIdsBySemanticId.keys)
        .distinct()
        .sortedBy(StableSemanticIdentity::value)
        .map { semanticId ->
            ProjectionSheetSubject(
                semanticId = semanticId,
                nodeIds = nodeIdsBySemanticId[semanticId].orEmpty(),
                connectionIds = connectionIdsBySemanticId[semanticId].orEmpty(),
            )
        }
}

private fun ProjectionSheetSubject.isPowerDistributionSubject(): Boolean {
    val normalized = semanticId.value.lowercase()
    return listOf("power", "supply", "ps", "breaker", "qf", "line", "load", "lplus").any(normalized::contains)
}

private fun ProjectionSheetSubject.isControlLogicSubject(): Boolean {
    val normalized = semanticId.value.lowercase()
    return listOf("controller", "plc", "hmi", "operator", "status", "do").any(normalized::contains)
}

private fun ProjectionSheetSubject.isFieldWiringSubject(): Boolean {
    val normalized = semanticId.value.lowercase()
    return listOf("field", "terminal", "xt", "motor", "conveyor", "u1", "component:m", "port:m", "do")
        .any(normalized::contains)
}

private fun ProjectionSheetSubject.hasRecognizedDocumentationRole(): Boolean {
    return isPowerDistributionSubject() || isControlLogicSubject() || isFieldWiringSubject()
}

private fun ProjectionSheetSubject.owningComponentSemanticIds(): List<StableSemanticIdentity> {
    val value = semanticId.value
    val ownerNames = when {
        value.startsWith("port:") -> listOf(value.removePrefix("port:").substringBefore('.'))
        else -> emptyList()
    }
    return ownerNames
        .filter(String::isNotBlank)
        .distinct()
        .map { ownerName -> StableSemanticIdentity("component:$ownerName") }
}

private fun ProjectionSheetSubject.filtered(
    nodeIds: Set<ProjectionNodeId> = emptySet(),
    connectionIds: Set<ProjectionConnectionId> = emptySet(),
): ProjectionSheetSubject? {
    val filteredNodeIds = if (nodeIds.isEmpty()) emptyList() else this.nodeIds.filter(nodeIds::contains)
    val filteredConnectionIds = if (connectionIds.isEmpty()) emptyList() else this.connectionIds.filter(connectionIds::contains)
    if (filteredNodeIds.isEmpty() && filteredConnectionIds.isEmpty()) {
        return null
    }
    return copy(
        nodeIds = filteredNodeIds,
        connectionIds = filteredConnectionIds,
    )
}

private fun ProjectionSheetSubject.occurrenceIds(): List<String> {
    return buildList {
        addAll(nodeIds.map(ProjectionNodeId::value))
        addAll(connectionIds.map(ProjectionConnectionId::value))
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
