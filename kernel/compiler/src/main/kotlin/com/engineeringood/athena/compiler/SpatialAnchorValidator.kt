package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.projection.ProjectionConnection
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionNode
import com.engineeringood.athena.projection.ProjectionOccurrencePortId
import com.engineeringood.athena.projection.ProjectionSheet
import com.engineeringood.athena.spatial.SpatialBoundarySide
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialSourceTrace

internal class SpatialAnchorValidator {
    fun validate(
        projection: ProjectionDocument,
        occurrences: List<SpatialOccurrenceGeometry>,
    ): List<SpatialDiagnostic> {
        val connectionsById = projection.connections.groupBy { connection -> connection.projectionId }
        val endpointIncidents = projection.connections.flatMap { connection ->
            listOfNotNull(
                connection.source?.occurrencePortId?.let { endpoint -> endpoint to connection },
                connection.target?.occurrencePortId?.let { endpoint -> endpoint to connection },
            )
        }.groupBy(keySelector = { (endpoint, _) -> endpoint }, valueTransform = { (_, connection) -> connection })
        return buildList {
            connectionsById
                .filterValues { connections -> connections.size > 1 }
                .forEach { (connectionId, duplicates) ->
                    add(
                        diagnostic(
                            subject = "Connection identity ${connectionId.value}",
                            problem = "is used by ${duplicates.size} projected Connections",
                            correction = "Give every projected Connection a unique identity before choosing Anchor sides.",
                            projectionIds = listOf(connectionId.value),
                            geometryIds = duplicates.map(ProjectionConnection::originGeometryElementId),
                        ),
                    )
                }
            projection.connections.forEach { connection ->
                if (connection.source == null) {
                    add(missingEndpointDiagnostic(connection, "source"))
                }
                if (connection.target == null) {
                    add(missingEndpointDiagnostic(connection, "target"))
                }
                val source = connection.source
                if (source != null && source == connection.target) {
                    val endpoint = source.occurrencePortId
                    add(
                        diagnostic(
                            subject = "Connection ${connection.projectionId.value}",
                            problem = "uses ${endpointText(endpoint)} as both source and target",
                            correction = "Connect two distinct occurrence-ports.",
                            projectionIds = listOf(
                                connection.projectionId.value,
                                endpoint.occurrenceId.value,
                                endpoint.portId.value,
                            ),
                            geometryIds = listOf(connection.originGeometryElementId),
                        ),
                    )
                }
            }
            endpointIncidents.forEach { (endpoint, incidentConnections) ->
                addAll(endpointDiagnostics(endpoint, incidentConnections, projection, occurrences))
            }
        }.canonicalDiagnostics()
    }

    fun validateCapacity(requests: List<SpatialAnchorRequest>): List<SpatialDiagnostic> =
        requests.groupBy { request -> request.geometry.occurrenceId to request.side }
            .entries
            .mapNotNull { (_, grouped) ->
                val first = grouped.first()
                val edgeLength = when (first.side) {
                    SpatialBoundarySide.LEFT, SpatialBoundarySide.RIGHT -> first.geometry.rectangle.height
                    SpatialBoundarySide.TOP, SpatialBoundarySide.BOTTOM -> first.geometry.rectangle.width
                }
                if (edgeLength >= grouped.size + 1) return@mapNotNull null
                diagnostic(
                    subject = "Occurrence ${first.geometry.occurrenceId.projectionId} ${first.side.name.lowercase()} side",
                    problem = "has $edgeLength drawing units for ${grouped.size} referenced ports",
                    correction = "Provide at least ${grouped.size + 1} drawing units on this side so every Anchor is distinct and non-corner.",
                    projectionIds = listOf(
                        first.geometry.sheetId,
                        first.geometry.occurrenceId.projectionId,
                    ) + grouped.map { request -> request.endpoint.portId.value }.sorted(),
                    geometryIds = grouped.flatMap(SpatialAnchorRequest::traceGeometryIds),
                )
            }.canonicalDiagnostics()

