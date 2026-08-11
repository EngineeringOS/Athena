package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.connection.ConnectionDocument
import com.engineeringood.athena.connection.ConnectionFact
import com.engineeringood.athena.connection.NetFact
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringFunction
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringPortOwner
import com.engineeringood.athena.ir.EngineeringReality
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ConnectionProjection
import com.engineeringood.athena.projection.ConnectionProjectionEndpoint
import com.engineeringood.athena.projection.ConnectionProjectionParticipant
import com.engineeringood.athena.projection.ConnectionProjectionId
import com.engineeringood.athena.projection.ConnectionProjectionIdentityKind
import com.engineeringood.athena.projection.ConnectionProjectionRole
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
    private val connectionIr: ConnectionDocument? = null,
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
        val connections = connectionIr?.let { ir ->
            (ir.connections.map { Triple(it.id, it.endpoints, ConnectionProjectionIdentityKind.CONNECTION) } +
                ir.nets.map { Triple(it.id, it.endpoints, ConnectionProjectionIdentityKind.NET) })
                .mapNotNull { (identity, endpoints, identityKind) ->
                    val participants = endpoints.mapNotNull { endpoint ->
                        val port = portsBySemanticId[endpoint.portId] ?: return@mapNotNull null
                        val ownerId = port.projectedEntityId(functionsById) ?: return@mapNotNull null
                        val owner = nodesBySemanticId[ownerId] ?: return@mapNotNull null
                        ConnectionProjectionParticipant(
                            role = endpoint.role,
                            endpoint = ConnectionProjectionEndpoint(
                                com.engineeringood.athena.projection.ProjectionOccurrencePortId(owner.projectionId, endpoint.portId),
                            ),
                        )
                    }
                    if (participants.size != endpoints.size) return@mapNotNull null
                    val origin = GeometryElementId("projection-origin/connection/${identity.value}")
                    ConnectionProjection(
                        projectionId = ConnectionProjectionId("${view.id}/${identityKind.name.lowercase()}/${identity.value}"),
                        semanticId = identity,
                        identityKind = identityKind,
                        role = if (identityKind == ConnectionProjectionIdentityKind.NET) ConnectionProjectionRole.NET else ConnectionProjectionRole.CONNECTION,
                        originGeometryElementId = origin,
                        participants = participants,
                    )
                }
        }.orEmpty()
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
        connections: List<ConnectionProjection>,
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
