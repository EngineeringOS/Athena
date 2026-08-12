package com.engineeringood.athena.compiler

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.projection.ConnectionProjection
import com.engineeringood.athena.projection.ProjectionDocument
import com.engineeringood.athena.projection.ProjectionOccurrencePort
import com.engineeringood.athena.projection.ProjectionOccurrencePortId
import com.engineeringood.athena.spatial.SpatialAnchorId
import com.engineeringood.athena.spatial.SpatialAnchorPosition
import com.engineeringood.athena.spatial.SpatialBoundarySide
import com.engineeringood.athena.spatial.SpatialDiagnostic
import com.engineeringood.athena.spatial.SpatialOccurrenceGeometry
import com.engineeringood.athena.spatial.SpatialOccurrencePortSubject
import com.engineeringood.athena.spatial.SpatialPoint
import com.engineeringood.athena.spatial.SpatialSourceTrace
import java.util.Collections
import kotlin.math.abs

data class SpatialAnchorCompilationResult(
    val anchorPositions: List<SpatialAnchorPosition>,
    val diagnostics: List<SpatialDiagnostic> = emptyList(),
)

internal data class SpatialAnchorRequest(
    val endpoint: ProjectionOccurrencePortId,
    val geometry: SpatialOccurrenceGeometry,
    val portFact: ProjectionOccurrencePort,
    val side: SpatialBoundarySide,
    val incidentConnections: List<ConnectionProjection>,
    val sheetOrigin: GeometryElementId,
    val occurrenceOrigin: GeometryElementId,
) {
    fun traceGeometryIds(): List<GeometryElementId> =
        listOf(sheetOrigin, occurrenceOrigin, portFact.originGeometryElementId) +
            geometry.sourceTrace.geometryElementIds +
            incidentConnections.map(ConnectionProjection::originGeometryElementId)
}