    private fun endpointDiagnostics(
        endpoint: ProjectionOccurrencePortId,
        incidentConnections: List<ProjectionConnection>,
        projection: ProjectionDocument,
        occurrences: List<SpatialOccurrenceGeometry>,
    ): List<SpatialDiagnostic> {
        val nodes = projection.nodes.filter { node -> node.projectionId == endpoint.occurrenceId }
        val portFacts = projection.occurrencePorts.filter { port -> port.occurrencePortId == endpoint }
        val geometries = occurrences.filter { geometry ->
            geometry.occurrenceId.projectionId == endpoint.occurrenceId.value
        }
        val owners = nodes.singleOrNull()?.let { node -> owningSheets(node, projection.sheets) }.orEmpty()
        val ownerSheetIds = owners.map { sheet -> sheet.sheetId.value }.toSet()
        val selectedSheetIds = if (owners.size <= 1) {
            owners.map { sheet -> sheet.sheetId.value }
        } else {
            incidentConnections.mapNotNull { connection ->
                projection.connectionSheetIds(connection).singleOrNull()?.takeIf(ownerSheetIds::contains)
            }.distinct().sorted()
        }
        val unresolvedIncidentSheet = owners.size > 1 && incidentConnections.any { connection ->
            projection.connectionSheetIds(connection).singleOrNull() !in ownerSheetIds
        }
        val projectionIds = owners.map { sheet -> sheet.sheetId.value } +
            listOf(endpoint.occurrenceId.value, endpoint.portId.value)
        val geometryIds = owners.map { sheet -> sheet.originGeometryElementId } +
            nodes.map(ProjectionNode::originGeometryElementId) +
            portFacts.map { port -> port.originGeometryElementId } +
            geometries.flatMap { geometry -> geometry.sourceTrace.geometryElementIds }
        return buildList {
            if (nodes.isEmpty()) {
                add(
                    diagnostic(
                        subject = endpointText(endpoint),
                        problem = "references an unknown Occurrence",
                        correction = "Project Occurrence ${endpoint.occurrenceId.value} before referencing its port.",
                        projectionIds = projectionIds,
                        geometryIds = geometryIds,
                    ),
                )
            } else if (nodes.size > 1) {
                add(
                    diagnostic(
                        subject = "Occurrence ${endpoint.occurrenceId.value}",
                        problem = "has ${nodes.size} Projection occurrence facts",
                        correction = "Publish exactly one projected Occurrence with this identity.",
                        projectionIds = projectionIds,
                        geometryIds = geometryIds,
                    ),
                )
            }
            if (nodes.size == 1 && owners.isEmpty()) {
                add(
                    diagnostic(
                        subject = "Occurrence ${endpoint.occurrenceId.value}",
                        problem = "resolves to ${owners.size} owning Sheets",
                        correction = "Reference this Occurrence from at least one Sheet subject list.",
                        projectionIds = projectionIds + owners.map { sheet -> sheet.sheetId.value },
                        geometryIds = geometryIds + owners.map { sheet -> sheet.originGeometryElementId },
                    ),
                )
            }
            if (nodes.size == 1 && unresolvedIncidentSheet) {
                add(
                    diagnostic(
                        subject = "Occurrence ${endpoint.occurrenceId.value}",
                        problem = "cannot select an owning Sheet from its incident Connections",
                        correction = "Publish every incident Connection on exactly one Sheet that contains this Occurrence.",
                        projectionIds = projectionIds + incidentConnections.map { connection -> connection.projectionId.value },
                        geometryIds = geometryIds + incidentConnections.map(ProjectionConnection::originGeometryElementId),
                    ),
                )
            }
            if (portFacts.size != 1) {
                add(
                    diagnostic(
                        subject = endpointText(endpoint),
                        problem = "has ${portFacts.size} projected occurrence-port facts",
                        correction = "Publish exactly one occurrence-port fact for this referenced engineering port.",
                        projectionIds = projectionIds,
                        geometryIds = geometryIds,
                    ),
                )
            }
            val selectedGeometries = geometries.filter { geometry -> geometry.sheetId in selectedSheetIds }
            if (geometries.isEmpty()) {
                add(
                    diagnostic(
                        subject = "Occurrence ${endpoint.occurrenceId.value}",
                        problem = "has 0 Spatial geometry facts",
                        correction = "Publish exactly one geometry fact for this referenced Occurrence.",
                        projectionIds = projectionIds + geometries.map { geometry -> geometry.sheetId },
                        geometryIds = geometryIds,
                    ),
                )
            }
            if (selectedSheetIds.size == 1 && geometries.isNotEmpty()) {
                val ownerSheet = selectedSheetIds.single()
                if (selectedGeometries.isEmpty()) {
                    geometries.forEach { geometry ->
                        add(
                            diagnostic(
                                subject = "Occurrence ${endpoint.occurrenceId.value}",
                                problem = "has geometry on Sheet ${geometry.sheetId} but belongs to Sheet $ownerSheet",
                                correction = "Publish this Occurrence geometry only on its owning Sheet $ownerSheet.",
                                projectionIds = projectionIds + listOf(ownerSheet, geometry.sheetId),
                                geometryIds = geometryIds + listOfNotNull(owners.singleOrNull()?.originGeometryElementId),
                            ),
                        )
                    }
                } else if (selectedGeometries.size > 1) {
                    add(
                        diagnostic(
                            subject = "Occurrence ${endpoint.occurrenceId.value}",
                            problem = "has ${selectedGeometries.size} Spatial geometry facts",
                            correction = "Publish exactly one geometry fact for this referenced Occurrence.",
                            projectionIds = projectionIds + selectedGeometries.map { geometry -> geometry.sheetId },
                            geometryIds = geometryIds,
                        ),
                    )
                }
            }
            if (selectedSheetIds.size > 1) {
                selectedSheetIds.forEach { sheetId ->
                    val matches = selectedGeometries.filter { geometry -> geometry.sheetId == sheetId }
                    if (matches.size != 1) {
                        add(
                            diagnostic(
                                subject = "Occurrence ${endpoint.occurrenceId.value} on Sheet $sheetId",
                                problem = "has ${matches.size} Spatial geometry facts",
                                correction = "Publish exactly one geometry fact for this repeated Occurrence on Sheet $sheetId.",
                                projectionIds = projectionIds + sheetId,
                                geometryIds = geometryIds,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun owningSheets(node: ProjectionNode, sheets: List<ProjectionSheet>): List<ProjectionSheet> =
        sheets.filter { sheet ->
            sheet.subjects.any { subject ->
                if (subject.nodeIds.isNotEmpty()) node.projectionId in subject.nodeIds else subject.semanticId == node.semanticId
            }
        }

    private fun missingEndpointDiagnostic(connection: ProjectionConnection, role: String): SpatialDiagnostic =
        diagnostic(
            subject = "Connection ${connection.projectionId.value} $role endpoint",
            problem = "is missing typed Occurrence and port identity",
            correction = "Reference one projected occurrence-port as the $role endpoint.",
            projectionIds = listOf(connection.projectionId.value),
            geometryIds = listOf(connection.originGeometryElementId),
        )

    private fun endpointText(endpoint: ProjectionOccurrencePortId): String =
        "Port ${endpoint.portId.value} on Occurrence ${endpoint.occurrenceId.value}"
}

internal fun diagnostic(
    subject: String,
    problem: String,
    correction: String,
    projectionIds: List<String>,
    geometryIds: List<GeometryElementId>,
): SpatialDiagnostic = SpatialDiagnostic(
    subject = subject,
    problem = problem,
    correction = correction,
    sourceTrace = SpatialSourceTrace(
        projectionIds = projectionIds.filter(String::isNotBlank).distinct().ifEmpty { listOf("Projection identity unavailable") },
        geometryElementIds = geometryIds
            .filter { geometryId -> geometryId.value.isNotBlank() }
            .distinctBy { geometryId -> geometryId.value }
            .sortedBy { geometryId -> geometryId.value }
            .ifEmpty { listOf(GeometryElementId("Projection geometry origin unavailable")) },
    ),
)

internal fun List<SpatialDiagnostic>.canonicalDiagnostics(): List<SpatialDiagnostic> =
    groupBy { diagnostic -> Triple(diagnostic.subject, diagnostic.problem, diagnostic.correction) }
        .map { (identity, diagnostics) ->
            if (diagnostics.size == 1) return@map diagnostics.single()
            SpatialDiagnostic(
                subject = identity.first,
                problem = identity.second,
                correction = identity.third,
                sourceTrace = SpatialSourceTrace(
                    projectionIds = diagnostics
                        .flatMap { diagnostic -> diagnostic.sourceTrace.projectionIds }
                        .distinct()
                        .sortedWith(compareBy(::projectionIdentityRank, String::toString)),
                    geometryElementIds = diagnostics
                        .flatMap { diagnostic -> diagnostic.sourceTrace.geometryElementIds }
                        .distinctBy { geometryId -> geometryId.value }
                        .sortedBy { geometryId -> geometryId.value },
                ),
            )
        }
        .sortedWith(compareBy(SpatialDiagnostic::subject, SpatialDiagnostic::problem, SpatialDiagnostic::correction))

private fun projectionIdentityRank(identity: String): Int = when {
    "/sheet/" in identity || identity.startsWith("sheet:") -> 0
    identity.startsWith("occ") || "/occurrence/" in identity || identity.startsWith("projection/node") -> 1
    identity.startsWith("port:") -> 2
    identity.startsWith("connection:") || "/connection/" in identity -> 3
    else -> 4
}
