package com.engineeringood.athena.compiler

import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionOccurrencePortId
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.spatial.SpatialAnchorId
import com.engineeringood.athena.spatial.SpatialConstructId
import com.engineeringood.athena.spatial.SpatialGridReferenceSubject
import com.engineeringood.athena.spatial.SpatialOccurrenceId
import com.engineeringood.athena.spatial.SpatialRegionId
import com.engineeringood.athena.spatial.SpatialRouteId
import com.engineeringood.athena.spatial.SpatialSourceTrace

internal class ProjectionSpatialCoverageInventory(
    private val planner: ProjectionPlacementPlanner,
) {
    fun occurrenceExpectations(
        projection: ProjectionDocument,
        sheet: ProjectionSheet,
    ): List<CoverageExpectation<SpatialOccurrenceId>> {
        val groupByNodeId = planner.placementGroups(sheet, projection).flatMap { group ->
            group.nodes.map { node -> node.projectionId to group }
        }.toMap()
        return projection.nodes.filter { node -> planner.sheetOwns(sheet, node) }.map { node ->
            val key = SpatialOccurrenceId(sheet.sheetId.value, node.projectionId.value)
            CoverageExpectation(
                key = key,
                subject = "Occurrence ${node.projectionId.value} on Sheet ${sheet.sheetId.value}",
                sourceTrace = occurrenceTrace(sheet, node),
                canonicalSourceTrace = SpatialSourceTrace(
                    projectionIds = listOf(
                        sheet.sheetId.value,
                        groupByNodeId.getValue(node.projectionId).regionId,
                        node.projectionId.value,
                    ),
                    geometryElementIds = listOf(node.originGeometryElementId),
                ),
            )
        }
    }

    fun regionExpectations(
        projection: ProjectionDocument,
        sheet: ProjectionSheet,
    ): List<CoverageExpectation<SpatialRegionId>> {
        val sheetNodesByLabel = projection.nodes.filter { node -> planner.sheetOwns(sheet, node) }
            .associateBy(ProjectionNode::label)
        return planner.placementGroups(sheet, projection).map { group ->
            val authored = sheet.regions.singleOrNull { region -> region.regionId == group.regionId }
            val memberNodes = authored?.occurrenceNames?.mapNotNull(sheetNodesByLabel::get) ?: group.nodes
            CoverageExpectation(
                key = SpatialRegionId(sheet.sheetId.value, group.regionId),
                subject = "Region ${group.regionId} on Sheet ${sheet.sheetId.value}",
                sourceTrace = SpatialSourceTrace(
                    projectionIds = listOf(sheet.sheetId.value, group.regionId) +
                        memberNodes.map { node -> node.projectionId.value },
                    geometryElementIds = listOf(authored?.originGeometryElementId ?: sheet.originGeometryElementId) +
                        memberNodes.map(ProjectionNode::originGeometryElementId),
                ),
            )
        }
    }

    fun constructExpectations(
        projection: ProjectionDocument,
        sheet: ProjectionSheet,
    ): List<CoverageExpectation<SpatialConstructId>> {
        val sheetNodesByLabel = projection.nodes.filter { node -> planner.sheetOwns(sheet, node) }
            .associateBy(ProjectionNode::label)
        return sheet.constructs.map { construct ->
            val memberNodes = construct.memberNames.mapNotNull(sheetNodesByLabel::get)
            CoverageExpectation(
                key = SpatialConstructId(sheet.sheetId.value, construct.constructId.value),
                subject = "Construct ${construct.constructId.value} on Sheet ${sheet.sheetId.value}",
                sourceTrace = SpatialSourceTrace(
                    projectionIds = listOf(sheet.sheetId.value, construct.constructId.value) +
                        memberNodes.map { node -> node.projectionId.value },
                    geometryElementIds = listOf(construct.originGeometryElementId) +
                        memberNodes.map(ProjectionNode::originGeometryElementId),
                ),
            )
        }
    }

    fun anchorExpectations(
        projection: ProjectionDocument,
        sheet: ProjectionSheet,
    ): List<CoverageExpectation<SpatialAnchorId>> {
        val visibleConnections = visibleConnections(projection, sheet)
        val incidents = visibleConnections.flatMap { connection ->
            listOfNotNull(connection.source?.occurrencePortId, connection.target?.occurrencePortId)
                .map { endpoint -> endpoint to connection }
        }.groupBy(keySelector = { (endpoint, _) -> endpoint }, valueTransform = { (_, connection) -> connection })
        return incidents.map { (endpoint, connections) ->
            val node = projection.nodes.first { candidate -> candidate.projectionId == endpoint.occurrenceId }
            val key = SpatialAnchorId(
                sheetId = sheet.sheetId.value,
                occurrenceId = SpatialOccurrenceId(sheet.sheetId.value, endpoint.occurrenceId.value),
                portId = endpoint.portId,
            )
            CoverageExpectation(
                key = key,
                subject = "Anchor ${key.value}",
                sourceTrace = endpointTrace(projection, sheet, node, endpoint, connections),
                canonicalSourceTrace = canonicalAnchorTrace(projection, sheet, node, endpoint, connections),
            )
        }
    }

    fun routeExpectations(
        projection: ProjectionDocument,
        sheet: ProjectionSheet,
    ): List<CoverageExpectation<SpatialRouteId>> = visibleConnections(projection, sheet).map { connection ->
        val routeId = SpatialRouteId(sheet.sheetId.value, connection.projectionId.value)
        CoverageExpectation(
            key = routeId,
            subject = "Route ${routeId.value}",
            sourceTrace = routeTrace(projection, sheet, connection),
            canonicalSourceTrace = canonicalRouteTrace(projection, sheet, connection),
        )
    }

    fun gridReferenceExpectations(
        occurrences: List<CoverageExpectation<SpatialOccurrenceId>>,
        constructs: List<CoverageExpectation<SpatialConstructId>>,
    ): List<CoverageExpectation<SpatialGridReferenceSubject>> =
        occurrences.map { occurrence ->
            val subject: SpatialGridReferenceSubject = SpatialGridReferenceSubject.Occurrence(occurrence.key)
            CoverageExpectation(
                subject,
                gridReferenceSubject(subject),
                occurrence.sourceTrace,
                occurrence.canonicalSourceTrace,
            )
        } + constructs.map { construct ->
            val subject: SpatialGridReferenceSubject = SpatialGridReferenceSubject.Construct(construct.key)
            CoverageExpectation(
                subject,
                gridReferenceSubject(subject),
                construct.sourceTrace,
                construct.canonicalSourceTrace,
            )
        }

    fun visibleConnections(projection: ProjectionDocument, sheet: ProjectionSheet): List<ProjectionConnection> =
        projection.connections
            .filter { connection -> connection.projectionId in sheet.subjects.flatMap { it.connectionIds } }
            .sortedBy { connection -> connection.projectionId.value }

    private fun endpointTrace(
        projection: ProjectionDocument,
        sheet: ProjectionSheet,
        node: ProjectionNode,
        endpoint: ProjectionOccurrencePortId,
        connections: List<ProjectionConnection>,
    ): SpatialSourceTrace = SpatialSourceTrace(
        projectionIds = listOf(sheet.sheetId.value, node.projectionId.value, endpoint.portId.value) +
            connections.map { connection -> connection.projectionId.value }.distinct().sorted(),
        geometryElementIds = (
            listOf(sheet.originGeometryElementId, node.originGeometryElementId) +
                projection.occurrencePorts.filter { port -> port.occurrencePortId == endpoint }
                    .map { port -> port.originGeometryElementId } +
                connections.map(ProjectionConnection::originGeometryElementId)
            ).distinctBy { geometryId -> geometryId.value }.sortedBy { geometryId -> geometryId.value },
    )

    private fun routeTrace(
        projection: ProjectionDocument,
        sheet: ProjectionSheet,
        connection: ProjectionConnection,
    ): SpatialSourceTrace {
        val endpoints = listOfNotNull(connection.source?.occurrencePortId, connection.target?.occurrencePortId)
        val nodes = endpoints.mapNotNull { endpoint ->
            projection.nodes.firstOrNull { node -> node.projectionId == endpoint.occurrenceId }
        }
        return SpatialSourceTrace(
            projectionIds = listOf(sheet.sheetId.value, connection.projectionId.value) +
                endpoints.flatMap { endpoint -> listOf(endpoint.occurrenceId.value, endpoint.portId.value) },
            geometryElementIds = (
                listOf(sheet.originGeometryElementId, connection.originGeometryElementId) +
                    nodes.map(ProjectionNode::originGeometryElementId) +
                    projection.occurrencePorts.filter { port -> port.occurrencePortId in endpoints }
                        .map { port -> port.originGeometryElementId }
                ).distinctBy { geometryId -> geometryId.value }.sortedBy { geometryId -> geometryId.value },
        )
    }

    private fun canonicalAnchorTrace(
        projection: ProjectionDocument,
        sheet: ProjectionSheet,
        node: ProjectionNode,
        endpoint: ProjectionOccurrencePortId,
        connections: List<ProjectionConnection>,
    ): SpatialSourceTrace {
        val required = endpointTrace(projection, sheet, node, endpoint, connections)
        val group = planner.placementGroups(sheet, projection).single { candidate ->
            candidate.nodes.any { member -> member.projectionId == node.projectionId }
        }
        return SpatialSourceTrace(
            projectionIds = required.projectionIds + listOf(group.regionId).filterNot(required.projectionIds::contains),
            geometryElementIds = required.geometryElementIds,
        )
    }

    private fun canonicalRouteTrace(
        projection: ProjectionDocument,
        sheet: ProjectionSheet,
        connection: ProjectionConnection,
    ): SpatialSourceTrace {
        val source = requireNotNull(connection.source).occurrencePortId
        val target = requireNotNull(connection.target).occurrencePortId
        val visible = visibleConnections(projection, sheet)
        fun anchorTrace(endpoint: ProjectionOccurrencePortId): SpatialSourceTrace {
            val node = projection.nodes.single { candidate -> candidate.projectionId == endpoint.occurrenceId }
            val incidents = visible.filter { candidate ->
                candidate.source?.occurrencePortId == endpoint || candidate.target?.occurrencePortId == endpoint
            }
            return canonicalAnchorTrace(projection, sheet, node, endpoint, incidents)
        }
        val sourceTrace = anchorTrace(source)
        val targetTrace = anchorTrace(target)
        val requiredProjectionIds = listOf(
            sheet.sheetId.value,
            connection.projectionId.value,
            source.occurrenceId.value,
            source.portId.value,
            target.occurrenceId.value,
            target.portId.value,
        )
        val requiredSet = requiredProjectionIds.toSet()
        return SpatialSourceTrace(
            projectionIds = requiredProjectionIds +
                (sourceTrace.projectionIds + targetTrace.projectionIds)
                    .filterNot(requiredSet::contains)
                    .distinct()
                    .sorted(),
            geometryElementIds = (
                listOf(connection.originGeometryElementId) +
                    sourceTrace.geometryElementIds +
                    targetTrace.geometryElementIds
                ).distinctBy { geometryId -> geometryId.value }.sortedBy { geometryId -> geometryId.value },
        )
    }
}