class SpatialAnchorCompiler internal constructor(
    private val validator: SpatialAnchorValidator = SpatialAnchorValidator(),
) {
    fun compile(
        projection: ProjectionDocument,
        occurrences: List<SpatialOccurrenceGeometry>,
    ): SpatialAnchorCompilationResult {
        val diagnostics = validator.validate(projection, occurrences)
        if (diagnostics.isNotEmpty()) return failure(diagnostics)

        val requests = requests(projection, occurrences)
        val capacityDiagnostics = validator.validateCapacity(requests)
        if (capacityDiagnostics.isNotEmpty()) return failure(capacityDiagnostics)

        val sheetOrder = projection.sheets.associate { sheet -> sheet.sheetId.value to sheet.order }
        val anchors = requests
            .groupBy { request -> request.geometry.occurrenceId to request.side }
            .flatMap { (_, grouped) ->
                val ordered = grouped.sortedBy { request -> request.endpoint.portId.value }
                ordered.mapIndexed { index, request -> request.toAnchor(index, ordered.size) }
            }
            .sortedWith(
                compareBy<SpatialAnchorPosition>(
                    { anchor -> sheetOrder.getValue(anchor.sheetId) },
                    SpatialAnchorPosition::sheetId,
                    { anchor -> anchor.subject.occurrenceId.projectionId },
                    { anchor -> anchor.subject.portId.value },
                ),
            )
        return SpatialAnchorCompilationResult(Collections.unmodifiableList(anchors))
    }

    private fun requests(
        projection: ProjectionDocument,
        occurrences: List<SpatialOccurrenceGeometry>,
    ): List<SpatialAnchorRequest> {
        val incidents = projection.connections.flatMap { connection ->
            connection.participants.map { participant ->
                participant.endpoint.occurrencePortId to (connection to EndpointRole.PARTICIPANT)
            }
        }.groupBy(keySelector = { (endpoint, _) -> endpoint }, valueTransform = { (_, incident) -> incident })
        val connected = incidents.flatMap { (endpoint, endpointIncidents) ->
            val node = projection.nodes.single { candidate -> candidate.projectionId == endpoint.occurrenceId }
            val owningSheetIds = projection.sheets.filter { sheet ->
                sheet.subjects.any { subject ->
                    if (subject.nodeIds.isNotEmpty()) node.projectionId in subject.nodeIds else subject.semanticId == node.semanticId
                }
            }.map { sheet -> sheet.sheetId.value }
            val incidentsBySheet = endpointIncidents.groupBy { (connection, _) ->
                if (owningSheetIds.size == 1) {
                    owningSheetIds.single()
                } else {
                    projection.connectionSheetIds(connection).single { sheetId -> sheetId in owningSheetIds }
                }
            }
            incidentsBySheet.map { (sheetId, sheetIncidents) ->
                val geometry = occurrences.single { occurrence ->
                    occurrence.sheetId == sheetId &&
                        occurrence.occurrenceId.projectionId == endpoint.occurrenceId.value
                }
                val portFact = projection.occurrencePorts.single { port -> port.occurrencePortId == endpoint }
                val canonicalIncident = sheetIncidents.minBy { (connection, _) -> connection.projectionId.value }
                val peerEndpoint = canonicalIncident.first.participants
                    .map { participant -> participant.endpoint.occurrencePortId }
                    .first { candidate -> candidate != endpoint }
                val peerGeometries = occurrences.filter { occurrence ->
                    occurrence.occurrenceId.projectionId == peerEndpoint.occurrenceId.value
                }
                val peerGeometry = peerGeometries.singleOrNull { occurrence -> occurrence.sheetId == sheetId }
                    ?: peerGeometries.single()
                val sheet = projection.sheets.single { candidate -> candidate.sheetId.value == sheetId }
                SpatialAnchorRequest(
                    endpoint = endpoint,
                    geometry = geometry,
                    portFact = portFact,
                    side = preferredSide(geometry, peerGeometry),
                    incidentConnections = sheetIncidents.map { (connection, _) -> connection }
                        .distinctBy { connection -> connection.projectionId }
                        .sortedBy { connection -> connection.projectionId.value },
                    sheetOrigin = sheet.originGeometryElementId,
                    occurrenceOrigin = node.originGeometryElementId,
                )
            }
        }
        // Every package-backed semantic Port remains an interaction fact, even before it is
        // connected. This publishes a selectable anchor without creating a connection or route.
        val unconnected = projection.occurrencePorts
            .filterNot { port -> port.occurrencePortId in incidents }
            .flatMap { portFact ->
                val endpoint = portFact.occurrencePortId
                val node = projection.nodes.single { candidate -> candidate.projectionId == endpoint.occurrenceId }
                val owningSheetIds = projection.sheets.filter { sheet ->
                    sheet.subjects.any { subject ->
                        if (subject.nodeIds.isNotEmpty()) node.projectionId in subject.nodeIds else subject.semanticId == node.semanticId
                    }
                }.map { sheet -> sheet.sheetId.value }
                owningSheetIds.map { sheetId ->
                    val geometry = occurrences.single { occurrence ->
                        occurrence.sheetId == sheetId && occurrence.occurrenceId.projectionId == endpoint.occurrenceId.value
                    }
                    SpatialAnchorRequest(
                        endpoint = endpoint,
                        geometry = geometry,
                        portFact = portFact,
                        side = unconnectedSide(endpoint),
                        incidentConnections = emptyList(),
                        sheetOrigin = projection.sheets.single { sheet -> sheet.sheetId.value == sheetId }.originGeometryElementId,
                        occurrenceOrigin = node.originGeometryElementId,
                    )
                }
            }
        return connected + unconnected
    }

    private fun unconnectedSide(endpoint: ProjectionOccurrencePortId): SpatialBoundarySide = when (
        (endpoint.portId.value.hashCode() and Int.MAX_VALUE) % 4
    ) {
        0 -> SpatialBoundarySide.TOP
        1 -> SpatialBoundarySide.RIGHT
        2 -> SpatialBoundarySide.BOTTOM
        else -> SpatialBoundarySide.LEFT
    }

    private fun preferredSide(
        owner: SpatialOccurrenceGeometry,
        peer: SpatialOccurrenceGeometry,
    ): SpatialBoundarySide {
        val dx2 = doubledCenter(peer.rectangle.x, peer.rectangle.width) -
            doubledCenter(owner.rectangle.x, owner.rectangle.width)
        val dy2 = doubledCenter(peer.rectangle.y, peer.rectangle.height) -
            doubledCenter(owner.rectangle.y, owner.rectangle.height)
        if (dx2 == 0L && dy2 == 0L) {
            return SpatialBoundarySide.RIGHT
        }
        return if (abs(dx2) >= abs(dy2)) {
            if (dx2 > 0L) SpatialBoundarySide.RIGHT else SpatialBoundarySide.LEFT
        } else {
            if (dy2 > 0L) SpatialBoundarySide.BOTTOM else SpatialBoundarySide.TOP
        }
    }

    private fun SpatialAnchorRequest.toAnchor(index: Int, count: Int): SpatialAnchorPosition {
        val rectangle = geometry.rectangle
        val edgeLength = when (side) {
            SpatialBoundarySide.LEFT, SpatialBoundarySide.RIGHT -> rectangle.height
            SpatialBoundarySide.TOP, SpatialBoundarySide.BOTTOM -> rectangle.width
        }
        val offset = ((index.toLong() + 1L) * edgeLength.toLong() / (count.toLong() + 1L)).toInt()
        val point = when (side) {
            SpatialBoundarySide.LEFT -> SpatialPoint(rectangle.x, rectangle.y + offset)
            SpatialBoundarySide.RIGHT -> SpatialPoint(rectangle.right, rectangle.y + offset)
            SpatialBoundarySide.TOP -> SpatialPoint(rectangle.x + offset, rectangle.y)
            SpatialBoundarySide.BOTTOM -> SpatialPoint(rectangle.x + offset, rectangle.bottom)
        }
        val subject = SpatialOccurrencePortSubject(geometry.occurrenceId, endpoint.portId)
        val requiredProjectionIds = listOf(geometry.sheetId, endpoint.occurrenceId.value, endpoint.portId.value) +
            incidentConnections.map { connection -> connection.projectionId.value }
        val requiredSet = requiredProjectionIds.toSet()
        return SpatialAnchorPosition(
            anchorId = SpatialAnchorId(geometry.sheetId, geometry.occurrenceId, endpoint.portId),
            sheetId = geometry.sheetId,
            subject = subject,
            side = side,
            point = point,
            sourceTrace = SpatialSourceTrace(
                projectionIds = requiredProjectionIds.distinct() + geometry.sourceTrace.projectionIds
                    .filterNot(requiredSet::contains)
                    .distinct()
                    .sorted(),
                geometryElementIds = traceGeometryIds()
                    .distinctBy { geometryId -> geometryId.value }
                    .sortedBy { geometryId -> geometryId.value },
            ),
        )
    }

    private fun failure(diagnostics: List<SpatialDiagnostic>): SpatialAnchorCompilationResult =
        SpatialAnchorCompilationResult(anchorPositions = emptyList(), diagnostics = diagnostics)

    private enum class EndpointRole { PARTICIPANT }
}

internal fun ProjectionDocument.connectionSheetIds(connection: ConnectionProjection): List<String> =
    sheets.filter { sheet ->
        sheet.subjects.any { subject -> connection.projectionId in subject.connectionIds }
    }.map { sheet -> sheet.sheetId.value }.sorted()
