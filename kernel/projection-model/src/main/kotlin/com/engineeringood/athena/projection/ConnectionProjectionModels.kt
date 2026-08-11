package com.engineeringood.athena.projection

import com.engineeringood.athena.geometry.GeometryElementId
import com.engineeringood.athena.ir.ConnectionEndpointRole
import com.engineeringood.athena.ir.StableSemanticIdentity

/** Kind of canonical identity selected by one ConnectionProjection. */
enum class ConnectionProjectionIdentityKind { CONNECTION, NET }

/** Closed role for how a canonical connection/net is presented in one view. */
enum class ConnectionProjectionRole { CONNECTION, NET, CONTINUATION }

@JvmInline
value class LogicalRouteTargetId(val value: String) {
    init {
        require(value.isNotBlank() && '"' !in value && '\n' !in value && '\r' !in value) {
            "Logical route target identity must be a non-blank quoted-name-safe identity"
        }
    }
}

/** Logical Sheet coordinate. Values are compiler units, never viewport or renderer pixels. */
data class LogicalRoutePoint(val column: Int, val row: Int) {
    init {
        require(column > 0 && row > 0) { "Logical route coordinates must be positive" }
    }
}

/** Closed source-authored route intent. Physical geometry remains Spatial Reality. */
sealed interface LogicalRouteConstraint {
    val targetId: LogicalRouteTargetId

    data class Via(
        override val targetId: LogicalRouteTargetId,
        val point: LogicalRoutePoint,
    ) : LogicalRouteConstraint
}

/** Selected occurrence Port and its canonical endpoint role in one projection. */
data class ConnectionProjectionParticipant(
    val role: ConnectionEndpointRole,
    val endpoint: ConnectionProjectionEndpoint,
)

/** Coordinate-free provenance carried by one ConnectionProjection. */
data class ConnectionProjectionSourceTrace(
    val projectionIds: List<String>,
    val geometryElementIds: List<GeometryElementId>,
)

/** Coordinate-free selected occurrence Port reference. */
data class ConnectionProjectionEndpoint(
    val occurrencePortId: ProjectionOccurrencePortId,
)

/**
 * View-local projection of exactly one canonical Connection IR identity.
 *
 * This contract intentionally carries no physical coordinates, route points, renderer state, or paint
 * guidance. Geometry is derived only by later Spatial Reality stages.
 */
data class ConnectionProjection(
    val projectionId: ConnectionProjectionId,
    val semanticId: StableSemanticIdentity,
    val identityKind: ConnectionProjectionIdentityKind = ConnectionProjectionIdentityKind.CONNECTION,
    val role: ConnectionProjectionRole = ConnectionProjectionRole.CONNECTION,
    val originGeometryElementId: GeometryElementId,
    val sourceTrace: ConnectionProjectionSourceTrace = ConnectionProjectionSourceTrace(
        projectionIds = listOf(projectionId.value, semanticId.value),
        geometryElementIds = listOf(originGeometryElementId),
    ),
    val participants: List<ConnectionProjectionParticipant>,
    val logicalRouteConstraints: List<LogicalRouteConstraint> = emptyList(),
) {
    init {
        require(projectionId.value.isNotBlank()) { "Connection Projection identity must not be blank" }
        require(semanticId.value.isNotBlank()) { "Connection Projection canonical identity must not be blank" }
        require(participants.size >= 2) { "Connection Projection requires at least two selected occurrence Ports" }
        require(participants.map { it.endpoint.occurrencePortId }.distinct().size == participants.size) {
            "Connection Projection occurrence Ports must be distinct"
        }
        require(logicalRouteConstraints.map(LogicalRouteConstraint::targetId).distinct().size == logicalRouteConstraints.size) {
            "Connection Projection logical route targets must be unique"
        }
    }
}
