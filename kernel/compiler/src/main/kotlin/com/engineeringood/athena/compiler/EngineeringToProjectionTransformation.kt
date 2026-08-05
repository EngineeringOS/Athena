package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringFunction
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringPortOwner
import com.engineeringood.athena.ir.EngineeringReality
import com.engineeringood.athena.ir.EngineeringSubjectReference
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionEndpoint
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
import com.engineeringood.athena.projection.ProjectionOccurrencePort
import com.engineeringood.athena.projection.ProjectionOccurrencePortId
import com.engineeringood.athena.projection.ProjectionReality
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.projection.ProjectionSheetId
import com.engineeringood.athena.projection.ProjectionSheetPublication
import com.engineeringood.athena.projection.ProjectionSheetSubject

class EngineeringToProjectionTransformation(
    private val view: ViewDefinition = ViewDefinition(
        id = "engineering-projection",
        displayName = "Engineering Projection",
    ),
) : RealityTransformation<EngineeringDocument, ProjectionDocument> {
    override fun transform(input: EngineeringDocument): RealityTransformationResult<ProjectionDocument> {
        val engineeringValidation = EngineeringReality.validate(input)
        if (!engineeringValidation.isValid) {
            return engineeringValidation.issues.toTransformationFailure()
        }

        val nodes = entityNodes(input)
        val nodesBySemanticId = nodes.associateBy(ProjectionNode::semanticId)
        val portsBySemanticId = input.ports.associateBy { port -> port.id }
        val functionsById = input.functions.associateBy { function -> function.id }
        val occurrencePorts = input.ports.mapNotNull { port ->
            val ownerId = port.projectedEntityId(functionsById) ?: return@mapNotNull null
            val owner = nodesBySemanticId[ownerId] ?: return@mapNotNull null
            ProjectionOccurrencePort(
                occurrencePortId = ProjectionOccurrencePortId(owner.projectionId, port.id),
                originGeometryElementId = GeometryElementId("projection-origin/port/${port.id.value}"),
            )
        }
        val relationshipConnections = input.relationships
            .filter { relationship ->
                relationship.participants.size == 2 &&
                    relationship.participants.all { participant ->
                        participant.subject is EngineeringSubjectReference.Port
                    }
            }
            .map { relationship ->
                val ports = relationship.participants.map { participant ->
                    (participant.subject as EngineeringSubjectReference.Port).port.resolvedIdentity
                        ?.let(portsBySemanticId::get)
                }
                ProjectionConnection(
                    projectionId = ProjectionConnectionId("projection/relationship/" + relationship.id.value),
                    semanticId = relationship.id,
                    originGeometryElementId = GeometryElementId("projection-origin/relationship/" + relationship.id.value),
                    source = ports.getOrNull(0)?.projectedEntityId(functionsById)
                        ?.let(nodesBySemanticId::get)
                        ?.let { owner -> ProjectionConnectionEndpoint(ProjectionOccurrencePortId(owner.projectionId, ports[0]!!.id)) },
                    target = ports.getOrNull(1)?.projectedEntityId(functionsById)
                        ?.let(nodesBySemanticId::get)
                        ?.let { owner -> ProjectionConnectionEndpoint(ProjectionOccurrencePortId(owner.projectionId, ports[1]!!.id)) },
                )
            }
        val connections = relationshipConnections
        val subjects = sheetSubjects(
            nodes = nodes,
            connections = connections,
        )
        val sheetId = ProjectionSheetId("${view.id}/sheet/01-main")
        val sheet = ProjectionSheet(
            sheetId = sheetId,
            displayName = "${view.displayName} Main",
            order = 0,
            subjects = subjects,
            publication = ProjectionSheetPublication.fromProjectionState(
                sheetId = sheetId,
                displayName = "${view.displayName} Main",
                order = 0,
                subjects = subjects,
            ),
        )
        val output = ProjectionDocument(
            view = view,
            nodes = nodes,
            connections = connections,
            occurrencePorts = occurrencePorts,
            sheets = listOf(sheet),
        )
        val projectionValidation = ProjectionReality.validate(output)
        if (!projectionValidation.isValid) {
            return projectionValidation.issues.toTransformationFailure()
        }
        return RealityTransformationResult.Success(output)
    }

    private fun entityNodes(input: EngineeringDocument): List<ProjectionNode> =
        input.entities
            .map { entity ->
                ProjectionNode(
                    projectionId = ProjectionNodeId("projection/node/${entity.id.value}"),
                    semanticId = entity.id,
                    label = entity.name,
                    originGeometryElementId = GeometryElementId("projection-origin/entity/${entity.id.value}"),
                )
            }

    private fun sheetSubjects(
        nodes: List<ProjectionNode>,
        connections: List<ProjectionConnection>,
    ): List<ProjectionSheetSubject> {
        val nodeSubjects = nodes.map { node ->
            ProjectionSheetSubject(
                semanticId = node.semanticId,
                nodeIds = listOf(node.projectionId),
            )
        }
        val connectionSubjects = connections.map { connection ->
            ProjectionSheetSubject(
                semanticId = connection.semanticId,
                connectionIds = listOf(connection.projectionId),
            )
        }
        return (nodeSubjects + connectionSubjects).sortedBy { subject -> subject.semanticId.value }
    }
}

private fun EngineeringPort.projectedEntityId(
    functionsById: Map<com.engineeringood.athena.ir.StableSemanticIdentity, EngineeringFunction>,
): com.engineeringood.athena.ir.StableSemanticIdentity? = when (val exactOwner = owner) {
    is EngineeringPortOwner.Entity -> exactOwner.entity.reference.resolvedIdentity
    is EngineeringPortOwner.Function -> exactOwner.function.reference.resolvedIdentity
        ?.let(functionsById::get)
        ?.owner
        ?.reference
        ?.resolvedIdentity
}
