package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringReality
import com.engineeringood.athena.layout.ViewDefinition
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionConnectionId
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionNodeId
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

        val nodes = componentNodes(input) + portNodes(input)
        val connections = input.connections
            .map { connection ->
                ProjectionConnection(
                    projectionId = ProjectionConnectionId("projection/connection/${connection.id.value}"),
                    semanticId = connection.id,
                    originGeometryElementId = GeometryElementId("projection-origin/connection/${connection.id.value}"),
                )
            }
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
            sheets = listOf(sheet),
        )
        val projectionValidation = ProjectionReality.validate(output)
        if (!projectionValidation.isValid) {
            return projectionValidation.issues.toTransformationFailure()
        }
        return RealityTransformationResult.Success(output)
    }

    private fun componentNodes(input: EngineeringDocument): List<ProjectionNode> =
        input.components
            .map { component ->
                ProjectionNode(
                    projectionId = ProjectionNodeId("projection/node/${component.id.value}"),
                    semanticId = component.id,
                    label = component.name,
                    originGeometryElementId = GeometryElementId("projection-origin/component/${component.id.value}"),
                )
            }

    private fun portNodes(input: EngineeringDocument): List<ProjectionNode> =
        input.ports
            .map { port ->
                ProjectionNode(
                    projectionId = ProjectionNodeId("projection/node/${port.id.value}"),
                    semanticId = port.id,
                    label = port.name,
                    originGeometryElementId = GeometryElementId("projection-origin/port/${port.id.value}"),
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
